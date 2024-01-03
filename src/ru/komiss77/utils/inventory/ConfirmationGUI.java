package ru.komiss77.utils.inventory;

import java.util.function.Consumer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import ru.komiss77.Ostrov;
import ru.komiss77.utils.ItemBuilder;

public class ConfirmationGUI implements InventoryProvider {
    private final boolean sound;
    private final Consumer<Boolean> consumer;
    private boolean confirm = false;

    public static void open(Player player, String title, Consumer<Boolean> consumer) {
       open(player, title, true, consumer);
    }

    public static void open(Player player, String title, boolean sound, Consumer<Boolean> consumer) {
        SmartInventory
            .builder()
            .title(title)
            .size(1)
            .provider(new ConfirmationGUI(consumer, sound))
            .build()
            .open(player);
   }

    private ConfirmationGUI(Consumer<Boolean> consumer, boolean playSound) {
        this.consumer = consumer;
        this.sound = playSound;
    }

    @Override
    public void init(Player player, InventoryContent contents) {
        contents.set(SlotPos.of(0, 2), ClickableItem.of((new ItemBuilder(Material.GREEN_WOOL)).name("§a✔").build(), (event) -> {
            this.confirm = true;
            if (this.sound) {
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_GUITAR, 0.7F, 1.25F);
            }

            player.closeInventory();
        }));
        contents.set(SlotPos.of(0, 6), ClickableItem.of((new ItemBuilder(Material.RED_WOOL)).name("§c✖").build(), (event) -> {
            this.confirm = false;
            if (this.sound) {
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_GUITAR, 0.7F, 0.75F);
            }

            player.closeInventory();
        }));
    }

    @Override
    public void onClose(Player player, InventoryContent contents) {
        Bukkit.getScheduler().runTaskLater(Ostrov.instance, () -> {
            this.consumer.accept(this.confirm);
        }, 1L);
    }
   
}
