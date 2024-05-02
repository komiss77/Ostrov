package ru.komiss77.commands;

import java.util.*;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import ru.komiss77.ApiOstrov;
import ru.komiss77.builder.menu.EntitySetup;
import ru.komiss77.modules.translate.Lang;
import ru.komiss77.objects.ValueSortedMap;
import ru.komiss77.utils.EntityUtil;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.ItemUtils;
import ru.komiss77.utils.inventory.*;


public class EntityCmd implements Listener, CommandExecutor {

    @Override
    public boolean onCommand(CommandSender cs, Command cmd, String string, String[] arg) {
        
        if ( ! (cs instanceof Player) ) {
            cs.sendMessage("§cНе консольная команда!");
            return true;
        }
        final Player p = (Player) cs;

     /* if (ApiOstrov.isLocalBuilder(cs, false)) { только через билдера!
        SmartInventory
          .builder()
          .id("EntityMain"+p.getName())
          .provider(new EntityWorldMenu(p.getWorld(), -1))
          .size(6, 9)
          .title("§2Сущности "+p.getWorld()
          .getName())
          .build()
          .open(p);
        return true;
      }*/

      if ( !p.hasPermission("ostrov.entity")) {
          p.sendMessage("§cУ Вас нет пава ostrov.entity !");
      }

      SmartInventory
          .builder()
          .id("EntityMain"+p.getName())
          .provider(new EntityWorldView(p.getWorld(), -1))
          .size(3, 9)
          .title("§2Сущности "+p.getWorld()
          .getName())
          .build()
          .open(p);

          return true;

    }
}





class EntityServerView implements InventoryProvider {

  private static final ClickableItem fill = ClickableItem.empty(new ItemBuilder(Material.YELLOW_STAINED_GLASS_PANE).name("§8.").build());
  private final List<World> worlds;

  public EntityServerView(final List<World> worlds) {
    this.worlds = worlds;
  }

  @Override
  public void init(final Player p, final InventoryContent contents) {
    p.playSound(p.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 5, 5);
    contents.fillRow(4, fill);

    final Pagination pagination = contents.pagination();
    final ArrayList<ClickableItem> menuEntry = new ArrayList<>();

    final Map<EntityUtil.EntityGroup,Integer>count=new HashMap<>();
    int total=0;

    for (final World world : worlds) {

      EntityUtil.EntityGroup group;
      for (final Entity e : world.getEntities()) {
        if (e.getType()==EntityType.PLAYER) continue;
        group=EntityUtil.group(e);
        if (count.containsKey(group)) {
          count.put(group, count.get(group)+1);
        } else {
          count.put(group, 1);
        }
        total++;
      }

      menuEntry.add( ClickableItem.of( new ItemBuilder(Material.HEART_OF_THE_SEA)
        .name("§f"+world.getName() )
        .addLore("§7")
        .addLore("§7Найдено всего: §e"+ total )
        .addLore(EntityUtil.EntityGroup.MONSTER.displayName+" §7: §6"+ (count.containsKey(EntityUtil.EntityGroup.MONSTER) ? "§e"+count.get(EntityUtil.EntityGroup.MONSTER) : "не найдено") )
        .addLore(EntityUtil.EntityGroup.CREATURE.displayName+" §7: §6"+ (count.containsKey(EntityUtil.EntityGroup.CREATURE) ? "§e"+count.get(EntityUtil.EntityGroup.CREATURE) : "не найдено") )
        .addLore(EntityUtil.EntityGroup.WATER_CREATURE.displayName+" §7: §6"+ (count.containsKey(EntityUtil.EntityGroup.WATER_CREATURE) ? "§e"+count.get(EntityUtil.EntityGroup.WATER_CREATURE) : "не найдено") )
        .addLore(EntityUtil.EntityGroup.AMBIENT.displayName+" §7: §6"+ (count.containsKey(EntityUtil.EntityGroup.AMBIENT) ? "§e"+count.get(EntityUtil.EntityGroup.AMBIENT) : "не найдено") )
        .addLore(EntityUtil.EntityGroup.WATER_AMBIENT.displayName+" §7: §6"+ (count.containsKey(EntityUtil.EntityGroup.WATER_AMBIENT) ? "§e"+count.get(EntityUtil.EntityGroup.WATER_AMBIENT) : "не найдено") )
        .addLore(EntityUtil.EntityGroup.UNDEFINED.displayName+" §7: §6"+ (count.containsKey(EntityUtil.EntityGroup.UNDEFINED) ? "§e"+count.get(EntityUtil.EntityGroup.UNDEFINED) : "не найдено") )
        .addLore("§7")
        .addLore("§7ЛКМ - подробно по миру")
        .addLore("§7")
        .build(), e -> {
        if (e.isLeftClick()) {
          SmartInventory.builder().id("EntityMain"+p.getName()). provider(new EntityWorldView(world, -1)). size(3, 9). title("§2Сущности "+world.getName()).build() .open(p);
        }
      }));


      count.clear();
      total = 0;


    }

    pagination.setItems(menuEntry.toArray(new ClickableItem[menuEntry.size()]));
    pagination.setItemsPerPage(36);

    contents.set( 5, 4, ClickableItem.of( new ItemBuilder(Material.OAK_DOOR).name("назад").build(), e ->
      p.closeInventory()
    ));

    if (!pagination.isLast()) {
      contents.set(5, 8, ClickableItem.of(ItemUtils.nextPage, e
        -> contents.getHost().open(p, pagination.next().getPage()) )
      );
    }

    if (!pagination.isFirst()) {
      contents.set(5, 0, ClickableItem.of(ItemUtils.previosPage, e
        -> contents.getHost().open(p, pagination.previous().getPage()) )
      );
    }

    pagination.addToIterator(contents.newIterator(SlotIterator.Type.HORIZONTAL, SlotPos.of(0, 0)).allowOverride(false));

  }

}



