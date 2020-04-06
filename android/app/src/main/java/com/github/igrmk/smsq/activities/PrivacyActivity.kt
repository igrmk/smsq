package com.github.igrmk.smsq.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.github.igrmk.smsq.R
import com.github.igrmk.smsq.helpers.linf
import kotlinx.android.synthetic.main.activity_privacy.*

class PrivacyActivity : AppCompatActivity() {
    private val tag = this::class.simpleName!!

    override fun onCreate(savedInstanceState: Bundle?) {
        linf(tag, "creating activity...")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_privacy)
        text.loadMarkdownFile("file:///android_asset/PRIVACY.md", "file:///android_asset/markdown.css")
    }
}
