package ru.komiss77;



import java.util.Iterator;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;






@SuppressWarnings("deprecation")
public class ChatListenerExample implements Listener {
    
    @EventHandler(priority = EventPriority.HIGHEST,ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent e) {
//System.out.println("---AsyncPlayerChatEvent sender="+e.getPlayer().getName()+" msg="+e.getMessage()+" reciep="+e.getRecipients());

        final Player sender = e.getPlayer();
        Player recipient;
        Iterator<Player> recipients;
        
        //разделяем по мирам - делюксчат не пропускает глобальный, но если в игре и кто-то зашел в лобби, то пишет
        recipients = e.getRecipients().iterator();
        while (recipients.hasNext()) {
            recipient = recipients.next(); //если получатель в другом мире, ему не отправляем
            if ( !recipient.getWorld().getName().equalsIgnoreCase(sender.getWorld().getName()) ) {
                recipients.remove();
            }
        }
            
        if (sender.getWorld().getName().equalsIgnoreCase("lobby")) return; //если в лобби - на обработку делюксчата
        
        
        if ( sender.getGameMode() == GameMode.SPECTATOR ) {  //если пишет зритель, получают все игроки в мире
            e.setFormat("§8[Зритель] %1$s §f§o≫§f %2$s");
            return;
        }
    }
    
    
    //обработка AsyncPlayerChatEvent (исходящих от игрока) этапы 1,2,3
    
    // 1
    //dchat получает AsyncPlayerChatEvent и создаёт DeluxeChatEvent, отмена делает return из AsyncPlayerChatEvent
    //можно играть getRecipients
 /*   @EventHandler 
    public void chat(DeluxeChatEvent e) {
        final Player p = e.getPlayer();
//System.out.println("1 DeluxeChatEvent name="+p.getName()+" local?"+DeluxeChat.isLocal(p.getUniqueId().toString())+" arena="+arena);

        if (!p.getWorld().getName().equalsIgnoreCase("lobby")) {
//System.out.println("2 DeluxeChatEvent cancel!!");
            e.setCancelled(true);
            return;
        }
        
        //разделяем по мирам - делюксчат срабатывает раньше
        Player recipient;
        Iterator<Player> recipients = e.getRecipients().iterator();
        while (recipients.hasNext()) {
            recipient = recipients.next(); //если получатель в другом мире, ему не отправляем
            if ( !recipient.getWorld().getName().equalsIgnoreCase(p.getWorld().getName()) ) {
                recipients.remove();
            }
        }
    }
    
   */ 
    // 2
    //после DeluxeChatEvent сообщение форматируется и рассылается локально тем, кто остался в deluxeChatEvent.getRecipients(). 
    //getJSONFormat() == null || getJSONChatMessage() == null || getJSONFormat().isEmpty(), return из AsyncPlayerChatEvent
    //после этого эвента локальные получатели удаляются
    //@EventHandler 
    //public void chat(DeluxeChatJSONEvent e) { 
//System.out.println("2 DeluxeChatJSONEvent");
    //}
    
    
    
    // 3
    //после рассылки локальным игрокам по списку getRecipients, результат этого эвента отправляется в банжи
    //getJSONFormat() == null || getChatMessage() == null || getJSONFormat().isEmpty() || getChatMessage().isEmpty())  return из AsyncPlayerChatEvent
    //@EventHandler 
    //public void chat(ChatToPlayerEvent e) { 
//System.out.println("3 ChatToPlayerEvent");
    //}
}
