package dev.samicpp.http

import java.net.SocketAddress

interface Socket{
    fun read(buffer: ByteArray): Int;
    fun read(buffer: ByteArray, offset: Int, length: Int): Int;
    fun write(buffer: ByteArray);
    fun close();
    fun flush();
    fun available():Int;
    fun isClosed():Boolean;
    val remoteAddress: SocketAddress;
    fun isHttps(): Boolean;
}

interface HttpClient{
    val address: SocketAddress;
    val headers: Map<String,List<String>>;
    val method: String;
    val version: String;
    val path: String;
    val host: String;
    val body: ByteArray;
    
    val isReady: Boolean;  //  indicates whether headers can be used
    val isComplete: Boolean;
}

interface HttpSocket{
    val client: HttpClient;

    var status: Int;
    var statusMessage: String;
    var compression: Compression;

    val isClosed: Boolean;
    val sentHeaders: Boolean;

    fun addHeader(name:String,value:String);
    fun setHeader(name:String,value:String);
    fun delHeader(name:String):List<String>;
    fun sendHead();
    
    fun close();
    fun close(buffer:ByteArray);
    fun close(message:String);

    fun write(buff:ByteArray);
    fun write(text:String);
    fun flush();

    fun readClient():HttpClient;
    fun available():Boolean;
    
    fun websocket():WebSocket;

    fun disconnect();

    fun isHttps(): Boolean;
}

enum class Compression{
    None,
    Gzip
}

sealed class HttpError(message:String?=null):Throwable(message){
    class ConnectionClosed(msg:String?=null):HttpError(msg)
}
