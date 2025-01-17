package ru.komiss77.commands;

import com.mojang.brigadier.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Cfg;
import ru.komiss77.builder.menu.WorldSetupMenu;
import ru.komiss77.commands.tools.OCmdBuilder;
import ru.komiss77.utils.inventory.SmartInventory;


public class WorldCmd {

  public WorldCmd() {

    new OCmdBuilder("world")
        .run(cntx -> {
          final CommandSender cs = cntx.getSource().getSender();
          if (!(cs instanceof final Player p)) {
            cs.sendMessage("§eНе консольная команда!");
            return 0;
          }
          if (ApiOstrov.isLocalBuilder(p, false)) {
            SmartInventory.builder()
                .id("Worlds" + p.getName())
                .provider(new WorldSetupMenu())
                .size(6, 9)
                .title("§2Миры сервера")
                .build().open(p);
            return 0;
          }
          if (Cfg.world_command) {
            if (p.hasPermission("ostrov.world")) {
              SmartInventory.builder()
                  .id("Worlds" + p.getName())
                  .provider(new WorldSelectMenu())
                  .size(3, 9)
                  .title("§2Миры сервера")
                  .build().open(p);
            } else {
              p.sendMessage("§cУ Вас нет пава ostrov.world !");
              return 0;
            }
          } else {
            p.sendMessage("§cСмена мира командой world отключён на этом сервере!");
            return 0;
          }
          return Command.SINGLE_SUCCESS;
        })
        .description("Перемещение между марами")
        .register();

  }
}