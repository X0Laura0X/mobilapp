package com.kotprog.notebook;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

public class NewNoteActivity extends AppCompatActivity {
    private FirebaseFirestore firebaseFirestore;
    private CollectionReference collectionReference;

    private NoteNotificationHandler noteNotificationHandler;

    TextView textViewNoteId;
    EditText editTextNoteTitle;
    EditText editTextNoteText;
    View deleteIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_note);

        textViewNoteId = findViewById(R.id.textViewNoteId);
        editTextNoteTitle = findViewById(R.id.editTextNoteTitle);
        editTextNoteText = findViewById(R.id.editTextNoteText);
        deleteIcon = findViewById(R.id.deleteIcon);

        firebaseFirestore = FirebaseFirestore.getInstance();
        collectionReference = firebaseFirestore.collection("Notes");

        String id = getIntent().getStringExtra("id");
        String title = getIntent().getStringExtra("title");
        String text = getIntent().getStringExtra("text");

        textViewNoteId.setText(id);
        editTextNoteTitle.setText(title);
        editTextNoteText.setText(text);

        noteNotificationHandler = new NoteNotificationHandler(this);
    }

    public void cancel(View view) {
        finish();
    }

    public void addNote(View view) {
        if (validateFields()) {
            Date currentDate = new Date();
            @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
            String formattedDate = dateFormat.format(currentDate);
            String noteId = textViewNoteId.getText().toString();
            String noteTitle = editTextNoteTitle.getText().toString();
            String noteText = editTextNoteText.getText().toString();

            collectionReference.whereEqualTo("id", noteId).limit(1)
                    .get().addOnSuccessListener(querySnapshot -> {
                        if (querySnapshot.size() > 0) {
                            Note existingNote = querySnapshot.getDocuments().get(0).toObject(Note.class);
                            assert existingNote != null;
                            existingNote.setTitle(noteTitle);
                            existingNote.setText(noteText);
                            existingNote.setDate(formattedDate);
                            collectionReference.document(querySnapshot.getDocuments().get(0).getId()).set(existingNote);
                            Toast.makeText(NewNoteActivity.this, "Note updated", Toast.LENGTH_LONG).show();
                            noteNotificationHandler.send(
                                    Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getEmail() + " updated a note"
                            );
                        } else {
                            String id = UUID.randomUUID().toString();
                            Note newNote = new Note(noteTitle, noteText, formattedDate);
                            newNote.setId(id);
                            collectionReference.document(id).set(newNote);
                            Toast.makeText(NewNoteActivity.this, "Note created", Toast.LENGTH_LONG).show();
                            noteNotificationHandler.send(
                                    Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getEmail() + " added a new note"
                            );
                        }
                        finish();
                    }).addOnFailureListener(e ->
                            Toast.makeText(NewNoteActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show()
                    );
        }
    }

    private boolean validateFields() {
        boolean isValid = true;

        String title = editTextNoteTitle.getText().toString().trim();
        String text = editTextNoteText.getText().toString().trim();

        if (title.isEmpty()) {
            editTextNoteTitle.setError("Title is required");
            editTextNoteTitle.requestFocus();
            isValid = false;
        }

        if (text.isEmpty()) {
            editTextNoteText.setError("Text is required");
            editTextNoteText.requestFocus();
            isValid = false;
        }

        return isValid;
    }

    public void deleteNote(View view) {
        Animation scaleAnimation = AnimationUtils.loadAnimation(this, R.anim.scale);
        deleteIcon.startAnimation(scaleAnimation);

        String id = textViewNoteId.getText().toString();

        if (!id.isEmpty()) {
            Query query = collectionReference.whereEqualTo("id", id).limit(1);
            query.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String documentId = document.getId();
                        firebaseFirestore.collection("Notes").document(documentId).delete().addOnSuccessListener(unused -> {
                            Toast.makeText(NewNoteActivity.this, "Note deleted", Toast.LENGTH_LONG).show();
                            noteNotificationHandler.send(
                                    Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getEmail() + " deleted a note"
                            );
                            finish();
                        }).addOnFailureListener(e ->
                            Toast.makeText(NewNoteActivity.this, "Error deleting note", Toast.LENGTH_LONG).show()
                        );
                    }
                } else {
                    Toast.makeText(this, "Error deleting note", Toast.LENGTH_LONG).show();
                }
            });
        }
    }
}
