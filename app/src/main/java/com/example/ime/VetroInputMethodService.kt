package com.example.ime

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color as AndroidColor
import android.graphics.drawable.ColorDrawable
import android.inputmethodservice.InputMethodService
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.MainActivity
import com.example.data.KeyboardPrefsRepository
import com.example.model.KeyboardSettings
import com.example.ui.keyboard.VetroKeyboardSurface
import com.example.ui.theme.VetroTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class VetroInputMethodService :
    InputMethodService(),
    LifecycleOwner,
    SavedStateRegistryOwner,
    ViewModelStoreOwner {

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    private val store = ViewModelStore()
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private lateinit var prefsRepository: KeyboardPrefsRepository
    private lateinit var controller: KeyboardController
    private val latestSettings = MutableStateFlow(KeyboardSettings())

    private var clipboardManager: ClipboardManager? = null
    private val clipboardListener = ClipboardManager.OnPrimaryClipChangedListener {
        captureClipboardFromSystem()
    }

    override val lifecycle: Lifecycle
        get() = lifecycleRegistry

    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    override val viewModelStore: ViewModelStore
        get() = store

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.currentState = Lifecycle.State.CREATED

        prefsRepository = KeyboardPrefsRepository.getInstance(applicationContext)
        val inputTarget = SystemImeInputTarget(
            service = this,
            onClipboardCaptured = { text ->
                serviceScope.launch { prefsRepository.addClipboardText(text) }
            },
            onOpenSettings = {
                val intent = Intent(this, MainActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                startActivity(intent)
            },
        )
        val speechManager = VetroSpeechManager(this, serviceScope)
        val soundManager = VetroSoundManager(this)
        controller = KeyboardController(
            inputTarget = inputTarget,
            prefsRepository = prefsRepository,
            scope = serviceScope,
            speechManager = speechManager,
            soundManager = soundManager,
        )

        serviceScope.launch {
            prefsRepository.settingsFlow.collect { settings ->
                latestSettings.value = settings
            }
        }

        clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        clipboardManager?.addPrimaryClipChangedListener(clipboardListener)
    }

    override fun onEvaluateFullscreenMode(): Boolean {
        // Keep Vetro non-obstructive in all orientations unless explicitly tiny landscape
        return false
    }

    override fun onCreateInputView(): View {
        window?.window?.apply {
            setBackgroundDrawable(ColorDrawable(AndroidColor.TRANSPARENT))
            decorView.setViewTreeLifecycleOwner(this@VetroInputMethodService)
            decorView.setViewTreeSavedStateRegistryOwner(this@VetroInputMethodService)
            decorView.setViewTreeViewModelStoreOwner(this@VetroInputMethodService)
        }

        return ComposeView(this).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setViewTreeLifecycleOwner(this@VetroInputMethodService)
            setViewTreeSavedStateRegistryOwner(this@VetroInputMethodService)
            setViewTreeViewModelStoreOwner(this@VetroInputMethodService)
            setContent {
                val settings by latestSettings.collectAsState()
                val uiState by controller.uiState.collectAsState()
                VetroTheme {
                    VetroKeyboardSurface(
                        settings = settings,
                        uiState = uiState,
                        controller = controller,
                        isSystemIme = true,
                    )
                }
            }
        }
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        lifecycleRegistry.currentState = Lifecycle.State.RESUMED
        controller.onStartInput(info, latestSettings.value)
        captureClipboardFromSystem()
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        super.onFinishInputView(finishingInput)
        if (lifecycleRegistry.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            lifecycleRegistry.currentState = Lifecycle.State.CREATED
        }
    }

    override fun onDestroy() {
        clipboardManager?.removePrimaryClipChangedListener(clipboardListener)
        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
        store.clear()
        serviceScope.cancel()
        super.onDestroy()
    }

    private fun captureClipboardFromSystem() {
        val clip = clipboardManager?.primaryClip ?: return
        if (clip.itemCount <= 0) return
        val text = clip.getItemAt(0)?.coerceToText(this)?.toString()
        if (!text.isNullOrBlank()) {
            serviceScope.launch {
                prefsRepository.addClipboardText(text)
            }
        }
    }
}
