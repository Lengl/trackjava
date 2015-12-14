package com.github.lengl.net.nio;

import com.github.lengl.Messages.ClientMessages.ShutdownMessage;
import com.github.lengl.Messages.InputHandler;
import com.github.lengl.Messages.ServerMessageService;
import com.github.lengl.net.Resources;
import com.github.lengl.net.Server;
import org.apache.commons.lang3.SerializationUtils;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 *
 */
public class NioServer implements Server {

  // The host:port combination to listen on
  private InetAddress hostAddress;
  private int port;

  // The channel on which we'll accept connections
  private ServerSocketChannel serverChannel;

  // The selector we'll be monitoring
  private Selector selector;

  // The buffer into which we'll read data when it's available
  private ByteBuffer readBuffer = ByteBuffer.allocate(8192);

  private final ExecutorService pool = Executors.newFixedThreadPool(3);

  // A list of ChangeRequest instances
  private final List<ChangeRequest> changeRequests = new LinkedList<>();

  // Maps a SocketChannel to a list of ByteBuffer instances
  private final Map<SocketChannel, List> pendingData = new HashMap<>();

  //Map <authorizedUserID, SocketChannel>
  public final Map<Long, SocketChannel> authorisedClients = new HashMap<>();
  public final Map<SocketChannel, InputHandler> inputHandlers = new HashMap<>();
  public final Resources resources;
  public Thread mainThread;


  public NioServer(InetAddress hostAddress, int port) throws Exception {
    resources = new Resources();
    this.hostAddress = hostAddress;
    this.port = port;
    selector = initSelector();
  }

