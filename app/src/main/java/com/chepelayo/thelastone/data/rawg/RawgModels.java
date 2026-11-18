package com.chepelayo.thelastone.data.rawg;

import com.chepelayo.thelastone.model.Game;
import com.google.gson.annotations.SerializedName;
import java.util.List;

/** Clases que Gson rellena con el JSON de RAWG (solo los campos que necesitamos). */
public final class RawgModels {

    private RawgModels() { }

    public static class SearchResponse {
        public List<RawgGame> results;
    }

    public static class RawgGame {
        public int id;
        public String name;
        public String released;                 // "2015-05-19"
        @SerializedName("background_image")
        public String backgroundImage;
        public List<Named> genres;
        public List<PlatformWrapper> platforms;
        public List<Named> developers;          // solo viene en el detalle (/games/{id})

        /** Convierte el resultado de RAWG en un Game para rellenar el formulario. */
        public Game toGame() {
            Game g = new Game();
            g.setTitle(name);
            g.setCoverUrl(backgroundImage);
            g.setGenre(firstName(genres));
            g.setDeveloper(firstName(developers));
            if (platforms != null && !platforms.isEmpty() && platforms.get(0).platform != null) {
                g.setPlatform(platforms.get(0).platform.name);
            }
            if (released != null && released.length() >= 4) {
                try {
                    g.setReleaseYear(Integer.parseInt(released.substring(0, 4)));
                } catch (NumberFormatException ignored) {
                    // fecha rara: dejamos el año a 0 y el usuario lo rellena
                }
            }
            return g;
        }

        private static String firstName(List<Named> list) {
            return (list == null || list.isEmpty() || list.get(0).name == null) ? "" : list.get(0).name;
        }
    }

    public static class Named {
        public String name;
    }

    public static class PlatformWrapper {
        public Named platform;
    }
}