package ru.komiss77.modules.menuItem;

import java.util.function.Consumer;
import com.destroystokyo.paper.ClientOption;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import ru.komiss77.modules.translate.Lang;
import ru.komiss77.utils.ItemUtil;
import ru.komiss77.utils.TCUtil;


public class MenuItem {

    private final ItemStack itemRu;
    private final ItemStack itemEn;
    public final int id;
    public final String name;
    public int slot;
    public boolean forced, give_on_join, give_on_respavn, give_on_world_change;
    public boolean duplicate; //выдавать, если уже есть
    public boolean can_move, can_drop, can_pickup, can_swap_hand;
    public boolean can_interact; //даёт ПКМ например для лука или ракеты
    public Consumer<Player> on_left_click, on_right_click, on_left_sneak_click, on_right_sneak_click;
    protected Consumer<InventoryClickEvent> on_inv_click;
    protected Consumer<PlayerInteractEvent> on_interact;
    protected Consumer<PlayerInteractAtEntityEvent> on_interact_at_entity;

    public MenuItem(final String name, final ItemStack is) {
        this.name = name;
        id = name.hashCode();//ApiOstrov.generateId();

        itemRu = is;//itemRu = new ItemBuilder(is).modelData(id).build();
        ItemMeta im = itemRu.getItemMeta();
        im.getPersistentDataContainer().set(MenuItemsManager.key, PersistentDataType.INTEGER, id);
        itemRu.setItemMeta(im);

        String displayName = is.hasItemMeta() && is.getItemMeta().hasDisplayName() ? TCUtil.deform(is.getItemMeta().displayName()) : "";
        displayName = Lang.t(displayName, Lang.EN);
        //im.displayName(TCUtils.form(displayName));

        itemEn = itemRu.clone();//new ItemBuilder(is).name(displayName).modelData(id).build();
        im = itemEn.getItemMeta();
        im.displayName(TCUtil.form(displayName));
        im.getPersistentDataContainer().set(MenuItemsManager.key, PersistentDataType.INTEGER, id);
        itemEn.setItemMeta(im);

    }

    public ItemStack getItem() {
        return itemRu;
    }

    public ItemType getType() {
        return itemRu.getType().asItemType();
    }

    public Material getMaterial() {
        return itemRu.getType();
    }

    public boolean give(final Player p) {
        if (!duplicate) { //если дубликаты не даём, быстрый чек по инвентарю
            if (forced) {
                final ItemStack there = p.getInventory().getItem(slot);
                if (MenuItemsManager.idFromItemStack(there) == id) return false;
            } else {
                for (final ItemStack is : p.getInventory().getContents()) {
                    if (MenuItemsManager.idFromItemStack(is) == id) return false;
                }
            }
        }
        return ItemUtil.giveItemTo(p, p.getClientOption(ClientOption.LOCALE)
            .equals("ru_ru") ? itemRu : itemEn, slot, forced);
    }

    public boolean give(final Player p, final int cSlot) {
        if (!duplicate) { //если дубликаты не даём, быстрый чек по инвентарю
            if (forced && !can_move) {
                final ItemStack there = p.getInventory().getItem(cSlot);
                if (MenuItemsManager.idFromItemStack(there) == id) return false;
            } else {
                for (final ItemStack is : p.getInventory().getContents()) {
                    if (MenuItemsManager.idFromItemStack(is) == id) return false;
                }
            }
        }
        return ItemUtil.giveItemTo(p, p.getClientOption(ClientOption.LOCALE)
            .equals("ru_ru") ? itemRu : itemEn, cSlot, forced);
    }

    @Deprecated
    public void giveForce(final Player p) {
        give(p);
        /*if (!duplicate) { //чекать тут отдельно, или ItemUtils.giveItemTo делает дубль при force
            final PlayerInventory inv = p.getInventory();
            final ItemStack curr = inv.getItem(slot);
            final MenuItem mi = MenuItemsManager.fromItemStack(curr);
            if (mi != null && mi.id == id) {
                return;
            }
        }
        ItemUtil.giveItemTo(p, p.getClientOption(ClientOption.LOCALE)
            .equals("ru_ru") ? itemRu : itemEn, slot, true);*/
    }

    @Deprecated
    public void giveForce(final Player p, final int cSlot) {
        give(p, cSlot);
        /*ItemUtil.giveItemTo(p, p.getClientOption(ClientOption.LOCALE)
            .equals("ru_ru") ? itemRu : itemEn, cSlot, true);*/
    }

    @Deprecated//ничего не делает...
    public int takeAway(final Player p) {
        int count = 0;
        MenuItem mi;
        final ItemStack[] cts = p.getInventory().getContents();
        for (int i = 0; i < cts.length; i++) {
            mi = MenuItemsManager.fromItemStack(cts[i]);
            if (mi != null && this.id == mi.id) {
                count += cts[i].getAmount();
                cts[i].setAmount(0);
            }
        }
        if (count > 0) p.updateInventory();//cts не обновляет это
        return count;
    }

    public int remove(final Player p) { //Iterator не использовать, java.lang.UnsupportedOperationException: Can't change the size of an inventory!
        int count = 0;
        MenuItem mi;
        //final Iterator<ItemStack> ite = p.getInventory().iterator();
        //while (ite.hasNext()) {
        //  final ItemStack it = ite.next();
        //  mi = MenuItemsManager.fromItemStack(it);
        //  if (mi!=null && this.id == mi.id) {
        //    count+= it.getAmount();
        //    ite.remove(); //java.lang.UnsupportedOperationException: Can't change the size of an inventory!
        //   }
        // }
        final ItemStack[] inv = p.getInventory().getContents();
        for (int i = 0; i < inv.length; i++) {
            mi = MenuItemsManager.fromItemStack(inv[i]);
            if (mi != null && this.id == mi.id) {
                count++;
                p.getInventory().setItem(i, ItemUtil.air);
            }
        }
        return count;
    }

}
