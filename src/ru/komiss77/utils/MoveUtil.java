package ru.komiss77.utils;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockType;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Levelled;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.block.data.type.Snow;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitRunnable;
import ru.komiss77.Ostrov;
import ru.komiss77.modules.world.LocFinder;
import ru.komiss77.modules.world.WXYZ;
import ru.komiss77.version.Nms;


//не переименовывать! юзают все плагины!
public class MoveUtil {

    //учесть поиск дна океана и лавы
    //p может быть null
    //  findSaveLocation вернуть GameMode.CREATIVE
    private static final int MAX_DST = 1;
    private static final LocFinder.DataCheck[] SAFE_CHECK = {
        (dt, y) -> {
            final BlockType bt = dt.getMaterial().asBlockType();
            switch (dt) {
                case final Snow sn: return sn.getLayers() > 4;
                case final Waterlogged sn: return sn.isWaterlogged() || bt.isSolid();
                case final Levelled lv: return lv.getLevel() == 0 //вода для кувшинок
                    && BlockType.WATER.equals(bt);
                default: break;
            }
            if (BlockType.BEDROCK.equals(bt)) return false;
//            if (BlockType.LAVA.equals(bt)) Bukkit.getConsoleSender().sendMessage("sl-" + bt.isSolid());
            return bt.isSolid();//крыша мира (как в незере)
        },
        (dt, y) -> {
            final BlockType bt = dt.getMaterial().asBlockType();
            return switch (dt) {
                case final Snow sn -> sn.getLayers() < 5;
                case final Waterlogged sn -> !sn.isWaterlogged();
                case final Levelled lv -> lv.getLevel() < 8 //не падающая вода
                    && BlockType.WATER.equals(bt);
                default -> LocUtil.isPassable(bt);
            };
        },
        (dt, y) -> {
            final BlockType bt = dt.getMaterial().asBlockType();
            return switch (dt) {
                case final Snow sn -> sn.getLayers() < 5;
                case final Waterlogged sn -> !sn.isWaterlogged();
                case final Levelled lv -> lv.getLevel() < 8 //не падающая вода
                    && BlockType.WATER.equals(bt);
                default -> LocUtil.isPassable(bt);
            };
        }
    };

    private static final LocFinder.Check[] AIR_CHECK = {
        (LocFinder.TypeCheck) (dt, y) -> dt.isAir(),
        (LocFinder.TypeCheck) (dt, y) -> dt.isAir(),
        (LocFinder.TypeCheck) (dt, y) -> dt.isAir()
    };

    public static boolean safeTP(final Player p, final Location feetLoc) {
        final Location finLoc;
        final WXYZ loc = new LocFinder(new WXYZ(feetLoc), SAFE_CHECK).find(LocFinder.DYrect.BOTH, MAX_DST, 1);
        if (loc == null) {
            final WXYZ alc = new LocFinder(new WXYZ(feetLoc), AIR_CHECK).find(LocFinder.DYrect.BOTH, MAX_DST, 1);
            if (alc == null) return false;

            BlockUtil.set(alc.getBlock().getRelative(BlockFace.DOWN), BlockType.YELLOW_STAINED_GLASS, false);
            new BukkitRunnable() {
                final WeakReference<Player> prf = new WeakReference<>(p);

                @Override
                public void run() {
                    final Player pl = prf.get();
                    if (pl == null || !pl.isOnline() || pl.isDead()
                        || alc.distAbs(pl.getLocation()) > 3) {
                        BlockUtil.set(alc.getBlock().getRelative(BlockFace.DOWN), BlockType.AIR, false);
                        this.cancel();
                    }
                }
            }.runTaskTimer(Ostrov.instance, 30, 10);
            finLoc = alc.getCenterLoc();
            tpCorrect(p, finLoc);
            return true;
        }

        finLoc = loc.getCenterLoc();
        final Block b = finLoc.getBlock();
        if (!b.getType().isAir()) {
            tpCorrect(p, finLoc);
            return true;
        }

        final Block rb = b.getRelative(BlockFace.DOWN);
        if (BlockUtil.is(rb, BlockType.WATER)
            || (rb.getBlockData() instanceof final Waterlogged wl && wl.isWaterlogged())) {
            if (isDrySolid(Nms.fastData(loc.w(), loc.x, loc.y - 2, loc.z))) {
                tpCorrect(p, finLoc);
                return true;
            }
            BlockUtil.set(b, BlockType.LILY_PAD, false);
            new BukkitRunnable() {
                final WeakReference<Player> prf = new WeakReference<>(p);
                public void run() {
                    final Player pl = prf.get();
                    if (pl == null || !pl.isOnline() || pl.isDead()
                        || new WXYZ(b).distAbs(pl.getLocation()) > 3) {
                        BlockUtil.set(b, BlockType.AIR, false);
                        this.cancel();
                    }
                }
            }.runTaskTimer(Ostrov.instance, 30, 10);
        }

        tpCorrect(p, finLoc);
        return true;
    }

