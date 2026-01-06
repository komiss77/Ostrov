package ru.komiss77.listener;


import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import com.destroystokyo.paper.event.player.PlayerStartSpectatingEntityEvent;
import com.destroystokyo.paper.event.player.PlayerStopSpectatingEntityEvent;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.input.SingleOptionDialogInput;
import io.papermc.paper.registry.data.dialog.input.TextDialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import io.papermc.paper.world.damagesource.CombatEntry;
import io.papermc.paper.world.damagesource.CombatTracker;
import net.kyori.adventure.text.event.ClickCallback;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.BlockType;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.*;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemType;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Ostrov;
import ru.komiss77.Timer;
import ru.komiss77.events.PlayerDisguiseEvent;
import ru.komiss77.modules.items.ItemBuilder;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;
import ru.komiss77.utils.ClassUtil;
import ru.komiss77.utils.StringUtil;
import ru.komiss77.utils.TCUtil;
import ru.komiss77.version.Craft;


public class TestLst implements Listener {


  public static final NamespacedKey test = new NamespacedKey("os", "test");//Key.key("faction", "perk");

  @EventHandler(ignoreCancelled = false)
  public void test(final PlayerDisguiseEvent e) {
    switch (e.action) {
      case LeashEvent, DamageEvent, MountEvent, DismountEvent, PickupEvent, SpectateEvent -> {
        ((Cancellable) e.event).setCancelled(false);
      }
    }
  }

  @EventHandler(ignoreCancelled = false)
  public void test(final PlayerInteractEvent e) {
    final Player p = e.getPlayer();
    if (!ApiOstrov.isLocalBuilder(p)) return;
    final Oplayer op = PM.getOplayer(p);

    if (e.getItem() == null) return;
    if (p.getCooldown(e.getItem()) > 0) return;
    p.setCooldown(e.getItem(), 5);//в креативе делает двойной интеракт!

       /*if (p.getGameMode() == GameMode.SPECTATOR) {
            p.sendMessage("§8SPECTATOR "+e.getAction());
            if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {

            } else if (e.getAction() == Action.LEFT_CLICK_AIR) {
                p.sendMessage("");
            }
            return;
        }*/

    if (e.getItem().getType() == Material.WOODEN_PICKAXE) {
      p.sendMessage("§8TestLst onInteract cancel");
      e.setCancelled(true);

      if (e.getAction() == Action.LEFT_CLICK_BLOCK) {
        //p.sendMessage("undis");
        //op.disguise.unDisguise();
        if (p.isSneaking()) {
          //p.sendMessage("LEFT_CLICK_BLOCK");
        } else {

        }
        return;
      }

      if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
        if (p.isSneaking()) {

        } else {
          p.sendMessage("");

//Ostrov.log_warn("nmsLe="+op.disguise.nmsLe);
        }
        return;
      }
      return;
    }


    if (true) return;

