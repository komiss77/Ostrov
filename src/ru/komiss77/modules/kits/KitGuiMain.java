package ru.komiss77.modules.kits;

import java.util.ArrayList;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.TCUtil;
import ru.komiss77.utils.TimeUtil;
import ru.komiss77.utils.inventory.*;


public class KitGuiMain implements InventoryProvider {

    private static final ItemStack up = new ItemBuilder(Material.HORN_CORAL_FAN).build();
    private static final ItemStack side = new ItemBuilder(Material.VINE).build();
    private static final ItemStack down = new ItemBuilder(Material.TUBE_CORAL).build();
    ;


    @Override
    public void init(final Player p, final InventoryContent content) {
      p.playSound(p.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 5, 5);
      //content.fillRect(SlotPos.of(0), SlotPos.of(53), ClickableItem.empty(fill));
      //content.fillRow(3, ClickableItem.empty(fill));
      //content.fillRow(4, ClickableItem.empty(fill));
      content.fillColumn(0, ClickableItem.empty(side));
      content.fillColumn(8, ClickableItem.empty(side));
      content.fillRow(0, ClickableItem.empty(up));
      content.fillRow(5, ClickableItem.empty(down));
      final Pagination pagination = content.pagination();


      // content.set(0, 4, ClickableItem.empty(new ItemBuilder(Material.STONECUTTER)
        //   .name("§7Наборов на сервере: §f"+KitManager.kits.size())
        //  //.lore("§7Состояние: §e"+arena.state.toString())
        //  .build()));

      final Oplayer op = PM.getOplayer(p);

        final ArrayList<ClickableItem> menuEntry = new ArrayList<>();


        ItemStack item;
        String giveInfo1 = "";
        String giveInfo2 = "";

        for (Kit kit : KitManager.kits.values()) {

            if (!kit.enabled) continue; //добавляем только включенные

          if (kit.needPermission && !p.hasPermission("ostrov.kit." + kit.name) && !p.hasPermission("ostrov.kit.*")) {

                giveInfo1 = "§cтребуется право §5ostrov.kit." + kit.name;
                giveInfo2 = "§cдля доступа к набору!";

            } else if (kit.accesBuyPrice == 0) {

            //if (PM.Kit_has_acces(p.name(), kit.name)) {

            final int secondLeft = KitManager.getSecondLetf(p, kit);
                if (secondLeft > 0) {
                    giveInfo1 = "§cПолучить можно через " + TimeUtil.secondToTime(secondLeft);
                } else {
                    giveInfo1 = "§fЛКМ §e- Получить набор.";
                }
                giveInfo2 = "§7цена получения: §5" + (kit.getPrice > 0 ? kit.getPrice + " §7лони" : "бесплатно");

                // } else {
                //    giveInfo1 = "§fЛКМ §e- Получить право доступа.";
                //    giveInfo2 = "§7(бесплатно)";
                // }


            } else if (kit.accesBuyPrice >= 0) {


                if (op.hasKitAcces(kit.name)) {

                  final int secondLeft = KitManager.getSecondLetf(p, kit);
                    if (secondLeft > 0) {
                        giveInfo1 = "§cПолучить можно через " + TimeUtil.secondToTime(secondLeft);
                    } else {
                        giveInfo1 = "§fЛКМ §e- Получить набор.";
                    }
                    giveInfo2 = "§7цена получения: §5" + (kit.getPrice > 0 ? kit.getPrice + " §7лони" : "бесплатно");

                } else {

                    giveInfo1 = "§fЛКМ §e- Покупка права доступа.";
                    giveInfo2 = "§7цена покупки: §5" + kit.accesBuyPrice + " §7лони";

                }

            }


            item = new ItemBuilder(kit.logoItem)
                    .lore("")
                    .lore(kit.rarity.displayName)
                    .lore("")
                    .lore(kit.enabled ? "§aАктивен§7, " + (kit.needPermission ? "§eтребуется право" : "§aдоступен всем") : "§сЗаблокирован")
                    .lore("§7цена доступа: " + (kit.accesBuyPrice == 0 ? "§8бесплатно" : "§e" + kit.accesBuyPrice + " §7лони"))
                    .lore("§7цена получения: " + (kit.getPrice == 0 ? "§8бесплатно" : "§e" + kit.getPrice + " §7лони"))
                    .lore("§7продажа доступа: " + (kit.accesSellPrice == 0 ? "§8никакой выгоды" : "§b" + kit.accesSellPrice + " §7лони"))
                    .lore(kit.delaySec == 0 ? "§8интервал получения не установлен" : "§7интервал получения: §6" + TimeUtil.secondToTime(kit.delaySec))
                    .lore("")
                    .lore("§fПКМ §7- §eпосмотреть состав")
                    .lore(giveInfo1)
                    .lore(giveInfo2)
                    .lore((kit.accesBuyPrice > 0 && op.hasKitAcces(kit.name)) ? "§9Shift+ПКМ §7- продать доступ за §e" + kit.accesSellPrice + " §7лони" : "")
                    .build();


            menuEntry.add(ClickableItem.of(item, e -> {
              //final Kit clickedKit = KitManager.kits.get(TCUtil.strip(e.getCurrentItem().getItemMeta().displayName()));
//System.out.println("-- ClickableItem clickedKit="+clickedKit+" name="+ ChatColor.strip(e.getCurrentItem().getItemMeta().getDisplayName()) );

              //if (clickedKit == null) return;
              if (e.getClick() == ClickType.LEFT) { //проверка на выключен везде!!
//System.out.println("-- ClickableItem clickedKit="+clickedKit+" name="+ ChatColor.strip(e.getCurrentItem().getItemMeta().getDisplayName()) );
                if (kit.accesBuyPrice > 0 && !op.hasKitAcces(kit.name)) {
                  p.closeInventory();
                  //p.performCommand("kit buyacces " + clickedKit.name);
                  KitManager.buyKitAcces(p, kit.name);
                    } else {
                  p.closeInventory();
                  KitManager.tryGiveKit(p, kit.name);
                  //p.performCommand("kit give " + clickedKit.name);
                    }
                //reopen(p, content);
              } else if (e.getClick() == ClickType.SHIFT_RIGHT) {
                if (op.hasKitAcces(kit.name)) {
                  p.closeInventory();
                  KitManager.trySellAcces(p, kit.name);
                  //p.performCommand("kit sellacces " + clickedKit.name);
                    }
                //reopen(p, content);
              } else if (e.getClick() == ClickType.RIGHT) {
                KitManager.openKitPrewiev(p, kit);
                //reopen(p, content);
                }
            }));  
            
        }

        pagination.setItems(menuEntry.toArray(new ClickableItem[menuEntry.size()]));
        pagination.setItemsPerPage(28);


        //прятать если нет
        if (!pagination.isFirst()) {
          content.set(2, 0, ClickableItem.of(new ItemBuilder(Material.PINK_STAINED_GLASS_PANE).name("назад").build(), p4
              -> content.getHost().open(p, pagination.previous().getPage()))
            );
          content.set(3, 0, ClickableItem.of(new ItemBuilder(Material.PINK_STAINED_GLASS_PANE).name("назад").build(), p4
              -> content.getHost().open(p, pagination.previous().getPage()))
            );
        }

        if (!pagination.isLast()) {
          content.set(2, 8, ClickableItem.of(new ItemBuilder(Material.LIME_STAINED_GLASS_PANE).name("далее").build(), p4
              -> content.getHost().open(p, pagination.next().getPage()))
            );
          content.set(3, 8, ClickableItem.of(new ItemBuilder(Material.LIME_STAINED_GLASS_PANE).name("далее").build(), p4
              -> content.getHost().open(p, pagination.next().getPage()))
            );
        }

      pagination.addToIterator(content.newIterator(SlotIterator.Type.HORIZONTAL, SlotPos.of(1, 1)).allowOverride(false));


    }


}
