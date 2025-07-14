package com.example.pmiexa2p;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ViewHolder> {

    private List<Contactos> contactosList;

    public ContactAdapter(List<Contactos> contactosList) {
        this.contactosList = contactosList;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtName, txtPhone, txtLat, txtLon;
        ImageView imgContact;
        Button btnDelete, btnUpdate;

        public ViewHolder(View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txtName);
            txtPhone = itemView.findViewById(R.id.txtPhone);
            txtLat = itemView.findViewById(R.id.txtLatitude);
            txtLon = itemView.findViewById(R.id.txtLongitude);
            imgContact = itemView.findViewById(R.id.imgContact);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnUpdate = itemView.findViewById(R.id.btnUpdate);
        }
    }

    @NonNull
    @Override
    public ContactAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.contactos, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactAdapter.ViewHolder holder, int position) {
        Contactos contactos = contactosList.get(position);
        holder.txtName.setText(contactos.getName());
        holder.txtPhone.setText(contactos.getPhone());
        holder.txtLat.setText("Lat: " + contactos.getLatitude());
        holder.txtLon.setText("Lon: " + contactos.getLongitude());

        // Convertir imagen Base64 a Bitmap
        try {
            String base64Image = contactos.getImage().replace("data:image/jpeg;base64,", "");
            byte[] decodedBytes = Base64.decode(base64Image, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
            holder.imgContact.setImageBitmap(bitmap);
        } catch (Exception e) {
            holder.imgContact.setImageResource(R.drawable.ic_launcher_background);
        }

        holder.btnDelete.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;

            ApiService apiService = ApiClient.getApiService();
            Call<Void> call = apiService.deleteContact(contactos.getId());

            call.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        contactosList.remove(pos);
                        notifyItemRemoved(pos);
                        Toast.makeText(v.getContext(), "Contacto eliminado", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(v.getContext(), "Error al eliminar", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Toast.makeText(v.getContext(), "Fallo: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
        holder.btnUpdate.setOnClickListener(v -> {
            View dialogView = LayoutInflater.from(v.getContext()).inflate(R.layout.actualizar_contactos, null);

            EditText editName = dialogView.findViewById(R.id.editActuaName);
            EditText editPhone = dialogView.findViewById(R.id.editActuaPhone);
            EditText editLat = dialogView.findViewById(R.id.editActuaLat);
            EditText editLon = dialogView.findViewById(R.id.editActuaLon);

            // Set current values
            editName.setText(contactos.getName());
            editPhone.setText(contactos.getPhone());
            editLat.setText(contactos.getLatitude());
            editLon.setText(contactos.getLongitude());

            new AlertDialog.Builder(v.getContext())
                    .setTitle("Actualizar contacto")
                    .setView(dialogView)
                    .setPositiveButton("Guardar", (dialog, which) -> {
                        String updatedName = editName.getText().toString().trim();
                        String updatedPhone = editPhone.getText().toString().trim();
                        String updatedLat = editLat.getText().toString().trim();
                        String updatedLon = editLon.getText().toString().trim();

                        if (updatedName.isEmpty() || updatedPhone.isEmpty() || updatedLat.isEmpty() || updatedLon.isEmpty()) {
                            Toast.makeText(v.getContext(), "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        Contactos updatedContactos = new Contactos(
                                contactos.getId(),
                                updatedName,
                                updatedPhone,
                                updatedLat,
                                updatedLon,
                                contactos.getImage() // Mantenemos la misma imagen
                        );

                        ApiService apiService = ApiClient.getApiService();
                        Call<Void> call = apiService.updateContact(contactos.getId(), updatedContactos);

                        call.enqueue(new Callback<Void>() {
                            @Override
                            public void onResponse(Call<Void> call, Response<Void> response) {
                                if (response.isSuccessful()) {
                                    // Actualiza la lista local
                                    contactosList.set(holder.getAdapterPosition(), updatedContactos);
                                    notifyItemChanged(holder.getAdapterPosition());
                                    Toast.makeText(v.getContext(), "Contacto actualizado", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(v.getContext(), "Error al actualizar", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onFailure(Call<Void> call, Throwable t) {
                                Toast.makeText(v.getContext(), "Fallo de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();

        });
        holder.itemView.setOnClickListener(v -> {
            String latitude = contactos.getLatitude();
            String longitude = contactos.getLongitude();

            String uri = "http://maps.google.com/maps?daddr=" + latitude + "," + longitude;
            Intent intent = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse(uri));
            intent.setPackage("com.google.android.apps.maps");
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return contactosList.size();
    }

    public void updateList(List<Contactos> newList) {
        contactosList = newList;
        notifyDataSetChanged();
    }
}