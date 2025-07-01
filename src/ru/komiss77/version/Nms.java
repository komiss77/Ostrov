package ru.komiss77.version;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Predicate;
import com.mojang.brigadier.tree.RootCommandNode;
import com.mojang.datafixers.DataFixer;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import io.papermc.paper.adventure.PaperAdventure;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.DeathProtection;
import io.papermc.paper.math.BlockPosition;
import io.papermc.paper.persistence.PaperPersistentDataContainerView;
import io.papermc.paper.persistence.PersistentDataContainerView;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedPlayerList;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.storage.PlayerDataStorage;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.bukkit.*;
import org.bukkit.block.BlockType;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.block.data.CraftBlockData;
import org.bukkit.craftbukkit.entity.CraftEntityTypes;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.persistence.CraftPersistentDataContainer;
import org.bukkit.craftbukkit.persistence.CraftPersistentDataTypeRegistry;
import org.bukkit.entity.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.components.FoodComponent;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.PluginManager;
import org.bukkit.util.Vector;
import org.spigotmc.SpigotConfig;
import ru.komiss77.Ostrov;
import ru.komiss77.modules.bots.BotEntity;
import ru.komiss77.modules.games.GM;
import ru.komiss77.modules.items.ItemBuilder;
import ru.komiss77.modules.items.PDC;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;
import ru.komiss77.modules.world.BVec;
import ru.komiss77.modules.world.WXYZ;
import ru.komiss77.modules.world.XYZ;
import ru.komiss77.notes.Slow;
import ru.komiss77.objects.Duo;
import ru.komiss77.scoreboard.SubTeam;
import ru.komiss77.utils.LocUtil;
import ru.komiss77.utils.NumUtil;
import ru.komiss77.utils.TCUtil;


public class Nms {
  public static final List<String> vanilaCommandToDisable;
  protected static final BlockPos.MutableBlockPos mutableBlockPosition; //не юзать при отправке пакетов напрямую! Пакет отправляется с задержкой, значение может уже измениться!
  private static final Key chatKey;
  private static final net.minecraft.world.level.block.state.BlockState sign;
  private static final String signId;

  static {
    vanilaCommandToDisable = Arrays.asList("execute",
        "bossbar", "defaultgamemode", "me", "help", "kick", "kill", "tell",
        "say", "spreadplayers", "teammsg", "tellraw", "trigger",
        "ban-ip", "banlist", "ban", "op", "pardon", "pardon-ip", "perf",
        "save-all", "save-off", "save-on", "setidletimeout", "publish");
    chatKey = Key.key("ostrov_chat", "listener");
    mutableBlockPosition = new BlockPos.MutableBlockPos(0, 0, 0);
    sign = Craft.toNMS(Material.OAK_SIGN.createBlockData());
    signId = BlockEntityType.getKey(BlockEntityType.SIGN).toString();
  }

  public static boolean isServerStopped() {
    return Craft.toNMS().hasStopped();
  }

  //ЛКМ и ПКМ на фейковый блок будут игнорироваться! Еще можно будет добавить подмену блока при получении чанка
  public static void fakeBlock(final Player p, final Location loc, final BlockData bd) {
    fakeBlock(p, LocUtil.asLong(loc), bd);
  }

  @Deprecated
  public static void fakeBlock(final Player p, final XYZ xyz, final BlockData bd) {
    fakeBlock(p, xyz.asLong(), bd);
  }
  public static void fakeBlock(final Player p, final BVec loc, final BlockData bd) {
    fakeBlock(p, loc.thick(), bd);
  }

  public static void fakeBlock(final Player p, final long l, final BlockData bd) {
    sendPacket(p, new ClientboundBlockUpdatePacket(BlockPos.of(l), Craft.toNMS(bd)));
    final Oplayer op = PM.getOplayer(p);
    op.fakeBlock.put(l, bd); //2! это заблочит исходящий пакет обновы
    op.hasFakeBlock = true;
  }

