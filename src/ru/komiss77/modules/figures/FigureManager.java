package ru.komiss77.modules.figures;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.EulerAngle;
import ru.komiss77.Config;
import ru.komiss77.Initiable;
import ru.komiss77.Ostrov;
import ru.komiss77.Timer;
import ru.komiss77.enums.Game;
import ru.komiss77.events.FigureActivateEntityEvent;
import ru.komiss77.objects.Figure;
import ru.komiss77.objects.Figure.FigureType;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.ItemUtils;
import ru.komiss77.utils.OstrovConfig;




public class FigureManager implements Initiable {

    private static final HashMap<Integer,Figure> figures; //ид фигуры, фигура - хранятся неизменно с запуска
    private static final List<Integer> ids;
    private static OstrovConfig config;
    public static ItemStack stick;
    private static final NamespacedKey key;
    private static BukkitTask task;
    
    static { //класс прогружается до загрузки миров, поэтому активируем в init после загрузки миров
        
        //перенести конфиг
        /*final File oldFigureDirectory = new File(Bukkit.getWorldContainer().getPath()+"/plugins/serverfigure");
        if (oldFigureDirectory.exists() && oldFigureDirectory.isDirectory()) {
            final File cfg = new File(Bukkit.getWorldContainer().getPath()+"/plugins/serverfigure/config.yml");
            final File newcfg = new File(Ostrov.instance.getDataFolder(), "figure.yml");
            if (cfg.exists()) {
                try {
                    Files.move(cfg.toPath(), newcfg.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    cfg.delete();
                    oldFigureDirectory.delete();
                } catch (IOException ex) {
                    Ostrov.log_err("Перемещение конфигурации фигур : "+ex.getMessage());
                }
            }
            
        }*/
        
        config = Config.manager.getNewConfig("figure.yml");
        figures = new HashMap<>();
        ids = new ArrayList<>();
        key = new NamespacedKey(Ostrov.instance, "figure");
        
        stick = new ItemBuilder(Material.STICK)
                .name("§aФигуры §7смотри Lore")
                .addLore("§aПКМ на стойку или моба §7- ")
                .addLore("§7создать фигуру.")
                .addLore("§7Создание начинается с выбора типа.")
                .addLore("§7Если тип не выбрать, фигура не создастся.")
                .addLore("§7После выбора типа происходит")
                .addLore("§7первое сохранение, и фигура станет активна.")
                .addLore("§7")
                .addLore("§aПКМ на фигуру §7- настройки")
                .addLore("§7")
                .build();
    
    }
    
    @Override
    public void postWorld() { //обход модулей после загрузки миров, т.к. не всё можно сделать onEnable
        init();
        Bukkit.getServer().getPluginManager().registerEvents(new FigureListener(), Ostrov.instance);
    }
    
    @Override
    public void reload() {
        init();
    }

