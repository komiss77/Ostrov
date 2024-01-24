package ru.komiss77.listener;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Rotatable;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.block.sign.Side;
import org.bukkit.block.sign.SignSide;
import org.bukkit.command.CommandException;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import io.papermc.paper.event.player.PlayerItemFrameChangeEvent;
import java.util.WeakHashMap;
import net.kyori.adventure.text.Component;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Config;
import ru.komiss77.LocalDB;
import ru.komiss77.Ostrov;
import ru.komiss77.Timer;
import ru.komiss77.builder.menu.EntitySetup;
import ru.komiss77.commands.PassportCmd;
import ru.komiss77.commands.PvpCmd;
import ru.komiss77.enums.ServerType;
import ru.komiss77.events.BsignLocalArenaClick;
import ru.komiss77.events.FriendTeleportEvent;
import ru.komiss77.modules.games.GM;
import ru.komiss77.modules.games.GameSign;
import ru.komiss77.modules.games.GameSignEditor;
import ru.komiss77.modules.menuItem.MenuItem;
import ru.komiss77.modules.menuItem.MenuItemsManager;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;
import ru.komiss77.modules.world.WorldManager;
import ru.komiss77.objects.CaseInsensitiveMap;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.ItemUtils;
import ru.komiss77.utils.LocationUtil;
import ru.komiss77.utils.SignEditMenu;
import ru.komiss77.utils.TCUtils;
import ru.komiss77.utils.TeleportLoc;
import ru.komiss77.utils.inventory.ConfirmationGUI;
import ru.komiss77.utils.inventory.InputButton;
import ru.komiss77.utils.inventory.InputButton.InputType;
import ru.komiss77.utils.inventory.ItemClickData;
import ru.komiss77.utils.inventory.SlotPos;
import ru.komiss77.utils.inventory.SmartInventory;
import ru.komiss77.version.VM;


public class PlayerLst implements Listener {

    private static final CaseInsensitiveMap <String> bungeeDataCache;
    public static WeakHashMap<Player,List <Component>> signFrontCache;
    public static WeakHashMap<Player,List <Component>> signBackCache;
    public static ItemStack signEdit;
    public static ItemStack gameSignEdit;
    public static ItemStack passport;
    
    static {
        signFrontCache = new WeakHashMap<>(); 
        signBackCache = new WeakHashMap<>(); 
        bungeeDataCache = new CaseInsensitiveMap<>();

        signEdit = new ItemBuilder(Material.WARPED_SIGN)
            .name("§fПомошник по табличкам")
            .addLore("")
            .addLore("§7Клик по табличке.")
            .addLore("")
            .addLore("§7ЛКМ - редактировать")
            .addLore("§7Шифт+ЛКМ - сменить тип")
            .addLore("")
            .addLore("§7ПКМ - скопировать")
            .addLore("§7Шифт+ПКМ - вставить")
            .addLore("")
            .addEnchant(Enchantment.LUCK)
            .build();

        gameSignEdit = new ItemBuilder(Material.CRIMSON_SIGN)
            .name("§fСерверные таблички")
            .addLore("")
            .addLore("§7ЛКМ по табличке - §cудалить")
            .addLore("")
            .addLore("§7ПКМ по табличке - ")
            .addLore("§7настроить отображаемую игру")
            .addEnchant(Enchantment.LUCK)
            .build();

        passport = new ItemBuilder(Material.PAPER)
             .name("§aПаспорт")
             .addFlags(ItemFlag.HIDE_ATTRIBUTES)
             .addFlags(ItemFlag.HIDE_ENCHANTS)
             .addFlags(ItemFlag.HIDE_UNBREAKABLE)
             .setUnbreakable(true)
             .addLore("")
             .addLore("§7Держите паспорт в руке,")
             .addLore("§7и окружающие смогут его")
             .addLore("§7посмотреть,сделав правый")
             .addLore("§7клик на Вас.")
             .addLore("")
             .addLore("§7Вы всегда можете")
             .addLore("§7достать документ из кармана")
             .addLore("§7набрав §b/passport get")
             .addLore("§7Изменить паспортные данные")
             .addLore("§7можно в профиле.")
             .addLore("")
             .build();
    }
    

