package com.david.minipokedexui.resources;

// PokemonResponse.java
import com.google.gson.annotations.SerializedName;
import java.util.List;

public class PokemonResponse {
    public int id;
    public String name;
    public int weight;

    public Sprites sprites;
    public List<TypeSlot> types;
    public List<AbilitySlot> abilities;

    public static class Sprites {
        @SerializedName("front_default")
        public String frontDefault;

        @SerializedName("back_default")
        public String backDefault;

        @SerializedName("front_shiny")
        public String frontShiny;

        @SerializedName("back_shiny")
        public String backShiny;

        @SerializedName("other")
        public OtherSprites other;

        public static class OtherSprites {
            @SerializedName("official-artwork")
            public OfficialArtwork officialArtwork;

            public static class OfficialArtwork {
                @SerializedName("front_default")
                public String frontDefault;
            }
        }
    }

    public static class TypeSlot {
        public Type type;

        public static class Type {
            public String name;
        }
    }

    public static class AbilitySlot {
        public Ability ability;

        public static class Ability {
            public String name;
        }
    }
}

