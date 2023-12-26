package ru.komiss77.modules.quests;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.bukkit.Material;

import net.kyori.adventure.bossbar.BossBar.Color;
import net.kyori.adventure.text.Component;
import ru.komiss77.modules.quests.progs.BlnProg;
import ru.komiss77.modules.quests.progs.IProgress;
import ru.komiss77.modules.quests.progs.NumProg;
import ru.komiss77.modules.quests.progs.VarProg;
import ru.komiss77.objects.CaseInsensitiveMap;
import ru.komiss77.utils.ItemUtils;


public class Quest {
    
//смещения работают относительно parent
    
//                             смещение Х 
//                                  | смещение Y                      одна буква-код задания, 
//                                  |    |  требуемое колл-во          или название кубоида
//                                  |    |   |                     после которого станет видимым
    /*DiscoverAllArea         ('a',   3,   0, 13, Material.COMPASS,		"HeavyFoot",        "Открыть все Локации",		"Исследуйте всю территорию лобби : 4рил", 4),//+при входе в новую зону сверяется размер изученных и существующих
    PandoraLuck             ('b',  -1,  -4,  0, Material.SPONGE,			"spawn",      	"Удача Пандоры", 	        "Испытайте свою удачу в Разломе Пандоры : 2рил", 2),//+ чекается при выходе из кубоида пандоры
    OpenTreassureChest      ('c',  -2,   0,  0, Material.ENDER_CHEST,           "PandoraLuck", 	"Открыть Сундук Сокровищ", 	"Получите примочки из Сундука Сокровищ : 4рил", 4), //+по эвенту косметики открытие сундука
    GreetNewBie             ('d',  -1,   4,  0, Material.QUARTZ,			"spawn",        	"Поприветствовать Новичка", "Нажмите ПКМ на нового игрока", 0),
    SpeakWithNPC            ('e',   2,   0,  0, Material.GLOBE_BANNER_PATTERN,  "newbie",       	"Разговорить Лоцмана", 	"Выведайте куда вы прибыли у Лоцмана", 0),
    ReachSpawn              ('f',   1,   2,  0, Material.ENDER_PEARL,           "newbie",       	"Добраться до Спавна", 	"Нажмите на Джина для перемещения на спавн", 0),//+при входе в зону спавн
    MiniRace                ('g',   2,  -1,  0, Material.TURTLE_HELMET,         "nopvp",       		"Олимпиада", 		"Пройти состязание менее чем за 5 минут", 0),
    MiniPark                ('h',   2,  -1,  0, Material.SMALL_DRIPLEAF,		"parkur",       	"Прыжок за Прыжком", 		"Пропрыгать 12+ блоков на мини-паркурах", 0),
    CobbleGen               ('i',   2,  -1, 12, Material.COBBLESTONE,           "skyworld",       	"Прокачка Острова", 		"Выкопать 12 булыжника в генераторе", 0),
    FindBlock               ('j',   2,  -1, 50, Material.NETHERITE_BLOCK,	"arcaim",       	"Юный Майнкрафтолог", 		"Найти 50 различных блоков в лобби", 0),
    MineDiam                ('k',   2,   1, 10, Material.DIAMOND,		"daaria",       	"Зазнавшийся Шахтер", 		"Добыть 10 алмазов в шахте", 0),
    CollectTax              ('l',   2,   1, 13, Material.RAW_GOLD,		"midgard",       	"Казначей",                 "Собрать золотых с жителей поселка", 0),
    KillMobs                ('v',   2,   1,  8, Material.REDSTONE,		"sedna",       	"Бардовый Воин",            "Убить 8 мертвецов из спавнера", 0),
    SumoVoid           	    ('m',   2,   1,  0, Material.SHULKER_SHELL,         "pvp",       	"Сумо Мастер",              "Сбить человека с сумо платформы", 0),
    SpawnGin          	    ('n',   3,  -2,  0, Material.BLAZE_ROD,             "newbie",  	"Раб Лампы",                "Освободи Джина из лампы", 0),
    OpenAdvancements        ('o',   2,   2,  0, Material.BOOKSHELF,             "newbie",      	"Грамотность",              "Посмотреть меню Квестов", 0),
    
    Navigation              ('p',   3,   0,  0, Material.ARROW,                 "spawn",      	"Навигатор",                "Навести компас на цель (клик на название в меню локаций)", 0),
    HeavyFoot               ('q',   2,   0,  0, Material.IRON_BOOTS,            "Navigation",     	"Тяжелая поступь",          "Совершите перемещение с помощью плиты", 0),
    Elytra                  ('r',  -2,   0,  0, Material.WRITTEN_BOOK,          "GreetNewBie",    	"Доктор Географ. Наук",     "Выполни все задания в Лобби : 8рил", 8),

    Passport                ('s',  2,   0,  50, Material.PAPER,                 "DiscoverAllArea",  "Гражданин Острова",    "Заполни более 50% полей Паспорта Островитянина", 0),
    TalkAllNpc              ('t', -2,   0,  10, Material.BOOKSHELF,             "OpenTreassureChest","Комерческий Агент",    "Поговори со всеми НПС : 4рил", 4),
    FirstMission            ('u',  0,   2,   0, Material.GOLD_INGOT,            "GreetNewBie",      "Путь к Успеху",        "Прими первую Миссию : 2рил", 2),
    ;*/
    //Навести компас на цель (или ТП, если всё открыто) в меню локаций

    
    protected static final Map<Character,Quest> codeMap = new HashMap<>();
    protected static final Map<String,Quest> nameMap = new CaseInsensitiveMap<>();
    protected static final Map<Quest,List<Component>> loreMap = new HashMap<>();
    
