package dev.samicpp.http

enum class WebSocketFrameType{
    Continuation,         
    Text,                 
    Binary,               
    ConnectionClose,      
    Ping,                 
    Pong,                 
    Other,
}

class WebSocketFrame(val raw: ByteArray){
    val final:Boolean=(raw[0].toInt() and 0x80)!=0
    val rsv:Int=raw[0].toInt() and 0x70
    val opcode:Int=raw[0].toInt() and 0xf

    private val len:Byte=(raw[1].toInt() and 0x7f).toByte()
    private val extLen:Long
    val length:Long get(){
        if(extLen!=0.toLong())return extLen
        else return len.toLong()
    }

    val mask:Boolean=(raw[1].toInt() and 0x80)!=0

    val maskKey:ByteArray

    val payload:ByteArray
    val remain:ByteArray

    val type:WebSocketFrameType
    init{
        var offset=2

        if(len.toInt()==126){
            offset+=2
            extLen=(raw[2].toLong() shl 8) or raw[3].toLong()
        }else if(len.toInt()==127){
            offset+=8
            extLen=(raw[2].toLong() shl 56) or (raw[3].toLong() shl 48) or (raw[4].toLong() shl 40) or (raw[5].toLong() shl 32) or
            (raw[6].toLong() shl 24) or (raw[7].toLong() shl 16) or (raw[8].toLong() shl 8) or raw[9].toLong()
        }else{
            extLen=0
        }

        if(mask){
            maskKey=raw.copyOfRange(offset, offset+4)
            offset+=4
        } else {
            maskKey=ByteArray(0)
        }

        payload=raw.copyOfRange(offset,offset+length.toInt())

        if(mask){
            for(i in 0..<payload.size){
                payload[i]=(payload[i].toInt() xor maskKey[i%4].toInt()).toByte()
            }
        }

        when(opcode){
            0->type=WebSocketFrameType.Continuation;
            1->type=WebSocketFrameType.Text;
            2->type=WebSocketFrameType.Binary;
            8->type=WebSocketFrameType.ConnectionClose;
            9->type=WebSocketFrameType.Ping;
            10->type=WebSocketFrameType.Pong;
            else->type=WebSocketFrameType.Other;
        }

        remain=raw.copyOfRange(offset+length.toInt(), raw.size)
    }
}
