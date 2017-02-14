package com.scoreboard;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.scoreboard.model.JassMatch;
import com.scoreboard.model.JassRound;
import com.scoreboard.types.Caller;
import com.scoreboard.types.JassMeld;
import com.scoreboard.types.PickerMode;
import com.scoreboard.types.SplitMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static com.scoreboard.model.Constants.JASS_DEFAULT_POINTS_GOAL;
import static com.scoreboard.model.Constants.JASS_DEFAULT_TEAM_COUNT;
import static com.scoreboard.model.Constants.JASS_MATCH_POINTS;
import static com.scoreboard.model.Constants.JASS_TOTAL_POINTS_IN_ROUND;
import static com.scoreboard.types.Caller.FIRST_TEAM;
import static com.scoreboard.types.Caller.SECOND_TEAM;
import static com.scoreboard.types.PickerMode.COMMON_GOAL;
import static com.scoreboard.types.PickerMode.FIRST_TEAM_GOAL;
import static com.scoreboard.types.PickerMode.SCORE;
import static com.scoreboard.types.PickerMode.SECOND_TEAM_GOAL;
import static com.scoreboard.types.SplitMode.NORMAL;
import static com.scoreboard.types.SplitMode.SPLIT;

public class JassScoreboardActivity extends AppCompatActivity implements OnClickListener {

    private JassMatch match;

    private int scoreMultiplier;

    private SplitMode splitMode;
    private Caller caller;
    private Stack<Caller> meldCallers;

    private TextView firstTeamScoreDisplay;
    private TextView secondTeamScoreDisplay;
    private ImageButton cancelButton;

