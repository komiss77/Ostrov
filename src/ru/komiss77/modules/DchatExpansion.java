package ru.komiss77.modules;

import org.bukkit.entity.Player;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Managers.SM;
import ru.komiss77.Ostrov;


public class DchatExpansion extends PlaceholderExpansion {
    private Ostrov plugin;

    /**
     * Since we register the expansion inside our own plugin, we
     * can simply use this method here to get an instance of our
     * plugin.
     *
     * @param plugin
     *        The instance of our plugin.
     */
    public DchatExpansion(Ostrov plugin){
//System.out.println("DchatExpansion 1");
        this.plugin = plugin;
    }

    /**
     * Because this is an internal class,
     * you must override this method to let PlaceholderAPI know to not unregister your expansion class when
     * PlaceholderAPI is reloaded
     *
     * @return true to persist through reloads
     */
    @Override
    public boolean persist(){
//System.out.println("DchatExpansion persist");
        return true;
    }

    /**
     * Because this is a internal class, this check is not needed
     * and we can simply return {@code true}
     *
     * @return Always true since it's an internal class.
     */
    @Override
    public boolean canRegister(){
//System.out.println("DchatExpansion canRegister");
        return true;
    }

    /**
     * The name of the person who created this expansion should go here.
     * <br>For convienience do we return the author from the plugin.yml
     * 
     * @return The name of the author as a String.
     */
    @Override
    public String getAuthor(){
//System.out.println("DchatExpansion getAuthor");
        return "komiss77";
    }

    /**
     * The placeholder identifier should go here.
     * <br>This is what tells PlaceholderAPI to call our onRequest 
     * method to obtain a value if a placeholder starts with our 
     * identifier.
     * <br>This must be unique and can not contain % or _
     *
     * @return The identifier in {@code %<identifier>_<value>%} as String.
     */
    @Override
    public String getIdentifier(){
//System.out.println("DchatExpansion getIdentifier");
        return "ostrov";
    }

    /**
     * This is the version of the expansion.
     * <br>You don't have to use numbers, since it is set as a String.
     *
     * For convienience do we return the version from the plugin.yml
     *
     * @return The version as a String.
     */
    @Override
    public String getVersion(){
//System.out.println("DchatExpansion getVersion");
        return "1.1";
    }

