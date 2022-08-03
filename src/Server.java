//Server.java

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.ServerError;
import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements Runnable{
    // server side
    private ArrayList<ConnectionHandler> connections;
    private ServerSocket server;
    private boolean done;
    private ExecutorService pool;


    public Server(){
        connections = new ArrayList<>();
        done = false;
    }


    @Override
    public void run() {
        try{
            this.server = new ServerSocket(9999);
            pool = Executors.newCachedThreadPool();
            while(!done) {
                Socket client = server.accept();
                ConnectionHandler handler = new ConnectionHandler(client);
                connections.add(handler);
                pool.execute(handler);
            }
        }catch (IOException e){
            e.printStackTrace();
            shutDown();
        }
    }
    // send message
    public void broadcast(String message){
        for (ConnectionHandler ch : connections){
            if(ch != null){
                ch.sendMessage(message);
            }
        }
    }

    public void shutDown(){
        done = true;
        pool.shutdown();
        try{
            if (!server.isClosed()){
                server.close();
            }
            for (ConnectionHandler ch : connections){
                ch.shutdown();
            }
            System.out.println("===Server shutdown===");
        }catch (IOException e){

        }

    }







    class ConnectionHandler implements Runnable{
        private Socket client;
        private BufferedReader in;
        private PrintWriter out;
        private String name;

        public ConnectionHandler(Socket client){
            this.client = client;
        }


        @Override
        public void run(){
            try {
                out = new PrintWriter(client.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                out.println("Please enter your name: ");
                name = in.readLine();
                System.out.println(name + " connected");
                broadcast(name + " has joined the chat");
                String userMessage;
                while ((userMessage = in.readLine()) != null){
                    if (userMessage.startsWith("/nick")){
                        String[] messageSplit = userMessage.split(" ", 2);
                        if (messageSplit.length == 2){
                            broadcast(name + " has changed nickname to " + messageSplit[1]);
                            System.out.println(name + " has changed nickname to " + messageSplit[1]);
                            this.name = messageSplit[1];
                            out.println("Successfully renamed to " + name);
                        }else {
                            System.out.println("No name has been entered");
                        }
                    }else if (userMessage.startsWith("/quit")){
                        broadcast(name + "left the chatroom");
                        shutdown();
                    }else{
                        broadcast(name + ": " + userMessage);
                    }
                }
            }catch(IOException e){
                shutdown();
            }

        }

        public void sendMessage(String message){
            out.println(message);
        }

        public void shutdown() {
            try{
                in.close();
                out.close();
                if(!client.isClosed()){
                    client.close();
                }
            }catch (IOException e){

            }
        }
    }

    public static void main(String[] args) {
        Server server = new Server();
        System.out.println("===server initialized, running...===");
        server.run();

    }
}
