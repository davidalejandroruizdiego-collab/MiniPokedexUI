package com.david.minipokedexui.resources;

// PokeApi.java
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface PokeApi {

    @GET("pokemon")
    Call<PokemonListResponse> getPokemonList(@Query("limit") int limit);

    @GET("pokemon/{nameOrId}")
    Call<PokemonResponse> getPokemon(@Path("nameOrId") String nameOrId);
}
