package ru.komiss77.modules.displays;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Display;
import org.bukkit.entity.Display.Billboard;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.ItemDisplay.ItemDisplayTransform;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.entity.TextDisplay.TextAlignment;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Transformation;
import org.joml.Math;
import org.joml.Vector3f;

import net.kyori.adventure.text.Component;
import ru.komiss77.ApiOstrov;
import ru.komiss77.modules.world.WXYZ;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.PlayerInput;
import ru.komiss77.utils.TCUtils;
import ru.komiss77.utils.inventory.ClickableItem;
import ru.komiss77.utils.inventory.ConfirmationGUI;
import ru.komiss77.utils.inventory.InputButton;
import ru.komiss77.utils.inventory.InputButton.InputType;
import ru.komiss77.utils.inventory.InventoryContent;
import ru.komiss77.utils.inventory.InventoryProvider;



public class DisplayMenu implements InventoryProvider {

    private static final BlockData std = Material.STONE.createBlockData();
    private static final ItemStack dst = new ItemStack(Material.CLOCK);

    private Display dis;

    public DisplayMenu(final Display dis) {
        this.dis = dis;
    }

    @Override
    public void init(final Player p, final InventoryContent its) {
        //dis.setGlowColorOverride(Color.WHITE);
    	final ClickableItem eti;

        final String bdesc;
        final Billboard bb;
        switch (dis.getBillboard()) {
            case CENTER:
            default:
                bdesc = "§7следя за §bpitch §7и §eyaw";
                bb = Billboard.FIXED;
                break;
            case FIXED:
                bdesc = "§7не следя за поворотами";
                bb = Billboard.HORIZONTAL;
                break;
            case HORIZONTAL:
                bdesc = "§7следя за §bpitch";
                bb = Billboard.VERTICAL;
                break;
            case VERTICAL:
                bdesc = "§7следя за §eyaw";
                bb = Billboard.CENTER;
                break;
        }
        its.set(1, ClickableItem.from(new ItemBuilder(Material.ENDER_EYE).name("§9Способ Показа")
                .addLore("§7Клик - поменять способ §9показа").addLore("§7сейчас: " + bdesc).build(), e -> {
            if (e.getEvent() instanceof InventoryClickEvent) {
                dis.setBillboard(bb);
                reopen(p, its);
            }
        }));

        its.set(3, ClickableItem.from(new ItemBuilder(Material.ENDER_PEARL).name("§5Телепорт к Себе")
                .addLore("§7Клик - §5тп §7дисплей где стоишь").addLore("§7локация: "
                + new WXYZ(dis.getLocation(), false).toString()).build(), e -> {
            if (e.getEvent() instanceof InventoryClickEvent) {
                dis.teleport(new WXYZ(p.getLocation()).getCenterLoc());
                p.closeInventory();
            }
        }));
        
        final Transformation tr = dis.getTransformation();
        final Vector3f scl = tr.getScale();
        its.set(5, ClickableItem.from(new ItemBuilder(Material.DRIED_KELP_BLOCK).name("§2Изменить Размер")
                .addLore("§7Клик - смена §2размера §7дисплея")
                .addLore("§7сейчас: x=" + scl.x + ", y=" + scl.y + ", z=" + scl.z).build(), e -> {
            if (e.getEvent() instanceof InventoryClickEvent) {
                PlayerInput.get(InputType.ANVILL, p, text -> {
                	final String[] pts = text.split(";");
                	if (pts.length == 3) {
                		try {
							dis.setTransformation(new Transformation(tr.getTranslation(), tr.getLeftRotation(), 
								new Vector3f(Float.parseFloat(pts[0]), Float.parseFloat(pts[1]), Float.parseFloat(pts[2])), tr.getRightRotation()));
						} catch (NumberFormatException ex) {
							p.sendMessage("§cНеправельный формат!");
						}
                	} else p.sendMessage("§cНеправельный формат!");
                    reopen(p, its);
                }, ApiOstrov.toSigFigs(scl.x, (byte) 3) + ";" + 
                	ApiOstrov.toSigFigs(scl.y, (byte) 3) + ";" + 
                	ApiOstrov.toSigFigs(scl.z, (byte) 3));
            }
        }));

        its.set(7, ClickableItem.from(new ItemBuilder(Material.TNT).name("§4Уничтожить Дисплей")
                .addLore("§7Клик - уничтожить").build(), e -> {
            if (e.getEvent() instanceof InventoryClickEvent) {
                ConfirmationGUI.open(p, "§4Удалить Дисплей?", confirm -> {
                    if (confirm) {
                        replace(null);
                        p.closeInventory();
                    }
                });
            }
        }));

        if (dis instanceof TextDisplay) {
        	eti = ClickableItem.empty(new ItemBuilder(Material.LIGHT_BLUE_STAINED_GLASS_PANE).name("§0.").build());
            its.set(9, eti);

            final TextDisplay tds = (TextDisplay) dis;
            final Component cmp = tds.text();
            
            its.set(13, new InputButton(InputType.ANVILL, new ItemBuilder(Material.GLOBE_BANNER_PATTERN).name("§6Замена Текста")
            		.addLore("§7Клик - записать новый §6текст").build(), cmp == null ? "" : TCUtils.toString(cmp).replace('§', '&'), msg -> {
                tds.text(TCUtils.format(msg.replace('&', '§')));
                reopen(p, its);
            }));

            its.set(11, ClickableItem.from(new ItemBuilder(Material.LADDER).name("§яЦентровка Текста")
                    .addLore("§7Клик - изменить §яцентровку").addLore("§7сейчас: §я" + tds.getAlignment().name()).build(), e -> {
                if (e.getEvent() instanceof InventoryClickEvent) {
                	switch (tds.getAlignment()) {
					case CENTER: tds.setAlignment(TextAlignment.LEFT); break;
					case LEFT: tds.setAlignment(TextAlignment.RIGHT); break;
					case RIGHT: tds.setAlignment(TextAlignment.CENTER); break;
					}
                    reopen(p, its);
                }
            }));

            its.set(21, ClickableItem.from(new ItemBuilder(Material.TINTED_GLASS).name("§фПрозрачность")
                    .addLore("§7Клик - изменить §фпрозрачность").addLore("§7сейчас: §ф" + (tds.isSeeThrough() ? "прозрачный" : "цельный")).build(), e -> {
                if (e.getEvent() instanceof InventoryClickEvent) {
                    tds.setSeeThrough(!tds.isSeeThrough());
                    reopen(p, its);
                }
            }));

            its.set(23, ClickableItem.from(new ItemBuilder(Material.INK_SAC).name("§dТени")
                    .addLore("§7Клик - изменить §dоттенок").addLore("§7сейчас оттенок: §d" + (tds.isShadowed() ? "есть" : "нету")).build(), e -> {
                if (e.getEvent() instanceof InventoryClickEvent) {
                    tds.setShadowed(!tds.isShadowed());
                    reopen(p, its);
                }
            }));
            
            its.set(15, new InputButton(InputType.ANVILL, new ItemBuilder(Material.FEATHER).name("§aДлинна Строки")
	                .addLore("§7Клик - изменить §aдлинну").addLore("§7сейчас длинна: §a" + tds.getLineWidth()).build(), 
	                String.valueOf(tds.getLineWidth()), msg -> {
                tds.setLineWidth(Math.max(ApiOstrov.getInteger(msg), 10));
                reopen(p, its);
            }));

            its.set(17, ClickableItem.from(new ItemBuilder(Material.BOOKSHELF).name("§чДисплей Блока")
                    .addLore("§7Клик - поменять тип на:").addLore("§7дисплей §отекста").build(), e -> {
                if (e.getEvent() instanceof InventoryClickEvent) {
                    final BlockDisplay nd = dis.getWorld().spawn(dis.getLocation(), BlockDisplay.class);
                    nd.setPersistent(true);
                    nd.setBillboard(Billboard.CENTER);
                    
                    nd.setBlock(std);
                    
                    replace(nd);
                    reopen(p, its);
                }
            }));

        } else if (dis instanceof BlockDisplay) {
        	eti = ClickableItem.empty(new ItemBuilder(Material.LIME_STAINED_GLASS_PANE).name("§0.").build());

            its.set(9, ClickableItem.from(new ItemBuilder(Material.GLOBE_BANNER_PATTERN).name("§оДисплей Текста")
                    .addLore("§7Клик - поменять тип на:").addLore("§7дисплей §отекста").build(), e -> {
                if (e.getEvent() instanceof InventoryClickEvent) {
                    final TextDisplay nd = dis.getWorld().spawn(dis.getLocation(), TextDisplay.class);
                    nd.setPersistent(true);
                    nd.setBillboard(Billboard.CENTER);
                    
                    nd.setSeeThrough(true);
                    nd.setShadowed(true);
                    nd.setLineWidth(200);
                    nd.setTextOpacity((byte) -1);
                    nd.text(TCUtils.format("§оКекст"));

                    replace(nd);
                    reopen(p, its);
                }
            }));

            final BlockData bd = ((BlockDisplay) dis).getBlock();
            its.set(13, ClickableItem.from(new ItemBuilder(bd.getMaterial()).name("§6Замена Блока")
                    .addLore("§7Клик - поменять §6блок §7на").addLore("§7тот, на котором §6стоишь").build(), e -> {
                if (e.getEvent() instanceof InventoryClickEvent) {
                    final BlockData nbd = p.getLocation().getBlock()
                            .getRelative(BlockFace.DOWN).getBlockData();
                    ((BlockDisplay) dis).setBlock(nbd.getMaterial().isAir() ? std : nbd);
                    reopen(p, its);
                }
            }));

            its.set(17, ClickableItem.from(new ItemBuilder(Material.WRITABLE_BOOK).name("§яДисплей Предмета")
                    .addLore("§7Клик - поменять тип на:").addLore("§7дисплей §бпредмета").build(), e -> {
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

        } else if (dis instanceof ItemDisplay) {
        	eti = ClickableItem.empty(new ItemBuilder(Material.ORANGE_STAINED_GLASS_PANE).name("§0.").build());
            its.set(17, eti);

            its.set(9, ClickableItem.from(new ItemBuilder(Material.BOOKSHELF).name("§чДисплей Блока")
                    .addLore("§7Клик - поменять тип на:").addLore("§7дисплей §отекста").build(), e -> {
                if (e.getEvent() instanceof InventoryClickEvent) {
                    final BlockDisplay nd = dis.getWorld().spawn(dis.getLocation(), BlockDisplay.class);
                    nd.setPersistent(true);
                    nd.setBillboard(Billboard.CENTER);
                    
                    nd.setBlock(std);
                    
                    replace(nd);
                    reopen(p, its);
                }
            }));

            final ItemStack it = ((ItemDisplay) dis).getItemStack();
            its.set(11, ClickableItem.from(new ItemBuilder(it == null ? dst : it).name("§6Замена Предмета")
                    .addLore("§7Клик §6предметом §7- поменять").addLore("§7на новый §6предмет").build(), e -> {
                if (e.getEvent() instanceof InventoryClickEvent) {
                    final InventoryClickEvent ev = (InventoryClickEvent) e.getEvent();
                    if (ev.getCursor() != null) {
                        ((ItemDisplay) dis).setItemStack(ev.getCursor());
                    }
                    reopen(p, its);
                }
            }));

            final String tdesc;
            final ItemDisplayTransform idt;
            switch (((ItemDisplay) dis).getItemDisplayTransform()) {
                case FIRSTPERSON_LEFTHAND, FIRSTPERSON_RIGHTHAND:
                    idt = ItemDisplayTransform.THIRDPERSON_RIGHTHAND;
                    tdesc = "§7вид с §3переди";
                    break;
                case THIRDPERSON_LEFTHAND, THIRDPERSON_RIGHTHAND:
                    idt = ItemDisplayTransform.FIXED;
                    tdesc = "§7вид со §3стороны";
                    break;
                case FIXED:
                    idt = ItemDisplayTransform.GROUND;
                    tdesc = "§7позиция §3фиксирована";
                    break;
                case GROUND:
                    idt = ItemDisplayTransform.GUI;
                    tdesc = "§7в §3поставленом §7виде";
                    break;
                case GUI:
                    idt = ItemDisplayTransform.HEAD;
                    tdesc = "§7как в §3инвентаре";
                    break;
                case HEAD:
                    idt = ItemDisplayTransform.NONE;
                    tdesc = "§7как на §3голове";
                    break;
                default:
                case NONE:
                    idt = ItemDisplayTransform.FIRSTPERSON_RIGHTHAND;
                    tdesc = "§3обычный";
                    break;
            }
            its.set(15, ClickableItem.from(new ItemBuilder(Material.COMPASS).name("§3Показ Предмета")
                    .addLore("§7Клик - поменять способ §9показа").addLore("§7сейчас: " + tdesc).build(), e -> {
                if (e.getEvent() instanceof InventoryClickEvent) {
                    ((ItemDisplay) dis).setItemDisplayTransform(idt);
                    reopen(p, its);
                }
            }));
        } else return;
        
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
