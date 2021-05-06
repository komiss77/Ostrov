package ru.komiss77.Objects;

import org.bukkit.Location;


public class Warp {
    
    String type;
    String owner;
    String desc;
    Location loc;
    boolean open;
    boolean need_perm;
    int cost;
    int counter;
    long created;

    public Warp(String name, String type, String owner, String desc, Location loc, boolean open, boolean need_perm, int use_cost, int counter, long create_time) {

        this.type = type;
        this.owner = owner;
        this.desc = desc;
        this.loc = loc;
        this.open = open;
        this.need_perm = need_perm;
        this.cost = use_cost;
        this.counter = counter;
        this.created = create_time;
                
    }

    public String Get_type() {
        return this.type;
    }

    public String Get_owner() {
        return this.owner;
    }

    public String Get_desc() {
        return this.desc;
    }

    public Location Get_loc() {
        return this.loc;
    }

    public boolean Is_open() {
        return this.open;
    }

    public boolean Need_perm() {
        return this.need_perm;
    }

    public int Get_cost() {
        return this.cost;
    }

    public int Get_counter() {
        return this.counter;
    }
 
    public long Get_createtime() {
        return this.created;
    }

    
    
    
    
    public void Set_open(boolean on) {
        this.open = on;
    }

    public void Set_cost(int cost) {
        this.cost = cost;
    }

    public void Add_count() {
        this.counter++;
    }
 
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
}
