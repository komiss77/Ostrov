package ru.komiss77;

import javax.annotation.Nullable;
import java.io.*;
import java.nio.charset.StandardCharsets;
import com.google.common.base.Charsets;
import org.bukkit.plugin.java.JavaPlugin;


public class OConfigManager {

    private final JavaPlugin plugin;

    /**
     * Manage custom configurations and files
     */
    public OConfigManager(final JavaPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Get new configuration with header
     * @param filePath - Path to file
     * @return - New SimpleConfig
     */
    @Deprecated
    public OConfig getNewConfig(final String filePath, final String[] header) {
        return config(filePath, header);

    }

    /**
     * Get new configuration
     * @param filePath - Path to file
     * @return - New SimpleConfig
     */
    @Deprecated
    public OConfig getNewConfig(final String filePath) {
        return config(filePath);
    }

    /**
     * Get new configuration
     * @param path Path to file
     * @return New SimpleConfig
     */
    public OConfig config(final String path) {
        return this.config(path, null);
    }

    /**
     * Get new configuration with header
     * @param path Path to file
     * @return New SimpleConfig
     */
    public OConfig config(final String path, final @Nullable String[] header) {
        final File file = this.getConfigFile(path);
        if (!file.exists()) {
            this.prepareFile(path);

            if (header != null && header.length != 0) {
                this.setHeader(file, header);
            }

        }
        return new OConfig(file, this.getCommentsNum(file));
    }

    /**
     * Get new configuration with header
     * @param path Path to file
     * @return New SimpleConfig
     */
    public OConfig config(final String path, final boolean loadable) {
        return this.config(path, null, loadable);
    }

    /**
     * Get new configuration with header
     * @param path Path to file
     * @return New SimpleConfig
     */
    public OConfig config(final String path, final @Nullable String[] header, final boolean loadable) {
        final File file = this.getConfigFile(path);
        if (file == null) return Cfg.getConfig();
        if (!file.exists()) {
            this.prepareFile(path);

            if (header != null && header.length != 0) {
                this.setHeader(file, header);
            }

        }
        return new OConfig(file, this.getCommentsNum(file), loadable);
    }

    /**
     * Get configuration file from string
     * @param file - File path
     * @return - New file object
     */
    private @Nullable File getConfigFile(final String file) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        File configFile;
        if (file.contains("/")) {
            if (file.startsWith("/")) {
                configFile = new File(plugin.getDataFolder() + file.replace("/", File.separator));
            } else {
                configFile = new File(plugin.getDataFolder() + File.separator + file.replace("/", File.separator));
            }
        } else {
            configFile = new File(plugin.getDataFolder(), file);
        }

        return configFile;

    }