    private static void tpCorrect(final Player p, final Location loc) {
        p.setVelocity(p.getVelocity().zero());
        p.setFallDistance(0);
        loc.setYaw(p.getEyeLocation().getYaw());
        loc.setPitch(p.getEyeLocation().getPitch());
        p.teleport(loc, PlayerTeleportEvent.TeleportCause.COMMAND);
    }

    private static boolean isDrySolid(final BlockData dt) {
        return dt.getMaterial().asBlockType().isSolid()
            || (dt instanceof final Waterlogged sn && !sn.isWaterlogged());
    }

  @Deprecated // уже месяц прошел а баг с тп в потоки воды не пофикшены
    public static boolean teleportSave(final Player p, final Location feetLoc, final boolean buildSafePlace) {
//Ostrov.log("teleportSave feetBlock="+feetLoc);
//сначала попытка коррекций +1..-1 из-за непоняток с точкой в ногах или под ногами
        if (!Bukkit.isPrimaryThread()) {
            Ostrov.sync(() -> teleportSave(p, feetLoc, buildSafePlace));
            return true;
        }
        //аркаим и билдер кидает в небо - как-то не очень, пусть работает для всех, не особо грузит.
        //if (ApiOstrov.isLocalBuilder(p)) {
        //    p.sendMessage("teleportSave для билдера");
        //}
        //if (p.getGameMode() == GameMode.CREATIVE || p.getGameMode() == GameMode.SPECTATOR) {
        //    p.teleport(feetLoc.getWorld().getHighestBlockAt(feetLoc).getLocation(), PlayerTeleportEvent.TeleportCause.COMMAND);
        //    return true;
        //}

        //отфильтровка высоты за пределами по оси Y
      int y_max = feetLoc.getWorld().getMaxHeight();

      if (feetLoc.getWorld().getEnvironment() == World.Environment.NETHER
          && feetLoc.getWorld().getGenerator() == null) { //фикс по генератору для островов - тепешило под платформу
        y_max = feetLoc.getWorld().getHighestBlockYAt(feetLoc.getBlockX(), feetLoc.getBlockZ()) - 3;
      }

      final int y_min = feetLoc.getWorld().getMinHeight();
        if (feetLoc.getBlockY() > y_max) {
            feetLoc.setY(y_max);
        } else if (feetLoc.getBlockY() < y_min) {
          feetLoc.setY(y_min);
        }

        final WXYZ feetXYZ = new WXYZ(feetLoc);
        final int y_ori = feetXYZ.y;
        Integer best = null, bestFluid = null, bestAir = null;
//Ostrov.log_warn("y_min="+y_min+" y_max="+y_max+" getMaxHeight="+feetLoc.getWorld().getMaxHeight());

        Nms.PlaceType pt = Nms.PlaceType.DANGEROUS;
        for (int add = 0; add < feetLoc.getWorld().getMaxHeight(); add++) {
            if (best != null && bestFluid != null && bestAir != null) break;
//Ostrov.log_warn("+++++++ y=" + feetXYZ.y + " " + Nms.isSafeLocation(p, feetXYZ) + " add=-" + add + ((feetXYZ.y > y_max) ? " SKIP!!" : ""));
            feetXYZ.y = y_ori + add;
            if (feetXYZ.y < y_max) {
                pt = Nms.isSafeLocation(feetXYZ);
//Ostrov.log_warn("y="+feetXYZ.y+" "+pt+" add="+add);
                switch (pt) {
                    case DANGEROUS:
                        continue;
                    case SAFELY:
                        if (best == null) {
                            best = feetXYZ.y;//p.teleport(feetXYZ.getCenterLoc(), PlayerTeleportEvent.TeleportCause.COMMAND);
                        }
                        break;
                    case FLUID:
                        if (bestFluid == null) bestFluid = feetXYZ.y;
                        break;
                    case AIR:
                        if (bestAir == null) bestAir = feetXYZ.y;
                        break;
                }
            }

            if (add > 0) {
                feetXYZ.y = y_ori - add;
//Ostrov.log_warn("----------- y="+feetXYZ.y+" "+Nms.isSafeLocation(p, feetXYZ)+" add=-"+add+add+((feetXYZ.y > y_max)?" SKIP!!":""));
                if (feetXYZ.y > y_min) {
                    pt = Nms.isSafeLocation(feetXYZ);
//Ostrov.log_warn("y="+feetXYZ.y+" "+pt+" add=-"+add);
                    switch (pt) {
                        case DANGEROUS:
                            continue;
                        case SAFELY:
                            if (best == null)
                                best = feetXYZ.y;//p.teleport(feetXYZ.getCenterLoc(), PlayerTeleportEvent.TeleportCause.COMMAND);
                            break;
                        case FLUID:
                            if (bestFluid == null) bestFluid = feetXYZ.y;
                            break;
                        case AIR:
                            if (bestAir == null) bestAir = feetXYZ.y;
                            break;
                    }
                }
            }
        }

//Ostrov.log_warn("best="+best+" bestFluid="+bestFluid+" bestAir="+bestAir);
        //анализ точки по критериям:
        //найдено безопасное, но выше найдена вода - приоритет поверхности воды
        //безопсного не найдено - выбираем лучшее по очереди вода,воздух
        if (best != null) {
            if (bestFluid != null && bestFluid > best) { //чтобы не тепешило в пещеры под водой, приоритет уровню жидкости
                feetXYZ.y = bestFluid;
                pt = Nms.PlaceType.FLUID;
            } else {
                feetXYZ.y = best;
                pt = Nms.PlaceType.SAFELY;
            }
        } else if (bestFluid != null) {
            feetXYZ.y = bestFluid;
            pt = Nms.PlaceType.FLUID;
        } else if (bestAir != null) {
            feetXYZ.y = bestAir;
            pt = Nms.PlaceType.AIR;
        } else {
            pt = Nms.PlaceType.DANGEROUS;
        }

//Ostrov.log_warn("pt="+pt+" y="+feetXYZ.y);

        switch (pt) {
            case SAFELY -> {
                p.teleport(feetXYZ.getCenterLoc(), PlayerTeleportEvent.TeleportCause.COMMAND);
                return true;
            }
            case AIR -> {
                buildPlatform(p, feetXYZ, Material.YELLOW_STAINED_GLASS, Material.AIR);
                return true;
            }
            case FLUID -> {
                Material downMat = Nms.getFastMat(feetXYZ.w, feetXYZ.x, feetXYZ.y - 1, feetXYZ.z);
                Material platformMat = Material.YELLOW_STAINED_GLASS;
                if (downMat == Material.LAVA) {
                    feetXYZ.y = feetXYZ.y + 1; //поднять на 1 блок
                    downMat = Nms.getFastMat(feetXYZ.w, feetXYZ.x, feetXYZ.y - 1, feetXYZ.z);
                    //buldPlatform(p, feetXYZ, downMat, Material.AIR);
                } else if (downMat == Material.WATER) { //чекнуть на возможность кувшинки
                    Block underFeetBlock = feetXYZ.getBlock().getRelative(BlockFace.DOWN);//w.getBlockAt(feetXYZ.x, feetXYZ.y - 1, feetXYZ.z)
                    BlockData bd = underFeetBlock.getBlockData();
//Ostrov.log_warn("downMat="+downMat+" bd="+bd);
                    if (bd instanceof Levelled lv) { //разливаться только с уменьшением (не давать столбы) и не расползаться в стороны по воздуху
//Ostrov.log_warn("lvl="+lv.getLevel()+" min="+lv.getMinimumLevel()+" max="+lv.getMinimumLevel());
                        if (lv.getLevel() == lv.getMinimumLevel()) {
                            final Location loc = feetXYZ.getCenterLoc();
                            final Material backMat = loc.getBlock().getType();
                            loc.getBlock().setType(Material.LILY_PAD);
                            new BukkitRunnable() {
                                final String name = p.getName();

                                @Override
                                public void run() {
                                    final Player pl = Bukkit.getPlayerExact(name);
                                    if (pl == null || !pl.isOnline() || pl.isDead() || pl.getLocation().getBlockX() != feetXYZ.x || pl.getLocation().getBlockZ() != feetXYZ.z) {
                                        this.cancel();
                                        loc.getBlock().setType(backMat);
                                    }
                                }
                            }.runTaskTimer(Ostrov.instance, 30, 10);
                            p.teleport(loc, PlayerTeleportEvent.TeleportCause.COMMAND);
                            return true;
                        }
                    }
                }
                buildPlatform(p, feetXYZ, platformMat, downMat);
                return true;
            }
            case DANGEROUS -> {
            }
        }


        //совсем всё плохо - приходим сюда
        feetXYZ.y = y_ori;
        if (buildSafePlace) {
            //final WXYZ feetXYZ = new WXYZ(feetLoc);
            final Material[] oldMat = {
                Nms.getFastMat(feetXYZ.w, feetXYZ.x, feetXYZ.y + 1, feetXYZ.z), //что было в голове
                Nms.getFastMat(feetXYZ.w, feetXYZ.x, feetXYZ.y, feetXYZ.z),//что было в ногах
                Nms.getFastMat(feetXYZ.w, feetXYZ.x, feetXYZ.y - 1, feetXYZ.z)//что было под ногами
            };
            new BukkitRunnable() { //чтобы не давало новое ТПР пока не сошел с места
                final String name = p.getName();

                @Override
                public void run() {
                    final Player pl = Bukkit.getPlayerExact(name);
                    if (pl == null || !pl.isOnline() || pl.isDead() || pl.getLocation().getBlockX() != feetXYZ.x || pl.getLocation().getBlockZ() != feetXYZ.z) {
                        this.cancel();
                        final Block feetBlock = feetXYZ.getBlock();
                        feetBlock.getRelative(BlockFace.UP).setType(oldMat[0]);
                        feetBlock.setType(oldMat[1]);
                        feetBlock.getRelative(BlockFace.DOWN).setType(oldMat[2]);
                    }
                }
            }.runTaskTimer(Ostrov.instance, 30, 10);

            //Ostrov.log("!safe y_ori="+y+" feetLoc="+feetLoc);
            final Block feetBlock = feetXYZ.getBlock();
            feetBlock.getRelative(BlockFace.UP).setType(Material.OAK_TRAPDOOR);
            feetBlock.setType(Material.AIR);
            feetBlock.getRelative(BlockFace.DOWN).setType(Material.GLASS);
            p.setVelocity(p.getVelocity().zero());
            p.setFallDistance(0);
            p.teleport(feetXYZ.getCenterLoc(), PlayerTeleportEvent.TeleportCause.COMMAND);

        } else { //ничего не вышло - просто тп на ориг.точку
            p.teleport(feetLoc, PlayerTeleportEvent.TeleportCause.COMMAND);//feetLoc.setY(feetLoc.getBlockY() + 0.5);//feetLoc.setY(y_ori + 0.5);
        }
        return true;
    }

