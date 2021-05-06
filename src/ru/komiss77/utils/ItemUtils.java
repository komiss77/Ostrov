package ru.komiss77.utils;

import com.meowj.langutils.lang.LanguageHelper;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.WeatherType;
import org.bukkit.block.Biome;
import org.bukkit.block.Sign;
import org.bukkit.craftbukkit.libs.org.apache.commons.codec.binary.Base64;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Commands.CMD;
import ru.komiss77.Commands.Pvp;
import ru.komiss77.Managers.PM;
import ru.komiss77.Ostrov;
import ru.komiss77.ProfileMenu.E_Prof;
import ru.komiss77.ProfileMenu.mainHandler;
import ru.komiss77.version.VM;



public class ItemUtils {


    private static HashMap <String, GameProfile> gameProfiles;
    public static ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
    public static Inventory profile_master;

    public static ItemStack panel, on, off, disable, variable, noperm, sun, rain, no_homes, no_regions, no_kits, no_warps, too_many, kit_no_acces;   
    public static ItemStack profile_empty, friend_empty, party_empty, profile_deny, previos_page, next_page, nextPage, previosPage;

    public static String profile_master_inv_name;
    
    public static void LoadItem () {
        
        gameProfiles = new HashMap<>();
    
        panel = new ItemStack(Material.GRAY_STAINED_GLASS_PANE, 1);
        Set_name( panel, ".");
       
        on = new ItemStack(Material.GREEN_CONCRETE, 1);
        Set_name( on, "§2Включено");
       
        off = new ItemStack(Material.RED_CONCRETE, 1);
        Set_name( off, "§4Выключено");
    
        disable = new ItemStack(Material.WHITE_CONCRETE, 1);
        Set_name( disable, "§7На этом сервере недоступно");
    
        variable = new ItemStack(Material.COMPARATOR, 1);
        Set_name( variable, "§6Переменная");
    
        noperm = new ItemStack(Material.YELLOW_CONCRETE, 1);
        Set_name( noperm, "§5Нет привилегии");
    
        sun = new ItemStack(Material.SUNFLOWER, 1);
        Set_name( sun, "§5Солнышко");
    
        rain = new ItemStack(Material.HOPPER, 1);
        Set_name( rain, "§5Дождик");
    
        no_homes = new ItemStack(Material.RED_BED, 1);
        Set_name( no_homes, "§4Нет точек дома");
        Set_lore( no_homes, "§bЛевый клик -"," §eустановить дом здесь", "" , "" );
    
        no_regions = new ItemStack(Material.FURNACE, 1);
        Set_name( no_regions, "§4Нет приватов");
        Set_lore( no_regions, "§bЛевый клик -"," §eсоздать приват", "" , "" );
    
        too_many = new ItemStack(Material.REDSTONE_TORCH, 1);
        Set_name( too_many, "§fОбъектов больше 17");
        Set_lore( too_many, "§eЛевый клик -","§eполучить полный список." , "", "" );
    
        kit_no_acces = new ItemStack(Material.RED_STAINED_GLASS_PANE, 1);
        
        profile_empty=new ItemBuilder(Material.GLASS_BOTTLE).setName("§7Ой!").setLore(Gen_lore(null, "Раздел в разрабоке!", "§e")).build();
        friend_empty=new ItemBuilder(Material.GLASS_BOTTLE).setName("§7Ой!").setLore(Gen_lore(null, "У Вас пока нет друзей!<br>Чтобы добавить друга/подругу,<br>наберите команду §b/fr add ник<br>или используйте голову.", "§e")).build();
        party_empty=new ItemBuilder(Material.GLASS_BOTTLE).setName("§7Ой!").setLore(Gen_lore(null, "У Вас пока нет команды!<br>Чтобы создать команду,<br>наберите §b/party create<br>или используйте флаг.", "§e")).build();
        profile_deny=new ItemBuilder(Material.BARRIER).setName("§cнедоступно").setLore(Gen_lore(null, "На данном сервере<br>этот раздел недоступен!", "§c")).build();
        
        previos_page=VM.getNmsServer().getCustomHead("f2599bd986659b8ce2c4988525c94e19ddd39fad08a38284a197f1b70675acc", "§fназад", "");
        next_page=VM.getNmsServer().getCustomHead("c2f910c47da042e4aa28af6cc81cf48ac6caf37dab35f88db993accb9dfe516", "§fвперёд", "");
        nextPage = VM.getNmsServer().getCustomHead("6d865aae2746a9b8e9a4fe629fb08d18d0a9251e5ccbe5fa7051f53eab9b94", "§fдалее", "");
        previosPage = VM.getNmsServer().getCustomHead("a2f0425d64fdc8992928d608109810c1251fe243d60d175bed427c651cbe", "§fназад", "");  
        
        
        
        
        
        
        
        
        
        
        profile_master_inv_name = "§l§oПрофиль";
        profile_master=Bukkit.createInventory( null, 54,  profile_master_inv_name );
        mainHandler.setcolorGlassLine(profile_master, Material.LIGHT_GRAY_STAINED_GLASS_PANE);
        for (E_Prof pr_:E_Prof.values()) {
            profile_master.setItem(pr_.slot, E_Prof.getItem(pr_));
        }
    }






    /**
     * 
     * @param current_lore  текущее lore. null - создать новое
     * @param text  текст. (br в > скобках)- перенос строки. <br>пробел или | -возможный перенос
     * @param text_color null или осносной цвет текста
     * @return 
     */
    @SuppressWarnings("unchecked")
    public static List<String> Gen_lore(List<String> current_lore, String text, String text_color){
        if (text_color==null) text_color="";
        text = text.replaceAll("&","§");
        if (current_lore==null) current_lore=new ArrayList<>();
        
        String[] блоки= {text};
        if (text.contains("<br>")) блоки=text.split("<br>");
        List<String> нарезка_построчно;
        //else блоки = {text};
        for(String блок: блоки){
            нарезка_построчно = split(блок, 25);
                for (String строчка : нарезка_построчно) {
                    current_lore.add(text_color+строчка);
                }
        }
            return current_lore;
			
    }
    
    
    public static List<String> split(String блок, int line_lenght) {
        //int text_lenght = блок.length()- (блок.length()-блок.replaceAll("§", "").length())*2;  //не учитываем цветовые коды в 
//System.out.println("line_lenght="+line_lenght+"to_split_lenght="+блок.length()+"  text_lenght="+text_lenght+" to_split="+блок);
        List<String> split=new ArrayList<>();
        if (блок.length() <= line_lenght) {
            split.add(блок);
            return split;
        }
        //String[] split = new String [text_lenght / line_lenght + 1];
        //Arrays.fill(split, "");
//System.out.println("  split=new String["+(int)(text_lenght / line_lenght + 1)+"]");        
        boolean nextLine = false;
        //int index = 0;
        int current_line_lenght=line_lenght;
        
        StringBuilder sb = new StringBuilder();
        char[] блок_array = блок.toCharArray();
        
        for (int position = 0; position < блок_array.length; position++) {
//System.out.println("111 index="+index+"  position="+position+" char="+блок_array[position] );        
            
            if (блок_array[position]=='§') {
//System.out.println("skip § 111 position="+position );        
                sb.append(блок_array[position]);
                //position++;
                current_line_lenght++;
                if (position < блок_array.length) {
                    position++;
                    sb.append(блок_array[position]);
                    current_line_lenght++;
                }
//System.out.println("skip § 222 position="+position );       
            } else {
//System.out.println("222 index="+index+"  position="+position );        
                if (position != 0 && position % current_line_lenght == 0) {
//System.out.println("nextLine 111 position="+position+"  current_line_lenght="+current_line_lenght );        
                    nextLine = true;
                }
                if(nextLine && (блок_array[position] == ' ' || блок_array[position] == '|' || блок_array[position] == ','  || блок_array[position] == '.')) {
                    nextLine = false;
                    split.add(sb.toString());
                    //index++;
                    sb = new StringBuilder();
                    current_line_lenght=line_lenght;
//System.out.println("nextLine 222 index="+index+" position="+position+"  current_line_lenght="+current_line_lenght );        
                } else sb.append(блок_array[position]);
            }
        }
        split.add(sb.toString()); //добавляем, что осталось
        

        return split;
    }
	




    
   
    
    
    
    

