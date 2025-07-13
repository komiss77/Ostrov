package ru.komiss77.version;

import java.util.Optional;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.advancements.*;
import net.minecraft.core.ClientAsset;
import net.minecraft.network.protocol.game.ClientboundUpdateAdvancementsPacket;
import net.minecraft.resources.ResourceLocation;
import io.papermc.paper.adventure.PaperAdventure;
import net.kyori.adventure.text.format.ShadowColor;
import net.kyori.adventure.text.Component;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import eu.endercentral.crazy_advancements.advancement.Advancement;
import eu.endercentral.crazy_advancements.NameKey;
import eu.endercentral.crazy_advancements.advancement.AdvancementDisplay;
import eu.endercentral.crazy_advancements.advancement.AdvancementFlag;
import eu.endercentral.crazy_advancements.advancement.ToastNotification;
import ru.komiss77.Ostrov;

//https://github.com/ZockerAxel/CrazyAdvancementsAPI
//https://github.com/Romindous/AdvanceAPI/tree/master

public class CrazyAdv {

  private static final AdvancementRewards advancementRewards =
      new AdvancementRewards(0, new ArrayList<>(), new ArrayList<>(), Optional.empty());
  private static final HashMap<NameKey, Float> smallestX = new HashMap<>();
  private static final HashMap<NameKey, Float> smallestY = new HashMap<>();


  public static void setSmallestX(NameKey tab, float smallestX) {
    CrazyAdv.smallestX.put(tab, smallestX);
  }

  public static float getSmallestX(NameKey key) {
    return smallestX.containsKey(key) ? smallestX.get(key) : 0;
  }

  public static void setSmallestY(NameKey tab, float smallestY) {
    CrazyAdv.smallestY.put(tab, smallestY);
  }

  public static float getSmallestY(NameKey key) {
    return smallestY.containsKey(key) ? smallestY.get(key) : 0;
  }

  public static float generateX(NameKey tab, float displayX) {
    return displayX - getSmallestX(tab);
  }

  public static float generateY(NameKey tab, float displayY) {
    return displayY - getSmallestY(tab);
  }


  public static net.minecraft.advancements.Advancement toNmsAdvancement(Advancement advancement) {
    final AdvancementDisplay display = advancement.getDisplay();

    final net.minecraft.world.item.ItemStack icon = CraftItemStack.asNMSCopy(display.getIcon());

    final NameKey back = display.background();
//Ostrov.log_warn("=========== back="+back);
    final Optional<ClientAsset> backgroundTexture = back == null
        ? Optional.empty() : Optional.of(new ClientAsset(back.getMinecraftKey()));
//if (back != null) Ostrov.log_warn("=========== backgroundTexture="+backgroundTexture.get());

    final Optional<ResourceLocation> parent = advancement.isRoot() ? Optional.empty()
        : Optional.of(advancement.getParent().getName().getMinecraftKey());

    float x = generateX(advancement.getTab(), display.generateX());
    float y = generateY(advancement.getTab(), display.generateY());

    final DisplayInfo advDisplay = new DisplayInfo(icon, PaperAdventure.asVanilla(advancement.isRoot() ? display.title().shadowColor(ShadowColor
        .shadowColor(0, 0, 0, 255)) : display.title()), PaperAdventure.asVanilla(display.description()), backgroundTexture,
        display.getFrame().getNMS(), false, false, advancement.hasFlag(AdvancementFlag.SEND_WITH_HIDDEN_BOOLEAN));
    advDisplay.setLocation(x, y);

    return new net.minecraft.advancements.Advancement(parent, Optional.of(advDisplay), advancementRewards,
        advancement.getCriteria().getCriteria(), advancement.getCriteria().getAdvancementRequirements(), false);
  }

  public static net.minecraft.advancements.Advancement toNmsToastAdvancement(ToastNotification notification) {
    net.minecraft.world.item.ItemStack icon = CraftItemStack.asNMSCopy(notification.getIcon());

    DisplayInfo advDisplay = new DisplayInfo(icon, PaperAdventure.asVanilla(notification.message()), PaperAdventure.asVanilla(Component.text("Toast Notification")),
        Optional.empty(), notification.getFrame().getNMS(), true, false, true);

    return new net.minecraft.advancements.Advancement(Optional.empty(), Optional.of(advDisplay), advancementRewards,
        ToastNotification.NOTIFICATION_CRITERIA.getCriteria(), ToastNotification.NOTIFICATION_CRITERIA.getAdvancementRequirements(), false);
  }


  public static void sendAdvPacket(Player player, boolean reset, List<Advancement> advancements, List<NameKey> removedAdvancements) {
    AdvancementsPacket packet = new AdvancementsPacket(player, reset, advancements, removedAdvancements);
    packet.send();
  }

