package dev.samicpp.http.hpack

import java.nio.charset.StandardCharsets
import java.lang.IllegalStateException


class Decoder(val size:Int=4096){
    private val static=StaticTable()  // static.get(Int): Header(val name:String,val value:String?=null)
    private val dynamic=DynamicTable(size)  // dynamic.add(Header); dynamic.get(Int): Header
    private val huffman=HuffmanDecoder(HuffmanTable())  // huffman.decode(ByteArray): ByteArray; huffman.encode(ByteArray): ByteArray;

    
    fun decode(block:ByteArray):List<Header>{
        val out=mutableListOf<Header>()
        val posRef=intArrayOf(0)

        while(posRef[0]<block.size){
            val b0=block[posRef[0]].toInt() and 0xFF

            when{
                (b0 and 0x80)!=0->{
                    val index=readInteger(block,posRef,7)
                    val h=getHeaderFromIndex(index)
                    out.add(Header(h.name,h.value?:""))
                }

                (b0 and 0xC0)==0x40->{
                    val nameIndex=readInteger(block,posRef,6)
                    val name=if(nameIndex==0) readString(block,posRef) else getHeaderNameFromIndex(nameIndex)
                    val value=readString(block,posRef)
                    val header=Header(name,value)
                    dynamic.add(header)
                    out.add(header)
                }

                (b0 and 0xE0)==0x20->{
                    val newSize=readInteger(block,posRef,5)
                    updateDynamicTableSize(newSize)
                }

                (b0 and 0xF0)==0x00->{
                    val nameIndex=readInteger(block,posRef,4)
                    val name=if(nameIndex==0) readString(block,posRef) else getHeaderNameFromIndex(nameIndex)
                    val value=readString(block,posRef)
                    out.add(Header(name,value))
                }

                (b0 and 0xF0)==0x10->{
                    val nameIndex=readInteger(block,posRef,4)
                    val name=if(nameIndex == 0) readString(block,posRef) else getHeaderNameFromIndex(nameIndex)
                    val value=readString(block,posRef)
                    out.add(Header(name,value))
                }

                else->{
                    throw IllegalStateException("Unrecognized header field representation at pos ${posRef[0]} (byte=0x${b0.toString(16)})")
                }
            }
        }

        return out
    }

    private fun readInteger(data:ByteArray,posRef:IntArray,prefixBits:Int):Int{
        var pos=posRef[0]
        if(pos>=data.size) throw IllegalStateException("Truncated integer at pos $pos")
        val first=data[pos].toInt() and 0xFF
        val prefixMask=(1 shl prefixBits)-1
        var value=first and prefixMask
        pos++
        if(value==prefixMask) {
            var shift=0
            var b:Int
            do {
                if(pos>=data.size) throw IllegalStateException("Truncated integer continuation at pos $pos")
                b=data[pos].toInt() and 0xFF
                pos++
                value+=(b and 0x7F) shl shift
                shift+=7
            } while ((b and 0x80)!=0)
        }

        posRef[0]=pos
        return value
    }

    private fun readString(data: ByteArray, posRef: IntArray): String {
        val pos=posRef[0]
        if (pos>=data.size) throw IllegalStateException("Truncated string length at pos $pos")

        val first=data[pos].toInt() and 0xFF
        val huffmanFlag=(first and 0x80)!=0
        val length=readInteger(data,posRef,7)
        val newPos=posRef[0]

        if(newPos+length>data.size) throw IllegalStateException("Truncated string bytes at pos $newPos (need $length, have ${data.size-newPos})")
        
        val bytes=data.copyOfRange(newPos,newPos+length)
        posRef[0]=newPos+length
        val raw=if(huffmanFlag) huffman.decode(bytes) else bytes
        
        return String(raw,StandardCharsets.UTF_8)
    }

    private fun getHeaderFromIndex(index:Int):Header{
        val staticCount=static.size()
        return if(index in 1..staticCount) {
            static.get(index)
        } else {
            val dynIndex=index-staticCount
            dynamic.get(dynIndex)
        }
    }

    private fun getHeaderNameFromIndex(index:Int):String{
        val h=getHeaderFromIndex(index)
        return h.name
    }

    private fun updateDynamicTableSize(newSize:Int){
        dynamic.maxSize=newSize
    }
}
