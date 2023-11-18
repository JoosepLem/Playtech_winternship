package main.java;

import java.util.UUID;

/**
 * Match class
 */
class Match {
    private UUID matchId;
    private double rateA;
    private double rateB;
    private char result;

    /**
     * Match class constructor where I give all the variables their values.
     * @param matchId match UUID
     * @param rateA A side rate to calculate winnings
     * @param rateB B side rate to calculate winnings
     * @param result Which side won
     */
    public Match(UUID matchId, double rateA, double rateB, char result) {
        this.matchId = matchId;
        this.rateA = rateA;
        this.rateB = rateB;
        this.result = result;
    }

    public double getRateA() {
        return rateA;
    }

    public double getRateB() {
        return rateB;
    }

    public char getResult() {
        return result;
    }
}