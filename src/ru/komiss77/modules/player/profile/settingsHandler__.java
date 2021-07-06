package ru.komiss77.modules.player.profile;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import ru.komiss77.ApiOstrov;
import ru.komiss77.enums.Data;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.Ostrov;
import ru.komiss77.modules.player.profile.mainHandler__.Selection;
import ru.komiss77.OstrovDB;
import ru.komiss77.utils.ItemUtils;
import ru.komiss77.version.AnvilGUI;
import ru.komiss77.version.VM;





class settingsHandler__ {
        
    
    static void injectPassportItems(final Player p, final Oplayer op, final boolean update_inventory) {
      /*  
        for (E_Pass pass_icon : E_Pass.values()) {   //обновлять на надо - обновится в mainHandler после возврата

            String res=op.getBungeeData(Data.fromName(pass_icon.toString()));
            if (res.isEmpty()) res = pass_icon.default_value;
            
            int value=0;
            if (Ostrov.isInteger(res)) value = Integer.valueOf(res);
            
                switch (pass_icon) {
                    case USER_GROUPS: 
                        for (String gr_:res.split(",")) {
                            if (OstrovDB.groups.containsKey(gr_) ) res=res.replace(gr_, " §3"+OstrovDB.groups.get(gr_).chat_name);
                        }                        
                        break;
                    case REG_TIME: 
                        res = ApiOstrov.dateFromStamp(value);
                        break;
                    case PLAY_TIME:
                        res = ApiOstrov.secondToTime(value);
                        break;
                    case РЕПУТАЦИЯ:
                        res = (value<0?"§4":(value>0?"§2":"§f"))+res;
                        break;
                    case КАРМА:
                        res = (value<0?"§4":(value>0?"§2":"§f"))+res;
                        break;
                }
            
            op.profile.setItem(pass_icon.slot, getItem(pass_icon, res, Selection.ПРОСМОТР_ПАСПОРТА.toString()));
        }
        if(update_inventory) p.updateInventory();*/
    }






    public static void onFieldClick(final InventoryClickEvent e, final Player p, final Oplayer op) {
//System.out.println("onFieldClick 1");
        //if (e.isShiftClick()) ;
        //else if (e.isLeftClick()) ;
        //else if (e.isRightClick()) ;
   /*     if (VM.getNmsNbtUtil().hasString(e.getCurrentItem(), "ostrov_profile_selection")) {
            Selection selection=Selection.fromString(VM.getNmsNbtUtil().getString(e.getCurrentItem(), "ostrov_profile_selection"));
//System.out.println("onFieldClick ostrov_profile_selection selection="+selection);
            if (selection==null) return;
//System.out.println("-passport selection="+selection.toString());
            
                switch (selection) {
                    
                    
                    case ПРОСМОТР_ПАСПОРТА:
                        if (e.isShiftClick()) {
                            p.performCommand("passport get");
                            return;
                        }
                        final E_Pass pass_icon = fromItem(e.getCurrentItem());
                        if (pass_icon==null) return;
                        final Data data_type = Data.fromName(pass_icon.toString());
//System.out.println("-pass_icon="+pass_icon.toString());
                        if (!pass_icon.editable) {
                            p.sendMessage(Ostrov.prefix+"§cЭтот параметр неизменяемый!");
                            return;
                        }
                        String current = op.getBungeeData(data_type);
                        if (current.isEmpty()) current=pass_icon.default_value;
                        
                            switch (pass_icon) {
                                case ПОЛ:
                                    op.setData( data_type, Sex.next(op.getBungeeData(data_type)) );
                                    break;  //break - вызвать обновление иконки (ниже)
                                default:
                                    anvillInput(p, op, pass_icon, current);
                                    return;
                            }
                        injectPassportItems(p, op, true);
                        break;
                            
                        
                    case ПРОСМОТР_СТАТИСТИКИ:
System.out.println("-ПРОСМОТР_СТАТИСТИКИ!!");
                        break;
                        
                         
                }
        }*/
        
     }

    
    
    /*
    
    private static void anvillInput(final Player p,  final Oplayer op, final E_Pass pass_icon, final String current) {
        
        AnvilGUI ag = new AnvilGUI( Ostrov.instance,  p, current, (player, reply) -> {
            
            if (reply.isEmpty() || reply.equals(pass_icon.default_value)) {
                p.sendMessage(Ostrov.prefix+"§сВы ничего не изменили");
                return "Введите значение!";
            }
            
            switch(pass_icon) {
                case РОДИЛСЯ:
                    if (!parseDate(reply)) {
                        p.sendMessage(Ostrov.prefix+"§сФормат ДД.ММ.ГГГГ");
                        return "Формат ДД.ММ.ГГГГ";
                    }
                    break;
                case ВКОНТАКТЕ:
                case ЮТУБ:
                case ТВИЧ:
                    if (!parseURL(reply)) {
                        p.sendMessage(Ostrov.prefix+"§сэто не ссылка! Надо что-то вроде http://ostrov77.ru/donate.html");
                        return "это не URL ссылка";
                    }
                    break;
                    
                case ТЕЛЕФОН:
                    final String onlyDidgits = reply.replaceAll("\\D+","");
                    if (onlyDidgits.length()!=10) {
                        p.sendMessage(Ostrov.prefix+"§сНомер телефона должен быть похож на (911)777-7777");
                        return "пример: (911)777-7777";
                    } else {
                        reply="("+onlyDidgits.substring(0,3)+") "+onlyDidgits.substring(3,6)+"-"+onlyDidgits.substring(6,10);
                    }
                    break;
                case МЫЛО:
                    if (!parseMail(reply)) {
                        p.sendMessage(Ostrov.prefix+"§сЭлектронная почта должна иметь вид "+player.getName()+"@ostrov77.ru");
                        return "пример: "+player.getName()+"@ostrov77.ru";
                    }
                    break;
            }
            final Data data = Data.fromName(pass_icon.toString());
            final boolean is_new_record=!op.getBungeeData(data).equals(pass_icon.default_value);
            
            op.setData(data, reply);
            injectPassportItems(p, op, false);
            
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        p.openInventory(op.profile);
                        p.playSound(p.getLocation(), Sound.BLOCK_COMPOSTER_EMPTY, 2, 2);

                    }
                }.runTaskLater(Ostrov.instance, 1);
                
            if (is_new_record) StatManager.calculateReputationBase(op);

            return null; //-закрыть
            //return "Incorrect.";
            
        });

    }*/

     
 
    
    










        

