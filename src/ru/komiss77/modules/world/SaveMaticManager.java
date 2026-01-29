package ru.komiss77.modules.world;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.bukkit.Bukkit;
import org.bukkit.World;
import ru.komiss77.Cfg;
import ru.komiss77.Initiable;
import ru.komiss77.Ostrov;

public class SaveMaticManager implements Initiable {

    private static final Path SAVE_PATH = Path.of(Schematic.DEF_PATH, "saves");
    public static final String PRFX = "save-";

    private static int sId = -1;

    public SaveMaticManager() {
        reload();
    }

    @Override
    public void postWorld() { //обход модулей после загрузки миров, т.к. не всё можно сделать onEnable
        if (!Cfg.savematics) return;
        Ostrov.async(() -> {
            try (final Stream<Path> saves = Files.walk(SAVE_PATH)) {
                saves.filter(Files::isRegularFile).forEach(svp -> {
                    if (!Files.isRegularFile(svp) || !svp.endsWith(Schematic.DEF_EXT)) return;
                    final Schematic sv = new Schematic(Bukkit.getConsoleSender(), svp.toFile(), true);
//Ostrov.log_warn("SaveMaticManager postWorld BVec.parse param="+sv.getParam());
                    sv.paste(Bukkit.getConsoleSender(), BVec.parse(sv.getParam()), true);
                });
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            sId = 0;
        });
    }

  public static int save(final @Nonnull Cuboid cb, final @Nonnull World w, final @Nullable Runnable onDone) {
        if (!Cfg.savematics || sId < 0) {
            Ostrov.log_warn("§6SaveMatics havent loaded yet!");
            return -1;
        }
        final int id = sId++;
        final Schematic sch = new Schematic(Bukkit.getConsoleSender(), PRFX + id, BVec.of(w, cb.minX + cb.spawnAddX,
            cb.minY + cb.spawnAddY, cb.minZ + cb.spawnAddZ).toString(), cb, w, false);
        Ostrov.async(() -> {
            sch.save(Bukkit.getConsoleSender(), SAVE_PATH.toString());
          if (onDone != null) Ostrov.sync(onDone);
        });
        return id;
    }

    public static void load(final int id) {
        if (!Cfg.savematics || id < 0) {
            Ostrov.log_warn("§6SaveMatics wrong format!");
            return;
        }
        Ostrov.async(() -> {
            final Path svp = Path.of(SAVE_PATH.toString(), PRFX + id + Schematic.DEF_EXT);
            if (!Files.exists(svp) || !Files.isRegularFile(svp)) {
                Ostrov.log_warn("§6No save file with id " + id + "!");
                return;
            }
            final Schematic sv = new Schematic(Bukkit.getConsoleSender(), svp.toFile(), true);
//Ostrov.log_warn("SaveMaticManager load BVec.parse param="+sv.getParam());
            sv.paste(Bukkit.getConsoleSender(), BVec.parse(sv.getParam()), true);
        });
    }

    @Override
    public void reload() {
        if (!Cfg.savematics) return;
        Ostrov.log_ok("§2Сохраматики включены!");
    }

    @Override
    public void onDisable() {
        if (!Cfg.savematics) return;
        Ostrov.log_ok("§6Сохраматики выключены!");
    }
}
