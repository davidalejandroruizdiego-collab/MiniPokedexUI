package com.david.minipokedexui;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.*;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.bumptech.glide.Glide;
import com.david.minipokedexui.resources.ApiClient;
import com.david.minipokedexui.resources.PokeApi;
import com.david.minipokedexui.resources.PokemonListResponse;
import com.david.minipokedexui.resources.PokemonResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private EditText etQuery;
    private Button btnSearch, btnClear;
    private ImageButton btnRandom;
    private CheckBox cbShiny;
    private ToggleButton tgSpriteArtwork;
    private RadioGroup rgSide;
    private RadioButton rbFront, rbBack;
    private Switch swDetails;
    private ImageView ivPokemon;
    private TextView tvBasic, tvDetails;

    private PokeApi pokeApi;

    private List<PokemonListResponse.PokemonListItem> pokemonList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        etQuery = findViewById(R.id.etQuery);
        btnSearch = findViewById(R.id.btnSearch);
        btnClear = findViewById(R.id.btnClear);
        btnRandom = findViewById(R.id.btnRandom);
        cbShiny = findViewById(R.id.cbShiny);
        tgSpriteArtwork = findViewById(R.id.tgSpriteArtwork);
        rgSide = findViewById(R.id.rgSide);
        rbFront = findViewById(R.id.rbFront);
        rbBack = findViewById(R.id.rbBack);
        swDetails = findViewById(R.id.swDetails);
        ivPokemon = findViewById(R.id.ivPokemon);
        tvBasic = findViewById(R.id.tvBasic);
        tvDetails = findViewById(R.id.tvDetails);

        pokeApi = ApiClient.getClient().create(PokeApi.class);

        loadPokemonList(50);

        btnSearch.setOnClickListener(v -> {
            String query = etQuery.getText().toString().trim().toLowerCase();
            if (!TextUtils.isEmpty(query)) {
                fetchPokemon(query);
            } else {
                Toast.makeText(this, "Introduce un nombre o ID válido", Toast.LENGTH_SHORT).show();
            }
        });

        btnRandom.setOnClickListener(v -> {
            if (pokemonList.isEmpty()) {
                Toast.makeText(this, "Lista de Pokémon no cargada aún", Toast.LENGTH_SHORT).show();
                return;
            }
            int randomIndex = new Random().nextInt(pokemonList.size());
            String randomName = pokemonList.get(randomIndex).name;
            fetchPokemon(randomName);
        });

        btnClear.setOnClickListener(v -> clearUI());

        swDetails.setOnCheckedChangeListener((buttonView, isChecked) -> {
            tvDetails.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });
    }

    private void loadPokemonList(int limit) {
        Call<PokemonListResponse> call = pokeApi.getPokemonList(limit);
        call.enqueue(new Callback<PokemonListResponse>() {
            @Override
            public void onResponse(Call<PokemonListResponse> call, Response<PokemonListResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    pokemonList = response.body().results;
                } else {
                    Toast.makeText(MainActivity.this, "Error al cargar lista de Pokémon", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<PokemonListResponse> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Fallo en conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchPokemon(String nameOrId) {
        tvBasic.setText("Cargando...");
        tvDetails.setText("");
        ivPokemon.setImageDrawable(null);

        Call<PokemonResponse> call = pokeApi.getPokemon(nameOrId);
        call.enqueue(new Callback<PokemonResponse>() {
            @Override
            public void onResponse(Call<PokemonResponse> call, Response<PokemonResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    PokemonResponse p = response.body();
                    updateUI(p);
                } else {
                    Toast.makeText(MainActivity.this, "Pokémon no encontrado", Toast.LENGTH_SHORT).show();
                    clearUI();
                }
            }

            @Override
            public void onFailure(Call<PokemonResponse> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Error de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                clearUI();
            }
        });
    }

    private void updateUI(PokemonResponse p) {
        tvBasic.setText(String.format("%s / %d", capitalize(p.name), p.id));

        if (swDetails.isChecked()) {
            StringBuilder details = new StringBuilder();
            details.append("Peso: ").append(p.weight).append("\n");

            details.append("Tipos: ");
            for (PokemonResponse.TypeSlot typeSlot : p.types) {
                details.append(capitalize(typeSlot.type.name)).append(" ");
            }
            details.append("\n");

            details.append("Habilidades: ");
            for (PokemonResponse.AbilitySlot abilitySlot : p.abilities) {
                details.append(capitalize(abilitySlot.ability.name)).append(" ");
            }
            tvDetails.setText(details.toString());
        } else {
            tvDetails.setText("");
        }

        String imageUrl = selectImageUrl(p);
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this)
                    .load(imageUrl)
                    .into(ivPokemon);
        } else {
            ivPokemon.setImageDrawable(null);
        }
    }

    private String selectImageUrl(PokemonResponse p) {
        boolean shiny = cbShiny.isChecked();
        boolean artwork = tgSpriteArtwork.isChecked();
        boolean front = rbFront.isChecked();

        if (artwork) {
            if (p.sprites.other != null && p.sprites.other.officialArtwork != null) {
                return p.sprites.other.officialArtwork.frontDefault;
            }
        } else {
            if (shiny) {
                return front ? p.sprites.frontShiny : p.sprites.backShiny;
            } else {
                return front ? p.sprites.frontDefault : p.sprites.backDefault;
            }
        }
        return null;
    }

    private void clearUI() {
        etQuery.setText("");
        tvBasic.setText("Nombre / ID");
        tvDetails.setText("");
        ivPokemon.setImageDrawable(null);
        cbShiny.setChecked(false);
        tgSpriteArtwork.setChecked(false);
        rbFront.setChecked(true);
        swDetails.setChecked(false);
    }

    private String capitalize(String s) {
        if (s == null || s.length() == 0) return s;
        return s.substring(0,1).toUpperCase() + s.substring(1);
    }
}