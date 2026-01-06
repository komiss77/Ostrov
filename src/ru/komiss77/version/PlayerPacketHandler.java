package ru.komiss77.version;

import java.util.function.Function;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.komiss77.Cfg;
import ru.komiss77.Ostrov;
import ru.komiss77.events.PlayerDisguiseEvent;
import ru.komiss77.modules.bots.BotManager;
import ru.komiss77.modules.bots.Botter;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.utils.PlayerInput;
import ru.komiss77.utils.inventory.InputButton;


public class PlayerPacketHandler extends ServerToPlayer {


  public PlayerPacketHandler(Oplayer op) {
    super(op);
    }

    //входящие пакеты от клиента до получения ядром
    @Override
    public void channelRead(final @NotNull ChannelHandlerContext chc, final @NotNull Object packet) throws Exception {
        //switch по getSimpleName не прокатит - названия другие - обфусцированы!
//Ostrov.log_warn("channelRead packet="+packet);
//if ( ! (packet instanceof ServerboundClientTickEndPacket t )  && ! (packet instanceof ServerboundKeepAlivePacket k ) ) {
//Ostrov.log_warn("p-> packet="+packet.getClass().getSimpleName());
//    Ostrov.log_warn("channelRead packet="+packet);
//}
      //Player p;

        switch (packet) {

          case
              final ServerboundInteractPacket interactPacket: //лкм пкм с пустой рукой. Если в руке что-то есть, то ServerboundUseItemOnPacket
                if (Cfg.bots) { //if (useEntityPacket.getActionType() == PacketPlayInUseEntity.b.b) {}
                  final int id = interactPacket.getEntityId();
                    for (final Botter bot : BotManager.botById.values()) {
                        if (bot.hashCode() == id) {
                            final FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
                          ServerboundInteractPacket.STREAM_CODEC.encode(buf, interactPacket);
                            super.channelRead(chc, reId(buf, bot.rid()));
                            return;
                        }
                    }
                }  // Paper start - PlayerUseUnknownEntityEvent
            if (op.disguise.type != null) {
//Ostrov.log_warn("p-> interactPacket type="+op.disguise.type+" getEntityId="+interactPacket.getEntityId());
              if (op.disguise.nmsEnt != null) {
                if (interactPacket.getEntityId() == op.disguise.nmsEnt.getId()) {
                  //Ostrov.log_warn("disguise p-> InteractPacket selfDisguiseId");
                  //op.disguise.action(PlayerDisguiseEvent.DisguiseAction.InteractSelfPacket);
                  return;
                } else {
//Ostrov.log_warn("disguise p-> interactPacket isAttack?"+interactPacket.isAttack());
                  //для зрителя только лкп/пкм на энтити
                  Ostrov.sync(() -> {
                    op.disguise.intercatAtEntity(interactPacket.getEntityId(), interactPacket.isAttack());
                  });
                  return;
                }
              }

              // else if (!interactPacket.isAttack() && op.disguise.type == EntityType.WOLF) { //LibsDisguised: If its an interaction that we should cancel, such as right clicking a wolf..
              //     Ostrov.log_warn("channelRead InteractPacket WOLF");
              //    return;
              // }
              //LibsDisguised PacketListenerClientInteract есть действия при пкм
            }
            break;


          case final ServerboundUseItemOnPacket useItemOnPacket://блокировка клика на фэйковый блок
            if (op.hasFakeBlock && op.fakeBlock.containsKey(useItemOnPacket.getHitResult().getBlockPos().asLong())) {
              return;
            }
            if (op.disguise.type != null) {
//Ostrov.log_warn("disguise p-> useItemOnPacket type="+op.disguise.type);
              //op.disguise.action(PlayerDisguiseEvent.DisguiseAction.UseItemOnPacket);
              Ostrov.sync(() -> {
                op.disguise.intercatAtBlock(useItemOnPacket.getHitResult());
              });
              return;
            }
            break;

          case final ServerboundPlayerActionPacket actionPacket://блокировка ломания фэйкогого блока
//Ostrov.log_warn("p-> actionPacket "+actionPacket.getAction());
            if (actionPacket.getAction() == ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK) {
              if (op.hasFakeBlock && op.fakeBlock.containsKey(actionPacket.getPos().asLong())) {
                return;
              }
            }
            if (op.disguise.type != null) {
              Ostrov.log_warn("disguise p-> actionPacket " + actionPacket.getAction());
              return;
            }
            break;


          case final ServerboundPickItemFromEntityPacket pickItemFromEntityPacket: //колёсико мыши на энтити
            if (op.disguise.type != null) {
//Ostrov.log_warn("disguise p-> PickItemFromEntity ");
              op.disguise.action(PlayerDisguiseEvent.DisguiseAction.PickItemFromEntityPacket);
              return;
            }
            break;

          case final ServerboundPickItemFromBlockPacket pickItemFromBlockPacket: //колёсико мыши на блок
            if (op.disguise.type != null) {
//Ostrov.log_warn("disguise p-> PickItemFromBlock ");
              op.disguise.action(PlayerDisguiseEvent.DisguiseAction.PickItemFromBlockPacket);
              return;
            }
            break;

          case final ServerboundSwingPacket swingPacket:
            if (op.disguise.type != null) {
//Ostrov.log_warn("disguise p-> Swing");
              //op.disguise.nmsMob.animateHurt(op.disguise.nmsMob.getYRot());
//Ostrov.log_warn("disguise p-> useItemOnPacket type="+op.disguise.type);
              //op.disguise.action(PlayerDisguiseEvent.DisguiseAction.UseItemOnPacket);
              Ostrov.sync(() -> {
                op.disguise.swing(swingPacket.getHand());
              });
              return;
              //action(PlayerDisguiseEvent.DisguiseAction.SwingPacket);
              //if (op.disguise.nmsEnt != null && op.disguise.nmsEnt instanceof LivingEntity nmsLe) {
              //  nmsLe.swing(swingPacket.getHand(), true);
              //}
            }
            break;


          case final ServerboundMovePlayerPacket move:
//Ostrov.log_warn("p-> lookPacket ");
            if (op.disguise.type != null) {
              // boolean pos = move instanceof ServerboundMovePlayerPacket.Pos || move instanceof ServerboundMovePlayerPacket.PosRot;
              boolean rot = move instanceof ServerboundMovePlayerPacket.Rot || move instanceof ServerboundMovePlayerPacket.PosRot;
              //ServerPlayer sp = op.disguise.sp;
              //if (pos) {
//Ostrov.log_warn("p-> pos Packet ");
              // if (op.disguise.nmsEnt != null) {
              //sp.connection.send(new ClientboundPlayerAbilitiesPacket(op.disguise.sp.getAbilities())); //обнулить скорость клиента (меняет колёсиком)
              // }
              //}
              if (rot) {
//Ostrov.log_warn("p-> rot Packet ");
                Ostrov.sync(() -> {
                  if (op.disguise.nmsEnt != null && op.disguise.nmsEnt instanceof LivingEntity) {
                    op.disguise.nmsEnt.forceSetRotation(move.yRot, false, move.xRot, false);
                  } //else if (op.disguise.nmsVehicle != null) {
                  //  op.disguise.nmsVehicle.forceSetRotation(move.yRot, false, move.xRot, false);
                  // }
                });
              }
              //return;
            }
            break;

          case final ServerboundPlayerInputPacket playerInput:
            if (op.disguise.type != null) {
//Ostrov.log_warn("p-> playerInput");
              op.disguise.lastInput = playerInput.input();
              return;
            }
                break;


          //case final ServerboundChangeGameModePacket changeGameMode:
//Ostrov.log_warn("p-> changeGameMode");
          // break;

          //case final ServerboundSetCarriedItemPacket setCarriedItem: //крутить колёсико
//Ostrov.log_warn("p-> setCarriedItem");
          //     break;

          case final ServerboundSignUpdatePacket sup://пакет ввода с таблички - не отдаём в сервер!
            Player p = op.getPlayer();
            if (p != null && PlayerInput.inputData.containsKey(p)) {  // в паспорте final String[] split = msg.split(" ");
              final String result = sup.getLines()[0] + " " + sup.getLines()[1] + " " + sup.getLines()[2] + " " + sup.getLines()[3];
              Ostrov.sync(() -> PlayerInput.onInput(p, InputButton.InputType.SIGN, result), 0);
              return;
            }
            break;

          default:
               /* final String name = packet.getClass().getSimpleName();
                switch (name) {
                    case "ServerboundClientTickEndPacket",
                         "ServerboundKeepAlivePacket",
                         "ServerboundSetCreativeModeSlotPacket",
                         "ServerboundCustomPayloadPacket" -> {
                        break;
                    }
                    default -> {
                        Ostrov.log_warn("p-> packet="+name);
                        break;
                    }
                }*/
                break;
        }


      if (nbtCheck.get()) { //не пропускаем в сервер хакнутые предметы от клиента
            //https://github.com/ds58/Panilla
            //PacketPlayInWindowClick = ServerboundContainerClickPacket
            //PacketPlayInSetCreativeSlot = ServerboundSetCreativeModeSlotPacket
            net.minecraft.world.item.ItemStack is;

            /*if (packet instanceof ServerboundContainerClickPacket p) {
                is = p.getCarriedItem(); //TODO fix
                if (!is.getComponents().isEmpty()) {//if (is != null && is.hasTag()) {
                    if (hacked(is, p.slotNum())) {
//                        containerClickItem.set(p, ItemStack.EMPTY);
                        super.channelRead(chc, new ServerboundContainerClickPacket(p.containerId(), p.stateId(),
                            p.slotNum(), p.buttonNum(), p.clickType(), ItemStack.EMPTY, p.carriedItem()));
                        return;
                    }
                }
            } else */
        if (packet instanceof ServerboundSetCreativeModeSlotPacket creativeModeSlotPacket) {
          is = creativeModeSlotPacket.itemStack();
                if (!is.getComponents().isEmpty()) {//if (is != null && is.hasTag()) {
                  if (hacked(is, creativeModeSlotPacket.slotNum())) {
//                        creativeSlotItem.set(p, ItemStack.EMPTY);
                    super.channelRead(chc, new ServerboundSetCreativeModeSlotPacket(creativeModeSlotPacket.slotNum(), ItemStack.EMPTY));
                        return;
                    }
                }
            }

        }

        super.channelRead(chc, packet);
    }



//Ostrov.log("UseItem fakeBlock?"+op.fakeBlock.containsKey(uip.getHitResult().getBlockPos().asLong()));
//Ostrov.log("START_DESTROY_BLOCK fakeBlock?"+op.fakeBlock.containsKey(pa.getPos().asLong()));







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