    public static void init() { //из острова postWorld
               
        config = Config.manager.getNewConfig("figure.yml");
        figures.clear();
        ids.clear();
        
        try {
            if (config.getConfigurationSection("фигуры")!=null && !config.getConfigurationSection("фигуры").getKeys(false).isEmpty()) {
                config.getConfigurationSection("фигуры").getKeys(false).stream().forEach( (id) -> {

                    final int figureId = Integer.parseInt(id);
                    //final Location location = LocationUtil.LocFromString(config.getString("фигуры."+id+".location"), false);
                    final EntityType entityType = config.getString("фигуры."+id+".entityType")==null ? EntityType.ARMOR_STAND : EntityType.valueOf(config.getString("фигуры."+id+".entityType"));
                    final FigureType type = FigureType.fromString(config.getString("фигуры."+id+".type"));
                    final Game game = Game.fromServerName(config.getString("фигуры."+id+".gameType"));
                    
                    final Figure figure = new Figure( figureId, entityType, type, game, config.getString("фигуры."+id+".location"));

                    figure.name = (config.getString("фигуры."+id+".name"));
                    figure.tag = config.getString("фигуры."+id+".tag")==null ? "": config.getString("фигуры."+id+".tag");
                    figure.leftclickcommand = config.getString("фигуры."+id+".leftclickcommand");
                    figure.rightclickcommand = config.getString("фигуры."+id+".rightclickcommand");
                    //figure.lastUuid = UUID.fromString(config.getString("фигуры."+id+".lastUuid"));
                    figures.put(figureId, figure);
                    ids.add(figureId);
                });
                
                Ostrov.log_ok("Загружено фигур: "+figures.size());
            }
        } catch (Exception ex) {
            Ostrov.log_err("Ошибка загрузки фигуры "+ex.getMessage());
            //ex.printStackTrace();
        }

        
        if (task!=null) {
            task.cancel();
        }
        task = new BukkitRunnable() {
            
            Figure f;
            //Chunk chunk;
            Location spawnLoc;
            int index = -1; //чтобы начало с первой
            
            @Override
            public void run() {
                
                if (ids.isEmpty()) return;
                index++;
                if (index>=ids.size()) index = 0;
                f = figures.get(ids.get(index));
                
                if (Timer.getTime()-f.lastCheck < 10) {
                    return;
                }
                f.lastCheck = Timer.getTime();
                
                if (f.spawn_try>=10 || Bukkit.getWorld(f.worldName)==null) {
                    return;
                }

                if (f.getEntityType()==null) {
                    Ostrov.log_err("тип энтити фигуры "+f.name+":"+f.type+" не указан.");
                    f.spawn_try++;
                    return;
                }

                //chunk = Bukkit.getWorld(f.worldName).getChunkAt(f.x>>4, f.z>>4);

//System.out.println(""+f.name+" chunk isLoaded?"+chunk.isLoaded()+" isEntitiesLoaded?"+chunk.isEntitiesLoaded()); 
                spawnLoc = f.getSpawnLocation();
                if (spawnLoc==null) {
                    Ostrov.log_err("локация фигуры "+f.name+":"+f.type+" недоступна.");
                    f.spawn_try++;
                    return;
                }         
                
                if (!spawnLoc.getChunk().isLoaded() || !spawnLoc.getChunk().isEntitiesLoaded() || !hasNearbyPlayers(spawnLoc)) { //пока чанк неактивен, ничего не пытаемся делать
                    return;
                }
                
                //if (!hasNearbyPlayers(spawnLoc)) { //пока чанк неактивен, ничего не пытаемся делать
//System.out.println(""+f.name+"!hasNearbyPlayers"); 
                //    return;
                //}



                if (spawnLoc.getBlock().getType().isSolid() && spawnLoc.getBlock().getRelative(BlockFace.UP).getType().isSolid()) {
                    Ostrov.log_err("фигура "+f.name+":"+f.type+" замурована.");
                    f.spawn_try++;
                    return;
                }
                
                
                
                if (f.entity==null) { //энтити не была определена - так будет только при первой проверке после загрузки!
                    
                    boolean found = false; //искать до первой, остальных удалять
                    
                    for (final Entity e : spawnLoc.getChunk().getEntities()) {
                        if (e.getType() == f.getEntityType()) {
                        	final Integer id = e.getPersistentDataContainer().get(key, PersistentDataType.INTEGER);
                            if (id != null && id == f.figureId) {
                                if (!found) { //привязать только первую, остальных удалить
                                    found = true;
                                    f.entity = e;
                                    //uuid.put(e.getUniqueId(), f.figureId);
                                    if (f.lastName!=null) {
                                        f.setName(f.lastName);
                                        f.lastName = null;
                                    }
                                    Bukkit.getPluginManager().callEvent(new FigureActivateEntityEvent(f));
                                } else {
                                    e.remove();
                                }
                                 
                            } else if (e.getLocation().getBlockX()==f.x && e.getLocation().getBlockY()==f.y && e.getLocation().getBlockZ()==f.z) {
                                e.remove(); //очистить дубли на той же координате
                            }

                        }
                    }
                }                  

                //если чанк и энтити загружены, но энтити дохлая - пытаться переспавнить
                if (f.entity==null || f.entity.isDead()) { //энтити не найдена - удалить активный ид и создать новую

                    f.entity = spawnLoc.getWorld().spawnEntity(spawnLoc, f.getEntityType(), false);
                    
                    if (f.lastName!=null) {
                        f.setName(f.lastName);
                        f.lastName = null;
                    }
                    equipFigure(f, f.entity, f.figureId); //в конфиге уже всё будет, сохраняется при создании! prepare(this);-выполнится в equipFigure
                    f.setDisplayName(f.name);
                    
                    f.spawn_try++;
                    if (f.spawn_try==10) {
                        Ostrov.log_err("10 неудачных спавнов фигуры "+f.name+":"+f.type+", отключаем.");
                    }
                    Bukkit.getPluginManager().callEvent(new FigureActivateEntityEvent(f));

                } else {

                    if (f.entity.getLocation().getBlockX()!=f.x || f.entity.getLocation().getBlockY()!=f.y || f.entity.getLocation().getBlockZ()!=f.z) {
                        f.teleport(spawnLoc); //вернуть на место, если переместилась
                    }

                }
                
                
                
            }
        }.runTaskTimer(Ostrov.instance, 5, 9);
     
    }

