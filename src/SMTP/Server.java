package SMTP;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.EOFException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Server {
    private static ServerSocket server = null;
    private Socket client = null;
    private static String clientName;
    private static DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
    
    public Server(String ipAddress, int port) throws Exception {
        if (ipAddress != null && !ipAddress.isEmpty()) {
            server = new ServerSocket(port, 1, InetAddress.getByName(ipAddress));
        } else {
            server = new ServerSocket(0, 1, InetAddress.getLocalHost());
        }
    }
    
    private static void print(String message) {
        LocalDateTime now = LocalDateTime.now();
        System.out.println("\r\n[" + dtf.format(now) + "] " + message);
    }
    
    private void waitForConnection() throws Exception {
        print("Waiting for a connection");
        client = server.accept();
        clientName = client.getInetAddress().getHostAddress() + "/" +
                client.getInetAddress().getHostName();
        print("Connected to " + clientName);
    }
    
    private void listen() throws Exception {
        while (true) {
            try {
                waitForConnection();
                ClientHandler clientSocket = new ClientHandler(client);
                new Thread(clientSocket).start();
            } catch (EOFException e) {
                print("Server closed the connection");
            }           
        }
    }
    
    private InetAddress getSocketAddress() {
        return server.getInetAddress();
    }
    
    private int getPort() {
        return server.getLocalPort();
    }
    
    public static void main(String[] args) throws Exception {
     // set the server IP and port number
        String serverIP = "192.168.56.1";
        int port = 7777;
        
        // if arguments have been provided, reassign the server IP and port number
        if (args.length > 0) {
            serverIP = args[0];
            port = Integer.parseInt(args[1]);
        }
        
        // create an object of type Server
        Server server = new Server(serverIP, port);
        print("Running server: " +
                "Host=" + server.getSocketAddress().getHostAddress() +
                " Port=" + server.getPort());
        
        server.listen();
    }
    
    private static class ClientHandler implements Runnable {
        private PrintWriter output;
        private InputStreamReader input;
        private BufferedReader br;
        private Socket client;
        
        public ClientHandler(Socket socket) {
            this.client = socket;
        }
        
        private void setupStreams() throws Exception {
            output = new PrintWriter(client.getOutputStream(), true);
            input = new InputStreamReader(client.getInputStream());
            br = new BufferedReader(input);
            print("Streams are setup");
        }
        
        private void sendMessage(String message) throws Exception {
            output.println(message);
        }
        
        private String handleEmail() throws Exception {
            String email = "";
            String line;
            
            // until the client sends a ".", concatenate the line to email, followed by a newline character
            while (!(line = br.readLine()).equals(".")) {
                email = email + line + "\n";
            }
            
            return email;
        }
        
        private void saveEmail(String sender, String recipient, String email) {
            try {
                // create a FileWriter object, and create a BufferedWriter object from it
                FileWriter fw = new FileWriter(recipient + ".txt");
                BufferedWriter bw = new BufferedWriter(fw);
                
                // write the email to the file and close the BufferedWriter
                bw.write(sender + "\n" + email);
                bw.close();
            } catch (IOException e) {
                // if there was a problem writing to the file
                print("Could not save the email");
            }
        }
        
        private void exchangeMessages() throws Exception {
            String line, sender = "", recipient = "", email = "";
            String[] lineSplitted;
            
            // until the message that is received is "QUIT", keep reading messages from the client
            while (!(line = br.readLine()).equals("QUIT")) {
                print(clientName + ": " + line);
                lineSplitted = line.split(" "); // split the line by spaces so the message can be identified easily
                
                // if the first word is "HELLO"
                if (lineSplitted[0].equals("HELLO") && lineSplitted.length == 2) {
                    // respond by greeting the client
                    sendMessage("250 Hello " + lineSplitted[1] + ", pleased to meet you");
                }
                
                // if the line starts with "MAIL FROM:"
                if (line.startsWith("MAIL FROM:")) {
                    // save the sender's email address and respond with an OK message
                    sender = lineSplitted[2].substring(1, lineSplitted[2].length() - 1);
                    sendMessage("250 ok");
                }
                
                // if the line starts with "RCPT TO:"
                if (line.startsWith("RCPT TO: ")) {
                    // save the recipient's email address and respond with an OK message
                    recipient = lineSplitted[2].substring(1, lineSplitted[2].length() - 1);
                    sendMessage("250 ok");
                }
                
                // if the message is "DATA"
                if (line.equals("DATA")) {
                    sendMessage("354 End data with <CR><LF>.<CR><LF>");
                    email = handleEmail(); // handle the email
                    saveEmail(sender, recipient, email); // save the email to a file
                    sendMessage("250 ok Message accepted for delivery");
                }
            }
        }
        
        private void initiateCommunication() throws Exception {
            sendMessage("220 " + server.getInetAddress().getHostName());
        }
        
        private void farewell() throws Exception {
            sendMessage("221 " + server.getInetAddress().getHostName() + " closing connection");
        }
        
        private void cleanUp() throws Exception {
            print("Closing connection to " + clientName);
            output.close();
            input.close();
            br.close();
            client.close();
        }
        
        @Override
        public void run() {
            try {
                setupStreams();
                initiateCommunication();
                exchangeMessages();
                farewell();
            } catch (EOFException e) {
                print("Server closed the connection");
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    cleanUp();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
