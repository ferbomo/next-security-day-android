package com.bbva.next.securityday.workshop.audio;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;

public interface VoiceCapture {

    interface Callback {
        void onBytesRecorded(byte[] bytes);
    }

    void start(@NonNull final Context context, @NonNull final Callback callback);

    void stop();

    @NonNull
    Pair<Boolean, String> isWavValid(@NonNull final String filePath);
}
