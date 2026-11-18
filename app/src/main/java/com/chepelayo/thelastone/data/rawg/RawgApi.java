package com.chepelayo.thelastone.data.rawg;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/** Endpoints de RAWG que usa la app. */
public interface RawgApi {

    /** Busca juegos por nombre. */
    @GET("games")
    Call<RawgModels.SearchResponse> searchGames(@Query("key") String apiKey,
                                                @Query("search") String query,
                                                @Query("page_size") int pageSize);

    /** Detalle de un juego (incluye desarrolladoras). */
    @GET("games/{id}")
    Call<RawgModels.RawgGame> getGame(@Path("id") int id, @Query("key") String apiKey);
}