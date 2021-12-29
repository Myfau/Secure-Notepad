package com.meehawek.passwordnotepad

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText

class NoteActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note)
        val helper = Helpers(applicationContext)

        val sharPref = getSharedPreferences(packageName, Context.MODE_PRIVATE)
        if (sharPref.contains("NUVHAw5t")) {
            val storedNotes = sharPref.getString("NUVHAw5t", "")!!
            val decrypted = helper.decryptString(storedNotes, sharPref)
            findViewById<EditText>(R.id.EditNote).setText(
                String(decrypted)
            )
            sharPref.edit().remove("NUVHAw5t").apply()
        }

        findViewById<Button>(R.id.save_button).setOnClickListener{
            val wholeNote = findViewById<EditText>(R.id.EditNote).text.toString()

            sharPref.edit().putString("NUVHAw5t", helper.encryptString(wholeNote, sharPref)).apply()
            finish()
        }

        findViewById<Button>(R.id.change_password_button).setOnClickListener{
            startActivity(Intent(this, PasswordChangeActivity::class.java))
        }
    }
}