  public static void fakeBlock(final Player p, final Location loc) {
    final Oplayer op = PM.getOplayer(p);
    //mutableBlockPosition.set(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    final long l = LocUtil.asLong(loc);
    if (op.hasFakeBlock && op.fakeBlock.remove(l) != null) {
      sendPacket(p, new ClientboundBlockUpdatePacket(BlockPos.of(l),
          ((CraftBlockData) loc.getBlock().getBlockData()).getState()));
      op.hasFakeBlock = !op.fakeBlock.isEmpty();
    }
    //PM.getOplayer(p).fakeBlock.remove(mutableBlockPosition.asLong());
  }

  public static void fakeItem(final Player p, final ItemStack it, final int slot) {
    sendPacket(p, new ClientboundSetPlayerInventoryPacket(slot, net.minecraft.world.item.ItemStack.fromBukkitCopy(it)));
  }

  private static final int OFH_SLOT = 40;
  public static void totemPop(final Player p, final ItemStack totem) {
    fakeItem(p, new ItemBuilder(totem).set(DataComponentTypes.DEATH_PROTECTION,
        DeathProtection.deathProtection().build()).build(), OFH_SLOT);
    sendPacket(p, new ClientboundEntityEventPacket(Craft.toNMS(p), EntityEffect.PROTECTED_FROM_DEATH.getData()));
    fakeItem(p, p.getInventory().getItemInOffHand(), OFH_SLOT);
  }

  public static void totemWorldPop(final Player p, final ItemStack totem) {
    final PlayerInventory inv = p.getInventory();
    final ItemStack ofh = inv.getItemInOffHand();
    inv.setItemInOffHand(new ItemBuilder(totem).set(DataComponentTypes.DEATH_PROTECTION,
        DeathProtection.deathProtection().build()).build());
    p.playEffect(EntityEffect.PROTECTED_FROM_DEATH);
    inv.setItemInOffHand(ofh);
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
    try {
      pipeline.addBefore("packet_handler", "ostrov_" + p.getName(), packetSpy);
    } catch (NoSuchElementException e) {
      //p.kick(TCUtil.form("<gold>Остров <apple>все еще загружается!")); //такого не должно быть, ищем ошибку
      Ostrov.log_err("addPlayerPacketSpy " + p.getName() + " : " + e.getMessage());
    } catch (IllegalArgumentException e) {
      Ostrov.log_warn("addPlayerPacketSpy " + p.getName() + " : " + e.getMessage());
    }
  }


  public static void removePlayerPacketSpy(final Player p) {  //при дисконнекте
    final Channel channel = Craft.toNMS(p).connection.connection.channel; //EntityPlayer->PlayerConnection->NetworkManager->Chanell->ChannelPipeline
    channel.eventLoop().submit(() -> {
      channel.pipeline().remove("ostrov_" + p.getName());
      return null;
    });
  }