    @EventHandler ( ignoreCancelled = true, priority = EventPriority.LOW )
    public void Command(PlayerCommandPreprocessEvent e) throws CommandException {
        //final String[] args = e.getMessage().replaceFirst("/", "").split(" ");
       // final String cmd = args[0].toLowerCase();
        final Player p = e.getPlayer();
        if (ApiOstrov.canBeBuilder(p) && !ApiOstrov.isLocalBuilder(p)) {
            final String cmd = e.getMessage().replaceFirst("/", "");
            if (cmd.startsWith("builder") || cmd.startsWith("gm")) return;
            final Oplayer op = PM.getOplayer(p);
            op.lastCommand =  cmd;
        }
    }

    //вызывается из SpigotChanellMsg
    public static void onBungeeData(final String name, final String raw) { 
        final Player p = Bukkit.getPlayerExact(name);
        if (p==null) { //данные пришли раньше PlayerJoinEvent
            bungeeDataCache.put(name, raw);
        } else { //если уже был PlayerJoinEvent
            PM.bungeeDataHandle(p, PM.getOplayer(p), raw); //просто прогрузить данные
        }
    }


    @EventHandler(priority = EventPriority.LOWEST) 
    public void onPlayerJoin(PlayerJoinEvent e) {
        e.joinMessage(null);
        final Player p = e.getPlayer();
        //LOCALE тут не получить!!! ловить PlayerLocaleChangeEvent
        final Oplayer op = PM.createOplayer(p);
        p.setShieldBlockingDelay(2);
        p.setNoDamageTicks(20);
        
        if (LocalDB.useLocalData) {
            
            p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20, 0));
            Ostrov.async( () ->  { 
                LocalDB.loadLocalData(p.getName());//локальные данные на загрузку независимо от данных с банжи!
            }, 10 );
            
        } else {
            if ( Config.set_gm && !p.isOp() ) p.setGameMode(Config.gm_on_join);
            if ( Config.walkspeed_on_join>0)  p.setWalkSpeed(Config.walkspeed_on_join);
        }
        
        final String bungeeData = bungeeDataCache.remove(p.getName());
        if (bungeeData!=null) { //данные пришли ранее, берём из кэша
            Ostrov.sync( ()-> PM.bungeeDataHandle(p, op, bungeeData) ,1); //- без задержки не выдавало предметы лобби!
        }
        if (Ostrov.MOT_D.equals("jail")) {
            ApiOstrov.sendTabList(p,  "§4ЧИСТИЛИЩЕ", "");
        } else {
            ApiOstrov.sendTabList(p, "", "");
        }
        
        //for (final Oplayer otherOp : PM.getOplayers()) {
            //otherOp.score.onJoin(op);
            //if (otherOp.score.hideNameTags) {
            //    otherOp.score.getTeam().addEntry(op.nik);
            //}
          //  VM.getNmsNameTag().updateTag( otherOp, p); //закинуть тэги других игроков вошедшему
       // }

    }

    
    @EventHandler(priority = EventPriority.MONITOR) 
    public void PlayerQuit(PlayerQuitEvent e) {
        e.quitMessage(null);
        final Player p = e.getPlayer();
        final Oplayer op = PM.remove(p.getName());
        if (op!=null) { //сохраняем, если было реально загружено!
            op.onLeave(p, true);
        }
        //VM.getNmsServer().removePacketSpy(p);
        //for (final Oplayer otherOp : PM.getOplayers()) {
            //otherOp.score.onQuit(op);
            //if (otherOp.score.hideNameTags) {
            //    otherOp.score.getTeam().removeEntry(p.getName());
            //}
        //}
        
    }

    
    

    
    @EventHandler (priority = EventPriority.LOW, ignoreCancelled = true)
    public void FriendTeleport(FriendTeleportEvent e) {
        if (e.source!=null && e.source.isOnline() && !e.source.isDead() && PM.inBattle(e.source.getName())) {
            e.setCanceled(true, "§cбитва.");
        }
    }



    
    
    
    
    @EventHandler(ignoreCancelled = false, priority = EventPriority.MONITOR)
    public void onInteractEntity(PlayerInteractEntityEvent e) {
        if ( e.getRightClicked().getType()==EntityType.PLAYER && PM.exist(e.getRightClicked().getName()) ) {
            final Player target=(Player) e.getRightClicked();
            //если у цели в руках паспорт - показать кликающему
            if  (ItemUtils.compareItem(target.getInventory().getItemInMainHand(), passport, true) || ItemUtils.compareItem(target.getInventory().getItemInOffHand(), passport, true)) {
                e.setCancelled(true);
                PassportCmd.showLocal(e.getPlayer(), target);
            }
        }
    }
    

    
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onDrop(PlayerDropItemEvent e) {
        if ( ItemUtils.compareItem(e.getItemDrop().getItemStack(), passport, true)) {
            e.getItemDrop().remove();
            e.getPlayer().updateInventory();
        }
    }
    
