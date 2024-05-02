package ru.komiss77.modules.quests;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import eu.endercentral.crazy_advancements.NameKey;
import eu.endercentral.crazy_advancements.advancement.Advancement;
import eu.endercentral.crazy_advancements.advancement.AdvancementDisplay;
import eu.endercentral.crazy_advancements.advancement.AdvancementDisplay.AdvancementFrame;
import eu.endercentral.crazy_advancements.advancement.AdvancementVisibility;
import eu.endercentral.crazy_advancements.advancement.ToastNotification;
import eu.endercentral.crazy_advancements.advancement.criteria.Criteria;
import eu.endercentral.crazy_advancements.event.AdvancementScreenCloseEvent;
import eu.endercentral.crazy_advancements.manager.AdvancementManager;
import net.kyori.adventure.bossbar.BossBar;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Config;
import ru.komiss77.LocalDB;
import ru.komiss77.Ostrov;
import ru.komiss77.events.LocalDataLoadEvent;
import ru.komiss77.events.QuestCompleteEvent;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.quests.Quest.QuestFrame;
import ru.komiss77.modules.quests.progs.IProgress;
import ru.komiss77.utils.DonatEffect;
import ru.komiss77.utils.TCUtils;

//     https://www.spigotmc.org/resources/crazy-advancements-api.51741/



public class AdvanceCrazy implements IAdvance, Listener {
	
    private static final AdvancementManager mgr = new AdvancementManager(new NameKey("ostrov", "pls"));
    private static final Map<Quest,Advancement> adm = new HashMap<>();
    private static Quest[] roots = new Quest[0]; //для отправки в порядке наслодования, или не отображаются некоторые
    protected static Consumer<Player> onAdvCls = p -> {};

  public AdvanceCrazy() {
    Bukkit.getPluginManager().registerEvents(this, Ostrov.getInstance());
  }

    public void buildAdv() {
    	Ostrov.async(() -> {
    		adm.clear();
    		mgr.removeAdvancement(mgr.getAdvancements().toArray(new Advancement[0]));
    		final ArrayList<Quest> rts = new ArrayList<>();
    		final HashMap<Quest, ArrayList<Quest>> clds = new HashMap<>();
        	for (final Quest qs : Quest.codeMap.values()) {
    			clds.putIfAbsent(qs.parent, new ArrayList<>());
        		if (qs.parent.equals(qs)) rts.add(qs);
        		else clds.get(qs.parent).add(qs);
        	}
        	
        	final Quest[] eq = new Quest[0];
        	roots = rts.toArray(eq);
        	for (final Entry<Quest, ArrayList<Quest>> en : clds.entrySet()) {
        		en.getKey().children = en.getValue().toArray(eq);
        	}
        	
        	for (final Quest rtq : roots) setDst(rtq, 0);
        	for (final Quest rtq : roots) setAdv(rtq);
        	
        	Ostrov.log_ok("Quests reloaded and built: " + Quest.codeMap.size());
    	});
    }
    
    private static int setDst(final Quest rt, final int step) {
    	rt.size = 0;
        for (final Quest ch : rt.children) rt.size = setDst(ch, rt.size);
        if (rt.size == 0) rt.size = 1;
    	rt.dx = 0.4f; rt.dy = rt.size * 0.5f + step;
        return rt.size + step;
    }
    
    private static void setAdv(final Quest qs) {
		final float ln = (qs.children.length - 1) * 0.5f;
		for (int i = 0; i < qs.children.length; i++) {
			qs.children[i].dx += Math.max(1f - Math.abs(ln - i) * 0.2f, 0f);
			qs.children[i].dy -= qs.size * 0.5f;
		}
		createCuboidAdv(qs);
		for (final Quest cq : qs.children) setAdv(cq);
    }
    
    private static Advancement createCuboidAdv(final Quest qst) {
        //final AdvancementFlag... flags //не ставить, или при каждом входе сыплет тосты по grantAdvancement
    	final Advancement adv = adm.get(qst);
    	if (adv != null) return adv;
    	
    	final AdvancementDisplay dis = new AdvancementDisplay(qst.icon, qst.displayName, qst.description, getFrame(qst), qst.backGround, getVision(qst));
        final Advancement ad;
    	if (qst.parent.equals(qst)) {
            dis.setCoordinates(qst.dx, qst.dy);
            ad = new Advancement(new NameKey("ostrov", "oq"+qst.hashCode()), dis);
    	} else {
    		final Advancement prt = createCuboidAdv(qst.parent);
            dis.setCoordinates(qst.dx + prt.getDisplay().getX(), qst.dy + prt.getDisplay().getY());
            ad = new Advancement(prt, new NameKey("ostrov", "oq"+qst.hashCode()), dis);
        }
    	
    	if (qst.amount > 0) ad.setCriteria(new Criteria(qst.amount));
        
        adm.put(qst, ad);
        mgr.addAdvancement(ad);
    	return ad;
    }
    
    /*private static void reSize(final Quest rt) {
    	rt.size = rt.children.length == 0 ? 0 : rt.children.length - 1;
        Quest rq = rt;
        while (rq.code != ((rq = rq.parent).code)) rq.size += rt.size;
        for (final Quest ch : rt.children) reSize(ch);
    }
    
    private static int setDst(final Quest rt, final int step) {
    	rt.dx = 2f; rt.dy = rt.size * 0.5f + step;
    	int stl = 0;
        for (final Quest ch : rt.children) stl = setDst(ch, stl);
        return rt.size + step;
    }*/
    
