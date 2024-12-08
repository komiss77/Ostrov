package ru.komiss77.listener;

import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Rotatable;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.block.sign.Side;
import org.bukkit.block.sign.SignSide;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Cfg;
import ru.komiss77.Timer;
import ru.komiss77.commands.PassportCmd;
import ru.komiss77.enums.ServerType;
import ru.komiss77.events.BsignLocalArenaClick;
import ru.komiss77.modules.games.GM;
import ru.komiss77.modules.games.GameSign;
import ru.komiss77.modules.games.GameSignEditor;
import ru.komiss77.modules.player.PM;
import ru.komiss77.utils.*;
import ru.komiss77.utils.inventory.*;


public class InteractLst implements Listener {

    public static final WeakHashMap<Player, List<Component>> signFrontCache;
    public static final WeakHashMap<Player, List<Component>> signBackCache;
    public static final ItemStack signEdit;
    public static final ItemStack gameSignEdit;
    public static final ItemStack passport;

    static {
        signFrontCache = new WeakHashMap<>();
        signBackCache = new WeakHashMap<>();

        signEdit = new ItemBuilder(Material.WARPED_SIGN)
                .name("§fПомошник по табличкам")
                .lore("")
                .lore("§7Клик по табличке.")
                .lore("")
                .lore("§7ЛКМ - редактировать")
                .lore("§7Шифт+ЛКМ - сменить тип")
                .lore("")
                .lore("§7ПКМ - скопировать")
                .lore("§7Шифт+ПКМ - вставить")
                .lore("")
                .enchant(Enchantment.CHANNELING, 1)
                .build();

        gameSignEdit = new ItemBuilder(Material.CRIMSON_SIGN)
                .name("§fСерверные таблички")
                .lore("")
                .lore("§7ЛКМ по табличке - §cудалить")
                .lore("")
                .lore("§7ПКМ по табличке - ")
                .lore("§7настроить отображаемую игру")
                .enchant(Enchantment.CHANNELING, 1)
                .build();

        passport = new ItemBuilder(Material.PAPER)
                .name("§aПаспорт")
                .modelData(77)
                .flags(ItemFlag.HIDE_ATTRIBUTES)
                .flags(ItemFlag.HIDE_ENCHANTS)
                .flags(ItemFlag.HIDE_UNBREAKABLE)
                .unbreak(true)
                .lore("")
                .lore("§7Держите паспорт в руке,")
                .lore("§7и окружающие смогут его")
                .lore("§7посмотреть,сделав правый")
                .lore("§7клик на Вас.")
                .lore("")
                .lore("§7Вы всегда можете")
                .lore("§7достать документ из кармана")
                .lore("§7набрав §b/passport get")
                .lore("§7Изменить паспортные данные")
                .lore("§7можно в профиле.")
                .lore("")
                .build();
    }


