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

Once the SMTP server is set up, clients can connect to it using the executable `JAR` file.

```console
java -cp SMTP.jar SMTP.Client [ip address] [port number]
```

Again, if the optional parameters are missing, `"192.168.56.1"` and `25` will be used respectively.

Multiple clients are able to connect to a single SMTP server and send an email by:

1. specifying their (sender) email address,

2. specifying the recipient's email address, and

3. writing the content of the email.

