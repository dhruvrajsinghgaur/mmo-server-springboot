package com.mmo.mmo_server;

import com.google.gson.Gson;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class GameLoop {

    private final GameState gameState = GameState.getInstance();
    private final GameService gameService;
    private Gson gson = new Gson();

    public GameLoop(GameService gameService){
        this.gameService = gameService;
    }

    @Scheduled(fixedRate = 50)
    public void tick(){
        if (gameState.phase != GamePhase.PLAYING) return;

        String stateJson = buildStateJson();

        List<String> keys = new ArrayList<>(gameState.getAllPlayers().keySet());
        for (int i = 0; i < keys.size(); i++) {
            GamePlayer player = gameState.getAllPlayers().get(keys.get(i));
            try{
                if (player.session.isOpen()) {
                    player.session.sendMessage(new TextMessage(stateJson));
                }
            }catch (Exception e){
                System.out.println("Failed to sent to " + player.username);
            }
        }

        String winner = gameService.checkWinner();
        if (winner != null) {
            System.out.println("Game over!! Winner is " + winner);
            for (int i = 0; i < keys.size(); i++) {
                String winMessage = gson.toJson(Map.of("type", "WINNER", "winner", winner));
                GamePlayer player = gameState.getAllPlayers().get(keys.get(i));
                try{
                    if (player != null && player.session.isOpen()) {
                        player.session.sendMessage(new TextMessage(winMessage));
                    }
                }catch (Exception e){
                    System.out.println("Failed to sent to " + player.username);
                }
            }
            gameState.reset();
        }
    }

    public String buildStateJson(){
        Map<String, Object> state = new HashMap<>();
        List<Map<String, Object>> playerList = new ArrayList<>();

        List<String> keys = new ArrayList<>(gameState.getAllPlayers().keySet());

        for (int i = 0; i < keys.size(); i++) {
            GamePlayer player = gameState.getAllPlayers().get(keys.get(i));
            Map<String, Object> playerData = new HashMap<>();

            playerData.put("username", player.username);
            playerData.put("x", player.x);
            playerData.put("y", player.y);
            playerData.put("hp", player.hp);
            playerData.put("action", player.action);
            playerList.add(playerData);
        }

        List<Map<String, Object>> weaponList = new ArrayList<>();
        for (int i = 0; i < gameState.weaponsOnGround.size(); i++) {
            Weapon w = gameState.weaponsOnGround.get(i);
            Map<String, Object> weaponData = new HashMap<>();
            weaponData.put("x", w.worldX);
            weaponData.put("y", w.worldY);
            weaponData.put("name", w.name);
            weaponList.add(weaponData);
        }

        state.put("type", "STATE");
        state.put("players", playerList);
        state.put("weapons", gameState.weaponsOnGround.size());
        state.put("weaponPositions", weaponList);
        return gson.toJson(state);
    }
}
