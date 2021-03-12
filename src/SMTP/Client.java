package SMTP;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private Socket socket;
    private PrintWriter output;
    private InputStreamReader input;
    private BufferedReader br;
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
    
    private void setupStreams() throws Exception {
        output = new PrintWriter(socket.getOutputStream(), true);
        input = new InputStreamReader(socket.getInputStream());
        br = new BufferedReader(input);
        System.out.println("\r\nStreams are setup");
    }
    
    private void sendMessage(String message) throws Exception {
        output.println(message);
    }
    
    private void farewell() throws Exception {
        sendMessage("QUIT");
    }
    
    private void sendData() throws Exception {
        String line;
        
        while (!(line = scanner.nextLine()).equals(".")) {
            sendMessage(line);
        }
        
        sendMessage(".");
    }
    
    private void exchangeMessages() throws Exception {
        String line;
        String message;
        boolean sentRecipient = false;
        
        while (!(line = br.readLine()).startsWith("221")) {
            System.out.println("\r\nServer: " + line);
            
            if (line.startsWith("220")) {
                sendMessage("HELLO " + socket.getInetAddress().getHostName());
            } else if (line.startsWith("250 Hello")) {
                System.out.print("\r\nMAIL FROM: ");
                message = scanner.nextLine();
                sendMessage("MAIL FROM: <" + message + ">");
            } else if (line.equals("250 ok") && !sentRecipient) {
                System.out.print("\r\nRCPT TO: ");
                message = scanner.nextLine();
                sendMessage("RCPT TO: <" + message + ">");
                sentRecipient = true;
            } else if (line.equals("250 ok") && sentRecipient) {
                sendMessage("DATA");
            } else if (line.startsWith("354")) {
                sendData();
            } else if (line.startsWith("250 ok Message")) {
                sendMessage("QUIT");
            }
        }
        
        System.out.println("\r\nServer: " + line);
    }
    
    private void cleanUp() throws Exception {
        System.out.println("\r\nClosing connection");
        output.close();
        input.close();
        socket.close();
    }
    
    private void start() throws Exception {
        try {
            setupStreams();
            exchangeMessages();
            farewell();
        } catch (EOFException e) {
            System.out.println("\r\nClient closed the connection");
        } finally {
            cleanUp();
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
