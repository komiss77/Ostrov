
package ru.komiss77.modules.kits;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import ru.komiss77.modules.kits.KitManager.Rarity;
import ru.komiss77.utils.ItemBuilder;



public class Kit {
    
    public String name;
    public String extraData1="";
    public String extraData2="";
    public String extraData3="";
    public List <String> extraList1 = new ArrayList<>();    
    public Rarity rarity = Rarity.Простой;
    public boolean modifyed = false; //для редактора
    public boolean enabled = false;
    public boolean needPermission = false;  //требует ли пермишен типа ostrov.kit.name
    public int accesBuyPrice = 0;  //цена покупки права
    public int accesSellPrice = 0;  //цена продажи права
    public int getPrice = 0;  //цена получения
    public int delaySec = 0;  //минуты между выдачами
    public ItemStack logoItem;
    public List <ItemStack> items = new ArrayList<>();    

    
    
    
    public Kit(final String name) {
        this.name = name;
        logoItem = new ItemBuilder(Material.BEDROCK).name("§c"+name).addLore("").build();
    }

    
    
    
    public Kit cloneWithNewName(String newKitName) {
        if (newKitName==null || newKitName.isEmpty()) {
            newKitName = name;
            for (int i=1; i<100; i++) {
                if (!KitManager.kits.containsKey(name+i)) {
                    newKitName = name+i;
                    break;
                }
            }
        }
        final Kit kit = new Kit(newKitName);
        kit.modifyed = true;
        kit.enabled = enabled;
        kit.needPermission = needPermission;
        kit.accesBuyPrice = accesBuyPrice;
        kit.accesSellPrice = accesSellPrice;
        kit.getPrice = getPrice;
        kit.delaySec = delaySec;
        kit.logoItem = new ItemBuilder(logoItem).name("§e§n§l"+newKitName).addLore("").build();
        //kit.items = new ArrayList<>();
        kit.items.addAll(items);
        return kit;
    }
    
    



}
