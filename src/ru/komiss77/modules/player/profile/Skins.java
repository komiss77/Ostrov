package ru.komiss77.modules.player.profile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import com.destroystokyo.paper.profile.CraftPlayerProfile;
import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import ru.komiss77.Ostrov;
import ru.komiss77.objects.CaseInsensitiveMap;

public class Skins {

    private static final CaseInsensitiveMap<PlayerProfile> named = new CaseInsensitiveMap<>();
    private static final HashMap<UUID, PlayerProfile> uided = new HashMap<>();

    public static GameProfile game(final PlayerProfile pp) {
        final GameProfile gp = new GameProfile(pp.getId() == null
            ? UUID.randomUUID() : pp.getId(), pp.getName());
        for (final ProfileProperty pr : pp.getProperties()) {
            gp.getProperties().put(pr.getName(),
                new Property(pr.getName(), pr.getValue(), pr.getSignature()));
        }
        return gp;
    }

    public static CompletableFuture<PlayerProfile> future(final String name) {
        final CompletableFuture<PlayerProfile> comp = new CompletableFuture<>();
        final Player pl = Bukkit.getPlayerExact(name);
        if (pl != null) {
            final PlayerProfile pp = pl.getPlayerProfile();
            uided.put(pl.getUniqueId(), pp);
            named.put(name, pp);
            comp.complete(pp); return comp;
        }
        final PlayerProfile pp = named.computeIfAbsent(name,
            nm -> new CraftPlayerProfile(null, nm));
        if (pp.isComplete()) {
            comp.complete(pp); return comp;
        }
        Ostrov.async(() -> {
            try {
                final InputStreamReader irn = new InputStreamReader(URI
                    .create("https://api.mojang.com/users/profiles/minecraft/" + name).toURL().openStream());
                final String id = (String) ((JSONObject) new JSONParser().parse(irn)).get("id");
                if (id.length() != 32) {
                    Ostrov.log_warn("Incorrect UUID format!");
                    return;
                }
                Ostrov.log_ok("UUID got for " + name);
                final UUID uid = UUID.fromString(new StringBuilder(id).insert(20, '-')
                    .insert(16, '-').insert(12, '-').insert(8, '-').toString());
                final PlayerProfile ppID = uided.computeIfAbsent(uid, u -> new CraftPlayerProfile(u, name));
                if (!ppID.complete(true, true)) return;
                Ostrov.log_ok("Skin registered for " + name);
                uided.put(uid, ppID); named.put(name, ppID);
                Ostrov.sync(() -> comp.complete(ppID));
            } catch (IOException | ParseException e) {}
        });
        return comp;
    }

    public static void future(final String name, final Consumer<PlayerProfile> cn) {
        future(name).whenComplete((pp, t) -> cn.accept(pp));
    }

    public static PlayerProfile present(final String name) {
        final CompletableFuture<PlayerProfile> comp = future(name);
        try {
            return comp.isDone() ? comp.get() : new CraftPlayerProfile(null, name);
        } catch (InterruptedException | ExecutionException e) {
            return new CraftPlayerProfile(null, name);
        }
    }

    public static CompletableFuture<PlayerProfile> future(final UUID id) {
        final CompletableFuture<PlayerProfile> comp = new CompletableFuture<>();
        final Player pl = Bukkit.getPlayer(id);
        if (pl != null) {
            final PlayerProfile pp = pl.getPlayerProfile();
            uided.put(id, pp);
            comp.complete(pp); return comp;
        }
        final PlayerProfile pp = uided.computeIfAbsent(id,
            u -> new CraftPlayerProfile(u, ""));
        if (pp.isComplete()) {
            comp.complete(pp);
            return comp;
        }
        Ostrov.async(() -> {
            if (!pp.complete(true, true)) return;
            Ostrov.log_ok("Skin registered for " + id.toString());
            uided.put(id, pp); Ostrov.sync(() -> comp.complete(pp));
            /*try {
                final InputStreamReader irn = new InputStreamReader(URI
                    .create("https://api.mojang.com/users/profiles/minecraft/" + name).toURL().openStream());
                final String id = (String) ((JSONObject) new JSONParser().parse(irn)).get("id");
                if (id.length() != 32) {
                    Ostrov.log_warn("Incorrect UUID format!");
                    return;
                }
                final UUID uid = UUID.fromString(new StringBuilder(id).insert(20, '-')
                    .insert(16, '-').insert(12, '-').insert(8, '-').toString());

                final InputStreamReader tsr = new InputStreamReader(URI
                    .create("https://sessionserver.mojang.com/session/minecraft/profile/" + id + "?unsigned=false").toURL().openStream());
                final JSONObject ppt = ((JSONObject) ((JSONArray) ((JSONObject) new JSONParser().parse(tsr)).get("properties")).getFirst());
                name_skin.put(name, new Duo<>((String) ppt.get("value"), (String) ppt.get("signature")));
                id_skin.put(uid, new Duo<>((String) ppt.get("value"), (String) ppt.get("signature")));
            } catch (NullPointerException | IOException | ParseException e) {}*/
        });
        return comp;
    }

    public static void future(final UUID id, final Consumer<PlayerProfile> cn) {
        future(id).whenComplete((pp, t) -> cn.accept(pp));
    }

    public static PlayerProfile present(final UUID id) {
        final CompletableFuture<PlayerProfile> comp = future(id);
        try {
            return comp.isDone() ? comp.get() : new CraftPlayerProfile(id, "");
        } catch (InterruptedException | ExecutionException e) {
            return new CraftPlayerProfile(null, "");
        }
    }
}
