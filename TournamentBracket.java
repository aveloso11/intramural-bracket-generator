import java.util.*;

public class TournamentBracket {
    private Match root;
    private Team[] teams;
    private ScoreMatrix scoreMatrix;
    private int totalRounds;
    private List<Match> allMatches;
    private TournamentType tournamentType;
    private List<Match> losersBracketMatches;
    private Match grandFinals;
    private Map<Match, Match> winnerToLoserMatch;

    public TournamentBracket(Team[] teams) {
        this(teams, TournamentType.SINGLE_ELIMINATION);
    }

    public TournamentBracket(Team[] teams, TournamentType type) {
        this.teams = teams;
        this.scoreMatrix = new ScoreMatrix(teams);
        this.tournamentType = type;
        this.allMatches = new ArrayList<>();
        this.winnerToLoserMatch = new HashMap<>();
        this.losersBracketMatches = new ArrayList<>();

        if (type == TournamentType.SINGLE_ELIMINATION) {
            buildSingleElimination(teams);
        } else if (type == TournamentType.ROUND_ROBIN) {
            buildRoundRobin(teams);
        } else if (type == TournamentType.DOUBLE_ELIMINATION) {
            buildDoubleElimination(teams);
        } else if (type == TournamentType.SWISS) {
            buildSwissSystem(teams);
        } else if (type == TournamentType.FREE_FOR_ALL) {
            buildFreeForAll(teams);
        } else {
            buildSingleElimination(teams);
        }
    }

    public TournamentBracket(Team[] teams, String type) {
        this(teams, convertToTournamentType(type));
    }

    private static TournamentType convertToTournamentType(String type) {
        if (type == null) return TournamentType.SINGLE_ELIMINATION;
        switch (type) {
            case "Single Elimination": return TournamentType.SINGLE_ELIMINATION;
            case "Double Elimination": return TournamentType.DOUBLE_ELIMINATION;
            case "Round Robin":        return TournamentType.ROUND_ROBIN;
            case "Swiss System":       return TournamentType.SWISS;
            case "Free For All":       return TournamentType.FREE_FOR_ALL;
            default:                   return TournamentType.SINGLE_ELIMINATION;
        }
    }

    // =====================================================================
    // BUILD METHODS
    // =====================================================================

    private void buildSingleElimination(Team[] teams) {
        this.totalRounds = (int) Math.ceil(Math.log(teams.length) / Math.log(2));
        int bracketSize = (int) Math.pow(2, totalRounds);
        List<Match> currentRound = new ArrayList<>();

        for (int i = 0; i < bracketSize; i += 2) {
            Match match = new Match(1);
            if (i < teams.length)     match.setTeam1(teams[i]);
            if (i + 1 < teams.length) match.setTeam2(teams[i + 1]);
            currentRound.add(match);
            allMatches.add(match);
        }

        int round = 2;
        while (currentRound.size() > 1) {
            List<Match> nextRound = new ArrayList<>();
            for (int i = 0; i < currentRound.size(); i += 2) {
                Match parentMatch = new Match(round);
                parentMatch.setLeftChild(currentRound.get(i));
                parentMatch.setRightChild(currentRound.get(i + 1));
                nextRound.add(parentMatch);
                allMatches.add(parentMatch);
            }
            currentRound = nextRound;
            round++;
        }

        this.totalRounds = round - 1;
        this.root = currentRound.get(0);
    }

    private void buildRoundRobin(Team[] teams) {
        this.totalRounds = 1;
        this.allMatches.clear();
        for (int i = 0; i < teams.length; i++) {
            for (int j = i + 1; j < teams.length; j++) {
                Match match = new Match(1);
                match.setTeam1(teams[i]);
                match.setTeam2(teams[j]);
                allMatches.add(match);
            }
        }
        this.root = null;
    }