    private static void buildPlatform(final Player p, final WXYZ feetXYZ, final Material platformMat, final Material backMat) {
        feetXYZ.getBlock().getRelative(BlockFace.DOWN).setType(platformMat);
        new BukkitRunnable() {
            final String name = p.getName();

            @Override
            public void run() {
                final Player pl = Bukkit.getPlayerExact(name);
                if (pl == null || !pl.isOnline() || pl.isDead() || pl.getLocation().getBlockX() != feetXYZ.x || pl.getLocation().getBlockZ() != feetXYZ.z) {
                    this.cancel();
                    feetXYZ.getBlock().getRelative(BlockFace.DOWN).setType(backMat);
                }
            }
        }.runTaskTimer(Ostrov.instance, 30, 10);
        p.teleport(feetXYZ.getCenterLoc(), PlayerTeleportEvent.TeleportCause.COMMAND);
    }

   /* private static boolean tryTp(final Player p, final WXYZ safeLoc, final Nms.PlaceType pt) {
        switch (pt) {
            case DANGEROUS:
                return false;
            case SAFELY:
                break;
            case FLUID:
            case AIR:
                final Material downMat = Nms.getFastMat(safeLoc.w, safeLoc.x, safeLoc.y - 1, safeLoc.z);
                //if (downMat == Material.WATER) { //была найдена поверхность воды - ставим кувшинку
//Ostrov.log("WATER!!!!"+feetLoc.getBlock().getType()+"->LILY_PAD");
                    safeLoc.getBlock().getRelative(BlockFace.DOWN).setType(Material.YELLOW_STAINED_GLASS);
                    new BukkitRunnable() { //чтобы не давало новое ТПР пока не сошел с места
                        final String name = p.getName();
                        int halfSec = 0;
                        @Override
                        public void run() {
                            final Player pl = Bukkit.getPlayerExact(name);
                            halfSec++;
                            if (halfSec > 60 || pl == null || !pl.isOnline() || pl.isDead() || pl.getLocation().getBlockX() != safeLoc.x || pl.getLocation().getBlockZ() != safeLoc.z) {
                                this.cancel();
                                safeLoc.getBlock().getRelative(BlockFace.DOWN).setType(downMat);
                            }
                        }
                    }.runTaskTimer(Ostrov.instance, 30, 10);
                //}
                break;
        }
        p.teleport(safeLoc.getCenterLoc(), PlayerTeleportEvent.TeleportCause.COMMAND);
        return true;
    }*/

