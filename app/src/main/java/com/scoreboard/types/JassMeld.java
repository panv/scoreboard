package com.scoreboard.types;

public enum JassMeld {

    MARRIAGE("Stöck"),
    THREE_CARDS("Twenty"),
    FIFTY("Fifty"),
    HUNDRED("Hundred"),
    FOUR_NINE("Hundred and fifty"),
    FOUR_JACKS("Two hundred");

    private final String meldName;

    JassMeld(String meldName) {
        this.meldName = meldName;
    }

    @Override
    public String toString() {
        return meldName;
    }

    public int value() {
        switch (this) {
            case MARRIAGE:
            case THREE_CARDS:
                return 20;
            case FIFTY:
                return 50;
            case HUNDRED:
                return 100;
            case FOUR_NINE:
                return 150;
            case FOUR_JACKS:
                return 200;
            default:
                return 0;
        }
    }

}
