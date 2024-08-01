package ru.komiss77.listener;

import java.util.HashMap;
import java.util.stream.Collectors;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import org.bukkit.entity.Player;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.ShulkerBox;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.enchantments.Enchantment;
import ru.komiss77.modules.menuItem.MenuItemsManager;
import ru.komiss77.utils.ItemUtils;
import ru.komiss77.utils.TCUtils;

//https://github.com/ds58/Panilla
//https://github.com/alexandrage/CreativeItemFilter/tree/master

public class NbtLst {

    private static final MetaCopierFactory metaCopierFactory;

    static {
        metaCopierFactory = new MetaCopierFactory();
    }

    public static void rebuildInventoryContrnt(final Player p) {
        ItemStack oldItem;
        for (int i = 0; i < p.getInventory().getContents().length; i++) {
            oldItem = p.getInventory().getContents()[i];
            if (oldItem != null) {
                p.getInventory().setItem(i, rebuild(oldItem));
                //p.getInventory().setItem(i, copyItemMeta(oldItem));
            }
        }

        for (int i = 0; i < p.getEnderChest().getContents().length; i++) {
            oldItem = p.getEnderChest().getContents()[i];
            if (oldItem != null) {
                p.getEnderChest().setItem(i, new ItemStack(rebuild(oldItem)));
                //p.getEnderChest().setItem(i, new ItemStack(copyItemMeta(oldItem)));
            }
        }
        p.updateInventory();
    }

    public static ItemStack rebuild(final ItemStack oldItem) {
//Ostrov.log_warn("copyItemMeta oldItem="+oldItem);

        if (MenuItemsManager.isSpecItem(oldItem))
            return oldItem; //if (VM.getNmsNbtUtil().hasString(oldItem, "ostrovItem")) return oldItem;

        final ItemStack newItem = new ItemStack(oldItem.getType(), Math.min(oldItem.getAmount(), oldItem.getMaxStackSize()));

        if (oldItem.hasItemMeta()) {

            final ItemMeta oldMeta = oldItem.getItemMeta();

            final ItemMeta newMeta = metaCopierFactory.getCopier(oldMeta).copyValidMeta(oldMeta, newItem.getType());

            if (oldMeta.hasDisplayName()) {
                newMeta.displayName(Component.text(StringUtils.clampString(TCUtils.deform(oldMeta.displayName()))));
            }

            if (oldMeta.hasLore()) { //яички с лоре через раздатчик крашат сервер
                newMeta.lore(oldMeta.lore().stream().map(lr -> TCUtils.form(StringUtils.clampString(TCUtils.deform(lr)))).collect(Collectors.toList()));
            }

            // copy enchantments
            //for (Enchantment enc : oldItem.getEnchantments().keySet()) {
            //   newItem.addUnsafeEnchantment(enc, oldItem.getEnchantments().get(enc) > enc.getMaxLevel() ? enc.getMaxLevel() : oldItem.getEnchantments().get(enc));
            // }
            //oldItem.getEnchantments().entrySet().stream().filter(entry -> entry.getValue() <= ((Enchantment)entry.getKey()).getMaxLevel()).forEach(entry -> newItem.addUnsafeEnchantment((Enchantment)entry.getKey(), (int)entry.getValue()));
            oldItem.getEnchantments().entrySet().stream()
                .filter(entry -> entry.getValue() <= entry.getKey().getMaxLevel())
                .forEach(entry -> newItem.addUnsafeEnchantment(entry.getKey(), entry.getValue()));

            // copy modifier
            //if (oldMeta.hasAttributeModifiers()) {
            //  oldMeta.getAttributeModifiers().asMap().forEach((key, value) -> value.stream().filter(mod -> mod.getAmount() <= 10.0).forEach(atr -> newMeta.addAttributeModifier(key, atr)));
            //}
            if (oldMeta.hasAttributeModifiers()) {
                oldMeta.getAttributeModifiers().asMap().entrySet()
                    .forEach(entry -> entry.getValue().stream().filter(mod -> mod.getAmount() <= 10)
                        .forEach(atr -> newMeta.addAttributeModifier(entry.getKey(), atr)));
            }

            // copy modeldata
            if (oldItem.getItemMeta().hasCustomModelData()) {
                newItem.getItemMeta().setCustomModelData(oldItem.getItemMeta().getCustomModelData());
            }

            // copy damage
            if (oldItem.getItemMeta() instanceof Damageable dOmeta) {
                final Damageable dNmeta = (Damageable) newItem.getItemMeta();
                if (dOmeta.hasDamage()) {
                    dNmeta.setDamage(dOmeta.getDamage());
                    newItem.setItemMeta(dNmeta);
                }
            }

            // copy repaircost
            if (oldItem.getItemMeta() instanceof Repairable rOmeta) {
                final Repairable rNmeta = (Repairable) newItem.getItemMeta();
                if (rOmeta.hasRepairCost()) {
                    rNmeta.setRepairCost(rOmeta.getRepairCost());
                    newItem.setItemMeta(rNmeta);
                }
            }

            newItem.setItemMeta(newMeta);

        }

        return newItem;


    }


