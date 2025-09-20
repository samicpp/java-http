package dev.samicpp.http


class Http2Stream(val streamID:Int,val conn:Http2Connection):HttpSocket{
    override val client: HttpClient=Http1Client()

    override var status: Int=200
    override var statusMessage: String=""
    override var compression: Compression=Compression.None

    override fun addHeader(name:String,value:String){}
    override fun setHeader(name:String,value:String){}
    override fun delHeader(name:String):List<String>{return listOf()}
    override fun sendHead(){}
    
    override fun close(){}
    override fun close(buffer:ByteArray){}
    override fun close(message:String){}

    override fun write(buff:ByteArray){}
    override fun write(text:String){}

    override fun readClient():HttpClient{return client}
    override fun available():Boolean{return false}
    
    override fun websocket():WebSocket{throw Exception("no")}

    override fun disconnect(){}
}