  public static @Nullable WXYZ findSafeLocation(final WXYZ feetLoc) {
        //if (p.getGameMode() == GameMode.CREATIVE || p.getGameMode() == GameMode.SPECTATOR) {
        //    return feetLoc;
        //}


        final int y_max;
        if (feetLoc.w.getEnvironment() == World.Environment.NETHER) { //фикс - тэпешит над верхним бедроком
            y_max = feetLoc.w.getHighestBlockYAt(feetLoc.x, feetLoc.z) - 3;
            if (feetLoc.y > y_max) {//в аду или при генерации как в аду (определяем потолок из бедрока)
                feetLoc.y = y_max;
            }
        } else {
            y_max = feetLoc.w.getMaxHeight() - 2;
        }
        final int y_ori = feetLoc.y;//feet_y;

        //проверка указанного места
        Nms.PlaceType safe = Nms.isSafeLocation(feetLoc);//isSafePlace(headMat, feetMat, downMat);

        //проверка на блок выше
        if (safe == Nms.PlaceType.SAFELY) {
            return feetLoc;
        }

        feetLoc.add(0, 1, 0);
        safe = Nms.isSafeLocation(feetLoc);//TeleportLoc.isSafePlace(upHead, headMat, feetMat);

        //проверка на блок ниже
        if (safe == Nms.PlaceType.SAFELY) {
            return feetLoc;
        }

        feetLoc.add(0, -2, 0);
        safe = Nms.isSafeLocation(feetLoc);//TeleportLoc.isSafePlace(feetMat, downMat, subDown);

        if (safe == Nms.PlaceType.SAFELY) {
            return feetLoc;
        }

        //сканируем с самого верха до самого низа
        feetLoc.y = y_max;
        for (; feetLoc.y > feetLoc.w.getMinHeight() + 1; feetLoc.y--) {
            if (Nms.isSafeLocation(feetLoc) == Nms.PlaceType.SAFELY) { //вода или подходит для стояния - сойдёт
                return feetLoc;
            }
        }

        return null;

    }


