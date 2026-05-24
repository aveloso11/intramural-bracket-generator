public class Match {
    private Match leftChild;
    private Match rightChild;
    private Team team1;
    private Team team2;
    private Team winner;
    private String score;
    private int round;
    private boolean completed;
    private int matchId;
    private static int idCounter = 1;

    public Match(int round) {
        this.round = round;
        this.completed = false;
        this.team1 = null;
        this.team2 = null;
        this.winner = null;
        this.leftChild = null;
        this.rightChild = null;
        this.matchId = idCounter++;
    }

    // GETTERS
    public Match getLeftChild() { return leftChild; }
    public Match getRightChild() { return rightChild; }
    public Team getTeam1() { return team1; }
    public Team getTeam2() { return team2; }
    public Team getWinner() { return winner; }
    public int getRound() { return round; }
    public boolean isCompleted() { return completed; }
    public int getMatchId() { return matchId; }
    public String getScore() { return score; }

    public boolean isReady() {
        return (team1 != null && team2 != null);
    }


    // SETTERS
    public void setLeftChild(Match leftChild) { this.leftChild = leftChild; }
    public void setRightChild(Match rightChild) { this.rightChild = rightChild; }
    public void setTeam1(Team team1) { this.team1 = team1; }
    public void setTeam2(Team team2) { this.team2 = team2; }

    public void setWinner(Team winner, int score1, int score2) {
        this.winner = winner;
        this.score = score1 + "-" + score2;
        this.completed = true;

        // UPDATED TEAM STATS
        Team loser = (team1 == winner) ? team2 : team1;
        if(loser != null) {
            winner.addWin(score1, score2);
            loser.addLoss(score1, score2);
        }
    }

    public String toString() {
        String t1 = (team1 == null) ? "TBD" : team1.getName();
        String t2 = (team2 == null) ? "TBD" : team2.getName();
        if (completed && winner != null) {
            return "R" + round + "M" + matchId + ":" + t1 + "vs" + t2 + "→" + winner.getName() + "(" + score + ")";
        }
        return "R" + round + "M" + matchId + ":" + t1 + "vs" + t2 + " [PENDING]";
    }
}
