package ru.komiss77.version;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.*;
import net.minecraft.world.item.ItemStack;
import org.bukkit.block.data.BlockData;
import ru.komiss77.Cfg;
import ru.komiss77.Ostrov;
import ru.komiss77.modules.bots.BotManager;
import ru.komiss77.modules.bots.Botter;
import ru.komiss77.modules.player.Oplayer;

public class ServerToPlayer extends ChannelDuplexHandler {

  protected final Oplayer op;
  public static Field moveIdField; //ClientboundMoveEntityPacket - получение ид бота
  public static AtomicBoolean nbtCheck = new AtomicBoolean(false);

  static {
    try {
      //утилитка поиска номера поля - не удалять!!
      //for (Field f : ClientboundMoveEntityPacket.class.getDeclaredFields()) {
      //  Ostrov.log_warn("ServerToPlayer f="+f);
      //}
      moveIdField = ClientboundMoveEntityPacket.class.getDeclaredFields()[0];
      moveIdField.setAccessible(true);
    } catch (ArrayIndexOutOfBoundsException ex) {
      Ostrov.log_err("PlayerPacketHandler getIdField : " + ex.getMessage());
    }
  }

  public ServerToPlayer(final Oplayer op) {
    this.op = op;
  }


  //исходящие пакеты от ядра до отправки клиенту
  @Override
  public void write(final ChannelHandlerContext chc, final Object packet, final ChannelPromise channelPromise) throws Exception {

    switch (packet) {

      //case final ClientboundMoveEntityPacket moveEntityPacket:
//Ostrov.log_warn("send moveEntityPacket "+moveEntityPacket);
      //if (op.disguise.nmsLe != null) {
      //int id = (int) moveIdField.get(moveEntityPacket);
      //if (id == op.disguise.nmsLe.getId()) {
//Ostrov.log_warn("send moveEntityPacket disguise id="+id);
      //}
//Ostrov.log_warn("send moveEntityPacket id="+id);
      //moveIdField.set(moveEntityPacket, op.disguise.selfDisguiseId);
//Ostrov.log_warn("подмена id="+moveIdField.get(moveEntityPacket));
      //}
      //break;

      //case final ClientboundAddEntityPacket addEntityPacket:
      //if (op.disguise.type != null) {
      //Ostrov.log_warn("send addEntityPacket "+addEntityPacket.getType()+" id="+addEntityPacket.getId());
      //}
      //break;

      //case final ClientboundRemoveEntitiesPacket removeEntitiesPacket:
      //    if (op.disguise.type != null) {
      //        for (int entityId : removeEntitiesPacket.getEntityIds()) {
      //             handleEntityId(player, entityId);
      //         }
      //    }
      //    break;

      /*case final ClientboundSetEntityMotionPacket setEntityMotionPacket:
          if (op.disguise.type != null) {
            if (op.disguise.nmsLe != null) {
              if (setEntityMotionPacket.getId() == op.disguise.nmsLe.getId()) {

              }
            }
          }
          break;*/

      case final ClientboundBlockUpdatePacket bup://блокировка клика на фэйковый блок
        //при интеракт отправляет обнову блока после эвента. Чтобы не делать отправку с задержкой тик, нужно подменить исход.пакет
        if (op.hasFakeBlock) {
          final BlockData bd = op.fakeBlock.get(bup.getPos().asLong());
          if (bd != null) {
            super.write(chc, new ClientboundBlockUpdatePacket(bup.getPos(), Craft.toNMS(bd)), channelPromise);
            return;
          }
        }
        break;

      case final ClientboundSetPassengersPacket setPassengersPacket:
        final Botter bt = BotManager.getBot(setPassengersPacket.getVehicle());
        if (bt != null) {
          final FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
          buf.writeVarInt(bt.hashCode());
          buf.writeVarIntArray(setPassengersPacket.getPassengers());
          final ClientboundSetPassengersPacket mp = ClientboundSetPassengersPacket.STREAM_CODEC.decode(buf);
          super.write(chc, mp, channelPromise);
          return;
        }
        break;

      default:
        /*final String name = packet.getClass().getSimpleName();
        switch (name) {
          case "ClientboundSetTitleTextPacket",
               "ClientboundLevelChunkWithLightPacket",
               "ClientboundKeepAlivePacket",
               "ClientboundSetTimePacket",
               "ClientboundTabListPacket" -> {
            break;
          }
          default -> {
            Ostrov.log_warn("send-> "+name);
            break;
          }
        }*/
        break;

    }
    //при интеракт отправляет обнову блока после эвента. Чтобы не делать отправку с задержкой тик, нужно подменить исход.пакет
    //if (packet instanceof final ClientboundBlockUpdatePacket bup && op.hasFakeBlock) {
    //    final BlockData bd = op.fakeBlock.get(bup.getPos().asLong());
    //    if (bd != null) {
    //        super.write(chc, new ClientboundBlockUpdatePacket(bup.getPos(), Craft.toNMS(bd)), channelPromise);
    //        return;
    //    }
    //}

    //if (packet instanceof final ClientboundSetPassengersPacket setPassengersPacket) {
    //    final Botter bt = BotManager.getBot(setPassengersPacket.getVehicle());
    //    if (bt != null) {
    //        final FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
    //        buf.writeVarInt(bt.hashCode());
    //        buf.writeVarIntArray(setPassengersPacket.getPassengers());
    //        final ClientboundSetPassengersPacket mp =
    //            ClientboundSetPassengersPacket.STREAM_CODEC.decode(buf);
    //        super.write(chc, mp, channelPromise);
    //        return;
    //    }
    //}

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
//                            containerSetSlotItem.set(setPassengersPacket, ItemStack.EMPTY);
              super.write(chc, new ClientboundContainerSetSlotPacket(p.getContainerId(),
                  p.getStateId(), p.getSlot(), ItemStack.EMPTY), channelPromise);
              return;
            }
          }
        }

      } else if (packet instanceof ClientboundContainerSetContentPacket p) {

        if (p.containerId() == 0) {
          is = p.carriedItem();
          List<ItemStack> items = p.items();
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


  protected boolean hacked(net.minecraft.world.item.ItemStack is, int slot) {
    //https://github.com/ds58/Panilla
    //CompoundTag tag = is.getTag();
    //tag.tags

    return false;
  }
}