    public static boolean Add_to_inv (Player p, int position, ItemStack item, boolean anycase, boolean duplicate) {

        PlayerInventory inv = p.getInventory();

//System.out.println("Выдаём "+item.getType()+" contains:"+inv.contains(item)+" duplicate:"+duplicate+"  >>> "+(inv.contains(item) && !duplicate));   

        if (inv.contains(item) && !duplicate) return true;                         //если есть и не дублировать, возврат
    //System.out.println("11111111111111");
        boolean found = false;
        int empty_pos;

        if (inv.getItem(position) == null) {                                        //если требуемая позиция пустая, 
    //System.out.println("22 "+position+"  "+item);
            inv.setItem(position, item);                                            //ставим предмет и возврат
            p.updateInventory();
            return true;
        } else {                                                                    //если не пустая, поиск свободного слота
            for (empty_pos = 0; empty_pos < 36; ++empty_pos) {          
                if (inv.getItem(empty_pos) == null) {
                    found = true;
                    break;
                }
            }
        }
    //System.out.println("2222");
        //if (!found && !anycase) return false;                                       //если не найден и не принудительно, отказ
        //ItemStack current = inv.getItem(position);                                  //берём предмет с требуемой позиции
        if (found) {                                                                //если место было найдено,
    //System.out.println("444 "+position+"  "+item);
            //inv.setItem(empty_pos, inv.getItem(position));                                        //переносим 
            inv.setItem(empty_pos, item);                                            //в нужный слот ставим предмет
            p.updateInventory();
            return true;                                                            //дело сделано
        } else {                                                                    //если пустое место не найдено
            if (anycase) {
                p.getWorld().dropItemNaturally(p.getLocation(), inv.getItem(position).clone());   //дропаем занятый слот
                inv.setItem(position, item);                                        //в нужный слот ставим предмет
                p.updateInventory();
                p.sendMessage("§4В Вашем инвентаре не было места, Дух Острова бросил занятый слот рядом!");
                return true;
            } else {
                p.getWorld().dropItemNaturally(p.getLocation(), item.clone());      //кидаем предмет рядом
                p.sendMessage("§4В Вашем инвентаре не было места, Дух Острова бросил выдаваемый предмет рядом!");
                return false;                                                       //если не принудительно, отказ
            }

        }
    }    


    @SuppressWarnings("deprecation")
    public static boolean get_Items (Player player, int count, Material mat) {
        Map<Integer, ? extends ItemStack> ammo = player.getInventory().all(mat);

        int found = 0;
        for (ItemStack stack : ammo.values()) {
            found += stack.getAmount();
        }
        if (count > found) return false;

        for (int index : ammo.keySet()) {
            ItemStack stack = ammo.get(index);
            int removed = Math.min(count, stack.getAmount());
            count -= removed;
            if (stack.getAmount() == removed)  player.getInventory().setItem(index, null);
            else stack.setAmount(stack.getAmount() - removed);
            if (count <= 0)  break;
        }

        player.updateInventory();
        return true;
    } 

    
    
    public static void substractItemInHand(final Player p, final EquipmentSlot hand){
        if (hand==EquipmentSlot.HAND) {
            if (p.getInventory().getItemInMainHand().getAmount() == 1){
                p.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
            } else {
                p.getInventory().getItemInMainHand().setAmount(p.getInventory().getItemInMainHand().getAmount() - 1);
            }
        } else if (hand==EquipmentSlot.OFF_HAND) {
            if (p.getInventory().getItemInOffHand().getAmount() == 1){
                p.getInventory().setItemInOffHand(new ItemStack(Material.AIR));
            } else {
                p.getInventory().getItemInOffHand().setAmount(p.getInventory().getItemInOffHand().getAmount() - 1);
            }
        }
    }


    
    public static boolean substractOneItem(final HumanEntity he, final Material mat) {
        if ( !he.getInventory().contains(mat)) {
            return false;
        }
        for (int i=0; i< he.getInventory().getContents().length; i++) {
            if (he.getInventory().getContents()[i]!=null && he.getInventory().getContents()[i].getType()==mat) {
                if (he.getInventory().getContents()[i].getAmount()>=2) {
                    he.getInventory().getContents()[i].setAmount(he.getInventory().getContents()[i].getAmount()-1);
                } else {
                    he.getInventory().getContents()[i].setAmount(0);
                    //he.getInventory().getContents()[i]=null;
                }
                return true;
            }
        }
        return false;
    }
    
    public static boolean substractAllItems(final HumanEntity he, final Material mat) {
        if ( !he.getInventory().contains(mat)) {
            return false;
        }
        boolean result=false;
        for (int i=0; i< he.getInventory().getContents().length; i++) {
            if (he.getInventory().getContents()[i]!=null && he.getInventory().getContents()[i].getType()==mat) {
                    he.getInventory().getContents()[i].setAmount(0);
                    result = true;
                }
            }
        return result;
    }

    public static boolean substractItem(final Player he, final Material mat, int ammount) {
        if (getItemCount(he, mat)<ammount) return false;
        final ItemStack[] cloneInv = new ItemStack[he.getInventory().getContents().length];// = playerInvClone.getContents();
        ItemStack toClone;
        for (int slot = 0; slot<he.getInventory().getContents().length ; slot++) {
            toClone = he.getInventory().getContents()[slot];
            cloneInv[slot] = toClone == null ? null : toClone.clone();
        }        
        for (int slot = 0; slot<cloneInv.length ; slot++) {
            if (cloneInv[slot] != null && mat==cloneInv[slot].getType()) {
                if (cloneInv[slot].getAmount()==ammount) { //найдено и убрано - дальше не ищем
                    cloneInv[slot].setType(Material.AIR);
                    ammount = 0;
                    //itemFindResult.remove(mat);
                    break; 
                } else if (cloneInv[slot].getAmount()>ammount) { //найдено больше чем надо - дальше не ищем
                    cloneInv[slot].setAmount(cloneInv[slot].getAmount()-ammount);
                    ammount = 0;
                    //itemFindResult.remove(mat);
                    break;
                } else if (cloneInv[slot].getAmount()<ammount) { //найдено меньше чем надо - убавили требуемое и ищем дальше
                    ammount-=cloneInv[slot].getAmount();
                    //itemFindResult.put(mat, ammount);
                    cloneInv[slot].setType(Material.AIR);
                }
            }
        }
        if (ammount == 0) {//if (itemFindResult.isEmpty()) {
            he.getInventory().setContents(cloneInv);
            he.updateInventory();
            return true;
        }
        return false;
    }

    public static int getItemCount(final HumanEntity he, final Material mat) {
        int result = 0;
        for (final ItemStack slot : he.getInventory().getContents() ) {
            if (slot!=null && slot.getType()==mat) {
                result+=slot.getAmount();
            }
        }
        return result;
    }



    @Deprecated
    public static ItemStack Create_with_name (ItemStack is, String name) {
        ItemMeta m = is.getItemMeta();
        m.setDisplayName(name);
        is.setItemMeta( m );
        return is;
        }

    public static ItemStack Set_name (ItemStack is, final String name) {
        ItemMeta m = is.getItemMeta();
        m.setDisplayName(name);
        is.setItemMeta( m );
        return is;
    }

    public static ItemStack Set_lore (ItemStack is, String lore1, String lore2, String lore3, String lore4 ) {
        ItemMeta m = is.getItemMeta();
        m.setLore(Arrays.asList( lore1, lore2, lore3, lore4 ) );
        is.setItemMeta( m );
        return is;
    }

    public  static ItemStack Add_lore(ItemStack itemstack, String s) {
        ItemMeta itemmeta = itemstack.getItemMeta();
        List<String> lores = new ArrayList();
        if (itemmeta.hasLore()) {
            lores = itemmeta.getLore();
        }
        lores.add(s);
        itemmeta.setLore(lores);
        itemstack.setItemMeta(itemmeta);
        return itemstack;
    }
























    
    
