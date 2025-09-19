package dev.samicpp.http.hpack

data class HuffmanCode(val code:Int, val length:Int)

// Appendix B #appendix-B
fun HuffmanTable():Array<HuffmanCode>{
    return arrayOf(
        HuffmanCode(0x1ff8, 13),
        HuffmanCode(0x7fffd8, 23),
        HuffmanCode(0xfffffe2, 28),
        HuffmanCode(0xfffffe3, 28),
        HuffmanCode(0xfffffe4, 28),
        HuffmanCode(0xfffffe5, 28),
        HuffmanCode(0xfffffe6, 28),
        HuffmanCode(0xfffffe7, 28),
        HuffmanCode(0xfffffe8, 28),
        HuffmanCode(0xffffea, 24),
        HuffmanCode(0x3ffffffc, 30),
        HuffmanCode(0xfffffe9, 28),
        HuffmanCode(0xfffffea, 28),
        HuffmanCode(0x3ffffffd, 30),
        HuffmanCode(0xfffffeb, 28),
        HuffmanCode(0xfffffec, 28),
        HuffmanCode(0xfffffed, 28),
        HuffmanCode(0xfffffee, 28),
        HuffmanCode(0xfffffef, 28),
        HuffmanCode(0xffffff0, 28),
        HuffmanCode(0xffffff1, 28),
        HuffmanCode(0xffffff2, 28),
        HuffmanCode(0x3ffffffe, 30),
        HuffmanCode(0xffffff3, 28),
        HuffmanCode(0xffffff4, 28),
        HuffmanCode(0xffffff5, 28),
        HuffmanCode(0xffffff6, 28),
        HuffmanCode(0xffffff7, 28),
        HuffmanCode(0xffffff8, 28),
        HuffmanCode(0xffffff9, 28),
        HuffmanCode(0xffffffa, 28),
        HuffmanCode(0xffffffb, 28),
        HuffmanCode(0x14, 6),
        HuffmanCode(0x3f8, 10),
        HuffmanCode(0x3f9, 10),
        HuffmanCode(0xffa, 12),
        HuffmanCode(0x1ff9, 13),
        HuffmanCode(0x15, 6),
        HuffmanCode(0xf8, 8),
        HuffmanCode(0x7fa, 11),
        HuffmanCode(0x3fa, 10),
        HuffmanCode(0x3fb, 10),
        HuffmanCode(0xf9, 8),
        HuffmanCode(0x7fb, 11),
        HuffmanCode(0xfa, 8),
        HuffmanCode(0x16, 6),
        HuffmanCode(0x17, 6),
        HuffmanCode(0x18, 6),
        HuffmanCode(0x0, 5),
        HuffmanCode(0x1, 5),
        HuffmanCode(0x2, 5),
        HuffmanCode(0x19, 6),
        HuffmanCode(0x1a, 6),
        HuffmanCode(0x1b, 6),
        HuffmanCode(0x1c, 6),
        HuffmanCode(0x1d, 6),
        HuffmanCode(0x1e, 6),
        HuffmanCode(0x1f, 6),
        HuffmanCode(0x5c, 7),
        HuffmanCode(0xfb, 8),
        HuffmanCode(0x7ffc, 15),
        HuffmanCode(0x20, 6),
        HuffmanCode(0xffb, 12),
        HuffmanCode(0x3fc, 10),
        HuffmanCode(0x1ffa, 13),
        HuffmanCode(0x21, 6),
        HuffmanCode(0x5d, 7),
        HuffmanCode(0x5e, 7),
        HuffmanCode(0x5f, 7),
        HuffmanCode(0x60, 7),
        HuffmanCode(0x61, 7),
        HuffmanCode(0x62, 7),
        HuffmanCode(0x63, 7),
        HuffmanCode(0x64, 7),
        HuffmanCode(0x65, 7),
        HuffmanCode(0x66, 7),
        HuffmanCode(0x67, 7),
        HuffmanCode(0x68, 7),
        HuffmanCode(0x69, 7),
        HuffmanCode(0x6a, 7),
        HuffmanCode(0x6b, 7),
        HuffmanCode(0x6c, 7),
        HuffmanCode(0x6d, 7),
        HuffmanCode(0x6e, 7),
        HuffmanCode(0x6f, 7),
        HuffmanCode(0x70, 7),
        HuffmanCode(0x71, 7),
        HuffmanCode(0x72, 7),
        HuffmanCode(0xfc, 8),
        HuffmanCode(0x73, 7),
        HuffmanCode(0xfd, 8),
        HuffmanCode(0x1ffb, 13),
        HuffmanCode(0x7fff0, 19),
        HuffmanCode(0x1ffc, 13),
        HuffmanCode(0x3ffc, 14),
        HuffmanCode(0x22, 6),
        HuffmanCode(0x7ffd, 15),
        HuffmanCode(0x3, 5),
        HuffmanCode(0x23, 6),
        HuffmanCode(0x4, 5),
        HuffmanCode(0x24, 6),
        HuffmanCode(0x5, 5),
        HuffmanCode(0x25, 6),
        HuffmanCode(0x26, 6),
        HuffmanCode(0x27, 6),
        HuffmanCode(0x6, 5),
        HuffmanCode(0x74, 7),
        HuffmanCode(0x75, 7),
        HuffmanCode(0x28, 6),
        HuffmanCode(0x29, 6),
        HuffmanCode(0x2a, 6),
        HuffmanCode(0x7, 5),
        HuffmanCode(0x2b, 6),
        HuffmanCode(0x76, 7),
        HuffmanCode(0x2c, 6),
        HuffmanCode(0x8, 5),
        HuffmanCode(0x9, 5),
        HuffmanCode(0x2d, 6),
        HuffmanCode(0x77, 7),
        HuffmanCode(0x78, 7),
        HuffmanCode(0x79, 7),
        HuffmanCode(0x7a, 7),
        HuffmanCode(0x7b, 7),
        HuffmanCode(0x7ffe, 15),
        HuffmanCode(0x7fc, 11),
        HuffmanCode(0x3ffd, 14),
        HuffmanCode(0x1ffd, 13),
        HuffmanCode(0xffffffc, 28),
        HuffmanCode(0xfffe6, 20),
        HuffmanCode(0x3fffd2, 22),
        HuffmanCode(0xfffe7, 20),
        HuffmanCode(0xfffe8, 20),
        HuffmanCode(0x3fffd3, 22),
        HuffmanCode(0x3fffd4, 22),
        HuffmanCode(0x3fffd5, 22),
        HuffmanCode(0x7fffd9, 23),
        HuffmanCode(0x3fffd6, 22),
        HuffmanCode(0x7fffda, 23),
        HuffmanCode(0x7fffdb, 23),
        HuffmanCode(0x7fffdc, 23),
        HuffmanCode(0x7fffdd, 23),
        HuffmanCode(0x7fffde, 23),
        HuffmanCode(0xffffeb, 24),
        HuffmanCode(0x7fffdf, 23),
        HuffmanCode(0xffffec, 24),
        HuffmanCode(0xffffed, 24),
        HuffmanCode(0x3fffd7, 22),
        HuffmanCode(0x7fffe0, 23),
        HuffmanCode(0xffffee, 24),
        HuffmanCode(0x7fffe1, 23),
        HuffmanCode(0x7fffe2, 23),
        HuffmanCode(0x7fffe3, 23),
        HuffmanCode(0x7fffe4, 23),
        HuffmanCode(0x1fffdc, 21),
        HuffmanCode(0x3fffd8, 22),
        HuffmanCode(0x7fffe5, 23),
        HuffmanCode(0x3fffd9, 22),
        HuffmanCode(0x7fffe6, 23),
        HuffmanCode(0x7fffe7, 23),
        HuffmanCode(0xffffef, 24),
        HuffmanCode(0x3fffda, 22),
        HuffmanCode(0x1fffdd, 21),
        HuffmanCode(0xfffe9, 20),
        HuffmanCode(0x3fffdb, 22),
        HuffmanCode(0x3fffdc, 22),
        HuffmanCode(0x7fffe8, 23),
        HuffmanCode(0x7fffe9, 23),
        HuffmanCode(0x1fffde, 21),
        HuffmanCode(0x7fffea, 23),
        HuffmanCode(0x3fffdd, 22),
        HuffmanCode(0x3fffde, 22),
        HuffmanCode(0xfffff0, 24),
        HuffmanCode(0x1fffdf, 21),
        HuffmanCode(0x3fffdf, 22),
        HuffmanCode(0x7fffeb, 23),
        HuffmanCode(0x7fffec, 23),
        HuffmanCode(0x1fffe0, 21),
        HuffmanCode(0x1fffe1, 21),
        HuffmanCode(0x3fffe0, 22),
        HuffmanCode(0x1fffe2, 21),
        HuffmanCode(0x7fffed, 23),
        HuffmanCode(0x3fffe1, 22),
        HuffmanCode(0x7fffee, 23),
        HuffmanCode(0x7fffef, 23),
        HuffmanCode(0xfffea, 20),
        HuffmanCode(0x3fffe2, 22),
        HuffmanCode(0x3fffe3, 22),
        HuffmanCode(0x3fffe4, 22),
        HuffmanCode(0x7ffff0, 23),
        HuffmanCode(0x3fffe5, 22),
        HuffmanCode(0x3fffe6, 22),
        HuffmanCode(0x7ffff1, 23),
        HuffmanCode(0x3ffffe0, 26),
        HuffmanCode(0x3ffffe1, 26),
        HuffmanCode(0xfffeb, 20),
        HuffmanCode(0x7fff1, 19),
        HuffmanCode(0x3fffe7, 22),
        HuffmanCode(0x7ffff2, 23),
        HuffmanCode(0x3fffe8, 22),
        HuffmanCode(0x1ffffec, 25),
        HuffmanCode(0x3ffffe2, 26),
        HuffmanCode(0x3ffffe3, 26),
        HuffmanCode(0x3ffffe4, 26),
        HuffmanCode(0x7ffffde, 27),
        HuffmanCode(0x7ffffdf, 27),
        HuffmanCode(0x3ffffe5, 26),
        HuffmanCode(0xfffff1, 24),
        HuffmanCode(0x1ffffed, 25),
        HuffmanCode(0x7fff2, 19),
        HuffmanCode(0x1fffe3, 21),
        HuffmanCode(0x3ffffe6, 26),
        HuffmanCode(0x7ffffe0, 27),
        HuffmanCode(0x7ffffe1, 27),
        HuffmanCode(0x3ffffe7, 26),
        HuffmanCode(0x7ffffe2, 27),
        HuffmanCode(0xfffff2, 24),
        HuffmanCode(0x1fffe4, 21),
        HuffmanCode(0x1fffe5, 21),
        HuffmanCode(0x3ffffe8, 26),
        HuffmanCode(0x3ffffe9, 26),
        HuffmanCode(0xffffffd, 28),
        HuffmanCode(0x7ffffe3, 27),
        HuffmanCode(0x7ffffe4, 27),
        HuffmanCode(0x7ffffe5, 27),
        HuffmanCode(0xfffec, 20),
        HuffmanCode(0xfffff3, 24),
        HuffmanCode(0xfffed, 20),
        HuffmanCode(0x1fffe6, 21),
        HuffmanCode(0x3fffe9, 22),
        HuffmanCode(0x1fffe7, 21),
        HuffmanCode(0x1fffe8, 21),
        HuffmanCode(0x7ffff3, 23),
        HuffmanCode(0x3fffea, 22),
        HuffmanCode(0x3fffeb, 22),
        HuffmanCode(0x1ffffee, 25),
        HuffmanCode(0x1ffffef, 25),
        HuffmanCode(0xfffff4, 24),
        HuffmanCode(0xfffff5, 24),
        HuffmanCode(0x3ffffea, 26),
        HuffmanCode(0x7ffff4, 23),
        HuffmanCode(0x3ffffeb, 26),
        HuffmanCode(0x7ffffe6, 27),
        HuffmanCode(0x3ffffec, 26),
        HuffmanCode(0x3ffffed, 26),
        HuffmanCode(0x7ffffe7, 27),
        HuffmanCode(0x7ffffe8, 27),
        HuffmanCode(0x7ffffe9, 27),
        HuffmanCode(0x7ffffea, 27),
        HuffmanCode(0x7ffffeb, 27),
        HuffmanCode(0xffffffe, 28),
        HuffmanCode(0x7ffffec, 27),
        HuffmanCode(0x7ffffed, 27),
        HuffmanCode(0x7ffffee, 27),
        HuffmanCode(0x7ffffef, 27),
        HuffmanCode(0x7fffff0, 27),
        HuffmanCode(0x3ffffee, 26),
        HuffmanCode(0x3fffffff, 30),
    )
}

