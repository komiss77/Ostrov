package ru.komiss77.modules.redis;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import io.netty.util.concurrent.FastThreadLocalThread;
import static com.google.common.base.Preconditions.checkNotNull;

public class OstrovThreadFactory implements ThreadFactory {

  private final AtomicInteger threadNumber = new AtomicInteger();
  private final String nameFormat;

  public OstrovThreadFactory(String nameFormat) {
    this.nameFormat = checkNotNull(nameFormat, "nameFormat");
  }

  @Override
  public Thread newThread(Runnable r) {
    String name = String.format(nameFormat, threadNumber.getAndIncrement());
    return new FastThreadLocalThread(r, name);
  }
}