    //поиск места заспавнило под водой
    @Deprecated
    public static Location findNearestSafeLocation(final Location loc, final Location lookAt) {
        if (loc == null) return null;
        if (!loc.getChunk().isLoaded()) loc.getChunk().load();

        World world = loc.getWorld();
        int startX = loc.getBlockX();
        int startZ = loc.getBlockZ();
        int startY = loc.getBlockY();

        final int maxY = world.getMaxHeight();
        final int minY = world.getMinHeight();

        final Location find = new Location(loc.getWorld(), 0, startY, 0);


        for (int dx = 1; dx <= 30; dx++) { // +/- 30 блоков вправо/влево
            for (int dz = 1; dz <= 30; dz++) { // +/- 30 блоков вперёд / назад

                int x = startX + (dx & 1) * -dx + (dx >> 1);
                int z = startZ + (dz & 1) * -dz + (dz >> 1);
                find.set(x + 0.5, startY, z + 0.5);

                for (int y = startY; y < maxY; y++) {  // +/- 386 блоков вверх / вниз
                    find.setY(y);// = world.getBlockAt(x, startY+dy, z).getLocation();//new Location(world, x, y, z);
                    if (isSafeLocation(find)) {
                        //feetLoc = centerOnBlock(feetLoc);
                        find.setYaw(loc.getYaw());
                        find.setPitch(loc.getPitch());
                        return find;
                    }
                }

                for (int y = startY; y > minY; y--) {
                    find.setY(y);// = world.getBlockAt(x, startY-dy, z).getLocation();//new Location(world, x, y, z);
                    if (isSafeLocation(find)) {
                        //feetLoc = centerOnBlock(feetLoc);
                        find.setYaw(loc.getYaw());
                        find.setPitch(loc.getPitch());
                        return find;
                    }
                }
            }
        }
        return null;
    }

