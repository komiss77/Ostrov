package ru.komiss77.listener;


import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockDispenseArmorEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.inventory.ItemStack;
import ru.komiss77.Ostrov;
import ru.komiss77.events.ArmorEquipEvent;
import ru.komiss77.events.ArmorEquipEvent.EquipMethod;
import ru.komiss77.enums.ArmorType;

// https://github.com/Arnuh/ArmorEquipEvent/blob/master/src/com/codingforcookies/armorequip/ArmorListener.java

public class ArmorEquipLst implements Listener {

    
    public ArmorEquipLst () {
        Ostrov.log_ok("§7ArmorEquipListener подключен.");
    }
  /*  private static final Set<String>set = new HashSet<>();

        
    @EventHandler(priority =  EventPriority.HIGH, ignoreCancelled = true)
    public final void onArmorChange(final PlayerArmorChangeEvent e){
//System.out.println("onArmorChange="+e.getOldItem()+" ->"+e.getNewItem()+" getSlotType="+e.getSlotType());
        if (set.remove(e.getPlayer().getName())) return;
        final Player p = e.getPlayer();
        
        if (e.)
        ArmorType type;
        switch (e.getSlotType()) {
            case HEAD : 
                type = ArmorType.HELMET; 
                break;
            case CHEST : 
                type = ArmorType.CHESTPLATE; 
                break;
            case LEGS : 
                type = ArmorType.LEGGINGS; 
                break;
            case FEET :
            default:
                type = ArmorType.BOOTS; 
                break;
        }
        final ArmorEquipEvent armorEquipEvent = new ArmorEquipEvent(e.getPlayer(), EquipMethod.PICK_DROP, type, e.getOldItem(), e.getNewItem());
        Bukkit.getServer().getPluginManager().callEvent(armorEquipEvent);

        if (armorEquipEvent.isCancelled()) {
            set.add(p.getName());
            Ostrov.sync( ()-> {
                
                if (e.getOldItem()!=null && e.getOldItem().getType()!=Material.AIR)  {
                    p.getInventory().remove(e.getOldItem());
                }
                
                if (e.getNewItem()!=null && e.getNewItem().getType()!=Material.AIR) {
                    p.getInventory().addItem(e.getNewItem());
                }
                
                switch (e.getSlotType()) {
                    case HEAD : 
                        p.getInventory().setHelmet(e.getOldItem());
                        break;
                    case CHEST : 
                        p.getInventory().setChestplate(e.getOldItem());
                        break;
                    case LEGS : 
                        p.getInventory().setLeggings(e.getOldItem());
                        break;
                    case FEET :
                    default:
                        p.getInventory().setBoots(e.getOldItem());
                        break;
                }

                p.updateInventory();

            }, 1);

        }

    }*/
/*	@EventHandler(priority =  EventPriority.HIGH, ignoreCancelled = true)
	public final void onArmor(final PlayerArmorChangeEvent e){
            ArmorType at;
            switch (e.getSlotType()) {
                case HEAD -> at = ArmorType.HELMET;
                case CHEST -> at = ArmorType.CHESTPLATE;
                case LEGS -> at = ArmorType.LEGGINGS;
                case FEET -> at = ArmorType.BOOTS;
                default -> at = ArmorType.BOOTS;
            }
            
            ArmorEquipEvent armorEquipEvent = new ArmorEquipEvent(e.getPlayer(), EquipMethod.HOTBAR, at, e.getOldItem(), e.getNewItem());
            Bukkit.getServer().getPluginManager().callEvent(armorEquipEvent);
//System.out.println("ArmorEquipListener canceled?"+armorEquipEvent.isCancelled());
                e.setCancelled(armorEquipEvent.isCancelled());
        }*/
        
        
	@EventHandler(priority =  EventPriority.HIGH, ignoreCancelled = true)
	public final void inventoryClick(final InventoryClickEvent e){
//System.out.println("ICE current="+e.getCurrentItem()+" cursor="+e.getCursor()+" action="+e.getAction()+" click="+e.getClick()+" slottype="+e.getSlotType()+" rawslot="+e.getRawSlot()+" slot="+e.getSlot());
            if(e.getAction() == InventoryAction.NOTHING) return;// Why does this get called if nothing happens??
            if(e.getClickedInventory()!= null && e.getClickedInventory().getType()!=InventoryType.PLAYER) return;
            if(e.getSlotType() != SlotType.ARMOR && e.getSlotType() != SlotType.QUICKBAR && e.getSlotType() != SlotType.CONTAINER) return;
            if (e.getInventory().getType()!=InventoryType.CRAFTING && e.getInventory().getType()!=InventoryType.PLAYER) return;
            //if(!(e.getWhoClicked() instanceof Player)) return;
            
            final Player p = (Player)e.getWhoClicked();
            
            
            ArmorEquipEvent armorEquipEvent = null;
            ArmorType newArmorType = null;
                
            switch (e.getClick()) {
                
            //с шифтом может только поставить на пустой слот или снять
            //одевает с шифтом креатив:
            //1) ICE current=ItemStack{AIR x 0} cursor=ItemStack{DIAMOND_CHESTPLATE x 1} action=PLACE_ALL click=CREATIVE slottype=ARMOR rawslot=6 slot=38+второй  ICE current=ItemStack{DIAMOND_CHESTPLATE x 1} cursor=ItemStack{AIR x 1} action=PLACE_ALL click=CREATIVE slottype=CONTAINER rawslot=13 slot=13
            //снимает с шифтом креатив:
            //1) ICE current=ItemStack{DIAMOND_CHESTPLATE x 1} cursor=ItemStack{AIR x 1} action=PLACE_ALL click=CREATIVE slottype=ARMOR rawslot=6 slot=38+второй  ICE current=ItemStack{AIR x 0} cursor=ItemStack{DIAMOND_CHESTPLATE x 1} action=PLACE_ALL click=CREATIVE slottype=CONTAINER rawslot=13 slot=13
            //в креативе вызываются два эвента, обрабатываем только первый!!
                case CREATIVE:
                    if (e.getSlotType()==SlotType.ARMOR) { //в креативе - одевание с шифтом
                        newArmorType = ArmorType.matchType(e.getRawSlot());
                        armorEquipEvent = new ArmorEquipEvent(p, EquipMethod.CREATIVE, newArmorType, null, e.getCursor());
                    } else if (e.getSlotType()==SlotType.CONTAINER) {
                        newArmorType = ArmorType.matchType(e.getCurrentItem());
                        //if (newArmorType!=null) 
                            armorEquipEvent = new ArmorEquipEvent(p, EquipMethod.CREATIVE, newArmorType, e.getCurrentItem(), null);
                    }   
                    break;
                    
            //одеть с шифтом выживание -может быть CONTAINER или QUICKBAR!!!
            //ICE current=ItemStack{DIAMOND_CHESTPLATE x 1} cursor=ItemStack{AIR x 0} action=MOVE_TO_OTHER_INVENTORY click=SHIFT_LEFT slottype=CONTAINER rawslot=29 slot=29
            //снять с шифтом выживание 
            //ICE current=ItemStack{DIAMOND_CHESTPLATE x 1} cursor=ItemStack{AIR x 0} action=MOVE_TO_OTHER_INVENTORY click=SHIFT_LEFT slottype=ARMOR rawslot=6 slot=38
                case SHIFT_LEFT:
                case SHIFT_RIGHT:
                    if (e.getSlotType()==SlotType.ARMOR ) { //снять с шифтом
                        newArmorType = ArmorType.matchType(e.getRawSlot());
                        armorEquipEvent = new ArmorEquipEvent(p, EquipMethod.SHIFT_CLICK, newArmorType, e.getCurrentItem(), null);
                    } else if (e.getSlotType()==SlotType.CONTAINER) {  //одеть с шифтом
                        newArmorType = ArmorType.matchType(e.getCurrentItem());
                        armorEquipEvent = new ArmorEquipEvent(p, EquipMethod.SHIFT_CLICK, newArmorType, null, e.getCurrentItem());
                    }   
                    break;
                    
            //навестись на слот брони и нажимать цифру
            //креатив - неотличим от шифта 
            //одеть  ICE current=ItemStack{AIR x 0}                 cursor=ItemStack{DIAMOND_CHESTPLATE x 1} action=PLACE_ALL click=CREATIVE slottype=ARMOR rawslot=6 slot=38 +второй ICE current=ItemStack{DIAMOND_CHESTPLATE x 1} cursor=ItemStack{AIR x 1} action=PLACE_ALL click=CREATIVE slottype=QUICKBAR rawslot=37 slot=1
            //снять  ICE current=ItemStack{DIAMOND_CHESTPLATE x 1}  cursor=ItemStack{AIR x 1}                action=PLACE_ALL click=CREATIVE slottype=ARMOR rawslot=6 slot=38+второй ICE current=ItemStack{AIR x 0} cursor=ItemStack{DIAMOND_CHESTPLATE x 1} action=PLACE_ALL click=CREATIVE slottype=QUICKBAR rawslot=37 slot=1
            //выживание
            //одеть    ICE current=ItemStack{AIR x 0}                cursor=ItemStack{AIR x 0} action=HOTBAR_SWAP click=NUMBER_KEY slottype=ARMOR rawslot=6 slot=38
            //заменить ICE current=ItemStack{DIAMOND_CHESTPLATE x 1} cursor=ItemStack{AIR x 0} action=HOTBAR_SWAP click=NUMBER_KEY slottype=ARMOR rawslot=6 slot=38
            //снять    ICE current=ItemStack{DIAMOND_CHESTPLATE x 1} cursor=ItemStack{AIR x 0} action=HOTBAR_SWAP click=NUMBER_KEY slottype=ARMOR rawslot=6 slot=38
                case NUMBER_KEY:
                    if (e.getSlotType()==SlotType.ARMOR) {
                        newArmorType = ArmorType.matchType(e.getRawSlot());
                        final ItemStack hotbarItem = e.getClickedInventory().getItem(e.getHotbarButton());
                        if (isAirOrNull(hotbarItem)) { //снятие
                            armorEquipEvent = new ArmorEquipEvent(p, EquipMethod.HOTBAR_SWAP, newArmorType, e.getCurrentItem(), null);
                        } else { //одевание/замена
                            armorEquipEvent = new ArmorEquipEvent(p, EquipMethod.HOTBAR_SWAP, newArmorType, e.getCurrentItem(), hotbarItem);
                        }
                    }
                    break;
                    
            //простое одевание взял-положил, работает так же на RIGHT
            //одевание
            //ICE current=ItemStack{AIR x 0} cursor=ItemStack{DIAMOND_CHESTPLATE x 1} action=PLACE_ALL click=LEFT slottype=ARMOR rawslot=6 slot=38
            //снятие
            //ICE current=ItemStack{DIAMOND_CHESTPLATE x 1} cursor=ItemStack{AIR x 0} action=PICKUP_ALL click=LEFT slottype=ARMOR rawslot=6 slot=38
            //замена на другой (незерит)
            //ICE current=ItemStack{DIAMOND_CHESTPLATE x 1} cursor=ItemStack{NETHERITE_CHESTPLATE x 1} action=SWAP_WITH_CURSOR click=LEFT slottype=ARMOR rawslot=6 slot=38    
                case LEFT:
                case RIGHT:
                    if (e.getSlotType()==SlotType.ARMOR) {
                        newArmorType = ArmorType.matchType(e.getRawSlot());
                        if (e.getAction()==InventoryAction.PLACE_ALL && e.getSlotType()==SlotType.ARMOR ) { //одеть
                            armorEquipEvent = new ArmorEquipEvent(p, EquipMethod.PICK_DROP, newArmorType, null, e.getCursor());
                        } else if (e.getAction()==InventoryAction.PICKUP_ALL && e.getSlotType()==SlotType.ARMOR ) { //снять
                            armorEquipEvent = new ArmorEquipEvent(p, EquipMethod.PICK_DROP, newArmorType, e.getCurrentItem(), null);
                        } else if (e.getAction()==InventoryAction.SWAP_WITH_CURSOR ) { //поменять
                            armorEquipEvent = new ArmorEquipEvent(p, EquipMethod.PICK_DROP, newArmorType, e.getCursor(), e.getCurrentItem());
                        }   
                    }
                    break;
				default:
					break;
            }
                
            
            if (armorEquipEvent!=null) {
                Bukkit.getServer().getPluginManager().callEvent(armorEquipEvent);
//System.out.println("ArmorEquipListener canceled?"+armorEquipEvent.isCancelled());
                e.setCancelled(armorEquipEvent.isCancelled());
            }
            

	}
	
        
        
