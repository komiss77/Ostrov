package ru.komiss77.modules.redis;

import com.google.common.collect.ImmutableList;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.bukkit.Bukkit;
import redis.clients.jedis.*;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.providers.PooledConnectionProvider;
import ru.komiss77.Initiable;
import ru.komiss77.Ostrov;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

//Redis Database, база данных Redis) для организации постоянного хранения снепшотов (снимков) данных
// Установка, настройка и работа с Redis https://www.dmosk.ru/miniinstruktions.php?mini=redis-ubuntu
// https://github.com/redis/jedis

public class RDS implements Initiable {
  public static final int PROXY_TIMEOUT = 30;
  public static JedisPoolProvider poolProvider;
  //protected static PubSubListener pubSubListener;
  protected static Subscriber subscriber;
  private static String ip = "37.59.46.87";

  static {
    try {
      if (ip.equals(InetAddress.getLocalHost().getHostAddress())) {
        ip = "127.0.0.1";
      }
    } catch (UnknownHostException ex) {
      Ostrov.log_err("RDS detect ip : "+ex.getMessage());
    }

    init();
  }

  @Override
  public void postWorld() {
  }

  @Override
  public void reload() {
    onDisable();
    init();
  }

  @Override
  public void onDisable() {
    unsubscribe();

    new RedisTask<Void>() {
      @Override
      public Void unifiedJedisTask(UnifiedJedis unifiedJedis) {
        unifiedJedis.hdel("heartbeats", Ostrov.MOT_D);
        return null;
      }
    }.execute();

    try {
      poolProvider.close();
    } catch (IOException ex) {
      Ostrov.log_err("RDS poolProvider.close : "+ex.getMessage());
    }
  }




  public static void init() {
    final JedisPoolConfig poolConfig = new JedisPoolConfig();
    poolConfig.setMaxTotal(3);
    poolConfig.setBlockWhenExhausted(true);
    //poolConfig.setMaxIdle(128);
    //poolConfig.setMinIdle(16);
    //poolConfig.setTestOnBorrow(true);
    //poolConfig.setTestOnReturn(true);
    //poolConfig.setTestWhileIdle(true);
    //poolConfig.setMinEvictableIdleTimeMillis(Duration.ofSeconds(60).toMillis());
    //poolConfig.setTimeBetweenEvictionRunsMillis(Duration.ofSeconds(30).toMillis());
    //poolConfig.setNumTestsPerEvictionRun(3);
    //poolConfig.setBlockWhenExhausted(true);

    try (final JedisPool jedisPool = new JedisPool(poolConfig, ip, 6379, 5000, "default", "redis", false)) {

      final GenericObjectPoolConfig<Connection> connectionGenericObjectPoolConfig = new GenericObjectPoolConfig<>();
      poolConfig.setMaxTotal(10);
      poolConfig.setBlockWhenExhausted(true);

      final HostAndPort hostAndPort = new HostAndPort(ip, 6379);
      final DefaultJedisClientConfig jedisClientConfig = DefaultJedisClientConfig.builder()
        .user("default")
        .password("redis")
        .timeoutMillis(5000)
        .ssl(false)
        .build();
      final ConnectionFactory connectionFactory = new ConnectionFactory(hostAndPort, jedisClientConfig);
      final PooledConnectionProvider pooledConnectionProvider = new PooledConnectionProvider(connectionFactory, connectionGenericObjectPoolConfig);

      poolProvider = new JedisPoolProvider(pooledConnectionProvider, jedisPool);

      new RedisTask<Void>() {
        @Override
        public Void unifiedJedisTask(final UnifiedJedis unifiedJedis) {
          final String info = new String((byte[]) unifiedJedis.sendCommand(Protocol.Command.INFO));
          for (final String s : info.split("\r\n")) {
            if (s.startsWith("redis_version:")) {
              final String version = s.split(":")[1];
              Ostrov.log("§aRedis server version: §b" + version);
              final long uuidCacheSize = unifiedJedis.hlen("uuid-cache");
              if (uuidCacheSize > 750000) {
                Ostrov.log("Looks like you have a really big UUID cache! Run https://github.com/ProxioDev/Brains");
              }
              subscribe();
              break;
            }
          }

          if (unifiedJedis.hexists("heartbeats", Ostrov.MOT_D)) {
            try {
              final long value = Long.parseLong(unifiedJedis.hget("heartbeats", Ostrov.MOT_D));
              final long redisTime = getRedisTime(unifiedJedis);
              if (redisTime < value + PROXY_TIMEOUT) {
                Ostrov.log_err("You have launched a possible impostor Velocity / Bungeecord instance. Another instance is already running.");
                Ostrov.log_err("For data consistency reasons, RedisBungee will now disable itself.");
                Ostrov.log_err("If this instance is coming up from a crash, create a file in your RedisBungee plugins directory with the name 'restarted_from_crash.txt' and RedisBungee will not perform this check.");
              }
            } catch (NumberFormatException ignored) {
            }
          }
          return null;
        }
      }.execute();
    }
  }

  protected static void subscribe () {
    unsubscribe();
    subscriber = new Subscriber();
    Bukkit.getScheduler().runTaskAsynchronously(Ostrov.getInstance(), ()-> {
      subscriber.run();
    });
  }
  protected static void unsubscribe () {
    if (subscriber!=null) {
      subscriber.poison();
      subscriber = null;
    }
  }
  //try (Jedis jedis = jedisPool.getResource()) {
  //jedis.subscribe(redisLst, "ostrov".getBytes(StandardCharsets.UTF_8));
  //  jedis.subscribe(redisLst, chanels.toArray(new String[0]));
  //} catch (Exception ex) {
  //   ex.printStackTrace();
  //  }


