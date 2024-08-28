package ru.komiss77.modules.regions.menu;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import ru.komiss77.ApiOstrov;
import ru.komiss77.modules.regions.RM;
import ru.komiss77.modules.regions.Template;
import ru.komiss77.utils.*;
import ru.komiss77.utils.inventory.*;
import ru.komiss77.utils.inventory.InputButton.InputType;


public class TemplateEditorMenu implements InventoryProvider {
  private static final ItemStack filler;
  private final Template t;

  static {
    filler = new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).name(" ").build();
  }

  public TemplateEditorMenu(final Template template) {
    this.t = template;
  }


  @Override
  public void init(final Player p, final InventoryContent contents) {
    contents.fillRow(0, ClickableItem.empty(TemplateEditorMenu.filler));
    contents.fillRow(4, ClickableItem.empty(TemplateEditorMenu.filler));


    contents.set(0, 4, ClickableItem.of(new ItemBuilder(t.getIconMat())
        .name("§7Установить иконку")
        .lore("§7Ткните сюда предметом из инвентаря")
        .lore("§7для смены иконки")
        .build(), e -> {
      if (e.getClick() == ClickType.LEFT && e.getCursor() != null && e.getCursor().getType() != Material.AIR) {
        p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1);
        t.iconMat = e.getCursor().getType();
        e.getView().getBottomInventory().addItem(new ItemStack[]{e.getCursor()});
        e.getView().setCursor(new ItemStack(Material.AIR));
        reopen(p, contents);
      }
    }));


    contents.set(1, 0, new InputButton(InputType.ANVILL, new ItemBuilder(Material.NAME_TAG)
        .name("§7Отображаемое название")
        .lore("§7Текущее: §6" + t.displayname)
        .build(), t.displayname, dn -> {
      p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1);
      t.displayname = dn;
      reopen(p, contents);
    }));


    //Описание
    contents.set(1, 1, ClickableItem.of(new ItemBuilder(Material.BOOK)
            .name("§7Описание")
            .lore("§7Текущее:")
            .lore(t.description)
            .lore("")
            .lore("§aЛКМ §7добавить строку")
            .lore("§aПКМ §7удалить последнюю строку.")
            .build(), e -> {
          if (e.getClick() == ClickType.RIGHT) {
            if (!t.description.isEmpty()) {
              t.description.remove(t.description.size() - 1);
              reopen(p, contents);
            }
          } else {
            PlayerInput.get(InputButton.InputType.ANVILL, p, s -> {
              t.description.add(TCUtil.translateAlternateColorCodes('&', s));
              reopen(p, contents);
            }, "строка..");
          }
        }
    ));


    contents.set(1, 2, ClickableItem.of(new ItemBuilder(Material.GRASS_BLOCK)
            .name("§7Разрешенные миры")
            .lore("§7В этом мире:")
            .lore(t.allowedWorlds.contains(p.getWorld().getName()) ? "§2§lДА" : "§4§lНЕТ")
            .lore("")
            .lore(t.allowedWorlds)
            .lore("§fЛКМ - редактировать")
            .lore("")
            .build(), e -> {
          SmartInventory.builder()
              .id("TemplateWorlds" + p.getName())
              .provider(new WorldSelectMenu(t))
              .size(1)
              .title("§fРазрешенные миры")
              .build().open(p);
        }
    ));

    //Цена
    contents.set(1, 3, new InputButton(InputType.ANVILL, new ItemBuilder(Material.GOLD_NUGGET)
        .name("§7Цена")
        .lore("§7Сейчас: §6" + t.price + " §7лони")
        .build(), String.valueOf(t.price), s4 -> {
      if (!ApiOstrov.isInteger(s4)) {
        p.sendMessage("§cВведите целое число!");
        this.reopen(p, contents);
      } else {
        p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1);
        t.price = Integer.parseInt(s4);
        reopen(p, contents);
      }
    }
    ));


    //Возврат денег
    contents.set(1, 4, new InputButton(InputType.ANVILL, new ItemBuilder(Material.GOLD_NUGGET)
        .name("§7Возврат денег")
        .lore("§7Данная сумма будет §aполучена")
        .lore("§7игроком после удаления региона.")
        .lore("§7Сейчас: §6" + t.refund)
        .build(), String.valueOf(t.refund), s5 -> {
      if (!ApiOstrov.isInteger(s5)) {
        p.sendMessage("§cВведите целое число!");
        reopen(p, contents);
      } else {
        p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1);
        t.refund = Integer.parseInt(s5);
        reopen(p, contents);
      }
    }
    ));


    //Размер
    contents.set(SlotPos.of(1, 5), new InputButton(InputType.ANVILL, new ItemBuilder(Material.BEACON)
        .name("§7Размер")
        .lore("§7Сейчас: §6" + t.size)
        .lore("§7Длинна каждой стороны")
        .lore("§7квадратного основания.")
        .build(), String.valueOf(t.size), s6 -> {
      if (!ApiOstrov.isInteger(s6)) {
        p.sendMessage("§cВведите целое число!");
        reopen(p, contents);
      } else {
        p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1);
        t.size = Integer.parseInt(s6);
        reopen(p, contents);
      }
    }
    ));


    //Материал ограждения
    contents.set(1, 6, ClickableItem.of(new ItemBuilder(t.borderMaterial == null ? Material.BARRIER : t.borderMaterial)
            .name("§7Материал ограждения")
            .lore("§7Ткните сюда предметом из инвентаря")
            .lore("§7для установки материала.")
            .lore("ПКМ - не строить ограду")
            .build(), e -> {
          if (e.getClick() == ClickType.LEFT) {
            if (e.getCursor() != null && e.getCursor().getType() != Material.AIR) {
              if (!e.getCursor().getType().isBlock()) {
                p.sendMessage("§cЭтот материал не может быть блоком!");
                reopen(p, contents);
              } else {
                p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1);
                t.borderMaterial = e.getCursor().getType();
                e.getView().getBottomInventory().addItem(new ItemStack[]{e.getCursor()});
                e.getView().setCursor(new ItemStack(Material.AIR));
                reopen(p, contents);
              }
            }
          } else if (e.getClick() == ClickType.RIGHT) {
            t.borderMaterial = null;
            reopen(p, contents);
          }
        }
    ));


    //права
    contents.set(1, 7, ClickableItem.of(new ItemBuilder(Material.BLAZE_POWDER)
        .name("§7Право для покупки")
        .lore(!t.permission ? "§8Не требуется" : "§f" + t.permission())
        .lore("")
        .lore(!t.permission ? "§aЛКМ -§cтребовать право" : "§aЛКМ -§aне требовать право")
        .build(), e -> {
      if (e.getClick() == ClickType.LEFT) {
        t.permission = !t.permission;
        reopen(p, contents);
      }
    }));


    //сохранить
    contents.set(2, 4, ClickableItem.of(new ItemBuilder(Material.EMERALD).name("§2Сохранить заготовку").build(), p1 -> {
          RM.saveTemplate(t);
          RM.openTemplateAdmin(p);
        }
    ));


    //удалить заготовку
    contents.set(2, 7, ClickableItem.of(new ItemBuilder(Material.TNT)
        .name("§4Удалить заготовку")
        .lore("§7После удаления заготовку не будет")
        .lore("§7доступна для покупки игроками.")
        .build(), p2 -> ConfirmationGUI.open(p, "§4Подтверждение", result -> {
          if (result) {
            p.playSound(p.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.5f, 1.15f);
            RM.delTemplate(t);
            RM.openTemplateAdmin(p);
          } else {
            reopen(p, contents);
            p.playSound(p.getLocation(), Sound.ENTITY_LEASH_KNOT_PLACE, 0.5f, 0.85f);
          }
        }
    )));


  }


  class WorldSelectMenu implements InventoryProvider {

    private static final ItemStack fill = new ItemBuilder(ItemType.BLACK_STAINED_GLASS_PANE).name("§8.").build();
    private final Template t;

    public WorldSelectMenu(final Template template) {
      this.t = template;
    }

    @Override
    public void init(final Player p, final InventoryContent contents) {
      p.playSound(p.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 5, 5);

      contents.fillRect(0, 0, 2, 8, ClickableItem.empty(fill));

      int c = 0;
      boolean allow;
      for (final World w : Bukkit.getWorlds()) {
        allow = t.allowedWorlds.contains(w.getName());
        contents.add(ClickableItem.of(new ItemBuilder(allow ? getWorldMat(w) : Material.GRAY_DYE)
                .name(w.getName())
                .lore(allow ? "§7ЛКМ - §4Запретить" : "§2Разрешить")
                .lore("")
                .build(), e -> {
              if (!t.allowedWorlds.remove(w.getName())) {
                t.allowedWorlds.add(w.getName());
              }
              reopen(p, contents);
            }
        ));
        c++;
        if (c == 7) break;
      }

      contents.set(2, 4, ClickableItem.of(new ItemBuilder(Material.OAK_DOOR).name("назад").build(), e ->
          RM.editTemplate(p, t)
      ));

    }

    private Material getWorldMat(final World w) {
      return switch (w.getEnvironment()) {
        case NORMAL -> Material.GRASS_BLOCK;
        case NETHER -> Material.NETHERRACK;
        case THE_END -> Material.END_STONE;
        default -> Material.WHITE_GLAZED_TERRACOTTA;
      };
    }
  }


}