  @Deprecated
  public static void signInput(final Player p, final String suggest, final XYZ signXyz) { //suggest придёт с '&'
    //final BlockData bd = Material.OAK_SIGN.createBlockData();
    mutableBlockPosition.set(signXyz.x, signXyz.y, signXyz.z);
    final ClientboundBlockUpdatePacket packet = new ClientboundBlockUpdatePacket(mutableBlockPosition, sign); //p.sendBlockChange(signXyz.getCenterLoc(), bd);
    sendPacket(p, packet);
    //final SignBlockEntity sign = new SignBlockEntity(mutableBlockPosition, null);
    final Component[] comps = new Component[4];
    Arrays.fill(comps, Component.empty());
    boolean last = true;
    switch (suggest.length() >> 4) {
      default:
        last = false;
      case 3:
        comps[3] = PaperAdventure.asVanilla(TCUtil.form(suggest.substring(48, last ? suggest.length() : 65)));
        last = false;
      case 2:
        comps[2] = PaperAdventure.asVanilla(TCUtil.form(suggest.substring(32, last ? suggest.length() : 47)));
        last = false;
      case 1:
        comps[1] = PaperAdventure.asVanilla(TCUtil.form(suggest.substring(16, last ? suggest.length() : 31)));
        last = false;
      case 0:
        comps[0] = PaperAdventure.asVanilla(TCUtil.form(suggest.substring(0, last ? suggest.length() : 15)));
        break;
    }
    final SignText signtext = new SignText(comps, comps, DyeColor.WHITE, true);
    //sign.setText(signtext, true);//sign.c(signtext);//
    final CompoundTag nbt = new CompoundTag();
    nbt.putString("id", signId);
    nbt.putInt("x", signXyz.x);
    nbt.putInt("y", signXyz.y);
    nbt.putInt("z", signXyz.z);
    final HolderLookup.Provider registryLookup = Craft.toNMS(p.getWorld()).registryAccess();
    final DynamicOps<Tag> dynamicops = registryLookup.createSerializationContext(NbtOps.INSTANCE);
    final DataResult<Tag> dataresult = SignText.DIRECT_CODEC.encodeStart(dynamicops, signtext);
    dataresult.result().ifPresent(nbtbase -> {
      nbt.put("front_text", nbtbase);
    });
    final ClientboundBlockEntityDataPacket entityDataPacket = new ClientboundBlockEntityDataPacket(mutableBlockPosition, BlockEntityType.SIGN, nbt);
    //sign.getUpdatePacket();
    sendPacket(p, entityDataPacket);// 1201 sendPacket(p, sign.j());
    final ClientboundOpenSignEditorPacket outOpenSignEditor = new ClientboundOpenSignEditorPacket(mutableBlockPosition, true);
    sendPacket(p, outOpenSignEditor);
  }

  public static final CraftPersistentDataTypeRegistry PDT_REG = new CraftPersistentDataTypeRegistry();
  public static PersistentDataContainer newPDC() {return new CraftPersistentDataContainer(PDT_REG);}
  public static PersistentDataContainer newPDC(final PersistentDataContainerView data) {
    if (!(data instanceof final PaperPersistentDataContainerView pd))
      return new CraftPersistentDataContainer(PDT_REG);
    final Map<String, Tag> tags = new HashMap<>();
    for (final NamespacedKey nk : pd.getKeys()) {
      final String k = nk.asString(); tags.put(k, pd.getTag(k));
    }
    return new CraftPersistentDataContainer(tags, PDT_REG);
  }
  public static final String P_B_V = "PublicBukkitValues";
  public static void setCustomData(final ItemStack it, final PDC.Data data) {
    if (it == null || data.isEmpty()) return;
    final DataComponentPatch.Builder builder = DataComponentPatch.builder();
    final CraftPersistentDataContainer pdc = new CraftPersistentDataContainer(PDT_REG);
    for (final Duo<NamespacedKey, Serializable> en : data) {
      switch (en.val()) {
        case final Byte d -> pdc.set(en.key(), PersistentDataType.BYTE, d);
        case final Long d -> pdc.set(en.key(), PersistentDataType.LONG, d);
        case final Integer d -> pdc.set(en.key(), PersistentDataType.INTEGER, d);
        case final Float d -> pdc.set(en.key(), PersistentDataType.FLOAT, d);
        case final Double d -> pdc.set(en.key(), PersistentDataType.DOUBLE, d);
        case final byte[] d -> pdc.set(en.key(), PersistentDataType.BYTE_ARRAY, d);
        case final int[] d -> pdc.set(en.key(), PersistentDataType.INTEGER_ARRAY, d);
        case final String d -> pdc.set(en.key(), PersistentDataType.STRING, d);
        default -> pdc.set(en.key(), PersistentDataType.STRING, en.val().toString());
      }
    }
    final CompoundTag pdcTag = new CompoundTag();
    for (final Map.Entry<String, Tag> en : pdc.getRaw().entrySet()) {
      pdcTag.put(en.getKey(), en.getValue());
    }
    final CompoundTag ct = new CompoundTag();
    ct.put(P_B_V, pdcTag);
    builder.set(DataComponents.CUSTOM_DATA, CustomData.of(ct));
    if (it instanceof final CraftItemStack cit) {
      cit.handle.applyComponents(builder.build());
    }
  }

