package ru.komiss77.modules.player.profile;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import com.google.common.collect.ImmutableList;
import net.kyori.adventure.text.Component;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import ru.komiss77.enums.Data;
import ru.komiss77.enums.Stat;
import ru.komiss77.modules.items.ItemBuilder;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.translate.Lang;
import ru.komiss77.utils.StringUtil;
import ru.komiss77.utils.TimeUtil;
import ru.komiss77.utils.inventory.ClickableItem;

//http://textures.minecraft.net/texture/be3db27cbd1789310409081ad8c42d690b08961b55cadd45b42d46bca28b8
//be3db27cbd1789310409081ad8c42d690b08961b55cadd45b42d46bca28b8 телик
//f7c7df52b5e50badb61fed7212d979e63fe94f1bde02b2968c6b156a770126c аптечка
//8a99342e2c73a9f3822628e796488234f258446f5a2d4d59dde4aa87db98 да
//16c60da414bf037159c8be8d09a8ecb919bf89a1a21501b5b2ea75963918b7b нет
//f2599bd986659b8ce2c4988525c94e19ddd39fad08a38284a197f1b70675acc < c2f910c47da042e4aa28af6cc81cf48ac6caf37dab35f88db993accb9dfe516 > кварц+зел стрелка
//5f133e91919db0acefdc272d67fd87b4be88dc44a958958824474e21e06d53e6 < e3fc52264d8ad9e654f415bef01a23947edbccccf649373289bea4d149541f70 > кварц+чёрн стрелка
//eed78822576317b048eea92227cd85f7afcc44148dcb832733baccb8eb56fa1 715445da16fab67fcd827f71bae9c1d2f90c73eb2c1bd1ef8d8396cd8e8 <> блок дуба
//https://minecraft-heads.com/custom-heads/miscellaneous/39696-star звезда 1c8e0cfebc7f9c7e16fbaaae025d1b1d19d5ee633666bcf25fa0b40d5bd21bcd
public enum Section {

    МИНИИГРЫ(
        0,//18,
        "<gradient:aqua:apple>§lВыбор Игры",
        "<gradient:aqua:apple>§lGame Selection",
        "98daa1e3ed94ff3e33e1d4c6e43f024c47d78a57ba4d38e75e7c9264106",
        ItemType.LIGHT_BLUE_STAINED_GLASS_PANE
    ),

    РЕЖИМЫ(
        0,//18,
        "<gradient:aqua:apple>§lВыбор Игры",
        "<gradient:aqua:apple>§lGame Selection",
        "98daa1e3ed94ff3e33e1d4c6e43f024c47d78a57ba4d38e75e7c9264106",
        ItemType.LIGHT_BLUE_STAINED_GLASS_PANE
    ),

    ВОЗМОЖНОСТИ(
        1,//19,
        "<gradient:apple:dark_aqua>§lВозможности",
        "<gradient:apple:dark_aqua>§lAbilities",
        "be3db27cbd1789310409081ad8c42d690b08961b55cadd45b42d46bca28b8",
        ItemType.LIME_STAINED_GLASS_PANE
    ),

    ПРОФИЛЬ(
        2,//20,
        "<gradient:dark_aqua:light_purple>§lПрофиль",
        "<gradient:dark_aqua:light_purple>§lProfile",
        "2433b16d98e0d9d335027f23332e208b7c3fff0d7984792ea48c93ca5cbcf1e1",
        ItemType.GRAY_STAINED_GLASS_PANE
    ),

    /*СТАТИСТИКА(
        21,
        "<gradient:light_purple:blue>§lСтатистика",
        "<gradient:light_purple:blue>§lStatistics",
        "5b4ddb8abed660825b68b922e22a9558c2f208938bd438eaeaccdc3941",
        ItemType.PURPLE_STAINED_GLASS_PANE
    ),*/

