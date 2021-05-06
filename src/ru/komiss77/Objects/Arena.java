package ru.komiss77.Objects;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import ru.komiss77.ApiOstrov;
import ru.komiss77.Enums.UniversalArenaState;
import ru.komiss77.Events.SignUpdateEvent;
import ru.komiss77.Ostrov;
import ru.komiss77.utils.ColorUtils;







public final class Arena {

    public S_info s_info;
    public int position;
    public int players;
    public String server;
    public String arena_name;
    public ItemStack item;
    public ItemMeta meta;
    private final List<String>lore=new ArrayList<>(Arrays.asList("", "", "", "", "", "", "", ""));
    public UniversalArenaState state;
    
    
    public Arena(final S_info s_info, final int position, final String server, final String arena_name, final UniversalArenaState state, final int players, final String raw_data, final String request, final String предметы ) {
        this.s_info=s_info;
        this.position=position;
        this.server=server;
        this.arena_name=arena_name;
        this.state = state;
        
        if (предметы.contains(":")) {
            item=new ItemStack( Material.valueOf(предметы.split(":")[0]));
            item.setDurability(Short.valueOf(предметы.split(":")[1]));
        } else item=new ItemStack( Material.valueOf(предметы));

        
        meta=this.item.getItemMeta();
        meta.setDisplayName(this.s_info.item.getItemMeta().getDisplayName() + (arena_name.equals("any")?"":" §7: "+arena_name) );
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES,ItemFlag.HIDE_DESTROYS,ItemFlag.HIDE_ENCHANTS,ItemFlag.HIDE_PLACED_ON);
        //lore; //0-игроки 1-пустая 2-5-строки - минимум для эвента
        
        update(state, players, raw_data);
        item.setItemMeta(meta);
//Bukkit.broadcastMessage("Arena slot="+position+" serv="+this.server+" arena="+this.arena_name);
    }

    public boolean isWaitingMode() {
        return lore.get(4).toLowerCase().contains("ожидание") ||  lore.get(5).toLowerCase().contains("ожидание");
    }
    
    public boolean isStartingMode() {
        return lore.get(4).toLowerCase().contains("старт");
    }
    
    
    public void update(final UniversalArenaState state, final int players, final String raw_data) {
        this.players=players;
        this.state=state;
        lore.set(0, players>0 ? "Игроки: "+players : "");this.lore.set(1,"");
        
        final String[]split = raw_data.split("<:>");
        
        int l=2;
        for (String line:split) {
            lore.set(l,line);
            l++;
            if (l>=7) break;
        }

        this.meta.setLore(lore);
        //if (SM.colorable.contains(item.getType())) item.setDurability(color); 1.12
        if (ApiOstrov.canChangeColor(item.getType())) ColorUtils.changeColor(item, state.attachedColor);
        this.item.setItemMeta(meta);
        try {
            this.s_info.arena_inv.setItem(position, item);
        } catch (NullPointerException ex) {
            Ostrov.log_err("Arena item update position="+position+" item="+item+" : "+ex.getMessage());
        }
        
        new BukkitRunnable() {
            @Override
            public void run() {
                Bukkit.getPluginManager().callEvent(new SignUpdateEvent(
                        server,
                        arena_name,
                        (split.length==0?"":split[0]), //защита от пустых данных
                        (split.length<=1?"":split[1]),
                        (split.length<=2?"":split[2]),
                        (split.length<=3?"":split[3]),
                        (split.length<=4?"":split[4]),
                        state
                    )
                );
            }
        }.runTask(Ostrov.GetInstance());
        
    }


    
    
    
    
}
