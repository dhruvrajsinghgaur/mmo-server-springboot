package com.mmo.mmo_server;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.Map;

@Component
public class GameWebSocket extends TextWebSocketHandler {

    private final GameService gameService;
    private final GameState gameState = GameState.getInstance();
    private final Gson gson = new Gson();

    public GameWebSocket(GameService gameService){
        this.gameService = gameService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session){
        System.out.println("Players Connected: " + session.getId());
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        try {
            Map<String, Object> data = gson.fromJson(message.getPayload(), Map.class);
            String type = (String) data.get("type");

            switch (type) {
                case "JOIN" -> {
                    // create new GamePlayer and add to gameState
                    String username = (String) data.get("username"); // ← read here
                    if (username == null) {
                        session.sendMessage(new TextMessage("Username required!"));
                        return;
                    }
                    int x = (int) (Math.random() * gameState.gridSize);
                    int y = (int) (Math.random() * gameState.gridSize);
                    GamePlayer player = new GamePlayer(username, 0, x, y, session);
                    gameState.addPlayer(player);
                    session.sendMessage(new TextMessage("Joined at " + x + "," + y));
                }
                case "MOVE" -> {
                    String username = (String) data.get("usename");
                    if (username == null) return;
                    int dx = ((Double) data.get("dx")).intValue();
                    int dy = ((Double) data.get("dy")).intValue();
                    gameService.movePlayer(username, dx, dy);
                }
                case "ATTACK" -> {
                    String username = (String) data.get("usename");
                    int dx = ((Double) data.get("dx")).intValue();
                    int dy = ((Double) data.get("dy")).intValue();
                    gameService.attack(username, dx, dy);
                }
                case "PING" -> {
                    session.sendMessage(new TextMessage("PONG"));
                }
            }

        } catch (Exception e) {
            System.out.println("Error handling message: " + e.getMessage());
            e.printStackTrace();
        }
    }


    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status){
        System.out.println("Player Disconnected: " + session.getId());
    }

}