    public static Location centerOnBlock(final Location loc) {
        return loc == null ? null : loc.set(loc.getBlockX() + 0.5, loc.getBlockY() + 0.5, loc.getBlockZ() + 0.5);
    }

    //не менять, юзают плагины!
    public static boolean isSafeLocation(final Location feetLoc) {
        if (feetLoc == null) return false;
        //final World w = feetLoc.getWorld();
        //final Material headMat = Nms.getFastMat(w, feetLoc.getBlockX(), feetLoc.getBlockY() + 1, feetLoc.getBlockZ());
        //final Material feetMat = Nms.getFastMat(w, feetLoc.getBlockX(), feetLoc.getBlockY(), feetLoc.getBlockZ());
        //final Material downMat = Nms.getFastMat(w, feetLoc.getBlockX(), feetLoc.getBlockY() - 1, feetLoc.getBlockZ());
        //return isSafePlace(headMat, feetMat, downMat);
        return Nms.isSafeLocation(new WXYZ(feetLoc)) == Nms.PlaceType.SAFELY;//isSafeLocation(new WXYZ(feetLoc));
    }

    @Deprecated
    public static boolean isSafePlace(final Material headMat, final Material feetMat, final Material downMat) {
        if (headMat == null || feetMat == null || downMat == null) return false;
        return LocUtil.isPassable(headMat) && LocUtil.isPassable(feetMat)
                && (LocUtil.canStand(downMat) || downMat == Material.WATER);//вода под ногами подходит
    }

    //public static boolean isSafeLocation(final WXYZ feetXYZ) {
    //     return Nms.isSafeLocation(feetXYZ);
    // }
    public static void moveDeny(final PlayerMoveEvent e) {//блокировка перемещения, для миниигр
        if (e.getTo().getY() < e.getFrom().getY()) {
            e.setTo(e.getFrom().add(0, 2, 0));
        } else {
            e.setTo(e.getFrom());
        }
    }
}


//в цикле не нашлось ничего хорошего - пробуем поставить в воду или воздух
        /*if (bestFluid !=0 || bestAir !=0) {
//Ostrov.log("WATER!!!!"+feetLoc.getBlock().getType()+"->LILY_PAD");
            if (bestFluid !=0) {
                if (bestFluid > best) { //чтобы не тепешило в пещеры под водой, приоритет уровню жидкости
                    feetXYZ.y = bestFluid;
                } else {
                    feetXYZ.y = best;
                }
Ostrov.log("bestFluid!!!! "+bestFluid);

            } else {
Ostrov.log("bestAir!!!! "+bestAir);
                feetXYZ.y = bestAir;
            }
            final Material downMat = Nms.getFastMat(feetXYZ.w, feetXYZ.x, feetXYZ.y - 1, feetXYZ.z);
//Ostrov.log("WATER!!!!"+feetLoc.getBlock().getType()+"->LILY_PAD");
            feetXYZ.getBlock().getRelative(BlockFace.DOWN).setType(Material.YELLOW_STAINED_GLASS);
                new BukkitRunnable() { //чтобы не давало новое ТПР пока не сошел с места
                    final String name = p.getName();
                    @Override
                    public void run() {
                        final Player pl = Bukkit.getPlayerExact(name);
                        if (pl == null || !pl.isOnline() || pl.isDead() || pl.getLocation().getBlockX() != feetXYZ.x || pl.getLocation().getBlockZ() != feetXYZ.z) {
                            this.cancel();
                            feetXYZ.getBlock().getRelative(BlockFace.DOWN).setType(downMat);
                        }
                    }
                }.runTaskTimer(Ostrov.instance, 30, 10);
            p.teleport(feetXYZ.getCenterLoc(), PlayerTeleportEvent.TeleportCause.COMMAND);
        }*/

