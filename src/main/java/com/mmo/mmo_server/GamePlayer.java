package com.mmo.mmo_server;

import org.springframework.web.socket.WebSocketSession;

import java.util.ArrayList;
import java.util.List;

public class GamePlayer {
    long id;
    String username;
    int x, y;
    int hp, maxHp;
    int dx, dy;
    Action action;
    Weapon equippedWeapon;
    List<Items> inventory;
    long lastActionTime;
    WebSocketSession session;

    public GamePlayer(String username, long id, int x, int y, WebSocketSession session) {
        this.username = username;
        this.id = id;
        this.x = x;
        this.y = y;
        this.session = session;
        this.hp = 100;
        this.maxHp = 100;
        this.action = Action.IDLE;
        this.inventory = new ArrayList<>();
        this.lastActionTime = 0;
        this.dx = 0;
        this.dy = 0;
    }

    public boolean isAlive(){
        return hp > 0;
    }

    public boolean canAct() {
        return System.currentTimeMillis() - lastActionTime > 200;
    }

    public void updateLastAction() {
        this.lastActionTime = System.currentTimeMillis();
    }

}
