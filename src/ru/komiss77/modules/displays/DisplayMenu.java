package ru.komiss77.modules.displays;

import org.bukkit.Location;
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
        final Billboard bb = switch (dis.getBillboard()) {
            default -> {
                bdesc = "§7следя за §bpitch §7и §eyaw";
                yield Billboard.FIXED;
            }
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
        };

        its.set(1, ClickableItem.from(new ItemBuilder(Material.ENDER_EYE)
          .name("§9Способ Показа")
          .addLore("§7сейчас: " + bdesc)
          .addLore("§8ЛКМ - менять")
          .build(), e -> {
            if (e.getEvent() instanceof InventoryClickEvent) {
                dis.setBillboard(bb);
                reopen(p, its);
            }
        }));

        its.set(3, ClickableItem.from(new ItemBuilder(Material.ENDER_PEARL)
          .name("§5Телепорт к ноге")
          .addLore("§7на локацию "+ new WXYZ(dis.getLocation(), false).toString())
          .addLore("§8ЛКМ - тп")
          .build(), e -> {
            if (e.getEvent() instanceof InventoryClickEvent) {
                final Location dl = dis.getLocation();
                dis.teleport(new WXYZ(p.getLocation()).getCenterLoc());
                dis.setRotation(dl.getYaw(), dl.getPitch());
                p.closeInventory();
            }
        }));

        final Transformation tr = dis.getTransformation();
        final Vector3f scl = tr.getScale();
        its.set(5, ClickableItem.from(new ItemBuilder(Material.DRIED_KELP_BLOCK)
          .name("§2Изменить Размер")
          .addLore("§7сейчас: x=" + scl.x + ", y=" + scl.y + ", z=" + scl.z)
          .addLore("§8ЛКМ - менять")
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
                }, ApiOstrov.toSigFigs(scl.x, (byte) 3) + ";"
                        + ApiOstrov.toSigFigs(scl.y, (byte) 3) + ";"
                        + ApiOstrov.toSigFigs(scl.z, (byte) 3));
            }
        }));

        its.set(7, ClickableItem.from(new ItemBuilder(Material.TNT)
          .name("§4Уничтожить Дисплей")
          .addLore("§7ЛКМ - §cуничтожить")
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






        if (dis instanceof final TextDisplay tds) {
            eti = ClickableItem.empty(new ItemBuilder(Material.LIGHT_BLUE_STAINED_GLASS_PANE).name("§0.").build());
            its.set(9, eti);

            final Component currentText = tds.text();

            its.set(11, ClickableItem.from(new ItemBuilder(Material.LADDER)
              .name("§яЦентровка Текста")
              .addLore("§7сейчас: §я" + tds.getAlignment().name())
              .addLore("§8ЛКМ - менять")
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

            its.set(13, ClickableItem.from(new ItemBuilder(Material.GLOBE_BANNER_PATTERN)
              .name("§6Текст")
              .addLore("§8ЛКМ - менять")
              .build(), e -> {
              if (e.getEvent() instanceof InventoryClickEvent) {
                PlayerInput.get( tds.getLineWidth()<40 ? InputType.ANVILL : InputType.CHAT, p, text -> {
                  tds.text(TCUtils.format(text));
                  reopen(p, its);
                }, TCUtils.toString(currentText) );
              }
            }));

          //its.set(13, new InputButton(InputType.ANVILL, new ItemBuilder(Material.GLOBE_BANNER_PATTERN)
         //   .name("§6Текст")
         //   .addLore("§8ЛКМ - менять")
         //   .build(), TCUtils.toString(currentText).replace('§', '&'), msg -> {
         //   tds.text(TCUtils.format(msg.replace('&', '§')));
          //  reopen(p, its);
         // }));

          its.set(15, new InputButton(InputType.ANVILL, new ItemBuilder(Material.FEATHER)
            .name("§aДлинна Строки")
            .addLore("§7Клик - изменить §aдлинну")
            .addLore("§7сейчас длинна: §a" + tds.getLineWidth())
            .build(),
            String.valueOf(tds.getLineWidth()), msg -> {
            tds.setLineWidth(Math.max(ApiOstrov.getInteger(msg), 10));
            reopen(p, its);
          }));

          its.set(17, ClickableItem.from(new ItemBuilder(Material.BOOKSHELF)
            .name("§аСейчас §оДисплей Текста")
            .addLore("§7Клик - поменять тип на:")
            .addLore("§7дисплей §оБлока")
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

          its.set(19, ClickableItem.from(new ItemBuilder(Material.PLAYER_HEAD)
            .name("§сПовернуть Дисплей")
            .addLore("§сповернуть §7куда смотришь")
            .build(), e -> {
            if (e.getEvent() instanceof InventoryClickEvent) {
              final Location loc = p.getLocation();
              dis.setRotation(loc.getYaw(), loc.getPitch());
              p.closeInventory();
            }
          }));

         its.set(21, ClickableItem.from(new ItemBuilder(tds.isSeeThrough() ? Material.GLASS : Material.TINTED_GLASS)
           .name("§фПрозрачность")
           .addLore("§7сейчас: §ф" + (tds.isSeeThrough() ? "прозрачный" : "цельный"))
           .addLore("§8ЛКМ - менять")
           .build(), e -> {
                if (e.getEvent() instanceof InventoryClickEvent) {
                    tds.setSeeThrough(!tds.isSeeThrough());
                    reopen(p, its);
                }
            }));

            its.set(23, ClickableItem.from(new ItemBuilder(Material.INK_SAC)
              .name("§dТени")
              .addLore("§7сейчас " + (tds.isShadowed() ? "§dесть" : "§6нету"))
              .addLore("§8ЛКМ - менять")
              .build(), e -> {
                if (e.getEvent() instanceof InventoryClickEvent) {
                    tds.setShadowed(!tds.isShadowed());
                    reopen(p, its);
                }
            }));






        } else if (dis instanceof BlockDisplay) {
            eti = ClickableItem.empty(new ItemBuilder(Material.LIME_STAINED_GLASS_PANE).name("§0.").build());

            its.set(9, ClickableItem.from(new ItemBuilder(Material.GLOBE_BANNER_PATTERN)
              .name("§аСейчас §оДисплей Блока")
              .addLore("§7Клик - поменять тип на:")
              .addLore("§7дисплей §отекста")
              .build(), e -> {
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
            its.set(13, ClickableItem.from(new ItemBuilder(bd.getMaterial())
              .name("§6Замена Блока")
              .addLore("§7ЛКМ §6блоком §7- поменять тип")
              .addLore("§7ПКМ §7- сделать камнем")
              //.addLore("§7тот, на котором §6стоишь")
              .build(), e -> {
                if (e.getEvent() instanceof InventoryClickEvent ice) {
                  if (ice.isLeftClick() && ice.getCursor().getType()!= Material.AIR ) {
                    Material mat = ice.getCursor().getType();
                    final BlockData nbd = mat.createBlockData();//p.getLocation().getBlock().getRelative(BlockFace.DOWN).getBlockData();
                    ((BlockDisplay) dis).setBlock(nbd);
                  } else if (ice.isRightClick()) {
                    ((BlockDisplay) dis).setBlock(std);
                  }
                reopen(p, its);
              }
            }));

            its.set(17, ClickableItem.from(new ItemBuilder(Material.WRITABLE_BOOK)
              .name("§аСейчас §яДисплей блока")
              .addLore("§7Клик - поменять тип на:")
              .addLore("§7дисплей §бпредмета")
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








        } else if (dis instanceof ItemDisplay) {
            eti = ClickableItem.empty(new ItemBuilder(Material.ORANGE_STAINED_GLASS_PANE).name("§0.").build());
            its.set(17, eti);

            its.set(9, ClickableItem.from(new ItemBuilder(Material.STONE)
              .name("§аСейчас §чДисплей предмета")
              .addLore("§7Клик - поменять тип на:")
              .addLore("§7дисплей §отекста")
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

            final ItemStack it = ((ItemDisplay) dis).getItemStack();
            its.set(11, ClickableItem.from(new ItemBuilder(it == null ? dst : it)
              .name("§6Замена Предмета")
              .addLore("§7Клик §6предметом §7- поменять")
              .addLore("§7на новый §6предмет")
              .build(), e -> {
                if (e.getEvent() instanceof final InventoryClickEvent ev) {
                    ((ItemDisplay) dis).setItemStack(ev.getCursor());
                    reopen(p, its);
                }
            }));

            final ItemDisplayTransform idt;
            final String tdesc = switch (((ItemDisplay) dis).getItemDisplayTransform()) {
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

            its.set(15, ClickableItem.from(new ItemBuilder(Material.COMPASS)
              .name("§3Показ Предмета")
              .addLore("§7Клик - поменять способ §9показа")
              .addLore("§7сейчас: " + tdesc)
              .build(), e -> {
                if (e.getEvent() instanceof InventoryClickEvent) {
                    ((ItemDisplay) dis).setItemDisplayTransform(idt);
                    reopen(p, its);
                }
            }));
        } else {
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
