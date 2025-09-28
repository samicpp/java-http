package dev.samicpp.http

import dev.samicpp.http.Socket
import dev.samicpp.http.WebSocketFrame

import java.io.ByteArrayOutputStream
import java.util.concurrent.locks.ReentrantLock

const val MAGIC:String="258EAFA5-E914-47DA-95CA-C5AB0DC85B11"

class WebSocket(private val conn:Socket){
    private val readLock=ReentrantLock()
    private val writeLock=ReentrantLock()

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
    fun incoming():List<WebSocketFrame>{
        val buff=read_all()
        if(buff.size==0)return listOf()
        val list=mutableListOf<WebSocketFrame>(WebSocketFrame(buff))
        var last=list[0]
        while(last.remain.size!=0){
            last=WebSocketFrame(last.remain)
            list.add(last)
        }
        return list
    }
    fun available():Boolean{
        return conn.available()>0
    }
    fun isClosed()=conn.isClosed()
    private fun createFrame(fin:Boolean,opcode:Int,payload:ByteArray):ByteArray{
        val buff=mutableListOf(
            ((if(fin)0x80 else 0x0)or(opcode and 0xf)).toByte()
        )
        if(payload.size<126) {
            buff.add((payload.size and 0x7f).toByte())
        } else if(payload.size<65536) { // 0xffff
            buff.add(126)
            buff.add((payload.size shr 8).toByte())
            buff.add((payload.size and 0xff).toByte())
        } else {
            buff.add(127)
            buff.add((payload.size shr 56).toByte())
            buff.add((payload.size shr 48).toByte())
            buff.add((payload.size shr 40).toByte())
            buff.add((payload.size shr 32).toByte())
            buff.add((payload.size shr 24).toByte())
            buff.add((payload.size shr 16).toByte())
            buff.add((payload.size shr 8).toByte())
            buff.add(payload.size.toByte())
        }
        buff.addAll(payload.toList())

        return buff.toByteArray()
    }
    private fun sendFrame(fin:Boolean,opcode:Int,payload:ByteArray){
        writeLock.lock()
        conn.write(createFrame(fin, opcode, payload))
        writeLock.unlock()
    }


    fun sendText(data:ByteArray){sendFrame(true, 1, data)}
    fun sendText(data:String){sendFrame(true, 1, data.encodeToByteArray())}
    
    fun sendBinary(data:ByteArray){sendFrame(true, 2, data)}
    fun sendBinary(data:String){sendFrame(true, 2, data.encodeToByteArray())}
    
    fun sendPing(data:ByteArray){sendFrame(true, 9, data)}
    fun sendPing(data:String){sendFrame(true, 9, data.encodeToByteArray())}
    
    fun sendPong(data:ByteArray){sendFrame(true, 10, data)}
    fun sendPong(data:String){sendFrame(true, 10, data.encodeToByteArray())}
    
    fun sendClose(status:Int,reason:ByteArray){
        val buff=ByteArrayOutputStream().apply {
            write(status shr 8)
            write(status and 0xFF)
            write(reason)
        }

        sendFrame(true, 8, buff.toByteArray())
    }
    fun sendClose(status:Int,reason:String){sendClose(status, reason.encodeToByteArray())}

    fun close(){
        conn.flush()
        conn.close()
    }
}