//        if (!new PlayerTeleportEvent(p, p.getLocation(), feetLoc, PlayerTeleportEvent.TeleportCause.PLUGIN).callEvent()) {
//          p.sendMessage(Ostrov.prefixWARN + "§cТелепорт был отменен!");
//          return false;
//        }

        /*if (p.getGameMode() == GameMode.CREATIVE || p.getGameMode() == GameMode.SPECTATOR) {
            p.teleport(feetLoc, PlayerTeleportEvent.TeleportCause.COMMAND);
            return true;
        }

        final World w = feetLoc.getWorld();

        if (w.getEnvironment() == World.Environment.NETHER) { //фикс - тэпешит над верхним бедроком
            int y_max = w.getHighestBlockYAt(feetLoc.getBlockX(), feetLoc.getBlockZ()) - 3;
            if (feetLoc.getBlockY() > y_max) {
                feetLoc.setY(y_max);
            }
        }
        final int x = feetLoc.getBlockX();
        int feet_y = feetLoc.getBlockY();
        final int y_ori = feet_y;
        final int z = feetLoc.getBlockZ();


        Material headMat = Nms.getFastMat(w, x, feet_y + 1, z);
        Material feetMat = Nms.getFastMat(w, x, feet_y, z);
        Material downMat = Nms.getFastMat(w, x, feet_y - 1, z);


        //проверка указанного места
        boolean safe = TeleportLoc.isSafePlace(headMat, feetMat, downMat);

        //проверка на блок выше
        if (!safe) {
            feet_y = y_ori + 1;//feetLoc.add(0, 1, 0);
            final Material upHead = Nms.getFastMat(w, x, y_ori + 2, z);
            safe = TeleportLoc.isSafePlace(upHead, headMat, feetMat);
            //LocationUtil.isPassable(upHead) && LocationUtil.isPassable(headMat)  && (LocationUtil.canStand(feetMat) || feetMat==Material.WATER);
            if (safe) downMat = feetMat; //если норм, прописать что под ногами в таком варианте
        }

        //проверка на блок ниже
        if (!safe) {
            feet_y = y_ori - 1;//feetLoc.subtract(0, 2, 0);
            final Material subDown = Nms.getFastMat(w, x, y_ori - 2, z);
            safe = TeleportLoc.isSafePlace(feetMat, downMat, subDown);
            //safe = LocationUtil.isPassable(feetMat)  && LocationUtil.isPassable(downMat) && (LocationUtil.canStand(subDown) || subDown==Material.WATER);
            if (safe) downMat = subDown; //если норм, прописать что под ногами в таком варианте
        }

        //сканируем с самого верха до самого низа
        if (!safe) {
            final boolean nether = w.getEnvironment() == World.Environment.NETHER;
            feet_y = w.getMaxHeight() - 2;
            for (; feet_y > w.getMinHeight() + 1; feet_y--) {
                //в аду или при генерации как в аду (определяем потолок из бедрока)

                headMat = feetMat; //VM.getNmsServer().getFastMat(w, x, y-1, z);
                feetMat = downMat;//VM.getNmsServer().getFastMat(w, x, y, z);
                downMat = Nms.getFastMat(w, x, feet_y - 1, z);
//Ostrov.log("find y="+y+" "+headMat+" "+feetMat+" "+downMat);
                if ((nether || feet_y > 0) && downMat == Material.BEDROCK) {
                    continue;
                }                //если над нижним блоком нет 2 блока для тела, пропускаем ниже
                //if (!LocationUtil.isPassable(headMat) || !LocationUtil.isPassable(feetMat)) {
                //    continue;
                //}
                //if (LocationUtil.canStand(downMat) || downMat==Material.WATER) { //вода или подходит для стояния - сойдёт
                feetLoc.setY(feet_y);
                if (TeleportLoc.isSafePlace(headMat, feetMat, downMat)) { //вода или подходит для стояния - сойдёт
                    safe = true;
                    break;
                }
            }
        }*/