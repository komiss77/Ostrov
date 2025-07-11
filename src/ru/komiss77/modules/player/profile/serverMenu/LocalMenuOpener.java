package ru.komiss77.modules.player.profile.serverMenu;

import org.bukkit.entity.Player;
import ru.komiss77.Ostrov;
import ru.komiss77.modules.games.GM;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.profile.Section;
import ru.komiss77.utils.inventory.SmartInventory;


public class LocalMenuOpener {

    public static void open(final Player p, final Oplayer op) {
        switch (GM.GAME) {

            case AR:
                op.menu.section = Section.ВОЗМОЖНОСТИ;
                op.menu.current = SmartInventory
                    .builder()
                    .id(op.nik + op.menu.section.name())
                    .provider(new Arcaim())
                    .size(6, 9)
                    .title("<gradient:blue:gold>§lМеню сервера " + GM.GAME.displayName)
                    .build()
                    .open(p);
                break;

            case DA:
                op.menu.section = Section.ВОЗМОЖНОСТИ;
                op.menu.current = SmartInventory
                    .builder()
                    .id(op.nik + op.menu.section.name())
                    .provider(new Daaria())
                    .size(6, 9)
                    .title("<gradient:blue:gold>§lМеню сервера " + GM.GAME.displayName)
                    .build()
                    .open(p);
                break;

            case SE:
                op.menu.section = Section.ВОЗМОЖНОСТИ;
                op.menu.current = SmartInventory
                    .builder()
                    .id(op.nik + op.menu.section.name())
                    .provider(new Sedna())
                    .size(3, 9)
                    .title("<gradient:blue:gold>§lМеню сервера " + GM.GAME.displayName)
                    .build()
                    .open(p);
                break;

            default:
                if (Ostrov.debug()) {
                    op.menu.section = Section.ВОЗМОЖНОСТИ;
                    op.menu.current = SmartInventory
                        .builder()
                        .id(op.nik + op.menu.section.name())
                        .provider(new Daaria())
                        .size(3, 9)
                        .title("<gradient:blue:gold>§lМеню сервера " + GM.GAME.displayName)
                        .build()
                        .open(p);
                } else {
                    op.menu.openLastSection(p);
                }
                break;

        }


    }


}
