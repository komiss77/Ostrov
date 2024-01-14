package ru.komiss77.modules.menuItem;

import com.destroystokyo.paper.ClientOption;
import java.util.function.Consumer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ru.komiss77.modules.translate.EnumLang;
import ru.komiss77.modules.translate.Lang;
import ru.komiss77.utils.ItemUtils;
import ru.komiss77.utils.TCUtils;



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
        final boolean eng = !p.getClientOption(ClientOption.LOCALE).equals("ru_ru");
        
        if (p.getInventory().getItem(slot) == null || p.getInventory().getItem(slot).getType()==Material.AIR) {                                        //если требуемая позиция пустая, 
    //System.out.println("22 "+position+"  "+item);
            p.getInventory().setItem(slot, eng ? itemEn : itemRu);                                            //ставим предмет и возврат
            p.updateInventory();
            return true;
        }
        
        if (anycase) { //если нужно поставить именно в этот слот
            p.getWorld().dropItemNaturally(p.getLocation(), p.getInventory().getItem(slot).clone());   //дропаем занятый слот
            p.getInventory().setItem(slot, eng ? itemEn : itemRu);                                        //в нужный слот ставим предмет
            p.updateInventory();
            //p.sendMessage("§4В Вашем инвентаре не было места, Дух Острова бросил занятый слот рядом!");
            return true;
        } 
        p.getInventory().addItem(eng ? itemEn : itemRu); //просто добавить
        return true;
//System.out.println("================ SpecItem give name="+name);
       // return ItemUtils.Add_to_inv(p, slot, item.clone(), anycase, false); //менюшки нокогда не дублируем!!
    }
    
    public void giveForce(final Player p) {
//System.out.println("================ SpecItem give name="+name);
        final boolean eng = !p.getClientOption(ClientOption.LOCALE).equals("ru_ru");
        p.getInventory().setItem(slot, eng ? itemEn : itemRu);
    }

    public void giveForce(final Player p, final int customSlot) {
//System.out.println("================ SpecItem give name="+name);
        final boolean eng = !p.getClientOption(ClientOption.LOCALE).equals("ru_ru");
        p.getInventory().setItem(customSlot, eng ? itemEn : itemRu);
    }

    public int takeAway(final Player p) {
        int count = 0;
        MenuItem mi;
        for (int i=0; i< p.getInventory().getContents().length; i++) {
            mi  = MenuItemsManager.fromItemStack(p.getInventory().getContents()[i]);
            if (mi!=null && this.id == mi.id) {
                count+=p.getInventory().getContents()[i].getAmount();
                p.getInventory().getContents()[i].setAmount(0);
            }
        }
        if (count>0) p.updateInventory();
//System.out.println("----isSpecItem type==?"+(is.getType()==item.getType())+" hasString?"+VM.getNmsNbtUtil().hasString(is, "ostrovItem")+" tag="+VM.getNmsNbtUtil().getString(is, "ostrovItem")+" name="+name);
        return count;
    }
    
}
