package ru.komiss77.modules.enchants;

import io.papermc.paper.enchantments.EnchantmentRarity;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.EntityCategory;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import ru.komiss77.Ostrov;
import ru.komiss77.modules.items.ItemClass;
import ru.komiss77.utils.TCUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class CustomEnchant extends Enchantment {
    
    public static final Map<NamespacedKey, CustomEnchant> VALUES = new HashMap<>();
    
    /*public static final ItemClass RANGED_OTHER = new ItemClass("RANGED_OTHER", 
    	Material.BOW, Material.CROSSBOW, Material.TRIDENT, Material.IRON_HOE, Material.GOLDEN_HOE, 
    	Material.DIAMOND_HOE, Material.NETHERITE_HOE, Material.STONE_HOE, Material.WOODEN_HOE);
    
    public static final ItemClass ARMOR_SHIELD = new ItemClass("ARMOR_SHIELD", Material.DIAMOND_HELMET, 
		Material.DIAMOND_CHESTPLATE, Material.DIAMOND_LEGGINGS, Material.DIAMOND_BOOTS,
		Material.GOLDEN_HELMET, Material.GOLDEN_CHESTPLATE, Material.GOLDEN_LEGGINGS,
		Material.GOLDEN_BOOTS, Material.IRON_HELMET, Material.IRON_CHESTPLATE,
		Material.IRON_LEGGINGS, Material.IRON_BOOTS, Material.NETHERITE_HELMET,
		Material.NETHERITE_CHESTPLATE, Material.NETHERITE_LEGGINGS, Material.NETHERITE_BOOTS,
		Material.LEATHER_HELMET, Material.LEATHER_CHESTPLATE, Material.LEATHER_LEGGINGS,
		Material.LEATHER_BOOTS, Material.TURTLE_HELMET, Material.CHAINMAIL_HELMET,
		Material.CHAINMAIL_CHESTPLATE, Material.CHAINMAIL_LEGGINGS, Material.CHAINMAIL_BOOTS, Material.SHIELD);
    
    public static final ItemClass CHEST_SHIELD = new ItemClass("CHEST_SHIELD", 
		Material.DIAMOND_CHESTPLATE, Material.GOLDEN_CHESTPLATE, Material.IRON_CHESTPLATE, 
		Material.NETHERITE_CHESTPLATE, Material.LEATHER_CHESTPLATE, Material.CHAINMAIL_CHESTPLATE, Material.SHIELD);
    
    public static final ItemClass LEGGINGS = new ItemClass("LEGGINGS", 
		Material.DIAMOND_LEGGINGS, Material.GOLDEN_LEGGINGS, Material.CHAINMAIL_LEGGINGS, 
		Material.IRON_LEGGINGS, Material.NETHERITE_LEGGINGS, Material.LEATHER_LEGGINGS);
    
    //ближнее
    public static final CustomEnchant FREEZE = new CustomEnchant("freeze", "Заморозка", (byte) 3,
        ItemClass.MELEE_AXE, new Enchantment[]{FIRE_ASPECT}, EnchantmentRarity.RARE, false, true, true, true);

    public static final CustomEnchant VAMPIRISM = new CustomEnchant("vampirism", "Вампиризм", (byte) 3,
        ItemClass.MELEE_AXE, new Enchantment[]{}, EnchantmentRarity.UNCOMMON, false, true, true, true);

    public static final CustomEnchant PROPAGATION = new CustomEnchant("propagation", "Разведение", (byte) 1,
        ItemClass.MELEE, new Enchantment[]{}, EnchantmentRarity.VERY_RARE, false, true, true, true);

    public static final CustomEnchant DAMAGE_ILLAGERS = new CustomEnchant("antillager", "Лекарьство", (byte) 5,
        ItemClass.MELEE_AXE, new Enchantment[]{DAMAGE_ALL, DAMAGE_ARTHROPODS, DAMAGE_UNDEAD, IMPALING},
        EnchantmentRarity.COMMON, false, true, true, true);

    public static final CustomEnchant PHANTOMIC = new CustomEnchant("phantomic", "Туманность", (byte) 6,
        ItemClass.MELEE_AXE, new Enchantment[]{}, EnchantmentRarity.RARE, false, true, true, true);

    public static final CustomEnchant NIMBLE = new CustomEnchant("nimble", "Проворность", (byte) 3,
        ItemClass.MELEE_TOOL, new Enchantment[]{}, EnchantmentRarity.UNCOMMON, false, true, true, true);
    //дальнее
    public static final CustomEnchant AEROWDYNAMIC = new CustomEnchant("aerowdynamic", "Аэродинамика", (byte) 4,
        RANGED_OTHER, new Enchantment[]{}, EnchantmentRarity.UNCOMMON, false, true, true, true);

    public static final CustomEnchant SPECTRAL = new CustomEnchant("spectral", "Спектралия", (byte) 3,
        ItemClass.RANGED, new Enchantment[]{MULTISHOT}, EnchantmentRarity.RARE, false, true, true, true);

    public static final CustomEnchant DISCHARGE = new CustomEnchant("discharge", "Разряд", (byte) 3,
        ItemClass.RANGED, new Enchantment[]{}, EnchantmentRarity.COMMON, false, true, true, true);

    public static final CustomEnchant REINSTATION = new CustomEnchant("reinstation", "Реституция", (byte) 5,
        ItemClass.RANGED, new Enchantment[]{}, EnchantmentRarity.VERY_RARE, false, true, true, true);

    public static final CustomEnchant BALOON = new CustomEnchant("baloon", "Шарик", (byte) 2,
        ItemClass.RANGED, new Enchantment[]{DISCHARGE}, EnchantmentRarity.RARE, false, true, true, true);

    public static final CustomEnchant AQUATIC = new CustomEnchant("aquatic", "Сырость", (byte) 5,
        RANGED_OTHER, new Enchantment[]{ARROW_DAMAGE, DAMAGE_ILLAGERS, IMPALING}, EnchantmentRarity.UNCOMMON, false, true, true, true);
    //броня
    public static final CustomEnchant REPULTION = new CustomEnchant("repultion", "Репульсия", (byte) 3,
        CHEST_SHIELD, new Enchantment[]{THORNS}, EnchantmentRarity.RARE, false, true, true, true);

    public static final CustomEnchant PROTECTION_POTIONS = new CustomEnchant("magic_protection", "Анти-Магия", (byte) 4,
        ItemClass.ARMOR, new Enchantment[]{PROTECTION_ENVIRONMENTAL, PROTECTION_EXPLOSIONS, PROTECTION_FALL,
            PROTECTION_FIRE, PROTECTION_PROJECTILE}, EnchantmentRarity.UNCOMMON, false, true, true, true);

    public static final CustomEnchant WITHERED = new CustomEnchant("withered", "Иссушение", (byte) 3,
        ARMOR_SHIELD, new Enchantment[]{}, EnchantmentRarity.VERY_RARE, false, true, true, true);

    public static final CustomEnchant RESTORATION = new CustomEnchant("restoration", "Обновление", (byte) 5,
        ItemClass.ARMOR, new Enchantment[]{}, EnchantmentRarity.RARE, false, true, true, true);

    public static final CustomEnchant NINJA = new CustomEnchant("ninja", "Ниндзя", (byte) 2,
        LEGGINGS, new Enchantment[]{}, EnchantmentRarity.UNCOMMON, false, true, true, true);

    public static final CustomEnchant VEINING = new CustomEnchant("veining", "Венозность", (byte) 3,
        ItemClass.TOOL, new Enchantment[]{}, EnchantmentRarity.RARE, false, true, true, true);

    public static final CustomEnchant EXPANSION = new CustomEnchant("expansion", "Экспансия", (byte) 2,
        ItemClass.TOOL, new Enchantment[]{VEINING}, EnchantmentRarity.UNCOMMON, false, true, true, true);

    public static final CustomEnchant REPARATION = new CustomEnchant("reparation", "Репарация", (byte) 4,
        ItemClass.TOOL, new Enchantment[]{}, EnchantmentRarity.VERY_RARE, false, true, true, true);

    public static final CustomEnchant SMELTING_TOUCH = new CustomEnchant("smelting_touch", "Переплавка", (byte) 1,
        ItemClass.TOOL, new Enchantment[]{SILK_TOUCH}, EnchantmentRarity.UNCOMMON, false, true, true, true);
    //проклятья
    public static final CustomEnchant LAGGING_CURSE = new CustomEnchant("curse_of_lagging", "Проклятие Лагов", (byte) 1,
        ItemClass.ALL, new Enchantment[]{}, EnchantmentRarity.UNCOMMON, true, true, true, true);

    public static final CustomEnchant FRAGMENT_CURSE = new CustomEnchant("curse_of_fragmentation", "Проклятие Дробления", (byte) 1,
        ItemClass.ALL, new Enchantment[]{}, EnchantmentRarity.COMMON, true, true, true, true);

    public static final CustomEnchant THE_DEAD_CURSE = new CustomEnchant("curse_of_the_dead", "Проклятие Мертвых", (byte) 1,
        ItemClass.ALL, new Enchantment[]{}, EnchantmentRarity.VERY_RARE, true, true, true, true);

    public static final CustomEnchant ROTTEN_CURSE = new CustomEnchant("curse_of_rotting", "Проклятие Гниения", (byte) 1,
        ItemClass.ALL, new Enchantment[]{}, EnchantmentRarity.RARE, true, true, true, true);*/

    public static final CustomEnchant GLINT = new CustomEnchant("glint", "", (byte) 1, (byte) 1,
		ItemClass.ALL, new Enchantment[]{}, new EnchantInfo(null, 0), EnchantmentRarity.COMMON, 
		false, false, false, false, e -> {}, e -> {}, e -> {}, e -> {}, e -> {});
    
//    public static final List<Enchantment> css = Arrays.asList(VANISHING_CURSE, BINDING_CURSE,
//            LAGGING_CURSE, FRAGMENT_CURSE, THE_DEAD_CURSE, ROTTEN_CURSE);

//    public static List<Enchantment> getCrss() {
//        Ostrov.async(() -> Collections.shuffle(css));
//        return css;
//    }
//    	openRegister();

    	/*enchInfo.put(PHANTOMIC, new EnchantInfo("Туманность", 1, "Удары при низком освещении", "увеличивают урон оружия"));
    	enchInfo.put(AQUATIC, new EnchantInfo("Сырость", 1, "Нанасит больше вреда мобам,", "отторгающим или ненавидящим воду"));
    	enchInfo.put(ARROW_DAMAGE, new EnchantInfo("Сила", 2, "Наносит больший урон всеми", "видами стрел"));
    	enchInfo.put(DAMAGE_ALL, new EnchantInfo("Острота", 2, "Наносит больший урон всеми", "видами ближних орудий"));
    	enchInfo.put(DAMAGE_ARTHROPODS, new EnchantInfo("Бич членистоногих", 1, "Наносит больше урона всем", "видам пауков и чешуйниц"));
    	enchInfo.put(DAMAGE_ILLAGERS, new EnchantInfo("Лекарьство", 1, "Наносит больше урона всем", "видам илладжеров"));
    	enchInfo.put(DAMAGE_UNDEAD, new EnchantInfo("Небесная кара", 2, "Наносит больше урона всем", "типам нежити"));
    	enchInfo.put(DIG_SPEED, new EnchantInfo("Эффективность", 2, "Прибавляет скорость добычи", "материалов, зависимо от инструмента"));
    	enchInfo.put(IMPALING, new EnchantInfo("Пронзатель", 1, "Наносит больше урона всем", "подводным созданиям"));
    	enchInfo.put(REINSTATION, new EnchantInfo("Реституция", 1, "Дает шанс сохранить стрелу", "при выстреле из дальнего оружия"));
    	enchInfo.put(RESTORATION, new EnchantInfo("Обновление", 3, "Дает регенерацию сразу после", "получения урона, на короткое время"));

    	enchInfo.put(ARROW_KNOCKBACK, new EnchantInfo("Откидывание", 1680, "Откидывает цели снарядами", "при попадании"));
    	enchInfo.put(EXPANSION, new EnchantInfo("Экспансия", 1800, "Увеличивает радиус выкопки", "различных материалов"));
    	enchInfo.put(FIRE_ASPECT, new EnchantInfo("Заговор огня", 1640, "Поджигает цель при ударе", "на несколько секунд"));
    	enchInfo.put(FROST_WALKER, new EnchantInfo("Ледоход", 1720, "Превращает воду в лед", "при ходьбе по ней"));
    	enchInfo.put(KNOCKBACK, new EnchantInfo("Отдача", 1680, "Откидывает цели при", "ближних ударах"));
    	enchInfo.put(NINJA, new EnchantInfo("Ниндзя", 1760, "Снижает шанс что тебя заметят", "монстры при передвижении"));
    	enchInfo.put(BALOON, new EnchantInfo("Шарик", 1760, "Приклепляет шарик при попадании,", "дает левитацию на пару секунд"));

    	enchInfo.put(DISCHARGE, new EnchantInfo("Разряд", 220, "Взрывает снаряд при попадании", "по существу или блоку"));
    	enchInfo.put(DURABILITY, new EnchantInfo("Прочность", 240, "Делает вещь более устойчивой", "к стрессу при работе"));
    	enchInfo.put(FREEZE, new EnchantInfo("Заморозка", 240, "Замораживает цель в глыбу", "льда при ударе"));
    	enchInfo.put(DEPTH_STRIDER, new EnchantInfo("Подводная ходьба", 200, "Позволяет быстрее плавать и", "ходить по дну водных тел"));
    	enchInfo.put(LOOT_BONUS_BLOCKS, new EnchantInfo("Удача", 260, "Дает шанс добыть больше ресурсов", "с конкретных материалов"));
    	enchInfo.put(LOOT_BONUS_MOBS, new EnchantInfo("Добыча", 260, "Дает шанс добыть больше", "ресурсов при убийстве мобов"));
    	enchInfo.put(LOYALTY, new EnchantInfo("Верность", 240, "Возвращает вам трезубец после", "попадания по блоку или мобу"));
    	enchInfo.put(LUCK, new EnchantInfo("Везучий рыбак", 200, "Увеличивает шанс выловить что-то", "интересное при рыбалке удочкой"));
    	enchInfo.put(LURE, new EnchantInfo("Приманка", 180, "Приманка на удочке уменьшает", "время, траченое на ловлю рыбы"));
    	enchInfo.put(OXYGEN, new EnchantInfo("Подводное дыхание", 220, "Сохраняет больше воздуха при", "погружении под воду"));
    	enchInfo.put(QUICK_CHARGE, new EnchantInfo("Быстрая перезарядка", 240, "Перезаряжает дальнее оружие", "немного быстрее"));
    	enchInfo.put(REPULTION, new EnchantInfo("Репульсия", 240, "Шанс откинуть алакующего или", "его снаряд, при получении урона"));
    	enchInfo.put(RIPTIDE, new EnchantInfo("Тягун", 260, "Позволяет быстро перемещаться", "в воде и во время дождя"));
    	enchInfo.put(NIMBLE, new EnchantInfo("Проворность", 240, "Уменьшает перезарядку инструмента", "перед след. использованием"));
    	enchInfo.put(SOUL_SPEED, new EnchantInfo("Скорость души", 200, "Позволяет быстрее перемещаться", "по песку и почве душ"));
    	enchInfo.put(SWIFT_SNEAK, new EnchantInfo("Проворство", 220, "Позволяет быстрее перемещаться", "присев (Шифт) в стелс режиме"));
    	enchInfo.put(SPECTRAL, new EnchantInfo("Спектралия", 260, "Шанс выстрелить дополнительную", "стрелу из лука или арбалета"));
    	enchInfo.put(SWEEPING_EDGE, new EnchantInfo("Разящий клинок", 220, "Наносит большую часть от", "основного урона, мобам вокруг"));
    	enchInfo.put(THORNS, new EnchantInfo("Шипы", 220, "Возвращает часть урона", "атакующему существу"));
    	enchInfo.put(VAMPIRISM, new EnchantInfo("Вампиризм", 240, "Возобновляет часть нанесенного", "урона как здоровье атакующему"));
    	enchInfo.put(VEINING, new EnchantInfo("Венозность", 240, "Позволяет выкапывать ближайние", "блоки одинакового типа"));
    	enchInfo.put(WITHERED, new EnchantInfo("Иссушение", 220, "Иссушает оппонента при", "ударе по броне"));

    	enchInfo.put(AEROWDYNAMIC, new EnchantInfo("Аэродинамика", 26, "Добавляет скорость, урон, и", "дистанцию всем снарядам"));
    	enchInfo.put(PIERCING, new EnchantInfo("Пронзающая стрела", 24, "Позволяет пронзить несколько", "поверхностей за выстрел"));
    	enchInfo.put(PROTECTION_ENVIRONMENTAL, new EnchantInfo("Защита", 28, "Дает больше защиты от", "всех типов урона"));
    	enchInfo.put(PROTECTION_EXPLOSIONS, new EnchantInfo("Взрывоустойчивость", 24, "Дает больше защиты от", "взрывов и разрывного урона"));
    	enchInfo.put(PROTECTION_FALL, new EnchantInfo("Невесомость", 26, "Дает больше защиты от урона,", "нанесенном при падении"));
    	enchInfo.put(PROTECTION_FIRE, new EnchantInfo("Огнеупорность", 24, "Дарует больше защиты от урона,", "при горении в огне и лаве"));
    	enchInfo.put(PROTECTION_POTIONS, new EnchantInfo("Анти-Магия", 24, "Защищает больше от вреда", "магии и зелий урона"));
    	enchInfo.put(PROTECTION_PROJECTILE, new EnchantInfo("Защита от снарядов", 26, "Дает больше защиты от всех", "типов дальних орудий"));
    	enchInfo.put(REPARATION, new EnchantInfo("Репарация", 28, "Шанс не потратить прочность", "при выкопке подходящего ресурса"));

    	enchInfo.put(ARROW_FIRE, new EnchantInfo("Воспламенение", 6800, "Поджег цели при попадании", "из дальнего оружия"));
    	enchInfo.put(CHANNELING, new EnchantInfo("Громовержец", 6400, "Призывает молнию при попадании,", "во время дождя или грозы"));
    	enchInfo.put(MULTISHOT, new EnchantInfo("Тройной выстрел", 7600, "Позволяет выстреливать три", "стрелы за раз, из арбалета"));
    	enchInfo.put(PROPAGATION, new EnchantInfo("Разведение", 7200, "Позволяет использовать зачарования", "предмета на все задетые цели"));
    	enchInfo.put(SILK_TOUCH, new EnchantInfo("Шёлковое касание", 7600, "Позволяет выкапывать некоторые", "блоки в их начальном виде"));
    	enchInfo.put(SMELTING_TOUCH, new EnchantInfo("Переплавка", 7200, "Переплавляет все плавимые", "ресурсы в их итоговый предмет"));
    	enchInfo.put(WATER_WORKER, new EnchantInfo("Подводник", 6800, "Дает возможность копать блоки", "под водой намного быстрее"));
        */

    private final String nm;
    private final NamespacedKey key;
    private final byte mxlvl;
    private final byte chance;
    private final ItemClass its;
    private final Enchantment[] cnfls;
    private final EnchantInfo info;
    private final EnchantmentRarity rrt;
    private final boolean isCrsd;
    private final boolean isTrsr;
    private final boolean isTrdbl;
    private final boolean isDscv;
    private final Consumer<EntityDamageByEntityEvent> onHit;
    private final Consumer<EntityDamageEvent> onArm;
    private final Consumer<ProjectileHitEvent> onPrj;
    private final Consumer<EntityShootBowEvent> onSht;
    private final Consumer<BlockBreakEvent> onBrk;
    
    public CustomEnchant(final String nmkey,
            final String nm,
            final byte mxlvl,
            final byte chance,
            final ItemClass its,
            final Enchantment[] cnfls,
            final EnchantInfo info,
            final EnchantmentRarity rrt,
            final boolean isCrsd,
            final boolean isTrsr,
            final boolean isTrdbl,
            final boolean isDscv,
            final Consumer<EntityDamageByEntityEvent> onHit,
            final Consumer<EntityDamageEvent> onArm,
            final Consumer<ProjectileHitEvent> onPrj,
            final Consumer<EntityShootBowEvent> onSht,
            final Consumer<BlockBreakEvent> onBrk) {
        super();
        this.nm = nm;
        this.mxlvl = mxlvl;
        this.chance = chance;
        this.its = its;
        this.cnfls = cnfls;
        this.info = info;
        this.rrt = rrt;
        this.isCrsd = isCrsd;
        this.isTrsr = isTrsr;
        this.isTrdbl = isTrdbl;
        this.isDscv = isDscv;
        this.onHit = onHit;
        this.onArm = onArm;
        this.onPrj = onPrj;
        this.onSht = onSht;
        this.onBrk = onBrk;

        this.key = NamespacedKey.minecraft(nmkey);
        if (VALUES.put(key, this) == null) {
            Ostrov.log_warn("Enchant " + nmkey + " is already registered!");
        }
    }
    
    @Override
    public String getName() {
        return nm;
    }
    
    public EnchantInfo getInfo() {
        return info;
    }
    
    public int getChance() {
        return Math.max(chance, 1);
    }
    
    @Override
    public boolean canEnchantItem(final ItemStack it) {
        return it != null && (its.equals(ItemClass.ALL) || its.has(it.getType()));
    }
    
    @Override
    public boolean conflictsWith(final Enchantment en) {
        final NamespacedKey k = en.getKey();
        for (final Enchantment e : cnfls) {
            if (k.equals(e.getKey())) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.BREAKABLE;
    }
    
    @Override
    public int getMaxLevel() {
        return mxlvl;
    }
    
    @Override
    public int getStartLevel() {
        return 1;
    }

    @Override
    public boolean isCursed() {
        return isCrsd;
    }

    @Override
    public boolean isTreasure() {
        return isTrsr;
    }

    @Override
    public String translationKey() {
        return "enchantment.minecraft." + getKey().getKey();
    }

    @Override
    public Component displayName(final int lvl) {
        final StringBuilder sb = new StringBuilder((isCrsd ? "§c" : "§7") + nm + " ");
        switch (lvl) {
            case 1:
                sb.append(getMaxLevel() == 1 ? "" : "I");
                break;
            case 2:
                sb.append("II");
                break;
            case 3:
                sb.append("III");
                break;
            case 4:
                sb.append("IV");
                break;
            case 5:
                sb.append("V");
                break;
            case 6:
                sb.append("VI");
                break;
            case 7:
                sb.append("VII");
                break;
            case 8:
                sb.append("VIII");
                break;
            case 9:
                sb.append("IX");
                break;
            case 10:
                sb.append("X");
                break;
            default:
                sb.append(lvl);
                break;
        }
        return TCUtils.format(sb.toString());
    }

    public static CustomEnchant getByKey(final NamespacedKey key) {
        return VALUES.get(key);
    }

    @Override
    public Set<EquipmentSlot> getActiveSlots() {
        return Set.of(EquipmentSlot.values());
    }

    @Override
    public float getDamageIncrease(final int lvl, final EntityCategory ec) {
        return 0;
    }

    @Override
    public EnchantmentRarity getRarity() {
        return rrt;
    }

    @Override
    public boolean isDiscoverable() {
        return isDscv;
    }

    @Override
    public int getMinModifiedCost(final int lvl) {
        return 1 + ((lvl - 1) << 5) / getMaxLevel();
    }

    @Override
    public int getMaxModifiedCost(final int lvl) {
        return 11 + ((lvl - 1) << 6) / getMaxLevel();
    }

    @Override
    public boolean isTradeable() {
        return isTrdbl;
    }
    
    public static CustomEnchant[] values() {
        return VALUES.values().toArray(new CustomEnchant[0]);
    }
    
    public boolean noCnflcts(final Map<Enchantment, Integer> ens) {
        for (final Enchantment e : ens.keySet()) {
            if (conflictsWith(e)) return false;
        }
        return true;
    }
    
    public Consumer<EntityDamageByEntityEvent> getOnHit() {
        return onHit;
    }
    
    public Consumer<EntityDamageEvent> getOnArm() {
        return onArm;
    }
    
    public Consumer<ProjectileHitEvent> getOnPrj() {
        return onPrj;
    }
    
    public Consumer<EntityShootBowEvent> getOnSht() {
        return onSht;
    }
    
    public Consumer<BlockBreakEvent> getOnBrk() {
        return onBrk;
    }

    @Override
    public @NotNull NamespacedKey getKey() {
        return key;
    }
}
