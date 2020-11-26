package com.bbva.next.securityday.workshop.audio.impl;

/*
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;

import com.bbva.next.securityday.workshop.audio.VoiceCapture;
import com.dasnano.sdkvoice.VDVoiceCapture;
import com.dasnano.sdkvoice.VeridasResult;

public class DaspeakVoiceCapture implements VoiceCapture {
    @Override
    public void start(@NonNull Context context, @NonNull Callback callback) {
        VDVoiceCapture.start(new VDVoiceCapture.IVDVoiceCapture() {
            @Override
            public void VDBytesRecorded(byte[] bytes) {
                callback.onBytesRecorded(bytes);
            }

            @Override
            public void VDVoiceCaptureFinished() {
                // Ignore
            }
        }, context);
    }

    @Override
    public void stop() {
        VDVoiceCapture.stop();
    }

    @Override
    public Pair<Boolean, String> isWavValid(@NonNull String filePath) {

        final VeridasResult result = VDVoiceCapture.isWavValid(filePath);
        return new Pair<>(result._result, result._message);
    }
}
*/
