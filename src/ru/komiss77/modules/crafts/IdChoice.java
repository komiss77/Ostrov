package ru.komiss77.modules.crafts;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.RecipeChoice.MaterialChoice;
import org.bukkit.persistence.PersistentDataType;
import ru.komiss77.modules.items.ItemGroup;
import ru.komiss77.utils.ItemUtil;
/*
//public class IdChoice extends MaterialChoice {
public final class IdChoice {

    private final String id;
    private static Map<String, RecipeChoice> ids;
    static {
      ids = new HashMap<>();
    }

   /* public static RecipeChoice of(final @Nullable ItemStack it) {
        if (ItemUtil.isBlank(it, false)) return RecipeChoice.empty();
        final String id = it.getPersistentDataContainer().get(ItemGroup.KEY, PersistentDataType.STRING);
        final RecipeChoice rc = new RecipeChoice.MaterialChoice(it.getType());
        ids.put(id, rc);
        return rc;
    }*

   // public IdChoice(final @Nullable String id, final Material... mt) {
    //    super(mt);
     //   this.id = id;
    //}

    public IdChoice(final @Nullable String id, final ItemStack... is) {
        super(is);
        this.id = id;
        //Bukkit.broadcast(Component.text("mt-" + mt[0].toString() + ", cmd-" + cmd));
    }

    public IdChoice(final ExactChoice mtc) {
        super(mtc.getChoices());
        this.id = null;
    }

    private static final Material[] emt = {null};

    @Override
    public IdChoice clone() {
        //return new IdChoice(id, getChoices().toArray(emt));
        return new IdChoice(id, getChoices().toArray(e));
    }

    @Override
    public ItemStack getItemStack() {
      final ItemStack is = getChoices().getFirst();
        final Material mt = is.getType();
        final ItemGroup cmts = ItemGroup.get(id);
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
            .get(ItemGroup.KEY, PersistentDataType.STRING));
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
*/

/*
package ru.komiss77.modules.crafts;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.RecipeChoice.MaterialChoice;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import ru.komiss77.modules.items.ItemGroup;
import ru.komiss77.utils.ItemUtil;

//public class IdChoice extends MaterialChoice  {
public final class IdChoice implements RecipeChoice {

    private final String id;
    private List<Material> choices = new ArrayList<>();
    //private final MaterialChoice mc;

    public IdChoice(final @Nullable String id, final Material... mt) {
        //super(mt);
        //mc = new MaterialChoice(mt);
        this.id = id;
        for (Material m : mt) {
            choices.add(m);
        }
        //Bukkit.broadcast(Component.text("mt-" + mt[0].toString() + ", cmd-" + cmd));
    }

    public IdChoice(final MaterialChoice mtc) {
        //super(mtc.getChoices());
        //mc = mtc;
        this.id = null;
        for (Material m : mtc.getChoices()) {
            choices.add(m);
        }
    }


    public static RecipeChoice of(final @Nullable ItemStack it) {
        if (ItemUtil.isBlank(it, false)) return RecipeChoice.empty();
        final String id = it.getPersistentDataContainer().get(ItemGroup.KEY, PersistentDataType.STRING);
        return new IdChoice(id, it.getType());
    }


    private static final Material[] emt = {null};

    @Override
    public IdChoice clone() {
        return new IdChoice(id, getChoices().toArray(emt));
    }

    public @NotNull List<Material> getChoices() {
        //return mc.getChoices();
        return Collections.unmodifiableList(this.choices);
    }

    //@Override
    public ItemStack getItemStack() {
        final Material mt = getChoices().getFirst();
        final ItemGroup cmts = ItemGroup.get(id);
        final ItemStack ci = cmts == null ? null : cmts.item(mt.asItemType());
        if (ci != null) return ci.asOne();
        final ItemStack it = new ItemStack(mt);
        return ItemUtil.isBlank(it, false) ? ItemUtil.air : it;
    }

    //@Override
    public boolean test(final ItemStack it) {
        if (it == null) return getChoices().contains(Material.AIR);
        if (!getChoices().contains(it.getType())) return false;
        return Objects.equals(id, it.getPersistentDataContainer()
            .get(ItemGroup.KEY, PersistentDataType.STRING));
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

 */
