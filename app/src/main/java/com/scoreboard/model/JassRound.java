package com.scoreboard.model;

import com.scoreboard.types.JassMeld;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class JassRound {

    private final int teamCount;
    private final int[] scores;
    private final List<List<JassMeld>> melds;
    private final int[] meldScores;

    public JassRound(int teamCount) {
        if (teamCount <= 0) {
            throw new IllegalArgumentException("Invalid number of teams");
        }

        this.teamCount = teamCount;
        this.scores = new int[teamCount];
        this.melds = new ArrayList<>(teamCount);
        this.meldScores = new int[teamCount];

        for (int i = 0; i < teamCount; ++i) {
            scores[i] = 0;
            melds.add(new ArrayList<JassMeld>());
            meldScores[i] = 0;
        }
    }

    /**
     * Returns the points of the specified team, excluding points obtained
     * with melds.
     *
     * @param teamIndex the index of the team
     * @return the points of the team for this round
     */
    public int getTeamCardScore(int teamIndex) {
        checkTeamIndex(teamIndex);
        return scores[teamIndex];
    }

    /**
     * Returns the points obtained with melds for the specified team.
     *
     * @param teamIndex the index of the team
     * @return the meld points of the team
     */
    public int getTeamMeldScore(int teamIndex) {
        checkTeamIndex(teamIndex);
        return meldScores[teamIndex];
    }

    /**
     * Returns the total points obtained by the given team for this round
     * (melds and card points).
     *
     * @param teamIndex the index of the team
     * @return the total points for this round
     */
    public int getTeamTotalScore(int teamIndex) {
        checkTeamIndex(teamIndex);
        return getTeamCardScore(teamIndex) + getTeamMeldScore(teamIndex);
    }

    /**
     * Sets the score of the given team.
     *
     * @param teamIndex the index of the team
     * @param score     the score of the team
     */
    public void setTeamScore(int teamIndex, int score) {
        checkTeamIndex(teamIndex);
        scores[teamIndex] = score;
    }

    /**
     * Adds the given meld to the current meld list and adds its point value
     * to the meld score.
     *
     * @param teamIndex the index of the team
     * @param meld      the meld
     */
    public void addMeldToTeam(int teamIndex, JassMeld meld) {
        checkTeamIndex(teamIndex);
        melds.get(teamIndex).add(meld);
        meldScores[teamIndex] += meld.value();
    }

    /**
     * Cancels the last meld obtained by the specified team, and updates the score.
     *
     * @param teamIndex the index of the team
     * @return the value of the meld that was cancelled
     */
    public int cancelLastMeld(int teamIndex) {
        checkTeamIndex(teamIndex);
        List<JassMeld> teamMelds = melds.get(teamIndex);
        int meldValue = 0;
        if (!teamMelds.isEmpty()) {
            JassMeld meld = teamMelds.remove(teamMelds.size() - 1);
            meldValue = meld.value();
            meldScores[teamIndex] -= meldValue;
        }
        return meldValue;
    }

    /**
     * Checks whether a meld was obtained by a team this round.
     *
     * @return true if a meld was obtained, false otherwise
     */
    public boolean meldWasSetThisRound() {
        boolean toReturn = false;
        for (int i = 0; i < teamCount && !toReturn; ++i) {
            toReturn |= !melds.get(i).isEmpty();
        }
        return toReturn;
    }

    public String meldsToString(int teamIndex) {
        checkTeamIndex(teamIndex);
        List<JassMeld> teamMelds = melds.get(teamIndex);

        if (teamMelds.isEmpty()) {
            return "-";
        }

        StringBuilder meldsString = new StringBuilder();
        for (Iterator<JassMeld> iterator = teamMelds.iterator(); iterator.hasNext(); ) {
            meldsString.append(iterator.next().toString());
            if (iterator.hasNext()) {
                meldsString.append(",").append(System.getProperty("line.separator"));
            }
        }
        return meldsString.toString();
    }

    private void checkTeamIndex(int teamIndex) {
        if (teamIndex < 0 || teamIndex >= teamCount) {
            throw new IndexOutOfBoundsException("Invalid team index");
        }
    }

}
