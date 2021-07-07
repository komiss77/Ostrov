package ru.komiss77.modules.player.profile;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import ru.komiss77.ApiOstrov;
import ru.komiss77.enums.Data;
import ru.komiss77.enums.Stat;
import ru.komiss77.modules.player.Oplayer;

import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.ItemUtils;
import ru.komiss77.utils.inventory.ClickableItem;

//http://textures.minecraft.net/texture/be3db27cbd1789310409081ad8c42d690b08961b55cadd45b42d46bca28b8
//be3db27cbd1789310409081ad8c42d690b08961b55cadd45b42d46bca28b8 телик
//f7c7df52b5e50badb61fed7212d979e63fe94f1bde02b2968c6b156a770126c аптечка
//8a99342e2c73a9f3822628e796488234f258446f5a2d4d59dde4aa87db98 да
//16c60da414bf037159c8be8d09a8ecb919bf89a1a21501b5b2ea75963918b7b нет
//f2599bd986659b8ce2c4988525c94e19ddd39fad08a38284a197f1b70675acc < c2f910c47da042e4aa28af6cc81cf48ac6caf37dab35f88db993accb9dfe516 > кварц+зел стрелка
//5f133e91919db0acefdc272d67fd87b4be88dc44a958958824474e21e06d53e6 < e3fc52264d8ad9e654f415bef01a23947edbccccf649373289bea4d149541f70 > кварц+чёрн стрелка
//eed78822576317b048eea92227cd85f7afcc44148dcb832733baccb8eb56fa1 715445da16fab67fcd827f71bae9c1d2f90c73eb2c1bd1ef8d8396cd8e8 <> блок дуба
//https://minecraft-heads.com/custom-heads/miscellaneous/39696-star звезда
public enum Section {

    ПРОФИЛЬ (45,"§fПрофиль", "", "362a1b473ba3c25e6eb1458dfc953921f5d91478af9d6dda07c032f928c", Material.RED_STAINED_GLASS_PANE),
    СТАТИСТИКА (46,"§fСтатистика", "", "a683dae6d132f52efa55e98c181def5403672fdec2cbcf4793142cb0e59bd0", Material.RED_STAINED_GLASS_PANE),
    ДОСТИЖЕНИЯ (47,"§fДостижения", "", "a8fa562856959dfefbc1328cdfd9d430b65af7f24cf326400767305b34c5b2e5", Material.CYAN_STAINED_GLASS_PANE), 
    
    ЗАДАНИЯ(48,"§bЗадания", "", "cf40942f364f6cbceffcf1151796410286a48b1aeba77243e218026c09cd1", Material.ORANGE_STAINED_GLASS_PANE), 
    ДРУЗЬЯ(49,"§aДрузья", "§aЛКМ §7- друзья<br>§aПКМ §7- настройки друзей<br>§cShift+Клик §7- режим удаления друзей<br><br>Добавить друга-клик головой<br>или команда §6/fr add ник", "3228db8b2cd49a4b986a3b0ea8a161a1742b1c07fd0e2b15be88dc76498e5", Material.PURPLE_STAINED_GLASS_PANE), 
    КОМАНДА(50,"§5Команда", "§aЛКМ §7- участники<br>§aПКМ §7- настройки команды<br>§cShift+Клик §7- режим удаления участников<br><br>Создать команду-§6/party create", "2c2be3bc607773264ceee0edd615316341ec2b465a16cdb578bdaedd643d", Material.GREEN_STAINED_GLASS_PANE), 
    //ДОНАТ(50,"§3Купить привилегии", "§aЛКМ §7- посмотреть привилегии<br>§aПКМ §7- пополнить счёт<br><br>§fНа сервере у Вас есть<br>§fрублёвый счёт (§eрил§f).<br>§fВы можете пополнять его<br>§fчерез платёжную систему,<br>§fи использовать эти средства<br>§fдля покупки привилегий, обмена<br>§fна игровую валюту (§eлони§f)<br>§fи прочее.", "d295a929236c1779eab8f57257a86071498a4870196941f4bfe1951e8c6ee21a", Material.CYAN_STAINED_GLASS_PANE), 
    СЕРВЕР(51,"§fВыбор сервера§7/§aPiPBoy", "§fЛКМ §7- Открыть меню выбора<br>§aПКМ §7-Открыть локальное меню", "", Material.BLACK_STAINED_GLASS_PANE), 
    //ПОМОЩЬ(52,"§2Помощь","§aЛКМ §7- по глобальным командам<br>§aПКМ §7- локальные команды<br>§aShift+клик §7- Администрация<br><br>Глобальные команды действуют<br>на всех серверах,<br>Локальные - выполняются только<br>на том сервере, где вы<br>находитесь в данный момент.", "fed4ae757f23445b5c9335cc5a8f7f7c6f9a5aee85bb69fe97f581dafb18d30", Material.BROWN_STAINED_GLASS_PANE), 
    //НАСТРОЙКИ(53,"§6Локальные настройки", "§aЛКМ §7- Настройки для текущего сервера<br>§aПКМ §7- редактировать пасспорт Островитянина", "b86b9d58bcd1a555f93e7d8659159cfd25b8dd6e9bce1e973822824291862", Material.BLACK_STAINED_GLASS_PANE), 
    //ЗАКРЫТЬ(53,"§4Закрыть", "§7Закрыть меню", "", Material.BLACK_STAINED_GLASS_PANE), 
    ;
    
