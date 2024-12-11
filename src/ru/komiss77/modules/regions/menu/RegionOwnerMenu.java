package ru.komiss77.modules.regions.menu;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.protection.managers.storage.StorageException;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import ru.komiss77.ApiOstrov;
import ru.komiss77.hook.WGhook;
import ru.komiss77.modules.regions.RM;
import ru.komiss77.modules.regions.Template;
import ru.komiss77.modules.world.Schematic;
import ru.komiss77.modules.world.WE;
import ru.komiss77.modules.world.XYZ;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.NumUtils;
import ru.komiss77.utils.ParticleUtil;
import ru.komiss77.utils.TimeUtil;
import ru.komiss77.utils.inventory.*;


public class RegionOwnerMenu implements InventoryProvider {

  private static final ClickableItem fill = ClickableItem.empty(new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).name(" ").build());
  private final ProtectedRegion region;

  public RegionOwnerMenu(final ProtectedRegion region) {
    this.region = region;
  }


  @Override
  public void init(Player p, InventoryContent content) {
    p.playSound(p.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 5, 5);

    content.fillBorders(fill);

    final Template t = RM.template(RM.templateName(region));

    //инфо о регионе
    if (t == null) {

      content.set(0, 4, ClickableItem.empty(new ItemBuilder(Material.FLOWER_BANNER_PATTERN)
          .name("§6>> §7Информация §6<<")
          .lore("§8Для региона не найдено")
          .lore("§8соответствующей заготовки.")
          .lore("§8Регион не удалится,")
          .lore("§8но невозможно получить")
          .lore("§8подробные данные о нём.")
          .build())
      );

    } else {

      String timeStamp = RM.createTime(region);
      final int stamp = NumUtils.intOf(timeStamp, 0);
      if (stamp > 0) {
        timeStamp = TimeUtil.dateFromStamp(stamp);
      }

      content.set(0, 4, ClickableItem.empty(new ItemBuilder(Material.FLOWER_BANNER_PATTERN)
          .name("§6>> §7Информация §6<<")
          .lore("§7Тип :§6 " + t.displayname)
          .lore("§7Основание :§6 " + t.size + "x" + t.size)
          .lore("§7Высота :§6 " + t.height)
          .lore("§7Подземная часть :§6 " + t.depth)
          .lore("§7Стоимость :§6 " + t.price)
          .lore("§7Возврат после удаления :§6 " + t.refund)
          .lore("§7Создан: §6" + timeStamp)
          .build()));
    }


    //кнопка удаления
    if (WE.hasJob(p)) {
      content.set(0, 8, ClickableItem.empty(
          new ItemBuilder(Material.TNT)
              .name("§4Удалить регион")
              .lore("§cДождитесь окончания операции!")
              .build()
      ));
    } else {
      content.set(0, 8, ClickableItem.of(
          new ItemBuilder(Material.TNT)
              .name("§4Удалить регион")
              .lore("§cНеобратимая операция!")
              .lore("§4Шифт+ПКМ §f- удалить")
              .lore(RM.regenOnDelete ? "§c§l        ВНИМАНИЕ !" : "§6Постройки останутся,")
              .lore(RM.regenOnDelete ? "§c§l Все постройки в регионе" : "§6но любой игрок сможет")
              .lore(RM.regenOnDelete ? "§c§l будут уничтожены!" : "§6их сломать.")
              .build(), e -> {
            if (e.getClick() == ClickType.SHIFT_RIGHT) {
              ConfirmationGUI.open(p, "§4Подтвердите удаление", true, confirm -> {
                if (confirm) {
                  if (RM.regenOnDelete) {
                    final Schematic sch = WE.getSchematic(Bukkit.getConsoleSender(), region.getId(), true);
                    WE.paste(Bukkit.getConsoleSender(), sch, new XYZ(BukkitAdapter.adapt(p.getWorld(), region.getMinimumPoint())), Schematic.Rotate.r0, true);
                  }
                  WGhook.getRegionManager(p.getWorld()).removeRegion(region.getId());
                  try {
                    WGhook.worldguard_platform.getRegionContainer().get(BukkitAdapter.adapt(p.getWorld())).saveChanges();
                  } catch (StorageException ex) {
                    ex.printStackTrace();
                  }
                  //возврат денег
                  final Template template = RM.template(RM.templateName(region));
                  if (template != null && template.refund > 0) {
                    ApiOstrov.moneyChange(p, template.refund, "Возврат денег за регион");
                  }
                  p.sendMessage("§cВаш регион удалён.");
                } else {
                  reopen(p, content);
                }
              });
            }
          }
      ));
    }


    //кнопка юзеры
    content.set(1, 1, ClickableItem.of(new ItemBuilder(Material.PLAYER_HEAD)
        .name("§6>> §fУправление пользователями §6<<")
            .lore("§7Пользователей" + (region.getMembers().getPlayerDomain().size() == 0 ? " нет" : ": " + region.getMembers().getPlayerDomain().size()))
            .lore("")
            .lore("§6ЛКМ §f- просмотр / удаление")
            .lore("§6ПКМ §f- добавление")
            .lore("§7Для добавления через меню,")
            .lore("§7кандидат должен находиться")
            .lore("§7в вашем регионе, и быть не далее")
            .lore("§75 блоков от вас.")
            .build(), inventoryClickEvent
            -> {
          if (inventoryClickEvent.getClick() == ClickType.LEFT) {

            SmartInventory.builder()
                .id("regiongui.editmembers")
                .provider(new MembersManageMenu(region))
                .size(5)
                .title("§2Пользователи")
                .build().open(p);

          } else if (inventoryClickEvent.getClick() == ClickType.RIGHT) {

            SmartInventory.builder()
                .id("regiongui.addmembers")
                .provider(new MembersAddMenu(region))
                .size(5)
                .title("§2Найдены рядом")
                .build().open(p);

          }
        }
    ));


    content.set(1, 3, ClickableItem.of(new ItemBuilder(Material.OAK_SIGN)
        .name("§6>> §fСообщения при входе/выходе §6<<")
        .build(), p2 -> {
      p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 2, 2);
      SmartInventory.builder()
          .provider(new MessagesSetupMenu(region))
          .size(3)
          .title("§2Сообщения")
          .build().open(p);
    }));


    //меню флагов
    //if (p.hasPermission("region.manage.flagmenu")) {
    p.playSound(p.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 5, 5);
    content.set(1, 5, ClickableItem.of(new ItemBuilder(Material.LIGHT_GRAY_BANNER)
            .name("§6>> §fУправление флагами региона §6<<")
            .build(), inventoryClickEvent
            -> {
          p.playSound(p.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 5, 5);
          SmartInventory.builder()
              .id("regiongui.flagMenu")
              .provider(new FlagsManageMenu(this.region))
              .size(5, 9)
              .title("§fУправление флагами региона")
              .build()
              .open(p);
        }
    ));
    //}


    //показ границ
    content.set(1, 7, ClickableItem.of(new ItemBuilder(Material.EXPERIENCE_BOTTLE)
        .name("§6>> §fПоказать границы §6<<")
        .lore("§7Для остановки показа")
        .lore("§7присядьте (клав. Shift)")
        .build(), e -> {
      p.closeInventory();
      p.playSound(p.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 5, 5);

      ParticleUtil.BorderDisplay(p,
          new XYZ(BukkitAdapter.adapt(p.getWorld(), region.getMinimumPoint())),
          new XYZ(BukkitAdapter.adapt(p.getWorld(), region.getMaximumPoint())),
          true);
    }));


    //установка точки ТП
       /* content.set( 2, 6, ClickableItem.of( new ItemBuilder(Material.ENDER_PEARL)
            .name(Language.INTERFACE_HOME_BUTTON.toString())
            .lore(Language.INTERFACE_HOME_BUTTON_DESC.getDescriptionArray())
            .build(), e -> {
            p.playSound(p.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 5, 5);
            region.setFlag((Flag)Flags.TELE_LOC, BukkitAdapter.adapt(p.getLocation()));
            region.setDirty(true);
            p.sendMessage(Language.INTERFACE_HOME_BUTTON_SUCCESS.toChatString());
        }));
        */


  }


}


