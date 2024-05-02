package ru.komiss77.modules.redis;

import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.UnifiedJedis;
import redis.clients.jedis.exceptions.JedisConnectionException;
import ru.komiss77.LocalDB;
import ru.komiss77.Ostrov;
import ru.komiss77.enums.Game;
import ru.komiss77.enums.GameState;
import ru.komiss77.modules.games.GM;
import ru.komiss77.modules.games.GameInfo;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Subscriber implements Runnable {

  //private final ReentrantLock lock = new ReentrantLock();
    private static final JedisPubSubHandler jpsh;
    private static final Set<String> addedChannels;

    static {
      addedChannels = new HashSet<>();
      jpsh = new JedisPubSubHandler();
    }

    @Override
    public void run() {
      RedisTask<Void> subTask = new RedisTask<Void>() {
        @Override
        public Void unifiedJedisTask(UnifiedJedis unifiedJedis) {
          addedChannels.add("ostrov");
          addedChannels.add("arenadata");
          unifiedJedis.subscribe(jpsh, addedChannels.toArray(new String[0]));
          return null;
        }
      };

      try {

        subTask.execute();

      } catch (Exception ex) {

        Ostrov.log_err("RDS  Subscriber : "+ex.getMessage());
        RDS.unsubscribe();
        //Bukkit.getScheduler().runTaskLaterAsynchronously(Ostrov.getInstance(), ()-> {
        //  Ostrov.log_warn("RDS  Subscriber : restore connection");
        //  RDS.subscriber.run();
        //}, 15*20);

        //plugin.executeAsyncAfter(this, TimeUnit.SECONDS, 5);
      }
    }

    public void addChannel(String... channel) {
      addedChannels.addAll(Arrays.asList(channel));
      jpsh.subscribe(channel);
    }

    public void removeChannel(String... channel) {
      Arrays.asList(channel).forEach(addedChannels::remove);
      jpsh.unsubscribe(channel);
    }

    public void poison() {
      addedChannels.clear();
      try {
        jpsh.unsubscribe();
      } catch (JedisConnectionException ex) {
        Ostrov.log_warn("RDS Subscribe poison : "+ex.getMessage());
      }
    }



}

class JedisPubSubHandler extends JedisPubSub {//BinaryJedisPubSub {
  @Override
  //public void onMessage(byte[] channel, byte[] message) {
  public void onMessage(final String channelName, final String msg) {
    //String channelName = new String(channel, StandardCharsets.UTF_8);
    //String msg = new String(message, StandardCharsets.UTF_8);
    try {
//Ostrov.log_warn("RedisLst : onMessage channel="+channelName+" msg="+msg);
      if (msg.isBlank()) return;
      switch (channelName) {
        case "ostrov":
          break;
        case "arenadata":
          final String[]s = msg.split(LocalDB.WORD_SPLIT);
          if (s.length == 9) {
            final Game game = Game.fromServerName(s[0]);
//Ostrov.log("GAME_INFO_TO_OSTROV s6="+s6+" game="+game+" arena="+s1);
            if (game != null) {
              final GameInfo gi = GM.getGameInfo(game);
              if (gi != null) {
                gi.update(s[1], s[2], GameState.valueOf(s[3]), Integer.parseInt(s[4]), s[5], s[6], s[7], s[8]);
              } else {
                Ostrov.log_err("RedisLst arenadata GameInfo==null : " + msg);
              }
            }
          } else {
            Ostrov.log_err("RedisLst arenadata msg.length != 9 : " + msg);
          }
          break;
        default:
          break;
      }


    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public void onPong(String s) {
    Ostrov.log_warn("===============RedisLst PONG : "+s);
  }

}




/*
  @Override
  public void subscribe(byte[]... channels) {
    this.lock.lock();
    try {
      for (byte[] channel : channels) {
        String channelName = new String(channel, StandardCharsets.UTF_8);
        if (this.subscribed.add(channelName)) {
          super.subscribe(channel);
        }
      }
    } finally {
      this.lock.unlock();
    }
  }

  @Override
  public void unsubscribe(byte[]... channels) {
    this.lock.lock();
    try {
      super.unsubscribe(channels);
    } finally {
      this.lock.unlock();
    }
  }

  @Override
  public void onSubscribe(byte[] channel, int subscribedChannels) {
      Ostrov.log_ok("§aRedisLst : subscribed to channel "+new String(channel, StandardCharsets.UTF_8));
  }

  @Override
  public void onUnsubscribe(byte[] channel, int subscribedChannels) {
    String channelName = new String(channel, StandardCharsets.UTF_8);
    Ostrov.log_ok("§aRedisLst : unsubscribed to channel "+channelName);
    subscribed.remove(channelName);
  }*/


