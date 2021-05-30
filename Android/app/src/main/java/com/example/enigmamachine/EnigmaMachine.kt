package com.example.enigmamachine

class EnigmaMachine(rotors: Array<String>, reflector: String?, rotorPositions: IntArray, ringSettings: IntArray) {
    // 0 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25
    // A B C D E F G H I J K  L  M  N  O  P  Q  R  S  T  U  V  W  X  Y  Z
    var leftRotor: Rotor
    var middleRotor: Rotor
    var rightRotor: Rotor
    var reflector: Reflector
    var alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"

    fun rotate() {
        if (middleRotor.isAtNotch) {
            middleRotor.turnover()
            leftRotor.turnover()
        } else if (rightRotor.isAtNotch) {
            middleRotor.turnover()
        }
        rightRotor.turnover()
    }

    fun encrypt(c: Int): Int {
        var c = c
        rotate()

        // Right to left
        val c1 = rightRotor.forward(c)
        val c2 = middleRotor.forward(c1)
        val c3 = leftRotor.forward(c2)

        // Reflector
        val c4 = reflector.forward(c3)

        // Left to right
        val c5 = leftRotor.backward(c4)
        val c6 = middleRotor.backward(c5)
        var c7 = rightRotor.backward(c6)

        return c7
    }

    fun encrypt(c: Char): Char {
        return (this.encrypt(c.toInt() - 65) + 65).toChar()
    }

    fun encrypt(input: String): String {
        val output = CharArray(input.length)
        for (i in input.indices) {
            output[i] = this.encrypt(input[i])
        }
        return output.joinToString("")
    }

    init {
        leftRotor = Rotor.Create(rotors[0], rotorPositions[0], ringSettings[0])
        middleRotor = Rotor.Create(rotors[1], rotorPositions[1], ringSettings[1])
        rightRotor = Rotor.Create(rotors[2], rotorPositions[2], ringSettings[2])
        this.reflector = Reflector.Create(reflector)
    }
}