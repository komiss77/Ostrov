package ru.komiss77.modules.netty;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import io.netty.handler.codec.bytes.ByteArrayEncoder;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import ru.komiss77.LocalDB;
import ru.komiss77.Ostrov;


public class OsQuery {

  private static final InetSocketAddress OUT_ADRDRES;
  private static final EventLoopGroup workerGroup; //группа событий, используемая при создании каналов между серверами и клиентом
  protected static final ExecutorService asyncExecutor;
  protected static Channel channel;
  private static final byte[] port;
  private static final byte[] hearBeat;
  private static final byte[] template;
  private static Bootstrap bs;

  static {
    OUT_ADRDRES = new InetSocketAddress("ostrov77.ru", 7777);
    workerGroup = new NioEventLoopGroup();
    asyncExecutor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(), new ThreadFactoryBuilder()
        .setNameFormat("OstrovAuth Async Event Executor - #%d").setDaemon(true).build());
    port = ByteBuffer.allocate(4).putInt(Bukkit.getPort()).array();
    hearBeat = new byte[]{(byte) 0xAB, (byte) 0xCD, QueryCode.HEARTBEAT, port[0], port[1], port[2], port[3]};
    template = new byte[]{(byte) 0xAB, (byte) 0xCD, 0x0, port[0], port[1], port[2], port[3]};
  }


  public OsQuery() {
    bs = new Bootstrap()
        .group(workerGroup)
        //.group(LOCAL_WORKER_GROUP.get()) //Nms
        .channel(NioSocketChannel.class) // TCP
        .handler(new ChannelInitializer<SocketChannel>() { // TCP
          protected void initChannel(SocketChannel socketChannel) throws Exception {
            ChannelPipeline pipi = socketChannel.pipeline();
            pipi.addFirst(new Logging()); //кастомный лог в консоль
            pipi.addLast("timeout", new ReadTimeoutHandler(33)); //если находится ниже любого декодера кадров в вашем конвейере (т. е. ближе к сети), — чтение одного байта сбросит таймер
            //pipi.addFirst(new LoggingHandler(LogLevel.INFO)); //подробный лог в консоль
            //pipi.addLast("decoder", new StringDecoder());//декодирует приходящие данные в строку
            /* 1.DelimiterBasedFrameDecoder использует разделитель для определения пакета;
             2.FixedLengthFrameDecoder использует фиксированную длину для определения пакета;
             3.LengthFieldBasedFrameDecoder и LengthFieldPrepender используются для добавления поля заголовка при отправке данных, поле заголовка содержит длину пакета. */
            pipi.addLast("frameEncoder", new LengthFieldPrepender(2, false)); //https://programmersought.com/article/6426277891/
            pipi.addLast("frameDecoder", new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 2, 0, 2));
            pipi.addLast("decoder", new ByteArrayDecoder());//декодирует приходящие данные в строку
            pipi.addLast("encoder", new ByteArrayEncoder());//кодирует строку в биты при отправке
            //  pipi.addLast("splitter", new LocalFrameDecoder());
            pipi.addLast(new TcpHandler());
          }
        })
    ;

    connect();
  }

  private static void connect() {
    if (channel != null) {
      if (channel.isOpen()) {
        channel.close();
      }
      channel = null;
    }
    try {
      final ChannelFuture future = bs.connect(OUT_ADRDRES).sync();//TCP
      future.addListener(new ChannelFutureListener() { //TCP
        @Override
        public void operationComplete(ChannelFuture future) throws Exception {
          if (future.isSuccess()) {
            Ostrov.log("§bOsQuery §3- соединяемся с " + OUT_ADRDRES.getAddress().getHostAddress() + ":" + OUT_ADRDRES.getPort());
            //future.channel().writeAndFlush(Unpooled.buffer().writeByte(5)); // Здесь перенести данные в ByteBuf
          } else {
            Ostrov.log_err("OsQuery - не удалось начать соединение с " + OUT_ADRDRES.getAddress() + ":" + OUT_ADRDRES.getPort() + " -> " + future.cause());
            future.cause().printStackTrace(System.err);
          }
        }
      });
      channel = future.channel();
    } catch (Exception ex) {
      Ostrov.log_warn("OsQuery init chanel : " + ex.getMessage());
    }
  }

  public static void heartBeat(final int secondCounter) {
    if (channel != null && channel.isOpen()) {
      if (secondCounter % 15 == 0) { //раз в 15сек-подробно
        short memTot = (short) (Runtime.getRuntime().totalMemory() >> 20);
        short memMax = (short) (Runtime.getRuntime().maxMemory() >> 20);
        short memFree = (short) (Runtime.getRuntime().freeMemory() >> 20);
        byte[] data = new byte[]{hearBeat[0], hearBeat[1], hearBeat[2], port[0], port[1], port[2], port[3],
            (byte) Bukkit.getOnlinePlayers().size(), (byte) Bukkit.getMaxPlayers(), (byte) Bukkit.getTPS()[0],
            (byte) ((memTot >> 8) & 0xff), (byte) (memTot & 0xff),
            (byte) ((memMax >> 8) & 0xff), (byte) (memMax & 0xff),
            (byte) ((memFree >> 8) & 0xff), (byte) (memFree & 0xff),
            (byte) ((secondCounter >> 24) & 0xff), (byte) ((secondCounter >> 16) & 0xff), (byte) ((secondCounter >> 8) & 0xff), (byte) (secondCounter & 0xff)
        };
        channel.writeAndFlush(data);
      } else { //каждую секунду - кратко
        channel.writeAndFlush(hearBeat);
      }
    } else {
      //channel.flush();
      //channel.close();
      if (secondCounter % 15 == 0) {
        connect();
      }
    }
//if (channel!=null) Ostrov.log_warn("isOpen?" + channel.isOpen() + " isActive?" + channel.isActive() + " isWritable?" + channel.isWritable()); else Ostrov.log_warn("channel=null");
  }

  public static void send(final byte type, final String data) {
    if (channel == null) return;
    final byte[] db = data.getBytes();
    byte[] b = Arrays.copyOf(template, template.length + db.length);
    System.arraycopy(db, 0, b, template.length, db.length);
    b[2] = type;
    channel.writeAndFlush(b);
  }

  public static void send(final byte type, final String s, @Nullable final Consumer onResponce) {
    if (channel == null) return;
    if (type == QueryCode.CHAT_RU) {
      Ostrov.log("CHAT_RU len=" + s.length());
    }
    final byte[] db = s.getBytes();
    byte[] b = Arrays.copyOf(template, template.length + db.length);
    System.arraycopy(db, 0, b, template.length, db.length);
    b[2] = type;
    channel.writeAndFlush(b);
  }


  public class TcpHandler extends SimpleChannelInboundHandler<byte[]> {
    //@Override
    //public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
    //   super.channelReadComplete(ctx);
    // }
    @Override
    public void channelRead0(ChannelHandlerContext ctx, byte[] data) throws Exception {
      //ByteBuf byteBuf = (ByteBuf) msg;
      //String s = byteBuf.toString(CharsetUtil.UTF_8);//превращаем данные в строку с нужной кодировкой
      final String responce = new String(data);
      if (responce.equals("HB")) {
        //Ostrov.log("§8heartbeat done");
        return;
      }
      final String[] s = responce.split(LocalDB.WORD_SPLIT);
      if (s.length < 2) {
        Ostrov.log_warn("TcpHandler Responce length < 2 : " + responce);
        return;
      }
      final String type = s[0];
      final String sender = s[1];
      final String msg = s.length >= 3 ? s[2] : "";
      final CommandSender cs = sender.equals("CONSOLE") ? Bukkit.getConsoleSender() : Bukkit.getPlayerExact(sender);
      if (cs == null) {
        Ostrov.log_warn("TcpHandler Responce CommandSender == null");
        return;
      }
      if (type.equals("TEXT")) {
        cs.sendMessage(msg);
        return;
      } else if (type.equals("COMPONENT")) {
        cs.sendMessage(MiniMessage.miniMessage().deserialize(msg));
        return;
      }
      Ostrov.log("Responce =" + responce);
      //int idx = responce.indexOf(LocalDB.W_SPLIT);
      //if (idx>0) {
      //  final String sender = data_string.substring(0, idx);
      //  final String cmd = data_string.substring( idx+1);
      // } else {
      //}
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {//Функция обработок ошибок
      Ostrov.log_warn("OsQuery TcpHandler exсept : " + cause);
      //cause.printStackTrace();
    }
    //@Override
    //public void channelActive(ChannelHandlerContext ctx) throws Exception {
    //  super.channelActive(ctx);
    //}
  }


  public class Logging extends LoggingHandler {
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
      Ostrov.log_warn("OsQuery Logging exceptionCaught cause=" + cause);
    }
  }

  public void shutdown() {
    workerGroup.shutdownGracefully();
  }


}






           /*.channel(NioDatagramChannel.class) //Datagram = UDP
          .handler(new ChannelInitializer<Channel>() { // UDP такой хандлер из примера в инете
             @Override
             protected void initChannel(Channel channel) {
               ChannelPipeline pipi = channel.pipeline();
               pipi.addFirst(new Logging()); //кастомный лог в консоль
               //pipi.addFirst(new LoggingHandler(LogLevel.INFO)); //подробный лог в консоль
               //pipi.addLast("frameDecoder", new DelimiterBasedFrameDecoder(80960, Delimiters.lineDelimiter()));
               pipi.addLast("decoder", new StringDecoder());//декодирует приходящие данные в строку
               //pipi.addLast("encoder", new StringEncoder());//кодирует строку в биты при отправке
               pipi.addLast("encoder", new ByteArrayEncoder());//кодирует строку в биты при отправке
               pipi.addLast(new UdpResponceHandler());
             }
           })*/

      /*final ChannelFuture future = bs.connect(OUT_ADRDRES); //UDP
      future.addListener(new ChannelFutureListener() { //UDP
        @Override
        public void operationComplete(ChannelFuture future) throws Exception {
          if (future.isSuccess()) {
            Ostrov.log_ok("OsQuery - подключились к "+OUT_ADRDRES.getAddress().getHostAddress()+":"+OUT_ADRDRES.getPort());
            //future.channel().writeAndFlush(Unpooled.buffer().writeByte(5)); // Здесь перенести данные в ByteBuf
          } else {
            Ostrov.log_err("OsQuery - не удалось подключиться к "+OUT_ADRDRES.getAddress()+":"+OUT_ADRDRES.getPort()+ " -> "+future.cause());
            future.cause().printStackTrace(System.err);
          }
        }
      });
      channel = (NioDatagramChannel) future.channel();*/