    @Override
    public void onDisable() {
    }

    
    private static boolean hasNearbyPlayers(final Location loc) {
        for (Player p : loc.getWorld().getPlayers()) {
            if (Math.abs(loc.getChunk().getX()-p.getChunk().getX())<2 && Math.abs(loc.getChunk().getZ()-p.getChunk().getZ())<2) {
                return true;
            }
        }
        return false;
    }
    
    
    
    protected static boolean isFigure(final Entity e) {
        return e!=null && !e.getPersistentDataContainer().isEmpty() && e.getPersistentDataContainer().has(key, PersistentDataType.INTEGER);
    }

    protected static Collection<Figure> getFigures() {
        return figures.values();
    }

    protected static Figure getFigure(final Entity e) {
        if (isFigure(e)) return figures.get(e.getPersistentDataContainer().get(key, PersistentDataType.INTEGER));//return figures.get(uuid.get(entityUUID));
        return null;
    }

    

    
    //сохранение из меню настроек или создания (в момент выбора типа)
    //или при спавне новой энтити
    
    protected static void saveFigure(final Player p, final Figure figure) {
        
        final Entity entity = figure.entity;//Link.get();
        if (!figures.containsKey(figure.figureId)) { //новая фигура, была создана, но не запомнена
            figures.put(figure.figureId, figure);
            ids.add(figure.figureId);
            prepare(entity, figure.figureId);
        }

        config.set("фигуры."+figure.figureId+".type", String.valueOf(figure.getType())); //тип игры или command
        config.set("фигуры."+figure.figureId+".entityType", String.valueOf(figure.getEntityType())); //тип игры или command
        config.set("фигуры."+figure.figureId+".name", figure.name);
        config.set("фигуры."+figure.figureId+".tag", figure.getTag());
        config.set( "фигуры."+figure.figureId+".gameType", figure.game==null ? "": figure.game.name().toLowerCase() );
        config.set("фигуры."+figure.figureId+".leftclickcommand", figure.leftclickcommand);
        config.set("фигуры."+figure.figureId+".rightclickcommand", figure.rightclickcommand);
        
        config.set("фигуры."+figure.figureId+".location", figure.worldName+":"+figure.x+":"+figure.y+":"+figure.z+":"+figure.yaw+":"+figure.pitch );//    LocationUtil.StringFromLocWithYawPitch(figure.getLocation()) );

        EntityEquipment equipment = null;
        
        if (figure.getEntityType()==EntityType.ARMOR_STAND) {
            final ArmorStand as = (ArmorStand) entity;
            config.set("фигуры."+figure.figureId+".head.x", as.getHeadPose().getX());
            config.set("фигуры."+figure.figureId+".head.y", as.getHeadPose().getY());
            config.set("фигуры."+figure.figureId+".head.z", as.getHeadPose().getZ());

            config.set("фигуры."+figure.figureId+".body.x", as.getBodyPose().getX());
            config.set("фигуры."+figure.figureId+".body.y", as.getBodyPose().getY());
            config.set("фигуры."+figure.figureId+".body.z", as.getBodyPose().getZ());

            config.set("фигуры."+figure.figureId+".leftarm.x", as.getLeftArmPose().getX());
            config.set("фигуры."+figure.figureId+".leftarm.y", as.getLeftArmPose().getY());
            config.set("фигуры."+figure.figureId+".leftarm.z", as.getLeftArmPose().getZ());

            config.set("фигуры."+figure.figureId+".rightarm.x", as.getRightArmPose().getX());
            config.set("фигуры."+figure.figureId+".rightarm.y", as.getRightArmPose().getY());
            config.set("фигуры."+figure.figureId+".rightarm.z", as.getRightArmPose().getZ());

            config.set("фигуры."+figure.figureId+".leftleg.x", as.getLeftLegPose().getX());
            config.set("фигуры."+figure.figureId+".leftleg.y", as.getLeftLegPose().getY());
            config.set("фигуры."+figure.figureId+".leftleg.z", as.getLeftLegPose().getZ());

            config.set("фигуры."+figure.figureId+".rightleg.x", as.getRightLegPose().getX());
            config.set("фигуры."+figure.figureId+".rightleg.y", as.getRightLegPose().getY());
            config.set("фигуры."+figure.figureId+".rightleg.z", as.getRightLegPose().getZ());
            
            equipment = as.getEquipment();
            
        } else if (entity instanceof LivingEntity livingEntity) {
            
            equipment = livingEntity.getEquipment();
        }
        
        if (equipment!=null) {
            config.set("фигуры."+figure.figureId+".helmet", equipment.getHelmet().getType()==Material.AIR ? "none" : ItemUtils.toString(equipment.getHelmet(),"<>"));
            config.set("фигуры."+figure.figureId+".chestplate", equipment.getChestplate().getType()==Material.AIR ? "none" : ItemUtils.toString(equipment.getChestplate(),"<>"));
            config.set("фигуры."+figure.figureId+".leggins", equipment.getLeggings().getType()==Material.AIR ? "none" : ItemUtils.toString(equipment.getLeggings(),"<>"));
            config.set("фигуры."+figure.figureId+".boots", equipment.getBoots().getType()==Material.AIR ? "none" : ItemUtils.toString(equipment.getBoots(),"<>"));
            config.set("фигуры."+figure.figureId+".lefthand", equipment.getItemInMainHand().getType()==Material.AIR ? "none" : ItemUtils.toString(equipment.getItemInMainHand(),"<>"));
            config.set("фигуры."+figure.figureId+".righhand", equipment.getItemInOffHand().getType()==Material.AIR ? "none" : ItemUtils.toString(equipment.getItemInOffHand(),"<>"));
        } else {
            config.set("фигуры."+figure.figureId+".helmet", "none");
            config.set("фигуры."+figure.figureId+".chestplate", "none");
            config.set("фигуры."+figure.figureId+".leggins", "none");
            config.set("фигуры."+figure.figureId+".boots", "none");
            config.set("фигуры."+figure.figureId+".lefthand", "none");
            config.set("фигуры."+figure.figureId+".righhand", "none");
        }
        
        config.saveConfig();
        config.reloadConfig();
        
        if (p!=null) {
            p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
            p.sendMessage("§aФигура сохранена!");
        }

    }

    
    protected static void setNewPosition(Player p, Figure figure) {
        if (figure.entity!=null && !figure.entity.isDead()) {
            figure.worldName = p.getWorld().getName();
            figure.x = p.getLocation().getBlockX();
            figure.y = p.getLocation().getBlockY();
            figure.z = p.getLocation().getBlockZ();
            figure.yaw = (int) p.getLocation().getYaw();
            figure.pitch = (int) p.getLocation().getPitch();
            figure.spawnLoc = null; //сброс для обновы при тп
            figure.teleport(figure.getSpawnLocation());
            saveFigure(p, figure);
        }    
    }
    
    
    protected static void deleteFigure(final Figure figure) {
        ids.remove(ids.indexOf(figure.figureId));//ids.remove(Integer.valueOf(figure.figureId)); //удалить как объект, а не индекс!
        figures.remove(figure.figureId);
        config.removeKey("фигуры."+figure.figureId);
        config.saveConfig();
        config.reloadConfig();
        if (figure.entity!=null && !figure.entity.getPersistentDataContainer().isEmpty() && figure.entity.getPersistentDataContainer().has(key, PersistentDataType.INTEGER)) {
            figure.entity.getPersistentDataContainer().remove(key);
        }
   }
    
    
    
    
    
    
    
