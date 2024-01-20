package ru.komiss77.scoreboard;

import java.util.Iterator;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import ru.komiss77.objects.CaseInsensitiveMap;
import ru.komiss77.utils.TCUtils;


public class CustomScore {
    private static final CaseInsensitiveMap<CustomScore> boards; //для удобства перебора
    private final String name;
    private final Scoreboard ownerBoard;
    //private ScoreBoardTeam team;
    private ScoreBoardBelow below;
    private final SideBar sideBar;
    private final Team ownerTeam;
    private final CaseInsensitiveMap<Team>registeredTeams = new CaseInsensitiveMap<>(); //всосанные тимы других игроков для префиксов и тд
    
    private boolean hideNameTags;
    
    static {
        boards = new CaseInsensitiveMap<>();
    }
    
    
    public CustomScore(final Player p) {
        name = p.getName();
        ownerBoard = Bukkit.getScoreboardManager().getNewScoreboard();
        sideBar = new SideBar(p, this, name);
        ownerTeam = ownerBoard.registerNewTeam("_" + name);
        //ownerTeam.addEntry(name);
        ownerTeam.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER); //кого добавить в эту тиму, его ник скроется
        ownerTeam.setCanSeeFriendlyInvisibles(false);
        p.setScoreboard(ownerBoard);
        onJoin();
    }
    
    public Scoreboard getScoreboard() {
        return ownerBoard;
    }
    
    public SideBar getSideBar() {
        return sideBar;
    }
    

    public Team getTeam() {
        return ownerTeam;
    }

    
    
    

    
    //при входе на серв владельца борды - всосать данные с других
    private void onJoin() {
        for (CustomScore score : boards.values()) {
            if (score.hideNameTags) { //у другого скрыт ник - добавить в тим этой борды
                ownerTeam.addEntry(score.name);
            } else {
                updTeam(score.name, score.ownerTeam); // закинуть данные с борд других
            }
        }
        boards.put(name, this); //добавлять после перебора других борд!
    }

    //при дисконнекте владельца борды
    public void onQuit() {
        boards.remove(name);
        for (CustomScore score : boards.values()) {
            if (hideNameTags) { //владелец этой борды был скрыт
                score.ownerTeam.removeEntry(name); //удалить его запись
            } else { //вычистить данные этой борды у других
                if (score.registeredTeams.remove( name)!=null) {
                    score.ownerBoard.getTeam("_"+name).unregister();
                }
            }
        }
        remove();
    }
    
    public void hideNameTag() { //скрыть ник владельца борды от остальных игроков
        if (!hideNameTags) { //сейчас не скрыто
            for (CustomScore score : boards.values()) {
                if (score.name.equals(name)) continue; //самому себе не добавлять
                if (score.registeredTeams.remove( name)!=null) { //убрать инфо о тиме владельца борды, или ник не скроется
                    score.ownerBoard.getTeam("_"+name).unregister();
                } 
                score.ownerTeam.addEntry(name);
            }
            hideNameTags = true;
        }
    }
    
    public void showNameTag() { //показать ник владельца борды остальным игрокам
        if (hideNameTags) { //сейчас скрыто
            for (CustomScore score : boards.values()) {
                if (score.name.equals(name)) continue; //самому себе не надо
                score.ownerTeam.removeEntry(name);
                score.updTeam(name, ownerTeam);
            }
            hideNameTags = false;
        }
    }
     
    public void tag(final Component prefix, final String color, final Component suffix) {
        ownerTeam.prefix(prefix.append(TCUtils.format(color)));
        ownerTeam.suffix(suffix);
        ownerTeam.color(TCUtils.chatColorFromString(color));
        if (hideNameTags) { //сейчас скрыто
//Ostrov.log_warn("ставим префиксы на боард, тэги ON.");
            showNameTag();//там же сделает updTeam
        } else { //обновить префиксы в других бордах
            for (CustomScore score : boards.values()) {
                if (score.name.equals(name)) continue; //самому себе не надо
                score.updTeam(name, ownerTeam); //в борды других игроков закинуть префиксы этой борды
            }
        }
        
    }
    
    //добавить в эту борду тимы других игроков
    private void updTeam(final String ownerName, final Team ownerTeam) {
        //if (ownerName.equals(name)) return; //в свою борду не пихаем
//Ostrov.log("addTeam name="+name+" : "+ownerName);
        Team t = registeredTeams.get(ownerName);
        if (t==null) {
            t = ownerBoard.registerNewTeam("_"+ownerName);
            t.addEntry(ownerName);
            registeredTeams.put(ownerName, t);
//Ostrov.log("registerNewTeam t="+t);
        }
        t.prefix(ownerTeam.prefix());
        t.suffix(ownerTeam.suffix());
        if (ownerTeam.hasColor()) {
            t.color(NamedTextColor.nearestTo(ownerTeam.color()));
        }
    }












    
    public ScoreBoardBelow getBelow() {
        return below;
    }
    
    public void showBelow(final String below_line, final int value) {
        if (below==null) {
            below = new ScoreBoardBelow(this, below_line, value);
        } else {
            below.update(below_line, value);
        }
    }
    
    public void removeBelow() {
        if (below != null) {
            below.scores.stream().forEach( sc -> ownerBoard.resetScores(sc.getEntry()));
            below.scores.clear();
            //final Iterator<Score> iterator = below.getScores().iterator();
            //while (iterator.hasNext()) {
            //    ownerBoard.resetScores(iterator.next().getEntry());
            //}
            below.getObjective().unregister();
            below = null;
        }
    }
    

    
    
    
    
    
    
    public void remove() {
       // removeTeam();
        removeBelow();
        final Iterator<Objective> iterator = ownerBoard.getObjectives().iterator();
        while (iterator.hasNext()) {
            iterator.next().unregister();
        }
        final Iterator<Team> iterator2 = ownerBoard.getTeams().iterator();
        while (iterator2.hasNext()) {
            iterator2.next().unregister();
        }
    }



   
    @Deprecated
    public void hideNameTags() { //скрыть ники остальных игроков от владельца борды
        //ownerTeam.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);
        //ownerTeam.addEntries(PM.getOplayersNames());
        //hideNameTags = true;
    }
    
    @Deprecated
    public void showNameTags() { //показать ники остальных игроков владельцу борды
        //ownerTeam.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.ALWAYS);
        //ownerTeam.removeEntries(ownerTeam.getEntries());//team.removeEntries(PM.getOplayersNames());
        //hideNameTags = false;
    }
    


    
}


   /* 
    //при входе на серв в каждую скору прилетит входящий оплеер
    public void onJoin(Oplayer joinOp) {
        if (hideNameTags) {
            ownerTeam.addEntry(joinOp.nik); //если в этой борде включено скрытие ников, добавить входящего в тиму
        } else {
            joinOp.score.addTeam(name, ownerTeam); //приходящему закинуть данные с этой борды
        }
    }

    //при дисконнектев каждую скору прилетит уходящий оплеер
    public void onQuit(Oplayer quitOp) {
        if (hideNameTags) {
            ownerTeam.removeEntry(quitOp.nik); //если в этой борде включено скрытие ников, убрать уходящего из тимы
        } else {
            if (registeredTeams.remove(quitOp.nik)!=null) { //в этой борде была типа выходящего игрока
                board.getTeam("_"+quitOp.nik).unregister();
            }
        }
    }*/
    