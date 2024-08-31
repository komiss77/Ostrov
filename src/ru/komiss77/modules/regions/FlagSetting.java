package ru.komiss77.modules.regions;

import java.util.Set;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.protection.flags.BooleanFlag;
import com.sk89q.worldguard.protection.flags.DoubleFlag;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.FlagContext;
import com.sk89q.worldguard.protection.flags.IntegerFlag;
import com.sk89q.worldguard.protection.flags.InvalidFlagFormat;
import com.sk89q.worldguard.protection.flags.SetFlag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.StringFlag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Ostrov;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.PlayerInput;
import ru.komiss77.utils.inventory.ClickableItem;
import ru.komiss77.utils.inventory.InputButton;
import ru.komiss77.utils.inventory.InputButton.InputType;
import ru.komiss77.utils.inventory.InventoryContent;

public class FlagSetting {

  public final String name;
  public String displayname;
  public boolean enabled;
  public Material iconMat;
  private final FlagInputType inputType;

  public FlagSetting(final Flag f, final String displayname, final Material iconMat, final boolean enabled) {
    name = f.getName();
    this.displayname = displayname;
    this.iconMat = iconMat;
    this.enabled = enabled;

    if (f instanceof StringFlag) {
      inputType = FlagInputType.STRING;
    } else if (f instanceof StateFlag) {
      inputType = FlagInputType.STATE;
    } else if (f instanceof SetFlag) { // HashSet
      inputType = FlagInputType.SET;
    } else if (f instanceof IntegerFlag) {
      inputType = FlagInputType.INTEGER;
    } else if (f instanceof DoubleFlag) {
      inputType = FlagInputType.DOUBLE;
    } else if (f instanceof BooleanFlag) {
      inputType = FlagInputType.BOOLEAN;
    } else {
      inputType = FlagInputType.OTHER;
    }
  }

  public static ClickableItem button(final Player p, final Flag f, final ProtectedRegion region, final InventoryContent contents) {
    final FlagSetting fs = RM.flags.get(f);
    //if (fs == null) {

    //}
    //String menuEntryname = "";
    Material mat = fs.iconMat;
    if (region.getFlags().containsKey(f)) {

      switch (fs.inputType) {

        case STATE -> {
          if (region.getFlag(f) == StateFlag.State.DENY) {
            mat = Material.PINK_DYE; //Piif (TCUtil.canChangeColor(menuEntry.getType())) menuEntry = TCUtil.changeColor(menuEntry, DyeColor.PINK);
          } else {
            mat = Material.LIME_DYE; //if (TCUtil.canChangeColor(menuEntry.getType())) menuEntry = TCUtil.changeColor(menuEntry, DyeColor.LIME);
          }
        }

        case BOOLEAN -> {
          if ((boolean) region.getFlag(f)) {
            mat = Material.PINK_DYE; //if (TCUtil.canChangeColor(menuEntry.getType())) menuEntry = TCUtil.changeColor(menuEntry, DyeColor.PINK);
          } else {
            mat = Material.LIME_DYE; //if (TCUtil.canChangeColor(menuEntry.getType())) menuEntry = TCUtil.changeColor(menuEntry, DyeColor.LIME);
          }
        }

        default -> {
          mat = Material.LIGHT_BLUE_DYE; //if (TCUtil.canChangeColor(menuEntry.getType())) menuEntry = TCUtil.changeColor(menuEntry, DyeColor.BLUE);
        }
      }

    } else {

      // if (TCUtil.canChangeColor(menuEntry.getType())) {
      //      menuEntry = TCUtil.changeColor(menuEntry, DyeColor.GRAY);
      //  }

    }

    final boolean hasPerm = ApiOstrov.isLocalBuilder(p, false) //чекать билдера, или кидает отрицательные права тоже!
        || (p.hasPermission("regiongui.flag." + fs.name) || p.hasPermission("regiongui.flag.all"))
        && !p.hasPermission("-regiongui.flag." + fs.name);

    ItemStack is = new ItemBuilder(mat == null ? Material.GRAY_DYE : mat)
        .name("§7" + fs.displayname)
        .flags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS)
        .lore("")
        .lore(getCurrentValue(region, f))
        .lore("")
        .lore(hasPerm ? "§6ЛКМ§7-изменить состояние" : "§cнет права менять")
        .lore(hasPerm && region.getFlags().containsKey(f) ? "§6ПКМ§7-сброс (сделать по умолчанию)" : "")
        .build();

