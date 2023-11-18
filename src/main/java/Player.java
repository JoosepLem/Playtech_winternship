package main.java;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Player class
 */
class Player {
    private UUID playerId;
    private long balance;
    private int wonGames;
    private int placedBets;
    private long casinoMoney;

    /**
     * Constructor with the required parameters all the other variables are 0 in the beginning.
     * @param playerId players UUID
     */
    public Player(UUID playerId) {
        this.playerId = playerId;
        this.balance = 0;
        this.wonGames = 0;
        this.placedBets = 0;
        this.casinoMoney = 0;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public long getBalance() {
        return balance;
    }

    public void updateBalance(long amount) {
        this.balance += amount;
    }

    public void updateCasinoMoney(long amount) {this.casinoMoney += amount; }

    public long getCasinoMoney() {return casinoMoney; }

    public void incrementWonGames() {
        this.wonGames++;
    }

    public void incrementPlacedBets() {
        this.placedBets++;
    }

    public BigDecimal getWinRate() {
        return placedBets == 0 ? BigDecimal.ZERO : new BigDecimal(wonGames).divide(new BigDecimal(placedBets), 2, BigDecimal.ROUND_HALF_UP);
    }
}