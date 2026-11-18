package com.chepelayo.thelastone.data;

import android.util.Log;
import com.chepelayo.thelastone.model.Game;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Único punto de acceso a Firestore.
 * Estructura: users/{uid}/games/{gameId}
 */
public class GameRepository {

    private static final String TAG = "GameRepository";
    private static final String USERS = "users";
    private static final String GAMES = "games";

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private CollectionReference gamesOf(String uid) {
        return db.collection(USERS).document(uid).collection(GAMES);
    }

    /** Escucha en tiempo real todo el catálogo del usuario. Devuelve el registro para poder cancelarlo. */
    public ListenerRegistration listenGames(String uid, Consumer<List<Game>> onData, Consumer<Exception> onError) {
        return gamesOf(uid).addSnapshotListener((snapshot, error) -> {
            if (error != null) { onError.accept(error); return; }
            if (snapshot == null) return;
            List<Game> list = new ArrayList<>();
            for (DocumentSnapshot doc : snapshot.getDocuments()) {
                try {
                    Game game = doc.toObject(Game.class);
                    if (game != null) {
                        game.setId(doc.getId());
                        list.add(game);
                    }
                } catch (RuntimeException e) {
                    // Un documento con datos mal formados no debe tumbar toda la lista
                    Log.w(TAG, "Documento inválido: " + doc.getId(), e);
                }
            }
            onData.accept(list);
        });
    }

    /** Escucha un único juego. Si se borra, onData recibe null. */
    public ListenerRegistration listenGame(String uid, String gameId, Consumer<Game> onData, Consumer<Exception> onError) {
        return gamesOf(uid).document(gameId).addSnapshotListener((doc, error) -> {
            if (error != null) { onError.accept(error); return; }
            if (doc == null || !doc.exists()) { onData.accept(null); return; }
            try {
                Game game = doc.toObject(Game.class);
                if (game != null) game.setId(doc.getId());
                onData.accept(game);
            } catch (RuntimeException e) {
                onError.accept(e);
            }
        });
    }

    /** Crea (id == null) o actualiza un juego. */
    public Task<Void> saveGame(String uid, Game game) {
        DocumentReference ref = (game.getId() == null)
                ? gamesOf(uid).document()
                : gamesOf(uid).document(game.getId());
        return ref.set(game);
    }

    public Task<Void> deleteGame(String uid, String gameId) {
        return gamesOf(uid).document(gameId).delete();
    }

    public Task<Void> setFavorite(String uid, String gameId, boolean favorite) {
        return gamesOf(uid).document(gameId).update("favorite", favorite);
    }
}