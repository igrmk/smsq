package com.github.igrmk.smsq.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.github.igrmk.smsq.R
import kotlinx.android.synthetic.main.activity_privacy.*

class PrivacyActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_privacy)
        text.loadMarkdownFile("file:///android_asset/PRIVACY.md", "file:///android_asset/markdown.css")
    }
}
