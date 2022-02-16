package com.sermah.gembrowser

import android.content.Context
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
import com.sermah.gembrowser.model.theming.FontManager
import com.sermah.gembrowser.model.theming.StyleManager
import com.sermah.gembrowser.databinding.ActivityBrowserBinding

import android.content.Intent
import android.os.Build
import android.view.View.*

import android.view.WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.WindowCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import com.sermah.gembrowser.data.content.ButtonsBarConfiguration
import com.sermah.gembrowser.databinding.BottomButtonsBarBinding
import com.sermah.gembrowser.databinding.BottomInputFieldBinding
import com.sermah.gembrowser.data.content.ContentLine
import com.sermah.gembrowser.data.content.IPage
import com.sermah.gembrowser.data.theming.AppColors
import com.sermah.gembrowser.data.theming.AppStyles
import com.sermah.gembrowser.viewmodel.BrowserActivityViewModel


class BrowserActivity : AppCompatActivity() {

    var backLeadsToExit : Boolean = true

    private lateinit var binding: ActivityBrowserBinding
    private lateinit var vm: BrowserActivityViewModel

    // assigned in assignViews()
    private lateinit var rvContent: RecyclerView
    private lateinit var btnHome: AppCompatImageButton
    private lateinit var btnBack: AppCompatImageButton
    private lateinit var btnMore: AppCompatImageButton
    private lateinit var btnContents: AppCompatImageButton
    private lateinit var btnBookmark: AppCompatImageButton
    private lateinit var fieldInput: EditText
    private lateinit var textTitle: TextView

    // assigned in inflate...()
    private lateinit var barButtons : LinearLayoutCompat
    private lateinit var barInput : FrameLayout

    // TODO: Move to viewModel
    private var styles: AppStyles = StyleManager.currentStyles

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBrowserBinding.inflate(layoutInflater)
        vm = ViewModelProvider(this).get(BrowserActivityViewModel::class.java)

