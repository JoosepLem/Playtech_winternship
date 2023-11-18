package main.java;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;

/**
 * Main class where all the logic happens
 */
public class Main {
    private static final Map<UUID, Player> players = new TreeMap<>(); // Treemap of players with their UUID-s
    private static final List<String> illegitimatePlayersinfo = new ArrayList<>(); // Arraylist of illegal player info that gets written into the result file
    private static final List<UUID> illegitimatePlayers = new ArrayList<>(); // Arraylist of UUID-s of the illegal players to access them quickly
    private static final Map<UUID, Match> matches = new TreeMap<>(); // Treemap of matches to access them quickly
    private static long casinoBalance = 0;

    /**
     * The Main function that calls out two functions that read in the data and process it and then writes it into the result file
     */
    public static void main(String[] args) {
        processInputFiles();
        processResultsFile();
    }

    /**
     * This function reads in all the data from the files and at the same time starts processing it
     */
    private static void processInputFiles() {
        try (Scanner playerScanner = new Scanner(new File("src/main/resources/player_data.txt"));
             Scanner matchScanner = new Scanner(new File("src/main/resources/match_data.txt"))) {

            while (matchScanner.hasNextLine()) {
                String[] matchData = matchScanner.nextLine().split(",");
                processMatchResult(matchData);
            }

            while (playerScanner.hasNextLine()) {
                String[] playerData = playerScanner.nextLine().split(",");
                processPlayerAction(playerData);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * This function creates Match objects that are put into the matches Treemap.
     * @param matchData one line from match_data.txt that contains the information of a match.
     */
    private static void processMatchResult(String[] matchData) {
        UUID matchId = UUID.fromString(matchData[0]);
        double rateA = Double.parseDouble(matchData[1]);
        double rateB = Double.parseDouble(matchData[2]);
        char result = matchData[3].charAt(0);
        Match match = new Match(matchId, rateA, rateB, result);

        matches.put(matchId, match);
    }

    /**
     * Takes the playerData that is read in from player_data.txt and then starts processing it by the actions that they did while playing
     * @param playerData List of all the actions that the players did
     */
    private static void processPlayerAction(String[] playerData) {
        UUID playerId = UUID.fromString(playerData[0]);
        String operation = playerData[1];
        UUID matchId = null;
        if (playerData.length > 2 && !playerData[2].isEmpty()) { // Checks if the operation has something to do with a particular match and then gets the matchID
            matchId = UUID.fromString(playerData[2]);
        }
        int coins = Integer.parseInt((playerData[3]));
        switch (operation) { // Calls out the proper function to process the current player action
            case "DEPOSIT" -> processDeposit(playerId, coins);
            case "BET" -> processBet(playerId, matchId, coins, playerData[4]);
            case "WITHDRAW" -> processWithdraw(playerId, coins);
            default -> {
            }
        }
    }

    /**
     * Checks if this player has already been listed and if not then creates a player and then deposits the money into his balance
     * @param playerId UUID of the player that made the deposit
     * @param coins Number of coins they want to deposit
     */
    private static void processDeposit(UUID playerId, int coins) {
        Player player = players.computeIfAbsent(playerId, k -> new Player(playerId));
        player.updateBalance(coins);
    }

    /**
     * This functions checks first if the player is still legal and if it is then also checks if the player already exists.
     * Then it does the check if the bet that the player wants to make is legal and correct, and if not, then the player is illegal.
     * Then it checks if the player wins, draws or loses and updates all the balances accordingly.
     * @param playerId UUID of the player
     * @param matchId UUID of the match
     * @param coins Number of coins that went into the bet
     * @param side Side that the player put his coin onto
     */
    private static void processBet(UUID playerId, UUID matchId, int coins, String side) {
        if (illegitimatePlayers.contains(playerId)) return;

        Player player = players.computeIfAbsent(playerId, k -> new Player(playerId));
        if (player.getBalance() < coins || coins <= 0) {
            illegitimatePlayersinfo.add(playerId + " " + "BET" + " " + matchId + " " + coins + " " + side);
            illegitimatePlayers.add(playerId);
            return;
        }
        Match match = matches.get(matchId);
        player.incrementPlacedBets();
        char result = match.getResult();
        if ((side.equals("A") && result == 'A') || (side.equals("B") && result == 'B')) {
            double rate = (side.equals("A") ? match.getRateA() : match.getRateB());
            double winnings = Math.floor(coins * rate);
            player.updateBalance((long) winnings);
            player.incrementWonGames();
            player.updateCasinoMoney((long) winnings);
            casinoBalance -= winnings;
        } else if (result == 'D') {
            player.updateBalance(0);
        } else {
            player.updateBalance(-coins);
            player.updateCasinoMoney(-coins);
            casinoBalance += coins;
        }
    }

    /**
     *
     * Checks if the player is still a legal player before doing the withdrawal process.
     * Then check if the withdrawal request is correct and if not, then put the players info correctly into an Arraylist that later is written into the result file.
     * If everything checks out, then the player balance gets updated and the withdrawal process ends.
     * @param playerId UUID of the player
     * @param coins Number of coins that the player wants to withdraw
     */
    private static void processWithdraw(UUID playerId, int coins) {
        if (illegitimatePlayers.contains(playerId)) return;
        Player player = players.computeIfAbsent(playerId, k -> new Player(playerId));
        if (player.getBalance() < coins || coins <= 0) {
            illegitimatePlayersinfo.add(playerId + " " + "WITHDRAW" + " " + "null" + " " + coins + " " + "null");
            illegitimatePlayers.add(playerId);
            return;
        }
        player.updateBalance(-coins);
    }

    /**
     * This creates the result file into the same directory as is my Main class and calls out the functions that write the needed information in correct ways into the file
     */
    private static void processResultsFile() {
        try (PrintWriter writer = new PrintWriter("src/main/java/result.txt")) {
            writeLegitimatePlayers(writer);
            writeIllegitimatePlayers(writer);
            writeCasinoBalance(writer);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Checks if the legal players list is empty and if not then writes them on separate lines in the correct way and then prints and empty line between legal and illegal players.
     * If the list is empty, then it just writes two empty lines and ends as is mentioned in the task.
     * @param writer File parameter
     */
    private static void writeLegitimatePlayers(PrintWriter writer) {
        if (!players.isEmpty()) { // Treemap with builtin UUID sorts the map automatically
            for (Player player : players.values()) {
                if (!illegitimatePlayers.contains(player.getPlayerId()))
                    writer.println(player.getPlayerId() + " " + player.getBalance() + " " + player.getWinRate());
            }
            writer.println();
        } else {
            writer.println();
            writer.println();
        }
    }

    /**
     * Checks if the illegal players list is empty and if not then writes them on separate lines in the correct way and then prints and empty line between illegal players and casino balance.
     * Also, it creates a new Hashset to remember what players have already been written into the result file so no player is written twice.
     * When an illegal player is found, then all of his bets will be canceled out from the casino balance, so these do not affect it at all.
     * If the list is empty, then it just writes two empty lines and ends as is mentioned in the task.
     * @param writer File parameter
     */
    private static void writeIllegitimatePlayers(PrintWriter writer) {
        if (!illegitimatePlayersinfo.isEmpty()) {
            Set<UUID> writtenPlayers = new HashSet<>(); // To track written players

            for (String illegalOperation : illegitimatePlayersinfo) {
                String[] parts = illegalOperation.split(" ");
                UUID playerId = UUID.fromString(parts[0]);
                long illegalMoney = players.get(playerId).getCasinoMoney();
                if (illegalMoney > 0) {
                    casinoBalance += illegalMoney;
                } else {
                    casinoBalance -= illegalMoney;
                }

                if (!writtenPlayers.contains(playerId)) {
                    writer.println(illegalOperation);
                    writtenPlayers.add(playerId);
                }
            }
            writer.println();
        } else {
            writer.println();
            writer.println();
        }
    }

    /**
     * This just writes the casino balance into the result file.
     * @param writer File parameter
     */
    private static void writeCasinoBalance(PrintWriter writer) {
        writer.println(casinoBalance);
    }
}