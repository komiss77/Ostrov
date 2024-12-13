package ru.komiss77.modules.crafts;

import javax.annotation.Nullable;
import java.util.Objects;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice.MaterialChoice;
import org.bukkit.persistence.PersistentDataType;
import ru.komiss77.modules.items.CustomMats;
import ru.komiss77.utils.ItemUtil;

public class IdChoice extends MaterialChoice {

    private final String id;

    public static IdChoice of(final @Nullable ItemStack it) {
        if (it == null) return new IdChoice(null, Material.AIR);
        final String id = it.getPersistentDataContainer().get(CustomMats.KEY, PersistentDataType.STRING);
        return new IdChoice(id, it.getType());
    }

    public IdChoice(final @Nullable String id, final Material... mt) {
        super(mt);
        this.id = id;
        //Bukkit.broadcast(Component.text("mt-" + mt[0].toString() + ", cmd-" + cmd));
    }

    public IdChoice(final MaterialChoice mtc) {
        super(mtc.getChoices());
        this.id = null;
    }

    private static final Material[] emt = {null};

    @Override
    public IdChoice clone() {
        return new IdChoice(id, getChoices().toArray(emt));
    }

    @Override
    public ItemStack getItemStack() {
        final Material mt = getChoices().getFirst();
        final CustomMats cmts = CustomMats.get(id);
        final ItemStack ci = cmts == null ? null : cmts.item(mt.asItemType());
        if (ci != null) return ci.asOne();
        final ItemStack it = new ItemStack(mt);
        return ItemUtil.isBlank(it, false) ? ItemUtil.air : it;
    }

    @Override
    public boolean test(final ItemStack it) {
        if (it == null) return getChoices().contains(Material.AIR);
        if (!getChoices().contains(it.getType())) return false;
        return Objects.equals(id, it.getPersistentDataContainer()
            .get(CustomMats.KEY, PersistentDataType.STRING));
    }

    @Override
    public boolean equals(final Object o) {
        if (o instanceof MaterialChoice) {
            for (final Material mt : ((MaterialChoice) o).getChoices()) {
                if (!getChoices().contains(mt)) return false;
            }
            return o instanceof IdChoice ?
                Objects.equals(((IdChoice) o).id, id) : id == null;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return super.hashCode() + Objects.hashCode(id);
    }
}
