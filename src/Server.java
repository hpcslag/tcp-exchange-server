/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author hpcslag
 */
public class Server {
    public static ConcurrentHashMap<String, Socket> actionMapping = new ConcurrentHashMap<String,Socket>();  
    
    public static int onlineCount = 0;
    public void OpenServer(){
        try{
            ServerSocket server = new ServerSocket(3333);
            System.out.println("Server Running in localhost:3333");
            
            //Listen Command , can bordcast
            Thread Commander = new Thread(new Commander());
            Commander.start();
            
            while (true) {
                Socket s = server.accept();
                Server.onlineCount ++;
                actionMapping.put("Client_"+Server.onlineCount,s);
                
                Thread Server = new Thread(new ServerRunner(s));
                Server.start();
            }
            
        }catch(Exception e){
            System.out.println("Server port could be used!");
        }
    }
    
    public static void main(String[] args) {
        Server m = new Server();
        m.OpenServer();
    }
}

class ServerRunner extends Thread{
    Socket s;
    ServerRunner(Socket s){
        this.s = s;
    }
    
    @Override
    public void run(){
        try{
            System.out.println("\n From " + s.getRemoteSocketAddress() + " Client_"+Server.onlineCount+"　Device Connected!");
            Thread MessageListener = new Thread(new MessageListener(s));
            
            MessageListener.start();
            
        }catch(Exception e){
            
        }
        
    }
}

class Commander implements Runnable{
    Socket s;
    
    Commander(){
    }
    
    @Override
    public void run() {
        try{
            Scanner scanner = new Scanner(System.in);
            while(true){
                System.out.print("司令官，請問發送什麼命令? ");
                String command = scanner.nextLine();
                //handler
                switch (command) {
                    case "bordcast":
                        bordcast();
                        break;
                    case "sendMessage":
                        sendMessage();
                        break;
                    default:
                        System.out.println("沒有廣播訊息!");
                }
            }
        }catch(Exception e){
            System.out.println("Writting Failed! " + e);
        }
    }
    
    public void bordcast() throws IOException{
        System.out.println("請問要發送什麼廣播訊息?");
        Scanner scanner = new Scanner(System.in);
        String message = scanner.next();
        for(Entry<String, Socket> entry : Server.actionMapping.entrySet()) {
            String key = entry.getKey();
            Socket s = entry.getValue();
            PrintWriter out = new PrintWriter(new OutputStreamWriter(s.getOutputStream()), true);
            out.println(message);
        }
    }
    
    public void sendMessage() throws IOException{
        System.out.println("請問要發送給誰?");
        Scanner scanner = new Scanner(System.in);
        String id = scanner.next();
        
        System.out.println("發送什麼訊息? ");
        String message = scanner.next();
        
        try{
            Socket s = (Socket)Server.actionMapping.get(id);
            PrintWriter out = new PrintWriter(new OutputStreamWriter(s.getOutputStream()), true);
            out.println(message);
        }catch(Exception e){
            System.out.println("查無此人");
        }
    }
    
}

class MessageListener implements Runnable{
    Socket s;
    
    MessageListener(Socket s){
        this.s = s;
    }
    
    @Override
    public void run() {
        try{
            BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));

            while (true) {
                if(br.ready()){
                    String message = br.readLine();
                    System.out.println("\n From " + s.getRemoteSocketAddress() + " Client_"+Server.onlineCount + " 傳遞的訊息是：" + message);
                    filter(message);
                }
            }
        }catch(Exception e){
            System.err.println(s.getRemoteSocketAddress() + " Disconnected!");
            Server.onlineCount --;
        }
    }
    
    public void filter(String message) throws IOException{
        PrintWriter out = new PrintWriter(new OutputStreamWriter(s.getOutputStream()), true);
        out.println("伺服器正確收到您的訊息。");
    } 
    
}