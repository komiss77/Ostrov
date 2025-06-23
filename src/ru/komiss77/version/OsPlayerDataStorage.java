package ru.komiss77.version;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import com.google.gson.*;
import com.mojang.datafixers.DataFixer;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.minecraft.FileUtil;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.ServerStatsCounter;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatType;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.FileNameDateFormatter;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.PlayerDataStorage;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import ru.komiss77.*;
import ru.komiss77.enums.ServerType;
import ru.komiss77.modules.games.GM;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;
import ru.komiss77.modules.quests.Quest;
import ru.komiss77.modules.quests.progs.IProgress;
import ru.komiss77.utils.LocUtil;
import ru.komiss77.utils.NumUtil;

public class OsPlayerDataStorage extends PlayerDataStorage {

  //PlayerAdvancements - подменяется путь при загрузке
  //ServerStatsCounter - сохраняется в нужный файл, загрузка -

  public static File dataDir;
  private static Method toJsonMethod;
  private static Field playerSavePath;
  private static Field statField;
  private static final Gson GSON;
  private static final DateTimeFormatter FORMATTER = FileNameDateFormatter.create();

  static {
    GSON = new GsonBuilder().create();
    try {
      toJsonMethod = ServerStatsCounter.class.getDeclaredMethod("toJson");
      toJsonMethod.setAccessible(true);
      //codec = PlayerAdvancements.class.getDeclaredField("codec");
      //codec.setAccessible(true);
      playerSavePath = PlayerAdvancements.class.getDeclaredField("playerSavePath");
      playerSavePath.setAccessible(true);
      statField = ServerPlayer.class.getDeclaredField("stats");
      statField.setAccessible(true);
    } catch (SecurityException | IllegalArgumentException | NoSuchMethodException | NoSuchFieldException ex) {
      Ostrov.log_err("OsPlayerDataStorage : " + ex.getMessage());
      ex.printStackTrace();
    }
  }

  public OsPlayerDataStorage(LevelStorageSource.LevelStorageAccess levelStorageAccess, DataFixer fixerUpper) {
    super(levelStorageAccess, fixerUpper);
    dataDir = new File(Bukkit.getWorldContainer().getPath() + File.separator + "playerdata");
    if (!dataDir.exists()) {
      dataDir.mkdir();
    }
  }


  public Optional<CompoundTag> load(Player nmsPlayer) {
    if (nmsPlayer instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
      org.bukkit.craftbukkit.entity.CraftPlayer craftPlayer = serverPlayer.getBukkitEntity();
      final Oplayer op = PM.createOplayer(craftPlayer); //создать обязательно тут

      PlayerAdvancements adv = serverPlayer.getAdvancements();
      try { //для гостей делайм файл-заглушку
        final String advFileName = op.isGuest ? "guest_adv.json" : nmsPlayer.getScoreboardName() + "_adv.json";
        final File advFile = new File(dataDir, advFileName);
        playerSavePath.set(adv, advFile.toPath());
        adv.reload(serverPlayer.getServer().getAdvancements());
        //Path path = (Path) playerSavePath.get(adv);
//Ostrov.log_warn("OsPlayerDataStorage adv load : Path="+path.toString());
        final String statFileName = op.isGuest ? "guest_stat.json" : nmsPlayer.getScoreboardName() + "_stat.json";
        final File statFile = new File(dataDir, statFileName);
        final ServerStatsCounter serverStatsCounter = new ServerStatsCounter(serverPlayer.getServer(), statFile);
        statField.set(serverPlayer, serverStatsCounter);

      } catch (NullPointerException | IllegalAccessException | IllegalArgumentException ex) {
        Ostrov.log_warn("OsPlayerDataStorage adv load : " + ex.getMessage());
        ex.printStackTrace();
      }


      return load(nmsPlayer.getName().getString(), nmsPlayer.getStringUUID()).map(tag -> {
        // Only update first played if it is older than the one we have
        //long modified = new File(dataDir, nmsPlayer.getStringUUID() + ".dat").lastModified();
        long modified = new File(dataDir, nmsPlayer.getName() + ".dat").lastModified();
        if (modified < craftPlayer.getFirstPlayed()) {
          craftPlayer.setFirstPlayed(modified);
        }

        //op.mysqlData.put("name", op.nik); //надо что-то добавить, или Timer будет думать, что не загрузилось
        //op.mysqlData.put("uuid", nmsPlayer.getStringUUID());
        String[] split;
        int splitterIndex;

        for (String key : tag.keySet()) {
//Ostrov.log_warn("key= "+key);
          //if (key.startsWith("os_")) {
          //  op.mysqlData.put(key.substring(3), tag.getString(key).get());
          //}
          switch (key) {
            case "os_positions" -> {
              split = LocalDB.LINE.split(tag.getString("os_positions").get());
              for (String positionInfo : split) {
                splitterIndex = LocalDB.WORD.index(positionInfo);
                if (splitterIndex > 0) {
                  op.world_positions.put(positionInfo.substring(0, splitterIndex), positionInfo.substring(splitterIndex + 1));
                }
              }
            }
            case "os_homes" -> {
              split = LocalDB.LINE.split(tag.getString("os_homes").get());
              for (String info : split) {
                splitterIndex = LocalDB.WORD.index(info);
                if (splitterIndex > 0) {
                  op.homes.put(info.substring(0, splitterIndex), info.substring(splitterIndex + 1));
                }
              }
            }
            case "os_kitsUseData" -> {
              split = LocalDB.LINE.split(tag.getString("os_kitsUseData").get());
              int stamp;
              for (String info : split) {
                splitterIndex = LocalDB.WORD.index(info);
                if (splitterIndex > 0) {
                  stamp = NumUtil.intOf(info.substring(splitterIndex + 1), 0);
                  if (stamp > 0) {
                    op.kits_use_timestamp.put(info.substring(0, splitterIndex), stamp);
                  }
                }
              }
            }
            case "os_quests" -> {
              op.mysqlData.put("quests", tag.getString("os_quests").get());
            }
          }
        }

//Ostrov.log_warn("mysqlData= "+op.mysqlData);
        nmsPlayer.load(tag); // From below

        Ostrov.log_ok("§2file данные " + op.nik + " загружны");

        return tag;

      });
    } else {
      return Optional.empty();
    }
  }

