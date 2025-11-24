package ru.komiss77;

import java.sql.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.komiss77.events.LocalDataLoadEvent;
import ru.komiss77.modules.entities.PvPManager;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;
import ru.komiss77.modules.quests.Quest;
import ru.komiss77.modules.quests.progs.IProgress;
import ru.komiss77.utils.MoveUtil;
import ru.komiss77.utils.NumUtil;
import ru.komiss77.utils.StringUtil;


public class LocalDB {

    public static boolean useLocalData = false; //ставится в PathServer, раньше было из конфига local_database.use
    private static String url;
    public static final StringUtil.Split WORD = StringUtil.Split.SMALL;
    public static final StringUtil.Split LINE = StringUtil.Split.MEDIUM;
    private static final Set<String> fieldsExist = new HashSet<>();
    ;
    protected static Connection connection;
    private static boolean tabbleSetupDone;

    public enum MysqlDataState {NONE, NEW_RECORD, LOADED, ERROR}

    //может когда-то потом переделать, пока слишком удобно и быстро
    public static final char L_SPLIT = '∬';
    public static final char W_SPLIT = '∫';
    public static final String LINE_SPLIT = "∬";
    public static final String WORD_SPLIT = "∫";


    //при загрузке делаем синхронно, если нет локального соединения - будет подвисать!
    //при тесте сединения вызывается async из Timer
    public static void init() {
        String host = Cfg.getConfig().getString("local_database.mysql_host");
        String user = Cfg.getConfig().getString("local_database.mysql_user");
        String passw = Cfg.getConfig().getString("local_database.mysql_passw");
        url = host + "?useSSL=false&allowPublicKeyRetrieval=true&useUnicode=true&characterEncoding=utf-8&user=" + user + "&password=" + passw;
        if (useLocalData) {
            connect();
            if (connection != null) {
                setupTable();
            }
        }
    }

    public static void executePstAsync(final CommandSender cs, final String querry) {
        if (!useLocalData || connection == null) return;
        Ostrov.async(() -> {
            PreparedStatement pst = null;
            try {
                pst = connection.prepareStatement(querry);
                pst.execute();
            } catch (SQLException ex) {
                Ostrov.log_err("LocalDB executePst " + querry + " : " + ex.getMessage());
                if (cs != null) cs.sendMessage("§cОшибка выполнения запроса " + querry);
            } finally {
                try {
                    if (pst != null) pst.close();
                } catch (SQLException ex) {
                    Ostrov.log_err("LocalDB executePst close " + ex.getMessage());
                }
            }
        }, 0);
    }


    // при PlayerQuitEvent Async, p!=null
    // при onDisable Sync, p может быть null
    public static void saveLocalData(final Oplayer op) {
//Ostrov.log_warn("---saveLocalData useLocalData?"+useLocalData+" conn?"+(connection==null?"null":"ok"+" isGuest?"+op.isGuest)+" mysqlDataState="+op.mysqlDataState);
//if (1==1) return; //заглушка, ничего не сохраняем
        if (!useLocalData) return; //op.mysqlData будет null, если при загрузке была ошибка
        if (op.isGuest) {
            Ostrov.log_warn("Выход гостя " + op.nik + ", данные не сохраняем.");
            return;
        }

        if (op.mysqlDataState == MysqlDataState.ERROR) {//op.dbError != null) {
            Ostrov.log_err("У " + op.nik + " была ошибка загрузки данных, не сохраняем.");
            return;
        }

        final long l = System.currentTimeMillis();

        if (!op.quests.isEmpty()) { //при загрузке ключа не будат, добавляется пустой при изменении наборов
            StringBuilder build = new StringBuilder();
            for (final Entry<Quest, IProgress> en : op.quests.entrySet()) {  //только при изменении!
                build.append(LINE.get()).append(en.getKey().code).append(en.getValue().isDone() ? "" : WORD.get() + en.getValue().getSave());
            }
            op.mysqlData.put("quests", build.isEmpty() ? "" : build.substring(1));//final String kitsUseData = build.replaceFirst(bigSplit, "");
        }

        //1мин=60 1час=3600 1день=86400 1мес=2592000 3мес=7776000 1год=31104000
        //минимум 90 дней
        op.mysqlData.put("lastActivity", String.valueOf(Timer.secTime()));//final int timeStamp = Timer.getTime();
        op.mysqlData.put("validTo", String.valueOf(ApiOstrov.getStorageLimit(op)));//final int validTo = ApiOstrov.getStorageLimit(op);

        final StringBuilder sb;
        final String query;

        if (op.mysqlData.containsKey("id")) { //запись уже была в БД
//op.mysqlData.put("vvv", "bbb");
            final String id = op.mysqlData.remove("id");
            sb = new StringBuilder("UPDATE `playerData` SET ");

            for (final String key : op.mysqlData.keySet()) {
                if (!fieldsExist.contains(key) && !addField(key)) { //не удалось добавить столбец - игнорим его данные
                    continue;
                }
                sb.append(",`").append(key).append("`='").append(op.mysqlData.get(key)).append("'");
            }

            sb.append(" WHERE `id`='").append(id).append("';");

            query = sb.toString().replaceFirst(",", "");
//Ostrov.log_warn("UPDATE! UserId="+id+" query="+query);

        } else { //создаём новую запись
            op.mysqlData.put("name", op.nik);
            op.mysqlData.put("uuid", op.id.toString());
            sb = new StringBuilder("INSERT INTO `playerData` (");
            final StringBuilder values = new StringBuilder(" VALUES (");

            for (final String key : op.mysqlData.keySet()) {
                if (!fieldsExist.contains(key) && !addField(key)) { //не удалось добавить столбец - игнорим его данные
                    continue;
//Ostrov.log_warn("добавить поле "+key);   
                }
                sb.append(",`").append(key).append("`");
                values.append(",'").append(op.mysqlData.get(key)).append("'");
            }
            sb.append(")");
            values.append(");");

            query = sb.toString().replaceFirst(",", "") + values.toString().replaceFirst(",", "");
//Ostrov.log_warn("INSERT! query="+query);   
        }


        try (final PreparedStatement pst = connection.prepareStatement(query)) {
            try {
                pst.executeUpdate();
                Ostrov.log_ok("§2mysql данные " + op.nik + " сохранены, " + (System.currentTimeMillis() - l) + "мс");

            } catch (SQLException e) {

                Ostrov.log_err("saveLocalData error " + op.nik + " -> " + e.getMessage());

            }
        } catch (SQLException ex) {
            Ostrov.log_err("saveLocalData close error - " + ex.getMessage());
        }
    }


