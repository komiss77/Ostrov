package ru.komiss77.modules.regions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import ru.komiss77.utils.ItemBuilder;

public class Template implements Comparable<Template> {

  public static final String PERM_FOR_ALl = "region.template.all";

  public final String name;
  public List<String> allowedWorlds = new ArrayList<>();
  public String displayname = "Обычный регион";
  public Material iconMat = Material.OAK_FENCE;
  public Material borderMaterial;//null - без ограды
  public List<String> description = Arrays.asList("§7Небольшой регион");
  public int price;
  public int refund;
  public boolean permission;
  public int size;
  public int height = 319;
  public int depth;

  public Template(final String name) {
    this.name = name;
  }


  public ItemStack getIconMat() {
    return new ItemBuilder(this.iconMat).name(this.displayname).build();
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

  // public void setNoPermDescription(final List<String> list) {
  //     this.noPermDescription = list.stream().map(s -> TCUtil.translateAlternateColorCodes('&', s)).collect((Collector<? super Object, ?, List<String>>)Collectors.toList());
  //  }
  // public void setNoPermDescription(List <String> list) {
  //     this.noPermDescription.clear();
  //     for (String desc : list) {
  //         this.description.add(TCUtil.translateAlternateColorCodes('&', desc));
  //    }
  //this.noPermDescription = (List)list.stream().map((str) -> {
  //    return TCUtil.translateAlternateColorCodes('&', str);
  // }).collect(Collectors.toList());
  // }


/*
  @Override
  public Map<String, Object> serialize() {
    final HashMap<String, Object> hashMap = new HashMap<>();//Maps.newHashMap();
    hashMap.put("name", this.name);
    hashMap.put("gui.displayname", this.displayname);
    hashMap.put("gui.icon", this.icon.toString());
    hashMap.put("gui.description", this.description);
    hashMap.put("gui.noPermDescription", this.noPermDescription);
    hashMap.put("data.size", this.size);
    hashMap.put("data.heigth", height==256 ? 319 : height);
    hashMap.put("data.depth", this.depth);
    hashMap.put("data.price", this.price);
    hashMap.put("data.refund", this.refund);
    hashMap.put("data.world", this.world);
    hashMap.put("data.permission", this.permission);
    hashMap.put("border.material", this.borderMaterial.toString());
    hashMap.put("border.enabled", this.generateBorder);
    hashMap.put("generation.runCommands", this.runCommands);
    return (Map<String, Object>)hashMap;
  }

          return "Заготовка(name=" + this.getName()+
              ", отображаемоеИмя=" + this.getDisplayname() +
              ", иконка=" + this.getIcon() +
              ", описание=" + this.getDescription() +
              ", размер=" + this.getSize() +
              ", мир=" + this.getWorld() +
              ", высота=" + this.getHeight() +
              ", клабина=" + this.getDepth() +
              ", цена=" + this.getPrice() +
              ", манибэк=" + this.getRefund() +
              ", материалЗабора=" + this.getBorderMaterial() +
              ", делатьЗабор=" + this.isGenerateBorder() +
              ", команды=" + this.getRunCommands() +
              ", права=" + this.getPermission() +
              ", сообщениеЕслиНетПрав=" + this.getNoPermDescription() + ")";

  public static Template deserialize(final Map<String, Object> map) {
    final Template template = new Template((String) map.get("name"));
    template.setDisplayname((String) map.get("gui.displayname"));
    template.setIcon(Material.valueOf((String)map.get("gui.icon")));
    template.description = ((List<String>)map.get("gui.description"));
    template.noPermDescription = ((List<String>)map.get("gui.noPermDescription"));
    template.setSize((int)map.get("data.size"));
    final int h = (int)map.get("data.heigth");
    template.setHeight(h==256 ? 319 : h);
    template.setDepth((int)map.get("data.depth"));
    template.setPrice((int)map.get("data.price"));
    template.setRefund((int)map.get("data.refund"));
    template.setWorld((String) map.get("data.world"));
    template.setPermission((String) map.getOrDefault("data.permission", ""));
    template.setBorderMaterial(Material.valueOf((String)map.get("border.material")));
    template.setGenerateBorder((boolean)map.get("border.enabled"));
    template.runCommands = ((List<String>)map.get("generation.runCommands"));
    RM.log_ok("Загружена заготовка " + template.getName()+ " - " + template.getDisplayname());
    return template;
  }  */


/*
  @Override
  public String toString() {
    return "Заготовка(name=" + this.getName()+
        ", отображаемоеИмя=" + this.getDisplayname() +
        ", иконка=" + this.getIcon() +
        ", описание=" + this.getDescription() +
        ", размер=" + this.getSize() +
        ", мир=" + this.getWorld() +
        ", высота=" + this.getHeight() +
        ", клабина=" + this.getDepth() +
        ", цена=" + this.getPrice() +
        ", манибэк=" + this.getRefund() +
        ", материалЗабора=" + this.getBorderMaterial() +
        ", делатьЗабор=" + this.isGenerateBorder() +
        ", команды=" + this.getRunCommands() +
        ", права=" + this.getPermission() +
        ", сообщениеЕслиНетПрав=" + this.getNoPermDescription() + ")";
  }*/

  @Override
  public int compareTo(final Template o) {
    return (this.size > o.size) ? 1 : ((this.size < o.size) ? -1 : 0);
  }


  @Override
  public int hashCode() {
    return Objects.hash(this.name);
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || (obj != null && obj instanceof Template && Objects.equals(this.name, ((Template) obj).name));
  }

  public String permission() {
    return "region.template." + name;
  }
}
