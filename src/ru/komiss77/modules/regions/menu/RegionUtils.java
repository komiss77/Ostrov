package ru.komiss77.modules.regions.menu;

@Deprecated
public class RegionUtils {

}
/*
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.internal.platform.WorldGuardPlatform;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.managers.storage.StorageException;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import ru.komiss77.ApiOstrov;
import ru.komiss77.enums.Game;
import ru.komiss77.modules.games.GM;
import ru.komiss77.modules.regions.menu.LandBuyMenu;
import ru.komiss77.modules.regions.menu.RegionSelectMenu;
import ru.komiss77.utils.TimeUtil;
import ru.komiss77.utils.inventory.SmartInventory;


public class RegionUtils {

    private static WorldGuardPlatform platform;
    
    static {
        RegionUtils.platform = WorldGuard.getInstance().getPlatform();
    }
/
    private void checkForRegions(Player player) {
        //RegionManager regionManager = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt((org.bukkit.World)player.getWorld()));
        final LocalPlayer lp = WorldGuardPlugin.inst().wrapPlayer(player);
        final com.sk89q.worldguard.protection.managers.RegionManager rm = RegionUtils.getRegionManager(player.getWorld());
        final BlockVector3 bv = BukkitAdapter.asBlockVector(player.getLocation());
        final ApplicableRegionSet applicableRegionSet = rm.getApplicableRegions(bv);

        if (applicableRegionSet.size() == 0) { //нет приватов в точке нахождения, открываем меню покупки - добавить тп в свои регионы
            if (GM.GAME == Game.DA) {
                final Location spl = player.getWorld().getSpawnLocation();
                final int dst = Math.max(Math.abs(bv.x() - spl.getBlockX()), Math.abs(bv.y() - spl.getBlockZ()));
                if (dst > RM.NO_CLAIM_AREA && !ApiOstrov.isLocalBuilder(player, true)) {
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 0.8f);
                    player.sendMessage("§cПривыты можно создавать только в §6" + RM.NO_CLAIM_AREA + " §cблоках от спавна. \nТвоя дистанция - §6" + dst + " §cблоков. Тп на спавн - §6/spawn");
                    return;
                }
            }

            SmartInventory.builder()
                .id("regiongui.claim")
                .provider(new LandBuyMenu())
                .size(5, 9)
                .title("§fПокупка региона")
                .build()
                .open(player);




        } else if (applicableRegionSet.size() == 1) { //только один приват в точке нахождения, открываем меню управления им

            final ProtectedRegion rg = applicableRegionSet.getRegions().iterator().next();

            if ( rg.isOwner(lp) ) { //если владелец, меню управления

                RM.openRegionOwnerMenu(player, rg);

            } else { //стоим в чужом привате!  //посылаем гулять или предложить тп в свои регионы

                //if (player.hasPermission("region.mod")) { //модератор - управление чужим приватом
                //    SmartInventory.builder(). provider(new RegionManagerInterface(rg)) .size(3) .title(Language.INTERFACE_MANAGE_TITLE.toString()) .build().open(player);
                // } else {
                if (rg.isMember(lp)) {
                    player.sendMessage("§5В этом регионе у вас права пользователя.");
                } else {
                    player.sendMessage("§cВы в чужом регионе.");
                }

                if (RegionUtils.getPlayerUserRegions(player).isEmpty()) {

                    TextComponent msg = new TextComponent( "§fНажмите сюда для ТП в свободное место!" );
                    msg.setHoverEvent( new HoverEvent(HoverEvent.Action.SHOW_TEXT,  new ComponentBuilder("§aКлик - случайный телепорт в свободное место").create()));
                    msg.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpr" ) );
                    player.spigot().sendMessage( msg);

                } else {

                    TextComponent msg = new TextComponent( "§fНажмите сюда для перехода в свой регион!" );
                    msg.setHoverEvent( new HoverEvent(HoverEvent.Action.SHOW_TEXT,  new ComponentBuilder("§aКлик-открыть меню ТП в свой регион").create()));
                    msg.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/land home" ) );
                    player.spigot().sendMessage( msg);

                }

            }





        } else  if (applicableRegionSet.size() > 1) {   //несколько приватов в точке нахождения

            //находим приваты игрока в данной точке
            final List <ProtectedRegion> playerOwndeRegions = new ArrayList<>();//RegionUtils.getPlayerRegions(player);
            for (final ProtectedRegion rg : applicableRegionSet.getRegions()) {
                if (rg.isOwner(lp)) {
                    playerOwndeRegions.add(rg);
                }
            }
            //если его приватов тут нет, посылаем гулять или предложить тп в свои регионы
            if (playerOwndeRegions.isEmpty()) {

                player.sendMessage("§eВы в чужом регионе! Отойдите за его границы.");

                if (RegionUtils.getPlayerUserRegions(player).isEmpty()) {

                    TextComponent msg = new TextComponent( "§fНажмите сюда для ТП в свободное место!" );
                    msg.setHoverEvent( new HoverEvent(HoverEvent.Action.SHOW_TEXT,  new ComponentBuilder("§aКлик - случайный телепорт в свободное место").create()));
                    msg.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpr" ) );
                    player.spigot().sendMessage( msg);

                } else {

                    TextComponent msg = new TextComponent( "§fНажмите сюда для перехода в свой регион!" );
                    msg.setHoverEvent( new HoverEvent(HoverEvent.Action.SHOW_TEXT,  new ComponentBuilder("§aКлик-открыть меню ТП в свой регион").create()));
                    msg.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/land home" ) );
                    player.spigot().sendMessage( msg);

                }


            } else {

                //несколько приватов в точке нахождения, открываем меню выбора каким управлять
                SmartInventory.builder()
                    .id("regiongui.regionselect")
                    .provider( new RegionSelectMenu(playerOwndeRegions))
                    .size(3)
                    .title("§fВыбор региона")
                    .build()
                    .open(player);

            }


        }



    }





    
    public static boolean saveRegions(final World world) {
        try {
            RegionUtils.platform.getRegionContainer().get(BukkitAdapter.adapt(world)).saveChanges();
            return true;
        } catch (StorageException ex) {
            ex.printStackTrace();
            return false;
        }
    }
    
    public static RegionManager getRegionManager(final World world) {
        return RegionUtils.platform.getRegionContainer().get(BukkitAdapter.adapt(world));
    }

    public static String getTemplateName (final ProtectedRegion region) {
        if (isValidRegionId(region.getId()))  {
 //System.out.println("getTemplateName ="+region.getId().split("-")[2]);
           return region.getId().split("-")[2];
        } else {
            return "";
        }
    }

    public static String getCreateTime (final ProtectedRegion region) {
        if (isValidRegionId(region.getId()))  {
            return TimeUtil.dateFromStamp( Integer.valueOf(region.getId().split("-")[3]));
        } else {
            return "";
        }
    }    
    
    // komiss77-rgui-template-timestamp
    public static boolean isValidRegionId(final String regionId) {
//System.out.println("isValidRegionId 1");
        if (regionId.contains("-")) {
            String[] split = regionId.split("-");
//System.out.println("isValidRegionId "+split[0]+" "+split[1]+" "+split[2]+" "+split[3]+ "integer?"+ApiOstrov.isInteger(split[3]));
            if ( split.length==4 && split[1].equals("rgui") && ApiOstrov.isInteger(split[3])) {
                return true;
            }
        } 
        return false;
    }
    
    
    
    
    
    
    
    
    
}
*/


    /*public static List<Vector> getLocationsFromRegion(final ProtectedRegion region) {
        final BlockVector3 minimumPoint = region.getMinimumPoint();
        final BlockVector3 maximumPoint = region.getMaximumPoint();
        final int n = region.getMaximumPoint().y() - region.getMinimumPoint().y();
        final ArrayList<Vector> list = new ArrayList<>();
        final ArrayList<Vector> list2 = new ArrayList<>();
        list2.add(new Vector(minimumPoint.x(), minimumPoint.y(), minimumPoint.z()));
        list2.add(new Vector(maximumPoint.x(), minimumPoint.y(), minimumPoint.z()));
        list2.add(new Vector(maximumPoint.x(), minimumPoint.y(), maximumPoint.z()));
        list2.add(new Vector(minimumPoint.x(), minimumPoint.y(), maximumPoint.z()));
        for (int i = 0; i < list2.size(); ++i) {
            final Vector vector = list2.get(i);
            Vector p2;
            if (i + 1 < list2.size()) {
                p2 = list2.get(i + 1);
            }
            else {
                p2 = list2.get(0);
            }
            final Vector add = vector.add(new Vector(0, n, 0));
            final Vector add2 = p2.add(new Vector(0, n, 0));
            list.addAll(regionLine(vector, p2));
            list.addAll(regionLine(add, add2));
            list.addAll(regionLine(vector, add));
            for (double n2 = 2.0; n2 < n; n2 += 2.0) {
                list.addAll(regionLine(vector.add(new Vector(0.0, n2, 0.0)), p2.add(new Vector(0.0, n2, 0.0))));
            }
        }
        return list;
    }

    public static List<Vector> regionLine(final Vector p1, final Vector p2) {
        final ArrayList<Vector> list = new ArrayList<>();
        final int n = (int)(p1.distance(p2) / 1.0) + 1;
        final Vector multiply = p2.subtract(p1).normalize().multiply(p1.distance(p2) / (n - 1));
        for (int i = 0; i < n; ++i) {
            list.add(p1.add(multiply.multiply(i)));
        }
        return list;
    }





    public static List <ProtectedRegion> getPlayerOwnedRegions (final Player bukkitplayer) {
        final List <ProtectedRegion> playerRegions = new ArrayList<>();
        for (final World world : Bukkit.getWorlds()) {
            playerRegions.addAll(getPlayerOwnedRegions(bukkitplayer, world));
        }
        return playerRegions;
    }

    public static List <ProtectedRegion> getPlayerOwnedRegions (final Player bukkitplayer, final World world) {
        final LocalPlayer lp = WorldGuardPlugin.inst().wrapPlayer(bukkitplayer);
        final List <ProtectedRegion> playerRegions = new ArrayList<>();
        final String start = lp.getName().toLowerCase()+"-rgui-";
        for (final ProtectedRegion rg : getRegionManager(world).getRegions().values()) {
            if (rg.isOwner(lp) || rg.getId().startsWith(start)) {
                playerRegions.add(rg);
            }
        }
        return playerRegions;
    }

    public static List <ProtectedRegion> getPlayerUserRegions (final Player bukkitplayer) {
        final List <ProtectedRegion> playerRegions = new ArrayList<>();
        for (final World world : Bukkit.getWorlds()) {
            playerRegions.addAll(getPlayerUserRegions(bukkitplayer, world));
        }
        return playerRegions;
    }

    public static List <ProtectedRegion> getPlayerUserRegions (final Player bukkitplayer, final World world) {
        final LocalPlayer lp = WorldGuardPlugin.inst().wrapPlayer(bukkitplayer);
        final List <ProtectedRegion> playerRegions = new ArrayList<>();
        for (final ProtectedRegion rg : getRegionManager(world).getRegions().values()) {
            //if (rg.isMember(lp) && rg.getId().startsWith(lp.getName()+"-rgui-")) {
            if (rg.isMember(lp)) {
                playerRegions.add(rg);
            }
        }
        return playerRegions;
    }
 */