    public static ItemStack getItem(final E_Pass pass, final String current_value, final String selection){
        //if (value==ЗАКРЫТЬ)  return new ItemBuilder(Material.BARRIER).setName("§4Закрыть").setLore(ItemUtils.Gen_lore(null, value.lore, "")).build();
        ItemStack result=new ItemStack(Material.valueOf(pass.mat));
        result = VM.getNmsNbtUtil().addString(result, "ostrov_profile_selection", selection);
        result = VM.getNmsNbtUtil().addString(result, "ostrov_profile_passport", pass.toString());
        //net.minecraft.server.v1_14_R1.ItemStack nms_Item = CraftItemStack.asNMSCopy(result);
        //NBTTagCompound tag = new NBTTagCompound();
        //tag.setString("ostrov_profile_selection", selection);
        //tag.setString("ostrov_profile_passport", pass.toString());
        //nms_Item.setTag(tag);
        //result=CraftItemStack.asBukkitCopy(nms_Item);

        ItemMeta im = result.getItemMeta();
        im.setDisplayName("§6"+pass.item_name);
        im.setLore(ItemUtils.Gen_lore(null, current_value+"<br><br>"+pass.lore+(pass.editable?"<br>§7Левый клик - редактировать<br>§7Shift+клик - достать":""), "§7"));
        im.addItemFlags(ItemFlag.HIDE_ATTRIBUTES,ItemFlag.HIDE_UNBREAKABLE);
        result.setItemMeta(im);  
        return result;
        
    }
    
    
    
    
    public static boolean isIcon(final ItemStack is){
        if (is==null || is.getType()==Material.AIR ) return false;
        return VM.getNmsNbtUtil().hasString(is, "ostrov_profile_passport");
    }
    
    public static E_Pass fromItem (final ItemStack is){
//System.out.println("-E_Pass.fromItem() isIcon?"+isIcon(is));
        if (isIcon(is)) {
            final String type=VM.getNmsNbtUtil().getString(is, "ostrov_profile_passport");
//System.out.println("--E_Pass.fromItem() type="+type+"  exist?"+(exist(type)));
            if (E_Pass.exist(type)) return E_Pass.valueOf(type);
        }
        return null;
    }

    

















    
    
    
    private enum Sex {
        бесполоe, Мальчик, Девочка, Гермафродит;
        
        public static String next (final String current) {
            for (Sex sex:Sex.values()) {
                if ( sex.toString().equals(ChatColor.stripColor(current)) ) {
                    switch (sex) {
                        case бесполоe: return "§3"+Мальчик.toString();
                        case Мальчик: return "§d"+Девочка.toString();
                        case Девочка: return "§6"+Гермафродит.toString();
                        //case Гермафродит: return бесполоe.toString();
                    }
                }
            }
        return бесполоe.toString();
        }
        
    }
    
    


















    private static boolean parseDate(final String input) {
        //if (!input.matches("\\d{2}.\\d{2}.\\d{4}")) return false;
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
        dateFormat.setLenient(false);
        try {
            Date test_date = dateFormat.parse(input);
//System.out.println("input="+input+" date="+test_date+" test_date.format="+dateFormat.format(test_date) );
            return dateFormat.format(test_date).equals(input);
        } catch (ParseException  ex) {
            return false;
        }
        //return true;
        //final String datePattern = "\\d{2}-\\d{2}-\\d{4}";
        //return input.matches(datePattern);
    }
    
    public static boolean parseURL(final String url) {  
        URL u = null;
        try {  
            u = new URL(url);  
        } catch (MalformedURLException e) {  
            return false;  
        }
        try {  
            u.toURI();  
        } catch (URISyntaxException e) {  
            return false;  
        }  
        return true;  
    } 
    
    
    public static boolean parseMail(final String email) {  
        //Matcher matcher = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE) .matcher(email);
        Matcher matcher = Pattern.compile("^([\\w-\\.]+){1,64}@([\\w&&[^_]]+){2,255}.[a-z]{2,}$", Pattern.CASE_INSENSITIVE) .matcher(email);
        return matcher.find();
    } 

    
    
    
    
    
    
    
    
}
