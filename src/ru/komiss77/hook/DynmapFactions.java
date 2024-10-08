package ru.komiss77.hook;

import java.util.HashSet;
import java.util.Set;
import org.bukkit.Bukkit;
import org.dynmap.DynmapAPI;
import org.dynmap.markers.*;





    /*
    strokecolor = cfg.getString(path+".strokeColor", "#FF0000");
    unownedstrokecolor = cfg.getString(path+".unownedStrokeColor", "#00FF00");
    strokeopacity = cfg.getDouble(path+".strokeOpacity", 0.8);
    strokeweight = cfg.getInt(path+".strokeWeight", 3);
    fillcolor = cfg.getString(path+".fillColor", "#FF0000");
    fillopacity = cfg.getDouble(path+".fillOpacity", 0.35);
    */

public class DynmapFactions {

    static final DynmapAPI api = (DynmapAPI) Bukkit.getPluginManager().getPlugin("dynmap");
    ;
    static final MarkerAPI markerapi = api.getMarkerAPI();
    static final MarkerSet set = markerapi.createMarkerSet("factions.markerset", "Кланы", null, false);
    static final MarkerIcon baseIcon = markerapi.getMarkerIcon("tower");

    enum Direction {XPLUS, ZPLUS, XMINUS, ZMINUS}

    ;


    protected static void updateFactions() {

        // Ostrov.async( ()-> {
           /* for(Faction f : FM.getFactions()) {
//Main.log_ok("updateFactions: "+f.name());
                final String descr = getDescription(f);

                drawFactionArea(f, descr);
                drawFactionBaseIcon(f, descr);

            }*/
        //  }, 5*20);

    }

    //https://colorscheme.ru/html-colors.html
    
    /*protected static String getDescription (final Faction f) {
        return  "<div class=\"regioninfo\"><div class=\"infowindow\"><span style=\"font-size:120%;\">"+ChatColor.strip(f.name())+
                        "</span><br /> <span style=\"font-weight:bold;color:#00FFFF\">"+ChatColor.strip(f.tagLine)+
                        "</span><br /> Лидер: <span style=\"font-weight:bold;color:#FFFFFF\">"+f.getOwner()+
                        "</span><br /> Участников: <span style=\"font-weight:bold;color:#FFFF00\">"+f.getMembers().size()+
                        "</span><br /> Уровень: <span style=\"font-weight:bold;color:#B0E0E6\">"+ChatColor.strip(Level.getLevelIcon(f.getLevel()))+
                        "</span><br /> Религия: <span style=\"font-weight:bold;color:#B0E0E6\">"+f.getReligy().name()+
                        (f.isOnline() ? "</span><br /> Статус: <span style=\"color:#00FF00;\">online" : "</span><br /> Статус: <span style=\"color:#008080;\">offline")+
                        "</span></div></div>"
                ;
    }*/


    protected static void wipe(final int factionID) {

        Marker home = set.findMarker(String.valueOf(factionID));
        if (home != null) {
            home.deleteMarker();
        }

        final String areaPrefix = factionID + "__";

        final Set<AreaMarker> find = new HashSet<>();//set.getAreaMarkers();
        for (AreaMarker am : set.getAreaMarkers()) {
            if (am.getMarkerID().startsWith(areaPrefix)) {
                find.add(am);
            }
        }
        for (AreaMarker am : find) {
            am.deleteMarker();
        }
    }
    
   /* protected static void drawFactionBaseIcon(final Faction f, final String descr) {
        Marker home = set.findMarker(String.valueOf(f.factionId));
        //if (m!=null) {
        //    m.deleteMarker();
        //}
        final String lbl = "база "+ChatColor.strip(f.name());

        //Marker home = markers.remove(f.factionId);

        if(home == null) {
            home = set.createMarker(String.valueOf(f.factionId), lbl, f.home.getWorld().name(),
                    f.home.getX(), f.home.getY(), f.home.getZ(), baseIcon, false);

        } else {
            home.setLocation(f.home.getWorld().name(), f.home.getX(), f.home.getY(), f.home.getZ());
            home.setLabel(lbl);   // Update label 
            home.setMarkerIcon(baseIcon);
        }
        
        if (home != null) {
            home.setDescription(descr); // Set popup /
            //markers.put(f.factionId, home);
        }
     }*/


