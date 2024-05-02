package ru.komiss77.modules.quests;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import ru.komiss77.ApiOstrov;
import ru.komiss77.modules.player.Oplayer;



public class AdvanceVanila implements IAdvance, Listener {



  @Override
  public void buildAdv() {

  }

  @Override
  public void loadPlQs(Player p, Oplayer op) {

  }

  @Override
  public void sendToast(Player p, Quest q) {

  }

  @Override
  public void sendToast(Player p, Material mt, String msg, Quest.QuestFrame frm) {
    ApiOstrov.sendTitle(p, "", "§7Квест: "+msg, 20, 40, 20);
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
  public void unregister() {

  }
}
  


    
    

    
    