    public int slot;
    public String item_name;
    public String lore;
    public String texture;
    public Material mat;
    
    
    private Section(int slot, String item_name, String lore, String texture, Material mat){
        this.slot = slot;
        this.item_name = item_name;
        this.lore = lore;
        this.texture = texture;
        this.mat = mat;
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
//System.out.println("++++++++++++++++++++++.E_Prof.getItem()");
        //if (profile==ЗАКРЫТЬ)  return new ItemBuilder(Material.BARRIER).setName("§4Закрыть").setLore(ItemUtils.Gen_lore(null, profile.lore, "§7")).build();
        return new ItemBuilder(Material.PLAYER_HEAD)
        .name(section.item_name)
        .setCustomHeadTexture(section.texture)
        .lore(ItemUtils.Gen_lore(null, section.lore, ""))
        .build();//VM.getNmsServer().getCustomHead(profile.texture, profile.item_name, profile.lore);

    }
    
    public static ClickableItem getMenuItem(final Section section, final Oplayer op){
        
        switch (section) {
            case ПРОФИЛЬ:
                return ClickableItem.of(new ItemBuilder(Material.PLAYER_HEAD)
                    .name(section.item_name)
                    .setCustomHeadTexture(section.texture)
                    .lore("") //0
                    .lore("") //1 подставляется =время= .lore( Stat.PLAY_TIME.desc+ApiOstrov.secondToTime(op.getStat(Stat.PLAY_TIME))) 
                    .lore( Stat.LEVEL.desc+op.getStat(Stat.LEVEL) +"  "+ ApiOstrov.getPercentBar(op.getStat(Stat.LEVEL)*25, op.getStat(Stat.EXP), true)  )
                    .lore("")
                    .lore( op.getDataInt(Data.REPORT_C)>0 ? Data.REPORT_C.desc+op.getDataInt(Data.REPORT_C) : "" )
                    .lore( op.getDataInt(Data.REPORT_P)>0 ? Data.REPORT_P.desc+op.getDataInt(Data.REPORT_P) : "" )    
                    .lore("")
                    .lore(op.menu.section==section ? "" : "§7ЛКМ - §fразвернуть профиль")
                    .lore("")
                    .build(), e -> {
                        if (op.menu.section!=section) op.menu.open(op.getPlayer(), section);
                    }
                );
                
            case СТАТИСТИКА:
                return ClickableItem.of(new ItemBuilder(Material.PLAYER_HEAD)
                    .name(section.item_name)
                    .setCustomHeadTexture(section.texture)
                    .lore("") //0
                    .lore("§7Дневня статистика обнуляется в полночь.") //1
                    .lore("§6До сброса дневной статистики:") //2
                    .lore("")//3 подставляется =до сброса=
                    .lore("")
                    .lore(op.menu.section==section ? "" : "§7ЛКМ - §fразвернуть статистику")
                    .lore("")
                    .lore(op.getDataInt(Data.KARMA)==0 ? "§fВаша Карма: §5нейтральная" : (op.getDataInt(Data.KARMA)>=0 ? "§7Ваша Карма: §a+"+op.getDataInt(Data.KARMA) : "§7Ваша Карма: §c-"+op.getDataInt(Data.KARMA))   )
                    .lore("")
                    .lore("§7Карма ухудшается при §cпроигрышах§7,")
                    .lore("§7и улучшается при §aвыиграшах§7.")
                    .lore("§7Игрок с хорошей кармой в команде")
                    .lore("§7может вытянуть игру!")
                    .lore("")
                    .build(), e -> {
                        if (op.menu.section!=section) op.menu.open(op.getPlayer(), section);
                    }
                );
                
            case ДОСТИЖЕНИЯ:
                return ClickableItem.of(new ItemBuilder(Material.PLAYER_HEAD)
                    .name(section.item_name)
                    .setCustomHeadTexture(section.texture)
                    .lore("")
                    .lore("§7Ваши достижения в различных режимах.")
                    .lore("§7Достижения - важная часть развития,")
                    .lore("§7за каждое Вы получаете дополнительный опыт.")
                    .lore("")
                    .lore(op.menu.section==section ? "" : "§7ЛКМ - §fразвернуть достижения")
                    .lore("")
                    .build(), e -> {
                        if (op.menu.section!=section) op.menu.open(op.getPlayer(), section);
                    }
                );
                
                
                default:
                    return ClickableItem.empty(new ItemBuilder(Material.PLAYER_HEAD)
                    .name(section.item_name)
                    .setCustomHeadTexture(section.texture)
                    .lore(ItemUtils.Gen_lore(null, section.lore, ""))
                    .build());
        }


    }
    
    
    
    
    
    
}
