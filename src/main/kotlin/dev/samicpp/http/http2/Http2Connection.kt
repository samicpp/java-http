package dev.samicpp.http

import java.io.ByteArrayOutputStream
import java.util.concurrent.locks.ReentrantLock
import dev.samicpp.http.hpack.Encoder
import dev.samicpp.http.hpack.Decoder


sealed class Http2Error(msg:String?=null):HttpError(msg){
    class InvalidPreface(msg:String?=null):Http2Error(msg);
    class Unsupported(msg:String?=null):Http2Error(msg);
    class MalformedFrame(msg:String?=null):Http2Error(msg);
}

internal data class StreamData(
    var end: Boolean=false,
    var closed: Boolean=false,
    var windowSize: Int=65535,
    var headers:List<Pair<String, String>> =listOf(),
    var headBuff:ByteArrayOutputStream=ByteArrayOutputStream(),
    var body:ByteArrayOutputStream=ByteArrayOutputStream(),
)

class Http2Connection(
    private val conn:Socket,
    var settings:Http2Settings=Http2Settings(4096,1,null,65535,16384,null),
    ){
    private val sendLock=ReentrantLock()
    private val readLock=ReentrantLock()
    private val writeLock=ReentrantLock()
    private val streamLock=ReentrantLock()
    private val hpackeLock=ReentrantLock()
    private val hpackdLock=ReentrantLock()

    private val hpackEncoder=Encoder(4096)
    private val hpackDecoder=Decoder(4096)

    internal val streamData:MutableMap<Int,StreamData> =mutableMapOf()
    internal var windowSize:Int=settings.initial_window_size?:65535

    private var maxStreamID:Int=0
    private var goaway:Boolean=false

    private val maxFrameSize:Int get()=settings.max_frame_size?:16384

    fun hpackEncode(headers: List<Pair<String, String>>):ByteArray{
        hpackeLock.lock()
        val buff=hpackEncoder.encode(headers)
        hpackeLock.unlock()
        return buff
    }
    fun hpackDecode(block:ByteArray):List<Pair<String,String>>{
        hpackdLock.lock()
        val headers=hpackDecoder.decode(block).map{it.toPair()}
        hpackdLock.unlock()
        return headers
    }

    val pre="PRI * HTTP/2.0\r\n\r\nSM\r\n\r\n"
    init{
        val preBuff=ByteArray(24)
        val read=conn.read(preBuff)
        val preface=preBuff.decodeToString()
        if(read<0)throw HttpError.ConnectionClosed()
        if(preface!=pre)throw Http2Error.InvalidPreface("Preface was $read")
    }
    private fun read_all(lock:Boolean=true):ByteArray{
        if(!available())return ByteArray(0)
        if(lock)readLock.lock()
        var tot=ByteArrayOutputStream()
        var buff=ByteArray(4096)
        while(true){
            val read=conn.read(buff)
            if(read>0)tot.write(buff,0,read)
            if(read<buff.size)break
        }
        if(lock)readLock.unlock()
        return tot.toByteArray()
    }
    fun read_one(lock:Boolean=true):Http2Frame{
        val buff=ByteArrayOutputStream()
        if(lock)readLock.lock()
        
        var offset=9

        val head=ByteArray(9)
        conn.read(head)
        buff.writeBytes(head)

        val length=((head[0].toInt() and 0xff) shl 16) or ((head[1].toInt() and 0xff) shl 8) or (head[2].toInt() and 0xff)
        val padLength=
        if((head[4].toInt() and 0x8)!=0) {
            offset+=1
            val one=ByteArray(0)
            conn.read(one)
            buff.writeBytes(one)
            one[0].toInt() and 0xff
        }
        else 0 

        val payload=ByteArray(length)
        val padding=ByteArray(padLength)
        conn.read(payload)
        conn.read(padding)
        buff.writeBytes(payload)
        buff.writeBytes(padding)

        if(lock)readLock.unlock()
        return parseHttp2Frame(buff.toByteArray()).first // nothing should remain
    }
    fun available():Boolean{
        return conn.available()>0
    }

    fun incoming():List<Http2Frame>{
        val frames=mutableListOf<Http2Frame>()
        var remain:ByteArray=read_all()
        
        // print("remain = [ ")
        // remain.forEach { print("${it.toInt() and 0xff} ") }
        // println("]")

        do{
            try{
                val pout=parseHttp2Frame(remain)
                frames.add(pout.first)
                remain=pout.second
            } catch(err:Throwable){
                // throw err
            }
        } while(remain.size!=0)
        return frames
    }

    fun handle(frames:List<Http2Frame>):List<Int>{
        streamLock.lock()
        val created=mutableListOf<Int>()
        check@
        for(frame in frames){
            try{
                when(frame.type){
                    Http2FrameType.Headers->{
                        if(goaway)continue@check
                        if(frame.streamID in streamData)continue@check
                        val data=StreamData()
                        data.headBuff.writeBytes(frame.payload)

                        streamData[frame.streamID]=data
                        created.add(frame.streamID)

                        if((frame.flags and 4)!=0)data.headers=hpackDecode(data.headBuff.toByteArray())
                        if((frame.flags and 1)!=0)data.end=true

                        if(maxStreamID<frame.streamID) maxStreamID=frame.streamID // should always be true
                    }
                    Http2FrameType.Continuation->{
                        if(frame.streamID !in streamData)continue@check
                        val data=streamData[frame.streamID]!!
                        data.headBuff.writeBytes(frame.payload)
                        if((frame.flags and 4)!=0)data.headers=hpackDecode(data.headBuff.toByteArray())
                        if((frame.flags and 1)!=0)data.end=true
                    }
                    Http2FrameType.Data->{
                        if(frame.streamID !in streamData)continue@check
                        val data=streamData[frame.streamID]!!
                        data.body.writeBytes(frame.payload)
                        if((frame.flags and 1)!=0)data.end=true
                        sendWindowUpdate(frame.streamID, frame.length)
                    }
                    Http2FrameType.Settings->{
                        if((frame.flags and 1)!=0||frame.streamID!=0)continue@check
                        val sett=frame.settings
                        val newSett=Http2Settings(
                            header_table_size       = sett.header_table_size?:settings.header_table_size,
                            enable_push             = sett.enable_push?:settings.enable_push,
                            max_concurrent_streams  = sett.max_concurrent_streams?:settings.max_concurrent_streams,
                            initial_window_size     = sett.initial_window_size?:settings.initial_window_size,
                            max_frame_size          = sett.max_frame_size?:settings.max_frame_size,
                            max_header_list_size    = sett.max_header_list_size?:settings.max_header_list_size,
                        )

                        if(newSett.header_table_size!=null)hpackEncoder.updateDynamicTableSize(newSett.header_table_size)

                        settings=newSett
                        sendSettingsAck()
                    }
                    Http2FrameType.Goaway->goaway=true
                    Http2FrameType.RstStream->streamData[frame.streamID]!!.closed=true
                    Http2FrameType.WindowUpdate->{
                        val update=((frame.payload[0].toInt() and 0xff) shl 24) or ((frame.payload[1].toInt() and 0xff) shl 16) or 
                                   ((frame.payload[2].toInt() and 0xff) shl 8) or (frame.payload[3].toInt() and 0xff)
                        if(frame.streamID==0)windowSize+=update
                        else streamData[frame.streamID]!!.windowSize+=update
                    }
                    Http2FrameType.Ping->{
                        if((frame.flags and 1)!=0)continue@check
                        sendPing(frame.payload)
                    }

                    else->{}
                }
            } catch(err: Throwable){
                ;
            }
        }
        streamLock.unlock()
        return created
    }

    fun getStream(streamID:Int):HttpSocket{
        // TODO: actually make stream class
        return Http2Stream(streamID,this)
    }

    fun sendData(streamID:Int,payload:ByteArray,last:Boolean){
        sendLock.lock()
        val stream=streamData[streamID]!!
        var remain=payload
        var min=if(windowSize>stream.windowSize)
            minOf(stream.windowSize, maxFrameSize)
        else
            minOf(windowSize, maxFrameSize)

        while(min<remain.size){
            var toSend=remain.copyOfRange(0, min)
            remain=remain.copyOfRange(min,remain.size)

            if(min>0)send_frame(true, streamID, 0, 0, toSend, ByteArray(0))
            stream.windowSize-=toSend.size
            handle(listOf(read_one()))

            min=if(windowSize>stream.windowSize)
                minOf(stream.windowSize, maxFrameSize)
            else
                minOf(windowSize, maxFrameSize)
        }
        send_frame(true, streamID, 0, if(last) 1 else 0, remain, ByteArray(0))
        sendLock.unlock()
    }
    fun sendHeaders(streamID:Int,headers:List<Pair<String,String>>,endStream:Boolean=false){
        sendLock.lock()
        val payload=hpackEncode(headers)
        if(payload.size>maxFrameSize){
            send_frame(true, streamID, 1, 0, payload.copyOfRange(0, maxFrameSize), ByteArray(0))
            var remain=payload.copyOfRange(maxFrameSize, payload.size)
            while(remain.size>maxFrameSize){
                send_frame(true, streamID, 9, 0, remain.copyOfRange(0, maxFrameSize), ByteArray(0))
                remain=remain.copyOfRange(maxFrameSize, remain.size)
            }
            send_frame(true, streamID, 9, if(endStream) 5 else 4, remain, ByteArray(0))
        } else{
            send_frame(true, streamID, 1, if(endStream) 5 else 4, payload, ByteArray(0))
        }
        sendLock.unlock()
    }
    fun sendSettings(settings:Http2Settings){
        val payload=settings.toBuffer()
        send_frame(true, 0, 4, 0, payload, ByteArray(0))
    }
    fun sendSettingsAck() {
        send_frame(true, 0, 4, 1, ByteArray(0), ByteArray(0))
    }
    fun sendWindowUpdate(streamID:Int,size:Int){
        val payload=byteArrayOf(
            (size shr 24).toByte(),
            (size shr 16).toByte(),
            (size shr 8).toByte(),
            size.toByte()
        )
        send_frame(true, streamID, 8, 0, payload, ByteArray(0))
    }
    fun sendPing(payload:ByteArray){
        send_frame(true, 0, 6, 0, payload, ByteArray(0))
    }
    fun sendPong(payload:ByteArray){
        send_frame(true, 0, 6, 1, payload, ByteArray(0))
    }
    fun sendGoaway(error:Int,message:ByteArray){
        val payload=ByteArrayOutputStream()
        payload.writeBytes(byteArrayOf(
            (error shr 24).toByte(),
            (error shr 16).toByte(),
            (error shr 8).toByte(),
            error.toByte()
        ))
        payload.writeBytes(message)
        send_frame(true, 0, 7, 0, payload.toByteArray(), ByteArray(0))
        goaway=true
    }
    fun sendRstStream(streamID:Int,error:Int){
        val payload=byteArrayOf(
            (error shr 24).toByte(),
            (error shr 16).toByte(),
            (error shr 8).toByte(),
            error.toByte()
        )
        send_frame(true, streamID, 3, 0, payload, ByteArray(0))
        streamData[streamID]!!.closed=true
    }

    // should ABSOLUTELY not be used under normal circumstances
    fun send_frame(lock:Boolean,streamID:Int,opcode:Int,flags:Int,payload:ByteArray,padding:ByteArray){
        val buff=ByteArrayOutputStream()
        if(lock)writeLock.lock()

        buff.writeBytes(byteArrayOf(
            (payload.size shr 16).toByte(),
            (payload.size shr 8).toByte(),
            (payload.size).toByte(),
        ))

        buff.write(opcode)
        buff.write(flags)

        buff.writeBytes(byteArrayOf( // write(ByteArray) also works
            (streamID shr 24).toByte(),
            (streamID shr 16).toByte(),
            (streamID shr 8).toByte(),
            (streamID).toByte(),
        ))

        if((flags and 0x20)!=0) buff.write(padding.size)

        buff.writeBytes(payload)

        if((flags and 0x20)!=0) buff.writeBytes(padding)

        conn.write(buff.toByteArray())
        if(lock)writeLock.unlock()
    }
}