    public static Inventory create_settings_inv (Player p) {
           //is.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
            //is.setAmount(0);
            //itemmeta.addItemFlags(new ItemFlag[] { itemflag});
        Inventory set = Bukkit.createInventory( p, 45,  "§1Личные настройки" );
        ItemStack is;
        ItemMeta meta;
    //System.out.println("МЕНЮ>> getAllowFlight"+p.getAllowFlight()+" полёта:"+p.getFlySpeed()+" ходьбы:"+p.getWalkSpeed()+" погода:"+p.getPlayerWeather()+" время:"+p.getPlayerTimeOffset());  
        Reset_info(set);
    //System.out.println("--------- 1710:"+Ostrov.v1710);        
            is = new ItemStack(Material.valueOf("ELYTRA"));
            meta = is.getItemMeta();
            meta.setDisplayName("§6Полёт");
            meta.setLore(Arrays.asList( "§7Левый клик - включить","§7Правый клик - выключить" ));
            is.setItemMeta(meta);
            set.setItem(9, is);
            if ( p.isOp() || p.hasPermission("ostrov.fly")) {
                if (p.getAllowFlight()) set.setItem(0, ItemUtils.on);
                else set.setItem(0, ItemUtils.off);
            } else set.setItem(0, ItemUtils.noperm);


            is = new ItemStack(Material.DIAMOND_SWORD);
            meta = is.getItemMeta();
            meta.setDisplayName("§aРежим ПВП");
            meta.setLore(Arrays.asList( "§7Левый клик - включить","§7Правый клик - выключить" ));
            is.setItemMeta(meta);
            set.setItem(10, is);
            if (Pvp.allow_pvp_command) {
                if (PM.getOplayer(p.getName()).pvp_allow) set.setItem(1, ItemUtils.on);
                else set.setItem(1, ItemUtils.off);
            } else set.setItem(1, ItemUtils.disable);


            is = new ItemStack(Material.FEATHER);
            meta = is.getItemMeta();
            meta.setDisplayName("§6Скорость полёта");
            meta.setLore(Arrays.asList( "§7Левый клик - менять","§7Правый клик - сброс" ));
            is.setItemMeta(meta);
            set.setItem(11, is);
            if (!CMD.speed_command) set.setItem(2, ItemUtils.disable);
            else if ( p.isOp() || p.hasPermission("ostrov.flyspeed")) {
                set.setItem(2, ItemUtils.variable.clone()); set.getItem(2).setAmount( (int)(p.getFlySpeed()*10) );
            } else set.setItem(2, ItemUtils.noperm);

            is = new ItemStack(Material.CHAINMAIL_BOOTS);
            meta = is.getItemMeta();
            meta.setDisplayName("§6Скорость ходьбы");
            meta.setLore(Arrays.asList( "§7Левый клик - менять","§7Правый клик - сброс" ));
            is.setItemMeta(meta);
            set.setItem(12, is);
            if (!CMD.speed_command) set.setItem(3, ItemUtils.disable);
            else if ( p.isOp() || p.hasPermission("ostrov.walkspeed")) {
                set.setItem(3, ItemUtils.variable.clone()); set.getItem(3).setAmount( (int)(p.getWalkSpeed()*10) );
            } else set.setItem(3, ItemUtils.noperm);

            is = new ItemStack(Material.WATER_BUCKET);
            meta = is.getItemMeta();
            meta.setDisplayName("§6Личная погода");
            meta.setLore(Arrays.asList( "§7Левый клик - вкл/выкл дождь","§7Правый клик - как на сервере" ));
            is.setItemMeta(meta);
            set.setItem(13, is);
            if (!CMD.pweather_command) set.setItem(4, ItemUtils.disable);
            else if ( p.isOp() || p.hasPermission("ostrov.pweather")) {
                if (p.getPlayerWeather()==null) set.setItem(4, ItemUtils.panel);
                else  if (p.getPlayerWeather().equals(WeatherType.CLEAR)) set.setItem(4, ItemUtils.sun);
                else set.setItem(4, ItemUtils.rain);
            }
            else set.setItem(4, ItemUtils.noperm);

            is = new ItemStack(Material.GOLD_NUGGET);
            meta = is.getItemMeta();
            meta.setDisplayName("§6Личное время - управление");
            meta.setLore(Arrays.asList( "§7ЛКМ - заморозить/синхронизировать","§7Правый - сброс настроек" ));
            is.setItemMeta(meta);
            set.setItem(14, is);
            if (!CMD.ptime_command) set.setItem(5, ItemUtils.disable);
            else if ( p.isOp() || p.hasPermission("ostrov.ptime")) {
                if (p.isPlayerTimeRelative()) set.setItem(5, ItemUtils.on);
                else set.setItem(5, ItemUtils.panel);
            }
            else set.setItem(5, ItemUtils.noperm);

            is = new ItemStack(Material.CLOCK);
            meta = is.getItemMeta();
            meta.setDisplayName("§6Личное время - настройка");
            meta.setLore(Arrays.asList( "§7Левый клик - прибавить","§7Правый - убавить", "В режиме Заморозка время фиксировано","При синхронизации - меняется","с серверным с заданным смещением"));
            is.setItemMeta(meta);
            set.setItem(15, is);
            if (!CMD.ptime_command) set.setItem(6, ItemUtils.disable);
            else if ( p.isOp() || p.hasPermission("ostrov.ptime")) {
                set.setItem(6, ItemUtils.variable.clone()); 
                if (((int)(p.getPlayerTimeOffset()/1000))<1) set.getItem(6).setAmount(1); 
                else set.getItem(6).setAmount( ((int)(p.getPlayerTimeOffset()/1000)) ); 
            } else set.setItem(6, ItemUtils.noperm);

            is = new ItemStack(Material.GOLDEN_APPLE);
            meta = is.getItemMeta();
            meta.setDisplayName("§6Санаторий");
            meta.setLore(Arrays.asList( "§7Левый клик - исцеление"));
            is.setItemMeta(meta);
            set.setItem(16, is);
            if (!CMD.heal_command) set.setItem(7, ItemUtils.disable);
            else if ( p.isOp() || p.hasPermission("ostrov.heal")) {
                set.setItem(7, ItemUtils.on);
                if ( (int) p.getHealth() == p.getMaxHealth() ) set.setItem(7, ItemUtils.on);
                else set.setItem(7, ItemUtils.off); 
                set.getItem(7).setAmount( (int) p.getHealth() );
            } else set.setItem(7, ItemUtils.noperm);

            is = new ItemStack(Material.ANVIL);
            meta = is.getItemMeta();
            meta.setDisplayName("§6Кузница");
            meta.setLore(Arrays.asList( "§7Левый клик - ремонт всего в инв."));
            is.setItemMeta(meta);
            set.setItem(17, is);
            if (!CMD.repair_command) set.setItem(8, ItemUtils.disable);
            else if ( p.isOp() || p.hasPermission("ostrov.repair")) {
                set.setItem(8, ItemUtils.on);
                if ( Need_repair(p) ) set.setItem(8, ItemUtils.off);
                else set.setItem(8, ItemUtils.on); 
            } else set.setItem(8, ItemUtils.noperm);


    //---- личное инфо ----
            is = new ItemStack(Material.PAPER);
            meta = is.getItemMeta();
            meta.setDisplayName("§bМои характеристики");
            meta.setLore(Arrays.asList( 
            "§6Группы:",
            "§f"+  ApiOstrov.getChatGroups(p.getName()),
            "§5Блоков поставлено: §6"+PM.Getbplace(p.getName()),
            "§5Блоков сломано: §6"+PM.Getbbreak(p.getName()),
            "§5Души игроков: §6"+PM.Getpkill(p.getName()),
            "§5Души монстров: §6"+PM.Getmonsterkill(p.getName()),
            "§5Души мобов: §6"+PM.Getmobkill(p.getName()),
            "§5Погиб: §6"+PM.Getbdead(p.getName())
            ));
            is.setItemMeta(meta);
            set.setItem(31, is);



    //------ настройки --------
            is = new ItemStack(Material.RED_BED);
            meta = is.getItemMeta();
            meta.setDisplayName("§bМои дома");
            meta.setLore(Arrays.asList( "§7Информация и управление домами"));
            is.setItemMeta(meta);
            set.setItem(37, is);

            is = new ItemStack(Material.FURNACE);
            meta = is.getItemMeta();
            meta.setDisplayName("§bМои приваты");
            meta.setLore(Arrays.asList( "§7Информация и управление приватами" ,"§6Левый клик - §bВ этом мире + управление", "§6Правый клик - §6Во всех мирах (только просмотр)"));
            is.setItemMeta(meta);
            set.setItem(39, is);

            is = new ItemStack(Material.ENDER_CHEST);
            meta = is.getItemMeta();
            meta.setDisplayName("§bМои наборы");
            meta.setLore(Arrays.asList( "§7Информация и управление наборами"));
            is.setItemMeta(meta);
            set.setItem(41, is);

            is = new ItemStack(Material.COMPASS);
            meta = is.getItemMeta();
            meta.setDisplayName("§bМои варпы");
            meta.setLore(Arrays.asList( "§7Информация и управление варпами"));
            is.setItemMeta(meta);
            set.setItem(43, is);



            is = new ItemStack(Material.RED_MUSHROOM);
            meta = is.getItemMeta();
            meta.setDisplayName("§4Закрыть");
            is.setItemMeta(meta);
            set.setItem(44, is);

            return set;

    }




