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
    private Map<Match, Match> winnerToLoserMatch; // Maps winners bracket match to its corresponding losers bracket match

    public TournamentBracket(Team[] teams) {
        this(teams, TournamentType.SINGLE_ELIMINATION);
    }

    public TournamentBracket(Team[] teams, TournamentType type) {
        this.teams = teams;
        this.scoreMatrix = new ScoreMatrix(teams);
        this.tournamentType = type;
        this.allMatches = new ArrayList<Match>();
        this.winnerToLoserMatch = new HashMap<>();
        
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

    // String constructor for backward compatibility
    public TournamentBracket(Team[] teams, String type) {
        this(teams, convertToTournamentType(type));
    }

    private static TournamentType convertToTournamentType(String type) {
        if (type == null) return TournamentType.SINGLE_ELIMINATION;
        
        switch(type) {
            case "Single Elimination": return TournamentType.SINGLE_ELIMINATION;
            case "Double Elimination": return TournamentType.DOUBLE_ELIMINATION;
            case "Round Robin": return TournamentType.ROUND_ROBIN;
            case "Swiss System": return TournamentType.SWISS;
            case "Free For All": return TournamentType.FREE_FOR_ALL;
            default: return TournamentType.SINGLE_ELIMINATION;
        }
    }

    private void buildSingleElimination(Team[] teams) {
        this.totalRounds = (int) Math.ceil(Math.log(teams.length) / Math.log(2));
        int bracketSize = (int) Math.pow(2, totalRounds);
        List<Match> currentRound = new ArrayList<>();

        for (int i = 0; i < bracketSize; i += 2) {
            Match match = new Match(1);
            if (i < teams.length) match.setTeam1(teams[i]);
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
        int numTeams = teams.length;
        int numRounds = (int) Math.ceil(Math.log(numTeams) / Math.log(2));
        int bracketSize = (int) Math.pow(2, numRounds);
        
        allMatches.clear();
        losersBracketMatches = new ArrayList<>();
        winnerToLoserMatch.clear();
        this.totalRounds = (numRounds * 2);
        
        // Winners Bracket
        List<Match> winnersRound = new ArrayList<>();
        List<Match> allWinnersMatches = new ArrayList<>();
        
        // Create first round winners bracket matches
        for (int i = 0; i < bracketSize; i += 2) {
            Match match = new Match(1);
            if (i < numTeams) match.setTeam1(teams[i]);
            if (i + 1 < numTeams) match.setTeam2(teams[i + 1]);
            if (match.getTeam1() != null || match.getTeam2() != null) {
                winnersRound.add(match);
                allWinnersMatches.add(match);
                allMatches.add(match);
            }
        }
        
        // Build rest of winners bracket
        int round = 2;
        while (winnersRound.size() > 1) {
            List<Match> nextRound = new ArrayList<>();
            for (int i = 0; i < winnersRound.size(); i += 2) {
                if (i + 1 < winnersRound.size()) {
                    Match parentMatch = new Match(round);
                    parentMatch.setLeftChild(winnersRound.get(i));
                    parentMatch.setRightChild(winnersRound.get(i + 1));
                    nextRound.add(parentMatch);
                    allWinnersMatches.add(parentMatch);
                    allMatches.add(parentMatch);
                } else {
                    nextRound.add(winnersRound.get(i));
                }
            }
            winnersRound = nextRound;
            round++;
        }
        
        Match winnersFinal = winnersRound.isEmpty() ? null : winnersRound.get(0);
        
        // Create losers bracket matches and link them to winners bracket matches
        int losersRoundNum = 2;
        List<Match> previousLosersRound = new ArrayList<>();
        
        // For each winners bracket round, create corresponding losers bracket matches
        for (int r = 1; r <= numRounds - 1; r++) {
            int matchesInThisRound = (int) Math.pow(2, numRounds - r - 1);
            if (matchesInThisRound < 1) matchesInThisRound = 1;
            
            List<Match> thisLosersRound = new ArrayList<>();
            for (int i = 0; i < matchesInThisRound; i++) {
                Match losersMatch = new Match(losersRoundNum);
                thisLosersRound.add(losersMatch);
                losersBracketMatches.add(losersMatch);
                allMatches.add(losersMatch);
            }
            
            // Link this losers round to previous losers round
            if (!previousLosersRound.isEmpty()) {
                for (int i = 0; i < thisLosersRound.size() && i < previousLosersRound.size() / 2; i++) {
                    if (i * 2 + 1 < previousLosersRound.size()) {
                        thisLosersRound.get(i).setLeftChild(previousLosersRound.get(i * 2));
                        thisLosersRound.get(i).setRightChild(previousLosersRound.get(i * 2 + 1));
                    }
                }
            }
            
            previousLosersRound = thisLosersRound;
            losersRoundNum++;
        }
        
        // Link winners bracket matches to losers bracket matches
        for (int i = 0; i < allWinnersMatches.size(); i++) {
            Match winnersMatch = allWinnersMatches.get(i);
            int winnersRoundNum = winnersMatch.getRound();
            
            // Find corresponding losers bracket match
            int targetLosersRound = winnersRoundNum + 1;
            int matchIndex = i / 2;
            
            for (Match losersMatch : losersBracketMatches) {
                if (losersMatch.getRound() == targetLosersRound) {
                    if (!winnerToLoserMatch.containsKey(winnersMatch)) {
                        winnerToLoserMatch.put(winnersMatch, losersMatch);
                        break;
                    }
                }
            }
        }
        
        // Grand Finals
        Match losersFinal = previousLosersRound.isEmpty() ? null : previousLosersRound.get(0);
        
        if (winnersFinal != null) {
            grandFinals = new Match(totalRounds);
            grandFinals.setLeftChild(winnersFinal);
            if (losersFinal != null) {
                grandFinals.setRightChild(losersFinal);
            }
            allMatches.add(grandFinals);
            this.root = grandFinals;
        } else {
            this.root = null;
        }
    }

    private void buildSwissSystem(Team[] teams) {
        this.totalRounds = Math.min(5, teams.length / 2);
        this.allMatches.clear();
        List<Team> shuffled = new ArrayList<>(Arrays.asList(teams));
        Collections.shuffle(shuffled);
        for (int round = 1; round <= totalRounds; round++) {
            for (int i = 0; i < shuffled.size(); i += 2) {
                if (i + 1 < shuffled.size()) {
                    Match match = new Match(round);
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

    public List<Match> getMatchesByRound(int round) {
        List<Match> result = new ArrayList<>();
        for (Match match : allMatches) {
            if (match.getRound() == round) {
                result.add(match);
            }
        }
        return result;
    }

    public List<Match> getAllMatches() {
        return allMatches;
    }

    public int getTotalRounds() {
        return totalRounds;
    }

    public Team[] getTeams() {
        return teams;
    }

    public Match getChampionship() {
        return root;
    }

    public ScoreMatrix getScoreMatrix() {
        return scoreMatrix;
    }

    public void recordWinner(Match match, Team winner, int score1, int score2) {
        if (match.getTeam1() == null || match.getTeam2() == null) {
            System.out.println("Error: Match does not have both teams assigned!");
            return;
        }

        match.setWinner(winner, score1, score2);

        Team team1 = match.getTeam1();
        Team team2 = match.getTeam2();
        scoreMatrix.recordMatch(team1.getId(), team2.getId(), score1, score2);

        if (tournamentType == TournamentType.SINGLE_ELIMINATION || tournamentType == TournamentType.DOUBLE_ELIMINATION) {
            propagateWinnerUp(match, winner);
        }

        System.out.println("✓ Recorded:" + match);
    }

    private void propagateWinnerUp(Match currentMatch, Team winner) {
        // Get the loser
        Team loser = null;
        if (currentMatch.getTeam1() == winner) {
            loser = currentMatch.getTeam2();
        } else if (currentMatch.getTeam2() == winner) {
            loser = currentMatch.getTeam1();
        }
        
        // Handle losers bracket - send loser to corresponding losers bracket match
        if (tournamentType == TournamentType.DOUBLE_ELIMINATION && loser != null && currentMatch.getRound() < totalRounds - 1) {
            Match losersMatch = winnerToLoserMatch.get(currentMatch);
            if (losersMatch != null) {
                if (losersMatch.getTeam1() == null) {
                    losersMatch.setTeam1(loser);
                    System.out.println("→ Loser " + loser.getName() + " sent to losers bracket match: " + losersMatch);
                } else if (losersMatch.getTeam2() == null) {
                    losersMatch.setTeam2(loser);
                    System.out.println("→ Loser " + loser.getName() + " sent to losers bracket match: " + losersMatch);
                }
                checkAndCompleteMatch(losersMatch);
            }
        }
        
        // Handle winners bracket propagation
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
            System.out.println("→ Match is ready: " + match);
        }
    }

    public List<Match> getPendingMatches() {
        List<Match> pending = new ArrayList<>();
        for (Match match : allMatches) {
            if (!match.isCompleted() && match.getTeam1() != null && match.getTeam2() != null) {
                pending.add(match);
            }
        }
        return pending;
    }

    public Match getCurrentMatch() {
        List<Match> pending = getPendingMatches();
        if (pending.isEmpty()) return null;
        return pending.get(0);
    }

    public String getProgress() {
        int total = allMatches.size();
        int completed = 0;
        for (Match m : allMatches) {
            if (m.isCompleted()) completed++;
        }
        return completed + "/" + total + " matches completed";
    }

    public Team getTournamentWinner() {
        if (tournamentType == TournamentType.ROUND_ROBIN || 
            tournamentType == TournamentType.SWISS || 
            tournamentType == TournamentType.FREE_FOR_ALL) {
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
        
        if (root != null && root.isCompleted()) {
            return root.getWinner();
        }
        return null;
    }

    public List<Match> getLosersBracketMatches() {
        return losersBracketMatches;
    }

    public Match getGrandFinals() {
        return grandFinals;
    }

    public void printBracket() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("🏆 TOURNAMENT BRACKET - " + tournamentType.getDisplayName() + " 🏆");
        System.out.println("=".repeat(60));
        for (int round = 1; round <= totalRounds; round++) {
            System.out.println("\n📍 ROUND " + round + ":");
            System.out.println("-".repeat(40));
            for (Match match : getMatchesByRound(round)) {
                System.out.println(" " + match);
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
                i+1, t.getName(), t.getWins(), t.getLosses(), t.getPointDifference(), t.getWinPercentage());
        }
        System.out.println("-".repeat(50));
    }
}