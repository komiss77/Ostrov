package ru.komiss77.utils;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import ru.komiss77.Ostrov;


public class EntityUtil {

    public static @Nullable LivingEntity lastDamager(final LivingEntity ent, final boolean owner) {
        return getDamager(ent.getLastDamageCause(), owner);
    }

    public static @Nullable LivingEntity getDamager(final EntityDamageEvent e, final boolean owner) {
        if (e instanceof final EntityDamageByEntityEvent ev) {
            if (ev.getDamager() instanceof Projectile && ((Projectile) ev.getDamager()).getShooter() instanceof final LivingEntity le) {
                if (le instanceof final Tameable tm && owner) {
                    return tm.getOwner() instanceof HumanEntity ? ((HumanEntity) tm.getOwner()) : null;
                } else return le;
            } else if (ev.getDamager() instanceof final LivingEntity le) {
                if (le instanceof final Tameable tm && owner) {
                    return tm.getOwner() instanceof HumanEntity ? ((HumanEntity) tm.getOwner()) : null;
                } else return le;
            }
        }
        return null;
    }

    public List<Projectile> shoot(final LivingEntity shooter, final ItemStack prj) {
        ItemStack weapon = shooter.getEquipment().getItemInMainHand();

        if (!ItemType.BOW.equals(weapon.getType().asItemType())
            && !ItemType.CROSSBOW.equals(weapon.getType().asItemType())) {
            weapon = shooter.getEquipment().getItemInOffHand();
        }

        if (!ItemType.BOW.equals(weapon.getType().asItemType())
            && !ItemType.CROSSBOW.equals(weapon.getType().asItemType())) {
            return List.of();
        }

        final Class<? extends AbstractArrow> arc;
        if (ItemType.ARROW.equals(prj.getType().asItemType())) {
            arc = Arrow.class;
        } else if (ItemType.SPECTRAL_ARROW.equals(prj.getType().asItemType())) {
            arc = SpectralArrow.class;
        } else if (ItemType.TIPPED_ARROW.equals(prj.getType().asItemType())) {
            arc = Arrow.class;
        } else if (ItemType.FIREWORK_ROCKET.equals(prj.getType().asItemType())) {
            final List<Projectile> arrows = new ArrayList<>();
            final int arrowCount = weapon.getEnchantmentLevel(Enchantment.MULTISHOT) > 0 ? 3 : 1;

            for (int i = 0; i < arrowCount; i++) {
                final Firework arrow = shooter.launchProjectile(Firework.class);
                if (prj.getItemMeta() instanceof final FireworkMeta fm) arrow.setFireworkMeta(fm);

                if (i > 0) {
                    final Vector spread = arrow.getVelocity().getCrossProduct(
                        new Vector(0, 1, 0)).normalize().multiply(0.3d * (i == 1 ? -1d : 1d));
                    arrow.setVelocity(arrow.getVelocity().add(spread));
                }

                arrows.add(arrow);
            }

            return arrows;
        } else return List.of();

        final List<Projectile> arrows = new ArrayList<>();
        final int arrowCount = weapon.getEnchantmentLevel(Enchantment.MULTISHOT) > 0 ? 3 : 1;

        for (int i = 0; i < arrowCount; i++) {
            final AbstractArrow arrow = shooter.launchProjectile(arc);
            if (arrow instanceof Arrow ar) {
                if (prj.getItemMeta() instanceof final PotionMeta pm) {
                    ar.setBasePotionType(pm.getBasePotionType());
                }
            }
            arrow.setPickupStatus(AbstractArrow.PickupStatus.DISALLOWED);
            arrow.setWeapon(weapon);

            if (i > 0) {
                final Vector spread = arrow.getVelocity().getCrossProduct(
                    new Vector(0, 1, 0)).normalize().multiply(0.3d * (i == 1 ? -1d : 1d));
                arrow.setVelocity(arrow.getVelocity().add(spread));
            }

            arrows.add(arrow);
        }

        return arrows;
    }

    public static EntityGroup group(final Entity e) {
        return group(e.getType());
    }

