package ru.komiss77.Listener;


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
import ru.komiss77.Events.ArmorEquipEvent;
import ru.komiss77.Events.ArmorEquipEvent.EquipMethod;
import ru.komiss77.Events.ArmorType;



public class ArmorEquipListener implements Listener {


	//private final List<String> blockedMaterials;

	public ArmorEquipListener(){
		//this.blockedMaterials = blockedMaterials;
	}
	//Event Priority is highest because other plugins might cancel the events before we check.

	@EventHandler(priority =  EventPriority.LOW, ignoreCancelled = true)
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
                    } else {
                        newArmorType = ArmorType.matchType(e.getCurrentItem());
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
                    } else {  //одеть с шифтом
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
                    newArmorType = ArmorType.matchType(e.getRawSlot());
                    final ItemStack hotbarItem = e.getClickedInventory().getItem(e.getHotbarButton());
                    if (isAirOrNull(hotbarItem)) { //снятие
                        armorEquipEvent = new ArmorEquipEvent(p, EquipMethod.HOTBAR_SWAP, newArmorType, e.getCurrentItem(), null);
                    } else { //одевание/замена
                        armorEquipEvent = new ArmorEquipEvent(p, EquipMethod.HOTBAR_SWAP, newArmorType, e.getCurrentItem(), hotbarItem);
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
                    newArmorType = ArmorType.matchType(e.getRawSlot());
                    if (e.getAction()==InventoryAction.PLACE_ALL && e.getSlotType()==SlotType.ARMOR ) { //одеть
                        armorEquipEvent = new ArmorEquipEvent(p, EquipMethod.PICK_DROP, newArmorType, null, e.getCursor());
                    } else if (e.getAction()==InventoryAction.PICKUP_ALL && e.getSlotType()==SlotType.ARMOR ) { //снять
                        armorEquipEvent = new ArmorEquipEvent(p, EquipMethod.PICK_DROP, newArmorType, e.getCurrentItem(), null);
                    } else if (e.getAction()==InventoryAction.SWAP_WITH_CURSOR ) { //поменять
                        armorEquipEvent = new ArmorEquipEvent(p, EquipMethod.PICK_DROP, newArmorType, e.getCursor(), e.getCurrentItem());
                    }   
                    break;
            }
                
            
            if (armorEquipEvent!=null) {
                Bukkit.getServer().getPluginManager().callEvent(armorEquipEvent);
                e.setCancelled(armorEquipEvent.isCancelled());
            }
            
            
            
            
           // final boolean shift = e.getClick()==ClickType.SHIFT_LEFT || e.getClick()==ClickType.SHIFT_RIGHT ;
         //   final boolean numberkey = e.getClick()==ClickType.NUMBER_KEY;

          //  newArmorType = ArmorType.matchType(shift ? e.getCurrentItem() : e.getCursor());
            
//System.out.println("inventoryClick newArmorType="+newArmorType+" shift="+shift+" numberkey="+numberkey+" e.getRawSlot()="+e.getRawSlot()+" ");
            //if(!shift && newArmorType != null && e.getRawSlot() != newArmorType.getSlot()){
                    // Used for drag and drop checking to make sure you aren't trying to place a helmet in the boots slot.
            //        return;
            //}
            
            
         /*   if (shift) { //пропустило с шифтом из хотбара!
                //newArmorType = ArmorType.matchType(e.getCurrentItem()); - делается выше тренарно
                if(newArmorType != null){
                    final boolean equipping = e.getRawSlot() != newArmorType.getSlot();//true;
                    //if(e.getRawSlot() == newArmorType.getSlot()){
                    //        equipping = false;
                    //}
                    if(newArmorType==ArmorType.HELMET && (equipping ? isAirOrNull(p.getInventory().getHelmet()) : !isAirOrNull(p.getInventory().getHelmet())) || 
                            newArmorType==ArmorType.CHESTPLATE && (equipping ? isAirOrNull(p.getInventory().getChestplate()) : !isAirOrNull(p.getInventory().getChestplate())) ||
                            newArmorType==ArmorType.LEGGINGS && (equipping ? isAirOrNull(p.getInventory().getLeggings()) : !isAirOrNull(p.getInventory().getLeggings())) ||
                            newArmorType==ArmorType.BOOTS && (equipping ? isAirOrNull(p.getInventory().getBoots()) : !isAirOrNull(p.getInventory().getBoots()))) {
                                ArmorEquipEvent armorEquipEvent = new ArmorEquipEvent(p, EquipMethod.SHIFT_CLICK, newArmorType, equipping ? null : e.getCurrentItem(), equipping ? e.getCurrentItem() : null);
                                Bukkit.getServer().getPluginManager().callEvent(armorEquipEvent);
                                if(armorEquipEvent.isCancelled()){
System.out.println("armorEquipEvent.isCancelled 1");
                                    e.setCancelled(true);
                                  //  e.getView().getBottomInventory().addItem(new ItemStack[] { e.getCursor() });
                                 //   e.getView().setCursor(new ItemStack(Material.AIR));
                                    Ostrov.sync( ()-> p.updateInventory(), 1);
                                    //p.updateInventory();
                                }
                    }
                }
                
            } else { //без шифта - значит взятое на курсор или наведение на слот и нажать цифра
                
                if(newArmorType != null && e.getRawSlot() != newArmorType.getSlot()) return;
                ItemStack newArmorPiece = e.getCursor();
                ItemStack oldArmorPiece = e.getCurrentItem();
                
                if(numberkey){
                    
                    //if(e.getClickedInventory().getType()==InventoryType.PLAYER){ // Prevents shit in the 2by2 crafting
                        // e.getClickedInventory() == The players inventory
                        // e.getHotBarButton() == key people are pressing to equip or unequip the item to or from.
                        // e.getRawSlot() == The slot the item is going to.
                        // e.getSlot() == Armor slot, can't use e.getRawSlot() as that gives a hotbar slot ;-;
                        ItemStack hotbarItem = e.getClickedInventory().getItem(e.getHotbarButton());
                        if(!isAirOrNull(hotbarItem)) {// Equipping
                            newArmorType = ArmorType.matchType(hotbarItem);
                            newArmorPiece = hotbarItem;
                            oldArmorPiece = e.getClickedInventory().getItem(e.getSlot());
                        } else {// Unequipping
                            newArmorType = ArmorType.matchType(!isAirOrNull(e.getCurrentItem()) ? e.getCurrentItem() : e.getCursor());
                        }
                    //}
                } else {
                    
                    if(isAirOrNull(e.getCursor()) && !isAirOrNull(e.getCurrentItem())){// unequip with no new item going into the slot.
                        newArmorType = ArmorType.matchType(e.getCurrentItem());
                    }
                    // e.getCurrentItem() == Unequip
                    // e.getCursor() == Equip
                    // newArmorType = ArmorType.matchType(!isAirOrNull(e.getCurrentItem()) ? e.getCurrentItem() : e.getCursor());
                }
                if ( newArmorType != null && e.getRawSlot() == newArmorType.getSlot() ) {
                    final EquipMethod method = (e.getAction()==InventoryAction.HOTBAR_SWAP || numberkey) ? EquipMethod.HOTBAR_SWAP : EquipMethod.PICK_DROP;
                    ArmorEquipEvent armorEquipEvent = new ArmorEquipEvent(p, method, newArmorType, oldArmorPiece, newArmorPiece);
                    Bukkit.getServer().getPluginManager().callEvent(armorEquipEvent);
                    if(armorEquipEvent.isCancelled()){
                        e.setCancelled(true);
                        Ostrov.sync( ()-> p.updateInventory(), 1);
                        //e.getView().getBottomInventory().addItem(new ItemStack[] { e.getCursor() });
                        //e.getView().setCursor(new ItemStack(Material.AIR));
System.out.println("armorEquipEvent.isCancelled 2");
                        //p.updateInventory();
                    }
                }
            }*/
	}
	
        
        
	@EventHandler(priority =  EventPriority.LOW) //ignoreCancelled не ставить!!! или пропускает ПКМ на воздух!!!
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
	
        
        
        
	@EventHandler(priority =  EventPriority.LOW, ignoreCancelled = true)
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
			if(armorEquipEvent.isCancelled()){
				e.setResult(Result.DENY);
				e.setCancelled(true);
			}
		}
		// Debug shit
		/*System.out.println("Slots: " + event.getInventorySlots().toString());
		System.out.println("Raw Slots: " + event.getRawSlots().toString());
		if(event.getCursor() != null){
			System.out.println("Cursor: " + event.getCursor().getType().name());
		}
		if(event.getOldCursor() != null){
			System.out.println("OldCursor: " + event.getOldCursor().getType().name());
		}
		System.out.println("Type: " + event.getType().name());*/
	}

        
        
	@EventHandler(priority =  EventPriority.LOW, ignoreCancelled = true)
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

	@EventHandler(priority =  EventPriority.LOW, ignoreCancelled = true)
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
        
        
	@EventHandler(priority =  EventPriority.LOW, ignoreCancelled = true)
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
	/**
	 * A utility method to support versions that use null or air ItemStacks.
        * @param item
        * @return 
	 */
	public static boolean isAirOrNull(ItemStack item){
		return item == null || item.getType().equals(Material.AIR);
	}
}

