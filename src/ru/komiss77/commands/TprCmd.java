package ru.komiss77.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import com.google.common.collect.ImmutableList;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Config;
import ru.komiss77.Ostrov;
import ru.komiss77.Timer;
import ru.komiss77.events.RandomTpFindEvent;
import ru.komiss77.hook.WGhook;
import ru.komiss77.modules.DelayTeleport;
import ru.komiss77.modules.translate.Lang;
import ru.komiss77.utils.TeleportLoc;
import ru.komiss77.version.Nms;


public class TprCmd implements CommandExecutor, TabCompleter{
	
    private static final HashMap<String,BukkitTask> tpData = new HashMap<>();
    private static final int TRY_PER_TICK = 3;
    
    
    @Override
    public List<String> onTabComplete(CommandSender cs, Command cmnd, String command, String[] args) {
        
        //final List <String> sugg = new ArrayList<>();
        
        
        switch (args.length) {
            
            case 1:
                //0- пустой (то,что уже введено)
                final List <String> sugg2 = new ArrayList<>();
                Bukkit.getOnlinePlayers().forEach( (p) ->  sugg2.add(p.getName())  );
                return sugg2;
                
            case 2:
                final List <String> sugg = new ArrayList<>();
                Bukkit.getWorlds().forEach( (w) ->  sugg.add(w.getName())  );
                return sugg;

            case 3:
                return Arrays.asList("10","100","1000");

        }
        
       return ImmutableList.of();
    }



    
    @Override
    public boolean onCommand(CommandSender cs, Command cmd, String string, String[] arg) {
        
        Player p = (cs instanceof Player) ? (Player)cs : null;
        final boolean console = cs instanceof ConsoleCommandSender;
        /*
        case "tpr":
            int delay = tpr_command;
            if ( delay < 1 && sender instanceof Player) {
                p.sendMessage( "§cТелепорт в случайное место командой отключён на этом сервере!");
                return true;
            }
            if (sender instanceof ConsoleCommandSender) {
                if (arg.length==1) {
                    p = Bukkit.getPlayerExact(arg[0]);
                    if (p==null) {
                        sender.sendMessage("§cИгрок "+arg[0]+" не найден!");
                        return true;
                    }
                    delay = 5;
                } else {
                    sender.sendMessage("§ctpr <ник>");
                    return true;
                }
            }

            if (!Timer.has(p, "tpr_command" ) ) {
                Timer.add(p, "tpr_command", delay);
                TprCmd.runCommand(p);
            } else {
                sender.sendMessage("§8Телепортер перезаряжается! Осталось: "+Timer.getLeft(p, "tpr_command")+" сек.!");
            }
            break;
        */
        int delay = Config.tpr_command;
        if ( delay < 1 && !console) {
            cs.sendMessage( "§c"+Lang.t(p, "Телепорт в случайное место командой отключён на этом сервере!"));
            return true;
        }
        
        if (arg.length>=1) { //при попытке тп другого игрока
            if (!ApiOstrov.isLocalBuilder(p) && !console) {
                cs.sendMessage( "§c"+Lang.t(p, "Вы не можете ТПР другого игрока!"));
                return true;
            }
        }
        
        World world = null;
        int radiusLimit = 0;
        
         switch (arg.length) {

            case 3:
                radiusLimit = ApiOstrov.getInteger(arg[2]);
                if (radiusLimit<1) {
                    cs.sendMessage("§c"+Lang.t(p, "Лимит радиуса поиска - число больше 1!"));
                    return true;
                }
                
            case 2:
                world = Bukkit.getWorld(arg[1]);
                if (world==null) {
                    cs.sendMessage("§c"+Lang.t(p, "Нет мира с названием ")+arg[1]);
                    return true;
                }
                //break;


            case 1:
                p = Bukkit.getPlayerExact(arg[0]);
                if (p==null) {
                    cs.sendMessage("§c"+Lang.t(p, "Игрок ")+arg[0]+Lang.t(p, " не найден!"));
                    return true;
                }
                delay = 5;
                break;
                
            case 0:
                if (console) {
                    cs.sendMessage("§cДля консоли - нужно указать ник!");
                    return true;
                }
                break;

                
            default:
                if (console) {
                    cs.sendMessage("§ctpr <ник> [мир] [радиус]");
                } else {
                    cs.sendMessage("§ctpr [ник] [мир] [радиус]");
                }
                return true;

        }
        
        if (world==null && p!=null) {
            world = p.getWorld();
        }
        
        //if (!console && world.getEnvironment() != World.Environment.NORMAL ) {
        //    cs.sendMessage( "§cТелепорт работает только в обычном мире!");
        //    return true;
        //} 
        
        if (!Timer.has(p, "tpr_command" ) ) {
            if (!ApiOstrov.isLocalBuilder(p)) {
                Timer.add(p, "tpr_command", delay);
            }
            runCommand(p, world, radiusLimit, console, false, pl -> pl.playSound(pl.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 5) );

            //p.playSound(p.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 5);
        } else {
            cs.sendMessage("§8"+Lang.t(p, "Телепортер перезаряжается! Осталось: ")+Timer.getLeft(p, "tpr_command")+" сек.!");
        }

        return true;
    }
    


    
    public static void runCommand(final Player p, final World world, final int radiusLimit, final boolean ignoreMove, final boolean anyCase, final Consumer<Player> onDone) {

        //if ( !p.hasPermission("ostrov.tpr.free")) {
        //    if (ApiOstrov.moneyGetBalance(p.getName())<100) {
        //        p.sendMessage("§cНедостаточно денег для перемещения! Стоимость: 100 лони");
        //        return;
        //    }
        //}
        if (tpData.containsKey(p.getName())) {
            p.sendMessage( "§c"+Lang.t(p, "Для Вас уже ищется место для телепорта!"));
            return;
        }

        //p.sendMessage("§bТелепортер ищет безопасное место для Вас...");


/*
    Это устанавливает максимально возможный размер в блоках, выраженный в радиусе, который может получить мировая граница.
        Установка большей границы мира приводит к успешному выполнению команд, 
        но фактическая граница не выходит за пределы этого ограничения блока.
        Установка max-world-size выше значения по умолчанию, похоже, ничего не дает.
     Установка max-world-size на 1000 позволяет игроку иметь границу мира 2000 × 2000.
     Установка max-world-size на 4000 дает игроку границу мира 8000 × 8000. 
        https://minecraft.fandom.com/wiki/World_border
        */
        //вычисляем максимум +/- для x,z - РАДИУС!!! 
        //для каждой команды все параметры внутри, или могут запускать в разных мирах и подменятся параметры!
        
        final int center_x=world.getWorldBorder().getCenter().getBlockX();
        final int center_z=world.getWorldBorder().getCenter().getBlockZ();

        final int worldDiameter = (int) world.getWorldBorder().getSize() < Bukkit.getServer().getMaxWorldSize() ? ((int) world.getWorldBorder().getSize()) : Bukkit.getServer().getMaxWorldSize() ;//VM.getNmsServer().getMaxWorldSize(world);//propertyManager.getInt("max-world-size", 500);
        //вычисляем минимум +/- для x,z
        final int min = (worldDiameter/2)/50; //при мире 5к даст для поиска - дельтф будет +/- ( рандом от min до max)100, при 500 даст 10
        int max = worldDiameter/2 - min;  // - min чтобы не прижимало к границе

        final int minFindRadius = radiusLimit>1 && min>radiusLimit ? radiusLimit : min;
        final int maxFindRadius = radiusLimit>1 && radiusLimit<max ? radiusLimit : max;
        
//Bukkit.broadcastMessage("radiusLimit="+radiusLimit+" worldDiameter="+ worldDiameter+" min="+ min+" max="+ max+" minFindRadius="+minFindRadius+" maxFindRadius="+maxFindRadius);    
        tpData.put(p.getName(), new BukkitRunnable() {

            final int xMax = center_x+maxFindRadius;
            final int xMin = center_x-maxFindRadius;
            final int zMax = center_z+maxFindRadius;
            final int zMin = center_z-maxFindRadius;

    //System.out.println("xMax="+ xMax+" xMin="+ xMin+" zMax="+ zMax+" zMin="+ zMin);        
            final int x = p.getLocation().getBlockX();
            final int y = p.getLocation().getBlockY();
            final int z = p.getLocation().getBlockZ();
            final int minY = p.getWorld().getMinHeight()+1;
            final int maxY = p.getWorld().getEnvironment()==World.Environment.NETHER ? 125 : p.getWorld().getMaxHeight()-2;
            
            final String name = p.getName();
            int find_try=100; //если делать меньше, то изменить ниже Поиск места: §3"+(100-find_try)+"%
            int tryPereTick = TRY_PER_TICK;
            int find_x, find_z, feet_y;
            Location feetLoc = new Location(world, 0, 0, 0);
            //Location temp;
            
            Material headMat = Material.AIR;
            Material feetMat = Material.AIR;;
            Material downMat;
            //final boolean wg = Ostrov.getWorldGuard()!=null;
            
                @Override
                public void run() {

                    if (p==null || !p.isOnline() || p.isDead()) {
                        this.cancel();
                        tpData.remove(name);
                        return;
                    }

                    if (!ignoreMove) {
                        if (p.getLocation().getBlockX()!=x || p.getLocation().getBlockY()!=y || p.getLocation().getBlockZ()!=z) {
                            ApiOstrov.sendActionBarDirect(p, "§c"+Lang.t(p, "ТП отменяется!"));
                            this.cancel();
                            tpData.remove(name);
                            return;
                        }
                    }
                    
                    if (find_try==0) {

                        done();
                        
                    } else {

                        if (ignoreMove) {
                            ApiOstrov.sendActionBarDirect(p, "§8"+Lang.t(p, "Поиск места")+": §3"+(100-find_try)+"%");
                        } else {
                            ApiOstrov.sendActionBarDirect(p, "§e"+Lang.t(p, "Сохраняйте неподвижность, ищем!")+" §b"+(100-find_try)+"%");
                        }
                        
                    }
                    
                    
                    tryPereTick = TRY_PER_TICK;
                    
                    for (; tryPereTick>0; tryPereTick--) {

                        find_x = Ostrov.random.nextBoolean() ? ApiOstrov.randInt(center_x+minFindRadius, xMax) : ApiOstrov.randInt(xMin, center_x-minFindRadius );
                        find_z = Ostrov.random.nextBoolean() ? ApiOstrov.randInt(center_z+minFindRadius, zMax) : ApiOstrov.randInt(zMin, center_z-minFindRadius );
                        
                        feet_y = maxY;
                        
                        feetLoc.set(find_x+0.5, feet_y, find_z+0.5);//=world.getBlockAt(find_x, world.getHighestBlockYAt(find_x, find_z), find_z).getLocation();

                        //в кланах приват чанками, поэтому можно чекнуть не определяя y
                       // if (faction && ApiFactions.geFaction(feetLoc) !=null) {
                       //     continue;
                       // }
                        final RandomTpFindEvent e = new RandomTpFindEvent(p, feetLoc);
                        Bukkit.getPluginManager().callEvent(e);
                        if (e.isCancelled()) {
                            continue;
                        }
                        
//Ostrov.log("                                    find_try="+find_try+" tryPereTick="+tryPereTick);
                        

                        for (; feet_y >= minY; feet_y--) {
                            
                            headMat = feetMat;
                            feetMat = downMat;
                            downMat = Nms.getFastMat(world, find_x, feet_y-1, find_z);
                            
                            //в аду или при генерации как в аду (определяем потолок из бедрока)
                            if ( (world.getEnvironment()==World.Environment.NETHER || feet_y>0) && downMat==Material.BEDROCK ) {
                                continue;
                            }

                            //feetMat = VM.getNmsServer().getFastMat(world, find_x, find_y+1, find_z);
                            //headMat = VM.getNmsServer().getFastMat(world, find_x, find_y+2, find_z);
//Ostrov.log(find_x+","+find_y+","+find_z+" "+downMat+"-"+feetMat+"-"+headMat);
                            //если над нижним блоком нет 2 блока для тела, пропускаем ниже
                            //if (!LocationUtil.isPassable(headMat) || !LocationUtil.isPassable(feetMat)) {
                            //    continue;
                            //}
                            feetLoc.setY(feet_y+0.6);
                            //if (LocationUtil.canStand(downMat) || downMat==Material.WATER) { //вода или подходит для стояния - сойдёт
                            if (TeleportLoc.isSafePlace(headMat, feetMat, downMat)) {
                                break;
                            }
                            
                        }
                        
                        
                        //if (feet_y<=minY) { //это будет в энде скорее всего
                        //    continue;
                        //}
                        
                        //feetLoc.setY(feet_y+0.6);//приподнять на пол блока
                        
                        if (Ostrov.wg && !WGhook.canBuild(p, feetLoc)) {
                            continue;
                        }

                        if (downMat==Material.WATER) { //была найдена поверхность воды - ставим кувшинку
//Ostrov.log("WATER!!!!"+feetLoc.getBlock().getType()+"->LILY_PAD");
                            feetLoc.getBlock().setType(Material.LILY_PAD);
                        } 
                        
                        done();
                        return;
                    }
                    
                    
                    find_try--;


                }
                
                
                
                private void done() {
                    this.cancel();
                    tpData.remove(name);
                    
                    if (find_try==0) {
                        if (anyCase) {
                            p.sendMessage("§b"+Lang.t(p, "Это лучшее, что мы смогли найти.."));
                        } else {
                            p.sendMessage("§b"+Lang.t(p, "Телепортер не смог найти подходящее место! Попробуйте позже.."));
                            return;
                        }
                    }
                    
                    if (ignoreMove) {
                        p.teleport(feetLoc);//ApiOstrov.teleportSave(p, feetLoc, true);
                    } else {
                        DelayTeleport.tp(p, feetLoc, 3, Lang.t(p, "Вы в рандомной локации."), true, true, DyeColor.WHITE);
                    }
                    if (onDone!=null) {
                        onDone.accept(p);
                    }
                    
                    if (downMat==Material.WATER) { //была поставлена кувшинка на воде
                        tpData.put( p.getName(), new BukkitRunnable () { //чтобы не давало новое ТПР пока не сошел с места
                            final String name = p.getName();
                            @Override
                            public void run() {
                                final Player pl = Bukkit.getPlayerExact(name);
                                if (pl==null || !pl.isOnline() || pl.isDead() || pl.getLocation().getBlockX()!=find_x || pl.getLocation().getBlockZ()!=find_z) {
                                    tpData.remove(name);
                                    this.cancel();
                                    feetLoc.getBlock().setType(Material.AIR);
                                }
                            }
                        }.runTaskTimer(Ostrov.instance, 30 + (ignoreMove ? 60 : 0), 10) );
                    }
                    
                }
                
                
                
                
            }.runTaskTimer(Ostrov.instance, 1, 1)
        );







        

    }


}
