package ru.komiss77.modules.player.profile;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import ru.komiss77.ApiOstrov;
import ru.komiss77.enums.Data;
import ru.komiss77.enums.Stat;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.utils.ItemBuilder;
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

    РЕЖИМЫ (
            45,
            "§b§lВыбор игры", 
         //   Arrays.asList(
         //           "§fЛКМ §7- Открыть меню выбора"
         //   ),
            "98daa1e3ed94ff3e33e1d4c6e43f024c47d78a57ba4d38e75e7c9264106",
            Material.LIGHT_BLUE_STAINED_GLASS_PANE
    ), 
    
    
   ВОЗМОЖНОСТИ (
            46,
            "§a§lВозможности", 
         //   Arrays.asList(
         //           "§fЛКМ §7- Открыть меню выбора"
         //   ),
            "be3db27cbd1789310409081ad8c42d690b08961b55cadd45b42d46bca28b8",
            Material.LIME_STAINED_GLASS_PANE
    ), 
    
    
    
    ПРОФИЛЬ (
            47,
            "§7§lПрофиль",
         //   Arrays.asList(
        //            ""
        //    ), 
            "2433b16d98e0d9d335027f23332e208b7c3fff0d7984792ea48c93ca5cbcf1e1", 
            Material.BLACK_STAINED_GLASS_PANE
    ),
    
    
    СТАТИСТИКА (
            48,
            "§d§lСтатистика", 
         //   Arrays.asList(
         //           ""
        //    ),
            "5b4ddb8abed660825b68b922e22a9558c2f208938bd438eaeaccdc3941",
            Material.PURPLE_STAINED_GLASS_PANE
    ),
    
    
    ДОСТИЖЕНИЯ (
            49,
            "§3§lДостижения",
          //  Arrays.asList(
         //           ""
         //   ), 
            "cf7cdeefc6d37fecab676c584bf620832aaac85375e9fcbff27372492d69f", 
            Material.BROWN_STAINED_GLASS_PANE
    ), 
    
    
    МИССИИ(
            50,
            "§6§lМиссии", 
         //   Arrays.asList(
         //           ""
        //    ),
            "bf6464a5ba11e1e59f0948a3d95846654253bf2822c6b1c1b3a4a3fd31ba4f",
            Material.ORANGE_STAINED_GLASS_PANE
    ), 
    
    
    ДРУЗЬЯ(
            51,
            "§a§lД§d§lр§c§lу§e§lз§9§lь§b§lя",//"§a§lДрузья",
          //  Arrays.asList(
          //          "§aЛКМ §7- друзья", "§aПКМ §7- настройки друзей", 
         //           "Добавить друга-клик головой","или команда §6/fr add ник"
        //    ), 
            "f3ebdbad610315ce554db4f56cb5ede6ac7ca6aa11cee02e85f94c52131d69",
            Material.LIME_STAINED_GLASS_PANE
    ), 
    
    КОМАНДА (
            52,
            "§c§lКоманда", 
           // Arrays.asList(
          //          "Создать команду-§6/party create",
          //          "§aЛКМ §7- участники", 
         //           "§aПКМ §7- настройки команды",
        //            "§cShift+Клик §7- режим удаления участников"
        //    ), 
            "359d1bbffad5422197b573d501465392feef6dc5d426dcd763efed7893d39d",
            Material.RED_STAINED_GLASS_PANE
    ),
    
    ГРУППЫ (
            53,
            "§c§lПривилегии", 
           // Arrays.asList(
          //          "Создать команду-§6/party create",
          //          "§aЛКМ §7- участники", 
         //           "§aПКМ §7- настройки команды",
        //            "§cShift+Клик §7- режим удаления участников"
        //    ), 
            "1c8e0cfebc7f9c7e16fbaaae025d1b1d19d5ee633666bcf25fa0b40d5bd21bcd",
            Material.YELLOW_STAINED_GLASS_PANE
    ), 
    
   /* РЕЖИМЫ (
            45,
            "§fВыбор игры", 
         //   Arrays.asList(
         //           "§fЛКМ §7- Открыть меню выбора"
         //   ),
            "362a1b473ba3c25e6eb1458dfc953921f5d91478af9d6dda07c032f928c",
            Material.BLACK_STAINED_GLASS_PANE
    ), 
    
    
    
    ПРОФИЛЬ (
            46,
            "§fПрофиль",
         //   Arrays.asList(
        //            ""
        //    ), 
            "a683dae6d132f52efa55e98c181def5403672fdec2cbcf4793142cb0e59bd0", 
            Material.RED_STAINED_GLASS_PANE
    ),
    
    
    СТАТИСТИКА (
            47,
            "§fСтатистика", 
         //   Arrays.asList(
         //           ""
        //    ),
            "a8fa562856959dfefbc1328cdfd9d430b65af7f24cf326400767305b34c5b2e5",
            Material.RED_STAINED_GLASS_PANE
    ),
    
    
    ДОСТИЖЕНИЯ (
            48,
            "§fДостижения",
          //  Arrays.asList(
         //           ""
         //   ), 
            "1c8e0cfebc7f9c7e16fbaaae025d1b1d19d5ee633666bcf25fa0b40d5bd21bcd", 
            Material.CYAN_STAINED_GLASS_PANE
    ), 
    
    
    ЗАДАНИЯ(
            49,
            "§bЗадания", 
         //   Arrays.asList(
         //           ""
        //    ),
            "cf40942f364f6cbceffcf1151796410286a48b1aeba77243e218026c09cd1",
            Material.ORANGE_STAINED_GLASS_PANE
    ), 
    
    
    ДРУЗЬЯ(
            50,
            "§aДрузья",
          //  Arrays.asList(
          //          "§aЛКМ §7- друзья", "§aПКМ §7- настройки друзей", 
         //           "Добавить друга-клик головой","или команда §6/fr add ник"
        //    ), 
            "3228db8b2cd49a4b986a3b0ea8a161a1742b1c07fd0e2b15be88dc76498e5",
            Material.PURPLE_STAINED_GLASS_PANE
    ), 
    
    КОМАНДА(
            51,
            "§5Команда", 
           // Arrays.asList(
          //          "Создать команду-§6/party create",
          //          "§aЛКМ §7- участники", 
         //           "§aПКМ §7- настройки команды",
        //            "§cShift+Клик §7- режим удаления участников"
        //    ), 
            "2c2be3bc607773264ceee0edd615316341ec2b465a16cdb578bdaedd643d",
            Material.GREEN_STAINED_GLASS_PANE
    ), 
    */
    
    //ДОНАТ(50,"§3Купить привилегии", "§aЛКМ §7- посмотреть привилегии<br>§aПКМ §7- пополнить счёт<br><br>§fНа сервере у Вас есть<br>§fрублёвый счёт (§eрил§f).<br>§fВы можете пополнять его<br>§fчерез платёжную систему,<br>§fи использовать эти средства<br>§fдля покупки привилегий, обмена<br>§fна игровую валюту (§eлони§f)<br>§fи прочее.", "d295a929236c1779eab8f57257a86071498a4870196941f4bfe1951e8c6ee21a", Material.CYAN_STAINED_GLASS_PANE), 
    //ПОМОЩЬ(52,"§2Помощь","§aЛКМ §7- по глобальным командам<br>§aПКМ §7- локальные команды<br>§aShift+клик §7- Администрация<br><br>Глобальные команды действуют<br>на всех серверах,<br>Локальные - выполняются только<br>на том сервере, где вы<br>находитесь в данный момент.", "fed4ae757f23445b5c9335cc5a8f7f7c6f9a5aee85bb69fe97f581dafb18d30", Material.BROWN_STAINED_GLASS_PANE), 
    //НАСТРОЙКИ(53,"§6Локальные настройки", "§aЛКМ §7- Настройки для текущего сервера<br>§aПКМ §7- редактировать пасспорт Островитянина", "b86b9d58bcd1a555f93e7d8659159cfd25b8dd6e9bce1e973822824291862", Material.BLACK_STAINED_GLASS_PANE), 
    //ЗАКРЫТЬ(53,"§4Закрыть", "§7Закрыть меню", "", Material.BLACK_STAINED_GLASS_PANE), 
    ;
    
    final public int slot;
    final public String item_name;
    //final public List<String> lore;
    final public String texture;
    final public Material glassMat;
    
    
    private Section(int slot, String item_name, String texture, Material glassMat){
        this.slot = slot;
        this.item_name = item_name;
      //  this.lore = lore;
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
        .name(section.item_name)
        .setCustomHeadTexture(section.texture)
        .build();
        //final ItemStack is = new ItemStack(Material.PLAYER_HEAD);
        //final ItemMeta im = is.getItemMeta();
        //im.displayName(Component.text(section.item_name));
        

    }
    
    public static ClickableItem getMenuItem(final Section section, final Oplayer op){
        
        switch (section) {
            
            case РЕЖИМЫ -> {
                return ClickableItem.of(new ItemBuilder(Material.PLAYER_HEAD)
                        .name(section.item_name)
                        .setCustomHeadTexture(section.texture)
                        .addLore("") //0
                        .addLore("§7Выбор игры или арены") //1
                        .addLore("")
                        .addLore("")
                        .addLore(op.menu.section==section ? "" : "§7ЛКМ - §fразвернуть режимы")
                        .addLore("")
                        .build(), e -> { //приклике на режимы если открыты арены - сбросить на игры и переоткрыть
                            if (op.menu.section!=section || op.menu.game!=null) op.menu.open(op.getPlayer(), section);
                        }
                );
            }
                
            
            case ВОЗМОЖНОСТИ -> {
                return ClickableItem.of(new ItemBuilder(Material.PLAYER_HEAD)
                        .name(section.item_name)
                        .setCustomHeadTexture(section.texture)
                        .addLore("")
                        .addLore("§7ЛКМ  - Локальные меню")
                        //.addLore(op.menu.section==section ? "" : "§7ЛКМ - §fоткрыть")
                        .addLore("")
                        .addLore("§7ПКМ - Локальные настройки")
                        .build(), e -> { //приклике на режимы если открыты арены - сбросить на игры и переоткрыть
                            switch (e.getClick()) {
                                case LEFT -> //if (op.menu.section!=section || op.menu.localSettingsPage)
                                    op.menu.openLocalMenu(op.getPlayer());
                                case RIGHT -> //if (op.menu.section!=section || !op.menu.localSettingsPage)
                                    op.menu.openLocalSettings(op.getPlayer());
                                case SHIFT_RIGHT -> {
                                }
                                default -> {}
                            }
                        }
                );
            }
                
            case ПРОФИЛЬ -> {
                return ClickableItem.of(new ItemBuilder(Material.PLAYER_HEAD)
                        .name(section.item_name)
                        .setCustomHeadTexture(section.texture)
                        .addLore("") //0
                        //1 игровое время обновление каждую секунду в ProfileManager
                        .addLore(Stat.PLAY_TIME.desc+ApiOstrov.secondToTime(op.getStat(Stat.PLAY_TIME)) )
                        //2 наиграно за сегодня обновление каждую секунду в ProfileManager
                        .addLore("§fНаиграно за сегодня : §e"+ApiOstrov.secondToTime(op.getDaylyStat(Stat.PLAY_TIME)))
                        .addLore( Stat.LEVEL.desc+op.getStat(Stat.LEVEL) +"  "+ ApiOstrov.getPercentBar(op.getStat(Stat.LEVEL)*25, op.getStat(Stat.EXP), true)  )
                        .addLore("")
                        .addLore( op.getDataInt(Data.REPORT_C)>0 ? Data.REPORT_C.desc+op.getDataInt(Data.REPORT_C) : "§8Замечаний нет" )
                        .addLore( op.getDataInt(Data.REPORT_P)>0 ? Data.REPORT_P.desc+op.getDataInt(Data.REPORT_P) : "§8Жалоб не поступало" )
                        .addLore("")
                        .addLore("§7ЛКМ - §fразвернуть профиль")
                        .addLore(op.isGuest ? "§8*Паспорт недоступен" : "§7ПКМ - §fПаспорт Островитянина")
                        .addLore(op.isGuest ? "§8гостям":"§7Шифт+ПКМ - Получить копию паспорта")
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
                                default -> {}
                            }
                        }
                );
            }
                
            case СТАТИСТИКА -> {
                return ClickableItem.of(new ItemBuilder(Material.PLAYER_HEAD)
                        .name(section.item_name)
                        .setCustomHeadTexture(section.texture)
                        .addLore("") //0
                        .addLore("§7Дневня статистика обнуляется в полночь.") //1
                        .addLore("§6До сброса дневной статистики:") //2
                        .addLore("")//3 =до сброса дневной статы= обновление каждую секунду в ProfileManager
                        .addLore("")
                        .addLore(op.menu.section==section ? "" : "§7ЛКМ - §fразвернуть статистику")
                        .addLore("")
                        .addLore("§fВаша Карма: "+op.getKarmaDisplay()   )
                        .addLore("")
                        .addLore("§7Карма ухудшается при §cпроигрышах§7,")
                        .addLore("§7и улучшается при §aвыиграшах§7.")
                        .addLore("§7Игрок с хорошей кармой в команде")
                        .addLore("§7может вытянуть игру!")
                        .addLore("")
                        .build(), e -> {
                            if (op.menu.section!=section) op.menu.open(op.getPlayer(), section);
                        }
                );
            }
                
            case ДОСТИЖЕНИЯ -> {
                return ClickableItem.of(new ItemBuilder(Material.PLAYER_HEAD)
                        .name(section.item_name)
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
                );
            }
                
            case МИССИИ -> {
                if (op.isGuest) {
                    return ClickableItem.empty(new ItemBuilder(Material.PLAYER_HEAD)
                            .name(section.item_name)
                            .setCustomHeadTexture(section.texture)
                            .addLore("")
                            .addLore("§8Миссии недоступны")
                            .addLore("§8в гостевом режиме")
                            .addLore("")
                            .build()
                    );
                } else {
                    return ClickableItem.of(new ItemBuilder(Material.PLAYER_HEAD)
                            .name(section.item_name)
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
                }
            }
                
           case ДРУЗЬЯ -> {
               if (op.isGuest) {
                   return ClickableItem.empty(new ItemBuilder(Material.PLAYER_HEAD)
                           .name(section.item_name)
                           .setCustomHeadTexture(section.texture)
                           .addLore("")
                           .addLore("§8Друзья недоступны")
                           .addLore("§8в гостевом режиме")
                           .addLore("")
                           .build()
                   );
               } else {
                   return ClickableItem.of(new ItemBuilder(Material.PLAYER_HEAD)
                           .name(section.item_name)
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
                                   default -> {}
                               }
                           }
                   );
               }
            }
                
           case КОМАНДА -> {
               return ClickableItem.of(new ItemBuilder(Material.PLAYER_HEAD)
                       .name(section.item_name)
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
                               case LEFT:
                                   Friends.openPartyMain(op); //открыть безусловно (для обновления списка и выходя из режима поиска)
                                   break;
                               case RIGHT:
                                   Friends.openPartySettings(op); //открыть безусловно (для обновления списка и выходя из режима поиска)
                                   break;
                               default:
                                   break;
                           }
                       }
               );
            }
                
                
           case ГРУППЫ -> {
               return ClickableItem.of(new ItemBuilder(Material.PLAYER_HEAD)
                       .name(section.item_name)
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
               );
            }
                

            default -> {
                return ClickableItem.empty(new ItemBuilder(Material.PLAYER_HEAD)
                        .name(section.item_name)
                        .setCustomHeadTexture(section.texture)
                        //  .addLore( section.lore)
                        .build());
            }
        }


    }
    
    
    
    
    
    
}
