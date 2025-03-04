package ru.komiss77;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class OConfig {

    private static final String LOAD = "load";

    private int comments;
    //private final OstrovConfigManager manager;

    private final boolean load; //стало пихать во все конфиги, перенёс в ItemGroup - переделал бы по нормальному вместо того чтоб убирать
    private final File file;
    protected FileConfiguration config;

    public OConfig(final File configFile, final int comments) {
        this.comments = comments;

        this.file = configFile;
        this.config = YamlConfiguration.loadConfiguration(configFile);
        load = config.getBoolean(LOAD);
    }

    public OConfig(final File configFile, final int comments, final boolean loadable) {
        this.comments = comments;

        this.file = configFile;
        this.config = YamlConfiguration.loadConfiguration(configFile);
        if (!loadable) {
            load = false;
            return;
        }
        if (!config.contains(LOAD)) {
            config.set(LOAD, false);
            saveConfig();
            load = false;
            return;
        }
        load = config.getBoolean(LOAD);
    }

    public boolean load() {return load;}

    public Object get(String path) {
        return this.config.get(path);
    }

    public Object get(String path, Object def) {
        return this.config.get(path, def);
    }

    public <T> T getObject(String path, Class<T> clazz) {
        return this.config.getObject(path, clazz);
    }

    public <T> T getObject(String path, Class<T> clazz, final T def) {
        return this.config.getObject(path, clazz, def);
    }

    public String getString(String path) {
        final String str = this.config.getString(path);
        return str == null ? "" : str;
    }

    public String getString(String path, String def) {
        return this.config.getString(path, def);
    }

    public int getInt(String path) {
        return this.config.getInt(path);
    }

    public int getInt(String path, int def) {
        return this.config.getInt(path, def);
    }

    public boolean getBoolean(String path) {
        return this.config.getBoolean(path);
    }

    public boolean getBoolean(String path, boolean def) {
        return this.config.getBoolean(path, def);
    }

    public void createSection(String path) {
        this.config.createSection(path);
    }

    public ConfigurationSection getConfigurationSection(String path) {
        return this.config.getConfigurationSection(path);
    }

    public double getDouble(String path) {
        return this.config.getDouble(path);
    }

    public double getDouble(String path, double def) {
        return this.config.getDouble(path, def);
    }

    public List<?> getList(String path) {
        return this.config.getList(path);
    }

    public List<?> getList(String path, List<?> def) {
        return this.config.getList(path, def);
    }

    public Collection<String> getStringList(String path) {
        return this.config.getStringList(path);
    }


    public boolean contains(String path) {
        return this.config.contains(path);
    }

    public void removeKey(String path) {
        this.config.set(path, null);
    }

    public void set(String path, Object value) {
        this.config.set(path, value);
    }

    public void addDefault(String path, Object value) {
        if (this.config.get(path) == null) this.config.set(path, value);
    }

    public void addDefault(String path, Object value, String comment) {
        if (this.get(path) == null) this.set(path, value, comment);
    }

    public void addDefault(String path, Object value, String[] comment) {
        if (this.get(path) == null) this.set(path, value, comment);
    }

    public void set(String path, Object value, String comment) {
        if (!this.config.contains(path)) {
            this.config.set(Cfg.manager.getPluginName() + "_COMMENT_" + comments, " " + comment);
            comments++;
        }

        this.config.set(path, value);

    }

    public void set(String path, Object value, String[] comment) {
        for (final String comm : comment) {
            if (!this.config.contains(path)) {
                this.config.set(Cfg.manager.getPluginName() + "_COMMENT_" + comments, " " + comm);
                comments++;
            }
        }
        this.config.set(path, value);
    }

    public void setHeader(String[] header) {
        Cfg.manager.setHeader(this.file, header);
        this.comments = header.length + 2;
        this.reloadConfig();
    }

    public void reloadConfig() {
        //this.config = YamlConfiguration.loadConfiguration(manager.getConfigContent(file));
        this.config = YamlConfiguration.loadConfiguration(file);
    }

    public void saveConfig() {
        Cfg.manager.saveConfig(config.saveToString(), this.file);

    }

    public Set<String> getKeys() {
        final Set<String> keys = config.getKeys(false);
        keys.remove(LOAD);
        return keys;
    }

}
