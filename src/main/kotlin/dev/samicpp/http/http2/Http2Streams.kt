package dev.samicpp.http

import dev.samicpp.http.Http2Client
import java.net.SocketAddress


data class Http2Client(
    val head:List<Pair<String,String>>,
    override val body:ByteArray,
    override val address:SocketAddress,
    override val isComplete: Boolean,
    ):HttpClient{
    override val headers: Map<String,List<String>>
    override val method: String
    override val version: String="HTTP/2"
    override val path: String
    override val host: String
    override val isReady: Boolean
    init{
        val headers=mutableMapOf<String,MutableList<String>>()
        var method="GET"
        var path="/"
        var host="about:blank"
        for((header,value) in head){
            if(header==":method"){
                method=value
            } else if(header==":path") {
                path=value
            } else if(header==":authority") {
                host=value
            } else if(!header.startsWith(":")) {
                if(header in headers)headers[header]!!.add(value)
                else headers[header]=mutableListOf(value)
            }
        }

        this.path=path
        this.host=host
        this.method=method
        this.headers=headers
        this.isReady=head.size>0
    }
}

// TODO: make sure headers dont exceed SETTINGS_MAX_HEADER_LIST_SIZE

class Http2Stream(val streamID:Int,val conn:Http2Connection):HttpSocket{
    override var client: HttpClient=Http2Client(listOf(),ByteArray(0),conn.remoteAddress,false)

    private var sentHead=false
    private var closed=false
    private val headers=mutableMapOf<String,MutableList<String>>()

    override var status: Int=200
    override var statusMessage: String=""
    override var compression: Compression=Compression.None

    override val isClosed get()=closed
    override val sentHeaders get()=sentHead

    override fun addHeader(name:String,value:String){
        val lname=name.lowercase()
        if(sentHead)return
        if(lname.startsWith(":"))return
        when(lname.lowercase()){
            "connection","content-length","transfer-encoding","content-encoding",
            ->return;
        }
        val hs=headers[lname]
        if (hs!=null){
            hs.add(value)
        } else {
            headers[lname]=mutableListOf(value)
        }
    }
    override fun setHeader(name:String,value:String){
        val lname=name.lowercase()
        if(sentHead)return
        if(lname.startsWith(":"))return
        when(lname){
            "connection","content-length","transfer-encoding","content-encoding",
            ->return;
        }
        headers[lname]=mutableListOf(value)
    }
    override fun delHeader(name:String):List<String>{
        val lname=name.lowercase()
        if(sentHead)return listOf()
        if(lname in headers)return headers.remove(lname)!!
        else return listOf()
    }
    override fun sendHead(){
        if(!sentHead){
            var headerList=mutableListOf<Pair<String,String>>( ":status" to status.toString() )
            for((header,values) in headers){
                for(value in values)headerList.add(header to value)
            }
            conn.sendHeaders(streamID, headerList)
            sentHead=true
        }
    }
    
    override fun close(){
        if(!closed){
            if(!sentHead){
                headers["content-length"]=mutableListOf("0")
                sendHead()
            }
            conn.sendData(streamID, ByteArray(0), true)
            closed=true
        }
    }
    override fun close(buffer:ByteArray){
        if(!closed){
            if(!sentHead&&compression==Compression.Gzip){
                val compressed=compressGzip(buffer)
                headers["content-encoding"]=mutableListOf("gzip")
                headers["content-length"]=mutableListOf(compressed.size.toString())
                sendHead()
                conn.sendData(streamID, compressed, true)
                closed=true
            } else {
                if(!sentHead){
                    headers["content-length"]=mutableListOf(buffer.size.toString())
                    sendHead()
                }
                conn.sendData(streamID, buffer, true)
                closed=true
            }
        }
    }
    override fun close(message:String)=close(message.encodeToByteArray())

    override fun write(buff:ByteArray){
        if(buff.isEmpty())return
        if(!closed){
            if(!sentHead)sendHead()
            conn.sendData(streamID, buff, false)
            closed=true
        }
    }
    override fun write(text:String)=write(text.encodeToByteArray())

    override fun readClient():HttpClient{
        val nclient=Http2Client(
            conn.streamData[streamID]!!.headers,
            conn.streamData[streamID]!!.body.toByteArray(),
            conn.remoteAddress,
            conn.streamData[streamID]!!.end,
        )
        client=nclient
        return client
    }
    override fun available():Boolean{return conn.available()}
    
    override fun websocket():WebSocket{
        conn.sendRstStream(streamID, 0xd)
        throw Http2Error.Unsupported("Cannot use WebSocket in a HTTP/2 connection")
    }

    override fun disconnect(){
        conn.sendRstStream(streamID, 7)
        closed=true
    }
    override fun isHttps()=conn.isHttps()
}
