package ru.komiss77.modules.player.mission;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Ostrov;
import ru.komiss77.RemoteDB;
import ru.komiss77.Timer;
import ru.komiss77.enums.Data;
import ru.komiss77.enums.Game;
import ru.komiss77.enums.HistoryType;
import ru.komiss77.enums.Stat;
import ru.komiss77.modules.games.GM;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.NumUtil;
import ru.komiss77.utils.inventory.ClickableItem;
import ru.komiss77.utils.inventory.InputButton;
import ru.komiss77.utils.inventory.InventoryContent;
import ru.komiss77.utils.inventory.InventoryProvider;


public class MissionWithdrawCreateMenu implements InventoryProvider {

    private static final ClickableItem fill = ClickableItem.empty(new ItemBuilder(Material.SCULK_VEIN).name("§8.").build());


    @Override
    public void init(final Player p, final InventoryContent content) {

        p.getWorld().playSound(p.getLocation(), Sound.BLOCK_AMETHYST_CLUSTER_BREAK, 5, 5);
        content.fillRect(0, 0, 4, 8, fill);

        final Oplayer op = PM.getOplayer(p);

        final int ril = op.getDataInt(Data.RIL);


        boolean can = true;
        final int min = MissionManager.getMin(op);

      if (ril < min) {// || ril > MissionManager.WITHDRAW_MAX) {

          content.set(2, 2, ClickableItem.empty(new ItemBuilder(Material.MAGENTA_DYE)
                .name("§5Вывод средств возможен от §b" + min + " до " + MissionManager.WITHDRAW_MAX + "рил")
                .lore("")
                .lore("§7Расчёт мин. суммы такой:")
                .lore("§7Колл-во предыдущих выводов * 5")
                .lore("§7Минимальняа сумма : §6" + min)
                .lore("§7Максимальная сумма")
                .lore("§7для одного вывода : §6" + MissionManager.WITHDRAW_MAX + "рил")
                .build()
            ));
            can = false;

        } else {

            content.set(2, 2, ClickableItem.empty(new ItemBuilder(Material.LIME_DYE)
                .name("§aРил для вывода достаточно")
                .lore("")
                .lore("§fУ Вас §e" + ril + " §fрил.")
                .lore("")
                .lore("§7Максимальная сумма")
                .lore("§7для одного вывода : §6" + MissionManager.WITHDRAW_MAX + "рил")
                .build()
            ));

        }


        if (GM.GAME != Game.LOBBY) {

          content.set(2, 3, ClickableItem.of(new ItemBuilder(Material.MAGENTA_DYE)
                    .name("§4✕ §6Надо Находиться в лобби")
                    .lore("")
                    .lore("§7Перейдите в лобби")
                    .lore("§7командной /hub")
                    .lore("")
                    .lore("§7ЛКМ - перейти")
                    .build(), e -> {
                    p.performCommand("server lobby");
                }
            ));
            can = false;

        } else {

            content.set(2, 3, ClickableItem.empty(new ItemBuilder(Material.LIME_DYE)
                .name("§a✔ §8Находиться в лобби")
                .build()
            ));

        }


        if (op.getDataString(Data.PHONE).isEmpty()) {

          content.set(2, 4, ClickableItem.of(new ItemBuilder(Material.MAGENTA_DYE)
                    .name("§4✕ §6Не указан номер телефона в профиле")
                    .lore("")
                    .lore("§7Оттедактируйте профиль")
                    .lore("§7командной /passport edit")
                    .lore("")
                    .lore("§7ЛКМ - редактировать")
                    .build(), e -> {
                    p.performCommand("passport edit");
                }
            ));
            can = false;

        } else {

            content.set(2, 4, ClickableItem.empty(new ItemBuilder(Material.LIME_DYE)
                .name("§a✔ §8Номер телефона указан")
                .lore("")
                .lore("§b" + op.getDataString(Data.PHONE))
                .lore("")
                .build()
            ));

        }

        if (op.getDataString(Data.NOTES).isEmpty()) {

          content.set(2, 5, ClickableItem.of(new ItemBuilder(Material.MAGENTA_DYE)
                    .name("§4✕ §6Не указано куда переводить в примечаниях")
                    .lore("")
                    .lore("§7Оттедактируйте 'примечания'")
                    .lore("§7в профиле")
                    .lore("§7командной /passport edit")
                    .lore("")
                    .lore("§7ЛКМ - редактировать")
                    .build(), e -> {
                    p.performCommand("passport edit");
                }
            ));
            can = false;

        } else {

            content.set(2, 5, ClickableItem.empty(new ItemBuilder(Material.LIME_DYE)
                .name("§a✔ §8Направление вывода указано")
                .lore("")
                .lore("§b" + op.getDataString(Data.NOTES))
                .lore("")
                .build()
            ));

        }


        if (!can) {
            content.set(2, 7, ClickableItem.empty(new ItemBuilder(Material.REDSTONE)
                .name("§сВывод невозможен.")
                .lore("§5<<<<<<<<<<<<")
                .lore("§7Для заказа вывода средств")
                .lore("§7должны быть выполнены все условия!")
                .lore("§5<<<<<<<<<<<<")
                .build()
            ));

        } else {


            final String ammountInfo = MissionManager.getMin(op) + " до " + ril;
            final ItemStack is = new ItemBuilder(Material.RAW_GOLD)
                .name("§e" + ril)
                .lore("")
                .lore("§fЛКМ - §6указать сумму")
                .lore("§fи вывести.")
                .lore("")
                .lore("§7Сумма будет снята с вашего")
                .lore("§7баланса РИЛ, и отправлена на")
                .lore("§7реквизиты, указанные в профиле.")
                .lore("§7Вывод осуществляется в течении")
                .lore("§33 дней§7, за процессом можно следить")
                .lore("§7в меню статуса заявок.")
                .lore("")
                .lore("§eОбратите внимание на технически")
                .lore("§eдоступные способы перевода:")
                //.lore("§f- §нКиви")
                //.lore("§f- §нЯндекс Кошелёк")
                .lore("§f- §нномер телефона на +7 (Россия)")
                .lore("§f- §нномер банковской карты")
                .lore("")
                .build();

            content.set(2, 7, new InputButton(InputButton.InputType.ANVILL, is, ammountInfo, msg -> {
                p.closeInventory();
                if (!NumUtil.isInt(msg)) {
                    p.sendMessage("§cДолжно быть число!");
                    PM.soundDeny(p);
                    return;
                }
              final int withdrav = Integer.parseInt(msg);
              if (withdrav < min || withdrav > MissionManager.WITHDRAW_MAX) {
                    p.sendMessage("§cСумма для вывод от " + min + " до " + MissionManager.WITHDRAW_MAX + " рил!");
                    PM.soundDeny(p);
                    reopen(p, content);
                    return;
                }


                final int current = op.getDataInt(Data.RIL);
              if (current < withdrav) {
                p.sendMessage("§cНа счету нет " + withdrav + " рил!");
                    return;
                }

              op.setData(Data.RIL, current - withdrav);
                RemoteDB.executePstAsync(p,
                    "INSERT INTO `withdraw` (name,summ,time,passPhone,passNote) VALUES ('" + op.nik + "', '" + withdrav + "', '" + Timer.getTime() + "', '" + op.getDataString(Data.PHONE) + "', '" + op.getDataString(Data.NOTES) + "'); "
                );
              Ostrov.history(HistoryType.MONEY_REAL_WITHDRAW, op, "заявка на вывод " + withdrav + ". Было " + current + " стало " + op.getDataInt(Data.RIL));
              p.sendMessage("§aЗаявка на вывод §b" + withdrav + " рил §aзарегистрирована.");
                p.playSound(p.getLocation(), Sound.BLOCK_SMITHING_TABLE_USE, 1, 1);
                ApiOstrov.addStat(p, Stat.WD_count);
              ApiOstrov.addStat(p, Stat.WD_amount, withdrav);


            }));

        }


    }


}