/*
  public class UdpResponceHandler extends SimpleChannelInboundHandler<DatagramPacket> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket data) throws Exception {
      ByteBuf byteBuf = data.content();//получаем данные из датаграммы
      String in = byteBuf.toString(CharsetUtil.UTF_8);//превращаем данные в строку с нужной кодировкой

Ostrov.log("ResponceHandler in="+in);

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {//Функция обработок ошибок
Ostrov.log_warn("OsQuery UdpResponceHandler cause : "+cause);
      //cause.printStackTrace();
    }

  }

 */


//кусочек из net.minecraft.network.Connection
//автовыбор EpollSocketChannel или NioSocketChannel
//Class<? extends SocketChannel> clazz; //ServerSocketChannel для сервера сокетов и SocketChannel для клиента сокетов
//EventLoopGroup eventLoopGroup;
// if (Epoll.isAvailable() ) {// && useEpollIfAvailable) {
//   clazz = EpollSocketChannel.class;
//    eventLoopGroup = NETWORK_EPOLL_WORKER_GROUP.get();
//  } else {
//    clazz = NioSocketChannel.class;
//    eventLoopGroup = NETWORK_WORKER_GROUP.get();
//  }
//----------------------------
//.channelFactory(transportType.datagramChannelFactory) //UDP как в прокси query
//.channel(NioSocketChannel.class)//.channel(clazz) //TCP выше автовыбор EpollSocketChannel или NioSocketChannel
//handler вызывается при каждом подключении, говоря системе о том, что будет использовано для обработки сообщений
           /* TCP .handler(new ChannelInitializer<SocketChannel>() {
             protected void initChannel(SocketChannel socketChannel) throws Exception {
               ChannelPipeline pipi = socketChannel.pipeline();
               //pipi.addFirst(new LoggingHandler(LogLevel.INFO)); //подробный лог в консоль
               pipi.addLast("decoder", new StringDecoder());//декодирует приходящие данные в строку
               pipi.addLast("encoder", new StringEncoder());//кодирует строку в биты при отправке
               pipi.addLast(new TcpResponceHandler());
             }
           })*/
//ChannelFuture f = bs.bind(port).sync();
//f.channel().closeFuture().sync();
//bs.bind(64000);


//FastDataObject fdo = new FastDataObject(in);//создаём объект с данными
//String action = fdo.getParameter("action");//получаем событие, для которого создадим пакет обработчик
//Packet packet = PacketManager.getPacket(action);//Создаем пакет-обработчик
//System.out.println("Packet "+ action +" created");
//packet.setChannel(ctx);//сюда кидаем  объект, с помощью которого будем отправлять обратно
//try {
//  packet.handle(fdo);//Обрабатываем пакет, задавая аргументом наши данные
//} catch (IOException ex) {
//  ex.printStackTrace();
//}


/*
  public class TcpResponceHandler extends SimpleChannelInboundHandler<Object> {
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
      super.channelReadComplete(ctx);
    }
    @Override
    public void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
      ByteBuf byteBuf = (ByteBuf) msg;
      String s = byteBuf.toString(Charset.defaultCharset());
      Ostrov.log("ResponceHandler in="+s);
    }
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
      super.channelActive(ctx);
    }
  }
 */