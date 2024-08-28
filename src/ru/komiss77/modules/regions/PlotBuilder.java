package ru.komiss77.modules.regions;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.domains.PlayerDomain;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Ostrov;
import ru.komiss77.modules.world.WE;


public class PlotBuilder {
  private final Player p;
  private final Template t;
  private final Location loc;
  private final com.sk89q.worldguard.protection.managers.RegionManager manager;
  private final LocalPlayer localPlayer;


  public PlotBuilder(final Player player, final Template claimTemplate) {
    this.p = player;
    this.t = claimTemplate;
    this.loc = player.getLocation();
    this.manager = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(player.getWorld()));
    this.localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);
  }

  public void build() {
    RM.stopPrewiev(p);
    //final int blockX = this.loc.getBlockX();
    //final int blockZ = this.loc.getBlockZ();
    int blockY = this.loc.getBlockY();

    if (blockY > 30) blockY = 30;

    //final int size = this.claimTemplate.getSize();
//System.out.println("claimTemplate size="+size);        
    if (t.permission && !p.hasPermission(Template.PERM_FOR_ALl) && !p.hasPermission(t.permission())) {
      p.sendMessage("§6Нет права " + t.permission());
      return;
    }
    final int price = ApiOstrov.isLocalBuilder(p) ? 0 : t.price;
    //if (!RegionGUI.econ.withdrawPlayer((OfflinePlayer)this.player.getPlayer(), (double)price).transactionSuccess()) {

    if (ApiOstrov.moneyGetBalance(p.getName()) < price) {
      p.sendMessage("§6Недостаточно лони для покупки региона!");
      return;
    }
    //final int halfSize = (int)Math.round(size / 2.0);
    final Vector top = t.getMaximumPoint(loc).toVector();//new Vector(down.getBlockX() + size, blockY + claimTemplate.getHeight(), down.getBlockZ() + size); //на 1 меньше, т.к. включая
    final Vector down = t.getMinimumPoint(loc).toVector();//new Vector(blockX - halfSize, blockY - claimTemplate.getDepth(), blockZ - halfSize); //находим нижний угол
    //final Vector top = new Vector(blockX + n, blockY + claimTemplate.getHeight(), blockZ + n);

    final String regName = this.p.getName() + "-rgui-" + t.name + "-" + ApiOstrov.currentTimeSec();

    //final ProtectedCuboidRegion region = new ProtectedCuboidRegion( this.plugin.getConfig().getString("region-identifier").replace("%player%", this.player.getName()).replace("%displayname%", this.regionName), BlockVector3.at(vector.getBlockX(), vector.getBlockY(), vector.getBlockZ()), BlockVector3.at(vector2.getBlockX(), vector2.getBlockY(), vector2.getBlockZ()));
    final ProtectedCuboidRegion region = new ProtectedCuboidRegion(regName,
        BlockVector3.at(top.getBlockX(), top.getBlockY(), top.getBlockZ()),
        BlockVector3.at(down.getBlockX(), down.getBlockY(), down.getBlockZ())
    );

    if (manager.overlapsUnownedRegion(region, localPlayer)) {// && this.plugin.getConfig().getBoolean("preventRegionOverlap", true)) {
      p.sendMessage("§6Ругион перекрывается с другим!");
      return;
    }

    //добавление владельца
    final DefaultDomain owners = region.getOwners();
    final PlayerDomain playerDomain = owners.getPlayerDomain();
    //WorldGuard.getInstance().getProfileCache().put(new Profile(this.player.getUniqueId(), this.player.getName()));
    //playerDomain.addPlayer(this.player.getUniqueId());
    playerDomain.addPlayer(p.getName());
    owners.setPlayerDomain(playerDomain);
    region.setOwners(owners);
    //region.setFlag((Flag)Flags.TELE_LOC, (Object)BukkitAdapter.adapt(p.getLocation()));

//System.out.println("claimTemplate size="+size+" region "+region.getMinimumPoint()+"  "+region.getMaximumPoint());        


    ApiOstrov.moneyChange(p.getName(), -price, "Покупка региона");
    //this.player.sendMessage(Language.REGION_CREATE_MONEY.toChatString().replace("%money%", new StringBuilder().append(price).toString()).replace("%currencyname%", RegionGUI.econ.currencyNameSingular()));
    region.setDirty(true);
    manager.addRegion(region);
    p.sendMessage("§aРегион успешно создан!");


    if (RM.regenOnDelete) {
      //ApiOstrov.getWorldEditor().save(player, BukkitAdapter.adapt(player.getWorld(), region.getMinimumPoint()), BukkitAdapter.adapt(player.getWorld(), region.getMaximumPoint()), RegionGUI.getInstance().getDataFolder() + "/schematics", regName.toLowerCase(), true);
      WE.save(p, BukkitAdapter.adapt(p.getWorld(), region.getMinimumPoint()), BukkitAdapter.adapt(p.getWorld(), region.getMaximumPoint()), regName.toLowerCase(), "");
    }


    if (t.borderMaterial != null) {
      final World world = p.getWorld();
      new BukkitRunnable() {
        @Override
        public void run() {
          //editSession.flushQueue();
          //Walls walls = new Walls(world, mat, region);
          Walls walls = new Walls(world, t.borderMaterial, down.toLocation(world), t.size);
        }
      }.runTaskLater(Ostrov.getInstance(), 30);

    }


  }

}
