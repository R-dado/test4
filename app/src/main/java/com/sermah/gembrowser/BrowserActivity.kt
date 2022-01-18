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
import com.sermah.gembrowser.data.content.ContentManager
import com.sermah.gembrowser.data.theming.FontManager
import com.sermah.gembrowser.data.theming.StyleManager
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
import android.widget.Button
import android.widget.HorizontalScrollView
import androidx.core.view.WindowCompat
import com.sermah.gembrowser.model.content.ContentLine
import com.sermah.gembrowser.model.theming.AppColors
import com.sermah.gembrowser.model.theming.AppStyles


class BrowserActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBrowserBinding
    private lateinit var contentRV: RecyclerView
    private lateinit var uriField: EditText
    private lateinit var homeButton: AppCompatImageButton
    private lateinit var menuButton: AppCompatImageButton
    private lateinit var infoBar: LinearLayoutCompat
    private var styles: AppStyles = StyleManager.currentStyles

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
            onSuccess = {_, lines -> updateContent(lines)},
            onInput         = { code, info, uri ->
                handleInput(info, uri, code == "11")
            },
            onRedirect      = { _, toUri, uri ->
                val to = Uri.parse(toUri)
                if (!to.equals(uri))
                    ContentManager.requestUri(to)
            },
            onTemporary     = tempHandleNonSuccess,
            onPermanent     = tempHandleNonSuccess,
            onCertificate   = tempHandleNonSuccess,
        )
        ContentManager.onNonGeminiScheme = {uri -> handleNonGeminiScheme(uri)}

        if(!StyleManager.loadedStyles){
            StyleManager.run {
                loadStyles(this@BrowserActivity, primaryName = "default_light")
                darkStyles = AppStyles(colors = primaryStyles.colors.inverted())
            }
        }

        FontManager.loadFonts(assets)
        StyleManager.stylePre.typeface = FontManager.get("DejaVuSansMono.ttf") // TODO: Customization - font styles
        updateDarkMode(resources.configuration)
        binding.appColors = styles.colors

        window.navigationBarColor = styles.colors.backgroundBottom
        window.statusBarColor = styles.colors.background
        val insetsController = WindowCompat.getInsetsController(window, binding.root)
        insetsController?.isAppearanceLightNavigationBars = AppColors.getLuminance(window.navigationBarColor) > 127
        insetsController?.isAppearanceLightStatusBars = AppColors.getLuminance(window.statusBarColor) > 127


        // TODO: Create ColorHook, hook views to their respective colors, so hooks update views on color change

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
        uriField.setText(ContentManager.currentTab.currentPage.uri.toString())
        (uriField.parent as View).clipToOutline = true

        contentRV.setHasFixedSize(true)
        contentRV.adapter = ContentAdapter(this, ContentManager.currentTab.currentPage.lines)
        if(intent?.data != null) ContentManager.requestUri(intent?.data!!)
        else if(ContentManager.currentTab.currentPage.lines.isEmpty())
            ContentManager.requestUri(Uri.parse(homepage)) // TODO: Customization - homepage
        else {
            updateContent(null)
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        updateDarkMode(newConfig)
    }

    override fun onBackPressed() {
        if (!ContentManager.loadPreviousPage()) super.onBackPressed()
    }

    fun updateDarkMode(cfg: Configuration) {
        when (cfg.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_YES -> StyleManager.isDark = true
            Configuration.UI_MODE_NIGHT_NO -> StyleManager.isDark = false
        }
        // StyleManager.updateStyles() is called automatically
        styles = StyleManager.currentStyles
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

    private fun updateContent(lines: List<ContentLine>?) {
        runOnUiThread {
            if (lines != null)
                contentRV.adapter = ContentAdapter(this, lines)
            assembleUriButtons(ContentManager.currentTab.currentPage.uri)
            uriField.setText(ContentManager.currentTab.currentPage.uri.toString())
            uriField.clearFocus()
        }
    }

    // TODO: Rework Uri building in other classes
    fun assembleUriButtons(uri: Uri) {
        val current = binding.uriCurrent
        val segmentsContainer = binding.uriSegmentsContainer
        segmentsContainer.removeAllViews()
        val newUri = uri.buildUpon().path("").query("")

        current.text = if (uri.pathSegments.size > 0) uri.lastPathSegment else uri.host
        val btnUriFirst = newUri.build()

        val btnLayoutFirst = layoutInflater.inflate(R.layout.button_uri_segment, null)
        btnLayoutFirst.clipToOutline = true
        val btnFirst = btnLayoutFirst.findViewById<Button>(R.id.uri_segment_btn)
        btnFirst.text = (uri.host ?: "...") + if (uri.pathSegments.size > 0) " ›" else ""
        btnFirst.setTextColor(styles.colors.textBottomSegments)
        btnFirst.setOnClickListener { ContentManager.requestUri(btnUriFirst) }

        segmentsContainer.addView(btnLayoutFirst)

        uri.pathSegments.forEachIndexed{
                i: Int, s: String ->

            newUri.appendPath(s)
            val btnUri = newUri.build()

            val btnLayout = layoutInflater.inflate(R.layout.button_uri_segment, null)
            btnLayout.clipToOutline = true
            val btn = btnLayout.findViewById<Button>(R.id.uri_segment_btn)
            btn.setTextColor(styles.colors.textBottomSegments)
            if(i < uri.pathSegments.size - 1) {
                btn.text = "$s ›"
                btn.setOnClickListener { ContentManager.requestUri(btnUri) }
            } else {
                btn.text = s
                btn.isEnabled = false
            }

            segmentsContainer.addView(btnLayout)
        }
    }
}