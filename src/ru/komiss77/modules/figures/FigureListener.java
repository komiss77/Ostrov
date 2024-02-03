package ru.komiss77.modules.figures;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.Merchant;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Ostrov;
import ru.komiss77.Timer;
import ru.komiss77.enums.ServerType;
import ru.komiss77.events.FigureClickEvent;
import ru.komiss77.events.GameInfoLoadEvent;
import ru.komiss77.events.GameInfoUpdateEvent;
import ru.komiss77.modules.games.GM;
import ru.komiss77.modules.games.GameInfo;
import ru.komiss77.modules.player.PM;
import ru.komiss77.objects.Figure;
import ru.komiss77.objects.Figure.FigureType;
import ru.komiss77.utils.ItemUtils;
import ru.komiss77.utils.inventory.ConfirmationGUI;
import ru.komiss77.utils.inventory.SmartInventory;


public class FigureListener implements Listener{
    
    
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onGameInfoLoadEvent ( final GameInfoLoadEvent e ) {
        GameInfo gi; 
        for (final Figure fig : FigureManager.getFigures()) {
//System.out.println("type"+fig.type+" game="+fig.game);
            if (fig.type!=FigureType.SERVER || fig.game==null) continue;
            gi = GM.getGameInfo(fig.game);
            if (gi==null) {
//Bukkit.broadcastMessage("onGameInfoLoadEvent gi==null "+fig.getGame());        
                fig.setDisplayName(fig.game.displayName+" §4[§cВыключен§4]");
            } else {
//Bukkit.broadcastMessage("onGameInfoLoadEvent online= "+gi.getOnline()+" "+fig.getGame());        
                if (gi.getOnline()>=0) {
                    fig.setDisplayName(fig.game.displayName+" §7[§f"+(gi.getOnline()==0?"никого нет":gi.getOnline())+"§7]");
                } else {
                    fig.setDisplayName(fig.game.displayName+" §4[§cВыключен§4]");
                }
                //fig.update(gi.getOnline()>=0 ? GameState.РАБОТАЕТ : GameState.ВЫКЛЮЧЕНА, gi.getOnline());
            }
        }
    }   
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onGameInfoUpdateEvent ( final GameInfoUpdateEvent e ) {
        //FigureManager.updateEvent(e.type,e.online);
        FigureManager.getFigures().forEach( (fig) -> {
            if (fig.game==e.getGame()) {
                //Ostrov.sync( () -> fig.update(e.online), 0);
//Bukkit.broadcastMessage("onGameInfoUpdateEvent "+e.getGame()+" : "+e.getOnline());        
//Bukkit.broadcastMessage("onGameInfoUpdateEvent online= "+e.getOnline()+" "+fig.getGame());        
                if (e.getOnline()>=0) {
                    fig.setDisplayName(fig.game.displayName+" §7[§f"+(e.getOnline()==0?"никого нет":e.getOnline())+"§7]");
                } else {
                    fig.setDisplayName(fig.game.displayName+" §4[§cВыключен§4]");
                }
                //fig.update(e.getState(), e.getOnline());
            }
        });
    }
    
    
    
    
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = false)
    public void onHit ( EntityDamageByEntityEvent e ) {
//System.out.println("EntityDamageByEntityEvent "+e.getDamager()+" getEntity "+e.getEntity());
        if (FigureManager.isFigure(e.getEntity())) {
            e.setCancelled(true);
            if (e.getDamager().getType()!=EntityType.PLAYER) return;
            
            final Player p = (Player) e.getDamager();
            if (Timer.has(p, "figure")) { //спамит 2 раза подряд
                return;
            }
            Timer.add(p, "figure", 1);
            Figure figure = FigureManager.getFigure(e.getEntity());
            if (figure==null) return; //обработчик могли удалить
            
            if (figure.type==null) {
                Ostrov.log_err("onRightClick type==null : "+figure.figureId);
            }
                

            switch (figure.type) {

                case COMMAND_CONFIRM:
                    ConfirmationGUI.open( p, "§4Вы согласны?", result -> {
                        p.closeInventory();
                        if (result) {
                            runCmd(p, figure.leftclickcommand);
                        }
                    });
                    break;

                case COMMAND:
                    runCmd(p, figure.leftclickcommand);
                    SpeachManager.animate(p, figure);
    //System.out.println( "rightclickcommand="+rightclickcommand.replaceAll("%player%",p.getName()) );
                    break;

                case EVENT:
                    if (figure.entity!=null) { //подгружается не сразу
                        final FigureClickEvent figureEvent = new FigureClickEvent ( p, figure, true);
                        Bukkit.getPluginManager().callEvent(figureEvent );
                        if (figureEvent.getAnswer()==null) {
                            SpeachManager.animate(p, figure);
                        } else {
                            SpeachManager.onAnswer(figureEvent.getAnswer());
                        }      
                    }
                    break;

                case SERVER: //лкм - меню арен
                    final GameInfo gi=GM.getGameInfo(figure.game);
                    if(gi==null) {
                        p.sendMessage(Ostrov.PREFIX+" §cИгра недоступна!");
                        return;
                    }   
                    if (gi.game.type==ServerType.ONE_GAME) {
                        p.closeInventory();
                        p.performCommand("server "+gi.getServername());
                    } else {
                        PM.getOplayer(p).menu.openArenaMenu(p, figure.game);
                    }
                    break;
            }                         
        }
    }   
    
    
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onMount (final EntityMountEvent e) {
//Ostrov.log("onMount isFigure="+FigureManager.isFigure(e.getMount()));
        e.setCancelled(FigureManager.isFigure(e.getMount()));
    }
    
    
    
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = false)
    public void onRighClick ( final PlayerInteractAtEntityEvent e ) {
//System.out.println("PlayerInteractAtEntityEvent 1");
        
        final Figure figure = FigureManager.getFigure(e.getRightClicked());
//Ostrov.log("Interact figure="+figure);
        if (ItemUtils.compareItem(FigureManager.stick, e.getPlayer().getInventory().getItemInMainHand(), false) ) { //если тыкаем палкой
            e.setCancelled(true);
            if ( !ApiOstrov.isLocalBuilder(e.getPlayer(), true) ) return;
//System.out.println("onRighClick 2 fig="+fig);
            
            if (figure==null) { //проверять - в 1 блоке не делать две!
                
                Figure found = null;
                for (final Figure f : FigureManager.getFigures()) {
                    if (f.isInSameBlock(e.getRightClicked().getLocation())) {
                        found = f;
                        break;
                    }
                    if (found!=null) {
                        e.getPlayer().sendMessage("§eВ этом блоке уже есть фигура с ИД "+f.figureId+"!");
                        return;
                    }
                }
                final Figure newFigure = new Figure(e.getRightClicked()); //создаём временную для передачи в билдера. Если тип не установить - не сохранится.
                if (e.getRightClicked() instanceof Merchant) {
                    Ostrov.sync(()->SmartInventory.builder().id("TypeSelectMenu"+e.getPlayer().getName()). provider(new TypeSelectMenu(newFigure)). size(6, 9). title("§fВыбор типа").build() .open(e.getPlayer()), 1);
                } else {
                    SmartInventory.builder().id("TypeSelectMenu"+e.getPlayer().getName()). provider(new TypeSelectMenu(newFigure)). size(6, 9). title("§fВыбор типа").build() .open(e.getPlayer());
                }
                
            } else {
//System.out.println("open  FigureMenu");
                if (e.getRightClicked() instanceof Merchant) {
                    //final Figure fg = figure;
                    Ostrov.sync(()->SmartInventory.builder().id("FigureMenu"+e.getPlayer().getName()). provider(new MenuSetup(figure)). size(6, 9). title("§fНастройка фигуры").build() .open(e.getPlayer()), 1);
                } else {
                    SmartInventory.builder().id("FigureMenu"+e.getPlayer().getName()). provider(new MenuSetup(figure)). size(6, 9). title("§fНастройка фигуры").build() .open(e.getPlayer());
                }
            
            }
            
            
        } else if (figure!=null) { //просто ПКМ
            
            e.setCancelled(true);

            if (Timer.has(e.getPlayer(), "figure")) { //спамит 2 раза подряд
                return;
            }
            Timer.add(e.getPlayer(), "figure", 1);
            final Player p = e.getPlayer();

            if (figure.type==null) {
                Ostrov.log_err("onRightClick type==null : "+figure.figureId);
            }

            
            switch (figure.type) {

                case COMMAND_CONFIRM:
                    ConfirmationGUI.open( p, "§4Вы согласны?", result -> {
                        p.closeInventory();
                        if (result) {
                            runCmd(p, figure.rightclickcommand);
                        }
                    });
    //System.out.println( "rightclickcommand="+rightclickcommand.replaceAll("%player%",p.getName()) );
                    break;

                case COMMAND:
                    runCmd(p, figure.rightclickcommand);
                    SpeachManager.animate(p, figure);
    //System.out.println( "rightclickcommand="+rightclickcommand.replaceAll("%player%",p.getName()) );
                    break;

                case EVENT:
                    if (figure.entity!=null) { //подгружается не сразу
                        final FigureClickEvent figureEvent = new FigureClickEvent ( p, figure, false);
                        Bukkit.getPluginManager().callEvent(figureEvent);
                        if (figureEvent.getAnswer()==null) {
                            SpeachManager.animate(p, figure);
                        } else {
                            SpeachManager.onAnswer(figureEvent.getAnswer());
                        }                   
                    }
                    break;

                case SERVER: //пкм - просто перейти на серв
                    final GameInfo gi=GM.getGameInfo(figure.game);
                    if(gi==null) {
                        p.sendMessage(Ostrov.PREFIX+" §cИгра недоступна!");
                        return;
                    }   
                    p.performCommand("server "+gi.getServername());
                    break;
            }
            
        }
        
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    private void runCmd(final Player p, final String rawCmd) {
        if (rawCmd.startsWith("@c")) {
            final String cmd = rawCmd.substring(2).trim().replaceAll("@p",p.getName()).replaceAll("@c","");
//System.out.println( "rightclickcommand="+rightclickcommand+" cmd="+cmd);
//System.out.println( " cmd="+cmd);
            if (cmd.startsWith("bossbar ") || cmd.startsWith("op ")) {
                p.sendMessage("ага, счас");//Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), "ba" );
            } else if (cmd.startsWith("say ")) {
                p.sendMessage(cmd.replaceFirst("say ", ""));
            } else{
                Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), cmd);
            }
        } else {
            p.performCommand( rawCmd.replaceAll("@p", p.getName()).trim());
        }
    }    
    
    
    
    
    
    
    
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPotion(final EntityPotionEffectEvent e) {
        if (FigureManager.isFigure(e.getEntity())) {
            e.setCancelled(true);
        }
    }
    
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true) 
    public void onPower ( CreeperPowerEvent e ) {
        if (FigureManager.isFigure(e.getEntity())) {
            e.setCancelled(true);
        }
    }
    
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onCombust ( EntityCombustEvent e ) {
        if (FigureManager.isFigure(e.getEntity())) {
            e.setCancelled(true);
        }
    }
    
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onTame ( EntityTameEvent e ) {
        if (FigureManager.isFigure(e.getEntity())) {
            e.setCancelled(true);
        }
    }
    
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onLeash ( PlayerLeashEntityEvent e ) {
        if (FigureManager.isFigure(e.getEntity())) {
            e.setCancelled(true);
        }
    }
        
    
    
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true) //
    public void onTradeOpen ( InventoryOpenEvent e ) {
        if (e.getInventory().getType()==InventoryType.MERCHANT && e.getInventory().getHolder()!=null 
        		&& FigureManager.isFigure(((Entity) e.getInventory().getHolder())) ) {
            e.setCancelled(true);
        }
    }
    
    
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true) //не даём дамажить
    public void onTarget ( EntityTargetLivingEntityEvent e ) {
        if (e.getTarget()!=null && FigureManager.isFigure(e.getTarget())) {
            e.setCancelled(true);
            e.setTarget(null);
        }
    }
    

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true) //не даём дамажить
    public void onDamage ( EntityDamageEvent e ) {
//System.out.println("EntityDamageEvent getEntity "+e.getEntity());
        if (FigureManager.isFigure(e.getEntity())) {
            e.setCancelled(true);
            e.setDamage(0);
        }
    }
    
    
    

    
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = false)
    public void onTp ( final EntityTeleportEvent e ) {
        //final Figure fig = FigureManager.getFigure(e.getEntity());
//System.out.println("-onLeftClick fig="+fig);   
        Figure figure = FigureManager.getFigure(e.getEntity());
//System.out.println(figure==null ? "figure==null" : "EntityTeleportEvent  figure="+figure.getName()+" allow?"+figure.allowTp);
        if (figure==null || figure.allowTp) return; //обработчик могли удалить
        final Location loc = figure.getSpawnLocation();
        if (loc!=null) e.setTo(loc);
            //e.setCancelled(true);
    }
    


    
    
    
  
    
    
}