    public static boolean invalidStackSize(final Player player, final ItemStack item) {
        if (item.getAmount() > item.getMaxStackSize()) {
            player.sendMessage("Размер стака превышает допустимый!");
            return true;
        }
        return false;
    }

    public static boolean Invalid_name_lenght(final Player player, final ItemStack item) {
        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName() && TCUtils.deform(item.getItemMeta().displayName()).length() > 40) {
            player.sendMessage("Превышена длина имени!");
            return true;
        }
        return false;
    }

    public static boolean Invalid_enchant(final Player player, final ItemStack item) {
        try {
            if (item.getEnchantments().isEmpty()) return false;
            for (Enchantment enchant : item.getEnchantments().keySet()) {
//System.out.println(" ------- enchant "+enchant+"   "+enchant.name()+"  lvl="+item.getEnchantments().get(enchant)+"  start="+enchant.getStartLevel()+"  max="+enchant.getMaxLevel());
                if (item.getEnchantments().get(enchant) < 0 || item.getEnchantments().get(enchant) > enchant.getMaxLevel())
                    return true;
            }
        } catch (IllegalArgumentException | NullPointerException ex) {
            player.sendMessage("Этот предмет содержит недоапустимые чары!");
            return true;
            //return new ItemStack(item.getType(), item.getAmount());
        }
        return false;
    }

    public static boolean Invalid_anvill(final Player player, final ItemStack item) {
        if (!ItemUtils.isBlank(item, false) && item.getType() == Material.ANVIL && item.getItemMeta() instanceof final Damageable dg) {
            if (dg.hasDamage()) {
                return switch (dg.getDamage()) {
                    case 0, 1, 2 -> false;
                    default -> true;
                };
            }
        }
        return false;
    }


    private static class MetaCopierFactory {
        private static final BlockStateMetaCopier blockStateMetaCopier;
        private static final HashMap<Class<? extends ItemMeta>, MetaCopier<ItemMeta>> copierCache;

        static {
            copierCache = new HashMap<>();
            blockStateMetaCopier = new BlockStateMetaCopier();
        }


        public MetaCopier<ItemMeta> getCopier(final ItemMeta oldMeta) {
            final Class<? extends ItemMeta> metaClass = oldMeta.getClass();
            final MetaCopier<ItemMeta> cached = copierCache.get(metaClass);
            if (cached != null) {
                return cached;
            }
            if (oldMeta instanceof BannerMeta) {
                return this.cache(metaClass, BannerMetaCopier.INSTANCE);
            } else if (oldMeta instanceof EnchantmentStorageMeta) {
                return this.cache(metaClass, EnchantmentStorageMetaCopier.INSTANCE);
            } else if (oldMeta instanceof BookMeta) {
                return this.cache(metaClass, BookMetaCopier.INSTANCE);
            } else if (oldMeta instanceof PotionMeta) {
                return this.cache(metaClass, PotionMetaCopier.INSTANCE);
            } else if (oldMeta instanceof LeatherArmorMeta) {
                return this.cache(metaClass, LeatherArmorMetaCopier.INSTANCE);
            } else if (oldMeta instanceof TropicalFishBucketMeta) {
                return this.cache(metaClass, TropicalFishBucketMetaCopier.INSTANCE);
            } else if (oldMeta instanceof FireworkMeta) {
                return this.cache(metaClass, FireworkMetaCopier.INSTANCE);
            } else if (oldMeta instanceof FireworkEffectMeta) {
                return this.cache(metaClass, FireworkEffectMetaCopier.INSTANCE);
            } else if (oldMeta instanceof MapMeta) {
                return this.cache(metaClass, MapMetaCopier.INSTANCE);
            } else if (oldMeta instanceof BlockStateMeta) {
                return this.cache(metaClass, new BlockStateMetaCopier());
            } else if (oldMeta instanceof CompassMeta) {
                return this.cache(metaClass, CompassMetaCopier.INSTANCE);
            } else if (oldMeta instanceof CrossbowMeta) {
                return this.cache(metaClass, CrossbowMetaCopier.INSTANCE);
            } else if (oldMeta instanceof KnowledgeBookMeta) {
                return this.cache(metaClass, KnowledgeBookMetaCopier.INSTANCE);
            } else if (oldMeta instanceof SuspiciousStewMeta) {
                return this.cache(metaClass, SuspiciousStewMetaCopier.INSTANCE);
            } else if (oldMeta instanceof SkullMeta) {
                return this.cache(metaClass, SkullMetaCopier.INSTANCE);
            }
            return NoOpMetaCopier.INSTANCE;
        }

        @SuppressWarnings({"unchecked", "rawtypes"})
        protected MetaCopier<ItemMeta> cache(final Class<? extends ItemMeta> clazz, final MetaCopier copier) {
            copierCache.put(clazz, copier);
            return copier;
        }
    }


    public interface MetaCopier<T extends ItemMeta> {
        ItemMeta copyValidMeta(final T p0, final Material p1);
    }


    public static class StringUtils {
        public static String clampString(final String string) {
            return clampString(string, 64);
        }

        public static String clampString(String string, final int limit) {
            if (string.length() > limit) string = string.substring(0, limit);
            return string.replaceAll("[^A-Za-zА-Яа-я0-9§\\s.]", "");
            //(string.length() < limit) ? string : string.substring(0, limit);
        }
    }


    public static class BannerMetaCopier implements MetaCopier<BannerMeta> {
        public static final BannerMetaCopier INSTANCE;

        static {
            INSTANCE = new BannerMetaCopier();
        }

        @Override
        public BannerMeta copyValidMeta(final BannerMeta oldMeta, final Material material) {
            final BannerMeta newMeta = (BannerMeta) Bukkit.getItemFactory().getItemMeta(material);
            oldMeta.getPatterns().forEach(newMeta::addPattern);
            return newMeta;
        }
    }

    public static class EnchantmentStorageMetaCopier implements MetaCopier<EnchantmentStorageMeta> {
        public static final EnchantmentStorageMetaCopier INSTANCE;

        static {
            INSTANCE = new EnchantmentStorageMetaCopier();
        }

        @Override
        public ItemMeta copyValidMeta(final EnchantmentStorageMeta oldMeta, final Material material) {
            final EnchantmentStorageMeta newEnchBookMeta = (EnchantmentStorageMeta) Bukkit.getItemFactory().getItemMeta(material);
            if (oldMeta.hasStoredEnchants()) {
                oldMeta.getStoredEnchants().entrySet().stream().filter(entry -> entry.getValue() <= entry.getKey().getMaxLevel()).forEach(entry -> newEnchBookMeta.addStoredEnchant(entry.getKey(), entry.getValue(), true));
            }
            return newEnchBookMeta;
        }
    }

    public static class BookMetaCopier implements MetaCopier<BookMeta> {
        public static final BookMetaCopier INSTANCE;

        static {
            INSTANCE = new BookMetaCopier();
        }

        @Override
        public ItemMeta copyValidMeta(final BookMeta oldMeta, final Material material) {
            final int pages = 50;
            final BookMeta newBookMeta = (BookMeta) Bukkit.getItemFactory().getItemMeta(material);
            if (oldMeta.hasAuthor()) {
                newBookMeta.setAuthor(StringUtils.clampString(oldMeta.getAuthor(), 16));
            }
            if (oldMeta.hasTitle()) {
                newBookMeta.setTitle(StringUtils.clampString(oldMeta.getTitle()));
            }
            if (oldMeta.hasGeneration()) {
                newBookMeta.setGeneration(oldMeta.getGeneration());
            }
            if (oldMeta.hasPages() && oldMeta.pages().size() <= pages) {
                newBookMeta.pages(oldMeta.pages().stream().map(cmp -> Component.text(StringUtils.clampString(TCUtils.deform(cmp), 16383))).collect(Collectors.toList()));
            } else {
                newBookMeta.pages(new Component[]{Component.text(" ")});
            }
            return newBookMeta;
        }
    }

    public static class PotionMetaCopier implements MetaCopier<PotionMeta> {
        public static final PotionMetaCopier INSTANCE;

        static {
            INSTANCE = new PotionMetaCopier();
        }

        @Override
        public ItemMeta copyValidMeta(final PotionMeta oldMeta, final Material material) {
            final PotionMeta newMeta = (PotionMeta) Bukkit.getItemFactory().getItemMeta(material);
            newMeta.setBasePotionType(oldMeta.getBasePotionType());
            if (oldMeta.hasCustomEffects()) {
                oldMeta.getCustomEffects().stream().filter(effect -> effect.getAmplifier() < 2).filter(effect -> effect.getDuration() < 600).forEach(effect -> newMeta.addCustomEffect(effect, true));
            }
            return newMeta;
        }
    }

    public static class LeatherArmorMetaCopier implements MetaCopier<LeatherArmorMeta> {
        public static final LeatherArmorMetaCopier INSTANCE;

        static {
            INSTANCE = new LeatherArmorMetaCopier();
        }

        @Override
        public ItemMeta copyValidMeta(final LeatherArmorMeta oldMeta, final Material material) {
            final LeatherArmorMeta newMeta = (LeatherArmorMeta) Bukkit.getItemFactory().getItemMeta(material);
            newMeta.setColor(oldMeta.getColor());
            return newMeta;
        }
    }

    public static class TropicalFishBucketMetaCopier implements MetaCopier<TropicalFishBucketMeta> {
        public static final TropicalFishBucketMetaCopier INSTANCE;

        static {
            INSTANCE = new TropicalFishBucketMetaCopier();
        }

        @Override
        public ItemMeta copyValidMeta(final TropicalFishBucketMeta oldMeta, final Material material) {
            final TropicalFishBucketMeta newMeta = (TropicalFishBucketMeta) Bukkit.getItemFactory().getItemMeta(material);
            newMeta.setBodyColor(oldMeta.getBodyColor());
            newMeta.setPattern(oldMeta.getPattern());
            newMeta.setPatternColor(oldMeta.getPatternColor());
            return newMeta;
        }
    }

    public static class FireworkMetaCopier implements MetaCopier<FireworkMeta> {
        public static final FireworkMetaCopier INSTANCE;

        static {
            INSTANCE = new FireworkMetaCopier();
        }

        @Override
        public ItemMeta copyValidMeta(final FireworkMeta oldMeta, final Material material) {
            final FireworkMeta newMeta = (FireworkMeta) Bukkit.getItemFactory().getItemMeta(material);
            if (oldMeta.getEffectsSize() < 50) {
                newMeta.addEffects(oldMeta.getEffects());
            }
            return oldMeta;
        }
    }

    public static class FireworkEffectMetaCopier implements MetaCopier<FireworkEffectMeta> {
        public static final FireworkEffectMetaCopier INSTANCE;

        static {
            INSTANCE = new FireworkEffectMetaCopier();
        }

        @Override
        public ItemMeta copyValidMeta(final FireworkEffectMeta oldMeta, final Material material) {
            final FireworkEffectMeta newMeta = (FireworkEffectMeta) Bukkit.getItemFactory().getItemMeta(material);
            if (oldMeta.hasEffect()) {
                newMeta.setEffect(oldMeta.getEffect());
            }
            return oldMeta;
        }
    }

    public static class MapMetaCopier implements MetaCopier<MapMeta> {
        public static final MapMetaCopier INSTANCE;

        static {
            INSTANCE = new MapMetaCopier();
        }

        @Override
        public ItemMeta copyValidMeta(final MapMeta oldMeta, final Material material) {
            final MapMeta newMeta = (MapMeta) Bukkit.getItemFactory().getItemMeta(material);
            if (oldMeta.hasColor()) {
                newMeta.setColor(oldMeta.getColor());
            }
            if (oldMeta.displayName() instanceof TranslatableComponent) {
                newMeta.displayName(oldMeta.displayName());
            }
            if (oldMeta.hasMapView()) {
                newMeta.setMapView(oldMeta.getMapView());
            }
            if (oldMeta.isScaling()) {
                newMeta.setScaling(true);
            }
            return newMeta;
        }
    }

    private static class BlockStateMetaCopier implements MetaCopier<BlockStateMeta> {
        @Override
        public ItemMeta copyValidMeta(final BlockStateMeta oMeta, final Material material) {
            final BlockStateMeta nMeta = (BlockStateMeta) Bukkit.getItemFactory().getItemMeta(material);
            final BlockState state = oMeta.getBlockState();
            if (state instanceof ShulkerBox oldBox) {
                for (int i = 0; i < oldBox.getInventory().getSize(); ++i) {
                    final ItemStack stack = oldBox.getInventory().getItem(i);
                    if (stack != null) {
                        oldBox.getInventory().setItem(i, rebuild(stack));
                    }
                }
                state.update();
                nMeta.setBlockState(state);
            }
            return nMeta;
        }
    }


    private static class CompassMetaCopier implements MetaCopier<CompassMeta> {
        public static final CompassMetaCopier INSTANCE = new CompassMetaCopier();

        @Override
        public ItemMeta copyValidMeta(CompassMeta oldMeta, Material material) {
            CompassMeta newMeta = (CompassMeta) Bukkit.getItemFactory().getItemMeta(material);
            if (oldMeta.hasLodestone()) {
                newMeta.setLodestone(oldMeta.getLodestone());
                newMeta.setLodestoneTracked(oldMeta.isLodestoneTracked());
            }
            return newMeta;
        }
    }

    private static class CrossbowMetaCopier implements MetaCopier<CrossbowMeta> {
        public static final CrossbowMetaCopier INSTANCE = new CrossbowMetaCopier();

        @Override
        public ItemMeta copyValidMeta(CrossbowMeta oldMeta, Material material) {
            CrossbowMeta newMeta = (CrossbowMeta) Bukkit.getItemFactory().getItemMeta(material);
            if (oldMeta.hasChargedProjectiles()) {
                newMeta.setChargedProjectiles(oldMeta.getChargedProjectiles());
            }
            return newMeta;
        }
    }

    private static class KnowledgeBookMetaCopier implements MetaCopier<KnowledgeBookMeta> {
        public static final KnowledgeBookMetaCopier INSTANCE = new KnowledgeBookMetaCopier();

        @Override
        public ItemMeta copyValidMeta(KnowledgeBookMeta oldMeta, Material material) {
            KnowledgeBookMeta newMeta = (KnowledgeBookMeta) Bukkit.getItemFactory().getItemMeta(material);
            if (oldMeta.hasRecipes()) {
                newMeta.setRecipes(oldMeta.getRecipes());
            }
            return newMeta;
        }
    }


    private static class SuspiciousStewMetaCopier implements MetaCopier<SuspiciousStewMeta> {
        public static final SuspiciousStewMetaCopier INSTANCE = new SuspiciousStewMetaCopier();

        @Override
        public ItemMeta copyValidMeta(SuspiciousStewMeta oldMeta, Material material) {
            SuspiciousStewMeta newMeta = (SuspiciousStewMeta) Bukkit.getItemFactory().getItemMeta(material);
            if (oldMeta.hasCustomEffects()) {
                for (PotionEffect effects : oldMeta.getCustomEffects()) {
                    newMeta.addCustomEffect(effects, false);
                }
            }
            return newMeta;
        }
    }

    private static class SkullMetaCopier implements MetaCopier<SkullMeta> {
        public static final SkullMetaCopier INSTANCE = new SkullMetaCopier();

        @Override
        public SkullMeta copyValidMeta(SkullMeta oldMeta, Material material) {
            SkullMeta newMeta = (SkullMeta) Bukkit.getItemFactory().getItemMeta(material);
            if (oldMeta.hasOwner()) {
                newMeta.setPlayerProfile(oldMeta.getPlayerProfile());
            }
            return newMeta;
        }
    }

    public static class NoOpMetaCopier implements MetaCopier<ItemMeta> {
        public static final NoOpMetaCopier INSTANCE;

        static {
            INSTANCE = new NoOpMetaCopier();
        }

        @Override
        public ItemMeta copyValidMeta(final ItemMeta oldMeta, final Material material) {
            return Bukkit.getItemFactory().getItemMeta(material);
        }
    }


}