    protected static void equipFigure (final Figure figure, final Entity entity, final int figureId) {

        prepare(entity, figureId);
 
        if (figure.getEntityType()==EntityType.ARMOR_STAND) {
            
            final ArmorStand as = (ArmorStand) entity;
            EulerAngle angle;
            angle = new EulerAngle( config.getDouble("фигуры."+figure.figureId+".head.x"), config.getDouble("фигуры."+figure.figureId+".head.y"), config.getDouble("фигуры."+figure.figureId+".head.z") );
            as.setHeadPose(angle);
            angle = new EulerAngle( config.getDouble("фигуры."+figure.figureId+".body.x"), config.getDouble("фигуры."+figure.figureId+".body.y"), config.getDouble("фигуры."+figure.figureId+".body.z") );
            as.setBodyPose(angle);
            angle = new EulerAngle( config.getDouble("фигуры."+figure.figureId+".leftarm.x"), config.getDouble("фигуры."+figure.figureId+".leftarm.y"), config.getDouble("фигуры."+figure.figureId+".leftarm.z") );
            as.setLeftArmPose(angle);
            angle = new EulerAngle( config.getDouble("фигуры."+figure.figureId+".rightarm.x"), config.getDouble("фигуры."+figure.figureId+".rightarm.y"), config.getDouble("фигуры."+figure.figureId+".rightarm.z") );
            as.setRightArmPose(angle);
            angle = new EulerAngle( config.getDouble("фигуры."+figure.figureId+".leftleg.x"), config.getDouble("фигуры."+figure.figureId+".leftleg.y"), config.getDouble("фигуры."+figure.figureId+".leftleg.z") );
            as.setLeftLegPose(angle);
            angle = new EulerAngle( config.getDouble("фигуры."+figure.figureId+".rightleg.x"), config.getDouble("фигуры."+figure.figureId+".rightleg.y"), config.getDouble("фигуры."+figure.figureId+".rightleg.z") );
            as.setRightLegPose(angle);
            
            if (!config.getString("фигуры."+figure.figureId+".helmet").equals("none")) as.getEquipment().setHelmet( ItemUtils.parseItem(config.getString("фигуры."+figure.figureId+".helmet"),"<>") );
            if (!config.getString("фигуры."+figure.figureId+".chestplate").equals("none")) as.getEquipment().setChestplate(ItemUtils.parseItem( config.getString("фигуры."+figure.figureId+".chestplate"),"<>") );
            if (!config.getString("фигуры."+figure.figureId+".leggins").equals("none")) as.getEquipment().setLeggings(ItemUtils.parseItem( config.getString("фигуры."+figure.figureId+".leggins"),"<>") );
            if (!config.getString("фигуры."+figure.figureId+".boots").equals("none")) as.getEquipment().setBoots(ItemUtils.parseItem( config.getString("фигуры."+figure.figureId+".boots"),"<>") );
            if (!config.getString("фигуры."+figure.figureId+".lefthand").equals("none")) as.getEquipment().setItemInMainHand(ItemUtils.parseItem( config.getString("фигуры."+figure.figureId+".lefthand"),"<>") );
            if (!config.getString("фигуры."+figure.figureId+".righhand").equals("none")) as.getEquipment().setItemInOffHand( ItemUtils.parseItem( config.getString("фигуры."+figure.figureId+".righhand"),"<>") );

        } else if (entity instanceof LivingEntity le) {
            
            if (!config.getString("фигуры."+figure.figureId+".helmet").equals("none")) le.getEquipment().setHelmet( ItemUtils.parseItem( config.getString("фигуры."+figure.figureId+".helmet"),"<>") );
            if (!config.getString("фигуры."+figure.figureId+".chestplate").equals("none")) le.getEquipment().setChestplate(ItemUtils.parseItem( config.getString("фигуры."+figure.figureId+".chestplate"),"<>") );
            if (!config.getString("фигуры."+figure.figureId+".leggins").equals("none")) le.getEquipment().setLeggings(ItemUtils.parseItem( config.getString("фигуры."+figure.figureId+".leggins"),"<>") );
            if (!config.getString("фигуры."+figure.figureId+".boots").equals("none")) le.getEquipment().setBoots(ItemUtils.parseItem( config.getString("фигуры."+figure.figureId+".boots"),"<>") );
            if (!config.getString("фигуры."+figure.figureId+".lefthand").equals("none")) le.getEquipment().setItemInMainHand(ItemUtils.parseItem( config.getString("фигуры."+figure.figureId+".lefthand"),"<>") );
            if (!config.getString("фигуры."+figure.figureId+".righhand").equals("none")) le.getEquipment().setItemInOffHand( ItemUtils.parseItem( config.getString("фигуры."+figure.figureId+".righhand"),"<>") );
            le.setCollidable(false);
        }

        entity.setCustomNameVisible(true);

    }
    
    
    
    public static void prepare( final Entity entity, final int figureId) {
//System.out.println("---------prepare()");
        entity.setGravity(false);
        entity.setInvulnerable(false); //не ставить true или не работает ЛКМ!!!
        entity.setSilent(true);
        entity.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, figureId);
        entity.setPersistent(true);
        
        if (entity.getType()==EntityType.ARMOR_STAND) {
            
            final ArmorStand as = (ArmorStand)entity;
            as.setAI(false);
            as.setCanPickupItems(false);
            as.setCollidable(false);
            //as.setSmall(true);
            //as.setMarker(true);
            as.setVisible(true);
            as.setBasePlate(false);
            as.setArms(true);
            as.setRemoveWhenFarAway(false);
            
        } else if (entity instanceof LivingEntity le) {
            
            le.setAI(false);
            le.setCanPickupItems(false);
            le.setCollidable(false);
            le.setRemoveWhenFarAway(false);
            
            le.setCollidable(false);
            //if(entity.getType()==EntityType.WANDERING_TRADER) {
            //    WanderingTrader wt = (WanderingTrader) entity;
            //    wt.getEquipment().
            //}
        }
    }

    
    
    
    
    

    
 



    



    
    
    
}
