package com.github.lengl.net.nio;

import com.github.lengl.Messages.ClientMessageService;
import com.github.lengl.Messages.ClientMessages.ShutdownMessage;
import com.github.lengl.Messages.InputHandler;
import com.github.lengl.Messages.Message;
import org.apache.commons.lang3.SerializationUtils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class NioClient {

  static Logger log = Logger.getLogger(NioClient.class.getName());


  public static final int PORT = 8123;

  private Selector selector;
  private SocketChannel channel;
  private ByteBuffer buffer = ByteBuffer.allocate(8192);
  public Thread mainThread;
  public Thread consoleThread;

  BlockingQueue<String> queue = new ArrayBlockingQueue<>(100);

  private InputHandler inputHandler = new ClientMessageService();


  public void init() throws Exception {

    mainThread = Thread.currentThread();
    // Слушаем ввод данных с консоли
    consoleThread = new Thread(() -> {
      Scanner scanner = new Scanner(System.in);
      while (!Thread.currentThread().isInterrupted()) {
        String line = scanner.nextLine();
        if ("/quit".equals(line)) {
          log.info("Exit!");
          mainThread.interrupt();
          System.exit(0);
        }

        try {
          queue.put(line);
        } catch (InterruptedException e) {
          log.log(Level.SEVERE, "Tried to put line in queue: ", e);
        }

        // Будим селектор
        SelectionKey key = channel.keyFor(selector);
        log.info("wake up: " + key.hashCode());
        key.interestOps(SelectionKey.OP_WRITE);
        selector.wakeup();
      }
    });
    consoleThread.start();


    selector = Selector.open();
    channel = SocketChannel.open();
    channel.configureBlocking(false);
    channel.connect(new InetSocketAddress("localhost", PORT));
    channel.register(selector, SelectionKey.OP_CONNECT);

    while (!Thread.currentThread().isInterrupted()) {
      //log.info("Waiting on select()...");
      int num = selector.select();
      //log.info("Raised " + num + " events");


      Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
      while (keyIterator.hasNext()) {
        SelectionKey sKey = keyIterator.next();
        keyIterator.remove();

        if (!sKey.isValid()) {
          continue;
        }

        if (sKey.isConnectable()) {
          log.info("[connectable] " + sKey.hashCode());

          try {
            channel.finishConnect();
          } catch (Exception e) {
            //TODO: log it!
            System.err.println("Unable to connect server");
            System.exit(0);
          }
          System.out.println("Connected successfully!");

          // теперь в канал можно писать
          sKey.interestOps(SelectionKey.OP_READ);
        } else if (sKey.isReadable()) {
          log.info("[readable]");

          buffer.clear();
          int numRead = channel.read(buffer);
          if (numRead < 0) {
            break;
          }
          //log.info("From server: " + new String(buffer.array()));
          Message msg = (Message) SerializationUtils.deserialize(buffer.array());
          System.out.printf("%s: %s\n", msg.getAuthor(), msg.getBody());
          if (msg instanceof ShutdownMessage) {
            consoleThread.interrupt();
            System.exit(0);
          }

        } else if (sKey.isWritable()) {
          log.info("[writable]");

          String line = queue.poll();
          if (line != null) {
            Message msg = inputHandler.react(new Message(line));
            if (msg != null) {
              byte[] userInput = SerializationUtils.serialize(msg);
              channel.write(ByteBuffer.wrap(userInput));
              // Ждем записи в канал
              sKey.interestOps(SelectionKey.OP_READ);
            }
          }
        }
      }
    }
  }

  public static void main(String[] args) throws Exception {
    try {
      LogManager.getLogManager().readConfiguration(
          NioClient.class.getResourceAsStream("/logging.properties"));
    } catch (IOException e) {
      System.err.println("Logger error. Please restart the client.");
      System.exit(0);
    }
    NioClient client = new NioClient();
    client.init();
  }
}