    private Toast toast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jass_scoreboard);

        match = new JassMatch(JASS_DEFAULT_TEAM_COUNT);
        splitMode = NORMAL;
        meldCallers = new Stack<>();

        setUp();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.score_display_1:
                displayScoreHistory(0);
                break;
            case R.id.score_display_2:
                displayScoreHistory(1);
                break;
            case R.id.score_update_1:
                caller = FIRST_TEAM;
                showNumPadScorePicker(SCORE);
                break;
            case R.id.score_update_2:
                caller = SECOND_TEAM;
                showNumPadScorePicker(SCORE);
                break;
            case R.id.meld_spinner_1:
                meldCallers.push(FIRST_TEAM);
                displayMeldSpinner(0);
                break;
            case R.id.meld_spinner_2:
                meldCallers.push(SECOND_TEAM);
                displayMeldSpinner(1);
                break;
            case R.id.scoreboard_cancel:
                if (match.getCurrentRoundIndex() == 0 && !match.meldWasSetThisRound()) {
                    Toast.makeText(this, R.string.toast_cannot_cancel, Toast.LENGTH_SHORT)
                            .show();
                    cancelButton.setEnabled(false);
                } else {
                    int teamIndex = match.meldWasSetThisRound() ? meldCallers.pop().ordinal() : 0;
                    match.cancelLastRound(teamIndex);
                    displayScore();
                }
                break;
            case R.id.scoreboard_common_points_goal:
                showNumPadScorePicker(COMMON_GOAL);
                break;
            case R.id.scoreboard_split_team_goals:
                switchSplitMode();
                break;
            case R.id.team_goal_1:
                showNumPadScorePicker(FIRST_TEAM_GOAL);
                break;
            case R.id.team_goal_2:
                showNumPadScorePicker(SECOND_TEAM_GOAL);
                break;
        }
    }

    private void setUp() {
        firstTeamScoreDisplay = (TextView) findViewById(R.id.score_display_1);
        firstTeamScoreDisplay.setOnClickListener(this);

        secondTeamScoreDisplay = (TextView) findViewById(R.id.score_display_2);
        secondTeamScoreDisplay.setOnClickListener(this);

        ImageButton firstTeamUpdateButton = (ImageButton) findViewById(R.id.score_update_1);
        firstTeamUpdateButton.setOnClickListener(this);

        ImageButton secondTeamUpdateButton = (ImageButton) findViewById(R.id.score_update_2);
        secondTeamUpdateButton.setOnClickListener(this);

        ImageButton firstMeldSpinner = (ImageButton) findViewById(R.id.meld_spinner_1);
        firstMeldSpinner.setOnClickListener(this);

        ImageButton secondMeldSpinner = (ImageButton) findViewById(R.id.meld_spinner_2);
        secondMeldSpinner.setOnClickListener(this);

        ImageButton splitGoals = (ImageButton) findViewById(R.id.scoreboard_split_team_goals);
        splitGoals.setOnClickListener(this);

        cancelButton = (ImageButton) findViewById(R.id.scoreboard_cancel);
        cancelButton.setOnClickListener(this);

        updatePointsGoal(JASS_DEFAULT_POINTS_GOAL);
    }

    private void showNumPadScorePicker(final PickerMode pickerMode) {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.numpad_score_picker);

        TextView title = (TextView) dialog.findViewById(R.id.score_picker_title);
        title.setText(pickerMode == SCORE ? R.string.jass_score_picker_prompt : R.string.jass_goal_picker_prompt);

        final TextView pointsDisplay = (TextView) dialog.findViewById(R.id.score_picker_score_display);
        pointsDisplay.setText("0");

        ViewGroup checkboxLayout = (ViewGroup) dialog.findViewById(R.id.score_picker_checkbox_layout);
        checkboxLayout.setVisibility(pickerMode == SCORE ? VISIBLE : INVISIBLE);

        scoreMultiplier = 1;
        CheckBox doubleScore = (CheckBox) dialog.findViewById(R.id.numpad_double_score);
        doubleScore.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                scoreMultiplier = isChecked ? 2 : 1;
            }
        });

        for (int i = 0; i < 10; ++i) {
            Button numpadI = (Button) dialog.findViewById(getButtonId(i));
            final int value = i;
            numpadI.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    updateScorePickerDisplay(pointsDisplay, value, pickerMode);
                }
            });
        }

        ImageButton numpadCorrect = (ImageButton) dialog.findViewById(R.id.numpad_correct);
        numpadCorrect.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                updateScorePickerDisplay(pointsDisplay, -1, pickerMode);
            }
        });

        Button matchButton = (Button) dialog.findViewById(R.id.score_picker_match);
        matchButton.setVisibility(pickerMode == SCORE ? VISIBLE : GONE);
        matchButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (caller) {
                    case FIRST_TEAM:
                        updateScore(JASS_MATCH_POINTS * scoreMultiplier, 0);
                        break;
                    case SECOND_TEAM:
                        updateScore(0, JASS_MATCH_POINTS * scoreMultiplier);
                        break;
                }
                dialog.dismiss();
                displayScore();
            }
        });

        Button confirmScore = (Button) dialog.findViewById(R.id.score_picker_confirm);
        confirmScore.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                int points = Integer.parseInt(pointsDisplay.getText().toString());
                switch (pickerMode) {
                    case SCORE:
                        computeScores(points * scoreMultiplier, scoreMultiplier);
                        break;
                    case COMMON_GOAL:
                        match.updatePointsGoal(points);
                        updatePointsGoal(points);
                        break;
                    case FIRST_TEAM_GOAL:
                        match.updatePointsGoal(0, points);
                        updateSplitGoal(0, points);
                        break;
                    case SECOND_TEAM_GOAL:
                        match.updatePointsGoal(1, points);
                        updateSplitGoal(1, points);
                        break;
                }
                dialog.dismiss();
            }
        });

        Button cancel = (Button) dialog.findViewById(R.id.score_picker_cancel);
        cancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private int getButtonId(int i) {
        switch (i) {
            case 0:
                return R.id.numpad_0;
            case 1:
                return R.id.numpad_1;
            case 2:
                return R.id.numpad_2;
            case 3:
                return R.id.numpad_3;
            case 4:
                return R.id.numpad_4;
            case 5:
                return R.id.numpad_5;
            case 6:
                return R.id.numpad_6;
            case 7:
                return R.id.numpad_7;
            case 8:
                return R.id.numpad_8;
            case 9:
                return R.id.numpad_9;
            default:
                return 0;
        }
    }

    private void switchSplitMode() {
        TextView commonGoal = (TextView) findViewById(R.id.scoreboard_common_points_goal);
        TextView firstTeamGoal = (TextView) findViewById(R.id.team_goal_1);
        TextView secondTeamGoal = (TextView) findViewById(R.id.team_goal_2);

        match.updatePointsGoal(JASS_DEFAULT_POINTS_GOAL);

        if (splitMode == NORMAL) {
            splitMode = SPLIT;

            commonGoal.setVisibility(INVISIBLE);

            String defaultGoal = Integer.toString(JASS_DEFAULT_POINTS_GOAL);

            firstTeamGoal.setVisibility(VISIBLE);
            firstTeamGoal.setText(defaultGoal);
            firstTeamGoal.setOnClickListener(this);

            secondTeamGoal.setVisibility(VISIBLE);
            secondTeamGoal.setText(defaultGoal);
            secondTeamGoal.setOnClickListener(this);
        } else {
            splitMode = NORMAL;
            commonGoal.setVisibility(VISIBLE);
            firstTeamGoal.setVisibility(GONE);
            secondTeamGoal.setVisibility(GONE);
        }
    }

    private void updateSplitGoal(int index, int newGoal) {
        int goalIndex = index == 0 ? R.id.team_goal_1 : R.id.team_goal_2;
        TextView goal = (TextView) findViewById(goalIndex);
        goal.setText(String.format(java.util.Locale.getDefault(), "%d", newGoal));
    }

    private void updatePointsGoal(int pointsGoal) {
        TextView goal = (TextView) findViewById(R.id.scoreboard_common_points_goal);
        String goalText = String.format(getString(R.string.jass_common_points_goal),
                "<b>" + pointsGoal + "</b>");
        goal.setText(Html.fromHtml(goalText));
        goal.setEnabled(true);
        goal.setOnClickListener(this);
    }

    private void displayMeldSpinner(final int teamIndex) {
        List<JassMeld> melds = new ArrayList<>(Arrays.asList(JassMeld.values()));
        final ArrayAdapter<JassMeld> meldAdapter = new ArrayAdapter<>(this, R.layout.spinner_meld, melds);
        new AlertDialog.Builder(this)
                .setAdapter(meldAdapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        JassMeld meld = meldAdapter.getItem(which);
                        match.setMeld(teamIndex, meld);
                        dialog.dismiss();
                        displayScore();
                        testEndOfMatch();
                        cancelButton.setEnabled(true);
                    }
                })
                .create().show();
    }

    private void computeScores(int callerScore, int scoreMultiplier) {
        int otherTeamScore = JASS_TOTAL_POINTS_IN_ROUND * scoreMultiplier - callerScore;
        switch (caller) {
            case FIRST_TEAM:
                updateScore(callerScore, otherTeamScore);
                break;
            case SECOND_TEAM:
                updateScore(otherTeamScore, callerScore);
                break;
        }
    }

    private void updateScore(int firstTeamScore, int secondTeamScore) {
        match.setScore(0, firstTeamScore);
        match.setScore(1, secondTeamScore);
        match.finishRound();
        cancelButton.setEnabled(true);
        displayScore();
        testEndOfMatch();
    }

    private void displayScore() {
        int firstTeamScore = match.getTotalMatchScore(0);
        firstTeamScoreDisplay.setText(
                String.format(java.util.Locale.getDefault(), "%d", firstTeamScore));
        int secondTeamScore = match.getTotalMatchScore(1);
        secondTeamScoreDisplay.setText(
                String.format(java.util.Locale.getDefault(), "%d", secondTeamScore));
    }

    private void updateScorePickerDisplay(TextView scoreDisplay, int value, PickerMode pickerMode) {
        String currentDisplay = scoreDisplay.getText().toString();
        if (value == -1) {
            if (currentDisplay.length() <= 1) {
                currentDisplay = "0";
            } else {
                currentDisplay = currentDisplay.substring(0, currentDisplay.length() - 1);
            }
        } else if (currentDisplay.equals("0") && value != 0) {
            currentDisplay = Integer.toString(value);
        } else if (!currentDisplay.equals("0")) {
            currentDisplay += value;
        }

        int displayedPoints = Integer.parseInt(currentDisplay);
        if (pickerMode == SCORE) {
            if (displayedPoints <= JASS_TOTAL_POINTS_IN_ROUND) {
                scoreDisplay.setText(currentDisplay);
            } else {
                displayToast(R.string.toast_invalid_score, JASS_TOTAL_POINTS_IN_ROUND);
            }
        } else {
            scoreDisplay.setText(currentDisplay);
        }
    }

    private void displayScoreHistory(int teamIndex) {
        final Dialog dialog = new Dialog(this) {
            @Override
            public boolean onTouchEvent(@NonNull MotionEvent event) {
                this.dismiss();
                return true;
            }
        };
        dialog.setContentView(R.layout.table_score_history);

        TableLayout tableLayout = (TableLayout) dialog.findViewById(R.id.table_score_history);
        LayoutInflater inflater = (LayoutInflater) getApplicationContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        int roundIndex = 1;

        for (JassRound round : match.getRounds()) {
            TableRow row = (TableRow) inflater.inflate(
                    R.layout.table_row_score_history, tableLayout, false);

            int black = ContextCompat.getColor(this, android.R.color.black);

            TextView roundIndexView = (TextView) row.findViewById(R.id.score_history_row_round_index);
            roundIndexView.setTextColor(black);
            roundIndexView.setText(String.format(java.util.Locale.getDefault(), "%d", roundIndex));
            ++roundIndex;

            TextView score = (TextView) row.findViewById(R.id.score_history_row_points);
            score.setTextColor(black);
            score.setText(String.format(java.util.Locale.getDefault(), "%d",
                    round.getTeamTotalScore(teamIndex)));

            TextView melds = (TextView) row.findViewById(R.id.score_history_row_melds);
            melds.setTextColor(black);
            melds.setText(round.meldsToString(teamIndex));

            tableLayout.addView(row);
        }

        dialog.show();
    }

    private void testEndOfMatch() {
        if (match.goalHasBeenReached()) {
            if (match.allTeamsHaveReachedGoal()) {
                match.setWinnerIndex(caller.ordinal());
            }
            displayEndOfMatchMessage(match.getWinnerIndex());
        }
    }

    private void displayEndOfMatchMessage(int winningTeamIndex) {
        String winningTeam = "Team " + (winningTeamIndex + 1);
        String message = String.format(getString(R.string.dialog_game_end), winningTeam);
        new AlertDialog.Builder(this)
                .setTitle(message)
                .setPositiveButton(R.string.dialog_leave, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(JassScoreboardActivity.this, JassScoreboardActivity.class);
                        startActivity(intent);
                        finish();
                        //recreate();
                    }
                })
                .setCancelable(false)
                .show();
    }

    private void displayToast(int stringId, int points) {
        if (toast != null) {
            toast.cancel();
        }
        toast = Toast.makeText(this,
                String.format(getString(stringId), points),
                Toast.LENGTH_SHORT);
        toast.show();
    }

}