	@EventHandler(priority =  EventPriority.HIGH) //ignoreCancelled не ставить!!! или пропускает ПКМ на воздух!!!
	public void playerInteractEvent(PlayerInteractEvent e){
//System.out.println("playerInteractEvent e.useItemInHand()="+e.useItemInHand()+" action="+e.getAction()+" canceled="+e.isCancelled());
		if( e.useItemInHand()==Result.DENY) return;
		//
		if(e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK){
                    //if(!e.useInteractedBlock().equals(Result.DENY)){
                    //if(e.getClickedBlock() != null && e.getAction() == Action.RIGHT_CLICK_BLOCK && !player.isSneaking()) {// Having both of these checks is useless, might as well do it though.
                            // Some blocks have actions when you right click them which stops the client from equipping the armor in hand.
                            //Material mat = e.getClickedBlock().getType();
                            //for(String s : blockedMaterials){
                            //	if(mat.name().equalsIgnoreCase(s)) return;
                            //}
                    //}
                    //}
                    final ArmorType newArmorType = ArmorType.matchType(e.getItem());
//System.out.println("playerInteractEvent newArmorType="+newArmorType);
                    if(newArmorType != null) { //в руке броня и пытаемся одеть - проверяем слоты брони, если не пустой то эвент
                        if(newArmorType==ArmorType.HELMET && isAirOrNull(e.getPlayer().getInventory().getHelmet()) || 
                                newArmorType==ArmorType.CHESTPLATE && isAirOrNull(e.getPlayer().getInventory().getChestplate()) || 
                                newArmorType==ArmorType.LEGGINGS && isAirOrNull(e.getPlayer().getInventory().getLeggings()) ||
                                newArmorType==ArmorType.BOOTS && isAirOrNull(e.getPlayer().getInventory().getBoots())) {
                                    ArmorEquipEvent armorEquipEvent = new ArmorEquipEvent(e.getPlayer(), EquipMethod.HOTBAR, ArmorType.matchType(e.getItem()), null, e.getItem());
                                    Bukkit.getServer().getPluginManager().callEvent(armorEquipEvent);
                                    if(armorEquipEvent.isCancelled()){
                                        e.setCancelled(true);
                                        e.setUseItemInHand(Result.DENY);
                                        e.getPlayer().updateInventory();
                                    }
                                }
                    }
            }
	}
	
        
        
        
	@EventHandler(priority =  EventPriority.HIGH, ignoreCancelled = true)
	public void inventoryDrag(InventoryDragEvent e){
		// getType() seems to always be even.
		// Old Cursor gives the item you are equipping
		// Raw slot is the ArmorType slot
		// Can't replace armor using this method making getCursor() useless.
		ArmorType type = ArmorType.matchType(e.getOldCursor());
		if(e.getRawSlots().isEmpty()) return;// Idk if this will ever happen
		if(type != null && type.getSlot() == e.getRawSlots().stream().findFirst().orElse(0)){
			ArmorEquipEvent armorEquipEvent = new ArmorEquipEvent((Player) e.getWhoClicked(), EquipMethod.DRAG, type, null, e.getOldCursor());
			Bukkit.getServer().getPluginManager().callEvent(armorEquipEvent);
//System.out.println("ArmorEquipEvent InventoryDragEvent: " );
			if(armorEquipEvent.isCancelled()){
				e.setResult(Result.DENY);
				e.setCancelled(true);
			}
		}

	}

        
        
