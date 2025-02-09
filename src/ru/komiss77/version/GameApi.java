package ru.komiss77.version;

import java.util.List;
import java.util.Optional;
import io.papermc.paper.adventure.PaperAdventure;
import io.papermc.paper.datacomponent.DataComponentTypes;
import net.minecraft.ChatFormatting;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import net.minecraft.network.protocol.game.CommonPlayerSpawnInfo;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.craftbukkit.block.data.CraftBlockData;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.komiss77.modules.world.WXYZ;


public class GameApi {


  //отлом из net.minecraft.world.entity.Mob public boolean doHurtTarget(ServerLevel level, Entity source) 1612
  public static void attack(final org.bukkit.entity.LivingEntity source, final List<org.bukkit.entity.Entity> entitys) {
    if (source == null || entitys == null || entitys.isEmpty()) return;
    //ServerPlayer sp = Craft.toNMS(p);
    final ItemStack inHand = source.getEquipment().getItemInMainHand();
//Ostrov.log_warn("========== attack "+source.getType()+" inHand="+inHand.getType()+" ============");    //org.bukkit.entity.Entity en = null;
    AttributeModifier attackMod = null;
    io.papermc.paper.datacomponent.item.ItemAttributeModifiers itemMods = inHand.getData(DataComponentTypes.ATTRIBUTE_MODIFIERS);//ItemData data =
    if (itemMods != null) {
      for (final io.papermc.paper.datacomponent.item.ItemAttributeModifiers.Entry ie : itemMods.modifiers()) {
        final AttributeModifier mod = ie.modifier();
//Core.log_warn("§fPaper mod="+mod.toString());
        //ai.removeModifier(mod);
        if (mod.getKey().getKey().equals("base_attack_damage")) {
//Ostrov.log_warn("§5inHand base_attack_damage val="+mod.getAmount());
          attackMod = mod;
          break;
        } //else {
//Ostrov.log_warn("§5inHand mod="+mod.toString());
        //}
      }
    }
    float knockback = 0;//, fire = 0;
    if (inHand.hasItemMeta()) {
      if (inHand.getItemMeta().hasEnchant(Enchantment.KNOCKBACK)) {
        knockback = inHand.getItemMeta().getEnchantLevel(Enchantment.KNOCKBACK);
      } //else if (inHand.getItemMeta().hasEnchant(Enchantment.FIRE_ASPECT)) {
      //  fire = inHand.getItemMeta().getEnchantLevel(Enchantment.FIRE_ASPECT);
      //}
    }
//Ostrov.log_warn("§5knockback="+knockback);


    AttributeInstance ai = source.getAttribute(Attribute.ATTACK_DAMAGE);
    if (ai == null) {
      if (attackMod != null) {
//Ostrov.log_warn("§bsource registerAttribute ATTACK_DAMAGE + addModifier");
        source.registerAttribute(Attribute.ATTACK_DAMAGE);
        ai = source.getAttribute(Attribute.ATTACK_DAMAGE);
        ai.addModifier(attackMod); //создали - сразу добавляем
      }
    } else {
      boolean add = false;
      for (AttributeModifier mod : ai.getModifiers()) {
        if (mod.getKey().getKey().equals("base_attack_damage")) {
          if (attackMod == null) {
            ai.removeModifier(attackMod);
//Ostrov.log_warn("§6source has base_attack_damage, remove");
          } else if (mod.getAmount() != attackMod.getAmount()) {
            ai.removeModifier(attackMod);
            add = true;
//Ostrov.log_warn("§6source mod.getAmount()!=attackMod.getAmount, remove");
          }
        }
      }
      if (add) {
        ai.addModifier(attackMod);
//Ostrov.log_warn("§a++source addModifier value="+attackMod.getAmount());
      }
    }

//Ostrov.log_warn("§a++source Mod ATTACK_DAMAGE result="+source.getAttribute(Attribute.ATTACK_DAMAGE).getValue());

    LivingEntity attacker = Craft.toNMS(source);
    ServerLevel level = attacker.level().getMinecraftWorld();//Craft.toNMS(p.getWorld());
    final float base_damage = (float) attacker.getAttributeValue(Attributes.ATTACK_DAMAGE);
    net.minecraft.world.item.ItemStack weaponItem = attacker.getWeaponItem();//Craft.toNMS(it);

    DamageSource damageSource = (DamageSource) Optional.ofNullable(weaponItem.getItem().getDamageSource(attacker))
        .orElse(attacker.damageSources().mobAttack(attacker));
//Ostrov.log_warn("DamageSource type="+damageSource.getEntity().getType()+" Weapon="+damageSource.getWeaponItem().asBukkitCopy().getType());
    net.minecraft.world.entity.Entity target;
    for (org.bukkit.entity.Entity entity : entitys) {
      target = Craft.toNMS(entity);
      float damage = EnchantmentHelper.modifyDamage(level, weaponItem, target, damageSource, base_damage);
      //damage += weaponItem.getItem().getAttackDamageBonus(target, damage, damageSource); //тут добавить только булаве
      boolean succes = target.hurtServer(level, damageSource, damage);

      if (succes) {
//Ostrov.log_warn("GameApi.attack "+source.getType()+"->"+entity.getType()+" final damage="+damage+" knockback="+knockback);
        if (target instanceof LivingEntity livingEntity) {
          if (knockback > 0) {
            livingEntity.knockback(
                knockback * 0.5F,
                Mth.sin(attacker.getYRot() * (float) (Math.PI / 180.0)),
                -Mth.cos(attacker.getYRot() * (float) (Math.PI / 180.0)),
                attacker, io.papermc.paper.event.entity.EntityKnockbackEvent.Cause.ENTITY_ATTACK // CraftBukkit // Paper - knockback events
            );
          }
          //EnchantmentHelper.doPostAttackEffectsWithItemSourceOnBreak(level, target, damageSource, weaponItem, null);
          EnchantmentHelper.doPostAttackEffects(level, target, damageSource);
          weaponItem.hurtEnemy((LivingEntity) target, attacker);
          weaponItem.getItem().hurtEnemy(weaponItem, (LivingEntity) target, attacker);
        }

        //if (source instanceof LivingEntity livingEntity) {
        //  weaponItem.hurtEnemy(livingEntity, attacker);
        //}

        //this.setLastHurtMob(source);
        //this.playAttackSound();
      }
    }

  }

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
          Nms.setNmsData(sl, sl.getBlockState(Nms.mutableBlockPosition), bs);
          //sl.setBlock(mutableBlockPosition, bs,)
        }
      }
    }
  }

}
