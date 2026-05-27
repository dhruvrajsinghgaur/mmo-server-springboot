package com.mmo.mmo_server;

public class Weapon extends Items {
    int attackPower;
    int armorDamage;
    int range;
    int durability;

    public Weapon(String name, int id, String type, int value, String description, int attackPower, int armorDamage,int range,int durability){
        super(name, id,"WEAPON", value, description);
        this.attackPower = attackPower;
        this.armorDamage = armorDamage;
        this.range = range;
        this.durability = durability;
    }
    @Override
    public String getInfo() {
        return String.format("%s | ATK: %d | ARM_DMG: %d | Range: %d | Durability: %d",
                name, attackPower, armorDamage, range, durability);
    }
}
