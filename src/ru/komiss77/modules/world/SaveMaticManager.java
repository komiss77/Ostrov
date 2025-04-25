package ru.komiss77.modules.world;

import java.nio.file.Files;
import java.nio.file.Path;
import org.bukkit.Bukkit;
import org.bukkit.World;
import ru.komiss77.Cfg;
import ru.komiss77.Initiable;
import ru.komiss77.OConfig;
import ru.komiss77.Ostrov;

public class SaveMaticManager implements Initiable {

    private static final Path SAVE_PATH = Path.of(Schematic.DEF_PATH, "saves");
    private static final String CON_NAME = "saves.yml";
    public static final String PRFX = "save-";

    private static int sId = -1;

    public SaveMaticManager() {
        reload();
    }

    @Override
    public void postWorld() { //обход модулей после загрузки миров, т.к. не всё можно сделать onEnable
        if (!Cfg.savematics) return;
        Ostrov.async(() -> {
            final OConfig irc = Cfg.manager.config(CON_NAME, false);
            for (final String svs : irc.getKeys()) {
                final Path svp = Path.of(SAVE_PATH.toString(), svs + Schematic.DEF_EXT);
                if (!Files.exists(svp) || !Files.isRegularFile(svp)) return;
                final Schematic sv = new Schematic(Bukkit.getConsoleSender(), svp.toFile(), true);
                sv.paste(Bukkit.getConsoleSender(), BVec.parse(irc.getString(svs)), true);
                irc.set(svs, null);
            }
            irc.saveConfig();
            sId = 0;
        });
    }

    public static int save(final Cuboid cb, final World w) {
        if (!Cfg.savematics || sId < 0) {
            Ostrov.log_warn("§6SaveMatics havent loaded yet!");
            return -1;
        }
        final int id = sId++;
        final Schematic sch = new Schematic(Bukkit.getConsoleSender(), PRFX + id, "", cb, w, false);
        Ostrov.async(() -> {
            sch.save(Bukkit.getConsoleSender(), SAVE_PATH.toString());
            final OConfig irc = Cfg.manager.config(CON_NAME, false);
            irc.set(sch.getName(), BVec.of(w, cb.minX, cb.minY, cb.minZ).toString());
            irc.saveConfig();
        });
        return id;
    }

    public static void load(final int id) {
        if (!Cfg.savematics || id < 0) {
            Ostrov.log_warn("§6SaveMatics wrong format!");
            return;
        }
        Ostrov.async(() -> {
            final OConfig irc = Cfg.manager.config(CON_NAME, false);
            final String name = PRFX + id;
            final String loc = irc.getString(name);
            if (loc == null || loc.isEmpty()) {
                Ostrov.log_warn("§6No save spawn with id " + id + "!");
                return;
            }
            final Path svp = Path.of(SAVE_PATH.toString(), name + Schematic.DEF_EXT);
            if (!Files.exists(svp) || !Files.isRegularFile(svp)) {
                Ostrov.log_warn("§6No save file with id " + id + "!");
                return;
            }
            final Schematic sv = new Schematic(Bukkit.getConsoleSender(), svp.toFile(), true);
            sv.paste(Bukkit.getConsoleSender(), BVec.parse(loc), true);
            irc.set(name, null);
            irc.saveConfig();
        });
    }

    @Override
    public void reload() {
        if (!Cfg.savematics) return;
        Ostrov.log_ok("§2Сохранения включены!");
    }

    @Override
    public void onDisable() {
        if (!Cfg.savematics) return;
        Ostrov.log_ok("§6Сохранения выключены!");
    }
}
