package ru.komiss77.version;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.world.item.ItemStack;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.block.data.CraftBlockData;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.komiss77.Ostrov;
import ru.komiss77.modules.bots.BotEntity;
import ru.komiss77.modules.bots.BotManager;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.utils.PlayerInput;
import ru.komiss77.utils.inventory.InputButton;


public class PlayerPacketHandler extends ChannelDuplexHandler {

    private final Oplayer op;
    public static Field interactIdField; //ServerboundInteractPacket - подмена ид для бота
    public static Field moveIdField; //ClientboundMoveEntityPacket - получение ид бота
    public static Field containerClickItem; //ServerboundContainerClickPacket - подмена входящего хакнутого предмета
    public static Field creativeSlotItem; //ServerboundSetCreativeModeSlotPacket - подмена входящего хакнутого предмета
    public static Field containerSetSlotItem; //ClientboundContainerSetSlotPacket - подмена исходящего хакнутого предмета
    public static AtomicBoolean nbtCheck = new AtomicBoolean(false);

    static {
        try {
            //утилитка поиска номера поля - не удалять!!
            //int i=0; for (Field f : ClientboundContainerSetSlotPacket.class.getDeclaredFields()) {Ostrov.log_warn(i+"="+f.getName()); i++;}
            interactIdField = ServerboundInteractPacket.class.getDeclaredFields()[0]; //по entityId не прокатит - на запущеном имена обфусцированны!
            interactIdField.setAccessible(true);
            moveIdField = ClientboundMoveEntityPacket.class.getDeclaredFields()[0];
            moveIdField.setAccessible(true);
            containerClickItem = ServerboundContainerClickPacket.class.getDeclaredFields()[6];
            containerClickItem.setAccessible(true);
            creativeSlotItem = ServerboundSetCreativeModeSlotPacket.class.getDeclaredFields()[1];
            creativeSlotItem.setAccessible(true);
            containerSetSlotItem = ClientboundContainerSetSlotPacket.class.getDeclaredFields()[5];
            containerSetSlotItem.setAccessible(true);
        } catch (ArrayIndexOutOfBoundsException ex) {
            Ostrov.log_err("PlayerPacketHandler getIdField : " + ex.getMessage());
            //ex.printStackTrace();
        }
    }


    public PlayerPacketHandler(final Oplayer op) {
        this.op = op;
    }

