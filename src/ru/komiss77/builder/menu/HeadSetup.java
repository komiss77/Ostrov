package ru.komiss77.builder.menu;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.*;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerTextures;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Config;
import ru.komiss77.Ostrov;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.ItemUtils;
import ru.komiss77.utils.OstrovConfig;
import ru.komiss77.utils.inventory.*;

/*
Перенос текстур из плагина https://docs.tweetzy.ca/official-plugins/skulls     ca.tweetzy.skulls.Skulls
        Ostrov.sync( ()->{
            int i = 0;
            ItemStack is;
            for (BaseCategory bc : BaseCategory.values()) {
                for (Skull sk : Skulls.getAPI().getSkulls(bc)) {
                    //Ostrov.log_warn(sk.getName()+":"+sk.getTexture());
                    i++;
                    final String p = bc.name()+"."+sk.getName().replaceAll("\\.","").replaceAll(":","");
                    if (!p.isEmpty()) {
                        is = sk.getItemStack();
                        SkullMeta sm = ((SkullMeta)is.getItemMeta());
                        //com.destroystokyo.paper.profile.PlayerProfile prof = sm.getPlayerProfile();
                       // prof.g
                        config.set(p, sk.getTexture());
                    }

                    //if (i>50) {
                   //     break;
                  //  }
                }
            }
            config.saveConfig();
            Ostrov.log_warn("========================================");
        }, 100);
        !!!!! объединить ANIMALS и Monsters чтобы стало 9 категорий !!!!!
 */


public class HeadSetup implements InventoryProvider {

    private static final EnumMap<HeadCategory, TreeMap<String, String>> headBase;
    private static final EnumMap<HeadCategory, List<String>> headIdx;
    private static final EnumMap<HeadCategory, ItemStack> icons;
    private static final ClickableItem c = ClickableItem.empty(new ItemStack(Material.BLUE_STAINED_GLASS_PANE));
    //private static final ClickableItem select = ClickableItem.empty(new ItemBuilder(Material.POINTED_DRIPSTONE).name("§8Ниже-варианты на выбор").build());
    private int page;
    private final Block b;
    private HeadCategory selected = HeadCategory.Символы;
    private static OstrovConfig cfg;


    static {
        cfg = Config.manager.getNewConfig("heads.yml");
        headBase = new EnumMap<>(HeadCategory.class);
        headIdx = new EnumMap<>(HeadCategory.class);
        icons = new EnumMap<>(HeadCategory.class);

        for (String categoryName : cfg.getKeys()) {
            try {
                final HeadCategory hc = HeadCategory.from(categoryName);
                if (hc == null) continue;
                final TreeMap<String, String> map = new TreeMap<>();
                cfg.getConfigurationSection(categoryName).getKeys(false).stream().forEach(skullName -> {
                    map.put(skullName, cfg.getString(categoryName + "." + skullName));
                });
                headBase.put(hc, map);
                headIdx.put(hc, new ArrayList<>(map.keySet()));
                final String iconName = map.keySet().stream().findFirst().orElse("");
                icons.put(hc, iconName.isEmpty() ? new ItemStack(Material.BARRIER) : skull(hc.name(), List.of(Component.text("§7В базе : §3" + map.size())), map.get(iconName)));
            } catch (IllegalArgumentException | NullPointerException ex) {
                Ostrov.log_err("LimiterLst reload flags : " + ex.getMessage());
            }
        }
    }

    public HeadSetup(final Block b) {
        this.b = b;
    }

    public static void openSetupMenu(final Player p, final Block b) {
        SmartInventory.builder()
                .provider(new HeadSetup(b))
                .size(6, 9)
                .title("§2Характеристики сущности").build()
                .open(p);
    }


