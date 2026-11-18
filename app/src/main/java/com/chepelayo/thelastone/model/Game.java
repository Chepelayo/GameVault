package com.chepelayo.thelastone.model;

import androidx.annotation.ColorRes;
import com.chepelayo.thelastone.R;
import com.google.firebase.firestore.Exclude;
import java.io.Serializable;
import java.util.Locale;
import java.util.Objects;

/**
 * Videojuego del catálogo. POJO compatible con Firestore:
 * constructor vacío + getters/setters. El id del documento NO se guarda como campo (@Exclude).
 * Es Serializable para poder pasarlo entre Activities.
 */
public class Game implements Serializable {

    private String id;
    private String title;
    private String genre;
    private String developer;
    private String platform;
    private int releaseYear;
    private String status = GameStatus.PENDING.name();
    private Integer playedYear;   // null si está pendiente
    private Double score;         // null si está pendiente
    private String notes;
    private boolean favorite;
    private String coverUrl;
    private long createdAt;

    /** Constructor vacío obligatorio para Firestore. */
    public Game() { }

    @Exclude public String getId() { return id; }
    @Exclude public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }
    public String getDeveloper() { return developer; }
    public void setDeveloper(String developer) { this.developer = developer; }
    public String getPlatform() { return platform; }
    public void setPlatform(String platform) { this.platform = platform; }
    public int getReleaseYear() { return releaseYear; }
    public void setReleaseYear(int releaseYear) { this.releaseYear = releaseYear; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getPlayedYear() { return playedYear; }
    public void setPlayedYear(Integer playedYear) { this.playedYear = playedYear; }
    public Double getScore() { return score; }
    public void setScore(Double score) { this.score = score; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public boolean isFavorite() { return favorite; }
    public void setFavorite(boolean favorite) { this.favorite = favorite; }
    public String getCoverUrl() { return coverUrl; }
    public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    // ---- Ayudas de presentación (@Exclude: no se guardan en Firestore) ----

    @Exclude
    public GameStatus getStatusEnum() { return GameStatus.fromKey(status); }

    /** Puntuación con un decimal ("9.5"), o cadena vacía si no hay. */
    @Exclude
    public String getScoreText() {
        return score == null ? "" : String.format(Locale.getDefault(), "%.1f", score);
    }

    /** Color de la insignia: verde (>=8), ámbar (>=5), rojo (<5). */
    @Exclude @ColorRes
    public int getScoreColorRes() {
        double s = score == null ? 0 : score;
        if (s >= 8) return R.color.score_high;
        if (s >= 5) return R.color.score_mid;
        return R.color.score_low;
    }

    /** Usado por DiffUtil para saber si hay que repintar el elemento. */
    @Exclude
    public boolean sameContentAs(Game o) {
        return releaseYear == o.releaseYear && favorite == o.favorite
                && Objects.equals(id, o.id) && Objects.equals(title, o.title)
                && Objects.equals(genre, o.genre) && Objects.equals(developer, o.developer)
                && Objects.equals(platform, o.platform) && Objects.equals(status, o.status)
                && Objects.equals(playedYear, o.playedYear) && Objects.equals(score, o.score)
                && Objects.equals(notes, o.notes) && Objects.equals(coverUrl, o.coverUrl);
    }
}