  public static void heartbeats() {
    //if (redisLst == null) return;
    new RedisTask<Void>() {
      @Override
      public Void unifiedJedisTask(UnifiedJedis unifiedJedis) {
        try {
          long redisTime = getRedisTime(unifiedJedis);
          unifiedJedis.hset("heartbeats", Ostrov.MOT_D, String.valueOf(redisTime));
//poolProvider.testConnection();
          if (subscriber==null) {
            subscribe();
          }
//Ostrov.log_ok("redis heartbeats : "+redisTime);
        } catch (JedisConnectionException ex) {
          // Redis server has disappeared!
          Ostrov.log_err("RDS heartbeats : "+ex.getMessage());
          unsubscribe();
          //e.printStackTrace();
          return null;
        }
        //try {
        //   plugin.updateProxiesIds();
        //  globalPlayerCount.set(plugin.getCurrentCount());
        // } catch (Throwable e) {
        //  plugin.logFatal("Unable to update data - did your Redis server go away?");
        //  e.printStackTrace();
        // }
        return null;
      }
    }.execute();
  }



  private static List<String> getCurrentProxiesIds(boolean lagged) {
    return new RedisTask<List<String>>() {
      @Override
      public List<String> unifiedJedisTask(UnifiedJedis unifiedJedis) {
        try {
          long time = getRedisTime(unifiedJedis);
          ImmutableList.Builder<String> servers = ImmutableList.builder();
          Map<String, String> heartbeats = unifiedJedis.hgetAll("heartbeats");
          for (Map.Entry<String, String> entry : heartbeats.entrySet()) {
            try {
              long stamp = Long.parseLong(entry.getValue());
              if (lagged ? time >= stamp + PROXY_TIMEOUT : time <= stamp + PROXY_TIMEOUT) {
                servers.add(entry.getKey());
              } else if (time > stamp + PROXY_TIMEOUT) {
                Ostrov.log_warn(entry.getKey() + " is " + (time - stamp) + " seconds behind! (Time not synchronized or server down?) and was removed from heartbeat.");
                unifiedJedis.hdel("heartbeats", entry.getKey());
              }
            } catch (NumberFormatException ignored) {
            }
          }
          return servers.build();
        } catch (JedisConnectionException e) {
          Ostrov.log_err("Unable to fetch server IDs");
          e.printStackTrace();
          return Collections.singletonList(Ostrov.MOT_D);
        }
      }
    }.execute();
  }


  public static void sendMessage(final String channel, final String message) {
    new RedisTask<Void>() {
      @Override
      public Void unifiedJedisTask(UnifiedJedis unifiedJedis) {
        try {
          unifiedJedis.publish(channel, message);
//Ostrov.log_warn("PDS sendChannelMessage channel="+channel+" message="+message);
        } catch (JedisConnectionException ex) {
          // Redis server has disappeared!
          Ostrov.log_err("RDS sendMessage : "+ex.getMessage());
          //throw new RuntimeException("Unable to publish channel message", e);
        }
        return null;
      }
    }.execute();
  }



  private static Long getRedisTime(UnifiedJedis unifiedJedis) {
    @SuppressWarnings("unchecked")
    final List<Object> data = (List<Object>) unifiedJedis.sendCommand(Protocol.Command.TIME);
    final List<String> times = new ArrayList<>();
    data.forEach((o) -> times.add(new String((byte[])o)));
    return Long.parseLong(times.get(0));//getRedisTime(times);
  }



}






    /*task = new BukkitRunnable() {
      @Override
      public void run() {
        new RedisTask<Void>() {
          @Override
          public Void unifiedJedisTask(UnifiedJedis unifiedJedis) {
            try {
              long redisTime = getRedisTime(unifiedJedis);
              unifiedJedis.hset("heartbeats", Ostrov.MOT_D, String.valueOf(redisTime));
            } catch (JedisConnectionException e) {
              // Redis server has disappeared!
              Ostrov.log_err("Unable to update heartbeat - did your Redis server go away?");
              e.printStackTrace();
              return null;
            }
            //try {
           //   plugin.updateProxiesIds();
            //  globalPlayerCount.set(plugin.getCurrentCount());
           // } catch (Throwable e) {
            //  plugin.logFatal("Unable to update data - did your Redis server go away?");
            //  e.printStackTrace();
           // }
            return null;
          }
        }.execute();
      }
    }.runTaskTimerAsynchronously(Ostrov.getInstance(), 21, 21);*/

 /* private static int getCurrentCount() {
    return new RedisTask<Long>() {
      @Override
      public Long unifiedJedisTask(UnifiedJedis unifiedJedis) {
        long total = 0;
        long redisTime = getRedisTime(unifiedJedis);
        Map<String, String> heartBeats = unifiedJedis.hgetAll("heartbeats");
        //так формируется список живых серверов
        for (Map.Entry<String, String> stringStringEntry : heartBeats.entrySet()) {
          String k = stringStringEntry.getKey();
          String v = stringStringEntry.getValue();
            long heartbeatTime = Long.parseLong(v);
          if (heartbeatTime + PROXY_TIMEOUT >= redisTime) {
            total = total + unifiedJedis.scard("proxy:" + k + ":usersOnline");
          }
        }
        return total;
      }
    }.execute().intValue();
  }*/
