package com.example.pmiexa2p;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.text.TextWatcher;
import android.text.Editable;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ContactosGuardados extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ContactAdapter contactAdapter;
    private Button btnBack;
    private EditText Busqueda;
    private List<Contactos> ListaOriginal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contactos_guardados);

        Busqueda = findViewById(R.id.editTextBusqueda);

        recyclerView = findViewById(R.id.recyclerViewContacts);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        btnBack = findViewById(R.id.btnAtras);
        btnBack.setOnClickListener(v -> finish());

        loadContacts();
    }

    private void loadContacts() {
        ApiService apiService = ApiClient.getApiService();
        Call<List<Contactos>> call = apiService.getAllContacts();

        call.enqueue(new Callback<List<Contactos>>() {
            @Override
            public void onResponse(Call<List<Contactos>> call, Response<List<Contactos>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ListaOriginal = response.body();
                    contactAdapter = new ContactAdapter(ListaOriginal);
                    recyclerView.setAdapter(contactAdapter);

                    // Listener de búsqueda
                    Busqueda.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {
                            filterContacts(s.toString());
                        }

                        @Override
                        public void afterTextChanged(Editable s) {}
                    });
                } else {
                    Toast.makeText(ContactosGuardados.this, "Error al cargar los contactos", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Contactos>> call, Throwable t) {
                Toast.makeText(ContactosGuardados.this, "Fallo de red: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void filterContacts(String query) {
        if (ListaOriginal == null) return;
        List<Contactos> filteredList = new ArrayList<>();
        for (Contactos contactos : ListaOriginal) {
            if (contactos.getName().toLowerCase().contains(query.toLowerCase()) ||
                    contactos.getPhone().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(contactos);
            }
        }
        contactAdapter.updateList(filteredList);
    }
}