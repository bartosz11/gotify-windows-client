package me.bartosz1.gotifyclient;

import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.IOException;

public class WebSocketHandler extends WebSocketListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketHandler.class);
    private final MainClass main;
    private final Config config;
    private WebSocket socket;

    public WebSocketHandler(MainClass main, Config config) {
        this.main = main;
        this.config = config;
    }

    public void start() {
        Request req = new Request.Builder().url(config.getUrl()+"/stream").addHeader("x-gotify-key", config.getKey()).build();
        main.getHttp().newWebSocket(req, this);
    }
    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        LOGGER.info("WebSocket connection opened successfully!");
        main.getTray().displayMessage("Gotify - Connection opened successfully.", "", TrayIcon.MessageType.INFO);
        socket = webSocket;
    }

    @Override
    public void onMessage(WebSocket webSocket, String text) {
        LOGGER.info("New WebSocket message: "+text);
        JSONObject json = new JSONObject(text);
        updateAppList();
        main.getTray().displayMessage("Gotify - New Message - "+main.getApps().get(json.getInt("appid")), json.getString("message"), TrayIcon.MessageType.INFO);
    }

    @Override
    public void onClosing(WebSocket webSocket, int code, String reason) {
        main.getTray().displayMessage("Gotify - Connection closing", "Reason: "+reason+", code: "+code, TrayIcon.MessageType.INFO);
        LOGGER.info("WebSocket is closing, reason: "+reason+", code: "+code);
        webSocket.close(1000, null);
    }

    @Override
    public void onFailure(WebSocket webSocket, Throwable t, Response response) {
        main.getTray().displayMessage("Gotify - Connection failure", t.getMessage(), TrayIcon.MessageType.INFO);
        LOGGER.error("WebSocket failure:");
        t.printStackTrace();
    }

    private void updateAppList() {
        Request req = new Request.Builder().url(config.getUrl()+"/application").addHeader("x-gotify-key", config.getKey()).build();
        try {
            Response resp = main.getHttp().newCall(req).execute();
            JSONArray array = new JSONArray(resp.body().string());
            main.getApps().clear();
            for (int i = 0; i < array.length(); i++) {
                JSONObject current = array.getJSONObject(i);
                main.getApps().put(current.getInt("id"), current.getString("name"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close(){
        socket.close(1000, "app exit");
    }
}
