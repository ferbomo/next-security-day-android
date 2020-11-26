package com.bbva.next.securityday.workshop.controller;

import android.content.Context;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.core.os.HandlerCompat;
import androidx.core.util.Pair;

import com.bbva.next.securityday.workshop.Callable;
import com.bbva.next.securityday.workshop.audio.VoiceCapture;
import com.bbva.next.securityday.workshop.audio.impl.WaveVoiceCapture;

import java.io.FileOutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class Step1 {

    final VoiceCapture voiceCapture = new WaveVoiceCapture();

    public static final long RECORD_DURATION_MILLIS = 10000L;

    CountDownTimer countdown = null;

    public void recordAudio(@NonNull final Context context,
                            @NonNull final Callable<Integer> onSecondsLeft,
                            @NonNull final Callable<byte[]> onSuccess,
                            @NonNull final Callable<Throwable> onFailure) {

        countdown = new CountDownTimer(RECORD_DURATION_MILLIS, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                onSecondsLeft.call((int) (millisUntilFinished / 1000));
            }

            @Override
            public void onFinish() {
                voiceCapture.stop();
                countdown = null;
            }
        };

        try {
            countdown.start();
            voiceCapture.start(context, onSuccess::call);
        } catch (final Throwable throwable) {
            onFailure.call(throwable);
        }
    }

    public void saveAudio(@NonNull final Context context,
                          @NonNull byte[] bytes,
                          @NonNull final String filename,
                          @NonNull final Callable<Void> onSuccess,
                          @NonNull final Callable<Throwable> onFailure) {
        try {
            final FileOutputStream os = context.openFileOutput(filename, Context.MODE_PRIVATE);
            os.write(bytes);
            os.flush();
            os.close();
            onSuccess.call(null);
        } catch (final Throwable throwable) {
            onFailure.call(throwable);
        }
    }

    public void validateAudio(@NonNull final Context context,
                              @NonNull final String filename,
                              @NonNull final Callable<Void> onSuccess,
                              @NonNull final Callable<Throwable> onFailure) {

        final ExecutorService executor = Executors.newSingleThreadExecutor();
        final Handler mainHandler = HandlerCompat.createAsync(Looper.getMainLooper());

        // Run work in a background thread
        executor.execute(() -> {

            try {
                final String filePath = context.getFileStreamPath(filename).getPath();
                final Pair<Boolean, String> result = voiceCapture.isWavValid(filePath);

                // Call back using the main UI thread
                mainHandler.post(() -> {
                    if (result.first) {
                        onSuccess.call(null);
                    } else {
                        onFailure.call(new Exception(result.second));
                    }
                });
            } catch (final Throwable throwable) {
                // Call back using the main UI thread
                mainHandler.post(() -> onFailure.call(throwable));
            }
        });
    }
}