    public static Inventory update_settings(Player p, Inventory set) {

        Reset_info(set);

            if (!CMD.heal_command) set.setItem(6, ItemUtils.disable);
            else if ( p.isOp() || p.hasPermission("ostrov.heal")) {
                set.setItem(7, ItemUtils.on);
                if ( (int) p.getHealth() == p.getMaxHealth() ) set.setItem(7, ItemUtils.on);
                else set.setItem(7, ItemUtils.off); 
                set.getItem(7).setAmount( (int) p.getHealth() );
            } else set.setItem(7, ItemUtils.noperm);

            if (!CMD.repair_command) set.setItem(8, ItemUtils.disable);
            else if ( p.isOp() || p.hasPermission("ostrov.repair")) {
                set.setItem(8, ItemUtils.on);
                if ( Need_repair(p) ) set.setItem(8, ItemUtils.off);
                else set.setItem(8, ItemUtils.on); 
            } else set.setItem(8, ItemUtils.noperm);


            ItemStack is = new ItemStack(Material.PAPER);
            ItemMeta meta = is.getItemMeta();
            meta.setDisplayName("§bМои характеристики");
            meta.setLore(Arrays.asList( 
            "§6Группы:",
            "§f"+  ApiOstrov.getChatGroups(p.getName()),
            "§5Блоков поставлено: §6"+PM.Getbplace(p.getName()),
            "§5Блоков сломано: §6"+PM.Getbbreak(p.getName()),
            "§5Души игроков: §6"+PM.Getpkill(p.getName()),
            "§5Души монстров: §6"+PM.Getmonsterkill(p.getName()),
            "§5Души мобов: §6"+PM.Getmobkill(p.getName()),
            "§5Погиб: §6"+PM.Getbdead(p.getName())
            ));
            is.setItemMeta(meta);
            set.setItem(31, is);

            return set;

    }




































    public static void Reset_info (Inventory set) {
            for (int i=18; i<36; i++) {
                set.setItem(i, panel);
            }
        }


    public static int float_to_int (float f) {
        int i = (int) (f*10);
            if (i<0) i = 0;
            else if (i>10) i = 10;
    //System.out.println("f:"+f+" i:"+i);
            return i;
        }





    public static boolean Need_repair(Player p) {
            boolean need =false;
            for (ItemStack item : p.getInventory().getContents()) {
                if (item != null && !item.getType().isBlock() && item.getDurability() != 0) {
                    need = true;
                    break;
                }
            }
            for (ItemStack item : p.getInventory().getArmorContents()) {
                if (item != null && !item.getType().isBlock() && item.getDurability() != 0) {
                    need = true;
                    break;
                }
            }
            return need;
        }

    public static Set<String> Repair_all(Player p) {

            Set <String> repaired = new HashSet<String>() {};

            for (ItemStack item : p.getInventory().getContents()) {
                if (item != null && !item.getType().isBlock() && item.getDurability() != 0) {
                    repairItem(item);
                    if (Ostrov.langUtils) repaired.add( LanguageHelper.getItemDisplayName( item, "ru_RU"));
                    else repaired.add( item.getType().name() );
                }
            }
            for (ItemStack item : p.getInventory().getArmorContents()) {
                if (item != null && !item.getType().isBlock() && item.getDurability() != 0) {
                    repairItem(item);
                    if (Ostrov.langUtils) repaired.add( LanguageHelper.getItemDisplayName( item, "ru_RU"));
                    else repaired.add( item.getType().name() );
                }
            }

            p.updateInventory();

            return repaired;
        }

    
    private static void repairItem(final ItemStack item) {
        final Material material = item.getType();
        if (material.isBlock() || material.getMaxDurability() < 1) return; //throw new Exception(tl("repairInvalidType"));
        if (item.getDurability() == 0) return; //throw new Exception(tl("repairAlreadyFixed"));
        item.setDurability((short)0);
    }

 
    public static boolean hasName(final ItemStack is) {
        return is!=null && is.hasItemMeta() && is.getItemMeta().hasDisplayName();
    }

    public static String getName(final ItemStack is) {
        if (hasName(is)) return is.getItemMeta().getDisplayName();
        else return "";
    }


    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    @Deprecated
    public static String itemStackToString (final ItemStack is) {
        return itemStackToString(is, "<>");
    }
    
    public static String itemStackToString (final ItemStack is, String paramSplitter) {
        if (is==null || is.getType()==Material.AIR) {
            return "bedrock:1";
        }
        String  res = is.getType().toString().toLowerCase()+":"+is.getAmount(); //apple<>1
        paramSplitter = " "+paramSplitter+" ";
        
        if (is.hasItemMeta()) {
            
            final ItemMeta im = is.getItemMeta();
            if (im.hasDisplayName()) {
                res=res+paramSplitter+"name:"+im.getDisplayName().replaceAll("§", "&");
            }
            
            if (im.hasLore()) {
                for (String lore:im.getLore()) {
                    if (lore.isEmpty()) {
                        res=res+paramSplitter+"lore:&7";
                    } else {
                        res=res+paramSplitter+"lore:"+lore;
                    }
                }
            }
            
            if (im.hasCustomModelData()) {
                res=res+paramSplitter+"custommodeldata:"+im.getCustomModelData();
            }
            
            if (!im.getItemFlags().isEmpty()) {
                for (ItemFlag itemFlag : im.getItemFlags()) {
                    res=res+paramSplitter+"itemflag:"+itemFlag.toString();
                }
            }
            if (im.isUnbreakable()) {
                res=res+paramSplitter+"unbreakable:true";
            }
            
        }
        
        if (is.getType().toString().startsWith("LEATHER_") && is.hasItemMeta()) {
            LeatherArmorMeta lam = (LeatherArmorMeta)is.getItemMeta();
            res=res+paramSplitter+"color:"+lam.getColor().getBlue()+":"+lam.getColor().getGreen()+":"+lam.getColor().getRed();
        }
        
        //if (is.getType().toString().contains("BOOK")) { !!учесть CraftMetaEnchantedBook cannot be cast to BookMeta для ENCHANTED_BOOK
         //   BookMeta bookMeta = (BookMeta)is.getItemMeta();
            //res=res+paramSplitter+"color:"+lam.getColor().getBlue()+":"+lam.getColor().getGreen()+":"+lam.getColor().getRed();
       // }
         
        if (is.getType()==Material.ENCHANTED_BOOK) {
            EnchantmentStorageMeta enchantedBookMeta = (EnchantmentStorageMeta) is.getItemMeta();
            if (enchantedBookMeta.hasStoredEnchants()) {
                for (Enchantment enchant : enchantedBookMeta.getStoredEnchants().keySet()) {
                    res=res+paramSplitter+"bookenchant:"+enchant.getKey().getKey()+":"+enchantedBookMeta.getStoredEnchantLevel(enchant);
                }
            }
        }

        if (is.getType()==Material.PLAYER_HEAD) {
            SkullMeta skullMeta = (SkullMeta) is.getItemMeta();
            //if (skullMeta.getCustomModelData()==777) {
            //    res=res+paramSplitter+"skulltexture:"+skullMeta.;
           // } else 
                if (skullMeta.hasOwner()) {
                    res=res+paramSplitter+"skullowneruuid:"+skullMeta.getOwningPlayer().getUniqueId().toString();
                }
            
        }

        if (!is.getEnchantments().isEmpty()) {
            for (Enchantment enchant : is.getEnchantments().keySet()) {
                res=res+paramSplitter+"enchant:"+enchant.getKey().getKey()+":"+is.getEnchantments().get(enchant);
            }
        }
        
        if ( is.getType()==Material.POTION  || is.getType()==Material.LINGERING_POTION || is.getType()==Material.SPLASH_POTION || is.getType()==Material.TIPPED_ARROW) {
            PotionMeta potionMeta = (PotionMeta) is.getItemMeta();
            res=res+paramSplitter+"basepotiondata:"+potionMeta.getBasePotionData().getType().toString().toLowerCase()+":"+potionMeta.getBasePotionData().isExtended()+":"+potionMeta.getBasePotionData().isUpgraded();
            if (potionMeta.hasCustomEffects()) {
                for (PotionEffect customPotionEffect : potionMeta.getCustomEffects()) {
                    res=res+paramSplitter+"custompotioneffect:"+customPotionEffect.getType()+":"+customPotionEffect.getDuration()+":"+customPotionEffect.getAmplifier();
                }
            }
            if (potionMeta.hasColor()) {
                res=res+paramSplitter+"color:"+potionMeta.getColor().getBlue()+":"+potionMeta.getColor().getGreen()+":"+potionMeta.getColor().getRed();
            }
        }
        
        
        return res;
    }    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    @Deprecated
    public static ItemStack getItemStackFromString ( final String item_as_string) {
        if (item_as_string.contains("<>")) return getItemStackFromString(item_as_string, "<>");
        else return getItemStackFromString(item_as_string, " : ");
    }
    
