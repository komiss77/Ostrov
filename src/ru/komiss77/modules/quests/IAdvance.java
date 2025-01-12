package ru.komiss77.modules.quests;

import java.util.function.Consumer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemType;
import ru.komiss77.modules.player.Oplayer;


public interface IAdvance {

    void buildAdv();

    void loadPlQs(final Player p, final Oplayer op);

    void sendToast(final Player p, final Quest q);

    void sendToast(final Player p, final ItemType mt, final String msg, final Quest.QuestFrame frm);

    void resetProgress(final Player p, final boolean rmv);

    void sendComplete(final Player p, final Quest q, final boolean silent);

    void sendProgress(final Player p, final Quest q, final int progress, final boolean silent);

    void onAdvCls(final Consumer<Player> onAdvCls);

    void unregister();

}
  


    
    

    
    

