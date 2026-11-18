package com.example.ime

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.core.content.ContextCompat
import com.example.model.VetroSpeechState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class VetroSpeechManager(
    private val context: Context,
    private val scope: CoroutineScope,
) {
    private val _speechState = MutableStateFlow(VetroSpeechState())
    val speechState: StateFlow<VetroSpeechState> = _speechState.asStateFlow()

    private var speechRecognizer: SpeechRecognizer? = null
    private var lastCommittedPartial: String = ""

    fun hasRecordPermission(): Boolean {
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO,
        ) == PackageManager.PERMISSION_GRANTED
        _speechState.update { it.copy(hasPermission = granted) }
        return granted
    }

    fun updateHoldProgress(progress: Float, isHolding: Boolean) {
        _speechState.update {
            it.copy(
                holdProgress = progress.coerceIn(0f, 1f),
                isHolding = isHolding,
            )
        }
    }

    fun startListening(onTextRecognized: (String) -> Unit) {
        if (!hasRecordPermission()) {
            _speechState.update {
                it.copy(
                    isListening = false,
                    errorMessage = "Microphone permission required for voice typing",
                )
            }
            return
        }

        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            _speechState.update {
                it.copy(
                    isListening = false,
                    errorMessage = "Speech recognition is not available on this device",
                )
            }
            return
        }

        stopListening()
        lastCommittedPartial = ""

        try {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        _speechState.update {
                            it.copy(isListening = true, errorMessage = null, partialText = "")
                        }
                    }

                    override fun onBeginningOfSpeech() {
                        _speechState.update { it.copy(isListening = true) }
                    }

                    override fun onRmsChanged(rmsdB: Float) {
                        // Normalize -2dB..10dB to 0f..1f for fluid waveform scaling
                        val norm = ((rmsdB + 2f) / 12f).coerceIn(0.05f, 1f)
                        _speechState.update { it.copy(rmsDb = norm) }
                    }

                    override fun onBufferReceived(buffer: ByteArray?) {}

                    override fun onEndOfSpeech() {
                        _speechState.update { it.copy(rmsDb = 0f) }
                    }

                    override fun onError(error: Int) {
                        // If no match or timeout, return to normal listening state
                        _speechState.update {
                            it.copy(
                                isListening = false,
                                rmsDb = 0f,
                                errorMessage = if (error == SpeechRecognizer.ERROR_NO_MATCH) null else "Listening ended",
                            )
                        }
                    }

                    override fun onResults(results: Bundle?) {
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val text = matches?.firstOrNull()?.trim()
                        if (!text.isNullOrEmpty()) {
                            // If partial was already appended, append only difference or commit
                            if (lastCommittedPartial.isEmpty()) {
                                onTextRecognized("$text ")
                            } else if (text.startsWith(lastCommittedPartial, ignoreCase = true)) {
                                val remaining = text.substring(lastCommittedPartial.length).trim()
                                if (remaining.isNotEmpty()) onTextRecognized("$remaining ")
                            }
                        }
                        _speechState.update {
                            it.copy(isListening = false, rmsDb = 0f, partialText = "")
                        }
                        lastCommittedPartial = ""
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val text = matches?.firstOrNull()?.trim()
                        if (!text.isNullOrEmpty()) {
                            _speechState.update { it.copy(partialText = text) }
                            if (text.length > lastCommittedPartial.length && text.startsWith(lastCommittedPartial, ignoreCase = true)) {
                                val delta = text.substring(lastCommittedPartial.length)
                                onTextRecognized(delta)
                                lastCommittedPartial = text
                            }
                        }
                    }

                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            }
            speechRecognizer?.startListening(intent)
            _speechState.update { it.copy(isListening = true, errorMessage = null) }
        } catch (e: Exception) {
            _speechState.update {
                it.copy(isListening = false, errorMessage = e.localizedMessage)
            }
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.destroy()
        } catch (_: Exception) {}
        speechRecognizer = null
        lastCommittedPartial = ""
        _speechState.update { it.copy(isListening = false, rmsDb = 0f, partialText = "") }
    }

    fun release() {
        stopListening()
    }
}
