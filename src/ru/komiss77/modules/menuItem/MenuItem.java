package ru.komiss77.modules.menuItem;

import java.util.function.Consumer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.komiss77.utils.ItemUtils;



public class MenuItem {

    private final ItemStack item;
    public final int id;
    public final String name;
    public int slot;
    public boolean forceGive, give_on_join, give_on_respavn, give_on_world_change;
    public boolean duplicate; //выдавать, если уже есть
    public boolean anycase; //если слот занят, предмет из слота будет дропнут и поставлен менюитем
    public boolean can_move, can_drop, can_pickup, can_swap_hand;
    public Consumer<Player> on_left_click, on_right_click, on_left_sneak_click, on_right_sneak_click;
    
    public MenuItem(final String name, final ItemStack is) {
        this.name=name;
        id = name.hashCode();//ApiOstrov.generateId();
        item = ItemUtils.setCusomModelData(is, id);
        //this.item=is;
//System.out.println("================ SpecItem name="+name+" id="+id);
    }
    
    public ItemStack getItem() {
        return item;
    }
    
    public Material getMaterial() {
        return item.getType();
    }
    
    public boolean give(final Player p) {
        if (!duplicate) { //если дубликаты не даём, быстрый чек по инвентарю
            for (final ItemStack is : p.getInventory().getContents()) {
                if (MenuItemsManager.idFromItemStack(is)==id) {
                    return false;
                }
            }
        }
        if (p.getInventory().getItem(slot) == null || p.getInventory().getItem(slot).getType()==Material.AIR) {                                        //если требуемая позиция пустая, 
    //System.out.println("22 "+position+"  "+item);
            p.getInventory().setItem(slot, item);                                            //ставим предмет и возврат
            p.updateInventory();
            return true;
        }
        
        if (anycase) { //если нужно поставить именно в этот слот
            p.getWorld().dropItemNaturally(p.getLocation(), p.getInventory().getItem(slot).clone());   //дропаем занятый слот
            p.getInventory().setItem(slot, item);                                        //в нужный слот ставим предмет
            p.updateInventory();
            //p.sendMessage("§4В Вашем инвентаре не было места, Дух Острова бросил занятый слот рядом!");
            return true;
        } 
        p.getInventory().addItem(item); //просто добавить
        return true;
//System.out.println("================ SpecItem give name="+name);
       // return ItemUtils.Add_to_inv(p, slot, item.clone(), anycase, false); //менюшки нокогда не дублируем!!
    }
    
    public void giveForce(final Player p) {
//System.out.println("================ SpecItem give name="+name);
        p.getInventory().setItem(slot, item);
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