    private void buildDoubleElimination(Team[] teams) {
        int numTeams   = teams.length;
        int numRounds  = (int) Math.ceil(Math.log(numTeams) / Math.log(2));
        int bracketSize = (int) Math.pow(2, numRounds);

        allMatches.clear();
        losersBracketMatches = new ArrayList<>();
        winnerToLoserMatch.clear();

        this.totalRounds = numRounds * 2;

        // -----------------------------------------------------------------
        // WINNERS BRACKET — tag every match with setIsWinnersBracket(true)
        // -----------------------------------------------------------------
        List<Match> winnersRound      = new ArrayList<>();
        List<Match> allWinnersMatches = new ArrayList<>();

        int matchId = 1;
        for (int i = 0; i < bracketSize; i += 2) {
            Match match = new Match(1);
            match.setMatchId(matchId++);
            match.setIsWinnersBracket(true);          // ← tag
            if (i < numTeams)     match.setTeam1(teams[i]);
            if (i + 1 < numTeams) match.setTeam2(teams[i + 1]);
            if (match.getTeam1() != null || match.getTeam2() != null) {
                winnersRound.add(match);
                allWinnersMatches.add(match);
                allMatches.add(match);
            }
        }

        int round = 2;
        while (winnersRound.size() > 1) {
            List<Match> nextRound = new ArrayList<>();
            for (int i = 0; i < winnersRound.size(); i += 2) {
                Match parentMatch = new Match(round);
                parentMatch.setMatchId(matchId++);
                parentMatch.setIsWinnersBracket(true);    // ← tag
                if (i < winnersRound.size())     parentMatch.setLeftChild(winnersRound.get(i));
                if (i + 1 < winnersRound.size()) parentMatch.setRightChild(winnersRound.get(i + 1));
                nextRound.add(parentMatch);
                allWinnersMatches.add(parentMatch);
                allMatches.add(parentMatch);
            }
            winnersRound = nextRound;
            round++;
        }

        Match winnersFinal = winnersRound.isEmpty() ? null : winnersRound.get(0);
        if (winnersFinal != null) {
            winnersFinal.setRound(numRounds);
        }

        // -----------------------------------------------------------------
        // LOSERS BRACKET — NOT tagged (isWinnersBracket stays false)
        // -----------------------------------------------------------------
        int losersMatchId = 1000;
        Map<Integer, List<Match>> losersByRound = new HashMap<>();

        for (int lr = 2; lr <= (numRounds * 2 - 2); lr++) {
            int matchesInRound;
            if (lr <= numRounds) {
                matchesInRound = (int) Math.pow(2, numRounds - lr);
            } else {
                matchesInRound = (int) Math.pow(2, lr - numRounds - 1);
            }
            if (matchesInRound < 1) matchesInRound = 1;

            List<Match> roundMatches = new ArrayList<>();
            for (int i = 0; i < matchesInRound; i++) {
                Match losersMatch = new Match(lr);
                losersMatch.setMatchId(losersMatchId++);
                // isWinnersBracket deliberately left false
                roundMatches.add(losersMatch);
                losersBracketMatches.add(losersMatch);
                allMatches.add(losersMatch);
            }
            losersByRound.put(lr, roundMatches);
        }

        // Connect losers bracket matches to each other
        for (int lr = 2; lr <= (numRounds * 2 - 3); lr++) {
            List<Match> currentRoundL = losersByRound.get(lr);
            List<Match> nextRoundL    = losersByRound.get(lr + 1);

            if (currentRoundL != null && nextRoundL != null) {
                for (int i = 0; i < nextRoundL.size() && i * 2 + 1 < currentRoundL.size(); i++) {
                    if (i * 2 < currentRoundL.size())
                        nextRoundL.get(i).setLeftChild(currentRoundL.get(i * 2));
                    if (i * 2 + 1 < currentRoundL.size())
                        nextRoundL.get(i).setRightChild(currentRoundL.get(i * 2 + 1));
                }
            }
        }

        // Connect winners losers to losers bracket
        for (Match winnersMatch : allWinnersMatches) {
            int winnersRoundNum   = winnersMatch.getRound();
            int targetLosersRound = winnersRoundNum + 1;

            List<Match> targetRound = losersByRound.get(targetLosersRound);
            if (targetRound != null && !targetRound.isEmpty()) {
                int position   = findPositionInWinnersBracket(winnersMatch, allWinnersMatches, bracketSize);
                int losersIndex = position / 2;
                if (losersIndex < targetRound.size()) {
                    winnerToLoserMatch.put(winnersMatch, targetRound.get(losersIndex));
                }
            }
        }

        // -----------------------------------------------------------------
        // GRAND FINALS
        // -----------------------------------------------------------------
        Match losersFinal = null;
        List<Match> lastLosersRound = losersByRound.get(numRounds * 2 - 2);
        if (lastLosersRound != null && !lastLosersRound.isEmpty()) {
            losersFinal = lastLosersRound.get(0);
        }

        if (winnersFinal != null) {
            grandFinals = new Match(totalRounds);
            grandFinals.setMatchId(matchId);
            grandFinals.setLeftChild(winnersFinal);
            if (losersFinal != null) grandFinals.setRightChild(losersFinal);
            allMatches.add(grandFinals);
            this.root = grandFinals;
        } else {
            this.root = null;
        }

        System.out.println("Double Elimination created: " + allMatches.size() + " total matches");
        System.out.println("  Winners matches: " + allWinnersMatches.size());
        System.out.println("  Losers matches:  " + losersBracketMatches.size());
        System.out.println("  Grand Final:     " + (grandFinals != null));
    }

