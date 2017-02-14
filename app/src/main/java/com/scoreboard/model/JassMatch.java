package com.scoreboard.model;

import com.scoreboard.types.JassMeld;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class JassMatch {

    private final int teamCount;
    private final List<JassRound> rounds;
    private final int[] totalScores;
    private final int[] pointsGoal;
    private int currentRoundIndex;
    private int winnerIndex;

    public JassMatch(int teamCount) {
        if (teamCount <= 0) {
            throw new IllegalArgumentException("Invalid number of teams");
        }

        this.teamCount = teamCount;
        this.rounds = new ArrayList<>();
        this.rounds.add(new JassRound(teamCount));
        this.totalScores = new int[teamCount];
        this.pointsGoal = new int[teamCount];
        this.currentRoundIndex = 0;
        this.winnerIndex = -1;
    }

    public List<JassRound> getRounds() {
        return Collections.unmodifiableList(rounds);
    }

    public int getCurrentRoundIndex() {
        return currentRoundIndex;
    }

    public int getWinnerIndex() {
        return winnerIndex;
    }

    /**
     * Returns the score of the team at the given index for the current Round.
     *
     * @param teamIndex the index of the team
     * @return the score of the team, for the currentRound
     */
    public int getCurrentRoundTeamScore(int teamIndex) {
        return rounds.get(currentRoundIndex).getTeamTotalScore(teamIndex);
    }

    /**
     * Returns the total score of the team at the given index.
     *
     * @param teamIndex the index of the team
     * @return the total score of the team for the match
     */
    public int getTotalMatchScore(int teamIndex) {
        return totalScores[teamIndex];
    }

    public boolean meldWasSetThisRound() {
        return rounds.get(currentRoundIndex).meldWasSetThisRound();
    }

    public boolean goalHasBeenReached() {
        return updateGoalAndWinner();
    }

    public boolean allTeamsHaveReachedGoal() {
        boolean allTeamsHaveReachedGoal = true;
        for (int i = 0; i < teamCount && allTeamsHaveReachedGoal; ++i) {
            allTeamsHaveReachedGoal &= totalScores[i] >= pointsGoal[i];
        }
        return allTeamsHaveReachedGoal;
    }

    /**
     * Sets the score of the team at the given index to the given points.
     *
     * @param teamIndex the index of the team
     * @param score     the score of the team
     */
    public void setScore(int teamIndex, int score) {
        rounds.get(currentRoundIndex).setTeamScore(teamIndex, score);
        updateTotalScore(teamIndex, score);
    }

    /**
     * Adds the given meld to the team at the given index.
     *
     * @param teamIndex the index of the team
     * @param meld      the meld
     */
    public void setMeld(int teamIndex, JassMeld meld) {
        rounds.get(currentRoundIndex).addMeldToTeam(teamIndex, meld);
        updateTotalScore(teamIndex, meld.value());
    }

    public void setWinnerIndex(int winnerIndex) {
        this.winnerIndex = winnerIndex;
    }

    public void updatePointsGoal(int goal) {
        for (int i = 0; i < teamCount; ++i) {
            pointsGoal[i] = goal;
        }
    }

    public void updatePointsGoal(int teamIndex, int goal) {
        pointsGoal[teamIndex] = goal;
    }

    /**
     * Closes the currentRound, updating the scores, and starts a new round.
     */
    public void finishRound() {
        if (!goalHasBeenReached()) {
            rounds.add(new JassRound(teamCount));
            ++currentRoundIndex;
        }
    }

    /**
     * Cancels the last round, deleting the points obtained in that round.
     *
     * @param teamIndex the index of the last team that updated its score
     * @throws UnsupportedOperationException if there is no round to cancel
     */
    public void cancelLastRound(int teamIndex) throws UnsupportedOperationException {
        if (currentRoundIndex == 0 && !meldWasSetThisRound()) {
            throw new UnsupportedOperationException("Nothing to cancel");
        }

        if (meldWasSetThisRound()) {
            JassRound currentRound = rounds.get(currentRoundIndex);
            int meldValue = currentRound.cancelLastMeld(teamIndex);
            updateTotalScore(teamIndex, -meldValue);
        } else {
            rounds.remove(currentRoundIndex);
            --currentRoundIndex;
            for (int i = 0; i < teamCount; ++i) {
                JassRound cancelledRound = rounds.get(currentRoundIndex);
                int cardValue = cancelledRound.getTeamCardScore(i);
                updateTotalScore(i, -cardValue);
                cancelledRound.setTeamScore(i, 0);
            }
        }
    }

    private void updateTotalScore(int teamIndex, int score) {
        totalScores[teamIndex] += score;
        updateGoalAndWinner();
    }

    private boolean updateGoalAndWinner() {
        boolean goalHasBeenReached = false;
        for (int i = 0; i < teamCount; ++i) {
            if (!goalHasBeenReached && totalScores[i] >= pointsGoal[i]) {
                goalHasBeenReached = true;
                winnerIndex = winnerIndex == -1 ? i : winnerIndex;
            }
        }
        return goalHasBeenReached;
    }

}
