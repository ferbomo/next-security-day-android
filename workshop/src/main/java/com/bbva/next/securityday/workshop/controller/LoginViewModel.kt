package com.bbva.next.securityday.workshop.controller

import android.content.Context
import com.bbva.next.securityday.workshop.Callable

class LoginViewModel(private val context: Context) {

    companion object {
        private const val REG_AUDIO = "reg.wav"
        private const val AUTH_AUDIO = "auth.wav"
        private const val MIN_CONFIDENCE_NUMBER = 0.75
    }

    enum class Event {
        REGISTRATION,
        AUTHENTICATION,
        AUTHENTICATING
    }

    private val audioManager = Step1()
    private val networkManager = Step2()
    private val assistantManager = Step3()

    /**
     * Log in a user with the desired email. If the user is not registered, it will be registered automatically.
     * @param email the user email
     * @param onEvent the type of event that will be notified to the caller moments before it is performed
     * @param onRecording the
     */
    fun login(
        email: String?,
        onEvent: Callable<Event>,
        onRecording: Callable<Int>,
        onSuccess: Callable<Void>,
        onFailure: Callable<Throwable>
    ) {
        if (email.isNullOrEmpty()) {
            onFailure.call(Exception("El email no puede estar vacío"))
            return
        }
        if (!email.isEmailValid()) {
            onFailure.call(Exception("El email introducido no es válido"))
            return
        }

        audioManager.saveIfNeeded(context, REG_AUDIO, false, {
            onEvent.call(Event.REGISTRATION)
        }, onRecording, {
            networkManager.registerIfNeeded(email, {
                authenticate(onEvent, onRecording, onSuccess, onFailure)
            }, onFailure)
        }, onFailure)
    }

    private fun authenticate(
        onEvent: Callable<Event>,
        onRecording: Callable<Int>,
        onSuccess: Callable<Void>,
        onFailure: Callable<Throwable>
    ) {
        context.deleteFile(AUTH_AUDIO)
        onEvent.call(Event.AUTHENTICATION)
        audioManager.saveIfNeeded(context, AUTH_AUDIO, true, null, onRecording, {
            onEvent.call(Event.AUTHENTICATING)
            networkManager.authenticate(context, REG_AUDIO, AUTH_AUDIO, {
                when {
                    it.confidenceNumber >= MIN_CONFIDENCE_NUMBER -> {
                        onSuccess.call(null)
                        assistantManager.openGala(context)
                        context.deleteFile(AUTH_AUDIO)
                    }
                    else -> onFailure.call(Exception("Voice does not match"))
                }
            }, onFailure)
        }, onFailure)
    }

    fun logout(
        email: String?,
        onSuccess: Callable<Void>,
        onFailure: Callable<Throwable>
    ) {

        if (email.isNullOrEmpty()) {
            onFailure.call(Exception("El email no puede estar vacío"))
            return
        }
        if (!email.isEmailValid()) {
            onFailure.call(Exception("El email introducido no es válido"))
            return
        }

        context.apply {
            deleteFile(REG_AUDIO)
            deleteFile(AUTH_AUDIO)
        }
        networkManager.isRegistered(email, {
            networkManager.unregisterEmail(email, onSuccess, onFailure)
        }, {
            if (it.message?.contains("404") == true) {
                onSuccess.call(null)
            } else {
                onFailure.call(it)
            }
        })
    }
}


/**
 * Save an audio at filename if it is not valid or not present. If the audio file exists and is valid,
 * This method calls `onSuccess` directly. Force saving the audio using the flag `force = true`.
 * @param context the Android context.
 * @param filename the desired filename to save.
 * @param force whether or not this operation should be forced by deleting filename first if it exists.
 * @param onEvent optional event that will be called if the audio file did not exist, was not valid, or if saving was forced.
 * @param onSuccess callback when the operation succeeds
 * @param onFailure callback when the operation fails
 */
private fun Step1.saveIfNeeded(
    context: Context,
    filename: String,
    force: Boolean = true,
    onEvent: (() -> Unit)? = null,
    onRecording: Callable<Int>,
    onSuccess: Callable<Void>,
    onFailure: Callable<Throwable>
) {
    if (force) {
        context.deleteFile(filename)
    }
    if (context.fileExists(filename)) {
        validateAudio(context, filename, onSuccess, {
            onEvent?.let { it() }
            recordAudio(context, onRecording, {
                saveAudio(context, it, filename, onSuccess, onFailure)
            }, onFailure)
        })
    }
    else {
        onEvent?.let { it() }
        recordAudio(context, onRecording, {
            saveAudio(context, it, filename, onSuccess, onFailure)
        }, onFailure)
    }
}

/**
 * Register an email if it is not already registered.
 */
private fun Step2.registerIfNeeded(
    email: String,
    onSuccess: Callable<Void>,
    onFailure: Callable<Throwable>
) {
    isRegistered(email, onSuccess, {
        registerEmail(email, { onSuccess.call(null) }, onFailure)
    })
}

private fun String.isEmailValid(): Boolean {
    return android.util.Patterns.EMAIL_ADDRESS.matcher(this).matches()
}

private fun Context.fileExists(filename: String) = fileList().contains(filename)

private fun Context.fileNotExists(filename: String) = !fileExists(filename)