package com.example.pmiexa2p;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.GET;
import retrofit2.http.*;

import java.util.List;

public interface ApiService {
    @POST("/contacts")
    Call<Void> saveContact(@Body Contactos contactos);

    @GET("/contacts")
    Call<List<Contactos>> getAllContacts();

    @DELETE("/contacts/{id}")
    Call<Void> deleteContact(@retrofit2.http.Path("id") int id);

    @PUT("/contacts/{id}")
    Call<Void> updateContact(@retrofit2.http.Path("id") int id, @Body Contactos contactos);
}