        final int time = Timer.secTime();
        final int volts = Ostrov.random.nextInt(MAX_VOLTS);
        final boolean check = Ostrov.random.nextBoolean();
        final WColor color = ClassUtil.rndElmt(COLORS);
        ClassUtil.shuffle(OPTIONS_LONG);
        final Condition cond = ClassUtil.rndElmt(OPTIONS_LONG);
        final String code = genCode(ClassUtil.rndElmt(CODES), cond);
        final Dialog dg = Dialog.create(builder -> builder.empty()
            .base(DialogBase.builder(TCUtil.form("<gradient:light_purple:aqua><bold>Меню Разминировки")).body(List.of(DialogBody.item(new ItemBuilder(ItemType.SHEARS).glint(true).build(),
                    DialogBody.plainMessage(TCUtil.form("<mint>=> <beige>Для Разминировки")), true, false, 16, 16),
                    DialogBody.plainMessage(TCUtil.form("<gold>Поставь правильные настройки и разрежь провода!\n\n<beige><bold>Серийный Код Бомбы:</bold>\n<pink><u>" + code)),
                    DialogBody.plainMessage(TCUtil.form("\n<beige>Выбери Параметр:"))))
                .inputs(List.of(
//                    DialogInput.bool("bool1", TCUtil.form("Bool1"), false, "Tru", "Falc"),
                    DialogInput.singleOption(CODE, 200, LIST_LONG, TCUtil.form("<sky>Код Бомбы"), false),
                    DialogInput.numberRange(VOLTS, 250, TCUtil.form("<white>Настрой кусачки на [<aqua>"
                        + volts + " V<white>]"), "%s, сейчас: %s V", 0f, MAX_VOLTS, (float) (MAX_VOLTS >> 1), 1f),
                    DialogInput.bool(CHECK, TCUtil.form("\n<beige>> " + (check ? "<green>Поставь Галочку" : "<red>Убери Галочку") + "\n")).initial(Ostrov.random.nextBoolean()).build(),
                    DialogInput.text("text", 1, TCUtil.form("<beige>Разрежь <mithril>цвет проводов<beige>, которых <gold>" + (check ? "больше" : "меньше") + " <beige>всего!"),
                        true, "", 1, TextDialogInput.MultilineOptions.create(1, 1)),
                    DialogInput.singleOption("wires", 160, List.of(SingleOptionDialogInput.OptionEntry.create("wires", TCUtil.form(genWires(color, check)), false)
                    ), TCUtil.form("<beige>Провода"), false)
                )).build())
            .type(DialogType.multiAction(List.of(
                ActionButton.builder(TCUtil.form("<red>⎨ <u>Красный</u> ⎬")).tooltip(TCUtil.form("<beige>Клик - Разрезать")).action(genAction(p, WColor.RED, color, cond, check, volts, time)).build(),
                ActionButton.builder(TCUtil.form("<yellow>⎨ <u>Желтый</u> ⎬")).tooltip(TCUtil.form("<beige>Клик - Разрезать")).action(genAction(p, WColor.YELLOW, color, cond, check, volts, time)).build(),
                ActionButton.builder(TCUtil.form("<green>⎨ <u>Зеленый</u> ⎬")).tooltip(TCUtil.form("<beige>Клик - Разрезать")).action(genAction(p, WColor.GREEN, color, cond, check, volts, time)).build(),
                ActionButton.builder(TCUtil.form("<blue>⎨ <u>Синий</u> ⎬")).tooltip(TCUtil.form("<beige>Клик - Разрезать")).action(genAction(p, WColor.BLUE, color, cond, check, volts, time)).build()
            ), null, 2))
        );
    p.showDialog(dg);
    }


    private static DialogAction.CustomClickAction genAction(final Player pl, final WColor choice, final WColor color, final Condition cond, final boolean check, final int volts, final int sec) {
        return DialogAction.customClick((res, au) -> {
            if (color != choice) {
                pl.sendMessage("KABOOM! color " + color.name());
                return;
            }
            final String code = res.getText(CODE);
            if (code == null || cond != Condition.parse(code)) {
                pl.sendMessage("KABOOM! cond " + cond.name());
                return;
            }
            final Boolean chb = res.getBoolean(CHECK);
            if (chb == null || check != chb) {
                pl.sendMessage("KABOOM! check " + check);
                return;
            }
            final Float fvl = res.getFloat(VOLTS);
            if (fvl == null || volts != fvl.intValue()) {
                pl.sendMessage("KABOOM! volts " + volts);
                return;
            }
            pl.sendMessage("Ты разминировал бiмбу! Заняло " + (Timer.secTime() - sec) + "сек");
        }, ClickCallback.Options.builder().uses(1).lifetime(Duration.ofDays(1)).build());
    }

    private String genCode(final String code, final Condition cnd) {
        return switch (cnd) {
            case NUM_SUM -> new StringBuilder(code).insert(Ostrov.random.nextInt(code.length()),
                StringUtil.rndChar(StringUtil.NUMBERS.substring(StringUtil.NUMBERS.length() >> 1))).toString();
            case HAS_UPPER -> new StringBuilder(code).insert(Ostrov.random.nextInt(code.length()), 'C')
                .insert(Ostrov.random.nextInt(code.length()), 'S').toString();
            /*case END_UPPER -> code + StringUtil.rndChar(StringUtil.UPPERS);
            case HAS_LOWER -> new StringBuilder(code).insert(Ostrov.random.nextInt(code.length()), 'a')
                .insert(Ostrov.random.nextInt(code.length()), 'w').insert(Ostrov.random.nextInt(code.length()), 'p').toString();*/
            case IS_LARGE -> new StringBuilder(code).insert(Ostrov.random.nextInt(code.length()), 'U')
                .insert(Ostrov.random.nextInt(code.length()), 'R').insert(Ostrov.random.nextInt(code.length()), 'L')
                .insert(Ostrov.random.nextInt(code.length()), 't').insert(Ostrov.random.nextInt(code.length()), 'a')
                .insert(Ostrov.random.nextInt(code.length()), 'r').insert(Ostrov.random.nextInt(code.length()), 'o').toString();
            case MORE_LOWER -> new StringBuilder(code).insert(Ostrov.random.nextInt(code.length()), 'k')
                .insert(Ostrov.random.nextInt(code.length()), 'r').insert(Ostrov.random.nextInt(code.length()), 'l').toString();
        };
    }

    private String genWires(final WColor clr, final boolean add) {
        final StringBuilder sb = new StringBuilder(MAX_WIRES << 2);
        final List<WColor> cls = new ArrayList<>(Arrays.asList(COLORS));
        final int[] counts = new int[COLORS.length];
        for (int i = 0; i != counts.length; i++) {
            counts[i] = MAX_WIRES + Ostrov.random.nextInt(2);
        }
        if (add) counts[clr.ordinal()]+=4;
        else counts[clr.ordinal()]-=4;
        while (!cls.isEmpty()) {
            final int ix = Ostrov.random.nextInt(cls.size());
            final WColor wc = cls.get(ix);
            sb.append(switch (wc) {
                case RED -> "<red>";
                case YELLOW -> "<yellow>";
                case GREEN -> "<green>";
                case BLUE -> "<blue>";
            }).append(WIRES[Ostrov.random.nextInt(WIRES.length)]);
            if (counts[wc.ordinal()]-- == 0) cls.remove(ix);
        }
        return sb.toString();
    }


  private enum Condition {
    NUM_SUM("<mithril>Сумма <gold>цифр <mithril>в коде более <gold>20ти"),
    //        END_UPPER("<mithril>Код кончается на <gold>большую <mithril>букву"),
    HAS_UPPER("<mithril>В коде есть <gold>'C' <mithril>и <gold>'S'"),
    //        HAS_LOWER("<mithril>В коде есть <gold>'a'<mithril>, <gold>'w'<mithril>, и <gold>'p'"),
    IS_LARGE("<mithril>Код имеет <gold>более 25 <mithril>символов"),
    MORE_LOWER("<mithril>Более <gold>половины <mithril>букв кода <gold>малые");

    private static final Map<String, Condition> names;

    private final SingleOptionDialogInput.OptionEntry entry;

    Condition(final String text) {
      entry = SingleOptionDialogInput.OptionEntry
          .create(name().toLowerCase(Locale.ROOT), TCUtil.form(text), false);
    }

    static {
      final Map<String, Condition> sm = new ConcurrentHashMap<>();
      for (final Condition cnd : Condition.values()) {
        sm.put(cnd.name().toLowerCase(Locale.ROOT), cnd);
      }
      names = Collections.unmodifiableMap(sm);
    }

    private SingleOptionDialogInput.OptionEntry entry() {
      return entry;
    }

    public static Condition parse(final String name) {
      return names.get(name);
    }
  }

  private enum WColor {RED, YELLOW, GREEN, BLUE}

  private static final Condition[] OPTIONS_LONG = {Condition.NUM_SUM/*, Condition.END_UPPER*/,
      Condition.HAS_UPPER/*, Condition.HAS_LOWER*/, Condition.IS_LARGE, Condition.MORE_LOWER};
  private static final List<SingleOptionDialogInput.OptionEntry> LIST_LONG =
      Arrays.stream(OPTIONS_LONG).map(Condition::entry).toList();

  private static final char[] WIRES = {'⎱', '⎛', '⎜', '⎝', '⎨', '⎫', '⎬', '⎭', '⎰', '⎱'};
  private static final String[] CODES = {"v9FcO1nV4YePiM2ud3Rl", "1Fo5PlecJvqK7pU3s2En", "Fl1Trn8PoYdMf5oI2Vu3", "G4mQeD5oFx7laP2jWtM1"};

  private static final int MAX_VOLTS = 500;
  private static final int MAX_WIRES = 9;
  private static final WColor[] COLORS = WColor.values();

  private static final String CODE = "code", VOLTS = "volts", CHECK = "check";




































    public void test(final EntityDamageEvent e) { //extends EntityEvent
        //Ostrov.log_warn("EntityDamageEvent "+e.getEntityType()+" cause="+e.getCause()+" src="+e.getDamageSource()+" dmg="+e.getDamage());
        if (e instanceof EntityDamageByEntityEvent edbe) {
            Ostrov.log_warn("cast EntityDamageByEntityEvent  " + e.getEntityType() + " cause=" + e.getCause() + " src=" + e.getDamageSource() + " dmg=" + e.getDamage());
            if (e.getEntity() instanceof LivingEntity le) {
                CombatTracker ct = le.getCombatTracker();
                Ostrov.log_warn(e.getEntityType() + " cause=" + e.getCause() + " InCombat?" + ct.isInCombat() + " TakingDamage?" + ct.isTakingDamage() + " dur=" + ct.getCombatDuration());
                for (CombatEntry ce : ct.getCombatEntries()) {
                    DamageSource ds = ce.getDamageSource();
                    if (ds.getDamageType() == DamageType.ARROW) {  //getDirectEntity = arrow
                        //Ostrov.log_warn("ds=ARROW by="+  ds.getCausingEntity().getName()+" dir="+ds.getDirectEntity());
                        Arrow ar = (Arrow) ds.getDirectEntity();
                        ProjectileSource ps = ar.getShooter();
                        if (ps instanceof Player p) {
                            Ostrov.log_warn("ds=ARROW shoter=player " + p.getName());
                        } else {
                            Ostrov.log_warn("ds=ARROW shoter=" + ds.getCausingEntity().getName());
                        }
                    } else if (ds.getDamageType() == DamageType.PLAYER_ATTACK) { //getDirectEntity = player
                        Ostrov.log_warn("ds=PLAYER_ATTACK by=" + ds.getCausingEntity().getName());
                    } else {
                        Ostrov.log_warn("ds=" + ds.getDamageType().getKey().getKey() + " CausingEntity=" + (ds.getCausingEntity() == null ? "null" : ds.getCausingEntity().getType()));
                    }
                }
                Ostrov.log_warn("");
                //Ostrov.log_warn(e.getEntityType()+" cause="+e.getCause()+" InCombat?"+ct.isInCombat()+" TakingDamage?"+ct.isTakingDamage()
                //    +" CombatEntries="+ct.getCombatEntries());
            }
        } else if (e instanceof EntityDamageByBlockEvent edbb) {
            Ostrov.log_warn("cast EntityDamageByBlockEvent  " + e.getEntityType() + " cause=" + e.getCause() + " src=" + e.getDamageSource() + " dmg=" + e.getDamage());
        } else {
            Ostrov.log_warn("EntityDamageEvent " + e.getEntityType() + " cause=" + e.getCause() + " src=" + e.getDamageSource() + " dmg=" + e.getDamage());
        }
    }

    //@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void test(final EntityDamageByEntityEvent e) { //extends EntityDamageEvent
        if (e.getDamager() instanceof Player) {
            Ostrov.log_warn("EntityDamageByEntityEvent " + e.getEntityType() + " cause=" + e.getCause() + " src=" + e.getDamageSource() + " dmg=" + e.getDamage());
        }
    }

    //@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void test(final EntityDamageByBlockEvent e) { //extends EntityDamageEvent
        Ostrov.log_warn("EntityDamageByBlockEvent " + e.getEntityType() + " cause=" + e.getCause() + " src=" + e.getDamageSource() + " dmg=" + e.getDamage());
    }

    //@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void test(final ProjectileLaunchEvent e) { //extends EntitySpawnEvent
        ProjectileSource ps = e.getEntity().getShooter();
        if (ps != null && ps instanceof Player p) {
            Ostrov.log_warn("ProjectileLaunchEvent " + e.getEntityType());
        }
    }

    // @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void test(final EntityShootBowEvent e) { //extends EntityEvent
        ProjectileSource ps = ((Projectile) e.getProjectile()).getShooter();
        if (ps != null && ps instanceof Player p) {
            Ostrov.log_warn("EntityShootBowEvent " + e.getEntityType() + " getHitEntity=" + e.getBow());
        }
    }

    //@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void test(final LingeringPotionSplashEvent e) { //extends ProjectileHitEvent
        Ostrov.log_warn("LingeringPotionSplashEvent " + e.getEntityType() + " getHitEntity=" + e.getHitEntity() + " getHitBlock=" + e.getHitBlock());
    }


    //@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void test(final ProjectileHitEvent e) { //extends EntityEvent
        Ostrov.log_warn("ProjectileHitEvent " + e.getEntityType() + " getHitEntity=" + e.getHitEntity() + " getHitBlock=" + e.getHitBlock());
    }

    // @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public static void onPot(final PotionSplashEvent e) { //extends ProjectileHitEvent
        Ostrov.log_warn("PotionSplashEvent " + e.getEntityType() + " getHitEntity=" + e.getHitEntity() + " getHitBlock=" + e.getHitBlock());
    }

    //@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public static void onPot(final CreatureSpawnEvent e) { //extends ProjectileHitEvent
        if (e.getSpawnReason() != CreatureSpawnEvent.SpawnReason.SPAWNER_EGG) e.setCancelled(true);
    }
}
