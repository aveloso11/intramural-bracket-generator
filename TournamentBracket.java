import java.util.*;

public class TournamentBracket {
    private Match root; // CHAMPIONSHIP MATCH (ROOT OF BINARY TREE)
    private Team[] teams;
    private ScoreMatrix scoreMatrix;
    private int totalRounds;
    private List<Match> allMatches;

    public TournamentBracket(Team[] teams) {
        this.teams = teams;
        this.scoreMatrix = new ScoreMatrix(teams);
        this.totalRounds = (int) Math.ceil(Math.log(teams.length) / Math.log(2));
        this.allMatches = new ArrayList<Match>();
        this.root = buildBracket(teams);
    }

    // BUILD THE BINARY TREE BRACKET FRROM BOTTUM UP
    private Match buildBracket(Team[] teams) {
        int bracketSize = (int) Math.pow(2, totalRounds);
        List<Match> currentRound = new ArrayList<>();

        // CREATE FIRST ROUND MATCHES (LEAF NODES) 
        for (int i = 0; i < bracketSize; i += 2) {
            Match match = new Match(1);
            if (i < teams.length) {
                match.setTeam1(teams[i]);
            }
            if (i + 1 < teams.length) {
                match.setTeam2(teams[i + 1]);
            }
            currentRound.add(match);
            allMatches.add(match);
        }

        // BUILD SUBSEQUENT ROUNDS (INTERNAL NODES)
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
        
        return currentRound.get(0); // ROOT IS THE CHAMPIONSHIP MATCH
    }
  
    // GET ALLL MATCHES AT A SPECIFIC ROUND
    public List<Match> getMatchesByRound(int round) {
        List<Match> result = new ArrayList<>();
        for (Match match : allMatches) {
            if (match.getRound() == round) {
                result.add(match);
            }
        }
        return result;
    } 

    // GET TOTAL ROUNDS
    public int getTotalRounds() {
        return totalRounds;
    }

    public Team[] getTeams() {
        return teams;
    }

    // GET CHAMPIONSHIP MATCH
    public Match getChampionship() {
        return root;
    }

    // GET SCORE MATRIX
    public ScoreMatrix getScoreMatrix() {
        return scoreMatrix;
    }

    // RECORD MATCH WINNER AND PROPAGATE UP THE TREE
    public void recordWinner(Match match, Team winner, int score1, int score2) {
        
        // VALIDATE TEAMS ARE CORRECT
        if(match.getTeam1() == null || match.getTeam2() == null) {
            System.out.println("Error: Match does not have both teams assigned!");
            return;
        }

        // RECORD WINNER IN MATCH
        match.setWinner(winner, score1, score2);

        // RECORD IN SCORE MATRIX
        Team team1 = match.getTeam1();
        Team team2 = match.getTeam2();
        scoreMatrix.recordMatch(team1.getId(),team2.getId(), score1, score2);

        // PROPAGATE WINNER TO PARENT MATCH
        propagateWinnerUp(match, winner);

        System.out.println("✓ Recorded:" + match);
    }

    // PROPAGATE WINNER UP THE LIBRARY BINARY TREE
    private void propagateWinnerUp(Match currentMatch, Team winner) {

        // FIND PARENT MATCH (SEARCH THROUGH ALL MATCHES)
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

    // CHECK IF BOTH TEAMS ARE ASSIGNED TO A MATCH
    private void checkAndCompleteMatch(Match match) {
        if (match.getTeam1() != null && match.getTeam2() != null && !match.isCompleted()) {
            System.out.println("→ Match is ready: " + match);
        }
    }

    // GET ALL PENDING MATCHES 
    public List<Match> getPendingMatches() {
        List<Match> pending = new ArrayList<>();
        for (Match match : allMatches) {
            if (!match.isCompleted() && match.getTeam1() != null && match.getTeam2() != null) {
                pending.add(match);
            }
        }
        return pending;
    }
    
    // GET THE CURRENT MATCH TO PLAY 
    public Match getCurrentMatch() {
        List<Match> pending = getPendingMatches();
        if (pending.isEmpty()) {
             return null;
        }
        return pending.get(0);
    }

    // GET TOURNAMENT PROGRESS AS STRING
    public String getProgress() {
        int total = allMatches.size();
        int completed = 0;
        for (Match m : allMatches) {
            if (m.isCompleted()) {
                 completed++;
            }
        }
        return completed + "/" + total + " matches completed";
    }

    // DISPLAY FULL BRACKET
    public void printBracket() {
        System.out.println(" \n" + "=".repeat(60));
        System.out.println("🏆 TOURNAMENT BRACKET - " + totalRounds + " Rounds 🏆");
        System.out.println("=".repeat(60));

        for (int round =1; round <= totalRounds; round++) {
            System.out.println("\n📍 ROUND " + round + ":");
            System.out.println("-".repeat(40));
            List<Match> roundMatches = getMatchesByRound(round);
            for (Match match : roundMatches) {
                System.out.println(" " + match);
            }
        }
        System.out.println("\n" + "=".repeat(60));
    }

    // DISPLAY STANDINGS 
    public void printStandings() {
        System.out.println("\n📊 TEAM STANDINGS:");
        System.out.println("-".repeat(50));
        List<Team> sortedTeams = Arrays.asList(teams);
        sortedTeams.sort((a,b) -> Integer.compare(b.getWins(), a.getWins()));

        for (int i = 0; i < sortedTeams.size(); i++) {
            Team t = sortedTeams.get(i);
            System.out.printf("%d. %-12s | Wins: %d | Losses: %d | PD: %d | Win%%: %.1f%%\n",  i+1, t.getName(), t.getWins(), t.getLosses(),  t.getPointDifference(), t.getWinPercentage());
        }
        System.out.println("-".repeat(50));
    }

    // GET TOURNAMENT WINNER
    public Team getTournamentWinner() {
        if (root.isCompleted()) {
            return root.getWinner();
        }
        return null;
    }
        
}
