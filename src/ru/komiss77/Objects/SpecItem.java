package ru.komiss77.Objects;

import java.util.function.Consumer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import ru.komiss77.utils.ItemUtils;
import ru.komiss77.version.VM;


public class SpecItem {

    private final ItemStack item;
    public final String name;
    public int slot;
    public boolean give_on_join,give_on_respavn,duplicate,anycase,give_on_world_change,can_move,can_drop,can_pickup,can_swap_hand;
    //public String left_click_conmmand="",right_click_conmmand="";
    public Consumer<Player> on_left_click,on_right_click, on_left_sneak_click, on_right_sneak_click;
    
    public SpecItem(final String name, final ItemStack is) {
        this.name=name;
        this.item=VM.getNmsNbtUtil().addString(is, "ostrovItem", name);
//System.out.println("================ SpecItem name="+name);
    }
    
    public ItemStack getItem() {
        return item;
    }
    
    @Deprecated
    public boolean give(final Player p) {
//System.out.println("================ SpecItem give name="+name);
        return ItemUtils.Add_to_inv(p, slot, item.clone(), anycase, false); //менюшки нокогда не дублируем!!
    }

    @Deprecated
    public boolean isSpecItem(final ItemStack is) {
//System.out.println("----isSpecItem type==?"+(is.getType()==item.getType())+" hasString?"+VM.getNmsNbtUtil().hasString(is, "ostrovItem")+" tag="+VM.getNmsNbtUtil().getString(is, "ostrovItem")+" name="+name);
        return is!=null && is.getType()==item.getType() && VM.getNmsNbtUtil().hasString(is, "ostrovItem") && VM.getNmsNbtUtil().getString(is, "ostrovItem").equalsIgnoreCase(name);
    }
    
}
