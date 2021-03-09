package SMTP;

import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private Socket socket;
    private InetAddress serverAddress;
    private int serverPort;
    private Scanner scanner;
    
    private Client(InetAddress serverAddress, int serverPort) throws Exception {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        
        // initiate the connection with the server using Socket
        // create a stream socket and connect it to the specified port number at the specified IP address
        this.socket = new Socket(this.serverAddress, this.serverPort);
        this.scanner = new Scanner(System.in);
    }
    
    private void start() throws Exception {
        String input;
        
        // create a new PrintWriter from an existing OutputStream (i.e., socket)
        // this convenience constructor creates the necessary intermediate OutputStreamWriter,
        // which will convert characters into bytes using the default character encoding
        while (true) {
            input = scanner.nextLine();
            PrintWriter output = new PrintWriter(this.socket.getOutputStream(), true);
            output.println(input);
            output.flush();
        }
    }
    
    public static void main(String[] args) throws Exception {
        // set the server IP and port number
        InetAddress serverIP = InetAddress.getByName("192.168.56.1");
        int port = 7777;
        
        if (args.length > 0) {
            serverIP = InetAddress.getByName(args[0]);
            port = Integer.parseInt(args[1]);
        }
        
        // call the constructor and pass the IP and port
        Client client = new Client(serverIP, port);
        System.out.println("\r\nConnected to server: " + client.socket.getInetAddress());
        client.start();
    }
}
