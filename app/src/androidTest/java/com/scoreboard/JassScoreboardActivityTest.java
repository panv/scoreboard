package com.scoreboard;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.View;
import android.view.ViewParent;
import android.widget.TableLayout;
import android.widget.TableRow;

import com.scoreboard.types.JassMeld;
import com.scoreboard.utils.ToastMatcher;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.pressBack;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.scoreboard.types.JassMeld.FIFTY;
import static com.scoreboard.types.JassMeld.FOUR_JACKS;
import static com.scoreboard.types.JassMeld.FOUR_NINE;
import static com.scoreboard.types.JassMeld.HUNDRED;
import static com.scoreboard.types.JassMeld.MARRIAGE;
import static com.scoreboard.types.JassMeld.THREE_CARDS;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

@RunWith(AndroidJUnit4.class)
public final class JassScoreboardActivityTest {

    @Rule
    public ActivityTestRule<JassScoreboardActivity> activityTestRule =
            new ActivityTestRule<>(JassScoreboardActivity.class);

    @Test
    public void updateButtonCorrectlyIncrementsScore() {
        incrementScore(0, "2", false);
        checkScoreDisplay("2", "155");
        incrementScore(1, "5", false);
        checkScoreDisplay("154", "160");
    }

    @Test
    public void matchButtonCorrectlyIncrementsScore() {
        onView(withId(R.id.score_update_1)).perform(click());
        onView(withId(R.id.score_picker_match)).perform(click());
        checkScoreDisplay("257", "0");
        onView(withId(R.id.score_update_2)).perform(click());
        onView(withId(R.id.score_picker_match)).perform(click());
        checkScoreDisplay("257", "257");
    }

    @Test
    public void cancelUpdateDoesNotUpdateScore() {
        incrementScore(0, "50", false);
        checkScoreDisplay("50", "107");
        onView(withId(R.id.score_update_1)).perform(click());
        onView(withId(R.id.score_picker_cancel)).perform(click());
        checkScoreDisplay("50", "107");
        onView(withId(R.id.score_update_2)).perform(click());
        onView(withId(R.id.score_picker_cancel)).perform(click());
        checkScoreDisplay("50", "107");
    }

    @Test
    public void doublePointsCorrectlyUpdatesScore() {
        incrementScore(0, "100", true);
        checkScoreDisplay("200", "114");
        incrementScore(1, "50", true);
        checkScoreDisplay("414", "214");
    }

    @Test
    public void scoreIsCorrectlyUpdatedAfterNumpadCorrect() {
        onView(withId(R.id.score_update_1)).perform(click());
        onView(withId(R.id.numpad_1)).perform(click());
        onView(withId(R.id.numpad_3)).perform(click());
        onView(withId(R.id.numpad_4)).perform(click());
        onView(withId(R.id.numpad_correct)).perform(click());
        onView(withId(R.id.numpad_9)).perform(click());
        onView(withId(R.id.score_picker_confirm)).perform(click());
        checkScoreDisplay("139", "18");
    }

    @Test
    public void scoreIsCorrectlyUpdatedAfterNumpadCorrectTheSecond() {
        onView(withId(R.id.score_update_2)).perform(click());
        onView(withId(R.id.numpad_0)).perform(click());
        onView(withId(R.id.numpad_2)).perform(click());
        onView(withId(R.id.numpad_6)).perform(click());
        onView(withId(R.id.numpad_correct)).perform(click());
        onView(withId(R.id.numpad_correct)).perform(click());
        onView(withId(R.id.numpad_correct)).perform(click());
        onView(withId(R.id.score_picker_confirm)).perform(click());
        checkScoreDisplay("157", "0");
    }

    @Test
    public void toastIsDisplayedForInvalidPoints() {
        incrementScore(0, "500", false);
        String pointToast = getInstrumentation().getTargetContext().getResources()
                .getString(R.string.toast_invalid_score);
        pointToast = String.format(pointToast, 157);
        onView(withText(pointToast)).inRoot(new ToastMatcher())
                .check(matches(isDisplayed()));
    }

    @Test
    public void addingMeldCorrectlyUpdatesScore() {
        addMeld(0, MARRIAGE);
        checkScoreDisplay("20", "0");
        addMeld(1, FOUR_JACKS);
        checkScoreDisplay("20", "200");
    }

    @Test
    public void cancelButtonDisplaysToastWhenNothingToCancel() {
        onView(withId(R.id.scoreboard_cancel)).perform(click());
        onView(withText(R.string.toast_cannot_cancel)).inRoot(new ToastMatcher())
                .check(matches(isDisplayed()));
    }