    //нужно для миниигр, они сохраняют/получают данные через op.mysqlData
    //вызывается PlayerJoinEvent ASYNC через 5 тиков!
    public static void loadLocalData(final String name) {
        //if (!useLocalData || !save_and_load_playerdata) return;
        if (connection == null) return;

        final Oplayer op = PM.getOplayer(name); //сюда приходит через 10 тиков, мог уже отключиться
        final Player p = Bukkit.getPlayerExact(name);

        if (op == null || p == null) {
            Ostrov.log_warn("PlayerJoinEvent loadLocalData " + name + " - уже оффлайн, player=" + (p == null ? "null" : p.isOnline() ? "online" : "offline") + " PM.exist?" + PM.exist(name));
            return;
        }

        if (op.isGuest) {
            Ostrov.sync(() -> {
                Bukkit.getPluginManager().callEvent(new LocalDataLoadEvent(p, op, null)); //записи не было
            }, 1);
            Ostrov.log_warn("Вход гостя " + op.nik + ", данные не загружаем.");
            return;
        }

        final long l = System.currentTimeMillis();

        ResultSet rs = null;
        try (final Statement stmt = LocalDB.getConnection().createStatement()) {

            rs = stmt.executeQuery("SELECT * FROM `playerData` WHERE `name` = '" + op.nik + "' LIMIT 1");

            if (!rs.next()) { //нет записи в БД - уходим на эвент
                //op.firstJoin = true;
                op.mysqlDataState = MysqlDataState.NEW_RECORD;
                Ostrov.sync(() -> {
                    Bukkit.getPluginManager().callEvent(new LocalDataLoadEvent(p, op, null)); //записи не было
                }, 1);
                if (op.firstJoin) { //мог быть файл с данными, тогда firstJoin поставится false в OsPlayerDataStorage
                    Ostrov.log_ok(op.nik + " : первый вход (firstJoin)");
                }
                return;
            }

            op.firstJoin = false; //false если есть запись в мускул ИЛИ файл с данными

            final ResultSetMetaData rmeta = rs.getMetaData();
            String fieldName;

            for (int i = 1; i <= rmeta.getColumnCount(); i++) {
                fieldName = rmeta.getColumnName(i);
                switch (fieldName) {
                    case "name", "uuid" -> {
                        //пропускаем, впихиваются выше
                    }
                    case "homes",  //сохраняться будут только при изменении
                         "kitsUseData", //сохраняться будут только при изменении
                         "positions" //сохраняется всегда
                        -> {
                    }
                    case "inventory", "armor", "ender", "potion",
                         "settings" -> {//сохраняться будет только при playerDataSQL
                    }
                    default -> op.mysqlData.put(rmeta.getColumnName(i), rs.getString(fieldName));
                }
            }
//Ostrov.log_warn("nonEmptyFields="+nonEmptyFields);
            //подстраховка - бывает загружает уже когда дисконнектился, чтобы не было пусто в дате
            op.mysqlData.put("id", rs.getString("id"));
            op.mysqlDataState = MysqlDataState.LOADED;

            final String qss = op.mysqlData.get("quests");
//Ostrov.log_warn("==quests="+qss);
            if (qss != null && !qss.isEmpty()) {
                final String[] split = LINE.split(qss);
                int stamp;
                for (String quest : split) {
                    if (quest.isEmpty()) continue;
                    final Quest qs = Quest.get(quest.charAt(0));
//Ostrov.log_warn("qs="+qs);
                    if (qs == null) continue;
                    final int splitterIndex = WORD.index(quest);
                    if (splitterIndex == 1) {
                        stamp = NumUtil.intOf(quest.substring(splitterIndex + 1), 0);
                        if (stamp > 0) op.quests.put(qs, qs.createPrg(stamp));
                    } else {
                        op.quests.put(qs, qs.createPrg(0).markDone());
                    }
                }
            }

            Ostrov.sync(() -> {
                if (PvPManager.no_damage_on_tp > 0) {
                    op.setNoDamage(PvPManager.no_damage_on_tp, true);
                }
                //пермишены наверняка уже будут - загрузка локал начинается через 10 тиков после входа
                if (p.getGameMode() == GameMode.SURVIVAL || p.getGameMode() == GameMode.ADVENTURE) {
                    if (p.isFlying() && (!Cfg.fly_command || !p.hasPermission("ostrov.fly"))) {
                        p.setFlying(false);
                        p.setAllowFlight(false);
                    }
                }

                if (Cfg.set_gm) { //на многих минииграх ставится ГМ из конфига
                    p.setGameMode(Cfg.gm_on_join);
                }
                if (p.getGameMode() == GameMode.SPECTATOR) { //SPECTATOR был поставлен при входе и не убрался, т.к. не было сохранения -
                    p.setGameMode(GameMode.SURVIVAL); //поставить выживание
                }
                if (p.getFlySpeed() > 0.1f && (!Cfg.fly_command || !Cfg.speed_command || !p.hasPermission("ostrov.flyspeed"))) {
                    p.setFlySpeed(0.1F);
                }
                if (p.getWalkSpeed() > 0.2f && (!Cfg.speed_command || !p.hasPermission("ostrov.walkspeed"))) {
                    p.setWalkSpeed(0.2F);
                }
                final LocalDataLoadEvent e = new LocalDataLoadEvent(p, op, null);
                Bukkit.getPluginManager().callEvent(e); //нормальный вызов с данными
                if (e.getLogoutLocation() != null) { //плагины могут изменять
                    MoveUtil.safeTP(p, e.getLogoutLocation());
                }
            }, 1);

            rs.close();

            //Загрузка оффлайн - платежей
            int offlinePayAdd = 0;
            int offlinePaySub = 0;

            rs = stmt.executeQuery("SELECT * FROM `moneyOffline` WHERE `name` LIKE '" + op.nik + "' ");
            while (rs.next()) {
                if (rs.getInt("value") > 0) {
                    offlinePayAdd += rs.getInt("value");
                } else {
                    offlinePaySub += rs.getInt("value");
                }
                ApiOstrov.moneyChange(p, rs.getInt("value"), "§5оффлайн платёж §f-> " + rs.getString("who"));
            }
            rs.close();

            if (offlinePayAdd != 0 || offlinePaySub != 0) {

                stmt.executeUpdate("DELETE FROM `moneyOffline` WHERE `name` LIKE '" + op.nik + "'");

                p.sendMessage((offlinePayAdd != 0 ? "§fВам поступили оффлайн-платежи на §a" + offlinePayAdd + " §fлони" : "") +
                    (offlinePayAdd != 0 && offlinePaySub != 0 ? "§f, и оффлайн-счета на §4" + offlinePaySub + "§f лони" : "") +
                    (offlinePayAdd == 0 ? "§fВам доставлены оффлайн-счета на §4" + offlinePaySub + "§f лони" : "") +
                    "."
                );
            }

            Ostrov.log_ok("§2данные mysql " + op.nik + " загружены, " + (System.currentTimeMillis() - l) + "мс");

        } catch (SQLException ex) {

            Ostrov.log_err("loadLocalData error  " + op.nik + " -> " + ex.getMessage());
            op.mysqlDataState = MysqlDataState.ERROR;
            //op.dbError = Error.PARSE; //op.mysqlData = null; //c null не будет сохранять при выходе!
            Ostrov.sync(() -> {
                op.updTabListName(p);
                Bukkit.getPluginManager().callEvent(new LocalDataLoadEvent(p, op, null)); //при ошибке вызов с пустыми данными
            }, 1);

        } finally {

            try {
                if (rs != null && !rs.isClosed()) rs.close();
            } catch (SQLException ex) {
                Ostrov.log_err("loadLocalData close error - " + ex.getMessage());
            }
        }
    }


