package ru.komiss77.scoreboard;

import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import ru.komiss77.objects.CaseInsensitiveMap;


public class CustomScore {
    private static final CaseInsensitiveMap<CustomScore> boards; //для удобства перебора
    private final String name;
    private final boolean botBoard;
    private Scoreboard ownerBoard;
    private final SideBar sideBar;
    private final Team ownerTeam;
    //private final CaseInsensitiveMap<Team>registeredTeams = new CaseInsensitiveMap<>(); //всосанные тимы других игроков для префиксов и тд
    
//    private boolean visible;
    
    static {
        boards = new CaseInsensitiveMap<>();
    }
    
    
    public CustomScore(final Player p) {
        name = p.getName();
        botBoard = false;
        ownerBoard = Bukkit.getScoreboardManager().getNewScoreboard();
        sideBar = new SideBar(p, this, name);
        ownerTeam = regTeam(name);//ownerBoard.registerNewTeam("_" + name);
        create();
        p.setScoreboard(ownerBoard);
    }
    
    public CustomScore(final String botName) {
        name = botName;
        botBoard = true;
        ownerBoard = Bukkit.getScoreboardManager().getNewScoreboard();
        sideBar = null;
        ownerTeam = regTeam(name);//ownerBoard.registerNewTeam("_" + name);
        create();
    }
    