  public static void main(String[] args) {
    try {
      Server server = new NioServer(null, 8123);
      server.startServer();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private Selector initSelector() throws IOException {
    // Create a new selector
    Selector socketSelector = SelectorProvider.provider().openSelector();

    // Create a new non-blocking server socket channel
    serverChannel = ServerSocketChannel.open();
    serverChannel.configureBlocking(false);

    // Bind the server socket to the specified address and port
    InetSocketAddress isa = new InetSocketAddress(this.hostAddress, this.port);
    serverChannel.socket().bind(isa);

    // Register the server socket channel, indicating an interest in
    // accepting new connections
    serverChannel.register(socketSelector, SelectionKey.OP_ACCEPT);

    return socketSelector;
  }

  @Override
  public void startServer() {
    mainThread = Thread.currentThread();
    //listening to console
    new Thread(() -> {
      Scanner scanner = new Scanner(System.in);
      while (true) {
        String line = scanner.nextLine();
        if ("/stop".equals(line)) {
          destroyServer();
          System.exit(0);
        }
      }
    }).start();
    while (!Thread.currentThread().isInterrupted()) {
      try {
        // Process any pending changes
        synchronized (changeRequests) {
          Iterator changes = this.changeRequests.iterator();
          while (changes.hasNext()) {
            ChangeRequest change = (ChangeRequest) changes.next();
            switch (change.type) {
              case ChangeRequest.CHANGEOPS:
                SelectionKey key = change.socket.keyFor(this.selector);
                key.interestOps(change.ops);
            }
          }
          this.changeRequests.clear();
        }

        // Wait for an event one of the registered channels
        this.selector.select();

        // Iterate over the set of keys for which events are available
        Iterator selectedKeys = this.selector.selectedKeys().iterator();
        while (selectedKeys.hasNext()) {
          SelectionKey key = (SelectionKey) selectedKeys.next();
          selectedKeys.remove();

          if (!key.isValid()) {
            continue;
          }

          // Check what event is available and deal with it
          if (key.isAcceptable()) {
            accept(key);
          } else if (key.isReadable()) {
            read(key);
          } else if (key.isWritable()) {
            write(key);
          }
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  public void send(SocketChannel socket, byte[] data) {
    synchronized (changeRequests) {
      // Indicate we want the interest ops set changed
      changeRequests.add(new ChangeRequest(socket, ChangeRequest.CHANGEOPS, SelectionKey.OP_WRITE));

      // And queue the data we want written
      synchronized (pendingData) {
        List queue = (List) pendingData.get(socket);
        if (queue == null) {
          queue = new ArrayList();
          pendingData.put(socket, queue);
        }
        queue.add(ByteBuffer.wrap(data));
      }
    }

    // Finally, wake up our selecting thread so it can make the required changes
    this.selector.wakeup();
  }

  private void accept(SelectionKey key) throws IOException {
    // For an accept to be pending the channel must be a server socket channel.
    ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();

    // Accept the connection and make it non-blocking
    SocketChannel socketChannel = serverSocketChannel.accept();
    Socket socket = socketChannel.socket();
    inputHandlers.put(socketChannel, new ServerMessageService(resources));
    socketChannel.configureBlocking(false);

    // Register the new SocketChannel with our Selector, indicating
    // we'd like to be notified when there's data waiting to be read
    socketChannel.register(selector, SelectionKey.OP_READ);
  }

  void removeClientTrace(SocketChannel channel) {
    inputHandlers.remove(channel);
    synchronized (authorisedClients) {
      if (authorisedClients.values().contains(channel)) {
        authorisedClients.values().remove(channel);
      }
    }
  }

  private void read(SelectionKey key) throws IOException {
    SocketChannel socketChannel = (SocketChannel) key.channel();

    // Clear out our read buffer so it's ready for new data
    readBuffer.clear();

    // Attempt to read off the channel
    int numRead;
    try {
      numRead = socketChannel.read(readBuffer);
    } catch (IOException e) {
      // The remote forcibly closed the connection, cancel
      // the selection key and close the channel.
      key.cancel();
      removeClientTrace(socketChannel);
      socketChannel.close();
      return;
    }

    if (numRead == -1) {
      // Remote entity shut the socket down cleanly. Do the
      // same from our end and cancel the channel.
      removeClientTrace(socketChannel);
      key.channel().close();
      key.cancel();
      return;
    }

    // Hand the data off to our worker thread
    pool.submit(new MyWorker(this, socketChannel, this.readBuffer.array(), numRead));
  }

  private void write(SelectionKey key) throws IOException {
    SocketChannel socketChannel = (SocketChannel) key.channel();

    synchronized (pendingData) {
      List queue = (List) pendingData.get(socketChannel);

      // Write until there's not more data ...
      while (!queue.isEmpty()) {
        ByteBuffer buf = (ByteBuffer) queue.get(0);
        socketChannel.write(buf);
        if (buf.remaining() > 0) {
          // ... or the socket's buffer fills up
          break;
        }
        queue.remove(0);
      }

      if (queue.isEmpty()) {
        // We wrote away all data, so we're no longer interested
        // in writing on this socket. Switch back to waiting for
        // data.
        key.interestOps(SelectionKey.OP_READ);
      }
    }
  }

  public void destroyServer() {
    //TODO: Not working properly.
    byte[] shutdownMsg = SerializationUtils.serialize(new ShutdownMessage());
    inputHandlers.keySet().stream().forEach(channel -> {
      pool.submit(new MyWorker(this, channel, shutdownMsg, shutdownMsg.length));
    });
    pool.shutdown();
    try {
      if (!pool.awaitTermination(30, TimeUnit.SECONDS)) {
        pool.shutdownNow();
        if (!pool.awaitTermination(30, TimeUnit.SECONDS))
          System.err.println("Pool did not terminate");
      }
    } catch (InterruptedException ie) {
      pool.shutdownNow();
      Thread.currentThread().interrupt();
    }
    mainThread.interrupt();
    selector.selectedKeys().forEach(selectionKey -> {
      try {
        selectionKey.channel().close();
      } catch (IOException ignored) {
        //TODO: Log it!
      }
      selectionKey.cancel();
    });
    try {
      selector.close();
    } catch (IOException ignored) {
    }
    resources.close();
  }
}
