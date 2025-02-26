package ru.komiss77.modules.signProtect;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import ru.komiss77.LocalDB;
import ru.komiss77.Timer;
import ru.komiss77.utils.StringUtil;


public class ProtectionData {
    public int valid;
    public String owner;
    public List<String> users;

    public ProtectionData(final String ownerName) {
//Ostrov.log("new ProtectionData ownerName");
        owner = ownerName;
        valid = Timer.secTime() + 2592000; //30*24*60*60
        users = new ArrayList<>();// List.of();
    }

    public ProtectionData(final Sign sign) { //для редактора - лист должен быть изменяемый!
        String data = sign.getPersistentDataContainer().get(SignProtect.KEY, PersistentDataType.STRING);
        final int idx1 = data.indexOf(LocalDB.W_SPLIT);
        final int idx2 = data.indexOf(LocalDB.L_SPLIT);
        valid = Integer.parseInt(data.substring(0, idx1));
        owner = data.substring(idx1 + 1, idx2);
        data = data.substring(idx2 + 1);
        users = data.length() > 1 ? new ArrayList<>(List.of(data.split(","))) : new ArrayList<>(); //для редактора - лист должен быть изменяемый!
    }

    public boolean isValid() {
        return valid == -1 || valid > Timer.secTime();
    }

    public boolean isOwner(final Player p) {
        return owner.equalsIgnoreCase(p.getName());
    }

    public boolean canUse(final Player p) {
        return owner.equalsIgnoreCase(p.getName()) || users.contains(p.getName());
    }


    private static final ProtectionData pd;

    static {
        pd = new ProtectionData("");
    }

    public static ProtectionData of(final Sign sign) {//для одноразовых проверочек, чтобы не плодить экземпляры
        String data = sign.getPersistentDataContainer().get(SignProtect.KEY, PersistentDataType.STRING);
        final int idx1 = data.indexOf(LocalDB.W_SPLIT);
        final int idx2 = data.indexOf(LocalDB.L_SPLIT);
        pd.valid = Integer.parseInt(data.substring(0, idx1));
        pd.owner = data.substring(idx1 + 1, idx2);
        data = data.substring(idx2 + 1);
        pd.users = data.length() > 1 ? List.of(data.split(",")) : List.of();
        return pd;
    }

    @Override
    public String toString() {
        return valid + LocalDB.WORD_SPLIT + owner + LocalDB.LINE_SPLIT + StringUtil.toString(users, ",");
    }

}


