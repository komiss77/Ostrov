package ru.komiss77.hook;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.World;

import org.dynmap.DynmapAPI;
import org.dynmap.markers.AreaMarker;
import org.dynmap.markers.CircleMarker;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerSet;
import ru.komiss77.modules.world.WorldManager;
import ru.komiss77.Ostrov;
import ru.komiss77.modules.wordBorder.CoordXZ;

public class DynmapFeatures {

    private static DynmapAPI api;
    private static MarkerAPI markApi;
    private static MarkerSet markSet;
//	private static int lineWeight = 3;
//	private static double lineOpacity = 1.0;
//	private static int lineColor = 0xFF0000;

    // Whether re-rendering functionality is available
    public static boolean renderEnabled() {
        return api != null;
    }

    // Whether circular border markers are available
    public static boolean borderEnabled() {
        return markApi != null;
    }

    public static void setup() {
        Plugin test = Bukkit.getServer().getPluginManager().getPlugin("dynmap");
        if (test == null || !test.isEnabled()) {
            return;
        }

        api = (DynmapAPI) test;

        // make sure DynMap version is new enough to include circular markers
        try {
            Class.forName("org.dynmap.markers.CircleMarker");

            // for version 0.35 of DynMap, CircleMarkers had just been introduced and were bugged (center position always 0,0)
            if (api.getDynmapVersion().startsWith("0.35-")) {
                throw new ClassNotFoundException();
            }
        } catch (ClassNotFoundException ex) {
            Ostrov.log_ok("DynMap is available, but border display is currently disabled: you need DynMap v0.36 or newer.");
            return;
        } catch (NullPointerException ex) {
            Ostrov.log_ok("DynMap is present, but an NPE (type 1) was encountered while trying to integrate. Border display disabled.");
            return;
        }

        try {
            markApi = api.getMarkerAPI();
            if (markApi == null) {
                return;
            }
        } catch (NullPointerException ex) {
            Ostrov.log_ok("DynMap is present, but an NPE (type 2) was encountered while trying to integrate. Border display disabled.");
            return;
        }

        // go ahead and show borders for all worlds
        showAllBorders();

        Ostrov.log_ok("Successfully hooked into DynMap for the ability to display borders.");
    }


    /*
	 * Re-rendering methods, used for updating trimmed chunks to show them as gone
	 * Sadly, not currently working. Might not even be possible to make it work.
     */
    public static void renderRegion(String worldName, CoordXZ coord) {
        if (!renderEnabled()) {
            return;
        }

        World world = Bukkit.getWorld(worldName);
        int y = (world != null) ? world.getMaxHeight() : 255;
        int x = CoordXZ.regionToBlock(coord.x);
        int z = CoordXZ.regionToBlock(coord.z);
        api.triggerRenderOfVolume(worldName, x, 0, z, x + 511, y, z + 511);
    }

    public static void renderChunks(String worldName, List<CoordXZ> coords) {
        if (!renderEnabled()) {
            return;
        }

        World world = Bukkit.getWorld(worldName);
        int y = (world != null) ? world.getMaxHeight() : 255;

        for (CoordXZ coord : coords) {
            renderChunk(worldName, coord, y);
        }
    }

    public static void renderChunk(String worldName, CoordXZ coord, int maxY) {
        if (!renderEnabled()) {
            return;
        }

        int x = CoordXZ.chunkToBlock(coord.x);
        int z = CoordXZ.chunkToBlock(coord.z);
        api.triggerRenderOfVolume(worldName, x, 0, z, x + 15, maxY, z + 15);
    }


    /*
	 * Methods for displaying our borders on DynMap's world maps
     */
    private static Map<String, CircleMarker> roundBorders = new HashMap<String, CircleMarker>();
    private static Map<String, AreaMarker> squareBorders = new HashMap<String, AreaMarker>();

