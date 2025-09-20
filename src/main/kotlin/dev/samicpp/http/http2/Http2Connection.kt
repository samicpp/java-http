package dev.samicpp.http

import java.io.ByteArrayOutputStream
import java.util.concurrent.locks.ReentrantLock
import dev.samicpp.http.hpack.Encoder
import dev.samicpp.http.hpack.Decoder


sealed class Http2Error(msg:String?=null):HttpError(msg){
    class InvalidPreface(msg:String?=null):Http2Error(msg)
}

class Http2Connection(private val conn:Socket){
    private val readLock=ReentrantLock()
    private val writeLock=ReentrantLock()

    private val hpackEncoder=Encoder(4096)
    private val hpackDecoder=Decoder(4096)

    private val pre="PRI * HTTP/2.0\r\n\r\nSM\r\n\r\n"
    init{
        val preBuff=ByteArray(24)
        val read=conn.read(preBuff)
        val preface=preBuff.decodeToString()
        if(read<0)throw HttpError.ConnectionClosed()
        if(preface!=pre)throw Http2Error.InvalidPreface("Preface was $read")
    }
    private fun read_all():ByteArray{
        readLock.lock()
        var tot=ByteArrayOutputStream()
        var buff=ByteArray(4096)
        while(true){
            val read=conn.read(buff)
            if(read>0)tot.write(buff,0,read)
            if(read<buff.size)break
        }
        readLock.unlock()
        return tot.toByteArray()
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
                throw err
            }
        } while(remain.size!=0)
        return frames
    }

    fun getStream(streamID:Int):HttpSocket{
        return Http2Stream(streamID,this)
    }

    fun sendData(streamID:Int,payload:ByteArray,last:Boolean){
        send_frame(true, streamID, 1, if(last)0x1 else 0, payload, ByteArray(0))
    }
    // should ABSOLUTELY not be used under normal circumstances
    fun send_frame(lock:Boolean,streamID:Int,opcode:Int,flags:Int,payload:ByteArray,padding:ByteArray){
        val buff=ByteArrayOutputStream()
        if(lock)writeLock.lock()

        buff.writeBytes(byteArrayOf(
            (payload.size shl 16).toByte(),
            (payload.size shl 8).toByte(),
            (payload.size).toByte(),
        ))

        buff.write(opcode)
        buff.write(flags)

        buff.writeBytes(byteArrayOf( // write(ByteArray) also works
            (streamID shl 24).toByte(),
            (streamID shl 16).toByte(),
            (streamID shl 8).toByte(),
            (streamID).toByte(),
        ))

        if((flags and 0x20)!=0) buff.write(padding.size)

        buff.writeBytes(payload)

        if((flags and 0x20)!=0) buff.writeBytes(padding)

        conn.write(buff.toByteArray())
        if(lock)writeLock.unlock()
    }
}