class EntityWorldView implements InventoryProvider {
  private static final ClickableItem fill = ClickableItem.empty(new ItemBuilder(Material.YELLOW_STAINED_GLASS_PANE).name("§8.").build());
  private int radius;
  private final World world;

  public EntityWorldView(final World world, final int radius) {
    this.radius = radius;
    this.world = world;
  }

  @Override
  public void init(final Player p, final InventoryContent contents) {
    p.playSound(p.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 5, 5);
    contents.fillBorders(fill);

    final Map<EntityUtil.EntityGroup, Integer> count = new HashMap<>();

    EntityUtil.EntityGroup group;

    if (radius > 0) {

      for (final Entity e : p.getNearbyEntities(radius, radius, radius)) {
        if (e.getType() == EntityType.PLAYER) continue;
        group = EntityUtil.group(e);//.VM.getNmsEntitygroup().getEntityType(e);
        if (count.containsKey(group)) {
          count.put(group, count.get(group) + 1);
        } else {
          count.put(group, 1);
        }
      }

    } else {

      for (final Entity e : world.getEntities()) {
        if (e.getType() == EntityType.PLAYER) continue;
        group = EntityUtil.group(e);//group=VM.getNmsEntitygroup().getEntityType(e);
        if (count.containsKey(group)) {
          count.put(group, count.get(group) + 1);
        } else {
          count.put(group, 1);
        }
      }

    }


    contents.set(0, 2, ClickableItem.of( new ItemBuilder(Material.SUNFLOWER)
      .name("§eПКМ - показать все миры")
      .addLore("")
      .addLore(ApiOstrov.isLocalBuilder(p, false) ? "§eПКМ - показать все миры" : "§eВключите режим билдера!")
      .addLore("")
      .build(), e -> {
      if (e.isLeftClick()) {
        //if (ApiOstrov.isLocalBuilder(p, true)) {
          SmartInventory
            .builder()
            .id("EntityWorlds"+p.getName())
            .provider(new EntityServerView(Bukkit.getWorlds()))
            .size(6, 9)
            .title("§2Сущности миров")
            .build()
            .open(p);
        //}
      }
    }));


    contents.set(0, 4, new InputButton(InputButton.InputType.ANVILL, new ItemBuilder(Material.FLOWER_BANNER_PATTERN)
      .name("§7Сущности в мире §a" + world.getName() + (radius > 0 ? " §7в радиусе §a" + radius : ""))
      .addLore("§7")
      .addLore("§fЛКМ - §bуказать радиус")
      .addLore("§7(0 - весь мир)")
      .addLore("§7")
      .build(), "" + radius, imput -> {

      if (!ApiOstrov.isInteger(imput)) {
        p.sendMessage("§cДолжно быть число!");
        return;
      }
      final int r = Integer.parseInt(imput);
      if (r < 0 || r > 100000) {
        p.sendMessage("§cот 0 до 100000!");
        return;
      }
      radius = r;
      reopen(p, contents);
    }));

    int worldLimit;
    for (EntityUtil.EntityGroup g : EntityUtil.EntityGroup.values()) {
      worldLimit = g.getWorldSpawnLimit(world, g);
      contents.add(ClickableItem.of(new ItemBuilder(g.displayMat)
        .name(g.displayName)
        .addLore("§7")
        .addLore("§f" + (count.containsKey(g) ? "§e" + count.get(g) : "не найдено"))
        .addLore("§7")
        .addLore("§7Лимит в настройках мира: §b" + (worldLimit > 0 ? worldLimit : "--"))
        .addLore("§7")
        .addLore("§7ЛКМ - группу подробно")
        .addLore("§7")
        .build(), e -> {
        if (e.isLeftClick()) {
          SmartInventory.builder()
            .id("EntityByGroup" + p.getName())
            .provider(new EntityGroupView(world, radius, g))
            .size(6, 9)
            .title("§2" + world.getName() + " " + g.displayName + " §1r=" + radius)
            .build()
            .open(p);
        }
      }));
    }

  }

}







