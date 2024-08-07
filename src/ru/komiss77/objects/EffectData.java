package ru.komiss77.objects;

import java.util.List;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import ru.komiss77.utils.TCUtil;

public class EffectData {

    public Particle particleEffect;
    private final Location location;

    public EffectData(final Particle particleeffect, final Location location) {
        this.particleEffect = particleeffect;
        this.location = location;
    }

    public void display() {
        if (particleEffect.getDataType() == Void.class) {

            location.getWorld().spawnParticle(particleEffect, location, 6, 1, 1, 1);

        } else if (particleEffect.getDataType() == Particle.DustOptions.class) {

            location.getWorld().spawnParticle(particleEffect, location, 6, 1, 1, 1, new Particle.DustOptions(TCUtil.randomCol(), 1));

        } else if (particleEffect.getDataType() == BlockData.class) {

        }
        //location.getWorld().playEffect(location, particleEffect, 0, 24 );
        //this.particleEffect.display(0.2F, 0.2F, 0.2F, 0.0F, 5, location, 64);
    }

    public void display(final List<Player> player_list) {
        for (Player p : player_list) {
            if (particleEffect.getDataType() == Void.class) {
                p.spawnParticle(particleEffect, location, 6, 1, 1, 1);
            } else if (particleEffect.getDataType() == Particle.DustOptions.class) {
                p.spawnParticle(particleEffect, location, 6, 1, 1, 1, new Particle.DustOptions(TCUtil.randomCol(), 1));
            }

        }
    }

}