    public static void moneyOffline(final String name, final int value, final String who) {
        if (!useLocalData || connection == null) {
            Ostrov.log_err("Оффлайн-перевод для " + name + " на сумму " + value + ", но локальная БД отключена!");
            return;
        }
        executePstAsync(Bukkit.getConsoleSender(), "INSERT INTO `moneyOffline` (name,value,who) VALUES ('" + name + "', '" + value + "', '" + who + "' );");
        final Player p = Bukkit.getPlayerExact(who);
        if (p != null) {
            p.sendMessage("§e" + name + " сейчас не на сервере, платёж будет выполнен при входе.");
        }
    }


    public static Connection getConnection() {
        return connection;
    }

    //вызывать ASYNC!!
    protected static void connect() {
        final long l = System.currentTimeMillis();
        disconnect();
        Ostrov.log_ok("§6MySQL - создаём local подключение..."); //не ставить log_err, или зацикливает!!!

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(url);
            Ostrov.log_ok("§6MySQL - local подключение создано за " + (System.currentTimeMillis() - l) + "мс.");
        } catch (SQLException | ClassNotFoundException e) {
            Ostrov.log_warn("§cMySql: соединение с базой local  не удалось, " + (System.currentTimeMillis() - l) + "мс. -> " + e.getMessage()); //не ставить log_err, или зацикливает!!!
            connection = null;
        }
    }

    public static void disconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
                //if (connection != null) {
                connection.close();
            }
            //connection = null;
        } catch (SQLException e) {
            Ostrov.log_warn("§cMySql: Disconnect local не удалось !" + e.getMessage());
        } finally {
            connection = null;
        }
    }


    private static void setupTable() {
        if (tabbleSetupDone) return;
        Statement stmt;
        try {
            stmt = connection.createStatement();
        } catch (SQLException e) {
            Ostrov.log_err("§4 setupTable: Ошибка инициализации БД -> " + e.getMessage());
            return;
        }

        final Set<String> tableExist = new HashSet<>();

        ResultSet rs;
        try {
            rs = stmt.executeQuery("SHOW TABLES;");
            while (rs.next()) {
                tableExist.add(rs.getString("Tables_in_" + connection.getCatalog()));
            }
            rs.close();
        } catch (SQLException ex) {
            Ostrov.log_err("§4 setupTable: Ошибка получения списка таблиц БД -> " + ex.getMessage());
        }


        if (tableExist.contains("data")) {
            try (final Statement stm = connection.createStatement()) {
                stm.executeUpdate(
                    "DROP TABLE `data` ; "
                );
            } catch (SQLException e) {
                Ostrov.log_err("§4 setupTable: Не удалось удалить таблицу data -> " + e.getMessage());
            }
        }


        if (useLocalData) {

            if (!tableExist.contains("playerData")) {
                try {
                    stmt.executeUpdate(
                        "CREATE TABLE IF NOT EXISTS `playerData` (" +
                            " `id` int(11) NOT NULL PRIMARY KEY AUTO_INCREMENT," +
                            " `name` varchar(16) CHARACTER SET utf8 COLLATE utf8_general_ci UNIQUE KEY NOT NULL," +
                            " `uuid` text NOT NULL," +
                            //" `settings` text NOT NULL," +
                            //" `homes` text NOT NULL," +
                            //" `positions` text NOT NULL," +
                            //" `inventory` mediumtext NOT NULL," +
                            //" `armor` text NOT NULL," +
                            //" `ender` mediumtext NOT NULL," +
                            //" `potion` text NOT NULL," +
                            //" `kitsUseData` text NOT NULL," +
                            " `lastActivity` int NOT NULL," +
                            " `validTo` int NOT NULL" +
                            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;"
                    );
                } catch (SQLException ex) {
                    Ostrov.log_err("§4 setupTable: Не удалось создать таблицу playerData -> " + ex.getMessage());
                }
            } else {
                try {
                    rs = stmt.executeQuery("SELECT * FROM `playerData` LIMIT 1");
                    final ResultSetMetaData rmeta = rs.getMetaData();
                    String fieldName;
                    boolean wipe = false;
                    for (int i = 1; i <= rmeta.getColumnCount(); i++) {
                        fieldName = rmeta.getColumnName(i);
                        switch (fieldName) {
                            case "homes", "kitsUseData", "positions",
                                 "inventory", "armor", "ender", "potion",
                                 "settings" -> {
                                wipe = true;
                                break;
                            }
                        }
                    }
                    if (wipe) {
                        stmt.executeUpdate(
                            "ALTER TABLE `playerData` DROP `settings`, DROP `homes`, DROP `kitsUseData`, DROP `positions`," +
                                "DROP `inventory`, DROP `armor`, DROP `ender`, DROP `potion`;"
                        );
                    }
                } catch (SQLException ex) {
                    Ostrov.log_err("§4 setupTable: Не удалось удалить старые поля playerData -> " + ex.getMessage());
                }

            }

            try {//SHOW COLUMNS FROM `playerData`
                rs = stmt.executeQuery("SHOW COLUMNS FROM `playerData`");
                while (rs.next()) {
                    fieldsExist.add(rs.getString("Field"));
                }
                rs.close();
//Ostrov.log_warn("fieldsExist="+fieldsExist);
            } catch (SQLException ex) {
                Ostrov.log_err("§4 setupTable: Не удалось получить список полей playerData -> " + ex.getMessage());
                fieldsExist.addAll(Arrays.asList("id", "name", "uuid", "settings", "homes", "positions", "inventory", "armor", "ender", "potion", "kitsUseData", "lastActivity", "validTo"));
            }


            if (!tableExist.contains("moneyOffline")) {
                try {
                    stmt.executeUpdate(
                        " CREATE TABLE IF NOT EXISTS `moneyOffline` ( " +
                            " `id` int NOT NULL PRIMARY KEY AUTO_INCREMENT," +
                            "  `name` varchar(16) NOT NULL," +
                            "  `value` int NOT NULL," +
                            "  `who` varchar(256) NOT NULL" +
                            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;");

                } catch (SQLException ex) {
                    Ostrov.log_err("§4 setupTable: Не удалось создать таблицу moneyOffline -> " + ex.getMessage());
                }
            }

        }


        if (!tableExist.contains("warps")) {
            try {
                stmt.executeUpdate(
                    " CREATE TABLE IF NOT EXISTS `warps` ( " +
                        "  `id` int NOT NULL PRIMARY KEY AUTO_INCREMENT, " +
                        "  `name` varchar(16) NOT NULL, " +
                        "  `dispalyMat` varchar(32) NOT NULL DEFAULT '', " +
                        "  `owner` varchar(16) NOT NULL DEFAULT '', " +
                        "  `descr` varchar(128) NOT NULL DEFAULT '', " +
                        "  `loc` varchar(64) NOT NULL DEFAULT '', " +
                        "  `system` tinyint(1) NOT NULL DEFAULT '1', " +
                        "  `open` tinyint(1) NOT NULL DEFAULT '1', " +
                        "  `need_perm` tinyint(1) NOT NULL DEFAULT '0', " +
                        "  `use_cost` int NOT NULL DEFAULT '0', " +
                        "  `use_counter` int NOT NULL DEFAULT '0', " +
                        "  `create_time` int NOT NULL DEFAULT '0' " +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;");

            } catch (SQLException ex) {
                Ostrov.log_err("§4 setupTable: Не удалось создать таблицу warps -> " + ex.getMessage());
            }
        }

        if (!tableExist.contains("errors")) {
            try {
                stmt.executeUpdate(
                    " CREATE TABLE IF NOT EXISTS `errors` ( " +
                        "`id` int NOT NULL PRIMARY KEY AUTO_INCREMENT," +
                        "`msg` varchar(512) NOT NULL," +
                        "`stamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP " +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;");

            } catch (SQLException ex) {
                Ostrov.log_err("§4 setupTable: Не удалось создать таблицу errors -> " + ex.getMessage());
            }
        }


        try {
            stmt.close();
        } catch (SQLException ex) {
            Ostrov.log_err("§4 setupTable: Ошибка закрытия Statement -> " + ex.getMessage());
        }
        tabbleSetupDone = true;
    }


    private static boolean addField(final String fieldName) {
        try (final Statement stm = connection.createStatement()) {
            //ALTER TABLE `playerData` ADD `ааа` VARCHAR(2) NOT NULL DEFAULT '' AFTER `validTo`;
            //connection.createStatement().executeUpdate( "ALTER TABLE `playerData` ADD COLUMN `"+fieldName+"` text NOT NULL;" );
            stm.executeUpdate("ALTER TABLE `playerData` ADD COLUMN `" + fieldName + "` VARCHAR(1024) NOT NULL DEFAULT '';");
            fieldsExist.add(fieldName);
            Ostrov.log_ok("§5Модификация таблицы `playerData` добавление столбца : " + fieldName);
            return true;
        } catch (SQLException ex) {
            Ostrov.log_err("Модификация таблицы `playerData`, добавление столбца : " + fieldName + " : " + ex.getMessage());
            return false;
        }
    }



}












/*

           //if (!op.mysqlData.containsKey("settings")) op.mysqlData.put("settings", ""); //нет дефолтного значения в табл.
            //if (!op.mysqlData.containsKey("homes")) op.mysqlData.put("homes", ""); //нет дефолтного значения в табл.
            //if (!op.mysqlData.containsKey("positions")) op.mysqlData.put("positions", ""); //нет дефолтного значения в табл.
            //if (!op.mysqlData.containsKey("inventory")) op.mysqlData.put("inventory", ""); //нет дефолтного значения в табл.
            //if (!op.mysqlData.containsKey("armor")) op.mysqlData.put("armor", ""); //нет дефолтного значения в табл.
            //if (!op.mysqlData.containsKey("ender")) op.mysqlData.put("ender", ""); //нет дефолтного значения в табл.
            //if (!op.mysqlData.containsKey("potion")) op.mysqlData.put("potion", ""); //нет дефолтного значения в табл.
            //if (!op.mysqlData.containsKey("kitsUseData")) op.mysqlData.put("kitsUseData", ""); //нет дефолтного значения в табл.

        //final String uuid = op.id.toString();
        /*if (online != null) {
            if (!online.contains(uuid)) {
                Ostrov.log_err("Данные " + op.nik + " уже были сохранены!");
                return;
            }
            online.removeKey(uuid);
            online.saveConfig();
        }


        //StringBuilder build = new StringBuilder();

      //if (!op.mysqlData.containsKey("name")) op.mysqlData.put("name", op.nik); //подстраховки - плагины могли очистить mysqlData
      //if (!op.mysqlData.containsKey("uuid")) op.mysqlData.put("uuid", op.id.toString()); //подстраховки - плагины могли очистить mysqlData
      //if (!op.mysqlData.containsKey("id") && op.mysqRecordId > Integer.MIN_VALUE) {
      //     op.mysqlData.put("id", String.valueOf(op.mysqRecordId));
      //}

        /*if (GM.GAME.type != ServerType.ARENAS) {
            if (p != null) {
                if (!op.mysqlData.containsKey("uuid")) op.mysqlData.put("uuid", uuid);
                if (op.spyOrigin == null) {
                    op.world_positions.put("logoutLoc", LocUtil.toDirString(p.getLocation()));
                    op.world_positions.put(p.getWorld().getName(), LocUtil.toDirString(p.getLocation()));
                } else {
                    op.world_positions.put("logoutLoc", LocUtil.toDirString(op.spyOrigin));
                    op.world_positions.put(p.getWorld().getName(), LocUtil.toDirString(op.spyOrigin));
                }
                if (p.getRespawnLocation() != null) {
                    op.world_positions.put("bedspawnLoc", LocUtil.toString(p.getRespawnLocation()));
                }
            }
            for (String posName : op.world_positions.keySet()) {
                build.append(LINE.get()).append(posName).append(WORD.get()).append(op.world_positions.get(posName));
            }
            op.mysqlData.put("positions", build.isEmpty() ? "" : build.substring(1));//final String positions = build.replaceFirst(bigSplit, "");
        } else {
            op.mysqlData.put("positions", "");
        }

        if (op.mysqlData.containsKey("homes")) { //при загрузке ключа не будат, добавляется пустой при изменении домов
            build = new StringBuilder();
            for (String home : op.homes.keySet()) { //только при изменении!
                build.append(LINE.get()).append(home).append(WORD.get()).append(op.homes.get(home));
            }
            op.mysqlData.put("homes", build.isEmpty() ? "" : build.substring(1));//final String homes = build.replaceFirst(bigSplit, "");
        }

        if (op.mysqlData.containsKey("kitsUseData")) { //при загрузке ключа не будат, добавляется пустой при изменении наборов
            build = new StringBuilder();
            for (String useTimeStamp : op.kits_use_timestamp.keySet()) {  //только при изменении!
                build.append(LINE.get()).append(useTimeStamp).append(WORD.get()).append(op.kits_use_timestamp.get(useTimeStamp));
            }
            op.mysqlData.put("kitsUseData", build.isEmpty() ? "" : build.substring(1));//final String kitsUseData = build.replaceFirst(bigSplit, "");
        }


       /*if (p == null) {
            op.mysqlData.put("settings", "");//settings = "";
            op.mysqlData.put("inventory", "null");//inventory = "null";
            op.mysqlData.put("armor", "null");//armor = "null";
            op.mysqlData.put("ender", "null");//ender = "null";
            op.mysqlData.put("potion", "null");//potion = "null";

        } else if (PLAYER_DATA_SQL) {
            if (PvPManager.getFlag(PvPManager.PvpFlag.drop_inv_inbattle) && PvPManager.getFlag(PvPManager.PvpFlag.antirelog) && op.pvp_time > 0) {
                op.mysqlData.put("inventory", "");
                op.mysqlData.put("armor", "");
            } else {
                op.mysqlData.put("inventory", ItemUtil.serialize(p.getInventory().getContents()));
                op.mysqlData.put("armor", ItemUtil.serialize(p.getInventory().getArmorContents()));
            }
            op.mysqlData.put("ender", ItemUtil.serialize(p.getEnderChest().getContents()));
            op.mysqlData.put("potion", ItemUtil.seripotlize(p.getActivePotionEffects()));
            op.mysqlData.put("settings", getSettings(p, op));//settings = sb.toString();
        }

//if (PLAYER_DATA_SQL) {
//   op.mysqlData.put("settings", "file"); //сохранение по новой схеме-данные в файле
//} else {
//   op.mysqlData.put("settings", ""); //в лобби нужен мускул но не нужен файл
//}
//op.mysqlData.put("homes", ""); //очистить старые данные, убрать когда всё мигрирует
//op.mysqlData.put("positions", ""); //очистить старые данные, убрать когда всё мигрирует
//op.mysqlData.put("inventory", ""); //очистить старые данные, убрать когда всё мигрирует
//op.mysqlData.put("armor", ""); //очистить старые данные, убрать когда всё мигрирует
//op.mysqlData.put("ender", ""); //очистить старые данные, убрать когда всё мигрирует
//op.mysqlData.put("potion", ""); //очистить старые данные, убрать когда всё мигрирует
//op.mysqlData.put("kitsUseData", "");  //очистить старые данные, убрать когда всё мигрирует



        /*if (online != null) {
            if (online.contains(uuid)) {
                op.mysqlData.put("name", op.nik); //надо что-то добавить, или Timer будет думать, что не загрузилось
                op.mysqlData.put("uuid", uuid);
                op.dbError = Error.DESYNC;
                ScreenUtil.sendTitleDirect(p, "<red>Ошибка Загрузки!", "<gold>Срочно сообщи об этом администрации!", 40, 1000, 100);
                p.sendMessage(Ostrov.PREFIX + "§cНапиши нам об ошибке в /discord или /telegram");
                Ostrov.sync(() -> {
                    Bukkit.getPluginManager().callEvent(new LocalDataLoadEvent(p, op, null)); //при ошибке вызов с пустыми данными
                }, 1);
                Ostrov.log_err("Данные " + op.nik + " уже были загружены!");
                return;
            }
            online.set(uuid, op.nik);
            Bukkit.getConsoleSender().sendMessage("Ok the config should have\n" + online.getKeys());
            online.saveConfig();
        }

            /*} else {
                String[] split;
                int splitterIndex;

                if (nonEmptyFields.contains("positions")) {//!rs.getString("positions").isEmpty()) {
                    split = LINE.split(rs.getString("positions"));
                    for (String positionInfo : split) {
                        splitterIndex = WORD.index(positionInfo);
                        if (splitterIndex > 0) {
                            op.world_positions.put(positionInfo.substring(0, splitterIndex), positionInfo.substring(splitterIndex + 1));
                        }
                    }
                }

                if (nonEmptyFields.contains("homes")) {//!rs.getString("homes").isEmpty()) {
                    split = LINE.split(rs.getString("homes"));
                    for (String info : split) {
                        splitterIndex = WORD.index(info);
                        if (splitterIndex > 0) {
                            op.homes.put(info.substring(0, splitterIndex), info.substring(splitterIndex + 1));
                        }
                    }
                }

                if (nonEmptyFields.contains("kitsUseData")) {//!rs.getString("kitsUseData").isEmpty()) {
                    split = LINE.split(rs.getString("kitsUseData"));
                    int stamp;
                    for (String info : split) {
                        splitterIndex = WORD.index(info);
                        if (splitterIndex > 0) {
                            stamp = NumUtil.intOf(info.substring(splitterIndex + 1), 0);
                            if (stamp > 0) {
                                op.kits_use_timestamp.put(info.substring(0, splitterIndex), stamp);
                            }
                        }
                    }
                }

                final ItemStack[] inventory;
                final ItemStack[] armor;
                final ItemStack[] ender;
                final Collection<PotionEffect> potion;

                if (!nonEmptyFields.contains("inventory")) {//rs.getString("inventory").isEmpty() || !PLAYER_DATA_SQL) {
                    inventory = null;
                } else {
                    if (rs.getString("inventory").equals("null")) {
                        Ostrov.log_err("Ошибка сохранения инвентаря в предыдущей сессии для " + op.nik);
                        inventory = null;
                    } else {
                        inventory = ItemUtil.deserialize(rs.getString("inventory"));
                    }
                }

                if (!nonEmptyFields.contains("armor")) {//rs.getString("armor").isEmpty() || !PLAYER_DATA_SQL) {
                    armor = null;
                } else {
                    if (rs.getString("armor").equals("error")) {
                        Ostrov.log_err("Ошибка сохранения экипировки в предыдущей сессии для " + op.nik);
                        armor = null;
                    } else {
                        armor = ItemUtil.deserialize(rs.getString("armor"));
                    }
                }

                if (!nonEmptyFields.contains("ender")) {//rs.getString("ender").isEmpty() || !PLAYER_DATA_SQL) {
                    ender = null;
                } else {
                    if (rs.getString("ender").equals("error")) {
                        Ostrov.log_err("Ошибка сохранения enderChest в предыдущей сессии для " + op.nik);
                        ender = null;
                    } else {
                        ender = ItemUtil.deserialize(rs.getString("ender"));
                    }
                }

                if (!nonEmptyFields.contains("potion")) {//rs.getString("potion").isEmpty() || !PLAYER_DATA_SQL) {
                    potion = null;
                } else {
                    if (rs.getString("potion").equals("error")) {
                        Ostrov.log_err("Ошибка сохранения potionEffects в предыдущей сессии для " + op.nik);
                        potion = null;
                    } else {
                        potion = ItemUtil.deseripotlize(rs.getString("potion"));
                    }
                }

                final String[] settingsAray = rs.getString("settings").isEmpty() ? null : rs.getString("settings").split(",");
                //p.sendMessage("load bedspawnLoc="+op.world_positions.get("bedspawnLoc"));
                final Location logout = LocUtil.stringToLoc(op.world_positions.get("logoutLoc"), false, true);
                //p.sendMessage("load logout="+logout);
                final Location bedspawnLoc = LocUtil.stringToLoc(op.world_positions.get("bedspawnLoc"), false, false);
                Ostrov.sync(() -> {

                    if (bedspawnLoc != null) {
                        p.setRespawnLocation(bedspawnLoc, true);
                        //p.sendMessage("load setBedSpawnLocation="+bedspawnLoc);
                    }
                    boolean update = false;
                    if (inventory != null) {
                        update = true;
                        p.getInventory().setContents(inventory);
                    }
                    if (armor != null) {
                        update = true;
                        p.getInventory().setArmorContents(armor);
                    }
                    if (ender != null) {
                        update = true;
                        p.getEnderChest().setContents(ender);
                    }
                    if (potion != null) {
                        p.addPotionEffects(potion);
                    }
                    if (update) p.updateInventory();

                    //op.mysqldata_loaded = true;

                    //возможно надо доработать смену logout при релоге пвп
                    if (settingsAray != null) {
                        applyLocalSettings(p, settingsAray);
                    }

                    if (PvPManager.no_damage_on_tp > 0) {
                        op.setNoDamage(PvPManager.no_damage_on_tp, true);
                    }

                    final LocalDataLoadEvent e = new LocalDataLoadEvent(p, op, logout);
                    Bukkit.getPluginManager().callEvent(e); //нормальный вызов с данными
                    if (e.getLogoutLocation() != null) { //плагины могут изменять
                        MoveUtil.safeTP(p, e.getLogoutLocation());
                    }

                }, 1);
            }







    private static String getSettings(final Player p, final Oplayer op) {
        final StringBuilder sb = new StringBuilder();
        sb.append((int) (p.getHealth() * 100)).append(","); //0
        sb.append(p.getFoodLevel()).append(","); //1
        sb.append(p.getLevel()).append(","); //2
        sb.append((int) (p.getExp() * 1000)).append(","); //3
        sb.append(p.getRemainingAir()).append(","); //4
        sb.append(p.getFireTicks()).append(","); //5
        sb.append((int) (p.getWalkSpeed() * 10)).append(","); //6
        sb.append(p.getAllowFlight() ? "1" : "0").append(","); //7
        sb.append((int) (p.getFlySpeed() * 10)).append(","); //8
        sb.append(p.getPlayerWeather() == null ? -1 : p.getPlayerWeather() == WeatherType.CLEAR ? 0 : 1).append(","); //9
        sb.append(p.isPlayerTimeRelative() ? "1" : "0").append(","); //10
        sb.append((int) (p.getPlayerTimeOffset() / 1000)).append(","); //11
        sb.append(op.pvp_allow ? "1" : "0").append(","); //12
        sb.append((int) (p.getAttribute(Attribute.MAX_HEALTH).getBaseValue() * 100)).append(","); //13
        sb.append((int) (p.getHealthScale() * 1000)).append(","); //14
        sb.append(p.getTotalExperience()).append(","); //15
        sb.append((int) (p.getSaturation() * 1000)).append(","); //16
        sb.append(p.getGameMode().name()).append(","); //17
        sb.append(PvPManager.getFlag(PvPManager.PvpFlag.antirelog) && op.pvp_time > 0 ? "1" : "0").append(","); //18

        return sb.toString();
    }


    public static void applyLocalSettings(final Player p, final String[] s) {

        double health = p.getHealth();
        double maxhealth = p.getAttribute(Attribute.MAX_HEALTH).getBaseValue();
        double healthScale = p.getHealthScale();
        boolean fly = false;
        int flyspeed = 1;
        int walkspeed = 2;
        int pweather = -1;
        //boolean rtime = false;
        //int ptime = 0;


        switch (s.length) {
            case 19:
                if (s[18].equals("1")) {
                    p.sendMessage("§4Вы сбежали во время битвы, растеряв инвентарь..");
                }
            case 18:
                try {
                    if (Cfg.set_gm) { //на многих минииграх ставится ГМ из конфига
                        p.setGameMode(Cfg.gm_on_join);
                    } else {
                        p.setGameMode(GameMode.valueOf(s[17])); //если нет - восстановить сохранённый
                    }
                } catch (NullPointerException ex) {
                    Ostrov.log_err("applyLocalSettings " + p.getName() + " setGameMode");
                }
            case 17:
                try {
                    p.setSaturation(Integer.parseInt(s[16]) / 1000f);
                } catch (NumberFormatException ex) {
                    Ostrov.log_err("applyLocalSettings " + p.getName() + " setSaturation");
                }
            case 16:
                try {
                    p.setTotalExperience(Integer.parseInt(s[15]));
                } catch (NumberFormatException ex) {
                    Ostrov.log_err("applyLocalSettings " + p.getName() + " setTotalExperience");
                }
//            p.giveExpLevels() - Gives the player the amount of experience levels specified. Levels can be taken by specifying a negative amount. - any
//            p.getLevel() - Gets the players current experience level. - any
//            p.setLevel() - Sets the players current experience level. - any

//            p.giveExp() - Gives the player the amount of experience specified. - def
//            p.getTotalExperience() - Gets the players total experience points the player has collected over time and is not currently displayed to the client. - any
//            p.setTotalExperience() - Sets the players total experience points the player has collected over time and is not currently displayed to the client. - any
//            calculateTotalExperiencePoints() - This differs from getTotalExperience() as it returns an up-to-date value that reflects level and progress. - def
//            setExperienceLevelAndProgress() - Updates the players level and progress to what would be reached when the experience had been collected. - def

//            p.getExp() - Gets the players current experience points towards the next level. 0 is "no progress" and 1 is "next level". - any
//            p.setExp() - Sets the players current experience points towards the next level. 0 is "no progress" and 1 is "next level". - any
//            p.getExperiencePointsNeededForNextLevel() - Gets the total amount of experience needed to reach the next level from zero. - def

//            p.sendExperienceChange() - Send an experience change. This fakes an experience change packet for a user. - any
            case 15:
                try {
                    healthScale = (Double.parseDouble(s[14]) / 1000);
                } catch (NumberFormatException ex) {
                    Ostrov.log_err("applyLocalSettings " + p.getName() + " healthScale");
                }
            case 14:
                try {
                    maxhealth = Double.parseDouble(s[13]) / 100;
                } catch (NumberFormatException ex) {
                    Ostrov.log_err("applyLocalSettings " + p.getName() + " maxhealth");
                }
            case 13:
                if (!s[12].equals("1")) PvPManager.pvpOff(PM.getOplayer(p));
                //case 12:
                //try { ptime = Integer.parseInt(s[11]); } catch (NumberFormatException ex) { Ostrov.log_err("applyLocalSettings "+p.getName()+" ptime"); }
                //case 11:
                //rtime = s[10].equals("1");
            case 10:
                try {
                    pweather = Integer.parseInt(s[9]);
                } catch (NumberFormatException ex) {
                    Ostrov.log_err("applyLocalSettings " + p.getName() + " pweather");
                }
            case 9:
                try {
                    flyspeed = Integer.parseInt(s[8]);
                } catch (NumberFormatException ex) {
                    Ostrov.log_err("applyLocalSettings " + p.getName() + " flyspeed");
                }
            case 8:
                fly = s[7].equals("1");
            case 7:
                try {
                    walkspeed = Integer.parseInt(s[6]);
                } catch (NumberFormatException ex) {
                    Ostrov.log_err("applyLocalSettings " + p.getName() + " walkspeed");
                }
            case 6:
                try {
                    p.setFireTicks(Integer.parseInt(s[5]));
                } catch (NumberFormatException ex) {
                    Ostrov.log_err("applyLocalSettings " + p.getName() + " setFireTicks");
                }
            case 5:
                try {
                    p.setRemainingAir(Integer.parseInt(s[4]));
                } catch (NumberFormatException ex) {
                    Ostrov.log_err("applyLocalSettings " + p.getName() + " setRemainingAir");
                }
            case 4:
                try {
                    p.setExp(Float.parseFloat(s[3]) / 1000);
                } catch (NumberFormatException ex) {
                    Ostrov.log_err("applyLocalSettings " + p.getName() + " setExp");
                }
            case 3:
                try {
                    p.setLevel(Integer.parseInt(s[2]));
                } catch (NumberFormatException ex) {
                    Ostrov.log_err("applyLocalSettings " + p.getName() + " setLevel");
                }
            case 2:
                try {
                    p.setFoodLevel(Integer.parseInt(s[1]));
                } catch (NumberFormatException ex) {
                    Ostrov.log_err("applyLocalSettings " + p.getName() + " setFoodLevel");
                }
            case 1:
                try {
                    health = Double.parseDouble(s[0]) / 100;
                } catch (NumberFormatException ex) {
                    Ostrov.log_err("applyLocalSettings " + p.getName() + " health");
                }
            default:
                break;
        }

        if (maxhealth != 0) {
            p.getAttribute(Attribute.MAX_HEALTH).setBaseValue(maxhealth);
        }
        if (health > maxhealth) {
            health = maxhealth;
        }
        p.setHealth(health);
        if (healthScale != 0 && Cfg.scale_health) {
            p.setHealthScale(healthScale);
            p.setHealthScaled(true);
        } else {
            p.setHealthScale(maxhealth);
            p.setHealthScaled(false);
        }

        //if (PlayerListener.set_gm) { //на многих минииграх ставится ГМ из конфига
        //p.setGameMode(PlayerListener.gm_on_join);
        // else
        if (p.getGameMode() == GameMode.SPECTATOR) { //SPECTATOR был поставлен при входе и не убрался, т.к. не было сохранения -
            p.setGameMode(GameMode.SURVIVAL); //поставить выживание
        }

        //пермишены наверняка уже будут - загрузка локал начинается через 10 тиков после входа
        if (p.getGameMode() == GameMode.SURVIVAL || p.getGameMode() == GameMode.ADVENTURE) {
            if (Cfg.fly_command && p.hasPermission("ostrov.fly") && fly) {
                p.setAllowFlight(true);
                p.setFallDistance(0);
            } else {
                p.setFlying(false);
                p.setAllowFlight(false);
            }
        }


        if (Cfg.fly_command && Cfg.speed_command && p.hasPermission("ostrov.flyspeed") && flyspeed > 0) {
            p.setFlySpeed((float) flyspeed / 10);
        } else {
            p.setFlySpeed(0.1F);
        }


        if (Cfg.speed_command && (p.hasPermission("ostrov.walkspeed")) && walkspeed > 0) {
            p.setWalkSpeed((float) walkspeed / 10);
        } else {
            p.setWalkSpeed(0.2F);
        }


        if (Cfg.pweather_command && p.hasPermission("ostrov.pweather")) {
            switch (pweather) {
                case 0 -> p.setPlayerWeather(WeatherType.CLEAR);
                case 1 -> p.setPlayerWeather(WeatherType.DOWNFALL);
                default -> p.resetPlayerWeather();
            }
        } else {
            p.resetPlayerWeather();
        }

    }
*/
