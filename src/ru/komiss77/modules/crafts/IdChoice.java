package ru.komiss77.modules.crafts;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.persistence.PersistentDataType;
import ru.komiss77.modules.items.ItemGroup;
import ru.komiss77.utils.ItemUtil;

public class IdChoice implements RecipeChoice {

    private final String id;
    private final List<ItemType> choices;

    public static RecipeChoice of(final @Nullable ItemStack it) {
        if (ItemUtil.isBlank(it, false)) return RecipeChoice.empty();
        final String id = it.getPersistentDataContainer().get(ItemGroup.KEY, PersistentDataType.STRING);
        return new IdChoice(id, it.getType());
    }

    public IdChoice(final @Nullable String id, final List<ItemType> choices) {
        this.id = id;
        this.choices = choices;
        //Bukkit.broadcast(Component.text("mt-" + mt[0].toString() + ", cmd-" + cmd));
    }

    public IdChoice(final @Nullable String id, final ItemType... mt) {
        this.id = id;
        this.choices = Arrays.asList(mt);
        //Bukkit.broadcast(Component.text("mt-" + mt[0].toString() + ", cmd-" + cmd));
    }

    @Deprecated
    public IdChoice(final @Nullable String id, final Material... mt) {
        this.id = id;
        this.choices = Arrays.stream(mt).map(Material::asItemType).toList();
        //Bukkit.broadcast(Component.text("mt-" + mt[0].toString() + ", cmd-" + cmd));
    }

    private static final Material[] emt = {null};

    @Override
    public IdChoice clone() {
        return new IdChoice(id, choices);
    }

    @Override
    public ItemStack getItemStack() {
        final ItemType mt = choices.getFirst();
        final ItemGroup cmts = ItemGroup.get(id);
        final ItemStack ci = cmts == null ? null : cmts.item(mt);
        if (ci != null) return ci.asOne();
        final ItemStack it = mt.createItemStack();
        return ItemUtil.isBlank(it, false) ? ItemUtil.air : it;
    }

    @Override
    public boolean test(final ItemStack it) {
        if (it == null) return choices.contains(ItemType.AIR);
        if (!choices.contains(it.getType())) return false;
        return Objects.equals(id, it.getPersistentDataContainer()
            .get(ItemGroup.KEY, PersistentDataType.STRING));
    }

    @Override
    public boolean equals(final Object o) {
        if (o instanceof final IdChoice oic) {
            for (final ItemType it : oic.choices) {
                if (!choices.contains(it)) return false;
            }
            return Objects.equals(oic.id, id);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return super.hashCode() + Objects.hashCode(id);
    }
}
