package ru.komiss77.modules.regions;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.mojang.brigadier.Command;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.LocationFlag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Cfg;
import ru.komiss77.OConfig;
import ru.komiss77.Ostrov;
import ru.komiss77.commands.tools.OCmdBuilder;
import ru.komiss77.modules.player.PM;
import ru.komiss77.modules.regions.menu.TemplateManageMenu;
import ru.komiss77.modules.regions.menu.RegionOwnerMenu;
import ru.komiss77.modules.regions.menu.TemplateEditorMenu;
import ru.komiss77.objects.CaseInsensitiveMap;
import ru.komiss77.utils.inventory.SmartInventory;


public final class RM {

  public static final int NO_CLAIM_AREA = 2000;
  public static OConfig cfg;
  public static CaseInsensitiveMap<Template> templates;
  public static final HashMap<Flag, FlagSetting> flags;
  private static final Set<String> forbiddenFlags;
  private static Listener cmdLst;
  public static HashMap<String, PreviewBlock> on_wiev;
  public static boolean regenOnDelete;

  static {
    cfg = Cfg.manager.getNewConfig("regionGUI.yml", new String[]{"", "Region GUI config file", ""});
    templates = new CaseInsensitiveMap<>();
    flags = new HashMap<>();
    on_wiev = new HashMap<>();
    forbiddenFlags = new HashSet<>();

    cmdLst = new Listener() {
      @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
      public void Command(PlayerCommandPreprocessEvent e) {
        if (
            ((e.getMessage().contains("rg") || e.getMessage().contains("region")) && (e.getMessage().contains("claim") || e.getMessage().contains("define")))
          //e.getMessage().startsWith("//wand")
        ) {
          if (!ApiOstrov.isLocalBuilder(e.getPlayer(), false)) {
            e.setMessage("land");
          }
        }
      }
    };
    cfg.addDefault("regenOnDelete", false);
    cfg.saveConfig();
    regenOnDelete = cfg.getBoolean("settings.regenOnDelete");

    loadTemplates();
    loadFlags();
  }

  public static void onWgHook() {
    new OCmdBuilder("land")
        .run(cntx -> {
          final CommandSender cs = cntx.getSource().getSender();
          if (!(cs instanceof final Player p)) {
            cs.sendMessage("§eНе консольная команда!");
            return 0;
          }
          //RegionUtils.checkForRegions(p);
          PM.getOplayer(p).menu.openRegions(p);
          /*SmartInventory.builder()
              .id("home-" + p.getName())
              .provider(new LandHomeMenu())
              .size(5, 9)
              .title("§fРегионы")
              .build().open(p);*/
          return Command.SINGLE_SUCCESS;
        })
        .description("Управление регионами")
        .register();
    Bukkit.getPluginManager().registerEvents(cmdLst, Ostrov.instance);
  }

  public static Template template(final String templateName) {
    return templates.get(templateName);
  }

  public static void loadFlags() {
    forbiddenFlags.add("allowed-cmds");
    forbiddenFlags.add("blocked-cmds");
    forbiddenFlags.add("send-chat");
    forbiddenFlags.add("invincible");
    forbiddenFlags.add("command-on-entry");
    forbiddenFlags.add("command-on-exit");
    forbiddenFlags.add("console-command-on-entry");
    forbiddenFlags.add("console-command-on-exit");
    forbiddenFlags.add("godmode");
    forbiddenFlags.add("worldedit");
    forbiddenFlags.add("chunk-unload");
    forbiddenFlags.add("passthrough");
    forbiddenFlags.add("price");
    forbiddenFlags.add("receive-chat");
    forbiddenFlags.add("greeting"); //настраивается в гл.меню
    forbiddenFlags.add("farewell"); //настраивается в гл.меню
    forbiddenFlags.add("greeting-title"); //настраивается в гл.меню
    forbiddenFlags.add("farewell-title"); //настраивается в гл.меню

    if (RM.cfg.getConfigurationSection("flags") != null) {
      String path;
      for (String flagName : RM.cfg.getConfigurationSection("flags").getKeys(false)) {
        path = "flags." + flagName + ".";
        final Flag f = WorldGuard.getInstance().getFlagRegistry().get(flagName);
        if (f == null) {
          Ostrov.log_warn("RegionGui : в WG нет флага " + flagName);
          continue;
        }
        flags.put(f, new FlagSetting(f, RM.cfg.getString(path + "displayname"), RM.cfg.getBoolean(path + "enabled")));
      }
    }

    int newFlag = 0;
    for (final Flag<?> f : WorldGuard.getInstance().getFlagRegistry().getAll()) {
      if (flags.containsKey(f) || forbiddenFlags.contains(f.getName()) || (f instanceof LocationFlag)) continue;
      final FlagSetting fs = new FlagSetting(f, "§7" + f.getName(), false);
      final String flagPath = "flags." + f.getName() + ".";
      RM.cfg.set(flagPath + "displayname", fs.displayname);
      RM.cfg.set(flagPath + "iconMat", fs.iconMat == null ? "none" : fs.iconMat.name());
      RM.cfg.set(flagPath + "enabled", fs.enabled);
      newFlag++;
      flags.put(f, fs);
    }
    if (newFlag > 0) {
      log_ok("§eВ конфигурацию флагов добавлено записей: " + newFlag);
      RM.cfg.saveConfig();
    }
  }


