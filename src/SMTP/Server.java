package SMTP;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private ServerSocket server;
    private PrintWriter output;
    private InputStreamReader input;
    private BufferedReader br;
    private Socket client;
    
    public Server(String ipAddress, int port) throws Exception {
        if (ipAddress != null && !ipAddress.isEmpty()) {
            this.server = new ServerSocket(port, 1, InetAddress.getByName(ipAddress));
        } else {
            this.server = new ServerSocket(0, 1, InetAddress.getLocalHost());
        }
    }
    
    private void waitForConnection() throws Exception {
        System.out.println("\r\nWaiting for a connection");
        client = server.accept();
        System.out.println("\r\nConnected to " + client.getInetAddress().getHostName());
    }
    
    private void setupStreams() throws Exception {
        output = new PrintWriter(client.getOutputStream(), true);
        input = new InputStreamReader(client.getInputStream());
        br = new BufferedReader(input);
        System.out.println("\r\nStreams are setup");
    }
    
    private void sendMessage(String message) throws Exception {
        output.println(message);
    }
    
    private void initiateCommunication() throws Exception {
        sendMessage("220 " + server.getInetAddress().getHostName());
    }
    
    private void farewell() throws Exception {
        sendMessage("221 " + server.getInetAddress().getHostName() + " closing connection");
    }
    
    private void handleData() throws Exception {
        String message = "";
        String line;
        
        while (!(line = br.readLine()).equals(".")) {
            System.out.println("\r\nClient: " + line);
            message = message + line;
        }
    }
    
    private void exchangeMessages() throws Exception {
        String line;
        String[] lineSplitted;
        
        while (!(line = br.readLine()).equals("QUIT")) {
            System.out.println("\rClient: " + line);
            lineSplitted = line.split(" ");
            
            if (lineSplitted[0].equals("HELLO") && lineSplitted.length == 2) {
                sendMessage("250 Hello " + lineSplitted[1] + ", pleased to meet you");
            } else if (line.startsWith("MAIL FROM:")) {
                // handle email: lineSplitted[2].substring(1, lineSplitted[2].length() - 1)
                sendMessage("250 ok");
            } else if (line.startsWith("RCPT TO: ")) {
                // handle email: lineSplitted[2].substring(1, lineSplitted[2].length() - 1)
                sendMessage("250 ok");
            } else if (line.equals("DATA")) {
                sendMessage("354 End data with <CR><LF>.<CR><LF>");
                handleData();
                sendMessage("250 ok Message accepted for delivery");
            }
        }
    }
    
    private void cleanUp() throws Exception {
        System.out.println("\r\nClosing connection");
        output.close();
        input.close();
        client.close();
    }
    
    private void listen() throws Exception {
        
//        while (true) {
            try {
                waitForConnection();
                setupStreams();
                initiateCommunication();
                exchangeMessages();
                farewell();
            } catch (EOFException e) {
                System.out.println("\r\nServer closed the connection");
            } finally {
                cleanUp();
            }
//        }
    }
    
    public InetAddress getSocketAddress() {
        return this.server.getInetAddress();
    }
    
    public int getPort() {
        return this.server.getLocalPort();
    }
    
    public static void main(String[] args) throws Exception {
        // set the server IP and port number
        String serverIP = "192.168.56.1"; // local IP address
        int port = 7777;
        
        if (args.length > 0) {
            serverIP = args[0];
            port = Integer.parseInt(args[1]);
        }
        
        // call the constructor and pass the IP and port
        Server server = new Server(serverIP, port);
        System.out.println("\r\nRunning server: " +
                "Host=" + server.getSocketAddress().getHostAddress() +
                " Port=" + server.getPort());
        
        server.listen();
    }
}
