package ru.komiss77.builder.menu;

import java.util.List;
import net.kyori.adventure.key.Key;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemType;
import ru.komiss77.Ostrov;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.ItemUtil;
import ru.komiss77.utils.inventory.ClickableItem;
import ru.komiss77.utils.inventory.InventoryContent;
import ru.komiss77.utils.inventory.InventoryProvider;


public class Sounds implements InventoryProvider {

    private Sound previos;
    private int page;

    public Sounds(final int page) {
        this.page = page;
    }


    @Override
    public void init(final Player p, final InventoryContent content) {
        int from = page * 44;
        int to = page * 44 + 45;

        content.set(5, 4, ClickableItem.of(new ItemBuilder(Material.OAK_DOOR)
            .name("<gold>Закрыть").build(), e -> p.closeInventory()));

        if (page > 0) {
            content.set(5, 0, ClickableItem.of(ItemUtil.previosPage, e
                    -> {
                    page--;
                    reopen(p, content);
                })
            );
        }

        final List<String> sounds = Ostrov.registries.SOUNDS
            .stream().map(s -> s.key().value()).sorted().toList();
        if (to > sounds.size()) {
            to = sounds.size();
        } else {
            content.set(5, 8, ClickableItem.of(ItemUtil.nextPage, e -> {
                page++; reopen(p, content);
            }));
        }

        for (int i = from; i != to; i++) {
            final String snm = sounds.get(i);
            final Sound sound = Ostrov.registries.SOUNDS.get(Key.key(snm));
            if (sound == null) continue;
            final String tpn;
            final int sp1 = snm.indexOf('.');
            if (sp1 < 0) tpn = snm;
            else {
                final String snm1 = snm.substring(sp1 + 1);
                final int sp2 = snm1.lastIndexOf('.');
                if (sp2 < 0) tpn = snm1;
                else tpn = snm1.substring(0, sp2);
            }

            ItemType tp = null;
            for (final String spl : tpn.split("\\.")) {
                tp = Ostrov.registries.ITEMS.get(Key.key(spl.toLowerCase()));
                if (tp == null) tp = Ostrov.registries.ITEMS.get(Key
                    .key(spl.toLowerCase() + "_spawn_egg"));
                if (tp != null) break;
            }
            if (tp == null) tp = ItemType.FLOW_BANNER_PATTERN;

            content.add(ClickableItem.of(new ItemBuilder(tp)
                .name("§f" + sound)
                .lore("§7")
                .lore("§7ЛКМ - играть")
                .lore("§7Шифт + ЛКМ - играть ускоренно")
                .lore("§7Шифт + ПКМ - играть замедленно")
                .lore("§7Средний клик - название в чат")
                .lore("§7")
                .build(), e -> {
                if (previos != null) {
                    p.stopSound(previos);
                }
                previos = sound;

                switch (e.getClick()) {
                    case LEFT:
                        p.playSound(p.getLocation(), sound, 1f, 1f);
                        break;
                    case SHIFT_LEFT:
                        p.playSound(p.getLocation(), sound, 1f, 2f);
                        break;
                    case SHIFT_RIGHT:
                        p.playSound(p.getLocation(), sound, 1f, 0.5f);
                        break;
                    case MIDDLE:
                        p.sendMessage(sound.key().value() + ", tp-" + tpn);
                        break;
                }
            }));
        }


        //pagination.setItems(menuEntry.toArray(new ClickableItem[menuEntry.size()]));
        //pagination.setItemsPerPage(45);    

        //отказано
        /*content.set(5, 2, ClickableItem.of(new ItemBuilder(Material.NETHERITE_SCRAP)
            .name("§fСлушать по очереди")
            .lore("§7")
            .lore("§7Сейчас №" + current)
            .lore("§7Название: " + Sound.values()[current])
            .lore("§7")
            .lore("§7ЛКМ - следующий")
            .lore("§7ПКМ - предыдущий")
            .lore("§7")
            .build(), e -> {

            switch (e.getClick()) {
                case LEFT:
                    current++;
                    if (current >= Sound.values().length) current = 0;
                    break;
                case RIGHT:
                    current--;
                    if (current <= 0) current = Sound.values().length - 1;
                    break;
                case CONTROL_DROP:
                    break;
                case CREATIVE:
                    break;
                case DOUBLE_CLICK:
                    break;
                case DROP:
                    break;
                case MIDDLE:
                    break;
                case NUMBER_KEY:
                    break;
                case SHIFT_LEFT:
                    break;
                case SHIFT_RIGHT:
                    break;
                case SWAP_OFFHAND:
                    break;
                case UNKNOWN:
                    break;
                case WINDOW_BORDER_LEFT:
                    break;
                case WINDOW_BORDER_RIGHT:
                    break;

            }
            if (previos != null) {
                p.stopSound(previos);
            }
            previos = Sound.values()[current];

            p.playSound(p.getLocation(), Sound.values()[current], 1, 1);
            reopen(p, content);
        }));*/
        

        /*
        if (!pagination.isLast()) {
            contents.set(5, 8, ClickableItem.of(ItemUtils.nextPage, e 
                    -> {
                contents.getHost().open(p, pagination.next().getPage()) ;
                current = pagination.getPage() * 45;
            }
            ));
        }

        if (!pagination.isFirst()) {
            contents.set(5, 0, ClickableItem.of(ItemUtils.previosPage, e 
                    -> {
                contents.getHost().open(p, pagination.previous().getPage()) ;
                current = pagination.getPage() * 45;
               })
            );
        }*/

        //pagination.addToIterator(contents.newIterator(SlotIterator.Type.HORIZONTAL, SlotPos.of(0, 0)).allowOverride(false));


        //onClose(p, content);


    }


}
