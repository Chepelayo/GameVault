package com.chepelayo.thelastone.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.chepelayo.thelastone.data.GameRepository;
import com.chepelayo.thelastone.model.Game;
import com.google.firebase.firestore.ListenerRegistration;
import java.util.ArrayList;
import java.util.List;

/**
 * Compartido por todas las pantallas de MainActivity.
 * Mantiene el catálogo actualizado en tiempo real y sobrevive a los giros de pantalla.
 */
public class GamesViewModel extends ViewModel {

    private final GameRepository repository = new GameRepository();
    private final MutableLiveData<List<Game>> games = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Exception> error = new MutableLiveData<>();
    private ListenerRegistration registration;
    private String uid;

    public LiveData<List<Game>> getGames() { return games; }
    public LiveData<Exception> getError() { return error; }
    public void clearError() { error.setValue(null); }

    /** Empieza a escuchar el catálogo (solo la primera vez). */
    public void start(String uid) {
        if (registration != null) return;
        this.uid = uid;
        registration = repository.listenGames(uid, games::setValue, error::setValue);
    }

    /** Alterna el favorito de un juego. */
    public void toggleFavorite(Game game) {
        if (uid == null || game.getId() == null) return;
        repository.setFavorite(uid, game.getId(), !game.isFavorite())
                .addOnFailureListener(error::setValue);
    }

    @Override
    protected void onCleared() {
        if (registration != null) registration.remove();
    }
}