    private int findPositionInWinnersBracket(Match match, List<Match> allWinnersMatches, int bracketSize) {
        for (int i = 0; i < allWinnersMatches.size(); i++) {
            if (allWinnersMatches.get(i) == match) {
                int matchesInRound = (int) Math.pow(2, totalRounds / 2 - match.getRound());
                if (matchesInRound < 1) matchesInRound = 1;
                return i % matchesInRound;
            }
        }
        return 0;
    }

    private void buildSwissSystem(Team[] teams) {
        this.totalRounds = Math.min(5, teams.length / 2);
        this.allMatches.clear();
        List<Team> shuffled = new ArrayList<>(Arrays.asList(teams));
        Collections.shuffle(shuffled);
        for (int r = 1; r <= totalRounds; r++) {
            for (int i = 0; i < shuffled.size(); i += 2) {
                if (i + 1 < shuffled.size()) {
                    Match match = new Match(r);
                    match.setTeam1(shuffled.get(i));
                    match.setTeam2(shuffled.get(i + 1));
                    allMatches.add(match);
                }
            }
            Collections.shuffle(shuffled);
        }
        this.root = null;
    }

    private void buildFreeForAll(Team[] teams) {
        this.totalRounds = 2;
        this.allMatches.clear();
        for (int i = 0; i < teams.length; i++) {
            for (int j = i + 1; j < teams.length; j++) {
                Match match = new Match(1);
                match.setTeam1(teams[i]);
                match.setTeam2(teams[j]);
                allMatches.add(match);
            }
        }
        this.root = null;
    }

    // =====================================================================
    // QUERY METHODS
    // =====================================================================

    /**
     * Returns all matches in a given round (winners + losers mixed).
     * Used by single elimination and generic callers.
     */
    public List<Match> getMatchesByRound(int round) {
        List<Match> result = new ArrayList<>();
        for (Match match : allMatches) {
            if (match.getRound() == round) result.add(match);
        }
        result.sort(Comparator.comparingInt(Match::getMatchId));
        return result;
    }

    /**
     * Returns only winners-bracket matches for a given round.
     * Used by the double elimination display so losers matches
     * with the same round number are not mixed in.
     */
    public List<Match> getWinnersMatchesByRound(int round) {
        List<Match> result = new ArrayList<>();
        for (Match match : allMatches) {
            if (match.getRound() == round
                    && match != grandFinals
                    && match.isWinnersBracket()) {
                result.add(match);
            }
        }
        result.sort(Comparator.comparingInt(Match::getMatchId));
        return result;
    }

    public List<Match> getAllMatches() { return allMatches; }

    public List<Match> getWinnersBracketMatches() {
        List<Match> winners = new ArrayList<>();
        for (Match match : allMatches) {
            if (match.isWinnersBracket() && match != grandFinals) {
                winners.add(match);
            }
        }
        return winners;
    }

    public int   getTotalRounds()  { return totalRounds; }
    public Team[] getTeams()       { return teams; }
    public Match getChampionship() { return root; }
    public ScoreMatrix getScoreMatrix() { return scoreMatrix; }

    public List<Match> getLosersBracketMatches() { return losersBracketMatches; }
    public Match       getGrandFinals()           { return grandFinals; }

    // =====================================================================
    // RECORD WINNER / PROPAGATION
    // =====================================================================

    public void recordWinner(Match match, Team winner, int score1, int score2) {
        if (match.getTeam1() == null || match.getTeam2() == null) {
            System.out.println("Error: Match does not have both teams assigned!");
            return;
        }

        match.setWinner(winner, score1, score2);

        Team team1 = match.getTeam1();
        Team team2 = match.getTeam2();
        scoreMatrix.recordMatch(team1.getId(), team2.getId(), score1, score2);

        if (tournamentType == TournamentType.SINGLE_ELIMINATION
                || tournamentType == TournamentType.DOUBLE_ELIMINATION) {
            propagateWinnerUp(match, winner);
        }

        System.out.println("✓ Recorded: " + match);
    }

