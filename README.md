# SMTP Client and Server 📧

An implementation of the Simple Mail Transfer Protocol (SMTP) allowing multiple clients to send emails to a  multithreaded SMTP server.

Emails are saved locally on the SMTP server as `.txt` files to verify the emails sent from clients to the server were successful.

## Prerequisites

[Java](https://www.java.com/en/download/help/download_options.html) must be installed in order to use the executable `JAR` file present in the repository.

## Usage

### Server

First, the SMTP server must be set up using the executable `JAR` file to listen for client connections.

```console
java -cp SMTP.jar SMTP.Server [ip address] [port number]
```

Should the optional parameters be omitted, `"192.168.56.1"` and `25` will be used respectively.

Using TCP, the server can establish a connection to multiple clients simultaneously and store the emails locally as `.txt` files.

### Client

Once the SMTP server is set up, multiple clients can connect to it simultaneously using the executable `JAR` file.

```console
java -cp SMTP.jar SMTP.Client [ip address] [port number]
```

Again, if the optional parameters are missing, `"192.168.56.1"` and `25` will be used respectively.

Once connected to an SMTP server, there are three stages in which the client will have to provide an input:

1. `MAIL FROM` – Specify their (sender) email address.

2. `RCPT TO` – Specify the recipient's email address.

3. `End data with <CR><LF>.<CR><LF>` – Write the content of the email. A new line is created by pressing `Enter`, and the email is concluded by transmitting a single `.` in the last line.

## Demo

Below is an example transcript of the communication between a server (S) and a client (C).

```
S: 220 host.uk
C: HELLO client.uk
S: 250 Hello client.uk, pleased to meet you
C: MAIL FROM: <sender@client.uk>
S: 250 ok
C: RCPT TO: <recipient@host.uk>
S: 250 ok
C: DATA
S: 354 End data with <CR><LF>.<CR><LF>
C: Hello recipient,
C: This is the second line of the email.
C: .
S: 250 ok Message accepted for delivery
C: QUIT
S: 221 host.uk closing connection
```