sealed class HuffmanSymbol{
    data class Symbol(val value:Byte):HuffmanSymbol()
    object EndOfString:HuffmanSymbol()
}

sealed class HuffmanDecoderError(message: String):Exception(message){
    object PaddingTooLarge:HuffmanDecoderError("Padding is larger than 7 bits")
    object InvalidPadding:HuffmanDecoderError("Padding does not match EOS most-significant bits")
    object EOSInString:HuffmanDecoderError("EOS symbol found inside the stream")
}

class HuffmanDecoder(private val table:Array<HuffmanCode>){
    private val lookup:MutableMap<Int,MutableMap<Int,HuffmanSymbol>> =mutableMapOf()
    private val eosCode:Pair<Int,Int>

    private val codeBySymbol:IntArray
    private val lenBySymbol:IntArray

    init{
        if(table.size!=257){
            throw IllegalArgumentException("Table must define 257 symbols (0-255 + EOS)")
        }

        var eos:Pair<Int,Int>?=null

        for ((symbol,code) in table.withIndex()) {
            val sub=lookup.getOrPut(code.length){ mutableMapOf() }
            if(symbol==256) {
                eos=code.code to code.length
                sub[code.code]=HuffmanSymbol.EndOfString
            } else {
                sub[code.code]=HuffmanSymbol.Symbol(symbol.toByte())
            }
        }

        eosCode=eos?:throw IllegalStateException("EOS symbol missing in table")

        codeBySymbol=IntArray(257)
        lenBySymbol=IntArray(257)
        for((i,c) in table.withIndex()){
            codeBySymbol[i]=c.code
            lenBySymbol[i]=c.length
        }
    }