class EntityGroupView implements InventoryProvider {
  private static final ClickableItem fill = ClickableItem.empty(new ItemBuilder(Material.YELLOW_STAINED_GLASS_PANE).name("§8.").build());
  private final World world;
  private int radius;
  private final EntityUtil.EntityGroup group;

  public EntityGroupView(final World world, final int radius, final EntityUtil.EntityGroup group) {
    this.world = world;
    this.radius = radius;
    this.group = group;
  }

  @Override
  public void init(final Player p, final InventoryContent contents) {
    p.playSound(p.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 5, 5);
    contents.fillRow(4, fill);

    final Pagination pagination = contents.pagination();
    final ArrayList<ClickableItem> menuEntry = new ArrayList<>();

    final ValueSortedMap<EntityType,Integer> count=new ValueSortedMap<>();

    if (radius>0) {

      for (final Entity e : p.getNearbyEntities(radius, radius, radius)) {
        if (e.getType()==EntityType.PLAYER) continue;
        if (EntityUtil.group(e.getType())==group) {
          if (count.containsKey(e.getType())) {
            count.put(e.getType(), count.get(e.getType())+1);
          } else {
            count.put(e.getType(), 1);
          }
        }
      }

    } else {

      for (final Entity e : world.getEntities()) {
        if (e.getType()==EntityType.PLAYER) continue;
        if (EntityUtil.group(e.getType())==group) {
          if (count.containsKey(e.getType())) {
            count.put(e.getType(), count.get(e.getType())+1);
          } else {
            count.put(e.getType(), 1);
          }
        }
      }

    }

    int find;
    for (final EntityType type : count.keySet()) {
      find = count.get(type);
      menuEntry.add(ClickableItem.of(ItemUtils.buildEntityIcon(type)
        .name(Lang.t(type, p))
        .setAmount(find>64 ? 1 : find)
        .addLore("§7")
        .addLore("§7Найдено: §e"+ find )
        .addLore("§7")
        .addLore("§7ЛКМ - подробно по типу")
        .addLore("§7")
        .addLore(ApiOstrov.isLocalBuilder(p, false) ? "§7Шифт+ПКМ - удалить всех мобов этого типа" : "")
        .build(), e -> {
        if (e.isLeftClick()) {
          SmartInventory.builder()
            .id("EntityByType"+p.getName())
            .provider(new EntityTypeView(world, radius, type))
            .size(6, 9)
            .title("§2"+world.getName()+", §6"+type+", §1r="+radius).build()
            .open(p);
        }
      }));
    }

    contents.set(5, 2, new InputButton(InputButton.InputType.ANVILL, new ItemBuilder(Material.FLOWER_BANNER_PATTERN)
      .name("§7Группа"+group.toString()+" в мире §a"+world.getName()+ (radius>0 ? " §7r=§a"+radius : "") )
      .addLore("§7")
      .addLore("§7ЛКМ - изменить радиус")
      .addLore("§7(0 - весь мир)")
      .addLore("§7")
      .build(), ""+radius, imput -> {

      if (!ApiOstrov.isInteger(imput)) {
        p.sendMessage("§cДолжно быть число!");
        return;
      }
      final int r = Integer.parseInt(imput);
      if (r<0 || r>100000) {
        p.sendMessage("§cот 0 до 100000!");
        return;
      }
      radius=r;
      reopen(p, contents);
    }));

    contents.set( 5, 4, ClickableItem.of( new ItemBuilder(Material.OAK_DOOR).name("назад").build(), e ->
      SmartInventory.builder()
        .id("EntityMain"+p.getName())
        . provider(new EntityWorldView(world, radius))
        . size(3, 9)
        . title("§2Сущности "+world.getName()+" §1r="+radius)
        .build()
        .open(p)
    ));

    pagination.setItems(menuEntry.toArray(new ClickableItem[0]));
    pagination.setItemsPerPage(36);

    if (!pagination.isLast()) {
      contents.set(5, 8, ClickableItem.of(ItemUtils.nextPage, e
        -> contents.getHost().open(p, pagination.next().getPage()) )
      );
    }

    if (!pagination.isFirst()) {
      contents.set(5, 0, ClickableItem.of(ItemUtils.previosPage, e
        -> contents.getHost().open(p, pagination.previous().getPage()) )
      );
    }

    pagination.addToIterator(contents.newIterator(SlotIterator.Type.HORIZONTAL, SlotPos.of(0, 0)).allowOverride(false));

  }

}






