package dev.samicpp.http

class Http2Connection(private val conn:Socket){
    fun getStream(streamID:Int):HttpSocket{
        return Http2Stream(streamID,this)
    }
}
