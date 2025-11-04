package lucascarvalho.programmingassign;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) throws IOException {

        Socket socket = null;
        InputStreamReader input = null;
        OutputStreamWriter output = null;
        

        socket = new Socket("localhost", 1234);

        input = new InputStreamReader(socket.getInputStream());
        output = new OutputStreamWriter(socket.getOutputStream());

        BufferedReader bufferedReader = new BufferedReader(input);
        BufferedWriter bufferedWriter = new BufferedWriter(output);

        Scanner scanner = new Scanner(System.in);

        String serverMsg = bufferedReader.readLine(); 
        System.out.println("Server: " + serverMsg); //read server prompt, server is asking for name
        String name = scanner.nextLine();
        bufferedWriter.write(name);
        bufferedWriter.newLine();
        bufferedWriter.flush();

        serverMsg = bufferedReader.readLine();
        System.out.println("Server: " + serverMsg); //welcomes the client

        //thread works in the background fetching messages from other clients
        Thread listenThread = new Thread(() -> {
            String msgFromServer;

            try {
                while ((msgFromServer = bufferedReader.readLine()) != null) {
                    System.out.println(msgFromServer);
                    System.out.println("Enter message: "); //display this prompt again once a message is received for clarity
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        listenThread.setDaemon(true);
        listenThread.start(); //client is connected, so start listening for messages from other clients
        

        while (true) {
            System.out.println("Enter message: ");
            String msg = scanner.nextLine(); //read user input

            bufferedWriter.write(msg); //writes message to the server
            bufferedWriter.newLine();
            bufferedWriter.flush();

            if (msg.equalsIgnoreCase("exit")) {
                break;
            }

        }
        
        
    }
}