    /**
     * This is the method called when a placeholder with our identifier 
     * is found and needs a value.
     * <br>We specify the value identifier in this method.
     * <br>Since version 2.9.1 can you use OfflinePlayers in your requests.
     *
     * @param  p
     *         A {@link org.bukkit.Player Player}.
     * @param  placeholder
     *         A String containing the identifier/value.
     *
     * @return possibly-null String of the requested identifier.
     */
    @Override
    public String onPlaceholderRequest(Player p, String placeholder){
//System.out.println("---> DchatExpansion onPlaceholderRequest "+placeholder);

        if(p == null) {
            return "";
        }
        
        switch (placeholder) {
            
            case "genderFemaleLastA":      
//System.out.println("onPlaceholderRequest "+placeholder+" result = "+ApiOstrov.isFemale(p.getName()));
                return ApiOstrov.isFemale(p.getName()) ? "а" : "" ;
                
            case "servername":      
                return SM.this_server_name;
                
            case "serverprefix":      
                return SM.chatLogo;
                
            case "serverlogo":      
                return SM.chatLogo;
                
            case "prefix":      
                return ApiOstrov.getPrefix(p);
                
            case "money":       
                return ApiOstrov.getBalanceStatus(p);
                
            case "groups":     
                return ApiOstrov.getChatGroups(p.getName());
                
            case "playtime":    
                return ApiOstrov.getPlayTime(p);
                
            case "suffix":            
                return ApiOstrov.getSuffix(p);
                
            default:
                return "+";
        }

    }


    
    
    
    
    
/*
    седна
    
    switch (placeholder) {
            
            case "world":  
                E_World e_world = E_World.by_world_name(p.getWorld().getName());
                return e_world.название;
                //break;
                
            case "скил":
                return sp.навык.name();
                
            case "уровень":
                return String.valueOf(sp.уровень);
                
            case "сила":
                return String.valueOf(sp.сила);
                
            case "интеллект":
                return String.valueOf(sp.интеллект);
                
            case "ловкость":
                return String.valueOf(sp.ловкость);
                
            case "защита":
                return String.valueOf(sp.защита);
                
            case "души":
                return String.valueOf(sp.души);
                
            case "здоровье":
                return String.valueOf(sp.здоровье);
                
            case "здоровьемакс":
                return String.valueOf(sp.здоровье_макс);
    
    
    
    
    
    
острова
    
     @Override
   public String onPlaceholderRequest(Player p, String identifier) {
       
       if (p==null) return "";
       PlayerInfo playerInfo = PlayerLogic.getPlayerInfo(p.getName());
        IslandInfo islandInfo = plugin.getIslandLogic().getIslandInfo(playerInfo);
        if (playerInfo == null || islandInfo == null) {
            return "--";
        }
//System.out.println(" 11111 identifier "+identifier+" "+islandInfo+" "+playerInfo);        
       switch (identifier) {
            case "island_level": return pre("{0,number,##.#}", islandInfo.getLevel());
            case "island_level_int": return pre("{0,number,#}", islandInfo.getLevel());
            case "island_rank": return getRank(islandInfo);
            case "island_leader": return islandInfo.getLeader();
            case "island_golems_max": return "" + islandInfo.getMaxGolems();
            case "island_monsters_max": return "" + islandInfo.getMaxMonsters();
            case "island_animals_max": return "" + islandInfo.getMaxAnimals();
            case "island_villagers_max": return "" + islandInfo.getMaxVillagers();
            case "island_partysize_max": return "" + islandInfo.getMaxPartySize();
            case "island_golems": return "" + plugin.getLimitLogic().getCreatureCount(islandInfo).get(LimitLogic.CreatureType.GOLEM);
            case "island_monsters": return "" + plugin.getLimitLogic().getCreatureCount(islandInfo).get(LimitLogic.CreatureType.MONSTER);
            case "island_animals": return "" + plugin.getLimitLogic().getCreatureCount(islandInfo).get(LimitLogic.CreatureType.ANIMAL);
            case "island_villagers": return "" + plugin.getLimitLogic().getCreatureCount(islandInfo).get(LimitLogic.CreatureType.VILLAGER);
            case "island_partysize": return "" + islandInfo.getPartySize();
            case "island_biome": return islandInfo.getBiome();
            case "island_bans": return ""+islandInfo.getBans();
            case "island_members": return ""+islandInfo.getMembers();
            case "island_trustees": return ""+islandInfo.getTrustees();
            case "island_location": return LocationUtil.asString(islandInfo.getIslandLocation());
            case "island_location_x": return pre("{0,number,#}", islandInfo.getIslandLocation().getBlockX());
            case "island_location_y": return pre("{0,number,#}", islandInfo.getIslandLocation().getBlockY());
            case "island_location_z": return pre("{0,number,#}", islandInfo.getIslandLocation().getBlockZ());
            case "island_schematic": return islandInfo.getSchematicName();
            default: return "";
       }
       
      //return p == null?"":(identifier.equals("island_level")?String.valueOf(this.plugin.getIslandLevel(p)):(identifier.equals("island_rank")?String.valueOf(this.plugin.getIslandRank(p)):null));
   }
   
     private String getRank(IslandInfo islandInfo) {
        IslandRank rank = plugin.getIslandLogic().getRank(islandInfo.getName());
        if (rank != null) {
            return pre("{0,number,#}", rank.getRank());
        } else {
            return "--";
        }
    }
    
    
    
                */    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
}