    public static ItemStack getItemStackFromString ( final String item_as_string, final String paramSplitter) {
        
        //grass:1<>name:nnn<>lore:sdsds:sdsd<>enchant:ARROW_DAMAGE:1<>dye:RED<>end
        
        ItemBuilder builder = new ItemBuilder(Material.BEDROCK);
        
        if (item_as_string==null || item_as_string.isEmpty() ) {
            builder.setName("§cСтрока для декодирования ошибочная!");
            return builder.build();
        }
        
//System.out.println("--- paramSplitter="+paramSplitter);
        if ( paramSplitter==null || paramSplitter.isEmpty() ) {
            builder.setName("§cРазделитель для декодирования ошибочный!");
            return builder.build();
        }
        
        final List <String> splittedParametrs = new ArrayList<>();
        
        for (String param : item_as_string.split(paramSplitter)) {
            if (!param.trim().isEmpty()) {
                splittedParametrs.add(param.trim());
//System.out.println("--- splittedParametrs.add="+param.trim());
            }
        }
        
        if (splittedParametrs.isEmpty()) {
            builder.setName("§cНе найдено никаких параметров!");
            return builder.build();
        }
        
        
        
//System.out.println("--- splittedParametrs.size="+splittedParametrs.size()+" 0="+splittedParametrs.get(0));
        Material mat;
        if ( splittedParametrs.get(0).contains(":") ) { //если с колличеством
            mat=Material.matchMaterial(splittedParametrs.get(0).trim().split(":")[0].trim());
            if ( mat != null) {
                builder.setType(mat);
                if (Ostrov.isInteger(splittedParametrs.get(0).trim().split(":")[1].trim())) {
                    builder.setAmount(Integer.valueOf(splittedParametrs.get(0).trim().split(":")[1].trim()));
                } else {
                    Ostrov.log_warn("Декодер предмета : §7строка >§f"+item_as_string+"§7<, неправильное колличество §f"+splittedParametrs.get(0).split(":")[1]);
                }
            } else {
                Ostrov.log_warn("Декодер предмета : §7строка >§f"+item_as_string+"§7<, нет материала §f"+splittedParametrs.get(0).split(":")[0]);
            }
        } else {
            mat=Material.matchMaterial(splittedParametrs.get(0).trim());
            if ( mat != null) {
                builder.setType(mat);
            } else {
                Ostrov.log_warn("Декодер предмета : §7строка >§f"+item_as_string+"§7<, нет материала §f"+splittedParametrs.get(0));
            }
        }
        
        if (splittedParametrs.size()==1) {
            return builder.build();
        }

//System.out.println("2 itemstack="+itemstack);
        String[] splittedParam;
        for (int j = 1; j < splittedParametrs.size(); ++j) {
            
        splittedParam = splittedParametrs.get(j).trim().split(":");
        if (splittedParam.length==1 ) {
            if (!splittedParam[0].equals("end")) Ostrov.log_warn("Декодер предмета : §7строка >§f"+item_as_string+"§7<, пустой параметр §f"+splittedParametrs.get(j));
            continue;
        }
        
//System.out.println("--"+temp[j]);
        try {
            
            switch (splittedParam[0].trim().toLowerCase()) {
                    
                case "name":
                    if (splittedParam.length==2) {
                        builder.setName(splittedParam[1].replaceAll("&", "§"));
                    } else {
                        Ostrov.log_warn("Декодер name : §7строка >§f"+item_as_string+"§7<, неверные параметры §f"+splittedParam[1].toUpperCase());
                    }
                    break;
                    
                    
                case "lore":
                    if (splittedParam.length>=2) {
                        builder.addLore(splittedParametrs.get(j).trim().replaceFirst("lore:", "").replaceAll("&", "§"));
                    } else {
                        Ostrov.log_warn("Декодер lore : §7строка >§f"+item_as_string+"§7<, неверные параметры §f"+splittedParam[1].toUpperCase());
                    }
                    break;
                    
                    
                case "color":
                    if (splittedParam.length==4) {
                        if ( ApiOstrov.isInteger(splittedParam[1]) && ApiOstrov.isInteger(splittedParam[2]) && ApiOstrov.isInteger(splittedParam[3]) ) {
                            builder.setColor(Color.fromRGB(Integer.valueOf(splittedParam[1]), Integer.valueOf(splittedParam[2]), Integer.valueOf(splittedParam[3])));
                        } else {
                            Ostrov.log_warn("Декодер color : §7строка >§f"+item_as_string+"§7<, должны быть числа §f"+splittedParam[1]+" "+splittedParam[2]+" "+splittedParam[3]);
                        }
                    } else {
                        Ostrov.log_warn("Декодер color : §7строка >§f"+item_as_string+"§7<, неверные параметры §f"+splittedParam[1].toUpperCase());
                    }
                    break;
                    
                    
                case "custommodeldata":
                    if (splittedParam.length==2) {
                        if ( ApiOstrov.isInteger(splittedParam[1])) {
                            int modelData = Integer.valueOf(splittedParam[1]);
                            if (modelData<0) modelData=0;
                            builder.setModelData(modelData);
                        } else {
                            Ostrov.log_warn("Декодер custommodeldata : §7строка >§f"+item_as_string+"§7<, должны быть числа §f"+splittedParam[1]);
                        }
                    } else {
                        Ostrov.log_warn("Декодер name : §7строка >§f"+item_as_string+"§7<, неверные параметры §f"+splittedParam[1].toUpperCase());
                    }
                    break;
                    
                case "itemflag":
                    if (splittedParam.length==2) {
                        final ItemFlag itemFlag = ItemFlag.valueOf(splittedParam[1]);
                        if ( itemFlag!=null ) {
                            builder.addFlags(itemFlag);
                        } else {
                            Ostrov.log_warn("Декодер itemflag : §7строка >§f"+item_as_string+"§7<, нет такого флага §f"+splittedParam[1]);
                        }
                    } else {
                        Ostrov.log_warn("Декодер itemflag : §7строка >§f"+item_as_string+"§7<, неверные параметры §f"+splittedParam[1].toUpperCase());
                    }
                    break;
                    
                    
                case "unbreakable":
                    builder.setUnbreakable(true);
                    break;

                case "skull":
                    if (splittedParam.length==2) {
                        builder.setSkullOwner(splittedParam[1]);
                    } else {
                        Ostrov.log_warn("Декодер skull : §7строка >§f"+item_as_string+"§7<, неверные параметры §f"+splittedParam[1].toUpperCase());
                    }
                    break;
                    
                case "skullowneruuid":
                    if (splittedParam.length==2) {
                        builder.setSkullOwnerUuid(splittedParam[1]);
                    } else {
                        Ostrov.log_warn("Декодер skullowneruuid : §7строка >§f"+item_as_string+"§7<, неверные параметры §f"+splittedParam[1].toUpperCase());
                    }
                    break;
                    
                case "skulltexture":
                    if (splittedParam.length==2) {
                        //
                        //builder.setSkullOwnerUuid(splittedParam[1]);
                    } else {
                        Ostrov.log_warn("Декодер skulltexture : §7строка >§f"+item_as_string+"§7<, неверные параметры §f"+splittedParam[1].toUpperCase());
                    }
                    break;

                //enchant:silk_touch:1
                case "enchant":
                case "bookenchant":
                    if (splittedParam.length==3) {
                        Enchantment enchant = EnchantDecode.fromEnchantmentName(splittedParam[1]);
                        if (enchant==null) enchant = Enchantment.getByKey (NamespacedKey.minecraft(splittedParam[1]));
                        if (enchant!=null) {
                            if ( ApiOstrov.isInteger(splittedParam[2]) ){
                                builder.addEnchantment(enchant, Integer.valueOf(splittedParam[2]));
                            } else {
                                Ostrov.log_warn("Декодер enchant : §7строка >§f"+item_as_string+"§7<, должны быть числа §f"+splittedParam[2]);
                            }                        
                        } else {
                            Ostrov.log_warn("Декодер enchant : §7строка >§f"+item_as_string+"§7<, нет таких чар §f"+splittedParam[1]);
                        }
                    } else {
                        Ostrov.log_warn("Декодер enchant : §7строка >§f"+item_as_string+"§7<, неверные параметры §f"+splittedParam[1].toUpperCase());
                    }
                    break;
                    
                    
                case "basepotiondata":
                    if (splittedParam.length==4) {
                        if (builder.getType()==Material.POTION ||builder.getType()==Material.TIPPED_ARROW || builder.getType()==Material.LINGERING_POTION || builder.getType()==Material.SPLASH_POTION) {
                            PotionType potionType = null;//PotionType.valueOf(splittedParam[1].toUpperCase());
                            for (final PotionType pt : PotionType.values()) {
                                if (pt.toString().equalsIgnoreCase(splittedParam[1])) {
                                    potionType = pt;
                                    break;
                                }
                            }
                            if (potionType!=null) {
                                if ( (splittedParam[2].equalsIgnoreCase("true") || splittedParam[2].equalsIgnoreCase("false")) &&
                                        (splittedParam[2].equalsIgnoreCase("true") || splittedParam[2].equalsIgnoreCase("false")) ){
                                    builder.setBasePotionData(new PotionData( potionType, Boolean.valueOf(splittedParam[2].toLowerCase()), Boolean.valueOf(splittedParam[3].toLowerCase()) ) );
                                } else {
                                    Ostrov.log_warn("Декодер basepotiondata : §7строка >§f"+item_as_string+"§7<, должно быть true/false §f"+splittedParam[2]+" "+splittedParam[3]);
                                }
                            } else {
                                Ostrov.log_warn("Декодер basepotiondata : §7строка >§f"+item_as_string+"§7<, нет PotionType §f"+splittedParam[1].toUpperCase());
                            }
                        } else {
                            Ostrov.log_warn("Декодер basepotiondata : §7строка >§f"+item_as_string+"§7<, неприменима к §f"+builder.getType());
                        }
                    } else {
                        Ostrov.log_warn("Декодер basepotiondata : §7строка >§f"+item_as_string+"§7<, неверные параметры §f"+splittedParam[1].toUpperCase());
                    }
                    break;
                    
                    
                case "effect":
                case "custompotioneffect":
                    if (splittedParam.length==4) {
                        if (builder.getType()==Material.POTION ||builder.getType()==Material.TIPPED_ARROW || builder.getType()==Material.LINGERING_POTION || builder.getType()==Material.SPLASH_POTION) {
                            PotionEffectType potionEffectType = null;//PotionEffectType.getByName(splittedParam[1].toUpperCase());
                            for (final PotionEffectType pe : PotionEffectType.values()) {
//System.out.println("effect find >"+splittedParam[1]+"< equalsIgnoreCase?"+pe.getName());
                                if (pe.getName().equalsIgnoreCase(splittedParam[1])) {
                                    potionEffectType = pe;
                                    break;
                                }
                            }
                            if (potionEffectType!=null) {
                                if ( ApiOstrov.isInteger(splittedParam[2]) && ApiOstrov.isInteger(splittedParam[2]) ){
                                    builder.addCustomPotionEffect(new PotionEffect( potionEffectType, Integer.valueOf(splittedParam[2].toLowerCase()), Integer.valueOf(splittedParam[3].toLowerCase()) ) );
                                } else {
                                    Ostrov.log_warn("Декодер custompotioneffect : §7строка >§f"+item_as_string+"§7<, должны быть числа §f"+splittedParam[2]+" "+splittedParam[3]);
                                }
                            } else {
                                Ostrov.log_warn("Декодер custompotioneffect : §7строка >§f"+item_as_string+"§7<, нет PotionType §f"+splittedParam[1].toUpperCase());
                            }
                        } else {
                            Ostrov.log_warn("Декодер custompotioneffect : §7строка >§f"+item_as_string+"§7<, неприменима к §f"+builder.getType());
                        }
                    } else {
                        Ostrov.log_warn("Декодер custompotioneffect : §7строка >§f"+item_as_string+"§7<, неверные параметры §f"+splittedParam[1].toUpperCase());
                    }
                    break;
                    
                default:
                    Ostrov.log_warn("Декодер custompotioneffect : §7строка >§f"+item_as_string+"§7<, параметр не распознан §f"+splittedParam[0]);
                    break;
                    
                    
                    
                
            }
        } catch (IllegalArgumentException | SecurityException | NullPointerException ex) {
            Ostrov.log_err("getItemStackFromString : "+item_as_string+" - "+ex.getMessage());
        }

        }
        
        return builder.build();
    }



    
    

    
    
    
    
    
   public static boolean compareItem(final ItemStack is1, final ItemStack is2, final boolean checkLore) {
       if (is1==null || is2==null) return false;
       
       if (is1.getType() == is2.getType()) {  //тип совпадает
           
           if (is1.hasItemMeta() && is2.hasItemMeta()) { //если у обоих есть мета
               
               if (is1.getItemMeta().hasDisplayName()&& is2.getItemMeta().hasDisplayName()) { //если у обоих есть название
               
                   if (is1.getItemMeta().getDisplayName().equals(is2.getItemMeta().getDisplayName())) { //если название совпадает
                       
                       if (!checkLore) return true;
                       
                       if (is1.getItemMeta().hasLore() && is2.getItemMeta().hasLore()) { //если у обоих есть лоре
                           
                           final List<String>lore1=is1.getItemMeta().getLore();
                           final List<String>lore2=is2.getItemMeta().getLore();
                           
                           if (!lore1.isEmpty() && !lore2.isEmpty()) {  //если обе лоре не пустые
                           
                               if (lore1.size() != lore2.size()) return false;  //если размеры лоре не одинаковые - нет
                               
                               for (int i=0; i<lore1.size(); i++) {
                                   if ( !lore1.get(i).equals(lore2.get(i))) {  //перебираем строки
                                       return false;  //хоть одна строка разная - предметы разные
                                   }
                               }
                               return true;
                               
                           } else return lore1.isEmpty() && lore2.isEmpty(); //если одна лоре пустая, другая тоже должна быть пустая
                           
                       } else return !is1.getItemMeta().hasLore() && !is2.getItemMeta().hasLore(); //если хотя бы у одного неты лоре, то и у другого не должно быть
                       
                   } else return false; //если название не совпадает - разные
                   
            } else return !is1.getItemMeta().hasDisplayName() && !is2.getItemMeta().hasDisplayName(); //если хотя бы у одного неты названия, то и у другого не должно быть
                   
           } else return !is1.hasItemMeta() && !is2.hasItemMeta(); //если хотя бы у одного неты меты, то и у другого не должно быть
           
       } else return false; //если тип не совпадает - разные
       
      //return is1 != null && is2 != null && is1.getType().equals(is2.getType()) && is1.getItemMeta().hasDisplayName() && is1.getItemMeta().hasDisplayName() && is1.getItemMeta().getDisplayName().equals(is2.getItemMeta().getDisplayName());
   }

