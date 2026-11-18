package com.chepelayo.thelastone.utils;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;
import com.chepelayo.thelastone.R;
import com.chepelayo.thelastone.model.Game;
import java.util.List;
import java.util.stream.Collectors;

/** Compartir un juego o la lista visible con cualquier app (WhatsApp, correo...). */
public final class ShareHelper {

    private ShareHelper() { }

    public static void shareGame(Context ctx, Game game) {
        send(ctx, line(ctx, game), ctx.getString(R.string.share_game_title));
    }

    public static void shareList(Context ctx, List<Game> games) {
        if (games.isEmpty()) {
            Toast.makeText(ctx, R.string.share_empty, Toast.LENGTH_SHORT).show();
            return;
        }
        // Lambda + Stream: una línea por juego
        String body = games.stream()
                .map(g -> "• " + line(ctx, g))
                .collect(Collectors.joining("\n"));
        String text = ctx.getString(R.string.share_list_header, games.size()) + "\n\n" + body;
        send(ctx, text, ctx.getString(R.string.share_list_title));
    }

    private static String line(Context ctx, Game g) {
        String text = ctx.getString(R.string.share_game_line,
                g.getTitle(), g.getPlatform(), ctx.getString(g.getStatusEnum().getLabelRes()));
        if (g.getScore() != null) {
            text += ctx.getString(R.string.share_score_suffix, g.getScoreText());
        }
        return text;
    }

    private static void send(Context ctx, String text, String chooserTitle) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, text);
        try {
            ctx.startActivity(Intent.createChooser(intent, chooserTitle));
        } catch (ActivityNotFoundException e) {
            Toast.makeText(ctx, R.string.error_no_share_app, Toast.LENGTH_SHORT).show();
        }
    }
}