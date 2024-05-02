package ru.komiss77.modules.quests;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import ru.komiss77.modules.player.Oplayer;


public interface IAdvance {

  public void buildAdv();

  public void loadPlQs(final Player p, final Oplayer op);

  public void sendToast(final Player p, final Quest q);

  public void sendToast(final Player p, final Material mt, final String msg, final Quest.QuestFrame frm);

  public void resetProgress(final Player p, final boolean rmv);

  public void sendComplete(final Player p, final Quest q, final boolean silent);

  public void sendProgress(final Player p, final Quest q, final int progress, final boolean silent);

  void unregister();

}
  


    
    

    
    