  @Deprecated
  public static void setCustomData(final ItemStack it, final PersistentDataContainerView data) {
    it.editPersistentDataContainer(pdc -> data.copyTo(pdc, true));
    /*if (it == null || data.isEmpty() || !(data instanceof final PaperPersistentDataContainerView pd)) return;
    final DataComponentPatch.Builder builder = DataComponentPatch.builder();
    final Map<String, Tag> tags = new HashMap<>();
    for (final NamespacedKey nk : pd.getKeys()) {
      final String k = nk.asString(); tags.put(k, pd.getTag(k));
    }
    final CraftPersistentDataContainer pdc = new CraftPersistentDataContainer(tags, PDT_REG);
    final CompoundTag pdcTag = new CompoundTag();
    for (final Map.Entry<String, Tag> en : pdc.getRaw().entrySet()) {
      pdcTag.put(en.getKey(), en.getValue());
    }
    final CompoundTag ct = new CompoundTag();
    ct.put(P_B_V, pdcTag);
    builder.set(DataComponents.CUSTOM_DATA, CustomData.of(ct));
    if (it instanceof final CraftItemStack cit) {
      cit.handle.applyComponents(builder.build());
    }*/
  }

  @Deprecated
  public static Material getFastMat(final World w, int x, int y, int z) {
    final ServerLevel sl = Craft.toNMS(w);
    final BlockState iBlockData = sl.getBlockState(mutableBlockPosition.set(x, y, z));
    return iBlockData.getBukkitMaterial();
  }

  public static BlockType fastType(final World w, int x, int y, int z) {
    final ServerLevel sl = Craft.toNMS(w);
    final BlockState iBlockData = sl.getBlockState(mutableBlockPosition.set(x, y, z));
    return Craft.fromNMS(iBlockData.getBlock());
  }

  @Deprecated
  public static Material getFastMat(final WXYZ loc) {
    final ServerLevel sl = Craft.toNMS(loc.w);
    final BlockState iBlockData = sl.getBlockState(mutableBlockPosition.set(loc.x, loc.y, loc.z));
    return iBlockData.getBukkitMaterial();
  }

  @Deprecated
  public static BlockType fastType(final WXYZ loc) {
    final ServerLevel sl = Craft.toNMS(loc.w);
    final BlockState iBlockData = sl.getBlockState(mutableBlockPosition.set(loc.x, loc.y, loc.z));
    return Craft.fromNMS(iBlockData.getBlock());
  }

  public static BlockType fastType(final World w, final BVec bv) {
    final ServerLevel sl = Craft.toNMS(w);
    final BlockState iBlockData = sl.getBlockState(mutableBlockPosition.set(bv.x, bv.y, bv.z));
    return Craft.fromNMS(iBlockData.getBlock());
  }

