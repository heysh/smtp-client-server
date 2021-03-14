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

/**
 * Class that acts as an SMTP server; allowing clients to connect and send emails.
 * @author Harshil Surendralal bf000259
 */
public class STServer {
    private ServerSocket server;
    private PrintWriter output;
    private InputStreamReader input;
    private BufferedReader br;
    private Socket client;
    
    /**
     * Create an object of type Server that creates a server socket at an IP address, with a given port.
     * @param ipAddress The IP address the server will be bound to.
     * @param port The port on which the server socket is bound.
     * @throws Exception
     */
    public STServer(String ipAddress, int port) throws Exception {
        if (ipAddress != null && !ipAddress.isEmpty()) {
            this.server = new ServerSocket(port, 1, InetAddress.getByName(ipAddress));
        } else {
            this.server = new ServerSocket(0, 1, InetAddress.getLocalHost());
        }
    }
    
    /**
     * Wait until a client wishes to connect to the server, and accept the connection.
     * @throws Exception
     */
    private void waitForConnection() throws Exception {
        System.out.println("\r\nWaiting for a connection");
        client = server.accept();
        System.out.println("\r\nConnected to " + client.getInetAddress().getHostName());
    }
    
    /**
     * Setup the input buffer, and the input and output streams that will be used to send and receive messages, to and from the client.
     * @throws Exception
     */
    private void setupStreams() throws Exception {
        output = new PrintWriter(client.getOutputStream(), true);
        input = new InputStreamReader(client.getInputStream());
        br = new BufferedReader(input);
        System.out.println("\r\nStreams are setup");
    }
    
    /**
     * Transmit a message to the client using their output stream.
     * @param message The message that will be transmitted.
     * @throws Exception
     */
    private void sendMessage(String message) throws Exception {
        output.println(message);
    }
    
    /**
     * Transmit the initial message to the client. 
     * @throws Exception
     */
    private void initiateCommunication() throws Exception {
        sendMessage("220 " + server.getInetAddress().getHostName());
    }
    
    /**
     * Read each line of the email that is being transmitted from the client.
     * @return The email that has been transmitted from the client.
     * @throws Exception
     */
    private String handleEmail() throws Exception {
        String email = "";
        String line;
        
        // until the client sends a ".", concatenate the line to email, followed by a newline character
        while (!(line = br.readLine()).equals(".")) {
            email = email + line + "\n";
        }
        
        return email;
    }
    
    /**
     * Save the email that has been transmitted by the client to a file, named after the intended recipient of the email.
     * @param sender The sender of the email.
     * @param recipient The recipient of the email.
     * @param email The email that has been transmitted by the client.
     */
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
            System.out.println("\r\nCould not save the email");
        }
    }
    
    /**
     * Primary method used to transmit and receive messages to and from the client.
     * @throws Exception
     */
    private void exchangeMessages() throws Exception {
        String line, sender = "", recipient = "", email = "";
        String[] lineSplitted;
        
        // until the message that is received is "QUIT", keep reading messages from the client
        while (!(line = br.readLine()).equals("QUIT")) {
            System.out.println("\r\nClient: " + line);
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
    
    /**
     * Transmit the final message to the client.
     * @throws Exception
     */
    private void farewell() throws Exception {
        sendMessage("221 " + server.getInetAddress().getHostName() + " closing connection");
    }
    
    /**
     * Close the socket, input buffer, and the input and output streams that were used to send and receive messages, to and from the client.
     * @throws Exception
     */
    private void cleanUp() throws Exception {
        System.out.println("\r\nClosing connection to " + client.getInetAddress().getHostName());
        output.close();
        input.close();
        br.close();
        client.close();
    }
    
    /**
     * Primary method used to accept and close connections from and to clients.
     * @throws Exception
     */
    private void listen() throws Exception {
        while (true) {
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
        }
    }
    
    /**
     * Get the IP address the server is be bound to.
     * @return The IP address.
     */
    public InetAddress getSocketAddress() {
        return this.server.getInetAddress();
    }
    
    /**
     * Get the port on which the server socket is bound.
     * @return The port.
     */
    public int getPort() {
        return this.server.getLocalPort();
    }
    
    /**
     * Main entry point to the program.
     * @param args The command line arguments passed to the program.
     * @throws Exception
     */
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
        STServer server = new STServer(serverIP, port);
        System.out.println("\r\nRunning server: " +
                "Host=" + server.getSocketAddress().getHostAddress() +
                " Port=" + server.getPort());
        
        server.listen();
    }
}
