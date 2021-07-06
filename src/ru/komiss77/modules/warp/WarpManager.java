package ru.komiss77.modules.warp;



import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Initiable;
import ru.komiss77.Ostrov;
import ru.komiss77.LocalDB;
import ru.komiss77.modules.DelayTeleport;
import ru.komiss77.utils.LocationUtil;





@Deprecated
public final class WarpManager extends Initiable {
    
    
    @Deprecated
    public static boolean Warp_exist(final String name) {
       return ApiOstrov.getWarpManager().exist(name);
    }


   
    private final HashMap <String, Warp> warps;
    //public final Map <String,Integer> warps_per_group;

    //public static boolean use;
    //public static boolean сonsoleOnlyUse = false;


    public WarpManager() {
        warps = new HashMap<>();
        //warps_per_group = new HashMap<>();
        reload();
    }

    @Override
    public void reload() {
        warps.clear();
        load();
        //warps_per_group.clear();
        
        //use = Cfg.GetCongig().getBoolean("modules.command.warp.use");
        //сonsoleOnlyUse = Cfg.GetCongig().getBoolean("modules.command.warp.сonsoleOnlyUse");
        //Cfg.GetCongig().getConfigurationSection("modules.command.warp.amount_per_group").getKeys(false).stream().forEach((s) -> {
        //    warps_per_group.put ( s, Cfg.GetCongig().getInt("modules.command.warp.amount_per_group."+s) );
        //});
        //if(use) load();
    }


