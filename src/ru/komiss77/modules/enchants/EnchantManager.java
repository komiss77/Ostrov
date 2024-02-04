package ru.komiss77.modules.enchants;

import ru.komiss77.Config;
import ru.komiss77.Initiable;
import ru.komiss77.Ostrov;


public class EnchantManager implements Initiable {

    public EnchantManager() {
    	if (!Config.enchants) {
    		Ostrov.log_ok("§6Зачарования выключены!");
    		return;
    	}
        //Bukkit.getPluginManager().registerEvents(new EnchantListener(), Ostrov.instance);
        reload();
    }
    
    @Override
    public void postWorld() { //обход модулей после загрузки миров, т.к. не всё можно сделать onEnable
    }
    
        
    @Override
    public void reload() {
    	if (!Config.enchants) {
    		Ostrov.log_ok("§6Зачарования выключены!");
    		return;
    	}
    	
        /*try {
            final Field f = Enchantment.class.getDeclaredField("acceptingNew");
            f.setAccessible(true);
            f.set(null, true);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }*/
        CustomEnchant.values();
    }

    @Override
    public void onDisable() {
    }

}
