package ru.komiss77.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.komiss77.enums.Operation;
import ru.komiss77.listener.SpigotChanellMsg;




public class SeenCmd implements CommandExecutor {


    
            //запрос банжи, если есть - разкодировать raw
            //если пустой - выкачать из снапшота БД
    
    @Override
    public boolean onCommand(CommandSender cs, Command cmd, String label, String[] args) {
       // if (sender.hasPermission("ostrov.pinfo")) {
       
        if ( ! (cs instanceof Player) ) {
            cs.sendMessage("§cНе консольная команда!");
            return true;
        }
        
        final Player sender=(Player) cs;       
        
        if (args.length == 1) {
            
            String targetName = args[0];
            
            SpigotChanellMsg.sendMessage(sender, Operation.REQUEST_PLAYER_DATA, sender.getName(), targetName);
                
            
            
            
            

        /*    sender.sendMessage( "§6Информация по §b"+offline_player.getName()+" §6от сервера §b"+Bukkit.getMotd() +  ((offline_player.isOnline())? " §2- сейчас на сервере!":" §4- сейчас офф.") );

            sender.sendMessage( "§7Первый вход: §f"+ApiOstrov.dateFromStamp(offline_player.getFirstPlayed())+"§7, Последний выход: §f"+( sender.hasPermission("ostrov.seen.full")? ApiOstrov.dateFromStamp(offline_player.getLastPlayed()) : "****" ) );
            
            if (PM.exist(args[0])) {
                final Oplayer op = PM.getOplayer(args[0]);
                sender.sendMessage( "§7Группы: §6"+  op.chat_group );
                sender.sendMessage ( "§7Режим: §6"+op.getPlayer().getGameMode().toString()+"§5, Здоровье: §3"+((int)op.getPlayer().getHealth())+"§5, Уровень: §3"+((int)op.getPlayer().getLevel())  );             
                //sender.sendMessage ( "§5Блоков поставлено: §6"+op.Getbplace()+ "§5, Блоков сломано: §6"+op.Getbbreak() );
                //sender.sendMessage ( "§5Убил игроков/монстров/мобов: §6"+op.Getpkill()+"/"+op.Getmonsterkill()+"/"+op.Getmobkill()+"§5, Погиб: §6"+op.Getbdead() );
               
            } else {

                if (!Ostrov.powerNBT) {
                    sender.sendMessage( "Для просмотра оффлайн-игроков нужен плагин PowerNBT !");
                    return true;
                }
                
                //OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[0]);
                
                //NBTCompound nbt = PowerNBT.getApi().readOfflinePlayer(offline_player);
                final Player online = VM.getNmsServer().getOfflinePlayer( args[0], offline_player.getUniqueId(), ((Player)sender).getLocation() );
                    sender.sendMessage ( "§7Режим: §6"+online.getGameMode().toString()+"§5, Здоровье: §3"+online.getHealth()+"§5, Уровень: §3"+online.getLevel()  );             
                    sender.sendMessage ( "§7Координаты выхода §6"+online.getWorld().getName()+", "+ (sender.hasPermission("ostrov.seen.full")? online.getLocation().getBlockX()+" x "+online.getLocation().getBlockY()+" x "+online.getLocation().getBlockZ() : "(нет права просмотра)") );
                
              /*  try {
                    sender.sendMessage ( "§7Режим: §6"+EnumGamemode.getById(nbt.getInt("playerGameType")).toString()+"§5, Здоровье: §3"+((int)nbt.getFloat("Health"))+"§5, Уровень: §3"+((int)nbt.getInt("Level"))  );             

                    NBTList pos = nbt.getList("Pos");
                    sender.sendMessage ( "§7Координаты выхода §6"+nbt.getString("SpawnWorld")+", "+ (sender.hasPermission("ostrov.seen.full")? pos.get(0).toString().substring(0, pos.get(0).toString().indexOf("."))+" x "+pos.get(1).toString().substring(0, pos.get(1).toString().indexOf("."))+" x "+pos.get(2).toString().substring(0, pos.get(2).toString().indexOf(".")) : "(нет права просмотра)") );

                } catch (Exception exception) {
                    sender.sendMessage( "§cОшибка! (" + exception + ")");
                    return true;
                }
                    

            }*/
            
        }
        return true;
    }
    

    
    public static void onResult(final Player sender, final int status, final String raw) {
        if (status==1) {
            sender.sendMessage("получен массив для : "+raw);
        } else {
            sender.sendMessage(" оффлайн, выкачать из снапшота БД - недоделано");
        }
    }

    
    
    


}
