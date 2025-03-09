package ru.komiss77.modules.netty;


public class QueryCode {

  public static final short MAGIC_OS_FIRST = 0xAB; //отправить как byte может, но сравнить нет!! поэтому short
  public static final short MAGIC_OS_SECOND = 0xCD;
  public static final byte HEARTBEAT = ((byte) 0x01);
  public static final byte REMOTE_CMD = ((byte) 0x02);
  public static final byte GAME_DATA = ((byte) 0x03);
  public static final byte CHAT_STRIP = ((byte) 0x04);
  public static final byte CHAT_RU = ((byte) 0x05);
  public static final byte CHAT_EN = ((byte) 0x06);

  public static final short MAGIC_SITE_FIRST = 0xFE;
  public static final short MAGIC_SITE_SECOND = 0xFD;
  public static final byte SITE_HANDSHAKE = ((byte) 0x0A);
  public static final byte SITE_AUTH = ((byte) 0x0B);
  public static final byte SITE_PROFILE = ((byte) 0x0C);
  public static final byte SITE_STAT = ((byte) 0x0D);
  public static final byte SITE_EXECUTE = ((byte) 0x0E);

}

