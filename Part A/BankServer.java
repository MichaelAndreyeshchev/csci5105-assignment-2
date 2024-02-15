// andr0821 and rasmu984

import java.net.*;
import java.io.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class BankServer {
  private static ServerSocket server; 
  private static int port; // port number on which the server will listen for incoming connections
  private static final ConcurrentHashMap<Integer, Account> accounts = new ConcurrentHashMap<>(); // ConcurrentHashMap that maps unique account IDs to Account objects, representing the bank accounts managed by the server
  private static final AtomicInteger accountUIDCounter = new AtomicInteger(1); // generate unique IDs for new accounts, starting from 1

  public static void main (String args[]) {
    if (args.length != 1)
         throw new RuntimeException("Syntax: EchoServer port-number");

    try {
        System.out.println("Starting on port " + args[0]);
        port = Integer.parseInt(args[0]);
        
        server = new ServerSocket(port); // create a server socket and bind it to the given port number

        while (true) { // loop where it waits for client connections, accepts them, and starts a new thread
            System.out.println ("Waiting for a client request");
            Socket client = server.accept();  // received a new connection request; a new socket is created for this connection
            System.out.println( "Received request from " + client.getInetAddress ());
            System.out.println( "Starting worker thread..." );
            ClientHandler c = new ClientHandler(client); // create a new thread to handle the request on this new connection 
            c.start ();
        }
    }

    catch (IOException e) {
        e.printStackTrace();
    }
  }

  private static class ClientHandler extends Thread { // responsible for handeling client requests
    private Socket clientSocket;

    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }
    
    public void run () { // executed when the thread starts
        try {
          InputStream istream = clientSocket.getInputStream ();
          OutputStream ostream = clientSocket.getOutputStream ();
          ObjectInputStream objectIStream = new ObjectInputStream(istream);
          ObjectOutputStream objectOStream = new ObjectOutputStream(ostream);

          Request request = (Request) objectIStream.readObject(); // deserializes requests from the client
          Response response;
          Account account;

          switch (request.getOperation()) {
            case "CreateAccount": // generates a new unique ID, creates an account, and adds it to the accounts map
                int UID = accountUIDCounter.getAndIncrement();
                account = new Account(UID);
                accounts.put(UID, account);
                response = new Response("OK", UID);
                ServerLogger.log(request.getOperation(), UID, -1, 0, response.getStatus());

                break;

            case "Deposit": // retrieves the account by ID and deposits the specified amount
                account = accounts.get(request.getSourceAcountUID());

                if (account != null && request.getAmount() >= 0) {
                    account.deposit(request.getAmount());
                    response = new Response("OK", account.getBalance());
                }

                else {
                    response = new Response("FAILED", -1);
                }

                ServerLogger.log(request.getOperation(), request.getSourceAcountUID(), request.getTargetAcountUID(), request.getAmount(), response.getStatus());

                break;

            case "GetBalance": // retrieves the account by ID and returns its balance
                account = accounts.get(request.getSourceAcountUID());

                if (account != null) {
                    response = new Response("OK", account.getBalance());
                }

                else {
                    response = new Response("FAILED", -1);
                }

                ServerLogger.log(request.getOperation(), request.getSourceAcountUID(), request.getTargetAcountUID(), response.getBalance(), response.getStatus());

                break;

            case "Transfer": // retrieves both the source and target accounts by ID, transfers the amount
                account = accounts.get(request.getSourceAcountUID());
                Account targetAccount = accounts.get(request.getTargetAcountUID());

                if (account != null && targetAccount != null && account.transfer(request.getAmount())) {
                    targetAccount.deposit(request.getAmount());
                    response = new Response("OK", request.getAmount());
                }

                else {
                    response = new Response("FAILED", request.getAmount());
                }

                ServerLogger.log(request.getOperation(), request.getSourceAcountUID(), request.getTargetAcountUID(), request.getAmount(), response.getStatus());

                break;

            default:
                response = new Response("ERROR", 0);
          }

          objectOStream.writeObject(response);
          objectOStream.flush();

          
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace ();
        }
    }
}
}