    /**
     * Create new file for config and copy resource into it
     * @param filePath - Path to file
     * @param resource - Resource to copy
     */
    public void prepareFile(String filePath, String resource) {

        File file = this.getConfigFile(filePath);

        if (file.exists()) {
            return;
        }

        try {
            file.getParentFile().mkdirs();
            file.createNewFile();

            if (resource != null && !resource.isEmpty()) {
                this.copyResource(plugin.getResource(resource), file);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Create new file for config without resource
     * @param filePath - File to create
     */
    public void prepareFile(String filePath) {
        this.prepareFile(filePath, null);
    }

    /**
     * Adds header block to config
     * @param file - Config file
     * @param header - Header lines
     */
    public void setHeader(File file, String[] header) {

        if (!file.exists()) {
            return;
        }

        try {
            String currentLine;
            StringBuilder config = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                while ((currentLine = reader.readLine()) != null) {
                    config.append(currentLine).append("\n");
                }
            }
            config.append("# +----------------------------------------------------+ #\n");

            for (String line : header) {

                if (line.length() > 50) {
                    continue;
                }

                int lenght = (50 - line.length()) / 2;
                StringBuilder finalLine = new StringBuilder(line);

                for (int i = 0; i < lenght; i++) {
                    finalLine.append(" ");
                    finalLine.reverse();
                    finalLine.append(" ");
                    finalLine.reverse();
                }

                if (line.length() % 2 != 0) {
                    finalLine.append(" ");
                }

                config.append("# < ").append(finalLine.toString()).append(" > #\n");

            }

            config.append("# +----------------------------------------------------+ #");

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                writer.write(this.prepareConfigString(config.toString()));
                writer.flush();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Read file and make comments SnakeYAML friendly
     * @param file - Path to file
     * @return - File as Input Stream
     */
    public InputStream getConfigContent(File file) {

        if (!file.exists()) {
            return null;
        }

        try {
            int commentNum = 0;

            String addLine;
            String currentLine;
            String pluginName = this.getPluginName();

            StringBuilder whole = new StringBuilder();
            InputStream configStream;
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                while ((currentLine = reader.readLine()) != null) {

                    if (currentLine.startsWith("#")) {
                        addLine = currentLine.replaceFirst("#", pluginName + "_COMMENT_" + commentNum + ":");
                        whole.append(addLine).append("\n");
                        commentNum++;

                    } else {
                        whole.append(currentLine).append("\n");
                    }

                }
                String config = whole.toString();
                configStream = new ByteArrayInputStream(config.getBytes(StandardCharsets.UTF_8));
            }
            return configStream;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

    }

    /**
     * Get comments from file
     * @param file - File
     * @return - Comments number
     */
    public int getCommentsNum(File file) {

        if (!file.exists()) {
            return 0;
        }

        try {
            int comments = 0;
            String currentLine;

            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                while ((currentLine = reader.readLine()) != null) {

                    if (currentLine.startsWith("#")) {
                        comments++;
                    }

                }
            }
            return comments;

        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }

    }

    /**
     * Get config content from file
     * @param filePath - Path to file
     * @return - readied file
     */
    public InputStream getConfigContent(String filePath) {
        return this.getConfigContent(this.getConfigFile(filePath));
    }


    private String prepareConfigString(String configString) {

        int lastLine = 0;
        int headerLine = 0;

        String[] lines = configString.split("\n");
        StringBuilder config = new StringBuilder();

        for (String line : lines) {

            if (line.startsWith(this.getPluginName() + "_COMMENT")) {
                String comment = "#" + line.trim().substring(line.indexOf(":") + 1);

                if (comment.startsWith("# +-")) {

                    /*
                     * If header line = 0 then it is
                     * header start, if it's equal
                     * to 1 it's the end of header
                     */

                    if (headerLine == 0) {
                        config.append(comment).append("\n");

                        lastLine = 0;
                        headerLine = 1;

                    } else {
                        config.append(comment).append("\n\n");

                        lastLine = 0;
                        headerLine = 0;

                    }

                } else {

                    /*
                     * Last line = 0 - Comment
                     * Last line = 1 - Normal path
                     */

                    String normalComment;

                    if (comment.startsWith("# ' ")) {
                        normalComment = comment.substring(0, comment.length() - 1).replaceFirst("# ' ", "# ");
                    } else {
                        normalComment = comment;
                    }

                    if (lastLine == 0) {
                        config.append(normalComment).append("\n");
                    } else {
                        config.append("\n").append(normalComment).append("\n");
                    }

                    lastLine = 0;

                }

            } else {
                config.append(line).append("\n");
                lastLine = 1;
            }

        }

        return config.toString();

    }


    /**
     * Saves configuration to file
     * @param configString - Config string
     * @param file - Config file
     */
    public void saveConfig(String configString, File file) {
        try (final Writer writer = new OutputStreamWriter(new FileOutputStream(file), Charsets.UTF_8)) {
            writer.write(prepareConfigString(configString));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("deprecation")
    public String getPluginName() {
        return plugin.getDescription().getName();
    }

    /**
     * Copy resource from Input Stream to file
     * @param resource - Resource from .jar
     * @param file - File to write
     */
    private void copyResource(InputStream resource, File file) {

        try {
            try (OutputStream out = new FileOutputStream(file)) {
                int lenght;
                byte[] buf = new byte[1024];

                while ((lenght = resource.read(buf)) > 0) {
                    out.write(buf, 0, lenght);
                }
            }
            resource.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
