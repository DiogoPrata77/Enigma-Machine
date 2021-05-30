package com.example.enigmamachine

import com.google.firebase.database.FirebaseDatabase
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun testEncryption() {
        var machine = EnigmaMachine(arrayOf("II", "I", "III"), "B", intArrayOf(2, 3 , 4), intArrayOf(0, 0 , 0))

        var decryptedMsg = "HELLO WORLD"

        var encryptedMsg = machine.encrypt(decryptedMsg)

        print(encryptedMsg)

    }


}