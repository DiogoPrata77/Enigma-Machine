package com.example.enigmamachine

import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.View
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.*

/**
 * Helper class for writting database entries into Firebase of a Rotor configuration
 *
 * @property config the wiring of the rotor.
 * @property position of the rotor.
 */
class RotorsDB{
    var config:String=""
    var position:String=" "

    constructor(config: String, position: String){
        this.config = config
        this.position = position
    }
}

/**
 * MainActivity class that render and encapsulates the display logic of the app
 *
 * @property rotorConfigBuffer a buffer for storing new rotor configs (I,II,III,IV,V).
 * @property machine the internal enigma machine state.
 */
class MainActivity : AppCompatActivity() {

    var rotorConfigBuffer = arrayOf("", "", "")
    var rotorPosConfigBuffer  = arrayOf("", "", "")
    var machine : EnigmaMachine = EnigmaMachine(arrayOf("I", "II", "III"), "B", intArrayOf(0, 0 , 0), intArrayOf(0, 0 , 0))

    /**
     * Start new database instance
     *
     * @return database reference
     */
    fun DBInit() : DatabaseReference {
        var database = FirebaseDatabase.getInstance("https://enigma-machine-se-2021.firebaseio.com/")
        return database.reference
    }

    /**
     * Helper class for a nice display of the rotors' configurations into its respective Spinner when
     * selecting or loading a new rotor configuration
     *
     * @param wiring the new rotor configuration.
     * @param rotor Spinner that display a rotor configuration.
     */
    fun updateSpinner(wiring: String, rotor: Spinner) {
        when (wiring) {
            "I" -> {
                val myAdapter = ArrayAdapter(this@MainActivity,
                        R.layout.support_simple_spinner_dropdown_item, resources.getStringArray(R.array.wiringsI))
                myAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item)
                rotor.adapter = myAdapter
            }
            "II" -> {
                val myAdapter = ArrayAdapter(this@MainActivity,
                        R.layout.support_simple_spinner_dropdown_item, resources.getStringArray(R.array.wiringsII))
                myAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item)
                rotor.adapter = myAdapter
            }
            "III" -> {
                val myAdapter = ArrayAdapter(this@MainActivity,
                        R.layout.support_simple_spinner_dropdown_item, resources.getStringArray(R.array.wiringsIII))
                myAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item)
                rotor.adapter = myAdapter
            }
            "VI" -> {
                val myAdapter = ArrayAdapter(this@MainActivity,
                        R.layout.support_simple_spinner_dropdown_item, resources.getStringArray(R.array.wiringsVI))
                myAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item)
                rotor.adapter = myAdapter
            }
            "V" -> {
                val myAdapter = ArrayAdapter(this@MainActivity,
                        R.layout.support_simple_spinner_dropdown_item, resources.getStringArray(R.array.wiringsV))
                myAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item)
                rotor.adapter = myAdapter
            }
        }
    }

    /**
     * Update every Spinner value being displayed according to the machine current configuration
     */
    fun updateSpinners(){
        val rotorLeftConfig = findViewById<View>(R.id.rotorLeftConfig) as Spinner
        val rotorMiddleConfig = findViewById<View>(R.id.rotorMiddleConfig) as Spinner
        val rotorRightConfig = findViewById<View>(R.id.rotorRightConfig) as Spinner

        updateSpinner(machine.leftRotor.name, rotorLeftConfig)
        updateSpinner(machine.middleRotor.name, rotorMiddleConfig)
        updateSpinner(machine.rightRotor.name, rotorRightConfig)
    }

    /**
     * Event attached to Spinner listener
     * where it saves new rotor configuration selected from Spinner into the rotorConfigBuffer
     */
    fun handleSpinnersChange(rotor: Spinner, rotorIndex: Int) {
        rotor.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View, i: Int, l: Long) {
                rotorConfigBuffer[rotorIndex] = rotor.selectedItem.toString()
            }
            override fun onNothingSelected(adapterView: AdapterView<*>?) {}
        }
    }

    /**
     * Initial spinner setup where the handleSpinnersChange event is added to each rotor's Spinner
     */
    fun spinnersSetup(){
        updateSpinners()

        val rotorLeftConfig = findViewById<View>(R.id.rotorLeftConfig) as Spinner
        val rotorMiddleConfig = findViewById<View>(R.id.rotorMiddleConfig) as Spinner
        val rotorRightConfig = findViewById<View>(R.id.rotorRightConfig) as Spinner

        handleSpinnersChange(rotorLeftConfig, 0)
        handleSpinnersChange(rotorMiddleConfig, 1)
        handleSpinnersChange(rotorRightConfig, 2)

    }

    /**
     * Encrypt the new message inside id.newMsg and send it to the database
     * along with the configuration that originated the new message
     *
     * @param dB the database reference to which we will send the new message and its configuration
     */
    fun encryptAndSend(dB : DatabaseReference){
        val textInputLayout = findViewById<TextInputEditText>(R.id.newMsg)
        val text: Editable? = textInputLayout.text

        val ciphertext = machine.encrypt(text.toString())

        var newCiphertextRef = dB.child("msg_to_raspberry").push()

        val entry: MutableMap<String, Any> = HashMap()
        entry["encrypted_msg"] = ciphertext
        entry["rotor_left"] = RotorsDB(machine.leftRotor.name,rotorPosConfigBuffer[0])
        entry["rotor_middle"] = RotorsDB(machine.middleRotor.name, rotorPosConfigBuffer[1])
        entry["rotor_right"] = RotorsDB(machine.rightRotor.name, rotorPosConfigBuffer[2])

        newCiphertextRef.setValue(entry)

        setRotorsLetter()
    }

    /**
     * Decrypt a new received message and display the plaintext bellow it
     */
    fun decryptAndShow(){
        var decryptButton = findViewById<Button>(R.id.decrypt)
        decryptButton.setOnClickListener {
            val ciphertextView = findViewById<TextView>(R.id.ciphertext)
            val plaintextView = findViewById<TextView>(R.id.plaintext)
            plaintextView.text = machine.encrypt(ciphertextView.text.toString())
        }
    }

    /**
     * Load ciphertext from database, following database updates on the ciphertext will be upload automatically
     *
     * @param dB database reference that retrieves the message
     */
    fun loadCiphertext( dB : DatabaseReference) {
        var query = dB.child("msg_to_android").limitToLast(1)
        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val ciphertextView = findViewById<TextView>(R.id.ciphertext)
                ciphertextView.text = snapshot.children.first().child("encrypted_msg").value.toString()
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("firebase", "onCancelled ${error.message}" )

            }
        })
    }

    /**
     * Updates the rotors letters, i.e., their positions according to the internal configuration of the machine
     */
    fun setRotorsLetter(){
        var rotorLeft = findViewById<TextView>(R.id.rotorLeft)
        var rotorMiddle = findViewById<TextView>(R.id.rotorMiddle)
        var rotorRight = findViewById<TextView>(R.id.rotorRight)

        if(rotorLeft != null && rotorMiddle != null && rotorRight !=null){
            rotorLeft.text = machine.alphabet[machine.leftRotor.position].toString()
            rotorMiddle.text = machine.alphabet[machine.middleRotor.position].toString()
            rotorRight.text =  machine.alphabet[machine.rightRotor.position].toString()
        }
    }

    /**
     * Load the configurations of the latest ciphertext received (the one that's currently being displayed).
     * Afterwards updated the UI to show the new configuration received
     *
     * @param dB database reference that retrieves the configurations
     */
    fun loadConfig(dB : DatabaseReference){
        var query = dB.child("msg_to_android").limitToLast(1)
        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var ref = snapshot.children.first()
                machine = EnigmaMachine(
                        arrayOf(
                                ref.child("rotor_left/config").value.toString(),
                                ref.child("rotor_middle/config").value.toString(),
                                ref.child("rotor_right/config").value.toString()
                        ),
                        "B",
                        intArrayOf(
                                machine.alphabet.indexOf(ref.child("rotor_left/position").value.toString()),
                                machine.alphabet.indexOf(ref.child("rotor_middle/position").value.toString()),
                                machine.alphabet.indexOf(ref.child("rotor_right/position").value.toString())
                        ),
                        intArrayOf(0, 0 , 0)
                )
                setRotorsLetter()
                updateSpinners()
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("firebase", "onCancelled ${error.message}" )
            }
        })
    }

    /**
     * Update the UI with new rotors' configurations set by user
     */
    fun setConfig() {
        val left_pos = findViewById<TextInputEditText>(R.id.setPosLeft).text!!.toString()
        val middle_pos = findViewById<TextInputEditText>(R.id.setPosMiddle).text!!.toString()
        val right_pos = findViewById<TextInputEditText>(R.id.setPosRight).text!!.toString()
        rotorPosConfigBuffer  = arrayOf(left_pos, middle_pos, right_pos)

        machine = EnigmaMachine(
                rotorConfigBuffer,
                "B",
                intArrayOf(
                        machine.alphabet.indexOf(left_pos),
                        machine.alphabet.indexOf(middle_pos),
                        machine.alphabet.indexOf(right_pos)
                ),
                intArrayOf(0, 0 , 0)
        )

        setRotorsLetter()
        updateSpinners()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var DB = DBInit()

        // Initial spinners setup
        spinnersSetup()

        // Set the rotors letters
        setRotorsLetter()

        // Load ciphertext (load automatically)
        loadCiphertext(DB)

        // Load new rotors configuration click event
        var rotorConfigBtnLoad = findViewById<Button>(R.id.rotorConfigBtnLoad)
        rotorConfigBtnLoad.setOnClickListener {
            loadConfig(DB)
        }

        // Set new rotors configuration click event
        var rotorConfigBtnSet = findViewById<Button>(R.id.rotorConfigBtnSet)
        rotorConfigBtnSet.setOnClickListener{
            setConfig()
        }

        // Decrypt and show plaintext event
        decryptAndShow()

        // Encrypt and send cipher text event
        var encryptAndSendBtn = findViewById<Button>(R.id.encrypt)
        encryptAndSendBtn.setOnClickListener{
            encryptAndSend(DB)
        }

    }
}