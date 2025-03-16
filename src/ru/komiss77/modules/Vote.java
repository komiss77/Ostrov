package ru.komiss77.modules;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import ru.komiss77.Ostrov;
import ru.komiss77.enums.Stat;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;

/*
== РЕКЛАМА ==
доменная зона https://cp.beget.com/dns/8492246
таблица https://docs.google.com/spreadsheets/d/1uHl2qSvPKUawutJbP6UfVYD0BKdSGCAOFqCereRKh_8/edit#gid=0
анализ посещемости https://be1.ru/stat/monitoringminecraft.ru https://pr-cy.ru/site-statistics/?domain=rulauncher.com
!!! Сделать баннер играй зарабатывай
экономика https://docs.google.com/document/d/1YR-WHXT63ezbIKMXaEkUdlpARxGTfXStZ5NhRPHIOCI/edit

в работе
https://minecraftservers.ru/server/8065 - нет доступа

С апи голосования
https://hotmc.ru/links-185821  +++
https://minecraftrating.ru/server/131995/
https://mineserv.top/ostrov77
https://mctop.su/servers/7192/vote/
https://tmonitoring.com/projects/edit/81975/ - перепроверить скрипт (не присылает), пока офф

без апи за голосование
https://klauncher.gg/monitoring/cabinet   https://klauncher.ru/monitoring/cabinet ++++ зона kl
https://misterlauncher.org/promote/10822/#buy-multi +++ зона mister    Купон на скидку 20% - SIDFN3A
https://rulauncher.com (https://ru-minecraft.ru) +++ зона rulauncher   переписка телега
https://monitoringminecraft.ru +зона mm
https://flauncher.biz/servers/917 (https://monitoringminecraft.net/) +на неделю 2600 (система баллов) +зона flauncher
https://minecraft-inside.ru/top/    https://minecraft-inside.ru/top/my/access/21904/
https://minecraftmonitoring.com/server/15129
 */


public class Vote {

  public static void onCommand(final Player p) { //из 100500cmd
    p.playSound(p.getLocation(), Sound.BLOCK_CHERRY_SAPLING_BREAK, .3f, 1);
    final Oplayer op = PM.getOplayer(p);
    final ItemStack book = new ru.komiss77.utils.ItemBuilder(Material.WRITTEN_BOOK).name("Журнал голосований").build();
    final BookMeta bookMetaRU = (BookMeta) book.getItemMeta();
    bookMetaRU.setTitle("Журнал голосований");
    bookMetaRU.setAuthor("Остров77");

    TextComponent.Builder b = Component.text();

    b.append(Component.text("§3Голосовалка\n\n"));

    for (Host host : Host.values()) {
      if (op.getDailyStat(host.stat) > 0) {
        b.append(host.done);
      } else {
        b.append(host.active);
      }
    }

    bookMetaRU.addPages(b.build());
    book.setItemMeta(bookMetaRU);

    p.openBook(book);
  }


  public static void onVote(final String senderInfo, final String voteInfo) { //после госовалки приходит чтото вроде vote_hotmc
    if (!voteInfo.startsWith("vote_")) {
      Ostrov.log_warn("onVote voteInfo начинается не с vote_ " + senderInfo + " : " + voteInfo);
      return;
    }
    final Player p = Bukkit.getPlayerExact(senderInfo);
    if (p == null) {
      Ostrov.log_warn("onVote player==null " + senderInfo + " : " + voteInfo);
      return;
    }
    final String hostName = voteInfo.substring(5);
    Host host = null;
    try {
      host = Host.valueOf(hostName);
    } catch (IllegalArgumentException ex) {
      Ostrov.log_warn("onVote unknow host " + senderInfo + " : " + voteInfo);
      return;
    }
    final Oplayer op = PM.getOplayer(p);
    if (op.getDailyStat(host.stat) > 0) {
      p.sendMessage("§6Сегодня вы уже голосовали на " + host.name() + ", попробуйте завтра.");
    } else {
      p.sendMessage("§aСпасибо за ваш голос на " + host.name() + "!");
      op.addStat(host.stat, 1);

      boolean all = true;
      for (Host h : Host.values()) {
        if (op.getDailyStat(h.stat) == 0) {
          all = false;
          break;
        }
      }
      if (all) {
        p.sendMessage("§aВы проголосовали на всех мониторингах! Награда единорог!");
        //todo mission
      } else {
        p.performCommand("vote"); //переоткрыть книгу
      }

    }

//Ostrov.log_warn("onVote player="+senderInfo+" f="+sf);

  }

  enum Host {

    hotmc(Component.text("§1§nHOTMC\n").hoverEvent(HoverEvent.showText(Component.text("Клик - голосовать")))
        .clickEvent(ClickEvent.openUrl("https://hotmc.ru/vote-185821")),
        Component.text("§8§mHOTMC\n").hoverEvent(HoverEvent.showText(Component.text("Сегодня уже голосовали, попробуйте завтра.")))
            .clickEvent(ClickEvent.openUrl("https://hotmc.ru/vote-185821")),
        Stat.VOTE_HT),

    mineserv(Component.text("§1§nMINESERV\n").hoverEvent(HoverEvent.showText(Component.text("Клик - голосовать")))
        .clickEvent(ClickEvent.openUrl("https://mineserv.top/ostrov77")),
        Component.text("§8§nMINESERV\n").hoverEvent(HoverEvent.showText(Component.text("Сегодня уже голосовали, попробуйте завтра.")))
            .clickEvent(ClickEvent.openUrl("https://mineserv.top/ostrov77")),
        Stat.VOTE_MS),

    //за проект - https://minecraftrating.ru/projects/ostrov77/ скрипт тот же как разделить?
    minerating(Component.text("§1§nMINERATING\n").hoverEvent(HoverEvent.showText(Component.text("Клик - голосовать")))
        .clickEvent(ClickEvent.openUrl("https://minecraftrating.ru/vote/131995/")),
        Component.text("§8§nMINERATING\n").hoverEvent(HoverEvent.showText(Component.text("Сегодня уже голосовали, попробуйте завтра.")))
            .clickEvent(ClickEvent.openUrl("https://minecraftrating.ru/vote/131995/")),
        Stat.VOTE_MR),

    mctop(Component.text("§1§nMCTOP\n").hoverEvent(HoverEvent.showText(Component.text("Клик - голосовать")))
        .clickEvent(ClickEvent.openUrl("https://mctop.su/servers/7192/vote/")),
        Component.text("§8§nMCTOP\n").hoverEvent(HoverEvent.showText(Component.text("Сегодня уже голосовали, попробуйте завтра.")))
            .clickEvent(ClickEvent.openUrl("https://mctop.su/servers/7192/vote/")),
        Stat.VOTE_MT),

    /* не присылает голоса
    tmonitoring ( Component.text("§1§nTmonitoring\n").hoverEvent(HoverEvent.showText(Component.text("Клик - голосовать")))
        .clickEvent(ClickEvent.openUrl("https://tmonitoring.com/server/ostrov-svobody1/")),
        Component.text("§8§nTmonitoring\n").hoverEvent(HoverEvent.showText(Component.text("Сегодня уже голосовали, попробуйте завтра.")))
            .clickEvent(ClickEvent.openUrl("https://tmonitoring.com/server/ostrov-svobody1/")),
        Stat.VOTE_MT),*/;

    public final Component active;
    public final Component done;
    public final Stat stat;

    private Host(final Component active, final Component done, final Stat stat) {
      this.active = active;
      this.done = done;
      this.stat = stat;
    }

  }


}

