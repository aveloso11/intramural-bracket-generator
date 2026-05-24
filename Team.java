public class Team {
    private String name;
    private int id;
    private int wins;
    private int losses;
    private int pointsScored;
    private int pointsAllowed;

    public Team(int id, String name) {
        this.id = id;
        this.name = name;
        this.wins = 0;
        this.losses = 0;
        this.pointsScored = 0;
        this.pointsAllowed = 0;
    }

    public String getName() { return name; }
    public int getId() { return id; }
    public int getWins() { return wins; }
    public int getLosses() { return losses; }
    public int getPointsScored() { return pointsScored; }
    public int getPointsAllowed() { return pointsAllowed; }
    public int getPointDifference() { return pointsScored - pointsAllowed; }

    public void addWin(int pointsFor, int pointssAgainst) {
        this.wins++;
        this.pointsScored += pointsFor;
        this.pointsAllowed += pointssAgainst;
    }
    
    public void addLoss(int pointsFor, int pointsAgainst) {
        this.losses++;
        this.pointsScored += pointsFor;
        this.pointsAllowed += pointsAgainst;
    }

    public double getWinPercentage() {
        int totalGames = wins + losses;
        if (totalGames ==0) return 0.0;
        return (double) wins / totalGames * 100;
    }

    public String toString() {
        return name + "(W:" + wins + ", L:" + losses + ", PD:" + getPointDifference() + ")";
    }
}
