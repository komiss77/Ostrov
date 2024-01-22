package ru.komiss77.modules.bots;

import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import com.destroystokyo.paper.entity.ai.Goal;
import ru.komiss77.modules.world.WXYZ;


public class TestBot extends BotEntity {

	public TestBot(final String name, final WXYZ loc) {
		super(name, loc.w);
		telespawn(loc.getCenterLoc(), null);
		//TCUtils.N + "[" + TCUtils.P + "Bot" + TCUtils.N + "] 
		updateTag("", "", '7');
	}
	
	@Override
	public Goal<Mob> getGoal(final Mob org) {
		return new TestGoal(this, org);
	}
	
	@Override
	public void onDamage(final EntityDamageEvent e) {
		if (e instanceof final EntityDamageByEntityEvent ee) {
			if (ee.getDamager() instanceof final Player pl) {
				//QuestManager.complete(pl, PM.getOplayer(pl, LobbyPlayer.class), Quests.greet);
			}
		}
		super.onDamage(e);
	}
	
	@Override
	public void onDeath(EntityDeathEvent e) {
		remove();
	}

/*
    //лестницы-
    //стрейфы
    public static final String nm = "";
    public static final EnumChatFormat clr = EnumChatFormat.h;

    private final World w;
    private final String name;
    public final Mob rplc;
    public final int rid;

    public Bot(final Spot start, final BotType bt) {
        super(Main.ds, BotManager.getNMSWrld(start.getWorld()), new GameProfile(UUID.randomUUID(),
                BotManager.names[Main.rnd.nextInt(BotManager.names.length)]), null);
        //final Pair<String, String> pr = getSkin("litb");
        final Pair<String, String> pr = bt.txs[Main.rnd.nextInt(bt.txs.length)];
        this.fy().getProperties().put("textures", new Property("textures", pr.getFirst(), pr.getSecond()));
        final XYZ loc = start.getLoc();
        this.setPosRaw(loc.x + 0.5d, loc.y + 0.1d, loc.z + 0.5d, true);
        //MainLis.bott = true;
        //this.this = this;
        this.w = start.getWorld();
        this.rplc = (Mob) w.spawnEntity(new Location(w, loc.x + 0.5d, loc.y + 0.1d, loc.z + 0.5d), EntityType.HUSK, false);
        this.rid = rplc.getEntityId();
        this.name = this.fy().getName();
//		this.bt = bt;
//		this.atkCD = 0;
//		this.strX = 0;
//		this.strZ = 0;
        rplc.setSilent(true);
        Bukkit.getMobGoals().removeAllGoals(rplc);
        Bukkit.getMobGoals().addGoal((Husk) rplc, 0, new BotGoal(this));
        //rplc.setInvisible(true);
        //rplc.setAI(false);
        BotManager.sendWrldPckts(this.s,
                new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.a, this),
                new PacketPlayOutNamedEntitySpawn(this),
                new PacketPlayOutEntityDestroy(rid));
        final Scoreboard sb = this.c.aF();
        final ScoreboardTeam st = sb.g(name);
        //st.b(IChatBaseComponent.a("§7"));
        st.a(clr);
        BotManager.sendWrldPckts(this.s,
                PacketPlayOutScoreboardTeam.a(st),
                PacketPlayOutScoreboardTeam.a(st, true),
                PacketPlayOutScoreboardTeam.a(st, name, PacketPlayOutScoreboardTeam.a.a),
                PacketPlayOutScoreboardTeam.a(st, false));
        sb.d(st);
        //TitleManager.hideTag(this.s);
        BotManager.npcs.put(rid, this);
        //Ostrov.log_warn("Bot at " + rplc.getLocation().toString());
    }


    protected boolean tryLadder(final Block on, final Vector vc) {
        final BlockFace bf = getVec4Face(vc);
        if (on.getType() == Material.LADDER && ((Directional) on.getBlockData()).getFacing().getOppositeFace() == bf) {
            if (vc.getY() > 0d) {
                rplc.setVelocity(new Vector(bf.getModX(), 0, bf.getModZ()).multiply(0.4d));
            } else {
                final Location loc = rplc.getLocation();
                rplc.setVelocity(new Vector((loc.getBlockX() + 0.5d) - loc.getX(), 0d, (loc.getBlockZ() + 0.5d) - loc.getZ()).multiply(0.4d));
            }
            return true;
        } else {
            final Block up = on.getRelative(BlockFace.UP);
            if (up.getType() == Material.LADDER && ((Directional) up.getBlockData()).getFacing().getOppositeFace() == bf) {
                rplc.setVelocity(new Vector(bf.getModX(), 2.4d, bf.getModZ()).multiply(0.1d));
                return true;
            }
        }
        return false;
    }

    protected boolean tryJump(final Location loc, final Vector vc) {
        if (rplc.isOnGround()) {
            double lx = loc.getX(), lz = loc.getZ();
            final int dHt = 3;
            final XYZ blc = new XYZ(w.getName(), 0, loc.getBlockY() - dHt, 0);
            //Bukkit.broadcast(Component.text("1"));
            final int tHt = dHt + 3;
            for (int i = 0; i < 5; i++) {
                lx += vc.getX();
                lz += vc.getZ();
                blc.x = (int) Math.floor(lx);
                blc.z = (int) Math.floor(lz);
                if (checkIfPass(w, blc, BlockFace.UP, tHt, false) != tHt) {
                    if (i == 0) {
                        break;
                    }
                    blc.y += tHt;
                    final int upY = tHt - checkIfPass(w, blc, BlockFace.DOWN, tHt + 2, false);
                    //Bukkit.broadcast(Component.text("u=" + upY));
                    //Bukkit.broadcast(Component.text("i=" + i));
                    if (upY < 4) {
                        /*final Vector vec = upY > 2 ? 
							vc.clone().multiply((i) * 0.16d + (upY) * 0.1d - 0.02d).setY(0.42d) :
							vc.clone().multiply((i) * 0.16d + (upY) * 0.1d - 0.02d).setY(0.42d);*
                        rplc.setVelocity(upY > 2 ? vc.clone().multiply((i) * 0.15d + (upY) * 0.14d - 0.08d).setY(0.42d)
                                : vc.clone().multiply((i) * 0.15d + (upY) * 0.12d - 0.08d).setY(0.42d));
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private int checkIfPass(final World w, final XYZ tst, final BlockFace bf, final int num, final boolean inv) {
        for (int i = 0; i < num; i++) {

            final boolean cll = VM.getNmsServer().getFastMat(w, tst.x + (bf.getModX() * i),
                    tst.y + (bf.getModY() * i), tst.z + (bf.getModZ() * i)).isCollidable();
            if (inv ? !cll : cll) {
                return i;
            }
        }
        return num;
    }

    protected void move(final Location loc, final Vector vc, final boolean look) {
        if (look) {
            loc.setDirection(vc);
        }
        final Vec3D ps = this.cY();
        this.b(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
        //loc.getWorld().playSound(loc, Sound.ENTITY_SHEEP_STEP, 1f, 1.2f);
        final Vector dl = new Vector(loc.getX() - ps.c, loc.getY() - ps.d, loc.getZ() - ps.e);
        BotManager.sendWrldPckts(this.s,
                new PacketPlayOutEntityHeadRotation(this, (byte) (loc.getYaw() * 256 / 360)),
                new PacketPlayOutRelEntityMoveLook(this.ae(), (short) (dl.getX() * 4096), (short) (dl.getY() * 4096), (short) (dl.getZ() * 4096), (byte) (loc.getYaw() * 256 / 360), (byte) (loc.getPitch() * 256 / 360), false));
    }

    public void hurt(final LivingEntity dmgr) {
        //final Location loc = rplc.getLocation();
        //BotManager.sendWrldPckts(this.s, new PacketPlayOutAnimation(this, 1));
        //loc.getWorld().playSound(loc, Sound.ENTITY_GENERIC_HURT, 1f, 1f);
        //Ostrov.sync(() -> dmgr.attack(rplc));
        //tgt = dmgr != null && dmgr instanceof LivingEntity ? (LivingEntity) dmgr : tgt;*
    }

    public void remove(final boolean anmt, final boolean npc) {
        if (npc) {
            BotManager.npcs.remove(rid);
        }
        if (anmt) {
            w.spawnParticle(Particle.SOUL, rplc.getLocation(), 40, 0.4d, 1.2d, 0.4d, 0d, null, false);
        }
        if (rplc != null) {
            rplc.remove();
        }
        BotManager.sendWrldPckts(this.s,
                new PacketPlayOutEntityDestroy(this.ae()),
                new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.e, this));
        this.a(RemovalReason.a);
    }

    /*private void attackMelee(final double dstS) {
		if (dstS < 8d && tgt.getNoDamageTicks() == 0) {
			this.atkCD = atkCD == 0 ? atkCD : atkCD - 1;
			final Location loc = rplc.getEyeLocation();
			final ItemStack hnd = rplc.getEquipment().getItemInMainHand();
			if (this.v(0.5f) == 0f) {
				final int dmg = getDmgFrom(hnd);
			}
			tgt.damage(1d, tgt.getType() == EntityType.PLAYER ? this.getBukkitLivingEntity() : rplc);
			tgt.setNoDamageTicks(12);
			loc.getWorld().playSound(loc, Sound.ENTITY_PLAYER_ATTACK_STRONG, 1f, 1.2f);
			BotManager.sendWrldPckts(this.s, new PacketPlayOutAnimation(this, 0));
			rplc.getPathfinder().stopPathfinding();
		}
	}

	private int getDmgFrom(ItemStack hnd) {
		
		return 0;
	}*
    protected void pickupIts(final Location loc) {
        for (final Item it : w.getEntitiesByClass(Item.class)) {
            //rplc.getWorld().getPlayers().get(0).sendMessage(loc.distanceSquared(it.getLocation()) + "");
            if (loc.distanceSquared(it.getLocation()) < 4d) {
                final ItemStack is = it.getItemStack();
                final EntityEquipment eq = rplc.getEquipment();
                final EquipmentSlot es = is.getType().getEquipmentSlot();
                final ItemStack eqi = eq.getItem(es);
                if (eqi == null || eqi.getType() == Material.AIR) {
                    eq.setItem(es, is);
                    it.remove();
                    w.playSound(loc, Sound.ENTITY_ITEM_PICKUP, 1f, 1.2f);
                    BotManager.sendWrldPckts(this.s, new PacketPlayOutEntityEquipment(this.ae(), updateIts()));
                } else if (es.ordinal() < 2 && eqi.getType().getMaxDurability() < is.getType().getMaxDurability()) {
                    w.dropItemNaturally(loc, eqi);
                    eq.setItem(es, is);
                    it.remove();
                    w.playSound(loc, Sound.ENTITY_ITEM_PICKUP, 1f, 1.2f);
                    BotManager.sendWrldPckts(this.s, new PacketPlayOutEntityEquipment(this.ae(), updateIts()));
                }
            }
        }
    }

    private List<Pair<EnumItemSlot, net.minecraft.world.item.ItemStack>> updateIts() {
        final EntityEquipment eq = rplc.getEquipment();
        final EquipmentSlot[] ess = EquipmentSlot.values();
        final EnumItemSlot[] eis = EnumItemSlot.values();
        @SuppressWarnings("unchecked")
        final Pair<EnumItemSlot, net.minecraft.world.item.ItemStack>[] its = (Pair<EnumItemSlot, net.minecraft.world.item.ItemStack>[]) new Pair<?, ?>[6];
        for (int i = its.length - 1; i >= 0; i--) {
            final ItemStack it = eq.getItem(ess[i]);
            its[i] = Pair.of(eis[i], net.minecraft.world.item.ItemStack.fromBukkitCopy(it == null ? Main.air : it));
        }
        return Arrays.asList(its);
    }

    public void updateAll(final NetworkManager nm) {
//nm.getPlayer().getBukkitEntity().sendMessage("updated");
        nm.a(new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.a, this));
        nm.a(new PacketPlayOutNamedEntitySpawn(this));
        nm.a(new PacketPlayOutEntityDestroy(rid));
        nm.a(new PacketPlayOutEntityHeadRotation(this, (byte) (this.getBukkitYaw() * 256 / 360)));
        nm.a(new PacketPlayOutEntity.PacketPlayOutRelEntityMoveLook(this.ae(), (short) 0, (short) 0, (short) 0, (byte) 0, (byte) 0, false));
        nm.a(new PacketPlayOutEntityEquipment(this.ae(), updateIts()));
        final Scoreboard sb = this.c.aF();
        final ScoreboardTeam st = sb.g(name);
        //st.b(IChatBaseComponent.a("§7"));
        st.a(clr);
        nm.a(PacketPlayOutScoreboardTeam.a(st));
        nm.a(PacketPlayOutScoreboardTeam.a(st, true));
        nm.a(PacketPlayOutScoreboardTeam.a(st, name, PacketPlayOutScoreboardTeam.a.a));
        nm.a(PacketPlayOutScoreboardTeam.a(st, false));
        sb.d(st);
    }

	public void updateAll(final NetworkManager nm) {
		nm.a(new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.a, this));
		nm.a(new PacketPlayOutNamedEntitySpawn(this));
		nm.a(new PacketPlayOutEntityDestroy(rid));
		nm.a(new PacketPlayOutEntityHeadRotation(this, (byte) (this.getBukkitYaw() * 256 / 360)));
		nm.a(new PacketPlayOutEntity.PacketPlayOutRelEntityMoveLook(this.ae(), (short) 0, (short) 0, (short) 0, (byte) 0, (byte) 0, false));
		nm.a(new PacketPlayOutEntityEquipment(this.ae(), updateIts()));
		final Scoreboard sb = this.c.aF();
		final ScoreboardTeam st = sb.g(name);
		//st.b(IChatBaseComponent.a("§7"));
		st.a(clr);
		nm.a(PacketPlayOutScoreboardTeam.a(st));
		nm.a(PacketPlayOutScoreboardTeam.a(st, true));
		nm.a(PacketPlayOutScoreboardTeam.a(st, name, PacketPlayOutScoreboardTeam.a.a));
		nm.a(PacketPlayOutScoreboardTeam.a(st, false));
		sb.d(st);
	}

	private BlockFace getVec4Face(final Vector vc) {
		if (Math.abs(vc.getX()) > Math.abs(vc.getZ())) {
			if (vc.getX() > 0) {
				return BlockFace.EAST;
			} else {
				return BlockFace.WEST;
			}
		} else {
			if (vc.getZ() > 0) {
				return BlockFace.SOUTH;
			} else {
				return BlockFace.NORTH;
			}
		}
	}*/


}