    public static void fillSign(final Sign sign, String suggest) {
        if (suggest==null || suggest.isEmpty()) return;
        //if (suggest.length()<=14) {
        //    sign.setLine(0, suggest);
        //} else {
            int line = 0;
            while( suggest.length() > 15 && line<=3) {
                sign.setLine(line, suggest.substring(0, 15));
//System.out.println("line="+line+" -> "+suggest.substring(0, 14));
                suggest = suggest.substring(15);
                line++;
            }
            if (line<=3 && !suggest.isEmpty()) { //добавляем остаток
                sign.setLine(line, suggest);
            }
        //}
        sign.update(); 
    }

    public static GameProfile getUrlGameProfile(String url) {
        if (gameProfiles.containsKey(url)) return gameProfiles.get(url);
        //if (!url.startsWith("http://")) url = "http://textures.minecraft.net/texture/" + url;
        final GameProfile gameProfile = new GameProfile(UUID.randomUUID(), null);
        final byte[] encodedData = Base64.encodeBase64(String.format("{textures:{SKIN:{url:\"%s\"}}}", url).getBytes());
        gameProfile.getProperties().put("textures", new Property("textures", new String(encodedData)));
        gameProfiles.put(url, gameProfile);
        return gameProfile;
    }

    public static GameProfile getTextureGameProfile(String blockTexture) {
        if (gameProfiles.containsKey(blockTexture)) return gameProfiles.get(blockTexture);
        //if (!skullTexture.startsWith("http://")) skullTexture = "http://textures.minecraft.net/texture/" + skullTexture;
        final GameProfile gameProfile = new GameProfile(UUID.randomUUID(), null);
        gameProfile.getProperties().put("textures", new Property("textures", blockTexture));
        gameProfiles.put(blockTexture, gameProfile);
        return gameProfile;
    }

    

    

   
    public enum EnchantDecode {
        PROTECTION_ENVIRONMENTAL (Enchantment.PROTECTION_ENVIRONMENTAL, "protection"),
        PROTECTION_FIRE (Enchantment.PROTECTION_FIRE, "fire_protection"),
        PROTECTION_FALL (Enchantment.PROTECTION_FALL, "feather_falling"),
        PROTECTION_EXPLOSIONS (Enchantment.PROTECTION_EXPLOSIONS, "blast_protection"),
        PROTECTION_PROJECTILE (Enchantment.PROTECTION_PROJECTILE, "projectile_protection"),
        OXYGEN (Enchantment.OXYGEN, "respiration"),
        WATER_WORKER (Enchantment.WATER_WORKER, "aqua_affinity"),
        THORNS (Enchantment.THORNS, "thorns"),
        DEPTH_STRIDER (Enchantment.DEPTH_STRIDER, "depth_strider"),
        FROST_WALKER (Enchantment.FROST_WALKER, "frost_walker"),
        BINDING_CURSE (Enchantment.BINDING_CURSE, "binding_curse"),
        DAMAGE_ALL (Enchantment.DAMAGE_ALL, "sharpness"),
        DAMAGE_UNDEAD (Enchantment.DAMAGE_UNDEAD, "smite"),
        DAMAGE_ARTHROPODS (Enchantment.DAMAGE_ARTHROPODS, "bane_of_arthropods"),
        KNOCKBACK (Enchantment.KNOCKBACK, "knockback"),
        FIRE_ASPECT (Enchantment.FIRE_ASPECT, "fire_aspect"),
        LOOT_BONUS_MOBS (Enchantment.LOOT_BONUS_MOBS, "looting"),
        SWEEPING_EDGE (Enchantment.SWEEPING_EDGE, "sweeping"),
        DIG_SPEED (Enchantment.DIG_SPEED, "efficiency"),
        SILK_TOUCH (Enchantment.SILK_TOUCH, "silk_touch"),
        DURABILITY (Enchantment.DURABILITY, "unbreaking"),
        LOOT_BONUS_BLOCKS (Enchantment.LOOT_BONUS_BLOCKS, "fortune"),
        ARROW_DAMAGE (Enchantment.ARROW_DAMAGE, "power"),
        ARROW_KNOCKBACK (Enchantment.ARROW_KNOCKBACK, "punch"),
        ARROW_FIRE (Enchantment.ARROW_FIRE, "flame"),
        ARROW_INFINITE (Enchantment.ARROW_INFINITE, "infinity"),
        LUCK (Enchantment.LUCK, "luck_of_the_sea"),
        LURE (Enchantment.LURE, "lure"),
        LOYALTY (Enchantment.LOYALTY, "loyalty"),
        IMPALING (Enchantment.IMPALING, "impaling"),
        RIPTIDE (Enchantment.RIPTIDE, "riptide"),
        CHANNELING (Enchantment.CHANNELING, "channeling"),
        MULTISHOT (Enchantment.MULTISHOT, "multishot"),
        QUICK_CHARGE (Enchantment.QUICK_CHARGE, "quick_charge"),
        PIERCING (Enchantment.PIERCING, "piercing"),
        MENDING (Enchantment.MENDING, "mending"),
        VANISHING_CURSE (Enchantment.VANISHING_CURSE, "vanishing_curse"),
        ;
        
