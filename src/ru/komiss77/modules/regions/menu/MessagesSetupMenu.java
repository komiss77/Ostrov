package ru.komiss77.modules.regions.menu;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import ru.komiss77.modules.regions.RM;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.PlayerInput;
import ru.komiss77.utils.inventory.ClickableItem;
import ru.komiss77.utils.inventory.InputButton;
import ru.komiss77.utils.inventory.InventoryContent;
import ru.komiss77.utils.inventory.InventoryProvider;
import ru.komiss77.utils.inventory.SlotPos;


public class MessagesSetupMenu implements InventoryProvider {

  private final ProtectedRegion region;
  private static final ClickableItem fill = ClickableItem.empty(new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).name(" ").build());


  public MessagesSetupMenu(final ProtectedRegion region) {
    this.region = region;
  }


  @Override
  public void init(final Player player, InventoryContent contents) {
    player.playSound(player.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 5, 5);

    contents.fillBorders(fill);


    contents.set(SlotPos.of(1, 2), ClickableItem.of(new ItemBuilder(Material.OAK_SIGN)
        .name("§eПриветствие в чате")
        .lore("§7Сейчас:")
        .lore(region.getFlags().containsKey(Flags.GREET_MESSAGE) ? region.getFlag(Flags.GREET_MESSAGE) : "§8не установлено")
        .lore("")
        .lore("§6ЛКМ §7- изменить")
        .lore("§6ПКМ §7- удалить")
        .build(), inventoryClickEvent -> {

      player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1);

      if (inventoryClickEvent.getClick() == ClickType.LEFT) {

        PlayerInput.get(InputButton.InputType.ANVILL, player, s -> {
          if (s.length() > 40) {
            player.sendMessage("§cНе больше 40 символов!");
          } else {
            region.setFlag(Flags.GREET_MESSAGE, s);
            reopen(player, contents);
          }
        }, "Вы вошли в приват " + player.getName());


      } else if (inventoryClickEvent.getClick() == ClickType.RIGHT && region.getFlags().containsKey(Flags.GREET_MESSAGE)) {

        region.setFlag(Flags.GREET_MESSAGE, null);
        this.reopen(player, contents);
      }
    }));


    //.name("§eТитры при входе")  "Здравствуйте!"
    contents.set(SlotPos.of(1, 3), ClickableItem.of(new ItemBuilder(Material.OAK_SIGN)
        .name("§eТитры при входе")
        .lore("§7Сейчас:")
        .lore(region.getFlags().containsKey(Flags.GREET_TITLE) ? region.getFlag(Flags.GREET_TITLE) : "§8не установлено")
        .lore("")
        .lore("§6ЛКМ §7- изменить")
        .lore("§6ПКМ §7- удалить")
        .build(), inventoryClickEvent -> {

      player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1);

      if (inventoryClickEvent.getClick() == ClickType.LEFT) {

        PlayerInput.get(InputButton.InputType.ANVILL, player, s -> {
          if (s.length() > 40) {
            player.sendMessage("§cНе больше 40 символов!");
          } else {
            region.setFlag(Flags.GREET_TITLE, s);
            reopen(player, contents);
          }
        }, "Здравствуйте!");


      } else if (inventoryClickEvent.getClick() == ClickType.RIGHT && region.getFlags().containsKey(Flags.GREET_TITLE)) {

        region.setFlag(Flags.GREET_TITLE, null);
        this.reopen(player, contents);

      }
    }));


    //.name("§eПрощание в чате")    "Вы покинули приват "+player.getName()
    contents.set(SlotPos.of(1, 5), ClickableItem.of(new ItemBuilder(Material.OAK_SIGN)
        .name("§eПрощание в чате")
        .lore("§7Сейчас:")
        .lore(region.getFlags().containsKey(Flags.FAREWELL_MESSAGE) ? region.getFlag(Flags.FAREWELL_MESSAGE) : "§8не установлено")
        .lore("")
        .lore("§6ЛКМ §7- изменить")
        .lore("§6ПКМ §7- удалить")
        .build(), inventoryClickEvent -> {

      player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1);

      if (inventoryClickEvent.getClick() == ClickType.LEFT) {

        PlayerInput.get(InputButton.InputType.ANVILL, player, s -> {
          if (s.length() > 40) {
            player.sendMessage("§cНе больше 40 символов!");
          } else {
            region.setFlag(Flags.FAREWELL_MESSAGE, s);
            reopen(player, contents);
          }
        }, "Вы покинули приват " + player.getName());

      } else if (inventoryClickEvent.getClick() == ClickType.RIGHT && region.getFlags().containsKey(Flags.FAREWELL_MESSAGE)) {

        region.setFlag(Flags.FAREWELL_MESSAGE, null);
        this.reopen(player, contents);

      }
    }));


    //.name("§eТитры при выходе")   "До свидания!"
    contents.set(SlotPos.of(1, 6), ClickableItem.of(new ItemBuilder(Material.OAK_SIGN)
        .name("§eТитры при выходе")
        .lore("§7Сейчас:")
        .lore(region.getFlags().containsKey(Flags.FAREWELL_TITLE) ? region.getFlag(Flags.FAREWELL_TITLE) : "§8не установлено")
        .lore("")
        .lore("§6ЛКМ §7- изменить")
        .lore("§6ПКМ §7- удалить")
        .build(), inventoryClickEvent -> {

      player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1);

      if (inventoryClickEvent.getClick() == ClickType.LEFT) {

        PlayerInput.get(InputButton.InputType.ANVILL, player, s -> {
          if (s.length() > 40) {
            player.sendMessage("§cНе больше 40 символов!");
          } else {
            region.setFlag(Flags.FAREWELL_TITLE, s);
            reopen(player, contents);
          }
        }, "До свидания!");

      } else if (inventoryClickEvent.getClick() == ClickType.RIGHT && region.getFlags().containsKey(Flags.FAREWELL_TITLE)) {

        region.setFlag(Flags.FAREWELL_TITLE, null);
        this.reopen(player, contents);

      }
    }));


    contents.set(2, 4, ClickableItem.of(new ItemBuilder(Material.OAK_DOOR).name("§fназад").build(), p1
        -> RM.openRegionOwnerMenu(player, region)));


  }


}

