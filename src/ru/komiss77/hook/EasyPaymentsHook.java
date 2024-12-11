package ru.komiss77.hook;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import ru.komiss77.Ostrov;
import ru.komiss77.RemoteDB;
import ru.komiss77.enums.GlobalLogType;
import ru.komiss77.utils.NumUtils;


public class EasyPaymentsHook {

  private static BukkitTask shopTask;

  public static void hook(Plugin ep) {
    HandlerList.unregisterAll(ep);
    Ostrov.log_warn("§eОбнаружен EasyPayments, отключен от эвентов.");

    //чистить только при старте, или потом не распознаёт ник
    RemoteDB.executePstAsync(Bukkit.getConsoleSender(), "TRUNCATE `lobby`.`easypayments_customers`");
    RemoteDB.executePstAsync(Bukkit.getConsoleSender(), "TRUNCATE `lobby`.`easypayments_payments`");

    if (shopTask != null) {
      shopTask.cancel();
    }

    shopTask = new BukkitRunnable() {
      @Override
      public void run() {
        if (RemoteDB.useOstrovData && RemoteDB.ready) {

          final Connection remote = RemoteDB.getConnection();
          if (remote == null) {
            Ostrov.log_warn("EasyPaymentsHook - нет соединения с local БД!");
            return;
          }

          Statement read = null;
          Statement write = null;
          ResultSet rs = null;

          //boolean wipe = false; //final Set<Integer> ids = new HashSet<>();

          try {
            read = remote.createStatement();
            write = remote.createStatement();

            rs = read.executeQuery(" SELECT * FROM `lobby`.`easypayments_purchases`");

            while (rs.next()) {
              String cmd = rs.getString("commands");
              String[] s;
              if (cmd.length() > 10) {
                cmd = cmd.substring(2, cmd.length() - 2);
                s = cmd.split(" ");
                if (s.length == 6 && s[0].equals("reward")) {
                  final String name = s[1];
                  if (name.equals("{user}")) { //ED еще на распознал ник юзера через свою БД
                    continue;
                  }
//Ostrov.log_warn("==="+name+" "+s[2]+" "+s[3]+" "+s[4]);
                  if (s[2].equals("ril")) {
                    //0:reward 1:komiss77 2:ril 3:add 4:1 5:ED
                    write.executeUpdate("INSERT INTO `ostrov`.`payments` (`name`, `rub`, `note`) VALUES ('"
                        + name + "', '" + s[4] + "', 'ED');");
                  } else if (s[2].equals("group")) {
                    //0:reward 1:komiss77 2:group 3:hero 4:1m 5:ED
                    int month = NumUtils.intOf(s[4].replaceFirst("m", ""), 0);
                    if (month > 0) {
                      write.executeUpdate("INSERT INTO `ostrov`.`payments` (`name`, `gr`, `days`, `note`) VALUES ('"
                          + name + "', '" + s[3] + "', '" + month * 30 + "', 'ED');");
                    } else {
                      Ostrov.globalLog(GlobalLogType.DONATE, Ostrov.MOT_D, "EasyPayments - конвертер длительности: " + cmd);
                    }
                  }
                  write.executeUpdate("DELETE FROM `lobby`.`easypayments_purchases` WHERE `id` = '" + rs.getString("id") + "';");

                }
              }
              //wipe = true;
            }

            rs.close();

            //if (wipe) {
            //write.executeUpdate("TRUNCATE `lobby`.`easypayments_customers`");
            //write.executeUpdate("TRUNCATE `lobby`.`easypayments_payments`");
            //read.executeUpdate("TRUNCATE `easypayments_purchases`");
            //}

            read.close();
            write.close();

          } catch (SQLException ex) {

            Ostrov.log_warn("§4EasyPaymentsHook: " + ex.getMessage());

          } finally {
            try {
              if (rs != null) rs.close();
              if (read != null) read.close();
              if (write != null) write.close();
            } catch (SQLException ex) {
              Ostrov.log_warn("§4EasyPaymentsHook Не удалось закрыть запросы: " + ex.getMessage());
            }
          }

        }
      }
    }.runTaskTimerAsynchronously(Ostrov.instance, 100, 20 * 5);

  }


}
