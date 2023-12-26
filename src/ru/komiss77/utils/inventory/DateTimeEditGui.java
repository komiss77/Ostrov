package ru.komiss77.utils.inventory;

import java.util.Calendar;
import java.util.TimeZone;
import java.util.function.Consumer;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import ru.komiss77.Timer;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.ItemUtils;

public class DateTimeEditGui implements InventoryProvider {
    
    private static final ClickableItem fill = ClickableItem.empty(new ItemBuilder(Material.MAGENTA_STAINED_GLASS_PANE).name("§8.").build());
    private final Consumer<Integer> consumer;
    private final Calendar calendar = Calendar.getInstance();
    public static void open(Player player, String title, Consumer<Integer> consumer) {
       open(player, title, Timer.getTime(), true, true, consumer);
    }

    public static void open(final Player p, final String title, final int stamp, final boolean editDate, final boolean editTime, Consumer<Integer> consumer) {
        SmartInventory
            .builder()
            .title(title)
            .size(6)
            .provider(new DateTimeEditGui(consumer, stamp, editDate, editTime))
            .build()
            .open(p);
   }

    private DateTimeEditGui(final Consumer<Integer> consumer, final int stamp, final boolean editDate, final boolean editTime) {
        this.consumer = consumer;
        calendar.setTimeZone(TimeZone.getTimeZone("Europe/Moscow"));
        calendar.setTimeInMillis(stamp*1000L);
    }

    
    