class EntityTypeView implements InventoryProvider {


  private static final ClickableItem fill = ClickableItem.empty(new ItemBuilder(Material.YELLOW_STAINED_GLASS_PANE).name("§8.").build());
  private final World world;
  private int radius;
  private final EntityType type;


  public EntityTypeView(final World world, final int radius, final EntityType type) {
    this.world = world;
    this.radius = radius;
    this.type = type;
  }

  @Override
  public void init(final Player p, final InventoryContent contents) {
    p.playSound(p.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 5, 5);
    contents.fillRow(4, fill);

    final Pagination pagination = contents.pagination();
    final ArrayList<ClickableItem> menuEntry = new ArrayList<>();


    final Map <Entity, Integer> entitys = new HashMap<>();
    double d;

    if (radius>0) {

      for (final Entity e : p.getNearbyEntities(radius, radius, radius)) {
        if (e.getType()==type) {
          d = p.getLocation().distance(e.getLocation());
          //entitys.put(e, LocationUtil.getDistance(p.getLocation(), e.getLocation()));
          entitys.put(e,(int)Math.floor(d));
        }
      }

    } else {

      for (final Entity e : world.getEntities()) {
        if (e.getType()==type) {
          if (p.getWorld().getName().equals(e.getWorld().getName())) {
            d = p.getLocation().distance(e.getLocation());
            entitys.put(e,(int)Math.floor(d));
          } else {
            entitys.put(e, -1);
          }

        }
      }

    }

    final SortedSet<Integer> distances = new TreeSet<>(entitys.values());

    for (final int dist : distances ) {
      for (final Entity entity : entitys.keySet()) {
        if (entitys.get(entity)==dist) {

          menuEntry.add( ClickableItem.of( new ItemBuilder(Material.ENDER_EYE)
            .name("§f"+ entity.getLocation().getBlockX()+" §7: §f"+entity.getLocation().getBlockY()+" §7: §f"+entity.getLocation().getBlockZ() )
            .addLore("§7Дистанция: "+( entitys.get(entity)==-1 ? "§8другой мир" : "§b"+entitys.get(entity)) )
            .addLore("§7")
            .addLore("§7ЛКМ - ТП к сущности")
            .addLore("§7ПКМ - изменить характеристики")
            .addLore("§7Шифт+ЛКМ - призвать")
            .addLore("§7Шифт+ПКМ - удалить")
            .addLore("§7")
            .build(), e -> {
//Ostrov.log("CLICK="+e.getClick());
            if (!ApiOstrov.isLocalBuilder(p, true)) return;
            switch (e.getClick()) {
              case LEFT -> p.teleport(entity);
              case RIGHT -> SmartInventory.builder()
                .provider(new EntitySetup(entity))
                .size(6, 9)
                .title("§2Характеристики сущности").build()
                .open(p);
              case SHIFT_LEFT -> entity.teleport(p);
              case SHIFT_RIGHT -> entity.remove();
              default -> {}
            }

            if (e.getClick()!=ClickType.RIGHT) reopen(p, contents);
          }));
        }
      }
    }


    pagination.setItems(menuEntry.toArray(new ClickableItem[menuEntry.size()]));
    pagination.setItemsPerPage(36);


    contents.set(5, 2, new InputButton(InputButton.InputType.ANVILL, new ItemBuilder(Material.FLOWER_BANNER_PATTERN)
      .name("§fРадиус: §e"+ (radius>0?radius:" весь мир") )
      .addLore("§7")
      .addLore("§7ЛКМ - изменить радиус")
      .addLore("§7(0 - весь мир)")
      .addLore("§7")
      .build(), ""+radius, imput -> {

      if (!ApiOstrov.isInteger(imput)) {
        p.sendMessage("§cДолжно быть число!");
        return;
      }
      final int r = Integer.parseInt(imput);
      if (r<0 || r>100000) {
        p.sendMessage("§cот 0 до 100000!");
        return;
      }
      radius=r;
      reopen(p, contents);
    }));


    contents.set( 5, 4, ClickableItem.of( new ItemBuilder(Material.OAK_DOOR).name("назад").build(), e ->
      SmartInventory.builder()
        .id("EntityByGroup"+p.getName())
        . provider(new EntityGroupView(world, radius, EntityUtil.group(type)))
        . size(6, 9)
        . title("§2"+world.getName()+" "+type+" §1r="+radius).build() .open(p)
    ));



    if (!pagination.isLast()) {
      contents.set(5, 8, ClickableItem.of(ItemUtils.nextPage, e
        -> contents.getHost().open(p, pagination.next().getPage()) )
      );
    }

    if (!pagination.isFirst()) {
      contents.set(5, 0, ClickableItem.of(ItemUtils.previosPage, e
        -> contents.getHost().open(p, pagination.previous().getPage()) )
      );
    }

    pagination.addToIterator(contents.newIterator(SlotIterator.Type.HORIZONTAL, SlotPos.of(0, 0)).allowOverride(false));

  }

}





















