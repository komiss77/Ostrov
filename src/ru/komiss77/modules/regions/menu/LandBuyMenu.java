package ru.komiss77.modules.regions.menu;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemFlag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import ru.komiss77.ApiOstrov;
import ru.komiss77.modules.games.GM;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;
import ru.komiss77.modules.regions.PlotBuilder;
import ru.komiss77.modules.regions.RM;
import ru.komiss77.modules.regions.Template;
import ru.komiss77.modules.world.WE;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.ScreenUtil;
import ru.komiss77.utils.inventory.ClickableItem;
import ru.komiss77.utils.inventory.InventoryContent;
import ru.komiss77.utils.inventory.InventoryProvider;


public class LandBuyMenu implements InventoryProvider {

  private static final ClickableItem fill = ClickableItem.empty(new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).name(" ").build());

  private final List<ProtectedRegion> owned;

  public LandBuyMenu(final List<ProtectedRegion> owned) {
    this.owned = owned;
  }

  @Override
  public void init(final Player p, final InventoryContent content) {

    content.fillRow(3, fill);

    final Oplayer op = PM.getOplayer(p);
    /*
    if (op==null) {
      RM.log_err("§c[ERROR] нет экземпляра Oplayer для "+p.getName());
      return;
    }

    int totatRegionLimit;//


    if (ApiOstrov.isLocalBuilder(p)) {
      totatRegionLimit = 77;
    } else {
      totatRegionLimit = ApiOstrov.getLimit(op, "region.total");//PM.getBigestPermValue(op, "region.limit.total");
      if (totatRegionLimit<1) totatRegionLimit = 1; //один приват всегда можно, раз уж есть плагин на сервере!
    }

    final List <ProtectedRegion> playerRegions = RegionUtils.getPlayerOwnedRegions(p);
    final int totalRegion = playerRegions.size();
*/

/*
    if (!playerRegions.isEmpty()) {
      content.set(4, 2, ClickableItem.of( new ItemBuilder(Material.GRAY_BED).name("§aТП в регион").build(), e -> {
        SmartInventory.builder()
            .id("home-" + p.getName())
            .provider(new LandHomeMenu())
            .size(5, 9)
            .title(Language.INTERFACE_HOME_TITLE.toString())
            .build()
            .open(p);
        p.playSound(p.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 5, 5);
      }));

      content.set(4, 6, ClickableItem.of( new ItemBuilder(Material.PAPER).name("§bСписок регионов в этом мире").build(), e -> {
        p.closeInventory();
        p.playSound(p.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 5, 5);
        p.performCommand("land list");
      }));
    }*/


    //получаем список заготовок для данного мира
    final List<Template> templateList = new ArrayList<>();//RM.getTemplates(p.getWorld());
    for (Template tp : RM.templates.values()) {
      if (tp.allowedWorlds.contains(p.getWorld().getName())) {
        templateList.add(tp);
      }
    }
    if (templateList.isEmpty()) {
      content.add(ClickableItem.empty(new ItemBuilder(Material.BARRIER)
          .name("§4Не вариантов покупки")
          .lore("§cДля этого мира нет заготовок региона для покупки!", "§c")
          .build()));
      return;
    }

    // if ( playerRegions.size() >= totatRegionLimit) {
    //    content.add( ClickableItem.empty( new ItemBuilder(Material.BARRIER)
    //        .name("§4Создание новых недоступно")
    //       .lore("§cВаш глобальный лимит: "+totatRegionLimit,"§c")
    //       .build() )
    //   );
    //   return;
    // }// else {

    for (final Template t : templateList) { //перебираем все заготовки

      int currentTemplateLimit = ApiOstrov.getLimit(op, "region." + t.name);//PM.getBigestPermValue(op, "region.limit."+t.getName());
      if (currentTemplateLimit < 1) currentTemplateLimit = 1;

      //подсчёт приватов такого типа
      int currentTemplateCount = 0;
      for (ProtectedRegion rg : owned) {
        if (t.name.equalsIgnoreCase(RM.templateName(rg))) {
          currentTemplateCount = ++currentTemplateCount;
        }
      }


      final ItemBuilder ib = new ItemBuilder(t.iconMat);
      ib.name(t.displayname);
      ib.flags(ItemFlag.HIDE_ATTRIBUTES);

      ib.lore(t.description);
      ib.lore("");
      //ib.lore("§7Ваши регионы: §3"+(totalRegion==0?"не найдено":totalRegion)+(" §7(лимит: §5"+totatRegionLimit+"§7)"));
      ib.lore("§7Регионы данного типа: §3" + (currentTemplateCount == 0 ? "не найдено" : currentTemplateCount) + (" §7(лимит: §5" + currentTemplateLimit + "§7)"));
      ib.lore("§7Размеры: §e" + t.size + "x" + t.size + "§7, вниз §e" + t.depth + "§7, вверх §e" + t.height);
      ib.lore("§7Цена: §b" + (t.price == 0 ? "бесплатно" : t.price + " §7лони."));
      ib.lore("");

      //ib.lore(lore);

      //если нет права на эту заготовку, не кликабельное и добавляем сообщение
      if (t.permission && !p.hasPermission(Template.PERM_FOR_ALl) && !p.hasPermission(t.permission())) {

        //ib.lore("");
        ib.lore("§cнет права " + t.permission());
        content.add(ClickableItem.empty(ib.build()));

      } else if (currentTemplateCount >= currentTemplateLimit) {

        ib.lore("§cЛимит регионов данного типа!");
        content.add(ClickableItem.empty(ib.build()));

      } else {

        ib.lore("§6ЛКМ §f- предпросмотр на местности");
        ib.lore(WE.hasJob(p) ? "§cДождитесь окончания операции!" : "§6ПКМ §f- создать регион");

        content.add(ClickableItem.of(ib.build(), e -> {

          if (e.getClick() == ClickType.RIGHT) { //пкм - покупка

            p.closeInventory();
            if (WE.hasJob(p)) {
              p.sendMessage("§cДождитесь окончания операции!");
              return;
            }
            p.playSound(p.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 5, 5);

            //создание привата
            new PlotBuilder(p, t).build();
            p.resetTitle();

            ApiOstrov.addCustomStat(p, GM.GAME.name() + "_region", 1);
//Bukkit.broadcastMessage("create "+GM.thisServerGame.name()+"_region");
          } else if (e.getClick() == ClickType.LEFT) { //лкм - предпросмотр

            //new RegionPreview(p, t.getSize() + 1);
            p.closeInventory();
            RM.startPreview(p, t);

            p.playSound(p.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 5, 5);

            ScreenUtil.sendTitle(p, "§6Предпросмотр", "§aДля покупки ПКМ в меню");
            //p.sendMessage("§6Предпросмотр региона");
            p.sendMessage("§aДля покупки ПРАВЫЙ клик в меню покупки.");
          }

        }));
      }

    }
    //  }


  }


}
