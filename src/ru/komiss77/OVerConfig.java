package ru.komiss77;

import java.io.File;
import java.util.Set;

public class OVerConfig extends OConfig {

    private static final String VER_PATH = "version";
    private static final int MIN_VER = 0;

    public final boolean isOld;

    public OVerConfig(final File configFile, final int comments, final int current) {
        super(configFile, comments);
        int version = getInt(VER_PATH, MIN_VER);
        isOld = version < current;
        if (!isOld) return;
        set(VER_PATH, current);
        saveConfig();
    }

    public Set<String> getKeys() {
        final Set<String> keys = this.config.getKeys(false);
        keys.remove(VER_PATH);
        return keys;
    }

}
