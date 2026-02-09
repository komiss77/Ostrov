package ru.komiss77.version;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.PositionMoveRotation;
import net.minecraft.world.item.ItemStack;
import org.bukkit.entity.EntityType;
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
  private static Constructor<ClientboundRotateHeadPacket> constructor;// = ClientboundRotateHeadPacket.class.getDeclaredConstructor();
  private static Field entityIdField;

  static {
    try {
      constructor = ClientboundRotateHeadPacket.class.getDeclaredConstructor(FriendlyByteBuf.class);
      constructor.setAccessible(true);
      //утилитка поиска номера поля - не удалять!!
      //for (Field f : ServerboundInteractPacket.class.getDeclaredFields()) {
      //  Ostrov.log_warn("PlayerPacketHandler f="+f);
      //}
      entityIdField = ServerboundInteractPacket.class.getDeclaredFields()[1];
      entityIdField.setAccessible(true);
    } catch (ArrayIndexOutOfBoundsException | NoSuchMethodException ex) {
      Ostrov.log_err("PlayerPacketHandler constructor : " + ex.getMessage());
    }
  }

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

          //лкм пкм с пустой рукой. Если в руке что-то есть, то ServerboundUseItemOnPacket
          //с версии 1.21.11 spectator отправляет пакеты только пкм блок(useItemOnPacket), лкм/пкм энтити(interactPacket), swing больше нет!
          case final ServerboundInteractPacket interactPacket:
            final int id = interactPacket.getEntityId();
            if (Cfg.bots) {
              for (final Botter bot : BotManager.botById.values()) {
                if (bot.hashCode() == id) {
                  //final FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
                  //ServerboundInteractPacket.STREAM_CODEC.encode(buf, interactPacket);
                  //super.channelRead(chc, reId(buf, bot.rid()));
                  //return;
                  entityIdField.set(interactPacket, bot.rid());  //подмена ид бота
                  break;
                }
              }
            }  // Paper start - PlayerUseUnknownEntityEvent
//Ostrov.log_warn("p-> interactPacket "+interactPacket.getEntityId()+":"+op.disguise.fakeTargetId+" isAttack?"+interactPacket.isAttack());

            if (op.disguise.fakeTargetId > 0) { //зрителю не даём контачить ни с чем, кроме fakeTarget!
//Ostrov.log_warn("p-> InteractPacket tick="+op.tick+" last="+op.disguise.lastInteractTick+" d="+(op.disguise.lastInteractTick - op.tick));
              if (op.disguise.lastInteractTick - op.tick > 0) return;
              op.disguise.lastInteractTick = op.tick + 5;

              //клик не в режиме маскировки
              if (op.disguise.nmsEnt == null) {
                //смотрим результат rt на предыдущем тике - луч упирался в энтити
                if (op.disguise.rt != null && op.disguise.rt.getHitEntity() != null) {
//int oldid = interactPacket.getEntityId();
                  entityIdField.set(interactPacket, op.disguise.rt.getHitEntity().getEntityId());  //подмена ид слама на ид результата rt
//Ostrov.log_warn("p-> InteractPacket relace id "+oldid+"->"+interactPacket.getEntityId());
                  break;
                }
              }
              Ostrov.sync(() -> {
                op.disguise.fakeTargetInteract(interactPacket.isAttack());
              });
              return;
            }
            break;


          //с версии 1.21.11 spectator отправляет пакеты только пкм блок(useItemOnPacket), лкм/пкм энтити(interactPacket), swing больше нет!
          case final ServerboundUseItemOnPacket useItemOnPacket://блокировка клика на фэйковый блок
            if (op.hasFakeBlock && op.fakeBlock.containsKey(useItemOnPacket.getHitResult().getBlockPos().asLong())) {
              return;
            }