    ДОСТИЖЕНИЯ(
        4,//22,
        "<gradient:blue:sky>§lДостижения",
        "<gradient:blue:sky>§lAchievements",
        "cf7cdeefc6d37fecab676c584bf620832aaac85375e9fcbff27372492d69f",
        ItemType.BROWN_STAINED_GLASS_PANE
    ),

    МИССИИ(
        5,//23,
        "<gradient:sky:gold>§lМиссии",
        "<gradient:sky:gold>§lMissions",
        "bf6464a5ba11e1e59f0948a3d95846654253bf2822c6b1c1b3a4a3fd31ba4f",
        ItemType.ORANGE_STAINED_GLASS_PANE
    ),

    ДРУЗЬЯ(
        6,//24,
        "§a§lД§d§lр§c§lу§e§lз§9§lь§b§lя",//"§a§lДрузья",
        "§a§lF§d§lr§c§li§e§le§9§ln§b§ld§3§ls",//"§a§lДрузья",
        "f3ebdbad610315ce554db4f56cb5ede6ac7ca6aa11cee02e85f94c52131d69",
        ItemType.LIME_STAINED_GLASS_PANE
    ),

    КОМАНДА(
        7,//25,
        "<gradient:gold:red>§lКоманда",
        "<gradient:gold:red>§lParty",
        "359d1bbffad5422197b573d501465392feef6dc5d426dcd763efed7893d39d",
        ItemType.RED_STAINED_GLASS_PANE
    ),

    ГРУППЫ(
        8,//26,
        "<gradient:red:indigo>§lПривилегии",
        "<gradient:red:indigo>§lDonations",
        "1c8e0cfebc7f9c7e16fbaaae025d1b1d19d5ee633666bcf25fa0b40d5bd21bcd",
        ItemType.YELLOW_STAINED_GLASS_PANE
    ),
    ;

    final public int column;
    final public String item_nameRu;
    final public String item_nameEn;
    final public String texture;
    final public ItemType glassMat;


    Section(int column, String item_nameRu, String item_nameEn, String texture, ItemType glassMat) {
        this.column = column;
        this.item_nameRu = item_nameRu;
        this.item_nameEn = item_nameEn;
        this.texture = texture;
        this.glassMat = glassMat;
    }


    public static boolean isProfileIcon(final int slot) {
        for (Section s_ : Section.values()) {
            //хз как это переделать под разные размеры, пока так
            if (18 + s_.column == slot) return true;
        }
        return false;
    }

    public static Section profileBySlot(final int slot) {
        for (Section s_ : Section.values()) {
            //хз как это переделать под разные размеры, пока так
            if (18 + s_.column == slot) return s_;
        }
        return null;
    }


    public static ItemStack getItem(final Section section, final Oplayer op) {
        return new ItemBuilder(ItemType.PLAYER_HEAD)
            .name(op.eng ? section.item_nameEn : section.item_nameRu)
            .headTexture(section.texture)
            .build();
    }

