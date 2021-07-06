package ru.komiss77;

import net.md_5.bungee.api.plugin.Plugin;


//не удалять, или банжик пытается грузить как баккит!!

public class OstrovBungee extends Plugin {
    
    private static OstrovBungee instance;
   // protected static String chanelName="ostrov:ostrov";    

    
    
    
    @Override
    public void onEnable() {
        instance = this;
        //this.getProxy().registerChannel(chanelName);
        //this.getProxy().getPluginManager().registerListener(this, new BungeeChanellMsg());
        //BungeeCord.getInstance().getLogger().log(Level.INFO, "§6Остров §bРежим BungeeCord");
    }

    //почему отличия в отправке??
    

    
    
    
   // public static final OstrovBungee getInstance() {
   //         return instance;
   // }  
    
    //public static void log_ok(final String s) {   instance.getLogger().log(Level.INFO, "{0}{1}", new String[]{"§6[§eOstrovBungee§6] §2", s}); }
    //public static void log_err(final String s) {   instance.getLogger().log(Level.INFO, "{0}{1}", new String[]{"§6[§eOstrovBungee§6] §4", s}); }



}
