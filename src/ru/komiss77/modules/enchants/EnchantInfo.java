package ru.komiss77.modules.enchants;

public class EnchantInfo {

    public final String rusName;
    public final short levelCost;
    public final String[] rusLore;

    public EnchantInfo(final String name, final int lvlCst, final String... lore) {
        this.rusName = name;
        this.levelCost = (short) lvlCst;
        for (int i = lore.length - 1; i >= 0; i--) {
            lore[i] = "§7" + lore[i];
        }
        this.rusLore = lore;
    }
}
