package ru.komiss77.modules.regions.menu;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import ru.komiss77.modules.regions.FlagSetting;
import ru.komiss77.modules.regions.RM;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.ItemUtil;
import ru.komiss77.utils.PlayerInput;
import ru.komiss77.utils.TCUtil;
import ru.komiss77.utils.inventory.*;


public class FlagsSetupMenu implements InventoryProvider {


  @Override
  public void init(Player p, InventoryContent content) {

    p.playSound(p.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 5, 5);

    List<ClickableItem> menuEntryList = new ArrayList<>();

    RM.flags.entrySet().stream().forEach(es -> {

      final FlagSetting fs = es.getValue();
      ItemStack is = new ItemBuilder(fs.enabled ? (fs.iconMat == null ? Material.LIME_DYE : fs.iconMat) : Material.GRAY_DYE)
          .name(fs.displayname)
          .lore(fs.enabled ? "§aдоступен§7. ПКМ - §4заблочить" : "§cзаблокирован. ПКМ - §2разблочить")
          .lore("")
          .lore("Клик предметом - сменить иконку")
          .lore("§6ЛКМ §7- изменить название")
          .build();

      menuEntryList.add(ClickableItem.of(is, e -> {
        if (e.getClick() == ClickType.LEFT) {

          if (e.getCursor() != null && e.getCursor().getType() != Material.AIR) {
            fs.iconMat = e.getCursor().getType();
            RM.saveFlag(es.getKey());
            reopen(p, content);
            p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 2, 2);

          } else {

            PlayerInput.get(InputButton.InputType.ANVILL, p, s -> {
              fs.displayname = TCUtil.translateAlternateColorCodes('&', s);
              RM.saveFlag(es.getKey());
              reopen(p, content);
              p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 2, 2);
            }, TCUtil.translateAlternateColorCodes('§', fs.displayname));

          }

        } else if (e.getClick() == ClickType.RIGHT) {
          fs.enabled = !fs.enabled;
          RM.saveFlag(es.getKey());
          reopen(p, content);
          p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 2, 2);
        }
      }));
    });


    Pagination pg = content.pagination();
    pg.setItems(menuEntryList.toArray(new ClickableItem[0]));
    pg.setItemsPerPage(45);
    pg.addToIterator(content.newIterator(SlotIterator.Type.HORIZONTAL, 0, 0).allowOverride(false));


    //content.set(4, 4, ClickableItem.of(new ItemBuilder(Material.OAK_DOOR).name("гл.меню").build(),
    //    e -> RM.openRegionOwnerMenu(p, region))
    //);


    if (!pg.isLast()) {
      content.set(5, 6, ClickableItem.of(ItemUtil.nextPage,
          e -> content.getHost().open(p, pg.next().getPage()))
      );
    }


    if (!pg.isFirst()) {
      content.set(5, 2, ClickableItem.of(ItemUtil.previosPage,
          e -> content.getHost().open(p, pg.previous().getPage()))
      );
    }


  }


}
