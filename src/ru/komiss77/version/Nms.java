package ru.komiss77.version;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Predicate;

import com.mojang.brigadier.tree.RootCommandNode;
import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import io.papermc.paper.adventure.PaperAdventure;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.bukkit.*;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_20_R3.block.data.CraftBlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.spigotmc.SpigotConfig;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Ostrov;
import ru.komiss77.modules.bots.BotEntity;
import ru.komiss77.modules.games.GM;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;
import ru.komiss77.modules.world.WXYZ;
import ru.komiss77.modules.world.XYZ;
import ru.komiss77.scoreboard.SubTeam;
import ru.komiss77.utils.FastMath;
import ru.komiss77.utils.LocationUtil;
import ru.komiss77.utils.TCUtils;


public class Nms {
  public static final List<String> vanilaCommandToDisable;
  protected static final BlockPos.MutableBlockPos mutableBlockPosition; //не юзать при отправке пакетов напрямую! Пакет отправляется с задержкой, значение может уже измениться!
  private static final Key chatKey;

  static {
    vanilaCommandToDisable = Arrays.asList("execute",
            "bossbar", "defaultgamemode", "me", "help", "kick", "kill", "tell",
            "say", "spreadplayers", "teammsg", "tellraw", "trigger",
            "ban-ip", "banlist", "ban", "op", "pardon", "pardon-ip", "perf", "save-all", "save-off", "save-on", "setidletimeout", "publish");
    chatKey = Key.key("ostrov_chat", "listener");
    mutableBlockPosition = new BlockPos.MutableBlockPos(0, 0, 0);
  }

  public static boolean isServerStopped() {
    return Craft.toNMS().hasStopped();
  }


  //ЛКМ и ПКМ на фейковый блок будут игнорироваться! Еще можно будет добавить подмену блока при получении чанка
  public static void fakeBlock(final Player p, final Location loc, final BlockData bd) {
    final long l = LocationUtil.asLong(loc);
    fakeBlock(p, l, bd);
  }

  public static void fakeBlock(final Player p, final XYZ xyz, final BlockData bd) {
    final long l = xyz.asLong();
    fakeBlock(p, l, bd);
  }

  public static void fakeBlock(final Player p, final long l, final BlockData bd) {
    final ClientboundBlockUpdatePacket packet = new ClientboundBlockUpdatePacket(BlockPos.of(l), ((CraftBlockData) bd).getState());
    sendPacket(p, packet);//mutableBlockPosition не катит, меняется пока пакет отправляется! надо BlockPos.of(l)
    final Oplayer op = PM.getOplayer(p);
    op.fakeBlock.put(l, bd); //2! это заблочит исходящий пакет обновы
    op.hasFakeBlock = true;
  }

  public static void fakeBlock(final Player p, final Location loc) {
    final Oplayer op = PM.getOplayer(p);
    //mutableBlockPosition.set(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    final long l = LocationUtil.asLong(loc);
    if (op.hasFakeBlock && op.fakeBlock.remove(l) != null) {
      final ClientboundBlockUpdatePacket packet = new ClientboundBlockUpdatePacket(BlockPos.of(l), ((CraftBlockData) loc.getBlock().getBlockData()).getState());
      sendPacket(p, packet);//p.sendBlockChange(loc, loc.getBlock().getBlockData());
      op.hasFakeBlock = !op.fakeBlock.isEmpty();
    }
    //PM.getOplayer(p).fakeBlock.remove(mutableBlockPosition.asLong());
  }

  public static void chatFix() { // Chat Report fix  https://github.com/e-im/FreedomChat https://www.libhunt.com/r/FreedomChat
    final ServerOutPacketHandler handler = new ServerOutPacketHandler();
    //подслушать исходящие от сервера пакеты
    io.papermc.paper.network.ChannelInitializeListenerHolder.addListener(chatKey,
            channel -> channel.pipeline().addAfter("packet_handler", "ostrov_chat_handler", handler)
    );
    Ostrov.log_ok("§bchatFix - блокировка уведомлений подписи чата");
    //код ниже не удалять, может пригодиться
    // final ServerInPacketHandler in = new ServerInPacketHandler(); // -так слушает все исходящие пакеты, а не отдельного игрока
    //io.papermc.paper.network.ChannelInitializeListenerHolder.addListener(
    //  chatKey, ch -> {
//Ostrov.log_warn("afterInitChannel ="+ch.remoteAddress());
    //channel.pipeline().addBefore("packet_handler", "ostrov_"+p.getName(), in);
    // }
    // );

  }


