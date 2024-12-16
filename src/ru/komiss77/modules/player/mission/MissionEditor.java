package ru.komiss77.modules.player.mission;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.komiss77.RemoteDB;
import ru.komiss77.Timer;
import ru.komiss77.enums.Stat;
import ru.komiss77.modules.player.PM;
import ru.komiss77.utils.*;
import ru.komiss77.utils.inventory.*;

public class MissionEditor implements InventoryProvider {


    private final Mission mi;
    private int oldid;
    private static final ClickableItem fill = ClickableItem.empty(new ItemBuilder(Material.PURPLE_STAINED_GLASS_PANE).name("§8.").build());
    private static final ClickableItem fill2 = ClickableItem.empty(new ItemBuilder(Material.YELLOW_STAINED_GLASS_PANE).name("§8.").build());
    //private static final ArrayList<NamedTextColor> ccs = new ArrayList<>(NamedTextColor.NAMES.values());


    public MissionEditor(final Mission mission) {
        this.mi = mission;
    }


    //если ид=0, значит новая миссия, сохранять инсерт
    //если не ноль,то апдейт


    @Override
    public void init(final Player p, final InventoryContent content) {
        p.playSound(p.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 5, 5);


        content.fillRow(0, fill);
        content.fillRow(5, fill);
        content.fillRect(2, 0, 4, 8, fill2);


        content.set(0, 4, ClickableItem.empty(new ItemBuilder(mi.mat)
                .name("§7Информация о миссии")
                .lore(mi.name)
                .lore("§7ID миссии: §3" + (mi.id < 0 ? "не назначен (новая)" : mi.id))
                .lore("§7Награда: §6" + mi.reward + " рил")
                .lore("§7Могут выполнить: §6" + mi.canComplete + " чел.")
                //.addLore("§7Будет доступна:")
                .lore("§7Доступна с:")
                .lore("§7" + TimeUtil.dateFromStamp(mi.activeFrom))
                .lore("§7Доступна по:")
                .lore("§7" + TimeUtil.dateFromStamp(mi.validTo))
                .lore("")
                .lore("§7Уровень не менее §6" + mi.level)
                .lore("§7Репутация не менее §6" + mi.reputation)
                .lore("")
                .lore(Mission.getRequest(p, mi))
                .build()));


        content.set(1, 0, ClickableItem.of(new ItemBuilder(mi.mat)
                .name("§7Иконка")
                .lore("§7Ткните сюда предметом из инвентаря")
                .lore("§7для смены иконки")
                .build(), e -> {
            if (e.isLeftClick() && e.getCursor() != null && e.getCursor().getType() != Material.AIR) {
                p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1);
                //e.setCancelled(true);
                mi.mat = e.getCursor().getType();
                mi.changed = true;
                e.getView().getBottomInventory().addItem(new ItemStack[]{e.getCursor()});
                e.getView().setCursor(new ItemStack(Material.AIR));
                reopen(p, content);
            }
            //return;
        }));


