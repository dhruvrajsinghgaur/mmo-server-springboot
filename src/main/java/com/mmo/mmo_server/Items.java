package com.mmo.mmo_server;

public abstract class Items {
    protected String name;
    protected int id;
    protected String type;
    protected int value;
    protected String description;

    public Items(String name, int id, String type, int value, String description){
        this.name = name;
        this.id = id;
        this.type = type;
        this.value = value;
        this.description = description;
    }
    public abstract String getInfo();
}
