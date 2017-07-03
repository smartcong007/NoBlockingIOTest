package base;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.TimeUnit;

/**
 * Created by zhengcong on 2017/7/3.
 */
public class SocketClient {

    public static void client(){
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        SocketChannel socketChannel = null;
        try
        {
            socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(false);
            socketChannel.connect(new InetSocketAddress(3138));  //连接到指定的服务端channel

            if(socketChannel.finishConnect())
            {
                TimeUnit.SECONDS.sleep(2);  //虽然客户端channel已经和服务端channel连接上了，但是服务端应答会有延时，所以线程务必先等待2秒
                long buf  = socketChannel.read(buffer);
                while (buf > 0){
                    buffer.flip();
                    while (buffer.hasRemaining()){
                        System.out.print((char)buffer.get());
                    }
                    System.out.println();
                    buf = socketChannel.read(buffer);
                }

                int i=0;
                while(true)
                {
                    TimeUnit.SECONDS.sleep(1);
                    String info = "I'm "+i+++"-th information from client"; //一定要保证要写入的字节数不要超过buffer的最大容量
                    buffer.clear();
                    buffer.put(info.getBytes());
                    buffer.flip();
                    while(buffer.hasRemaining()){
                        System.out.println("第"+i+"次回话发起");
                        socketChannel.write(buffer);
                    }



                }
            }
        }
        catch (IOException | InterruptedException e)
        {
            e.printStackTrace();
        }
        finally{
            try{
                if(socketChannel!=null){
                    socketChannel.close();
                }
            }catch(IOException e){
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        client();
    }

}