    /* Handle specific faction on specific world /*/
    /*
    protected static void drawFactionArea(final Faction f, final String descr) {
        
        final String areaPrefix = f.factionId+"__";
        
        final Set <AreaMarker> find = new HashSet<>();//set.getAreaMarkers();
        for (AreaMarker am : set.getAreaMarkers()) {
            if (am.getMarkerID().startsWith(areaPrefix)) {
                find.add(am);
            }
        }
        for (AreaMarker am : find) {
            am.deleteMarker();
        }

        if (f.getClaims().isEmpty())  return;
        
        LinkedList<Claim> nodevals = new LinkedList<>();
        final TileFlags curblks = new TileFlags();
        for (final Claim c : f.getClaims()) {
            curblks.setFlag(Land.getChunkX(c.cLoc), Land.getChunkZ(c.cLoc), true);
            nodevals.addLast(c);
        }
        
        int poly_index = 0;
        while (nodevals != null) {
            LinkedList<Claim> ournodes = null;
            LinkedList<Claim> newlist = null;
            TileFlags ourblks = null;
            int minx = Integer.MAX_VALUE;
            int minz = Integer.MAX_VALUE;
            
            for (final Claim node : nodevals) {
                final int nodex = Land.getChunkX(node.cLoc);
                final int nodez = Land.getChunkZ(node.cLoc);
                if (ourblks == null && curblks.getFlag(nodex, nodez)) {
                    ourblks = new TileFlags();
                    ournodes = new LinkedList<>();
                    floodFillTarget(curblks, ourblks, nodex, nodez);
                    ournodes.add(node);
                    minx = nodex;
                    minz = nodez;
                } else if (ourblks != null && ourblks.getFlag(nodex, nodez)) {
                    ournodes.add(node);
                    if (nodex < minx) {
                        minx = nodex;
                        minz = nodez;
                    } else {
                        if (nodex != minx || nodez >= minz) {
                            continue;
                        }
                        minz = nodez;
                    }
                } else {
                    if (newlist == null) {
                        newlist = new LinkedList<>();
                    }
                    newlist.add(node);
                }
            }
            
            nodevals = newlist;
            
            if (ourblks != null) {
                final int init_x = minx;
                final int init_z = minz;
                int cur_x = minx;
                int cur_z = minz;
                Direction dir = Direction.XPLUS;
                final ArrayList<int[]> linelist = new ArrayList<>();
                linelist.add(new int[] { init_x, init_z });
                while (cur_x != init_x || cur_z != init_z || dir != Direction.ZMINUS) {
                    switch (dir) {
                        case XPLUS: {
                            if (!ourblks.getFlag(cur_x + 1, cur_z)) {
                                linelist.add(new int[] { cur_x + 1, cur_z });
                                dir = Direction.ZPLUS;
                                continue;
                            }
                            if (!ourblks.getFlag(cur_x + 1, cur_z - 1)) {
                                ++cur_x;
                                continue;
                            }
                            linelist.add(new int[] { cur_x + 1, cur_z });
                            dir = Direction.ZMINUS;
                            ++cur_x;
                            --cur_z;
                            continue;
                        }
                        case ZPLUS: {
                            if (!ourblks.getFlag(cur_x, cur_z + 1)) {
                                linelist.add(new int[] { cur_x + 1, cur_z + 1 });
                                dir = Direction.XMINUS;
                                continue;
                            }
                            if (!ourblks.getFlag(cur_x + 1, cur_z + 1)) {
                                ++cur_z;
                                continue;
                            }
                            linelist.add(new int[] { cur_x + 1, cur_z + 1 });
                            dir = Direction.XPLUS;
                            ++cur_x;
                            ++cur_z;
                            continue;
                        }
                        case XMINUS: {
                            if (!ourblks.getFlag(cur_x - 1, cur_z)) {
                                linelist.add(new int[] { cur_x, cur_z + 1 });
                                dir = Direction.ZMINUS;
                                continue;
                            }
                            if (!ourblks.getFlag(cur_x - 1, cur_z + 1)) {
                                --cur_x;
                                continue;
                            }
                            linelist.add(new int[] { cur_x, cur_z + 1 });
                            dir = Direction.ZPLUS;
                            --cur_x;
                            ++cur_z;
                            continue;
                        }
                        case ZMINUS: {
                            if (!ourblks.getFlag(cur_x, cur_z - 1)) {
                                linelist.add(new int[] { cur_x, cur_z });
                                dir = Direction.XPLUS;
                                continue;
                            }
                            if (!ourblks.getFlag(cur_x - 1, cur_z - 1)) {
                                --cur_z;
                                continue;
                            }
                            linelist.add(new int[] { cur_x, cur_z });
                            dir = Direction.XMINUS;
                            --cur_x;
                            --cur_z;
                            continue;
                        }
                    }
                }
                final int sz = linelist.size();
                final double[] polyX = new double[sz];
                final double[] polyZ = new double[sz];
                for (int i = 0; i < sz; ++i) {
                    final int[] line = linelist.get(i);
                    polyX[i] = line[0] * 16.0;
                    polyZ[i] = line[1] * 16.0;
                }
                
                
                final String polyId = areaPrefix+poly_index;
                    
                //AreaMarker areaMarker = areas.remove(polyId); // Existing area?
                //if(areaMarker == null) {
                    final AreaMarker areaMarker = set.createAreaMarker(polyId, f.name(), false, f.home.getWorld().name(), polyX, polyZ, false);
//info("areaMarker="+areaMarker);
                    if(areaMarker == null) {
                        Main.log_err("error adding area marker " + f.name());
                        return;
                    }
                //} else {
                //    areaMarker.setCornerLocations(polyX, polyZ); // Replace corner locations /
                //    areaMarker.setLabel(f.name());   // Update label
                //}

                areaMarker.setDescription(descr); // Set popup 

                areaMarker.setLineStyle(3, 0.8, 0x00FF00);//(as.strokeweight, as.strokeopacity, strokecolor 0x00FF00);
                areaMarker.setFillStyle(0.35, f.getChatColor().asBungee().hashCode());//(as.fillopacity, fillcolor 0xFF0000);

                poly_index++;
                
                //areas.put(polyId, areaMarker);
                    
            }
            
        }
        
        

  
    }
*/
   
    /*
    private static int floodFillTarget(TileFlags src, TileFlags dest, int x, int y) {
        int cnt = 0;
        ArrayDeque<int[]> stack = new ArrayDeque<>();
        stack.push(new int[] { x, y });
        
        while(stack.isEmpty() == false) {
            int[] nxt = stack.pop();
            x = nxt[0];
            y = nxt[1];
            if(src.getFlag(x, y)) { 
                src.setFlag(x, y, false);   
                dest.setFlag(x, y, true);  
                cnt++;
                if(src.getFlag(x+1, y))
                    stack.push(new int[] { x+1, y });
                if(src.getFlag(x-1, y))
                    stack.push(new int[] { x-1, y });
                if(src.getFlag(x, y+1))
                    stack.push(new int[] { x, y+1 });
                if(src.getFlag(x, y-1))
                    stack.push(new int[] { x, y-1 });
            }
        }
        return cnt;
    }
    
    */


}



















