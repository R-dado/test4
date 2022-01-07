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

import android.app.AlertDialog
import android.content.Intent
import android.os.Build

import android.text.InputType
import android.util.Log
import android.view.View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
import android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

import android.view.WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS
import android.widget.HorizontalScrollView
import com.google.android.material.button.MaterialButton
import com.sermah.gembrowser.data.StyleManager.dpToPx


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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
            resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_NO) {
            window.decorView.systemUiVisibility = FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS or
                    SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR or SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }

        ContentManager.contentUpdateHandler = ContentManager.ContentUpdateHandler (
            onSuccess = fun(_, lines) {
                runOnUiThread {
                    contentRV.adapter = ContentAdapter(this, lines)
                    assembleUriButtons(ContentManager.currentPage.uri)
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
            onTemporary     = tempHandleNonSuccess,
            onPermanent     = tempHandleNonSuccess,
            onCertificate   = tempHandleNonSuccess,
        )
        ContentManager.onNonGeminiScheme = fun (uri: Uri) {handleNonGeminiScheme(uri)}

        FontManager.loadFonts(assets)
        StyleManager.stylePre.typeface = FontManager.get("DejaVuSansMono.ttf") // TODO: Customization - font styles
        reloadStyleDark()

        homeButton = binding.homepageBtn
        menuButton = binding.menuBtn
        uriField = binding.uriField
        infoBar = binding.informationBar
        contentRV = binding.contentRecyclerView
        binding.uriSegmentsContainer.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            (binding.uriSegmentsContainer.parent as HorizontalScrollView).fullScroll(View.FOCUS_RIGHT)
        }

        infoBar.visibility = View.INVISIBLE
        infoBar.isFocusableInTouchMode = true
        infoBar.setOnFocusChangeListener { _, _ ->
            if (!infoBar.hasFocus()) hideInfoBar()
        } // TODO: Fix

        homeButton.setOnClickListener{ ContentManager.requestUri(Uri.parse(homepage)) }

        uriField.setOnKeyListener (
            fun(_: View, k: Int, e: KeyEvent): Boolean {
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
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        applicationContext.startActivity(intent)
    }

    val tempHandleNonSuccess = fun(code: String, meta: String, uri: Uri) { // TODO: Replace
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
        runOnUiThread {
            val layout = layoutInflater.inflate(R.layout.dialog_input, null)
            layout.findViewById<TextView>(R.id.dialog_input_server_name).text =
                getString(R.string.dialog_input_server_name, toUri.host)
            val input = layout.findViewById<EditText>(R.id.dialog_input_field)
            if (sensitive) input.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            AlertDialog.Builder(this, R.style.Theme_GemBrowser_AlertDialog)
                .setTitle(if (title.isNotBlank()) title else getString(R.string.dialog_input_title))
                .setView(layout)
                .setPositiveButton(getString(R.string.dialog_input_submit)) { _, _ ->
                    ContentManager.requestUri(Uri.parse(toUri.toString() + "?" + Uri.encode(input.text.toString())))
                }
                .setNegativeButton(getString(R.string.general_cancel)) { _, _ -> }
                .show()
            Log.d("BrowserActivity", "Handling Input")
        }
    }

    // TODO: Rework Uri building in other classes
    fun assembleUriButtons(uri: Uri) {
        val current =  binding.uriCurrent
        val segmentsContainer = binding.uriSegmentsContainer
        segmentsContainer.removeAllViews()
        val newUri = uri.buildUpon().path("").query("")
        if (uri.pathSegments.size > 0){
            current.text = uri.lastPathSegment
            current.setPadding(current.paddingLeft, current.paddingTop, current.paddingRight, 0)
            val btnUriFirst = newUri.build()

            val btnLayoutFirst = layoutInflater.inflate(R.layout.button_uri_segment, null)
            val btnFirst = btnLayoutFirst.findViewById<MaterialButton>(R.id.uri_segment)
            btnFirst.text = uri.host
            if (uri.pathSegments.size == 1) btnFirst.icon = null
            btnFirst.setOnClickListener { ContentManager.requestUri(btnUriFirst) }

            segmentsContainer.addView(btnLayoutFirst)

            uri.pathSegments.forEachIndexed{
                    i: Int, s: String ->

                if (i < uri.pathSegments.size - 1) {
                    newUri.appendPath(s)
                    val btnUri = newUri.build()

                    val btnLayout = layoutInflater.inflate(R.layout.button_uri_segment, null)
                    val btn = btnLayout.findViewById<MaterialButton>(R.id.uri_segment)
                    btn.text = s
                    if (i == uri.pathSegments.size - 2) btn.icon = null
                    btn.setOnClickListener { ContentManager.requestUri(btnUri) }

                    segmentsContainer.addView(btnLayout)
                }
            }

        } else {
            current.text = uri.host
            current.setPadding(current.paddingLeft, current.paddingTop, current.paddingRight, dpToPx(this, 6f))
        }
    }
}