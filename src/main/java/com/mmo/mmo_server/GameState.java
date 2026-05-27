package com.mmo.mmo_server;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class GameState {

    private static GameState instance;

    ConcurrentHashMap<String, GamePlayer> players;
    List<Weapon> weaponsOnGround;
    boolean gameRunning;
    int gridSize = 100;

    public GameState(){
        players = new ConcurrentHashMap<>();
        weaponsOnGround = new ArrayList<>();
        gameRunning = false;
    }

    public static GameState getInstance(){
        if (instance == null) instance = new GameState();
        return instance;
    }

    public void addPlayer(GamePlayer player){
        players.put(player.username, player);
    }

    public void removePlayer(GamePlayer player){
        players.remove(player.username);
    }

    public void removePlayer(String username){
        players.remove(username);
    }

    public GamePlayer getPlayer(String username){
        return players.get(username);
    }

    public ConcurrentHashMap<String, GamePlayer> getAllPlayers(){
        return players;
    }

    public void addWeapon(Weapon weapon){
        weaponsOnGround.add(weapon);
    }

    public void removeWeapon(Weapon weapon){
        weaponsOnGround.remove(weapon);
    }



}