	@EventHandler(priority =  EventPriority.HIGH, ignoreCancelled = true)
	public void itemBreakEvent(PlayerItemBreakEvent e){
		ArmorType type = ArmorType.matchType(e.getBrokenItem());
		if(type != null){
                    final Player p = e.getPlayer();
                    ArmorEquipEvent armorEquipEvent = new ArmorEquipEvent(p, EquipMethod.BROKE, type, e.getBrokenItem(), null);
                    Bukkit.getServer().getPluginManager().callEvent(armorEquipEvent);
                    if(armorEquipEvent.isCancelled()){
                        ItemStack i = e.getBrokenItem().clone();
                        i.setAmount(1);
                        //i.setDurability((short) (i.getDurability() - 1));
                        switch (type) {
                            case HELMET:
                                p.getInventory().setHelmet(i);
                                break;
                            case CHESTPLATE:
                                p.getInventory().setChestplate(i);
                                break;
                            case LEGGINGS:
                                p.getInventory().setLeggings(i);
                                break;
                            case BOOTS:
                                p.getInventory().setBoots(i);
                                break;
                        }
                    }
            }
	}

	@EventHandler(priority =  EventPriority.HIGH, ignoreCancelled = true)
	public void playerDeathEvent(PlayerDeathEvent e){
		if(e.getKeepInventory()) return;
		final Player p = e.getEntity();
		for(ItemStack i : p.getInventory().getArmorContents()){
            if(!isAirOrNull(i)){
                Bukkit.getServer().getPluginManager().callEvent(new ArmorEquipEvent(p, EquipMethod.DEATH, ArmorType.matchType(i), i, null));
                // No way to cancel a death event.
            }
		}
	}
        
        
	@EventHandler(priority =  EventPriority.HIGH, ignoreCancelled = true)
	public void dispenseArmorEvent(BlockDispenseArmorEvent e){
            if (e.getTargetEntity().getType()!=EntityType.PLAYER) return;
            ArmorType type = ArmorType.matchType(e.getItem());
            if(type != null){
                Player p = (Player) e.getTargetEntity();
                ArmorEquipEvent armorEquipEvent = new ArmorEquipEvent(p, ArmorEquipEvent.EquipMethod.DISPENSER, type, null, e.getItem());
                Bukkit.getServer().getPluginManager().callEvent(armorEquipEvent);
                e.setCancelled(armorEquipEvent.isCancelled());
            }
	}

        
	public static boolean isAirOrNull(ItemStack item){
		return item == null || item.getType().equals(Material.AIR);
	}
        
        
        
        
        
        
}








