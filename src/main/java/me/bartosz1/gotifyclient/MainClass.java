package me.bartosz1.gotifyclient;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class MainClass extends Application {

    //todo reconnect thing
    //todo better tray icon lolol
    //todo thread safe stuff

    private OkHttpClient http;
    private TrayIcon tray;
    private WebSocketHandler wsHandler;
    private final HashMap<Integer, String> apps = new HashMap<>();
    private static final Logger LOGGER = LoggerFactory.getLogger(MainClass.class);

    @Override
    public void start(Stage stage) throws AWTException, InterruptedException {
        Platform.setImplicitExit(false);
        http = new OkHttpClient.Builder().readTimeout(10, TimeUnit.SECONDS).writeTimeout(10, TimeUnit.SECONDS).callTimeout(10, TimeUnit.SECONDS).connectTimeout(10, TimeUnit.SECONDS).build();
        tray = setupTray();
        Config cfg = new Config(stage, this);
        SystemTray.getSystemTray().add(tray);
    }

    public static void main(String[] args) {
        launch();
    }

    public OkHttpClient getHttp() {
        return http;
    }

    public TrayIcon getTray() {
        return tray;
    }

    public HashMap<Integer, String> getApps() {
        return apps;
    }

    private TrayIcon setupTray() {
        TrayIcon tray = new TrayIcon(Toolkit.getDefaultToolkit().createImage(MainClass.class.getResource("/icon.png")), "Gotify Client");
        PopupMenu menu = new PopupMenu();
        MenuItem info = new MenuItem("Gotify Client v1.0");
        info.setEnabled(false);
        MenuItem exit = new MenuItem("Exit");
        exit.addActionListener(listener -> {
            LOGGER.info("Exiting - reason: tray button");
            if (wsHandler != null) wsHandler.close();
            System.exit(0);
        });
        menu.add(info);
        menu.addSeparator();
        menu.add(exit);
        tray.setPopupMenu(menu);
        return tray;
    }

    public void setWsHandler(WebSocketHandler wsHandler) {
        this.wsHandler = wsHandler;
    }
}