//Ostrov.log_warn("disguise p-> useItemOnPacket ");
            if (op.disguise.fakeTargetId > 0) { //зрителю не даём контачить ни с чем, кроме fakeTarget!
//Ostrov.log_warn("disguise p-> useItemOnPacket type="+op.disguise.type);
              return;
            }
            break;

          case final ServerboundPlayerActionPacket actionPacket://блокировка ломания фэйкогого блока
            if (actionPacket.getAction() == ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK) {
              if (op.hasFakeBlock && op.fakeBlock.containsKey(actionPacket.getPos().asLong())) {
                return;
              }
            }
//Ostrov.log_warn("disguise p-> actionPacket " + actionPacket.getAction());
            if (op.disguise.fakeTargetId > 0) {//зрителю не даём контачить ни с чем, кроме fakeTarget!
              return;
            }
            break;

          case final ServerboundMovePlayerPacket move:
//Ostrov.log_warn("p-> move ");
            //когда пассажир на маскировке, move от игрока не приходят, только rot
            if (op.disguise.fakeTargetId > 0 || op.disguise.nmsEnt != null) {
              boolean pos = move instanceof ServerboundMovePlayerPacket.Pos || move instanceof ServerboundMovePlayerPacket.PosRot;
              boolean rot = move instanceof ServerboundMovePlayerPacket.Rot || move instanceof ServerboundMovePlayerPacket.PosRot;
              if (rot) {
//Ostrov.log_warn("p-> rot Packet ");
                Ostrov.sync(() -> {
                  if (op.disguise.fakeTargetId > 0) {
                    final FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
                    buf.writeVarInt(op.disguise.fakeTargetId);
                    byte degress = Mth.packDegrees(move.yRot - 180);
                    buf.writeByte(degress);
                    try { //слим разворачивать задом чтобы не просвечивали глаза
                      ClientboundRotateHeadPacket rotateHeadPacket = constructor.newInstance(buf);
//Ostrov.log_warn("rot "+move.yRot+" -180="+(move.yRot-180)+" degress="+degress+" res="+rotateHeadPacket.getYHeadRot());
                      op.disguise.sp.connection.send(rotateHeadPacket);
                    } catch (InstantiationException | IllegalAccessException | InvocationTargetException ex) {
                      Ostrov.log_warn("PlayerPacketHandler ServerboundMovePlayerPacket constructor : " + ex.getMessage());
                    }
                  }
                  if (op.disguise.nmsEnt != null) {
                    if (op.disguise.type == EntityType.ENDER_DRAGON) { //на драконе задом наперёд
                      op.disguise.nmsEnt.forceSetRotation(move.yRot - 180, false, move.xRot, false);
                    } else {
                      op.disguise.nmsEnt.forceSetRotation(move.yRot, false, move.xRot, false);
                    }
                  }
                });
              }
              if (pos) {
//Ostrov.log_warn("p-> pos Packet ");
                //когда пассажир на маскировке, move от игрока не приходят, только rot
                op.disguise.syncPosition();
              }
              //return;
            }
            break;

          case final ServerboundPlayerInputPacket playerInput:
