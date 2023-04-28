package com.kotprog.notebook;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Objects;

public class NoteActivity extends AppCompatActivity {
    private static final String LOG_TAG = NoteActivity.class.getName();

    private CollectionReference collectionReference;

    private RecyclerView noteRecyclerView;
    private ArrayList<Note> notes;
    private NoteAdapter noteAdapter;

    private int gridNumber = 1;
    private boolean firstLoad = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            Log.i(LOG_TAG, "Authenticated user");
        } else {
            finish();
        }

        noteRecyclerView = findViewById(R.id.noteRecyclerView);
        noteRecyclerView.setLayoutManager(new GridLayoutManager(this, gridNumber));
        notes = new ArrayList<>();

        noteAdapter = new NoteAdapter(this, notes);

        noteRecyclerView.setAdapter(noteAdapter);

        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        collectionReference = firebaseFirestore.collection("Notes");

        initializeNotes();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void initializeNotes() {
        notes.clear();

        collectionReference.orderBy("date", Query.Direction.DESCENDING).limit(100).get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                Note note = doc.toObject(Note.class);
                notes.add(note);
            }

            noteAdapter.notifyDataSetChanged();
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu, menu);
        MenuItem menuItem = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(menuItem);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                noteAdapter.getFilter().filter(s);
                return false;
            }
        });

        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.newNote:
                Intent intent = new Intent(this, NewNoteActivity.class);
                startActivity(intent);
                return true;
            case R.id.view:
                if (gridNumber == 1) {
                    changeSpanCount(item, R.drawable.ic_view_comfy, 2);
                } else {
                    changeSpanCount(item, R.drawable.ic_view_stream, 1);
                }
                return true;
            case R.id.logout:
                FirebaseAuth.getInstance().signOut();
                GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_SIGN_IN);
                googleSignInClient.signOut().addOnCompleteListener(this, task ->
                        Log.i(LOG_TAG, "Google logout")
                );
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void changeSpanCount(MenuItem item, int ic_view, int i) {
        gridNumber = i;
        item.setIcon(ic_view);
        GridLayoutManager layoutManager = (GridLayoutManager) noteRecyclerView.getLayoutManager();
        Objects.requireNonNull(layoutManager).setSpanCount(i);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (firstLoad) {
            firstLoad = false;
        } else {
            initializeNotes();
        }
    }
}
