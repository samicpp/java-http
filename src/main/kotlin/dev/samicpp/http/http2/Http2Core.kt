package dev.samicpp.http

import kotlin.byteArrayOf
// https://datatracker.ietf.org/doc/html/rfc7540

data class Http2Frame(
    val raw:ByteArray,
    val opcode:Int,
    val type:Http2FrameType,
    val stringType:String,
)

class Http2Settings(settingsBuffer:ByteArray){
    val header_table_size:Int? = 4_096
    val enable_push:Int? = 1
    val max_concurrent_streams:Int? = -1
    val initial_window_size:Int? = 65_535
    val max_frame_size:Int? = 16_384
    val max_header_list_size:Int? = -1

    init { 
        settingsBuffer.toString() // 😭
    }
}

fun parseHttp2Frame(buff: ByteArray):Pair<Http2Frame,ByteArray>{
    return Http2Frame(buff,0, Http2FrameType.Unknown, "") to ByteArray(0)
}

//11.2 #autoid-88
enum class Http2FrameType{
    Data,
    Headers,
    Priority,
    RstStream,
    Settings,
    PushPromise,
    Ping,
    Goaway,
    WindowUpdate,
    Continuation,
    Unknown,
}