    //входящие пакеты от клиента до получения ядром
    @Override
    public void channelRead(final @NotNull ChannelHandlerContext chc, final @NotNull Object packet) throws Exception {
        //switch по getSimpleName не прокатит - названия другие - обфусцированы!

        if (packet instanceof final ServerboundInteractPacket ip) { // Paper start - PlayerUseUnknownEntityEvent
            if (BotManager.enable) { //if (useEntityPacket.getActionType() == PacketPlayInUseEntity.b.b) {}
                final int id = ip.getEntityId();
                for (final BotEntity bot : BotManager.botById.values()) {
                    if (bot.hashCode() == id) {
                        interactIdField.set(ip, bot.rid);
                        break;
                    }
                }
            }

        } else if (packet instanceof ServerboundSignUpdatePacket sup) {
            //пакет ввода с таблички - не отдаём в сервер!
            final Player p = op.getPlayer();
            if (p != null && PlayerInput.inputData.containsKey(p)) {  // в паспорте final String[] split = msg.split(" ");
                final String result = sup.getLines()[0] + " " + sup.getLines()[1] + " " + sup.getLines()[2] + " " + sup.getLines()[3];
                Ostrov.sync(() -> PlayerInput.onInput(p, InputButton.InputType.SIGN, result), 0);
                return;
            }

        } else if (packet instanceof ServerboundPlayerActionPacket pa) {
            //блокировка ломания фэйкогого блока
            if (pa.getAction() == ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK) {
                if (op.hasFakeBlock && op.fakeBlock.containsKey(pa.getPos().asLong())) {
                    return;
                }
            }


        } else if (packet instanceof ServerboundUseItemOnPacket uip) {
            //блокировка клика на фэйковый блок
            if (uip.getHitResult() != null) {
                if (op.hasFakeBlock && op.fakeBlock.containsKey(uip.getHitResult().getBlockPos().asLong())) {
                    return;
                }
            }

        }


        if (nbtCheck.get()) { //не пропускаем в сервер хакнутые предметы от клиента
            //https://github.com/ds58/Panilla
            //PacketPlayInWindowClick = ServerboundContainerClickPacket
            //PacketPlayInSetCreativeSlot = ServerboundSetCreativeModeSlotPacket
            net.minecraft.world.item.ItemStack is = null;
            int slot;

            if (packet instanceof ServerboundContainerClickPacket p) {
                is = p.getCarriedItem();
                if (is != null && !is.getComponents().isEmpty()) {//if (is != null && is.hasTag()) {
                    if (hacked(is, p.getSlotNum())) {
                        //cброс предмета = PacketPlayOutSetSlot packet = new PacketPlayOutSetSlot(entityPlayer.bR.j, entityPlayer.bR.k(), slot, new ItemStack(Blocks.a));
                        //ClientboundContainerSetSlotPacket packet = new ClientboundContainerSetSlotPacket();
                        containerClickItem.set(p, ItemStack.EMPTY);
                        //return;
                    }
                }
            } else if (packet instanceof ServerboundSetCreativeModeSlotPacket p) {
                is = p.itemStack();
                if (is != null && !is.getComponents().isEmpty()) {//if (is != null && is.hasTag()) {
                    if (hacked(is, p.slotNum())) {
                        //cброс предмета = PacketPlayOutSetSlot packet = new PacketPlayOutSetSlot(entityPlayer.bR.j, entityPlayer.bR.k(), slot, new ItemStack(Blocks.a));
                        //ClientboundContainerSetSlotPacket packet = new ClientboundContainerSetSlotPacket();
                        creativeSlotItem.set(p, ItemStack.EMPTY);
                        //return;
                    }
                }
            }

        }

        super.channelRead(chc, packet);
    }


    private boolean hacked(net.minecraft.world.item.ItemStack is, int slot) {
        //https://github.com/ds58/Panilla
        //CompoundTag tag = is.getTag();
        //tag.tags

        return false;
    }

//Ostrov.log("UseItem fakeBlock?"+op.fakeBlock.containsKey(uip.getHitResult().getBlockPos().asLong()));
//Ostrov.log("START_DESTROY_BLOCK fakeBlock?"+op.fakeBlock.containsKey(pa.getPos().asLong()));


