package ru.komiss77.builder.menu;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.*;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Colorable;
import ru.komiss77.modules.translate.Lang;
import ru.komiss77.utils.FastMath;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.TCUtils;
import ru.komiss77.utils.inventory.ClickableItem;
import ru.komiss77.utils.inventory.InventoryContent;
import ru.komiss77.utils.inventory.InventoryProvider;
import ru.komiss77.utils.inventory.SmartInventory;


public class EntitySetup implements InventoryProvider {


    private final ClickableItem c = ClickableItem.empty(new ItemStack(Material.GLOW_LICHEN));
    private final Entity en;

    public EntitySetup(final Entity e) {
        this.en = e;
    }

    public static void openSetupMenu(final Player p, final Entity entity) {
        SmartInventory.builder()
                .provider(new EntitySetup(entity))
                .size(6, 9)
                .title("§2Характеристики сущности").build()
                .open(p);
    }


    @Override
    public void init(final Player p, final InventoryContent content) {
        p.playSound(p.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 5, 5);
        content.fillBorders(c);


        if (en instanceof LivingEntity le) {

            content.add(ClickableItem.of(new ItemBuilder(le.hasAI() ? Material.LIME_DYE : Material.CLAY_BALL)
                    .name("§fAI " + (le.hasAI() ? "§aЕсть" : "§cНет"))
                    .addLore("")
                    .addLore("§7ЛКМ - §6" + (le.hasAI() ? "§aвыключить" : "§cвключить"))
                    .addLore("")
                    .build(), e -> {
                if (e.isLeftClick()) {
                    le.setAI(!le.hasAI());
                }
                reopen(p, content);
            }));

        }

        if (en instanceof Ageable) {
            content.add(ClickableItem.of(new ItemBuilder(((Ageable) en).isAdult() ? Material.LIME_DYE : Material.CLAY_BALL)
                    .name(((Ageable) en).isAdult() ? "§6ВЗРОСЛЫЙ" : "§6ребёнок")
                    .build(), e -> {
                if (e.isLeftClick()) {
                    if (((Ageable) en).isAdult()) {
                        ((Ageable) en).setBaby();
                    } else {
                        ((Ageable) en).setAdult();
                    }
                    reopen(p, content);
                }
            }));
        } //else if (en instanceof Zombie) {
        //    ((Zombie) en).setBaby(!((Zombie) en).isBaby());
        //}

        if (en.getType() == EntityType.VILLAGER) {

            final Profession prof = ((Villager) en).getProfession();
            final Profession prof_prev = Profession.values()[(prof.ordinal() - 1 + Profession.values().length) % Profession.values().length];
            final Profession prof_next = Profession.values()[prof.ordinal() + 1 % Profession.values().length];

            content.add(ClickableItem.of(new ItemBuilder(Material.ANVIL)
                    .name("§fПрофессия")
                    .addLore("")
                    .addLore(Component.text("§7ПКМ - сделать §6").append(Lang.t(prof_prev, p).style(Style.style(NamedTextColor.GOLD))))
                    .addLore(Component.text("§fСейчас : §e§l").append(Lang.t(prof, p).style(Style.style(NamedTextColor.YELLOW)).decorate(TextDecoration.BOLD)))
                    .addLore(Component.text("§7ЛКМ - сделать §6").append(Lang.t(prof_next, p).style(Style.style(NamedTextColor.GOLD))))
                    .addLore("")
                    .build(), e -> {
                if (e.isLeftClick()) {
                    ((Villager) en).setProfession(prof_next);
                } else if (e.isRightClick()) {
                    ((Villager) en).setProfession(prof_prev);
                }
                reopen(p, content);
            }));

            final Villager.Type type = ((Villager) en).getVillagerType();
            final Villager.Type type_prev = Villager.Type.values()[(type.ordinal() - 1 + Villager.Type.values().length) % Villager.Type.values().length];
            final Villager.Type type_next = Villager.Type.values()[type.ordinal() + 1 % Villager.Type.values().length];

            content.add(ClickableItem.of(new ItemBuilder(Material.ANVIL)
                    .name("§fТип")
                    .addLore("")
                    .addLore("§7ПКМ - сделать §6" + type_prev)
                    .addLore("§fСейчас : §e§l" + type)
                    .addLore("§7ЛКМ - сделать §6" + type_next)
                    .addLore("")
                    .build(), e -> {
                if (e.isLeftClick()) {
                    ((Villager) en).setVillagerType(type_next);
                } else if (e.isRightClick()) {
                    ((Villager) en).setVillagerType(type_prev);
                }
                reopen(p, content);
            }));

        } else if (en.getType() == EntityType.ZOMBIE_VILLAGER) {

            final Profession prof = ((ZombieVillager) en).getVillagerProfession();
            final Profession prof_prev = Profession.values()[(prof.ordinal() - 1 + Profession.values().length) % Profession.values().length];
            final Profession prof_next = Profession.values()[prof.ordinal() + 1 % Profession.values().length];

            content.add(ClickableItem.of(new ItemBuilder(Material.ANVIL)
                    .name("§fПрофессия")
                    .addLore("")
                    .addLore(Component.text("§7ПКМ - сделать §6").append(Lang.t(prof_prev, p).style(Style.style(NamedTextColor.GOLD))))
                    .addLore(Component.text("§fСейчас : §e§l").append(Lang.t(prof, p).style(Style.style(NamedTextColor.YELLOW)).decorate(TextDecoration.BOLD)))
                    .addLore(Component.text("§7ЛКМ - сделать §6").append(Lang.t(prof_next, p).style(Style.style(NamedTextColor.GOLD))))
                    .addLore("")
                    .build(), e -> {
                if (e.isLeftClick()) {
                    ((ZombieVillager) en).setVillagerProfession(prof_next);
                } else if (e.isRightClick()) {
                    ((ZombieVillager) en).setVillagerProfession(prof_prev);
                }
                reopen(p, content);
            }));
        }


        if (en.getType() == EntityType.SLIME || en.getType() == EntityType.MAGMA_CUBE) {
            Slime sl = (Slime) en;
            content.add(ClickableItem.of(new ItemBuilder(en.getType() == EntityType.SLIME ? Material.SLIME_BLOCK : Material.MAGMA_BLOCK)
                    .name("§fРазмер")
                    .addLore("")
                    .addLore("§fСейчас : §e§l" + sl.getSize())
                    .addLore(sl.getSize() < 120 ? "§7ЛКМ - §a+10" : "")
                    .addLore(sl.getSize() > 10 ? "§7ПКМ - сделать §c-10" : "")
                    .addLore("")
                    .build(), e -> {
                if (e.isLeftClick() && sl.getSize() < 120) {
                    sl.setSize(sl.getSize() + 10);
                } else if (e.isRightClick()) {
                    sl.setSize(sl.getSize() - 10);
                }
                reopen(p, content);
            }));
        }

        if (en.getType() == EntityType.WOLF) {
            content.add(ClickableItem.of(new ItemBuilder(((Wolf) en).isSitting() ? Material.LIME_DYE : Material.CLAY_BALL)
                    .name(((Wolf) en).isSitting() ? "§6Сидит" : "§6стоит")
                    .build(), e -> {
                if (e.isLeftClick()) {
                    ((Wolf) en).setSitting(!((Wolf) en).isSitting());
                    reopen(p, content);
                }
            }));
        }

        if (en.getType() == EntityType.CREEPER) {
            content.add(ClickableItem.of(new ItemBuilder(((Creeper) en).isPowered() ? Material.LIME_DYE : Material.CLAY_BALL)
                    .name(((Creeper) en).isPowered() ? "§6Заряжен" : "§6спокоен")
                    .build(), e -> {
                if (e.isLeftClick()) {
                    ((Creeper) en).setPowered(!((Creeper) en).isPowered());
                    reopen(p, content);
                }
            }));
            ((Creeper) en).setPowered(!((Creeper) en).isPowered());
        }

        if (en.getType() == EntityType.RABBIT) {
            final Rabbit.Type rt = ((Rabbit) en).getRabbitType();
            content.add(ClickableItem.of(new ItemBuilder(Material.RABBIT_HIDE)
                    .name("§fТип зайчика")
                    .addLore("§fСейчас : §e§l" + rt.name())
                    .build(), e -> {
                if (e.isLeftClick()) {
                    final Rabbit.Type rt2 = FastMath.rotateEnum(rt);//Rabbit.Type.values()[rt.ordinal() + 1 % Rabbit.Type.values().length];
                    ((Rabbit) en).setRabbitType(rt2);//rabbitTypeNext(((Rabbit) en).getRabbitType()));
                    if (((Rabbit) en).getRabbitType() != Rabbit.Type.THE_KILLER_BUNNY) {
                        en.setCustomNameVisible(false);
                    }
                    reopen(p, content);
                }
            }));
        }

        if (en.getType() == EntityType.SHEEP) {
            content.add(ClickableItem.of(new ItemBuilder(((Sheep) en).isSheared() ? Material.LIME_DYE : Material.CLAY_BALL)
                    .name(((Sheep) en).isSheared() ? "§6Стриженная" : "§6Мохнатая")
                    .build(), e -> {
                if (e.isLeftClick()) {
                    ((Sheep) en).setSheared(!((Sheep) en).isSheared());
                    reopen(p, content);
                }
            }));
        }


        if (en instanceof Colorable) {
            DyeColor dc = ((Colorable) en).getColor();
            content.add(ClickableItem.of(new ItemBuilder(Material.ORANGE_GLAZED_TERRACOTTA)
                    .name("§fЦвет")
                    .addLore("§fСейчас : " + TCUtils.toChat(dc) + TCUtils.dyeDisplayName(dc))
                    .build(), e -> {
                if (e.isLeftClick()) {
                    final DyeColor dc2 = FastMath.rotateEnum(dc);//DyeColor.values()[dc.ordinal() + 1 % DyeColor.values().length];
                    ((Colorable) en).setColor(dc2);
                    reopen(p, content);
                }
            }));
        }

        if (en.getType() == EntityType.WOLF) {
            DyeColor dc = ((Wolf) en).getCollarColor();
            content.add(ClickableItem.of(new ItemBuilder(Material.ORANGE_GLAZED_TERRACOTTA)
                    .name("§fЦвет")
                    .addLore("§fСейчас : " + TCUtils.toChat(dc) + TCUtils.dyeDisplayName(dc))
                    .build(), e -> {
                if (e.isLeftClick()) {
                    final DyeColor dc2 = FastMath.rotateEnum(dc);//DyeColor.values()[dc.ordinal() + 1 % DyeColor.values().length];
                    ((Wolf) en).setCollarColor(dc2);
                    reopen(p, content);
                }
            }));
            //int curr_color = ((Wolf) en).getCollarColor().ordinal();
            // curr_color++;
            //if (curr_color >= DyeColor.values().length) {
            //    curr_color = 0;
            //}
            //((Wolf) en).setCollarColor(DyeColor.values()[curr_color]);
        }


        if (en.getType() == EntityType.LLAMA || en.getType() == EntityType.LLAMA_SPIT) {
            Llama.Color dc = ((Llama) en).getColor();
            content.add(ClickableItem.of(new ItemBuilder(Material.RABBIT_HIDE)
                    .name("§fЦвет")
                    .addLore("§fСейчас : " + dc.name())
                    .build(), e -> {
                if (e.isLeftClick()) {
                    final Llama.Color dc2 = FastMath.rotateEnum(dc);//Llama.Color.values()[dc.ordinal() + 1 % Llama.Color.values().length];
                    ((Llama) en).setColor(dc2);
                    reopen(p, content);
                }
            }));
            //switch (((Llama) en).getColor()) {
            //     case BROWN ->
            //            ((Llama) en).setColor(Llama.Color.CREAMY);
            //    case CREAMY ->
            //            ((Llama) en).setColor(Llama.Color.GRAY);
            //    case GRAY ->
            //            ((Llama) en).setColor(Llama.Color.WHITE);
            //    case WHITE ->
            //             ((Llama) en).setColor(Llama.Color.BROWN);
            // }
        }


        if (en.getType() == EntityType.HORSE) {
            Horse.Color dc = ((Horse) en).getColor();
            content.add(ClickableItem.of(new ItemBuilder(Material.ORANGE_GLAZED_TERRACOTTA)
                    .name("§fЦвет")
                    .addLore("§fСейчас : " + dc.name())
                    .build(), e -> {
                if (e.isLeftClick()) {
                    final Horse.Color dc2 = FastMath.rotateEnum(dc);//Horse.Color.values()[dc.ordinal() + 1 % Horse.Color.values().length];
                    ((Horse) en).setColor(dc2);
                    reopen(p, content);
                }
            }));
        }


        if (en instanceof Steerable) {
            Steerable st = (Steerable) en;
            content.add(ClickableItem.of(new ItemBuilder(st.hasSaddle() ? Material.LIME_DYE : Material.CLAY_BALL)
                    .name(st.hasSaddle() ? "§6Осёдланная" : "§6Дикая")
                    .build(), e -> {
                if (e.isLeftClick()) {
                    st.setSaddle(!st.hasSaddle());//(Sheep) en).setSheared(!((Sheep) en).isSheared());
                    reopen(p, content);
                }
            }));
        }

        if (en instanceof ChestedHorse) {
            ChestedHorse st = (ChestedHorse) en;
            content.add(ClickableItem.of(new ItemBuilder(st.isCarryingChest() ? Material.LIME_DYE : Material.CLAY_BALL)
                    .name(st.isCarryingChest() ? "§6Грузовая" : "§6Пустая")
                    .build(), e -> {
                if (e.isLeftClick()) {
                    st.setCarryingChest(!st.isCarryingChest());//(Sheep) en).setSheared(!((Sheep) en).isSheared());
                    reopen(p, content);
                }
            }));
        }

        //switch (en.getType()) {
        //     case PIG ->
        //            ((Pig) en).setSaddle(!((Pig) en).hasSaddle());
        //    case HORSE -> {
        //       if (((Horse) en).getInventory().getSaddle() != null && ((Horse) en).getInventory().getSaddle().getType() == Material.SADDLE) {
        //           ((Horse) en).getInventory().setSaddle(new ItemStack(Material.AIR));
        //       } else {
        //           ((Horse) en).getInventory().setSaddle(new ItemStack(Material.SADDLE));
        //       }
        //    }
        //    case MULE, DONKEY ->
        //           ((ChestedHorse) en).setCarryingChest(!((ChestedHorse) en).isCarryingChest());
        // }


        if (en.getType() == EntityType.WOLF) {
            content.add(ClickableItem.of(new ItemBuilder(((Wolf) en).isTamed() ? Material.LIME_DYE : Material.CLAY_BALL)
                    .name(((Wolf) en).isTamed() ? "§6Ручной" : "§6Дикий")
                    .build(), e -> {
                if (e.isLeftClick()) {
                    ((Wolf) en).setTamed(!((Wolf) en).isTamed());
                    reopen(p, content);
                }
            }));
        }


        if (en.getType() == EntityType.OCELOT) {
            Cat.Type dc = ((Cat) en).getCatType();
            content.add(ClickableItem.of(new ItemBuilder(Material.PUFFERFISH)
                    .name("§fТип")
                    .addLore("§fСейчас : " + dc.name())
                    .build(), e -> {
                if (e.isLeftClick()) {
                    final Cat.Type dc2 = FastMath.rotateEnum(dc);//Cat.Type.values()[dc.ordinal() + 1 % Cat.Type.values().length];
                    ((Cat) en).setCatType(dc2);
                    reopen(p, content);
                }
            }));
            //switch (((Cat) en).getCatType()) {
            //    case BLACK ->
            //            ((Cat) en).setCatType(Cat.Type.RED);
            //    case RED ->
            //           ((Cat) en).setCatType(Cat.Type.SIAMESE);
            //   case SIAMESE ->
            //           ((Cat) en).setCatType(Cat.Type.RAGDOLL);
            //   case RAGDOLL ->
            //           ((Cat) en).setCatType(Cat.Type.BLACK);
            //}
        }

        if (en.getType() == EntityType.PARROT) {
            Parrot.Variant dc = ((Parrot) en).getVariant();
            content.add(ClickableItem.of(new ItemBuilder(Material.PUFFERFISH)
                    .name("§fТип")
                    .addLore("§fСейчас : " + dc.name())
                    .build(), e -> {
                if (e.isLeftClick()) {
                    final Parrot.Variant dc2 = FastMath.rotateEnum(dc);//Parrot.Variant.values()[dc.ordinal() + 1 % Parrot.Variant.values().length];
                    ((Parrot) en).setVariant(dc2);
                    reopen(p, content);
                }
            }));
            //switch (((Parrot) en).getVariant()) {
            //   case BLUE ->
            //           ((Parrot) en).setVariant(Parrot.Variant.CYAN);
            //  case CYAN ->
            //          ((Parrot) en).setVariant(Parrot.Variant.GRAY);
            //  case GRAY ->
            //           ((Parrot) en).setVariant(Parrot.Variant.GREEN);
            //   case GREEN ->
            //            ((Parrot) en).setVariant(Parrot.Variant.RED);
            //    case RED ->
            //            ((Parrot) en).setVariant(Parrot.Variant.BLUE);
            //}
        }

        if (en.getType() == EntityType.SNOWMAN) {
            content.add(ClickableItem.of(new ItemBuilder(((Snowman) en).isDerp() ? Material.LIME_DYE : Material.CLAY_BALL)
                    .name(((Snowman) en).isDerp() ? "§6Тающий" : "§6Свежий")
                    .build(), e -> {
                if (e.isLeftClick()) {
                    ((Snowman) en).setDerp(!((Snowman) en).isDerp());
                    reopen(p, content);
                }
            }));
        }


        content.set(5, 8, ClickableItem.of(new ItemBuilder(Material.REDSTONE)
                .name("§4Убрать моба")
                .build(), e -> {
            if (e.isLeftClick()) {
                en.remove();
                p.closeInventory();
            }
        }));

    }


}