    public void loadPlQs(final Player p, final Oplayer op) {
    	if (!Config.quests) return;
        
    	mgr.addPlayer(p);
        for (final Quest rt : roots) {
        	final IProgress prg = op.quests.get(rt);
            if (prg != null && prg.isDone()) mgr.grantAdvancement(p, adm.get(rt));
            giveChildren(p, op, rt);
        }
        
        mgr.updateVisibility(p);
    }
    
    private static boolean giveChildren(final Player p, final Oplayer op, final Quest of) {
    	boolean fnd = false;
    	for (final Quest q : of.children) {
    		final IProgress prg = op.quests.get(q);
    		if (prg == null) continue;
            if (prg.isDone()) mgr.grantAdvancement(p, adm.get(q));
            else QuestManager.iAdvance.sendProgress(p, q, prg.getProg(), true);
            giveChildren(p, op, q);
            fnd = true;
    	}
    	return fnd;
    }


    private static AdvancementVisibility getVision(final Quest q) {
        return switch (q.vision) {
            case ALWAYS -> AdvancementVisibility.ALWAYS;
            case HIDDEN -> AdvancementVisibility.HIDDEN;
            case PARENT -> AdvancementVisibility.PARENT_GRANTED;
        };
    }


	private static AdvancementFrame getFrame(final Quest q) {
        return switch (q.frame) {
            case CHALLENGE -> AdvancementFrame.CHALLENGE;
            case GOAL -> AdvancementFrame.GOAL;
			case TASK -> AdvancementFrame.TASK;
        };
	}


    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
    public void onAdvClose(final AdvancementScreenCloseEvent e) {
        if(Bukkit.isPrimaryThread()) onAdvCls.accept(e.getPlayer());
       	else Ostrov.sync(() -> onAdvCls.accept(e.getPlayer()));
    }
    
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onQsLoad(final LocalDataLoadEvent e) {
        final Oplayer op = e.getOplayer();
        final String qss = op.mysqlData.get("quests");
        if (qss == null || qss.isEmpty() || !Config.quests) return;
        final String[] split = qss.split(LocalDB.LINE_SPLIT);
//      p.sendMessage(Arrays.toString(split));
	    int stamp;
	    for (String quest : split) {
	        if (quest.isEmpty()) continue;
	        final Quest qs = QuestManager.byCode(quest.charAt(0));
	        if (qs == null) continue;
	        final int splitterIndex = quest.indexOf(LocalDB.W_SPLIT);
	        if (splitterIndex==1) {
	            stamp = ApiOstrov.getInteger(quest.substring(splitterIndex+1));
	            if (stamp>0) op.quests.put(qs, qs.createPrg(stamp));
	        } else op.quests.put(qs, qs.createPrg(0).markDone());
	    }

	    QuestManager.showForPl(e.getPlayer(), op);
    }
    
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = false)
    public static void onQuit(final PlayerQuitEvent e) {
        mgr.removePlayer(e.getPlayer());
    }
    
    
    
    public void sendToast(final Player p, final Quest q) {
    	sendToast(p, q.icon, "§aНовый квест: "+q.displayName, q.frame);
    }
    
    public void sendToast(final Player p, final Material mt, final String msg, final QuestFrame frm) {
    	new ToastNotification(mt, msg, switch (frm) {
    	case CHALLENGE -> AdvancementFrame.CHALLENGE;
    	case GOAL -> AdvancementFrame.GOAL;
    	default -> AdvancementFrame.TASK;
		}).send(p);
    }
    
    public void resetProgress(final Player p, final boolean rmv) {
        for (final Advancement ad : mgr.getAdvancements()) {
            //Error occurred (in the plugin loader) while disabling Ostrov v2.0 java.lang.IllegalStateException: zip file closed
            //at java.util.zip.ZipFile.ensureOpen(ZipFile.java:831) ~[?:?]
            //at ru.komiss77.modules.quests.AdvanceCrazy.resetProgress(AdvanceCrazy.java:222) ~[Ostrov.jar:?]
            //at ru.komiss77.modules.quests.QuestManager.onDisable(QuestManager.java:63) ~[Ostrov.jar:?]
            //at ru.komiss77.Ostrov.lambda$onDisable$1(Ostrov.java:138) ~[Ostrov.jar:?]
            mgr.revokeAdvancement(p, ad);
        }
        if (rmv) mgr.removePlayer(p);
    }
    
    public void sendComplete(final Player p, final Quest q, final boolean silent) {
        final Advancement ad = adm.get(q);
        if (ad != null && new QuestCompleteEvent(p, q).callEvent()) {
    		mgr.grantAdvancement(p, ad);
            if (!silent) {
                DonatEffect.spawnRandomFirework(p.getLocation());
                final String chatColor = TCUtils.randomColor();
                p.sendMessage(" ");
                p.sendMessage(TCUtils.format(chatColor + "§m=-=-§к §kAA §eВыполнены условия достижения §к§kAA " + chatColor + "§m-=-="));
                p.sendMessage(TCUtils.format(chatColor + q.displayName + " §f: §aКвест завершен!"));
                p.sendMessage(" ");
            	ad.displayToast(p);
            }
            mgr.updateTab(p, ad.getTab());
            ApiOstrov.addExp(p, QuestManager.QUEST_EXP);
        }
    }
    
    public void sendProgress(final Player p, final Quest q, final int progress, final boolean silent) {
        final Advancement ad = adm.get(q);
        if (ad != null) {
        	mgr.setCriteriaProgress(p, ad, progress);
        	if (!silent) {
        		ApiOstrov.sendBossbarDirect(p, "§сПрогресс : §f" + q.displayName, 4, q.getBBColor(), 
        			BossBar.Overlay.PROGRESS, q.amount == 0 ? 1f : (float) progress / q.amount);
        	}
        }
    }

  @Override
  public void unregister() {
    HandlerList.unregisterAll(this);
  }
}
