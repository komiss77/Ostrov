package ru.komiss77.modules.items;

import org.bukkit.inventory.ItemStack;
import ru.komiss77.modules.rolls.Roll;
import ru.komiss77.utils.ClassUtil;
import ru.komiss77.utils.ItemUtil;

public class ItemRoll extends Roll<ItemStack> {

    public ItemRoll(final String id, final ItemStack it) {
        super(id, it, it.getAmount(), 0);
    }

    public ItemRoll(final String id, final ItemStack it, final int number) {
        super(id, it, number, 0);
    }

    public ItemRoll(final String id, final ItemStack it, final int number, final int extra) {
        super(id, it, number, extra);
    }

    public static ItemRoll get(final String id) {
        return ClassUtil.cast(Roll.get(id), ItemRoll.class);
    }

    @Override
    protected ItemStack asAmount(final int amt) {
        it.setAmount(amt);
        return it;
    }

    @Override
    protected String encode() {
        return ItemUtil.write(it);
    }

    public static void loadAll() {
        load(ItemRoll.class, cs -> new ItemRoll(cs.getName(),
            ItemUtil.parse(cs.getString(VAL)),
            cs.getInt(NUM, 0), cs.getInt(EX, 0)));
    }
}
