package ru.komiss77;



import java.util.Iterator;
import me.clip.deluxechat.events.DeluxeChatEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.Plugin;





public class ChatListenerExample implements Listener  {
    private static Plugin plugin;
    
    
    public ChatListenerExample(final Plugin plugin) {
        this.plugin=plugin;
    }
    
    
    /*
    @EventHandler
    public void onPlayerChatEvent(final AsyncPlayerChatEvent asyncPlayerChatEvent) {
        final Player player = asyncPlayerChatEvent.getPlayer();
        if (Kitbattle.playerData.keySet().contains(player.getName()) && Config.ShowRankInChat) {
            if (asyncPlayerChatEvent.getMessage().contains("%")) {
                player.sendMessage(kb + Messages.CantUseCharacter);
                asyncPlayerChatEvent.setCancelled(true);
                return;
            }
            asyncPlayerChatEvent.setFormat(String.valueOf(Config.RanksPrefix.replace("%rank%", Kitbattle.playerData.get(player.getName()).getRank().getName())) + asyncPlayerChatEvent.getFormat());
        }
    }*/
    
    @EventHandler(priority = EventPriority.HIGHEST,ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent e) {
//System.out.println("---AsyncPlayerChatEvent sender="+e.getPlayer().getName()+" msg="+e.getMessage()+" reciep="+e.getRecipients());

        final Player p = e.getPlayer();
        Player recipient;
        Iterator<Player> recipients;
        
        //разделяем по мирам - делюксчат не пропускает глобальный, но если в игре и кто-то зашел в лобби, то пишет
        recipients = e.getRecipients().iterator();
        while (recipients.hasNext()) {
            recipient = recipients.next(); //если получатель в другом мире, ему не отправляем
            if ( !recipient.getWorld().getName().equalsIgnoreCase(p.getWorld().getName()) ) {
                recipients.remove();
            }
        }
            
        if (p.getWorld().getName().equalsIgnoreCase("lobby")) return; //если в лобби - на обработку делюксчата
        
        
        if ( p.getGameMode() == GameMode.SPECTATOR ) {  //если пишет зритель, получают все игроки в мире
            e.setFormat("§8[Зритель] %1$s §f§o≫§f %2$s");
            return;
        }
        
        
      /*  if (Kitbattle.playerData.containsKey(p.getName())) {
            final PlayerData pd = Kitbattle.playerData.get(p.getName());
//System.out.println("message.startsWith !");            
            TextComponent msg = new TextComponent( "§8[§6"+pd.getRank().getName()+ "§8] §f"+p.getName()+" §7≫ §f"+e.getMessage() );
            HoverEvent he = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§7Сообщение").create());
            msg.setHoverEvent( he );
            
            for (Player pl: e.getRecipients()) {
//System.out.println("---> ! "+pl.getName()+" [Всем] "+e.getMessage());            
                pl.spigot().sendMessage(msg);
            }
            e.getRecipients().clear();

        }*/
        
        
        
        
        
        
        
        
        
        
        
      /*  final Arena arena = BedwarsAPI.getArena(p);
//System.out.println("---2 arena="+arena);
        if (arena == null || arena.GetStatus()!=ArenaStatus.Running ) return; //арена не выбрана или не игра - делюксчат подставляет команду вместо префикса
        
//System.out.println("---2 getSpectators="+arena.getSpectators());
        
        final Team team = arena.GetPlayerTeam(p);

        if (team == null) return; //нет команда - хз что делать? скорее всего это зритель
        
        TextComponent msg;
        HoverEvent he;
        
        if ( e.getMessage().startsWith("!") ) { //если всем
//System.out.println("message.startsWith !");            
            msg = new TextComponent( "§8[§fВсем§8] "+team.getChatColor()+"§8["+team.getName()+ "§8] §f"+p.getName()+" §o"+team.getChatColor()+"≫ §f"+e.getMessage().replaceFirst("!", "") );
            he = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§7Сообщение всем командам").create());
            msg.setHoverEvent( he );
            
            for (Player pl: e.getRecipients()) {
//System.out.println("---> ! "+pl.getName()+" [Всем] "+e.getMessage());            
                pl.spigot().sendMessage(msg);
            }
            e.getRecipients().clear();

        } else { //только в команде
            
            Arena recipientArena;
            Team recipientTeam;
            
            msg = new TextComponent( "§f"+p.getName()+"§o"+team.getChatColor()+"≫ §f"+e.getMessage());
            he = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(
                    "§7Сообщение видно только вашей команде.\n"
                    + "§7Чтобы сказать всем командам,\n"
                    + "§7в начале сообщения добавьте !"
            ).create());
            msg.setHoverEvent( he );
            //msg.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/ " ) );
            
            for (Player pl: e.getRecipients()) {
                recipientArena =BedwarsAPI.getArena(pl);
                    if (recipientArena!=null) {
                        recipientTeam = recipientArena.GetPlayerTeam(pl);
                            if (recipientTeam!=null && recipientTeam.getName().equalsIgnoreCase(team.getName())) {
                            //e.setFormat ( "§f%1$s §o"+team.getChatColor()+"-> §f%2$s" );
                            //message = message.trim()+"§8 !-всем";
                            //e.setMessage(message);
                                pl.spigot().sendMessage(msg);
                            }
                            
                    }
            }
            e.getRecipients().clear();

        }*/
        
    }    
    
    
    
    

    


//по входящим с банжи
    //после входящего PluginMessageReceived сообщение отправляется в sendBungeeChat (CompatibilityManager)
    //в sendBungeeChat по какому-то флагу либо сразу рассылается, либо рассылается тем, у кого не локальный
    //переключение локальный/глобальный: e.getPlayer().getUniqueId().toString()
    /*
        public boolean setLocal(final String s) {
        if (DeluxeChat.localPlayers == null) {
            (DeluxeChat.localPlayers = new ArrayList<String>()).add(s);
            return true;
        }
        if (DeluxeChat.localPlayers.contains(s)) {
            return false;
        }
        DeluxeChat.localPlayers.add(s);
        return true;
    }
    
    public boolean setGlobal(final String s) {
        if (DeluxeChat.localPlayers == null) {
            DeluxeChat.localPlayers = new ArrayList<String>();
            return false;
        }
        if (!DeluxeChat.localPlayers.contains(s)) {
            return false;
        }
        DeluxeChat.localPlayers.remove(s);
        return true;
    }
    
    public static boolean isLocal(final String s) {
        return DeluxeChat.localPlayers != null && DeluxeChat.localPlayers.contains(s);
    }

    */

    
    

    
    
    
    
    
    
    
    
    
    
    
    
    
    
    //обработка AsyncPlayerChatEvent (исходящих от игрока) этапы 1,2,3
    
    // 1
    //dchat получает AsyncPlayerChatEvent и создаёт DeluxeChatEvent, отмена делает return из AsyncPlayerChatEvent
    //можно играть getRecipients
    @EventHandler 
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

      //  if (Kitbattle.playerData.containsKey(p.getName())) {
      //      final PlayerData pd = Kitbattle.playerData.get(p.getName());
//System.out.println("message.startsWith !");            
      //      e.getDeluxeFormat().setPrefix( "§7<§6"+pd.getRank().getName()+"§7> §7" );

       // }
        
        
        //далее определяем команду для отображения в чате лобби
      /*  final Arena arena = BedwarsAPI.getArena(p);
        if (arena!=null) {
            //далее - уже на арене
            final Team team = arena.GetPlayerTeam(p);
            if (team==null) {       //команда еще не выбрана
                e.getDeluxeFormat().setPrefix("§8<команда?> §7");
            } else {//команда уже выбрана
                e.getDeluxeFormat().setPrefix( team.getChatColor()+"<"+team.getName()+"> §7" );
            }
            */
            
        }
        
        
    }
  
    
    
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
    
   
    
    
    
    
    
    