    public static void showAllBorders() {
        if (!borderEnabled()) {
            return;
        }

        // in case any borders are already shown
        removeAllBorders();

        if (!WorldManager.dynmapEnable) {
            // don't want to show the marker set in DynMap if our integration is disabled
            if (markSet != null) {
                markSet.deleteMarkerSet();
            }
            markSet = null;
            return;
        }

        // make sure the marker set is initialized
        markSet = markApi.getMarkerSet("worldborder.markerset");
        if (markSet == null) {
            markSet = markApi.createMarkerSet("worldborder.markerset", "WorldBorder", null, false);
        } else {
            markSet.setMarkerSetLabel("WorldBorder");
        }

        for (World w : Bukkit.getWorlds()) {
            showSquareBorder(w);
        }

        //Map<String, BorderData> borders = Config.getBorders();
        //for(Entry<String, BorderData> stringBorderDataEntry : borders.entrySet())
        //{
        //Entry wdata = stringBorderDataEntry;
        //String worldName = ((String)wdata.getKey());
        //BorderData border = (BorderData)wdata.getValue();
        //showBorder(worldName, border);
        //}
    }

    /*public static void showBorder(World w)
	{
		if (!borderEnabled()) return;

		if (!Config.DynmapBorderEnabled()) return;

		if ((border.getShape() == null) ? Config.ShapeRound() : border.getShape())
			showRoundBorder(worldName, border);
		else
			showSquareBorder(worldName, border);
	}

	private static void showRoundBorder(String worldName, BorderData border)
	{
		if (squareBorders.containsKey(worldName))
			removeBorder(worldName);

		CircleMarker marker = roundBorders.get(worldName);
		if (marker == null)
		{
			marker = markSet.createCircleMarker("worldborder_"+worldName, Config.DynmapMessage(), false, worldName, border.getX(), 64.0, border.getZ(), border.getRadiusX(), border.getRadiusZ(), true);
			marker.setLineStyle(lineWeight, lineOpacity, lineColor);
			marker.setFillStyle(0.0, 0x000000);
			roundBorders.put(worldName, marker);
		}
		else
		{
			marker.setCenter(worldName, border.getX(), 64.0, border.getZ());
			marker.setRadius(border.getRadiusX(), border.getRadiusZ());
		}
	}
     */
    private static void showSquareBorder(World w) {
        //if (roundBorders.containsKey(worldName))
        //removeBorder(worldName);
        final int worldRadius = (int) w.getWorldBorder().getSize() < Bukkit.getServer().getMaxWorldSize() ? ((int) w.getWorldBorder().getSize()) : Bukkit.getServer().getMaxWorldSize();//VM.getNmsServer().getMaxWorldSize(p.getWorld());//propertyManager.getInt("max-world-size", 500);

        // corners of the square border
        //double[] xVals = {border.getX() - border.getRadiusX(), border.getX() + border.getRadiusX()};
        //double[] zVals = {border.getZ() - border.getRadiusZ(), border.getZ() + border.getRadiusZ()};
        double[] xVals = {w.getWorldBorder().getCenter().getBlockX() - worldRadius, w.getWorldBorder().getCenter().getBlockX() + worldRadius};
        double[] zVals = {w.getWorldBorder().getCenter().getBlockZ() - worldRadius, w.getWorldBorder().getCenter().getBlockZ() + worldRadius};

        AreaMarker marker = squareBorders.get(w.getName());
        if (marker == null) {
            marker = markSet.createAreaMarker("worldborder_" + w.getName(), WorldManager.dynmapMessage, false, w.getName(), xVals, zVals, true);
            marker.setLineStyle(3, 1.0, 0xFF0000);
            marker.setFillStyle(0.0, 0x000000);
            squareBorders.put(w.getName(), marker);
        } else {
            marker.setCornerLocations(xVals, zVals);
        }
    }

    public static void removeAllBorders() {
        if (!borderEnabled()) {
            return;
        }

        for (CircleMarker marker : roundBorders.values()) {
            marker.deleteMarker();
        }
        roundBorders.clear();

        for (AreaMarker marker : squareBorders.values()) {
            marker.deleteMarker();
        }
        squareBorders.clear();
    }

    public static void removeBorder(String worldName) {
        if (!borderEnabled()) {
            return;
        }

        CircleMarker marker = roundBorders.remove(worldName);
        if (marker != null) {
            marker.deleteMarker();
        }

        AreaMarker marker2 = squareBorders.remove(worldName);
        if (marker2 != null) {
            marker2.deleteMarker();
        }
    }
}