    public final char code; //только для загрузки/сохранения!
    public final int amount;
    public final Material icon;
    public final String displayName;
    public final String description;
    public final String backGround;
    public final QuestVis vision;
    public final QuestFrame frame;
    public final Comparable<?>[] needs;
    public final Quest parent;
//    public final Quest root;
    public final int pay;
    
    public Quest[] children;
    public float dx, dy;
    public int size;
    
    //с квестами связано
    //public static final Map<String,Integer>racePlayers = new HashMap<>();
    
    
    public <G extends Comparable<?>> Quest (final char code, final Material icon, final int amount, 
    	final @Nullable G[] needs, final Quest parent, final String displayName, final String description, 
    	final String backGround, final QuestVis vision, final QuestFrame frame, final int pay) {
    	
        this.code = code;
        this.icon = icon;
        this.amount = amount;
        this.parent = parent == null ? this : parent;
        this.displayName = displayName;
        this.description = description;
        this.backGround = backGround;
        this.vision = vision;
        this.frame = frame;
        this.needs = needs;
        this.pay = pay;
        
        children = new Quest[0];
        dx = 0f; dy = 0f;
        size = 1;
        
        codeMap.put(code, this);
        nameMap.put(displayName, this);
        loreMap.put(this, ItemUtils.genLore(null, description));
        
//        Quest rq = this;
//        while (rq.code != ((rq = rq.parent).code));
//        root = rq;
    }
    
    public IProgress createPrg(final int prg) {
    	if (needs != null) return new VarProg(prg, needs);
    	else if (amount == 0) return new BlnProg(prg);
    	else return new NumProg(prg, amount);
    }
    
    @Override
	public boolean equals(final Object o) {
		return o instanceof Quest && ((Quest) o).code == code;
	}
    
    @Override
	public int hashCode() {
		return code;
	}
    
    public enum QuestVis {
    	ALWAYS, PARENT, HIDDEN;
    }
    
    public enum QuestFrame {
    	TASK, GOAL, CHALLENGE;
    }

	public Color getBBColor() {
		switch (frame) {
		case CHALLENGE:
			return Color.PINK;
		case GOAL:
			return Color.BLUE;
		default:
			return Color.YELLOW;
		}
	}
	
	@Override
		public String toString() {
			return displayName + ", n=" + amount + ", dx/dy=" + dx + "/" + dy + ", chs=" + children.length + ", sz=" + size;
		}

}
