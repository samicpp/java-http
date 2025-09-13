package dev.samicpp.http
import dev.samicpp.http.Socket as IfaceSocket

import java.net.Socket
import java.net.SocketAddress
import java.net.ServerSocket
import java.io.OutputStream
import java.io.InputStream

class TcpSocket(private val tcp: Socket): IfaceSocket{
    private val input:InputStream=tcp.getInputStream()
    private val output:OutputStream=tcp.getOutputStream()

    override fun read(buffer: ByteArray):Int{
        return input.read(buffer)
    }
    override fun write(buffer: ByteArray){
        return output.write(buffer)
    }
    override fun close(){
        return tcp.close()
    }
    override fun flush(){
        return output.flush()
    }
    override val remoteAddress=tcp.remoteSocketAddress
}