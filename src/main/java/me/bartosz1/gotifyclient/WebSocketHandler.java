package me.bartosz1.gotifyclient;

import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class WebSocketHandler extends WebSocketListener {

    private static final OkHttpClient http = new OkHttpClient.Builder().readTimeout(10,TimeUnit.SECONDS).writeTimeout(10,TimeUnit.SECONDS).callTimeout(10,TimeUnit.SECONDS).connectTimeout(10,TimeUnit.SECONDS).build();
    private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketHandler.class);
    private final MainClass main;
    private final String url;
    private final String key;
    private WebSocket socket;
    private Request startReq;
    private boolean isReconnecting = false;
    private int reconnectAttempts = 0;

    public WebSocketHandler(MainClass main, String url, String key) {
        this.main = main;
        this.url = url;
        this.key = key;
    }

    public void start() {
        if (startReq == null) {
            startReq = new Request.Builder().url(url+"/stream").addHeader("x-gotify-key", key).build();
        }
        http.newWebSocket(startReq, this);
    }
    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        if (isReconnecting) {
            LOGGER.info("Reconnected to WebSocket.");
            //just for those people getting triggered by wrong grammar / spelling or whatever
            if (reconnectAttempts==1) main.getTray().displayMessage("Gotify - Reconnected after "+reconnectAttempts+" attempt.", "", TrayIcon.MessageType.INFO);
            else main.getTray().displayMessage("Gotify - Reconnected after "+reconnectAttempts+" attempts.", "", TrayIcon.MessageType.INFO);
            socket = webSocket;
            isReconnecting = false;
            return;
        }
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
        if (!isReconnecting) main.getTray().displayMessage("Gotify - Connection failure", "Next attempt in 30 seconds.", TrayIcon.MessageType.INFO);
        LOGGER.error("WebSocket connection failure: "+t.getMessage());
        reconnect();
    }

    private void updateAppList() {
        Request req = new Request.Builder().url(url+"/application").addHeader("x-gotify-key", key).build();
        try {
            Response resp = http.newCall(req).execute();
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
        if (socket != null) {
            socket.close(1000, null);
        }
    }

    private void reconnect(){
        try {
            isReconnecting = true;
            reconnectAttempts++;
            Thread.sleep(30000);
            start();
        } catch (Exception ignored) {}

    }
}
