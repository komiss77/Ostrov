package ru.komiss77.version;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import com.mojang.brigadier.tree.RootCommandNode;
import com.mojang.datafixers.DataFixer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedPlayerList;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.world.level.storage.PlayerDataStorage;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.spigotmc.SpigotConfig;
import ru.komiss77.Cfg;
import ru.komiss77.LocalDB;
import ru.komiss77.Ostrov;
import ru.komiss77.modules.games.GM;

public class PathServer {

  public static final List<String> vanilaCommandToDisable;

  static {
    vanilaCommandToDisable = Arrays.asList("execute",
        "bossbar", "defaultgamemode", "me", "help", "kick", "kill", "tell",
        "say", "spreadplayers", "teammsg", "tellraw", "trigger",
        "ban-ip", "banlist", "ban", "op", "pardon", "pardon-ip", "perf",
        "save-all", "save-off", "save-on", "setidletimeout", "publish");
  }

  public static void path() {


    SpigotConfig.belowZeroGenerationInExistingChunks = false;
    SpigotConfig.restartOnCrash = false;
    SpigotConfig.movedWronglyThreshold = 1.6;//Double.MAX_VALUE;
    SpigotConfig.movedTooQuicklyMultiplier = 10;//Double.MAX_VALUE;
    SpigotConfig.sendNamespaced = false;//Bukkit.spigot().getConfig().s
    SpigotConfig.whitelistMessage = "§cНа сервере включен список доступа, и вас там нет!";
    SpigotConfig.unknownCommandMessage = "§cКоманда не найдена. §a§l/menu §f-открыть меню.";
    SpigotConfig.serverFullMessage = "Слишком много народу!";
    SpigotConfig.outdatedClientMessage = "§cВаш клиент устарел! Пожалуйста, используйте §b{0}";
    SpigotConfig.outdatedServerMessage = "§cСервер старой версии {0}, вход невозможен.";
    SpigotConfig.restartMessage = "§4Перезагрузка...";

    //по умолчанию. Ниже меняем, где надо по другому
    SpigotConfig.disableAdvancementSaving = true;
    SpigotConfig.disabledAdvancements = Collections.emptyList();
    //SpigotConfig.disabledAdvancements = Arrays.asList("*", "minecraft:story/disabled");
    SpigotConfig.disableStatSaving = true;
    SpigotConfig.disablePlayerDataSaving = true; //файлы .dat не надо
    LocalDB.useLocalData = true; //сохранять в мускул

    switch (GM.GAME) {
      case LOBBY -> {
        //файлы не надо, база нужна
      }
      case PA -> {
        //файлы не надо, база нужна
      }
      case JL -> {
        LocalDB.useLocalData = false;
      }
      case DA, AR, MI, SK, OB, SE -> {
        SpigotConfig.disableAdvancementSaving = false;
        SpigotConfig.disableStatSaving = false;
        SpigotConfig.disablePlayerDataSaving = false;
        //LocalDB.useLocalData = true;
      }
      case CS, GR, HS, KB, SG, SW, WZ, ZH -> { //миниигры где использовать мускул
        //LocalDB.useLocalData = true;
      }
      case BW, BB, QU, SN, TW -> { //миниигры где НЕ использовать мускул
        LocalDB.useLocalData = false;
      }
      default -> {

        //if (Ostrov.MOT_D.equals("loll")) {
        LocalDB.useLocalData = Cfg.getConfig().getBoolean("local_database.use_default");
        //}
        //SpigotConfig.disableAdvancementSaving = true;
        //SpigotConfig.disableStatSaving = true;
        //SpigotConfig.disablePlayerDataSaving = true;
        //LocalDB.useLocalData = true;
      }
    }

    if (SpigotConfig.disablePlayerDataSaving == false) {
      final DedicatedServer dedicatedServer = Craft.toNMS();
      final DedicatedPlayerList dedicatedPlayerList = dedicatedServer.getPlayerList();
      final PlayerDataStorage oldPds = dedicatedPlayerList.playerIo;
      try {
        final Field dataFixerField = oldPds.getClass().getDeclaredField("fixerUpper");
        dataFixerField.setAccessible(true);
        final DataFixer fixerUpper = (DataFixer) dataFixerField.get(oldPds);
        dataFixerField.setAccessible(false);
        //osPds.playerDir = oldPds.getPlayerDir();
        final OsPlayerDataStorage osPds = new OsPlayerDataStorage(dedicatedServer.storageSource, fixerUpper);//dedicatedPlayerList.playerIo;
        //dedicatedServer.playerDataStorage = osPds;
        //dedicatedPlayerList.playerIo = osPds;
        final Field playerIoField = dedicatedPlayerList.getClass().getField("playerIo"); //getDeclaredField-без наслодования
        playerIoField.setAccessible(true);
        playerIoField.set(dedicatedPlayerList, osPds);
        playerIoField.setAccessible(false);
        final Field playerDataStorageField = dedicatedServer.getClass().getField("playerDataStorage");
        playerDataStorageField.setAccessible(true);
        playerDataStorageField.set(dedicatedServer, osPds);
        playerDataStorageField.setAccessible(false);
      } catch (IllegalAccessException | IllegalArgumentException | NoSuchFieldException | SecurityException ex) {
        Ostrov.log_warn("nms Server pathServer PlayerDataStorage : " + ex.getMessage());
        ex.printStackTrace();
      }
    }

    final MinecraftServer srv = MinecraftServer.getServer();
    final com.mojang.brigadier.CommandDispatcher<CommandSourceStack> dispatcher = srv.getCommands().getDispatcher();
    final RootCommandNode<CommandSourceStack> root = dispatcher.getRoot();

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
            children.remove(name);
            literals.remove(name);
            arguments.remove(name);
          }
      );

    } catch (IllegalAccessException | IllegalArgumentException | NoSuchFieldException | SecurityException ex) {
      Ostrov.log_warn("nms Server pathServer RootCommandNode : " + ex.getMessage());
    }


    //отключить устаревшие тайминги
    try {
      final File cfg = new File(Bukkit.getWorldContainer().getPath() + "/config/paper-global.yml");
      final YamlConfiguration yml = YamlConfiguration.loadConfiguration(cfg);
      if (yml.getConfigurationSection("timings") != null) {
        if (yml.getConfigurationSection("timings").getBoolean("enabled")) {
          yml.getConfigurationSection("timings").set("enabled", false);
          yml.save(cfg);
        }
      }
    } catch (IOException | NullPointerException ex) {
      Ostrov.log_err("не удалось изменить timings : " + ex.getMessage());
    }


    Ostrov.log_ok("§bСервер сконфигурирован, отключено ванильных команд: " + vanilaCommandToDisable.size());
  }


  //public static boolean storeWorldPosition() { //серв сохраняет сам
  //  return switch (GM.GAME) {
  //    case AR, DA, MI, SE -> true;
  //    default -> false;
  //  };
  //}


}