//    public static Area ar;
//    private Team tm = null;
    
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = false)
    public void Interact (final PlayerInteractEvent e) {
        if ( e.getAction()==Action.PHYSICAL ) return;
        
        final Player p = e.getPlayer();
        final ItemStack inHand = e.getItem();
  
        //фикс для NAME_TAG
        if (inHand!=null && inHand.getType() == Material.NAME_TAG 
            && e.getAction().isRightClick() && GM.GAME.type==ServerType.ONE_GAME ) {  //отловил баг на змейке, походу на минииграх это не надо
            final ItemMeta im = inHand.getItemMeta();
            new InputButton(InputType.ANVILL, inHand, im.hasDisplayName() ? TCUtils.toString(im.displayName()).replace('§', '&') : "Название", nm -> {
                im.displayName(TCUtils.format(nm.replace('&', '§')));
                inHand.setItemMeta(im);
                p.closeInventory();
            }).run(new ItemClickData(p, new InventoryClickEvent(p.getOpenInventory(), SlotType.CONTAINER, 0,
                ClickType.LEFT, InventoryAction.PICKUP_ALL), ClickType.LEFT, ItemUtils.air, SlotPos.of(0, 0)));
        }
        
        //if (inHand!=null && inHand.getType() == Material.BLAZE_ROD) {
        	
//            QuestManager.showForPl(p, PM.getOplayer(p));
//            QuestManager.addProgress(p, PM.getOplayer(p), QuestManager.q1);
//            QuestManager.addProgress(p, PM.getOplayer(p), QuestManager.q2);
//            QuestManager.addProgress(p, PM.getOplayer(p), QuestManager.q3);
        //}
        
        /*if (inHand!=null && inHand.getType() == Material.BLAZE_ROD) {
        	final FakeItemDis fid = DisplayManager.fakeItemAnimate(p, p.getEyeLocation().add(p.getEyeLocation().getDirection().multiply(2d)));
        	fid.setItem(new ItemStack(Material.GRASS_BLOCK)).setRotate(false).setOnClick(pl -> {
        		if (pl.getEntityId() != p.getEntityId()) return;
        		pl.teleport(pl.getLocation().add(0d, 10d, 0d));
        		fid.remove();
        	}).setOnLook(pl -> ApiOstrov.sendActionBarDirect(pl, "Hello There")).setName("Wonderful")
        	.setNameVis(false).setFollow(true).setIsDone(tk -> tk > 1000 || p.isSneaking()).create();
        	DisplayManager.fakeTextAnimate(p, p.getEyeLocation().add(p.getEyeLocation().getDirection().multiply(2d)), "hello, this is the start of something absolutely beautiful, and im excited to see where it goes", true, true, 4);
        }*/

        //паспорт
        if (ItemUtils.compareItem(inHand, passport, true)) { //посмотреть свой паспорт
            e.setUseItemInHand(Event.Result.DENY);
            if (e.getAction().isRightClick()) {
                PassportCmd.showLocal(p, p);
            } 
        }
        
        
        //Клик по табличке
//Ostrov.log("ALL_SIGNS?"+(e.getClickedBlock() != null && Tag.ALL_SIGNS.isTagged(e.getClickedBlock().getType())));
        if (e.getClickedBlock() != null && 
                (Tag.ALL_SIGNS.isTagged(e.getClickedBlock().getType()) || Tag.ALL_HANGING_SIGNS.isTagged(e.getClickedBlock().getType()) ) ) {
            
            //редактор таблички и серверные таблички
            if ( ApiOstrov.isLocalBuilder(p, false) ) {
                
                if ( ItemUtils.compareItem(signEdit, inHand, false)) {
                    signEdit(p, e);
                    return;
                    
                } else if ( ItemUtils.compareItem(gameSignEdit, inHand, false)) {
                    e.setCancelled(true);
                    final String locAsString = LocationUtil.toString(e.getClickedBlock().getLocation());
                    if (e.getAction()==Action.LEFT_CLICK_BLOCK) {
                        if (GM.signs.containsKey(locAsString)) {
                            ConfirmationGUI.open(p, "Удалить табличку?", (result)-> {
                                    if(result) {
                                        e.getClickedBlock().breakNaturally();
                                        GM.deleteGameSign(p, locAsString);
                                    }
                                } 
                            );
                        } else {
                            p.sendMessage("§6Это на серверная табличка!");
                        }
                    } else {
                        if (GM.signs.containsKey(locAsString)) {
                            p.sendMessage("§6Это серверная табличка, сначала сломайте её!");
                            return;
                        }
                        SmartInventory.builder()
                        .type(InventoryType.CHEST)
                        .id("GameSignEditor"+p.getName()) 
                        .provider(new GameSignEditor( (Sign) e.getClickedBlock().getState() ))
                        .title("§fНастройка серверной таблички")
                        .size(6, 9)
                        .build()
                        .open(p);
                    }
                    return;
                }
            }
            
            //клик по серверной табличке
            final String locAsString = LocationUtil.toString(e.getClickedBlock().getLocation());
            final GameSign gameSign = GM.signs.get(locAsString);

            if (gameSign!=null) {
                if (Timer.has(p, "gameSign")) {
                    p.sendMessage("§8подождите 2 секунды..");
                    return;
                }
                Timer.add(p, "gameSign", 2);

                e.setUseInteractedBlock(Event.Result.DENY);
                e.setUseItemInHand(Event.Result.DENY); //если не отменять, то может сразу сработать слим выхода с арены

                if (GM.GAME.type==ServerType.ARENAS) {
                    Bukkit.getPluginManager().callEvent(new BsignLocalArenaClick( p, gameSign.arena ));
                } else {
                    p.performCommand("server "+gameSign.server+" "+gameSign.arena);//ApiOstrov.sendToServer (p, gameSign.server, gameSign.arena);
                }
            }
            
            //командная табличка
            if ( e.getAction()==Action.RIGHT_CLICK_BLOCK) {//if (Tag.WALL_SIGNS.isTagged(e.getClickedBlock().getType())) {
                final Sign sign = (Sign) e.getClickedBlock().getState();
                final SignSide ss = sign.getSide(Side.FRONT);
                final String line0=TCUtils.stripColor( ss.line(0)).toLowerCase();
                final String line1=TCUtils.stripColor( ss.line(1));
                if (line0.isEmpty() || line1.isEmpty()) return;
    //System.out.println("Sign_click 222 "+line0);
                switch (line0) {
                    case "[команда]" -> {
                        //if (ServerListener.checkCommand(p, line1.toLowerCase())) return;
                        p.performCommand(line1.toLowerCase());
                        return;
                    }
                    case "[место]" -> {
                        p.performCommand( "warp "+TCUtils.stripColor(line1).toLowerCase() );
                        return;
                    }
                }
            }
        }
        
        //блокировка лавы
        if ( e.getAction()==Action.RIGHT_CLICK_BLOCK) {
            
            if (Config.disable_lava && inHand!=null && inHand.getType().toString().contains("LAVA") && !ApiOstrov.isLocalBuilder(p, false)) {
                e.setUseItemInHand(Event.Result.DENY);
                ApiOstrov.sendActionBarDirect(p, "§cЛава запрещена на этом сервере!");
                //return;
            }
            
        }
        
        
    }
    
    



    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void Sign_create(SignChangeEvent e) {
        final Player p = e.getPlayer();
    	final String line0 = TCUtils.stripColor(TCUtils.toString(e.line(0)));

        if (line0.equalsIgnoreCase("[Команда]") || line0.equalsIgnoreCase("[Место]")) {
            if (!ApiOstrov.isLocalBuilder(p, true)) {
                e.line(0, Component.text("§8"+line0));
            } else {
                e.line(0, Component.text("§2"+line0));
            }
        } else {
            e.line(0, Component.text(line0.replaceAll("&", "§")));
        }
        
        e.line(1, Component.text(TCUtils.toString(e.line(1)).replaceAll("&", "§")));
        e.line(2, Component.text(TCUtils.toString(e.line(2)).replaceAll("&", "§")));
        e.line(3, Component.text(TCUtils.toString(e.line(3)).replaceAll("&", "§")));
    }

    
    



    

    private void signEdit(final Player p, final PlayerInteractEvent e) {
        e.setCancelled(true);

        Sign sign = (Sign) e.getClickedBlock().getState();
        if (e.getAction()==Action.LEFT_CLICK_BLOCK) {
            if (p.isSneaking()) { //шифт+лкм - сменить тип
                final Block b = e.getClickedBlock();
                //final List<String> lines = sign.getSide(Side.FRONT).lines().stream().map(TCUtils::toString).collect(Collectors.toList());
                final List <Component> linesFront = sign.getSide(Side.FRONT).lines();
                final List <Component> linesBack = sign.getSide(Side.BACK).lines();
                final List<Material> types = new ArrayList<>();// = new ArrayList<>( Tag.WALL_SIGNS.isTagged(b.getType()) ? Tag.WALL_SIGNS.getValues() : Tag.STANDING_SIGNS.getValues());
                
                if (Tag.WALL_SIGNS.isTagged(b.getType())) {
                    types.addAll(Tag.WALL_SIGNS.getValues());
                } else if (Tag.STANDING_SIGNS.isTagged(b.getType())) {
                    types.addAll(Tag.STANDING_SIGNS.getValues());
                } else if (Tag.WALL_HANGING_SIGNS.isTagged(b.getType())) {
                    types.addAll(Tag.WALL_HANGING_SIGNS.getValues());
                } else if (Tag.CEILING_HANGING_SIGNS.isTagged(b.getType())) {
                    types.addAll(Tag.CEILING_HANGING_SIGNS.getValues());
                }
                int order = types.indexOf(b.getType()); //подбор следующего материала таблички
                order++;
                if (order>=types.size()) order=0;
                final Material newMat = types.get(order);
    //Bukkit.broadcastMessage("newMat="+newMat+" WALL_SIGNS?"+Tag.WALL_SIGNS.isTagged(b.getType())+" STANDING"+Tag.STANDING_SIGNS.isTagged(b.getType())+" SIGNS"+Tag.SIGNS.isTagged(b.getType()));

                if (Tag.WALL_SIGNS.isTagged(b.getType())) {
                    final WallSign wsData = (WallSign) newMat.createBlockData();//org.bukkit.block.data.type.WallSign
                    wsData.setFacing(((Directional) b.getBlockData()).getFacing());
                    wsData.setWaterlogged(((Waterlogged) b.getBlockData()).isWaterlogged());
                    b.setBlockData(wsData);
                } else if (Tag.STANDING_SIGNS.isTagged(b.getType())) {
                    final org.bukkit.block.data.type.Sign snData = (org.bukkit.block.data.type.Sign) newMat.createBlockData();//org.bukkit.block.data.type.Sign
                    snData.setRotation(((Rotatable) b.getBlockData()).getRotation());
                    snData.setWaterlogged(((Waterlogged) b.getBlockData()).isWaterlogged());
                    b.setBlockData(snData);
                }

                sign = (Sign) b.getState();
                //for (byte i = 0; i < 4; i++) {
                //    frontSide.line(i, linesFront.get(i));
                //    backSide.line(i, linesBack.get(i));
                //}
                final SignSide frontSide = sign.getSide(Side.FRONT);
                int i = 0;
                for (final Component c : linesFront) { //хз, так будет универсальнее - кол-во строк может измениться
                    frontSide.line(i++, c);
                }
                i=0;
                final SignSide backSide = sign.getSide(Side.BACK);
                for (final Component c : linesBack) {
                    backSide.line(i++, c);
                }
                sign.update();

            } else {
                SmartInventory.builder()
                    //.type(InventoryType.HOPPER)
                    .id("SignEditSelectLine"+p.getName()) 
                    .provider(new SignEditMenu(sign))
                    .title("§fВыберите строку")
                    .size(3, 9)
                    .build()
                    .open(p);
            }
        } else if (e.getAction()==Action.RIGHT_CLICK_BLOCK) {
            if (p.isSneaking()) {
                if (signFrontCache.containsKey(p)) {
                    final SignSide frontSide = sign.getSide(Side.FRONT);
                    int i = 0;
                    for (final Component c : signFrontCache.get(p)) { //хз, так будет универсальнее - кол-во строк может измениться
                        frontSide.line(i++, c);
                    }
                    i=0;
                    final SignSide backSide = sign.getSide(Side.BACK);
                    for (final Component c : signBackCache.get(p)) { //хз, так будет универсальнее - кол-во строк может измениться
                        backSide.line(i++, c);
                    }
                    //for (byte i = 0; i < 4; i++) {
                    //    frontSide.line(i, signFrontCache.get(p).get(i));
                    //    backSide.line(i, signBackCache.get(p).get(i));
                    //}
                    //final SignSide ss = sign.getSide(Side.FRONT);
                    //ss.line(0, Component.text(signCache.get(p.getName())[0]));
                   // ss.line(1, Component.text(signCache.get(p.getName())[1]));
                   // ss.line(2, Component.text(signCache.get(p.getName())[2]));
                   // ss.line(3, Component.text(signCache.get(p.getName())[3]));
                    sign.update();
                } else {
                    p.sendMessage("В буфере нет скопированной таблички.");
                }
            } else {
                //if (!signCache.containsKey(p.getName())) {
               //     signCache.put(p.getName(), new String[4]);
                //}
                //final SignSide ss = sign.getSide(Side.FRONT);
                final SignSide frontSide = sign.getSide(Side.FRONT);
                signFrontCache.put(p, frontSide.lines());
                final SignSide backSide = sign.getSide(Side.BACK);
                signBackCache.put(p, backSide.lines());
               // signCache.get(p.getName())[0] = TCUtils.toString(ss.line(0));
               // signCache.get(p.getName())[1] = TCUtils.toString(ss.line(1));
               // signCache.get(p.getName())[2] = TCUtils.toString(ss.line(2));
                //signCache.get(p.getName())[3] = TCUtils.toString(ss.line(3));
                p.sendMessage("Содержимое таблички скопировано в буфер. Шифт+ПКМ на другую - вставить.");
            }
        }
    }
    
      
    
    
    
    
    
    
    
    
    
    
     
    
    
        
        
        
        
 // ----------------------------- ACTION ----------------------
            
    @EventHandler(ignoreCancelled = true,priority = EventPriority.MONITOR)    
    public void onPlace(BlockPlaceEvent e) {
        //PM.getOplayer(e.getPlayer().getName()).last_breack=Timer.Единое_время();
        if ( Config.disable_break_place && !ApiOstrov.isLocalBuilder(e.getPlayer()) ) e.setCancelled(true);
        //else if (!clear_stats) PM.Addbplace(e.getPlayer().getName());
    }
    
    @EventHandler(ignoreCancelled = true,priority = EventPriority.MONITOR)    
    public void onBreak(BlockBreakEvent e) {
      //  PM.getOplayer(e.getPlayer().getName()).last_breack=Timer.Единое_время();
        if ( Config.disable_break_place && !ApiOstrov.isLocalBuilder(e.getPlayer()) ) e.setCancelled(true);
        //else if (!clear_stats) PM.get(e.getPlayer().getName());
    }
 
        
        
    @EventHandler(ignoreCancelled = true,priority = EventPriority.MONITOR)    
    public void onHangingBreakByEntityEvent(HangingBreakByEntityEvent e) {
        if ( e.getRemover()!=null && e.getRemover().getType()==EntityType.PLAYER && PM.exist(e.getRemover().getName())) {
                if ( Config.disable_break_place &&  !ApiOstrov.isLocalBuilder(e.getRemover()) ) e.setCancelled(true);
        } 

    }
       
    @EventHandler(ignoreCancelled = true,priority = EventPriority.MONITOR)    
    public void onHangingBreakEvent(HangingBreakEvent e) {
        if ( e.getEntity() instanceof Player player) {
                if ( Config.disable_break_place &&   !ApiOstrov.isLocalBuilder(player) ) e.setCancelled(true);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = false)
    public void onPlayerItemFrameChangeEvent(final PlayerItemFrameChangeEvent e) {
        if (Config.disable_break_place && !ApiOstrov.isLocalBuilder(e.getPlayer(), true)) {
            e.setCancelled(true);
            //return;
        }
    }
    
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onPlayerInteractAtEntityEvent(PlayerInteractAtEntityEvent e) {

        final Player p = e.getPlayer();
       
        if (ApiOstrov.isLocalBuilder(p, false)) {
            if (p.isSneaking()) {
                Ostrov.sync(() -> {
                    SmartInventory.builder()
                        . provider(new EntitySetup(e.getRightClicked()))
                        . size(6, 9)
                        . title("§2Характеристики сущности").build()
                        .open(p);
                }, 1); //через тик, илил открывает меню торговли
            }
        }

        switch (e.getRightClicked().getType()) {
            
            case ARMOR_STAND -> e.setCancelled(Config.disable_break_place && !ApiOstrov.isLocalBuilder(p, true));
            
            case ITEM_FRAME, GLOW_ITEM_FRAME -> {
                if (Config.disable_break_place && !ApiOstrov.isLocalBuilder(p, true)) {
                    e.setCancelled(true);
                    return;
                }
                final ItemStack it = p.getInventory().getItemInMainHand();
                if (ItemUtils.isBlank(it, false)) {
                    break;
                }
                final ItemFrame ent;
                switch (it.getType()) {
                    case GLOWSTONE_DUST -> {
                        ent = (ItemFrame) e.getRightClicked();
                        if (!ent.isGlowing()) {
                            ent.setGlowing(true);
                            p.getInventory().setItemInMainHand(it.subtract());
                        }
                    }
                    case GUNPOWDER -> {
                        ent = (ItemFrame) e.getRightClicked();
                        if (ent.isVisible()) {
                            ent.setVisible(false);
                            p.getInventory().setItemInMainHand(it.subtract());
                        }
                    }
                    case SUGAR -> {
                        ent = (ItemFrame) e.getRightClicked();
                        if (!ent.isVisible() || ent.isGlowing()) {
                            ent.setVisible(true);
                            ent.setGlowing(false);
                            p.getInventory().setItemInMainHand(it.subtract());
                        }
                    }
                    default -> {}
                }
            }

            default -> {}
        }

    }


   

    @EventHandler(ignoreCancelled = true,priority=EventPriority.LOWEST)
    public void PlayerArmorStandManipulateEvent(PlayerArmorStandManipulateEvent e){
        if ( Config.disable_break_place && !e.getPlayer().isOp()) e.setCancelled(true);
    }    
        
    
//---------------------------------------------------
       
        
        
        
        
        
        
        
        
        
        
        
        
        
// ----------------------------------- MOVE --------------------------------


        
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onPlayerRespawn(PlayerRespawnEvent e) {
            final Oplayer op = PM.getOplayer(e.getPlayer());
            if(op==null) return;
            
            if (Config.home_command && op.homes.containsKey("home")) {
                final Player p = e.getPlayer();
                Location loc = LocationUtil.LocFromString(op.homes.get("home"));
                if (!TeleportLoc.isSafeLocation(loc)) {
                    loc = TeleportLoc.findNearestSafeLocation(loc, null);
                }
                if (loc==null) {
                    p.sendMessage("§7Не получилось респавниться дома - точка дома может быть опасна.");
                } else {
                    e.setRespawnLocation(loc);
                }
            }
        
        if (op.pvp_time>0) {
            Ostrov.sync(()-> {
                PvpCmd.pvpEndFor(op, e.getPlayer()); //восстановить настроки до начала битвы, убрать тэги
            }, 5);
        }

        
    }      
    
    
    @EventHandler ( ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onWorldChange(PlayerTeleportEvent e) {
        
        final Player p = e.getPlayer();
        final Oplayer op = PM.getOplayer(e.getPlayer());
        if (op==null) return;
        
        final String world_from = e.getFrom().getWorld().getName();
        final String world_to = e.getTo().getWorld().getName();
        
        if (!world_from.equals(world_to)) {
    		if (!ApiOstrov.isLocalBuilder(p, false) && world_to.endsWith(WorldManager.buildWorldName)) {
    			e.setCancelled(e.getCause() == TeleportCause.SPECTATE);
				//ApiOstrov.sendToServer(p, "lobby0", "");
    		}
    		
            if (PvpCmd.no_damage_on_tp > 0) {
               op.setNoDamage(PvpCmd.no_damage_on_tp, true);//no_damage=PvpCmd.no_damage_on_tp;
            }
            op.world_positions.put(world_from, LocationUtil.toDirString(p.getLocation()));   //op.PM.OP_Set_world_position(e.getPlayer(), world_from);                      //сохраняем точку выхода
        }
        
    }
        
        
    
 //------------------------------------------------------------------------   
 
        

    
 

    
    
    
    
    
    
    
    
// ------------------------------- ITEM -------------------------------------------    
 
    

    
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void EntityBowShoot(EntityShootBowEvent e) {
        if (e.getEntityType()==EntityType.PLAYER) {

            final Player p = (Player) e.getEntity();
            if (!PM.exist(p.getName())) return;
            
            if (MenuItemsManager.hasItem("tpbow")) {
                final MenuItem si = MenuItemsManager.fromItemStack(e.getBow());
                if (si!=null) {
                    if (Timer.has(p, "bow_teleport")) {//if (PM.getOplayer(p.getName()).bow_teleport_cooldown>0) {
                        p.sendMessage("§cПерезарядка лука.. осталось §4"+Timer.getLeft(p, "bow_teleport")+" сек.");
                        e.setCancelled(true);
                        e.getProjectile().remove();
                    } else {
                        Timer.add(p, "bow_teleport", 4);
                        e.getProjectile().setMetadata("bowteleport", new FixedMetadataValue(Ostrov.instance, "ostrov"));
                    }
                }
            }
            
            if (!p.isOp() && p.getGameMode().equals(GameMode.CREATIVE)) {
                ApiOstrov.sendActionBar(p, "§cПВП в креативе заблокирован!");
                e.setCancelled(true);
            }
        }
    }  
   
   /* @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void ProjectileHitEvent(final ProjectileHitEvent e) {
        
        if (MenuItemsManager.hasItem("tpbow") && e.getEntity().getShooter() instanceof Player && e.getEntity().hasMetadata("bowteleport")) {
            Location destination =  (e.getEntity()).getLocation().clone();
            e.getEntity().remove();
            final Player p = (Player)e.getEntity().getShooter();
            destination.setPitch(p.getLocation().getPitch());
            destination.setYaw(p.getLocation().getYaw());
            p.teleport(destination, PlayerTeleportEvent.TeleportCause.COMMAND);
            p.playSound(p.getLocation(),Sound.ENTITY_BAT_HURT, 2, 1);
        }

    }*/
        
// ------------------------------------------------------------------------
    
    
    
    
    
    
    
    
    
    
    
// ---------------------------- Режимы битвы ---------------------------------
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent e) { 
        if ( e.getEntityType()==EntityType.PLAYER ) {
            if (!PM.exist(e.getEntity().getName())) return; //защита от бота
            switch (e.getCause()) {
                case VOID:
                    if (Config.disable_void) {
                        e.setDamage(0);
                        e.getEntity().teleport(Bukkit.getWorlds().get(0).getSpawnLocation(), PlayerTeleportEvent.TeleportCause.COMMAND);
                    }
                    return;
                //чары шипы на оружие-ранит нападающего
                //молния
                //дыхание дракона
                //кактусы
                //огонь
                //горение
                //BlockMagma
                //EntityVex
                //утопление
                //голод
                case FALL, THORNS, LIGHTNING, DRAGON_BREATH, 
                CONTACT, FIRE, FIRE_TICK, HOT_FLOOR, CRAMMING, 
                DROWNING, STARVATION, LAVA:
                default:
                    if ( Config.disable_damage ) e.setCancelled(true);
                    //return;
            }
        } else {
            if (e.getCause()==EntityDamageEvent.DamageCause.VOID) {
                e.getEntity().remove();
                Ostrov.log_warn("Удалена бесконечно падающая в бездну сущность "+ e.getEntity());
                return;
            }
            if ( Config.disable_damage ) e.setCancelled(true);
        }
    }
//------------------------------------------------------------------------------ 
    
    
    
    

    
    
    @EventHandler(ignoreCancelled = true)
    public void onPlayerLoseFood(FoodLevelChangeEvent e) { 
        if ( Config.disable_hungry ) {
            e.setCancelled(true);
            (e.getEntity()).setFoodLevel(20);
        }
    }
  
    
    
}
