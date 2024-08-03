package ru.komiss77.builder.menu;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.ItemUtil;
import ru.komiss77.utils.inventory.ClickableItem;
import ru.komiss77.utils.inventory.InventoryContent;
import ru.komiss77.utils.inventory.InventoryProvider;


public class Sounds implements InventoryProvider {


    private Sound previos;
    private int current;
    private int page;

    public Sounds(final int page) {
        this.page = page;
    }


    @Override
    public void init(final Player p, final InventoryContent content) {

        Material mat;
        String[] split;
        String find;

        int from = page * 44;
        int to = page * 44 + 45;
        if (to >= Sound.values().length) to = Sound.values().length;

        for (int i = from; i < to; i++) {

            final Sound sound = Sound.values()[i];


            mat = Material.FLOWER_BANNER_PATTERN;

            split = sound.toString().split("_");

            //if (split[0].equals("BLOCK_")) {
            if (split.length >= 5) { //BLOCK_ LILY_PAD _PLACE
                //switch (split[4]) {
                // case "CLICK":
                //   find = split[1].length()<=3 ? split[1]+"_" : split[1];
                //    break;
                // default:
                find = split[1] + "_" + split[2] + "_" + split[3];
                // }
            } else if (split.length >= 4) { //BLOCK_ LILY_PAD _PLACE
                switch (split[3]) {
                    case "ON":
                    case "OFF":
                    case "BREACK":
                    case "USE":
                    case "SUCCES":
                        find = split[1].length() <= 3 ? split[1] + "_" : split[1];
                        break;
                    default:
                        find = split[1] + "_" + split[2];
                }
            } else {//if (split.length>=3) { //BLOCK_ LEVER _CLICK
                find = split[1].length() <= 3 ? split[1] + "_" : split[1];
            }

            for (Material m : Material.values()) {
                if (!m.isItem()) continue;
                if (String.valueOf(m).contains(find)) {
                    mat = m;
                    break;
                }
            }
            //}

            content.add(ClickableItem.of(new ItemBuilder(mat)
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
                current = sound.ordinal();

                switch (e.getClick()) {
                    case LEFT:
                        p.playSound(p.getLocation(), sound, 1, 1);
                        break;
                    case SHIFT_LEFT:
                        p.playSound(p.getLocation(), sound, 1, 2);
                        break;
                    case SHIFT_RIGHT:
                        p.playSound(p.getLocation(), sound, 1, 0.5f);
                        break;
                    case MIDDLE:
                        p.performCommand(String.valueOf(sound));
                        break;
                    case CONTROL_DROP:
                        break;
                    case CREATIVE:
                        break;
                    case DOUBLE_CLICK:
                        break;
                    case DROP:
                        break;
                    case NUMBER_KEY:
                        break;
                    case RIGHT:
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
            }));

        }


        //pagination.setItems(menuEntry.toArray(new ClickableItem[menuEntry.size()]));
        //pagination.setItemsPerPage(45);    


        content.set(5, 2, ClickableItem.of(new ItemBuilder(Material.NETHERITE_SCRAP)
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
        }));


        content.set(5, 4, ClickableItem.of(new ItemBuilder(Material.OAK_DOOR).name("закрыть").build(), e ->
                p.closeInventory()
            //SmartInventory.builder().id("EntityByGroup"+p.getName()). provider(new EntityByGroup(world, radius, VM.getNmsEntitygroup().getEntytyGroup(type))). size(6, 9). title("§2"+world.getName()+" "+VM.getNmsEntitygroup().getEntytyGroup(type).displayName+" §1r="+radius).build() .open(p)
        ));
        

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

        if (to < Sound.values().length) {
            content.set(5, 8, ClickableItem.of(ItemUtil.nextPage, e
                    -> {
                    page++;
                    reopen(p, content);
                })
            );
        }

        if (page > 0) {
            content.set(5, 0, ClickableItem.of(ItemUtil.previosPage, e
                    -> {
                    page--;
                    reopen(p, content);
                })
            );
        }


        //onClose(p, content);


    }


}
