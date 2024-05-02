package ru.komiss77.modules.items;

import ru.komiss77.Initiable;
import ru.komiss77.Ostrov;

public class ItemManager implements Initiable {

  @Override
  public void postWorld() { //обход модулей после загрузки миров, т.к. не всё можно сделать onEnable
  }

  @Override
  public void reload() {
    Ostrov.log_ok("§2Предметы включены!");
    ItemRoll.loadAll();
  }

  @Override
  public void onDisable() {
    Ostrov.log_ok("§6Предметы выключены!");
  }
}
