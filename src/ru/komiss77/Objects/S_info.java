package ru.komiss77.Objects;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import ru.komiss77.ApiOstrov;
import ru.komiss77.Enums.UniversalArenaState;
import ru.komiss77.Events.SignUpdateEvent;
import ru.komiss77.Events.SinfoUpdateEvent;
import ru.komiss77.Managers.SM;
import ru.komiss77.Managers.SM.S_type;
import ru.komiss77.Managers.Warps;
import ru.komiss77.Ostrov;
import ru.komiss77.utils.ColorUtils;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.ItemUtils;







public class S_info {
    public S_type type;
    public String server;
    public String level;
    public String description;
    public int position;
    public Inventory arena_inv;
    private final HashMap<Integer,Arena>arenas=new HashMap<>(); //название арены,арена
    public ItemStack item;
    private List<String>lore=new ArrayList<>(Arrays.asList("", "", "", ""));
    
    public int players;
    private boolean flash;
     
    
    public S_info( final S_type type, final String server, final String level, final String description, final String item_desc, final int position, final String предмет, final int players) {
        this.type=type;
        this.server=server;
        this.level=level;
        this.description=description;
        this.position=position;
        arena_inv=Bukkit.createInventory(null, 45, "§2Сервера "+server);
        arena_inv.setItem(44, new ItemBuilder(Material.BARRIER).setName("§5Назад").build());
        
        //if (предметы.contains(":")) {
        //item=new ItemStack( Material.(предметы.split(":")[0]));
        Material mat = Material.matchMaterial(предмет) ;
        if (mat==null || mat==Material.AIR) {
            Ostrov.log_err("при создании S_info для "+server+" не найден материал "+предмет+", заменено на серый бетон.");
            mat=Material.GRAY_CONCRETE;
        }
        item=new ItemStack( mat );
            //item.setDurability(Short.valueOf(предметы.split(":")[1]));
        //} else {
        //    item=new ItemStack( Material.valueOf(предметы));
        //}
        
        ItemMeta meta=item.getItemMeta();
        meta.setDisplayName(description==null?"§cошибка":description);
        //this.meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES,ItemFlag.HIDE_DESTROYS,ItemFlag.HIDE_PLACED_ON);
        
        lore.set(0,"§7Игроки: "+players);
        
        if (type==S_type.SINGLE) lore.set(2,"§a⊳ Клик - перейти на сервер"); //line 1-4+разделитель
        else {
            if (Warps.use && Warps.Warp_exist(server)) {
                lore.set(1,"§a  Лев.клик - к табличкам");
                lore.set(2,"§a⊳ Прав.клик - выбрать арену"); //line 1-4+разделитель
            } else {
                lore.set(2,"§a⊳ Клик - выбрать арену");
            }
        }
        
        
        lore=ItemUtils.Gen_lore(lore, item_desc, "§7");
        meta.setLore(lore);
        item.setItemMeta(meta);
//this.item.addUnsafeEnchantment(Enchantment.LUCK, 1);
        SM.main_inv.setItem(position, item);
        
    }


    
    
    
    
    
    public void addArena(final String arena_name, final Arena arena) {
        arena.position=arenas.size();
        arenas.put(arena.position, arena);
        arena_inv.setItem(arena.position, arena.item);
    }

    
    
    
    
    
    public void invClick(final Player p, final int slot) {
//System.out.println(" S_info invClick slot="+slot);                                    
        if (slot==44) {
            p.openInventory(SM.main_inv);
            p.playSound(p.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 2, 2);
            return;
        }
        
        if (arenas.containsKey(slot)) {
            ApiOstrov.sendToServer(p, arenas.get(slot).server, arenas.get(slot).arena_name);
        }
    }
    
    public void updatePlayerCount(final int players) {
//System.out.println("updatePlayerCount "+players);                                    
        if (type==S_type.SINGLE) {
            this.players=players;
            new BukkitRunnable() {
                @Override
                public void run() {  //вызов для пересчёта общего онлайна в этой игре
                    Bukkit.getPluginManager().callEvent(new SignUpdateEvent(server, "any", "", "", "", "", "", UniversalArenaState.НЕОПРЕДЕЛЕНО));
                }
            }.runTask(Ostrov.GetInstance());
            
        } else {
            this.players=0;
            arenas.values().stream().forEach((a) -> {
                this.players+=a.players;
            });
        }
        lore.set( 0, this.players>=0 ? "§7Игроки: "+this.players : "" );
    }
    

    
    public void updateArena(final String server, final String arena_name,  final UniversalArenaState state, final int players, final String raw_data) {
//Bukkit.broadcastMessage("updateArena server="+server+" arena="+arena_name+"  lines="+lines); 
        final Arena arena = getArena(server, arena_name);
        if (arena!=null) {
            arena.update(state, players, raw_data);  //SignUpdateEvent по имени арены внутри
            updatePlayerCount(players); //пересчитать онлайн
            new BukkitRunnable() {
                @Override
                public void run() {
                    Bukkit.getPluginManager().callEvent(new SinfoUpdateEvent(server.substring(0, 2), players ));
                }
            }.runTask(Ostrov.instance);
            
        }
    }

    public Arena getArena(String server, String arena_name) {
        for (Arena a:arenas.values()) {
            if (a.server.equals(server) && a.arena_name.equals(arena_name)) return a;
        }
        return null;
    }
    
    public void do_Tick() {
//System.out.println("do_Tick server="+this.server+" wiew="+this.arena_inv.getViewers());        
//this.update(Ostrov.randInt(0, 100));
        ItemMeta meta=item.getItemMeta();
        if (this.players>=0) {
            if (flash) {
                flash=false;
                lore.set(1,lore.get(1).replaceFirst("§a⊳", "§a ") );
                lore.set(2,lore.get(2).replaceFirst("§a ", "§a⊳") );
            } else {
                flash=true;
                lore.set(1,lore.get(1).replaceFirst("§a ", "§a⊳") );
                lore.set(2,lore.get(2).replaceFirst("§a⊳", "§a ") );
            }
            //if (SM.colorable.contains(item.getType())) item.setDurability((short) Ostrov.randInt(0, 15)); 1.12
           if (ApiOstrov.canChangeColor(item.getType())) ColorUtils.changeColor(item, (short) ApiOstrov.randInt(0, 15));
        } else if (type==S_type.SINGLE) {
            lore.set(2,"§4Сервер выключен" );
            //if (SM.colorable.contains(item.getType())) item.setDurability((short)0); 1.12
           if (ApiOstrov.canChangeColor(item.getType())) ColorUtils.changeColor(item, (short) 0);
        }
        meta.setLore(lore); //1 ArrayIndexOutOfBoundsException: 7
        item.setItemMeta(meta);  //2 Null string not allowed
        SM.main_inv.setItem(position, item);
        
    }

    public int getArenaCount() {
        return arenas.size();
    }

    public Collection<Arena> getArenas() {
        return arenas.values();
    }



    
    
}