    @Override
    public void init(final Player p, final InventoryContent content) {


        if (b.getType() != Material.PLAYER_HEAD && b.getType() != Material.PLAYER_WALL_HEAD) {
            p.sendMessage("§cБлок уже не голова!");
            return;
        }

        p.playSound(p.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 0.3f, 2);
        final boolean builder = ApiOstrov.isLocalBuilder(p);

        final TreeMap<String, String> map = headBase.get(selected);
        final List<String> idx = headIdx.get(selected);
        final int from = page * 44;
        int to = page * 44 + 36;
        if (to >= idx.size()) to = idx.size();

        final List<Component> lore = builder ? List.of(Component.text("§cQ - удалить")) : null;
        //String name;
        for (int i = from; i < to; i++) {
            final String name = idx.get(i);
            final ItemStack is = skull("§e" + name, lore, map.get(name));
            content.add(ClickableItem.of(is, e -> {
                if (e.getClick() == ClickType.DROP) {
                    if (builder) {
                        headIdx.get(selected).remove(name);
                        headBase.get(selected).remove(name);
                        cfg.set(selected.name + "." + name, null);
                        cfg.saveConfig();
                        reopen(p, content);
//Ostrov.log_warn("is="+e.getCurrentItem()+" dis="+(TCUtils.toString(e.getCurrentItem().displayName())) );
                        //final String n = TCUtils.stripColor(TCUtils.toString(e.getCurrentItem().displayName()));
                        //net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer plainSerializer = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.se(e.getCurrentItem().displayName());
                        //final String n = ((TextComponent)e.getCurrentItem().displayName()).content();
                        //final Component c = e.getCurrentItem().displayName();
//Ostrov.log_warn(" dis="+(TCUtils.toString(e.getCurrentItem().displayName())) );
                        //p.sendMessage(c);
                        //p.sendMessage("toString="+ TCUtils.toString(c));

                    }
                    return;
                }
                if (e.isLeftClick()) {
                    if (b.getType() != Material.PLAYER_HEAD && b.getType() != Material.PLAYER_WALL_HEAD) {
                        p.sendMessage("§cБлок уже не голова!");
                        return;
                    }
                    p.closeInventory();
                    final Skull skull = ((Skull) b.getState());
                    skull.setPlayerProfile(((SkullMeta) is.getItemMeta()).getPlayerProfile());
                    skull.update();
                }
            }));
        }

        content.fillRow(4, c);
        //content.set(4, selected.ordinal(), select);


        int slot = 0;
        for (HeadCategory hc : HeadCategory.values()) {//(Map.Entry<HeadCategory, TreeMap<String,String>> en : headBase.entrySet()) {
            ItemStack is;
            if (selected == hc) {
                is = new ItemBuilder(Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE)
                        .name(hc.name())
                        .addLore("§7В базе : §3" + headIdx.get(hc).size())
                        .addFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ITEM_SPECIFICS)
                        .build();
                //is.addUnsafeEnchantment(Enchantment.LUCK, 1);
            } else {
                is = icons.get(hc);
            }
            content.set(5, slot, ClickableItem.of(is, e -> {
                if (e.isLeftClick()) {
                    selected = hc;
                    page = 0;
                    reopen(p, content);
                }
            }));
            slot++;
        }


        if (to < idx.size()) {
            content.set(4, 8, ClickableItem.of(ItemUtils.nextPage, e -> {
                        page++;
                        reopen(p, content);
                    })
            );
        }

        if (page > 0) {
            content.set(4, 0, ClickableItem.of(ItemUtils.previosPage, e -> {
                        page--;
                        reopen(p, content);
                    })
            );
        }


    }

    private static ItemStack skull(final String name, final List<Component> lore, final String link) {
//Ostrov.log_warn("skull:"+name+" url="+link);
        final ItemStack is = new ItemStack(Material.PLAYER_HEAD);
        final ItemMeta im = is.getItemMeta();
        im.displayName(Component.text(name));
        if (lore != null) {
            im.lore(lore);
        }
        try {
            final URL url = URI.create(link).toURL();
            SkullMeta sm = (SkullMeta) im;
            //com.destroystokyo.paper.profile.PlayerProfile profile = ItemUtils.getProfile(link);
            //com.destroystokyo.paper.profile.PlayerProfile profile = sm.getPlayerProfile();//ItemUtils.getProfile(link);
            final com.destroystokyo.paper.profile.PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID());
            final PlayerTextures textures = profile.getTextures();
            textures.setSkin(url);
            profile.setTextures(textures);
            //sm.setPlayerProfile(ItemUtils.getProfile(link));
            sm.setPlayerProfile(profile);
//Ostrov.log_warn("skull:"+name+" url="+url.toString()+" profile="+profile+" textures="+textures);
            //playerProfilesCache.put(SHA_or_URL, profile);
        } catch (MalformedURLException | IllegalArgumentException ex) {
            Ostrov.log_warn("Invalid texture SHA_or_URL");
        }
        is.setItemMeta(im);
        return is;
    }


}

enum HeadCategory {
    Мобы("ANIMALS"), //объединить ANIMALS и Monsters
    Еда("FOOD_AND_DRINKS"),
    Символы("ALPHABET"),
    Блоки("BLOCKS"),
    Декорации("DECORATION"),
    Люди("HUMANS"),
    Гуманоиды("HUMANOID"),
    Разное("MISCELLANEOUS"),
    Растения("PLANTS"),
    ;

    public final String name;

    private HeadCategory(final String name) {
        this.name = name;
    }

    public static HeadCategory from(String categoryName) {
        for (HeadCategory hc : HeadCategory.values()) {
            if (hc.name.equalsIgnoreCase(categoryName)) {
                return hc;
            }
        }
        return null;
    }
}

