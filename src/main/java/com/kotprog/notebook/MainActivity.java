package com.kotprog.notebook;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private static final String LOG_TAG = MainActivity.class.getName();
    private final int REQUEST_CODE = 1;

    private FirebaseAuth firebaseAuth;
    private GoogleSignInClient googleClient;

    EditText editTextEmail;
    EditText editTextPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);

        firebaseAuth = FirebaseAuth.getInstance();

        GoogleSignInOptions googleOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleClient = GoogleSignIn.getClient(this, googleOptions);
    }

    public void signUpActivity(View ignoredView) {
        Intent intent = new Intent(this, SignUpActivity.class);
        startActivity(intent);
    }

    public void signIn(View view) {
        Intent intent = new Intent(this, NoteActivity.class);

        String email = editTextEmail.getText().toString();
        String password = editTextPassword.getText().toString();

        if (validateFields()) {
            firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    startActivity(intent);
                } else {
                    Log.d(LOG_TAG, "Unsuccessful login: " + Objects.requireNonNull(task.getException()).getMessage());
                    Toast.makeText(MainActivity.this, "User does not exist", Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    @Override
    public void onActivityResult(int request, int result, Intent data) {
        super.onActivityResult(request, result, data);

        if (request == 10) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseGoogleAuth(account.getIdToken());
            } catch (ApiException apiException) {
                Log.d(LOG_TAG, apiException.getMessage());
            }
        }
    }

    private void firebaseGoogleAuth(String idToken) {
        Intent intent = new Intent(this, NoteActivity.class);

        AuthCredential authCredential = GoogleAuthProvider.getCredential(idToken, null);
        firebaseAuth.signInWithCredential(authCredential).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                startActivity(intent);
            } else {
                Log.d(LOG_TAG, "Unsuccessful login: " + Objects.requireNonNull(task.getException()).getMessage());
            }
        });
    }

    public void signInWithGoogle(View view) {
        Intent intent = googleClient.getSignInIntent();
        startActivityForResult(intent, 10);
    }

    private boolean validateFields() {
        boolean isValid = true;

        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (email.isEmpty()) {
            editTextEmail.setError("Email is required");
            editTextEmail.requestFocus();
            isValid = false;
        }

        if (password.isEmpty()) {
            editTextPassword.setError("Password is required");
            editTextPassword.requestFocus();
            isValid = false;
        }

        return isValid;
    }
}