    @Test
    public void cancelLastRoundCorrectlyUpdatesScore() {
        incrementScore(0, "60", false);
        checkScoreDisplay("60", "97");
        incrementScore(1, "33", false);
        checkScoreDisplay("184", "130");
        onView(withId(R.id.scoreboard_cancel)).perform(click());
        checkScoreDisplay("60", "97");
        onView(withId(R.id.scoreboard_cancel)).perform(click());
        checkScoreDisplay("0", "0");
    }

    @Test
    public void cancelLastMeldCorrectlyUpdatesScore() {
        incrementScore(1, "100", false);
        checkScoreDisplay("57", "100");
        addMeld(0, FOUR_JACKS);
        checkScoreDisplay("257", "100");
        onView(withId(R.id.scoreboard_cancel)).perform(click());
        checkScoreDisplay("57", "100");
    }

    @Test
    public void cancelSequenceIsCorrect() {
        addMeld(0, FOUR_NINE);
        incrementScore(0, "100", false);
        addMeld(1, FIFTY);
        incrementScore(1, "50", false);
        addMeld(0, HUNDRED);
        addMeld(0, THREE_CARDS);
        checkScoreDisplay("477", "157");
        onView(withId(R.id.scoreboard_cancel)).perform(click());
        checkScoreDisplay("457", "157");
        onView(withId(R.id.scoreboard_cancel)).perform(click());
        checkScoreDisplay("357", "157");
        onView(withId(R.id.scoreboard_cancel)).perform(click());
        checkScoreDisplay("250", "107");
        onView(withId(R.id.scoreboard_cancel)).perform(click());
        checkScoreDisplay("250", "57");
        onView(withId(R.id.scoreboard_cancel)).perform(click());
        checkScoreDisplay("150", "0");
        onView(withId(R.id.scoreboard_cancel)).perform(click());
        checkScoreDisplay("0", "0");
        onView(withId(R.id.scoreboard_cancel)).perform(click());
        onView(withText(R.string.toast_cannot_cancel)).inRoot(new ToastMatcher())
                .check(matches(isDisplayed()));
    }

    @Test
    public void endOfMatchMessageIsDisplayed() {
        for (int i = 0; i < 4; ++i) {
            onView(withId(R.id.score_update_1)).perform(click());
            onView(withId(R.id.score_picker_match)).perform(click());
        }
        String message = String.format(getInstrumentation().getTargetContext()
                .getResources().getString(R.string.dialog_game_end), "Team 1");
        onView(withText(message)).check(matches(isDisplayed()));
    }

    @Test
    public void correctWinnerIsDisplayedWhenBothTeamsHaveReachedGoal() {
        for (int i = 0; i < 3; ++i) {
            onView(withId(R.id.score_update_1)).perform(click());
            onView(withId(R.id.score_picker_match)).perform(click());
            onView(withId(R.id.score_update_2)).perform(click());
            onView(withId(R.id.score_picker_match)).perform(click());
        }
        addMeld(0, FOUR_JACKS);
        addMeld(1, FOUR_JACKS);
        incrementScore(1, "50", false);
        String message = String.format(getInstrumentation().getTargetContext()
                .getResources().getString(R.string.dialog_game_end), "Team 2");
        onView(withText(message)).check(matches(isDisplayed()));
    }

    @Test
    public void scoreHistoryIsDisplayed() {
        onView(withId(R.id.score_display_1)).perform(click());
        onView(withId(R.id.table_score_history)).check(matches(isDisplayed()));
        onView(withId(R.id.table_score_history)).perform(pressBack());
    }

    @Test
    public void historyIsCorrect() {
        incrementScore(1, "50", false);
        incrementScore(0, "7", false);
        addMeld(1, THREE_CARDS);
        onView(withId(R.id.score_display_2)).perform(click());
        onView(atPositionInTable(1, 1)).check(matches(withText("50")));
        onView(atPositionInTable(1, 2)).check(matches(withText("150")));
        onView(atPositionInTable(2, 3)).check(matches(withText(THREE_CARDS.toString())));
        onView(withId(R.id.table_score_history)).perform(pressBack());
    }

    @Test
    public void setGoalCorrectlySetsGoal() {
        incrementScore(-1, "700", false);
        String playingTo = String.format(getInstrumentation().getTargetContext().getResources()
                .getString(R.string.jass_common_points_goal), Integer.toString(700));
        onView(withId(R.id.scoreboard_common_points_goal)).check(matches(withText(playingTo)));
    }

