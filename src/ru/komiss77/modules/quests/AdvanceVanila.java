package ru.komiss77.modules.quests;

import java.util.function.Consumer;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemType;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.utils.ScreenUtil;


public class AdvanceVanila implements IAdvance, Listener {


    @Override
    public void buildAdv() {
    }

    @Override
    public void loadPlQs(Player p, Oplayer op) {
    }

    @Override
    public void sendToast(Player p, Quest q) {
      ScreenUtil.sendTitle(p, "", "§7Квест: " + q.displayName, 20, 40, 20);
    }

    @Override
    public void sendToast(Player p, ItemType mt, String msg, Quest.QuestFrame frame) {
        ScreenUtil.sendTitle(p, "", "§7Квест: " + msg, 20, 40, 20);
    }

    @Override
    public void resetProgress(Player p, boolean rmv) {
    }

    @Override
    public void sendComplete(Player p, Quest q, boolean silent) {
    }

    @Override
    public void sendProgress(Player p, Quest q, int progress, boolean silent) {
    }

  @Override
  public void onAdvCls(final Consumer<Player> onAdvCls) {
    }

    @Override
    public void unregister() {
    }
}
  


    
    

    
    

