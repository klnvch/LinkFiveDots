package by.klnvch.link5dots;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import by.klnvch.link5dots.settings.SettingsUtils;

public class MainActivity extends AppCompatActivity {

    private GameView view;
    private AlertDialog alertDialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_board);

        //restore view
        view = (GameView) findViewById(R.id.game_view);

        view.setOnGameEventListener(new GameView.OnGameEventListener() {
            @Override
            public void onMoveDone(Dot currentDot, Dot previousDot) {
                if (previousDot == null || previousDot.getType() == Dot.OPPONENT) {
                    // set user dot
                    currentDot.setType(Dot.USER);
                    view.setDot(currentDot);
                    // set bot dot
                    Dot botDot = Bot.findAnswer(view.getCopyOfNet());
                    botDot.setType(Dot.OPPONENT);
                    view.setDot(botDot);
                }
            }

            @Override
            public void onGameEnd(HighScore highScore) {
                showAlertDialog(highScore);
            }
        });

        String username = SettingsUtils.getUserName(this, null);
        if (username != null) {
            TextView tvUsername = (TextView) findViewById(R.id.user_name);
            tvUsername.setText(username);
        }

        ((App) getApplication()).getTracker();
    }

    @Override
    protected void onStart() {
        super.onStart();
        GoogleAnalytics.getInstance(this).reportActivityStart(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        view.restore(getPreferences(MODE_PRIVATE));
        view.invalidate();
        view.isOver();
    }

    @Override
    protected void onPause() {
        super.onPause();
        view.save(getPreferences(MODE_PRIVATE));
    }

    @Override
    protected void onStop() {
        super.onStop();
        GoogleAnalytics.getInstance(this).reportActivityStop(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_undo:
                undoLastMove();
                return true;
            case R.id.menu_new_game:
                newGame();
                return true;
            case R.id.menu_search:
                searchLastMove();
                return true;
        }
        return false;
    }

    @Override
    public boolean onSearchRequested() {
        searchLastMove();
        return true;
    }

    private void showAlertDialog(final HighScore highScore) {
        if (alertDialog == null || !alertDialog.isShowing()) {
            //final long gameStatus = data.getLong(GAME_STATUS);
            //final long numberOfMoves = data.getLong(NUMBER_OF_MOVES);
            //final long timeElapsed = data.getLong(ELAPSED_TIME);

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            if (highScore.getStatus() == HighScore.WON) {
                builder.setTitle(R.string.end_win);
            } else {
                builder.setTitle(R.string.end_lose);
            }
            String str = getString(R.string.end_move, highScore.getScore(), highScore.getTime());
            builder.setMessage(str);
            builder.setPositiveButton(R.string.end_new_game, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    newGame();
                }
            });
            builder.setNeutralButton(R.string.end_publish, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    publishScore(highScore);
                }
            });
            builder.setNegativeButton(R.string.end_undo, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    undoLastMove();
                }
            });
            alertDialog = builder.create();
            alertDialog.show();
        }
    }

    private void undoLastMove() {
        view.undoLastMove(2);
        //
        Tracker tracker = ((App) getApplication()).getTracker();
        tracker.send(new HitBuilders.EventBuilder()
                .setCategory("Main")
                .setAction("Undo")
                .build());
    }

    private void newGame() {
        view.resetGame();
        //
        Tracker tracker = ((App) getApplication()).getTracker();
        tracker.send(new HitBuilders.EventBuilder()
                .setCategory("Main")
                .setAction("New")
                .build());
    }

    private void publishScore(HighScore highScore) {
        Intent i = new Intent(MainActivity.this, ScoresActivity.class);
        Bundle data = new Bundle();
        data.putLong(HighScore.GAME_STATUS, highScore.getStatus());
        data.putLong(HighScore.NUMBER_OF_MOVES, highScore.getScore());
        data.putLong(HighScore.ELAPSED_TIME, highScore.getTime());
        i.putExtras(data);
        startActivity(i);
        //
        Tracker tracker = ((App) getApplication()).getTracker();
        tracker.send(new HitBuilders.EventBuilder()
                .setCategory("Main")
                .setAction("Publish")
                .build());
    }

    private void searchLastMove() {
        view.switchHideArrow();
        //
        Tracker tracker = ((App) getApplication()).getTracker();
        tracker.send(new HitBuilders.EventBuilder()
                .setCategory("Main")
                .setAction("Search")
                .build());
    }
}
