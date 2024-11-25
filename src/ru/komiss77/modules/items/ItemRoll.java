package ru.komiss77.modules.items;

import org.bukkit.inventory.ItemStack;
import ru.komiss77.modules.rolls.Roll;
import ru.komiss77.utils.ItemUtil;

public class ItemRoll extends Roll<ItemStack> {

    private static final String SEP = "=";

    public ItemRoll(final String id, final ItemStack it) {
        super(id, it, it.getAmount(), 0);
    }

    public ItemRoll(final String id, final ItemStack it, final int number) {
        super(id, it, number, 0);
    }

    public ItemRoll(final String id, final ItemStack it, final int number, final int extra) {
        super(id, it, number, extra);
    }

    @Override
    protected ItemStack asAmount(final int amt) {
        it.setAmount(amt);
        return it;
    }

    @Override
    protected String encode() {
        return ItemUtil.toString(it, SEP);
    }

    public static void loadAll() {
        load(ItemRoll.class, cs -> new ItemRoll(cs.getName(),
            ItemUtil.parseItem(cs.getString(VAL), SEP),
            cs.getInt(NUM, 0), cs.getInt(EX, 0)));
    }
}