    private void load() {
        if (!LocalDB.useLocalData) return;

        Ostrov.async( () ->{
            
            Statement stmt = null;
            ResultSet rs = null;
            Material mat;
            int create_time;
             
            try {
                stmt = ApiOstrov.getLocalConnection().createStatement(); 
                
                rs = stmt.executeQuery("SHOW COLUMNS FROM `warps` LIKE 'type'");
                if (rs.next()) {
                    stmt.executeUpdate( "ALTER TABLE `warps` DROP `type`;" );
                    stmt.executeUpdate( "ALTER TABLE `warps` ADD `dispalyMat` VARCHAR(32) NOT NULL DEFAULT '' AFTER `name`; " );
                    stmt.executeUpdate( "ALTER TABLE `warps` ADD `system` TINYINT(1) NOT NULL DEFAULT '1' AFTER `loc`; " );
                }
                
                
                
                rs = stmt.executeQuery( "SELECT * FROM `warps` " );

                while (rs.next()) {
                    if (rs.getString("create_time").length()>11) {
                        create_time = (int) (rs.getLong("create_time")/1000);
                    } else {
                        create_time = rs.getInt("create_time");
                    }
                    final Warp warp = new Warp(rs.getString("name"), rs.getString("owner"), create_time);
                    
                    if (!rs.getString("dispalyMat").isEmpty()) {
                        mat = Material.matchMaterial(rs.getString("dispalyMat"));
                        if (mat!=null) warp.dispalyMat = mat;
                    }
                    warp.descr = rs.getString("descr");
                    warp.loc = LocationUtil.LocFromString(rs.getString("loc"), false);
                    warp.system = rs.getBoolean("system");
                    warp.open = rs.getBoolean("open");
                    warp.need_perm = rs.getBoolean("need_perm");
                    warp.use_cost = rs.getInt("use_cost");
                    warp.use_counter = rs.getInt("use_counter");
                    
                    warps.put(warp.warpName, warp);
                }
                
                rs.close();
                stmt.close();
                Ostrov.log_ok("§2Загружено варпов: §l"+warps.size());

            } catch (SQLException e) {
                
                Ostrov.log_err("§4Не удалось загрузить варпы : "+e.getMessage());
                
            } finally {
                
                try {
                    if (rs!=null) rs.close();
                    if (stmt!=null) stmt.close();
                } catch (SQLException ex) {
                    Ostrov.log_err("не удалось закрыть соединение варпы: "+ex.getMessage());
                }
                
            }
            
        }, 0);
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
   

    public Collection<Warp> getWarps() {
        return warps.values();
    }
    
    public boolean exist(final String name) {
        return warps.containsKey(name);
    }

    public Warp getWarp(final String name) {
        return warps.get(name);
    }

    public Set <String> getWarpNames() {
        return warps.keySet();
    }

    
    
    
    
    
    
    
    
    
    
    
    
    
    
    public void tryWarp(final Player p, final String warpName) {
        p.closeInventory();
        final Warp w = warps.get(warpName);
        if (w==null) {
            p.sendMessage("§cМесто "+warpName+" не найдено!");
            return;
        }
        if (w.loc==null || w.loc.getWorld()==null) {
            p.sendMessage("§cЛокация "+warpName+" недоступна!");
            return;
        }
        if ( !w.open && !ApiOstrov.isLocalBuilder(p, false) && !w.isOwner(p) ) {
            p.sendMessage("§cМесто "+warpName+" закрыто!");
            return;
        }
        if (w.need_perm && !ApiOstrov.isLocalBuilder(p, false) && !w.isOwner(p) && !p.hasPermission("warp.use."+warpName)) {
            p.sendMessage("§cДля посещения данного места  требуется право §4warp.use."+warpName);
            return;
        }
        if ( w.isPaid() && !ApiOstrov.isLocalBuilder(p, false) && !w.isOwner(p) ) {
            if (ApiOstrov.moneyGetBalance(p.getName())<w.use_cost) {
                p.sendMessage("§cНедостаточно лони для посещения! Требуется: "+w.use_cost);
                return;
            }
            ApiOstrov.moneyChange(p, -w.use_cost, "Посещение "+warpName);
            if (!w.system) {
                ApiOstrov.moneyChange(w.owner, w.use_cost, "Посещение "+w.warpName);
            }
        }
        if (p.getVehicle()!=null) {
            p.getVehicle().eject();
        }
        if (ApiOstrov.isLocalBuilder(p, false)) {
            p.teleport(w.loc);
            p.sendMessage("§7Телепорт в режиме строителя на "+warpName);
        } else {
            DelayTeleport.tp(p, w.loc, 5, "§6Перемещение на "+warpName+" прошло удачно.", true, true, DyeColor.YELLOW);
        }
        w.use_counter++;
        saveCounter(warpName, w.use_counter);
    }

    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    public void saveCounter(final String warpname, final int count) {
        if (!LocalDB.useLocalData) return;
        Ostrov.async(()-> {
            try ( Statement stmt = ApiOstrov.getLocalConnection().createStatement(); )
            {
               stmt.executeUpdate( "UPDATE `warps` SET `use_counter`= '"+count+"' WHERE `name` = '"+warpname+"'  LIMIT 1" ); 
               stmt.close();
            } catch (SQLException e) { 
                Ostrov.log_err("§4Не удалось добавить счётчик "+warpname+" : "+e.getMessage());
            }
        }, 0);
    }

    public void delWarp(final Player p, final String warpName) {
        warps.remove(warpName);
        Ostrov.async(()-> {
            try ( Statement stmt = ApiOstrov.getLocalConnection().createStatement(); )
            {
               int res = stmt.executeUpdate( "DELETE FROM `warps` WHERE `name` = '"+warpName+"'  LIMIT 1" ); 
               stmt.close();
               
                if (res == 1) {
                    p.sendMessage("§2Варп "+warpName+" удалён!");
                    Ostrov.log_ok("§2Варп "+warpName+" удалён!");
                } else {
                    p.sendMessage("§4Варп "+warpName+" не удалён - запись не найдена");
                }
                            
            } catch (SQLException e) { 
                Ostrov.log_err("§4Не удалось удалить место"+warpName+" : "+e.getMessage());
            }
        }, 0);
    }

    public void changeOpen(final String warpName) {
        final Warp w = warps.get(warpName);
        if (w==null) return;
        w.open = !w.open;
        Ostrov.async(()-> {
            try ( Statement stmt = ApiOstrov.getLocalConnection().createStatement(); )
            {
               stmt.executeUpdate( "UPDATE `warps` SET `open`= '"+(w.open ? 1 : 0)+"' WHERE `name` = '"+warpName+"'  LIMIT 1" ); 
               stmt.close();
            } catch (SQLException e) { 
                Ostrov.log_err("§4Не удалось  счётчик SET open "+warpName+" : "+e.getMessage());
            }
        }, 0);
    }
    
    public void setCost(final Warp w, final int cost) {
        w.use_cost = cost;
        Ostrov.async(()-> {
            try ( Statement stmt = ApiOstrov.getLocalConnection().createStatement(); )
            {
               stmt.executeUpdate( "UPDATE `warps` SET `use_cost`= '"+w.use_cost+"' WHERE `name` = '"+w.warpName+"'  LIMIT 1" ); 
               stmt.close();
            } catch (SQLException e) { 
                Ostrov.log_err("§4Не установить SET `use_cost`  "+w.warpName+" : "+e.getMessage());
            }
        }, 0);
    }

    public void saveWarp(final Player p, final Warp warp) {
        warps.put(warp.warpName, warp);
        Ostrov.async(()-> {
            try (
                PreparedStatement pst = ApiOstrov.getLocalConnection().prepareStatement ( "INSERT INTO `warps` (`name`, `owner`, `loc`, `system`, `create_time` ) VALUES "
                        + "( ?, ?, ?, ?, ? ) " +
                        "ON DUPLICATE KEY UPDATE "
                        + "dispalyMat ='"+warp.dispalyMat +"', "
                        + "descr='"+warp.descr+"', "
                        + "loc=VALUES(loc), "
                        + "need_perm='"+(warp.need_perm?1:0)+"' ")
                )

                {            
                        pst.setString(1, warp.warpName);
                        pst.setString(2, warp.owner );
                        pst.setString(3, LocationUtil.StringFromLocWithYawPitch(warp.loc) );
                        pst.setBoolean(4, warp.system );
                        pst.setInt(5, ApiOstrov.currentTimeSec() );

                pst.executeUpdate();
                pst.close();
                //if (res == 1 ) {
                //    p.sendMessage("§2Данные варпа "+warp.warpName+" сохранены в БД!");
                //    Ostrov.log_ok("§2Данные варпа "+warp.warpName+" сохранены"); 
                //} else {
            //        p.sendMessage("§4Не удалось сохранить данные варпа "+warp.warpName+" - отказ БД!");
                //}

            } catch (SQLException e) { 
                Ostrov.log_err("§4Не удалось сохранить данные варпа "+warp.warpName+" : "+e.getMessage());
            } 
        }, 0);    
    }

   












}
