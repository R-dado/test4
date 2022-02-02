package com.sermah.gembrowser

import android.content.res.Configuration
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
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
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build

import android.text.InputType
import android.view.Gravity
import android.view.View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
import android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
import android.view.ViewGroup

import android.view.WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.WindowCompat
import androidx.core.view.children
import androidx.databinding.DataBindingUtil
import com.google.android.material.snackbar.Snackbar
import com.sermah.gembrowser.databinding.BottomButtonsBarBinding
import com.sermah.gembrowser.databinding.BottomInputFieldBinding
import com.sermah.gembrowser.model.content.ContentLine
import com.sermah.gembrowser.model.theming.AppColors
import com.sermah.gembrowser.model.theming.AppStyles


class BrowserActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBrowserBinding
    private lateinit var contentRV: RecyclerView
    private lateinit var uriField: EditText
    private lateinit var homeButton: AppCompatImageButton
    private lateinit var prevButton: AppCompatImageButton
    private lateinit var moreButton: AppCompatImageButton
    private lateinit var buttonsBar : LinearLayoutCompat
    private lateinit var inputBar : FrameLayout
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
            onSuccess = {_, lines -> updateContent(lines) },
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
        binding.palette = styles.colors

        window.navigationBarColor = styles.colors.backgroundBottom
        window.statusBarColor = styles.colors.background
        val insetsController = WindowCompat.getInsetsController(window, binding.root)
        insetsController?.isAppearanceLightNavigationBars = AppColors.getLuminance(window.navigationBarColor) > 127
        insetsController?.isAppearanceLightStatusBars = AppColors.getLuminance(window.statusBarColor) > 127

        inflateInputField()
        inflateButtons()

        homeButton = buttonsBar.findViewById(R.id.btn_homepage)
        moreButton = buttonsBar.findViewById(R.id.btn_more)
        prevButton = buttonsBar.findViewById(R.id.btn_history_back)
        uriField = inputBar.findViewById(R.id.input_field)
        contentRV = binding.contentRecyclerView

        homeButton.setOnClickListener{ ContentManager.requestUri(Uri.parse(homepage)) }
        prevButton.setOnClickListener{ ContentManager.loadPreviousPage() }

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
            updateTitle()
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
        val message = if (meta.isNotBlank()) "Error $code: ${meta.trim()}" else "Error $code"
        runOnUiThread {
            Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
                .setBackgroundTint(styles.colors.backgroundSnackbar)
                .setTextColor(styles.colors.textSnackbar)
                .setAnchorView(binding.bottomBar)
                .show()
        }
    }

    fun inflateButtons(){
        val bbBinding = DataBindingUtil.inflate<BottomButtonsBarBinding>(
            layoutInflater,
            R.layout.bottom_buttons_bar,
            binding.bottomBarButtonsFrame,
            true)
        buttonsBar = bbBinding.root as LinearLayoutCompat
        bbBinding.palette = styles.colors
    }

    fun inflateInputField(){
        val ibBinding = DataBindingUtil.inflate<BottomInputFieldBinding>(
            layoutInflater,
            R.layout.bottom_input_field,
            binding.inputFieldFrame,
            true)
        inputBar = ibBinding.root as FrameLayout
        ibBinding.palette = styles.colors
    }

    fun updateTitle() {
        binding.bottomBarTitle.text =
            ContentManager.currentTab.currentPage.run { "$favicon ${getTitle()}" }
    }

    fun handleInput(title: String = "", toUri: Uri, sensitive: Boolean = false) { // TODO: Remove all hardcoded strings
        runOnUiThread {
            val layout = layoutInflater.inflate(R.layout.dialog_input, null)
            layout.findViewById<TextView>(R.id.dialog_input_server_name).text =
                getString(R.string.dialog_input_server_name, toUri.host)
            layout.background = ContextCompat.getDrawable(this, R.drawable.bg_popup_rounded)?.also{
                DrawableCompat.setTint(it, styles.colors.background)
            }
            (layout as ViewGroup).children.forEach {
                when (it) {
                    is Button -> it.setTextColor(styles.colors.accent)
                    is TextView -> it.setTextColor(styles.colors.defaultText)
                }
            }
            layout.findViewById<TextView>(R.id.dialog_input_title).text =
                if (title.isNotBlank()) title else getString(R.string.dialog_input_title)
            val input = layout.findViewById<EditText>(R.id.dialog_input_field)
            if (sensitive) input.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD

            val dialog = AlertDialog.Builder(this, R.style.Theme_GemBrowser_AlertDialog)
                .setView(layout)
                .show()
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            layout.findViewById<Button>(R.id.dialog_input_btn_send).run {
                text = resources.getString(R.string.dialog_input_submit)
                setOnClickListener {
                    ContentManager.requestUri(Uri.parse(toUri.toString() + "?" + Uri.encode(input.text.toString())))
                    dialog.cancel()
                }
                setTextColor(styles.colors.accent)
            }
            layout.findViewById<Button>(R.id.dialog_input_btn_cancel).run {
                text = resources.getString(R.string.general_cancel)
                setOnClickListener {
                    dialog.cancel()
                }
                setTextColor(styles.colors.accent)
            }
            // Log.d("BrowserActivity", "Handling Input")
        }
    }

    private fun updateContent(lines: List<ContentLine>?) {
        runOnUiThread {
            if (lines != null)
                contentRV.adapter = ContentAdapter(this, lines)
            //assembleUriButtons(ContentManager.currentTab.currentPage.uri)
            updateTitle()
            uriField.setText(ContentManager.currentTab.currentPage.uri.toString())
            uriField.clearFocus()
            prevButton.isEnabled = ContentManager.currentTab.backSize > 0
            prevButton.alpha = if (prevButton.isEnabled) 1f else 0.5f
        }
    }
}