    /*protected static Vec3 getInputVector(Vec3 relative, float motionScaler, float facing) {
        double d = relative.lengthSqr();
        if (d < 1.0E-7) {
            return Vec3.ZERO;
        } else {
            Vec3 vec3 = (d > 1.0 ? relative.normalize() : relative).scale((double)motionScaler);
            float sin = Mth.sin(facing * 0.017453292F);
            float cos = Mth.cos(facing * 0.017453292F);
            return new Vec3(vec3.x * (double)cos - vec3.z * (double)sin, vec3.y, vec3.z * (double)cos + vec3.x * (double)sin);
        }
    }*/

    /*private Vec3 calculatePlayerInputSpeed(Vec3 speed) {
        //if (this.minecart.getFirstPassenger() instanceof ServerPlayer serverPlayer) {
            Vec3 lastClientMoveIntent = serverPlayer.getLastClientMoveIntent();
            if (lastClientMoveIntent.lengthSqr() > 0.0) {
                Vec3 vec3 = lastClientMoveIntent.normalize();
                double d = speed.horizontalDistanceSqr();
                if (vec3.lengthSqr() > 0.0 && d < 0.01) {
                    return speed.add(new Vec3(vec3.x, 0.0, vec3.z).normalize().scale(0.001));
                }
            }

            return speed;
        //} else {
        //    return speed;
        //}
    }*/


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

