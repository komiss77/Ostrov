package ru.komiss77.version;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.komiss77.Cfg;
import ru.komiss77.Ostrov;
import ru.komiss77.modules.bots.BotManager;
import ru.komiss77.modules.bots.Botter;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.utils.PlayerInput;
import ru.komiss77.utils.inventory.InputButton;


public class PlayerPacketHandler extends ChannelDuplexHandler {

    private final Oplayer op;
//    public static Field interactIdField; //ServerboundInteractPacket - подмена ид для бота
    public static Field moveIdField; //ClientboundMoveEntityPacket - получение ид бота
//    public static Field containerClickItem; //ServerboundContainerClickPacket - подмена входящего хакнутого предмета
//    public static Field creativeSlotItem; //ServerboundSetCreativeModeSlotPacket - подмена входящего хакнутого предмета
//    public static Field containerSetSlotItem; //ClientboundContainerSetSlotPacket - подмена исходящего хакнутого предмета
    public static AtomicBoolean nbtCheck = new AtomicBoolean(false);

    static {
        try {
            //утилитка поиска номера поля - не удалять!!
            //int i=0; for (Field f : ClientboundContainerSetSlotPacket.class.getDeclaredFields()) {Ostrov.log_warn(i+"="+f.getName()); i++;}
//            interactIdField = ServerboundInteractPacket.class.getDeclaredFields()[1]; //по entityId не прокатит - на запущеном имена обфусцированны!
//            interactIdField.setAccessible(true);
            moveIdField = ClientboundMoveEntityPacket.class.getDeclaredFields()[0];
            moveIdField.setAccessible(true);
//            containerClickItem = ServerboundContainerClickPacket.class.getDeclaredFields()[8];
//            containerClickItem.setAccessible(true);
//            creativeSlotItem = ServerboundSetCreativeModeSlotPacket.class.getDeclaredFields()[2];
//            creativeSlotItem.setAccessible(true);
//            containerSetSlotItem = ClientboundContainerSetSlotPacket.class.getDeclaredFields()[5];
//            containerSetSlotItem.setAccessible(true);
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

        switch (packet) {
            case final ServerboundInteractPacket ip:
                if (Cfg.bots) { //if (useEntityPacket.getActionType() == PacketPlayInUseEntity.b.b) {}
                    final int id = ip.getEntityId();
                    for (final Botter bot : BotManager.botById.values()) {
                        if (bot.hashCode() == id) {
                            final FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
                            ServerboundInteractPacket.STREAM_CODEC.encode(buf, ip);

                            super.channelRead(chc, reId(buf, bot.rid()));
                            return;
                        }
                    }
                }  // Paper start - PlayerUseUnknownEntityEvent
                break;
            case final ServerboundSignUpdatePacket sup://пакет ввода с таблички - не отдаём в сервер!
                final Player p = op.getPlayer();
                if (p != null && PlayerInput.inputData.containsKey(p)) {  // в паспорте final String[] split = msg.split(" ");
                    final String result = sup.getLines()[0] + " " + sup.getLines()[1] + " " + sup.getLines()[2] + " " + sup.getLines()[3];
                    Ostrov.sync(() -> PlayerInput.onInput(p, InputButton.InputType.SIGN, result), 0);
                    return;
                }
                break;
            case final ServerboundPlayerActionPacket pa://блокировка ломания фэйкогого блока
                if (pa.getAction() == ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK) {
                    if (op.hasFakeBlock && op.fakeBlock.containsKey(pa.getPos().asLong())) {
                        return;
                    }
                }
                break;
            case final ServerboundUseItemOnPacket uip://блокировка клика на фэйковый блок
                if (op.hasFakeBlock && op.fakeBlock.containsKey(uip.getHitResult().getBlockPos().asLong())) {
                    return;
                }
                break;
            default:
                break;
        }


        if (nbtCheck.get()) { //не пропускаем в сервер хакнутые предметы от клиента
            //https://github.com/ds58/Panilla
            //PacketPlayInWindowClick = ServerboundContainerClickPacket
            //PacketPlayInSetCreativeSlot = ServerboundSetCreativeModeSlotPacket
            net.minecraft.world.item.ItemStack is;

            if (packet instanceof ServerboundContainerClickPacket p) {
                is = p.getCarriedItem();
                if (!is.getComponents().isEmpty()) {//if (is != null && is.hasTag()) {
                    if (hacked(is, p.getSlotNum())) {
//                        containerClickItem.set(p, ItemStack.EMPTY);
                        super.channelRead(chc, new ServerboundContainerClickPacket(p.getContainerId(), p.getStateId(),
                            p.getSlotNum(), p.getButtonNum(), p.getClickType(), ItemStack.EMPTY, p.getChangedSlots()));
                        return;
                    }
                }
            } else if (packet instanceof ServerboundSetCreativeModeSlotPacket p) {
                is = p.itemStack();
                if (!is.getComponents().isEmpty()) {//if (is != null && is.hasTag()) {
                    if (hacked(is, p.slotNum())) {
//                        creativeSlotItem.set(p, ItemStack.EMPTY);
                        super.channelRead(chc, new ServerboundSetCreativeModeSlotPacket(p.slotNum(), ItemStack.EMPTY));
                        return;
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
        if (packet instanceof final ClientboundBlockUpdatePacket bup && op.hasFakeBlock) {
            final BlockData bd = op.fakeBlock.get(bup.getPos().asLong());
            if (bd != null) {
                super.write(chc, new ClientboundBlockUpdatePacket(bup.getPos(), Craft.toNMS(bd)), channelPromise);
                return;
            }
        }

        if (packet instanceof final ClientboundSetPassengersPacket p) {
            final Botter bt = BotManager.getBot(p.getVehicle());
            if (bt != null) {
                final FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
                buf.writeVarInt(bt.hashCode());
                buf.writeVarIntArray(p.getPassengers());
                final ClientboundSetPassengersPacket mp =
                    ClientboundSetPassengersPacket.STREAM_CODEC.decode(buf);
                super.write(chc, mp, channelPromise);
                return;
            }
        }

        if (Cfg.bots) {
            int id = 0;
            if (packet instanceof final ClientboundAddEntityPacket p) {
                id = p.getId();
            } else if (packet instanceof final ClientboundSetEntityDataPacket p) {
                id = p.id();
            } else if (packet instanceof final ClientboundTeleportEntityPacket p) {
                id = p.id();
            } else if (packet instanceof final ClientboundUpdateAttributesPacket p) {
                id = p.getEntityId();
            } else if (packet instanceof ClientboundMoveEntityPacket) {
                id = (int) moveIdField.get(packet);
            }

            if (id != 0 && BotManager.botById.containsKey(id)) {
                return; //не пропускать пакеты дальше
            }

            if (packet instanceof ClientboundBundlePacket clientboundBundlePacket) {
                final Iterator<?> pit = clientboundBundlePacket.subPackets().iterator();
                while (pit.hasNext()) {
                    final Object pc = pit.next();
                    if (pc instanceof final ClientboundAddEntityPacket p) {
                        id = p.getId();
                    } else if (pc instanceof final ClientboundSetEntityDataPacket p) {
                        id = p.id();
                    } else if (pc instanceof final ClientboundTeleportEntityPacket p) {
                        id = p.id();
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
            net.minecraft.world.item.ItemStack is;

            if (packet instanceof ClientboundContainerSetSlotPacket p) {
                if (p.getContainerId() == 0) {// check if window is not player inventory and we are ignoring non-player inventories
                    is = p.getItem();
                    if (!is.getComponents().isEmpty()) {//if (is != null && is.hasTag()) {
                        if (hacked(is, p.getSlot())) {
//                            containerSetSlotItem.set(p, ItemStack.EMPTY);
                            super.write(chc, new ClientboundContainerSetSlotPacket(p.getContainerId(),
                                p.getStateId(), p.getSlot(), ItemStack.EMPTY), channelPromise);
                            return;
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

    private static ServerboundInteractPacket reId(final FriendlyByteBuf buf, final int id) {
        final FriendlyByteBuf reBuf = new FriendlyByteBuf(Unpooled.buffer());
        buf.readVarInt();
        reBuf.writeVarInt(id);
        final ActionType atp = buf.readEnum(ActionType.class);
        reBuf.writeEnum(atp);
        final Action action = atp.reader.apply(buf);
        action.write(reBuf);
        reBuf.writeBoolean(buf.readBoolean());
        return ServerboundInteractPacket.STREAM_CODEC.decode(reBuf);
    }

    interface Action {
        ActionType getType();

        void dispatch(ServerboundInteractPacket.Handler handler);

        void write(FriendlyByteBuf buf);
    }

    enum ActionType {
        INTERACT(InteractionAction::new),
        ATTACK(buf -> ATTACK_ACTION),
        INTERACT_AT(InteractionAtLocationAction::new);

        final Function<FriendlyByteBuf, Action> reader;

        ActionType(final Function<FriendlyByteBuf, Action> handlerGetter) {
            this.reader = handlerGetter;
        }
    }

    static final Action ATTACK_ACTION = new Action() {
        @Override
        public ActionType getType() {
            return ActionType.ATTACK;
        }

        @Override
        public void dispatch(ServerboundInteractPacket.Handler handler) {
            handler.onAttack();
        }

        @Override
        public void write(FriendlyByteBuf buf) {
        }
    };

    static class InteractionAction implements Action {
        private final InteractionHand hand;

        InteractionAction(InteractionHand hand) {
            this.hand = hand;
        }

        private InteractionAction(FriendlyByteBuf buf) {
            this.hand = buf.readEnum(InteractionHand.class);
        }

        @Override
        public ActionType getType() {
            return ActionType.INTERACT;
        }

        @Override
        public void dispatch(ServerboundInteractPacket.Handler handler) {
            handler.onInteraction(this.hand);
        }

        @Override
        public void write(FriendlyByteBuf buf) {
            buf.writeEnum(this.hand);
        }
    }

    static class InteractionAtLocationAction implements Action {
        private final InteractionHand hand;
        private final Vec3 location;

        InteractionAtLocationAction(InteractionHand hand, Vec3 pos) {
            this.hand = hand;
            this.location = pos;
        }

        private InteractionAtLocationAction(FriendlyByteBuf buf) {
            this.location = new Vec3(buf.readFloat(), buf.readFloat(), buf.readFloat());
            this.hand = buf.readEnum(InteractionHand.class);
        }

        @Override
        public ActionType getType() {
            return ActionType.INTERACT_AT;
        }

        @Override
        public void dispatch(ServerboundInteractPacket.Handler handler) {
            handler.onInteraction(this.hand, this.location);
        }

        @Override
        public void write(FriendlyByteBuf buf) {
            buf.writeFloat((float)this.location.x);
            buf.writeFloat((float)this.location.y);
            buf.writeFloat((float)this.location.z);
            buf.writeEnum(this.hand);
        }
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

