package ru.komiss77.modules.regions.menu;

import java.util.ArrayList;
import java.util.List;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import ru.komiss77.ApiOstrov;
import ru.komiss77.enums.Game;
import ru.komiss77.hook.WGhook;
import ru.komiss77.modules.DelayTeleport;
import ru.komiss77.modules.games.GM;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;
import ru.komiss77.modules.player.profile.Section;
import ru.komiss77.modules.regions.RM;
import ru.komiss77.modules.regions.Template;
import ru.komiss77.modules.world.Cuboid;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.ItemUtil;
import ru.komiss77.utils.inventory.*;


public class RegionMenu implements InventoryProvider {


  private final Oplayer op;
  private static final ClickableItem fill = ClickableItem.empty(new ItemBuilder(Section.ВОЗМОЖНОСТИ.glassMat).name("§8.").build());


  public RegionMenu(final Oplayer op) {
    this.op = op;
  }

  @Override
  public void init(final Player p, final InventoryContent content) {
    p.playSound(p.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 5, 5);

    //линия - разделитель
    content.fillRow(4, fill);

    //выставить иконки внизу
    for (Section section : Section.values()) {
      content.set(section.slot, Section.getMenuItem(section, op));
    }

    final Pagination pagination = content.pagination();
    final ArrayList<ClickableItem> menuEntry = new ArrayList<>();


    final List<ProtectedRegion> owned = new ArrayList<>();
    //final List<ProtectedRegion> member = new ArrayList<>();
    final LocalPlayer lp = WorldGuardPlugin.inst().wrapPlayer(p);

    int rgCount = 1;
    Template t;
    String createTime;

    for (final World world : Bukkit.getWorlds()) {

      for (final ProtectedRegion rg : WGhook.getRegionManager(world).getRegions().values()) {

        if (rg.isOwner(lp)) {// || rg.getId().startsWith(start)) {

          owned.add(rg);
          t = RM.template(RM.templateName(rg));
          createTime = RM.createTime(rg);

          menuEntry.add(ClickableItem.of(new ItemBuilder(Material.GRAY_BED)
              .name("§7Регион §6" + rgCount)
              .lore("§fВы владелец.")
              .lore("")
              .lore("§7Тип региона: " + (t == null ? "не определён" : t.displayname))
              .lore("§7Создан: §6" + (createTime.isEmpty() ? "§8нет данных" : createTime))
              .lore("§7Пользователей" + (rg.getMembers().getPlayerDomain().size() == 0 ? " нет" : ": " + rg.getMembers().getPlayerDomain().size()))
              //.lore("")
              //.lore("§7Примерная локация региона:")
              //.lore("§6"+rg.getMaximumPoint().x()+", "+rg.getMaximumPoint().y()+", "+rg.getMaximumPoint().z())
              .lore("")
              .lore("§fЛКМ §7- телепорт в регион")
              .lore("§fПКМ §7- управлять регионом")
              .lore("")
              //.lore(regionButton)
              .build(), e -> {
            if (e.getClick() == ClickType.LEFT) {
              regionTp(p, world, rg);
            } else if (e.getClick() == ClickType.RIGHT) {
              RM.openRegionOwnerMenu(p, rg);
            }
          }));

        } else if (rg.isMember(lp)) {

          //member.add(rg);
          t = RM.template(RM.templateName(rg));
          createTime = RM.createTime(rg);

          menuEntry.add(ClickableItem.of(new ItemBuilder(Material.GRAY_BED)
              .name("§7Регион §6" + rgCount)
              .lore("§fВы пользователь.")
              .lore("")
              //.lore("§7Тип региона: "+(t==null ? "не определён" : t.displayname))
              //.lore("§7Создан: §6"+(createTime.isEmpty()?"§8нет данных":createTime))
              //.lore ("§7Пользователей"+(rg.getMembers().getPlayerDomain().size()==0 ? " нет" : ": "+rg.getMembers().getPlayerDomain().size()))
              //.lore("")
              //.lore(rg.getFlag((Flag) Flags.TELE_LOC) != null ? "ЛКМ - телепорт в регион" : "")
              //.lore("")
              .lore("§7Примерная локация региона:")
              .lore(rg.getMaximumPoint().x() + "," + rg.getMaximumPoint().y() + "," + rg.getMaximumPoint().z())
              .lore("§fЛКМ §7- телепорт в регион")
              //.lore(regionButton)
              .build(), e -> {
            regionTp(p, world, rg);
          }));

          rgCount++;

        }
      }

    }


    //if (rgCount==1) {
    //    content.add( ClickableItem.empty( new ItemBuilder(Material.BARRIER).name("§4Нет регионов!").lore( ItemUtil.genLore(null, "Не найдено ни одного вашего региона в каком-либо мире!", "§c") ).build() ) );
    //}


    int limit;//


    if (ApiOstrov.isLocalBuilder(p)) {
      limit = 77;
    } else {
      limit = ApiOstrov.getLimit(op, "region.total");//PM.getBigestPermValue(op, "region.limit.total");
      if (limit < 1) limit = 1; //один приват всегда можно, раз уж есть плагин на сервере!
    }

    final List<String> problems = new ArrayList<>();

    final BlockVector3 bv = BukkitAdapter.asBlockVector(p.getLocation());
    final RegionManager rm = WGhook.getRegionManager(p.getWorld());

    if (owned.size() >= limit) {
      problems.add("§6Лимит регионов: §e" + limit + "§7, §6уже создано: §5" + owned.size());
      problems.add("");
    }

    if (GM.GAME == Game.DA) {
      final Location spl = p.getWorld().getSpawnLocation();
      final int dst = Math.max(Math.abs(bv.x() - spl.getBlockX()), Math.abs(bv.y() - spl.getBlockZ()));
      if (dst > RM.NO_CLAIM_AREA && !ApiOstrov.isLocalBuilder(p, true)) {
        problems.add("§cРегион можно создать в §6" + RM.NO_CLAIM_AREA);
        problems.add(" §cблоках от спавна. §7(сейчас:§6" + dst + "§7");
        //problems.add("Твоя дистанция - §6" + dst + " §cблоков.");
        problems.add("");
      }
    }

    final ApplicableRegionSet rgSet = rm.getApplicableRegions(bv);
    if (rgSet.size() > 0) {
      problems.add(" §cВы находитесь в каком-то регионе.");
      problems.add("");
    }


    if (problems.isEmpty()) {
      menuEntry.add(ClickableItem.of(new ItemBuilder(Material.TRIAL_KEY)
              .name("§aСоздать регион")
              .lore("")
              .build(), e -> {
            SmartInventory.builder()
                .id("regiongui.claim")
                .provider(new LandBuyMenu(owned))
                .size(5, 9)
                .title("§fПокупка региона")
                .build()
                .open(p);
          }
      ));
    } else {
      menuEntry.add(ClickableItem.empty(new ItemBuilder(Material.TRIAL_KEY)
          .name("§c§mСоздать регион")
          .lore("")
          .lore("§6Вы не можете добавть")
          .lore("§6новые регионы:")
          .lore("")
          .lore(problems)
          .build()
      ));
    }


    pagination.setItems(menuEntry.toArray(new ClickableItem[menuEntry.size()]));
    pagination.setItemsPerPage(36);


    if (!pagination.isLast()) {
      content.set(4, 8, ClickableItem.of(ItemUtil.nextPage, e
              -> {
            content.getHost().open(p, pagination.next().getPage());
          }
      ));
    }

    if (!pagination.isFirst()) {
      content.set(4, 0, ClickableItem.of(ItemUtil.previosPage, e
              -> {
            content.getHost().open(p, pagination.previous().getPage());
          })
      );
    }

    pagination.addToIterator(content.newIterator(SlotIterator.Type.HORIZONTAL, SlotPos.of(0, 0)).allowOverride(false));


  }

  private void regionTp(Player p, final World world, ProtectedRegion region) {
    //if (region.getFlag((Flag)Flags.TELE_LOC) != null) {
    //    com.sk89q.worldedit.util.Location location = (com.sk89q.worldedit.util.Location)region.getFlag((Flag)Flags.TELE_LOC);
    //    org.bukkit.Location location2 = BukkitAdapter.adapt((com.sk89q.worldedit.util.Location)location);
    //    p.teleport(location2);
    //} else {
    //p.sendMessage("§6Точка ТП в регионе не установлена");
    //}
    p.closeInventory();
    final Location loc1 = BukkitAdapter.adapt(world, region.getMinimumPoint());
    final Location loc2 = BukkitAdapter.adapt(world, region.getMaximumPoint());
    final Cuboid cuboid = new Cuboid(loc1, loc2);
    DelayTeleport.tp(p, cuboid.getCenter(loc1), 5, "Вы вернулись в свой регион.", true, true, DyeColor.LIGHT_BLUE);//p.teleport(location2);
    p.playSound(p.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 5, 5);
  }


  @Override
  public void onClose(final Player p, final InventoryContent content) {
    PM.getOplayer(p).menu.current = null;
  }


}
