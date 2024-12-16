package ru.komiss77.modules.regions;

import java.util.*;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.domains.PlayerDomain;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.LocationFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Cfg;
import ru.komiss77.OConfig;
import ru.komiss77.Ostrov;
import ru.komiss77.hook.WGhook;
import ru.komiss77.modules.regions.menu.FlagsSetupMenu;
import ru.komiss77.modules.regions.menu.RegionOwnerMenu;
import ru.komiss77.modules.regions.menu.TemplateEditorMenu;
import ru.komiss77.modules.regions.menu.TemplateSetupMenu;
import ru.komiss77.modules.world.WE;
import ru.komiss77.objects.CaseInsensitiveMap;
import ru.komiss77.utils.NumUtil;
import ru.komiss77.utils.TCUtil;
import ru.komiss77.utils.inventory.SmartInventory;


public final class RM {

  public static final int NO_CLAIM_AREA = 2000;
  public static OConfig cfg;
  public static CaseInsensitiveMap<Template> templates;
  public static final HashMap<Flag, FlagSetting> flags;
  private static final Set<String> forbiddenFlags;
  //private static Listener cmdLst;
  public static HashMap<String, PreviewTask> on_wiev;
  public static boolean regenOnDelete;

  static {
    cfg = Cfg.manager.getNewConfig("regionGUI.yml", new String[]{"", "Region GUI config file", ""});
    templates = new CaseInsensitiveMap<>();
    flags = new HashMap<>();
    on_wiev = new HashMap<>();
    forbiddenFlags = new HashSet<>();

   /* cmdLst = new Listener() {
      @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
      public void Command(PlayerCommandPreprocessEvent e) {
        if (ApiOstrov.isLocalBuilder(e.getPlayer(), false)) {
          return;
        }
        if ( e.getMessage().startsWith("/rg") || e.getMessage().startsWith("/region") ) {
          if (e.getMessage().contains("claim") || e.getMessage().contains("define") ) {
            //final Player p = e.getPlayer();
            //e.setCancelled(true);
            //PM.getOplayer(p).menu.openRegions(p);
            e.setMessage("land");
          }
        }
      }
    };*/

    if (!loadFlags()) {
      cfg.addDefault("regenOnDelete", false);
      cfg.saveConfig();
    }

    regenOnDelete = cfg.getBoolean("regenOnDelete");

    loadTemplates();

  }

  public static void onWgHook() {
   /* new OCmdBuilder("land")
        .run(cntx -> {
          final CommandSender cs = cntx.getSource().getSender();
          if (!(cs instanceof final Player p)) {
            cs.sendMessage("§eНе консольная команда!");
            return 0;
          }
          //RegionUtils.checkForRegions(p);
          PM.getOplayer(p).menu.openRegions(p);
          return Command.SINGLE_SUCCESS;
        })
        .description("Управление регионами")
        .register();*/
    //Bukkit.getPluginManager().registerEvents(cmdLst, Ostrov.instance);
  }

  public static Template template(final String templateName) {
    return templates.get(templateName);
  }

  public static boolean loadFlags() {
    boolean ok = true;

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
    forbiddenFlags.add("dynmap-boost");

    if (cfg.getConfigurationSection("flags") != null) {
      String path;
      for (String flagName : cfg.getConfigurationSection("flags").getKeys(false)) {
        path = "flags." + flagName + ".";
        final Flag f = WorldGuard.getInstance().getFlagRegistry().get(flagName);
        if (f == null) {
          Ostrov.log_warn("RegionGui : в WG нет флага " + flagName);
          continue;
        }
        flags.put(f, new FlagSetting(f, TCUtil.translateAlternateColorCodes('&', cfg.getString(path + "displayname")),
            Material.matchMaterial(cfg.getString(path + "iconMat")), cfg.getBoolean(path + "enabled"))
        );
      }
    } else {
      ok = false;
    }
    int newFlag = 0;
    for (final Flag<?> f : WorldGuard.getInstance().getFlagRegistry().getAll()) {
      if (flags.containsKey(f) || forbiddenFlags.contains(f.getName()) || (f instanceof LocationFlag)) continue;
      final FlagSetting fs = new FlagSetting(f, "§7" + f.getName(), null, false);
      final String flagPath = "flags." + f.getName() + ".";
      cfg.set(flagPath + "displayname", TCUtil.translateAlternateColorCodes('§', fs.displayname));
      cfg.set(flagPath + "iconMat", "none");
      cfg.set(flagPath + "enabled", fs.enabled);
      flags.put(f, fs);
      newFlag++;
    }
    if (ok && newFlag > 0) {
      log_ok("§eВ конфигурацию флагов добавлено записей: " + newFlag);
      cfg.saveConfig();
    }
    return ok;
  }

  public static void saveFlag(final Flag f) {
    final FlagSetting fs = flags.get(f);
    final String flagPath = "flags." + f.getName() + ".";
    cfg.set(flagPath + "displayname", TCUtil.translateAlternateColorCodes('§', fs.displayname));
    cfg.set(flagPath + "iconMat", fs.iconMat == null ? "none" : fs.iconMat.name());
    cfg.set(flagPath + "enabled", fs.enabled);
    cfg.saveConfig();
  }