    public static ClickableItem getMenuItem(final Section section, final Oplayer op) {

        final List<Component> lore;
        final Consumer<InventoryClickEvent> consumer;

        switch (section) {

            case РЕЖИМЫ -> {
                if (op.eng) {
                    lore = Arrays.asList(
                        Component.empty(),
                        Component.text("§7Click - §fChoose a gamemode"),
                        Component.text("§for a specific arena there")
                    );
                } else {
                    lore = Arrays.asList(
                        Component.empty(),
                        Component.text("§7Клик - §fВыбор режима"),
                        Component.text("§fили конкретной арены")
                    );
                }
                consumer = e -> {
                    if (op.menu.section != section || op.menu.game != null) op.menu.open(op.getPlayer(), section);
                };
            }


            case ВОЗМОЖНОСТИ -> {
                if (op.eng) {
                    lore = Arrays.asList(
                        Component.empty(),
                        Component.text("§7LMB - §fLocal menu"),
                        Component.text("§7RMB - §fLocal settings")
                    );
                } else {
                    lore = Arrays.asList(
                        Component.empty(),
                        Component.text("§7ЛКМ - §fЛокальные меню"),
                        Component.text("§7ПКМ - §fЛокальные настройки")
                    );
                }
                consumer = e -> {
                    op.menu.openLocalSettings(op.getPlayer(), e.isRightClick());
                };
            }

            case ПРОФИЛЬ -> {
                if (op.eng) {
                    lore = Arrays.asList(
                        Component.empty(),//0
                        Component.text(Lang.t(Stat.LEVEL.desc, Lang.EN) + op.getStat(Stat.LEVEL) + "  " + StringUtil.getPercentBar(op.getStat(Stat.LEVEL) * 25, op.getStat(Stat.EXP), true)),
                        Component.empty(),
                        //1 игровое время обновление каждую секунду в ProfileManager
                        Component.text(Lang.t(Stat.PLAY_TIME.desc, Lang.EN) + TimeUtil.secondToTime(op.getStat(Stat.PLAY_TIME))),
                        //2 наиграно за сегодня обновление каждую секунду в ProfileManager
                        Component.text("§6Play time today: §e" + TimeUtil.secondToTime(op.getDailyStat(Stat.PLAY_TIME))),
                        Component.empty(),
                        Component.text("§7LMB - §fShow your profile"),
                        Component.text(op.isGuest ? "§8Passport Unavailable" : "§7RMB - §3Islander Passport")
                    );
                } else {
                    lore = Arrays.asList(
                        Component.empty(),
                        Component.text(Stat.LEVEL.desc + op.getStat(Stat.LEVEL) + " " + StringUtil.getPercentBar(op.getStat(Stat.LEVEL) * 25, op.getStat(Stat.EXP), true)),
                        Component.empty(),
                        //1 игровое время обновление каждую секунду в ProfileManager
                        Component.text(Stat.PLAY_TIME.desc + TimeUtil.secondToTime(op.getStat(Stat.PLAY_TIME))),
                        //2 наиграно за сегодня обновление каждую секунду в ProfileManager
                        Component.text("§6Сегодня Наиграно: §a" + TimeUtil.secondToTime(op.getDailyStat(Stat.PLAY_TIME))),
                        Component.empty(),
                        Component.text("§7ЛКМ - §fРазвернуть профиль"),
                        Component.text(op.isGuest ? "§8Паспорт Недоступен" : "§7ПКМ - §3Паспорт Островитянина")
                    );
                }
                consumer = e -> {
                    switch (e.getClick()) {
                        case LEFT -> op.menu.open(op.getPlayer(), section);
                        case RIGHT -> op.getPlayer().performCommand("passport");
                    }
                };
            }

            /*case СТАТИСТИКА -> {
                if (op.eng) {
                    lore = Arrays.asList(
                        Component.empty(),//0
                        Component.text("§7Daily stat reset at midnight."),
                        Component.text("§6Before resetting daily stat:"),
                        Component.empty(),//3 =до сброса дневной статы= обновление каждую секунду в ProfileManager
                        Component.empty(),
                        Component.text(op.menu.section == section ? "" : "§7LMB - §fshow all stat"),
                        Component.empty(),
                        Component.text("§fYour Karma: " + op.getKarmaDisplay()),
                        Component.empty(),
                        Component.text("§7Karma worse when §cdefeat§7,"),
                        Component.text("§7and improves with §awin§7."),
                        Component.text("§7Player with good karma in team"),
                        Component.text("§7can be very useful!"),
                        Component.empty()
                    );
                } else {
                    lore = Arrays.asList(
                        Component.empty(),//0
                        Component.text("§7Дневня статистика обнуляется в полночь."),
                        Component.text("§6До сброса дневной статистики:"),
                        Component.empty(),//3 =до сброса дневной статы= обновление каждую секунду в ProfileManager
                        Component.empty(),
                        Component.text(op.menu.section == section ? "" : "§7ЛКМ - §fразвернуть статистику"),
                        Component.empty(),
                        Component.text("§fВаша Карма: " + op.getKarmaDisplay()),
                        Component.empty(),
                        Component.text("§7Карма ухудшается при §cпроигрышах§7,"),
                        Component.text("§7и улучшается при §aвыиграшах§7."),
                        Component.text("§7Игрок с хорошей кармой в команде"),
                        Component.text("§7может быть весьма полезен!"),
                        Component.empty()
                    );
                }
                consumer = e -> {
                    if (op.menu.section != section) op.menu.open(op.getPlayer(), section);
                };
            }*/

            case ДОСТИЖЕНИЯ -> {
                if (op.eng) {
                    lore = Arrays.asList(
                        Component.empty(),
                        Component.text("§7Your achievements in various gamemodes."),
                        Component.text("§7Completing them grants you experience."),
                        Component.text(op.menu.section == section ? "" : "§7Click - §fShow achievements")
                    );
                } else {
                    lore = Arrays.asList(
                        Component.empty(),
                        Component.text("§7Твои достижения в различных режимах."),
                        Component.text("§7За их выполнение тебе дается опыт."),
                        Component.text(op.menu.section == section ? "" : "§7Клик - §fРазвернуть достижения")
                    );
                }
                consumer = e -> {
                    if (op.menu.section != section) op.menu.open(op.getPlayer(), section);
                };
            }

            case МИССИИ -> {
                if (op.isGuest) {
                    if (op.eng) {
                        lore = Arrays.asList(
                            Component.empty(),//0
                            Component.text("§8Not available in guest mode")
                        );
                    } else {
                        lore = Arrays.asList(
                            Component.empty(),//0
                            Component.text("§8Недоступно в гостевом режиме")
                        );
                    }
                    consumer = null;
                } else {
                    if (op.eng) {
                        lore = Arrays.asList(
                            Component.empty(),
                            Component.text("§7Complete §3Missions §7to earn §eRil§7!"),
                            Component.text(op.menu.section == section ? "" : "§7Click - §fAvailable missions")
                        );
                    } else {
                        lore = Arrays.asList(
                            Component.empty(),
                            Component.text("§7Выполняй §3Миссии §7и получай §eРил§7!"),
                            Component.text(op.menu.section == section ? "" : "§7Клик - §fДоступные миссии")
                        );
                    }
                    consumer = e -> op.menu.open(op.getPlayer(), section);
                }
               /* if (op.isGuest) {
                    return ClickableItem.empty(new ItemBuilder(Material.PLAYER_HEAD)
                            .name(op.eng ? Lang.t(section.item_name, EnumLang.EN_US) : section.item_name)
                            .setCustomHeadTexture(section.texture)
                            .addLore("")
                            .addLore("§8Миссии недоступны")
                            .addLore("§8в гостевом режиме")
                            .addLore("")
                            .build()
                    );
                } else {
                    return ClickableItem.of(new ItemBuilder(Material.PLAYER_HEAD)
                            .name(op.eng ? Lang.t(section.item_name, EnumLang.EN_US) : section.item_name)
                            .setCustomHeadTexture(section.texture)
                            .addLore("")
                            .addLore("§7Здесь вы увидите миссии,")
                            .addLore("§7в корорых участвуете")
                            .addLore("§7и прогресс по ним.")
                            .addLore("")
                            .addLore("§7Чтобы начать новую,")
                            .addLore("§7отменить или завершить миссию,")
                            .addLore("§7обратитесь к §bИнспектору§7.")
                            .addLore(op.menu.section==section ? "" : "§7ЛКМ - §fоткрыть")
                            .addLore("")
                            .addLore("§7Вы можете просмотреть")
                            .addLore("§7 все возможные миссии (в т.ч. прошедшие")
                            .addLore("§7и планируемые) в Журнале.")
                            .addLore("§7ПКМ - §fЖурнал \"Миссия сегодня\"")
                            .build(), e -> {
                                if (e.isLeftClick()) {
                                    op.menu.open(op.getPlayer(), section);
                                } else if (e.isRightClick()) {
                                    op.getPlayer().performCommand("mission journal");
                                }
                            }
                    );
                }*/
            }

            case ДРУЗЬЯ -> {
                if (op.isGuest) {
                    if (op.eng) {
                        lore = Arrays.asList(
                            Component.empty(),//0
                            Component.text("§8Not available in guest mode")
                        );
                    } else {
                        lore = Arrays.asList(
                            Component.empty(),//0
                            Component.text("§8Недоступно в гостевом режиме")
                        );
                    }
                    consumer = null;
                } else {
                    final int msg = op.globalInt(Data.FRIENDS_MSG_OFFLINE);
                    if (op.eng) {
                        lore = Arrays.asList(
                            Component.empty(),
                            Component.text("§7LMB - §fShow your friends"),
                            Component.text(msg > 0 ? "§2New mail (§a" + msg + "§2)!" : "§8(no new mail)"),
                            Component.text("§7RMB - §fFriend settings")
                        );
                    } else {
                        lore = Arrays.asList(
                            Component.empty(),
                            Component.text("§7ЛКМ - §fТвои друзья"),
                            Component.text(msg > 0 ? "§2Новые письма (§a" + msg + "§2)!" : "§8(нет новых писем)"),
                            Component.text("§7ПКМ - §fНастройки дружбы")
                        );
                    }
                    consumer = e -> {
                        switch (e.getClick()) {
                            case LEFT -> Friends.openFriendsMain(op); //открыть безусловно (для обновления списка и выходя из режима поиска)
                            case RIGHT -> Friends.openFriendsSettings(op); //открыть безусловно (для обновления списка и выходя из режима поиска)
                        }
                    };
                }
            }

            case КОМАНДА -> {
                if (op.eng) {
                    lore = Arrays.asList(
                        Component.empty(),
                        Component.text("§7Create a party and play together!"),
                        Component.text("§7LMB - §fManage party"),
                        Component.text("§7RMB - §fParty settings"),
                        Component.empty(),
                        Component.text("§5Quick invite:"),
                        Component.text("§d/p <name>"),
                        Component.empty()
                    );
                } else {
                    lore = Arrays.asList(
                        Component.empty(),
                        Component.text("§7Создай команду для совместной игры!"),
                        Component.text("§7ЛКМ - §fУправление командой"),
                        Component.text("§7ПКМ - §fНастройки команды"),
                        Component.empty(),
                        Component.text("§5Быстрое приглашение:"),
                        Component.text("§d/p <ник>"),
                        Component.empty()
                    );
                }
                consumer = e -> {
                    switch (e.getClick()) {
                        case LEFT -> Friends.openPartyMain(op); //открыть безусловно (для обновления списка и выходя из режима поиска)
                        case RIGHT -> Friends.openPartySettings(op); //открыть безусловно (для обновления списка и выходя из режима поиска)
                    }
                };
            }


            case ГРУППЫ -> {
                if (op.eng) {
                    lore = Arrays.asList(
                        Component.empty(),
                        Component.text("§7LMB - §fPaid features"),
                        Component.text("§7RMB - §fAdministration"),
                        Component.empty()
                    );
                } else {
                    lore = Arrays.asList(
                        Component.empty(),
                        Component.text("§7ЛКМ - §fВозможности привилегий"),
                        Component.text("§7ПКМ - §fПерсонал Острова"),
                        Component.empty()
                    );
                }
                consumer = e -> {
                    switch (e.getClick()) {
                        case LEFT -> op.menu.openDonate(op);
                        case RIGHT -> op.menu.showStaff(op);
                    }
                };
            }


            default -> {
                lore = ImmutableList.of();
                consumer = null;
            }

        }

        final ItemStack is = new ItemBuilder(ItemType.PLAYER_HEAD).name(op.eng ? section.item_nameEn : section.item_nameRu)
            .lore(lore).headTexture(section.texture).build();
        return consumer == null ? ClickableItem.empty(is) : ClickableItem.of(is, consumer);
    }


}