        public Enchantment enchantment;
        public String key;
        
        private EnchantDecode (Enchantment enchantment, String key) {
            this.enchantment = enchantment;
            this.key = key;
        }
        
        
        public static Enchantment fromEnchantmentName (final String name) {
            if (name==null || name.isEmpty()) return null;
            for (EnchantDecode ed : EnchantDecode.values()) {
                if (ed.toString().equalsIgnoreCase(name)) {
                    return ed.enchantment;
                }
            }
            return null;
        }
        
   }
   
   

    @Deprecated
    public static ItemStack getBiomeIcon (final Biome b) {
        return buildBiomeIcon(b).build();
    }     
     
    public static ItemBuilder buildBiomeIcon (final Biome b) {
        final ItemBuilder builder = new ItemBuilder(Material.TROPICAL_FISH_BUCKET);
//System.out.println("getBiomeIcon "+b.toString());       
        if ( b.toString().equalsIgnoreCase("NETHER") || b.toString().equalsIgnoreCase("NETHER_WASTES") ) {
            builder.setType(Material.NETHERRACK);
        } else {
            
            switch (b) {
                case BADLANDS: 
                    builder.setType(Material.RED_SAND);
                    //builder.setName("§eBADLANDS");
                    break;
                case BADLANDS_PLATEAU: 
                    builder.setType(Material.RED_SANDSTONE);
                    break;
                case BAMBOO_JUNGLE: 
                    builder.setType(Material.BAMBOO);
                    break;
                 case BAMBOO_JUNGLE_HILLS: 
                    builder.setType(Material.BAMBOO);
                    break;
                 case BEACH: 
                    builder.setType(Material.HORN_CORAL_FAN);
                    break;
                 case BIRCH_FOREST: 
                    builder.setType(Material.BIRCH_LOG);
                    break;
                 case BIRCH_FOREST_HILLS: 
                    builder.setType(Material.BIRCH_WOOD);
                    break;
                 case COLD_OCEAN: 
                    builder.setType(Material.BLUE_CONCRETE_POWDER);
                    break;
                 case DARK_FOREST: 
                    builder.setType(Material.DARK_OAK_LOG);
                    break;
                 case MUSHROOM_FIELDS: 
                    builder.setType(Material.MYCELIUM);
                    break;
                 case DARK_FOREST_HILLS: 
                    builder.setType(Material.DARK_OAK_WOOD);
                    break;
                 case DEEP_COLD_OCEAN: 
                    builder.setType(Material.BLUE_CONCRETE);
                    break;
                 case DEEP_FROZEN_OCEAN: 
                    builder.setType(Material.BLUE_ICE);
                    break;
                 case DEEP_LUKEWARM_OCEAN: 
                    builder.setType(Material.LIGHT_BLUE_CONCRETE);
                    break;
                 case DEEP_OCEAN: 
                     builder.setType(Material.BLUE_WOOL);
                     break;
                 case DEEP_WARM_OCEAN: 
                     builder.setType(Material.CYAN_CONCRETE);
                     break;
                 case DESERT: 
                     builder.setType(Material.SAND);
                     break;
                 case DESERT_HILLS: 
                     builder.setType(Material.SANDSTONE);
                     break;
                 case DESERT_LAKES: 
                     builder.setType(Material.CHISELED_SANDSTONE);
                     break;
                 case END_BARRENS: 
                     builder.setType(Material.END_STONE);
                     break;
                 case END_HIGHLANDS: 
                     builder.setType(Material.END_STONE_BRICKS);
                     break;
                 case END_MIDLANDS: 
                     builder.setType(Material.END_STONE_BRICKS);
                     break;
                 case ERODED_BADLANDS: 
                     builder.setType(Material.DEAD_BUSH);
                     break;
                 case FLOWER_FOREST: 
                     builder.setType(Material.ROSE_BUSH);
                     break;
                 case FOREST: 
                     builder.setType(Material.DARK_OAK_LOG);
                     break;
                 case FROZEN_OCEAN: 
                     builder.setType(Material.PACKED_ICE);
                     break;
                 case FROZEN_RIVER: 
                     builder.setType(Material.LIGHT_BLUE_DYE);
                     break;
                 case GIANT_SPRUCE_TAIGA: 
                     builder.setType(Material.SPRUCE_SAPLING);
                     break;
                 case GIANT_SPRUCE_TAIGA_HILLS: 
                     builder.setType(Material.SPRUCE_SAPLING);
                     break;
                 case GIANT_TREE_TAIGA: 
                     builder.setType(Material.DARK_OAK_SAPLING);
                     break;
                 case GIANT_TREE_TAIGA_HILLS: 
                     builder.setType(Material.DARK_OAK_SAPLING);
                     break;
                 case GRAVELLY_MOUNTAINS: 
                     builder.setType(Material.GRAVEL);
                     break;
                 case ICE_SPIKES: 
                     builder.setType(Material.ICE);
                     break;
                 case JUNGLE: 
                     builder.setType(Material.JUNGLE_LOG);
                     break;
                 case JUNGLE_EDGE: 
                     builder.setType(Material.STRIPPED_JUNGLE_LOG);
                     break;
                 case JUNGLE_HILLS: 
                     builder.setType(Material.JUNGLE_WOOD);
                     break;
                 case LUKEWARM_OCEAN: 
                     builder.setType(Material.LIGHT_BLUE_CONCRETE_POWDER);
                     break;
                 case MODIFIED_BADLANDS_PLATEAU: 
                     builder.setType(Material.CHISELED_RED_SANDSTONE);
                     break;
                 case MODIFIED_GRAVELLY_MOUNTAINS: 
                     builder.setType(Material.CLAY);
                     break;
                 case MODIFIED_JUNGLE: 
                     builder.setType(Material.JUNGLE_SAPLING);
                     break;
                 case MODIFIED_JUNGLE_EDGE: 
                     builder.setType(Material.JUNGLE_SAPLING);
                     break;
                 case MODIFIED_WOODED_BADLANDS_PLATEAU: 
                     builder.setType(Material.DEAD_BUSH);
                     break;
                 case MOUNTAIN_EDGE: 
                     builder.setType(Material.ANDESITE);
                     break;
                 case MOUNTAINS: 
                     builder.setType(Material.STONE);
                     break;
                 case MUSHROOM_FIELD_SHORE:
                     builder.setType(Material.RED_MUSHROOM_BLOCK);
                     break;
                 case OCEAN: 
                     builder.setType(Material.WATER_BUCKET);
                     break;
                 case PLAINS: 
                     builder.setType(Material.GRASS_BLOCK);
                     break;
                 case RIVER: 
                     builder.setType(Material.BLUE_DYE);
                     break;
                 case SAVANNA: 
                     builder.setType(Material.ACACIA_LOG);
                     break;
                 case SAVANNA_PLATEAU: 
                     builder.setType(Material.ACACIA_WOOD);
                     break;
                 case SHATTERED_SAVANNA: 
                     builder.setType(Material.STRIPPED_ACACIA_LOG);
                     break;
                 case SHATTERED_SAVANNA_PLATEAU: 
                     builder.setType(Material.STRIPPED_ACACIA_WOOD);
                     break;
                 case SMALL_END_ISLANDS: 
                     builder.setType(Material.END_STONE);
                     break;
                 case SNOWY_BEACH: 
                     builder.setType(Material.SNOW);
                     break;
                 case SNOWY_MOUNTAINS: 
                     builder.setType(Material.SNOW_BLOCK);
                     break;
                 case SNOWY_TAIGA: 
                     builder.setType(Material.WHITE_WOOL);
                     break;
                 case SNOWY_TAIGA_HILLS: 
                     builder.setType(Material.WHITE_CONCRETE_POWDER);
                     break;
                 case SNOWY_TAIGA_MOUNTAINS: 
                     builder.setType(Material.WHITE_CONCRETE);
                     break;
                 case SNOWY_TUNDRA: 
                     builder.setType(Material.BONE_BLOCK);
                     break;
                 case STONE_SHORE: 
                     builder.setType(Material.DEAD_TUBE_CORAL_BLOCK);
                     break;
                 case SUNFLOWER_PLAINS: 
                     builder.setType(Material.SUNFLOWER);
                     break;
                 case SWAMP: 
                     builder.setType(Material.LILY_PAD);
                     break;
                 case SWAMP_HILLS: 
                     builder.setType(Material.VINE);
                     break;
                 case TAIGA: 
                     builder.setType(Material.SPRUCE_LOG);
                     break;
                 case TAIGA_HILLS: 
                     builder.setType(Material.SPRUCE_WOOD);
                     break;
                 case TAIGA_MOUNTAINS: 
                     builder.setType(Material.STRIPPED_SPRUCE_LOG);
                     break;
                 case TALL_BIRCH_FOREST: 
                     builder.setType(Material.STRIPPED_BIRCH_LOG);
                     break;
                 case TALL_BIRCH_HILLS: 
                     builder.setType(Material.STRIPPED_BIRCH_WOOD);
                     break;
                 case THE_END: 
                     builder.setType(Material.END_STONE);
                     break;
                 case THE_VOID: 
                     builder.setType(Material.BEDROCK);
                     break;
                 case WARM_OCEAN: 
                     builder.setType(Material.CYAN_CONCRETE_POWDER);
                     break;
                 case WOODED_BADLANDS_PLATEAU: 
                     builder.setType(Material.STRIPPED_OAK_WOOD);
                     break;
                 case WOODED_HILLS: 
                     builder.setType(Material.STRIPPED_OAK_LOG);
                     break;
                 case WOODED_MOUNTAINS: 
                     builder.setType(Material.STRIPPED_OAK_WOOD);
                     break;
                default:
                    //builder.setType(Material.TROPICAL_FISH_BUCKET);
                    //builder.setName("§e"+b.toString().toLowerCase());
                    break;
            }
        }
        
        if (Ostrov.langUtils) {
            builder.setName(LanguageHelper.getBiomeName(b, "ru_RU"));
//System.out.println("setName LanguageHelper "+LanguageHelper.getBiomeName(b, "ru_RU"));        
        } else {
            builder.setName(b.toString());
//System.out.println("setName "+b.toString());        
        }
        
       return builder;
    }
     
     
     
     
 
     
     
     
    public static ItemBuilder buildEntityIcon(final EntityType type) {
        final ItemBuilder builder = new ItemBuilder(Material.PLAYER_HEAD);
        
        // VM.getNmsServer().getCustomHead("6d865aae2746a9b8e9a4fe629fb08d18d0a9251e5ccbe5fa7051f53eab9b94", "§fдалее", "");
        switch (type) {
            case ARMOR_STAND : builder.setType(Material.STRIPPED_OAK_WOOD); break;
            
            case ZOMBIE : builder.setCustomHeadTexture("6d865aae2746a9b8e9a4fe629fb08d18d0a9251e5ccbe5fa7051f53eab9b94"); break;
            
        }
        //
        
        
        if (Ostrov.langUtils) {
            builder.setName(LanguageHelper.getEntityName(type, "ru_RU"));
        } else {
            builder.setName(type.toString());
        }
        
        return builder;
    }
    
     
     
     
     
     
    public static boolean isTools(final Material mat) {
        if (
                mat.toString().endsWith("_SWORD") ||
                mat.toString().endsWith("_CHESTPLATE") ||
                mat.toString().endsWith("_HELMET") ||
                mat.toString().endsWith("_BOOTS") ||
                mat.toString().endsWith("_LEGGINS")
                ) return true;
            
         switch (mat) {
             case SHIELD:
             case TRIDENT:
             case CROSSBOW:
             case BOW:
                 return true;
         }
        return false;
    }  
     
    public static boolean isWeapons(final Material mat) {
        if (
                mat.toString().endsWith("_AXE") ||
                mat.toString().endsWith("_SHOVEL") ||
                mat.toString().endsWith("_PICKAXE") ||
                mat.toString().endsWith("_HOE") 
                ) return true;
            
         switch (mat) {
             case SHEARS:
             case FLINT_AND_STEEL:
             case FISHING_ROD:
             case COMPASS:
             case LEAD:
             case CLOCK:
             case NAME_TAG:
                 return true;
         }
        return false;
    }  
     
     
     
     
     
     
     
     
     
     
     
     
      
}