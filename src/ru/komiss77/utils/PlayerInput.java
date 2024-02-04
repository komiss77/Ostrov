package ru.komiss77.utils;

import java.util.WeakHashMap;
import java.util.function.Consumer;
import java.util.Arrays;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import ru.komiss77.modules.world.XYZ;
import ru.komiss77.objects.InputData;
import ru.komiss77.utils.inventory.InputButton;
import ru.komiss77.utils.inventory.InputButton.InputType;
import ru.komiss77.version.AnvilGUI;
import ru.komiss77.version.VM;


// НЕ ПЕРЕМЕЩАТЬ!! 
public class PlayerInput implements Listener {

    public static final WeakHashMap<Player,InputData> inputData; //для авторизации - там нет оплеера
    private static final ItemStack left, out;
    
    static {
        inputData = new WeakHashMap<>();
        left = new ItemBuilder(Material.RED_DYE)
                    //.name("§7Сейчас:")
                    //.addLore("§7"+sugg)
                    //.addLore("Режим ввода текста")
                    //.addLore("§7Наберите в строке")
                    //.addLore("§7значение и нажмите")
                    //.addLore("§7на результат.")
                    //.addLore("")
                    .addLore("§6Клик сюда - ")
                    .addLore("§eоставить как есть")
                    .build();
        out = new ItemBuilder(Material.LIME_DYE)
                    //.name("§7Сейчас:")
                    //.addLore("§7"+sugg)
                    .addLore("Режим ввода текста")
                    .addLore("")
                    .addLore("§fНаберите в строке")
                    .addLore("§fзначение и")
                    .addLore("§bнажмите сюда.")
                    //.addLore("")
                    //.addLore("§6Клик  - ")
                    //.addLore("§eпринять")
                    .build();
    }

    public static void get(final InputType type, final Player p, final Consumer<String> consumer, String suggest) {
        final String sugg = suggest==null ? "" : suggest.replaceAll("§", "&");
//Ostrov.log("PlayerInput get type="+type+" sugg="+sugg);
        XYZ xyz = null;
                
        switch (type) {
            case ANVILL -> {
                new AnvilGUI.Builder()
                    .title("Введите значение")
                    .text(sugg)
                    .itemLeft(left)
                    //.itemRight(right)
                    .itemOutput(out)
                    //.onClick(p1 -> consumer.accept(sugg))
                    .onClick( (slot, stateSnapshot) -> { // Either use sync or async variant, not both
//Ostrov.log("AnvilGUI.Slot="+slot);
                        switch (slot) {
                            case AnvilGUI.Slot.INPUT_LEFT -> consumer.accept(sugg);
                            case AnvilGUI.Slot.INPUT_RIGHT -> {}
                            case AnvilGUI.Slot.OUTPUT -> consumer.accept(stateSnapshot.getText());
                        }
                        return Arrays.asList(AnvilGUI.ResponseAction.close());
                        //if(stateSnapshot.getText().equalsIgnoreCase("you")) {
                        //    stateSnapshot.getPlayer().sendMessage("You have magical powers!");
                        //    return Arrays.asList(AnvilGUI.ResponseAction.close());
                        //} else {
                       //     return Arrays.asList(AnvilGUI.ResponseAction.replaceInputText("Try again"));
                        //}
                    })
                    .open(p);
                return;
            }
            
            case CHAT -> {
                p.closeInventory();
                p.sendMessage(TCUtils.format("§fНаберите в чате значение "+(sugg.isEmpty() ? "и нажмите Ввод" : "§b>Клик - подставить текущее<"))
                    .hoverEvent(HoverEvent.showText(TCUtils.format("§7Клик - подставить текст для редактирования")))
                    .clickEvent(ClickEvent.suggestCommand(sugg)));
            	}
            
            case SIGN -> {
                xyz = new XYZ(p.getLocation());
                xyz.y-=3;
                VM.server().signInput(p, sugg, xyz);
            }
                
        }

        inputData.put(p, new InputData(type, consumer, xyz));

    }
    

    
    public static void onInput(final Player p, final InputButton.InputType type, final String result) { //вызов только SUNC !!!
//Ostrov.log_warn("onInput "+p.getName()+" result="+result);
        final InputData data = inputData.remove(p);

        if (data==null || data.type != type) {
            return;
        }

        data.setResult(result);
        
        if (data.type==InputType.CHAT) {
            p.sendMessage("");
            p.sendMessage("§aЗначение получено: ");
            p.sendMessage(TCUtils.format("§f"+data.getResult()));
            p.sendMessage("");
        } else if (data.type==InputType.SIGN) {
            if (data.xyz != null) {
                final Location loc =  data.xyz.getCenterLoc();
                p.sendBlockChange(loc, loc.getBlock().getBlockData());
            }
        }
        data.accept();
    }

    
}
    

    
    


    


    

    
  
    
