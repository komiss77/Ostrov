package ru.komiss77.modules.regions;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import ru.komiss77.utils.ItemBuilder;

public class Template {

  public static final String PERM_FOR_ALl = "region.template.all";

  public final String name;
  public List<String> allowedWorlds = new ArrayList<>();
  public String displayname = "§bНебольшой регион";
  public Material iconMat = Material.OAK_FENCE;
  public Material borderMaterial = Material.OAK_FENCE;//null - без ограды
  public List<String> description = new ArrayList<>();
  public int price = 0;
  public int refund = 0;
  public boolean permission = false;
  public int size = 10;
  public int height = 319;
  public int depth = 5;

  public Template(final String name) {
    this.name = name;
  }

  public Location getMinimumPoint(final Location loc) {
    final int halfSize = (int) Math.round(size / 2.0);
    int blockY = loc.getBlockY();
    if (blockY > 30) blockY = 30;
    blockY -= depth;
    if (blockY < 0) blockY = 0;
    return new Location(loc.getWorld(), loc.getBlockX() - halfSize, blockY, loc.getBlockZ() - halfSize);
  }

  public Location getMaximumPoint(final Location loc) {
    final Location high = getMinimumPoint(loc).add(size, 0, size);
    int blockY = loc.getBlockY();
    blockY += height;
    if (blockY > 319) blockY = 319;
    high.setY(blockY);
    return high;
  }

  public String permission() {
    return "region.template." + name;
  }

  public ItemStack editorIcon(boolean canEdit) {
    return new ItemBuilder(iconMat)
        .name(name)
        .lore(displayname)
        .lore("§7Размеры: §e" + size + "x" + size + "§7, вниз §e" + depth + "§7, вверх §e" + height)
        .lore("§7Цена: §b" + (price == 0 ? "бесплатно. " : price + " §7лони. ") + (permission ? "§6Требует §e" + permission() : "§2Доступен всем"))
        .lore("§8Доступен в мирах:")
        .lore(allowedWorlds)
        .lore("§8Описание:")
        .lore(description)
        .lore("")
        .lore(canEdit ? "§fЛкм - редактировать" : "")
        .build();
  }

}
