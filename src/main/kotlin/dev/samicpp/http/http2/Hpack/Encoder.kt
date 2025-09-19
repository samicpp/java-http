package dev.samicpp.http.hpack

class Encoder(val size:Int=4096){
    private val static=StaticTable()
    private val dynamic=DynamicTable(size)

}
