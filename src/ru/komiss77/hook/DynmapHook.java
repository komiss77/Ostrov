package ru.komiss77.hook;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.dynmap.DynmapCore;
import org.dynmap.DynmapWorld;
import org.dynmap.MapManager;
import org.dynmap.MarkersComponent;
import org.dynmap.bukkit.DynmapPlugin;
import org.dynmap.storage.MapStorage;
import org.dynmap.storage.MySQLMapStorage;
import ru.komiss77.Ostrov;
import ru.komiss77.enums.Game;
import ru.komiss77.modules.games.GM;


//https://github.com/webbukkit/dynmap
//https://www.spigotmc.org/resources/dynmap.274/
//https://www.spigotmc.org/resources/liveatlas-alternative-map-ui-dynmap-pl3xmap-squaremap.86939/
//https://github.com/webbukkit/dynmap/wiki/Dynmap-with-Nginx

// TODO
// показать спавн
// на островке зум карты побольше
// название мира показывать как ник
// убрать пещерный вид

public class DynmapHook {

  //private static final Set<String> purged;
  public static final NamespacedKey MAP = new NamespacedKey(Ostrov.instance, "dynmap");

    static {
      //purged = new HashSet<>();
    }

  //из PluginEnableEvent - основные миры уже подгружены
    public static void hook(final Plugin plugin) {

        Ostrov.dynmap = true;

      switch (GM.GAME) {
        case MI, AR, OB, SK -> {
          for (World w : Bukkit.getWorlds()) {
            onWorldLoad(w);
          }
        }
        default -> {
          if (Ostrov.MOT_D.equals("home")) {
            for (World w : Bukkit.getWorlds()) {
              onWorldLoad(w);
            }
          }
        }
      }



        Ostrov.log_ok("§bНайден и пропатчен Dynmap!");

    }

  //PluginEnableEvent - hook
  //ServerLst-onWorldLoaded
  public static void onWorldLoad(World w) {
    switch (GM.GAME) {
      case Game.MI -> show(w, w.getName());
      case AR, OB, SK -> {
        if (w.getName().equals("world")) {
          show(w, w.getName());
        }
      }
      default -> {
        if (Ostrov.MOT_D.equals("home")) {
          show(w, w.getName());
        }
      }
    }
    }

  public static void show(final World w, final String displayName) {
//Ostrov.log_ok(" ========= showWorld "+world.name());
    w.getPersistentDataContainer().set(MAP, PersistentDataType.BOOLEAN, true);

    DynmapWorld dw = DynmapPlugin.dw(w);
    if (dw == null) {
      dw = DynmapPlugin.addWorld(w);//new DynmapWorld(w);
      //DynmapPlugin.world_by_name.put(w.getName(), dw);
      //DynmapPlugin.world_by_name.put(dw.dynmapName(), dw);
      dw.showborder = false;
      dw.setTitle(displayName);
      if (!w.getName().equals(displayName)) { //так будет на островах
        dw.setExtraZoomOutLevels(4);
      }
      DynmapCore.updateConfigHashcode();
      DynmapCore.mapManager.activateWorld(dw);
      MarkersComponent.addUpdateWorld(dw, dw.getSpawnLocation());
    } else {
      DynmapCore.mapManager.loadWorld(dw);
    }
    //dw.
    Ostrov.log_warn("dynmap show " + w.getName());

    }


