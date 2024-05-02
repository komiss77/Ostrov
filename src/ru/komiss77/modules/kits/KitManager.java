package ru.komiss77.modules.kits;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Config;
import ru.komiss77.Initiable;
import ru.komiss77.modules.player.PM;
import ru.komiss77.objects.CaseInsensitiveMap;
import ru.komiss77.Ostrov;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.ItemUtils;
import ru.komiss77.utils.OstrovConfig;
import ru.komiss77.utils.inventory.SmartInventory;



/*
АДАПТАЦИЯ
                Kit kit = new Kit(kitName);
                        kit.items.add(itemStack);
                kit.enabled = true;
                kit.accesBuyPrice = kitPrice;
                kit.accesSellPrice = kitPrice/2;
                switch (kirRariry) {
                    case COMMON : kit.rarity = KitManager.Rarity.Простой;
                    case RARE : kit.rarity = KitManager.Rarity.Раритетный;
                    case LEGENDARY : kit.rarity = KitManager.Rarity.Легендарный;
                }
                kit.logoItem = new ItemBuilder (kitLogo).lore(list3).build();
                KitManager.kits.put(kitName, kit);
                KitManager.saveKit(Bukkit.getConsoleSender(), kit);
*/


public final class KitManager implements Initiable {
    
    private static OstrovConfig kitsConfig;
    public static CaseInsensitiveMap <Kit> kits;

    public KitManager() {
        kits = new CaseInsensitiveMap<>();
        reload();
    }
     
    
    @Override
    public void postWorld() { //обход модулей после загрузки миров, т.к. не всё можно сделать onEnable
    }
    
    

    @Override
    public void onDisable() {
    }   
    
    @Override
    public void reload() {
        try {
            kits.clear();

            kitsConfig = Config.manager.getNewConfig("kits.yml", new String[]{"", "Ostrov77 kits file", ""});
            
            if (kitsConfig.getConfigurationSection("kits")!=null) {
                
                if (ApiOstrov.getLocalConnection()==null) {
                    Ostrov.log_warn("§eKits §6: без локальной БД информация о наборах игроков сохраняться не будет!");
                }

                for (String kitName : kitsConfig.getConfigurationSection("kits").getKeys(false) ) {

                    final List<ItemStack>items = new ArrayList<>();
                    try {
                        for (String itemAsString :  kitsConfig.getStringList("kits."+kitName+".items") ) {
                            items.add( ItemUtils.parseItem(itemAsString, "<>"));
                            if (items.size()==28) {
                                Ostrov.log_warn("Kits : загрузка набора "+kitName+" : превышел лимит предметов, обрезаем до 27.");
                                break;
                            }
                        }

                        final Kit kit = new Kit (kitName);

                        kit.rarity = Rarity.fromString( kitsConfig.getString("kits."+kitName+".rarity", Rarity.Простой.toString()) );
                        kit.extraData1 = kitsConfig.getString("kits."+kitName+".extraData1", "");
                        kit.extraData2 = kitsConfig.getString("kits."+kitName+".extraData2", "");
                        kit.extraData3 = kitsConfig.getString("kits."+kitName+".extraData3", "");
                        kit.extraList1.addAll( kitsConfig.getStringList("kits."+kitName+".extraList1" ) );
                        kit.enabled = kitsConfig.getBoolean("kits."+kitName+".enabled", false);
                        kit.needPermission = kitsConfig.getBoolean("kits."+kitName+".needPermission", false);
                        kit.accesBuyPrice = kitsConfig.getInt("kits."+kitName+".accesBuyPrice", 0);
                        kit.accesSellPrice = kitsConfig.getInt("kits."+kitName+".accesSellPrice", 0);
                        kit.getPrice = kitsConfig.getInt("kits."+kitName+".getPrice", 0);
                        kit.delaySec = kitsConfig.getInt("kits."+kitName+".delayMin", 0) * 60;
                        kit.logoItem = new ItemBuilder( ItemUtils.parseItem(kitsConfig.getString("kits."+kitName+".logoItem", ""), "<>"))
                                .name("§e§n§l"+kitName)
                                .build();
                        kit.items.addAll(items);

                        KitManager.kits.put(kitName, kit);
                        items.clear();

                    } catch (Exception ex) { 
                        Ostrov.log_err("§4Не удалось загрузить набор "+kitName+" : "+ex.getMessage());
                    }
                }

                Ostrov.log_ok("§2Kits §7: Загружены наборы : "+kits.keySet().size());
            }
            
        } catch (Exception ex) { 
            Ostrov.log_err("§4Не удалось инициализировать KitManager : "+ex.getMessage());
        }


            
    //System.out.println("use:"+use+" kits:"+kits);

    }





    

    
    


