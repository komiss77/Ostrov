package ru.komiss77.version;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.komiss77.Ostrov;
import ru.komiss77.modules.bots.BotEntity;
import ru.komiss77.modules.bots.BotManager;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.utils.PlayerInput;
import ru.komiss77.utils.inventory.InputButton;

import java.lang.reflect.Field;
import java.util.Iterator;

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


public class PlayerPacketHandler extends ChannelDuplexHandler {

    private final Oplayer op;
  public static Field interactIdField, moveIdField;

  static {
    try {
      interactIdField = ServerboundInteractPacket.class.getDeclaredFields()[0]; //по entityId не прокатит - на запущеном имена обфусцированны!
      interactIdField.setAccessible(true);
      moveIdField = ClientboundMoveEntityPacket.class.getDeclaredFields()[0];
      moveIdField.setAccessible(true);
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
      switch (packet) {
          case final ServerboundInteractPacket ip -> {
//Ostrov.log(""+pk.);
              if (BotManager.enable) {
                  final int id = ip.getEntityId();
                  for (final BotEntity bot : BotManager.botById.values()) {
                      if (bot.hashCode() == id) {
                          interactIdField.set(ip, bot.rid);
                          break;
                      }
                  }
//                if (useEntityPacket.getActionType() == PacketPlayInUseEntity.b.b) {}
              }  // Paper start - PlayerUseUnknownEntityEvent
          }
          case ServerboundSignUpdatePacket sup -> {
              final Player p = op.getPlayer();//Bukkit.getPlayerExact(name);

              if (p != null && PlayerInput.inputData.containsKey(p)) {  // в паспорте final String[] split = msg.split(" ");
                  final String result = sup.getLines()[0] + " " + sup.getLines()[1] + " " + sup.getLines()[2] + " " + sup.getLines()[3];
                  Ostrov.sync(() -> PlayerInput.onInput(p, InputButton.InputType.SIGN, result), 0);
                  return; //пакет ввода с таблички не отдаём в сервер!
              }
          }
          case ServerboundPlayerActionPacket pa -> {

              if (pa.getAction() == ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK
                      && op.hasFakeBlock && op.fakeBlock.containsKey(pa.getPos().asLong())) {
                  return;

              }
          }
          case ServerboundUseItemOnPacket uip -> {
              if (op.hasFakeBlock && op.fakeBlock.containsKey(uip.getHitResult().getBlockPos().asLong())) {
                  return;

              }
          }
          default -> {
          }
      }
        
        super.channelRead(chc, packet);
    }


  //исходящие пакеты от ядра до отправки клиенту
  @Override
    public void write(final ChannelHandlerContext chc, final Object packet, final ChannelPromise channelPromise) throws Exception {

    //при интеракт отправляет обнову блока после эвента. Чтобы не делать отправку с задержкой тик, нужно подменить исход.пакет
      if (packet instanceof ClientboundBlockUpdatePacket bup && op.hasFakeBlock) {
        final BlockData bd = op.fakeBlock.get(bup.getPos().asLong());
        if (bd!=null) {
          bup = new ClientboundBlockUpdatePacket(bup.getPos(), Craft.toNMS(bd));
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
                final Iterator<Packet<? super ClientGamePacketListener>> pit = clientboundBundlePacket.subPackets().iterator();
                while (pit.hasNext()) {
                    final Packet<?> pc = pit.next();
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

        super.write(chc, packet, channelPromise);
    }
}