//Ostrov.log_warn("p-> playerInput");
            if (op.disguise.type != null) {
              op.disguise.lastInput = playerInput.input();
              return;
            }
            break;

          case final ServerboundPickItemFromEntityPacket pickItemFromEntityPacket: //колёсико мыши на энтити
            if (op.disguise.type != null) {
//Ostrov.log_warn("disguise p-> PickItemFromEntity ");
              Ostrov.sync(() -> {
                op.disguise.action(PlayerDisguiseEvent.DisguiseAction.PICK_ITEM_FROM_ENTITY_PACKET);
              });
              return;
            }
            break;

          case final ServerboundPickItemFromBlockPacket pickItemFromBlockPacket: //колёсико мыши на блок
            if (op.disguise.type != null) {
//Ostrov.log_warn("disguise p-> PickItemFromBlock ");
              Ostrov.sync(() -> {
                op.disguise.action(PlayerDisguiseEvent.DisguiseAction.PICK_ITEM_FROM_BLOCK_PACKET);
              });
              return;
            }
            break;

          case final ServerboundSignUpdatePacket sup://пакет ввода с таблички - не отдаём в сервер!
            Player p = op.getPlayer();
            if (p != null && PlayerInput.inputData.containsKey(p)) {  // в паспорте final String[] split = msg.split(" ");
              final String result = sup.getLines()[0] + " " + sup.getLines()[1] + " " + sup.getLines()[2] + " " + sup.getLines()[3];
              Ostrov.sync(() -> PlayerInput.onInput(p, InputButton.InputType.SIGN, result), 0);
              return;
            }
            break;

          case final ServerboundSetCreativeModeSlotPacket creativeModeSlotPacket:
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
              is = creativeModeSlotPacket.itemStack();
                if (!is.getComponents().isEmpty()) {//if (is != null && is.hasTag()) {
                  if (hacked(is, creativeModeSlotPacket.slotNum())) {
//                        creativeSlotItem.set(p, ItemStack.EMPTY);
                    super.channelRead(chc, new ServerboundSetCreativeModeSlotPacket(creativeModeSlotPacket.slotNum(), ItemStack.EMPTY));
                    return;
                  }
                }
            }
            break;

          //с версии 1.21.11 spectator отправляет пакеты только пкм блок(useItemOnPacket), лкм/пкм энтити(interactPacket), swing больше нет!
          //case final ServerboundSwingPacket swingPacket:
//Ostrov.log_warn("disguise p-> Swing");
          //if (op.disguise.type != null) {
          //Ostrov.sync(() -> {
          //  op.disguise.swing(swingPacket.getHand());
          // });
          // return;
          //}
          //break;

          //case final ServerboundChangeGameModePacket changeGameMode:
//Ostrov.log_warn("p-> changeGameMode");
          // break;

          //case final ServerboundSetCarriedItemPacket setCarriedItem: //крутить колёсико
//Ostrov.log_warn("p-> setCarriedItem");
          //     break;

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


        super.channelRead(chc, packet);
    }



//Ostrov.log("UseItem fakeBlock?"+op.fakeBlock.containsKey(uip.getHitResult().getBlockPos().asLong()));
//Ostrov.log("START_DESTROY_BLOCK fakeBlock?"+op.fakeBlock.containsKey(pa.getPos().asLong()));







   /* private static ServerboundInteractPacket reId(final FriendlyByteBuf buf, final int id) {
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
    }*/




            /*if (op.disguise.fakeTargetId > 0 || op.disguise.nmsEnt != null) {
              if (op.tick - op.disguise.lastInteractTick < 5) return;
              op.disguise.lastInteractTick = op.tick;

//Ostrov.log_warn("p-> interactPacket "+interactPacket.getEntityId()+":"+op.disguise.fakeTargetId+" isAttack?"+interactPacket.isAttack());

                if (op.disguise.fakeTargetId > 0) {
//Ostrov.log_warn("p-> InteractPacket fakeTarget isAttack?"+interactPacket.isAttack());
                  Ostrov.sync(() -> {
                    op.disguise.fakeTargetInteract(interactPacket.isAttack());
                  });
                } else if (interactPacket.getEntityId() == op.disguise.nmsEnt.getId()) {
//Ostrov.log_warn("disguise p-> InteractPacket nmsEnt isAttack?"+interactPacket.isAttack());
                  //op.disguise.action(PlayerDisguiseEvent.DisguiseAction.InteractSelfPacket);
                } else {
//Ostrov.log_warn("disguise p-> interactPacket isAttack?"+interactPacket.isAttack());
                  //для зрителя только лкп/пкм на энтити
                  Ostrov.sync(() -> {
                    op.disguise.intercatAtEntity(interactPacket.getEntityId(), interactPacket.isAttack());
                  });
                }

              // else if (!interactPacket.isAttack() && op.disguise.type == EntityType.WOLF) { //LibsDisguised: If its an interaction that we should cancel, such as right clicking a wolf..
              //     Ostrov.log_warn("channelRead InteractPacket WOLF");
              //    return;
              // }
              //LibsDisguised PacketListenerClientInteract есть действия при пкм
              return;
            }*/

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