/*


   // @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onJoin(PlayerJoinEvent e) {
        rebuildInventoryContrnt(e.getPlayer());
    }


    public static void rebuildInventoryContrnt(final Player p) {
        ItemStack oldItem;
        for (int i=0; i<p.getInventory().getContents().length; i++) {
            oldItem = p.getInventory().getContents()[i];
            if (oldItem!=null) {
                p.getInventory().setItem(i, rebuild(oldItem));
            }
        }

        for (int i=0; i<p.getEnderChest().getContents().length; i++) {
            oldItem = p.getEnderChest().getContents()[i];
            if (oldItem!=null) {
                p.getEnderChest().setItem(i, new ItemStack(rebuild(oldItem)));
            }
        }
        p.updateInventory();
    }




   // @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryCreativeEvent(InventoryCreativeEvent e) {
//System.out.println("NbtListener:InventoryCreativeEvent");

        final ItemStack oldItem = e.getCursor();

        e.setCursor(rebuild(oldItem));
        e.setCurrentItem(rebuild(oldItem));

       /* if (needCheck(e.getCursor())) {
            if (invalidStackSize((Player) e.getWhoClicked(), e.getCursor())) e.getCursor().setAmount(e.getCursor().getMaxStackSize());
            if (Invalid_name_lenght((Player) e.getWhoClicked(), e.getCursor())) e.getCursor().getItemMeta().setDisplayName(e.getCursor().getItemMeta().getDisplayName().substring(0,28));
            if (Invalid_anvill((Player) e.getWhoClicked(), e.getCursor())) e.setCursor(new ItemStack( e.getCursor().getType(),  e.getCursor().getAmount()));
            if (Invalid_enchant((Player) e.getWhoClicked(), e.getCursor())) e.setCursor( Repair_enchant(e.getCurrentItem()));
            if (hasInvalidNbt((Player) e.getWhoClicked(), e.getCursor())) e.setCursor(new ItemStack( e.getCursor().getType(),  e.getCursor().getAmount()));
        }
        if (needCheck(e.getCurrentItem())) {
            if (invalidStackSize((Player) e.getWhoClicked(), e.getCurrentItem())) e.getCurrentItem().setAmount(e.getCurrentItem().getMaxStackSize());
            if (Invalid_name_lenght((Player) e.getWhoClicked(), e.getCurrentItem())) e.setCurrentItem(new ItemStack( e.getCurrentItem().getType(),  e.getCurrentItem().getAmount()));
            if (Invalid_anvill((Player) e.getWhoClicked(), e.getCurrentItem())) e.setCurrentItem(new ItemStack( e.getCurrentItem().getType(),  e.getCurrentItem().getAmount()));
            if (Invalid_enchant((Player) e.getWhoClicked(), e.getCurrentItem())) e.setCurrentItem( Repair_enchant(e.getCurrentItem()));
            if (hasInvalidNbt((Player) e.getWhoClicked(), e.getCurrentItem())) e.setCurrentItem(new ItemStack( e.getCurrentItem().getType(),  e.getCurrentItem().getAmount()));
        }/
    }


// @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
public void onPlayerPickupItemEvent(EntityPickupItemEvent e) {
//System.out.println("NbtListener:PlayerPickupItemEvent");
  if (e.getEntityType()!=EntityType.PLAYER || !needCheck(e.getItem().getItemStack()) ) return;
final Player p = (Player) e.getEntity();
  if (invalidStackSize(p, e.getItem().getItemStack())) e.getItem().getItemStack().setAmount(e.getItem().getItemStack().getMaxStackSize());
  if (Invalid_name_lenght(p, e.getItem().getItemStack())) e.getItem().getItemStack().getItemMeta().displayName(Component.text(TCUtils.deform(e.getItem().getItemStack().getItemMeta().displayName()).substring(0,28)));
  if (Invalid_anvill(p, e.getItem().getItemStack())) e.getItem().setItemStack(new ItemStack( e.getItem().getItemStack().getType(),  e.getItem().getItemStack().getAmount()));
  if (Invalid_enchant(p, e.getItem().getItemStack())) e.getItem().setItemStack( Repair_enchant(e.getItem().getItemStack()));
  //if (hasInvalidNbt(p, e.getItem().getItemStack())) e.getItem().setItemStack(new ItemStack( e.getItem().getItemStack().getType(),  e.getItem().getItemStack().getAmount()));
  }




//@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
public void onPlayerDropItemEvent(PlayerDropItemEvent e) {
//System.out.println("NbtListener:PlayerDropItemEvent");
  if ( !needCheck(e.getItemDrop().getItemStack()) ) return;

  if (invalidStackSize(e.getPlayer(), e.getItemDrop().getItemStack())) e.getItemDrop().getItemStack().setAmount(e.getItemDrop().getItemStack().getMaxStackSize());
  if (Invalid_name_lenght(e.getPlayer(), e.getItemDrop().getItemStack())) e.getItemDrop().getItemStack().getItemMeta().displayName(Component.text(TCUtils.deform(e.getItemDrop().getItemStack().getItemMeta().displayName()).substring(0,28)));
  if (Invalid_anvill(e.getPlayer(), e.getItemDrop().getItemStack())) e.getItemDrop().setItemStack(new ItemStack( e.getItemDrop().getItemStack().getType(),  e.getItemDrop().getItemStack().getAmount()));
  if (Invalid_enchant(e.getPlayer(), e.getItemDrop().getItemStack())) e.getItemDrop().setItemStack( Repair_enchant(e.getItemDrop().getItemStack()));
  //if (hasInvalidNbt(e.getPlayer(), e.getItemDrop().getItemStack())) e.getItemDrop().setItemStack(new ItemStack( e.getItemDrop().getItemStack().getType(),  e.getItemDrop().getItemStack().getAmount()));

  }

/*
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryClickEvent(InventoryClickEvent e) {
//System.out.println("NbtListener:InventoryCreativeEvent");
     //   if (!ServerListener.nbt_checker  ) return;

        if (e.getWhoClicked() instanceof Player) {
            if (Need_check(e.getCursor())) {
                if (Invalid_stack_size((Player) e.getWhoClicked(), e.getCursor())) e.getCursor().setAmount(e.getCursor().getMaxStackSize());
                if (Invalid_name_lenght((Player) e.getWhoClicked(), e.getCursor())) e.setCursor(new ItemStack( e.getCursor().getType(),  e.getCursor().getAmount()));
                if (Invalid_anvill((Player) e.getWhoClicked(), e.getCursor())) e.setCursor(new ItemStack( e.getCursor().getType(),  e.getCursor().getAmount()));
                if (Invalid_enchant((Player) e.getWhoClicked(), e.getCursor())) e.setCursor( Repair_enchant(e.getCurrentItem()));
                if (Is_invalid_nbt((Player) e.getWhoClicked(), e.getCursor())) e.setCursor(new ItemStack( e.getCursor().getType(),  e.getCursor().getAmount()));
            }
            if (Need_check(e.getCurrentItem())) {
                if (Invalid_stack_size((Player) e.getWhoClicked(), e.getCurrentItem())) e.getCurrentItem().setAmount(e.getCurrentItem().getMaxStackSize());
                if (Invalid_name_lenght((Player) e.getWhoClicked(), e.getCurrentItem())) e.setCurrentItem(new ItemStack( e.getCurrentItem().getType(),  e.getCurrentItem().getAmount()));
                if (Invalid_anvill((Player) e.getWhoClicked(), e.getCurrentItem())) e.setCurrentItem(new ItemStack( e.getCurrentItem().getType(),  e.getCurrentItem().getAmount()));
                if (Invalid_enchant((Player) e.getWhoClicked(), e.getCurrentItem())) e.setCurrentItem( Repair_enchant(e.getCurrentItem()));
                if (Is_invalid_nbt((Player) e.getWhoClicked(), e.getCurrentItem())) e.setCurrentItem(new ItemStack( e.getCurrentItem().getType(),  e.getCurrentItem().getAmount()));
            }

        }
    }


    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerItemHeld(PlayerItemHeldEvent e) {
        if ( !Need_check(e.getPlayer().getInventory().getItem(e.getNewSlot()))) return;

        if (Invalid_stack_size( e.getPlayer(), e.getPlayer().getInventory().getItem(e.getNewSlot()))) e.getPlayer().getInventory().getItem(e.getNewSlot()).setAmount(e.getPlayer().getInventory().getItem(e.getNewSlot()).getMaxStackSize());
        if (Invalid_name_lenght( e.getPlayer(), e.getPlayer().getInventory().getItem(e.getNewSlot()))) e.getPlayer().getInventory().setItem(e.getNewSlot(), new ItemStack( e.getPlayer().getInventory().getItem(e.getNewSlot()).getType(),  e.getPlayer().getInventory().getItem(e.getNewSlot()).getAmount()));
        if (Invalid_anvill( e.getPlayer(), e.getPlayer().getInventory().getItem(e.getNewSlot()))) e.getPlayer().getInventory().setItem(e.getNewSlot(), new ItemStack( e.getPlayer().getInventory().getItem(e.getNewSlot()).getType(),  e.getPlayer().getInventory().getItem(e.getNewSlot()).getAmount()));
        if (Invalid_enchant(e.getPlayer(), e.getPlayer().getInventory().getItem(e.getNewSlot()))) e.getPlayer().getInventory().setItem(e.getNewSlot(), Repair_enchant(e.getPlayer().getInventory().getItem(e.getNewSlot())));
        if (Is_invalid_nbt( e.getPlayer(), e.getPlayer().getInventory().getItem(e.getNewSlot()))) e.getPlayer().getInventory().setItem(e.getNewSlot(), new ItemStack( e.getPlayer().getInventory().getItem(e.getNewSlot()).getType(),  e.getPlayer().getInventory().getItem(e.getNewSlot()).getAmount()));
    }
 /




public static boolean needCheck (final ItemStack item){
  if ( item == null || item.getType() == Material.AIR ) return false;
  return !Tag.SIGNS.isTagged(item.getType());
  }



public static boolean invalidStackSize(final Player player, final ItemStack item){
  if (item.getAmount()>item.getMaxStackSize()) {
  player.sendMessage("Размер стака превышает допустимый!");
  return true;
  }
  return false;
  }

public static boolean Invalid_name_lenght(final Player player, final ItemStack item){
  if (item.hasItemMeta() && item.getItemMeta().hasDisplayName() && TCUtils.deform(item.getItemMeta().displayName()).length() > 40) {
  player.sendMessage("Превышена длина имени!");
  return true;
  }
  return false;
  }

public static boolean Invalid_enchant(final Player player, final ItemStack item){
  try{
  if (item.getEnchantments().isEmpty()) return false;
  for ( Enchantment enchant : item.getEnchantments().keySet() ) {
//System.out.println(" ------- enchant "+enchant+"   "+enchant.getName()+"  lvl="+item.getEnchantments().get(enchant)+"  start="+enchant.getStartLevel()+"  max="+enchant.getMaxLevel());
  if (item.getEnchantments().get(enchant)<0 || item.getEnchantments().get(enchant)>enchant.getMaxLevel()) return true;
  }
  } catch (IllegalArgumentException | NullPointerException ex) {
  player.sendMessage("Этот предмет содержит недоапустимые чары!");
  return true;
  //return new ItemStack(item.getType(), item.getAmount());
  }
  return false;
  }

public static ItemStack Repair_enchant(ItemStack item) {
//System.out.println(" 1111 Repair_enchant "+item);
  try{
  List<Enchantment> ench_list = new ArrayList<>(item.getEnchantments().keySet());
//System.out.println(" 222 Repair_enchant "+ench_set);
  ench_list.stream().forEach((enchant) -> {
//System.out.println(" ------- enchant "+enchant+"   "+enchant.getName()+"  lvl="+item.getEnchantments().get(enchant));
  item.removeEnchantment(enchant);
  item.addEnchantment(enchant, enchant.getStartLevel());
  });
  return item;
  } catch (IllegalArgumentException | NullPointerException ex) {
  return new ItemStack(item.getType(), item.getAmount());
  }
  }


public static boolean Invalid_anvill(final Player player, final ItemStack item){
  if (!ItemUtils.isBlank(item, false) && item.getType()==Material.ANVIL && item.getItemMeta() instanceof final Damageable dg) {
  if (dg.hasDamage()) {
  return switch (dg.getDamage()) {
  case 0, 1, 2 -> false;
default -> true;
  };
  }
  }
  return false;
  }



  */

