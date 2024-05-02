package ru.komiss77.enums;

import ru.komiss77.Initiable;
import ru.komiss77.listener.LimiterLst;
import ru.komiss77.listener.ResourcePacksLst;
import ru.komiss77.modules.Informator;
import ru.komiss77.modules.Pandora;
import ru.komiss77.modules.bots.BotManager;
import ru.komiss77.modules.crafts.Crafts;
import ru.komiss77.modules.displays.DisplayManager;
import ru.komiss77.modules.drops.RollManager;
import ru.komiss77.modules.enchants.EnchantManager;
import ru.komiss77.modules.entities.EntityManager;
import ru.komiss77.modules.figures.FigureManager;
import ru.komiss77.modules.items.ItemManager;
import ru.komiss77.modules.kits.KitManager;
import ru.komiss77.modules.menuItem.MenuItemsManager;
import ru.komiss77.modules.quests.QuestManager;
import ru.komiss77.modules.redis.RDS;
import ru.komiss77.modules.scores.ScoreManager;
import ru.komiss77.modules.signProtect.SignProtectLst;
import ru.komiss77.modules.warp.WarpManager;
import ru.komiss77.modules.world.WE;
import ru.komiss77.modules.world.WorldManager;


public enum Module {

  resourcePacks (ResourcePacksLst.class),
  limiterListener (LimiterLst.class),
  menuItems (MenuItemsManager.class),
  kitManager (KitManager.class),
  pandora (Pandora.class),
  informator (Informator.class),
  warps (WarpManager.class),
  worldManager (WorldManager.class),
  worldEditor (WE.class),
  enchants (EnchantManager.class),
  crafts (Crafts.class),
  displays (DisplayManager.class),
  figure (FigureManager.class),
  bots (BotManager.class),
  quests (QuestManager.class),
  signProtect (SignProtectLst.class),
  scores (ScoreManager.class),
  entities (EntityManager.class),
  rolls (RollManager.class),
  items (ItemManager.class),
  redis (RDS.class),
  ;

  public final Class<? extends Initiable> clazz;

  Module(final Class<? extends Initiable> clazz) {
        this.clazz = clazz;
    }

}