        binding.root.isFocusableInTouchMode = true
        setContentView(binding.root)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
            resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_NO
        ) {
            window.decorView.systemUiVisibility = FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS or
                    SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR or SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }

        if(!StyleManager.loadedStyles){
            StyleManager.run {
                loadStyles(this@BrowserActivity, primaryName = "default_light")
                darkStyles = AppStyles(colors = primaryStyles.colors.inverted())
            }
        }

        // TODO: Move fonts to viewModel
        FontManager.loadFonts(assets)
        StyleManager.stylePre.typeface = FontManager.get("DejaVuSansMono.ttf") // TODO: Customization - font styles
        updateDarkMode(resources.configuration)
        binding.palette = styles.colors

        systemBarsColoring()
        inflateInputField()
        inflateButtons()
        assignViews()

        observeGlobal()
        bbBecomeTitle()

        textTitle.setOnClickListener{
            vm.viewOpenUriInput()
        }

        btnHome.setOnClickListener{ vm.viewOpenHomepage() }
        btnBack.setOnClickListener{ vm.viewGoBackward() }
        btnBack.setOnLongClickListener{ vm.viewShowHistory(); true }
        btnContents.setOnClickListener{ vm.viewShowContents() }
        // btnBookmark.setOnClickListener{} // Set in observeStuff()
        btnBookmark.setOnLongClickListener{ vm.viewShowBookmarks(); true }
        btnMore.setOnClickListener{
            // TODO: Inflate menu
        }

        (fieldInput.parent as View).clipToOutline = true

        rvContent.setHasFixedSize(true)

        // Load intent or homepage if no content
        if(intent?.data != null) vm.viewRequestUri(intent?.data!!)// TODO: Customization - homepage
        else { updateContent(null) }
    }

    private fun systemBarsColoring() {
        window.navigationBarColor = styles.colors.backgroundBottom
        window.statusBarColor = styles.colors.background
        val insetsController = WindowCompat.getInsetsController(window, binding.root)
        insetsController?.isAppearanceLightNavigationBars = AppColors.getLuminance(window.navigationBarColor) > 127
        insetsController?.isAppearanceLightStatusBars = AppColors.getLuminance(window.statusBarColor) > 127
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        updateDarkMode(newConfig)
    }

    override fun onBackPressed() {
        if (backLeadsToExit) super.onBackPressed()
        else vm.viewGoBackward()
    }

    private fun observeGlobal() {
        vm.bbState.observe(this, observerBottomBarState)
        vm.page.observe(this, observerPage)
        vm.bbButtonsConfig.observe(this, observerButtonsConfig)
        vm.intentUri.observe(this, observerIntentUri)
        vm.warningMessage.observe(this, observerMessage)
    }

    private fun updateDarkMode(cfg: Configuration) {
        when (cfg.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_YES -> StyleManager.isDark = true
            Configuration.UI_MODE_NIGHT_NO -> StyleManager.isDark = false
        }
        // StyleManager.updateStyles() is called automatically
        styles = StyleManager.currentStyles
    }

    private fun intentOutside(uri: Uri) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = uri
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        applicationContext.startActivity(intent)
    }

    private fun showMessage(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .setBackgroundTint(styles.colors.backgroundSnackbar)
            .setTextColor(styles.colors.textSnackbar)
            .setAnchorView(binding.bottomBar)
            .show()
    }

    //////////////////////////////////////////////
    // Inflating and assigning view to variables

    private fun inflateButtons(){
        val bbBinding = DataBindingUtil.inflate<BottomButtonsBarBinding>(
            layoutInflater,
            R.layout.bottom_buttons_bar,
            binding.bottomBarButtonsFrame,
            true)
        barButtons = bbBinding.root as LinearLayoutCompat
        bbBinding.palette = styles.colors
    }

    private fun inflateInputField(){
        val ibBinding = DataBindingUtil.inflate<BottomInputFieldBinding>(
            layoutInflater,
            R.layout.bottom_input_field,
            binding.inputFieldFrame,
            true)
        barInput = ibBinding.root as FrameLayout
        ibBinding.palette = styles.colors
    }

    private fun assignViews() {
        rvContent   = binding.contentRecyclerView
        textTitle   = binding.bottomBarTitle

        fieldInput  = barInput.findViewById(R.id.input_field)
        btnHome     = barButtons.findViewById(R.id.btn_homepage)
        btnMore     = barButtons.findViewById(R.id.btn_more)
        btnBack     = barButtons.findViewById(R.id.btn_history_back)
        btnContents = barButtons.findViewById(R.id.btn_contents)
        btnBookmark = barButtons.findViewById(R.id.btn_bookmark)
    }

    ////////////////////////////
    // Observers

    private val observerInput = Observer<String>{
        fieldInput.setText(it)
    }

    private val observerTitle = Observer<String>{
        binding.bottomBarTitle.text = it
    }

    private val observerBottomBarState = Observer<BrowserActivityViewModel.BottomBarState>{
        when (it) {
            BrowserActivityViewModel.BottomBarState.TITLE ->
                bbBecomeTitle()
            BrowserActivityViewModel.BottomBarState.URI_INPUT ->
                bbBecomeUriInput()
            BrowserActivityViewModel.BottomBarState.QUERY_INPUT ->
                bbBecomeQueryInput()
            // TODO: complete
            BrowserActivityViewModel.BottomBarState.BOOKMARKS ->
                bbBecomeBookmarks()
            BrowserActivityViewModel.BottomBarState.CONTENTS ->
                bbBecomeContents()
            BrowserActivityViewModel.BottomBarState.HISTORY ->
                bbBecomeHistory()
            null -> {}
        }
    }

    private val observerButtonsConfig = Observer<ButtonsBarConfiguration>{
        if (it == null) return@Observer
        btnBookmark.setImageResource(
            if (it.isBookmark) R.drawable.ic_bookmark
            else R.drawable.ic_bookmark_border
        )
        btnBookmark.setOnClickListener{ _ ->
            if (it.isBookmark) vm.viewDeleteBookmark()
            else vm.viewAddBookmark()
        }
        btnContents.isEnabled = it.hasContents
        btnBack.isEnabled = it.canBack
        backLeadsToExit = !it.canBack

        btnContents.alpha = if (btnContents.isEnabled) 1f else 0.5f
        btnBack.alpha = if (btnBack.isEnabled) 1f else 0.5f
    }

    private val observerPage = Observer<IPage>{
        updateContent(it.content)
    }

    private val observerIntentUri = Observer<Uri> {
        intentOutside(it)
    }

    private val observerMessage = Observer<String> {
        if (it.isNotBlank()) showMessage(it)
    }

    ///////////////////////////////
    // Bottom bar transformations

    private fun bbBecomeTitle() {
        // Observers match those of UriInput state
        vm.bbQueryPrompt.removeObserver(observerTitle)
        vm.bbQueryInput.removeObserver(observerInput)

        vm.bbTitle.observe(this, observerTitle)
        vm.bbUriInput.observe(this, observerInput)

        // Show title, hide input field
        textTitle.visibility = View.VISIBLE
        fieldInput.visibility = View.GONE
        textTitle.isClickable = true
        fieldInput.setOnKeyListener(::uriInput)
        fieldInput.setHint(R.string.field_uri_hint)
    }

    private fun bbBecomeUriInput() {
        // Observers match those of Title state
        vm.bbQueryPrompt.removeObserver(observerTitle)
        vm.bbQueryInput.removeObserver(observerInput)

        vm.bbTitle.observe(this, observerTitle)
        vm.bbUriInput.observe(this, observerInput)

        // Hide title, show input field
        textTitle.visibility = View.GONE
        fieldInput.visibility = View.VISIBLE
        textTitle.isClickable = false
        fieldInput.setOnKeyListener(::uriInput)
        fieldInput.setHint(R.string.field_uri_hint)

        fieldInput.selectAll()
        focusInput()
    }

    private fun bbBecomeQueryInput() {
        vm.bbUriInput.removeObserver(observerInput)
        vm.bbTitle.removeObserver(observerTitle)

        vm.bbQueryInput.observe(this, observerInput)
        vm.bbQueryPrompt.observe(this, observerTitle)

        // Show title (prompt) and input field
        textTitle.visibility = View.VISIBLE
        fieldInput.visibility = View.VISIBLE
        textTitle.isClickable = false
        fieldInput.setOnKeyListener(::queryInput)
        fieldInput.setHint(R.string.field_query_hint)

        fieldInput.append("")
        focusInput()

        // TODO: Think about adding SEND button
    }

    private fun bbBecomeBookmarks() {
        vm.bbQueryInput.removeObserver(observerInput)
        vm.bbUriInput.removeObserver(observerInput)
        vm.bbTitle.removeObserver(observerTitle)
        vm.bbQueryPrompt.removeObserver(observerTitle)

        textTitle.setText(R.string.title_bookmarks)
        textTitle.isClickable = false
        textTitle.visibility = View.VISIBLE
        fieldInput.visibility = View.GONE
    }

    private fun bbBecomeContents() {
        vm.bbQueryInput.removeObserver(observerInput)
        vm.bbUriInput.removeObserver(observerInput)
        vm.bbTitle.removeObserver(observerTitle)
        vm.bbQueryPrompt.removeObserver(observerTitle)

        textTitle.setText(R.string.title_contents)
        textTitle.isClickable = false
        textTitle.visibility = View.VISIBLE
        fieldInput.visibility = View.GONE
    }

    private fun bbBecomeHistory() {
        vm.bbQueryInput.removeObserver(observerInput)
        vm.bbUriInput.removeObserver(observerInput)
        vm.bbTitle.removeObserver(observerTitle)
        vm.bbQueryPrompt.removeObserver(observerTitle)

        textTitle.setText(R.string.title_history)
        textTitle.isClickable = false
        textTitle.visibility = View.VISIBLE
        fieldInput.visibility = View.GONE
    }

    ////////////////////////////
    // Content update

    private fun updateContent(lines: List<ContentLine>?) {
        if (lines != null)
            rvContent.adapter = ContentAdapter(this, lines)
        //assembleUriButtons(ContentManager.currentTab.currentPage.uri)
        fieldInput.clearFocus()
    }

    ////////////////////////////
    // Input field

    private fun uriInput(v: View, k: Int, e: KeyEvent): Boolean {
        if((e.action != KeyEvent.ACTION_DOWN) || (k != KeyEvent.KEYCODE_ENTER)) return false
        if(fieldInput.text.isNotBlank())
            vm.viewRequestUri(fieldInput.text.toString().trim())
        return true
    }

    private fun queryInput(v: View, k: Int, e: KeyEvent): Boolean {
        if((e.action != KeyEvent.ACTION_DOWN) || (k != KeyEvent.KEYCODE_ENTER)) return false
        vm.viewSendQuery(fieldInput.text.toString())
        return true
    }

    private fun focusInput() {
        fieldInput.requestFocus()
        val imm: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(fieldInput, InputMethodManager.SHOW_IMPLICIT)
    }
}