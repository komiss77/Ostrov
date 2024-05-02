package ru.komiss77.modules.redis;

import java.io.Closeable;
import java.io.IOException;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.providers.PooledConnectionProvider;
import ru.komiss77.Ostrov;


interface Summoner<P> extends Closeable {
  P obtainResource();
}

public class JedisPoolProvider implements Summoner<JedisPooled> {

  private final PooledConnectionProvider connectionProvider;
  private final JedisPool jedisPool;

  public JedisPoolProvider(final PooledConnectionProvider connectionProvider, final JedisPool jedisPool) {
    this.connectionProvider = connectionProvider;
    this.jedisPool = jedisPool;
    testConnection();
  }

  public void testConnection() {
    // test connections
    if (jedisPool != null) {
      try (Jedis jedis = jedisPool.getResource()) {
        // Test the connection to make sure configuration is right
        jedis.ping(String.valueOf(System.currentTimeMillis()));

        try (final JedisPooled jedisPooled = obtainResource()) {
          jedisPooled.set("random_data", "0");
          jedisPooled.del("random_data");
        }
        //Ostrov.log_ok("JedisPoolProvider ping responce : "+jedis.ping());
      } catch ( Exception ex) {//JedisConnectionException
        //ex.printStackTrace();
        Ostrov.log_err("JedisPoolProvider : "+ex.getMessage());
      }
    }
  }

  @Override
  public JedisPooled obtainResource() {
    // create UnClosable JedisPool *disposable*
    return new NotClosableJedisPooled(connectionProvider);
  }

  public JedisPool getCompatibilityJedisPool() {
    return jedisPool;
  }

  @Override
  public void close() throws IOException {
    if (jedisPool != null) {
      jedisPool.close();
    }
    connectionProvider.close();
  }
}






class NotClosableJedisPooled extends JedisPooled {
  NotClosableJedisPooled(PooledConnectionProvider provider) {
    super(provider);
  }
  @Override
  public void close() {
  }
}
