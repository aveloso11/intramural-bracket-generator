import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import javafx.geometry.Pos;

public class app extends Application {
    
    private TournamentBracket tournament;
    private TournamentType currentType = TournamentType.SINGLE_ELIMINATION;
    private Team[] teams;
    private TextArea outputArea;
    private ComboBox<String> matchSelector;
    private ComboBox<Team> winnerCombo;
    private TextField scoreField1, scoreField2;
    private Label team1Label, team2Label;
    private Label matchInfoLabel;
    private Match currentSelectedMatch = null;
    
    @Override
    public void start(Stage myStage) {
        myStage.setTitle("Intramural Sports Bracket Generator");
        
        // CREATE TEAMS
        teams = new Team[]{
            new Team(0, "Dragons"),
            new Team(1, "Tigers"),
            new Team(2, "Falcons"),
            new Team(3, "Sharks"),
            new Team(4, "Wolves"),
            new Team(5, "Eagles"),
            new Team(6, "Panthers"),
            new Team(7, "Lions")
        };
        
        // INITIALIZE TOURNAMENT
        tournament = new TournamentBracket(teams);
        
        // CREATE OUTPUT AREA
        outputArea = new TextArea();
        outputArea.setEditable(false);
        outputArea.setPrefHeight(400);
        outputArea.setStyle("-fx-font-family: monospace; -fx-font-size: 12px;");
        
        // TOURNAMENT TYPE SELECTOR
        Label typeLabel = new Label("Tournament Format:");
        ComboBox<TournamentType> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll(TournamentType.values());
        typeCombo.setValue(currentType);
        typeCombo.setOnAction(e -> {
            currentType = typeCombo.getValue();
            resetTournament();
        });
        
        // MATCH SELECTOR
        Label selectLabel = new Label("Select Match:");
        matchSelector = new ComboBox<>();
        matchSelector.setPrefWidth(350);
        matchSelector.setOnAction(e -> loadSelectedMatch());
        
        Button refreshButton = new Button("Refresh Match List");
        refreshButton.setOnAction(e -> refreshMatchList());
        
        // SCORE INPUT SELECTION WITH CLEAR TEAM LABELS 
        matchInfoLabel = new Label("Select a match from dropdown");
        matchInfoLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #2c3e50;");
        
        // TEAM 1 PANEL
        team1Label = new Label("Team 1:");
        team1Label.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #2196F3;");
        scoreField1 = new TextField();
        scoreField1.setPrefWidth(80);
        scoreField1.setPromptText("Score");
        
        // VS LABEL
        Label vsLabel = new Label("VS");
        vsLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #f44336;");
        
        // TEAM 2 PANEL
        team2Label = new Label("Team 2:");
        team2Label.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #FF9800;");
        scoreField2 = new TextField();
        scoreField2.setPrefWidth(80);
        scoreField2.setPromptText("Score");
        
        // WINNER SELECTION
        Label winnerLabel = new Label("Winner:");
        winnerLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
        winnerCombo = new ComboBox<>();
        winnerCombo.setPrefWidth(150);
        winnerCombo.setPromptText("Select winner");
        
        Button submitButton = new Button("✓ SUBMIT MATCH RESULT");
        submitButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");
        submitButton.setOnAction(e -> submitMatch());
        
        // TEAM 1 SCORE BOX
        VBox team1Box = new VBox(5, team1Label, scoreField1);
        team1Box.setAlignment(Pos.CENTER);
        team1Box.setPadding(new Insets(10));
        team1Box.setStyle("-fx-border-color: #2196F3; -fx-border-width: 2; -fx-border-radius: 5; -fx-background-color: #e3f2fd; -fx-background-radius: 5;");
        
        // TEAM 2 SCORE BOX
        VBox team2Box = new VBox(5, team2Label, scoreField2);
        team2Box.setAlignment(Pos.CENTER);
        team2Box.setPadding(new Insets(10));
        team2Box.setStyle("-fx-border-color: #FF9800; -fx-border-width: 2; -fx-border-radius: 5; -fx-background-color: #fff3e0; -fx-background-radius: 5;");
        
        // WINNER BOX
        VBox winnerBox = new VBox(5, winnerLabel, winnerCombo);
        winnerBox.setAlignment(Pos.CENTER);
        winnerBox.setPadding(new Insets(10));
        
        HBox scoreBox = new HBox(20, team1Box, vsLabel, team2Box, winnerBox);
        scoreBox.setAlignment(Pos.CENTER);
        
        HBox submitBox = new HBox(20, submitButton);
        submitBox.setAlignment(Pos.CENTER);
        submitBox.setPadding(new Insets(10));
        
        HBox selectBox = new HBox(10, selectLabel, matchSelector, refreshButton);
        selectBox.setAlignment(Pos.CENTER);
        
        VBox inputPanel = new VBox(10, matchInfoLabel, scoreBox, submitBox);
        inputPanel.setPadding(new Insets(15));
        inputPanel.setStyle("-fx-border-color: #ccc; -fx-border-width: 1; -fx-background-color: #fafafa; -fx-border-radius: 5;");
        
        // CONTROL BUTTONS
        Button showBracketBtn = new Button("Show Bracket");
        Button showStandingsBtn = new Button("Show Standings");
        Button showMatrixBtn = new Button("Show Score Matrix");
        Button resetBtn = new Button("Reset Tournament");
        
        showBracketBtn.setOnAction(e -> showBracket());
        showStandingsBtn.setOnAction(e -> showStandings());
        showMatrixBtn.setOnAction(e -> showMatrix());
        resetBtn.setOnAction(e -> resetTournament());
        
        HBox buttonBox = new HBox(10, showBracketBtn, showStandingsBtn, showMatrixBtn, resetBtn);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(10));
        
