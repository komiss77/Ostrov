package ru.komiss77.enums;

import org.bukkit.inventory.ItemStack;
import ru.komiss77.listener.ArmorEquipLst;




public enum ArmorType {
    
	HELMET(5), CHESTPLATE(6), LEGGINGS(7), BOOTS(8);

	private final int slot;

	ArmorType(int slot){
		this.slot = slot;
	}
        
        public static ArmorType matchType(final int slot){
	switch (slot) {
            case 5 : return HELMET;
            case 6 : return CHESTPLATE;
            case 7 : return LEGGINGS;
            case 8 : return BOOTS;
        }
            return null;
	}
	/**
	 * Attempts to match the ArmorType for the specified ItemStack.
	 *
	 * @param itemStack The ItemStack to parse the type of.
	 * @return The parsed ArmorType. (null if none were found.)
	 */
        //@Deprecated
        public static ArmorType matchType(final ItemStack itemStack){
		if(ArmorEquipLst.isAirOrNull(itemStack)) return null;
		final String type = itemStack.getType().name();
		if(type.endsWith("_HELMET") || type.endsWith("_SKULL") || type.endsWith("_HEAD")) return HELMET;
		else if(type.endsWith("_CHESTPLATE") || type.equals("ELYTRA")) return CHESTPLATE;
		else if(type.endsWith("_LEGGINGS")) return LEGGINGS;
		else if(type.endsWith("_BOOTS")) return BOOTS;
		else return null;
	}
	/*public final static ArmorType matchType(final ItemStack itemStack){
		if(itemStack == null) { return null; }
		switch (itemStack.getType()){
			case DIAMOND_HELMET:
			case GOLDEN_HELMET:
			case IRON_HELMET:
			case CHAINMAIL_HELMET:
			case LEATHER_HELMET:
				return HELMET;
			case DIAMOND_CHESTPLATE:
			case GOLDEN_CHESTPLATE:
			case IRON_CHESTPLATE:
			case CHAINMAIL_CHESTPLATE:
			case LEATHER_CHESTPLATE:
				return CHESTPLATE;
			case DIAMOND_LEGGINGS:
			case GOLDEN_LEGGINGS:
			case IRON_LEGGINGS:
			case CHAINMAIL_LEGGINGS:
			case LEATHER_LEGGINGS:
				return LEGGINGS;
			case DIAMOND_BOOTS:
			case GOLDEN_BOOTS:
			case IRON_BOOTS:
			case CHAINMAIL_BOOTS:
			case LEATHER_BOOTS:
				return BOOTS;
			default:
				return null;
		}
	}*/

	public int getSlot(){
		return slot;
	}
        
}