    fun decode(buf:ByteArray):ByteArray{
        var current=0
        var currentLen=0
        val result=mutableListOf<Byte>()

        for(bit in BitIterator(buf)){
            current=(current shl 1) or if(bit) 1 else 0
            currentLen++

            val subtable=lookup[currentLen]
            val symbol=subtable?.get(current)
            if (symbol!=null) {
                when(symbol) {
                    is HuffmanSymbol.Symbol->result.add(symbol.value);
                    is HuffmanSymbol.EndOfString->throw HuffmanDecoderError.EOSInString;
                }
                current=0
                currentLen=0
            }
        }

        if(currentLen>7) {
            throw HuffmanDecoderError.PaddingTooLarge
        }; if (currentLen>0) {
            val rightAlignCurrent=current shl (32-currentLen)
            val rightAlignEos=eosCode.first shl (32-eosCode.second)
            val mask=if(currentLen==0) 0 else ((1 shl currentLen)-1) shl (32-currentLen)
            val eosMask=rightAlignEos and mask
            if (eosMask!=rightAlignCurrent) {
                throw HuffmanDecoderError.InvalidPadding
            }
        }

        return result.toByteArray()
    }

    fun encode(input:ByteArray):ByteArray{
        val out=mutableListOf<Byte>()

        var acc:Long=0L
        var accLen:Int=0

        for(b in input){
            val sym=b.toInt() and 0xff
            val code=codeBySymbol[sym].toLong() and 0xffffffffL
            val len=lenBySymbol[sym]

            acc=(acc shl len) or code
            accLen+=len

            while(accLen>=8){
                val shift=accLen-8
                val outByte=((acc shr shift) and 0xFF).toInt()
                out.add(outByte.toByte())
                accLen-=8
                
                if(accLen==0) {
                    acc=0L
                } else {
                    val mask=(1L shl accLen)-1L
                    acc=acc and mask
                }
            }
        }

        if(accLen>0){
            val padBits=8-accLen
            val padMask=(1L shl padBits)-1L
            val finalByte=(((acc shl padBits) or padMask) and 0xFF).toInt()
            out.add(finalByte.toByte())
        }

        return out.toByteArray()
    }
}

class BitIterator(private val buf:ByteArray):Iterator<Boolean>{
    private var byteIndex=0
    private var bitPos=7

    override fun hasNext():Boolean{
        return byteIndex<buf.size
    }

    override fun next():Boolean{
        if(!hasNext())throw NoSuchElementException()
        val b=buf[byteIndex].toInt() and 0xff
        val bit=((b shr bitPos) and 1)==1
        if(bitPos==0) {
            bitPos=7
            byteIndex++
        } else {
            bitPos--
        }
        return bit
    }
}
