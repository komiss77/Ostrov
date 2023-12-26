package ru.komiss77.commands;


import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import com.google.common.collect.ImmutableList;
import java.util.UUID;
import ru.komiss77.LocalDB;
import ru.komiss77.Ostrov;
import ru.komiss77.Timer;
import ru.komiss77.hook.DynmapHook;
import ru.komiss77.objects.CaseInsensitiveSet;
import ru.komiss77.hook.WGhook;



public class CleanCmd implements CommandExecutor, TabCompleter {
    
    
    @Override
    public List<String> onTabComplete(CommandSender cs, Command cmnd, String command, String[] arg) {
        //final List <String> sugg = new ArrayList<>();
//System.out.println("l="+strings.length+" 0="+strings[0]);
        switch (arg.length) {
        case 1:
            //0- пустой (то,что уже введено)
            break;
        case 2:
            //1-то,что вводится (обновляется после каждой буквы
            break;
        }
        
       return ImmutableList.of();
    }

    @Override
    public boolean onCommand(CommandSender cs, Command cmd, String string, String[] arg) {
        
        if (!cs.isOp()) {
            cs.sendMessage( "§сДоступна только операторам!");
            return true;
        }
        
        if (arg.length==2 && arg[0].equals("dynmap")) {
            if (Ostrov.dynmap) {
                DynmapHook.purge(arg[1]);
            } else {
                Ostrov.log_warn("Dynmap нет в плагинах!");
            }
            return true;
        }
        
        
        
        if (LocalDB.getConnection()==null) {
            cs.sendMessage( "§cНет соединения с БД!");
            return true;
        }
        if (!LocalDB.useLocalData) {
            cs.sendMessage( "§cЭтот сервер не сохраняет данные!");
            return true;
        }
        if (Timer.has("clean".hashCode())) {
            cs.sendMessage( "§6Очистка уже запущена...");
            return true;
        }
        Timer.add("clean".hashCode(), 10);
        
        final int currentTime = Timer.getTime();
        final int threeMonthLater = Timer.getTime()-3*30*24*60*60;
//cs.sendMessage(new TextComponent("три_месяца_назад="+threeMonthLater));
        
        if (threeMonthLater>System.currentTimeMillis()/1000) {
            cs.sendMessage("три_месяца_назад недопустимо - больше currentTimeMillis!");  
            return true;
        }

        if (threeMonthLater<=0) {
            cs.sendMessage("три_месяца_назад недопустимо - <=0 !");
            return true;
        }
        
        
        Collection<String> validUsers=new CaseInsensitiveSet();
        Map<UUID,String> validUuids=new HashMap<>(); //uuid,name
        
        Set<Integer> id_to_del=new HashSet<>();
        //Set<String> name_to_del=new HashSet<>();
            
            
        Ostrov.async(()-> {
            
        boolean mysqlError = true; //при ошибке sql validId будет пустой, снесёт всё!!
        try {

            PreparedStatement prepStmt = LocalDB.getConnection().prepareStatement("DELETE FROM `playerData` WHERE `lastActivity`<'"+threeMonthLater+"' AND `validTo`<'"+currentTime+"' ;" );
            prepStmt.executeUpdate();
            prepStmt.close();

            //загрузка оставшихся ников
            Statement stmt = LocalDB.getConnection().createStatement();
            ResultSet rs = stmt.executeQuery( "SELECT `name`,`uuid` FROM playerData ;" );
            while (rs.next()) {
                validUsers.add(rs.getString("name"));
                if (rs.getString("uuid").length()==36) {
                    validUuids.put(UUID.fromString(rs.getString("uuid")),rs.getString("name"));
                }
            } 
            rs.close();

cs.sendMessage("§eИз локальной БД удалены не заходившие более 3мес. и §fvalidTo §eменьше текущей даты.");
cs.sendMessage("§aосталось в базе ников: §f"+validUsers.size()+"§a, uuid: §f"+validUuids.size());





            rs = stmt.executeQuery( "SELECT `id`,`name` FROM `moneyOffline` ;" );
            while (rs.next()) {
                if (!validUsers.contains(rs.getString("name"))) {
                    id_to_del.add(rs.getInt("id"));
                }
            } 
            rs.close();
            for (int id:id_to_del) {
                prepStmt = LocalDB.getConnection().prepareStatement("DELETE FROM `moneyOffline` WHERE `id`="+id );
                prepStmt.executeUpdate();
            }
cs.sendMessage("§e moneyOffline - удалено:"+id_to_del.size());
            id_to_del.clear();




            mysqlError = false;


            if (stmt!=null) stmt.close();


        } catch (SQLException e) { 

            Ostrov.log_err("§с clean 1 - "+e.getMessage());

        }
//System.out.println("mysqlError="+mysqlError); 
        
        if (mysqlError) return;
            

        
        
            File dataDir = new File (Bukkit.getWorldContainer().getPath()+File.separator+Bukkit.getWorlds().get(0).getName()+ File.separator+"playerdata");
//Ostrov.log_warn("getPath="+Bukkit.getWorldContainer().getPath());
//Ostrov.log_warn("getAbsolutePath="+Bukkit.getWorldContainer().getAbsolutePath());
//Ostrov.log_warn("dataDir="+dataDir);
            if (dataDir.isDirectory()) {
                int dot;
                UUID uuid;

                File[] files = dataDir.listFiles();
                File pdFile;
                int count = 0;
                
                for (int i = 0; i < files.length; i++) {//for (File f : dataDir.listFiles()) {
                    pdFile=files[i];
                    dot = pdFile.getName().indexOf(".");
                    if (dot>0) {
                        uuid = UUID.fromString(pdFile.getName().substring(0, dot));
                        if (!validUuids.containsKey(uuid)) { //uuid удалён из базы
                            pdFile.delete();
//lastModify = (int) (Files.getLastModifiedTime(f.toPath()).toMillis()/1000);
                            count++;
                        }
                    }
                }
cs.sendMessage("§e playerDataFile - удалено:"+count);
            }        
            
            
            dataDir = new File (Bukkit.getWorldContainer().getPath()+File.separator+Bukkit.getWorlds().get(0).getName()+ File.separator+"advancements");
//Ostrov.log_warn("getPath="+Bukkit.getWorldContainer().getPath());
//Ostrov.log_warn("getAbsolutePath="+Bukkit.getWorldContainer().getAbsolutePath());
//Ostrov.log_warn("dataDir="+dataDir);
            if (dataDir.isDirectory()) {
                int dot;
                UUID uuid;

                File[] files = dataDir.listFiles();
                File pdFile;
                int count = 0;
                
                for (int i = 0; i < files.length; i++) {//for (File f : dataDir.listFiles()) {
                    pdFile=files[i];
                    dot = pdFile.getName().indexOf(".");
                    if (dot>0) {
                        uuid = UUID.fromString(pdFile.getName().substring(0, dot));
                        if (!validUuids.containsKey(uuid)) { //uuid удалён из базы
                            pdFile.delete();
                            count++;
                        }
                    }
                }
cs.sendMessage("§e advancements - удалено:"+count);
            }              
            
            
            dataDir = new File (Bukkit.getWorldContainer().getPath()+File.separator+Bukkit.getWorlds().get(0).getName()+ File.separator+"stats");
//Ostrov.log_warn("getPath="+Bukkit.getWorldContainer().getPath());
//Ostrov.log_warn("getAbsolutePath="+Bukkit.getWorldContainer().getAbsolutePath());
//Ostrov.log_warn("dataDir="+dataDir);
            if (dataDir.isDirectory()) {
                int dot;
                UUID uuid;

                File[] files = dataDir.listFiles();
                File pdFile;
                int count = 0;
                
                for (int i = 0; i < files.length; i++) {//for (File f : dataDir.listFiles()) {
                    pdFile=files[i];
                    dot = pdFile.getName().indexOf(".");
                    if (dot>0) {
                        uuid = UUID.fromString(pdFile.getName().substring(0, dot));
                        if (!validUuids.containsKey(uuid)) { //uuid удалён из базы
                            //if (Bukkit.getPlayer(uuids.get(uuid))!=null) { //на всяк случай,вдруг онлайн
                           //     continue;
                            //}
                            pdFile.delete();
                            count++;
                        }
                    }
                }
cs.sendMessage("§e stats - удалено:"+count);
            }  



            
            if (Ostrov.wg) {
                final int deleted = WGhook.purgeDeadRegions(validUsers, validUuids.keySet());
cs.sendMessage("§e WG regions - удалено:"+deleted);
            }
            
            
            
            
            
            
            
        }, 20);
        
        
        
    
        return true;
        
    }

    
    





    
    
    
    
    
    
    
    
    
    

}
    
    
 