    public static boolean buyKitAcces ( final Player player, final String kitName ) {
        
        if (player==null || !player.isOnline()) {
            //player.sendMessage("§4игрока нет на сервере!");
            return false;
        }
        
        final Kit kit = kits.get(kitName);
        
        if (kit == null) {
            player.sendMessage("§4Нет такого набора! Возможные: §e"+getKitsNames() );
            return false;
        }

        if (!kit.enabled) {
            player.sendMessage("§4Набор не доступен на этом сервере!" );
            return false;
        }

        if ( kit.needPermission && !player.hasPermission("ostrov.kit."+kitName) && !player.hasPermission("ostrov.kit.*") ) {
            player.sendMessage("§4Для доступа к набору нужно иметь право ostrov.kit."+kitName );
            return false;
        }

        if ( kit.accesBuyPrice==0 ) {
            player.sendMessage("§2Набор не требует покупки права доступа!" );
            return false;
        }

        final Oplayer op = PM.getOplayer(player);
        if (op.hasKitAcces(kitName ) ) {
            player.sendMessage("§2Вы уже имеете доступ к набору "+kitName+" !" );
            return false;
        }


        if ( ApiOstrov.moneyGetBalance(player.getName()) < kit.accesBuyPrice ) {
            player.sendMessage("§4У Вас недостаточно денег для покупки права доступа - стоимость: "+kit.accesBuyPrice );
            return false;
        } 

        ApiOstrov.moneyChange(player, -kit.accesBuyPrice, "доступ к набору "+kitName );
        op.addKitAcces(kitName);
        player.sendMessage("§aКупено право доступа к набору "+kitName+" . Теперь можно получать его!" );
        return true;
    }



    public static boolean trySellAcces ( final Player player, final String kitName ) {
        if (player==null || !player.isOnline()) {
            //player.sendMessage("§4игрока нет на сервере!");
            return false;
        }
        
        final Kit kit = kits.get(kitName);
        
        if (kit == null) {
            player.sendMessage("§4Нет такого набора! Возможные: §e"+getKitsNames() );
            return false;
        }

        if (!kit.enabled) {
            player.sendMessage("§4Набор не доступен на этом сервере!" );
            return false;
        }

        if ( kit.accesBuyPrice==0 ) {
            player.sendMessage("§4Этот набор не требовал покупки права доступа!" );
            return false;
        }

        if ( kit.needPermission && !player.hasPermission("ostrov.kit."+kitName) && !player.hasPermission("ostrov.kit.*") ) {
            player.sendMessage("§4У вас нет права доступа к набору "+kitName );
            return false;
        }

        final Oplayer op = PM.getOplayer(player);
        if ( !op.hasKitAcces(kitName ) ) {
            player.sendMessage("§4У вас нет купленного права доступа к набору "+kitName+" !" );
            return false;
        }
        
        op.revokeKitAcces(kitName);
        ApiOstrov.moneyChange(player, kit.accesSellPrice, "Продажа доступа к набору "+kitName);
        player.sendMessage("§aпродано право доступа к набору "+kitName+". Получать его больше нельзя!" );
        return true;
    }

    
    

