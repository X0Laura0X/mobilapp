package com.kotprog.notebook;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.ViewHolder> implements Filterable {
    private static final String LOG_TAG = NoteActivity.class.getName();

    private ArrayList<Note> notesData;
    private final ArrayList<Note> notesDataAll;
    private final Context noteContext;

    private int lastPosition = -1;

    private final Filter noteFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            ArrayList<Note> notesFilteredData = new ArrayList<>();
            FilterResults filterResults = new FilterResults();

            if (charSequence == null || charSequence.length() == 0) {
                filterResults.count = notesDataAll.size();
                filterResults.values = notesDataAll;
            } else {
                String filterPattern = charSequence.toString().toLowerCase().trim();

                for (Note note : notesDataAll) {
                    if (note.getTitle().toLowerCase().contains(filterPattern)) {
                        notesFilteredData.add(note);
                    }
                }
                filterResults.count = notesFilteredData.size();
                filterResults.values = notesFilteredData;
            }
            return filterResults;
        }

        @SuppressLint("NotifyDataSetChanged")
        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            notesData = (ArrayList<Note>) filterResults.values;
            notifyDataSetChanged();
        }
    };

    NoteAdapter(Context context, ArrayList<Note> notes) {
        this.notesData = notes;
        this.notesDataAll = notes;
        this.noteContext = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(noteContext).inflate(R.layout.note, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull NoteAdapter.ViewHolder holder, int position) {
        try {
            Note currentNote = notesData.get(position);
            holder.bindTo(currentNote);

            if (holder.getAdapterPosition() > lastPosition) {
                Animation fadeInAnimation = AnimationUtils.loadAnimation(noteContext, R.anim.fade_in);
                fadeInAnimation.setStartOffset(holder.getAdapterPosition() * 500L);
                holder.itemView.startAnimation(fadeInAnimation);
                lastPosition = holder.getAdapterPosition();
            }
        } catch (IndexOutOfBoundsException indexOutOfBoundsException) {
            Log.d(LOG_TAG, indexOutOfBoundsException.getMessage());
        }
    }

    @Override
    public int getItemCount() {
        return notesData.size();
    }

    @Override
    public Filter getFilter() {
        return noteFilter;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textViewNoteId;
        private final TextView textViewNoteTitle;
        private final TextView textViewNoteText;
        private final TextView textViewNoteModifiedAt;

        public ViewHolder(View item) {
            super(item);

            this.textViewNoteId = item.findViewById(R.id.textViewNoteId);
            this.textViewNoteTitle = item.findViewById(R.id.textViewNoteTitle);
            this.textViewNoteText = item.findViewById(R.id.textViewNoteText);
            this.textViewNoteModifiedAt = item.findViewById(R.id.textViewNoteModifiedAt);

            item.findViewById(R.id.layoutNote).setOnClickListener(view -> {
                Intent intent = new Intent(item.getContext(), NewNoteActivity.class);
                intent.putExtra("id", textViewNoteId.getText().toString());
                intent.putExtra("title", textViewNoteTitle.getText().toString());
                intent.putExtra("text", textViewNoteText.getText().toString());
                item.getContext().startActivity(intent);
            });
        }

        public void bindTo(Note currentNote) {
            textViewNoteId.setText(currentNote.getId());
            textViewNoteTitle.setText(currentNote.getTitle());
            textViewNoteText.setText(currentNote.getText());
            textViewNoteModifiedAt.setText(currentNote.getDate());
        }
    }
}