/*


    public static void onClick(final BBPlayer bp, final int slot) {
        switch(slot) {
            
            case 0:
                if (bp.targetEntity instanceof LivingEntity) {
                    LivingEntity le = (LivingEntity) bp.targetEntity;
                    le.setAI(false);
                }
                //final net.minecraft.world.entity.Entity nms_entity = ((CraftEntity) bp.targetEntity).getHandle();
                //if (nms_entity instanceof EntityInsentient) ((EntityInsentient)nms_entity).setNoAI( !((EntityInsentient)nms_entity).isNoAI() );
                break;
            case 1:
                if (bp.targetEntity instanceof Ageable) {
                    if (((Ageable)bp.targetEntity).isAdult()) ((Ageable)bp.targetEntity).setBaby(); else ((Ageable)bp.targetEntity).setAdult();
                } else if (bp.targetEntity instanceof Zombie) {
                    ((Zombie)bp.targetEntity).setBaby( !((Zombie)bp.targetEntity).isBaby() );
                }
                break;

            case 3:
                if (bp.targetEntity.getType()==EntityType.SLIME || bp.targetEntity.getType()==EntityType.MAGMA_CUBE) {
                    if (((Slime)bp.targetEntity).getSize()>=8) {
                        ((Slime)bp.targetEntity).setSize(1);
                    } else if (((Slime)bp.targetEntity).getSize()>=5) {
                        ((Slime)bp.targetEntity).setSize(8);
                    } else {
                        ((Slime)bp.targetEntity).setSize(5);
                    }
                } 
               break;
            case 4:
                if (bp.targetEntity.getType()==EntityType.WOLF) ((Wolf)bp.targetEntity).setSitting( !((Wolf)bp.targetEntity).isSitting() );
                break;
                
            case 5:
                if (bp.targetEntity.getType()==EntityType.CREEPER) ((Creeper)bp.targetEntity).setPowered( !((Creeper)bp.targetEntity).isPowered() );
                break;
                
            case 6:
                if (bp.targetEntity.getType()==EntityType.RABBIT) {
                   ((Rabbit)bp.targetEntity).setRabbitType(rabbitTypeNext(((Rabbit)bp.targetEntity).getRabbitType()));
                   if (((Rabbit)bp.targetEntity).getRabbitType()!=Rabbit.Type.THE_KILLER_BUNNY) bp.targetEntity.setCustomNameVisible(false);
                } 
               break;
            case 7:
                if (bp.targetEntity.getType()==EntityType.SHEEP) {
                   ((Sheep)bp.targetEntity).setSheared( !((Sheep)bp.targetEntity).isSheared());
                } 
               break;
            case 8:
                if (bp.targetEntity instanceof Colorable) {
                    int curr_color=((Colorable)bp.targetEntity).getColor().ordinal();
                    curr_color++;
                    if (curr_color>=DyeColor.values().length) curr_color=0;
                    ((Colorable)bp.targetEntity).setColor(DyeColor.values()[curr_color]);
                }
                break;
               
               
               
               
               
               
               
                


            case 9:
                 switch (bp.targetEntity.getType()) {
                    case PIG:
                        ((Pig)bp.targetEntity).setSaddle( !((Pig)bp.targetEntity).hasSaddle() );
                        break;
                    case HORSE:
                        if (((Horse)bp.targetEntity).getInventory().getSaddle()!=null && ((Horse)bp.targetEntity).getInventory().getSaddle().getType()==Material.SADDLE) ((Horse)bp.targetEntity).getInventory().setSaddle(new ItemStack(Material.AIR)); else ((Horse)bp.targetEntity).getInventory().setSaddle(new ItemStack(Material.SADDLE));
                        break; 
                    case MULE:
                    case DONKEY:
                        ((ChestedHorse)bp.targetEntity).setCarryingChest( !((ChestedHorse)bp.targetEntity).isCarryingChest() );
                        break;
                    }
                break;
            case 10:
                if (bp.targetEntity.getType()==EntityType.HORSE) {
                    ((Horse)bp.targetEntity).setColor(horseColorNext(((Horse)bp.targetEntity).getColor()));
                }
                break;
            case 11:
                if (bp.targetEntity.getType()==EntityType.HORSE) {
                    ((Horse)bp.targetEntity).setStyle(horseStileNext(((Horse)bp.targetEntity).getStyle()));
                } if (bp.targetEntity.getType()==EntityType.WOLF) {
                    ((Wolf)bp.targetEntity).setTamed(!((Wolf)bp.targetEntity).isTamed());
                }
                break;
            case 12:
                if (bp.targetEntity.getType()==EntityType.LLAMA || bp.targetEntity.getType()==EntityType.LLAMA_SPIT) {
System.out.println("Llama color = "+((Llama)bp.targetEntity).getColor());
                    switch (((Llama)bp.targetEntity).getColor()) {
                        case BROWN: ((Llama)bp.targetEntity).setColor(Llama.Color.CREAMY);break;
                        case CREAMY: ((Llama)bp.targetEntity).setColor(Llama.Color.GRAY);break;
                        case GRAY: ((Llama)bp.targetEntity).setColor(Llama.Color.WHITE);break;
                        case WHITE: ((Llama)bp.targetEntity).setColor(Llama.Color.BROWN);break;
                    }
                }
                break;
            case 13:
                if (bp.targetEntity.getType()==EntityType.WOLF) {
                    int curr_color=((Wolf)bp.targetEntity).getCollarColor().ordinal();
                    curr_color++;
                    if (curr_color>=DyeColor.values().length) curr_color=0;
                    ((Wolf)bp.targetEntity).setCollarColor(DyeColor.values()[curr_color]);
                }
                break;
            case 14:
                if (bp.targetEntity.getType()==EntityType.OCELOT) {
                    switch (((Cat)bp.targetEntity).getCatType()) {
                        case BLACK: ((Cat)bp.targetEntity).setCatType(Cat.Type.RED);break;
                        case RED: ((Cat)bp.targetEntity).setCatType(Cat.Type.SIAMESE);break;
                        case SIAMESE: ((Cat)bp.targetEntity).setCatType(Cat.Type.RAGDOLL);break;
                        case RAGDOLL: ((Cat)bp.targetEntity).setCatType(Cat.Type.BLACK);break;
                    }
                }
                break;
            case 15:
                if (bp.targetEntity.getType()==EntityType.PARROT) {
                    switch (((Parrot)bp.targetEntity).getVariant()) {
                        case BLUE: ((Parrot)bp.targetEntity).setVariant(Parrot.Variant.CYAN);break;
                        case CYAN: ((Parrot)bp.targetEntity).setVariant(Parrot.Variant.GRAY);break;
                        case GRAY: ((Parrot)bp.targetEntity).setVariant(Parrot.Variant.GREEN);break;
                        case GREEN: ((Parrot)bp.targetEntity).setVariant(Parrot.Variant.RED);break;
                        case RED: ((Parrot)bp.targetEntity).setVariant(Parrot.Variant.BLUE);break;
                    }
                }
                break;
            case 16:
                if (bp.targetEntity.getType()==EntityType.SNOWMAN) {
                    ((Snowman)bp.targetEntity).setDerp(!((Snowman)bp.targetEntity).isDerp());
                }
                break;


               
                
                
                
                
                
                
                
                
               
            case 22:
                final Vector vector=bp.getPlayer().getLocation().toVector().subtract(bp.targetEntity.getLocation().toVector());
                bp.targetEntity.teleport(bp.targetEntity.getLocation().clone().setDirection(vector));
                bp.getPlayer().closeInventory();
                return;
                
            case 26:
                bp.targetEntity.remove();
                if (bp.plot.entity_count>0) bp.plot.entity_count--;
                bp.plot.sendAB("§fУдалено существо "+bp.targetEntity.getType().toString()+", теперь можно добавить: §b"+(bp.arena.maxCreaturesPerPlot-bp.plot.entity_count));
                bp.getPlayer().closeInventory();
                return;
        }
        openEntityMenu(bp);
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    public static void openEntityMenu(final BBPlayer bp) {
        if (bp.targetEntity==null) return;
        
        final Inventory entity_menu = Bukkit.createInventory(null, 27, "§2Настройки существа");
        ItemStack is;
        boolean has = false;
            if (bp.targetEntity instanceof LivingEntity) {
                    LivingEntity le = (LivingEntity) bp.targetEntity;
                    has = le.hasAI();
                }
            //final net.minecraft.world.entity.Entity nms_entity = ((CraftEntity) bp.targetEntity).getHandle();
            //boolean has = (nms_entity instanceof EntityInsentient) && !((EntityInsentient)nms_entity).isNoAI();
                entity_menu.setItem(0, new ItemBuilder(has ? Material.JACK_O_LANTERN : Material.PUMPKIN).name("§eВключение/выключение разума").setLore( has ? "§2AI включен" : "§4AI вылючен", "").build());
            
            if (bp.targetEntity instanceof Ageable) {
                has = ((Ageable)bp.targetEntity).isAdult();
                    //entity_menu.setItem(1, new ItemBuilder(has ? Material.MELON_BLOCK : Material.MELON).name("§eИзменение возраста").setLore( has ? "§2Взрослый" : "§bМалыш", "§b").build());
                    entity_menu.setItem(1, new ItemBuilder(has ? Material.MELON : Material.MELON_SLICE).name("§eИзменение возраста").setLore( has ? "§2Взрослый" : "§bМалыш", "§b").build());
            } else if (bp.targetEntity instanceof Zombie) {
                has = !((Zombie)bp.targetEntity).isBaby();
                    //entity_menu.setItem(1, new ItemBuilder(has ? Material.MELON_BLOCK : Material.MELON).name("§eИзменение возраста").setLore( has ? "§2Взрослый" : "§bМалыш", "§b").build());
                    entity_menu.setItem(1, new ItemBuilder(has ? Material.MELON : Material.MELON_SLICE).name("§eИзменение возраста").setLore( has ? "§2Взрослый" : "§bМалыш", "§b").build());
            }

            if (bp.targetEntity.getType()==EntityType.VILLAGER) {
                VillaferProf vp = VillaferProf.valueOf(((Villager)bp.targetEntity).getProfession().toString());
                    entity_menu.setItem(2, new ItemBuilder(vp.mat).name("§eИзменение профессии").setLore(vp.translate, "§7").build());
            } else if (bp.targetEntity.getType()==EntityType.ZOMBIE_VILLAGER) {
                VillaferProf vp = VillaferProf.valueOf(((ZombieVillager)bp.targetEntity).getVillagerProfession().toString());
                    entity_menu.setItem(2, new ItemBuilder(vp.mat).name("§eИзменение профессии").setLore(vp.translate, "§7").build());
            } 

            if (bp.targetEntity.getType()==EntityType.SLIME || bp.targetEntity.getType()==EntityType.MAGMA_CUBE) {
                if (((Slime)bp.targetEntity).getSize()>=8) {
                    entity_menu.setItem(3, new ItemBuilder(Material.GOLD_BLOCK).name("§eКрупный").build());
                } else if (((Slime)bp.targetEntity).getSize()>=8) {
                    entity_menu.setItem(3, new ItemBuilder(Material.GOLD_INGOT).name("§eСредний").build());
                } else {
                    entity_menu.setItem(3, new ItemBuilder(Material.GOLD_NUGGET).name("§eМелкий").build());
                }
            } 
            
            if (bp.targetEntity.getType()==EntityType.RABBIT) {
               entity_menu.setItem(6, new ItemBuilder(Material.LEATHER).name("§e"+((Rabbit)bp.targetEntity).getRabbitType().toString()).build());
            }            
            
            
            if (bp.targetEntity.getType()==EntityType.HORSE) {
                has = ((Horse)bp.targetEntity).getInventory().getSaddle()!=null && ((Horse)bp.targetEntity).getInventory().getSaddle().getType() == Material.SADDLE;
                    entity_menu.setItem(9, new ItemBuilder(Material.SADDLE).name("§eСедло").setLore( has ? "§2Есть" : "§сНет", "").build());
                    
                    is = new ItemBuilder(Material.WHITE_CONCRETE_POWDER).name("§eЦвет щёрстки").build();
                    is = TCUtils.changeColor(is, horseColorData(((Horse)bp.targetEntity).getColor()) );
                    //entity_menu.setItem(10, new ItemBuilder(Material.CONCRETE_POWDER, horseColorData(((Horse)bp.targetEntity).getColor()) ).name("§eЦвет щёрстки").build());
                    entity_menu.setItem(10, is);
                    entity_menu.setItem(11, new ItemBuilder(horseTypeMat(((Horse)bp.targetEntity).getStyle()) ).name("§eПорода").build());
            }
            
            if (bp.targetEntity instanceof Colorable) {
                is = new ItemBuilder(Material.WHITE_WOOL).name("§7Менять цвет").build();
                is = TCUtils.changeColor(is, ((Colorable)bp.targetEntity).getColor());
               //entity_menu.setItem(8, new ItemBuilder(Material.WOOL, colorDataFromDyeColor(((Colorable)bp.targetEntity).getColor())).name("§7Менять цвет").build());
               entity_menu.setItem(8, is);
                
            }            
            
            
            if (bp.targetEntity.getType()==EntityType.PIG) {
                has = ((Pig)bp.targetEntity).hasSaddle();
                    entity_menu.setItem(9, new ItemBuilder(Material.SADDLE).name("§eСедло").setLore( has ? "§2Есть" : "§сНет", "").build());
            } else if (bp.targetEntity.getType()==EntityType.MULE || bp.targetEntity.getType()==EntityType.DONKEY) {
                has = ((ChestedHorse)bp.targetEntity).isCarryingChest();
                    entity_menu.setItem(9, new ItemBuilder(Material.CHEST).name("§eСундук").setLore( has ? "§2Есть" : "§сНет", "").build());
            } 

            if (bp.targetEntity.getType()==EntityType.WOLF) {
                has = ((Wolf)bp.targetEntity).isTamed();
                if (has) {
                    entity_menu.setItem(11, new ItemBuilder(Material.NAME_TAG).name("§7Ручной").build());
                    is = new ItemBuilder(Material.WHITE_WOOL).name("§7Цвет Ошейника").build();
                    is = TCUtils.changeColor(is, ((Wolf)bp.targetEntity).getCollarColor());
                    //entity_menu.setItem(13, new ItemBuilder(Material.WOOL, colorDataFromDyeColor(((Wolf)bp.targetEntity).getCollarColor())).name("§7Цвет Ошейника").build());
                    entity_menu.setItem(13, is);
                } else {
                    entity_menu.setItem(11, new ItemBuilder(Material.BONE).name("§7Дикий").build());
                    entity_menu.setItem(13, new ItemStack(Material.AIR));
                }
                has = ((Wolf)bp.targetEntity).isSitting();
                    entity_menu.setItem(4, new ItemBuilder(Material.TRIPWIRE_HOOK).name("§eСидеть").setLore( has ? "§fДа" : "§fНет", "").build());
            }
            
            if (bp.targetEntity.getType()==EntityType.CREEPER) {
                has = ((Creeper)bp.targetEntity).isPowered();
                    entity_menu.setItem(5, new ItemBuilder(Material.TNT).name("§eВзрывоопасный").setLore( has ? "§fДа" : "§fНет", "").build());
            }
            
            if (bp.targetEntity.getType()==EntityType.SHEEP) {
                has = ((Sheep)bp.targetEntity).isSheared();
                    entity_menu.setItem(7, new ItemBuilder(Material.SHEARS).name("§eСтриженая").setLore( has ? "§fДа" : "§fНет", "").build());
            }
            
            if (bp.targetEntity.getType()==EntityType.LLAMA || bp.targetEntity.getType()==EntityType.LLAMA_SPIT) {
                switch (((Llama)bp.targetEntity).getColor()) {
                    case BROWN: entity_menu.setItem(12, new ItemBuilder(Material.BROWN_WOOL).name("§6Коричневая").build());break;
                    case CREAMY: entity_menu.setItem(12, new ItemBuilder(Material.PINK_WOOL).name("§сРозовая").build());break;
                    case GRAY: entity_menu.setItem(12, new ItemBuilder(Material.GRAY_WOOL).name("§7Серая").build());break;
                    case WHITE: entity_menu.setItem(12, new ItemBuilder(Material.WHITE_WOOL).name("§fБелая").build());break;
                }
            }
            if (bp.targetEntity.getType()==EntityType.OCELOT) {
                    entity_menu.setItem(14, new ItemBuilder(Material.LEATHER).name("§eТип: "+((Ocelot)bp.targetEntity).getCatType().toString()).build());
            }
            if (bp.targetEntity.getType()==EntityType.PARROT) {
                    entity_menu.setItem(15, new ItemBuilder(Material.LEATHER ).name("§eТип: "+((Parrot)bp.targetEntity).getVariant().toString()).build());
            }
            if (bp.targetEntity.getType()==EntityType.SNOWMAN) {
                has = !((Snowman)bp.targetEntity).isDerp();
                    entity_menu.setItem(16, new ItemBuilder(Material.PACKED_ICE).name("§eСтрашный").setLore( has ? "§fДа" : "§fНет", "").build());
            }
            //горение ??

        entity_menu.setItem(22, new ItemBuilder(Material.COMPASS).name("§eПовернись ко мне!").build());
        
        
        entity_menu.setItem(26, new ItemBuilder(Material.BARRIER).name("§сУдалить существо").build());

        bp.getPlayer().openInventory(entity_menu);
    }

    

    
    
    
    
    
    
    
    
    
    
    
    
    
    
        
    public static Horse.Color horseColorNext(final Horse.Color current) {
        switch (current) {
            case BLACK : 
                return Horse.Color.BROWN;
            case BROWN : 
                return Horse.Color.CHESTNUT;
            case CHESTNUT : 
                return Horse.Color.CREAMY;
            case CREAMY : 
                return Horse.Color.DARK_BROWN;
            case DARK_BROWN : 
                return Horse.Color.GRAY;
            case GRAY : 
                return Horse.Color.WHITE;
            default: 
                return Horse.Color.BLACK;
        }
    }   

    public static DyeColor horseColorData(final Horse.Color current) {
        switch (current) {
            case BLACK : 
                return DyeColor.BLACK;
            case BROWN : 
                return DyeColor.BROWN;
            case CHESTNUT : 
                return DyeColor.LIGHT_GRAY;
            case CREAMY : 
                return DyeColor.YELLOW;
            case DARK_BROWN : 
                return DyeColor.BROWN;
            case GRAY : 
                return DyeColor.GRAY;
            case WHITE : 
            default: 
                return DyeColor.WHITE;
        }
    }   

    private static Material horseTypeMat(final Horse.Style style) {
        switch (style) {
            case BLACK_DOTS: 
                return Material.MELON_SEEDS;
            case WHITE: 
                return Material.PRISMARINE_CRYSTALS;
            case WHITEFIELD: 
                return Material.BEETROOT_SEEDS;
            case WHITE_DOTS: 
                return Material.PUMPKIN_SEEDS;
            case NONE: 
            default: 
            return Material.CLAY_BALL;
        }
    }

    private static Horse.Style horseStileNext(final Horse.Style style) {
        switch (style) {
            case BLACK_DOTS: 
                return Horse.Style.NONE;
            case NONE: 
                return Horse.Style.WHITE;
            case WHITE: 
                return Horse.Style.WHITEFIELD;
            case WHITEFIELD: 
                return Horse.Style.WHITE_DOTS;
            case WHITE_DOTS: 
            default: 
            return Horse.Style.BLACK_DOTS;
        }
    }

    private static Rabbit.Type rabbitTypeNext(final Rabbit.Type current) {
        switch(current) {
            case BLACK:
                return Rabbit.Type.BLACK_AND_WHITE;
            case BLACK_AND_WHITE:
                return Rabbit.Type.BROWN;
            case BROWN:
                return Rabbit.Type.GOLD;
            case GOLD:
                return Rabbit.Type.SALT_AND_PEPPER;
            case SALT_AND_PEPPER:
                return Rabbit.Type.THE_KILLER_BUNNY;
            case THE_KILLER_BUNNY:
                return Rabbit.Type.WHITE;
            case WHITE:
            default:
                return Rabbit.Type.BLACK;
        }
    }





*/