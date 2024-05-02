package ru.komiss77.hook;

import java.io.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.google.common.io.ByteArrayDataInput;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Ostrov;
import ru.komiss77.Timer;
import ru.komiss77.enums.Chanell;
import ru.komiss77.enums.Data;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.ItemUtils;
import ru.komiss77.utils.TCUtils;
import ru.komiss77.utils.inventory.*;

public class SkinRestorerHook {

  protected static final String TEXTURES_NAME;
  private static final Map<Integer, Map<String, String>> cache;

  static {
    TEXTURES_NAME = "textures";
    cache = new HashMap<>();
  }

  public static void openGui (final Player p, final int page) {
    final Map<String, String> skinList = cache.get(page);
    if (skinList == null) {
      requestPage(p, page);
      return;
    }
    SmartInventory
      .builder()
      .id(p.getName()+"skins")
      .provider(new SkinGui(page, skinList))
      .size(6, 9)
      .title("§fНастройка скина"+(page==0?"":" §7стр."+page))
      .build()
      .open(p);
  }

  public static void onMsg(final Player p, final ByteArrayDataInput in) {
    if (p==null || !p.isOnline()) {
      return;
    }

      String subChannel = in.readUTF();

      if (subChannel.equalsIgnoreCase("returnSkinsV3")) {
        in.readUTF(); //playerName
        final int page = in.readInt();
        final short len = in.readShort();
//Ostrov.log("onMsg subChannel="+subChannel+" name="+p.getName()+" page="+page);

        final byte[] msgBytes = new byte[len];
        in.readFully(msgBytes);
        final Map<String, String> skinList = convertToMap(msgBytes);
        cache.put(page, skinList);
        openGui(p, page);


      } else if (subChannel.equalsIgnoreCase("SkinUpdateV2")) {

        if (p.getVehicle() != null) {
          p.getVehicle().removePassenger(p);
        }
        final PlayerProfile profile = p.getPlayerProfile();
        profile.getProperties().removeIf(profileProperty -> profileProperty.getName().equals(TEXTURES_NAME));
        final String value = in.readUTF();
        final String signature = in.readUTF();
        profile.getProperties().add(new ProfileProperty(TEXTURES_NAME, value, signature));
        p.setPlayerProfile(profile);
        //hideAndShow - вроде не надо в папер
      }


  }

  private static Map<String, String> convertToMap(byte[] byteArr) {
    try {
      DataInputStream ois = new DataInputStream(new GZIPInputStream(new ByteArrayInputStream(byteArr)));
      int size = ois.readInt();
      Map<String, String> map = new LinkedHashMap<>(size);
      for (int i = 0; i < size; i++) {
        String key = ois.readUTF();
        String value = ois.readUTF();
        map.put(key, value);
      }
      return map;
    } catch (IOException e) {
      e.printStackTrace();
      return Collections.emptyMap();
    }
  }


  public static void requestPage(final Player p, final int page) {
    final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    final DataOutputStream out = new DataOutputStream(bytes);
    try {
      out.writeUTF("getSkins");
      out.writeUTF(p.getName());
      out.writeInt(page);
      p.sendPluginMessage(Ostrov.instance, Chanell.SKIN.name, bytes.toByteArray());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static void setSkin(final Player p, final String skinName) {
    if (Timer.has(p, "skin")) {
      p.sendMessage("§6Вы сможете сменить скин через "+Timer.getLeft(p, "skin")+" сек.!");
      return;
    } else {
      Timer.add(p, "skin", 20);
    }
    final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    final DataOutputStream out = new DataOutputStream(bytes);
    try {
      out.writeUTF("setSkin");
      out.writeUTF(p.getName());
      out.writeUTF(skinName);
      p.sendPluginMessage(Ostrov.instance, Chanell.SKIN.name, bytes.toByteArray());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static void resetSkin(final Player p) {
    final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    final DataOutputStream out = new DataOutputStream(bytes);
    try {
      out.writeUTF("clearSkin");
      out.writeUTF(p.getName());
      p.sendPluginMessage(Ostrov.instance, Chanell.SKIN.name, bytes.toByteArray());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

}















 class SkinGui implements InventoryProvider {

    private int page;
    final Map<String, String> skinList;

  private static final ClickableItem fill = ClickableItem.empty(new ItemBuilder(Material.NETHER_SPROUTS).name("§8.").build());


  public SkinGui(final int page, final Map<String, String> skinList) {
    this.page = page;
    this.skinList = skinList;
  }



  @Override
  public void init(final Player p, final InventoryContent content) {
    p.playSound(p.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 5, 5);

    content.fillRow(4, fill);



    if (skinList.isEmpty()) {

      content.add(ClickableItem.empty(new ItemBuilder(Material.GLASS_BOTTLE)
        .name("§7нет записей!")
        .build()
      ));

    } else {

      int skinCount = 0;
      for (Map.Entry<String,String> en : skinList.entrySet()) {
        if (skinCount >= 36) {
          Ostrov.log_warn("SkinsGUI: Skin count is more than 36, skipping...");
          break;
        }
        final String skinName = en.getKey();
        final ItemStack is = new ItemBuilder(Material.PLAYER_HEAD)
          .name(skinName)
          .addLore("ЛКМ - посмотреть на сайте")
          .addLore("ПКМ - одеть")
          .setCustomHeadTexture(en.getValue())
          .build();

        content.add(ClickableItem.of( is, e-> {
            if (e.getClick() == ClickType.LEFT) {
              p.closeInventory();
              //https://ru.namemc.com/profile/Whaut
              p.sendMessage(Component.text("§f§l* Клик сюда - посмотреть на сайте *", NamedTextColor.WHITE)
                .hoverEvent(HoverEvent.showText(Component.text("§f§l* Клик сюда - посмотреть на сайте *")))
                .clickEvent(ClickEvent.openUrl("https://ru.namemc.com/profile/"+skinName)));
            } else if (e.getClick() == ClickType.RIGHT) {
                SkinRestorerHook.setSkin(p, skinName);
            }
          } )
        );
        skinCount++;
      }

    }

    content.set(5, 2,  new InputButton( InputButton.InputType.ANVILL, new ItemBuilder(Material.NAME_TAG)
        .name("§3Скин по нику")
        .addLore("")
        .addLore("§7Ввести название")
        .addLore("§7лицензионного аккаунта")
        .addLore("")
        .build(),  "alex", msg -> {

        if (msg.length()>16) {
          p.sendMessage("§сНе более 16 символов!");
          return;
        }
        if (!ApiOstrov.checkString(msg, true, false)) {
          p.sendMessage("§сНедопустимые символы! Можно только A-Z/a-z/0-9");
          return;
        }
        SkinRestorerHook.setSkin(p, msg);
      }
      )
    );

    final ItemStack is = new ItemBuilder(Material.REDSTONE)
      .name("§6Удалить скин")
      .build();

    content.set(5, 6, ClickableItem.of( is, e-> {
        if (e.getClick() == ClickType.LEFT) {
          p.closeInventory();
          SkinRestorerHook.resetSkin(p);
        }
      })
    );

    if (page>0) {
      content.set(5, 0, ClickableItem.of(ItemUtils.previosPage, e
          -> {
        SkinRestorerHook.openGui(p, page-1);
        })
      );
    }

    if (page<999 && skinList.size()==36) {
      content.set(5, 8, ClickableItem.of(ItemUtils.nextPage, e
          -> {
          SkinRestorerHook.openGui(p, page+1);
        }
      ));
    }


    }









}
