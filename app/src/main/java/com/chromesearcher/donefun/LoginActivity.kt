package com.chromesearcher.donefun

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import com.crashlytics.android.Crashlytics
import io.fabric.sdk.android.Fabric

class LoginActivity : AppCompatActivity() {

    private val DEFAULT_BOARD_ID: String = "main"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Fabric.with(this, Crashlytics())
        setContentView(R.layout.activity_login)

        val signinButton = findViewById<Button>(R.id.button_signin)

        signinButton.setOnClickListener {
            // invoke BoardActivity
            var newIntent = Intent(this, BoardActivity::class.java)
            newIntent.putExtra("board", DEFAULT_BOARD_ID)
            startActivity(newIntent)
        }
    }
}