    private void create() {
        //ownerTeam.addEntry(name);
        ownerTeam.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER); //кого добавить в эту тиму, его ник скроется
        ownerTeam.setCanSeeFriendlyInvisibles(false);
        //при входе на серв не бота - всосать данные с других
        if (!botBoard) {
            for (CustomScore otherScore : boards.values()) {
                updTeam(otherScore.name, otherScore.ownerTeam); // закинуть данные с борд других
                /*if (!otherScore.visible) { //у другого скрыт ник - добавить в тим этой борды
                    ownerTeam.addEntry(otherScore.name);
                } else {
                }*/
            }
        }
        boards.put(name, this); //добавлять после перебора других борд!
    }

    //при дисконнекте владельца борды
   /* public void onQuit() {
        boards.remove(name);
        Team team;
        for (CustomScore otherScore : boards.values()) {
            if (!visible) { //владелец этой борды был скрыт
                otherScore.ownerTeam.removeEntry(name); //удалить его запись
            } else { //вычистить данные этой борды у других
                team = otherScore.ownerBoard.getTeam("_"+name);
                if ( team !=null) {//if (otherScore.registeredTeams.remove( name)!=null) {
                    team.unregister();//otherScore.unregTeam(name);//score.ownerBoard.getTeam("_"+name).unregister();
                }
            }
        }
        remove();
        ownerBoard = null;
    }*/
    
    public void remove() {
        if (ownerBoard==null) {
            return;
        }
        boards.remove(name);
        Team team;
        for (final CustomScore otherScore : boards.values()) {
            team = otherScore.ownerBoard.getTeam("_"+name);
            if ( team !=null) {//if (otherScore.registeredTeams.remove( name)!=null) {
                team.unregister();//otherScore.unregTeam(name);//score.ownerBoard.getTeam("_"+name).unregister();
            }
            /*if (!visible) { //владелец этой борды был скрыт
                otherScore.ownerTeam.removeEntry(name); //удалить его запись
            } else { //вычистить данные этой борды у других
            }*/
        }
        below(false);//removeBelow();
        for (final Objective ob : ownerBoard.getObjectives()) {
            ob.unregister();
        }
        for (final Team tm : ownerBoard.getTeams()) {
            tm.unregister();
        }
        ownerBoard = null;
    }    
    //владелец борды увидел target (только игрока, не бота!)
    public void startTrack(final String target) {
        if (ownerBoard!=null) { //скрыть тэг цели
            ownerTeam.addEntry(target);
        }
    }

    //владелец борды больше не видит target (только игрока, не бота!)
    public void stopTrack(final String target) {
        if (ownerBoard!=null) { //убрать из списка, чтобы не замусоривать
            ownerTeam.removeEntry(target);
        } 
    }

    
    
    /*public void tag (final boolean visible) {
        if (PM.exist(name)) {
            Ostrov.log_warn("CustomScore : тэги игрока ставить через Oplayer.tag !!!");
            PM.getOplayer(name).tag(visible);
            return;
        }
        if (ownerBoard==null) {
            Ostrov.log_warn("CustomScore : ставится тэк на выключенную борду : "+name);
            return;
        }
        if (visible) {
            if (!this.visible) { //сейчас скрыто
                this.visible = true;
                for (CustomScore otherScore : boards.values()) {
                    if (otherScore.name.equals(name)) continue; //самому себе не надо
                    otherScore.ownerTeam.removeEntry(name);
                    otherScore.updTeam(name, ownerTeam);
                }
            }
        } else {
            if (this.visible) { //сейчас не скрыто
                this.visible = false;
                Team team;
                for (CustomScore otherScore : boards.values()) {
                    if (otherScore.name.equals(name)) continue; //самому себе не добавлять
                    team = otherScore.ownerBoard.getTeam("_"+name);
                    if ( team !=null) {//otherScore.registeredTeams.remove( name)!=null) { //убрать инфо о тиме владельца борды, или ник не скроется
                        team.unregister();//otherScore.unregTeam(name);//score.ownerBoard.getTeam("_"+name).unregister();
                    } 
                    otherScore.ownerTeam.addEntry(name);
                }
            }
        }
    }*/
    
    /*public void tab(final String prefix, final String nameColor, final String suffix) {
        if (PM.exist(name)) {
            Ostrov.log_warn("CustomScore : тэги игрока ставить через Oplayer.tag !!!");
            final Oplayer op = PM.getOplayer(name);
            op.tag(prefix, suffix);
            return;
        }
        if (ownerBoard==null) {
            Ostrov.log_warn("CustomScore : ставится тэк на выключенную борду : "+name);
            return;
        }
        ownerTeam.prefix(TCUtils.format(prefix));
        ownerTeam.suffix(TCUtils.format(suffix));
        ownerTeam.color(TCUtils.chatColorFromString(nameColor));
        for (CustomScore otherScore : boards.values()) {
            if (otherScore.name.equals(name)) continue; //самому себе не надо
            otherScore.updTeam(name, ownerTeam); //в борды других игроков закинуть префиксы этой борды
        }
        if (!visible) { //сейчас скрыто
            tag(true);//showNameTag();//там же сделает updTeam
        } else { //обновить префиксы в других бордах
        }
    }*/
    
    //добавить в эту борду тимы других игроков
    private void updTeam(final String otherName, final Team otherTeam) {
        Team t = ownerBoard.getTeam("_"+otherName);//registeredTeams.get(otherName);
        if (t==null) {
            t = ownerBoard.registerNewTeam("_"+otherName);
            t.addEntry(otherName);
        }
        t.prefix(otherTeam.prefix());
        t.suffix(otherTeam.suffix());
        if (otherTeam.hasColor()) {
            t.color(NamedTextColor.nearestTo(otherTeam.color()));
        }
    }


    private Team regTeam(final String name) {
        final Team t = ownerBoard.getTeam("_"+name);//try-catch медленный
//        Ostrov.log_err("CustomScore registeredTeam : "+ex.getMessage());
        return t == null ? ownerBoard.registerNewTeam("_"+name) : t;
    }

    public static void allStartTrack(final String name) {
        for (final CustomScore cs : boards.values()) {
            cs.ownerTeam.addEntry(name);
        }
    }

    public static void allStopTrack(final String name) {
        for (final CustomScore cs : boards.values()) {
            cs.ownerTeam.removeEntry(name);
        }
    }






    @Deprecated
    public ScoreBoardBelow getBelow() {
        return new ScoreBoardBelow(this, name, 0);//below;
    }

    @Deprecated
    public void below(final String text, final int value) {
        //хз, коряво работает - показывает у другит вместо своей борды
        /*if (belowObj==null) {
            belowObj = ownerBoard.registerNewObjective("below", Criteria.DUMMY, TCUtils.format(text));
            belowObj.setDisplaySlot(DisplaySlot.BELOW_NAME);
            belowObj.displayName(TCUtils.format(text));
            //below = new ScoreBoardBelow(this, below_line, value);
        } else if (belowScore!=null && belowText!=null && !belowText.equals(text)) { //текст изменился
            ownerBoard.resetScores(belowScore.getEntry());
        }
        belowText = text;
        belowValue = value;
        belowScore = belowObj.getScore(text);
        belowScore.setScore(value);
Ostrov.log("belowObj="+belowObj+"belowScore="+belowScore);
        // else {
        //    below.update(below_line, value);
        //}*/
    }

    @Deprecated
    public void below(final boolean show) {
       /* if (show) {
            if (belowText!=null) {
                below(belowText, belowValue);
            }
        } else {
            if (belowObj!=null) {
                if (belowScore!=null) {
                    ownerBoard.resetScores(belowScore.getEntry());
                    belowScore = null;
                }
                belowObj.unregister();
                belowObj = null;
            }
        }*/
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


    
    
    @Deprecated
    public void removeBelow() {
        //if (below != null) {
       //     below.scores.stream().forEach( sc -> ownerBoard.resetScores(sc.getEntry()));
       //     below.scores.clear();
       //     //final Iterator<Score> iterator = below.getScores().iterator();
            //while (iterator.hasNext()) {
            //    ownerBoard.resetScores(iterator.next().getEntry());
            //}
          //  below.getObjective().unregister();
         //   below = null;
        //}
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
    