package by.klnvch.link5dots;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class TwoPlayersActivity extends AppCompatActivity  {

    private GameView view;
    private AlertDialog alertDialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.play);

        view = (GameView)findViewById(R.id.game_view);

        view.setOnGameEventListener(new GameView.OnGameEventListener() {
            @Override
            public void onMoveDone(Offset currentDot, Offset previousDot) {
                if (previousDot == null || previousDot.getType() == Offset.OPPONENT) {
                    // set user dot
                    currentDot.setType(Offset.USER);
                    view.setDot(currentDot);
                } else {
                    currentDot.setType(Offset.OPPONENT);
                    view.setDot(currentDot);
                }
            }

            @Override
            public void onGameEnd(HighScore highScore) {
                showAlertDialog(highScore);
            }
        });
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
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId()){
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

    private void showAlertDialog(final HighScore highScore){
        if(alertDialog == null || !alertDialog.isShowing()) {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            String str = getString(R.string.end_move, highScore.getScore(), highScore.getTime());
            builder.setMessage(str);
            builder.setPositiveButton(R.string.end_new_game, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    newGame();
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

    private void undoLastMove(){
        view.undoLastMove(1);
    }

    private void newGame(){
        view.resetGame();
    }

    private void searchLastMove(){
        view.switchHideArrow();
    }
}
