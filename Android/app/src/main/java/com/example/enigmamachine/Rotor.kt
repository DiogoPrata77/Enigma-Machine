package com.example.enigmamachine


class Rotor(var name: String, wiring: String, rotorPosition: Int, notchPos: Int, ringSetting: Int) {
    protected var forwardWiring: IntArray
    protected var backwardWiring: IntArray
    var position: Int
        protected set
    protected var notchPos: Int
    protected var ringMapping: Int

    fun forward(c: Int): Int {
        return encipher(c, position, ringMapping, forwardWiring)
    }

    fun backward(c: Int): Int {
        return encipher(c, position, ringMapping, backwardWiring)
    }

    val isAtNotch: Boolean
        get() = notchPos == position

    fun turnover() {
        position = (position + 1) % 26
    }

    companion object {
        fun Create(name: String?, rotorPosition: Int, ringSetting: Int): Rotor {
            return when (name) {
                "I" -> Rotor("I", "EKMFLGDQVZNTOWYHXUSPAIBRCJ", rotorPosition, 16, ringSetting)
                "II" -> Rotor("II", "AJDKSIRUXBLHWTMCQGZNPYFVOE", rotorPosition, 4, ringSetting)
                "III" -> Rotor("III", "BDFHJLCPRTXVZNYEIWGAKMUSQO", rotorPosition, 21, ringSetting)
                "IV" -> Rotor("IV", "ESOVPZJAYQUIRHXLNFTGKDCMWB", rotorPosition, 9, ringSetting)
                "V" -> Rotor("V", "VZBRGITYUPSDNHLXAWMJQOFECK", rotorPosition, 25, ringSetting)
                else -> Rotor("ID", "ABCDEFGHIJKLMNOPQRSTUVWXYZ", rotorPosition, 0, ringSetting)
            }
        }

        protected fun decodeWiring(wiring: String): IntArray {
            val charWiring = wiring.toCharArray()
            val wiring = IntArray(charWiring.size)
            for (i in charWiring.indices) {
                wiring[i] = (charWiring[i] - 65).toInt()
            }
            return wiring
        }

        protected fun inverseWiring(wiring: IntArray): IntArray {
            val inverse = IntArray(wiring.size)
            for (i in wiring.indices) {
                val forward = wiring[i]
                inverse[forward] = i
            }
            return inverse
        }

        protected fun encipher(k: Int, pos: Int, ring: Int, mapping: IntArray): Int {
            val shift = pos - ring
            return (mapping[(k + shift + 26) % 26] - shift + 26) % 26
        }
    }

    init {
        forwardWiring = decodeWiring(wiring)
        backwardWiring = inverseWiring(forwardWiring)
        position = rotorPosition
        this.notchPos = notchPos
        this.ringMapping = ringSetting
    }
}