  public static void addPlayerPacketSpy(final Player p, final Oplayer op) {
    final PlayerPacketHandler packetSpy = new PlayerPacketHandler(op);
    final ChannelPipeline pipeline = Craft.toNMS(p).connection.connection.channel.pipeline();////EntityPlayer->PlayerConnection->NetworkManager->Chanell->ChannelPipeli
    pipeline.addBefore("packet_handler", "ostrov_" + p.getName(), packetSpy);
  }


  public static void removePlayerPacketSpy(final Player p) {  //при дисконнекте
    final Channel channel = Craft.toNMS(p).connection.connection.channel; //EntityPlayer->PlayerConnection->NetworkManager->Chanell->ChannelPipeline
    channel.eventLoop().submit(() -> {
      channel.pipeline().remove("ostrov_" + p.getName());
      return null;
    });
  }


  public static void signInput(final Player p, final String suggest, final XYZ signXyz) { //suggest придёт с '&'
    final BlockData bd = Material.OAK_SIGN.createBlockData();
    p.sendBlockChange(signXyz.getCenterLoc(), bd);

    mutableBlockPosition.set(signXyz.x, signXyz.y, signXyz.z);
    final SignBlockEntity sign = new SignBlockEntity(mutableBlockPosition, null);
    final Component[] comps = new Component[4];
    Arrays.fill(comps, Component.empty());
    boolean last = true;
    switch (suggest.length() >> 4) {
      default:
        last = false;
      case 3:
        comps[3] = PaperAdventure.asVanilla(TCUtils.format(suggest.substring(48, last ? suggest.length() : 65)));
        last = false;
      case 2:
        comps[2] = PaperAdventure.asVanilla(TCUtils.format(suggest.substring(32, last ? suggest.length() : 47)));
        last = false;
      case 1:
        comps[1] = PaperAdventure.asVanilla(TCUtils.format(suggest.substring(16, last ? suggest.length() : 31)));
        last = false;
      case 0:
        comps[0] = PaperAdventure.asVanilla(TCUtils.format(suggest.substring(0, last ? suggest.length() : 15)));
        break;
    }

    final SignText signtext = new SignText(comps, comps, DyeColor.WHITE, true);
    sign.setText(signtext, true);//sign.c(signtext);//

    ClientboundBlockEntityDataPacket packet = sign.getUpdatePacket();
    sendPacket(p, packet);// 1201 sendPacket(p, sign.j());

    final ClientboundOpenSignEditorPacket outOpenSignEditor = new ClientboundOpenSignEditorPacket(mutableBlockPosition, true);
    sendPacket(p, outOpenSignEditor);//ep.c.a(outOpenSignEditor);//sendPacket(outOpenSignEditor);
  }


  public static @NotNull Material getFastMat(final World w, int x, int y, int z) {
    final ServerLevel sl = Craft.toNMS(w);
    final BlockState iBlockData = sl.getBlockState(mutableBlockPosition.set(x, y, z));
    return iBlockData.getBukkitMaterial();
  }

  public static @NotNull Material getFastMat(final WXYZ loc) {
    final ServerLevel sl = Craft.toNMS(loc.w);
    final BlockState iBlockData = sl.getBlockState(mutableBlockPosition.set(loc.x, loc.y, loc.z));
    return iBlockData.getBukkitMaterial();
  }

