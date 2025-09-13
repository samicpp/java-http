package dev.samicpp.http

import java.net.SocketAddress

interface Socket{
    fun read(buffer: ByteArray): Int
    fun write(buffer: ByteArray)
    fun close()
    fun flush()
    fun address(): SocketAddress
}

