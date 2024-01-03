package ru.komiss77.version.v1_20_R1;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scheduler.BukkitRunnable;
import org.spigotmc.SpigotConfig;
import com.mojang.brigadier.tree.RootCommandNode;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.papermc.paper.adventure.PaperAdventure;
import net.kyori.adventure.key.Key;
import net.minecraft.commands.CommandListenerWrapper;
import net.minecraft.core.BlockPosition.MutableBlockPosition;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.network.protocol.game.PacketPlayInUpdateSign;
import net.minecraft.network.protocol.game.PacketPlayOutBlockChange;
import net.minecraft.network.protocol.game.PacketPlayOutOpenSignEditor;
import net.minecraft.network.protocol.game.PacketPlayOutSetSlot;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.item.EnumColor;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.entity.TileEntitySign;
import net.minecraft.world.level.block.state.IBlockData;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Ostrov;
import ru.komiss77.modules.games.GM;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;
import ru.komiss77.modules.world.Cuboid;
import ru.komiss77.modules.world.XYZ;
import ru.komiss77.utils.PlayerInput;
import ru.komiss77.utils.TCUtils;
import ru.komiss77.utils.inventory.InputButton;
import ru.komiss77.version.IServer;

public class Server implements IServer {

    public static final List<String> vanilaCommandToDisable = Arrays.asList("execute",
            "bossbar", "defaultgamemode", "me", "help", "kick", "kill", "tell",
            "say", "spreadplayers", "teammsg", "tellraw", "trigger",
            "ban-ip", "banlist", "ban", "op", "pardon", "pardon-ip", "perf", "save-all", "save-off", "save-on", "setidletimeout", "publish");
    // private static Field bQ; //bU = net.minecraft.world.entity.player.EntityHuman -> public final ContainerPlayer bT; 
    // private static Method cC; //nmsWorld  //net.minecraft.world.entity.Entity public World cC()
    protected static final MutableBlockPosition mutableBlockPosition;
    private static final DedicatedServer ds;
    private static final IChatBaseComponent emtc = IChatBaseComponent.a("");
    private static final IBlockData signIbd;
    private static final Key chatKey;
    private static final Method getWrld = mkGet(".CraftWorld", "getHandle");
    private static final Method getEt = mkGet(".entity.CraftEntity", "getHandle");
    private static final Method getLE = mkGet(".entity.CraftLivingEntity", "getHandle");
    private static final Method getPl = mkGet(".entity.CraftPlayer", "getHandle");

