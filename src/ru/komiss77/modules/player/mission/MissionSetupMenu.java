package ru.komiss77.modules.player.mission;

import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.text.Component;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemType;
import ru.komiss77.RemoteDB;
import ru.komiss77.Timer;
import ru.komiss77.modules.items.ItemBuilder;
import ru.komiss77.modules.player.PM;
import ru.komiss77.utils.ItemUtil;
import ru.komiss77.utils.TimeUtil;
import ru.komiss77.utils.inventory.*;


//настройки миссий билдером

public class MissionSetupMenu implements InventoryProvider {

    private static final ClickableItem fill = ClickableItem.empty(new ItemBuilder(ItemType.BLACK_STAINED_GLASS_PANE).name("§8.").build());
    private final List<Mission> missions;


    public MissionSetupMenu(final List<Mission> missions) {
        this.missions = missions;
    }


    @Override
    public void init(final Player p, final InventoryContent content) {
        p.playSound(p.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 5, 5);
        //content.fillRect(0,0,  5,8, ClickableItem.empty(fill));

        content.fillRow(4, fill);


        final List<ClickableItem> buttons = new ArrayList<>(missions.size());
        //Stat stat;
        ItemType displayMat;


        for (final Mission mi : missions) {
            final List<Component> lore = new ArrayList<>();

            lore.add(Component.text("§7ID: §3" + mi.id)); //0
            lore.add(Component.text("§7Награда: §e" + mi.reward + " рил")); //1
            lore.add(Component.text("§7Лимит выполнения: §6" + mi.canComplete + " раз")); //2
            lore.add(Component.empty()); //3
            lore.add(Component.empty()); //4
            lore.add(Component.text("§7Претенденты: §f" + mi.doing));
            lore.add(Component.text("§7Доступна с:"));
            lore.add(Component.text("§7" + TimeUtil.dateFromStamp(mi.activeFrom)));
            lore.add(Component.text("§7Доступна по:"));
            lore.add(Component.text("§7" + TimeUtil.dateFromStamp(mi.validTo)));
            lore.add(Component.empty());
            lore.add(Component.text("§7Уровень не менее §6" + mi.level));
            lore.add(Component.text("§7Репутация не менее §6" + mi.reputation));
            lore.add(Component.empty());
            lore.addAll(Mission.getRequest(p, mi));
            lore.add(Component.empty());
            lore.add(Component.text("§7ЛКМ - настроить"));
            lore.add(Component.text("§7клав.Q - §cудалить"));
            lore.add(Component.empty());


            //активна сейчас
            if (Timer.secTime() >= mi.activeFrom && Timer.secTime() < mi.validTo) {
                lore.set(4, Component.text(MissionManager.missions.containsKey(mi.id) ? "§aПодгружена, активна" : "§6Ожижает подгрузки"));
                displayMat = mi.mat;
            } else {
                lore.set(4, Component.text(MissionManager.missions.containsKey(mi.id) ? "§dОжижает выгрузки" : "§5неактивна"));
                displayMat = ItemType.GRAY_DYE;
            }

            if (mi.canComplete <= 0) {
                lore.set(2, Component.text("§7Счётчик выполнения §сисчерпан!"));
                displayMat = ItemType.REDSTONE;
            }

            buttons.add(ClickableItem.of(new ItemBuilder(displayMat)
                            .name(mi.displayName())
                            .lore(lore)
                            .build(), e -> {
                        if (e.getClick() == ClickType.LEFT) {
                            MissionManager.editMission(p, mi);
                        } else if (e.getClick() == ClickType.DROP) {
                            RemoteDB.executePstAsync(p, "DELETE FROM `missions` WHERE `missionId` = '" + mi.id + "' ");
                            missions.remove(mi);
                            reopen(p, content);
                        }
                    }
            ));


        }

        buttons.add(ClickableItem.of(
                        new ItemBuilder(ItemType.PLAYER_HEAD)
                                .name("§aдобавить")
                                .headTexture(ItemUtil.Texture.add)
                                .lore("§7")
                                .build(), e -> {
                            final Mission mi = new Mission();
                            mi.changed = true;
                            MissionManager.editMission(p, mi);
                        }
                )
        );


        final Pagination pagination = content.pagination();
        pagination.setItems(buttons.toArray(ClickableItem[]::new));
        pagination.setItemsPerPage(36);


        if (!pagination.isLast()) {
            content.set(4, 8, ClickableItem.of(ItemUtil.nextPage, e
                            -> {
                        content.getHost().open(p, pagination.next().getPage());
                    }
            ));
        }

        if (!pagination.isFirst()) {
            content.set(4, 0, ClickableItem.of(ItemUtil.previosPage, e
                            -> {
                        content.getHost().open(p, pagination.previous().getPage());
                    })
            );
        }

        pagination.addToIterator(content.newIterator(SlotIterator.Type.HORIZONTAL, SlotPos.of(0, 0)).allowOverride(false));


        content.set(5, 0, ClickableItem.of(new ItemBuilder(ItemType.PLAYER_HEAD)
                .headTexture(ItemUtil.Texture.previosPage)
                .name("§7назад")
                .build(), e -> {
            if (PM.getOplayer(p).setup != null) {
                PM.getOplayer(p).setup.openMainSetupMenu(p);
            } else {
                p.performCommand("admin");
            }
        }));


        content.set(5, 2, ClickableItem.of(new ItemBuilder(ItemType.BOOK)
                        .name("§7Редактор названий customStat")
                        .build(), e -> {
                    SmartInventory.builder()
                            .id("Редактор названий customStat")
                            .provider(new CustomStatNameEditor())
                            .size(6, 9)
                            .title("Редактор названий customStat")
                            .build()
                            .open(p);
                }
        ));


        content.set(5, 4, ClickableItem.of(new ItemBuilder(ItemType.REPEATER)
                        .name("§7Обновить список")
                        .build(), e -> {
                    MissionManager.openMissionsEditMenu(p);
                }
        ));

        content.set(5, 5, ClickableItem.of(new ItemBuilder(ItemType.HOPPER_MINECART)
                        .name("§eПринудительная загрузка из БД")
                        .build(), e -> {
                    p.performCommand("mission forceload");//MissionManager.loadMissions();
                    MissionManager.openMissionsEditMenu(p);
                }
        ));
    }
}
