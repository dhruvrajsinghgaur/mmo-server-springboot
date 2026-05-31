package com.mmo.mmo_server;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class GameService {

    private final PlayerRepository playerRepository;
    private GameState gameState = GameState.getInstance();

    public GameService(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    public void movePlayer(String username, int dx, int dy){
        GamePlayer player = gameState.getPlayer(username);

        if (player == null || !player.isAlive()) return;
        if (!player.canAct()) return;

        int newX = player.x + dx;
        int newY = player.y + dy;

        if (newX < 0 || newX >= gameState.gridSize) return;
        if (newY < 0 || newY >= gameState.gridSize) return;

        player.x = newX;
        player.y = newY;
        player.action = Action.MOVING;
        player.updateLastAction();

        checkWeaponPickup(player);
    }

    public void checkWeaponPickup(GamePlayer player){
        for (int i = 0; i < gameState.weaponsOnGround.size(); i++) {
            Weapon weapon = gameState.weaponsOnGround.get(i);
            if (player.x == weapon.worldX && player.y == weapon.worldY) {
                player.inventory.add(weapon);
                if (player.equippedWeapon == null){
                    player.equippedWeapon = weapon;
                }
                gameState.removeWeapon(weapon);
                break;
            }
        }
    }

    public void spawnWeapons(){
        Random random = new Random();

        Weapon sword = new Weapon("Sword", 1, 100, "A sharp sword", 25, 3, 1, 50);
        sword.worldX = random.nextInt(gameState.gridSize);
        sword.worldY = random.nextInt(gameState.gridSize);
        gameState.addWeapon(sword);

        Weapon axe = new Weapon("Axe", 2, 150, "A heavy axe", 35, 8, 1, 30);
        axe.worldX = random.nextInt(gameState.gridSize);
        axe.worldY = random.nextInt(gameState.gridSize);
        gameState.addWeapon(axe);

        Weapon bow = new Weapon("Bow", 3, 200, "A ranged bow", 20, 1, 3, 40);
        bow.worldX = random.nextInt(gameState.gridSize);
        bow.worldY = random.nextInt(gameState.gridSize);
        gameState.addWeapon(bow);
    }

    public void attack(String username, int dx, int dy){
        GamePlayer attacker = gameState.getPlayer(username);

        if (attacker == null || !attacker.isAlive() || !attacker.canAct()) return;

        int targetX = attacker.x + dx;
        int targetY = attacker.y + dy;

        if (attacker.equippedWeapon == null){
            GamePlayer targetPlayer = getPlayerAtTile(targetX, targetY);
            if (targetPlayer != null){
                targetPlayer.hp = targetPlayer.hp - 1;
                if (targetPlayer.hp <= 0){
                    targetPlayer.hp = 0;
                    targetPlayer.action = Action.DEAD;
                }
            }
        }

        else if (attacker.equippedWeapon.range == 1) {
            GamePlayer targetPlayer = getPlayerAtTile(targetX, targetY);
            if (targetPlayer != null){
                targetPlayer.hp = targetPlayer.hp - attacker.equippedWeapon.attackPower;
                if (targetPlayer.hp <= 0){
                    targetPlayer.hp = 0;
                    targetPlayer.action = Action.DEAD;
                }
            }
        }

        else {
            for (int i = 0; i < attacker.equippedWeapon.range; i++) {
                int scanX = targetX + (dx * i);
                int scanY = targetY + (dy * i);
                GamePlayer targetPlayer = getPlayerAtTile(scanX, scanY);
                if (targetPlayer != null) {
                    targetPlayer.hp = targetPlayer.hp - attacker.equippedWeapon.attackPower;
                    if (targetPlayer.hp <= 0) {
                        targetPlayer.hp = 0;
                        targetPlayer.action = Action.DEAD;
                    }
                    break;
                }
            }
        }

        attacker.updateLastAction();
        attacker.action = Action.ATTACKING;
    }

    private GamePlayer getPlayerAtTile(int x, int y){
        List<String> keys = new ArrayList<>(gameState.getAllPlayers().keySet());
        for (int i = 0; i < keys.size(); i++) {
            GamePlayer player = gameState.getAllPlayers().get(keys.get(i));
            if (player.x == x && player.y == y && player.isAlive())  return player;
        }
        return null;
    }

    public String checkWinner(){

        if (gameState.phase != GamePhase.PLAYING) return null;

        List<GamePlayer> alivePlayers = new ArrayList<>();
        List<String> keys = new ArrayList<>(gameState.getAllPlayers().keySet());

        for (int i = 0; i < keys.size(); i++) {
            GamePlayer player = gameState.getAllPlayers().get(keys.get(i));
            if (player.isAlive()) alivePlayers.add(player);
        }
        if (alivePlayers.size() == 1) {
            gameState.phase = GamePhase.ENDED;
            gameState.reset();
            return alivePlayers.get(0).username;
        }

        if (alivePlayers.size() == 0) {
            gameState.phase = GamePhase.ENDED;
            gameState.reset();
            return "DRAW";
        }
        return null;
    }
}
