package SMTP;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

/**
 * Class that acts as an SMTP client; connecting to a server and sending emails.
 * @author Harshil Surendralal
 *
 */
public class Client {
    private Socket socket;
    private PrintWriter output;
    private InputStreamReader input;
    private BufferedReader br;
    private InetAddress serverAddress;
    private int serverPort;
    private Scanner scanner;
    private DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

    /**
     * Create an object of type Client that creates a stream socket, and connects it to the specified port on the server's IP address.
     * @param serverAddress The IP address of the server.
     * @param serverPort The port on the server.
     * @throws Exception
     */
    private Client(InetAddress serverAddress, int serverPort) throws Exception {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;

        // create a stream socket and connect it to the specified port number at the specified IP address
        this.socket = new Socket(this.serverAddress, this.serverPort);
        this.scanner = new Scanner(System.in);
    }

    /**
     * Print a message to the console prefaced with a timestamp.
     * @param message The message that is to be printed.
     */
    private void println(String message) {
        LocalDateTime now = LocalDateTime.now();
        System.out.println("\r\n[" + dtf.format(now) + "] " + message);
    }

    /**
     * Print a message to the console prefaced with a timestamp.
     * @param message The message that is to be printed.
     */
    private void print(String message) {
        LocalDateTime now = LocalDateTime.now();
        System.out.print("\r\n[" + dtf.format(now) + "] " + message);
    }

    /**
     * Setup the input buffer, and the input and output streams that will be used to send and receive messages, to and from the server.
     * @throws Exception
     */
    private void setupStreams() throws Exception {
        output = new PrintWriter(socket.getOutputStream(), true);
        input = new InputStreamReader(socket.getInputStream());
        br = new BufferedReader(input);
        println("Streams are setup");
    }

    /**
     * Transmit a message to the server using their output stream.
     * @param message The message that will be transmitted.
     * @throws Exception
     */
    private void sendMessage(String message) throws Exception {
        output.println(message);
    }

    /**
     * Capture each line of the email and transmit them to the server.
     * @throws Exception
     */
    private void sendEmail() throws Exception {
        String line;

        // until the client types a ".", send each line to the server
        while (!(line = scanner.nextLine()).equals(".")) {
            sendMessage(line);
        }

        sendMessage(".");
    }

    /**
     * Primary method used to transmit and receive messages to and from the server.
     * @throws Exception
     */
    private void exchangeMessages() throws Exception {
        String line;
        String message;
        boolean sentRecipient = false;
        boolean sentData = false;

        // until the message that is received starts with "221", keep reading messages from the server
        while (!(line = br.readLine()).startsWith("221")) {
            println("Server: " + line);

            // if the line starts with "220", respond with a greeting
            if (line.startsWith("220")) {
                sendMessage("HELLO " + socket.getInetAddress().getHostName());
            }

            // if the line starts with "250 Hello"
            if (line.startsWith("250 Hello")) {
                // prompt the user to enter the sender of the email and transmit it to the server
                print("MAIL FROM: ");
                message = scanner.nextLine();
                sendMessage("MAIL FROM: <" + message + ">");
            }

            // if the message is "250 ok" and the user hasn't entered the recipient of the email
            if (line.equals("250 ok") && !sentRecipient) {
                // prompt the user to enter the recipient and transmit it to the server
                print("RCPT TO: ");
                message = scanner.nextLine();
                sendMessage("RCPT TO: <" + message + ">");
                sentRecipient = true; // user has sent the recipient now
            }

            // if the message is "250 ok", and the user has entered the recipient of the email,
            // and "DATA" has not been sent yet, respond with "DATA"
            if (line.equals("250 ok") && sentRecipient && !sentData) {
                sendMessage("DATA");
                sentData = true;
            }

            // if the line starts with "354", the user can now type the email they wish to send
            if (line.startsWith("354")) {
                sendEmail();
            }

            // if the line starts with "250 ok Message", respond with "QUIT"
            if (line.startsWith("250 ok Message")) {
                sendMessage("QUIT");
            }
        }

        // print the final message received from the server
        println("Server: " + line);
    }

    /**
     * Close the socket, input buffer, and the input and output streams that were used to send and receive messages, to and from the server.
     * @throws Exception
     */
    private void cleanUp() throws Exception {
        println("Closing connection");
        output.close();
        input.close();
        br.close();
        socket.close();
    }

    /**
     * Primary method used to connect and disconnect to and from a server.
     * @throws Exception
     */
    private void start() throws Exception {
        try {
            setupStreams();
            exchangeMessages();
        } catch (EOFException e) {
            println("Client closed the connection");
        } finally {
            cleanUp();
        }
    }

    /**
     * Main entry point to the program.
     * @param args The command line arguments passed to the program.
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        // set the server IP and port number
        InetAddress serverIP = InetAddress.getByName("192.168.56.1");
        int port = 7777;

        // if arguments have been provided, reassign the server IP and port number
        if (args.length > 0) {
            serverIP = InetAddress.getByName(args[0]);
            port = Integer.parseInt(args[1]);
        }

        // create an object of type Client
        Client client = new Client(serverIP, port);
        client.println("Connected to server: " + client.socket.getInetAddress());

        client.start();
    }
}