/*    
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void ArmorClick (InventoryClickEvent e) {
//System.out.println("ArmorClick InventoryClickEvent");

        if( p.getType()!=EntityType.PLAYER || e.getCurrentItem() == null ) return;
        if(e.getClickedInventory() != null && e.getClickedInventory().getType()!=InventoryType.PLAYER) return;
        if (e.getInventory().getType()!=InventoryType.CRAFTING && e.getInventory().getType()!=InventoryType.PLAYER) return;
        if(e.getSlotType() != InventoryType.SlotType.ARMOR && e.getSlotType() != InventoryType.SlotType.QUICKBAR && e.getSlotType() != InventoryType.SlotType.CONTAINER) return;

        boolean shift = false, numberkey = false;
        if(e.getClick().equals(ClickType.SHIFT_LEFT) || e.getClick().equals(ClickType.SHIFT_RIGHT)){
                shift = true;
        }
        if(e.getClick().equals(ClickType.NUMBER_KEY)){
                numberkey = true;
        }

        ArmorType newArmorType = ArmorType.matchType(shift ? e.getCurrentItem() : e.getCursor());
        if(!shift && newArmorType != null && e.getRawSlot() != newArmorType.getSlot()){
                // Used for drag and drop checking to make sure you aren't trying to place a helmet in the boots place.
                return;
        }
        if(shift){
                newArmorType = ArmorType.matchType(e.getCurrentItem());
                if(newArmorType != null){
                        boolean equipping = true;
                        if(e.getRawSlot() == newArmorType.getSlot()){
                                equipping = false;
                        }
                        if(newArmorType.equals(ArmorType.HELMET) && (equipping ? p.getInventory().getHelmet() == null : p.getInventory().getHelmet() != null) || newArmorType.equals(ArmorType.CHESTPLATE) && (equipping ? p.getInventory().getChestplate() == null : p.getInventory().getChestplate() != null) || newArmorType.equals(ArmorType.LEGGINGS) && (equipping ? p.getInventory().getLeggings() == null : p.getInventory().getLeggings() != null) || newArmorType.equals(ArmorType.BOOTS) && (equipping ? p.getInventory().getBoots() == null : p.getInventory().getBoots() != null)){
                                ArmorEquipEvent armorEquipEvent = new ArmorEquipEvent(p, ArmorEquipEvent.EquipMethod.SHIFT_CLICK, newArmorType, equipping ? null : e.getCurrentItem(), equipping ? e.getCurrentItem() : null);
                                Bukkit.getServer().getPluginManager().callEvent(armorEquipEvent);
                                if(armorEquipEvent.isCancelled()){
                                        e.setCancelled(true);
                                }
                        }
                }
        }else{
                ItemStack newArmorPiece = e.getCursor();
                ItemStack oldArmorPiece = e.getCurrentItem();
                if(numberkey){
                        if(e.getClickedInventory().getType().equals(InventoryType.PLAYER)){// Prevents shit in the 2by2 crafting
                                // e.getClickedInventory() == The players inventory
                                // e.getHotBarButton() == key people are pressing to equip or unequip the item to or from.
                                // e.getRawSlot() == The slot the item is going to.
                                // e.getSlot() == Armor slot, can't use e.getRawSlot() as that gives a hotbar slot ;-;
                                ItemStack hotbarItem = e.getClickedInventory().getItem(e.getHotbarButton());
                                if(hotbarItem != null){// Equipping
                                        newArmorType = ArmorType.matchType(hotbarItem);
                                        newArmorPiece = hotbarItem;
                                        oldArmorPiece = e.getClickedInventory().getItem(e.getSlot());
                                }else{// Unequipping
                                        newArmorType = ArmorType.matchType(e.getCurrentItem() != null && e.getCurrentItem().getType() != Material.AIR ? e.getCurrentItem() : e.getCursor());
                                }
                        }
                }else{
                        // e.getCurrentItem() == Unequip
                        // e.getCursor() == Equip
                        newArmorType = ArmorType.matchType(e.getCurrentItem() != null && e.getCurrentItem().getType() != Material.AIR ? e.getCurrentItem() : e.getCursor());
                }
                if(newArmorType != null && e.getRawSlot() == newArmorType.getSlot()){
                        ArmorEquipEvent.EquipMethod method = ArmorEquipEvent.EquipMethod.DRAG;
                        if(e.getAction().equals(InventoryAction.HOTBAR_SWAP) || numberkey) method = ArmorEquipEvent.EquipMethod.HOTBAR_SWAP;
                        ArmorEquipEvent armorEquipEvent = new ArmorEquipEvent(p, method, newArmorType, oldArmorPiece, newArmorPiece);
                        Bukkit.getServer().getPluginManager().callEvent(armorEquipEvent);
                        if(armorEquipEvent.isCancelled()){
                                e.setCancelled(true);
                        }
                }
        }
        
    }       
        
        
 
    
    
     

@EventHandler
    public void playerInteractEvent(PlayerInteractEvent e){
//System.out.println("ArmorClick PlayerInteractEvent");
        if(e.getAction() == Action.PHYSICAL || e.getItem()==null) return;
        if(e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK){
            //final Player player = e.getPlayer();
            //if(e.getClickedBlock() != null && e.getAction() == Action.RIGHT_CLICK_BLOCK){// Having both of these checks is useless, might as well do it though.
                    // Some blocks have actions when you right click them which stops the client from equipping the armor in hand.
            //        Material mat = e.getClickedBlock().getType();
            //        for(String s : no_check){
            //                if(mat.name().equalsIgnoreCase(s)) return;
            //        }
            //}
//System.out.println("ArmorEquipEvent 111");            
            ArmorType newArmorType = ArmorType.matchType(e.getItem());
            if(newArmorType != null){
//System.out.println("ArmorEquipEvent 111 newArmorType="+newArmorType);            
                    if(newArmorType.equals(ArmorType.HELMET) && e.getPlayer().getInventory().getHelmet() == null || newArmorType.equals(ArmorType.CHESTPLATE) && e.getPlayer().getInventory().getChestplate() == null || newArmorType.equals(ArmorType.LEGGINGS) && e.getPlayer().getInventory().getLeggings() == null || newArmorType.equals(ArmorType.BOOTS) && e.getPlayer().getInventory().getBoots() == null){
//System.out.println("ArmorEquipEvent 111 newArmorType="+newArmorType);            
                            ArmorEquipEvent armorEquipEvent = new ArmorEquipEvent(e.getPlayer(), EquipMethod.HOTBAR, ArmorType.matchType(e.getItem()), null, e.getItem());
                            Bukkit.getServer().getPluginManager().callEvent(armorEquipEvent);
                            if(armorEquipEvent.isCancelled()){
                                    e.setCancelled(true);
                                    e.getPlayer().updateInventory();
                            }
                    }
            }
        }
    }

    
    
    
@EventHandler
    public void dispenserFireEvent(BlockDispenseEvent e){
            ArmorType type = ArmorType.matchType(e.getItem());
            if(ArmorType.matchType(e.getItem()) != null){
                    Location loc = e.getBlock().getLocation();
                    for(Player p : loc.getWorld().getPlayers()){
                            if(loc.getBlockY() - p.getLocation().getBlockY() >= -1 && loc.getBlockY() - p.getLocation().getBlockY() <= 1){
                                    if(p.getInventory().getHelmet() == null && type.equals(ArmorType.HELMET) || p.getInventory().getChestplate() == null && type.equals(ArmorType.CHESTPLATE) || p.getInventory().getLeggings() == null && type.equals(ArmorType.LEGGINGS) || p.getInventory().getBoots() == null && type.equals(ArmorType.BOOTS)){
                                            org.bukkit.block.Dispenser dispenser = (org.bukkit.block.Dispenser) e.getBlock().getState();
                                            org.bukkit.material.Dispenser dis = (org.bukkit.material.Dispenser) dispenser.getData();
                                            BlockFace directionFacing = dis.getFacing();
                                            // Someone told me not to do big if checks because it's hard to read, look at me doing it -_-
                                            if(directionFacing == BlockFace.EAST && p.getLocation().getBlockX() != loc.getBlockX() && p.getLocation().getX() <= loc.getX() + 2.3 && p.getLocation().getX() >= loc.getX() || directionFacing == BlockFace.WEST && p.getLocation().getX() >= loc.getX() - 1.3 && p.getLocation().getX() <= loc.getX() || directionFacing == BlockFace.SOUTH && p.getLocation().getBlockZ() != loc.getBlockZ() && p.getLocation().getZ() <= loc.getZ() + 2.3 && p.getLocation().getZ() >= loc.getZ() || directionFacing == BlockFace.NORTH && p.getLocation().getZ() >= loc.getZ() - 1.3 && p.getLocation().getZ() <= loc.getZ()){
                                                    ArmorEquipEvent armorEquipEvent = new ArmorEquipEvent(p, EquipMethod.DISPENSER, ArmorType.matchType(e.getItem()), null, e.getItem());
                                                    Bukkit.getServer().getPluginManager().callEvent(armorEquipEvent);
                                                    if(armorEquipEvent.isCancelled()){
                                                            e.setCancelled(true);
                                                    }
                                                    return;
                                            }
                                    }
                            }
                    }
            }
    }

    
    
	@EventHandler
	public void itemBreakEvent(PlayerItemBreakEvent e){
		ArmorType type = ArmorType.matchType(e.getBrokenItem());
		if(type != null){
			Player p = e.getPlayer();
			ArmorEquipEvent armorEquipEvent = new ArmorEquipEvent(p, EquipMethod.BROKE, type, e.getBrokenItem(), null);
			Bukkit.getServer().getPluginManager().callEvent(armorEquipEvent);
			if(armorEquipEvent.isCancelled()){
				ItemStack i = e.getBrokenItem().clone();
				i.setAmount(1);
				i.setDurability((short) (i.getDurability() - 1));
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
                                        default:
                                            break;
                                    }
			}
		}
	}

        
        
        
@EventHandler
    public void playerDeathEvent(PlayerDeathEvent e){
            if (!(e.getEntity() instanceof Player)) return;
            for(ItemStack i : e.getEntity().getInventory().getArmorContents()){
                    if(i != null && !i.getType().equals(Material.AIR)){
                            Bukkit.getServer().getPluginManager().callEvent(new ArmorEquipEvent(e.getEntity(), EquipMethod.DEATH, ArmorType.matchType(i), i, null));
                            // No way to cancel a death event.
                    }
            }
    }








*/