    public static boolean tryGiveKit ( final Player player, final String kitName ) {

        if (player==null || !player.isOnline()) {
            //player.sendMessage("§4игрока нет на сервере!");
            return false;
        }
        
        final Kit kit = kits.get(kitName);
        
        if (kit==null) {
            player.sendMessage("§4Нет такого набора! Возможные: §e"+getKitsNames() );
            return false;
        }

        if (!kit.enabled) {
            player.sendMessage("§4Набор не доступен на этом сервере!" );
            return false;
        }

        if ( kit.needPermission && !player.hasPermission("ostrov.kit."+kitName) && !player.hasPermission("ostrov.kit.*") ) {
            player.sendMessage("§4Для получения набора нужно иметь право ostrov.kit."+kitName );
            return false;
        }

        final Oplayer op = PM.getOplayer(player);
        if ( kit.accesBuyPrice>0 && !op.hasKitAcces(kitName) ) {
            player.sendMessage("§4Сначала нужно купить право доступа к набору командой §6/kit buyacces "+kitName );
            return false;
        }

    //System.out.println("Kit_delay "+(Kit_delay(kit))); 
    //System.out.println("Curr time "+(System.currentTimeMillis()/1000)); 
    //System.out.println("Kit_last_acces "+PM.Kit_last_acces(p.getName(),kit)); 
        int secondLeft = getSecondLetf(player, kit);
    //System.out.println("left "+left); 

        if ( secondLeft>0){
            player.sendMessage("§4До следующего получения набора нужно подождать "+ApiOstrov.secondToTime(secondLeft));
            return false;
        }

        if (kit.items.size() > freeSpace(player) ) {
            player.sendMessage("§4В инвентаре недостаточно свободного места! Требуется слотов: §e"+kit.items.size() );
            return false;

        }
    //System.out.println("Is_need_get_payment "+Is_need_get_payment(kit)); 
    //System.out.println("bal "+PM.OP_GetBalance(p.getName())); 
    //System.out.println("Kit_give_cost "+Kit_give_cost(kit)); 

        if ( kit.getPrice>0 && (ApiOstrov.moneyGetBalance(player.getName()) < kit.getPrice )) {
            player.sendMessage("§4Hедостаточно денег для получения набора - стоимость: "+kit.getPrice );
            return false;
        } 

        ApiOstrov.moneyChange(player, -kit.getPrice, "выдача набора "+kitName );
        
        giveKit(player, kitName, true);
        return true;

    }

    public static int getSecondLetf(final Player player, final Kit kit) {
//System.out.println("getMinLetf deley="+kit.delayMin+"  lastAccesBelow="+( Timer.Единое_время()/1000 - PM.Kit_last_acces(player.getName(), kit.name) )+
        //" res="+((int) (kit.delayMin*60 - ( Timer.Единое_время()/1000 - PM.Kit_last_acces(player.getName(), kit.name) ))) ); 
        //return kit.delaySec - Math.ceil( ApiOstrov.currentTimeSec() - PM.Kit_last_acces(player.getName(), kit.name)  ) ;
        final Oplayer op = PM.getOplayer(player);
        return kit.delaySec - ( ApiOstrov.currentTimeSec() - op.getKitUseStamp(kit.name) )  ;
    }

    public static void giveKit( final Player p, final String kitName, final boolean equipArmor ) {
        final Kit kit = kits.get(kitName);
        if (kit==null) return;
        
        boolean equiped;
        
        for (ItemStack is: kit.items) {
            equiped = false;
            
            if (equipArmor) {
                if (is.getType().toString().endsWith("_HELMET") && p.getInventory().getHelmet()==null) {
                    p.getInventory().setHelmet(is);
                    equiped=true;
                }
                if (is.getType().toString().endsWith("_CHESTPLATE") && p.getInventory().getChestplate()==null) {
                    p.getInventory().setChestplate(is);
                    equiped=true;
                }
                if (is.getType().toString().endsWith("_LEGGINGS") && p.getInventory().getLeggings()==null) {
                    p.getInventory().setLeggings(is);
                    equiped=true;
                }
                if (is.getType().toString().endsWith("_BOOTS") && p.getInventory().getBoots()==null) {
                    p.getInventory().setBoots(is);
                    equiped=true;
                }
            }
            
            if (!equiped) p.getInventory().addItem(is.clone());
        }
        
        p.updateInventory();;
        final Oplayer op = PM.getOplayer(p);
       op.setKitUseTimestamp(kitName);
        p.sendMessage("§aВсе компонетны набора "+kitName+" добавлены в инвентарь!");
        
    }
 

    // ---------- Основное -------------
    public static Set <String> getKitsNames() {
        return kits.keySet();
    }
    public static boolean kitExist ( String kitName ) {
        return kits.containsKey(kitName);
    }




    public static int freeSpace ( final Player p ) {
        int free=0;
        for ( int i=0; i<p.getInventory().getSize(); i++) {
            if ( p.getInventory().getItem(i) == null) free++;
        }
        return free;
    }
   



    
    
    
    
    
    
    
    
    

    public static void openGuiMain(final Player p) {
        SmartInventory inv = SmartInventory.builder().id("KitGuiMain:"+p.getName()). provider(new KitGuiMain()). size(6, 9). title("§2Наборы"). build();
        inv.open(p);
    }
    
