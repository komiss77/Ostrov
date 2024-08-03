package ru.komiss77.builder.menu;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import ru.komiss77.Ostrov;
import ru.komiss77.RemoteDB;
import ru.komiss77.modules.translate.Lang;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.ItemUtil;
import ru.komiss77.utils.TCUtils;
import ru.komiss77.utils.inventory.*;

public class LangEditor implements InventoryProvider {


    private final List<ClickableItem> buttons;
    private final int page;
    private final boolean hasNext;
    private static final ClickableItem fill;
    //private final static TreeMap<String,String> sorted;


    static {
        fill = ClickableItem.empty(new ItemBuilder(Material.SCULK_VEIN).name("§8.").build());
        //sorted = new TreeMap<>(Lang.ruToEng);
    }

    public LangEditor(final List<ClickableItem> buttons, final int page, final boolean hasNext) {
        this.buttons = buttons;
        this.page = page;
        this.hasNext = hasNext;
    }


    @Override
    public void init(final Player p, final InventoryContent content) {
        p.playSound(p.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 5, 5);

        //final Oplayer op = PM.getOplayer(p);
        content.fillRow(5, fill);


        if (buttons.isEmpty()) {

            content.add(ClickableItem.empty(new ItemBuilder(Material.GLASS_BOTTLE)
                .name("§7нет записей!")
                .build()
            ));

        } else {

            for (final ClickableItem ci : buttons) {
                content.add(ci);
            }

        }


        if (hasNext) {
            content.set(5, 8, ClickableItem.of(ItemUtil.nextPage, e
                -> edit(p, page + 1))
            );
        }

        if (page > 0) {
            content.set(5, 0, ClickableItem.of(ItemUtil.previosPage, e
                -> edit(p, page - 1))
            );
        }


    }


    public static void edit(final Player p, final int page) {

       /* final List<ClickableItem> buttons = new ArrayList<>();
        boolean hasNext = false;
        
        for (Entry <String, String> e : Lang.ruToEng.entrySet()) {
            
        }
        
        
        
        SmartInventory
            .builder()
            //.id(op.nik + section.name())
            .provider(new LangEditor(buttons, page, next))
            .size(6, 9)
            .title("Переводики "+(page+1))
            .build()
            .open(p);*/

        Ostrov.async(() -> {

            final List<ClickableItem> buttons = new ArrayList<>();
            boolean hasNext = false;

            Statement stmt = null;
            ResultSet rs = null;

            try {
                stmt = RemoteDB.getConnection().createStatement();

                rs = stmt.executeQuery("SELECT `rus` FROM `lang` ORDER BY `stamp` DESC LIMIT " + page * 45 + ",46");

                int count = 0;

                while (rs.next()) {
                    if (count == 45) {
                        hasNext = true;
                        break;
                    } else {
                        final String rus = rs.getString("rus");
                        final String eng = Lang.getTranslate(rus);//rs.getString("eng");

                        buttons.add(new InputButton(InputButton.InputType.CHAT, new ItemBuilder(Material.PAPER)
                            .name(TCUtils.form(rus))
                            .lore(TCUtils.form(eng))
                            .build(), eng.replaceAll("§", "&"), input -> {
                            input = input.replaceAll("&", "§");
                            Lang.upd(rus, input);
                            //p.sendMessage(TCUtils.form("§f>> "+input));
                            edit(p, page);//reopen(p, content);
                        }));


                       /* buttons.add(ClickableItem.empty(new ItemBuilder(Material.PAPER)
                                .name(TCUtils.form(rus))
                                .addLore("")
                                //.addLore(ItemUtils.genLore(null, rs.getString("report"), "§7"))
                                .addLore("")
                                .addLore("")
                                .build()
                        ));*/
                    }
                    count++;
                }

                final boolean next = hasNext;

                Ostrov.sync(() -> {
                    SmartInventory
                        .builder()
                        //.id(op.nik + section.name())
                        .provider(new LangEditor(buttons, page, next))
                        .size(6, 9)
                        .title("Переводики " + (page + 1))
                        .build()
                        .open(p);
                }, 0);

            } catch (SQLException e) {

                Ostrov.log_err("§сLangEditor - " + e.getMessage());

            } finally {
                try {
                    if (rs != null) {
                        rs.close();
                    }
                    if (stmt != null) {
                        stmt.close();
                    }
                } catch (SQLException e) {
                    Ostrov.log_err("§сLangEditor close - " + e.getMessage());
                }
            }

        }, 20);

    }


}