        HBox typeBox = new HBox(10, typeLabel, typeCombo);
        typeBox.setAlignment(Pos.CENTER_LEFT);
        typeBox.setPadding(new Insets(5));
        
        VBox root = new VBox(10, typeBox, selectBox, inputPanel, buttonBox, outputArea);
        root.setPadding(new Insets(10));
        
        Scene scene = new Scene(root, 1000, 800);
        myStage.setScene(scene);
        myStage.show();
        
        // LOAD INITIAL DATA
        refreshMatchList();
        showBracket();
    }
    
    private void refreshMatchList() {
        matchSelector.getItems().clear();
        
        for (int round = 1; round <= tournament.getTotalRounds(); round++) {
            for (Match match : tournament.getMatchesByRound(round)) {
                if (!match.isCompleted()) {
                    String team1 = match.getTeam1() != null ? match.getTeam1().getName() : "TBD";
                    String team2 = match.getTeam2() != null ? match.getTeam2().getName() : "TBD";
                    String display = "Round " + round + ": " + team1 + " vs " + team2;
                    matchSelector.getItems().add(display);
                }
            }
        }
        
        if (matchSelector.getItems().isEmpty()) {
            matchSelector.getItems().add("No pending matches - Tournament Complete!");
            matchInfoLabel.setText("🏆 TOURNAMENT COMPLETE! 🏆");
        }
    }
    
    private void loadSelectedMatch() {
        String selected = matchSelector.getValue();
        if (selected == null || selected.startsWith("No pending")) {
            return;
        }
        
        // PARSE ROUND FROM SELECTION
        int round = Integer.parseInt(selected.substring(selected.indexOf("Round ") + 6, selected.indexOf(":")));
        String teamsPart = selected.substring(selected.indexOf(":") + 2);
        String[] teamNames = teamsPart.split(" vs ");
        String team1Name = teamNames[0];
        String team2Name = teamNames[1];
        
        // FIND THE MATCH
        for (Match match : tournament.getMatchesByRound(round)) {
            if (!match.isCompleted()) {
                String mTeam1 = match.getTeam1() != null ? match.getTeam1().getName() : "TBD";
                String mTeam2 = match.getTeam2() != null ? match.getTeam2().getName() : "TBD";
                if (mTeam1.equals(team1Name) && mTeam2.equals(team2Name)) {
                    currentSelectedMatch = match;
                    break;
                }
            }
        }
        
        if (currentSelectedMatch != null) {
            String team1 = currentSelectedMatch.getTeam1().getName();
            String team2 = currentSelectedMatch.getTeam2().getName();
            
            matchInfoLabel.setText("📋 CURRENT MATCH: " + team1 + " vs " + team2);
            
            // UPDATE LABELS WITH ACTUAL TEAM NAMES
            team1Label.setText(team1 + ":");
            team2Label.setText(team2 + ":");
            
            // UPDATE WINNER COMBO BOX WITH TEAM NAMES
            winnerCombo.getItems().clear();
            winnerCombo.getItems().add(currentSelectedMatch.getTeam1());
            winnerCombo.getItems().add(currentSelectedMatch.getTeam2());
            
            // SET PROMPT TO SHOW WHICH TEAM IS WHICH Set 
            winnerCombo.setPromptText("Select winner (" + team1 + " or " + team2 + ")");
            
            scoreField1.clear();
            scoreField2.clear();
            scoreField1.setPromptText(team1 + " score");
            scoreField2.setPromptText(team2 + " score");
        }
    }
    
    private void submitMatch() {
        if (currentSelectedMatch == null) {
            outputArea.appendText("\n[ERROR] Please select a match first!\n");
            return;
        }
        
        if (currentSelectedMatch.isCompleted()) {
            outputArea.appendText("\n[ERROR] This match is already completed!\n");
            refreshMatchList();
            return;
        }
        
        // GET SCORES 
        int score1, score2;
        try {
            score1 = Integer.parseInt(scoreField1.getText());
            score2 = Integer.parseInt(scoreField2.getText());
        } catch (NumberFormatException e) {
            outputArea.appendText("\n[ERROR] Please enter valid numbers for scores!\n");
            return;
        }
        
        // GET WINNER
        Team winner = winnerCombo.getValue();
        if (winner == null) {
            outputArea.appendText("\n[ERROR] Please select the winner!\n");
            return;
        }
        
        // VALIDATE WINNER IS ONE OF THE TEAMS 
        if (winner != currentSelectedMatch.getTeam1() && winner != currentSelectedMatch.getTeam2()) {
            outputArea.appendText("\n[ERROR] Winner must be one of the competing teams!\n");
            return;
        }
        
        // RECORD THE RESULT
        tournament.recordWinner(currentSelectedMatch, winner, score1, score2);
        
        String winnerName = winner.getName();
        String loserName = (winner == currentSelectedMatch.getTeam1()) ? 
            currentSelectedMatch.getTeam2().getName() : currentSelectedMatch.getTeam1().getName();
        
        outputArea.appendText("\n✓ " + winnerName + " defeated " + loserName + " (" + score1 + "-" + score2 + ")\n");
        
        // REFRESH EVERYTHING
        refreshMatchList();
        showBracket();
        showStandings();
        
        // CLEAR SELECTION
        currentSelectedMatch = null;
        matchInfoLabel.setText("Select a match from dropdown");
        team1Label.setText("Team 1:");
        team2Label.setText("Team 2:");
        winnerCombo.getItems().clear();
        scoreField1.clear();
        scoreField2.clear();
    }
    
    private void showBracket() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n").append("=".repeat(80)).append("\n");
        sb.append("              TOURNAMENT BRACKET - ").append(currentType.getDisplayName()).append("\n");
        sb.append("=".repeat(80)).append("\n\n");
        
        // LOOP THROUGH EACH ROUND OF THE TORUNAMENT (1 TO TOTALROUNDS)
        for (int round = 1; round <= tournament.getTotalRounds(); round++) {
            sb.append("┌").append("─".repeat(50)).append("┐\n");
            sb.append("│         ROUND ").append(round).append("         │\n");
            sb.append("├").append("─".repeat(50)).append("┤\n");
            
            // GET ALL MATCHES IN THE CURRENT ROUND FORM THE BINARY TREE
            for (Match match : tournament.getMatchesByRound(round)) {

                // GET TEAM NAMES ( OR "TBD" IF NOT ASSIGNED YET)
                String team1 = match.getTeam1() != null ? match.getTeam1().getName() : "TBD";
                String team2 = match.getTeam2() != null ? match.getTeam2().getName() : "TBD";
                
                // DISPLAY DIFFERENT STATUS BASED ON MATCH STATE
                sb.append(String.format("│  %-20s vs %-20s  │\n", team1, team2));
                
                if (match.isCompleted() && match.getWinner() != null) {

                    //MATCH FINISHED = SHOWING WINNER AND SCORE
                    sb.append(String.format("│  WINNER: %-35s  │\n", match.getWinner().getName() + " (" + match.getScore() + ")"));
                } else if (match.getTeam1() != null && match.getTeam2() != null && 
                           !match.getTeam1().getName().equals("TBD") && !match.getTeam2().getName().equals("TBD")) {

                    // BOTH TEAMS ASSIGNED = MATCHED READY TO PLAY
                    sb.append("│  STATUS: READY TO PLAY                                 │\n");
                } else {

                    // WAITING FOR WINNER FROM PREVIOUS ROUND
                    sb.append("│  STATUS: WAITING FOR WINNER                            │\n");
                }
                sb.append("├").append("─".repeat(50)).append("┤\n");
            }
            sb.append("\n");
        }
        
        // SHOW TOURNAMENT PROGRESS (EX: 1/7 MATCHES COMPLETED)
        sb.append("\n").append("PROGRESS: ").append(tournament.getProgress()).append("\n");
        
        // IT DISPLAY IF TOURNAMENT HAS A CHAMPION
        Team champion = tournament.getTournamentWinner();
        if (champion != null) {
            sb.append("\n").append("█".repeat(80)).append("\n");
            sb.append("                    🏆 CHAMPION: ").append(champion.getName()).append(" 🏆\n");
            sb.append("█".repeat(80)).append("\n");
        }
        
        outputArea.setText(sb.toString());
    }
    
    private void showStandings() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n").append("=".repeat(80)).append("\n");
        sb.append("                      TEAM STANDINGS\n");
        sb.append("=".repeat(80)).append("\n\n");

        // CREATE TABLE HEADER
        sb.append(String.format("%-5s %-15s %-8s %-8s %-8s %-10s\n", 
            "Rank", "Team", "Wins", "Losses", "PD", "Win%"));
        sb.append("-".repeat(60)).append("\n");
        
        // CLONE TEAMS ARRAY TO AVOID MODIFYING ORIGINAL
        Team[] sorted = teams.clone();

        // BUBBLE SORT ALGORITHM - SORTS TEAMS BY WINS (HIGHEST TO LOWEST)
        for (int i = 0; i < sorted.length - 1; i++) {
            for (int j = i + 1; j < sorted.length; j++) {
                if (sorted[j].getWins() > sorted[i].getWins()) {
                    
                    // SWAP IF TEAM J HAS MORE WINS THAN TEAM I
                    Team temp = sorted[i];
                    sorted[i] = sorted[j];
                    sorted[j] = temp;
                }
            }
        }
        
        // DISPLAY EACH TEAM WITH THEIR STATUS
        for (int i = 0; i < sorted.length; i++) {
            Team t = sorted[i];

            // ASSIGN MEDALS FOR TOP 3
            String medal = "";
            if (i == 0) medal = "🥇 ";
            else if (i == 1) medal = "🥈 ";
            else if (i == 2) medal = "🥉 ";
            else medal = "   ";
            
            // FORMAT: (MEDAL, RANK, TEAM NAME, WINS, LOSSES, POINT DIFFM, WIN%)
            sb.append(String.format("%s%-5d %-15s %-8d %-8d %-8d %-9.1f%%\n", 
                medal, i+1, t.getName(), t.getWins(), t.getLosses(), 
                t.getPointDifference(), t.getWinPercentage()));
        }
        
        outputArea.appendText(sb.toString());
    }
    
    private void showMatrix() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n").append("=".repeat(80)).append("\n");
        sb.append("                      SCORE MATRIX\n");
        sb.append("=".repeat(80)).append("\n");
        sb.append("(Row Team vs Column Team - shows points scored by Row team)\n\n");
        
        // PRINT HEADER ROW (TEAM NAMES)
        sb.append(String.format("%-12s", ""));
        for (Team t : teams) {

            // LIMITING LONG NAMES TO 8 CHARACTERS
            String name = t.getName().length() > 8 ? t.getName().substring(0, 8) : t.getName();
            sb.append(String.format("%-10s", name));
        }
        sb.append("\n");
        sb.append("-".repeat(12 + 10 * teams.length)).append("\n");
        
        // PRINT EACH ROW OF THE MATRIX
        for (int i = 0; i < teams.length; i++) {

            // PRINT ROW HEADER (TEAM NAME)
            String name = teams[i].getName().length() > 8 ? teams[i].getName().substring(0, 8) : teams[i].getName();
            sb.append(String.format("%-12s", name));

            // PRINT EACH CELL IN THE ROW
            for (int j = 0; j < teams.length; j++) {
                if (i == j) {

                    // DIAGONAL CEELLS (SAME TEAM)
                    sb.append(String.format("%-10s", "---"));
                } else {

                    // GET SCORE FROM THE 2D MATRIX
                    int score = tournament.getScoreMatrix().getScore(i, j);
                    if (score == -1) {

                        // -1 MEANS TEAM HAVEN'T PLAYED YET
                        sb.append(String.format("%-10s", "⚔️"));
                    } else {

                        // SHOW THE ACTUAL SCORE
                        sb.append(String.format("%-10d", score));
                    }
                }
            }
            sb.append("\n");
        }
        
        outputArea.appendText(sb.toString());
    }
    
    // CREATE FRESH NAMES WITH ZERO WINS/LOSSES
    private void resetTournament() {
        teams = new Team[]{
            new Team(0, "Dragons"),
            new Team(1, "Tigers"),
            new Team(2, "Falcons"),
            new Team(3, "Sharks"),
            new Team(4, "Wolves"),
            new Team(5, "Eagles"),
            new Team(6, "Panthers"),
            new Team(7, "Lions")
        };
        
        // CREATE NEW TOURNAMENT BRACKET 
        tournament = new TournamentBracket(teams);
        refreshMatchList();
        showBracket();

        // LOG RESET ACTION
        outputArea.appendText("\n[RESET] Tournament has been reset!\n");
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}