    public static void openKitPrewiev(final Player p, final Kit kit) {
        SmartInventory inv = SmartInventory.builder().id("KitPrewiev:"+kit.name+":"+p.getName()). provider(new KitPrewiev(kit)). size(6, 9). title("§1Просмотр набора §6"+kit.name). build();
        inv.open(p);                    
    }

    public static void openKitEditMain(final Player p) {
        SmartInventory inv = SmartInventory.builder().id("KitEditMain:"+p.getName()). provider(new KitEditMain()). size(6, 9). title("§4Администрирование наборов"). build();
        inv.open(p);
    }

   public static void openKitSettingsEditor(final Player player, final Kit kit) {
        SmartInventory inv = SmartInventory.builder().id("KitSettingsEditor:"+kit.name+":"+player.getName()). provider(new KitSettingsEditor(kit)). size(6, 9). title("§4Настройки набора §6"+kit.name). build();
        inv.open(player);
    }

   public static void openKitKitComponentEditor(final Player player, final Kit kit) {
        SmartInventory inv = SmartInventory.builder().id("KitComponentEditor:"+kit.name+":"+player.getName()). provider(new KitComponentEditor(kit)). size(6, 9). title("§4Компоненты набора §6"+kit.name). build();
        inv.open(player);                    
    }

    
    
    



    public static boolean saveKit(final CommandSender sender, final Kit kit) {
//System.out.println("saveKit");        
        kits.put(kit.name, kit);
        
        final List <String> itemsList = new ArrayList<>();
        for (ItemStack is : kit.items) {
            itemsList.add(ItemUtils.toString(is, "<>"));
        }
//System.out.println("saveKit itemsList="+itemsList);        

        kitsConfig.set("kits."+kit.name+".enabled", kit.enabled);
        kitsConfig.set("kits."+kit.name+".rarity", kit.rarity.toString());
        kitsConfig.set("kits."+kit.name+".extraData1", kit.extraData1);
        kitsConfig.set("kits."+kit.name+".extraData2", kit.extraData2);
        kitsConfig.set("kits."+kit.name+".extraData3", kit.extraData3);
        kitsConfig.set("kits."+kit.name+".extraList1", kit.extraList1);
        kitsConfig.set("kits."+kit.name+".needPermission", kit.needPermission);
        kitsConfig.set("kits."+kit.name+".accesBuyPrice", kit.accesBuyPrice);
        kitsConfig.set("kits."+kit.name+".accesSellPrice", kit.accesSellPrice);
        kitsConfig.set("kits."+kit.name+".getPrice", kit.getPrice);
        kitsConfig.set("kits."+kit.name+".delayMin", kit.delaySec/60);
        kitsConfig.set("kits."+kit.name+".logoItem", ItemUtils.toString(new ItemBuilder(kit.logoItem).name((String) null).build(), "<>"));
        
        kitsConfig.set("kits."+kit.name+".items", itemsList);
        kitsConfig.saveConfig();
        
        kit.modifyed = false;
        
        if (sender!=null) sender.sendMessage("§aНабор §b"+kit.name+" §aсохранён на диск!");
        return true;
    }
    
    public static boolean deleteKit(final Player player, final String kitName) {
        

        kitsConfig.set("kits."+kitName, null);
        kitsConfig.saveConfig();
        kits.remove(kitName);
        

        player.sendMessage("§eНабор §b"+kitName+" §cудалён!");
        return true;

        
    }
    
    
    



    public enum Rarity {
        
        Простой ("§6Простой"),
        Продвинутый ("§eПродвинутый"),
        Крутой ("§2Крутой"),
        Раритетный ("§aРаритетный"),
        Легендарный ("§bЛегендарный"),
        ;
        
        public String displayName;
        
        Rarity (final String displayName) {
            this.displayName = displayName;
        }
        
        public static Rarity fromString (final String asString) {
            for (Rarity r:values()) {
                if (r.toString().equalsIgnoreCase(asString)) return r;
            }
            return Простой;
        }
        
        public static Rarity rotate (final Rarity current) {
            
            switch(current) {
                case Простой: return Продвинутый;
                case Продвинутый: return Крутой;
                case Крутой: return Раритетный;
                case Раритетный: return Легендарный;
                case Легендарный: return Простой;
                    
            }
            return Простой;
        }
        
    }






}
