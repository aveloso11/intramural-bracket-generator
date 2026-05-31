import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.paint.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;      

public class app extends Application {
    
    private TournamentBracket tournament;
    private Team[] teams;
    private VBox participantsList;
    private VBox bracketView;
    private TextField bracketNameField;
    private ComboBox<String> bracketTypeCombo;
    private TextField sportField;
    private TextArea descriptionArea;
    private Label statusLabel;
    private ProgressBar progressBar;
    private Label progressLabel;
    
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Intramural Sports Bracket Generator");
    
        teams = new Team[0];
        
        Team[] tempTeams = new Team[]{new Team(0, "Loading...")};
        tournament = new TournamentBracket(tempTeams);
        
        VBox leftPanel = createParticipantsPanel();
        VBox centerPanel = createBracketViewPanel();
        VBox rightPanel = createInformationPanel();
        HBox bottomPanel = createBottomProgressPanel();
        
        HBox contentArea = new HBox(10, leftPanel, centerPanel, rightPanel);
        contentArea.setPadding(new Insets(10));
        contentArea.setFillHeight(true);
        HBox.setHgrow(centerPanel, Priority.ALWAYS);
        
        leftPanel.setPrefWidth(250);
        centerPanel.setPrefWidth(500);
        rightPanel.setPrefWidth(280);
      
        BorderPane mainLayout = new BorderPane();
        mainLayout.setCenter(contentArea);
        mainLayout.setBottom(bottomPanel);
        mainLayout.setTop(createHeaderBar());
        mainLayout.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        mainLayout.setStyle("-fx-background-color: linear-gradient(to right, #040D43, #7F8EE3);");
        
        Scene scene = new Scene(mainLayout, 1200, 800);
        scene.setFill(Color.web("#040D43"));
        primaryStage.setScene(scene);
        primaryStage.show();
        
        bracketView.getChildren().clear();
        Label emptyLabel = new Label("No teams added yet.\nClick 'ADD' to add participants.");
        emptyLabel.setFont(Font.font("Arial", 14));
        emptyLabel.setTextFill(Color.web("#E0E6ED"));
        emptyLabel.setAlignment(Pos.CENTER);
        bracketView.getChildren().add(emptyLabel);
        
