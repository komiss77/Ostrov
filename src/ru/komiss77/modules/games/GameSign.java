package ru.komiss77.modules.games;

import org.bukkit.Location;
import org.bukkit.Material;
import ru.komiss77.enums.Game;
import ru.komiss77.utils.BlockUtil;
import ru.komiss77.utils.TCUtil;


public class GameSign {

    public final Location signLoc;
    public final Game game;
    public final String server;
    public final String arena;

    public Location attachement_loc;
    public Material attachement_mat;

    public GameSign(final Location loc, final Game game, final String server, final String arena) {
        this.signLoc = loc;
        this.server = server;
        this.arena = arena;
        this.game = game;

        attachement_loc = BlockUtil.getSignAttachedBlock(signLoc.getBlock()).getLocation();
        attachement_mat = attachement_loc.getBlock().getType();

        if (!TCUtil.canChangeColor(attachement_mat)) {
            attachement_loc = null;
        }
    }


}
