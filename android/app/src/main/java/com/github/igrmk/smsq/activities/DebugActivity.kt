package com.github.igrmk.smsq.activities

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.github.igrmk.smsq.R
import com.github.igrmk.smsq.helpers.myPreferences
import com.github.igrmk.smsq.myApplication
import java.io.ByteArrayInputStream

class DebugActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_debug)
    }

    fun onSendLogsClicked(@Suppress("UNUSED_PARAMETER") view: View) {
        val logs = myApplication.log.get()
        val emailIntent = Intent(Intent.ACTION_SEND)
        emailIntent.type = "message/rfc822"
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "smsQ feedback")
        emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf("igrmkx+smsq@gmail.com"))
        emailIntent.putExtra(Intent.EXTRA_TEXT, "YOUR FEEDBACK HERE\n\n$logs")
        try {
            startActivity(Intent.createChooser(emailIntent, "Send logs to developer"))
        } catch (ex: ActivityNotFoundException) {
            Toast.makeText(this, "No email clients installed", Toast.LENGTH_LONG).show()
        }
    }

    fun onClearPrefsClicked(@Suppress("UNUSED_PARAMETER") view: View) {
        with(myPreferences.edit()) {
            clear()
            apply()
        }
    }
}
