import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.image.*;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.*;
import java.net.*;

public class Client extends Application {
    private static final String SERVER_ADDRESS = "127.0.0.1";
    private static final int SERVER_PORT = 9999;
    private TextArea messagesArea;
    private TextField inputField;
    private TextField nameField;

    private PrintWriter writer;

    @Override
    public void start(Stage primaryStage) {
        showNameInputDialog(primaryStage);
    }

    private void showNameInputDialog(Stage primaryStage) {
        Stage nameStage = new Stage();

        VBox nameBox = new VBox();
        nameBox.setPadding(new Insets(20));

        Label nameLabel = new Label("Enter your name:");
        nameField = new TextField();
        nameField.setPromptText("Enter Your Name...");

        Button submitButton = new Button("Submit");

        submitButton.setOnAction(e -> {
            String name = nameField.getText().trim();
            if (!name.isEmpty()) {
                nameStage.close();
            }
        });

        nameField.setOnKeyPressed(e -> {
            String name = nameField.getText().trim();
            if(e.getCode() == KeyCode.ENTER) {
                if(!name.isEmpty()) {
                    nameStage.close();
                }
            }
        });

        nameBox.getChildren().addAll(nameLabel, nameField, submitButton);

        Scene nameScene = new Scene(nameBox, 300, 150);
        nameStage.setScene(nameScene);
        nameStage.setTitle("Name Input");

        nameStage.setOnHidden(e -> {
            launchMainApplication(primaryStage);
        });

        nameStage.show();
    }

    public void launchMainApplication(Stage primaryStage) {

        BorderPane pane = new BorderPane();
        pane.setPadding(new Insets(10));

        messagesArea = new TextArea();
        messagesArea.setEditable(false);
        messagesArea.setWrapText(true);
        messagesArea.setStyle("-fx-control-inner-background: #444444;-fx-prompt-text-fill: #CCCCCC;");

        ScrollPane paneForMessagesArea = new ScrollPane(messagesArea);
        paneForMessagesArea.setFitToWidth(true);

        BorderPane.setMargin(paneForMessagesArea, new Insets(0, 10, 10, 10));
        pane.setCenter(paneForMessagesArea);

        // Set the ScrollPane to fit both width and height to its container
        paneForMessagesArea.setFitToHeight(true);
        paneForMessagesArea.setFitToWidth(true);

        inputField = new TextField();
        inputField.setStyle("-fx-control-inner-background: #444444;-fx-prompt-text-fill: #CCCCCC;");
        inputField.setPromptText("Type your message...");

        Button sendButton = new Button();

        ImageView imageIcon = new ImageView(new Image("image/buttonIcon.png"));
        imageIcon.setFitWidth(20);
        imageIcon.setFitHeight(20);

        sendButton.setGraphic(imageIcon);
        sendButton.setStyle("-fx-control-inner-background: #03527E");

        HBox messageBox = new HBox(inputField, sendButton);
        HBox.setHgrow(inputField, Priority.ALWAYS);
        sendButton.setPrefWidth(60);
        messageBox.setSpacing(5);
        BorderPane.setMargin(messageBox, new Insets(0, 10, 10, 10));
        pane.setBottom(messageBox);

        sendButton.setOnAction(e -> sendMessage());

        inputField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                sendMessage();
            }
        });


        Scene scene = new Scene(pane, 400, 300);

        pane.setBackground(new Background(new BackgroundFill(Color.web("#888888"), null, null)));

        primaryStage.setTitle("Client");
        primaryStage.setScene(scene);
        primaryStage.show();

        inputField.requestFocus();

        connectToServer();
    }

    @Override
    public void stop() {
        // Close resources when the application is stopped
        if (writer != null) {
            writer.close();
        }
    }


    private void connectToServer() {
        try {
            Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            InputStream input = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            writer = new PrintWriter(socket.getOutputStream(), true);
    
            new Thread(() -> {
                try {
                    String message;
                    while ((message = reader.readLine()) != null) {
                        final String finalMessage = message; // Define a final variable
                        Platform.runLater(() -> messagesArea.appendText(finalMessage + "\n"));
                    }
                } catch (IOException e) {
                    System.out.println("You are disconnected...");
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendMessage() {
        //TextFlow textFlow = new TextFlow();
        String name = nameField.getText().trim();
        String message = inputField.getText().trim();

        Platform.runLater(() -> {
            messagesArea.appendText("You: "+ message + "\n");
        });

        if (!message.isEmpty()) {
            String messageToSend = (name.isEmpty() ? "Anonymous" : name) + ": " + message;
            writer.println(messageToSend);
            inputField.clear();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
