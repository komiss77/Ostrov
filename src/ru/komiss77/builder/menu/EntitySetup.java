package ru.komiss77.builder.menu;

import java.util.List;
import io.papermc.paper.registry.RegistryKey;
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
import ru.komiss77.OStrap;
import ru.komiss77.modules.translate.Lang;
import ru.komiss77.utils.ClassUtil;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.TCUtil;
import ru.komiss77.utils.inventory.ClickableItem;
import ru.komiss77.utils.inventory.InventoryContent;
import ru.komiss77.utils.inventory.InventoryProvider;
import ru.komiss77.utils.inventory.SmartInventory;


public class EntitySetup implements InventoryProvider {


    private static final List<Profession> VILL_PROFS = OStrap.retrieveAll(RegistryKey.VILLAGER_PROFESSION);
    private static final List<Villager.Type> VILL_TYPES = OStrap.retrieveAll(RegistryKey.VILLAGER_TYPE);
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

        switch (en) {
            case final Villager vg:

                final Profession prof = vg.getProfession();
                final int pi = VILL_PROFS.indexOf(prof);
                final int psz = VILL_PROFS.size();
                final Profession prof_prev = VILL_PROFS.get((pi - 1 + psz) % psz);
                final Profession prof_next = VILL_PROFS.get((pi + 1) % psz);

                content.add(ClickableItem.of(new ItemBuilder(Material.ANVIL)
                    .name("§fПрофессия")
                    .lore("")
                    .lore(TCUtil.form("§7ПКМ - сделать §6").append(Lang.t(prof_prev, p).style(Style.style(NamedTextColor.GOLD))))
                    .lore(TCUtil.form("§fСейчас : §e§l").append(Lang.t(prof, p).style(Style.style(NamedTextColor.YELLOW)).decorate(TextDecoration.BOLD)))
                    .lore(TCUtil.form("§7ЛКМ - сделать §6").append(Lang.t(prof_next, p).style(Style.style(NamedTextColor.GOLD))))
                    .lore("")
                    .build(), e -> {
                    if (e.isLeftClick()) {
                        vg.setProfession(prof_next);
                    } else if (e.isRightClick()) {
                        vg.setProfession(prof_prev);
                    }
                    reopen(p, content);
                }));

                final Villager.Type type = vg.getVillagerType();
                final int ti = VILL_TYPES.indexOf(prof);
                final int tsz = VILL_TYPES.size();
                final Villager.Type type_prev = VILL_TYPES.get((ti - 1 + tsz) % tsz);
                final Villager.Type type_next = VILL_TYPES.get((ti + 1) % tsz);

                content.add(ClickableItem.of(new ItemBuilder(Material.ANVIL)
                    .name("§fТип")
                    .lore("")
                    .lore("§7ПКМ - сделать §6" + type_prev)
                    .lore("§fСейчас : §e§l" + type)
                    .lore("§7ЛКМ - сделать §6" + type_next)
                    .lore("")
                    .build(), e -> {
                    if (e.isLeftClick()) {
                        vg.setVillagerType(type_next);
                    } else if (e.isRightClick()) {
                        vg.setVillagerType(type_prev);
                    }
                    reopen(p, content);
                }));
                break;
            case final Ageable ag:
                content.add(ClickableItem.of(new ItemBuilder(ag.isAdult() ? Material.LIME_DYE : Material.CLAY_BALL)
                    .name(ag.isAdult() ? "§6ВЗРОСЛЫЙ" : "§6ребёнок")
                    .build(), e -> {
                    if (e.isLeftClick()) {
                        if (ag.isAdult()) {
                            ag.setBaby();
                        } else {
                            ag.setAdult();
                        }
                        reopen(p, content);
                    }
                }));
                break;
            case final LivingEntity le:
                content.add(ClickableItem.of(new ItemBuilder(le.hasAI() ? Material.LIME_DYE : Material.CLAY_BALL)
                    .name("§fAI " + (le.hasAI() ? "§aЕсть" : "§cНет"))
                    .lore("")
                    .lore("§7ЛКМ - §6" + (le.hasAI() ? "§aвыключить" : "§cвключить"))
                    .lore("")
                    .build(), e -> {
                    if (e.isLeftClick()) {
                        le.setAI(!le.hasAI());
                    }
                    reopen(p, content);
                }));
                break;
            default:
                break;
        }

