package ru.komiss77.modules.world;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;
import ru.komiss77.Ostrov;
import ru.komiss77.listener.ArcaimLst;
import ru.komiss77.objects.IntHashMap;
import ru.komiss77.utils.LocationUtil;


public class Land {
    private static final IntHashMap<String> worldNames;
    private static final IntHashMap<ChunkContent> contents;
    private static final ChunkContent EMPTY;
    private static boolean hasOverlap;

    static {
        worldNames = new IntHashMap();
        contents = new IntHashMap();
        EMPTY = new ChunkContent();
    }


    public static void load(final World w) {
        //чтобы не делать отдельную мапу для номеров, или вложенные мапы с UUID, в принципе хватает такой реализации
        if (worldNames.containsKey(w.getName().length())) {
            if (!hasOverlap) {
                hasOverlap = true;
                Ostrov.log_warn("Land : перекрытие миров с одинаковой длинной названия - учитывать при использованиии ChunkContent.");
            }
            return;
        }
        worldNames.put(w.getName().length(), w.getName());
    }

    public static void unload(final World w) {
        contents.remove(w.getName().length());
        worldNames.remove(w.getName().length());
    }

    public static void unload(final Chunk chunk) {
        contents.remove(LocationUtil.cLoc(chunk.getWorld().getName(), chunk.getX(), chunk.getZ()));
    }


    // *************** RedstoneClockController ***************
  /*public static ArcaimLst.RC getRedstone(final Block b) {
    ChunkContent content = content(b.getLocation());
    if (content.redstone!=null) {
      return content.redstone.get(content.sLoc(b.getX(), b.getY(), b.getZ()));
    }
    return null;
  }
  public static void addRedstone(final Block b, final ArcaimLst.RC clock) {
    final int cLoc = LocationUtil.cLoc(b.getWorld().getName(), b.getX()>>4, b.getZ()>>4);
    ChunkContent content = contents.get(cLoc);
    if (content == null) {
      content = new ChunkContent();
      contents.put(cLoc, content);
    }
    if (content.redstone==null) {
      content.redstone = new IntHashMap<>();
    }
    content.redstone.put(content.sLoc(b.getX(), b.getY(), b.getZ()), clock);
  }

  public static void delRedstone(final Block b) {
    final int cLoc = LocationUtil.cLoc(b.getWorld().getName(), b.getX()>>4, b.getZ()>>4);
    ChunkContent content = contents.get(cLoc);
    if (content == null) {
      return;
    }
    if (content.redstone!=null) {
      content.redstone.remove(content.sLoc(b.getX(), b.getY(), b.getZ()));
      if (content.redstone.isEmpty()) {
        content.redstone = null;
      }
    }
    if (content.empty()) {
      contents.remove(cLoc);
    }
  }*/
// *************** RedstoneClockController END ***************


    public static @NotNull ChunkContent content(final String worldName, final int cX, final int cZ) {
        return contents.getOrDefault(LocationUtil.cLoc(worldName, cX, cZ), EMPTY);
    }

    public static @NotNull ChunkContent content(final Location loc) {
        return contents.getOrDefault(LocationUtil.cLoc(loc.getWorld().getName(), loc.getBlockX() >> 4, loc.getBlockZ() >> 4), EMPTY);
    }

    public static @NotNull ChunkContent content(final Chunk chunk) {
        return contents.getOrDefault(LocationUtil.cLoc(chunk.getWorld().getName(), chunk.getX(), chunk.getZ()), EMPTY);
    }


}