    if (hasPerm) {

      return ClickableItem.of(is, e -> {

        if (e.getClick() == ClickType.RIGHT && region.getFlags().containsKey(f)) {

          region.setFlag(f, null);
          contents.getHost().getProvider().reopen(p, contents);

        } else {

          switch (fs.inputType) {
            case DOUBLE, INTEGER, STRING -> {
              p.closeInventory();
              new InputButton(InputType.ANVILL, new ItemStack(Material.STONE), "Flag", value -> {
                setFlag(p, region, f, value);
                Bukkit.getScheduler().runTaskLater(Ostrov.getInstance(), () -> contents.getHost().getProvider().reopen(p, contents), 1L);
                //Ostrov.sync(() -> contents.getHost().getProvider().reopen(p, contents), 1);
                //}).run(new InventoryClickEvent(p.getOpenInventory(), SlotType.CONTAINER, 0, ClickType.LEFT, InventoryAction.PICKUP_ALL)
              });
            }

            case SET -> {
              p.sendMessage("§fНаберите в чате новое значение для флага и нажмите Enter");
              PlayerInput.get(InputButton.InputType.CHAT, p, value -> {
                setFlag(p, region, f, value);
                Bukkit.getScheduler().runTaskLater(Ostrov.getInstance(), () -> contents.getHost().getProvider().reopen(p, contents), 1L);
              }, "");
            }
            case BOOLEAN, STATE -> {
              switchState(region, f);
              contents.getHost().getProvider().reopen(p, contents);
            }
            default -> {
            }

          }
        }
      });

    } else {

      return ClickableItem.empty(is);

    }
  }


  private static void switchState(final ProtectedRegion region, final Flag f) {
    final FlagSetting fs = RM.flags.get(f);
    if (fs == null) {
      return;
    }
    if (fs.inputType == FlagInputType.STATE) {

      final StateFlag stateFlag = (StateFlag) f;
      if (region.getFlags().containsKey(f)) {
        if (region.getFlag(stateFlag) == StateFlag.State.DENY) {
          region.setFlag(stateFlag, StateFlag.State.ALLOW);
          //player.sendMessage(Language.FLAG_ALLOWED.toString().replace("%flag%", this.getName()));
        } else {
          region.setFlag(stateFlag, StateFlag.State.DENY);
          //player.sendMessage(Language.FLAG_DENIED.toString().replace("%flag%", this.getName()));
        }
      } else {
        region.setFlag(stateFlag, StateFlag.State.ALLOW);
        //player.sendMessage(Language.FLAG_ALLOWED.toString().replace("%flag%", this.getName()));
      }

    } else {
      final BooleanFlag booleanFlag = (BooleanFlag) f;
      if (region.getFlags().containsKey(f)) {
        if (!(boolean) region.getFlag(booleanFlag)) {
          region.setFlag(booleanFlag, true);
          //player.sendMessage(Language.FLAG_ALLOWED.toString().replace("%flag%", this.getName()));
        } else {
          region.setFlag(booleanFlag, false);
          //player.sendMessage(Language.FLAG_DENIED.toString().replace("%flag%", this.getName()));
        }
      } else {
        region.setFlag(booleanFlag, true);
        //player.sendMessage(Language.FLAG_ALLOWED.toString().replace("%flag%", this.getName()));
      }
    }
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  public static String getCurrentValue(final ProtectedRegion region, final Flag f) {
    if (!region.getFlags().containsKey(f)) {
      return "§8Неактивен";
    }
    final FlagSetting fs = RM.flags.get(f);
    if (fs == null) {
      return "§8Флага нет в базе";
    }
//System.out.println( "getCurrentValue flag="+getFlag().getName()+" inputType="+inputType  );
    switch (fs.inputType) {
      case BOOLEAN:
        return (boolean) region.getFlag(f) ? "§2Да" : "§4Нет";

      case STATE:
        return region.getFlag(f) == StateFlag.State.ALLOW ? "§2Да" : "§4Нет";

      case DOUBLE:
        //return F.name(new StringBuilder().append((double)region.getFlag((Flag)flag)).toString());
        return "" + (double) region.getFlag((DoubleFlag) f);

      case INTEGER:
        //return F.name(new StringBuilder().append((int)region.getFlag((Flag)flag)).toString());
        return "" + (int) region.getFlag((IntegerFlag) f);

      case STRING:
        // return F.name(((String)region.getFlag((Flag)flag)).toString());
        return (String) region.getFlag(f);

      case SET:
        //return F.format((Iterable)region.getFlag(flag), ",", "none");
        SetFlag<?> var2 = (SetFlag<?>) f;
        return (String) ((Set) region.getFlag(var2)).stream().collect(Collectors.joining(",", "[", "]"));

      case OTHER:
      default:
        //return "§8Неопределён";
        return region.getFlag(f).toString();

    }
  }

  public static String suggestValue(final ProtectedRegion region, final Flag f) {

    final FlagSetting fs = RM.flags.get(f);
    if (fs == null) {
      return "§8Флага нет в базе";
    }
    final boolean isPresent = region.getFlags().containsKey(f);
    //if (!region.getFlags().containsKey(flag)) {
    //    return "§8Неактивен";
    //}

//System.out.println( "getCurrentValue flag="+getFlag().getName()+" inputType="+inputType  );
    switch (fs.inputType) {

      case DOUBLE:
      case INTEGER:
        if (isPresent) {
          return getCurrentValue(region, f);
        } else {
          return "0";
        }

      case STRING:
        if (isPresent) {
          return getCurrentValue(region, f);
        } else {
          return "";
        }

      case OTHER:
      default:
        return "новое значение";

    }
  }


  //protected static <V> void setFlag(final ProtectedRegion region, final Flag<V> flag, final Actor sender, final String value) throws InvalidFlagFormat {
  protected static <V> void setFlag(final Player player, final ProtectedRegion region, final Flag<V> flag, final String value) {
    try {
      region.setFlag(flag, flag.parseInput(FlagContext.create().setSender(BukkitAdapter.adapt(player)).setInput(value).setObject("region", region).build()));
    } catch (InvalidFlagFormat ex) {
      player.sendMessage("§cНедопустимое значение : " + ex.getMessage());
    }
  }

  //public String getName() {
  //    return this.displayname;
  // }

  //@Override
  // public int compareTo(final FlagSetting other) {
  //     return getId().compareTo(other.getId());
  // }

  //public Permission getPermission() {
  //    return this.permission;
  //}
  enum FlagInputType {
    BOOLEAN,
    STATE,
    INTEGER,
    DOUBLE,
    STRING,
    SET,
    OTHER;
  }
}
