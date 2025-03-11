package ru.komiss77.modules.player.profile;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import ru.komiss77.Ostrov;
import ru.komiss77.RemoteDB;
import ru.komiss77.Timer;
import ru.komiss77.enums.*;
import ru.komiss77.hook.WGhook;
import ru.komiss77.modules.games.ArenaInfo;
import ru.komiss77.modules.games.GM;
import ru.komiss77.modules.games.GameInfo;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;
import ru.komiss77.modules.player.Perm;
import ru.komiss77.modules.player.mission.MissionManager;
import ru.komiss77.modules.player.mission.MissionWithdrawViewMenu;
import ru.komiss77.modules.player.profile.serverMenu.LocalMenuOpener;
import ru.komiss77.modules.regions.menu.RegionMenu;
import ru.komiss77.modules.translate.Lang;
import ru.komiss77.objects.Group;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.ItemUtil;
import ru.komiss77.utils.TCUtil;
import ru.komiss77.utils.TimeUtil;
import ru.komiss77.utils.inventory.ClickableItem;
import ru.komiss77.utils.inventory.InputButton;
import ru.komiss77.utils.inventory.SmartInventory;


public class ProfileManager {

    public static String passportPrefix = "§aПаспорт ";

    private static final ItemStack loadError = new ItemBuilder(Material.MUSIC_DISC_11).name("§cОшибка загрузки данных..").build();
    private static final ItemStack air = new ItemStack(Material.AIR);
    private static final ItemStack lime = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
    private static final ItemStack green = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);

    public Oplayer op;
    private BukkitTask loadAnimations;
    public Inventory current;

    public Section section = Section.РЕЖИМЫ;
    public Game game = null;  //для динамической обновы. null значит открыты большие, или арены игры
    protected int gamePage;
    protected int arenaPage;
    protected boolean localSettingsPage;
    protected boolean staffPage;
    public ProfileMode profileMode = ProfileMode.Главное;
    public FriendMode friendMode = FriendMode.Просмотр;
    public LocalMode localdMode = LocalMode.Главное;

    public ProfileManager(final Oplayer op) {
        this.op = op;
    }

    public boolean isProfileInventory(String invTitle) { //для распознавания при проверке ресурс-пака
        if (invTitle.contains(" : ")) {
            invTitle = invTitle.split(" : ")[0];
        }
//Ostrov.log("isProfileInventory="+invTitle);
        switch (invTitle) {
            case "Режимы", "Локальные настройки", "Возможности", "Профиль", "Статистика", "Достижения", "Миссии",
                 "Друзья", "Команда",
                 "Games", "Local Settings", "Possibilities", "Profile", "Statistics", "Achievements", "Missions",
                 "Friends", "Party" -> {
                return true;
            }
        }
        return invTitle.startsWith("Меню сервера") || invTitle.startsWith("Server menu");
    }

    public void open(final Player p, final Section section) {

        this.section = section;
        stopLoadAnimations();

        switch (section) {

            case РЕЖИМЫ -> {
                game = null; //при клике или переходе на режимы если открыты арены - сбросить на игры и переоткрыть
                current = SmartInventory
                        .builder()
                        //.parent(parent)
                        .id(op.nik + section.name())
                        .title(op.eng ? section.item_nameEn : section.item_nameRu)
                        .provider(new GameMenu(false))
                        .size(6, 9)
                        .build()
                        .open(p);
            }

            case МИНИИГРЫ -> {
                game = null; //при клике или переходе на режимы если открыты арены - сбросить на игры и переоткрыть
                current = SmartInventory
                        .builder()
                        //.parent(parent)
                        .id(op.nik + section.name())
                        .title(op.eng ? section.item_nameEn : section.item_nameRu)
                        .provider(new GameMenu(true))
                        .size(6, 9)
                        .build()
                        .open(p);
            }

            case ВОЗМОЖНОСТИ -> {
                if (localSettingsPage) {
                    current = SmartInventory
                            .builder()
                            .id(op.nik + section.name())
                            .title(op.eng ? section.item_nameEn : section.item_nameRu)
                            .provider(new LocalSettings())
                            .size(3, 9)
                            .build()
                            .open(p);
                } else {
                    localSettingsPage = true;
                    LocalMenuOpener.open(p, op);
                }
            }

            case ПРОФИЛЬ -> {
                profileMode = ProfileMode.Главное;
                current = SmartInventory
                        .builder()
                        .id(op.nik + section.name())
                        .title(op.eng ? section.item_nameEn : section.item_nameRu)
                        .provider(new ProfileSection())
                        .size(6, 9)
                        .build()
                        .open(p);
            }

            case СТАТИСТИКА -> current = SmartInventory
                    .builder()
                    .id(op.nik + section.name())
                    .title(op.eng ? section.item_nameEn : section.item_nameRu)
                    .provider(new StatSection())
                    .size(6, 9)
                    .build()
                    .open(p);

            case ДОСТИЖЕНИЯ -> current = SmartInventory
                    .builder()
                    .id(op.nik + section.name())
                    .title(op.eng ? section.item_nameEn : section.item_nameRu)
                    .provider(new AdvSection())
                    .size(6, 9)
                    .build()
                    .open(p);

            case МИССИИ -> MissionManager.openMissionsMenu(op, true);

            case ДРУЗЬЯ -> Friends.openFriendsMain(op);

            case КОМАНДА -> Friends.openPartyMain(op);

            case ГРУППЫ -> openDonate(op);

        }
    }

    public void openLastSection(final Player p) {
        open(p, section);
    }

    public void openLocalMenu(final Player p) {
        LocalMenuOpener.open(p, op);
    }


    public void openLocalSettings(final Player p, final boolean settings) {
        localSettingsPage = settings;
        open(p, Section.ВОЗМОЖНОСТИ);
    }

    public void openDonate(final Oplayer op) {
        section = Section.ГРУППЫ;
        staffPage = false;
        current = SmartInventory
                .builder()
                .id(op.nik + section.name())
                .provider(new Donate())
                .size(6, 9)
                .title(op.eng ? "<gradient:red:indigo>§lSupport the project" : "<gradient:red:indigo>§lПоддержать проект")
                .build()
                .open(op.getPlayer());
    }

    public void showStaff(final Oplayer op) {
        section = Section.ГРУППЫ;
        staffPage = true;
        runLoadAnimations();

        Ostrov.async(() -> {

            final List<ClickableItem> buttons = new ArrayList<>();

            Statement stmt = null;
            ResultSet rs = null;

            try {
                stmt = RemoteDB.getConnection().createStatement();

                //SELECT `id`,`bungeestaff`.`name`, `gr`, `master`, `data`,`sience`,`logout`,`reputation`,`phone`,`email`,`birth`,`land`,`city`, `discord`,`vk`,`youtube` FROM `bungeestaff` LEFT JOIN `userData` ON `bungeestaff`.`name` = `userData`.`name` ORDER BY `id` ASC
                //rs = stmt.executeQuery( "SELECT * FROM "+Table.PEX_BUNGEE_STAFF.table_name+"  ORDER BY `id` ASC " );
                rs = stmt.executeQuery("SELECT " + Table.PEX_BUNGEE_STAFF.table_name + ".`id`," + Table.PEX_BUNGEE_STAFF.table_name + ".`name`, `gr`, `master`, `data`,`sience`,`logout`,`reputation`,`phone`,`email`,`birth`,`land`,`city`, `discord`,`vk`,`youtube`"
                        + " FROM " + Table.PEX_BUNGEE_STAFF.table_name + " LEFT JOIN " + Table.USER.table_name
                        + " ON " + Table.PEX_BUNGEE_STAFF.table_name + ".`name` = " + Table.USER.table_name + ".`name` ORDER BY `id` ASC");
                Group group;
                Material mat;

                while (rs.next()) {

                    group = Perm.getGroup(rs.getString("gr"));
                    mat = group == null ? Material.EMERALD : Material.matchMaterial(group.mat);
                    if (mat == null) {
                        mat = Material.LEATHER_HELMET;
                    }
                    //`family`,`birth`, 
                    buttons.add(ClickableItem.empty(new ItemBuilder(mat)
                            .flags(ItemFlag.HIDE_ATTRIBUTES)
                            .name("§f" + rs.getString("name"))
                            .lore("")
                            .lore(group == null ? rs.getString("parent") : "§e" + group.chat_name)
                            .lore(group == null ? "§cустаревшая" : "")
                            .lore("")
                            //.addLore("§7Назначение:")
                            .lore(rs.getString("master"))
                            .lore(rs.getString("data"))
                            .lore("")
                            .lore("репутация: " + (rs.getString("reputation") == null ? "0" : rs.getString("reputation")))
                            .lore("страна: " + (rs.getString("land") == null ? "0" : rs.getString("land")))
                            .lore("город: " + (rs.getString("city") == null ? "0" : rs.getString("city")))
                            .lore("тел.: " + (rs.getString("phone") == null ? "0" : rs.getString("phone")))
                            .lore("почта: " + (rs.getString("email") == null ? "0" : rs.getString("email")))
                            .lore("ВК: " + (rs.getString("vk") == null ? "0" : rs.getString("vk")))
                            .lore("ДС: " + (rs.getString("discord") == null ? "0" : rs.getString("discord")))
                            .lore("Ютуб: " + (rs.getString("youtube") == null ? "0" : rs.getString("youtube")))
                            .lore("")
                            .lore("§7На сервере с")
                            .lore(rs.getString("sience") == null ? "§7неизвестно" : TimeUtil.dateFromStamp(rs.getInt("sience")))
                            .lore("")
                            .lore("§7Последняя активность:")
                            .lore(rs.getString("logout") == null ? "§7неизвестно" : TimeUtil.dateFromStamp(rs.getInt("logout")))
                            .lore("")
                            .build()
                    ));
                }

                Ostrov.sync(() -> {
                    if (section == Section.ГРУППЫ && staffPage) {
//System.out.println("rawData="+rawData);
                        stopLoadAnimations();
                        current = SmartInventory
                                .builder()
                                .id(op.nik + section.name())
                                .provider(new CI_MultiPage(buttons, Section.ГРУППЫ.glassMat))
                                .size(6, 9)
                                .title(op.eng ? Section.ГРУППЫ.item_nameEn : Section.ГРУППЫ.item_nameRu)
                                .build()
                                .open(op.getPlayer());
                    }// else p.sendMessage("уже другое меню"); }
                }, 0);

            } catch (SQLException e) {

                Ostrov.log_err("§с showStaff - " + e.getMessage());

            } finally {
                try {
                    if (rs != null) {
                        rs.close();
                    }
                    if (stmt != null) {
                        stmt.close();
                    }
                } catch (SQLException e) {
                    Ostrov.log_err("§с showStaff close - " + e.getMessage());
                }
            }

        }, 20);

    }

    // ********** Подменю профиля *************
    public void openWithdrawalRequest(final Player p, final boolean inProfile) {
        if (inProfile) {
            section = Section.ПРОФИЛЬ;
            profileMode = ProfileMode.Вывод;
            runLoadAnimations();
        }

        Ostrov.async(() -> {

            final List<ClickableItem> buttons = new ArrayList<>();

            try (Statement stmt = RemoteDB.getConnection().createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT * FROM `withdraw` WHERE `name`='" + p.getName() + "' ORDER BY `time` DESC")) {


                while (rs.next()) {
                    switch (rs.getString("status")) {
                        case "ожидание" -> buttons.add(ClickableItem.empty(new ItemBuilder(Material.WHITE_CANDLE)
                                .name(TimeUtil.dateFromStamp(rs.getInt("time")))
                                .lore("")
                                .lore("§7сумма : §e" + rs.getInt("summ"))
                                .lore("")
                                .lore("§7Статус:")
                                .lore("§fОжидает обработки")
                                .lore("")
                                .build()
                        ));
                        case "выполнено" -> buttons.add(ClickableItem.empty(new ItemBuilder(Material.LIME_CANDLE)
                                .name(TimeUtil.dateFromStamp(rs.getInt("time")))
                                .lore("")
                                .lore("§7сумма : §e" + rs.getInt("summ"))
                                .lore("")
                                .lore("§7Статус:")
                                .lore("§aвыполнено")
                                .lore(ItemUtil.genLore(null, rs.getString("note"), "§7"))
                                .lore("")
                                .build()
                        ));
                        case "ошибка" -> {
                            final int id = rs.getInt("id");

                          buttons.add(new InputButton(InputButton.InputType.ANVILL, new ItemBuilder(Material.RED_CANDLE)
                              .name(TimeUtil.dateFromStamp(rs.getInt("time")))
                              .lore("")
                              .lore("§7сумма : §e" + rs.getInt("summ"))
                              .lore("")
                              .lore("§7Статус: §cошибка")
                              .lore("")
                              .lore(ItemUtil.genLore(null, rs.getString("note"), "§7"))
                              .lore("")
                              .lore("§7ЛКМ - §bредактировать примечание")
                              .lore("")
                              .build(), "", s -> {
                            p.closeInventory();
                            RemoteDB.executePstAsync(p, "UPDATE `withdraw` SET `status`='ожидание', `note`='" + s + "' WHERE `id`=" + id);
                            p.sendMessage("§fЗаявка на вывод отправлена на повторную обработку");
                          }
                          ));
                            /*buttons.add(ClickableItem.of(new ItemBuilder(Material.RED_CANDLE)
                                            .name(TimeUtil.dateFromStamp(rs.getInt("time")))
                                            .lore("")
                                            .lore("§7сумма : §e" + rs.getInt("summ"))
                                            .lore("")
                                            .lore("§7Статус: §cошибка")
                                            .lore("")
                                            .lore(ItemUtil.genLore(null, rs.getString("note"), "§7"))
                                            .lore("")
                                            .lore("§7ЛКМ - §bповторить обработку")
                                            .lore("")
                                            .build(), e -> {
                                        if (e.getClick() == ClickType.LEFT) {
                                            p.closeInventory();
                                            RemoteDB.executePstAsync(p, "UPDATE `withdraw` SET `status`='ожидание' WHERE `id`=" + id);
                                            p.sendMessage("§fЗаявка на вывод отправлена на повторную обработку");
                                        }
                                    }
                            ));*/
                        }
                    }

                }

                Ostrov.sync(() -> {
                    if (inProfile && section == Section.ПРОФИЛЬ && profileMode == ProfileMode.Вывод) {
//System.out.println("rawData="+rawData);
                        stopLoadAnimations();
                        current = SmartInventory
                                .builder()
                                .id(op.nik + section.name())
                                .provider(new CI_MultiPage(buttons, Material.BLACK_STAINED_GLASS_PANE))
                                .size(6, 9)
                                .title("Профиль : Заявки на вывод")
                                .build()
                                .open(p);
                    } else {
                        SmartInventory
                                .builder()
                                .provider(new MissionWithdrawViewMenu(buttons))
                                .size(5, 9)
                                .title("§l§lЗаявки на вывод")
                                .build()
                                .open(p);
                    }
                }, 0);

            } catch (SQLException e) {

                Ostrov.log_err("§с openWithdrawalRequest - " + e.getMessage());

            }

        }, 20);

    }

    public void openPassport(final Player p) {
        section = Section.ПРОФИЛЬ;
        profileMode = ProfileMode.Паспорт;
        //runLoadAnimations();
        //Ostrov.sync(() -> {
        //if (section == Section.ПРОФИЛЬ && profileMode == ProfileMode.Паспорт) {
        //stopLoadAnimations();
        current = SmartInventory.builder()
                .id(op.nik + section.name())
                .provider(new Passport())
                .size(6, 9)
                .title("Профиль : Паспорт") //не переименовывыть! юзает QuestManager
                .build()
                .open(p);
        // }// else p.sendMessage("уже другое меню"); }
        // }, 30);
    }

    public void openIgnoreList(final Player p) {
        section = Section.ПРОФИЛЬ;
        profileMode = ProfileMode.Игнор;
        runLoadAnimations();
        Ostrov.sync(() -> {
            if (section == Section.ПРОФИЛЬ && profileMode == ProfileMode.Игнор) {
                stopLoadAnimations();
                current = SmartInventory
                        .builder()
                        .id(op.nik + section.name())
                        .provider(new IgnoreList())
                        .size(6, 9)
                        .title("Профиль : Игнор")
                        .build()
                        .open(p);
            }// else p.sendMessage("уже другое меню"); }
        }, 30);
    }

    public void openPerms(final Player p, final int page) {
        section = Section.ПРОФИЛЬ;
        profileMode = ProfileMode.Пермишены;
        runLoadAnimations();

        Ostrov.sync(() -> {
            if (section == Section.ПРОФИЛЬ && profileMode == ProfileMode.Пермишены) {
                stopLoadAnimations();
                current = SmartInventory
                        .builder()
                        .id(op.nik + section.name())
                        .provider(new ShowPermissions())
                        .size(6, 9)
                        .title("Профиль : Права")
                        .build()
                        .open(p);
            }// else p.sendMessage("уже другое меню"); }
        }, 30);
    }

    public void openJournal(final Player p, final int page) {
        section = Section.ПРОФИЛЬ;
        profileMode = ProfileMode.Журнал;
        runLoadAnimations();

        Ostrov.async(() -> {

            final List<ClickableItem> buttons = new ArrayList<>();
            boolean hasNext = false;

            Statement stmt = null;
            ResultSet rs = null;

            try {
                stmt = RemoteDB.getConnection().createStatement();

                rs = stmt.executeQuery("SELECT * FROM " + Table.HISTORY.table_name + " WHERE `target` = '" + p.getName() + "'  ORDER BY `data` DESC LIMIT " + page * 36 + ",37");

                int count = 0;
                HistoryType type;

                while (rs.next()) {
                    if (count == 36) {
                        hasNext = true;
                        break;
                    } else {
                        type = HistoryType.by_action(rs.getString("action"));
                        buttons.add(ClickableItem.empty(new ItemBuilder(Material.matchMaterial(type.displayMat))
                                .name(type.for_chat)
                                .lore("§7источник : §b" + rs.getString("sender"))
                                .lore("IP : " + rs.getString("target_ip"))
                                .lore("")
                                .lore(ItemUtil.genLore(null, rs.getString("report"), "§7"))
                                .lore("")
                                .lore(TimeUtil.dateFromStamp(rs.getInt("data")))
                                .lore("")
                                .build()
                        ));
                        //logs.add(new Log(type.for_chat, rs.getString("sender"), rs.getString("target_ip"), rs.getString("report"), rs.getInt("data")));
                    }
                    count++;
                }

                final boolean next = hasNext;

                Ostrov.sync(() -> {
                    if (section == Section.ПРОФИЛЬ && profileMode == ProfileMode.Журнал) {
//System.out.println("rawData="+rawData);
                        stopLoadAnimations();
                        current = SmartInventory
                                .builder()
                                .id(op.nik + section.name())
                                .provider(new ShowJournal(buttons, page, next))
                                .size(6, 9)
                                .title("Профиль : Журнал")
                                .build()
                                .open(p);
                    }// else p.sendMessage("уже другое меню"); }
                }, 0);

            } catch (SQLException e) {

                Ostrov.log_err("§сopenJournal - " + e.getMessage());

            } finally {
                try {
                    if (rs != null) {
                        rs.close();
                    }
                    if (stmt != null) {
                        stmt.close();
                    }
                } catch (SQLException e) {
                    Ostrov.log_err("§сopenJournal close - " + e.getMessage());
                }
            }

        }, 20);

    }

    public void openAkkauntsDB(final Player p) {
        section = Section.ПРОФИЛЬ;
        profileMode = ProfileMode.АккаунтыБД;
        runLoadAnimations();

        Ostrov.async(() -> {

            final List<ClickableItem> buttons = new ArrayList<>();

            Statement stmt = null;
            ResultSet rs = null;

            try {
                stmt = RemoteDB.getConnection().createStatement();

                rs = stmt.executeQuery("SELECT `name`,`ipprotect`,`sience`,`logout`,`phone`,`email` FROM " + Table.USER.table_name + " WHERE `ip` = '" + op.getDataString(Data.IP) + "' ");

                while (rs.next()) {
                    buttons.add(ClickableItem.empty(new ItemBuilder(rs.getString("name").equalsIgnoreCase(p.getName()) ? Material.WRITTEN_BOOK : Material.BOOK)
                            .name("§e" + rs.getString("name"))
                            .lore("")
                            .lore("§7Защита по IP : " + (rs.getBoolean("ipprotect") ? "§cДа" : "§2Нет"))
                            .lore("")
                            .lore("§7Дата регистрации:")
                            .lore(TimeUtil.dateFromStamp(rs.getInt("sience")))
                            .lore("")
                            .lore("§7Последняя активность:")
                            .lore(TimeUtil.dateFromStamp(rs.getInt("logout")))
                            .lore("")
                            .lore("§7До автоудаления примерно:")
                            .lore(TimeUtil.secondToTime(8035200 - (Timer.getTime() - rs.getInt("logout"))))
                            .lore("")
                            .lore("тел.: " + rs.getString("phone"))
                            .lore("почта.: " + rs.getString("email"))
                            .lore("")
                            .build()
                    ));
                }

                if (buttons.size() >= 5) {
                    buttons.add(ClickableItem.empty(new ItemBuilder(Material.REDSTONE)
                            .name("§eИнформация о лимите")
                            .lore("")
                            .lore("§7Вы не можете добавить")
                            .lore("§7новые аккаунты")
                            .lore("")
                            .build()
                    ));
                } else {
                    buttons.add(ClickableItem.empty(new ItemBuilder(Material.EMERALD)
                            .name("§eИнформация о лимите")
                            .lore("")
                            .lore("§7Можно создать")
                            .lore("§7аккаунтов : " + (5 - buttons.size()))
                            .lore("")
                            .build()
                    ));
                }

                Ostrov.sync(() -> {
                    stopLoadAnimations();
                    if (section == Section.ПРОФИЛЬ && profileMode == ProfileMode.АккаунтыБД) {
//System.out.println("rawData="+rawData);
                        //stopLoadAnimations();
                        current = SmartInventory
                                .builder()
                                .id(op.nik + section.name())
                                .provider(new CI_OnePage(buttons, Section.ПРОФИЛЬ.glassMat))
                                .size(6, 9)
                                .title("Профиль : Аккаунты")
                                .build()
                                .open(p);
                    }// else p.sendMessage("уже другое меню"); }
                }, 0);

            } catch (SQLException e) {

                Ostrov.log_err("§с openAkkaunts - " + e.getMessage());

            } finally {
                try {
                    if (rs != null) {
                        rs.close();
                    }
                    if (stmt != null) {
                        stmt.close();
                    }
                } catch (SQLException e) {
                    Ostrov.log_err("§с openAkkaunts close - " + e.getMessage());
                }
            }

        }, 20);

    }

    public void openGroupsAndPermsDB(final Player p, final int page) {
        section = Section.ПРОФИЛЬ;
        profileMode = ProfileMode.ГруппыПраваБД;
        runLoadAnimations();

        Ostrov.async(() -> {

            final List<ClickableItem> buttons = new ArrayList<>();

            Statement stmt = null;
            ResultSet rs = null;
            Group group;
            Material mat;

            try {
                stmt = RemoteDB.getConnection().createStatement();

                rs = stmt.executeQuery("SELECT * FROM " + Table.PEX_BUNGEE_STAFF.table_name + " WHERE `name` = '" + op.nik + "' ");
                while (rs.next()) {
                    group = Perm.getGroup(rs.getString("gr"));
                    mat = group == null ? Material.EMERALD : Material.matchMaterial(group.mat);
                    if (mat == null) {
                        mat = Material.DIAMOND;
                    }

                    buttons.add(ClickableItem.empty(new ItemBuilder(mat)
                            .name(group == null ? rs.getString("parent") : "§e" + group.chat_name)
                            .lore(group == null ? "§cустаревшая" : "")
                            .lore("")
                            .lore("§7Назначение:")
                            .lore(rs.getString("data"))
                            .lore(rs.getString("master").isEmpty() ? "" : "§7от " + rs.getString("master"))
                            .lore("")
                            .build()
                    ));
                }
                rs.close();

                rs = stmt.executeQuery("SELECT * FROM " + Table.PEX_USER_GROUPS.table_name + " WHERE `name` = '" + op.nik + "' ");
                while (rs.next()) {
                    group = Perm.getGroup(rs.getString("parent"));
                    mat = group == null ? Material.EMERALD : Material.matchMaterial(group.mat);
                    if (mat == null) {
                        mat = Material.DIAMOND;
                    }

                    buttons.add(ClickableItem.empty(new ItemBuilder(mat)
                            .name(group == null ? rs.getString("parent") : "§e" + group.chat_name)
                            .lore(group == null ? "§cустаревшая" : "")
                            .lore("")
                            .lore("§7Добавлено:")
                            .lore(rs.getString("added"))
                            .lore("")
                            .lore("§7Действует до:")
                            .lore(rs.getBoolean("forever") ? "навсегда" : TimeUtil.dateFromStamp(rs.getInt("valid_to")))
                            .lore("§7Примечания:")
                            .lore(rs.getString("note"))
                            .build()
                    ));
                }
                rs.close();

                rs = stmt.executeQuery("SELECT * FROM " + Table.PEX_USER_PERMS.table_name + " WHERE `name` = '" + op.nik + "' ");
                while (rs.next()) {
                    buttons.add(ClickableItem.empty(new ItemBuilder(Material.LIME_DYE)
                            .name("§7пермишен")
                            .lore("§f" + rs.getString("perm"))
                            .lore("")
                            .lore("§7Добавлено:")
                            .lore(rs.getString("added"))
                            .lore("")
                            .lore("§7Действует до:")
                            .lore(rs.getBoolean("forever") ? "навсегда" : TimeUtil.dateFromStamp(rs.getInt("valid_to")))
                            .lore("§7Примечания:")
                            .lore(rs.getString("note"))
                            .build()
                    ));
                }

                Ostrov.sync(() -> {
                    stopLoadAnimations();
                    if (section == Section.ПРОФИЛЬ && profileMode == ProfileMode.ГруппыПраваБД) {
//System.out.println("rawData="+rawData);
                        stopLoadAnimations();
                        current = SmartInventory
                                .builder()
                                .id(op.nik + section.name())
                                .provider(new GroupsAndPermsDB(buttons))
                                .size(6, 9)
                                .title(op.eng ? "Profile: Groups and perms" : "Профиль : Группы и права")
                                .build()
                                .open(p);
                    }// else p.sendMessage("уже другое меню"); }
                }, 0);

            } catch (SQLException e) {

                Ostrov.log_err("§с openGroupsAndPermsDB - " + e.getMessage());

            } finally {
                try {
                    if (rs != null) {
                        rs.close();
                    }
                    if (stmt != null) {
                        stmt.close();
                    }
                } catch (SQLException e) {
                    Ostrov.log_err("§с openGroupsAndPermsDB close - " + e.getMessage());
                }
            }

        }, 20);
    }
