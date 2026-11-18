package com.chepelayo.thelastone.data.rawg;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/** Singleton de Retrofit configurado para RAWG. */
public final class RawgClient {

    private static RawgApi api;

    private RawgClient() { }

    public static synchronized RawgApi getApi() {
        if (api == null) {
            api = new Retrofit.Builder()
                    .baseUrl("https://api.rawg.io/api/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(RawgApi.class);
        }
        return api;
    }
}