package dev.samicpp.http.hpack

import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets


class Encoder(val size:Int=4096){
    private val static=StaticTable()  // static.get(Int): Header(val name:String,val value:String?=null)
    private val dynamic=DynamicTable(size)  // dynamic.add(Header); dynamic.get(Int): Header
    private val huffman=HuffmanDecoder(HuffmanTable())  // huffman.decode(ByteArray): ByteArray; huffman.encode(ByteArray): ByteArray;

    
    fun encode(headers:List<Pair<String,String>>):ByteArray{
        val out=ByteArrayOutputStream()
        val staticCount=static.size()

        for((name,value) in headers){
            val exactIndex=findExactIndex(name,value,staticCount)
            if(exactIndex!=null) {
                writeIndexedHeaderField(out,exactIndex)
                continue
            }

            val nameIndex=findNameIndex(name,staticCount)

            if(nameIndex!=null) {
                writeLiteralWithIncrementalIndexingIndexedName(out,nameIndex,value)
                dynamic.add(Header(name,value))
            } else {
                writeLiteralWithIncrementalIndexingNewName(out,name,value)
                dynamic.add(Header(name,value))
            }
        }

        return out.toByteArray()
    }

    private fun findExactIndex(name:String,value:String,staticCount:Int):Int?{
        for(i in 1..staticCount){
            val h=static.get(i)
            if(h?.name==name&&(h.value?:"")==value) return i
        }
        val dSize=dynamic.size()
        for(i in 1..dSize){
            val h=dynamic.get(i)
            if(h?.name==name&&h.value==value){
                return staticCount+i
            }
        }
        return null
    }
 
    private fun findNameIndex(name:String,staticCount:Int):Int?{
        for (i in 1..staticCount) {
            val h=static.get(i)
            if(h?.name==name) return i
        }
        val dSize=dynamic.size()
        for (i in 1..dSize) {
            val h=dynamic.get(i)
            if(h?.name==name) return staticCount+i
        }
        return null
    }

    private fun writeInteger(out:ByteArrayOutputStream,value:Int,prefixBits:Int,prefixStatic:Int){
        val maxPrefix=(1 shl prefixBits)-1
        if(value<maxPrefix) {
            out.write(prefixStatic or value)
        } else {
            out.write(prefixStatic or maxPrefix)
            var remainder=value-maxPrefix
            while(remainder>=128){
                val b=(remainder and 0x7f) or 0x80
                out.write(b)
                remainder=remainder ushr 7
            }
            out.write(remainder)
        }
    }

    private fun writeString(out:ByteArrayOutputStream,s:String){
        val raw=s.toByteArray(StandardCharsets.UTF_8)
        val huff=huffman.encode(raw)
        val useHuff=huff.size<raw.size
        val bytes=if(useHuff) huff else raw

        val prefixFlag=if(useHuff) 0x80 else 0x00
        writeInteger(out,bytes.size,7,prefixFlag)
        out.write(bytes)
    }
    
    
    private fun writeIndexedHeaderField(out:ByteArrayOutputStream,index:Int){
        writeInteger(out,index,7,0x80)
    }

    private fun writeLiteralWithIncrementalIndexingIndexedName(out:ByteArrayOutputStream,nameIndex:Int,value:String){
        writeInteger(out,nameIndex,6,0x40)
        writeString(out,value)
    }

    private fun writeLiteralWithIncrementalIndexingNewName(out:ByteArrayOutputStream,name:String,value:String){
        writeInteger(out,0,6,0x40)
        writeString(out,name)
        writeString(out,value)
    }
}
