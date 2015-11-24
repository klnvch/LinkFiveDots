package by.klnvch.link5dots.online;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import by.klnvch.link5dots.GameView;
import by.klnvch.link5dots.HighScore;
import by.klnvch.link5dots.Dot;
import by.klnvch.link5dots.R;
import by.klnvch.link5dots.settings.SettingsUtils;

public class OnlineActivity extends AppCompatActivity {

    private GameView view;

    private String userName = "";
    private String enemyName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.game_board);

        view = (GameView)findViewById(R.id.game_view);

        view.setOnGameEventListener(new GameView.OnGameEventListener() {
            @Override
            public void onMoveDone(Dot currentDot, Dot previousDot) {

            }
            @Override
            public void onGameEnd(HighScore highScore) {

            }
        });

        userName = SettingsUtils.getUserName(this, null);
        if (userName != null) {
            TextView tvUsername = (TextView)findViewById(R.id.user_name);
            tvUsername.setText(userName);
        }
        TextView tvOpponentName = (TextView)findViewById(R.id.opponent_name);
        tvOpponentName.setText("-");
    }
}
