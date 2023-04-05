package com.kotprog.notebook;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class SignUpActivity extends AppCompatActivity {
    private static final String LOG_TAG = SignUpActivity.class.getName();

    private FirebaseAuth firebaseAuth;

    EditText editTextEmail;
    EditText editTextPassword;
    EditText editTextPasswordAgain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextPasswordAgain = findViewById(R.id.editTextPasswordAgain);

        firebaseAuth = FirebaseAuth.getInstance();
    }

    public void cancel(View view) {
        finish();
    }

    public void signUp(View view) {
        Intent intent = new Intent(this, NoteActivity.class);

        String email = editTextEmail.getText().toString();
        String password = editTextPassword.getText().toString();

        if (validateFields()) {
            firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    startActivity(intent);
                } else {
                    Log.d(LOG_TAG, "Unsuccessful sign up: " + Objects.requireNonNull(task.getException()).getMessage());
                    Toast.makeText(SignUpActivity.this, "This email is taken", Toast.LENGTH_LONG).show();
                }
            });
        } else {
            Toast.makeText(this, "Fill all the fields correctly", Toast.LENGTH_LONG).show();
        }
    }

    private boolean validateFields() {
        boolean isValid = true;

        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String passwordAgain = editTextPasswordAgain.getText().toString().trim();

        String emailPattern = "[a-zA-Z\\d._-]+@[a-z]+\\.+[a-z]+";

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

        if (password.length() < 6) {
            editTextPasswordAgain.setError("Password must be at least 6 character");
            editTextPasswordAgain.requestFocus();
            isValid = false;
        }

        if (!password.equals(passwordAgain)) {
            editTextPasswordAgain.setError("Passwords must match");
            editTextPasswordAgain.requestFocus();
            isValid = false;
        }

        if (!email.matches(emailPattern)) {
            editTextEmail.setError("The email is badly formatted");
            editTextEmail.requestFocus();
            isValid = false;
        }

        return isValid;
    }
}
