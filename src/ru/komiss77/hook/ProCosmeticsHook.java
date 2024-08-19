package ru.komiss77.hook;

import org.bukkit.plugin.Plugin;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Ostrov;
import ru.komiss77.enums.Data;
import ru.komiss77.modules.player.PM;
import se.file14.procosmetics.ProCosmetics;
import se.file14.procosmetics.api.ProCosmeticsProvider;
import se.file14.procosmetics.economy.EconomyFailureException;
import se.file14.procosmetics.economy.IEconomyProvider;
import se.file14.procosmetics.user.User;


public class ProCosmeticsHook implements IEconomyProvider {

    public static final ProCosmeticsHook provider;

    static {
        provider = new ProCosmeticsHook();
    }

    public static void hook(final Plugin plugin) {
        ProCosmetics api = ProCosmeticsProvider.get();
        api.getEconomyManager().register(provider);
        Ostrov.log_ok("§bПодключен ProCosmetics!");
    }


    @Override
    public String getPlugin() {
        return "Ostrov";
    }

    @Override
    public void hook(ProCosmetics proCosmetics) throws EconomyFailureException {
    }

    @Override
    public void addCoins(User user, int i) {
        ApiOstrov.moneyChange(user.getPlayer(), i, "Косметика");
    }

    @Override
    public void setCoins(User user, int i) {
        Ostrov.log_warn("Косметика пытается установить баланс, отказано.");
    }

    @Override
    public int getCoins(User user) {
        return PM.getOplayer(user.getPlayer()).getDataInt(Data.LONI);
    }

    @Override
    public boolean hasCoins(User user, int i) {
        return PM.getOplayer(user.getPlayer()).getDataInt(Data.LONI) >= i;
    }

    @Override
    public void removeCoins(User user, int i) {
        ApiOstrov.moneyChange(user.getPlayer(), -i, "Косметика");
    }
}
