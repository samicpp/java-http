package dev.samicpp.http


import java.net.InetSocketAddress
import java.net.SocketAddress
import java.nio.file.Paths
import kotlin.collections.mutableListOf


class FakeHttpClient: HttpClient{
    override val address: SocketAddress = InetSocketAddress("0.0.0.0",0)
    override val headers: Map<String,List<String>> = mapOf("content-length" to listOf("0"))
    override val method: String = "NILL"
    override val version: String = "HTTP/0"
    override val path: String = "/"
    override val host: String = "about:blank"
    override val body: ByteArray = ByteArray(0)
    override val isReady: Boolean = true
    override val isComplete: Boolean = true
}

class FakeHttpSocket: HttpSocket{
    override val client: HttpClient = FakeHttpClient()

    override var status: Int = 200
    override var statusMessage: String = "OK"
    override var compression: Compression = Compression.None

    var closed=false
    var sentHead=false
    var canRead=true
    override val isClosed: Boolean get()= closed
    override val sentHeaders: Boolean get()= sentHead

    var headers = mutableMapOf<String,MutableList<String>>()
    override fun addHeader(name:String,value:String){
        val hs=headers[name]
        if (hs!=null){
            hs.add(value)
        } else {
            headers[name]=mutableListOf(value)
        }
    }
    override fun setHeader(name:String,value:String){
        headers[name]=mutableListOf(value)
    }
    override fun delHeader(name:String):List<String>{
        if(name in headers)return headers.remove(name)!!
        else return listOf()
    }
    override fun sendHead(){ sentHead=true }
    
    override fun close(){ closed=true; sentHead=true }
    override fun close(buffer:ByteArray)=close()
    override fun close(message:String)=close()

    override fun write(buff:ByteArray)=sendHead()
    override fun write(text:String)=sendHead()

    override fun readClient():HttpClient{
        canRead=false
        return client
    }
    override fun available():Boolean=canRead;
    
    override fun websocket():dev.samicpp.http.WebSocket=throw Exception("cannot start websocket in fake connection")

    override fun disconnect(){}
    override fun isHttps()=true
}