  @Deprecated
  public static Material getFastMat(final Location loc) {
    final ServerLevel sl = Craft.toNMS(loc.getWorld());
    final BlockState iBlockData = sl.getBlockState(mutableBlockPosition.set(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
    return iBlockData.getBukkitMaterial();
  }

  public static BlockType fastType(final Location loc) {
    final ServerLevel sl = Craft.toNMS(loc.getWorld());
    final BlockState iBlockData = sl.getBlockState(mutableBlockPosition.set(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
    return Craft.fromNMS(iBlockData.getBlock());
  }

  public static BlockData fastData(final World w, int x, int y, int z) {
    final ServerLevel sl = Craft.toNMS(w);
    final BlockState iBlockData = sl.getBlockState(mutableBlockPosition.set(x, y, z));
    return iBlockData.createCraftBlockData();
  }

  @Deprecated
  public static BlockData fastData(final WXYZ loc) {
    final ServerLevel sl = Craft.toNMS(loc.w);
    final BlockState iBlockData = sl.getBlockState(mutableBlockPosition.set(loc.x, loc.y, loc.z));
    return iBlockData.createCraftBlockData();
  }

  public static BlockData fastData(final World w, final BVec bv) {
    final ServerLevel sl = Craft.toNMS(w);
    final BlockState iBlockData = sl.getBlockState(mutableBlockPosition.set(bv.x, bv.y, bv.z));
    return iBlockData.createCraftBlockData();
  }

  public static BlockData fastData(final Location loc) {
    final ServerLevel sl = Craft.toNMS(loc.getWorld());
    final BlockState iBlockData = sl.getBlockState(mutableBlockPosition.set(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
    return iBlockData.createCraftBlockData();
  }

  public static void fastType(final World w, final List<BlockPosition> poss, final BlockType bt) {
    final ServerLevel sl = Craft.toNMS(w);
    final BlockState bs = Craft.toNMS(bt).defaultBlockState();
    for (final BlockPosition bp : poss) {
      mutableBlockPosition.set(bp.blockX(), bp.blockY(), bp.blockZ());
      setNmsData(sl, sl.getBlockState(mutableBlockPosition), bs);
    }
  }

  @Deprecated
  public static void fastData(final WXYZ wxyz, final BlockPosition dims, final BlockData bd) {
    final ServerLevel sl = Craft.toNMS(wxyz.w);
    final BlockState bs = Craft.toNMS(bd);
    for (int x_ = 0; x_ != dims.blockX(); x_++) {
      for (int z_ = 0; z_ != dims.blockY(); z_++) {
        for (int y_ = 0; y_ != dims.blockZ(); y_++) {
          mutableBlockPosition.set(wxyz.x + x_, wxyz.y + y_, wxyz.z + z_);
          setNmsData(sl, sl.getBlockState(mutableBlockPosition), bs);
        }
      }
    }
  }

  public static void fastData(final World w, final BVec bv, final BVec dims, final BlockData bd) {
    final ServerLevel sl = Craft.toNMS(w);
    final BlockState bs = Craft.toNMS(bd);
    for (int x_ = 0; x_ != dims.x; x_++) {
      for (int z_ = 0; z_ != dims.y; z_++) {
        for (int y_ = 0; y_ != dims.z; y_++) {
          mutableBlockPosition.set(bv.x + x_, bv.y + y_, bv.z + z_);
          setNmsData(sl, sl.getBlockState(mutableBlockPosition), bs);
        }
      }
    }
  }

  @Deprecated
  public static String getBiomeKey(final World w, int x, int y, int z) {
    final BiomeManager bm = Craft.toNMS(w).getBiomeManager();
    final Optional<ResourceKey<Biome>> opk = bm.getNoiseBiomeAtPosition(new BlockPos(x, y, z)).unwrapKey();
    return opk.map(bk -> bk.location().getPath()).orElse("void");
  }

  @Deprecated
  public static String getBiomeKey(final WXYZ loc) {
    final BiomeManager bm = Craft.toNMS(loc.w).getBiomeManager();
    final Optional<ResourceKey<Biome>> opk = bm.getNoiseBiomeAtPosition(new BlockPos(loc.x, loc.y, loc.z)).unwrapKey();
    return opk.map(bk -> bk.location().getPath()).orElse("void");
  }

  //упрощенный вид
  private static final int FST_FLAGS = 2 | 16 | 1024;
  protected static void setNmsData(final ServerLevel sl, final BlockState old, final BlockState curr) {
    if (old.hasBlockEntity() && curr.getBlock() != old.getBlock()) {
      sl.removeBlockEntity(mutableBlockPosition);
    }
    final boolean success = sl.setBlock(mutableBlockPosition, curr, FST_FLAGS); // NOTIFY | NO_OBSERVER | NO_PLACE (custom)
    if (success) sl.sendBlockUpdated(mutableBlockPosition, old, curr, 3);
  }

  @Deprecated
  public enum PlaceType {
    SAFELY, FLUID, AIR, DANGEROUS
  }

  @Deprecated
  public static PlaceType isSafeLocation(WXYZ feetXYZ) {
    final ServerLevel sl = Craft.toNMS(feetXYZ.w);
    //final ServerPlayer sp = p==null ? null : Craft.toNMS(p);
    BlockState state = sl.getBlockState(mutableBlockPosition.set(feetXYZ.x, feetXYZ.y + 1, feetXYZ.z));//голова
    boolean headOk = !canStandOnCenter(sl, state) && state.getFluidState().isEmpty();
//info (p, "§7голова §6", state, sl, sp, headOk);
    if (!headOk) return PlaceType.DANGEROUS;
    //state.entityCanStandOn(sl, mutableBlockPosition, sp);
    state = sl.getBlockState(mutableBlockPosition.setY(feetXYZ.y));//ноги
    boolean feetOk = !canStandOnCenter(sl, state);//можно стоять в воде высотой в 1 блок   && state.getFluidState().isEmpty() ;
//info (p, "§7ноги §6", state, sl, sp, feetOk);
    if (!feetOk) return PlaceType.DANGEROUS;
    state = sl.getBlockState(mutableBlockPosition.setY(feetXYZ.y - 1));//под ногами
    boolean underFeetOk = state.getBlock().hasCollision && canStandOnCenter(sl, state);
//info (p, "§7под §6", state, sl, sp, underFeetOk);
    if (!underFeetOk) {
      if (state.isAir()) {
        return PlaceType.AIR;
      } else if (!state.getFluidState().isEmpty()) {
        return PlaceType.FLUID;
      } else {
        return PlaceType.DANGEROUS;
      }
    }
    return PlaceType.SAFELY;
  }

  private static boolean canStandOnCenter(final ServerLevel sl, final BlockState state) {
    if (state.isAir() || !state.getBlock().hasCollision) return false;
    final VoxelShape faceShape = state.getCollisionShape(sl, mutableBlockPosition).getFaceShape(Direction.UP);
    return (faceShape.min(Direction.Axis.X) <= 0.5d && faceShape.max(Direction.Axis.X) >= 0.5d) &&
        (faceShape.min(Direction.Axis.Z) <= 0.5d && faceShape.max(Direction.Axis.Z) >= 0.5d);
  }


  public static void pathServer() {
    final MinecraftServer srv = MinecraftServer.getServer();
    final com.mojang.brigadier.CommandDispatcher<CommandSourceStack> dispatcher = srv.getCommands().getDispatcher();
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

    } catch (IllegalAccessException | IllegalArgumentException | NoSuchFieldException | SecurityException ex) {
      Ostrov.log_warn("nms Server pathServer RootCommandNode : " + ex.getMessage());
    }

    SpigotConfig.belowZeroGenerationInExistingChunks = false;
    SpigotConfig.restartOnCrash = false;
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
      case DA, AR, MI, SK, OB -> {
        SpigotConfig.disableAdvancementSaving = false;
        SpigotConfig.disabledAdvancements = Collections.emptyList();
        SpigotConfig.disableStatSaving = false;
        SpigotConfig.disablePlayerDataSaving = false;
      }
      case LOBBY -> {
        SpigotConfig.disableAdvancementSaving = true;
        SpigotConfig.disabledAdvancements = Arrays.asList("*", "minecraft:story/disabled");
        SpigotConfig.disableStatSaving = true;
        SpigotConfig.disablePlayerDataSaving = false;
      }
      default -> {
        SpigotConfig.disableAdvancementSaving = true;
        SpigotConfig.disabledAdvancements = Arrays.asList("*", "minecraft:story/disabled");
        SpigotConfig.disableStatSaving = true;
        SpigotConfig.disablePlayerDataSaving = true;
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

    if (SpigotConfig.disablePlayerDataSaving == false) {
      final DedicatedServer dedicatedServer = Craft.toNMS();
      final DedicatedPlayerList dedicatedPlayerList = dedicatedServer.getPlayerList();
      final PlayerDataStorage oldPds = dedicatedPlayerList.playerIo;
      try {
        final Field dataFixerField = oldPds.getClass().getDeclaredField("fixerUpper");
        dataFixerField.setAccessible(true);
        final DataFixer fixerUpper = (DataFixer) dataFixerField.get(oldPds);
        dataFixerField.setAccessible(false);
        //osPds.playerDir = oldPds.getPlayerDir();
        final OsPlayerDataStorage osPds = new OsPlayerDataStorage(dedicatedServer.storageSource, fixerUpper);//dedicatedPlayerList.playerIo;
        //dedicatedServer.playerDataStorage = osPds;
        //dedicatedPlayerList.playerIo = osPds;
        final Field playerIoField = dedicatedPlayerList.getClass().getField("playerIo"); //getDeclaredField-без наслодования
        playerIoField.setAccessible(true);
        playerIoField.set(dedicatedPlayerList, osPds);
        playerIoField.setAccessible(false);
        final Field playerDataStorageField = dedicatedServer.getClass().getField("playerDataStorage");
        playerDataStorageField.setAccessible(true);
        playerDataStorageField.set(dedicatedServer, osPds);
        playerDataStorageField.setAccessible(false);
      } catch (IllegalAccessException | IllegalArgumentException | NoSuchFieldException | SecurityException ex) {
        Ostrov.log_warn("nms Server pathServer PlayerDataStorage : " + ex.getMessage());
        ex.printStackTrace();
      }
    }

    Ostrov.log_ok("§bСервер сконфигурирован, отключено ванильных команд: " + vanilaCommandToDisable.size());
  }

  private static void makeStaticFinalFieldWritable(Field field) {
    try {
      var lookup = MethodHandles.privateLookupIn(field.getClass(), MethodHandles.lookup());
      var modifiersHandle = lookup.findVarHandle(field.getClass(), "modifiers", int.class);
      var modifiers = field.getModifiers();
      modifiersHandle.set(field, modifiers & ~Modifier.FINAL);

      var rootHandle = lookup.findVarHandle(field.getClass(), "root", Field.class);
      var root = (Field) rootHandle.get(field);
      if (root != null && root != field) {
        makeStaticFinalFieldWritable(root);
      }
    } catch (IllegalAccessException | NoSuchFieldException e) {
      throw new Error(e);
    }
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

  @Deprecated
  @Slow(priority = 1)
  public static FoodComponent getFood(final ItemStack it) {
    final FoodComponent fc = it.getItemMeta().getFood();
    final net.minecraft.world.item.ItemStack fi = net.minecraft.world.item.ItemStack.fromBukkitCopy(it);
    final FoodProperties fp = fi.getItem().components().get(DataComponents.FOOD);
    fc.setCanAlwaysEat(fp.canAlwaysEat());
    fc.setNutrition(fp.nutrition());
    fc.setSaturation(fp.saturation());
    return fc;
//    fc.setUsingConvertsTo(fp.usingConvertsTo().orElseGet(() ->
//        new net.minecraft.world.item.ItemStack(Items.AIR)).asBukkitCopy());
//    fc.setEffects(fp.effects().getFirst().effect());
  }

  public static int getTps() {
    return MinecraftServer.TPS;
  }

  public static int getitemDespawnRate(final World w) { //skyworld
    return Craft.toNMS(w).spigotConfig.itemDespawnRate;
  }

  public static EntityType typeByClass(final Class<? extends LivingEntity> cls) {
    return CraftEntityTypes.getEntityTypeData(cls).entityType();
  }

  @Deprecated
  public static void sendBlockCrack(final Player p, final WXYZ bl, final float state) {
    p.sendBlockDamage(bl.getCenterLoc(), state);
  }

  /*
  Slots 0-8 are as follows: 0 crafting output, 1-4 crafting input,
  5 helmet, 6 chestplate, 7 leggings, and 8 boots. Then, 9-35 work exactly the same as setItem(). The hotbar
  for PacketPlayOutSetSlot starts at index 36, and continues to index 44. Items placed where index is < 0 or > 44 have no action.
   */
  public static void sendFakeEquip(final Player p, final int playerInventorySlot, final ItemStack item) {
//Ostrov.log_warn("sendFakeEquip " + playerInventorySlot + " " + item.getType());
    final ServerPlayer sp = Craft.toNMS(p); //5-шлем
    sp.connection.send(new ClientboundContainerSetSlotPacket(sp.inventoryMenu.containerId,
        sp.inventoryMenu.getStateId(), playerInventorySlot, net.minecraft.world.item.ItemStack.fromBukkitCopy(item)));
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

  public static void swing(final LivingEntity le, final EquipmentSlot hand) {
    Craft.toNMS(le).swinging = false;
    le.swingHand(hand);
  }

  public static void noFallDmg(final Player pl) {
    Craft.toNMS(pl).setIgnoreFallDamageFromCurrentImpulse(true);
  }

  public static boolean hasFallDmg(final Player pl) {
    return !Craft.toNMS(pl).isIgnoringFallDamageFromCurrentImpulse();
  }

  public static void zoom(final Player pl, final float zoom) {
    final Abilities ab = new Abilities();
    ab.invulnerable = ab.flying = ab.mayfly = ab.instabuild
        = switch (pl.getGameMode())
    {case CREATIVE, SPECTATOR -> true; default -> false;};
    if (zoom > 10f) {
      final float rev = Math.max(21f-zoom, 1f);
      ab.setWalkingSpeed(rev * rev * -0.1f);
    } else ab.setWalkingSpeed(zoom * zoom / 10f);
    Nms.sendPacket(pl, new ClientboundPlayerAbilitiesPacket(ab));
  }

  @Deprecated
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
    final byte yawByte = NumUtil.pack(180f - NumUtil.toDegree((float) Math.atan2(vx, vz)) + NumUtil.randInt(-10, 10));
    final byte pitchByte = NumUtil.pack(90 - NumUtil.toDegree((float) Math.acos(vy)) + (NumUtil.rndBool() ? 10 : -5));
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
    final byte yawByte = NumUtil.pack(e.getLocation().getYaw());//toPackedByte(f.yaw);
    final byte pitchByte = NumUtil.pack(e.getLocation().getPitch());//toPackedByte(f.pitch);
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
        packet.packedItems().add(new SynchedEntityData.DataValue<>(0, BotEntity.flags.serializer(), (byte) 64));
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
      packet.packedItems().add(new SynchedEntityData.DataValue<>(0, BotEntity.flags.serializer(), (byte) 64));

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



/*

  private static void info(Player p, String prefix, BlockState state, ServerLevel sl, ServerPlayer sp, boolean result) {
    VoxelShape faceShape = null;// = state.getShape(sl, mutableBlockPosition);
    boolean hasCollision = state.getBlock().hasCollision;
    boolean canStandOnCenter = false;
    if (hasCollision) {
      faceShape = state.getCollisionShape(sl, mutableBlockPosition).getFaceShape(Direction.UP);
      canStandOnCenter = (faceShape.min(Direction.Axis.X) <= 0.5d && faceShape.max(Direction.Axis.X) >= 0.5d) &&
              (faceShape.min(Direction.Axis.Z) <= 0.5d && faceShape.max(Direction.Axis.Z) >= 0.5d);
      //final BlockHitResult hitResult = this.clipWithInteractionOverride(start, end, pos, voxelshape, state);
    }
    //double d = shape.collide(Direction.Axis.Y, sp.getBoundingBox(), 1);
    Ostrov.log_warn(prefix
            + state.getBukkitMaterial()
            + (hasCollision ? " §a" : " §c") + "hasCollision"
            + (canStandOnCenter ? " §a" : " §c") + "canStandOnCenter"
            //+(hasCollision? (" §acollision UP X="+faceShape.min(Direction.Axis.X)+"/"+faceShape.max(Direction.Axis.X)
            //+" Y="+faceShape.min(Direction.Axis.Y)+"/"+faceShape.max(Direction.Axis.Y)
            //+" Z="+faceShape.min(Direction.Axis.Z)+"/"+faceShape.max(Direction.Axis.Z)):" §chasCollision")
            //+(full?" §a":" §c")+"fullBlock"
            //+(state.entityCanStandOn(sl, mutableBlockPosition, sp)?" §a":" §c")+"canStand"юзает isFaceFull - это громоздко
            + (!state.getFluidState().isEmpty() ? " §a" : " §c") + "fluidState=" + state.getFluidState().getOwnHeight()
            //+" §7collide=§3"+d
            + (result ? " §a" : " §c") + "result"
    );

  }

 */