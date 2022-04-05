package me.bartosz1.gotifyclient;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Properties;

public class Config {

    private String url;
    private String key;
    private WebSocketHandler ws;


    public Config(Stage stage, MainClass main) {
        ws = new WebSocketHandler(main, this);
        if (!new File("config.properties").exists()) {
            firstTimeSetupForm(stage);
            main.setWsHandler(ws);
        }
        else {
            Properties properties = new Properties();
            try {
                properties.load(new FileReader("config.properties"));
                url = properties.getProperty("url");
                key = properties.getProperty("key");
                main.setWsHandler(ws);
                ws.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void firstTimeSetupForm(Stage stage) {
        stage.setTitle("Gotify Client");
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        Scene scene = new Scene(grid, 300, 275);
        Label urLabel = new Label("Gotify URL");
        grid.add(urLabel, 1, 1);
        TextField urlField = new TextField();
        grid.add(urlField, 1, 2);
        Label keyLabel = new Label("Gotify client key");
        grid.add(keyLabel, 1,3);
        TextField keyField = new TextField();
        grid.add(keyField, 1, 4);
        Button button = new Button("Submit");
        grid.add(button, 1, 5);
        button.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (urlField.getText().isEmpty() || keyField.getText().isEmpty()) {
                    showAlert(Alert.AlertType.ERROR, grid.getScene().getWindow(), "Form error", "Some field is null");
                    return;
                }
                writeToConfig(urlField.getText(), keyField.getText());
                stage.hide();
                ws.start();
            }
        });
        stage.setScene(scene);
        stage.show();
    }

    private void showAlert(Alert.AlertType alertType, Window owner, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initOwner(owner);
        alert.setHeight(100);
        alert.setWidth(200);
        alert.show();
    }

    private void writeToConfig(String url, String key) {
        this.url = url;
        this.key = key;
        File file = new File("config.properties");
        try {
            if (!file.exists()) file.createNewFile();
            if (url.endsWith("/")) url = url.substring(0, url.length()-1);
            PrintWriter print = new PrintWriter("config.properties");
            print.println("url="+url);
            print.println("key="+key);
            print.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getUrl() {
        return url;
    }

    public String getKey() {
        return key;
    }
}