        updateProgress();
    }
    
    private HBox createHeaderBar() {
        HBox header = new HBox();
        header.setPadding(new Insets(15));
        header.setStyle("-fx-background-color: linear-gradient(to right, #040D43, #7F8EE3);");
        
        Label title = new Label("🏆 INTRAMURAL BRACKET MAKER");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        title.setTextFill(Color.web("#FFD862"));
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        header.getChildren().addAll(title, spacer);
        header.setAlignment(Pos.CENTER_LEFT);
        
        return header;
    }

    private void updateParticipantsList() {
        participantsList.getChildren().clear();
        
        if (teams.length == 0) {
            Label emptyLabel = new Label("There are no participants yet.\nClick 'ADD' to add participants.");
            emptyLabel.setStyle("-fx-text-fill: #E0E6ED; -fx-font-size: 12px;");
            participantsList.getChildren().add(emptyLabel);
            return;
        }
        
        for (Team team : teams) {
            CheckBox cb = new CheckBox(team.getName());
            cb.setUserData(team);
            cb.setStyle("-fx-font-size: 12px; -fx-padding: 5;");
            participantsList.getChildren().add(cb);
        }
    }
    
    private VBox createParticipantsPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(15));
        panel.setStyle("-fx-background-color: #040D43; -fx-border-color: #7F8EE3; -fx-border-radius: 5; -fx-background-radius: 5;");
        
        Label title = new Label("PARTICIPANTS");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        title.setTextFill(Color.web("#FFD862"));
        
        participantsList = new VBox(5);
        participantsList.setPadding(new Insets(5));
        
        updateParticipantsList();
        
        ScrollPane scrollPane = new ScrollPane(participantsList);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(400);
        scrollPane.setStyle("-fx-background: #152055; -fx-border-color: #7F8EE3;");
        
        Button addTeamBtn = createStyledButton("ADD", "#7F8EE3");
        Button removeTeamBtn = createStyledButton("REMOVE", "#e74c3c");

        String addNormal  = "-fx-background-color: #7F8EE3; -fx-text-fill: white; -fx-padding: 8 16; -fx-background-radius: 3; -fx-border-radius: 3; -fx-border-color: transparent; -fx-font-weight: bold;";
        String addHover   = "-fx-background-color: #5a6abf; -fx-text-fill: white; -fx-padding: 8 16; -fx-background-radius: 3; -fx-border-radius: 3; -fx-border-color: transparent; -fx-font-weight: bold;";

        String remNormal  = "-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-padding: 8 16; -fx-background-radius: 3; -fx-border-radius: 3; -fx-border-color: transparent; -fx-font-weight: bold;";
        String remHover   = "-fx-background-color: #c0392b; -fx-text-fill: white; -fx-padding: 8 16; -fx-background-radius: 3; -fx-border-radius: 3; -fx-border-color: transparent; -fx-font-weight: bold;";

        addTeamBtn.setStyle(addNormal);
        removeTeamBtn.setStyle(remNormal);
        addTeamBtn.setOnMouseEntered(e -> addTeamBtn.setStyle(addHover));
        addTeamBtn.setOnMouseExited(e -> addTeamBtn.setStyle(addNormal));
        removeTeamBtn.setOnMouseEntered(e -> removeTeamBtn.setStyle(remHover));
        removeTeamBtn.setOnMouseExited(e -> removeTeamBtn.setStyle(remNormal));
        addTeamBtn.setOnAction(e -> addTeam());
        removeTeamBtn.setOnAction(e -> removeSelectedTeams());

        HBox buttonBox = new HBox(10, addTeamBtn, removeTeamBtn);
        buttonBox.setAlignment(Pos.CENTER);
        
        Label tip = new Label("Quick Tip: Add participants by checking the box above");
        tip.setFont(Font.font("Arial", 10));
        tip.setTextFill(Color.web("#E0E6ED"));
        tip.setWrapText(true);
        
        panel.getChildren().addAll(title, scrollPane, buttonBox, tip);
        return panel;
    }

    private VBox createBracketViewPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(15));
        panel.setStyle("-fx-background-color: #040D43; -fx-border-color: #7F8EE3; -fx-border-radius: 5; -fx-background-radius: 5;");
        
        Label title = new Label("BRACKET VIEW");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        title.setTextFill(Color.web("#FFD862"));
        
        bracketView = new VBox(10);
        bracketView.setPadding(new Insets(10));
        bracketView.setStyle("-fx-background-color: #152055; -fx-border-color: #7F8EE3; -fx-border-width: 2; -fx-border-radius: 5; -fx-background-radius: 5;");
        
        panel.getChildren().addAll(title, bracketView);
        VBox.setVgrow(bracketView, Priority.ALWAYS);
        
        return panel;
    }
    
    private void addTeam() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add Team");
        dialog.setHeaderText("Enter team name:");
        dialog.setContentText("Team name:");
        
        dialog.showAndWait().ifPresent(name -> {
            if (!name.trim().isEmpty()) {
                Team[] newTeams = new Team[teams.length + 1];
                System.arraycopy(teams, 0, newTeams, 0, teams.length);
                newTeams[teams.length] = new Team(teams.length, name.trim());
                teams = newTeams;
                
                System.out.println("Total teams now: " + teams.length);
                
                if (teams.length >= 2) {
                    tournament = new TournamentBracket(teams, bracketTypeCombo.getValue());
                    updateBracketView();
                    System.out.println("Tournament recreated with " + teams.length + " teams");
                } else {
                    bracketView.getChildren().clear();
                    Label msgLabel = new Label("Add at least 2 teams to start the tournament.\nCurrent teams: " + teams.length);
                    msgLabel.setFont(Font.font("Arial", 14));
                    msgLabel.setTextFill(Color.web("#FFFFFF"));
                    msgLabel.setAlignment(Pos.CENTER);
                    bracketView.getChildren().add(msgLabel);
                }
                
                updateParticipantsList();
                updateProgress();
            }
        });
    }
    
    private void removeSelectedTeams() {
        List<Team> remainingTeams = new ArrayList<>();
        List<Team> teamsToRemove = new ArrayList<>();
        
        for (javafx.scene.Node node : participantsList.getChildren()) {
            if (node instanceof CheckBox) {
                CheckBox cb = (CheckBox) node;
                if (cb.isSelected() && cb.getUserData() instanceof Team) {
                    Team selectedTeam = (Team) cb.getUserData();
                    teamsToRemove.add(selectedTeam);
                }
            }
        }
        
        if (teamsToRemove.isEmpty()) {
            showAlert("No Selection", "Please check the box next to the team(s) you want to remove.");
            return;
        }
        
        for (Team team : teams) {
            boolean shouldRemove = false;
            for (Team removeTeam : teamsToRemove) {
                if (team.getId() == removeTeam.getId()) {
                    shouldRemove = true;
                    break;
                }
            }
            if (!shouldRemove) {
                remainingTeams.add(team);
            }
        }
        
        teams = remainingTeams.toArray(new Team[0]);
        participantsList.getChildren().clear();
        
        for (Team team : teams) {
            CheckBox cb = new CheckBox(team.getName());
            cb.setUserData(team);
            cb.setStyle("-fx-font-size: 12px; -fx-padding: 5;");
            participantsList.getChildren().add(cb);
        }
        
        updateProgress();
        showAlert("Teams Removed", teamsToRemove.size() + " team(s) have been removed.\nRemaining teams: " + teams.length);
    }
    
    private VBox createInformationPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(15));
        panel.setStyle("-fx-background-color: #040D43; -fx-border-color: #7F8EE3; -fx-border-radius: 5; -fx-background-radius: 5;");
        
        Label title = new Label("BRACKET INFORMATION");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        title.setTextFill(Color.web("#FFD862"));
        
        Label nameLabel = new Label("Bracket Name:");
        nameLabel.setTextFill(Color.web("#FFFFFF"));
        nameLabel.setStyle("fx-font-weight: bold; -fx-font-size: 12px;");
        bracketNameField = new TextField("");
        bracketNameField.setPromptText("Enter bracket name...");
        bracketNameField.setStyle("-fx-background-color: #152055;-fx-text-fill: #E0E6ED; -fx-border-color: #7F8EE3; -fx-border-radius: 3;");
        
        Label typeLabel = new Label("Bracket Type:");
        typeLabel.setTextFill(Color.web("#FFFFFF"));
        typeLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");

        bracketTypeCombo = new ComboBox<>();
        bracketTypeCombo.getItems().addAll("Single Elimination", "Double Elimination", "Round Robin", "Swiss System", "Free For All");
        bracketTypeCombo.setValue("Single Elimination");
        bracketTypeCombo.setStyle("-fx-background-color: #152055; -fx-border-color: #7F8EE3; -fx-border-radius: 3;");
        bracketTypeCombo.setCellFactory(listView -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle(null);
                } else {
                    setText(item);
                    setStyle("-fx-text-fill: #E0E6ED; -fx-background-color: #152055;");
                }
            }
        });

        bracketTypeCombo.setButtonCell(new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle(null);
                } else {
                    setText(item);
                    setStyle("-fx-text-fill: #E0E6ED;");
                }
            }
        });

        bracketTypeCombo.setOnAction(e -> {
            String selected = bracketTypeCombo.getValue();
            System.out.println("Bracket type changed to: " + selected);
            if (teams.length >= 2) {
                tournament = new TournamentBracket(teams, selected);
                updateBracketView();
                updateProgress();
            }
        });
        
        Label sportLabel = new Label("Sport/Game:");
        sportLabel.setTextFill(Color.web("#FFFFFF"));
        sportLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
        sportField = new TextField("");
        sportField.setPromptText("e.g. Basketball, Valorant...");
        sportField.setStyle("-fx-background-color: #152055;-fx-text-fill: #E0E6ED; -fx-border-color: #7F8EE3; -fx-border-radius: 3;");
        
        Label descLabel = new Label("Bracket Description:");
        descLabel.setTextFill(Color.web("#FFFFFF"));
        descLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
        descriptionArea = new TextArea();
        descriptionArea.setPromptText("Enter tournament description...");
        descriptionArea.setPrefHeight(80);
        descriptionArea.setStyle(
            "-fx-control-inner-background: #152055; " +
            "-fx-background-color: transparent; " + 
            "-fx-border-color: #7F8EE3; " +
            "-fx-border-radius: 3; " +
            "-fx-prompt-text-fill: #e0e6edc2; " +
            "-fx-text-fill: #FFFFFF;" 
        );
        
        Label statusTitle = new Label("BRACKET STATUS:");
        statusTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #FFD862;");
        statusLabel = new Label("PENDING");
        statusLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #e74c3c;");
        
        Separator separator = new Separator();
        
        Button saveBracketBtn = createStyledButton("SAVE BRACKET", "#7F8EE3");
        saveBracketBtn.setPrefWidth(Double.MAX_VALUE);
        saveBracketBtn.setOnAction(e -> saveBracket());

        Button loadBtn = createStyledButton("LOAD BRACKET", "#7F8EE3");
        loadBtn.setPrefWidth(Double.MAX_VALUE);
        loadBtn.setOnAction(e -> loadBracket());

        Button exitBtn = createStyledButton("EXIT", "#e74c3c");
        exitBtn.setPrefWidth(Double.MAX_VALUE);
        exitBtn.setOnAction(e -> System.exit(0));

        String blueNormal = "-fx-background-color: #7F8EE3; -fx-text-fill: white; -fx-padding: 8 16; -fx-background-radius: 3; -fx-border-radius: 3; -fx-border-color: transparent; -fx-font-weight: bold;";
        String blueHover  = "-fx-background-color: #5a6abf; -fx-text-fill: white; -fx-padding: 8 16; -fx-background-radius: 3; -fx-border-radius: 3; -fx-border-color: transparent; -fx-font-weight: bold;";
        String redNormal  = "-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-padding: 8 16; -fx-background-radius: 3; -fx-border-radius: 3; -fx-border-color: transparent; -fx-font-weight: bold;";
        String redHover   = "-fx-background-color: #c0392b; -fx-text-fill: white; -fx-padding: 8 16; -fx-background-radius: 3; -fx-border-radius: 3; -fx-border-color: transparent; -fx-font-weight: bold;";

        saveBracketBtn.setStyle(blueNormal);
        loadBtn.setStyle(blueNormal);
        exitBtn.setStyle(redNormal);
        saveBracketBtn.setOnMouseEntered(e -> saveBracketBtn.setStyle(blueHover));
        saveBracketBtn.setOnMouseExited(e -> saveBracketBtn.setStyle(blueNormal));

        loadBtn.setOnMouseEntered(e -> loadBtn.setStyle(blueHover));
        loadBtn.setOnMouseExited(e -> loadBtn.setStyle(blueNormal));

        exitBtn.setOnMouseEntered(e -> exitBtn.setStyle(redHover));
        exitBtn.setOnMouseExited(e -> exitBtn.setStyle(redNormal));

        Label tip = new Label("To save your bracket, click the 'Save Bracket' button. You will then be able to create an account where you can manage your bracket and start the tournament.");
        tip.setFont(Font.font("Arial", 10));
        tip.setTextFill(Color.web("#7f8c8d"));
        tip.setWrapText(true);
        
        panel.getChildren().addAll(
            title, nameLabel, bracketNameField,
            typeLabel, bracketTypeCombo,
            sportLabel, sportField,
            descLabel, descriptionArea,
            statusTitle, statusLabel,
            separator, loadBtn, saveBracketBtn, exitBtn, tip
        );
        
        return panel;
    }
    
    private HBox createBottomProgressPanel() {
        HBox panel = new HBox(10);
        panel.setPadding(new Insets(10, 15, 10, 15));
        panel.setStyle("-fx-background-color: linear-gradient(to right, #040D43, #7F8EE3); -fx-border-width: 1 0 0 0;");
        panel.setAlignment(Pos.CENTER_LEFT);
        
        Label progressTitle = new Label("Progress:");
        progressTitle.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        progressTitle.setTextFill(Color.web("#FFFFFF"));
        progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(300);
        progressLabel = new Label("0% Complete");
        progressLabel.setTextFill(Color.web("#FFFFFF"));
        progressLabel.setFont(Font.font("Arial", 11));
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        panel.getChildren().addAll(progressTitle, progressBar, progressLabel, spacer);
        return panel;
    }
    
    private void updateBracketView() {
        bracketView.getChildren().clear();
        
        if (teams.length < 2) {
            Label msgLabel = new Label("Add at least 2 teams to start the tournament.\nClick 'ADD' to add participants.");
            msgLabel.setFont(Font.font("Arial", 14));
            msgLabel.setTextFill(Color.web("#7f8c8d"));
            msgLabel.setAlignment(Pos.CENTER);
            bracketView.getChildren().add(msgLabel);
            return;
        }
        
        String bracketType = bracketTypeCombo.getValue();
        System.out.println("Updating bracket view for: " + bracketType);
        
        if (bracketType.equals("Single Elimination")) {
            displaySingleElimination();
        } else if (bracketType.equals("Double Elimination")) {
            displayDoubleElimination();
        } else if (bracketType.equals("Round Robin")) {
            displayRoundRobin();
        } else if (bracketType.equals("Swiss System")) {
            displaySwissSystem();
        } else if (bracketType.equals("Free For All")) {
            displayFreeForAll();
        } else {
            displaySingleElimination();
        }
    }

    // ========== SINGLE ELIMINATION ==========

    private void displaySingleElimination() {
        int totalRounds = tournament.getTotalRounds();
        int totalSlots = (int) Math.pow(2, totalRounds);
        double rowHeight = 85;
        double colWidth = 220;
        double totalHeight = totalSlots * rowHeight + 100;
        double totalWidth = totalRounds * colWidth + 200;
        
        Pane bracketPane = new Pane();
        bracketPane.setPrefSize(totalWidth, totalHeight);
        bracketPane.setStyle("-fx-background-color: #f5f5f5;");
        
        Canvas canvas = new Canvas(totalWidth, totalHeight);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setStroke(Color.web("#3498db"));
        gc.setLineWidth(2);
        
        for (int round = 1; round < totalRounds; round++) {
            List<Match> currentMatches = tournament.getMatchesByRound(round);
            List<Match> nextMatches = tournament.getMatchesByRound(round + 1);
            
            if (currentMatches.isEmpty() || nextMatches.isEmpty()) continue;
            
            int spacing = totalSlots / currentMatches.size();
            int nextSpacing = totalSlots / nextMatches.size();
            
            for (int i = 0; i < currentMatches.size(); i += 2) {
                if (i + 1 >= currentMatches.size()) break;
                
                int row1 = (i * spacing) + (spacing / 2);
                int row2 = ((i + 1) * spacing) + (spacing / 2);
                int nextRow = (i / 2) * nextSpacing + (nextSpacing / 2);
                
                double x1 = (round - 1) * colWidth + 150;
                double x2 = round * colWidth;
                double centerX = x1 + 60;
                
                double y1 = row1 * rowHeight + 40;
                double y2 = row2 * rowHeight + 40;
                double targetY = nextRow * rowHeight + 40;
                
                gc.strokeLine(x1, y1, centerX, y1);
                gc.strokeLine(x1, y2, centerX, y2);
                gc.strokeLine(centerX, y1, centerX, targetY);
                gc.strokeLine(centerX, y2, centerX, targetY);
                gc.strokeLine(centerX, targetY, x2, targetY);
            }
        }
        
        bracketPane.getChildren().add(canvas);
        
        for (int round = 1; round <= totalRounds; round++) {
            List<Match> matches = tournament.getMatchesByRound(round);
            if (matches.isEmpty()) continue;
            
            int spacing = totalSlots / matches.size();
            if (spacing < 1) spacing = 1;
            
            for (int i = 0; i < matches.size(); i++) {
                Match match = matches.get(i);
                int rowIndex = (i * spacing) + (spacing / 2);
                double x = (round - 1) * colWidth + 10;
                double y = rowIndex * rowHeight + 10;
                
                VBox matchCard = createCompactMatchCard(match, round, i + 1, 12);
                matchCard.setLayoutX(x);
                matchCard.setLayoutY(y);
                bracketPane.getChildren().add(matchCard);
            }
        }
        
        ScrollPane scrollPane = new ScrollPane(bracketPane);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(false);
        scrollPane.setPrefHeight(550);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setStyle("-fx-background: #f5f5f5; -fx-border-color: #ddd;");
        
        bracketView.getChildren().clear();
        bracketView.getChildren().add(scrollPane);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        
        Team champion = tournament.getTournamentWinner();
        if (champion != null) {
            addChampionDisplay(champion);
        }
    }

    // ========== DOUBLE ELIMINATION (Valorant-style) ==========

    /**
     * Renders a Valorant-style double elimination bracket:
     *  - Winners bracket on top, flowing left → right with connector lines
     *  - Losers bracket below, flowing left → right with connector lines
     *  - Grand Finals column on the far right, centered between both brackets
     * All match cards are absolutely positioned on a single Pane so lines
     * can be drawn behind them on a Canvas overlay.
     */
    private void displayDoubleElimination() {
        // --- Layout constants ---
        final double CARD_W      = 160;
        final double CARD_H      = 70;
        final double COL_GAP     = 60;   // horizontal gap between round columns
        final double MATCH_V_GAP = 20;   // minimum vertical gap between cards in same round

        // --- Collect rounds ---
        int totalRounds   = tournament.getTotalRounds();
        int winnersRounds = Math.max(1, totalRounds / 2);

        // Winners bracket matches per round
        Map<Integer, List<Match>> winnersByRound = new HashMap<>();
        for (int r = 1; r <= winnersRounds; r++) {
            List<Match> ms = tournament.getWinnersMatchesByRound(r);
            if (!ms.isEmpty()) winnersByRound.put(r, ms);
        }

        // Losers bracket matches by round
        List<Match> losersAll = tournament.getLosersBracketMatches();
        Map<Integer, List<Match>> losersByRound = new HashMap<>();
        int maxLosersRound = 0;
        for (Match m : losersAll) {
            losersByRound.computeIfAbsent(m.getRound(), k -> new ArrayList<>()).add(m);
            if (m.getRound() > maxLosersRound) maxLosersRound = m.getRound();
        }

        // Grand final
        Match grandFinal = tournament.getGrandFinals();

        // --- Column X positions ---
        // Winners columns: rounds 1..winnersRounds
        // Losers columns:  rounds 2..maxLosersRound  (same x-grid, offset by one column)
        // Grand Final column: after the last of either bracket

        int wCols = winnersByRound.isEmpty() ? 0 : winnersRounds;
        int lCols = losersByRound.isEmpty()  ? 0 : (maxLosersRound - 1); // rounds 2..max = (max-1) cols
        int totalCols = Math.max(wCols, lCols) + 1; // +1 for grand final

        double colStride = CARD_W + COL_GAP;
        double[] colX = new double[totalCols + 1];
        for (int c = 0; c <= totalCols; c++) colX[c] = 20 + c * colStride;

        // --- Compute Y positions for each match using recursive centering ---
        // For the Winners bracket: round 1 matches are evenly spaced; each subsequent
        // round's match is vertically centered between its two feeder matches.
        // For the Losers bracket: same approach independently.

        Map<Match, Double> matchY = new HashMap<>();

        // Winners bracket Y layout
        // Round 1: evenly space all matches
        if (!winnersByRound.isEmpty()) {
            List<Match> r1 = winnersByRound.get(1);
            if (r1 != null) {
                double slotH = CARD_H + MATCH_V_GAP;
                for (int i = 0; i < r1.size(); i++) {
                    matchY.put(r1.get(i), i * slotH);
                }
                // Subsequent rounds: center between pairs from previous round
                for (int r = 2; r <= winnersRounds; r++) {
                    List<Match> prev = winnersByRound.get(r - 1);
                    List<Match> curr = winnersByRound.get(r);
                    if (prev == null || curr == null) continue;
                    for (int i = 0; i < curr.size(); i++) {
                        int idx1 = i * 2;
                        int idx2 = i * 2 + 1;
                        if (idx2 < prev.size()) {
                            double y1 = matchY.get(prev.get(idx1));
                            double y2 = matchY.get(prev.get(idx2));
                            matchY.put(curr.get(i), (y1 + y2) / 2.0);
                        } else if (idx1 < prev.size()) {
                            matchY.put(curr.get(i), matchY.get(prev.get(idx1)));
                        }
                    }
                }
            }
        }

        // Losers bracket Y layout — starts below winners bracket
        // Determine winners bracket total height for offset
        double winnersHeight = 0;
        for (double y : matchY.values()) winnersHeight = Math.max(winnersHeight, y);
        double lossersOffsetY = winnersHeight + CARD_H + 60; // gap between brackets

        if (!losersByRound.isEmpty()) {
            // Find first losers round
            int firstLR = Integer.MAX_VALUE;
            for (int r : losersByRound.keySet()) if (r < firstLR) firstLR = r;

            List<Match> lr1 = losersByRound.get(firstLR);
            if (lr1 != null) {
                double slotH = CARD_H + MATCH_V_GAP;
                for (int i = 0; i < lr1.size(); i++) {
                    matchY.put(lr1.get(i), lossersOffsetY + i * slotH);
                }
                // Subsequent losers rounds
                List<Integer> lRounds = new ArrayList<>(losersByRound.keySet());
                java.util.Collections.sort(lRounds);
                for (int ri = 1; ri < lRounds.size(); ri++) {
                    int prevR = lRounds.get(ri - 1);
                    int currR = lRounds.get(ri);
                    List<Match> prev = losersByRound.get(prevR);
                    List<Match> curr = losersByRound.get(currR);
                    if (prev == null || curr == null) continue;

                    if (curr.size() == prev.size()) {
                        // Same number: keep same Y positions
                        for (int i = 0; i < curr.size(); i++) {
                            if (i < prev.size()) {
                                matchY.put(curr.get(i), matchY.get(prev.get(i)));
                            }
                        }
                    } else {
                        // Half: center between pairs
                        for (int i = 0; i < curr.size(); i++) {
                            int idx1 = i * 2;
                            int idx2 = i * 2 + 1;
                            if (idx2 < prev.size()) {
                                double y1 = matchY.get(prev.get(idx1));
                                double y2 = matchY.get(prev.get(idx2));
                                matchY.put(curr.get(i), (y1 + y2) / 2.0);
                            } else if (idx1 < prev.size()) {
                                matchY.put(curr.get(i), matchY.get(prev.get(idx1)));
                            }
                        }
                    }
                }
            }
        }

        // Grand final Y: center between last winners match and last losers match
        double gfY = lossersOffsetY / 2.0; // default: midpoint
        if (!winnersByRound.isEmpty() && !losersByRound.isEmpty()) {
            // last winners match Y
            List<Match> lastW = winnersByRound.get(winnersRounds);
            if (lastW == null) {
                // walk back
                for (int r = winnersRounds; r >= 1; r--) {
                    if (winnersByRound.containsKey(r)) { lastW = winnersByRound.get(r); break; }
                }
            }
            // last losers match Y
            List<Match> lastL = losersByRound.isEmpty() ? null :
                losersByRound.get(maxLosersRound);

            double wY = (lastW != null && !lastW.isEmpty() && matchY.containsKey(lastW.get(0)))
                        ? matchY.get(lastW.get(0)) : 0;
            double lY = (lastL != null && !lastL.isEmpty() && matchY.containsKey(lastL.get(0)))
                        ? matchY.get(lastL.get(0)) : lossersOffsetY;
            gfY = (wY + lY) / 2.0;
        }

        // --- Map rounds to column indices ---
        // Winners: round r → column (r-1)
        // Losers:  round r → column (r-2) but capped to not overlap
        //   e.g. losers round 2 → col 1, round 3 → col 2 …
        // This mirrors the Valorant layout where LR1 is under WR1, LR2 under WR2, etc.

        Map<Match, Integer> matchCol = new HashMap<>();
        for (Map.Entry<Integer, List<Match>> e : winnersByRound.entrySet()) {
            int col = e.getKey() - 1; // 0-based
            for (Match m : e.getValue()) matchCol.put(m, col);
        }
        for (Map.Entry<Integer, List<Match>> e : losersByRound.entrySet()) {
            int col = e.getKey() - 2; // losers round 2 → col 0, etc.
            if (col < 0) col = 0;
            for (Match m : e.getValue()) matchCol.put(m, col);
        }
        // Grand final gets last column
        int gfCol = totalCols - 1;

        // --- Compute canvas size ---
        double maxX = colX[gfCol] + CARD_W + 40;
        double maxY = 0;
        for (double y : matchY.values()) maxY = Math.max(maxY, y);
        maxY = Math.max(maxY, gfY) + CARD_H + 60;

        // --- Build canvas + pane ---
        Pane bracketPane = new Pane();
        bracketPane.setPrefSize(maxX, maxY);
        bracketPane.setStyle("-fx-background-color: #f8f8f8;");

        Canvas canvas = new Canvas(maxX, maxY);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setLineWidth(2);

        // Helper: right-center of a card
        // card right edge X = colX[col] + CARD_W;  card center Y = matchY + CARD_H/2
        // Helper: left-center of a card
        // card left edge X = colX[col];             card center Y = matchY + CARD_H/2

        // --- Draw Winners bracket connector lines ---
        gc.setStroke(Color.web("#27ae60"));
        for (int r = 1; r < winnersRounds; r++) {
            List<Match> currMs = winnersByRound.get(r);
            List<Match> nextMs = winnersByRound.get(r + 1);
            if (currMs == null || nextMs == null) continue;
            int currCol = r - 1;
            int nextCol = r;
            for (int i = 0; i < nextMs.size(); i++) {
                Match next = nextMs.get(i);
                int src1 = i * 2;
                int src2 = i * 2 + 1;
                if (src1 >= currMs.size()) continue;
                Match m1 = currMs.get(src1);
                double x1 = colX[currCol] + CARD_W;
                double y1 = matchY.get(m1) + CARD_H / 2.0;
                double targetY = matchY.get(next) + CARD_H / 2.0;
                double targetX = colX[nextCol];
                double midX = x1 + COL_GAP / 2.0;
                // elbow line from m1
                gc.strokeLine(x1, y1, midX, y1);
                gc.strokeLine(midX, y1, midX, targetY);
                gc.strokeLine(midX, targetY, targetX, targetY);

                if (src2 < currMs.size()) {
                    Match m2 = currMs.get(src2);
                    double y2 = matchY.get(m2) + CARD_H / 2.0;
                    gc.strokeLine(x1, y2, midX, y2);
                    gc.strokeLine(midX, y2, midX, targetY);
                }
            }
        }

        // --- Draw Losers bracket connector lines ---
        gc.setStroke(Color.web("#e74c3c"));
        List<Integer> lRounds = new ArrayList<>(losersByRound.keySet());
        java.util.Collections.sort(lRounds);
        for (int ri = 0; ri < lRounds.size() - 1; ri++) {
            int rCurr = lRounds.get(ri);
            int rNext = lRounds.get(ri + 1);
            List<Match> currMs = losersByRound.get(rCurr);
            List<Match> nextMs = losersByRound.get(rNext);
            if (currMs == null || nextMs == null) continue;
            int currCol = Math.max(0, rCurr - 2);
            int nextCol = Math.max(0, rNext - 2);

            if (nextMs.size() < currMs.size()) {
                // merging: pairs → one
                for (int i = 0; i < nextMs.size(); i++) {
                    Match next = nextMs.get(i);
                    int src1 = i * 2, src2 = i * 2 + 1;
                    double targetY = matchY.get(next) + CARD_H / 2.0;
                    double targetX = colX[nextCol];
                    double midX    = colX[currCol] + CARD_W + COL_GAP / 2.0;
                    if (src1 < currMs.size()) {
                        double y1 = matchY.get(currMs.get(src1)) + CARD_H / 2.0;
                        gc.strokeLine(colX[currCol] + CARD_W, y1, midX, y1);
                        gc.strokeLine(midX, y1, midX, targetY);
                        gc.strokeLine(midX, targetY, targetX, targetY);
                    }
                    if (src2 < currMs.size()) {
                        double y2 = matchY.get(currMs.get(src2)) + CARD_H / 2.0;
                        gc.strokeLine(colX[currCol] + CARD_W, y2, midX, y2);
                        gc.strokeLine(midX, y2, midX, targetY);
                    }
                }
            } else {
                // same count: straight across (loser feeds into same-index next match)
                for (int i = 0; i < nextMs.size() && i < currMs.size(); i++) {
                    double y1 = matchY.get(currMs.get(i)) + CARD_H / 2.0;
                    double y2 = matchY.get(nextMs.get(i)) + CARD_H / 2.0;
                    double x1 = colX[currCol] + CARD_W;
                    double x2 = colX[nextCol];
                    double midX = x1 + COL_GAP / 2.0;
                    gc.strokeLine(x1, y1, midX, y1);
                    gc.strokeLine(midX, y1, midX, y2);
                    gc.strokeLine(midX, y2, x2, y2);
                }
            }
        }

        // --- Draw lines from last Winners/Losers match into Grand Final ---
        gc.setStroke(Color.web("#f39c12"));
        gc.setLineWidth(2.5);
        double gfLeftX = colX[gfCol];
        double gfCenterY = gfY + CARD_H / 2.0;

        // from last winners
        if (!winnersByRound.isEmpty()) {
            List<Match> lastW = winnersByRound.get(winnersRounds);
            if (lastW == null) for (int r = winnersRounds; r >= 1; r--)
                if (winnersByRound.containsKey(r)) { lastW = winnersByRound.get(r); break; }
            if (lastW != null && !lastW.isEmpty() && matchY.containsKey(lastW.get(0))) {
                int wCol = matchCol.get(lastW.get(0));
                double wx = colX[wCol] + CARD_W;
                double wy = matchY.get(lastW.get(0)) + CARD_H / 2.0;
                double midX = wx + COL_GAP / 2.0;
                gc.strokeLine(wx, wy, midX, wy);
                gc.strokeLine(midX, wy, midX, gfCenterY);
                gc.strokeLine(midX, gfCenterY, gfLeftX, gfCenterY);
            }
        }
        // from last losers
        if (!losersByRound.isEmpty()) {
            List<Match> lastL = losersByRound.get(maxLosersRound);
            if (lastL != null && !lastL.isEmpty() && matchY.containsKey(lastL.get(0))) {
                int lCol = matchCol.get(lastL.get(0));
                double lx = colX[lCol] + CARD_W;
                double ly = matchY.get(lastL.get(0)) + CARD_H / 2.0;
                double midX = lx + COL_GAP / 2.0;
                gc.strokeLine(lx, ly, midX, ly);
                gc.strokeLine(midX, ly, midX, gfCenterY);
                gc.strokeLine(midX, gfCenterY, gfLeftX, gfCenterY);
            }
        }

        bracketPane.getChildren().add(canvas);

        // --- Section labels ---
        // "WINNERS BRACKET" above first winners match
        Label wLabel = new Label("🏆 WINNERS BRACKET");
        wLabel.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        wLabel.setTextFill(Color.web("#27ae60"));
        wLabel.setLayoutX(colX[0]);
        wLabel.setLayoutY(0);
        bracketPane.getChildren().add(wLabel);

        double losserLabelY = lossersOffsetY - 24;
        Label lLabel = new Label("💀 LOSERS BRACKET");
        lLabel.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        lLabel.setTextFill(Color.web("#e74c3c"));
        lLabel.setLayoutX(colX[0]);
        lLabel.setLayoutY(losserLabelY);
        bracketPane.getChildren().add(lLabel);

        // --- Round labels ---
        // Winners
        for (Map.Entry<Integer, List<Match>> e : winnersByRound.entrySet()) {
            int r = e.getKey();
            int col = r - 1;
            String rName = getWinnersRoundName(r, winnersRounds);
            Label rl = new Label(rName);
            rl.setFont(Font.font("Arial", FontWeight.BOLD, 11));
            rl.setTextFill(Color.web("#2c3e50"));
            rl.setLayoutX(colX[col]);
            rl.setLayoutY(18);
            bracketPane.getChildren().add(rl);
        }
        // Losers
        for (int r : lRounds) {
            int col = Math.max(0, r - 2);
            String rName = "LR" + (r - 1);
            Label rl = new Label(rName);
            rl.setFont(Font.font("Arial", FontWeight.BOLD, 11));
            rl.setTextFill(Color.web("#c0392b"));
            rl.setLayoutX(colX[col]);
            rl.setLayoutY(losserLabelY + 16);
            bracketPane.getChildren().add(rl);
        }
        // Grand Final label
        Label gfLabel = new Label("GRAND FINAL");
        gfLabel.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        gfLabel.setTextFill(Color.web("#e67e22"));
        gfLabel.setLayoutX(colX[gfCol]);
        gfLabel.setLayoutY(gfY - 20);
        bracketPane.getChildren().add(gfLabel);

        // --- Place Winners match cards ---
        for (Map.Entry<Integer, List<Match>> e : winnersByRound.entrySet()) {
            int r = e.getKey();
            int col = r - 1;
            for (Match m : e.getValue()) {
                if (!matchY.containsKey(m)) continue;
                VBox card = createDeMatchCard(m, true, false);
                card.setLayoutX(colX[col]);
                card.setLayoutY(matchY.get(m));
                card.setPrefWidth(CARD_W);
                card.setMaxWidth(CARD_W);
                bracketPane.getChildren().add(card);
            }
        }

        // --- Place Losers match cards ---
        for (Map.Entry<Integer, List<Match>> e : losersByRound.entrySet()) {
            int col = Math.max(0, e.getKey() - 2);
            for (Match m : e.getValue()) {
                if (!matchY.containsKey(m)) continue;
                VBox card = createDeMatchCard(m, false, false);
                card.setLayoutX(colX[col]);
                card.setLayoutY(matchY.get(m));
                card.setPrefWidth(CARD_W);
                card.setMaxWidth(CARD_W);
                bracketPane.getChildren().add(card);
            }
        }

        // --- Place Grand Final card ---
        if (grandFinal != null) {
            VBox gfCard = createDeMatchCard(grandFinal, false, true);
            gfCard.setLayoutX(colX[gfCol]);
            gfCard.setLayoutY(gfY);
            gfCard.setPrefWidth(CARD_W + 20);
            gfCard.setMaxWidth(CARD_W + 20);
            bracketPane.getChildren().add(gfCard);
        }

        // --- Wrap in scroll pane ---
        ScrollPane scrollPane = new ScrollPane(bracketPane);
        scrollPane.setFitToWidth(false);
        scrollPane.setFitToHeight(false);
        scrollPane.setPrefHeight(600);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setStyle("-fx-background: #f8f8f8; -fx-border-color: #ddd;");

        bracketView.getChildren().clear();
        bracketView.getChildren().add(scrollPane);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        Team champion = tournament.getTournamentWinner();
        if (champion != null) addChampionDisplay(champion);
    }

    /**
     * Creates a compact match card for the double elimination bracket.
     * isWinners controls the border accent color; isGrandFinal gives a gold style.
     */
    private VBox createDeMatchCard(Match match, boolean isWinners, boolean isGrandFinal) {
        VBox card = new VBox(2);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(4, 8, 4, 8));

        String border = isGrandFinal ? "#f39c12" : (isWinners ? "#27ae60" : "#e74c3c");
        String bg     = isGrandFinal ? "#fff8e1" : "white";
        card.setStyle(
            "-fx-background-color: " + bg + ";" +
            "-fx-border-color: " + border + ";" +
            "-fx-border-width: 2;" +
            "-fx-border-radius: 4;" +
            "-fx-background-radius: 4;"
        );

        String t1Name = match.getTeam1() != null ? match.getTeam1().getName() : "TBD";
        String t2Name = match.getTeam2() != null ? match.getTeam2().getName() : "TBD";
        boolean done  = match.isCompleted();
        Team winner   = match.getWinner();
        String score  = match.getScore() != null ? match.getScore() : "";

        // Team 1 row
        HBox row1 = new HBox(4);
        row1.setAlignment(Pos.CENTER_LEFT);
        Label lbl1 = new Label(t1Name);
        lbl1.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        lbl1.setMaxWidth(110);
        lbl1.setWrapText(false);
        Label sc1 = new Label();
        sc1.setFont(Font.font("Arial", 11));
        if (done && !score.isEmpty()) {
            String[] p = score.split("-");
            if (p.length > 0) sc1.setText(p[0].trim());
            if (winner != null && winner.getName().equals(t1Name)) {
                lbl1.setTextFill(Color.web("#27ae60"));
                sc1.setTextFill(Color.web("#27ae60"));
            } else {
                lbl1.setTextFill(Color.GRAY);
                sc1.setTextFill(Color.GRAY);
            }
        }
        Region spacer1 = new Region(); HBox.setHgrow(spacer1, Priority.ALWAYS);
        row1.getChildren().addAll(lbl1, spacer1, sc1);

        // Divider
        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: #ddd;");

        // Team 2 row
        HBox row2 = new HBox(4);
        row2.setAlignment(Pos.CENTER_LEFT);
        Label lbl2 = new Label(t2Name);
        lbl2.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        lbl2.setMaxWidth(110);
        lbl2.setWrapText(false);
        Label sc2 = new Label();
        sc2.setFont(Font.font("Arial", 11));
        if (done && !score.isEmpty()) {
            String[] p = score.split("-");
            if (p.length > 1) sc2.setText(p[1].trim());
            if (winner != null && winner.getName().equals(t2Name)) {
                lbl2.setTextFill(Color.web("#27ae60"));
                sc2.setTextFill(Color.web("#27ae60"));
            } else {
                lbl2.setTextFill(Color.GRAY);
                sc2.setTextFill(Color.GRAY);
            }
        }
        Region spacer2 = new Region(); HBox.setHgrow(spacer2, Priority.ALWAYS);
        row2.getChildren().addAll(lbl2, spacer2, sc2);

        card.getChildren().addAll(row1, sep, row2);

        // Report button — only if match is ready and not done
        boolean canReport = !done
            && match.getTeam1() != null && match.getTeam2() != null
            && !t1Name.equals("TBD") && !t2Name.equals("TBD");

        if (canReport) {
            Button btn = new Button("REPORT");
            String btnColor = isGrandFinal ? "#f39c12" : (isWinners ? "#27ae60" : "#e74c3c");
            btn.setStyle(
                "-fx-background-color: " + btnColor + ";" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 9px;" +
                "-fx-padding: 2 6;" +
                "-fx-background-radius: 3;"
            );
            btn.setOnAction(e -> showScoreDialog(match));
            HBox btnRow = new HBox(btn);
            btnRow.setAlignment(Pos.CENTER_RIGHT);
            card.getChildren().add(btnRow);
        }

        if (isGrandFinal && done && winner != null) {
            Label champ = new Label("🏆 " + winner.getName());
            champ.setFont(Font.font("Arial", FontWeight.BOLD, 10));
            champ.setTextFill(Color.web("#e67e22"));
            card.getChildren().add(champ);
        }

        return card;
    }

    private String getWinnersRoundName(int round, int totalWinnersRounds) {
        int fromEnd = totalWinnersRounds - round;
        if (fromEnd == 0) return "Upper Final";
        if (fromEnd == 1) return "Upper Semis";
        if (fromEnd == 2) return "Upper QF";
        return "Upper R" + round;
    }

    // ========== ROUND ROBIN ==========

    private void displayRoundRobin() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));
        container.setStyle("-fx-background-color: #f5f5f5;");
        
        Label title = new Label("ROUND ROBIN - Standings & Results");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        title.setTextFill(Color.web("#2c3e50"));
        container.getChildren().add(title);
        
        GridPane standingsGrid = new GridPane();
        standingsGrid.setHgap(10);
        standingsGrid.setVgap(5);
        standingsGrid.setPadding(new Insets(10));
        standingsGrid.setStyle("-fx-background-color: white; -fx-border-color: #ddd;");
        
        String[] headers = {"Rank", "Team", "Wins", "Losses", "PD", "Win %"};
        for (int i = 0; i < headers.length; i++) {
            Label header = new Label(headers[i]);
            header.setFont(Font.font("Arial", FontWeight.BOLD, 12));
            header.setTextFill(Color.WHITE);
            header.setStyle("-fx-background-color: #3498db; -fx-padding: 8;");
            standingsGrid.add(header, i, 0);
        }
        
        List<Team> sortedTeams = new ArrayList<>(Arrays.asList(teams));
        sortedTeams.sort((a, b) -> Integer.compare(b.getWins(), a.getWins()));
        
        for (int i = 0; i < sortedTeams.size(); i++) {
            Team t = sortedTeams.get(i);
            standingsGrid.add(new Label(String.valueOf(i + 1)), 0, i + 1);
            standingsGrid.add(new Label(t.getName()), 1, i + 1);
            standingsGrid.add(new Label(String.valueOf(t.getWins())), 2, i + 1);
            standingsGrid.add(new Label(String.valueOf(t.getLosses())), 3, i + 1);
            standingsGrid.add(new Label(String.valueOf(t.getPointDifference())), 4, i + 1);
            standingsGrid.add(new Label(String.format("%.1f%%", t.getWinPercentage())), 5, i + 1);
        }
        
        container.getChildren().add(standingsGrid);
        
        Label matchesLabel = new Label("ALL MATCHES");
        matchesLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        container.getChildren().add(matchesLabel);
        
        VBox matchesList = new VBox(5);
        for (Match match : tournament.getAllMatches()) {
            HBox matchRow = createMatchResultRow(match);
            matchesList.getChildren().add(matchRow);
        }
        container.getChildren().add(matchesList);
        
        ScrollPane scrollPane = new ScrollPane(container);
        scrollPane.setFitToWidth(true);
        bracketView.getChildren().add(scrollPane);
    }

    // ========== SWISS SYSTEM ==========

    private void displaySwissSystem() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));
        container.setStyle("-fx-background-color: #f5f5f5;");
        
        Label title = new Label("SWISS SYSTEM TOURNAMENT");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        container.getChildren().add(title);
        
        GridPane standingsGrid = new GridPane();
        standingsGrid.setHgap(10);
        standingsGrid.setVgap(5);
        standingsGrid.setPadding(new Insets(10));
        standingsGrid.setStyle("-fx-background-color: white; -fx-border-color: #ddd;");
        
        String[] headers = {"Rank", "Team", "Wins", "Losses", "Points", "Opponent Score"};
        for (int i = 0; i < headers.length; i++) {
            Label header = new Label(headers[i]);
            header.setFont(Font.font("Arial", FontWeight.BOLD, 11));
            header.setStyle("-fx-background-color: #9b59b6; -fx-text-fill: white; -fx-padding: 5;");
            standingsGrid.add(header, i, 0);
        }
        
        List<Team> sortedTeams = new ArrayList<>(Arrays.asList(teams));
        sortedTeams.sort((a, b) -> Integer.compare(b.getWins(), a.getWins()));
        
        for (int i = 0; i < sortedTeams.size(); i++) {
            Team t = sortedTeams.get(i);
            standingsGrid.add(new Label(String.valueOf(i + 1)), 0, i + 1);
            standingsGrid.add(new Label(t.getName()), 1, i + 1);
            standingsGrid.add(new Label(String.valueOf(t.getWins())), 2, i + 1);
            standingsGrid.add(new Label(String.valueOf(t.getLosses())), 3, i + 1);
            standingsGrid.add(new Label(String.valueOf(t.getPointsScored())), 4, i + 1);
            standingsGrid.add(new Label(String.valueOf(t.getPointsAllowed())), 5, i + 1);
        }
        
        container.getChildren().add(standingsGrid);
        
        Label pairingsLabel = new Label("CURRENT ROUND PAIRINGS");
        pairingsLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        pairingsLabel.setStyle("-fx-padding: 10 0 5 0;");
        container.getChildren().add(pairingsLabel);
        
        List<Match> pendingMatches = tournament.getPendingMatches();
        if (pendingMatches.isEmpty()) {
            Label noMatches = new Label("All rounds complete! Check standings for winner.");
            noMatches.setFont(Font.font("Arial", 12));
            noMatches.setTextFill(Color.GRAY);
            container.getChildren().add(noMatches);
        } else {
            for (Match match : pendingMatches) {
                HBox matchRow = createSimpleMatchRow(match);
                matchRow.setStyle("-fx-padding: 5; -fx-border-color: #ddd; -fx-border-width: 1;");
                
                Button reportBtn = new Button("Report Score");
                reportBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
                reportBtn.setOnAction(e -> showScoreDialog(match));
                matchRow.getChildren().add(reportBtn);
                
                container.getChildren().add(matchRow);
            }
        }
        
        ScrollPane scrollPane = new ScrollPane(container);
        scrollPane.setFitToWidth(true);
        bracketView.getChildren().add(scrollPane);
    }

    // ========== FREE FOR ALL ==========

    private void displayFreeForAll() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));
        container.setStyle("-fx-background-color: #f5f5f5;");
        
        Label title = new Label("FREE FOR ALL - Leaderboard");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        container.getChildren().add(title);
        
        GridPane leaderboard = new GridPane();
        leaderboard.setHgap(10);
        leaderboard.setVgap(5);
        leaderboard.setPadding(new Insets(10));
        leaderboard.setStyle("-fx-background-color: white; -fx-border-color: #ddd;");
        
        String[] headers = {"Rank", "Team", "Wins", "Losses", "Points Scored", "Points Allowed"};
        for (int i = 0; i < headers.length; i++) {
            Label header = new Label(headers[i]);
            header.setFont(Font.font("Arial", FontWeight.BOLD, 11));
            header.setStyle("-fx-background-color: #2c3e50; -fx-text-fill: white; -fx-padding: 5;");
            leaderboard.add(header, i, 0);
        }
        
        List<Team> sortedTeams = new ArrayList<>(Arrays.asList(teams));
        sortedTeams.sort((a, b) -> Integer.compare(b.getWins(), a.getWins()));
        
        for (int i = 0; i < sortedTeams.size(); i++) {
            Team t = sortedTeams.get(i);
            leaderboard.add(new Label(String.valueOf(i + 1)), 0, i + 1);
            leaderboard.add(new Label(t.getName()), 1, i + 1);
            leaderboard.add(new Label(String.valueOf(t.getWins())), 2, i + 1);
            leaderboard.add(new Label(String.valueOf(t.getLosses())), 3, i + 1);
            leaderboard.add(new Label(String.valueOf(t.getPointsScored())), 4, i + 1);
            leaderboard.add(new Label(String.valueOf(t.getPointsAllowed())), 5, i + 1);
        }
        
        container.getChildren().add(leaderboard);
        
        ScrollPane scrollPane = new ScrollPane(container);
        scrollPane.setFitToWidth(true);
        bracketView.getChildren().add(scrollPane);
    }

    // ========== MATCH CARD HELPERS ==========
    
    private VBox createCompactMatchCard(Match match, int round, int seedNumber, double fontSize) {
        VBox card = new VBox(3);
        card.setAlignment(Pos.CENTER);
        card.setStyle("-fx-background-color: white; -fx-border-color: #27ae60; -fx-border-width: 1.5; -fx-border-radius: 4; -fx-background-radius: 4; -fx-padding: 4 8; -fx-min-width: 140;");
        
        String team1Name = (match.getTeam1() != null) ? match.getTeam1().getName() : "TBD";
        String team2Name = (match.getTeam2() != null) ? match.getTeam2().getName() : "TBD";
        boolean isCompleted = match.isCompleted();
        Team winner = match.getWinner();
        String score = (match.getScore() != null) ? match.getScore() : "";
        
        HBox team1Box = new HBox(5);
        team1Box.setAlignment(Pos.CENTER);
        Label team1Label = new Label(team1Name);
        team1Label.setFont(Font.font("Arial", FontWeight.BOLD, fontSize));
        
        Label score1Label = new Label();
        if (isCompleted && !score.isEmpty()) {
            String[] parts = score.split("-");
            if (parts.length > 0) {
                score1Label.setText(parts[0]);
                if (winner != null && winner.getName().equals(team1Name)) {
                    score1Label.setTextFill(Color.GREEN);
                    team1Label.setTextFill(Color.GREEN);
                }
            }
        }
        score1Label.setFont(Font.font("Arial", fontSize - 1));
        team1Box.getChildren().addAll(team1Label, score1Label);
        
        Label vsLabel = new Label(isCompleted && !score.isEmpty() ? "" : "VS");
        vsLabel.setFont(Font.font("Arial", FontWeight.BOLD, fontSize - 2));
        vsLabel.setTextFill(Color.RED);
        
        HBox team2Box = new HBox(5);
        team2Box.setAlignment(Pos.CENTER);
        Label team2Label = new Label(team2Name);
        team2Label.setFont(Font.font("Arial", FontWeight.BOLD, fontSize));
        
        Label score2Label = new Label();
        if (isCompleted && !score.isEmpty()) {
            String[] parts = score.split("-");
            if (parts.length > 1) {
                score2Label.setText(parts[1]);
                if (winner != null && winner.getName().equals(team2Name)) {
                    score2Label.setTextFill(Color.GREEN);
                    team2Label.setTextFill(Color.GREEN);
                }
            }
        }
        score2Label.setFont(Font.font("Arial", fontSize - 1));
        team2Box.getChildren().addAll(team2Label, score2Label);
        
        if (!isCompleted && match.getTeam1() != null && match.getTeam2() != null && 
            !team1Name.equals("TBD") && !team2Name.equals("TBD")) {
            Button reportBtn = new Button("REPORT");
            reportBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: " + (fontSize - 2) + "px; -fx-background-radius: 3; -fx-padding: 2 8;");
            reportBtn.setOnAction(e -> showScoreDialog(match));
            card.getChildren().addAll(team1Box, vsLabel, team2Box, reportBtn);
        } else if (isCompleted && winner != null) {
            Label winnerLabel = new Label("✓ " + winner.getName());
            winnerLabel.setFont(Font.font("Arial", FontWeight.BOLD, fontSize - 1));
            winnerLabel.setTextFill(Color.GREEN);
            card.getChildren().addAll(team1Box, vsLabel, team2Box, winnerLabel);
        } else {
            card.getChildren().addAll(team1Box, vsLabel, team2Box);
        }
        
        return card;
    }

    private HBox createMatchResultRow(Match match) {
        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-padding: 5; -fx-border-color: #eee; -fx-border-width: 1;");
        
        String team1 = match.getTeam1() != null ? match.getTeam1().getName() : "TBD";
        String team2 = match.getTeam2() != null ? match.getTeam2().getName() : "TBD";
        
        Label matchLabel = new Label(team1 + " vs " + team2);
        matchLabel.setPrefWidth(200);
        
        Label resultLabel = new Label();
        if (match.isCompleted() && match.getWinner() != null) {
            resultLabel.setText("WINNER: " + match.getWinner().getName() + " (" + match.getScore() + ")");
            resultLabel.setTextFill(Color.GREEN);
        } else {
            resultLabel.setText("PENDING");
            resultLabel.setTextFill(Color.RED);
        }
        
        Button reportBtn = new Button("Report");
        reportBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
        reportBtn.setOnAction(e -> showScoreDialog(match));
        
        if (match.isCompleted()) {
            reportBtn.setDisable(true);
            reportBtn.setText("Done");
        }
        
        row.getChildren().addAll(matchLabel, resultLabel, reportBtn);
        return row;
    }

    private HBox createSimpleMatchRow(Match match) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-padding: 3;");
        
        String team1 = match.getTeam1() != null ? match.getTeam1().getName() : "TBD";
        String team2 = match.getTeam2() != null ? match.getTeam2().getName() : "TBD";
        
        row.getChildren().add(new Label(team1 + " vs " + team2));
        
        if (match.isCompleted() && match.getWinner() != null) {
            Label winnerLabel = new Label("→ " + match.getWinner().getName());
            winnerLabel.setTextFill(Color.GREEN);
            row.getChildren().add(winnerLabel);
        }
        
        return row;
    }

    private void addChampionDisplay(Team champion) {
        HBox championBox = new HBox();
        championBox.setAlignment(Pos.CENTER);
        championBox.setStyle("-fx-background-color: #f1c40f; -fx-border-radius: 8; -fx-padding: 15;");
        Label championLabel = new Label("🏆 CHAMPION: " + champion.getName() + " 🏆");
        championLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        championLabel.setTextFill(Color.web("#2c3e50"));
        championBox.getChildren().add(championLabel);
        bracketView.getChildren().add(championBox);
    }

    // ========== SCORE DIALOG ==========
    
    private void showScoreDialog(Match match) {
        System.out.println("Opening dialog for match: " + match.getMatchId());
        
        if (match.isCompleted()) {
            showAlert("Match Already Completed", "This match has already been reported.");
            return;
        }
        
        if (match.getTeam1() == null || match.getTeam2() == null) {
            showAlert("Match Not Ready", "Both teams are not assigned yet.");
            return;
        }
        
        Stage dialogStage = new Stage();
        dialogStage.setTitle("Report Match Result");
        dialogStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        
        VBox dialogVBox = new VBox(15);
        dialogVBox.setPadding(new Insets(20));
        dialogVBox.setAlignment(Pos.CENTER);
        dialogVBox.setStyle("-fx-background-color: white;");
        
        Label titleLabel = new Label(match.getTeam1().getName() + " vs " + match.getTeam2().getName());
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        titleLabel.setTextFill(Color.web("#2c3e50"));
        
        HBox team1Box = new HBox(10);
        team1Box.setAlignment(Pos.CENTER);
        Label team1Label = new Label(match.getTeam1().getName() + ":");
        team1Label.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        TextField score1Field = new TextField();
        score1Field.setPromptText("Score");
        score1Field.setPrefWidth(80);
        team1Box.getChildren().addAll(team1Label, score1Field);
        
        HBox team2Box = new HBox(10);
        team2Box.setAlignment(Pos.CENTER);
        Label team2Label = new Label(match.getTeam2().getName() + ":");
        team2Label.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        TextField score2Field = new TextField();
        score2Field.setPromptText("Score");
        score2Field.setPrefWidth(80);
        team2Box.getChildren().addAll(team2Label, score2Field);
        
        HBox winnerBox = new HBox(10);
        winnerBox.setAlignment(Pos.CENTER);
        Label winnerLabel = new Label("Winner:");
        winnerLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        ComboBox<Team> winnerCombo = new ComboBox<>();
        winnerCombo.getItems().addAll(match.getTeam1(), match.getTeam2());
        winnerCombo.setPromptText("Select winner");
        winnerCombo.setPrefWidth(150);
        winnerBox.getChildren().addAll(winnerLabel, winnerCombo);
        
        HBox buttonBox = new HBox(20);
        buttonBox.setAlignment(Pos.CENTER);
        Button submitBtn = new Button("Submit");
        submitBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20; -fx-background-radius: 5;");
        Button cancelBtn = new Button("Cancel");
        cancelBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20; -fx-background-radius: 5;");
        buttonBox.getChildren().addAll(submitBtn, cancelBtn);
        
        dialogVBox.getChildren().addAll(titleLabel, team1Box, team2Box, winnerBox, buttonBox);
        
        Scene dialogScene = new Scene(dialogVBox, 350, 300);
        dialogStage.setScene(dialogScene);
        
        submitBtn.setOnAction(e -> {
            try {
                String score1Text = score1Field.getText();
                String score2Text = score2Field.getText();
                
                if (score1Text.isEmpty() || score2Text.isEmpty()) {
                    showAlert("Error", "Please enter both scores!");
                    return;
                }
                
                int score1 = Integer.parseInt(score1Text);
                int score2 = Integer.parseInt(score2Text);
                Team winner = winnerCombo.getValue();
                
                if (winner == null) {
                    showAlert("Error", "Please select the winner!");
                    return;
                }
                
                tournament.recordWinner(match, winner, score1, score2);
                updateBracketView();
                updateProgress();
                
                showAlert("Success", "Match result recorded!\n" + winner.getName() + " wins " + score1 + "-" + score2);
                dialogStage.close();
                
            } catch (NumberFormatException ex) {
                showAlert("Error", "Please enter valid numbers for scores!");
            }
        });
        
        cancelBtn.setOnAction(e -> dialogStage.close());
        
        dialogStage.showAndWait();
    }

    // ========== SAVE / LOAD ==========
    
    private void loadBracket() {
        File saveDir = new File("saved_brackets");
        if (!saveDir.exists()) {
            showAlert("No Brackets", "No saved brackets found.");
            return;
        }
        
        File[] files = saveDir.listFiles();
        if (files == null || files.length == 0) {
            showAlert("No Brackets", "No saved brackets found.");
            return;
        }
        
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Load Bracket");
        fileChooser.setInitialDirectory(saveDir);
        fileChooser.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("Text Files", "*.txt"));
        
        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            try (java.util.Scanner scanner = new java.util.Scanner(selectedFile)) {
                String bracketName = "";
                String bracketType = "Single Elimination";
                String sport = "";
                StringBuilder description = new StringBuilder();
                List<String> teamNames = new ArrayList<>();
                
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    if (line.startsWith("BRACKET NAME: ")) {
                        bracketName = line.substring(14);
                    } else if (line.startsWith("BRACKET TYPE: ")) {
                        bracketType = line.substring(14);
                    } else if (line.startsWith("SPORT/GAME: ")) {
                        sport = line.substring(12);
                    } else if (line.startsWith("DESCRIPTION: ")) {
                        description.append(line.substring(13));
                    } else if (line.startsWith("Team: ")) {
                        String teamName = line.substring(6);
                        int idIndex = teamName.indexOf(" |");
                        if (idIndex > 0) teamName = teamName.substring(0, idIndex);
                        teamNames.add(teamName);
                    }
                }
                
                teams = new Team[teamNames.size()];
                for (int i = 0; i < teamNames.size(); i++) {
                    teams[i] = new Team(i, teamNames.get(i));
                }
                
                bracketNameField.setText(bracketName);
                bracketTypeCombo.setValue(bracketType);
                sportField.setText(sport);
                descriptionArea.setText(description.toString());
                
                if (teams.length >= 2) {
                    tournament = new TournamentBracket(teams, bracketType);
                    updateBracketView();
                } else {
                    bracketView.getChildren().clear();
                    Label msgLabel = new Label("Not enough teams to create bracket.\nNeed at least 2 teams.");
                    msgLabel.setFont(Font.font("Arial", 14));
                    msgLabel.setTextFill(Color.web("#7f8c8d"));
                    msgLabel.setAlignment(Pos.CENTER);
                    bracketView.getChildren().add(msgLabel);
                }
                
                updateParticipantsList();
                updateProgress();
                showAlert("Bracket Loaded", "Successfully loaded: " + selectedFile.getName());
                
            } catch (FileNotFoundException e) {
                showAlert("Load Error", "Could not load file: " + e.getMessage());
            }
        }
    }

    private void saveBracket() {
        String name = bracketNameField.getText();
        String type = bracketTypeCombo.getValue();
        String sport = sportField.getText();
        String description = descriptionArea.getText();
        
        File saveDir = new File("saved_brackets");
        if (!saveDir.exists()) saveDir.mkdir();
        
        String filename = name.replaceAll("\\s+", "_") + ".txt";
        File saveFile = new File(saveDir, filename);
        
        try (PrintWriter writer = new PrintWriter(saveFile)) {
            writer.println("BRACKET NAME: " + name);
            writer.println("BRACKET TYPE: " + type);
            writer.println("SPORT/GAME: " + sport);
            writer.println("DESCRIPTION: " + description);
            writer.println("STATUS: " + statusLabel.getText());
            writer.println("TEAMS: " + teams.length);
            writer.println("----------------------------------------");
            
            for (Team team : teams) {
                writer.println("Team: " + team.getName() + " | Wins: " + team.getWins() + " | Losses: " + team.getLosses());
            }
            
            writer.println("----------------------------------------");
            writer.println("MATCH RESULTS:");
            
            for (Match match : tournament.getAllMatches()) {
                if (match.isCompleted()) {
                    String t1 = match.getTeam1() != null ? match.getTeam1().getName() : "TBD";
                    String t2 = match.getTeam2() != null ? match.getTeam2().getName() : "TBD";
                    writer.println(t1 + " vs " + t2 + " -> Winner: " + match.getWinner().getName() + " (" + match.getScore() + ")");
                }
            }
            
            showAlert("Bracket Saved", "Bracket '" + name + "' saved to:\n" + saveFile.getAbsolutePath());
            
        } catch (FileNotFoundException e) {
            showAlert("Save Error", "Could not save bracket: " + e.getMessage());
        }
    }
    
    // ========== PROGRESS & UTILITY ==========
    
    private void updateProgress() {
        if (teams.length < 2 || tournament.getAllMatches().isEmpty()) {
            progressBar.setProgress(0);
            progressLabel.setText("0% Complete");
            statusLabel.setText("PENDING");
            statusLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #e74c3c;");
            return;
        }
        
        int totalMatches = tournament.getAllMatches().size();
        int completedMatches = 0;
        for (Match m : tournament.getAllMatches()) {
            if (m.isCompleted()) completedMatches++;
        }
        
        double progress = totalMatches > 0 ? (double) completedMatches / totalMatches : 0;
        progressBar.setProgress(progress);
        progressLabel.setText(String.format("%d%% Complete", (int)(progress * 100)));
        
        if (tournament.getTournamentWinner() != null) {
            statusLabel.setText("COMPLETE");
            statusLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #27ae60;");
        } else {
            statusLabel.setText("PENDING");
            statusLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #e74c3c;");
        }
    }
    
    private Button createStyledButton(String text, String color) {
        Button btn = new Button(text);
        String normalStyle = "-fx-background-color: " + color + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15; -fx-background-radius: 5;";
        String hoverStyle  = "-fx-background-color: " + adjustColor(color) + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15; -fx-background-radius: 5;";
        btn.setStyle(normalStyle);
        btn.setOnMouseEntered(e -> btn.setStyle(hoverStyle));
        btn.setOnMouseExited(e -> btn.setStyle(normalStyle));
        return btn;
    }

    private String adjustColor(String hex) {
        if (hex.equals("#7F8EE3")) return "#5a6abf";
        if (hex.equals("#e74c3c")) return "#c0392b";
        return hex;
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}