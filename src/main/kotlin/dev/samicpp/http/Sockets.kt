package dev.samicpp.http
import dev.samicpp.http.Socket as IfaceSocket

import java.net.Socket
import java.net.SocketAddress
import java.net.ServerSocket
import java.io.OutputStream
import java.io.InputStream
import javax.net.ssl.SSLSocket

class TcpSocket(val tcp: Socket): IfaceSocket{ // also compatible with SSLSocket
    private val input:InputStream=tcp.getInputStream()
    private val output:OutputStream=tcp.getOutputStream()

    override fun read(buffer: ByteArray):Int{
        return input.read(buffer)
    }
    override fun read(buffer: ByteArray, offset: Int, length: Int):Int{
        return input.read(buffer,offset,length)
    }
    override fun write(buffer: ByteArray){
        return output.write(buffer)
    }
    override fun close(){
        input.close()
        output.close()
        return tcp.close()
    }
    override fun flush(){
        return output.flush()
    }
    override fun available():Int{
        return input.available()
    }
    override fun isClosed():Boolean{
        return tcp.isClosed()
    }
    override val remoteAddress=tcp.remoteSocketAddress
}

class TlsSocket(val tls: SSLSocket): IfaceSocket{
    private val input:InputStream=tls.getInputStream()
    private val output:OutputStream=tls.getOutputStream()

    override fun read(buffer: ByteArray):Int{
        return input.read(buffer)
    }
    override fun read(buffer: ByteArray, offset: Int, length: Int):Int{
        return input.read(buffer,offset,length)
    }
    override fun write(buffer: ByteArray){
        return output.write(buffer)
    }
    override fun close(){
        input.close()
        output.close()
        return tls.close()
    }
    override fun flush(){
        return output.flush()
    }
    override fun available():Int{
        return input.available()
    }
    override fun isClosed():Boolean{
        return tls.isClosed()
    }
    override val remoteAddress=tls.remoteSocketAddress
}
