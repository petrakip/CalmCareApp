package com.example.diamonds;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;

import java.util.List;

public class PhoneListAdapter extends RecyclerView.Adapter<PhoneListAdapter.ContactViewHolder> {

    public interface OnContactClickListener {
        void onContactClick(Contact contact);
    }

    public interface OnListChangedListener {
        void onListChanged(boolean isEmpty);
    }

    public interface OnImageClickListener {
        void onImageClick(int position);
    }

    private List<Contact> contactList;
    private final OnContactClickListener listener;
    private final Context context;
    private OnListChangedListener listChangedListener;
    private final OnImageClickListener imageClickListener;

    public PhoneListAdapter(Context context, List<Contact> contactList,
                            OnContactClickListener contactClickListener,
                            OnImageClickListener imageClickListener) {
        this.context = context;
        this.contactList = contactList;
        this.listener = contactClickListener;
        this.imageClickListener = imageClickListener;
    }

    public void setOnListChangedListener(OnListChangedListener listener) {
        this.listChangedListener = listener;
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_contact, parent, false);
        return new ContactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        Contact contact = contactList.get(position);
        holder.nameTextView.setText(contact.getName());
        holder.phoneTextView.setText(contact.getPhone());

        // Show image from URI or drawable with Glide
        String imageUri = contact.getImageUri();

        if (imageUri != null) {
            Uri uri = Uri.parse(imageUri);
            if (imageUri.startsWith("content://") || imageUri.startsWith("file://")) {
                Glide.with(context)
                        .load(uri)
                        .placeholder(R.drawable.contact_person_icon)
                        .error(R.drawable.contact_person_icon)
                        .into(holder.imageView);
            } else {
                int resId = context.getResources().getIdentifier(
                        imageUri.replace(".png", "").replace(".jpg", "").trim(),
                        "drawable",
                        context.getPackageName()
                );
                holder.imageView.setImageResource(resId != 0 ? resId : R.drawable.contact_person_icon);
            }
        } else {
            holder.imageView.setImageResource(R.drawable.contact_person_icon);
        }

        // Remove contact
        holder.removeBtn.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Διαγραφή Επαφής")
                    .setMessage("Είστε σίγουρος ότι θέλετε να διαγράψετε την επαφή;")
                    .setPositiveButton("Ναι", (dialog, which) -> {
                        int pos = holder.getAdapterPosition();
                        if (pos != RecyclerView.NO_POSITION) {
                            contactList.remove(pos);
                            notifyItemRemoved(pos);

                            SharedPreferences prefs = context.getSharedPreferences("contacts", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = prefs.edit();
                            String category = ((PhoneList) context).getIntent().getStringExtra("category");
                            editor.putString(category, new Gson().toJson(contactList));
                            editor.apply();

                            if (listChangedListener != null) {
                                listChangedListener.onListChanged(contactList.isEmpty());
                            }
                        }
                    })
                    .setNegativeButton("Άκυρο", null)
                    .show();
        });

        // Click for call
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onContactClick(contact);
            }
        });

        // Click on image to change it
        holder.imageView.setOnClickListener(v -> {
            if (imageClickListener != null) {
                imageClickListener.onImageClick(holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return contactList.size();
    }

    static class ContactViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView, phoneTextView;
        ImageView imageView;
        Button removeBtn;

        public ContactViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.contactName);
            phoneTextView = itemView.findViewById(R.id.contactPhone);
            imageView = itemView.findViewById(R.id.imageContactView);
            removeBtn = itemView.findViewById(R.id.removeContactBtn);
        }
    }
}
