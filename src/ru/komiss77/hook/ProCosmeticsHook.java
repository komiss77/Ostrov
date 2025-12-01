package ru.komiss77.hook;

import java.util.concurrent.CompletableFuture;
import it.unimi.dsi.fastutil.booleans.BooleanIntPair;
import org.bukkit.plugin.Plugin;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Ostrov;
import ru.komiss77.enums.Data;
import ru.komiss77.modules.player.PM;
import se.file14.procosmetics.api.ProCosmetics;
import se.file14.procosmetics.api.ProCosmeticsProvider;
import se.file14.procosmetics.api.economy.EconomyProvider;
import se.file14.procosmetics.api.user.User;


public class ProCosmeticsHook implements EconomyProvider {

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
    public void hook(ProCosmetics proCosmetics) throws IllegalStateException {
    }

  //@Override
  //public void addCoins(User user, int i) {
  //    ApiOstrov.moneyChange(user.getPlayer(), i, "Косметика");
  //}

  //@Override
  //public void setCoins(User user, int i) {
  //    Ostrov.log_warn("Косметика пытается установить баланс, отказано.");
  //}

    @Override
    public int getCoins(User user) {
        return PM.getOplayer(user.getPlayer()).getDataInt(Data.LONI);
    }

    @Override
    public boolean hasCoins(User user, int i) {
        return PM.getOplayer(user.getPlayer()).getDataInt(Data.LONI) >= i;
    }


    @Override
    public CompletableFuture<BooleanIntPair> getCoinsAsync(User user) {
      int i = getCoins(user);
      return CompletableFuture.completedFuture(BooleanIntPair.of(true, i));
    }

  @Override
  public CompletableFuture<Boolean> addCoinsAsync(User user, int i) {
    ApiOstrov.moneyChange(user.getPlayer(), i, "Косметика");
    return CompletableFuture.completedFuture(true);
  }

  @Override
  public CompletableFuture<Boolean> setCoinsAsync(User user, int i) {
    Ostrov.log_warn("Косметика пытается установить баланс, отказано.");
    return CompletableFuture.completedFuture(true);
  }

  @Override
  public CompletableFuture<Boolean> removeCoinsAsync(User user, int i) {
        ApiOstrov.moneyChange(user.getPlayer(), -i, "Косметика");
    return CompletableFuture.completedFuture(true);
    }

  //@Override
  //public void removeCoins(User user, int i) {
  //   ApiOstrov.moneyChange(user.getPlayer(), -i, "Косметика");
  //  }
}
