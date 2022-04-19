package me.bartosz1.gotifyclient;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.util.HashMap;

public class MainClass extends Application {

    private TrayIcon tray;
    private WebSocketHandler wsHandler;
    private final HashMap<Integer, String> apps = new HashMap<>();
    private static final Logger LOGGER = LoggerFactory.getLogger(MainClass.class);

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) throws AWTException {
        Platform.setImplicitExit(false);
        tray = setupTray();
        SystemTray.getSystemTray().add(tray);
        Config cfg = new Config(stage, this);
        if (new File("config.properties").exists()) {
            wsHandler = new WebSocketHandler(this, cfg.getUrl(), cfg.getKey());
            wsHandler.start();
        }
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
        MenuItem info = new MenuItem("Gotify Client v1.1");
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

    public WebSocketHandler getWebSocketHandler() {
        return wsHandler;
    }

    public void setWebSocketHandler(WebSocketHandler wsHandler) {
        this.wsHandler = wsHandler;
    }

}