        content.set(1, 1, new InputButton(InputButton.InputType.ANVILL, new ItemBuilder(Material.ACACIA_SIGN)
                .name("§7Название")
                .lore("")
                .lore("§7Сейчас: ")
                .lore(mi.displayName())
                .lore("")
                .lore("§7ЛКМ - изменить")
                .lore("")
                .lore("§fТолько название,")
                .lore("§fбез цвета!")
                .build(), mi.name, newName -> {
            if (newName.length() > 32) {
                p.sendMessage("§cСлишком длинное название! (лимит32)");
                PM.soundDeny(p);
            } else {
                mi.name = TCUtil.strip(newName);
                mi.displayName = null;
                mi.changed = true;
                p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1);
                reopen(p, content);
            }
        }));


        content.set(1, 2, new InputButton(InputButton.InputType.ANVILL, new ItemBuilder(Material.ORANGE_GLAZED_TERRACOTTA)
                .name("§7Цвет названия")
                .lore(TCUtil.form(mi.nameColor + "ТИПА НАЗВАНИЕ"))
                .lore("")
                .lore("§7ЛКМ - изменить")
                .lore("")
                .lore("§fВ формате &цвет")
                .lore("§fМожно кастомные и градиент!")
                .build(), mi.nameColor.replaceAll("§", "&"), newColor -> {
            if (newColor.length() > 5) {
                p.sendMessage("§cСлишком длинный цветовой код! (макс.5)");
                PM.soundDeny(p);
            } else {
                mi.nameColor = newColor.replaceAll("&", "§");
                mi.displayName = null;
                mi.changed = true;
                p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1);
                reopen(p, content);
            }
        }));


        content.set(1, 3, ClickableItem.of(new ItemBuilder(Material.GOLD_INGOT)
                .name("§7Награда за выполнение")
                .amount(mi.reward)
                .lore("§7")
                .lore("§7Сейчас: §b" + mi.reward + " рил")
                .lore("§7")
                .lore("§7ЛКМ : +1 (макс.64)")
                .lore("§7ПКМ : -1")
                .lore("§7")
                .build(), e -> {
            if (e.isLeftClick() && mi.reward < 64) {
                p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1);
                mi.changed = true;
                mi.reward++;
                reopen(p, content);
            } else if (e.isRightClick() && mi.reward > 1) {
                p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1);
                mi.changed = true;
                mi.reward--;
                reopen(p, content);
            } else {
                PM.soundDeny(p);
            }
            //return;
        }));


        content.set(1, 4, ClickableItem.of(new ItemBuilder(Material.CARTOGRAPHY_TABLE)
                .name("§7Счётчик выполнений")
                //.setAmount(mi.rewardFund)
                .lore("§7Сколько человек могут")
                .lore("§7выполнить данную миссию?")
                .lore("§7")
                .lore("§7Сейчас: §b" + (mi.canComplete))
                .lore("§7Призовой фонд составит:")
                .lore("§f" + mi.canComplete + "*" + mi.reward + "=" + mi.canComplete * mi.reward + " рил")
                .lore("§7")
                .lore("§7ЛКМ : +1 (макс.64)")
                .lore("§7ПКМ : -1")
                .lore("§7")
                .build(), e -> {
            if (e.isLeftClick() && mi.canComplete < 64) {
                p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1);
                mi.changed = true;
                mi.canComplete++;
                reopen(p, content);
            } else if (e.isRightClick() && mi.canComplete > 1) {
                p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1);
                mi.changed = true;
                mi.canComplete--;
                reopen(p, content);
            } else {
                PM.soundDeny(p);
            }
            //return;
        }));


        content.set(1, 5, new InputButton(InputButton.InputType.ANVILL, new ItemBuilder(Material.EXPERIENCE_BOTTLE)
                .name("§eТребуемый уровень")
                .lore("§7")
                .lore("§7Сейчас: " + (mi.level == 0 ? "§fневажен" : "§b" + mi.level))
                .lore("§7")
                .lore("§7Требуемый уровень островитянина")
                .lore("§7для выполнения миссии.")
                .lore("§7")
                .lore("§7ЛКМ - изменить")
                .lore("§7")
                .build(), "" + mi.level, imput -> {
            if (!NumUtil.isInt(imput)) {
                p.sendMessage("§cДолжно быть число!");
                PM.soundDeny(p);
                return;
            }
            final int level = Integer.valueOf(imput);
            if (level < 0 || level > 127) {
                p.sendMessage("§cот 0 до 127!");
                PM.soundDeny(p);
                return;
            }
            mi.level = level;
            mi.changed = true;
            p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1);
            reopen(p, content);
        }));


        content.set(1, 6, new InputButton(InputButton.InputType.ANVILL, new ItemBuilder(Material.EMERALD)
                .name("§eТребуемая репутация")
                .lore("§7")
                .lore("§7Сейчас: §b" + mi.reputation)
                .lore("§7")
                .lore("§eТребуемая репутация островитянина")
                .lore("§7для выполнения миссии.")
                .lore("§7")
                .lore("§7ЛКМ - изменить")
                .lore("§7")
                .build(), "" + mi.reputation, imput -> {
            if (!NumUtil.isInt(imput)) {
                p.sendMessage("§cДолжно быть число!");
                PM.soundDeny(p);
                return;
            }
            final int reputation = Integer.valueOf(imput);
            if (reputation < -99 || reputation > 99) {
                p.sendMessage("§cот -99 до 99!");
                PM.soundDeny(p);
                return;
            }
            mi.reputation = reputation;
            mi.changed = true;
            p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1);
            reopen(p, content);
        }));


        content.set(1, 7, ClickableItem.of(new ItemBuilder(Material.CLOCK)
                .name("§7Время начала")
                .lore("§7")
                //.addLore("§7Сейчас: ")
                .lore("§f" + TimeUtil.dateFromStamp(mi.activeFrom))
                .lore("§7ЛКМ - изменить")
                .lore("§7")
                .lore("§7")
                .lore(mi.activeFrom < Timer.getTime() ? "§eначало меньше текущего времени!" : "")
                .lore(mi.activeFrom > mi.validTo ? "§cначало не может быть после окончания!!" : "")
                .lore("§7")
                .build(), e -> {
            DateTimeEditGui.open(p, "старт миссии", mi.activeFrom, true, true, time -> {
                // if (time<Timer.getTime()) {
                //   p.sendMessage("§cначало не может быть в прошлом!");
                //return;
                //  }
                if (time > mi.validTo) {
                    p.sendMessage("§cначало не может быть после окончания!");
                    //return;
                }
                mi.activeFrom = time;
                mi.changed = true;
                reopen(p, content);
            });
            //return;
        }));

        content.set(1, 8, ClickableItem.of(new ItemBuilder(Material.CLOCK)
                .name("§7Время окончания")
                .lore("§7")
                //.addLore("§7Сейчас: ")
                .lore("§f" + TimeUtil.dateFromStamp(mi.validTo))
                .lore("§7")
                .lore("§7ЛКМ - изменить")
                .lore(mi.validTo > mi.validTo ? "§eокончание меньше текущего времени!" : "")
                .lore(mi.validTo < mi.activeFrom ? "§cокончание не может быть раньше начала!" : "")
                .lore("§7")
                .build(), e -> {
            DateTimeEditGui.open(p, "окончание миссии", mi.validTo, true, true, time -> {
                //if (time<Timer.getTime()) {
                //    p.sendMessage("§cокончание не может быть в прошлом!");
                //return;
                //}
                if (time < mi.activeFrom) {
                    p.sendMessage("§cокончание не может быть раньше начала!");
                    //return;
                }
                mi.validTo = time;
                mi.changed = true;
                reopen(p, content);
            });
            //return;
        }));


        // *************** Требования *************


        int slot = 28;
        Stat stat;
        String displayName;
        boolean showAmmount;

        for (String requestName : mi.request.keySet()) {

            stat = Stat.fromName(requestName);
            final int currentAmmount = mi.request.get(requestName);

            if (stat == null) {
                displayName = MissionManager.customStatsDisplayNames.containsKey(requestName) ? MissionManager.customStatsDisplayNames.get(requestName) : requestName;
                showAmmount = MissionManager.customStatsShowAmmount.containsKey(requestName) ? MissionManager.customStatsShowAmmount.get(requestName) : true;

                content.set(slot, ClickableItem.of(new ItemBuilder(MissionManager.customStatMat(requestName))
                        .name("§7Требование: §6customStat")
                        .lore("§7значение String: §f" + requestName)
                        .lore(showAmmount ? "§7колличество: §f" + currentAmmount : "§8колличество скрыто")
                        .lore("")
                        .lore("§7Будет показана как:")
                        .lore(displayName + (showAmmount ? " §7: §d" + currentAmmount : ""))
                        .lore("")
                        .lore("§7ЛКМ - изменить тип")
                        .lore("§7Шифт+ЛКМ - ввести значение")
                        .lore(currentAmmount < 10000 ? "§7ПКМ - добавить" : "§8предел")
                        .lore(currentAmmount > 1 ? "§7Шифт+ПКМ - убавить" : "§8предел")
                        .lore("§7клав. Q - отменить требование")
                        .lore("§7")
                        .build(), e -> {
                    mi.changed = true;
                    switch (e.getClick()) {
                        case LEFT -> {
                            SmartInventory.builder()
                                    .id("Выбор требования")
                                    .provider(new RequestSelect(mi))
                                    .size(6, 9)
                                    .title("Выбор требования")
                                    .build()
                                    .open(p);
                            return;
                        }
                        case SHIFT_LEFT -> {
                            PlayerInput.get(InputButton.InputType.ANVILL, p, (msg) -> {
                                if (!NumUtil.isInt(msg)) {
                                    p.sendMessage("§cДолжно быть число!");
                                }
                                final int value = Integer.parseInt(msg);
                                if (value < 0 || value > 10000) {
                                    p.sendMessage("§cот 0 до 10000!");
                                }
                                mi.request.replace(requestName, value);
                                reopen(p, content);
                            }, "1");
                            /*new AnvilGUI.Builder()
                                    .title("от 0 до 10000")
                                    .text("1")
                                    .onComplete( (p1, msg) -> {
                                        if (!ApiOstrov.isInteger(msg)) {
                                            p.sendMessage("§cДолжно быть число!");
                                            return AnvilGUI.Response.text("");
                                        }
                                        final int value = Integer.parseInt(msg);
                                        if (value<0 || value>10000) {
                                            p.sendMessage("§cот 0 до 10000!");
                                            return AnvilGUI.Response.text("");
                                        }
                                        mi.request.replace(requestName, value);
                                        reopen(p, content);
                                        return AnvilGUI.Response.text("");
                                    })
                                    .open(p);*/
                            return;
                        }
                        case RIGHT -> {
                            if (mi.request.get(requestName) < 10000) {
                                mi.request.replace(requestName, mi.request.get(requestName) + 1);
                                reopen(p, content);
                            }
                        }
                        case SHIFT_RIGHT -> {
                            if (mi.request.get(requestName) > 1) {
                                mi.request.replace(requestName, mi.request.get(requestName) - 1);
                                reopen(p, content);
                            }
                        }
                        case DROP -> {
                            mi.request.remove(requestName);
                            reopen(p, content);
                        }
                        default -> {
                        }
                    }
                    //reopen(p, content);
                }));

            } else {

                content.set(slot, ClickableItem.of(new ItemBuilder(Material.matchMaterial(stat.game.mat))
                        .name("§7Требование: §3стата §b" + requestName)
                        .lore("§7Игра: " + stat.game.displayName)
                        .lore("")
                        .lore("§7Будет показана как:")
                        .lore(stat.game.displayName + "§7, " + stat.desc + "§d" + currentAmmount)
                        .lore("")
                        .lore("§7ЛКМ - изменить тип")
                        .lore(currentAmmount < 100 ? "§7ПКМ - добавить" : "§8предел")
                        .lore(currentAmmount > 1 ? "§7Шифт+ПКМ - убавить" : "§8предел")
                        .lore("§7клав. Q - отменить требование")
                        .lore("§7")
                        .build(), e -> {
                    mi.changed = true;
                    switch (e.getClick()) {
                        case LEFT -> {
                            SmartInventory.builder()
                                    .id("Выбор требования")
                                    .provider(new RequestSelect(mi))
                                    .size(6, 9)
                                    .title("Выбор требования")
                                    .build()
                                    .open(p);
                            return;
                        }
                        case RIGHT -> {
                            if (mi.request.get(requestName) < 100) {
                                mi.request.replace(requestName, mi.request.get(requestName) + 1);
                                reopen(p, content);
                            }
                        }
                        case SHIFT_RIGHT -> {
                            if (mi.request.get(requestName) > 1) {
                                mi.request.replace(requestName, mi.request.get(requestName) - 1);
                                reopen(p, content);
                            }
                        }
                        case DROP -> {
                            mi.request.remove(requestName);
                            reopen(p, content);
                        }
                        default -> {
                        }
                    }
                    //reopen(p, content);
                }));
            }

            slot++;
        }

        for (; slot <= 34; slot++) {
            content.set(slot, ClickableItem.of(new ItemBuilder(Material.FIREWORK_STAR)
                    .name("§7Свободный слот требования")
                    .lore("§7")
                    .lore("§7ЛКМ - задать")
                    .lore("§7")
                    .build(), e -> {
                if (e.isLeftClick()) {
                    mi.changed = true;
                    SmartInventory.builder()
                            .id("Выбор требования")
                            .provider(new RequestSelect(mi))
                            .size(6, 9)
                            .title("Выбор требования")
                            .build()
                            .open(p);
                }
            }));
        }


        content.set(5, 0, ClickableItem.of(new ItemBuilder(Material.PLAYER_HEAD)
                .headTexture(ItemUtil.Texture.previosPage)
                .name("§7назад")
                .build(), e -> {

            MissionManager.openMissionsEditMenu(p);
        }));


        if (mi.id > 0) {
            content.set(5, 3, ClickableItem.of(new ItemBuilder(Material.SOUL_CAMPFIRE)
                    .name("§eПерезапуск миссии")
                    .lore("§7")
                    .lore("§7Будет сброшен ИД,")
                    .lore("§7и миссия будет как новая.")
                    .lore("§7Старт установтся на §3сейчас")
                    //.addLore("§f"+ApiOstrov.dateFromStamp(mi.activeFrom))
                    .lore("§7")
                    .lore("§7Опции сброса:")
                    .lore("§6ЛКМ§7:срок на §f" + TimeUtil.secondToTime(mi.validTo - mi.activeFrom))
                    .lore("§6ПКМ§7:со сроком на §fнеделю")
                    .lore("§7")
                    .lore("§7Счётчик выполнений установится на §630")
                    .lore("§7")
                    .lore("§cБез сохранения ничего не изменится!")
                    .lore("§7")
                    .build(), e -> {
                oldid = mi.id;
                mi.id = -1;
                mi.changed = true;
                mi.canComplete = 30;
                mi.doing = 0;
                if (e.isLeftClick()) {
                    //oldid = mi.id;
                    //mi.id=-1;
                    //mi.changed = true;
                    //mi.rewardFund=30;
                    //mi.doing=0;
                    mi.validTo = Timer.getTime() + (mi.validTo - mi.activeFrom); //сначала validTo! или activeFrom уже будет другой!
                    mi.activeFrom = Timer.getTime();
                } else if (e.isRightClick()) {
                    //oldid = mi.id;
                    //mi.id=-1;
                    //mi.changed = true;
                    //mi.rewardFund=30;
                    //mi.doing=0;
                    mi.activeFrom = Timer.getTime();
                    mi.validTo = mi.activeFrom + 7 * 24 * 60 * 60;
                }
                //mi.id=-1;
                //mi.changed = true;

                reopen(p, content);
            }));
        }


        if (mi.changed) {
            content.set(5, 6, ClickableItem.of(new ItemBuilder(Material.JUKEBOX)
                    .name("§aСохранить изменения в БД")
                    .lore("§7")
                    .lore("§7Вы внесли изменения,")
                    .lore("§7рекомендуется сохранение.")
                    .lore("§7")
                    .lore("§7После сохранения данные")
                    .lore("§7о миссиях прогрузятся на все")
                    .lore("§7сервера в течении 5 минут.")
                    .lore("§7")
                    .lore("§cБез сохранения ничего не изменится!")
                    .lore("§7")
                    .build(), e -> {

                if (mi.id == -1) {
                    RemoteDB.executePstAsync(p, "INSERT INTO `missions` (`name`, `nameColor`, `mat`, `level`, `reputation`, `request`, `reward`, `rewardFund`, `activeFrom`, `validTo`) "
                            + "VALUES ('" + mi.name + "', '" + mi.nameColor + "', '" + mi.mat.name() + "', '" + mi.level + "', '" + mi.reputation + "', '" + Mission.getRequestString(mi) + "', '" + mi.reward + "', '" + mi.canComplete + "', '" + mi.activeFrom + "', '" + mi.validTo + "');");
                    if (oldid != 0) {
                        RemoteDB.executePstAsync(p, "DELETE FROM `missions` WHERE 'id'='" + oldid + "'; ");
                    }
                } else {
                    RemoteDB.executePstAsync(p, "UPDATE `missions` SET `name`='" + mi.name + "', `nameColor`='" + mi.nameColor + "', `mat`='" + mi.mat.name() + "', `level`='" + mi.level + "', `reputation`='" + mi.reputation + "', `request`='" + Mission.getRequestString(mi) + "', `reward`='" + mi.reward + "', `rewardFund`='" + mi.canComplete + "', `activeFrom`='" + mi.activeFrom + "', `validTo`='" + mi.validTo + "' WHERE `missionId`='" + mi.id + "'");
                }
                MissionManager.openMissionsEditMenu(p);
            }));
        }


    }


}
