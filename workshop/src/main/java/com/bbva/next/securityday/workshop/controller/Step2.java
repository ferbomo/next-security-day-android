package com.bbva.next.securityday.workshop.controller;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.core.os.HandlerCompat;

import com.bbva.next.securityday.workshop.Callable;
import com.bbva.next.securityday.workshop.model.User;
import com.bbva.next.securityday.workshop.model.VoiceAuthentication;
import com.google.gson.Gson;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

import static com.bbva.next.securityday.workshop.network.Constants.ACCEPTS;
import static com.bbva.next.securityday.workshop.network.Constants.APPLICATION_JSON;
import static com.bbva.next.securityday.workshop.network.Constants.APPLICATION_OCTET_STREAM;
import static com.bbva.next.securityday.workshop.network.Constants.CONTENT_TYPE;
import static com.bbva.next.securityday.workshop.network.Constants.MIDDLEWARE_AUTH_URL;
import static com.bbva.next.securityday.workshop.network.Constants.MIDDLEWARE_URL;
import static com.bbva.next.securityday.workshop.network.Constants.MULTIPART_FORM;

public final class Step2 {

    private static final HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();

    static {
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BASIC);
    }

    public void registerEmail(@NonNull final String email,
                              @NonNull final Callable<User> onSuccess,
                              @NonNull final Callable<Throwable> onFailure) {
        // Petición:
        // POST
        // url:
        //     Constants.MIDDLEWARE_URL
        // headers:
        //     Accept: application/json
        //     Content-Type: application/json
        // body:
        //    {
        //        "userId": "<email>"
        //    }

        final ExecutorService executor = Executors.newSingleThreadExecutor();
        final Handler mainHandler = HandlerCompat.createAsync(Looper.getMainLooper());

        // Run work in a background thread
        executor.execute(() -> {
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(loggingInterceptor)
                    .build();

            try {

                final User user = new User();
                user.setUserId(email);
                final Gson gson = new Gson();
                final String json = gson.toJson(user);

                final RequestBody body = RequestBody.create(json, MediaType.parse(APPLICATION_JSON));

                final Request request = new Request.Builder()
                        .url(MIDDLEWARE_URL)
                        .addHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .addHeader(ACCEPTS, APPLICATION_JSON)
                        .post(body)
                        .build();

                final Response response = client.newCall(request).execute();
                final User registeredUser = gson.fromJson(response.body().string(), User.class);

                // Call back using the main UI thread
                mainHandler.post(() -> onSuccess.call(registeredUser));

            } catch (Throwable e) {
                // Call back using the main UI thread
                mainHandler.post(() -> onFailure.call(e));
            }
        });
    }

    public void isRegistered(@NonNull final String email,
                             @NonNull final Callable<Void> onSuccess,
                             @NonNull final Callable<Throwable> onFailure) {
        // Petición:
        // GET
        // url:
        //     Constants.MIDDLEWARE_URL/<email>
        // headers:
        //     Accept: application/json
        //     Content-Type: application/json

        final ExecutorService executor = Executors.newSingleThreadExecutor();
        final Handler mainHandler = HandlerCompat.createAsync(Looper.getMainLooper());

        // Run work in a background thread
        executor.execute(() -> {
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(loggingInterceptor)
                    .build();

            try {

                final Request request = new Request.Builder()
                        .url(MIDDLEWARE_URL + email)
                        .addHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .addHeader(ACCEPTS, APPLICATION_JSON)
                        .get()
                        .build();

                final Response response = client.newCall(request).execute();

                // Call back using the main UI thread
                mainHandler.post(() -> {

                    if (response.code() < 400) {
                        onSuccess.call(null);
                    } else if (response.code() == 404) {
                        onFailure.call(new Exception("El email " + email + " no está registrado"));
                    } else {
                        onFailure.call(new Exception("Error: returned status code is " + response.code()));
                    }
                });

            } catch (Throwable e) {
                // Call back using the main UI thread
                mainHandler.post(() -> onFailure.call(e));
            }
        });
    }

    public void authenticate(@NonNull final Context context,
                             @NonNull final String anchorAudio,
                             @NonNull final String targetAudio,
                             @NonNull final Callable<VoiceAuthentication> onSuccess,
                             @NonNull final Callable<Throwable> onFailure) {
        // Petición:
        // POST
        // url:
        //     Constants.MIDDLEWARE_AUTH_URL
        // headers:
        //     Content-Type: multipart/form-data
        //     Accepts: application/json
        // body:
        //     Content-Disposition: form-data; name="anchorAudio"; filename="reg.wav"
        //                          Content-Type: audio/wav
        //     Content-Disposition: form-data; name="targetAudio"; filename="auth.wav"
        //                          Content-Type: audio/wav

        final ExecutorService executor = Executors.newSingleThreadExecutor();
        final Handler mainHandler = HandlerCompat.createAsync(Looper.getMainLooper());

        // Run work in a background thread
        executor.execute(() -> {
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(loggingInterceptor)
                    .build();

            final File anchorAudioFile = context.getFileStreamPath(anchorAudio);
            final File targetAudioFile = context.getFileStreamPath(targetAudio);

            try {

                final RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                        .addFormDataPart("anchorAudio", anchorAudio, RequestBody.create(MediaType.parse(APPLICATION_OCTET_STREAM), anchorAudioFile))
                        .addFormDataPart("targetAudio", targetAudio, RequestBody.create(MediaType.parse(APPLICATION_OCTET_STREAM), targetAudioFile))
                        .build();
                final Request request = new Request.Builder()
                        .url(MIDDLEWARE_AUTH_URL)
                        .addHeader(CONTENT_TYPE, MULTIPART_FORM)
                        .addHeader(ACCEPTS, APPLICATION_JSON)
                        .method("POST", body)
                        .build();

                final Response response = client.newCall(request).execute();
                final VoiceAuthentication authentication = new Gson().fromJson(response.body().string(), VoiceAuthentication.class);

                // Call back using the main UI thread
                mainHandler.post(() -> onSuccess.call(authentication));

            } catch (Throwable e) {
                // Call back using the main UI thread
                mainHandler.post(() -> onFailure.call(e));
            }
        });
    }

    public void unregisterEmail(@NonNull final String email,
                                @NonNull final Callable<Void> onSuccess,
                                @NonNull final Callable<Throwable> onFailure) {
        // Petición:
        // DELETE
        // url:
        //     Constants.MIDDLEWARE_URL/<email>
        // headers:
        //     Accept: application/json
        //     Content-Type: application/json
        final ExecutorService executor = Executors.newSingleThreadExecutor();
        final Handler mainHandler = HandlerCompat.createAsync(Looper.getMainLooper());

        // Run work in a background thread
        executor.execute(() -> {
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(loggingInterceptor)
                    .build();

            try {
                final Request request = new Request.Builder()
                        .url(MIDDLEWARE_URL + email)
                        .addHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .addHeader(ACCEPTS, APPLICATION_JSON)
                        .delete()
                        .build();

                final Response response = client.newCall(request).execute();

                // Call back using the main UI thread
                mainHandler.post(() -> {

                    if (response.code() < 400) {
                        onSuccess.call(null);
                    } else {
                        onFailure.call(new Exception("Error: returned status code is " + response.code()));
                    }
                });

            } catch (Throwable e) {
                // Call back using the main UI thread
                mainHandler.post(() -> onFailure.call(e));
            }
        });
    }
}