    @Test
    public void cancelSetGoalDoesNotUpdateGoal() {
        String playingTo = String.format(getInstrumentation().getTargetContext().getResources()
                .getString(R.string.jass_common_points_goal), Integer.toString(1000));
        onView(withId(R.id.scoreboard_common_points_goal)).check(matches(withText(playingTo)));
        onView(withId(R.id.scoreboard_common_points_goal)).perform(click());
        onView(withId(R.id.score_picker_cancel)).perform(click());
        onView(withId(R.id.scoreboard_common_points_goal)).check(matches(withText(playingTo)));
    }

    @Test
    public void splitGoalsDisplaysBothGoals() {
        onView(withId(R.id.scoreboard_split_team_goals)).perform(click());
        onView(withId(R.id.team_goal_1)).check(matches(isDisplayed()));
        onView(withId(R.id.team_goal_2)).check(matches(isDisplayed()));
        onView(withId(R.id.scoreboard_common_points_goal)).check(matches(not(isDisplayed())));
    }

    @Test
    public void canReturnToNormalModeFromSplitMode() {
        onView(withId(R.id.scoreboard_split_team_goals)).perform(click());
        onView(withId(R.id.scoreboard_common_points_goal)).check(matches(not(isDisplayed())));
        onView(withId(R.id.scoreboard_split_team_goals)).perform(click());
        onView(withId(R.id.scoreboard_common_points_goal)).check(matches(isDisplayed()));
    }

    @Test
    public void splitGoalsCanUpdateBothTeamsGoals() {
        onView(withId(R.id.scoreboard_split_team_goals)).perform(click());
        incrementScore(2, "456", false);
        onView(withId(R.id.team_goal_1)).check(matches(withText("456")));
        incrementScore(3, "789", false);
        onView(withId(R.id.team_goal_2)).check(matches(withText("789")));
    }

    private void checkScoreDisplay(String firstDisplay, String secondDisplay) {
        onView(withId(R.id.score_display_1)).check(matches(withText(firstDisplay)));
        onView(withId(R.id.score_display_2)).check(matches(withText(secondDisplay)));
    }

    private void incrementScore(int teamIndex, final String value, boolean doubleScore) {
        int button = 0;
        switch (teamIndex) {
            case 0:
                button = R.id.score_update_1;
                break;
            case 1:
                button = R.id.score_update_2;
                break;
            case 2:
                button = R.id.team_goal_1;
                break;
            case 3:
                button = R.id.team_goal_2;
                break;
            case -1:
                button = R.id.scoreboard_common_points_goal;
                break;
        }
        onView(withId(button)).perform(click());
        for (int i = 0; i < value.length(); ++i) {
            int buttonId = buttonId(value.charAt(i));
            onView(withId(buttonId)).perform(click());
        }
        if (doubleScore) {
            onView(withId(R.id.numpad_double_score)).perform(click());
        }
        onView(withId(R.id.score_picker_confirm)).perform(click());
    }

    private void addMeld(int teamIndex, JassMeld meld) {
        int meldSpinner = teamIndex == 0 ? R.id.meld_spinner_1 : R.id.meld_spinner_2;
        onView(withId(meldSpinner)).perform(click());
        onData(allOf(is(instanceOf(JassMeld.class)), is(meld))).perform(click());
    }

    private int buttonId(char value) {
        switch (value) {
            case '0':
                return R.id.numpad_0;
            case '1':
                return R.id.numpad_1;
            case '2':
                return R.id.numpad_2;
            case '3':
                return R.id.numpad_3;
            case '4':
                return R.id.numpad_4;
            case '5':
                return R.id.numpad_5;
            case '6':
                return R.id.numpad_6;
            case '7':
                return R.id.numpad_7;
            case '8':
                return R.id.numpad_8;
            case '9':
                return R.id.numpad_9;
            default:
                return -1;
        }
    }

    private static Matcher<View> atPositionInTable(final int x, final int y) {
        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("is at position # " + x + " , " + y);
            }

            @Override
            public boolean matchesSafely(View view) {
                ViewParent viewParent = view.getParent();
                if (!(viewParent instanceof TableRow)) {
                    return false;
                }
                TableRow row = (TableRow) viewParent;
                TableLayout table = (TableLayout) row.getParent();
                return table.indexOfChild(row) == y && row.indexOfChild(view) == x;
            }
        };
    }

}