  public Optional<CompoundTag> load(String name, String uuid) {
    Optional<CompoundTag> optional = load(name, uuid, ".dat");
    if (optional.isEmpty()) {
      backup(name, uuid, ".dat");
    }
    return optional.or(() -> load(name, uuid, ".dat_old")).map(compoundTag -> { // CraftBukkit
      int dataVersion = NbtUtils.getDataVersion(compoundTag, -1);
      compoundTag = DataFixTypes.PLAYER.updateToCurrentVersion(this.fixerUpper, compoundTag, dataVersion);
      // player.load(compoundTag); // CraftBukkit - handled above
      return compoundTag;
    });
  }


  public void save(Player player) { //после PlayerQuitEvent, учесть автосохраннение
    final String name = player.getName().getString();
    org.bukkit.craftbukkit.entity.CraftPlayer craftPlayer = (CraftPlayer) player.getBukkitEntity();
    final Oplayer op = PM.getOplayer(craftPlayer);//remove(p.getUniqueId());
    if (op.makeToRemove) {//if (op==null) { //при выключении серв. вызывается дважды - сервером для сохранения и при PlayerQuitEvent, когда ор уже нет.
      //Ostrov.log_warn("OsPlayerDataStorage op = null! "+name);
      PM.remove(craftPlayer);
    }
//Ostrov.log_warn("OsPlayerDataStorage save "+name+" makeToRemove="+op.makeToRemove);
    //if (org.spigotmc.SpigotConfig.disablePlayerDataSaving) return; // Spigot
    if (!LocalDB.useLocalData) return;

    if (op.isGuest) {
      Ostrov.log_warn("OsPlayerDataStorage Выход гостя " + op.nik + ", данные не сохраняем.");
      return;
    }
    try {
      CompoundTag tag = player.saveWithoutId(new CompoundTag());

      // for (Map.Entry<String,String> en : op.mysqlData.entrySet()) {
      //   tag.putString("os_" + en.getKey(), en.getValue());
      // }

      StringBuilder build = new StringBuilder();

      if (GM.GAME.type != ServerType.ARENAS) {
        if (op.spyOrigin == null) {
          op.world_positions.put("logoutLoc", LocUtil.toDirString(craftPlayer.getLocation()));
          //op.mysqlData.put(craftPlayer.getWorld().getName(), LocUtil.toDirString(craftPlayer.getLocation()));
        } else {
          op.world_positions.put("logoutLoc", LocUtil.toDirString(op.spyOrigin));
          //op.mysqlData.put(craftPlayer.getWorld().getName(), LocUtil.toDirString(op.spyOrigin));
        }
        //if (p.getRespawnLocation() != null) {
        //   op.world_positions.put("bedspawnLoc", LocUtil.toString(p.getRespawnLocation()));
        //}
        Location exist;
        for (Map.Entry<String, String> en : op.world_positions.entrySet()) {
          exist = LocUtil.stringToLoc(en.getValue(), false, false);
          if (exist == null) { //фикс: на скайблоке насохраняло гостевые миры
            Ostrov.log_warn("OsPlayerDataStorage save world_positions мир " + en.getKey() + " недоступен, игнор.");
          } else {
            build.append(LocalDB.LINE.get()).append(en.getKey()).append(LocalDB.WORD.get()).append(en.getValue());
          }
        }
        tag.putString("os_positions", build.isEmpty() ? "" : build.substring(1));//final String positions = build.replaceFirst(bigSplit, "");
      }


      if (!op.homes.isEmpty()) { //при загрузке ключа не будат, добавляется пустой при изменении домов
        build = new StringBuilder();
        for (String home : op.homes.keySet()) { //только при изменении!
          build.append(LocalDB.LINE.get()).append(home).append(LocalDB.WORD.get()).append(op.homes.get(home));
        }
        tag.putString("os_homes", build.isEmpty() ? "" : build.substring(1));//final String homes = build.replaceFirst(bigSplit, "");
      }

      if (!op.kits_use_timestamp.isEmpty()) { //при загрузке ключа не будат, добавляется пустой при изменении наборов
        build = new StringBuilder();
        for (String useTimeStamp : op.kits_use_timestamp.keySet()) {  //только при изменении!
          build.append(LocalDB.LINE.get()).append(useTimeStamp).append(LocalDB.WORD.get()).append(op.kits_use_timestamp.get(useTimeStamp));
        }
        tag.putString("os_kitsUseData", build.isEmpty() ? "" : build.substring(1));//final String kitsUseData = build.replaceFirst(bigSplit, "");
      }

      if (!op.quests.isEmpty()) { //при загрузке ключа не будат, добавляется пустой при изменении наборов
        build = new StringBuilder();
        for (final Map.Entry<Quest, IProgress> en : op.quests.entrySet()) {  //только при изменении!
          build.append(LocalDB.LINE.get()).append(en.getKey().code).append(en.getValue().isDone() ? "" : LocalDB.WORD.get() + en.getValue().getSave());
        }
        tag.putString("os_quests", build.isEmpty() ? "" : build.substring(1));//final String kitsUseData = build.replaceFirst(bigSplit, "");
      }

      Path path = dataDir.toPath();
      Path path1 = Files.createTempFile(path, player.getScoreboardName() + "-", ".dat");
      NbtIo.writeCompressed(tag, path1);
      Path path2 = path.resolve(name + ".dat");//path.resolve(player.getStringUUID() + ".dat");
      Path path3 = path.resolve(name + ".dat_old");//path.resolve(player.getStringUUID() + ".dat_old");
      Util.safeReplaceFile(path2, path1, path3);

    } catch (Exception ex) {
      Ostrov.log_warn("OsPlayerDataStorage Failed to save player data for " + name + ":" + ex.getMessage()); // Paper - Print exception
    }
    Ostrov.log_ok("§2file данные " + op.nik + " сохранены");
    //final ServerPlayer sp = (ServerPlayer) player;

    //org.spigotmc.SpigotConfig.disableStatSaving = false;
    if (!org.spigotmc.SpigotConfig.disableStatSaving) {
      //ServerStatsCounter serverStatsCounter = sp.getStats(); // CraftBukkit
      //if (serverStatsCounter != null) {
      //serverStatsCounter.save();
          /*File statFile = new File(dataDir, name + "_stat.json");
          try {
            final String toJson = (String) toJsonMethod.invoke(serverStatsCounter);
            FileUtils.writeStringToFile(statFile, toJson);//FileUtils.writeStringToFile(statFile, serverStatsCounter.toJson());
          } catch (IOException | NullPointerException | IllegalAccessException | IllegalArgumentException |
                   InvocationTargetException ex) {
            Ostrov.log_warn("OsPlayerDataStorage stat save : " + ex.getMessage());
          }*/
      //}
    } else {
      Ostrov.log_warn("OsPlayerDataStorage disableStatSaving");
    }

    //org.spigotmc.SpigotConfig.disableAdvancementSaving = false;
    if (!org.spigotmc.SpigotConfig.disableAdvancementSaving) {
      //PlayerAdvancements playerAdvancements = sp.getAdvancements();
      //if (playerAdvancements != null) {
      //playerAdvancements.save();
         /* try {
            final JsonElement jsonElement = (JsonElement) codec.get(asData(playerAdvancements));
            // JsonElement jsonElement = (JsonElement)playerAdvancements.codec.encodeStart(JsonOps.INSTANCE, playerAdvancements.asData()).getOrThrow();
            //playerAdvancements.save();
            File advFile = new File(dataDir, name + "_adv.json");
            try (Writer bufferedWriter = Files.newBufferedWriter(advFile.toPath(), StandardCharsets.UTF_8)) {
              GSON.toJson(jsonElement, GSON.newJsonWriter(bufferedWriter));
            }
          } catch (NullPointerException | IllegalAccessException | IllegalArgumentException | IOException ex) {
            Ostrov.log_warn("OsPlayerDataStorage adv save : " + ex.getMessage());
          }*/
      //}
    } else {
      Ostrov.log_warn("OsPlayerDataStorage disableAdvancementSaving");
    }

  }


/*
  private Data asData(PlayerAdvancements playerAdvancements) {
    Map<ResourceLocation, AdvancementProgress> map = new LinkedHashMap<>();
    playerAdvancements.progress.forEach((advancementHolder, progress) -> {
      if (progress.hasProgress()) {
        map.put(advancementHolder.id(), progress);
      }
    });
    return new Data(map);
  }

  record Data(Map<ResourceLocation, AdvancementProgress> map) {
    public static final Codec<Data> CODEC = Codec.unboundedMap(ResourceLocation.CODEC, AdvancementProgress.CODEC)
        .xmap(Data::new, Data::map);

    public void forEach(BiConsumer<ResourceLocation, AdvancementProgress> action) {
      this.map.entrySet().stream().sorted(Map.Entry.comparingByValue()).forEach(entry -> action.accept(entry.getKey(), entry.getValue()));
    }
  }
*/


