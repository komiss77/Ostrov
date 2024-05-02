package ru.komiss77.modules.crafts;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice.MaterialChoice;
import org.bukkit.inventory.meta.ItemMeta;

import ru.komiss77.modules.items.CustomItems;
import ru.komiss77.utils.ItemUtils;

import java.util.Objects;

public class CMDMatChoice extends MaterialChoice {
	
	private final Integer cmd;
	
	public static CMDMatChoice of(final ItemStack it) {
		if (it == null) return new CMDMatChoice(null, Material.AIR);
		return new CMDMatChoice(it.hasItemMeta() && it.getItemMeta().hasCustomModelData() ? 
				it.getItemMeta().getCustomModelData() : null, it.getType());
	}
	
	public CMDMatChoice(final Integer cmd, final Material... mt) {
		super(mt); this.cmd = cmd;
		//Bukkit.broadcast(Component.text("mt-" + mt[0].toString() + ", cmd-" + cmd));
	}
	
	public CMDMatChoice(final MaterialChoice mtc) {
		super(mtc.getChoices()); this.cmd = null;
	}
	
	private static final Material[] emt = {null};
	@Override
	public CMDMatChoice clone() {
		return new CMDMatChoice(cmd, getChoices().toArray(emt));
	}

	@Override
	public ItemStack getItemStack() {
		final Material mt = getChoices().get(0);
		final ItemStack ci = CustomItems.getCstmItm(cmd).getItem(mt);
		if (ci != null) return ci.asOne();
		final ItemStack it = new ItemStack(mt);
		if (ItemUtils.isBlank(it, false)) return ItemUtils.air;
		final ItemMeta im = it.getItemMeta();
		im.setCustomModelData(cmd);
		it.setItemMeta(im);
		return it;
	}

	@Override
	public boolean test(final ItemStack it) {
		if (it == null) return getChoices().contains(Material.AIR);
		if (!getChoices().contains(it.getType())) return false;
		if (!it.hasItemMeta()) return cmd == null;
		final ItemMeta im = it.getItemMeta();
		return Objects.equals(cmd, im.hasCustomModelData() ? im.getCustomModelData() : null);
	}
	
	@Override
	public boolean equals(final Object o) {
		if (o instanceof MaterialChoice) {
			for (final Material mt : ((MaterialChoice) o).getChoices()) {
				if (!getChoices().contains(mt)) return false;
			}
			return o instanceof CMDMatChoice ? Objects.equals(((CMDMatChoice) o).cmd, cmd) : cmd == null;
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return super.hashCode() + (cmd == null ? 0 : cmd);
	}
}