// ***********************************    

    public void openArenaMenu(final Player p, final Game game) { //при клике по иконке игры в GameSection
        if (op.getStat(Stat.LEVEL) < game.level) {
            p.sendMessage("§cБудет доступны с уровня §e" + game.level);
            PM.soundDeny(p);
            return;
        }
        if (op.reputationCalc < game.reputation) {
            p.sendMessage("§cДоступны при репутации §a+" + game.reputation);
            PM.soundDeny(p);
            return;
        }
        section = Section.РЕЖИМЫ;
        this.game = game;
        arenaPage = 0;
        current = SmartInventory
                .builder()
                .id(op.nik + section.name())
                .provider(new ArenaSection())
                .size(6, 9)
                .title(Lang.t(p, "Арены ") + Lang.t(p, game.displayName))
                .build()
                .open(p);
    }

    // ******** локальные субМеню **************
    public void openTPAsection(final Player p) {
        section = Section.ВОЗМОЖНОСТИ;
        localdMode = LocalMode.TPA;
        current = SmartInventory
                .builder()
                .id(op.nik + op.menu.section.name())
                .provider(new TpaMenu())
                .size(6, 9)
                .title(op.eng ? "TP : Request" : "Телепорт : Запрос")
                .build()
                .open(op.getPlayer());
    }

    public void findRegions(final Player p) {
        section = Section.ВОЗМОЖНОСТИ;
        localdMode = LocalMode.Регионы;
        runLoadAnimations();

        final List<ClickableItem> buttons = new ArrayList<>();

        //ItemUtil.Set_lore(bed, "§fВ этом привате вы пользователь!",   "§6Название: §b"+rg.getId(),   "§6Координаты: §7"+h_loc.getWorld().getName()+", "+h_loc.getBlockX()+" x "+h_loc.getBlockY()+" x "+h_loc.getBlockZ(), "§aЛевый клик - подробно" );
        // ItemUtil.Set_lore(bed, "§6Координаты: §7"+h_loc.getWorld().getName()+",", "§7  "+h_loc.getBlockX()+" x "+h_loc.getBlockY()+" x "+h_loc.getBlockZ(), (allow_rg_tp)?"§aЛевый клик - §2ТП В ЭТОТ ПРИВАТ":"§aЛевый клик - подробно", "§6Правый клик - §4УДАЛИТЬ" );
        final Map<ProtectedRegion, String> regions = WGhook.findPlayerRegions(p, null, true, true);
        final LocalPlayer lp = WorldGuardPlugin.inst().wrapPlayer(p);

        for (final ProtectedRegion rg : regions.keySet()) {
            //buttons.add( ClickableItem.empty(new ItemBuilder( rg.isOwner(p.getName()) ? Material.WARPED_FENCE : Material.CHAINMAIL_BOOTS )
            buttons.add(ClickableItem.empty(new ItemBuilder(rg.isOwner(lp) ? Material.WARPED_FENCE : Material.CHAINMAIL_BOOTS)
                    .name("§e" + rg.getId())
                    .lore("")
                    .lore(rg.isOwner(lp) ? "§7Вы - §6Владелец" : "§7Вы - §3Пользователь")
                    .lore("")
                    .lore("§6Координаты:")
                    .lore("§7" + regions.get(rg) + ", " + WGhook.getRegionLocationInfo(rg))
                    .lore("")
                    .build()
            ));
        }

        Ostrov.sync(() -> {
            if (section == Section.ВОЗМОЖНОСТИ && localdMode == LocalMode.Регионы) {
                stopLoadAnimations();
                current = SmartInventory
                        .builder()
                        .id(op.nik + op.menu.section.name())
                        .provider(new CI_OnePage(buttons, Section.ВОЗМОЖНОСТИ.glassMat))
                        .size(6, 9)
                        .title(op.eng ? "Yours regions" : "Регионы")
                        .build()
                        .open(op.getPlayer());
            }// else p.sendMessage("уже другое меню"); }
        }, 30);

    }

    public void openHomes(final Player p) {
        section = Section.ВОЗМОЖНОСТИ;
        localdMode = LocalMode.Дома;

        current = SmartInventory
            .builder()
            .id(op.nik + op.menu.section.name())
            .provider(new HomeMenu(op))
            .size(6, 9)
            .title(op.eng ? "Yours homes" : "Точки дома")
            .build()
            .open(op.getPlayer());

    }

    public void openRegions(final Player p) {
        section = Section.ВОЗМОЖНОСТИ;
        localdMode = LocalMode.Регионы;

        current = SmartInventory
            .builder()
            .id(op.nik + op.menu.section.name())
            .provider(new RegionMenu(op))
            .size(6, 9)
            .title(op.eng ? "Yours regions" : "Ваши регионы")
            .build()
            .open(op.getPlayer());

    }

    public void tick(final Player p) {
//if (1==1) return;
//System.out.println("tick hasContent?"+im.hasContent(p));
        // if (current!=null) { //if (PM.im.hasContent(p)) {
        if (current == null) {
            return; //нет открытого раздела - ничего не делаем
        }        //нет открытого раздела - ничего не делаем

        //подставить игровое время на иконке профиля, если меньше недели
        if (op.getStat(Stat.PLAY_TIME) < 604800) {
            setLine(p, Section.ПРОФИЛЬ.slot, 1, Lang.t(p, Stat.PLAY_TIME.desc) + TimeUtil.secondToTime(op.getStat(Stat.PLAY_TIME)));
        }
        //подставить наиграно за сегодня
        setLine(p, Section.ПРОФИЛЬ.slot, 2, (op.eng ? "§fPlayTime today : §e" : "§fНаиграно за сегодня : §e") + TimeUtil.secondToTime(op.getDailyStat(Stat.PLAY_TIME)));
        //поставить время до сброса дневной статы на иконке статы
        setLine(p, Section.СТАТИСТИКА.slot, 3, "§3" + TimeUtil.secondToTime(Timer.leftBeforeResetDayly()));

        switch (section) {

            case РЕЖИМЫ -> {
                if (game == null) {
                    GameInfo gi;
                    for (Game g : Game.values()) {
                        if (g.menuPage != gamePage || current.getContents().length <= g.menuSlot) {
                            continue;
                        }
                        gi = GM.getGameInfo(g);
                        if (gi != null && g.menuSlot > 0) {
                            current.setItem(g.menuSlot, gi.getIcon(op));
                        } //обновляем только активные
                    }

                } else {
                    GameInfo gi = GM.getGameInfo(game);
                    if (gi == null || arenaPage * 36 > gi.count()) {
                        return;
                    }
                    ArenaInfo ai;
                    for (int slot = arenaPage * 36; slot < gi.count(); slot++) {
                        ai = gi.getArena(slot);
                        if (current.getContents().length <= slot) {
                            break;
                        }
                        if (ai != null) {
                            current.setItem(slot, ai.getIcon(op));
                        }
                    }
                }
                p.updateInventory();
            }

            case ПРОФИЛЬ -> {
                if (profileMode == ProfileMode.Главное) {
                    //на иконке с часиками
                    if (op.getStat(Stat.PLAY_TIME) < 604800) {
                        setLine(p, 10, 4, Lang.t(p, Stat.PLAY_TIME.desc) + TimeUtil.secondToTime(op.getStat(Stat.PLAY_TIME)));
                    }
                    setLine(p, 10, 5, (op.eng ? "§fPlayTime today : §e" : "§fНаиграно за сегодня : §e") + TimeUtil.secondToTime(op.getDailyStat(Stat.PLAY_TIME)));
                }
            }

        }

    }

    private void setLine(final Player p, final int slot, final int line, final String value) {
        if (current == null || current.getContents().length <= slot) {
            return;
        }

        //im.getContents(p).get().getInventory().setItem(slot, ItemUtil.loreLine(im.getContents(p).get().getInventory().getItem(slot), line, value));  //set(Section.СТАТИСТИКА.slot, im.getContents(p).get().g);
        try {
            ItemStack is = current.getItem(slot);
            if (is != null && is.hasItemMeta()) {
                ItemMeta im = is.getItemMeta();
                if (im.hasLore()) {
                    List<Component> lore = is.getItemMeta().lore();
                    lore.set(line, TCUtil.form(value));
                    im.lore(lore);
                    is.setItemMeta(im);
                    current.setItem(slot, is);
                }
            }
            //current.setItem(slot, ItemUtil.loreLine(im.getContents(p).get().getInventory().getItem(slot), line, value));  //set(Section.СТАТИСТИКА.slot, im.getContents(p).get().g);
        } catch (NoSuchElementException | NullPointerException ex) {
            Ostrov.log_warn("ProfileManager setLine : " + ex.getMessage());
            current = null;
        }
    }

    private void clearTop() {
        if (current != null) {
            for (int slot = 0; slot <= (current.getContents().length < 35 ? current.getContents().length : 35); slot++) {
                if (current.getContents()[slot] != null) {
                    current.setItem(slot, null);
                }
            }
        }
    }

    public void runLoadAnimations() {
        clearTop();
        if (current != null) {
            for (int slot = 36; slot <= 44; slot++) {
                current.setItem(slot, air);
            }
        }
        if (loadAnimations != null) {
            loadAnimations.cancel();
        }
        loadAnimations = new BukkitRunnable() {

            int count = 0;
            int slot = 36;
            boolean add = true;

            @Override
            public void run() {

                if (current == null) {
                    this.cancel();
                    return;
                }

                if (count == 30) {
                    this.cancel();
                    current.setItem(13, loadError);
                    return;
                }

                current.setItem(slot, lime);
                if (add) {
                    if (slot >= 37) {
                        current.setItem(slot - 1, green);
                    }
                    if (slot >= 38) {
                        current.setItem(slot - 2, air);
                    }
                    slot++;
                    if (slot == 44) {
                        add = false;
                    }
                } else {
                    if (slot <= 43) {
                        current.setItem(slot + 1, green);
                    }
                    if (slot <= 42) {
                        current.setItem(slot + 2, air);
                    }
                    slot--;
                    if (slot == 36) {
                        add = true;
                    }
                }

                count++;
//System.out.println("count="+count);
            }
        }.runTaskTimer(Ostrov.instance, 1, 2);

    }

    public void stopLoadAnimations() {
        if (loadAnimations != null) {
            loadAnimations.cancel();
            loadAnimations = null;
        }
    }

    public enum FriendMode {
        Просмотр, Поиск, Настройки, Письма
    }

    public enum ProfileMode {
        Главное, Журнал, Вывод, Пермишены, Паспорт, Игнор, Репорты, АккаунтыБД, ГруппыПраваБД
    }

    public enum LocalMode {
        Главное, TPA, Регионы, Дома
    }

}
