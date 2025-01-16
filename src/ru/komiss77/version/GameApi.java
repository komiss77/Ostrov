package ru.komiss77.version;

import java.util.Collections;
import java.util.Optional;
import io.papermc.paper.adventure.PaperAdventure;
import net.minecraft.ChatFormatting;
import net.minecraft.core.*;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.DimensionTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import net.minecraft.network.protocol.game.CommonPlayerSpawnInfo;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import org.bukkit.*;
import org.bukkit.craftbukkit.block.data.CraftBlockData;
import org.bukkit.entity.Player;
import ru.komiss77.modules.world.WXYZ;


public class GameApi {

  //также этим можно обновить видимый скин. Пока в стадии разработки, мучения не чистить!!
  public static void sendFakeDimension(final Player p, final World.Environment environment) {
    //Level.RESOURCE_KEY_CODEC.parse()
    //ResourceLocation.withDefaultNamespace("overworld")
    final ServerPlayer sp = Craft.toNMS(p);


    RegistryAccess ra = null;
    ra = Craft.toNMS(p.getWorld()).registryAccess();
//ra = RegistryAccess.RegistryEntry;

    Registry<LevelStem> levelStemRegistry = ra.lookupOrThrow(Registries.LEVEL_STEM);
    LevelStem customStem = levelStemRegistry.getValue(LevelStem.END);
    Holder<DimensionType> dtHolder = customStem.type();

    //final HolderLookup.Provider registryLookup = Craft.toNMS(p.getWorld()).registryAccess();
    //dt = ra.lookupOrThrow(Registries.DIMENSION_TYPE).getResourceKey(customStem.type().value()).get();
    //dt = net.minecraft.world.level.dimension.BuiltinDimensionTypes.OVERWORLD;
    //DimensionType dt = registryLookup.lookupOrThrow(Registries.DIMENSION_TYPE).get(net.minecraft.world.level.dimension.BuiltinDimensionTypes.OVERWORLD).get().value();

    //Registries.DIMENSION_TYPE.registryKey();
    //Level.RESOURCE_KEY_CODEC.parse(Level.END);

    //net.minecraft.core.RegistryAccess.registryAccess().getRegistry(Level.END);
    //&& this.registryAccess().lookupOrThrow(Registries.DIMENSION_TYPE).getResourceKey(customStem.type().value()).orElseThrow() == net.minecraft.world.level.dimension.BuiltinDimensionTypes.OVERWORLD
    //HolderLookup<DimensionType> holderLookup = registries.lookupOrThrow(Registries.DIMENSION_TYPE);

    CommonPlayerSpawnInfo playerSpawnInfo = new CommonPlayerSpawnInfo(
        //Registries.DIMENSION_TYPE.registryKey().,
        //BuiltinDimensionTypes.END.location().withPath(""),
        //DimensionTypes.bootstrap(),
        //Level.RESOURCE_KEY_CODEC.parse(Level.END),
        //sp.level().dimensionTypeRegistration(),
        dtHolder,
        Level.END,//sp.level().dimension(),
        BiomeManager.obfuscateSeed(p.getWorld().getSeed()),
        sp.gameMode.getGameModeForPlayer(),
        sp.gameMode.getPreviousGameModeForPlayer(),
        sp.level().isDebug(),
        true,//sp.level().isFlat(),
        Optional.of(GlobalPos.of(sp.level().dimension(), sp.getOnPos())),//sp.getLastDeathLocation(),
        sp.getPortalCooldown(),
        sp.level().getSeaLevel()
    );
    // respawn packet
    Nms.sendPacket(p, new ClientboundRespawnPacket(playerSpawnInfo, (byte) 0x01));
    // pos packet
    // Location lo = p.getLocation();
    // ClientboundPlayerPositionPacket positionPacket = new ClientboundPlayerPositionPacket(lo.x(), lo.y(), lo.z(), lo.getYaw(), lo.getPitch(), Collections.emptySet(), 0);
    // send packets
    //serverPlayer.connection.send(positionPacket);
    // send level info
    // server
    //DedicatedPlayerList dedicatedPlayerList = ((CraftServer) Bukkit.getServer()).getHandle();
    //dedicatedPlayerList.sendLevelInfo(serverPlayer, serverPlayer.serverLevel());
    // send all player info
    //dedicatedPlayerList.sendAllPlayerInfo(serverPlayer);

  }

  public static String fromComponent(net.kyori.adventure.text.Component paperComponent) {
    if (paperComponent == null) return "";
    net.minecraft.network.chat.Component component = PaperAdventure.asVanilla(paperComponent);
    if (component instanceof io.papermc.paper.adventure.AdventureComponent)
      component = ((io.papermc.paper.adventure.AdventureComponent) component).deepConverted();
    component = (net.minecraft.network.chat.Component) component;
    StringBuilder out = new StringBuilder();

    boolean hadFormat = false;
    for (Component c : component) {
      Style modi = c.getStyle();
      TextColor color = modi.getColor();
      if (c.getContents() != PlainTextContents.EMPTY || color != null) {
        if (color != null) {
          if (color.format != null) {
            out.append(color.format);
          } else {
            out.append(ChatColor.COLOR_CHAR).append("x");
            for (char magic : color.serialize().substring(1).toCharArray()) {
              out.append(ChatColor.COLOR_CHAR).append(magic);
            }
          }
          hadFormat = true;
        } else if (hadFormat) {
          out.append(ChatColor.RESET);
          hadFormat = false;
        }
      }
      if (modi.isBold()) {
        out.append(ChatFormatting.BOLD);
        hadFormat = true;
      }
      if (modi.isItalic()) {
        out.append(ChatFormatting.ITALIC);
        hadFormat = true;
      }
      if (modi.isUnderlined()) {
        out.append(ChatFormatting.UNDERLINE);
        hadFormat = true;
      }
      if (modi.isStrikethrough()) {
        out.append(ChatFormatting.STRIKETHROUGH);
        hadFormat = true;
      }
      if (modi.isObfuscated()) {
        out.append(ChatFormatting.OBFUSCATED);
        hadFormat = true;
      }
      c.getContents().visit((x) -> {
        out.append(x);
        return Optional.empty();
      });
    }
    return out.toString();
  }


  //для избавления твиста от НМС.
  public static void setFastMat(final WXYZ wxyz, final int sizeX, final int sizeY, final int sizeZ, final Material mat) {
    final ServerLevel sl = Craft.toNMS(wxyz.w);
    final net.minecraft.world.level.block.state.BlockState bs = ((CraftBlockData) mat.createBlockData()).getState();
    for (int x_ = 0; x_ != sizeX; x_++) {
      for (int z_ = 0; z_ != sizeZ; z_++) {
        for (int y_ = 0; y_ != sizeY; y_++) {
          Nms.mutableBlockPosition.set(wxyz.x + x_, wxyz.y + y_, wxyz.z + z_);
          //CraftBlock.setTypeAndData(sl, Nms.mutableBlockPosition, sl.getBlockState(Nms.mutableBlockPosition), bs, false);
          Nms.setNmsData(sl, Nms.mutableBlockPosition, sl.getBlockState(Nms.mutableBlockPosition), bs);
          //sl.setBlock(mutableBlockPosition, bs,)
        }
      }
    }
  }

}