        //TODO заканчиваем то что сверху

        if (en.getType() == EntityType.ZOMBIE_VILLAGER) {
            final Profession prof = ((ZombieVillager) en).getVillagerProfession();
            final Profession prof_prev = Profession.values()[(prof.ordinal() - 1 + Profession.values().length) % Profession.values().length];
            final Profession prof_next = Profession.values()[prof.ordinal() + 1 % Profession.values().length];

            content.add(ClickableItem.of(new ItemBuilder(Material.ANVIL)
                .name("§fПрофессия")
                .lore("")
                .lore(TCUtil.form("§7ПКМ - сделать §6").append(Lang.t(prof_prev, p).style(Style.style(NamedTextColor.GOLD))))
                .lore(TCUtil.form("§fСейчас : §e§l").append(Lang.t(prof, p).style(Style.style(NamedTextColor.YELLOW)).decorate(TextDecoration.BOLD)))
                .lore(TCUtil.form("§7ЛКМ - сделать §6").append(Lang.t(prof_next, p).style(Style.style(NamedTextColor.GOLD))))
                .lore("")
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
                .lore("")
                .lore("§fСейчас : §e§l" + sl.getSize())
                .lore(sl.getSize() < 120 ? "§7ЛКМ - §a+10" : "")
                .lore(sl.getSize() > 10 ? "§7ПКМ - сделать §c-10" : "")
                .lore("")
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
        }

        if (en.getType() == EntityType.RABBIT) {
            final Rabbit.Type rt = ((Rabbit) en).getRabbitType();
            content.add(ClickableItem.of(new ItemBuilder(Material.RABBIT_HIDE)
                .name("§fТип зайчика")
                .lore("§fСейчас : §e§l" + rt.name())
                .build(), e -> {
                if (e.isLeftClick()) {
                    final Rabbit.Type rt2 = ClassUtil.rotateEnum(rt);//Rabbit.Type.values()[rt.ordinal() + 1 % Rabbit.Type.values().length];
                    ((Rabbit) en).setRabbitType(rt2);//rabbitTypeNext(((Rabbit) en).getRabbitType()));
                    if (((Rabbit) en).getRabbitType() != Rabbit.Type.THE_KILLER_BUNNY) {
                        en.setCustomNameVisible(false);
                    }
                    reopen(p, content);
                }
            }));
        }

        if (en.getType() == EntityType.SHEEP) {
            content.add(ClickableItem.of(new ItemBuilder(((Sheep) en).readyToBeSheared() ? Material.CLAY_BALL : Material.LIME_DYE)
                .name(((Sheep) en).readyToBeSheared() ? "§6Мохнатая" : "§6Стриженная")
                .build(), e -> {
                if (e.isLeftClick()) {
                    if (((Sheep) en).readyToBeSheared()) {
                        ((Sheep) en).shear();
                    } else {
//                        ((Sheep) en).setSheared(false); Deprecated
                    }
                    reopen(p, content);
                }
            }));
        }


        if (en instanceof Colorable) {
            DyeColor dc = ((Colorable) en).getColor();
            content.add(ClickableItem.of(new ItemBuilder(Material.ORANGE_GLAZED_TERRACOTTA)
                .name("§fЦвет")
                .lore("§fСейчас : " + TCUtil.toChat(dc) + TCUtil.dyeDisplayName(dc))
                .build(), e -> {
                if (e.isLeftClick()) {
                    final DyeColor dc2 = ClassUtil.rotateEnum(dc);//DyeColor.values()[dc.ordinal() + 1 % DyeColor.values().length];
                    ((Colorable) en).setColor(dc2);
                    reopen(p, content);
                }
            }));
        }

        if (en.getType() == EntityType.WOLF) {
            DyeColor dc = ((Wolf) en).getCollarColor();
            content.add(ClickableItem.of(new ItemBuilder(Material.ORANGE_GLAZED_TERRACOTTA)
                .name("§fЦвет")
                .lore("§fСейчас : " + TCUtil.toChat(dc) + TCUtil.dyeDisplayName(dc))
                .build(), e -> {
                if (e.isLeftClick()) {
                    final DyeColor dc2 = ClassUtil.rotateEnum(dc);//DyeColor.values()[dc.ordinal() + 1 % DyeColor.values().length];
                    ((Wolf) en).setCollarColor(dc2);
                    reopen(p, content);
                }
            }));
        }


        if (en.getType() == EntityType.LLAMA || en.getType() == EntityType.LLAMA_SPIT) {
            Llama.Color dc = ((Llama) en).getColor();
            content.add(ClickableItem.of(new ItemBuilder(Material.RABBIT_HIDE)
                .name("§fЦвет")
                .lore("§fСейчас : " + dc.name())
                .build(), e -> {
                if (e.isLeftClick()) {
                    final Llama.Color dc2 = ClassUtil.rotateEnum(dc);//Llama.Color.values()[dc.ordinal() + 1 % Llama.Color.values().length];
                    ((Llama) en).setColor(dc2);
                    reopen(p, content);
                }
            }));
        }


        if (en.getType() == EntityType.HORSE) {
            Horse.Color dc = ((Horse) en).getColor();
            content.add(ClickableItem.of(new ItemBuilder(Material.ORANGE_GLAZED_TERRACOTTA)
                .name("§fЦвет")
                .lore("§fСейчас : " + dc.name())
                .build(), e -> {
                if (e.isLeftClick()) {
                    final Horse.Color dc2 = ClassUtil.rotateEnum(dc);//Horse.Color.values()[dc.ordinal() + 1 % Horse.Color.values().length];
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
                .lore("§fСейчас : " + dc.name())
                .build(), e -> {
                if (e.isLeftClick()) {
                    final Cat.Type dc2 = nextCat(dc);//Cat.Type.values()[dc.ordinal() + 1 % Cat.Type.values().length];
                    ((Cat) en).setCatType(dc2);
                    reopen(p, content);
                }
            }));
        }

        if (en.getType() == EntityType.PARROT) {
            Parrot.Variant dc = ((Parrot) en).getVariant();
            content.add(ClickableItem.of(new ItemBuilder(Material.PUFFERFISH)
                .name("§fТип")
                .lore("§fСейчас : " + dc.name())
                .build(), e -> {
                if (e.isLeftClick()) {
                    final Parrot.Variant dc2 = ClassUtil.rotateEnum(dc);//Parrot.Variant.values()[dc.ordinal() + 1 % Parrot.Variant.values().length];
                    ((Parrot) en).setVariant(dc2);
                    reopen(p, content);
                }
            }));
        }

        if (en.getType() == EntityType.SNOW_GOLEM) {
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

    private Cat.Type nextCat(Cat.Type dc) {
        if (dc == Cat.Type.TABBY) return Cat.Type.BLACK;
        else if (dc == Cat.Type.BLACK) return Cat.Type.RED;
        else if (dc == Cat.Type.RED) return Cat.Type.SIAMESE;
        else if (dc == Cat.Type.SIAMESE) return Cat.Type.BRITISH_SHORTHAIR;
        else if (dc == Cat.Type.BRITISH_SHORTHAIR) return Cat.Type.CALICO;
        else if (dc == Cat.Type.CALICO) return Cat.Type.PERSIAN;
        else if (dc == Cat.Type.PERSIAN) return Cat.Type.RAGDOLL;
        else if (dc == Cat.Type.RAGDOLL) return Cat.Type.WHITE;
        else if (dc == Cat.Type.WHITE) return Cat.Type.JELLIE;
        //else if (dc == Cat.Type.JELLIE) return Cat.Type.ALL_BLACK;
        return Cat.Type.ALL_BLACK;
    }


}