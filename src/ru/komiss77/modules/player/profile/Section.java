package ru.komiss77.modules.player.profile;

import com.google.common.collect.ImmutableList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import ru.komiss77.ApiOstrov;
import ru.komiss77.enums.Data;
import ru.komiss77.enums.Stat;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.translate.Lang;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.ItemUtils;
import ru.komiss77.utils.TCUtils;
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

    МИНИИГРЫ (
            45,
            "§b|с§lВыбор Игры",
            "§b|с§lGame Selection",
            "98daa1e3ed94ff3e33e1d4c6e43f024c47d78a57ba4d38e75e7c9264106",
            Material.LIGHT_BLUE_STAINED_GLASS_PANE
    ),

    РЕЖИМЫ (
            45,
            "§b|с§lВыбор Игры",
            "§b|с§lGame Selection",
            "98daa1e3ed94ff3e33e1d4c6e43f024c47d78a57ba4d38e75e7c9264106",
            Material.LIGHT_BLUE_STAINED_GLASS_PANE
    ),

    ВОЗМОЖНОСТИ (
            46,
            "§с|3§lВозможности",
            "§с|3§lAbilities",
            "be3db27cbd1789310409081ad8c42d690b08961b55cadd45b42d46bca28b8",
            Material.LIME_STAINED_GLASS_PANE
    ), 
    
    ПРОФИЛЬ (
            47,
            "§3|d§lПрофиль",
            "§3|d§lProfile",
            "2433b16d98e0d9d335027f23332e208b7c3fff0d7984792ea48c93ca5cbcf1e1", 
            Material.GRAY_STAINED_GLASS_PANE
    ),
    
    СТАТИСТИКА (
            48,
            "§d|9§lСтатистика",
            "§d|9§lStatistics",
            "5b4ddb8abed660825b68b922e22a9558c2f208938bd438eaeaccdc3941",
            Material.PURPLE_STAINED_GLASS_PANE
    ),
    
    ДОСТИЖЕНИЯ (
            49,
            "§9|н§lДостижения",
            "§9|н§lAchievements",
            "cf7cdeefc6d37fecab676c584bf620832aaac85375e9fcbff27372492d69f", 
            Material.BROWN_STAINED_GLASS_PANE
    ), 
    
    МИССИИ(
            50,
            "§н|6§lМиссии",
            "§н|6§lMissions",
            "bf6464a5ba11e1e59f0948a3d95846654253bf2822c6b1c1b3a4a3fd31ba4f",
            Material.ORANGE_STAINED_GLASS_PANE
    ), 
    
    ДРУЗЬЯ(
            51,
            "§a§lД§d§lр§c§lу§e§lз§9§lь§b§lя",//"§a§lДрузья",
            "§a§lF§d§lr§c§li§e§le§9§ln§b§ld§3§ls",//"§a§lДрузья",
            "f3ebdbad610315ce554db4f56cb5ede6ac7ca6aa11cee02e85f94c52131d69",
            Material.LIME_STAINED_GLASS_PANE
    ), 
    
    КОМАНДА (
            52,
            "§6|c§l§lКоманда",
            "§6|c§lParty",
            "359d1bbffad5422197b573d501465392feef6dc5d426dcd763efed7893d39d",
            Material.RED_STAINED_GLASS_PANE
    ),
    
    ГРУППЫ (
            53,
            "§c|ф§lПривилегии",
            "§c|ф§lDonations",
            "1c8e0cfebc7f9c7e16fbaaae025d1b1d19d5ee633666bcf25fa0b40d5bd21bcd",
            Material.YELLOW_STAINED_GLASS_PANE
    ), 
    ;
    
    final public int slot;
    final public String item_nameRu;
    final public String item_nameEn;
    final public String texture;
    final public Material glassMat;
    
    
    Section(int slot, String item_nameRu, String item_nameEn, String texture, Material glassMat){
        this.slot = slot;
        this.item_nameRu = item_nameRu;
        this.item_nameEn = item_nameEn;
        this.texture = texture;
        this.glassMat = glassMat;
    }
    
    
    public static boolean isProfileIcon(final int slot){
        for(Section s_: Section.values()){
            if (s_.slot==slot) return true;
        }
        return false;
    }
    
    public static Section profileBySlot(final int slot){
        for(Section s_: Section.values()){
            if (s_.slot==slot) return s_;
        }
        return null;
    }
    
    
    
    public static ItemStack getItem(final Section section, final Oplayer op){
        return new ItemBuilder(Material.PLAYER_HEAD)
        .name(op.eng ? section.item_nameEn : section.item_nameRu)
        .setCustomHeadTexture(section.texture)
        .build();
    }
    
    public static ClickableItem getMenuItem(final Section section, final Oplayer op){
        
        final List<Component>lore;
        final Consumer<InventoryClickEvent> consumer;
        
        switch (section) {
            
            case РЕЖИМЫ -> {
                if (op.eng) {
                    lore = Arrays.asList(
                        Component.empty(),
                        Component.text("§7Choise a game or arena"),
                        Component.empty(),
                        Component.empty(),
                        Component.text(op.menu.section==section ? "" : "§7LMB - §fshow games"),
                        Component.empty()
                    );
                } else {
                    lore = Arrays.asList(
                        Component.empty(),
                        Component.text("§7Выбор игры или арены"),
                        Component.empty(),
                        Component.empty(),
                        Component.text(op.menu.section==section ? "" : "§7ЛКМ - §fразвернуть режимы"),
                        Component.empty()
                    );
                }
                consumer = e -> {if (op.menu.section!=section || op.menu.game!=null) op.menu.open(op.getPlayer(), section);};
               /* return ClickableItem.of(new ItemBuilder(Material.PLAYER_HEAD)
                        .name(op.eng ? Lang.t(section.item_name, EnumLang.EN_US) : section.item_name)
                        .setCustomHeadTexture(section.texture)
                        .addLore("") //0
                        .addLore(op.eng ? "":"§7Выбор игры или арены") //1
                        .addLore("")
                        .addLore("")
                        .addLore(op.menu.section==section ? "" : (op.eng ? "":"§7ЛКМ - §fразвернуть режимы"))
                        .addLore("")
                        .build(), e -> { //приклике на режимы если открыты арены - сбросить на игры и переоткрыть
                            if (op.menu.section!=section || op.menu.game!=null) op.menu.open(op.getPlayer(), section);
                        }
                );*/
            }
                
            
            case ВОЗМОЖНОСТИ -> {
                if (op.eng) {
                    lore = Arrays.asList(
                       Component.empty(),
                        Component.text("§7LMB - Local menu"),
                        Component.empty(),
                        Component.text("§7RMB - Local settings"),
                        Component.empty()
                    );
                } else {
                    lore = Arrays.asList(
                        Component.empty(),
                        Component.text("§7ЛКМ  - Локальные меню"),
                        Component.empty(),
                        Component.text("§7ПКМ - Локальные настройки"),
                        Component.empty()
                    );
                }
                consumer = e -> {
                    op.menu.openLocalSettings(op.getPlayer(), e.isRightClick());
                };

               /* return ClickableItem.of(new ItemBuilder(Material.PLAYER_HEAD)
                        .name(op.eng ? Lang.t(section.item_name, EnumLang.EN_US) : section.item_name)
                        .setCustomHeadTexture(section.texture)
                        .addLore("")
                        .addLore(op.eng ? "":"§7ЛКМ  - Локальные меню")
                        //.addLore(op.menu.section==section ? "" : "§7ЛКМ - §fоткрыть")
                        .addLore("")
                        .addLore(op.eng ? "":"§7ПКМ - Локальные настройки")
                        .build(), e -> { //приклике на режимы если открыты арены - сбросить на игры и переоткрыть
                            switch (e.getClick()) {
                                case LEFT -> //if (op.menu.section!=section || op.menu.localSettingsPage)
                                    op.menu.openLocalMenu(op.getPlayer());
                                case RIGHT -> //if (op.menu.section!=section || !op.menu.localSettingsPage)
                                    op.menu.openLocalSettings(op.getPlayer());
                                case SHIFT_RIGHT -> {
                                }
                            }
                        }
                );*/
            }
                
            case ПРОФИЛЬ -> {
                if (op.eng) {
                    lore = Arrays.asList(
                        Component.empty(),//0
                        //1 игровое время обновление каждую секунду в ProfileManager
                        Component.text( "§fPlayTime total : §a"+ApiOstrov.secondToTime(op.getStat(Stat.PLAY_TIME))),
                        //2 наиграно за сегодня обновление каждую секунду в ProfileManager
                        Component.text( Lang.t( Stat.PLAY_TIME.desc,Lang.EN)+ApiOstrov.secondToTime(op.getDaylyStat(Stat.PLAY_TIME))),
                        Component.text(Lang.t( Stat.LEVEL.desc, Lang.EN)+op.getStat(Stat.LEVEL) +"  "+ ApiOstrov.getPercentBar(op.getStat(Stat.LEVEL)*25, op.getStat(Stat.EXP), true)),
                        Component.empty(),
                        Component.text(op.getDataInt(Data.REPORT_C)>0 ? "§cConsole reports §7: "+op.getDataInt(Data.REPORT_C) : "§8No comments"),
                        Component.text(op.getDataInt(Data.REPORT_P)>0 ? "§cPlayer reports §7: "+op.getDataInt(Data.REPORT_P) : "§8No complaints received"),
                        Component.empty(),
                        Component.text("§7LMB - §fexpand profile"),
                        Component.text(op.isGuest ? "§8*Passport not available" : "§7RMB - §fPassport of an Islander"),
                        Component.text(op.isGuest ? "§8for guests":"§7Shift+RMB - Get a copy of your passport"),
                        Component.empty()
                    );
                } else {
                    lore = Arrays.asList(
                        Component.empty(),//0
                        //1 игровое время обновление каждую секунду в ProfileManager
                        Component.text( Stat.PLAY_TIME.desc+ApiOstrov.secondToTime(op.getStat(Stat.PLAY_TIME))),
                        //2 наиграно за сегодня обновление каждую секунду в ProfileManager
                        Component.text("§fНаиграно за сегодня : §e"+ApiOstrov.secondToTime(op.getDaylyStat(Stat.PLAY_TIME))),
                        Component.text(Stat.LEVEL.desc+op.getStat(Stat.LEVEL) +"  "+ ApiOstrov.getPercentBar(op.getStat(Stat.LEVEL)*25, op.getStat(Stat.EXP), true)),
                        Component.empty(),
                        Component.text(op.getDataInt(Data.REPORT_C)>0 ? "§cРепорты консоли §7: "+op.getDataInt(Data.REPORT_C) : "§8Замечаний нет"),
                        Component.text(op.getDataInt(Data.REPORT_P)>0 ? "§cРепорты игроков §7: "+op.getDataInt(Data.REPORT_P) : "§8Жалоб не поступало"),
                        Component.empty(),
                        Component.text("§7ЛКМ - §fразвернуть профиль"),
                        Component.text(op.isGuest ? "§8*Паспорт недоступен" : "§7ПКМ - §fПаспорт Островитянина"),
                        Component.text(op.isGuest ? "§8гостям":"§7Шифт+ПКМ - Получить копию паспорта"),
                        Component.empty()
                    );
                }
                consumer = e -> {
                    switch (e.getClick()) {
                        case LEFT -> op.menu.open(op.getPlayer(), section); //открыть безусловно (для обновления списка и выходя из режима поиска)
                        case RIGHT -> op.getPlayer().performCommand("passport edit");//if (!op.isGuest) op.menu.openPassport(op.getPlayer());
                        case SHIFT_RIGHT -> {
                            if (!op.isGuest) op.getPlayer().performCommand("passport get");
                        }
                    }
                };

                /*return ClickableItem.of(new ItemBuilder(Material.PLAYER_HEAD)
                        .name(op.eng ? Lang.t(section.item_name, EnumLang.EN_US) : section.item_name)
                        .setCustomHeadTexture(section.texture)
                        .addLore("") //0
                        //1 игровое время обновление каждую секунду в ProfileManager
                        .addLore(Stat.PLAY_TIME.desc+ApiOstrov.secondToTime(op.getStat(Stat.PLAY_TIME)) )
                        //2 наиграно за сегодня обновление каждую секунду в ProfileManager
                        .addLore((op.eng ? "":"§fНаиграно за сегодня : §e")+ApiOstrov.secondToTime(op.getDaylyStat(Stat.PLAY_TIME)))
                        .addLore( Stat.LEVEL.desc+op.getStat(Stat.LEVEL) +"  "+ ApiOstrov.getPercentBar(op.getStat(Stat.LEVEL)*25, op.getStat(Stat.EXP), true)  )
                        .addLore("")
                        .addLore( op.getDataInt(Data.REPORT_C)>0 ? Data.REPORT_C.desc+op.getDataInt(Data.REPORT_C) : (op.eng ? "":"§8Замечаний нет") )
                        .addLore( op.getDataInt(Data.REPORT_P)>0 ? Data.REPORT_P.desc+op.getDataInt(Data.REPORT_P) : (op.eng ? "":"§8Жалоб не поступало") )
                        .addLore("")
                        .addLore(op.eng ? "":"§7ЛКМ - §fразвернуть профиль")
                        .addLore(op.isGuest ? (op.eng ? "":"§8*Паспорт недоступен") : (op.eng ? "":"§7ПКМ - §fПаспорт Островитянина"))
                        .addLore(op.isGuest ? (op.eng ? "":"§8гостям"):(op.eng ? "":"§7Шифт+ПКМ - Получить копию паспорта"))
                        .addLore("")
                        .build(), e -> {
                            switch (e.getClick()) {
                                case LEFT -> op.menu.open(op.getPlayer(), section); //открыть безусловно (для обновления списка и выходя из режима поиска)
                                case RIGHT -> {
                                    if (!op.isGuest) op.menu.openPassport(op.getPlayer());
                                }
                                case SHIFT_RIGHT -> {
                                    if (!op.isGuest) op.getPlayer().performCommand("passport get");
                                }
                            }
                        }
                );*/
            }
                
            case СТАТИСТИКА -> {
                if (op.eng) {
                    lore = Arrays.asList(
                        Component.empty(),//0
                        Component.text("§7Daily stat reset at midnight."),
                        Component.text("§6Before resetting daily stat:"),
                        Component.empty(),//3 =до сброса дневной статы= обновление каждую секунду в ProfileManager
                        Component.empty(),
                        Component.text(op.menu.section==section ? "" : "§7LMB - §fshow all stat"),
                        Component.empty(),
                        Component.text("§fYour Karma: "+op.getKarmaDisplay() ),
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
                        Component.text(op.menu.section==section ? "" : "§7ЛКМ - §fразвернуть статистику"),
                        Component.empty(),
                        Component.text("§fВаша Карма: "+op.getKarmaDisplay() ),
                        Component.empty(),
                        Component.text("§7Карма ухудшается при §cпроигрышах§7,"),
                        Component.text("§7и улучшается при §aвыиграшах§7."),
                        Component.text("§7Игрок с хорошей кармой в команде"),
                        Component.text("§7может быть весьма полезен!"),
                        Component.empty()
                    );
                }
                consumer = e -> {if (op.menu.section!=section) op.menu.open(op.getPlayer(), section);};
            }
                
            case ДОСТИЖЕНИЯ -> {
                if (op.eng) {
                    lore = Arrays.asList(
                        Component.empty(),
                        Component.text("§7Your achievements"),
                        Component.text("§7in various games."),
                        Component.text("§7Achievements - important"),
                        Component.text("§7part of development,"),
                        Component.text("§7for each you get"),
                        Component.text("§7additional experience."),
                        Component.text(op.menu.section==section ? "" : "§7LMB - §fshow achievements"),
                        Component.empty()
                    );
                } else {
                    lore = Arrays.asList(
                        Component.empty(),
                        Component.text("§7Ваши достижения"),
                        Component.text("§7в различных режимах."),
                        Component.text("§7Достижения - важная"),
                        Component.text("§7часть развития,"),
                        Component.text("§7за каждое Вы получаете"),
                        Component.text("§7дополнительный опыт."),
                        Component.text(op.menu.section==section ? "" : "§7ЛКМ - §fразвернуть достижения"),
                        Component.empty()
                    );
                }
                consumer = e -> {if (op.menu.section!=section) op.menu.open(op.getPlayer(), section);};
                /*return ClickableItem.of(new ItemBuilder(Material.PLAYER_HEAD)
                        .name(op.eng ? Lang.t(section.item_name, EnumLang.EN_US) : section.item_name)
                        .setCustomHeadTexture(section.texture)
                        .addLore("")
                        .addLore("§7Ваши достижения")
                        .addLore("§7в различных режимах.")
                        .addLore("§7Достижения - важная")
                        .addLore("§7часть развития,")
                        .addLore("§7за каждое Вы получаете")
                        .addLore("§7дополнительный опыт.")
                        .addLore(op.menu.section==section ? "" : "§7ЛКМ - §fразвернуть достижения")
                        .addLore("")
                        .build(), e -> {
                            if (op.menu.section!=section) op.menu.open(op.getPlayer(), section);
                        }
                );*/
            }
                
            case МИССИИ -> {
                if (op.isGuest) {
                   if (op.eng) {
                        lore = Arrays.asList(
                            Component.empty(),//0
                            Component.text("§8Missions not available"),
                            Component.text("§8in guest mode"),
                            Component.empty()
                        );
                    } else {
                        lore = Arrays.asList(
                            Component.empty(),//0
                            Component.text("§8Миссии недоступны"),
                            Component.text("§8в гостевом режиме"),
                            Component.empty()
                        );
                    }
                   consumer = null;
                } else {
                    if (op.eng) {
                        lore = Arrays.asList(
                            Component.empty(),
                            Component.text("§7Here you can see missions,"),
                            Component.text("§7in which you participate"),
                            Component.text("§7and progress on them."),
                            Component.empty(),
                            Component.text("§7To start a new one,"),
                            Component.text("§7cancel or complete the mission,"),
                            Component.text("§7contact to §bInspector§7."),
                            Component.text(op.menu.section==section ? "" : "§7LMB - §fopen"),
                            Component.empty(),
                            Component.text("§7You can view all possible"),
                            Component.text("§7missions (including outdated"),
                            Component.text("§7and planned) In the journal."),
                            Component.text("§7RMB - §fJournal \"Mission today\""),
                            Component.empty()
                        );
                    } else {
                        lore = Arrays.asList(
                            Component.empty(),
                            Component.text("§7Здесь вы увидите миссии,"),
                            Component.text("§7в корорых участвуете"),
                            Component.text("§7и прогресс по ним."),
                            Component.empty(),
                            Component.text("§7Чтобы начать новую,"),
                            Component.text("§7отменить или завершить миссию,"),
                            Component.text("§7обратитесь к §bИнспектору§7."),
                            Component.text(op.menu.section==section ? "" : "§7ЛКМ - §fоткрыть"),
                            Component.empty(),
                            Component.text("§7Вы можете просмотреть"),
                            Component.text("§7 все возможные миссии (в т.ч. прошедшие"),
                            Component.text("§7и планируемые) в Журнале."),
                            Component.text("§7ПКМ - §fЖурнал \"Миссия сегодня\""),
                            Component.empty()
                        );
                    }
                     consumer = e -> {
                        if (e.isLeftClick()) {
                            op.menu.open(op.getPlayer(), section);
                        } else if (e.isRightClick()) {
                            op.getPlayer().performCommand("mission journal");
                        }
                    };
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
                            Component.empty(),
                            Component.text("§8Friends are unavailable"),
                            Component.text("§8in guest mode"),
                            Component.empty()
                        );
                    } else {
                        lore = Arrays.asList(
                            Component.empty(),
                            Component.text("§8Друзья недоступны"),
                            Component.text("§8в гостевом режиме"),
                            Component.empty()
                        );
                    }
                    consumer = null;
               } else {
                    if (op.eng) {
                        lore = Arrays.asList(
                            Component.empty(),
                            Component.text("§7Your friends"),
                            Component.empty(),
                            Component.text("§7LMB - §fshow/add friends"),
                            Component.text(op.getDataInt(Data.FRIENDS_MSG_OFFLINE)>0 ? "§7RMB - §eview mail" : "§8no mail"),
                            Component.text("§7shift+PCM - §ffriendship settings"),
                            Component.empty()
                        );
                    } else {
                        lore = Arrays.asList(
                            Component.empty(),
                            Component.text("§7Ваши Друзья"),
                            Component.empty(),
                            Component.text("§7ЛКМ - §fпоказать/добавить друзей"),
                            Component.text(op.getDataInt(Data.FRIENDS_MSG_OFFLINE)>0 ? "§7ПКМ - §eпросмотр писем" : "§8писем нет"),
                            Component.text("§7Шифт+ПКМ - §fнастройки дружбы"),
                            Component.empty()
                        );
                    }
                    consumer = e -> {
                        switch (e.getClick()) {
                            case LEFT -> Friends.openFriendsMain(op); //открыть безусловно (для обновления списка и выходя из режима поиска)
                            case RIGHT -> Friends.openFriendsMail(op); //открыть безусловно (для обновления списка и выходя из режима поиска)
                            case SHIFT_RIGHT -> Friends.openFriendsSettings(op);
                        }
                    };                
               }

                
                
            /*   if (op.isGuest) {
                   return ClickableItem.empty(new ItemBuilder(Material.PLAYER_HEAD)
                           .name(op.eng ? Lang.t(section.item_name, EnumLang.EN_US) : section.item_name)
                           .setCustomHeadTexture(section.texture)
                           .addLore("")
                           .addLore("§8Друзья недоступны")
                           .addLore("§8в гостевом режиме")
                           .addLore("")
                           .build()
                   );
               } else {
                   return ClickableItem.of(new ItemBuilder(Material.PLAYER_HEAD)
                           .name(op.eng ? Lang.t(section.item_name, EnumLang.EN_US) : section.item_name)
                           .setCustomHeadTexture(section.texture)
                           .addLore("")
                           .addLore("§7Ваши Друзья")
                           .addLore("")
                           .addLore("§7ЛКМ - §fпоказать/добавить друзей")
                           .addLore(op.getDataInt(Data.FRIENDS_MSG_OFFLINE)>0 ? "§7ПКМ - §eпросмотр писем" : "§8ПКМ - просмотр писем")
                           .addLore("§7Шифт+ПКМ - §fнастройки дружбы")
                           .addLore("")
                           .build(), e -> {
                               switch (e.getClick()) {
                                   case LEFT -> Friends.openFriendsMain(op); //открыть безусловно (для обновления списка и выходя из режима поиска)
                                   case RIGHT -> Friends.openFriendsMail(op); //открыть безусловно (для обновления списка и выходя из режима поиска)
                                   case SHIFT_RIGHT -> Friends.openFriendsSettings(op);
                               }
                           }
                   );
               }*/
            }
                
           case КОМАНДА -> {
                if (op.eng) {
                    lore = Arrays.asList(
                        Component.empty(),
                        Component.text("§7Party"),
                        Component.empty(),
                        Component.text("§7Party must be created"),
                        Component.text("§7for co-op play."),
                        Component.text("§7You be in party until leave"),
                        Component.text("§7party or disconnect."),
                        Component.empty(),
                        Component.text("§7LMB - §fmanagement"),
                        Component.text("§7RMB - §fparty settings"),
                        Component.empty(),
                        Component.text("§5Invite crossServer:"),
                        Component.text("§d/patry invite <name>"),
                        Component.empty()
                    );
                } else {
                    lore = Arrays.asList(
                        Component.empty(),
                        Component.text("§7Команда"),
                        Component.empty(),
                        Component.text("§7Команда создаётся"),
                        Component.text("§7для совместной игры."),
                        Component.text("§7Вы будете в команде до выхода"),
                        Component.text("§7из команды или дисконнекта."),
                        Component.empty(),
                        Component.text("§7ЛКМ - §fуправление"),
                        Component.text("§7ПКМ - §fнастройки команды"),
                        Component.empty(),
                        Component.text("§5Пригласить кросСерверно:"),
                        Component.text("§d/patry invite <ник>"),
                        Component.empty()
                    );
                }
                consumer = e -> {
                    switch (e.getClick()) {
                       case LEFT -> Friends.openPartyMain(op); //открыть безусловно (для обновления списка и выходя из режима поиска)
                       case RIGHT -> Friends.openPartySettings(op); //открыть безусловно (для обновления списка и выходя из режима поиска)
                   }
                };
             /*  return ClickableItem.of(new ItemBuilder(Material.PLAYER_HEAD)
                       .name(op.eng ? Lang.t(section.item_name, EnumLang.EN_US) : section.item_name)
                       .setCustomHeadTexture(section.texture)
                       .addLore("")
                       .addLore("§7Команда")
                       .addLore("")
                       .addLore("§7Команда создаётся")
                       .addLore("§7для совместной игры.")
                       .addLore("§7Вы будете в команде до выхода")
                       .addLore("§7командой или дисконнекта.")
                       .addLore("")
                       .addLore("§7ЛКМ - §fуправление")
                       .addLore("§7ПКМ - §fнастройки команды")
                       .addLore("")
                       .addLore("§5Пригласить кросСерверно:")
                       .addLore("§d/patry invite <ник>")
                       .addLore("")
                       .build(), e -> {
                           switch (e.getClick()) {
                               case LEFT -> Friends.openPartyMain(op); //открыть безусловно (для обновления списка и выходя из режима поиска)
                               case RIGHT -> Friends.openPartySettings(op); //открыть безусловно (для обновления списка и выходя из режима поиска)

                           }
                       }
               );*/
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
                        Component.text("§7ЛКМ - §fПлатные возможности"),
                        Component.text("§7ПКМ - §fАдминистрация"),
                        Component.empty()
                    );
                }
                consumer = e -> {                           
                    switch (e.getClick()) {
                        case LEFT -> op.menu.openDonate(op);
                        case RIGHT -> op.menu.showStaff(op);
                    }
                };
               /*return ClickableItem.of(new ItemBuilder(Material.PLAYER_HEAD)
                       .name(op.eng ? Lang.t(section.item_name, EnumLang.EN_US) : section.item_name)
                       .setCustomHeadTexture(section.texture)
                       .addLore("")
                       .addLore("§7ЛКМ - §fПлатные возможности")
                       .addLore("§7ПКМ - §fАдминистрация")
                       .addLore("")
                       .build(), e -> {
                           switch (e.getClick()) {
                               case LEFT -> op.menu.openDonate(op);
                               case RIGHT -> op.menu.showStaff(op);
                               default -> {}
                           }
                       }
               );*/
            }
                

            default -> {
                    lore = ImmutableList.of();
                    consumer = null;
                /*return ClickableItem.empty(new ItemBuilder(Material.PLAYER_HEAD)
                        .name(op.eng ? Lang.t(section.item_name, EnumLang.EN_US) : section.item_name)
                        .setCustomHeadTexture(section.texture)
                        //  .addLore( section.lore)
                        .build());*/
            }
            
        }

        final ItemStack is = new ItemStack(Material.PLAYER_HEAD);
        final ItemMeta im = is.getItemMeta();
        im.displayName(TCUtils.format(op.eng ? section.item_nameEn : section.item_nameRu));
        im.lore(lore);
        ItemUtils.setHeadTexture((SkullMeta) im, section.texture);
        is.setItemMeta(im);
        return consumer == null ? ClickableItem.empty(is) : ClickableItem.of(is, consumer);
    }
    
    
    
    
    
    
}