  public static @NotNull Material getFastMat(final Location loc) {
    final ServerLevel sl = Craft.toNMS(loc.getWorld());
    final BlockState iBlockData = sl.getBlockState(mutableBlockPosition.set(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
    return iBlockData.getBukkitMaterial();
  }


  public static void pathServer() {
    final MinecraftServer srv = MinecraftServer.getServer();
    final com.mojang.brigadier.CommandDispatcher<CommandSourceStack> dispatcher = srv.vanillaCommandDispatcher.getDispatcher();
    final RootCommandNode<CommandSourceStack> root = dispatcher.getRoot();

    try {
      Field childrenField = root.getClass().getSuperclass().getDeclaredField("children");
      childrenField.setAccessible(true);

      Field literalsField = root.getClass().getSuperclass().getDeclaredField("literals");
      literalsField.setAccessible(true);

      Field argumentsField = root.getClass().getSuperclass().getDeclaredField("arguments");
      argumentsField.setAccessible(true);

      Map<?, ?> children = (Map<?, ?>) childrenField.get(root);
      Map<?, ?> literals = (Map<?, ?>) literalsField.get(root);
      Map<?, ?> arguments = (Map<?, ?>) argumentsField.get(root);

      //Полученного экземпляра Field уже достаточно для доступа к изменяемым приватным полям.
      vanilaCommandToDisable.forEach((name) -> {
                children.remove(name);
                literals.remove(name);
                arguments.remove(name);
              }
      );

    } catch (IllegalAccessException | IllegalArgumentException | NoSuchFieldException |
             SecurityException ex) { //NoSuchFieldException | SecurityException | IllegalAccessException ex) {
      Ostrov.log_warn("nms Server pathServer : " + ex.getMessage());
    }

    SpigotConfig.belowZeroGenerationInExistingChunks = false;
    SpigotConfig.restartOnCrash = false;
    SpigotConfig.disablePlayerDataSaving = true;
    SpigotConfig.movedWronglyThreshold = 1.6;//Double.MAX_VALUE;
    SpigotConfig.movedTooQuicklyMultiplier = 10;//Double.MAX_VALUE;
    SpigotConfig.sendNamespaced = false;//Bukkit.spigot().getConfig().s
    SpigotConfig.whitelistMessage = "§cНа сервере включен список доступа, и вас там нет!";
    SpigotConfig.unknownCommandMessage = "§cКоманда не найдена. §a§l/menu §f-открыть меню.";
    SpigotConfig.serverFullMessage = "Слишком много народу!";
    SpigotConfig.outdatedClientMessage = "§cВаш клиент устарел! Пожалуйста, используйте §b{0}";
    SpigotConfig.outdatedServerMessage = "§cСервер старой версии {0}, вход невозможен.";
    SpigotConfig.restartMessage = "§4Перезагрузка...";

    switch (GM.GAME) {
      case AR, DA, OB, SK, SG, MI -> {
        SpigotConfig.disableAdvancementSaving = false;
        SpigotConfig.disabledAdvancements = Collections.emptyList();
        SpigotConfig.disableStatSaving = false;
      }
      default -> {
        SpigotConfig.disableAdvancementSaving = true;
        SpigotConfig.disabledAdvancements = Arrays.asList("*", "minecraft:story/disabled");
        SpigotConfig.disableStatSaving = true;
      }
    }

    //отключить устаревшие тайминги
    try {
      final File cfg = new File(Bukkit.getWorldContainer().getPath() + "/config/paper-global.yml");
      final YamlConfiguration yml = YamlConfiguration.loadConfiguration(cfg);
      if (yml.getConfigurationSection("timings") != null) {
        if (yml.getConfigurationSection("timings").getBoolean("enabled")) {
          yml.getConfigurationSection("timings").set("enabled", false);
          yml.save(cfg);
        }
      }
    } catch (IOException | NullPointerException ex) {
      Ostrov.log_err("не удалось изменить timings : " + ex.getMessage());
    }

    Ostrov.log_ok("§bСервер сконфигурирован, отключено ванильных команд: " + vanilaCommandToDisable.size());
  }


  public static void pathWorld(final World w) {
    final ServerLevel ws = Craft.toNMS(w);
    ws.spigotConfig.tileMaxTickTime = 5;
    ws.spigotConfig.entityMaxTickTime = 5;
  }


  public static void pathPermissions() {
    final PluginManager spm = Bukkit.getPluginManager();
    for (Permission dp : spm.getDefaultPermissions(false)) {
      dp.setDefault(PermissionDefault.OP);
    }
  }


  public static int getTps() {
    return MinecraftServer.TPS;
  }


  public static int getitemDespawnRate(final World w) { //skyworld
    return Craft.toNMS(w).spigotConfig.itemDespawnRate;
  }


  public static void sendFakeEquip(final Player p, final int playerInventorySlot, final ItemStack itemStack) {
    final ServerPlayer sp = Craft.toNMS(p);
    sp.connection.send(new ClientboundContainerSetSlotPacket(sp.inventoryMenu.containerId,
            playerInventorySlot, playerInventorySlot, net.minecraft.world.item.ItemStack.fromBukkitCopy(itemStack)));
  }


  public static void sendChunkChange(final Player p, final Chunk chunk) {
    chunk.getWorld().refreshChunk(chunk.getX(), chunk.getZ());
    final ServerLevel ws = Craft.toNMS(p.getWorld());
    final LevelChunk nmsChunk = ws.getChunkIfLoaded(chunk.getX(), chunk.getZ());
    if (nmsChunk == null) return;
    final ClientboundLevelChunkWithLightPacket packet = new ClientboundLevelChunkWithLightPacket(
            nmsChunk, ws.getLightEngine(), null, null, true);
    sendPacket(p, packet);//toNMS(p).c.a(packet);//sendPacket(p, packet);
  }

  public static void setAggro(final Mob le, final boolean aggro) {
    Craft.toNMS(le).setAggressive(aggro);
  }

  //для фигур

  public static void sendLookAtPlayerPacket(final Player p, final Entity e) {
    if (p == null || !p.isOnline() || e == null) {
      return;
    }
    final Vector direction = e.getLocation().toVector().subtract(p.getLocation().toVector()).normalize();
    double vx = direction.getX();
    double vy = direction.getY();
    double vz = direction.getZ();
    final byte yawByte = FastMath.toPackedByte(180f - FastMath.toDegree((float) Math.atan2(vx, vz)) + ApiOstrov.randInt(-10, 10));
    final byte pitchByte = FastMath.toPackedByte(90 - FastMath.toDegree((float) Math.acos(vy)) + (ApiOstrov.randBoolean() ? 10 : -5));
    final ServerPlayer entityPlayer = Craft.toNMS(p);
    final net.minecraft.world.entity.Entity el = Craft.toNMS(e);
    ClientboundRotateHeadPacket head = new ClientboundRotateHeadPacket(el, yawByte);
    entityPlayer.connection.send(head);
    ClientboundMoveEntityPacket.Rot packet = new ClientboundMoveEntityPacket.Rot(e.getEntityId(), yawByte, pitchByte, true);
    entityPlayer.connection.send(packet);
  }

  public static void sendLookResetPacket(final Player p, final Entity e) {
    if (p == null || !p.isOnline() || e == null) {
      return;
    }
    final byte yawByte = FastMath.toPackedByte(e.getLocation().getYaw());//toPackedByte(f.yaw);
    final byte pitchByte = FastMath.toPackedByte(e.getLocation().getPitch());//toPackedByte(f.pitch);
    final ServerPlayer entityPlayer = Craft.toNMS(p);
    final net.minecraft.world.entity.Entity el = Craft.toNMS(e);
    ClientboundRotateHeadPacket head = new ClientboundRotateHeadPacket(el, yawByte);
    entityPlayer.connection.send(head);
    ClientboundMoveEntityPacket.Rot packet = new ClientboundMoveEntityPacket.Rot(e.getEntityId(), yawByte, pitchByte, true);
    entityPlayer.connection.send(packet);
  }

  public static void colorGlow(final Entity e, final NamedTextColor color, final boolean fakeGlow) {
    if (e != null && e.isValid()) {
//      final BotEntity be = BotManager.getBot(ent.getEntityId(), BotEntity.class);
//      final Entity e = be == null ? ent : be.getBukkitEntityRaw();
      new SubTeam(e.getUniqueId().toString()).include(e).color(color).send(e.getWorld());

      if (fakeGlow) {
        final ClientboundSetEntityDataPacket packet = new ClientboundSetEntityDataPacket(e.getEntityId(), Craft.toNMS(e).getEntityData().getNonDefaultValues());
        packet.packedItems().add(new SynchedEntityData.DataValue<>(0, BotEntity.flags.getSerializer(), (byte) 64));
        Nms.sendWorldPackets(e.getWorld(), packet);
      } else {
        e.setGlowing(true);
      }
    }
  }

  public static void colorGlow(final Entity e, final NamedTextColor color, final Predicate<Player> to) {
    if (e != null && e.isValid()) {
//      final BotEntity be = BotManager.getBot(ent.getEntityId(), BotEntity.class);
//      final Entity e = be == null ? ent : be.getBukkitEntityRaw();
      final SubTeam st = new SubTeam(e.getUniqueId().toString()).include(e).color(color);
      final ClientboundSetEntityDataPacket packet = new ClientboundSetEntityDataPacket(e.getEntityId(), Craft.toNMS(e).getEntityData().getNonDefaultValues());
      packet.packedItems().add(new SynchedEntityData.DataValue<>(0, BotEntity.flags.getSerializer(), (byte) 64));

      for (final Player p : e.getWorld().getPlayers()) {
        if (to.test(p)) {
          st.send(p);
          Nms.sendPacket(p, packet);
        }
      }
    }
  }

  public static void sendPacket(final Player p, final Packet<?> packet) {
    Craft.toNMS(p).connection.send(packet);
  }

  public static void sendWorldPacket(final World w, final Packet<?> packet) {
    for (final Player p : w.getPlayers()) Craft.toNMS(p).connection.send(packet);
  }

  public static void sendWorldPacket(final World w, final Predicate<Player> send, final Packet<?> packet) {
    for (final Player p : w.getPlayers()) if (send.test(p)) Craft.toNMS(p).connection.send(packet);
  }

  @SafeVarargs
  public static void sendPackets(final Player p, Packet<ClientGamePacketListener>... packets) {
    Craft.toNMS(p).connection.send(new ClientboundBundlePacket(Arrays.asList(packets)));
  }

  @SafeVarargs
  public static void sendWorldPackets(final World w, Packet<ClientGamePacketListener>... packets) {
    final ClientboundBundlePacket cbp = new ClientboundBundlePacket(Arrays.asList(packets));
    for (Player p : w.getPlayers()) Craft.toNMS(p).connection.send(cbp);
  }

  @SafeVarargs
  public static void sendWorldPackets(final World w, final Predicate<Player> send, Packet<ClientGamePacketListener>... packets) {
    final ClientboundBundlePacket cbp = new ClientboundBundlePacket(Arrays.asList(packets));
    for (Player p : w.getPlayers()) if (send.test(p)) Craft.toNMS(p).connection.send(cbp);
  }


}


//лишнее, предлагаемый текст не надо подсвечивать, так не изменить первый цветовой код
        /*EnumColor color = EnumColor.a;
        if (suggest.length() >= 2 && (suggest.charAt(0) == '§' || suggest.charAt(0) == '&')) {
            switch (suggest.charAt(1)) {
                case '0' -> color = EnumColor.a;
                case '1' -> color = EnumColor.b;
                case '2' -> color = EnumColor.c;
                case '3' -> color = EnumColor.d;
                case '4' -> color = EnumColor.e;
                case '5' -> color = EnumColor.f;
                case '6' -> color = EnumColor.g;
                case '7' -> color = EnumColor.h;
                case '8' -> color = EnumColor.i;
                case '9' -> color = EnumColor.j;
                case 'a' -> color = EnumColor.k;
                case 'b' -> color = EnumColor.l;
                case 'c' -> color = EnumColor.m;
                case 'd' -> color = EnumColor.n;
                case 'e' -> color = EnumColor.o;
                case 'f' -> color = EnumColor.p;
            }
            suggest = suggest.substring(2);
        }*/







  /*
  private static Method getNmsMethod(final String path, final String methodName) {
      try {
          return Class.forName(Bukkit.getServer().getClass().getPackageName() + path).getDeclaredMethod(methodName);
      } catch (NoSuchMethodException | SecurityException | ClassNotFoundException ex) {
          Ostrov.log_err("Server getNmsMethod : "+ex.getMessage());
          //e.printStackTrace();
          return null;
      }
  }


  private static Field getIdFld(final Class<?> cls) {
      final Field fld = cls.getDeclaredFields()[0];
      fld.setAccessible(true);
      return fld;
  }

  /*
  @Override
  public ServerLel toNMS(final World w) {
      try {
          return (ServerLevel) CraftWorldMethod.invoke(w);
      } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
          return null;
      }
  }


  @Override
  public net.minecraft.world.entity.Entity toNMS(final Entity en) {
      try {
          return (net.minecraft.world.entity.Entity) CraftEntityMethod.invoke(en);
      } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
          return null;
      }
  }


  @Override
  public EntityLiving toNMS(final LivingEntity le) {
      try {
          return (EntityLiving) CraftLivingEntityMethod.invoke(le);
      } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
          return null;
      }
  }


  @Override
  public ServerPlayer toNMS(final Player p) {
      try {
          return (ServerPlayer) CraftPlayerMethod.invoke(p);
      } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
          return null;
      }
  }


  @Override
  public DedicatedServer toNMS() {
      return nmsServer;
  }

      @Override
  public byte[] encodeBase64(byte[] binaryData) {
      return org.apache.commons.codec.binary.Base64.encodeBase64(binaryData);
  }



  @Override
  public void chatFix() { // Chat Report fix  https://github.com/e-im/FreedomChat https://www.libhunt.com/r/FreedomChat
      final ServerOutPacketHandler handler = new ServerOutPacketHandler();
      //подслушать исходящие от сервера пакеты
      io.papermc.paper.network.ChannelInitializeListenerHolder.addListener(
              chatKey,
              channel -> channel.pipeline().addAfter("packet_handler", "ostrov_chat_handler", handler)
      );
      Ostrov.log_ok("§bchatFix - блокировка уведомлений подписи чата");
  }


    @Override
    public void addPacketSpy () {
       // final In handler = new In();  -так слушает все пакеты, а не отдельного игрока
       // io.papermc.paper.network.ChannelInitializeListenerHolder.addListener(
       //         chatKey, channel -> channel.pipeline().addBefore("packet_handler", "ostrov_spy", handler)
       // );
    }
       */
    /*
    @Override //добавляется в bungeeDataHandler
    public PlayerPacketHandler addPacketSpy (final Player p, final Oplayer op) {
        final PlayerPacketHandler packetSpy = new PlayerPacketHandler(op);
        final ChannelPipeline pipeline = ((CraftPlayer)p).getHandle().connection.connection.channel.pipeline();////EntityPlayer->PlayerConnection->NetworkManager->Chanell->ChannelPipeline
Connection packet_handler = (Connection) pipeline.get("packet_handler");
Ostrov.log("---- addPacketSpy packet_handler="+packet_handler);
      pipeline.addBefore("packet_handler", "ostrov_"+p.getName(), packetSpy);
        return packetSpy;
    }


    @Override
    public void removePacketSpy (final Player p) {  //при дисконнекте
        final Channel channel = ((CraftPlayer)p).getHandle().connection.connection.channel; //EntityPlayer->PlayerConnection->NetworkManager->Chanell->ChannelPipeline
        channel.eventLoop().submit(() -> {
            channel.pipeline().remove("ostrov_"+p.getName());
            return null;
        });
    }
*/

     /*
    @Override
    public void sendPacket(final Player p, final Packet<?> packet) {
        ((CraftPlayer)p).getHandle().connection.send(packet);
    }


    @Override
    @SafeVarargs
    public final void sendWorldPackets(final World w, final Packet... ps) {
        if (ps.length == 1) {
            final Packet packet = ps[0];
            for (Player p : w.getPlayers()) {  //for (final EntityPlayer ep : ((WorldServer) w).x()) {
                sendPacket(p, packet);//toNMS(p).c.c.a(packet);//ep.c.c.a(packet);
            }
        } else {
            final ClientboundBundlePacket packets = new ClientboundBundlePacket(Arrays.asList(ps));
            for (Player p : w.getPlayers()) { //for (final EntityPlayer ep : ((WorldServer) w).x()) {
                sendPacket(p, packets);//toNMS(p).c.c.a(packets);//ep.c.c.a(packets);
            }
        }
    }*/