  public static void sendToastPacket(Player player, boolean add, ToastNotification notification) {
    new ToastPacket(player, add, notification).send();
  }

}


class AdvancementsPacket {
  private final Player player;
  private final boolean reset;
  private final List<Advancement> advancements;
  private final List<NameKey> removedAdvancements;

  public AdvancementsPacket(Player player, boolean reset, List<Advancement> advancements, List<NameKey> removedAdvancements) {
    this.player = player;
    this.reset = reset;
    this.advancements = advancements == null ? new ArrayList<>() : new ArrayList<>(advancements);
    this.removedAdvancements = removedAdvancements == null ? new ArrayList<>() : new ArrayList<>(removedAdvancements);
  }

  public Player getPlayer() {
    return player;
  }

  public boolean isReset() {
    return reset;
  }

  public List<Advancement> getAdvancements() {
    return new ArrayList<>(advancements);
  }

  public List<NameKey> getRemovedAdvancements() {
    return new ArrayList<>(removedAdvancements);
  }

  public ClientboundUpdateAdvancementsPacket build() {
    //Create Lists
    List<net.minecraft.advancements.AdvancementHolder> advancements = new ArrayList<>();
    Set<ResourceLocation> removedAdvancements = new HashSet<>();
    Map<ResourceLocation, AdvancementProgress> progress = new HashMap<>();

    //Populate Lists
    for (Advancement advancement : this.advancements) {
      net.minecraft.advancements.Advancement nmsAdvancement = convertAdvancement(advancement);
      advancements.add(new AdvancementHolder(advancement.getName().getMinecraftKey(), nmsAdvancement));
      progress.put(advancement.getName().getMinecraftKey(), advancement.getProgress(getPlayer()).getNmsProgress());
    }
    for (NameKey removed : this.removedAdvancements) {
      removedAdvancements.add(removed.getMinecraftKey());
    }

    //Create Packet
    ClientboundUpdateAdvancementsPacket packet = new ClientboundUpdateAdvancementsPacket(isReset(), advancements, removedAdvancements, progress, true);
    return packet;
  }

  protected net.minecraft.advancements.Advancement convertAdvancement(Advancement advancement) {
    return CrazyAdv.toNmsAdvancement(advancement);
  }

  public void send() {
    ClientboundUpdateAdvancementsPacket packet = build();
    ((CraftPlayer) getPlayer()).getHandle().connection.send(packet, null);
  }
}


class ToastPacket {
  private final Player player;
  private final boolean add;
  private final ToastNotification notification;

  public ToastPacket(Player player, boolean add, ToastNotification notification) {
    this.player = player;
    this.add = add;
    this.notification = notification;
  }

  public Player getPlayer() {
    return player;
  }

  public boolean isAdd() {
    return add;
  }

  public ToastNotification getNotification() {
    return notification;
  }

  public ClientboundUpdateAdvancementsPacket build() {
    //Create Lists
    List<AdvancementHolder> advancements = new ArrayList<>();
    Set<ResourceLocation> removedAdvancements = new HashSet<>();
    Map<ResourceLocation, AdvancementProgress> progress = new HashMap<>();

    //Populate Lists
    if (add) {
      advancements.add(new AdvancementHolder(ToastNotification.NOTIFICATION_NAME.getMinecraftKey(), CrazyAdv.toNmsToastAdvancement(getNotification())));
      progress.put(ToastNotification.NOTIFICATION_NAME.getMinecraftKey(), ToastNotification.NOTIFICATION_PROGRESS.getNmsProgress());
    } else {
      removedAdvancements.add(ToastNotification.NOTIFICATION_NAME.getMinecraftKey());
    }

    //Create Packet
    ClientboundUpdateAdvancementsPacket packet = new ClientboundUpdateAdvancementsPacket(false, advancements, removedAdvancements, progress, true);
    return packet;
  }

  public void send() {
    ClientboundUpdateAdvancementsPacket packet = build();
    ((CraftPlayer) getPlayer()).getHandle().connection.send(packet);
  }

}


/*
   class VisibilityAdvancementsPacket extends AdvancementsPacket {

    private static List<Advancement> stripInvisibleAdvancements(Player player, List<Advancement> advancements) {
      Iterator<Advancement> advancementsIterator = advancements.iterator();

      while(advancementsIterator.hasNext()) {
        Advancement advancement = advancementsIterator.next();
        AdvancementDisplay display = advancement.getDisplay();

        boolean visible = display.isVisible(player, advancement);
        advancement.saveVisibilityStatus(player, visible);
        if(!visible) {
          advancementsIterator.remove();
        }
      }

      return advancements;
    }

    public VisibilityAdvancementsPacket(Player player, boolean reset, List<Advancement> advancements, List<NameKey> removedAdvancements) {
      super(player, reset, stripInvisibleAdvancements(player, advancements), removedAdvancements);
    }

  }

*/



