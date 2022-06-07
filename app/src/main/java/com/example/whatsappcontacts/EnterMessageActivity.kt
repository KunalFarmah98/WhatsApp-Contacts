package com.example.whatsappcontacts

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.widget.Button
import android.widget.EditText

val MSG = "Message"

class EnterMessageActivity : AppCompatActivity() {
    var et: EditText? = null
    var set: Button? = null
    var cancel: Button? = null
    var sPref: SharedPreferences? = null

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_enter_message)
        sPref = getSharedPreferences("WhatsAppContacts", Context.MODE_PRIVATE)

        et = findViewById(R.id.message)
        et?.setText(sPref?.getString(MSG, ""))
        set = findViewById(R.id.set)
        cancel = findViewById(R.id.cancel)

        cancel?.setOnClickListener {
            finish()
        }

        set?.setOnClickListener {
            sPref!!.edit().putString(MSG, et?.text.toString()).apply()
            finish()
        }

    }
}