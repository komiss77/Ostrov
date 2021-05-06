package ru.komiss77.Events;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;



public class BattleModeEvent extends Event{
    
    private static HandlerList handlers = new HandlerList();
    private Entity attack;
    private Entity target;
    private DamageCause cause;
    private boolean canceled;

    public BattleModeEvent(Entity attack, Entity target, DamageCause cause) {
        this.attack = attack;
        this.target = target;
        this.cause = cause;
        this.canceled=false;
    }


    public Entity Get_atack_entity() {
        return this.attack;
    }   
    public Entity Get_target_entity() {
        return this.target;
    }   
    
    public DamageCause Get_DamageCause() {
        return this.cause;
    }   
    
    public boolean Atack_is_player() {
        //return this.attack.getType()==EntityType.PLAYER && !Ostrov.isCitizen(this.attack); - НПС откидываются в проверке пвп
        return this.attack.getType()==EntityType.PLAYER;
    }   
    public boolean Target_is_player() {
        //return this.target.getType()==EntityType.PLAYER && !Ostrov.isCitizen(this.target);- НПС откидываются в проверке пвп
        return this.target.getType()==EntityType.PLAYER;
    }   
   
    public boolean Is_canceled() {
        return this.canceled;
    }   
   
    public void Set_canceled(boolean canceled) {
        this.canceled=canceled;
    }   
   
   
   

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }


}