    public static void purge(final String worldName) {
        //если мир не прошкл по WorldUnloadEvent, динмап будет снова писать файлы!
        //чистить только когда мир выгружен!

      //   if (core == null) {
      //      return;
      //   }
      final World w = Bukkit.getWorld(worldName);
      if (w == null) {
        Ostrov.log_warn("dynmap purge " + worldName + " : не загружен!");
            return;
      }
      if (!w.getPersistentDataContainer().has(MAP)) {
        Ostrov.log_warn("dynmap purge - мир никогда не показывался");
        return;
      }
      w.getPersistentDataContainer().remove(MAP);

      DynmapWorld dw = DynmapPlugin.removeWorld(w);
      if (dw == null) {
        Ostrov.log_warn("dynmap purge - DynmapWorld = null");
        return;
      }

      MapManager.cancelRender(dw.dynmapName, Bukkit.getConsoleSender());
      MapManager.purgeQueue(Bukkit.getConsoleSender(), dw.dynmapName);

      if (dw.storage instanceof final MySQLMapStorage sql) {
        Ostrov.async(() -> {
          //Ostrov.log_warn(" ========= purge "+worldName);
          boolean err = false;
          Connection c = null;
          try {
            c = sql.getConnection();
            Statement stmt = c.createStatement();
            final Set<Integer> ids = new HashSet<>();
            ResultSet rs = stmt.executeQuery("SELECT `id` FROM `Maps` WHERE `WorldID`='" + dw.dynmapName + "';");
            while (rs.next()) {
              ids.add(rs.getInt("id"));
            }
            rs.close();


            if (ids.isEmpty()) {
              Ostrov.log_ok("dynmap purge " + worldName + " : записей в Maps не найдено");
              return;
            }

            stmt.executeUpdate("DELETE FROM `StandaloneFiles` WHERE `FileName`='dynmap_" + dw.dynmapName + ".json' ;");
            stmt.executeUpdate("DELETE FROM `MarkerFiles` WHERE `FileName`='" + dw.dynmapName + "' ;");
            stmt.executeUpdate("DELETE FROM `Maps` WHERE `WorldID`='" + dw.dynmapName + "' ;");

            for (int id : ids) {
              stmt.executeUpdate("DELETE FROM `Tiles` WHERE `MapID`='" + id + "' ;");
            }

            Ostrov.log_ok("dynmap purge " + worldName + " : удалено " + ids.size() + " карт");

            //stmt.close();

          } catch (SQLException ex) {
            Ostrov.log_err("purge " + worldName + " : " + ex.getMessage());
            err = true;
          } catch (MapStorage.StorageShutdownException e) {
            throw new RuntimeException(e);
          } finally {
            if (c != null) sql.releaseConnection(c, err);
            //purged.remove(worldName);
          }
        }, 0);
      }

     /*    if (getConnection == null || releaseConnection == null) {
            Ostrov.log_warn("DynmapHook hook : хранилище НЕ MySql!");
            return;
        }

        if (Bukkit.getWorld(worldName) != null) {
            Ostrov.log_warn("dynmap purge " + worldName + " : сначала надо выгрузить мир!");
            return;
        }

        final MySQLMapStorage sql = (MySQLMapStorage) core.getDefaultMapStorage();
        final Connection c;
        try {
            c = (Connection) getConnection.invoke(sql);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            Ostrov.log_warn("dynmap purge getConnection : " + ex.getMessage());
            return;
        }

        if (purged.contains(worldName)) {
            Ostrov.log_warn("dynmap purge " + worldName + " : уже отправлено на удаление!");
            return;
        }
        purged.add(worldName);

        Ostrov.async(() -> {
            MapStorage storage = core.getDefaultMapStorage();
            //Ostrov.log_warn(" ========= purge "+worldName);
            if (storage instanceof MySQLMapStorage) {
                //MySQLMapStorage sql = (MySQLMapStorage) storage;
                //Connection c = null;
                boolean err = false;
                try (Statement stmt = c.createStatement()) {
                    //c = sql.getConnection();
                    //Statement stmt = c.createStatement();
                    final Set<Integer> ids = new HashSet<>();
                    ResultSet rs = stmt.executeQuery("SELECT `id` FROM `Maps` WHERE `WorldID`='" + worldName + "';");
                    while (rs.next()) {
                        ids.add(rs.getInt("id"));
                    }
                    rs.close();


                    if (ids.isEmpty()) {
                        Ostrov.log_ok("dynmap purge " + worldName + " : записей в Maps не найдено");
                        return;
                    }

                    stmt.executeUpdate("DELETE FROM `StandaloneFiles` WHERE `FileName`='dynmap_" + worldName + ".json' ;");
                    stmt.executeUpdate("DELETE FROM `MarkerFiles` WHERE `FileName`='" + worldName + "' ;");
                    stmt.executeUpdate("DELETE FROM `Maps` WHERE `WorldID`='" + worldName + "' ;");

                    for (int id : ids) {
                        stmt.executeUpdate("DELETE FROM `Tiles` WHERE `MapID`='" + id + "' ;");
                    }

                    Ostrov.log_ok("dynmap purge " + worldName + " : удалено " + ids.size() + " карт");

                    //stmt.close();

                } catch (SQLException ex) {
                    Ostrov.log_err("purge " + worldName + " : " + ex.getMessage());
                    err = true;
                } finally {
                    try {
                        releaseConnection.invoke(sql, c, err);
                    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                        Ostrov.log_warn("dynmap purge getConnection : " + ex.getMessage());
                    }
                    //sql.releaseConnection(c, err);
                    purged.remove(worldName);
                }
            }
        }, 0);*/
    }


    
    
 /*   public static void purge(final String worldname) {
Ostrov.log_warn(" ========= purge "+worldname);
        dp.getMapManager().cancelRender(worldname, sender);
        // And purge update queue for world
        dp.getMapManager().purgeQueue(sender, worldname);
        
        Runnable purgejob = new Runnable() {
            public void run() {
                world.purgeTree();
                sender.sendMessage("Purge of files for world '" + worldname + "' completed");
            }
        };
        /* Schedule first tile to be worked /
        scheduleDelayedJob(purgejob, 0);

    }*/


}


//  private static DynmapPlugin dp;
//  private static DynmapCore core;
//private static Method getWorld;
//private static Method getConnection;
//private static Method releaseConnection;


