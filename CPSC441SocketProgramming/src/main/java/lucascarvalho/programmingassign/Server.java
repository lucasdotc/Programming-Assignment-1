package lucascarvalho.programmingassign;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;

import static lucascarvalho.programmingassign.Server.sendMsgToClients;

public class Server {

    private static int clientCount = 0; //tracks number of connected clients
    private static final int MAX_CLIENTS = 3; //maximum number of clients allowed
    private static int port = 1234;
    public static ArrayList<ClientHandler> clients = new ArrayList<>(); //list to keep track of connected clients
    public static HashMap<String, ClientSession> clientSessions = new HashMap<>(); //list to keep track of client sessions and join/exit times
    public static void main(String[] args) throws IOException {

        Socket socket = null;

        ServerSocket serverSocket = new ServerSocket(port); //initialize server socket
        System.out.println("Server is now running on port " + port);

        while (true) {
            try {
                socket = serverSocket.accept();
                //tests to see if maximum number of clients has been reached
                if (clientCount >= MAX_CLIENTS) {
                    try (BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))) {//using buffered writer to wrap output stream writer for efficiency
                        bufferedWriter.write("Server is full. Try again later.");
                        bufferedWriter.newLine();
                        bufferedWriter.flush();
                    }    
                    socket.close();
                    continue; //skip the rest of the loop and wait for another client, but don't kill the server
                }
                clientCount++; //increment client count
                String clientName = "Client " + clientCount;
                ClientHandler clientHandler = new ClientHandler(socket, clientName); //creates a new ClientHandler for the connected client, which will handle client actions on the server side
                clients.add(clientHandler);
                new Thread(clientHandler, clientName + " Thread").start(); //starts a new thread for the current client, allowing multiple clients to connect simultaneously (multithreading)
                ClientSession clientSession = new ClientSession(clientName, Instant.now());
                clientSessions.put(clientName, clientSession);

                System.out.println(clientName + " has joined the chat."); 

            } catch (IOException e) {
                e.printStackTrace();
                }
        }
    }

    //general function that sends message to all connected clients except the sender
    static void sendMsgToClients (String msg, ClientHandler sender) {
        for (ClientHandler ch : Server.clients) {
            if (ch != sender){
                ch.sendMsg(msg);
            }
        }
    }

    static void disconnectClient(ClientHandler client) {
        clients.remove(client);
        ClientSession session = clientSessions.get(client.getClientName());
        session.setExitTime(Instant.now());
        clientCount--;
    }

    

}
//class to handle client actions on the server side
class ClientHandler implements Runnable {
    private Socket socket;
    private String clientName;

    public ClientHandler(Socket socket, String clientName) {
        this.socket = socket;
        this.clientName = clientName;
    }

    public String getClientName() {
        return clientName;
    }

    @Override
    public void run(){
        // Handle client communication here
        InputStreamReader input = null; //read data from client
        OutputStreamWriter output = null;
        BufferedReader bufferedReader = null; //wraps the InputStreamReader to make input more efficient
        BufferedWriter bufferedWriter = null; //wraps the OutputStreamReader to make output more efficient
        String name = "";
                

                try {
                    input = new InputStreamReader(socket.getInputStream());
                    bufferedReader = new BufferedReader(input);
                    bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                    sendMsg("Enter your name: ");
                    name = bufferedReader.readLine();
                    sendMsg("Welcome " + name + "! You can start sending messages. Type 'exit' to leave.");
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    String msg;

                    while ((msg = bufferedReader.readLine()) != null) {
                        if (msg.trim().equalsIgnoreCase("exit")) {
                            Server.disconnectClient(this);
                            break;
                        }
                        sendMsgToClients(name + ": " + msg, this);
                        System.out.println("Received -> " + name + ": " + msg);
                        sendMsg("ACK: "+ msg);
                    }

                    
                    
                    
                } catch (Exception e) {
                    e.printStackTrace();
                }               
                
            }
            //function to streamline writing messages thru the BufferedWriter
            void sendMsg (String msg) {
                try {
                    OutputStreamWriter output = new OutputStreamWriter(this.socket.getOutputStream());
                    BufferedWriter writer = new BufferedWriter(output);
                    writer.write(msg);
                    writer.newLine();
                    writer.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            
    }
    //keeping track of the clients join and exit times
    class ClientSession{
        private String clientName;
        private Instant joinTime;
        private Instant exitTime;

        public ClientSession(String clientName, Instant joinTime) {
            this.clientName = clientName;
            this.joinTime = joinTime;
        }

        public String getClientName() {
            return clientName;
        }

        public void setExitTime(Instant exitTime) {
            this.exitTime = exitTime;
        }
    }