package com.example.enigmamachine

class Reflector(wiring: String) {
    protected var forwardWiring: IntArray
    fun forward(c: Int): Int {
        return forwardWiring[c]
    }

    companion object {
        fun Create(name: String?): Reflector {
            return when (name) {
                "B" -> Reflector("YRUHQSLDPXNGOKMIEBFZCWVJAT")
                "C" -> Reflector("FVPJIAOYEDRZXWGCTKUQSBNMHL")
                else -> Reflector("ZYXWVUTSRQPONMLKJIHGFEDCBA")
            }
        }

        protected fun decode(wiring: String): IntArray {
            val charWiring = wiring.toCharArray()
            val wiring = IntArray(charWiring.size)
            for (i in charWiring.indices) {
                wiring[i] = (charWiring[i] - 65).toInt()
            }
            return wiring
        }
    }

    init {
        forwardWiring = decode(wiring)
    }
}