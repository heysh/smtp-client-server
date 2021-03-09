package SMTP;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private ServerSocket server;
    
    public Server(String ipAddress, int port) throws Exception {
        if (ipAddress != null && !ipAddress.isEmpty()) {
            this.server = new ServerSocket(port, 1, InetAddress.getByName(ipAddress));
        } else {
            this.server = new ServerSocket(0, 1, InetAddress.getLocalHost());
        }
    }
    
    private void listen() throws Exception {
        // listen to incoming client's requests via the ServerSocket
        String data = null;
        Socket client = this.server.accept();
        String clientAddress = client.getInetAddress().getHostAddress();
        System.out.println("\r\nNew client connection from " + clientAddress);
        
        // print received datagrams from client
        BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
        while ((data = in.readLine()) != null) {
            System.out.println("\r\nMessage from " + clientAddress + ": " + data);
            client.sendUrgentData(1);
        }
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
