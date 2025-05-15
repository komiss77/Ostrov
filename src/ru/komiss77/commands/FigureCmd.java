package ru.komiss77.commands;

import com.mojang.brigadier.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Ostrov;
import ru.komiss77.commands.tools.OCmdBuilder;
import ru.komiss77.modules.figures.MenuMain;
import ru.komiss77.utils.inventory.SmartInventory;


public class FigureCmd {

  public FigureCmd() {
    new OCmdBuilder("figure")
        .run(cntx -> {
          final CommandSender cs = cntx.getSource().getSender();
          if (!(cs instanceof final Player p)) {
            cs.sendMessage("§eНе консольная команда!");
            return 0;
          }
          if (ApiOstrov.isLocalBuilder(p, true)) {
            SmartInventory.builder()
                .id("MenuMain" + p.getName())
                .provider(new MenuMain())
                .size(1, 9)
                .title("§fФигуры")
                .build()
                .open(p);
          } else {
            p.sendMessage("§cдоступно билдерам");
          }
          return Command.SINGLE_SUCCESS;
        })
        .description("Фигуры")
        .register(Ostrov.mgr);
  }
}