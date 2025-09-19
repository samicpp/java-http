package dev.samicpp.http.hpack

class Decoder(val size:Int=4096){
    private val static=StaticTable()
    private val dynamic=DynamicTable(size)
}
