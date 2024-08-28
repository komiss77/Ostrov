package ru.komiss77.modules.regions.menu;

@Deprecated
public class RegionSelectMenu {
}

/*
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import ru.komiss77.modules.regions.RM;
import ru.komiss77.modules.regions.menu.RegionUtils;
import ru.komiss77.modules.regions.Template;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.inventory.ClickableItem;
import ru.komiss77.utils.inventory.InventoryContent;
import ru.komiss77.utils.inventory.InventoryProvider;
import ru.komiss77.utils.inventory.Pagination;
import ru.komiss77.utils.inventory.SlotIterator;
import ru.komiss77.utils.inventory.SlotPos;

//несколько приватов в точке нахождения, открываем меню выбора каким управлять
public class RegionSelectMenu implements InventoryProvider {
    private final List<ProtectedRegion> regions;
    
    public RegionSelectMenu(final List<ProtectedRegion> regions) {
        this.regions = regions;
    }
    
    //если стоять в перекрывающихся регионах
    @Override
    public void init(final Player player, InventoryContent inventoryContents) {
        player.playSound(player.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 5, 5);
        
        Pagination pagination = inventoryContents.pagination();
        ArrayList<ClickableItem> menuEntrys = new ArrayList<>();
        
        int number = 1;
        for (ProtectedRegion region : this.regions) {
            
            final Template template = RM.template( RegionUtils.getTemplateName(region) );
            final String createTime = RegionUtils.getCreateTime(region);
            
            //String string = ChatColor.GREEN + region.getId();
            //String string2 = Language.INTERFACE_SELECT_DESCRIPTION.toString().replace("%region%", string);
            
            ItemStack itemStack = new ItemBuilder(Material.BOOK)
                    .name("§7Регион §6"+number)
                    .lore("§7Тип региона: "+(template==null ? "не определён" : template.displayname))
                    .lore("§7Создан: §6"+(createTime.isEmpty()?"§8нет данных":createTime))
                    .lore ("§7Пользователей"+(region.getMembers().getPlayerDomain().size()==0 ? " нет" : ": "+region.getMembers().getPlayerDomain().size()))
                    .lore("ЛКМ - перейти к управлению")
                    .build();
            number++;
            
            menuEntrys.add(ClickableItem.of( itemStack, inventoryClickEvent ->
                RM.openRegionOwnerMenu(player, region) ));
        }
        
        ClickableItem[] arrclickableItem = new ClickableItem[menuEntrys.size()];
        ClickableItem[] citems = menuEntrys.toArray(arrclickableItem);
        
        pagination.setItems(citems);
        pagination.setItemsPerPage(18);
        pagination.addToIterator(inventoryContents.newIterator(SlotIterator.Type.HORIZONTAL, SlotPos.of(1, 0)));
        
    }

}
*/