    @EventHandler(ignoreCancelled = false, priority = EventPriority.MONITOR)
    public void onInteractEntity(PlayerInteractEntityEvent e) {
        if (e.getRightClicked().getType() == EntityType.PLAYER) {
            final Player target = (Player) e.getRightClicked();
            //если у цели в руках паспорт - показать кликающему
            if (isPassport(target.getInventory().getItem(e.getHand()))) {
                e.setCancelled(true);
                PassportCmd.showLocal(e.getPlayer(), target);
            }
        }
    }


    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = false)
    public void Interact(final PlayerInteractEvent e) {
        if (e.getAction() == Action.PHYSICAL) return;

        final Player p = e.getPlayer();
        if (p.getGameMode() == GameMode.SPECTATOR && (e.getAction() == Action.LEFT_CLICK_AIR || e.getAction() == Action.LEFT_CLICK_BLOCK)) {
            if (p.getOpenInventory().getType() != InventoryType.CHEST) {
                if (PM.getOplayer(p.getUniqueId()).setup != null) {
                    PM.getOplayer(p.getUniqueId()).setup.openSetupMenu(p);
                    return;
                } //else {
                //p.performCommand("menu"); может перекрыть в минииграх
                //}
            }
        }
        final ItemStack inHand = e.getItem();

        //фикс для NAME_TAG
        if (inHand != null && inHand.getType() == Material.NAME_TAG
                && e.getAction().isRightClick() && GM.GAME.type == ServerType.ONE_GAME) {  //отловил баг на змейке, походу на минииграх это не надо
            final ItemMeta im = inHand.getItemMeta();
            new InputButton(InputButton.InputType.ANVILL, inHand, im.hasDisplayName() ? TCUtil.deform(im.displayName()).replace('§', '&') : "Название", nm -> {
                im.displayName(TCUtil.form(nm.replace('&', '§')));
                inHand.setItemMeta(im);
                p.closeInventory();
            }).run(new ItemClickData(p, new InventoryClickEvent(p.getOpenInventory(), InventoryType.SlotType.CONTAINER, 0,
                    ClickType.LEFT, InventoryAction.PICKUP_ALL), ClickType.LEFT, ItemUtil.air, SlotPos.of(0, 0)));
            return;
        }

        //для отладки есть специальный TestLst

        //паспорт
        if (isPassport(inHand)) { //посмотреть свой паспорт
            e.setUseItemInHand(Event.Result.DENY);
            if (e.getAction().isRightClick()) {
                PassportCmd.showLocal(p, p);
            }
            return;
        }

        final Block b = e.getClickedBlock();
        if (b != null) {

            //Клик по табличке
            if (Tag.ALL_SIGNS.isTagged(b.getType()) || Tag.ALL_HANGING_SIGNS.isTagged(b.getType())) {

                //редактор таблички и серверные таблички
                if (ApiOstrov.isLocalBuilder(p, false)) {

                    if (ItemUtil.compareItem(signEdit, inHand, false)) {
                        signEdit(p, e);
                        return;

                    } else if (ItemUtil.compareItem(gameSignEdit, inHand, false)) {
                        e.setCancelled(true);
                        final String locAsString = LocUtil.toString(b.getLocation());
                        if (e.getAction() == Action.LEFT_CLICK_BLOCK) {
                            if (GM.signs.containsKey(locAsString)) {
                                ConfirmationGUI.open(p, "Удалить табличку?", (result) -> {
                                            if (result) {
                                                b.breakNaturally();
                                                GM.deleteGameSign(p, locAsString);
                                            }
                                        }
                                );
                            } else {
                                p.sendMessage("§6Это на серверная табличка!");
                            }
                        } else {
                            if (GM.signs.containsKey(locAsString)) {
                                p.sendMessage("§6Это серверная табличка, сначала сломайте её!");
                                return;
                            }
                            SmartInventory.builder()
                                    .type(InventoryType.CHEST)
                                    .id("GameSignEditor" + p.getName())
                                    .provider(new GameSignEditor((Sign) b.getState()))
                                    .title("§fНастройка серверной таблички")
                                    .size(6, 9)
                                    .build()
                                    .open(p);
                        }
                        return;
                    }
                }

                //клик по серверной табличке
                final String locAsString = LocUtil.toString(b.getLocation());
                final GameSign gameSign = GM.signs.get(locAsString);
//Ostrov.log("locAsString="+locAsString+" gameSign="+gameSign);
                if (gameSign != null) {
                    e.setUseInteractedBlock(Event.Result.DENY);
                    e.setUseItemInHand(Event.Result.DENY); //если не отменять, то может сразу сработать слим выхода с арены
                    if (Timer.has(p, "gameSign")) {
                        p.sendMessage("§8подождите..");
                        return;
                    }
                    Timer.add(p, "gameSign", 1);

                    if (GM.GAME.type == ServerType.ARENAS) {
                        Bukkit.getPluginManager().callEvent(new BsignLocalArenaClick(p, gameSign.arena));
                    } else {
                        p.performCommand("server " + gameSign.server + " " + gameSign.arena);//ApiOstrov.sendToServer (p, gameSign.server, gameSign.arena);
                    }
                } else if (GM.GAME.type == ServerType.ARENAS) {
                    e.setUseInteractedBlock(Event.Result.DENY); //на минииграх редактируют таблички
                }

                //командная табличка
                if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
                    final Sign sign = (Sign) b.getState();
                    final SignSide ss = sign.getSide(Side.FRONT);
                    final String line0 = TCUtil.strip(ss.line(0)).toLowerCase();
                    final String line1 = TCUtil.strip(ss.line(1));
                    if (line0.isEmpty() || line1.isEmpty()) return;
                    switch (line0) {
                        case "[команда]" -> {
                            p.performCommand(line1.toLowerCase());
                            return;
                        }
                        case "[место]" -> {
                            p.performCommand("warp " + TCUtil.strip(line1).toLowerCase());
                            return;
                        }
                    }
                }
            }

            //блокировка лавы
            if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
                if (Cfg.disable_lava && inHand != null && inHand.getType().toString().contains("LAVA") && !ApiOstrov.isLocalBuilder(p, false)) {
                    e.setUseItemInHand(Event.Result.DENY);
                    ScreenUtil.sendActionBarDirect(p, "§cЛава запрещена на этом сервере!");
                    //return;
                }
            }

        }


    }


    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void Sign_edit(SignChangeEvent e) {
        final Player p = e.getPlayer();
        final String line0 = TCUtil.strip(TCUtil.deform(e.line(0)));

        if (line0.equalsIgnoreCase("[Команда]") || line0.equalsIgnoreCase("[Место]")) {
            if (!ApiOstrov.isLocalBuilder(p, true)) {
                e.line(0, Component.text("§8" + line0));
            } else {
                e.line(0, Component.text("§2" + line0));
            }
        } else {
            e.line(0, Component.text(line0.replaceAll("&", "§")));
        }

        e.line(1, Component.text(TCUtil.deform(e.line(1)).replaceAll("&", "§")));
        e.line(2, Component.text(TCUtil.deform(e.line(2)).replaceAll("&", "§")));
        e.line(3, Component.text(TCUtil.deform(e.line(3)).replaceAll("&", "§")));
    }


    private void signEdit(final Player p, final PlayerInteractEvent e) {
        e.setCancelled(true);
        final Block b = e.getClickedBlock();
        if (b == null) return; //тупо, но без этого подчёркивает желтым - бесит
        Sign sign = (Sign) b.getState();
        if (e.getAction() == Action.LEFT_CLICK_BLOCK) {
            if (p.isSneaking()) { //шифт+лкм - сменить тип
                final List<Component> linesFront = sign.getSide(Side.FRONT).lines();
                final List<Component> linesBack = sign.getSide(Side.BACK).lines();
                final List<Material> types = new ArrayList<>();// = new ArrayList<>( Tag.WALL_SIGNS.isTagged(b.getType()) ? Tag.WALL_SIGNS.getValues() : Tag.STANDING_SIGNS.getValues());

                if (Tag.WALL_SIGNS.isTagged(b.getType())) {
                    types.addAll(Tag.WALL_SIGNS.getValues());
                } else if (Tag.STANDING_SIGNS.isTagged(b.getType())) {
                    types.addAll(Tag.STANDING_SIGNS.getValues());
                } else if (Tag.WALL_HANGING_SIGNS.isTagged(b.getType())) {
                    types.addAll(Tag.WALL_HANGING_SIGNS.getValues());
                } else if (Tag.CEILING_HANGING_SIGNS.isTagged(b.getType())) {
                    types.addAll(Tag.CEILING_HANGING_SIGNS.getValues());
                }
                int order = types.indexOf(b.getType()); //подбор следующего материала таблички
                order++;
                if (order >= types.size()) order = 0;
                final Material newMat = types.get(order);

                if (Tag.WALL_SIGNS.isTagged(b.getType())) {
                    final WallSign wsData = (WallSign) newMat.createBlockData();//org.bukkit.block.data.type.WallSign
                    wsData.setFacing(((Directional) b.getBlockData()).getFacing());
                    wsData.setWaterlogged(((Waterlogged) b.getBlockData()).isWaterlogged());
                    b.setBlockData(wsData);
                } else if (Tag.STANDING_SIGNS.isTagged(b.getType())) {
                    final org.bukkit.block.data.type.Sign snData = (org.bukkit.block.data.type.Sign) newMat.createBlockData();//org.bukkit.block.data.type.Sign
                    snData.setRotation(((Rotatable) b.getBlockData()).getRotation());
                    snData.setWaterlogged(((Waterlogged) b.getBlockData()).isWaterlogged());
                    b.setBlockData(snData);
                }

                sign = (Sign) b.getState();
                final SignSide frontSide = sign.getSide(Side.FRONT);
                int i = 0;
                for (final Component c : linesFront) { //хз, так будет универсальнее - кол-во строк может измениться
                    frontSide.line(i++, c);
                }
                i = 0;
                final SignSide backSide = sign.getSide(Side.BACK);
                for (final Component c : linesBack) {
                    backSide.line(i++, c);
                }
                sign.update();

            } else {
                SmartInventory.builder()
                        .id("SignEditSelectLine" + p.getName())
                        .provider(new SignEditMenu(sign))
                        .title("§fВыберите строку")
                        .size(3, 9)
                        .build()
                        .open(p);
            }
        } else if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (p.isSneaking()) {
                if (signFrontCache.containsKey(p)) {
                    final SignSide frontSide = sign.getSide(Side.FRONT);
                    int i = 0;
                    for (final Component c : signFrontCache.get(p)) { //хз, так будет универсальнее - кол-во строк может измениться
                        frontSide.line(i++, c);
                    }
                    i = 0;
                    final SignSide backSide = sign.getSide(Side.BACK);
                    for (final Component c : signBackCache.get(p)) { //хз, так будет универсальнее - кол-во строк может измениться
                        backSide.line(i++, c);
                    }
                    sign.update();
                } else {
                    p.sendMessage("В буфере нет скопированной таблички.");
                }
            } else {
                final SignSide frontSide = sign.getSide(Side.FRONT);
                signFrontCache.put(p, frontSide.lines());
                final SignSide backSide = sign.getSide(Side.BACK);
                signBackCache.put(p, backSide.lines());
                p.sendMessage("Содержимое таблички скопировано в буфер. Шифт+ПКМ на другую - вставить.");
            }
        }
    }

    //это намого быстрее чем через compareItem
    private boolean isPassport(final ItemStack is) {
        return is != null && is.getType() == passport.getType() && is.hasItemMeta()
                && is.getItemMeta().hasCustomModelData() && is.getItemMeta().getCustomModelData() == passport.getItemMeta().getCustomModelData();
    }


}
