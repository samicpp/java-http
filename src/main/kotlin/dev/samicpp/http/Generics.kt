package dev.samicpp.http

import java.net.SocketAddress

interface Socket{
    fun read(buffer: ByteArray): Int;
    fun write(buffer: ByteArray);
    fun close();
    fun flush();
    val remoteAddress: SocketAddress;
}

interface HttpClient{
    val headers: Map<String,String>;
    val method: String;
    val version: String;
    val path: String;
}