/*
    contents.set(1, 1, ClickableItem.of(new ItemBuilder(Material.ZOMBIE_HEAD)
      .name(EntityUtil.EntityGroup.MONSTER.displayName)
      .addLore("§7")
      .addLore("§f"+  (count.containsKey(EntityUtil.EntityGroup.MONSTER) ? "§e"+count.get(EntityUtil.EntityGroup.MONSTER) : "не найдено") )
      .addLore("§7")
      .addLore("§7Лимит в настройках мира: §b" + (world.getSpawnLimit(SpawnCategory.MONSTER)>0 ? world.getSpawnLimit(SpawnCategory.MONSTER) : "--"))
      .addLore("§7")
      .addLore("§7ЛКМ - группу подробно")
      .addLore("§7")
      .build(), e -> {
      if (e.isLeftClick()) {
        SmartInventory.builder()
          .id("EntityByGroup"+p.getName())
          . provider(new EntityGroupView(world, radius, EntityUtil.EntityGroup.MONSTER))
          . size(6, 9)
          . title("§2"+world.getName()+" "+ EntityUtil.EntityGroup.MONSTER.displayName+" §1r="+radius)
          .build()
          .open(p);
      }
    }));


    contents.set(1, 2, ClickableItem.of(new ItemBuilder(Material.LEATHER_HORSE_ARMOR)
      .name(EntityUtil.EntityGroup.CREATURE.displayName)
      .addLore("§7")
      .addLore("§f"+  (count.containsKey(EntityUtil.EntityGroup.CREATURE) ? "§e"+count.get(EntityUtil.EntityGroup.CREATURE) : "не найдено") )
      .addLore("§7")
      .addLore("§7Лимит в настройках мира: §b" + (world.getSpawnLimit(SpawnCategory.ANIMAL)>0 ? world.getSpawnLimit(SpawnCategory.ANIMAL) : "--"))
      .addLore("§7")
      .addLore("§7ЛКМ - группу подробно")
      .addLore("§7")
      .build(), e -> {
      if (e.isLeftClick()) {
        SmartInventory.builder()
          .id("EntityByGroup"+p.getName())
          . provider(new EntityGroupView(world, radius, EntityUtil.EntityGroup.CREATURE))
          . size(6, 9). title("§2"+world.getName()+" "+ EntityUtil.EntityGroup.CREATURE.displayName+" §1r="+radius)
          .build()
          .open(p);
      }
    }));


    contents.set(1, 3, ClickableItem.of(new ItemBuilder(Material.NAUTILUS_SHELL)
      .name(EntityUtil.EntityGroup.WATER_CREATURE.displayName)
      .addLore("§7")
      .addLore("§f"+  (count.containsKey(EntityUtil.EntityGroup.WATER_CREATURE) ? "§e"+count.get(EntityUtil.EntityGroup.WATER_CREATURE) : "не найдено") )
      .addLore("§7")
      .addLore("§7Лимит в настройках мира: §b" + (world.getSpawnLimit(SpawnCategory.WATER_ANIMAL)>0 ? world.getSpawnLimit(SpawnCategory.WATER_ANIMAL) : "--"))
      .addLore("§7")
      .addLore("§7ЛКМ - группу подробно")
      .addLore("§7")
      .build(), e -> {
      if (e.isLeftClick()) {
        SmartInventory.builder()
          .id("EntityByGroup"+p.getName())
          . provider(new EntityGroupView(world, radius, EntityUtil.EntityGroup.WATER_CREATURE))
          . size(6, 9). title("§2"+world.getName()+" "+ EntityUtil.EntityGroup.WATER_CREATURE.displayName+" §1r="+radius)
          .build()
          .open(p);
      }
    }));


    contents.set(1, 4, ClickableItem.of(new ItemBuilder(Material.COAL)
      .name(EntityUtil.EntityGroup.AMBIENT.displayName)
      .addLore("§7")
      .addLore("§f"+  (count.containsKey(EntityUtil.EntityGroup.AMBIENT) ? "§e"+count.get(EntityUtil.EntityGroup.AMBIENT) : "не найдено") )
      .addLore("§7")
      .addLore("§7Лимит в настройках мира: §b" + (world.getSpawnLimit(SpawnCategory.AMBIENT)>0 ? world.getSpawnLimit(SpawnCategory.AMBIENT) : "--"))
      .addLore("§7")
      .addLore("§7ЛКМ - группу подробно")
      .addLore("§7")
      .build(), e -> {
      if (e.isLeftClick()) {
        SmartInventory.builder()
          .id("EntityByGroup"+p.getName())
          . provider(new EntityGroupView(world, radius, EntityUtil.EntityGroup.AMBIENT))
          . size(6, 9)
          . title("§2"+world.getName()+" "+ EntityUtil.EntityGroup.AMBIENT.displayName+" §1r="+radius)
          .build()
          .open(p);
      }
    }));


    contents.set(1, 5, ClickableItem.of(new ItemBuilder(Material.TROPICAL_FISH)
      .name(EntityUtil.EntityGroup.WATER_AMBIENT.displayName)
      .addLore("§7")
      .addLore("§f"+  (count.containsKey(EntityUtil.EntityGroup.WATER_AMBIENT) ? "§e"+count.get(EntityUtil.EntityGroup.WATER_AMBIENT) : "не найдено") )
      .addLore("§7")
      .addLore("§7Лимит в настройках мира: §b" + (world.getSpawnLimit(SpawnCategory.WATER_AMBIENT)>0 ? world.getSpawnLimit(SpawnCategory.WATER_AMBIENT) : "--"))
      .addLore("§7")
      .addLore("§7ЛКМ - группу подробно")
      .addLore("§7")
      .build(), e -> {
      if (e.isLeftClick()) {
        SmartInventory.builder()
          .id("EntityByGroup"+p.getName())
          . provider(new EntityGroupView(world, radius, EntityUtil.EntityGroup.WATER_AMBIENT))
          . size(6, 9)
          . title("§2"+world.getName()+" "+ EntityUtil.EntityGroup.WATER_AMBIENT.displayName+" §1r="+radius)
          .build()
          .open(p);
      }
    }));


    contents.set(1, 7, ClickableItem.of(new ItemBuilder(Material.ARMOR_STAND)
      .name(EntityUtil.EntityGroup.UNDEFINED.displayName)
      .addLore("§7")
      .addLore("§f"+  (count.containsKey(EntityUtil.EntityGroup.UNDEFINED) ? "§e"+count.get(EntityUtil.EntityGroup.UNDEFINED) : "не найдено") )
      .addLore("§7")
      .addLore("§7ЛКМ - группу подробно")
      .addLore("§7")
      .build(), e -> {
      if (e.isLeftClick()) {
        SmartInventory.builder()
          .id("EntityByGroup"+p.getName())
          . provider(new EntityGroupView(world, radius, EntityUtil.EntityGroup.UNDEFINED))
          . size(6, 9)
          . title("§2"+world.getName()+" "+ EntityUtil.EntityGroup.UNDEFINED.displayName+" §1r="+radius)
          .build()
          .open(p);
      }
    }));
*/