    //исходящие пакеты от ядра до отправки клиенту
    @Override
    public void write(final ChannelHandlerContext chc, final Object packet, final ChannelPromise channelPromise) throws Exception {

        //при интеракт отправляет обнову блока после эвента. Чтобы не делать отправку с задержкой тик, нужно подменить исход.пакет
        if (packet instanceof ClientboundBlockUpdatePacket bup && op.hasFakeBlock) {
            final BlockData bd = op.fakeBlock.get(bup.getPos().asLong());
            if (bd != null) {
                bup = new ClientboundBlockUpdatePacket(bup.getPos(), ((CraftBlockData) bd).getState());
                super.write(chc, bup, channelPromise);
//Ostrov.log("replace ClientboundBlockUpdatePacket ");
                return;
            }
        }

        //if (packet instanceof ClientboundLevelChunkPacketData lcp && op.hasFakeBlock) {
        //  lcp.  - возможно добавить в будущем, но обработчик будет громоздкий!
        // }

        if (BotManager.enable) {
            int id = 0;
            if (packet instanceof final ClientboundAddEntityPacket p) {
                id = p.getId();
            } else if (packet instanceof final ClientboundSetEntityDataPacket p) {
                id = p.id();
            } else if (packet instanceof final ClientboundTeleportEntityPacket p) {
                id = p.getId();
            } else if (packet instanceof final ClientboundUpdateAttributesPacket p) {
                id = p.getEntityId();
            } else if (packet instanceof ClientboundMoveEntityPacket) {
                id = (int) moveIdField.get(packet);
            }

            if (id != 0 && BotManager.botById.containsKey(id)) {
                return; //не пропускать пакеты дальше
            }

            if (packet instanceof ClientboundBundlePacket clientboundBundlePacket) {
                final Iterator pit = clientboundBundlePacket.subPackets().iterator();
                while (pit.hasNext()) {
                    final Object pc = pit.next();
                    if (pc instanceof final ClientboundAddEntityPacket p) {
                        id = p.getId();
                    } else if (pc instanceof final ClientboundSetEntityDataPacket p) {
                        id = p.id();
                    } else if (pc instanceof final ClientboundTeleportEntityPacket p) {
                        id = p.getId();
                    } else if (pc instanceof final ClientboundUpdateAttributesPacket p) {
                        id = p.getEntityId();
                    } else if (pc instanceof ClientboundMoveEntityPacket) {
                        id = (int) moveIdField.get(pc);
                    }

                    if (id != 0 && BotManager.botById.containsKey(id)) {
                        pit.remove(); //вырезать пакет из кучи
                    }
                }
            }
        }

        if (nbtCheck.get()) { //не отсылаем клиенту хакнутые предметы от сервера
            //PacketPlayOutSetSlot = ClientboundContainerSetSlotPacket
            //PacketPlayOutWindowItems = ClientboundContainerSetContentPacket
            //PacketPlayOutSpawnEntity = ClientboundAddEntityPacket
            net.minecraft.world.item.ItemStack is = null;
            int slot;

            if (packet instanceof ClientboundContainerSetSlotPacket p) {
                if (p.getContainerId() == 0) {// check if window is not player inventory and we are ignoring non-player inventories
                    is = p.getItem();
                    if (is != null && !is.getComponents().isEmpty()) {//if (is != null && is.hasTag()) {
                        if (hacked(is, p.getSlot())) {
                            containerSetSlotItem.set(p, ItemStack.EMPTY);
                            //return;
                        }
                    }
                }

            } else if (packet instanceof ClientboundContainerSetContentPacket p) {

                if (p.getContainerId() == 0) {
                    is = p.getCarriedItem();
                    List<ItemStack> items = p.getItems();
                    for (int i = 0; i < items.size(); i++) {
                        is = items.get(i);
                        if (is != null && !is.getComponents().isEmpty()) {//if (is != null && is.hasTag()) {
                            if (hacked(is, i)) {
                                items.set(0, ItemStack.EMPTY);
                            }
                        }
                    }
                }

            } else if (packet instanceof ClientboundAddEntityPacket p) {
                //stripNbtFromItemEntity(e.getEntityId());
            }


        }

        super.write(chc, packet, channelPromise);
    }
}






/*
ClientboundBlockUpdatePacket отправляется из
ServerPlayerGameMode: handleBlockBreakAction при ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, destroyAndAck, destroyBlock,
ServerGamePacketListenerImpl: handleUseItemOn(ServerboundUseItemOnPacket packet)


ServerGamePacketListenerImpl :
	handleUseItemOn(ServerboundUseItemOnPacket packet)
	handlePlayerAction(ServerboundPlayerActionPacket packet)
	ServerboundPlayerActionPacket.Action packetplayinblockdig_enumplayerdigtype = packet.getAction(); (START_DESTROY_BLOCK, ABORT_DESTROY_BLOCK,STOP_DESTROY_BLOCK)

	public static long asLong(int x, int y, int z) {
        return (((long) x & (long) 67108863) << 38) | (((long) y & (long) 4095)) | (((long) z & (long) 67108863) << 12); // Paper - inline constants and simplify
    }

	public static BlockPos of(long packedPos) {
        return new BlockPos((int) (packedPos >> 38), (int) ((packedPos << 52) >> 52), (int) ((packedPos << 26) >> 38)); // Paper - simplify/inline
    }

 */