    private void propagateWinnerUp(Match currentMatch, Team winner) {
        Team loser = (currentMatch.getTeam1() == winner)
                     ? currentMatch.getTeam2()
                     : currentMatch.getTeam1();

        if (tournamentType == TournamentType.DOUBLE_ELIMINATION && loser != null) {
            Match losersMatch = winnerToLoserMatch.get(currentMatch);
            if (losersMatch != null && !losersMatch.isCompleted()) {
                if (losersMatch.getTeam1() == null) {
                    losersMatch.setTeam1(loser);
                    System.out.println("→ Loser " + loser.getName()
                            + " sent to losers bracket round " + losersMatch.getRound());
                } else if (losersMatch.getTeam2() == null
                        && losersMatch.getTeam1() != loser) {
                    losersMatch.setTeam2(loser);
                    System.out.println("→ Loser " + loser.getName()
                            + " sent to losers bracket round " + losersMatch.getRound());
                }
            }
        }

        for (Match match : allMatches) {
            if (match.getLeftChild() == currentMatch) {
                match.setTeam1(winner);
                checkAndCompleteMatch(match);
                propagateWinnerUp(match, winner);
                return;
            } else if (match.getRightChild() == currentMatch) {
                match.setTeam2(winner);
                checkAndCompleteMatch(match);
                propagateWinnerUp(match, winner);
                return;
            }
        }
    }

    private void checkAndCompleteMatch(Match match) {
        if (match.getTeam1() != null && match.getTeam2() != null && !match.isCompleted()) {
            System.out.println("→ Match ready: " + match);
        }
    }

    public List<Match> getPendingMatches() {
        List<Match> pending = new ArrayList<>();
        for (Match match : allMatches) {
            if (!match.isCompleted()
                    && match.getTeam1() != null
                    && match.getTeam2() != null) {
                pending.add(match);
            }
        }
        return pending;
    }

    public Match getCurrentMatch() {
        List<Match> pending = getPendingMatches();
        return pending.isEmpty() ? null : pending.get(0);
    }

    public String getProgress() {
        int total = allMatches.size();
        int completed = 0;
        for (Match m : allMatches) if (m.isCompleted()) completed++;
        return completed + "/" + total + " matches completed";
    }

    public Team getTournamentWinner() {
        if (tournamentType == TournamentType.ROUND_ROBIN
                || tournamentType == TournamentType.SWISS
                || tournamentType == TournamentType.FREE_FOR_ALL) {
            Team champion = null;
            int mostWins = -1;
            for (Team team : teams) {
                if (team.getWins() > mostWins) {
                    mostWins = team.getWins();
                    champion = team;
                } else if (team.getWins() == mostWins && champion != null) {
                    if (team.getPointDifference() > champion.getPointDifference()) {
                        champion = team;
                    }
                }
            }
            return champion;
        }

        if (root != null && root.isCompleted()) return root.getWinner();
        return null;
    }

    // =====================================================================
    // DEBUG / PRINT
    // =====================================================================

    public void printBracket() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("🏆 TOURNAMENT BRACKET - " + tournamentType.getDisplayName() + " 🏆");
        System.out.println("=".repeat(60));

        if (tournamentType == TournamentType.DOUBLE_ELIMINATION) {
            System.out.println("\n📍 WINNERS BRACKET:");
            System.out.println("-".repeat(40));
            for (int r = 1; r <= totalRounds / 2; r++) {
                System.out.println("\n  Round " + r + ":");
                for (Match match : getWinnersMatchesByRound(r)) {
                    System.out.println("    " + match);
                }
            }

            System.out.println("\n📍 LOSERS BRACKET:");
            System.out.println("-".repeat(40));
            for (Match match : losersBracketMatches) {
                System.out.println("    Round " + match.getRound() + ": " + match);
            }

            if (grandFinals != null) {
                System.out.println("\n📍 GRAND FINALS:");
                System.out.println("-".repeat(40));
                System.out.println("    " + grandFinals);
            }
        } else {
            for (int r = 1; r <= totalRounds; r++) {
                System.out.println("\n📍 ROUND " + r + ":");
                System.out.println("-".repeat(40));
                for (Match match : getMatchesByRound(r)) {
                    System.out.println("    " + match);
                }
            }
        }

        System.out.println("\n" + "=".repeat(60));
    }

    public void printStandings() {
        System.out.println("\n📊 TEAM STANDINGS:");
        System.out.println("-".repeat(50));
        List<Team> sortedTeams = Arrays.asList(teams);
        sortedTeams.sort((a, b) -> Integer.compare(b.getWins(), a.getWins()));
        for (int i = 0; i < sortedTeams.size(); i++) {
            Team t = sortedTeams.get(i);
            System.out.printf("%d. %-12s | Wins: %d | Losses: %d | PD: %d | Win%%: %.1f%%\n",
                    i + 1, t.getName(), t.getWins(), t.getLosses(),
                    t.getPointDifference(), t.getWinPercentage());
        }
        System.out.println("-".repeat(50));
    }
}