package ru.komiss77.modules.menuItem;

import com.destroystokyo.paper.ClientOption;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ru.komiss77.modules.translate.Lang;
import ru.komiss77.utils.ItemUtils;
import ru.komiss77.utils.TCUtils;

import java.util.Iterator;
import java.util.function.Consumer;



public class MenuItem {

    private final ItemStack itemRu;
    private final ItemStack itemEn;
    public final int id;
    public final String name;
    public int slot;
    public boolean forceGive, give_on_join, give_on_respavn, give_on_world_change;
    public boolean duplicate; //выдавать, если уже есть
    public boolean anycase; //если слот занят, предмет из слота будет дропнут и поставлен менюитем
    public boolean can_move, can_drop, can_pickup, can_swap_hand;
    public boolean can_interact; //даёт ПКМ например для лука или ракеты
    public Consumer<Player> on_left_click, on_right_click, on_left_sneak_click, on_right_sneak_click;
    protected Consumer<InventoryClickEvent> on_inv_click;
    protected Consumer<PlayerInteractEvent> on_interact;
    
    public MenuItem(final String name, final ItemStack is) {
        this.name=name;
        id = name.hashCode();//ApiOstrov.generateId();
        itemRu = ItemUtils.setCusomModelData(is, id);
        itemEn = is.clone();
        
        final ItemMeta im = itemEn.getItemMeta();
        im.setCustomModelData(id);
        String displayName = im.hasDisplayName() ? TCUtils.toString(im.displayName()) : "";
        displayName = Lang.t( displayName, Lang.EN);
        im.displayName(TCUtils.format(displayName));
        
        itemEn.setItemMeta(im);
    }
    
    public ItemStack getItem() {
        return itemRu;
    }
    
    public Material getMaterial() {
        return itemRu.getType();
    }
    
    public boolean give(final Player p) {
        if (!duplicate) { //если дубликаты не даём, быстрый чек по инвентарю
            for (final ItemStack is : p.getInventory().getContents()) {
                if (MenuItemsManager.idFromItemStack(is)==id) {
                    return false;
                }
            }
        }
        final boolean ru = p.getClientOption(ClientOption.LOCALE).equals("ru_ru");
        ItemUtils.giveItemTo(p, ru ? itemRu : itemEn, slot, false);
        return true;
//System.out.println("================ SpecItem give name="+name);
       // return ItemUtils.Add_to_inv(p, slot, item.clone(), anycase, false); //менюшки нокогда не дублируем!!
    }
    
    public void giveForce(final Player p) {
      ItemUtils.giveItemTo(p, p.getClientOption(ClientOption.LOCALE)
        .equals("ru_ru") ? itemRu : itemEn, slot, true);
    }

    public void giveForce(final Player p, final int customSlot) {
      ItemUtils.giveItemTo(p, p.getClientOption(ClientOption.LOCALE)
        .equals("ru_ru") ? itemRu : itemEn, customSlot, true);
    }

    @Deprecated//ничего не делает...
    public int takeAway(final Player p) {
        int count = 0;
        MenuItem mi;
      final ItemStack[] cts = p.getInventory().getContents();
      for (int i = 0; i< cts.length; i++) {
            mi = MenuItemsManager.fromItemStack(cts[i]);
            if (mi!=null && this.id == mi.id) {
                count+= cts[i].getAmount();
                cts[i].setAmount(0);
            }
        }
        if (count>0) p.updateInventory();//cts не обновляет это
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
        if (mi!=null && this.id == mi.id) {
          count++;
          p.getInventory().setItem(i, ItemUtils.air);
        }
      }
      return count;
    }
    
}
