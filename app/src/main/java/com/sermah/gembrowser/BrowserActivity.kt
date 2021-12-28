package com.sermah.gembrowser

import android.content.res.Configuration
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.recyclerview.widget.RecyclerView
import com.sermah.gembrowser.adapter.ContentAdapter
import com.sermah.gembrowser.data.ContentManager
import com.sermah.gembrowser.data.FontManager
import com.sermah.gembrowser.data.StyleManager
import com.sermah.gembrowser.databinding.ActivityBrowserBinding
import android.view.animation.TranslateAnimation
import android.content.DialogInterface

import android.R.id.input
import android.app.AlertDialog
import android.content.Intent

import android.text.Editable
import android.text.InputType

class BrowserActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBrowserBinding
    private lateinit var contentRV: RecyclerView
    private lateinit var uriField: EditText
    private lateinit var homeButton: AppCompatImageButton
    private lateinit var menuButton: AppCompatImageButton
    private lateinit var infoBar: LinearLayoutCompat

    private var homepage = "gemini://gemini.circumlunar.space/"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBrowserBinding.inflate(layoutInflater)

        binding.root.isFocusableInTouchMode = true
        setContentView(binding.root)

        ContentManager.contentUpdateHandler = ContentManager.ContentUpdateHandler (
            onSuccess = fun(_, lines) {
                runOnUiThread {
                    contentRV.adapter = ContentAdapter(this, lines)
                    uriField.setText(ContentManager.currentPage.uri.toString())
                    uriField.clearFocus()
                }
            },
            onInput         = fun(code, info, uri) {
                handleInput(info, uri, code == "11")
            },
            onRedirect      = fun(_, toUri, _) {
                ContentManager.requestUri(Uri.parse(toUri))
            },
            onTemporary     = ::tempHandleNonSuccess,
            onPermanent     = ::tempHandleNonSuccess,
            onCertificate   = ::tempHandleNonSuccess,
        )
        ContentManager.onNonGeminiScheme = ::handleNonGeminiScheme

        FontManager.loadFonts(assets)
        StyleManager.stylePre.typeface = FontManager.get("DejaVuSansMono.ttf") // TODO: Customization - font styles
        reloadStyleDark()

        homeButton = binding.homepageBtn
        menuButton = binding.menuBtn
        uriField = binding.uriField
        infoBar = binding.informationBar
        contentRV = binding.contentRecyclerView

        infoBar.visibility = View.INVISIBLE
        infoBar.isFocusableInTouchMode = true
        infoBar.setOnFocusChangeListener { _, _ ->
            if (!infoBar.hasFocus()) hideInfoBar()
        } // TODO: Fix

        homeButton.setOnClickListener{ ContentManager.requestUri(Uri.parse(homepage)) }

        uriField.setOnKeyListener (
            fun(v: View, k: Int, e: KeyEvent): Boolean {
                if((e.action == KeyEvent.ACTION_DOWN) && (k == KeyEvent.KEYCODE_ENTER)) {
                    if(uriField.text.isNotBlank()) Uri.parse(
                        uriField.text.toString().trim()
                    ).run {
                        var uri = this
                        if (uri.scheme == null) uri = Uri.parse("gemini://$this")
                        ContentManager.requestUri(uri)
                    }
                    return true
                }
                return false
            })
        uriField.background.alpha = 15
        uriField.setText(ContentManager.currentPage.uri.toString())

        contentRV.setHasFixedSize(true)
        contentRV.adapter = ContentAdapter(this, ContentManager.currentPage.lines)
        if(intent?.data != null) ContentManager.requestUri(intent?.data!!)
        else if(ContentManager.currentPage.lines.isEmpty())
            ContentManager.requestUri(Uri.parse(homepage)) // TODO: Customization - homepage
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        reloadStyleDark()
    }

    override fun onBackPressed() {
        if (!ContentManager.loadPreviousPage()) super.onBackPressed()
    }

    fun reloadStyleDark() {
        when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_YES -> StyleManager.isDark = true
            Configuration.UI_MODE_NIGHT_NO -> StyleManager.isDark = false
        }
    }

    fun handleNonGeminiScheme(uri: Uri) {
        // Probably in future someone (me) will want to do scheme-specific stuff
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = uri
        applicationContext.startActivity(intent)
    }

    fun tempHandleNonSuccess(code: String, meta: String, uri: Uri) { // TODO: Replace
        Thread {
            run {
                showInfoBar("Error code: $code", meta)
                Thread.sleep(5000)
                hideInfoBar()
            }
        }.start()
    }

    fun showInfoBar(title: String, message: String? = "") {
        runOnUiThread {
            infoBar.findViewById<TextView>(R.id.information_title).text = title
            val desc = infoBar.findViewById<TextView>(R.id.information_desc)
            if (message?.isNotBlank() == true) {
                desc.visibility = View.VISIBLE
                desc.text = message
            } else {
                desc.visibility = View.GONE
                desc.text = ""
            }
            infoBar.requestFocus()
            if(infoBar.visibility == View.INVISIBLE){
                infoBar.visibility = View.VISIBLE
                val animate = TranslateAnimation(
                    0f,
                    0f,
                    -infoBar.height.toFloat(),
                    0f
                )
                animate.duration = 500
                animate.fillAfter = true
                infoBar.startAnimation(animate)
            }
        }
    }

    fun hideInfoBar() {
        if(infoBar.visibility == View.INVISIBLE) return

        infoBar.visibility = View.INVISIBLE
        val animate = TranslateAnimation(
            0f,
            0f,
            0f,
            -infoBar.height.toFloat()
        )
        animate.duration = 500
        animate.fillAfter = true
        infoBar.clearFocus()
        infoBar.startAnimation(animate)
    }

    fun handleInput(title: String = "", toUri: Uri, sensitive: Boolean = false) { // TODO: Remove all hardcoded strings
        val input = EditText(this)
        if (sensitive) input.inputType =
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        AlertDialog.Builder(this)
            .setTitle(if (title.isNotBlank()) title else "Input Prompt")
            .setView(input)
            .setPositiveButton("Send") { _, _ ->
                ContentManager.requestUri(Uri.parse(toUri.toString() + "?" + Uri.encode(input.text.toString())))
            }
            .setNegativeButton("Cancel") { _, _ -> }
            .show()
    }
}