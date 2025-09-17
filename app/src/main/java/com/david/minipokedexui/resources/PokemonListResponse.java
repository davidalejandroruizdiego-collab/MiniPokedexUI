package com.david.minipokedexui.resources;

// PokemonListResponse.java
import com.google.gson.annotations.SerializedName;
import java.util.List;

public class PokemonListResponse {
    public int count;
    public String next;
    public String previous;

    @SerializedName("results")
    public List<PokemonListItem> results;

    public static class PokemonListItem {
        public String name;
        public String url;
    }
}
