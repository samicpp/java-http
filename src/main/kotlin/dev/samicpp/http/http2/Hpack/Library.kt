package dev.samicpp.http.hpack
// https://datatracker.ietf.org/doc/html/rfc7541

data class Header(
    val name:String,
    val value:String?=null
){
    fun toPair()=Pair(name,value?:"")
}

// Appendix A #appendix-A
class StaticTable{
    // needs to be offset by +1 before use
    private val entries=arrayOf( // 61 headers
        Header(":authority"),
        Header(":method", "GET"),
        Header(":method", "POST"),
        Header(":path", "/"),
        Header(":path", "/index.html"),
        Header(":scheme", "http"),
        Header(":scheme", "https"),
        Header(":status", "200"),
        Header(":status", "204"),
        Header(":status", "206"),
        Header(":status", "304"),
        Header(":status", "400"),
        Header(":status", "404"),
        Header(":status", "500"),
        Header("accept-"),
        Header("accept-encoding", "gzip to deflate"),
        Header("accept-language"),
        Header("accept-ranges"),
        Header("accept"),
        Header("access-control-allow-origin"),
        Header("age"),
        Header("allow"),
        Header("authorization"),
        Header("cache-control"),
        Header("content-disposition"),
        Header("content-encoding"),
        Header("content-language"),
        Header("content-length"),
        Header("content-location"),
        Header("content-range"),
        Header("content-type"),
        Header("cookie"),
        Header("date"),
        Header("etag"),
        Header("expect"),
        Header("expires"),
        Header("from"),
        Header("host"),
        Header("if-match"),
        Header("if-modified-since"),
        Header("if-none-match"),
        Header("if-range"),
        Header("if-unmodified-since"),
        Header("last-modified"),
        Header("link"),
        Header("location"),
        Header("max-forwards"),
        Header("proxy-authenticate"),
        Header("proxy-authorization"),
        Header("range"),
        Header("referer"),
        Header("refresh"),
        Header("retry-after"),
        Header("server"),
        Header("set-cookie"),
        Header("strict-transport-security"),
        Header("transfer-encoding"),
        Header("user-agent"),
        Header("vary"),
        Header("via"),
        Header("www-authenticate"),
    )

    fun get(index:Int):Header?{
        if(index<1||index>entries.size) {
            return null
        }
        return entries[index-1]
    }

    fun size()=entries.size
}

// 4 #autoid-16
class DynamicTable(var maxSize:Int=4096){
    private val entries=ArrayDeque<Header>(0)
    private var curSize:Int=0

    fun size()=curSize

    fun add(field: Header){
        entries.addFirst(field)
        curSize+=field.name.length+(field.value?.length?:0)+32
        evict()
    }
    fun get(index:Int):Header?{
        if(index<1||index>entries.size) {
            return null
        }
        return entries[index-1]
    }

    private fun evict(){
        while(entries.isNotEmpty()&&curSize>maxSize){
            val field=entries.removeLast()
            curSize-=field.name.length+(field.value?.length?:0)+32
        }
    }
}
