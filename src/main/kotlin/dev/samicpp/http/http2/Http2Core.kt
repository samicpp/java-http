package dev.samicpp.http
// https://datatracker.ietf.org/doc/html/rfc7540

import kotlin.byteArrayOf


data class Http2Frame(
    val frameSize:Int,
    
    val streamID:Int,
    val opcode:Int,
    val type:Http2FrameType,
    val stringType:String,
    val flags:Int,

    val payload:ByteArray,
    val padding:ByteArray,
    val priotity:ByteArray,
    val settings:Http2Settings,
){
    val length get()=payload.size
    val padLength get()=padding.size
    val prioLength get()=priotity.size
}

data class Http2Settings(
    val header_table_size:Int?,       //  0x1
    val enable_push:Int?,             //  0x2
    val max_concurrent_streams:Int?,  //  0x3
    val initial_window_size:Int?,     //  0x4
    val max_frame_size:Int?,          //  0x5
    val max_header_list_size:Int?,    //  0x6
)

fun parseHttp2Frame(buff: ByteArray):Pair<Http2Frame,ByteArray>{
    var start=9
    val length=((buff[0].toInt() and 0xff) shl 16) or ((buff[1].toInt() and 0xff) shl 8) or (buff[2].toInt() and 0xff)
    val opcode=buff[3].toInt() and 0xff
    val flags=buff[4].toInt() and 0xff
    val streamID=((buff[5].toInt() and 0xff) shl 24) or ((buff[6].toInt() and 0xff) shl 16) or 
                 ((buff[7].toInt() and 0xff) shl 8) or (buff[8].toInt() and 0xff)
    
    val padLength=
    if((flags and 0x8)!=0){ // padded
        start+=1
        buff[9].toInt() and 0xff
    } else { 0 }

    val frameLength=start+length+padLength

    // println("length = $length")

    val priority=
    if((flags and 0x20)!=0){
        buff.copyOfRange(start, start+5)
    } else { 
        ByteArray(0) 
    }

    val payload=
    if((flags and 0x20)==0)buff.copyOfRange(start, start+length)
    else buff.copyOfRange(start+5, start+length)

    val padding=buff.copyOfRange(start+length, start+length+padLength)

    val remaining=buff.copyOfRange(frameLength, buff.size)

    val (type,stringType)=when(opcode){
        0->Http2FrameType.Data to "Data"
        1->Http2FrameType.Headers to "Headers"
        2->Http2FrameType.Priority to "Priority"
        3->Http2FrameType.RstStream to "RstStream"
        4->Http2FrameType.Settings to "Settings"
        5->Http2FrameType.PushPromise to "PushPromise"
        6->Http2FrameType.Ping to "Ping"
        7->Http2FrameType.Goaway to "Goaway"
        8->Http2FrameType.WindowUpdate to "WindowUpdate"
        9->Http2FrameType.Continuation to "Continuation"
        else->Http2FrameType.Unknown to "Unkown"
    }

    val settings:Http2Settings=
    if(opcode==0x4){
        // var header_table_size:Int?=null
        // var enable_push:Int?=null
        // var max_concurrent_streams:Int?=null
        // var initial_window_size:Int?=null
        // var max_frame_size:Int?=null
        // var max_header_list_size:Int?=null
        // for(i in 0 until payload.size step 6){val it=payload.copyOfRange(i,6) // more efficient?
        // // buff.asList().chunked(6).forEach{
        //     val name=((it[0].toInt() and 0xff) shl 8) or (it[1].toInt() and 0xff)
        //     val value=((it[2].toInt() and 0xff) shl 24) or ((it[3].toInt() and 0xff) shl 16) or 
        //               ((it[4].toInt() and 0xff) shl 8) or (it[5].toInt() and 0xff)
        //     when(name){
        //         1->header_table_size=value
        //         2->enable_push=value
        //         3->max_concurrent_streams=value
        //         4->initial_window_size=value
        //         5->max_frame_size=value
        //         6->max_header_list_size=value
        //     }
        // }
        // Http2Settings(header_table_size, enable_push, max_concurrent_streams, initial_window_size, max_frame_size, max_header_list_size)
        parseHttp2Settings(payload)
    } else { 
        Http2Settings(null,null,null,null,null,null)
    }

    return Http2Frame(
        frameLength,
        streamID,
        opcode,
        type,
        stringType,
        flags,
        payload,
        padding,
        priority,
        settings,
    ) to 
    remaining
}

fun parseHttp2Settings(buff:ByteArray):Http2Settings{
    var header_table_size:Int?=null
    var enable_push:Int?=null
    var max_concurrent_streams:Int?=null
    var initial_window_size:Int?=null
    var max_frame_size:Int?=null
    var max_header_list_size:Int?=null
    for(i in 0 until buff.size step 6){ val it=buff.copyOfRange(i,i+6) // more efficient?
    // buff.asList().chunked(6).forEach{
        val name=((it[0].toInt() and 0xff) shl 8) or (it[1].toInt() and 0xff)
        val value=((it[2].toInt() and 0xff) shl 24) or ((it[3].toInt() and 0xff) shl 16) or 
                    ((it[4].toInt() and 0xff) shl 8) or (it[5].toInt() and 0xff)
        when(name){
            1->header_table_size=value
            2->enable_push=value
            3->max_concurrent_streams=value
            4->initial_window_size=value
            5->max_frame_size=value
            6->max_header_list_size=value
        }
    }
    return Http2Settings(header_table_size, enable_push, max_concurrent_streams, initial_window_size, max_frame_size, max_header_list_size)
}

//11.2 #autoid-88
enum class Http2FrameType{
    Data,          // 0x0
    Headers,       // 0x1
    Priority,      // 0x2
    RstStream,     // 0x3
    Settings,      // 0x4
    PushPromise,   // 0x5
    Ping,          // 0x6
    Goaway,        // 0x7
    WindowUpdate,  // 0x8
    Continuation,  // 0x9
    Unknown,       // >0x9
}