  public static void loadTemplates() {
    if (cfg.getConfigurationSection("templates") != null) {
      String path;
      for (String templateName : cfg.getConfigurationSection("templates").getKeys(false)) {
        path = "templates." + templateName + ".";
        final Template t = new Template(templateName);
        t.allowedWorlds = (List<String>) cfg.getList(path + "allowedWorlds");
        t.displayname = TCUtil.translateAlternateColorCodes('&', cfg.getString(path + "displayname"));
        t.iconMat = Material.matchMaterial(cfg.getString(path + "iconMat"));
        if (t.iconMat == null) t.iconMat = Material.BEDROCK;
        t.description.clear();
        for (String s : (List<String>) cfg.getList(path + "description")) {
          t.description.add(TCUtil.translateAlternateColorCodes('&', s));
        }
        //t.description = (List<String>) cfg.getList(path + "description");
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
    cfg.set(path + "displayname", TCUtil.translateAlternateColorCodes('§', t.displayname));
    cfg.set(path + "iconMat", t.iconMat.name());
    final List<String> list = new ArrayList<>();
    for (String s : t.description) {
      list.add(TCUtil.translateAlternateColorCodes('§', s));
    }
    cfg.set(path + "description", list);
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
    on_wiev.put(player.getName(), new PreviewTask(player, template));
  }

  public static void stopPrewiev(final Player player) {
    final PreviewTask pb = on_wiev.remove(player.getName());
    if (pb != null) {
      pb.stop(player, false); //из on_wiev удаляет там
      //PreviewBlockManager.on_wiev.remove(player.getName());
    }
  }


  public static void buy(final Player p, final Template t) {
    final Location loc = p.getLocation();
    RM.stopPrewiev(p);
    int blockY = loc.getBlockY();
    if (blockY > 30) blockY = 30;

    if (t.permission && !p.hasPermission(Template.PERM_FOR_ALl) && !p.hasPermission(t.permission())) {
      p.sendMessage("§6Нет права " + t.permission());
      return;
    }
    final int price = ApiOstrov.isLocalBuilder(p) ? 0 : t.price;
    if (ApiOstrov.moneyGetBalance(p.getName()) < price) {
      p.sendMessage("§6Недостаточно лони для покупки региона!");
      return;
    }

    final org.bukkit.util.Vector top = t.getMaximumPoint(loc).toVector();//new Vector(down.getBlockX() + size, blockY + claimTemplate.getHeight(), down.getBlockZ() + size); //на 1 меньше, т.к. включая
    final Vector down = t.getMinimumPoint(loc).toVector();//new Vector(blockX - halfSize, blockY - claimTemplate.getDepth(), blockZ - halfSize); //находим нижний угол

    final String regName = p.getName() + "-rgui-" + t.name + "-" + ApiOstrov.currentTimeSec();

    final ProtectedCuboidRegion region = new ProtectedCuboidRegion(regName,
        BlockVector3.at(top.getBlockX(), top.getBlockY(), top.getBlockZ()),
        BlockVector3.at(down.getBlockX(), down.getBlockY(), down.getBlockZ())
    );

    final LocalPlayer lp = WGhook.inst.wrapPlayer(p);
    final RegionManager manager = WGhook.getRegionManager(p.getWorld());
    if (manager.overlapsUnownedRegion(region, lp)) {// && this.plugin.getConfig().getBoolean("preventRegionOverlap", true)) {
      p.sendMessage("§6Ругион перекрывается с другим!");
      return;
    }

    //добавление владельца
    final DefaultDomain owners = region.getOwners();
    final PlayerDomain playerDomain = owners.getPlayerDomain();
    playerDomain.addPlayer(lp);//playerDomain.addPlayer(p.getName());
    owners.setPlayerDomain(playerDomain);
    region.setOwners(owners);
    //region.setFlag((Flag)Flags.TELE_LOC, (Object)BukkitAdapter.adapt(p.getLocation()));
    region.setDirty(true);
    manager.addRegion(region);

    ApiOstrov.moneyChange(p.getName(), -price, "Покупка региона " + t.displayname);
    //p.sendMessage("§aРегион успешно создан!");

    if (RM.regenOnDelete) {
      WE.save(Bukkit.getConsoleSender(), BukkitAdapter.adapt(p.getWorld(), region.getMinimumPoint()), BukkitAdapter.adapt(p.getWorld(), region.getMaximumPoint()), regName.toLowerCase(), "");
    }

    if (t.borderMaterial != null) {
      final World world = p.getWorld();
      new BukkitRunnable() {
        @Override
        public void run() {
          WallTask walls = new WallTask(world, t.borderMaterial, down.toLocation(world), t.size);
        }
      }.runTaskLater(Ostrov.getInstance(), 30);
    }

    p.sendMessage("§aВы создали регион, ваши постройки в нём защищены. Вы можете настроить регион через меню.");

  }


  public static void switchRegen(Player p) {
    regenOnDelete = !regenOnDelete;
    cfg.set("regenOnDelete", regenOnDelete);
    cfg.saveConfig();
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
        .provider(new TemplateSetupMenu())
        .size(6)
        .title("§8Редактор заготовок [" + p.getWorld().getName() + "]")
        .build().open(p);
  }

  public static void openFlagAdmin(Player p) {
    SmartInventory.builder()
        .provider(new FlagsSetupMenu())
        .size(6)
        .title("§8Редактор флагов")
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
    if (split.length == 4 && split[1].equals("rgui") && NumUtil.isInt(split[3])) {
      return split[2];
    }
    return "";
  }

  public static String createTime(final ProtectedRegion region) {
    final String[] split = region.getId().split("-");
    if (split.length == 4 && split[1].equals("rgui") && NumUtil.isInt(split[3])) {
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