    private static Method mkGet(final String pth, final String mtd) {
        try {
            return Class.forName(Bukkit.getServer().getClass().getPackageName() + pth).getDeclaredMethod(mtd);
        } catch (NoSuchMethodException | SecurityException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    static {
        chatKey = Key.key("ostrov_chat", "listener");
        mutableBlockPosition = new MutableBlockPosition(0, 0, 0);
        signIbd = getBD();
        DedicatedServer dds = null;
        try {
        	dds = (DedicatedServer) Bukkit.getServer().getClass().getMethod("getServer").invoke(Bukkit.getServer());
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
        	e.printStackTrace();
        }
        ds = dds;
    }

    private static IBlockData getBD() {
        try {
            return (IBlockData) mkGet(".block.data.CraftBlockData", "getState").invoke(Material.ACACIA_SIGN.createBlockData());
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public WorldServer toNMS(final World w) {
        try {
            return (WorldServer) getWrld.invoke(w);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            return null;
        }
    }

    @Override
    public net.minecraft.world.entity.Entity toNMS(final Entity en) {
        try {
            return (net.minecraft.world.entity.Entity) getEt.invoke(en);
            //return (EntityPlayer) getEt.invoke(en);
        //ClassCastException: class net.minecraft.world.entity.monster.EntityVex cannot be cast to class net.minecraft.server.level.EntityPlayer (net.minecraft.world.entity.monster.EntityVex and net.minecraft.server.level.EntityPlayer are in unnamed module of loader java.net.URLClassLoader @2b71fc7e)
	//at ru.komiss77.version.v1_20_R1.Server.toNMS(Server.java:119) ~[Ostrov.jar:?]
	//at ru.komiss77.version.v1_20_R1.EntityGroup.sendLookAtPlayerPacket(EntityGroup.java:258) ~[Ostrov.jar:?]
	//at ru.komiss77.modules.figure.SpeachTask.sendLookResetPacket(SpeachTask.java:118) ~[Ostrov.jar:?]
	//at ru.komiss77.modules.figure.SpeachTask.cancel(SpeachTask.java:77) ~[Ostrov.jar:?]
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            return null;
        }
    }

    @Override
    public EntityLiving toNMS(final LivingEntity le) {
        try {
            return (EntityLiving) getLE.invoke(le);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            return null;
        }
    }

    @Override
    public EntityPlayer toNMS(final Player p) {
        try {
            return (EntityPlayer) getPl.invoke(p);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            return null;
        }
    }

    @Override
    public DedicatedServer toNMS() {
        return ds;
    }

    @Override
    public void chatFix() {

        // Chat Report fix    
        // https://github.com/e-im/FreedomChat https://www.libhunt.com/r/FreedomChat
        final ChatHandler handler = new ChatHandler();
        io.papermc.paper.network.ChannelInitializeListenerHolder.addListener(
                chatKey,
                channel -> channel.pipeline().addAfter("packet_handler", "ostrov_chat_handler", handler)
        );
        Ostrov.log_ok("§bchatFix - блокировка уведомлений подписи чата");

    }

    @Override
    public void signInput(final Player p, String suggest, final XYZ signXyz) {

        final EntityPlayer ep = toNMS(p);

        mutableBlockPosition.d(signXyz.x, signXyz.y, signXyz.z);
        PacketPlayOutBlockChange packet = new PacketPlayOutBlockChange(mutableBlockPosition, signIbd);
        ep.c.a(packet);

        final TileEntitySign sign = new TileEntitySign(mutableBlockPosition, signIbd);

        EnumColor color = EnumColor.a;
        if (suggest.length() >= 2 && suggest.charAt(0) == '§') {
            switch (suggest.charAt(1)) {
                case '0' ->
                    color = EnumColor.a;
                case '1' ->
                    color = EnumColor.b;
                case '2' ->
                    color = EnumColor.c;
                case '3' ->
                    color = EnumColor.d;
                case '4' ->
                    color = EnumColor.e;
                case '5' ->
                    color = EnumColor.f;
                case '6' ->
                    color = EnumColor.g;
                case '7' ->
                    color = EnumColor.h;
                case '8' ->
                    color = EnumColor.i;
                case '9' ->
                    color = EnumColor.j;
                case 'a' ->
                    color = EnumColor.k;
                case 'b' ->
                    color = EnumColor.l;
                case 'c' ->
                    color = EnumColor.m;
                case 'd' ->
                    color = EnumColor.n;
                case 'e' ->
                    color = EnumColor.o;
                case 'f' ->
                    color = EnumColor.p;
            }
            suggest = suggest.substring(2);
        }

        final IChatBaseComponent[] comps = new IChatBaseComponent[4];
        Arrays.fill(comps, emtc);
        boolean last = true;
        switch (suggest.length() >> 4) {
            default:
                last = false;
            case 3:
                comps[3] = PaperAdventure.asVanilla(TCUtils.format(suggest.substring(48, last ? suggest.length() : 65)));
                last = false;
            case 2:
                comps[2] = PaperAdventure.asVanilla(TCUtils.format(suggest.substring(32, last ? suggest.length() : 47)));
                last = false;
            case 1:
                comps[1] = PaperAdventure.asVanilla(TCUtils.format(suggest.substring(16, last ? suggest.length() : 31)));
                last = false;
            case 0:
                comps[0] = PaperAdventure.asVanilla(TCUtils.format(suggest.substring(0, last ? suggest.length() : 15)));
                break;
        }

        final SignText signtext = new SignText(comps, comps, color, true);
        sign.a(signtext, true);
        ep.c.a(sign.j());

        final PacketPlayOutOpenSignEditor outOpenSignEditor = new PacketPlayOutOpenSignEditor(mutableBlockPosition, true);
        ep.c.a(outOpenSignEditor);//sendPacket(outOpenSignEditor);*/

        final ChannelDuplexHandler channelDuplexHandler = new ChannelDuplexHandler() {
            @Override
            public void channelRead(ChannelHandlerContext ctx, Object packet) throws Exception {
                if (packet instanceof PacketPlayInUpdateSign signPacket) {
                    final String chName = ctx.name();
//Ostrov.log_warn("inUpdateSign ctx="+chName);
                    final ChannelPipeline pipeline = ctx.pipeline();
                    if (pipeline.get(chName) != null) {
                        pipeline.remove(chName);
                    }

                    if (chName.startsWith("ostrov_sign_")) {
                        final String name = ctx.name().substring(12);
                        if (!name.isEmpty()) {
                            //final Oplayer op = PM.getOplayer(name);
                            //if (op.inputData != null && op.inputData.type == InputButton.InputType.SIGN) {
                            final String result = signPacket.d()[0] + signPacket.d()[1] + signPacket.d()[2] + signPacket.d()[3];
                            //op.inputData.setResult(result);
                            PlayerInput.onInput(name, InputButton.InputType.SIGN, result);
                            //}
                            //final BlockPosition bps = signPacket.a();
                        }
                        //return;
                    }

                }
                super.channelRead(ctx, packet);
            }
        ;
        };
        
        final ChannelPipeline pipeline = ep.c.h.m.pipeline();////EntityPlayer->PlayerConnection->NetworkManager->Chanell->ChannelPipeline
        pipeline.addBefore("packet_handler", "ostrov_sign_" + p.getName(), channelDuplexHandler);

    }

    @Override
    public Material getFastMat(final World w, int x, int y, int z) {
        final WorldServer worldServer = toNMS(w);
        final IBlockData iBlockData = worldServer.a_(mutableBlockPosition.d(x, y, z));
        return iBlockData.getBukkitMaterial();
    }

    @Override
    public byte[] encodeBase64(byte[] binaryData) {
        return org.apache.commons.codec.binary.Base64.encodeBase64(binaryData);
    }

    @Override
    public void pathServer() {
        final MinecraftServer srv = toNMS();

        final com.mojang.brigadier.CommandDispatcher<CommandListenerWrapper> dispatcher = srv.vanillaCommandDispatcher.a();
        final RootCommandNode<CommandListenerWrapper> root = dispatcher.getRoot();

        try {

            Field childrenField = root.getClass().getSuperclass().getDeclaredField("children");
            childrenField.setAccessible(true);

            Field literalsField = root.getClass().getSuperclass().getDeclaredField("literals");
            literalsField.setAccessible(true);

            Field argumentsField = root.getClass().getSuperclass().getDeclaredField("arguments");
            argumentsField.setAccessible(true);

            Map<?, ?> children = (Map<?, ?>) childrenField.get(root);
            Map<?, ?> literals = (Map<?, ?>) literalsField.get(root);
            Map<?, ?> arguments = (Map<?, ?>) argumentsField.get(root);

            //Полученного экземпляра Field уже достаточно для доступа к изменяемым приватным полям.
            vanilaCommandToDisable.forEach((name) -> {
                if (children.containsKey(name)) {
                    children.remove(name);
                }
                if (literals.containsKey(name)) {
                    literals.remove(name);
                }
                if (arguments.containsKey(name)) {
                    arguments.remove(name);
                }
            }
            );

        } catch (Exception ex) { //NoSuchFieldException | SecurityException | IllegalAccessException ex) {
            Ostrov.log_warn("nms Server pathServer : " + ex.getMessage());
        }

        SpigotConfig.belowZeroGenerationInExistingChunks = false;
        SpigotConfig.restartOnCrash = false;
        SpigotConfig.disablePlayerDataSaving = true;
        SpigotConfig.movedWronglyThreshold = 1.6;//Double.MAX_VALUE;
        SpigotConfig.movedTooQuicklyMultiplier = 10;//Double.MAX_VALUE;
        SpigotConfig.sendNamespaced = false;//Bukkit.spigot().getConfig().s
        SpigotConfig.whitelistMessage = "§cНа сервере включен список доступа, и вас там нет!";
        SpigotConfig.unknownCommandMessage = "§c?? §fНаберите комадну §b§l/menu";
        SpigotConfig.serverFullMessage = "Слишком много народу!";
        SpigotConfig.outdatedClientMessage = "§cВаш клиент устарел! Пожалуйста, используйте §b{0}";
        SpigotConfig.outdatedServerMessage = "§cСервер старой версии {0}, вход невозможен.";
        SpigotConfig.restartMessage = "§4Перезагрузка...";

        switch (GM.GAME) {
            case AR, DA, OB, SW, MI -> {
//Ostrov.log("+++++++++++++++++++++++++++++");
                SpigotConfig.disableAdvancementSaving = false;
                SpigotConfig.disabledAdvancements = Collections.emptyList();
                SpigotConfig.disableStatSaving = false;
            }
            default -> {
//Ostrov.log("--------------------------------");
                SpigotConfig.disableAdvancementSaving = true;
                SpigotConfig.disabledAdvancements = Arrays.asList("*", "minecraft:story/disabled");
                SpigotConfig.disableStatSaving = true;
            }
        }

        Ostrov.log_ok("§bСервер сконфигурирован, отключено ванильных команд: " + vanilaCommandToDisable.size());
    }

    @Override
    public void pathWorld(final World w) {
        WorldServer ws = toNMS(w);
        ws.spigotConfig.tileMaxTickTime = 5;
        ws.spigotConfig.entityMaxTickTime = 5;
    }

    @Override
    public void pathPermissions() {
        final PluginManager spm = Bukkit.getPluginManager();
        for (Permission dp : spm.getDefaultPermissions(false)) {
            dp.setDefault(PermissionDefault.OP);
        }
    }

    @Override
    public int getTps() {
        return (int) MinecraftServer.TPS;
    }

    @Override
    public int getitemDespawnRate(final World w) { //skyworld
        //return SpigotConfig.itemDespawnRate;
        final WorldServer ws = toNMS(w);
        return ws.spigotConfig.itemDespawnRate;
    }

    @Override
    public void sendFakeEquip(final Player p, final int playerInventorySlot, final ItemStack itemStack) {
        final EntityPlayer entityPlayer = toNMS(p);
        net.minecraft.world.inventory.Container cont = entityPlayer.bQ;
        final Packet<?> packet = new PacketPlayOutSetSlot(cont.j, playerInventorySlot, playerInventorySlot,
                net.minecraft.world.item.ItemStack.fromBukkitCopy(itemStack));
        entityPlayer.c.a(packet);
    }

    @Override
    public void sendChunkChange(final Player p, final Chunk chunk) {
        chunk.getWorld().refreshChunk(chunk.getX(), chunk.getZ());
        final net.minecraft.world.level.World nmsWorld = toNMS(p.getWorld());
        final net.minecraft.world.level.chunk.Chunk nmsChunk = nmsWorld.getChunkIfLoaded(chunk.getX(), chunk.getZ());
        final ClientboundLevelChunkWithLightPacket packet = new ClientboundLevelChunkWithLightPacket(nmsChunk, nmsWorld.s_(), null, null, true);
        toNMS(p).c.a(packet);//sendPacket(p, packet);
    }

    @Override
    public void BorderDisplay(final Player p, final XYZ minPoint, final XYZ maxPoint, final boolean tpToCenter) {
        final Oplayer op = PM.getOplayer(p);
        final Cuboid cuboid = new Cuboid(minPoint, maxPoint);

        if (op.displayCube != null && !op.displayCube.isCancelled()) {
            op.displayCube.cancel();
        }

        op.displayCube = new BukkitRunnable() {
            final Set<XYZ> border = cuboid.getBorder();
            final Location particleLoc = new Location(p.getWorld(), 0, 0, 0);

            @Override
            public void run() {
                if (p == null || !p.isOnline()) {
                    this.cancel();
                    return;
                }
                if (p.isDead() || p.isSneaking()) {
                    p.resetTitle();
                    this.cancel();
                    return;
                }
                border.stream().forEach(
                        (xyz) -> {
                            particleLoc.set(xyz.x, xyz.y, xyz.z);
                            if (xyz.pitch >= 5) { //стенки
                                p.spawnParticle(Particle.FIREWORKS_SPARK, particleLoc, 0);
                            } else {
                                p.spawnParticle(Particle.VILLAGER_HAPPY, particleLoc, 0);
                            }
                        }
                );
                ApiOstrov.sendTitle(p, "", "§7Шифт - остановить показ", 0, 30, 0);
            }
        }.runTaskTimerAsynchronously(Ostrov.instance, 10, 25);

        if (tpToCenter && !cuboid.contains(p.getLocation())) {
            final Location center = cuboid.getCenter(p.getLocation());
            p.teleport(center);
        }
    }

    @Override
    public BlockData getBlockData(final IBlockData iBlockData) {
        return iBlockData.createCraftBlockData();
    }

}
