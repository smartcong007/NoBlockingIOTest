package base;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;

/**
 * Created by zhengcong on 2017/7/3.
 */
public class SocketServer {

    private static Integer port = 3138;


    public static void start(){
        Selector selector = null;
        ServerSocketChannel serverSocketChannel = null;
        System.out.println("start to listen to port "+port);
        try {
            serverSocketChannel = ServerSocketChannel.open();
            ServerSocket serverSocket = serverSocketChannel.socket();
            serverSocket.bind(new InetSocketAddress(port));
            selector = Selector.open();
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);  //把ServerSocket对应的channel注册到选择器上，准备接收客户端的接入
            while (true){

                int n = selector.select();
                if(n == 0){
                    System.out.println("==");
                    continue;
                }
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                while (iterator.hasNext()){
                    SelectionKey key = iterator.next();
                    if(key.isAcceptable()){    //处理刚接入的客户端channel
                        ServerSocketChannel server = (ServerSocketChannel) key.channel();
                        SocketChannel channel = server.accept();
                        answer(channel);    //对客户端进行应答
                        registerChannel(key.selector(),channel,SelectionKey.OP_READ);  //准备好从客户端channel读取数据

                    }

                    if (key.isReadable()){

                        key.attach(ByteBuffer.allocateDirect(1024));
                        readDataFromSocket(key);


                    }

                    iterator.remove();
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                if(selector != null){
                    selector.close();
                }
                if(serverSocketChannel != null){
                    serverSocketChannel.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
    public static void registerChannel(Selector selector, SelectableChannel channel, int operation){ //将当前通道的特定操作注册到选择器上

        if (channel == null){
            return;
        }
        try {
            channel.configureBlocking(false);
            channel.register(selector,operation);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void readDataFromSocket(SelectionKey key){     //从客户端channel读取数据

        try {
            SocketChannel socketChannel = (SocketChannel) key.channel();
            ByteBuffer buf = (ByteBuffer) key.attachment();
            long count = socketChannel.read(buf);
            while (count>0){

                buf.flip();
                while (buf.hasRemaining()){
                    System.out.print((char)buf.get());
                }
                System.out.println();
                buf.clear();
                count = socketChannel.read(buf);

            }

            if(count<0){
                socketChannel.close();   //从客户端channel读取完数据后记得立即关闭channel
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void answer(SocketChannel channel){    //作简单的客户端应答

        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        byteBuffer.clear();
        byteBuffer.put("Server has received the request from client".getBytes());
        byteBuffer.flip();
        try {
            while (byteBuffer.hasRemaining()) {
                channel.write(byteBuffer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {

        start();

    }


}

