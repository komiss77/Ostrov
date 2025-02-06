package ru.komiss77.modules.rolls;

import ru.komiss77.Initiable;
import ru.komiss77.Ostrov;

public class RollManager implements Initiable {

    public RollManager() {
        reload();
    }

    @Override
    public void postWorld() {}

    @Override
    public void reload() {
        Ostrov.log_ok("§2Перебор включен!");
//        RollTree.loadAll();
    }

    @Override
    public void onDisable() {
        Ostrov.log_ok("§6Перебор выключен!");
    }
}
