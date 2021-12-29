package com.meehawek.passwordnotepad

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

class PasswordChangeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_password_change)
        val helper = Helpers(applicationContext)

        findViewById<Button>(R.id.save_new_password_button).setOnClickListener {
            val sharPref = getSharedPreferences(packageName, Context.MODE_PRIVATE)

            val oldPassword = helper.generateHash(findViewById<EditText>(R.id.old_password_edit).text.toString().toCharArray(), sharPref)
            val newPassword = helper.generateHash(findViewById<EditText>(R.id.new_password_edit).text.toString().toCharArray(), sharPref)

            val saved = sharPref.getString("9uW2hXh3", null)

            if (sharPref.contains("9uW2hXh3")) {
                if (oldPassword == saved){
                    sharPref.edit().remove("9uW2hXh3").apply()
                    sharPref.edit().putString("9uW2hXh3", newPassword).apply()

                    Toast.makeText(this, R.string.changed_password, Toast.LENGTH_LONG).show()
                    finish()
                } else {
                    Toast.makeText(this, R.string.wrong_password, Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}