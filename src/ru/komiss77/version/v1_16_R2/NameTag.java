package ru.komiss77.version.v1_16_R2;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.minecraft.server.v1_16_R2.ChatComponentText;

import net.minecraft.server.v1_16_R2.EnumChatFormat;
import net.minecraft.server.v1_16_R2.Packet;
import net.minecraft.server.v1_16_R2.PacketPlayOutScoreboardTeam;
import org.bukkit.craftbukkit.v1_16_R2.entity.CraftPlayer;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import ru.komiss77.version.INameTag;




public class NameTag implements INameTag {

   
    @Override
    public void sendNameTag(final Player player, final String name, final int param, final List<String> members) {
        final Packet packet = createPacket(name, param, members);
        send(player, packet);
    }
    
    
    @Override
    public void sendNameTag(final String name, final int param, final List<String> members) {
        final Packet packet = createPacket(name, param, members);
        send(packet);
    }
    

    
    
    
    @Override
    public void sendNameTag(final Player player, final String name, final String prefix, String suffix, final int param, final Collection<?> players) {
        final Packet packet = createPacket(name, prefix, suffix, param, players);
        send(player, packet);
    }

    @Override
    public void sendNameTag(final String name, final String prefix, String suffix, final int param, final Collection<?> players) {
        final Packet packet = createPacket(name, prefix, suffix, param, players);
        send(packet);
    }

    
    
    
    private Packet createPacket (final String name, final int param, final List<String> members) {
        Packet packet = new PacketPlayOutScoreboardTeam();
        if (param != 3 && param != 4) {
            throw new IllegalArgumentException("Method must be join or leave for player constructor");
        }
        setupDefaults(packet, name, param);
        addMembers(packet, members);
        return packet;
    }

    
    
    
    
    private Packet createPacket (final String name, final String prefix, String suffix, final int param, final Collection<?> players) {
        Packet packet = new PacketPlayOutScoreboardTeam();
        setupDefaults(packet, name, param);
        if (param != 0) {
            if (param != 2) {
                return null;
            }
        }
        try {
            final String color = ChatColor.getLastColors(prefix);
            String colorCode = null;
            if (!color.isEmpty()) {
                colorCode = color.substring(color.length() - 1);
                String chatColor = ChatColor.getByChar(colorCode).name();
                if (chatColor.equalsIgnoreCase("MAGIC")) {
                    chatColor = "OBFUSCATED";
                }
                //final Enum<?> colorEnum = Enum.valueOf(PacketWrapper.typeEnumChatFormat, chatColor);
                setField(packet, "g", EnumChatFormat.valueOf(chatColor));//PacketAccessor.TEAM_COLOR.set(this.packet, colorEnum);
            }
            setField(packet, "b", new ChatComponentText(name));//PacketAccessor.DISPLAY_NAME.set(this.packet, PacketWrapper.ChatComponentText.newInstance(name));
            setField(packet, "c", new ChatComponentText(prefix));//PacketAccessor.PREFIX.set(this.packet, PacketWrapper.ChatComponentText.newInstance(prefix));
            if (colorCode != null) {
                suffix = ChatColor.getByChar(colorCode) + suffix;
            }
            setField(packet, "d", new ChatComponentText(suffix));//PacketAccessor.SUFFIX.set(this.packet, PacketWrapper.ChatComponentText.newInstance(suffix));
            setField(packet, "j", 1);//PacketAccessor.PACK_OPTION.set(this.packet, 1);
            //if (PacketAccessor.VISIBILITY != null) { //version.split("_")[1]) >= 8; v1_16_R2 = 15
                setField(packet, "e", "always");//PacketAccessor.VISIBILITY.set(this.packet, "always");
            //}
            if (param == 0) {
                addMembers(packet, players); //(Collection)PacketAccessor.MEMBERS.get(this.packet)).addAll(players);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return packet;
    }
    
    
    
    private void addMembers(Packet packet, Collection<?> players) {
        players = ((players == null || players.isEmpty()) ? new ArrayList<>() : players);
        try {
            Field field = packet.getClass().getDeclaredField("h");
            field.setAccessible(true);
            //Collection h = (Collection)field.get(packet);
//System.out.println("addMembers 1 h="+h);
            //h.addAll(players);
//System.out.println("addMembers 2 h="+h);
            ((Collection)field.get(packet)).addAll(players);
            //field.set(packet, h);
            field.setAccessible(false);
        } catch (NoSuchFieldException | SecurityException | IllegalAccessException ex) {
            ex.printStackTrace();
        }
        //((Collection)PacketAccessor.MEMBERS.get(this.packet)).addAll(players);
    
    }
    
    private void setupDefaults(Packet packet, final String name, final int param) {
        try {
            setField(packet, "a", name);// PacketAccessor.TEAM_NAME.set(this.packet, name);
            setField(packet, "i", param);// PacketAccessor.PARAM_INT.set(this.packet, param);
            //if (NametagHandler.DISABLE_PUSH_ALL_TAGS && PacketAccessor.PUSH != null) {
            //    PacketAccessor.PUSH.set(this.packet, "never");
            //}
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    //     mem  pref suff  team int opt  dispN col  push  vis
    //v1_14("h", "c", "d", "a", "i", "j", "b", "g", "f", "e"),
    //v1_15("h", "c", "d", "a", "i", "j", "b", "g", "f", "e"),
    //;
    private void setField(Packet packet, final String path, final Object newValue) {
        try {
            Field field = packet.getClass().getDeclaredField(path);
            field.setAccessible(true);
            field.set(packet, newValue);
            field.setAccessible(false);
        } catch (NoSuchFieldException | SecurityException | IllegalAccessException ex) {
            ex.printStackTrace();
        }
        
    }



    private void send(final Packet packet) {
        //PacketAccessor.sendPacket(getOnline(), this.packet);
        for (Player player : Bukkit.getOnlinePlayers()) {
            send(player,packet);
        }
    }
    
    private void send(final Player player, final Packet packet) {
        //PacketAccessor.sendPacket(player, this.packet);
        ((CraftPlayer)player).getHandle().playerConnection.sendPacket(packet);
    }
    
   /* static {
        try {
            if (!PacketAccessor.isLegacyVersion()) {
                final String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
                final Class<?> typeChatComponentText = Class.forName("net.minecraft.server." + version + ".ChatComponentText");
                PacketWrapper.ChatComponentText = typeChatComponentText.getConstructor(String.class);
                //PacketWrapper.typeEnumChatFormat = (Class<? extends Enum>)Class.forName("net.minecraft.server." + version + ".EnumChatFormat");
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }*/

    



    

    
}
