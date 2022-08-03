import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

// Client.java
public class Client implements Runnable{

    private Socket client;
    private BufferedReader in;
    private PrintWriter out;
    private boolean done;
    @Override
    public void run() {
        try {
            client = new Socket("192.168.0.25", 9999);
            out = new PrintWriter(client.getOutputStream(),true);
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            InputHandler inHandler = new InputHandler();
            Thread t = new Thread(inHandler);
            t.start();
            String inMessage;
            while ((inMessage = in.readLine()) != null){
                System.out.println(inMessage);
            }
        }catch (IOException e){
            shutdown();
        }
    }
    public void shutdown(){
        done = true;
        try{
            System.out.println("You have left the chatroom");
            in.close();
            out.close();
            if(!client.isClosed()){
                client.close();
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    class InputHandler implements Runnable{
        @Override
        public void run(){
            try {
                BufferedReader inReader = new BufferedReader((new InputStreamReader(System.in)));
                while(!done){
                    String message = inReader.readLine();
                    if (message.startsWith("/quit")){
                        out.println("/quit");
                        shutdown();
                    }else{
                        out.println(message);
                    }
                }
            }catch (IOException e){
                shutdown();
            }
        }
    }

    public static void main(String[] args) {
        Client client = new Client();
        System.out.println("===client initialized, running...===");
        client.run();
        System.out.println("===client shutdown===");
    }
}