//static final DynmapAPI api = (DynmapAPI)Bukkit.getPluginManager().getPlugin("dynmap"); ;
//static final MarkerAPI markerapi = api.getMarkerAPI();
//static final MarkerSet set =  markerapi.createMarkerSet("factions.markerset", "Кланы", null, false);
//static final MarkerIcon baseIcon = markerapi.getMarkerIcon("tower");

  /*      dp = (DynmapPlugin) plugin;
        try {
            final Field coreField = dp.getClass().getDeclaredField("core");
            coreField.setAccessible(true);
            core = (DynmapCore) coreField.get(dp);
//Ostrov.log("DynmapHook core = "+core);
            getWorld = dp.getClass().getDeclaredMethod("getWorld", World.class);

            final MapStorage storage = core.getDefaultMapStorage();
            if (storage instanceof final MySQLMapStorage sql) {
                getConnection = sql.getClass().getDeclaredMethod("getConnection");
                getConnection.setAccessible(true);
                releaseConnection = sql.getClass().getDeclaredMethod("releaseConnection", java.sql.Connection.class, boolean.class);
                releaseConnection.setAccessible(true);
            } else {
                Ostrov.log_warn("DynmapHook : хранилище НЕ MySql!");
                return;
            }

        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException |
                 NoSuchMethodException ex) {
            Ostrov.log_err("DynmapHook hook : " + ex.getMessage());
            ex.printStackTrace();
            core = null;
            return;
        }

        core.playerfacemgr = null;
        core.skinUrlProvider = null;

        RegisteredListener rl;
        final Iterator<RegisteredListener> it = HandlerList.getRegisteredListeners(dp).iterator();
        while (it.hasNext()) {
            rl = it.next();

            boolean disable = true;

            final Listener lst = rl.getListener();
//Ostrov.log("RegisteredListener="+lst.getClass().name()+" priority="+rl.getPriority());

            Method[] methods = lst.getClass().getDeclaredMethods();
            for (Method m : methods) {
//Ostrov.log("Method="+m.name());
                switch (m.getName()) {
                    case "onPlayerJoin",
                         "onPlayerQuit",
                         //"onWorldUnload", - делаем отдельный эвент
                         "onBlockPlace",
                         "onBlockBreak",
                         "onChunkPopulate",
                         "onPluginEnabled" -> disable = false;
                }
            }

            if (disable) {
                HandlerList.unregisterAll(lst);
//Ostrov.log("------- выкл");
            }
        }

        Listener worldTrigger = new Listener() {
            // @EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
            // public void onWorldLoad(WorldLoadEvent event) {
            //      BukkitWorld w = getWorld(event.getWorld());
            //     if(core.processWorldLoad(w))    /* Have core process load first - fire event listeners if good load after
            //         core.listenerManager.processWorldEvent(EventType.WORLD_LOAD, w);
            //  }
            @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
            public void onWorldUnload(final WorldUnloadEvent event) {
                final BukkitWorld w = getWorld(event.getWorld());
                if (w != null) {
                    core.listenerManager.processWorldEvent(EventType.WORLD_UNLOAD, w);
                    w.setWorldUnloaded();
                    core.processWorldUnload(w);
                }
            }

        };
        Bukkit.getPluginManager().registerEvents(worldTrigger, Ostrov.instance);
*/

//for (RegisteredListener rl : HandlerList.getRegisteredListeners(dp)) {
//for (int i = 0; i < hl.getRegisteredListeners().length; i++) {
//for (RegisteredListener rl : HandlerList.getRegisteredListeners(dynmap)) {
//for (RegisteredListener rl : WorldLoadEvent.getHandlerList().getRegisteredListeners(dynmap)) {
//RegisteredListener rl = hl.getRegisteredListeners()[i];
//Ostrov.log_warn("rl="+rl.getListener().getClass().name()+" plugin="+rl.getPlugin().name()+" priority="+rl.getPriority());
//   if (rl.getPlugin().name().equals("dynmap")) {
//Ostrov.log_warn("------");
//if (rl.getListener() instanceof )
// }
// }
//}
//WorldLoadEvent.getHandlerList().




    /*  public static void createMaps() {
  //Ostrov.log_warn(" ------------- updateMaps ");
          Ostrov.async( ()-> {
              DynmapFactions.updateFactions();
          }, 5*20);
      }

      public static void updateBaseIcon(final Faction f) {
          Ostrov.async( ()-> {
              DynmapFactions.drawFactionBaseIcon(f, DynmapFactions.getDescription(f));
          }, 1);
      }

      public static void updateFactionArea(final Faction f) {
          Ostrov.async( ()-> {
              DynmapFactions.drawFactionArea(f, DynmapFactions.getDescription(f));
          }, 1);
      }

      public static void wipe(final int factionID) {
          Ostrov.async( ()-> {
              DynmapFactions.wipe(factionID);
          }, 1);
      }
  */


// public MapManager getMapManager() {
//    return core == null ? null : core.mapManager;
// }


   /* public static BukkitWorld getWorld(final World w) {
        try {
            return (BukkitWorld) getWorld.invoke(dp, w); //dp.getWorld(world);
        } catch (NullPointerException | IllegalAccessException | IllegalArgumentException |
                 InvocationTargetException ex) {
            Ostrov.log_warn("DynmapHook getWorld : " + ex.getMessage());
            return null;
        }
    }*/