  private Optional<CompoundTag> load(String name, String stringUuid, String suffix) { // до PlayerJoinEvent
//Ostrov.log_warn("OsPlayerDataStorage load "+name+ (Bukkit.isPrimaryThread()?" sync":" async"));
    File file = new File(dataDir, name + suffix); // CraftBukkit
    if (file.exists() && file.isFile()) {
      try {
        CompoundTag tag = NbtIo.readCompressed(file.toPath(), NbtAccounter.unlimitedHeap());

//Ostrov.log_warn("OsPlayerDataStorage load CompoundTag "+name);
        Optional<CompoundTag> optional = Optional.of(tag);
        //if (usingWrongFile) {
        //file.renameTo(new File(file.getPath() + ".offline-read"));
        //}
        return optional;
      } catch (Exception var5) {
        Ostrov.log_warn("OsPlayerDataStorageFailed to load player data for " + name); // CraftBukkit
      }
    }

    return Optional.empty();
  }

  private void backup(String name, String stringUuid, String suffix) { // CraftBukkit
    Path path = dataDir.toPath();
    Path path1 = path.resolve(name + suffix); //path.resolve(stringUuid + suffix); // CraftBukkit
    //Path path2 = path.resolve(stringUuid + "_corrupted_" + LocalDateTime.now().format(FORMATTER) + suffix); // CraftBukkit
    Path path2 = path.resolve(name + "_corrupted_" + LocalDateTime.now().format(FORMATTER) + suffix); // CraftBukkit
    if (Files.isRegularFile(path1)) {
      try {
        Files.copy(path1, path2, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
      } catch (Exception ex) {
        Ostrov.log_warn("OsPlayerDataStorage Failed to copy the player.dat file for " + name + ":" + ex.getMessage()); // CraftBukkit
      }
    }
  }


  public File getPlayerDir() {
    return dataDir;
  }

  private static <T> Codec<Map<Stat<?>, Integer>> createTypedStatsCodec(StatType<T> type) {
    Codec<T> codec = type.getRegistry().byNameCodec();
    Codec<Stat<?>> codec1 = codec.flatComapMap(
        type::get,
        stat -> stat.getType() == type
            ? DataResult.success((T) stat.getValue())
            : DataResult.error(() -> "Expected type " + type + ", but got " + stat.getType())
    );
    return Codec.unboundedMap(codec1, Codec.INT);
  }

}


