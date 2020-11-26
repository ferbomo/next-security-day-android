package com.bbva.next.securityday.workshop.controller;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public final class Step3 {

    public static final String GALA_URL = "https://assistant.google.com/services/invoke/uid/0000003cb25cd365?hl=es";

    public void openGala(final Context context) {

        final Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(GALA_URL));
        context.startActivity(intent);
    }
}