    @Override
    public void init(final Player p, final InventoryContent contents) {
        
        p.playSound(p.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 0.7F, 1.25F);
        
        contents.fillColumn(0, fill);
        contents.fillRow(5, fill);
        
//System.out.println("init() stamp="+stamp); 
        
        






        int day = calendar.get(Calendar.DAY_OF_MONTH);
        contents.set(0, 1, ClickableItem.of((new ItemBuilder(Material.PLAYER_HEAD)).name("§4-").setCustomHeadTexture(ItemUtils.Texture.down).build(), (e) -> {
            calendar.add(Calendar.DAY_OF_MONTH, -1);
            reopen(p, contents);
        }));
        contents.set(1, 1, ClickableItem.empty((
                new ItemBuilder(Material.PLAYER_HEAD))
                        .name("день")
                        .setCustomHeadTexture(ItemUtils.getNumberTexture((int)(day/10)))
                        .build())
        );
        contents.set(0, 2, ClickableItem.of((new ItemBuilder(Material.PLAYER_HEAD)).name("§2+").setCustomHeadTexture(ItemUtils.Texture.up).build(), (e) -> {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            reopen(p, contents);
        }));
        contents.set(1, 2, ClickableItem.empty((
                new ItemBuilder(Material.PLAYER_HEAD))
                        .name("день")
                        .setCustomHeadTexture(ItemUtils.getNumberTexture(day - (int)(day/10)*10))
                        .build())
        );

        
        
        contents.set(1, 3, ClickableItem.empty((
                new ItemBuilder(Material.PLAYER_HEAD)
                        .name("§8.")
                        .setCustomHeadTexture(ItemUtils.Texture.dot))
                        .build())
        );


        int month = calendar.get(Calendar.MONTH)+1;
//System.out.println("MONTH="+month);
        contents.set(0, 4, ClickableItem.of((new ItemBuilder(Material.PLAYER_HEAD)).name("§4-").setCustomHeadTexture(ItemUtils.Texture.down).build(), (e) -> {
            calendar.add(Calendar.MONTH, -1);
            reopen(p, contents);
        }));
        contents.set(1, 4, ClickableItem.empty((
                new ItemBuilder(Material.PLAYER_HEAD))
                        .name("месяц")
                        .setCustomHeadTexture(ItemUtils.getNumberTexture((int)(month/10)))
                        .build())
        );
        contents.set(0, 5, ClickableItem.of((new ItemBuilder(Material.PLAYER_HEAD)).name("§2+").setCustomHeadTexture(ItemUtils.Texture.up).build(), (e) -> {
            calendar.add(Calendar.MONTH, 1);
            reopen(p, contents);
        }));
        contents.set(1, 5, ClickableItem.empty((
                new ItemBuilder(Material.PLAYER_HEAD))
                        .name("месяц")
                        .setCustomHeadTexture(ItemUtils.getNumberTexture(month - (int)(month/10)*10))
                        .build())
        );

        
        
        contents.set(1, 6, ClickableItem.empty((
                new ItemBuilder(Material.PLAYER_HEAD)
                        .name("§8.")
                        .setCustomHeadTexture(ItemUtils.Texture.dot))
                        .build())
        );



        int year = calendar.get(Calendar.YEAR)-2000;
        if (year>1) {
            contents.set(0, 7, ClickableItem.of((new ItemBuilder(Material.PLAYER_HEAD)).name("§4-").setCustomHeadTexture(ItemUtils.Texture.down).build(), (e) -> {
                calendar.add(Calendar.YEAR, -1);
                reopen(p, contents);
            }));
        }
        contents.set(1, 7, ClickableItem.empty((
                new ItemBuilder(Material.PLAYER_HEAD))
                        .name("год")
                        .setCustomHeadTexture(ItemUtils.getNumberTexture((int)(year/10)))
                        .build())
        );
        if (year<99) {
            contents.set(0, 8, ClickableItem.of((new ItemBuilder(Material.PLAYER_HEAD)).name("§2+").setCustomHeadTexture(ItemUtils.Texture.up).build(), (e) -> {
                calendar.add(Calendar.YEAR, 1);
                reopen(p, contents);
            }));
        }
        contents.set(1, 8, ClickableItem.empty((
                new ItemBuilder(Material.PLAYER_HEAD))
                        .name("год")
                        .setCustomHeadTexture(ItemUtils.getNumberTexture(year - (int)(year/10)*10))
                        .build())
        );




        
        
        
        
        
        
        
        
        


        
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        contents.set(3, 1, ClickableItem.empty((
                new ItemBuilder(Material.PLAYER_HEAD))
                        .name("час")
                        .setCustomHeadTexture(ItemUtils.getNumberTexture((int)(hour/10)))
                        .build())
        );
        contents.set(4, 1, ClickableItem.of((new ItemBuilder(Material.PLAYER_HEAD)).name("§4-").setCustomHeadTexture(ItemUtils.Texture.down).build(), (e) -> {
            calendar.add(Calendar.HOUR_OF_DAY, -1);
            reopen(p, contents);
        }));
        contents.set(3, 2, ClickableItem.empty((
                new ItemBuilder(Material.PLAYER_HEAD))
                        .name("час")
                        .setCustomHeadTexture(ItemUtils.getNumberTexture(hour - (int)(hour/10)*10))
                        .build())
        );
        contents.set(4, 2, ClickableItem.of((new ItemBuilder(Material.PLAYER_HEAD)).name("§2+").setCustomHeadTexture(ItemUtils.Texture.up).build(), (e) -> {
            calendar.add(Calendar.HOUR_OF_DAY, 1);
            reopen(p, contents);
        }));

        
        
        contents.set(3, 3, ClickableItem.empty((
                new ItemBuilder(Material.PLAYER_HEAD)
                        .name("§8:")
                        .setCustomHeadTexture(ItemUtils.Texture.dotdot))
                        .build())
        );
        
        int min = calendar.get(Calendar.MINUTE);
        contents.set(3, 4, ClickableItem.empty((
                new ItemBuilder(Material.PLAYER_HEAD))
                        .name("мин")
                        .setCustomHeadTexture(ItemUtils.getNumberTexture((int)(min/10)))
                        .build())
        );
        contents.set(4, 4, ClickableItem.of((new ItemBuilder(Material.PLAYER_HEAD)).name("§4-").setCustomHeadTexture(ItemUtils.Texture.down).build(), (e) -> {
            calendar.add(Calendar.MINUTE, -1);
            reopen(p, contents);
        }));
        contents.set(3, 5, ClickableItem.empty((
                new ItemBuilder(Material.PLAYER_HEAD))
                        .name("мин")
                        .setCustomHeadTexture(ItemUtils.getNumberTexture(min - (int)(min/10)*10))
                        .build())
        );
        contents.set(4, 5, ClickableItem.of((new ItemBuilder(Material.PLAYER_HEAD)).name("§2+").setCustomHeadTexture(ItemUtils.Texture.up).build(), (e) -> {
            calendar.add(Calendar.MINUTE, 1);
            reopen(p, contents);
        }));

        
        
        
        
        contents.set(3, 6, ClickableItem.empty((
                new ItemBuilder(Material.PLAYER_HEAD)
                        .name("§8:")
                        .setCustomHeadTexture(ItemUtils.Texture.dotdot))
                        .build())
        );
        
        int sec = calendar.get(Calendar.SECOND);
        contents.set(3, 7, ClickableItem.empty((
                new ItemBuilder(Material.PLAYER_HEAD))
                        .name("сек")
                        .setCustomHeadTexture(ItemUtils.getNumberTexture((int)(sec/10)))
                        .build())
        );
        contents.set(4, 7, ClickableItem.of((new ItemBuilder(Material.PLAYER_HEAD)).name("§4-").setCustomHeadTexture(ItemUtils.Texture.down).build(), (e) -> {
            calendar.add(Calendar.SECOND, -1);
            reopen(p, contents);
        }));
        contents.set(3, 8, ClickableItem.empty((
                new ItemBuilder(Material.PLAYER_HEAD))
                        .name("сек")
                        .setCustomHeadTexture(ItemUtils.getNumberTexture(sec - (int)(sec/10)*10))
                        .build())
        );
        contents.set(4, 8, ClickableItem.of((new ItemBuilder(Material.PLAYER_HEAD)).name("§2+").setCustomHeadTexture(ItemUtils.Texture.up).build(), (e) -> {
            calendar.add(Calendar.SECOND, 1);
            reopen(p, contents);
        }));
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        contents.set(5, 0, ClickableItem.of((new ItemBuilder(Material.BLAZE_POWDER))
                .name("§eСброс")
                .addLore("§7Подставить текущее время")
                //.addLore("§8unix time="+(int)(calendar.getTimeInMillis()/1000))
                //.addLore("§8calendar="+(int)(calendar.getTimeInMillis()/1000))
                .build(), (e) -> {
                    calendar.setTimeInMillis(Timer.getTime()*1000L);
                    reopen(p, contents);
        }));
        
        contents.set(5, 8, ClickableItem.of((new ItemBuilder(Material.SLIME_BALL))
                .name("§eПринять")
                .addLore("§8unix time="+(int)(calendar.getTimeInMillis()/1000))
                //.addLore("§8calendar="+(int)(calendar.getTimeInMillis()/1000))
                .build(), (e) -> {
                    //p.closeInventory();
                    //consumer.accept(stamp);
                    consumer.accept((int)(calendar.getTimeInMillis()/1000));
        }));
        

        
    }
}