    public static EntityGroup group(final EntityType type) {
        // type.getKey();
        //EnumCreatureType enumcreaturetype = (net.minecraft.world.Entiti)entity.(net.minecraft.world.EntityTypes)ae().(net.minecraft.world.EnumCreatureType)f();
        //  EntityTypes et = EntityTypes.a("").get();
        switch (type) {
            case RAVAGER:
            case PILLAGER:
            case ZOGLIN:
            case PIGLIN:
            case DROWNED:
            case SHULKER:
            case ENDERMITE:
            case WITCH:
            case ENDER_DRAGON:
            case MAGMA_CUBE:
            case BLAZE:
            case SILVERFISH:
            case ENDERMAN:
            case ZOMBIFIED_PIGLIN:
            case GIANT:
            case CREEPER:
            case SPIDER:
            case GHAST:
            case SLIME:
            case PHANTOM:
            case ZOMBIE:
            case SKELETON:
            case CAVE_SPIDER:
            case GUARDIAN:
            case ZOMBIE_VILLAGER:
            case VEX:
            case VINDICATOR:
            case EVOKER:
            case ILLUSIONER:
            case WITHER:
            case WITHER_SKELETON:
            case STRAY:
            case HUSK:
            case PIGLIN_BRUTE:
            case WARDEN:
            case ELDER_GUARDIAN:
            case BREEZE:
            case BOGGED:
                return EntityGroup.MONSTER;


            case PARROT:
            case LLAMA_SPIT:
            case LLAMA:
            case RABBIT:
            case CAT:
            case HORSE:
            case OCELOT:
            case FOX:
            case MOOSHROOM:
            case WOLF:
            case COW:
            case SHEEP:
            case POLAR_BEAR:
            case PIG:
            case PANDA:
            case BEE:
            case CHICKEN:
            case VILLAGER:
            case WANDERING_TRADER:
            case IRON_GOLEM:
            case SNOW_GOLEM:
            case DONKEY:
            case MULE:
            case SKELETON_HORSE:
            case ZOMBIE_HORSE:
            case TURTLE:
            case HOGLIN:
            case GOAT:
            case CAMEL:
            case SNIFFER:
            case TRADER_LLAMA:
            case ALLAY:
            case STRIDER:
            case ARMADILLO:
                return EntityGroup.CREATURE;


            case BAT:
                return EntityGroup.AMBIENT;


            case DOLPHIN:
            case SQUID:
            case GLOW_SQUID:
            case AXOLOTL:
            case FROG:
            case TADPOLE:
                return EntityGroup.WATER_CREATURE;


            case TROPICAL_FISH:
            case COD:
            case SALMON:
            case PUFFERFISH:
                return EntityGroup.WATER_AMBIENT;


            case AREA_EFFECT_CLOUD:
            case ARMOR_STAND:
            case ARROW:
            case BOAT:
            case DRAGON_FIREBALL:
            case ITEM:
            case EGG:
            case END_CRYSTAL:
            case ENDER_PEARL:
                //case ENDER_SIGNAL: ЧТО ЭТО????
            case EYE_OF_ENDER:
            case WIND_CHARGE:
            case BREEZE_WIND_CHARGE:
                break;
            case EVOKER_FANGS:
            case EXPERIENCE_ORB:
            case FALLING_BLOCK:
            case FIREBALL:
            case FIREWORK_ROCKET:
            case FISHING_BOBBER:
            case ITEM_FRAME:
            case LEASH_KNOT:
            case LIGHTNING_BOLT:
            case MINECART:
            case CHEST_MINECART:
            case COMMAND_BLOCK_MINECART:
            case FURNACE_MINECART:
            case HOPPER_MINECART:
            case SPAWNER_MINECART:
            case TNT_MINECART:
            case PAINTING:
            case TNT:
            case SHULKER_BULLET:
            case SMALL_FIREBALL:
            case SNOWBALL:
            case SPECTRAL_ARROW:
            case POTION:
            case EXPERIENCE_BOTTLE:
            case TRIDENT:
            case UNKNOWN:
            case WITHER_SKULL:
            case PLAYER:
            case GLOW_ITEM_FRAME:
            case CHEST_BOAT:
            case OMINOUS_ITEM_SPAWNER:

            case MARKER:
            case INTERACTION:
            case ITEM_DISPLAY:
            case TEXT_DISPLAY:
            case BLOCK_DISPLAY:
                break;
        }

        //если выше ничего не выстрелило, то определяем о старинке
        return EntityGroup.UNDEFINED;
    }

    public static EntityType typeFromEgg(final Material mat) {
        try {
            return EntityType.valueOf(mat.name().replaceFirst("_SPAWN_EGG", ""));
        } catch (IllegalArgumentException ex) {
            Ostrov.log_warn("EntityUtil typeFromEgg : " + mat + " не конвертируется в EntityType!");
        }
        return null;
    }


    public enum EntityGroup {
        /**
         * Монстры, могут агрится на игрока
         */
        MONSTER("§4Монстры", Material.ZOMBIE_HEAD), //не переименовывать! или придётся переделывать конфиги лимитера!!
        /**
         * Животные, могут быть скрещеными
         */
        CREATURE("§2Сухопутные животные", Material.LEATHER_HORSE_ARMOR),
        /**
         * Обитатели, улучшают атмосферу
         */
        AMBIENT("§5Сухопутные обитатели", Material.COAL),
        /**
         * Спруты и делифины, декор
         */
        WATER_CREATURE("§bВодные животные", Material.NAUTILUS_SHELL),
        /**
         * Рибки с которых падает рыба
         */
        WATER_AMBIENT("§1Водные обитатели", Material.TROPICAL_FISH),
        /**
         * Прочие сущности, не мобы
         */
        UNDEFINED("§6Прочие", Material.ARMOR_STAND),

        TILE("§3TileEntity", Material.ITEM_FRAME),

        TICKABLE_TILE("§3TickableTile", Material.BLUE_SHULKER_BOX);

        public static EntityGroup matchGroup(String groupName) {
            for (EntityGroup g : EntityGroup.values()) {
                if (g.name().equalsIgnoreCase(groupName)) {
                    return g;
                }
            }
            return EntityGroup.UNDEFINED;
        }

        public final String displayName;
        public final Material displayMat;

        EntityGroup(final String displayName, final Material displayMat) {
            this.displayName = displayName;
            this.displayMat = displayMat;
        }

        public static int getWorldSpawnLimit(final World world, final EntityGroup group) {
            return switch (group) {
                case MONSTER -> world.getSpawnLimit(SpawnCategory.MONSTER);
                case CREATURE -> world.getSpawnLimit(SpawnCategory.ANIMAL) + world.getSpawnLimit(SpawnCategory.AXOLOTL);
                case AMBIENT -> world.getSpawnLimit(SpawnCategory.AMBIENT);
                case WATER_CREATURE -> world.getSpawnLimit(SpawnCategory.WATER_ANIMAL);
                case WATER_AMBIENT -> world.getSpawnLimit(SpawnCategory.WATER_AMBIENT)
                    + world.getSpawnLimit(SpawnCategory.WATER_UNDERGROUND_CREATURE);
                default -> 0;//world.getSpawnLimit(SpawnCategory.MISC); IllegalArgumentException: SpawnCategory.MISC are not supported
            };
        }

    }


}
