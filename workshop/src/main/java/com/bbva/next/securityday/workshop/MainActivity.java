package com.bbva.next.securityday.workshop;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bbva.next.securityday.workshop.controller.Step1;

public class MainActivity extends AppCompatActivity {

    private static final String[] REQUIRED_PERMISSIONS = {Manifest.permission.RECORD_AUDIO};
    private static final int REQUEST_PERMISSIONS_CODE = 1000;

    private static final String REGISTER_FILENAME = "reg.wav";
    private static final String AUTHENTICATE_FILENAME = "auth.wav";

    private static final double MIN_CONFIDENCE_NUMBER = 0.75;

    final Step1 step1 = new Step1();

    Toast toast;
    Button recordAudioButton;
    Button saveAudioButton;
    Button validateAudioButton;
    EditText emailEditText;
    Button checkRegistrationButton;
    Button registerButton;
    Button authenticateButton;
    Button unregisterButton;
    Button launchGalaButton;
    EditText loginEditText;
    Button loginButton;
    Button logoutButton;

    // TODO: refactor this
    byte[] bytesRecorded = null;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bindViews();
        prepareStep1();
    }

    private void bindViews() {
        toast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
        recordAudioButton = findViewById(R.id.bt_record_audio);
        saveAudioButton = findViewById(R.id.bt_save_audio);
        validateAudioButton = findViewById(R.id.bt_validate_audio);
        emailEditText = findViewById(R.id.et_email);
        checkRegistrationButton = findViewById(R.id.bt_check_registration);
        registerButton = findViewById(R.id.bt_register);
        authenticateButton = findViewById(R.id.bt_authenticate);
        unregisterButton = findViewById(R.id.bt_unregister);
        launchGalaButton = findViewById(R.id.bt_launch_gala);
        loginEditText = findViewById(R.id.et_login);
        loginButton = findViewById(R.id.bt_login);
        logoutButton = findViewById(R.id.bt_logout);
    }

    private void prepareStep1() {

        final String filename = REGISTER_FILENAME;

        saveAudioButton.setEnabled(false);
        validateAudioButton.setEnabled(false);

        recordAudioButton.setOnClickListener(v -> {

            saveAudioButton.setEnabled(false);
            validateAudioButton.setEnabled(false);
            // Exit early when we click the button if we do not have permission to record audio.
            // Note that we are checking the permissions every time we click the record button,
            // since permission grants may change over time.
            if (missingRequiredPermissions()) {
                return;
            }

            showToast("⏺ Grabando!");

            step1.recordAudio(this,
                    secondsLeft -> showToast("⏳ " + secondsLeft + " segundos..."),
                    bytesRecorded -> {
                        showToast("✅ Audio grabado");
                        this.bytesRecorded = bytesRecorded;
                        saveAudioButton.setEnabled(true);

                    }, recordError -> showAlert("❌ Error", recordError.getMessage()));
        });

        saveAudioButton.setOnClickListener(v -> {

            final byte[] bytesRecorded = this.bytesRecorded;

            step1.saveAudio(this, bytesRecorded, filename, success -> {

                showToast("✅ Archivo " + filename + " guardado");
                validateAudioButton.setEnabled(true);

            }, error -> showAlert("❌ Error", error.getMessage()));
        });

        validateAudioButton.setOnClickListener(v ->
                step1.validateAudio(
                        this,
                        filename,
                        ok -> showToast("✅ Archivo " + filename + " validado"),
                        error -> showAlert("❌ Error", error.getMessage())));
    }

    private boolean missingRequiredPermissions() {

        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_PERMISSIONS_CODE);
                return true;
            }
        }
        return false;
    }

    private void showToast(final String text) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            toast.cancel();
            toast.setText(text);
            toast.show();
        }
        else {
            Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
        }
    }

    private void showAlert(final String title, final String message) {
        new AlertDialog.Builder(this).setTitle(title).setMessage(message).setCancelable(true).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        for (int result : grantResults) {
            // If we do not have all the permissions we need, there is nothing we can do about it.
            if (result != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }
    }
}
