package dev.samicpp.http

import java.io.ByteArrayOutputStream


sealed class Http2Error(msg:String?=null):HttpError(msg){
    class InvalidPreface(msg:String?=null):Http2Error(msg)
}

class Http2Connection(private val conn:Socket){
    private val pre="PRI * HTTP/2.0\r\n\r\nSM\r\n\r\n"
    init{
        val preBuff=ByteArray(24)
        val read=conn.read(preBuff)
        val preface=preBuff.decodeToString()
        if(read<0)throw HttpError.ConnectionClosed()
        if(preface!=pre)throw Http2Error.InvalidPreface("Preface was $read")
    }
    private fun read_all():ByteArray{
        var tot=ByteArrayOutputStream()
        var buff=ByteArray(4096)
        while(true){
            val read=conn.read(buff)
            if(read>0)tot.write(buff,0,read)
            if(read<buff.size)break
        }
        return tot.toByteArray()
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
}