  public static void loadTemplates() {
    if (cfg.getConfigurationSection("templates") != null) {
      String path;
      for (String templateName : cfg.getConfigurationSection("templates").getKeys(false)) {
        path = "templates." + templateName + ".";
        final Template t = new Template(templateName);
        t.allowedWorlds = (List<String>) cfg.getList(path + "allowedWorlds");
        t.displayname = cfg.getString(path + "displayname");
        t.iconMat = Material.matchMaterial(cfg.getString(path + "iconMat"));
        if (t.iconMat == null) t.iconMat = Material.BEDROCK;
        t.description = (List<String>) cfg.getList(path + "description");
        t.size = cfg.getInt(path + "size");
        t.height = cfg.getInt(path + "height");
        t.depth = cfg.getInt(path + "depth");
        t.price = cfg.getInt(path + "price");
        t.refund = cfg.getInt(path + "refund");
        t.borderMaterial = Material.matchMaterial(cfg.getString(path + "borderMaterial"));
        //if (t.borderMaterial == null) t.borderMaterial = Material.OAK_FENCE;
        t.permission = cfg.getBoolean(path + "permission");
        templates.put(templateName, t);
      }
    }
  }

  public static void saveTemplate(final Template t) {
    final String path = "templates." + t.name + ".";
    cfg.set(path + "allowedWorlds", t.allowedWorlds);
    cfg.set(path + "displayname", t.displayname);
    cfg.set(path + "iconMat", t.iconMat.name());
    cfg.set(path + "description", t.description);
    cfg.set(path + "size", t.size);
    cfg.set(path + "height", t.height);
    cfg.set(path + "depth", t.depth);
    cfg.set(path + "price", t.price);
    cfg.set(path + "refund", t.refund);
    cfg.set(path + "borderMaterial", t.borderMaterial == null ? "none" : t.borderMaterial.name());
    cfg.set(path + "permission", t.permission);
    cfg.saveConfig();
  }

  public static void delTemplate(final Template t) {
    templates.remove(t.name);
    cfg.removeKey("templates." + t.name);
    cfg.saveConfig();
  }


  public static void startPreview(final Player player, final Template template) {
    stopPrewiev(player);
    on_wiev.put(player.getName(), new PreviewBlock(player, template));
  }

  public static void stopPrewiev(final Player player) {
    final PreviewBlock pb = on_wiev.remove(player.getName());
    if (pb != null) {
      pb.stop(player, false); //из on_wiev удаляет там
      //PreviewBlockManager.on_wiev.remove(player.getName());
    }
  }


  public static void openRegionOwnerMenu(final Player player, final ProtectedRegion region) {
    SmartInventory.builder()
        .provider(new RegionOwnerMenu(region))
        .size(4)
        .title("Управление регионами")
        .build().open(player);
  }

  public static void openTemplateAdmin(final Player p) {
    SmartInventory.builder()
        .provider(new TemplateManageMenu())
        .size(6)
        .title("§8Редактор заготовок [" + p.getWorld().getName() + "]")
        .build().open(p);
  }

  public static void editTemplate(final Player p, final Template t) {
    SmartInventory.builder()
        .provider(new TemplateEditorMenu(t))
        .size(3).title("Редактирование " + t.name)
        .build().open(p);
  }

  public static String templateName(final ProtectedRegion region) {
    final String[] split = region.getId().split("-");
    if (split.length == 4 && split[1].equals("rgui") && ApiOstrov.isInteger(split[3])) {
      return split[2];
    }
    return "";
  }

  public static String createTime(final ProtectedRegion region) {
    final String[] split = region.getId().split("-");
    if (split.length == 4 && split[1].equals("rgui") && ApiOstrov.isInteger(split[3])) {
      return split[3];
    }
    return "§8нет данных";
  }

  public static void log_ok(String s) {
    Bukkit.getConsoleSender().sendMessage("§2[§fРегионы§2] §7:§2 " + s);
  }

  public static void log_warn(String s) {
    Bukkit.getConsoleSender().sendMessage("§2[§fРегионы§2] §7:§6 " + s);
  }

  public static void log_err(String s) {
    Bukkit.getConsoleSender().sendMessage("§2[§fРегионы§2] §7:§c " + s);
  }


}
