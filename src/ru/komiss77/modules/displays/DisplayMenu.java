package ru.komiss77.modules.displays;

import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.block.BlockType;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.*;
import org.bukkit.entity.Display.Billboard;
import org.bukkit.entity.ItemDisplay.ItemDisplayTransform;
import org.bukkit.entity.TextDisplay.TextAlignment;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.util.Transformation;
import org.joml.Math;
import org.joml.Vector3f;
import ru.komiss77.modules.items.ItemBuilder;
import ru.komiss77.modules.world.BVec;
import ru.komiss77.utils.*;
import ru.komiss77.utils.inventory.*;
import ru.komiss77.utils.inventory.InputButton.InputType;

public class DisplayMenu implements InventoryProvider {

    private static final BlockData std = BlockType.STONE.createBlockData();
    private static final ItemStack dst = ItemType.CLOCK.createItemStack();

    private Display dis;

    public DisplayMenu(final Display dis) {
        this.dis = dis;
    }

    @Override
    public void init(final Player p, final InventoryContent its) {
        //dis.setGlowColorOverride(Color.WHITE);
        final ClickableItem eti;

        final String bdesc;
        final Billboard bb = switch (dis.getBillboard()) {
            case FIXED -> {
                bdesc = "§7не следя за поворотами";
                yield Billboard.HORIZONTAL;
            }
            case HORIZONTAL -> {
                bdesc = "§7следя за §bpitch";
                yield Billboard.VERTICAL;
            }
            case VERTICAL -> {
                bdesc = "§7следя за §eyaw";
                yield Billboard.CENTER;
            }
            default -> {
                bdesc = "§7следя за §bpitch §7и §eyaw";
                yield Billboard.FIXED;
            }
        };

        its.set(1, ClickableItem.from(new ItemBuilder(ItemType.ENDER_EYE)
            .name("§9Способ Показа")
            .lore("§7сейчас: " + bdesc)
            .lore("§8ЛКМ - менять")
            .build(), e -> {
            if (e.getEvent() instanceof InventoryClickEvent) {
                dis.setBillboard(bb);
                reopen(p, its);
            }
        }));

        its.set(3, ClickableItem.from(new ItemBuilder(ItemType.ENDER_PEARL)
            .name("§5Телепорт к ноге")
            .lore("§7на локацию " + BVec.of(dis.getLocation()).toString())
            .lore("§8ЛКМ - тп")
            .build(), e -> {
            if (e.getEvent() instanceof InventoryClickEvent) {
                final Location dl = dis.getLocation();
                dis.teleport(BVec.of(p.getLocation()).center(dis.getWorld()));
                dis.setRotation(dl.getYaw(), dl.getPitch());
                p.closeInventory();
            }
        }));

        final Transformation tr = dis.getTransformation();
        final Vector3f scl = tr.getScale();
        its.set(5, ClickableItem.from(new ItemBuilder(ItemType.DRIED_KELP_BLOCK)
            .name("§2Изменить Размер")
            .lore("§7сейчас: x=" + scl.x + ", y=" + scl.y + ", z=" + scl.z)
            .lore("§8ЛКМ - менять")
            .build(), e -> {
            if (e.getEvent() instanceof InventoryClickEvent) {
                PlayerInput.get(InputType.ANVILL, p, text -> {
                    final String[] pts = text.split(";");
                    if (pts.length == 3) {
                        try {
                            dis.setTransformation(new Transformation(tr.getTranslation(), tr.getLeftRotation(),
                                new Vector3f(Float.parseFloat(pts[0]), Float.parseFloat(pts[1]), Float.parseFloat(pts[2])), tr.getRightRotation()));
                        } catch (NumberFormatException ex) {
                            p.sendMessage("§cНеправильный формат!");
                        }
                    } else {
                        p.sendMessage("§cНеправильный формат!");
                    }
                    reopen(p, its);
                }, StringUtil.toSigFigs(scl.x, (byte) 3) + ";"
                    + StringUtil.toSigFigs(scl.y, (byte) 3) + ";"
                    + StringUtil.toSigFigs(scl.z, (byte) 3));
            }
        }));

        its.set(7, ClickableItem.from(new ItemBuilder(ItemType.TNT)
            .name("§4Уничтожить Дисплей")
            .lore("§7ЛКМ - §cуничтожить")
            .build(), e -> {
            if (e.getEvent() instanceof InventoryClickEvent) {
                ConfirmationGUI.open(p, "§4Удалить Дисплей?", confirm -> {
                    if (confirm) {
                        replace(null);
                        p.closeInventory();
                    }
                });
            }
        }));


        switch (dis) {
            case final TextDisplay tds:
                eti = ClickableItem.empty(new ItemBuilder(ItemType.LIGHT_BLUE_STAINED_GLASS_PANE).name("§0.").build());
                its.set(9, eti);

                final Component currentText = tds.text();

                its.set(11, ClickableItem.from(new ItemBuilder(ItemType.LADDER)
                    .name("§яЦентровка Текста")
                    .lore("§7сейчас: §я" + tds.getAlignment().name())
                    .lore("§8ЛКМ - менять")
                    .build(), e -> {
                    if (e.getEvent() instanceof InventoryClickEvent) {
                        switch (tds.getAlignment()) {
                            case CENTER:
                                tds.setAlignment(TextAlignment.LEFT);
                                break;
                            case LEFT:
                                tds.setAlignment(TextAlignment.RIGHT);
                                break;
                            case RIGHT:
                                tds.setAlignment(TextAlignment.CENTER);
                                break;
                        }
                        reopen(p, its);
                    }
                }));

                its.set(13, ClickableItem.from(new ItemBuilder(ItemType.GLOBE_BANNER_PATTERN)
                    .name("§6Текст")
                    .lore(tds.text())
                    .lore("§8ЛКМ - менять")
                    .build(), e -> {
                    if (e.getEvent() instanceof InventoryClickEvent) {
                        PlayerInput.get(tds.getLineWidth() < 40 ? InputType.ANVILL : InputType.CHAT, p, text -> {
                            tds.text(TCUtil.form(text));
                            reopen(p, its);
                        }, TCUtil.deform(currentText));
                    }
                }));

                its.set(15, new InputButton(InputType.ANVILL, new ItemBuilder(ItemType.FEATHER)
                    .name("§aДлинна Строки")
                    .lore("§7Клик - изменить §aдлинну")
                    .lore("§7сейчас длинна: §a" + tds.getLineWidth())
                    .build(),
                    String.valueOf(tds.getLineWidth()), msg -> {
                    tds.setLineWidth(Math.max(NumUtil.intOf(msg, 0), 10));
                    reopen(p, its);
                }));

                its.set(17, ClickableItem.from(new ItemBuilder(ItemType.BOOKSHELF)
                    .name("§аСейчас §оДисплей Текста")
                    .lore("§7Клик - поменять тип на:")
                    .lore("§7дисплей §оБлока")
                    .build(), e -> {
                    if (e.getEvent() instanceof InventoryClickEvent) {
                        final BlockDisplay nd = dis.getWorld().spawn(dis.getLocation(), BlockDisplay.class);
                        nd.setPersistent(true);
                        nd.setBillboard(Billboard.CENTER);
                        nd.setBlock(std);
                        replace(nd);
                        reopen(p, its);
                    }
                }));

                its.set(19, ClickableItem.from(new ItemBuilder(ItemType.PLAYER_HEAD)
                    .name("§сПовернуть Дисплей")
                    .lore("§сповернуть §7куда смотришь")
                    .build(), e -> {
                    if (e.getEvent() instanceof InventoryClickEvent) {
                        final Location loc = p.getLocation();
                        dis.setRotation(loc.getYaw(), loc.getPitch());
                        p.closeInventory();
                    }
                }));

                its.set(21, ClickableItem.from(new ItemBuilder(tds.isSeeThrough() ? ItemType.GLASS : ItemType.TINTED_GLASS)
                    .name("§фПрозрачность")
                    .lore("§7сейчас: §ф" + (tds.isSeeThrough() ? "прозрачный" : "цельный"))
                    .lore("§8ЛКМ - менять")
                    .build(), e -> {
                    if (e.getEvent() instanceof InventoryClickEvent) {
                        tds.setSeeThrough(!tds.isSeeThrough());
                        reopen(p, its);
                    }
                }));

                its.set(23, ClickableItem.from(new ItemBuilder(ItemType.INK_SAC)
                    .name("§dТени")
                    .lore("§7сейчас " + (tds.isShadowed() ? "§dесть" : "§6нету"))
                    .lore("§8ЛКМ - менять")
                    .build(), e -> {
                    if (e.getEvent() instanceof InventoryClickEvent) {
                        tds.setShadowed(!tds.isShadowed());
                        reopen(p, its);
                    }
                }));
                break;
            case BlockDisplay bds:
                eti = ClickableItem.empty(new ItemBuilder(ItemType.LIME_STAINED_GLASS_PANE).name("§0.").build());

                its.set(9, ClickableItem.from(new ItemBuilder(ItemType.GLOBE_BANNER_PATTERN)
                    .name("§аСейчас §оДисплей Блока")
                    .lore("§7Клик - поменять тип на:")
                    .lore("§7дисплей §отекста")
                    .build(), e -> {
                    if (e.getEvent() instanceof InventoryClickEvent) {
                        final TextDisplay nd = dis.getWorld().spawn(dis.getLocation(), TextDisplay.class);
                        nd.setPersistent(true);
                        nd.setBillboard(Billboard.CENTER);

                        nd.setSeeThrough(true);
                        nd.setShadowed(true);
                        nd.setLineWidth(200);
                        nd.setTextOpacity((byte) -1);
                        nd.text(TCUtil.form("§оКекст"));

                        replace(nd);
                        reopen(p, its);
                    }
                }));

                final BlockData bd = bds.getBlock();
                final ItemType tp = bd.getMaterial().asItemType();
                its.set(13, ClickableItem.from(new ItemBuilder(tp == null
                    || tp == ItemType.AIR ? ItemType.STONE : tp)
                    .name("§6Замена Блока")
                    .lore("§7ЛКМ §6блоком §7- поменять тип")
                    .lore("§7ПКМ §7- сделать камнем")
                    .build(), e -> {
                    if (e.getEvent() instanceof InventoryClickEvent ice) {
                        if (ice.isLeftClick() && !ItemUtil.isBlank(ice.getCursor(), false)) {
                            bds.setBlock(ice.getCursor().getType().asBlockType().createBlockData());
                        } else if (ice.isRightClick()) {
                            bds.setBlock(std);
                        }
                        reopen(p, its);
                    }
                }));

                its.set(17, ClickableItem.from(new ItemBuilder(ItemType.WRITABLE_BOOK)
                    .name("§аСейчас §яДисплей блока")
                    .lore("§7Клик - поменять тип на:")
                    .lore("§7дисплей §бпредмета")
                    .build(), e -> {
                    if (e.getEvent() instanceof InventoryClickEvent) {
                        final ItemDisplay nd = dis.getWorld().spawn(dis.getLocation(), ItemDisplay.class);
                        nd.setPersistent(true);
                        nd.setBillboard(Billboard.CENTER);

                        nd.setItemStack(dst);
                        nd.setItemDisplayTransform(ItemDisplayTransform.NONE);

                        replace(nd);
                        reopen(p, its);
                    }
                }));
                break;
            case ItemDisplay ids:
                eti = ClickableItem.empty(new ItemBuilder(ItemType.ORANGE_STAINED_GLASS_PANE).name("§0.").build());
                its.set(17, eti);

                its.set(9, ClickableItem.from(new ItemBuilder(ItemType.STONE)
                    .name("§аСейчас §чДисплей предмета")
                    .lore("§7Клик - поменять тип на:")
                    .lore("§7дисплей §отекста")
                    .build(), e -> {
                    if (e.getEvent() instanceof InventoryClickEvent) {
                        final BlockDisplay nd = dis.getWorld().spawn(dis.getLocation(), BlockDisplay.class);
                        nd.setPersistent(true);
                        nd.setBillboard(Billboard.CENTER);

                        nd.setBlock(std);

                        replace(nd);
                        reopen(p, its);
                    }
                }));

                final ItemStack it = ids.getItemStack();
                its.set(11, ClickableItem.from(new ItemBuilder(it)
                    .name("§6Замена Предмета")
                    .lore("§7Клик §6предметом §7- поменять")
                    .lore("§7на новый §6предмет")
                    .build(), e -> {
                    if (e.getEvent() instanceof final InventoryClickEvent ev) {
                        ids.setItemStack(ev.getCursor());
                        reopen(p, its);
                    }
                }));

                final ItemDisplayTransform idt;
                final String tdesc = switch (ids.getItemDisplayTransform()) {
                    case FIRSTPERSON_LEFTHAND, FIRSTPERSON_RIGHTHAND -> {
                        idt = ItemDisplayTransform.THIRDPERSON_RIGHTHAND;
                        yield "§7вид с §3переди";
                    }
                    case THIRDPERSON_LEFTHAND, THIRDPERSON_RIGHTHAND -> {
                        idt = ItemDisplayTransform.FIXED;
                        yield "§7вид со §3стороны";
                    }
                    case FIXED -> {
                        idt = ItemDisplayTransform.GROUND;
                        yield "§7позиция §3фиксирована";
                    }
                    case GROUND -> {
                        idt = ItemDisplayTransform.GUI;
                        yield "§7в §3поставленом §7виде";
                    }
                    case GUI -> {
                        idt = ItemDisplayTransform.HEAD;
                        yield "§7как в §3инвентаре";
                    }
                    case HEAD -> {
                        idt = ItemDisplayTransform.NONE;
                        yield "§7как на §3голове";
                    }
                    default -> {
                        idt = ItemDisplayTransform.FIRSTPERSON_RIGHTHAND;
                        yield "§3обычный";
                    }
                };

                its.set(15, ClickableItem.from(new ItemBuilder(ItemType.COMPASS)
                    .name("§3Показ Предмета")
                    .lore("§7Клик - поменять способ §9показа")
                    .lore("§7сейчас: " + tdesc)
                    .build(), e -> {
                    if (e.getEvent() instanceof InventoryClickEvent) {
                        ids.setItemDisplayTransform(idt);
                        reopen(p, its);
                    }
                }));
                break;
            case null:
            default:
                return;
        }

        its.set(0, eti);
        its.set(8, eti);
        its.set(18, eti);
        its.set(26, eti);
    }

    private void replace(final Display ds) {
        dis.remove();
        dis = ds;
    }

}
