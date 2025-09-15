package dev.samicpp.http

import kotlin.collections.mutableListOf
import kotlin.collections.mutableMapOf
import kotlin.byteArrayOf

import java.io.ByteArrayOutputStream
import java.util.zip.GZIPOutputStream
import java.util.zip.GZIPInputStream
import java.security.MessageDigest
import java.util.Base64

// same namespace imports unnecessary 
import dev.samicpp.http.Socket
import dev.samicpp.http.HttpClient
import dev.samicpp.http.Compression

fun compressGzip(buff:ByteArray):ByteArray {
    val bos=ByteArrayOutputStream()
    GZIPOutputStream(bos).use{ it.write(buff) }
    return bos.toByteArray()
}

fun decompressGzip(buff:ByteArray):ByteArray {
    return GZIPInputStream(buff.inputStream()).readBytes()
}


class Http1Socket(private val conn:Socket):HttpSocket{
    private val headers=mutableMapOf<String,MutableList<String>>( "Connection" to mutableListOf("close") )
    private var closed=false
    private var head_closed=false
    private val _client=Http1Client()
    
    override val client: HttpClient get()=_client

    // tx
    override var status: Int = 200 // unsafe to use UShort
    override var statusMessage: String = "OK"
    override var compression:Compression=Compression.None
    
    override fun addHeader(name:String,value:String){
        if(head_closed)return
        if(name.startsWith(":"))return
        when(name.lowercase()){
            "connection","content-length","transfer-encoding","content-encoding",
            ->return;
        }

        val hs=headers[name]
        if (hs!=null){ // can use ?.let but i dont like it
            hs.add(value)
        } else {
            headers[name]=mutableListOf(value)
        }
    }
    override fun setHeader(name:String,value:String){
        if(head_closed)return
        if(name.startsWith(":"))return
        when(name.lowercase()){
            "connection","content-length","transfer-encoding","content-encoding",
            ->return;
        }

        headers[name]=mutableListOf(value)
    }
    override fun delHeader(name:String):List<String>{
        if(head_closed)return listOf()
        if(name in headers)return headers.remove(name)!!
        else return listOf()
    }

    override fun sendHead(){
        if (!head_closed){
            var totalHead=StringBuilder()
            for((header,values) in headers)for(value in values){
                totalHead.append("$header: $value\r\n")
            }
            val total="HTTP/1.1 $status $statusMessage\r\n$totalHead\r\n"
            val btotal=total.encodeToByteArray()
            conn.write(btotal)
            head_closed=true
        }
    }

    override fun close(){
        if(!closed&&!head_closed){
            setHeader("Content-Length", "0")
            sendHead()
            closed=true
        } else if (!closed){
            conn.write(byteArrayOf(48,13,10,13,10)) // 0\r\n\r\n
        }
    }
    override fun close(buffer:ByteArray){
        if(!closed&&!head_closed){
            val data=when(compression){
                Compression.Gzip->{
                    headers["Content-Encoding"]=mutableListOf("gzip")
                    compressGzip(buffer)
                }
                else->buffer
            }
            headers["Content-Length"]=mutableListOf(data.size.toString())
            sendHead()
            conn.write(data)
            closed=true
        }
    }
    override fun close(message:String){
        if(!closed){
            close(message.encodeToByteArray())
        }
    }

    private fun write_chunk(buff:ByteArray){
        val tot=buff.size.toString(16).encodeToByteArray()+
        byteArrayOf(13,10)+
        buff+byteArrayOf(13,10)
        conn.write(tot)
    }

    override fun write(buff:ByteArray){
        if(!closed&&buff.count()>0){
            if(!head_closed){
                // when(compression){
                //     Compression.Gzip->headers["Content-Encoding"]=mutableListOf("gzip"), 
                //     else->{}
                // }
                headers["Transfer-Encoding"]=mutableListOf("chunked")
                sendHead()
            }
            // val data=when(compression){
            //     Compression.Gzip->compressGzip(buff)
            //     else->buff
            // }
            write_chunk(buff)
        }
    }
    override fun write(text:String){
        if(!closed)write(text.encodeToByteArray())
    }

    // rx
    private fun splitHead(buff:ByteArray):Pair<ByteArray,ByteArray>{
        for(i in 0..<(buff.size-3)){
            // println("${buff[i+0]}${buff[i+0]==13.toByte()}  ${buff[i+1]}${buff[i+1]==10.toByte()}  ${buff[i+2]}${buff[i+2]==13.toByte()}  ${buff[i+3]}${buff[i+3]==10.toByte()}")
            if(
                buff[i+0]==13.toByte()&&
                buff[i+1]==10.toByte()&&
                buff[i+2]==13.toByte()&&
                buff[i+3]==10.toByte()
            ) {
                val head=buff.copyOfRange(0,i)
                val body=if(buff.size>i+3){
                    buff.copyOfRange(i+4,buff.size)
                } else {
                    ByteArray(0)
                }
                return head to body
            }
        }
        return ByteArray(0) to ByteArray(0)
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
    override fun readClient():HttpClient{
        val buff=read_all()
        val (bhead,body)=splitHead(buff)
        val head=bhead.decodeToString()
        
        // println("buff[${buff.size}] = \"${buff.decodeToString()}\"")
        // print("buff = [ ")
        // for(i in buff)print("$i ")
        // println("]")
        // println("bhead[${bhead.size}]\nbody[${body.size}]")

        val lines=head.split("\r\n")
        val mpv=lines[0].split(" ")
        
        _client._method=mpv[0]
        _client._path=mpv[1]
        _client._version=mpv[2]

        for (i in 1..<lines.size){
            val (header,value)=lines[i].split(":",limit=2)
            if(header in _client._headers){ // header in client._headers
                _client._headers[header.lowercase()]?.add(value.trim())
            } else {
                _client._headers[header.lowercase()]=mutableListOf(value.trim())
            }
        }
        _client._body=body

        return _client
    }


    override fun websocket():WebSocket{
        val key = client.headers["sec-websocket-key"]?.get(0)
        if(key is String){
            val sha1=MessageDigest.getInstance("sha-1")
            val hash=sha1.digest((key+MAGIC).encodeToByteArray())
            val base=Base64.getEncoder().encodeToString(hash)
            
            val acce="HTTP/1.1 101 Switching Protocols\r\nUpgrade: websocket\r\nConnection: Upgrade\r\nSec-WebSocket-Accept: $base\r\n\r\n"
            conn.write(acce.encodeToByteArray())
            closed=true
            head_closed=true
            return WebSocket(conn)
        } else {
            throw Error("no websocket key")
        }

    }
}

class Http1Client():HttpClient{
    internal var _headers=mutableMapOf<String,MutableList<String>>()
    internal var _method=""
    internal var _version=""
    internal var _path=""
    internal var _body=ByteArray(0)

    override val headers: Map<String,List<String>> get()=_headers
    override val method: String get()=_method
    override val version: String get()=_version
    override val path: String get()=_path
    override val body: ByteArray get()=_body
}

// fun ByteArray.indexOfSequence(sequence:ByteArray):Int{
//     outer@ 
//     for(i in 0..this.size-sequence.size){
//         for (j in sequence.indices) {
//             if (this[i + j]!=sequence[j])continue@outer
//         }
//         return i
//     }
//     return -1
// }
