import java.net.*;
import java.io.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class BankServer {
  private static ServerSocket server;
  private static int port;
  private static final ConcurrentHashMap<Integer, Account> accounts = new ConcurrentHashMap<>();
  private static final AtomicInteger accountUIDCounter = new AtomicInteger(1);

  public static void main (String args[]) {
    if (args.length != 1)
         throw new RuntimeException("Syntax: EchoServer port-number");

    try {
        System.out.println("Starting on port " + args[0]);
        port = Integer.parseInt(args[0]);
        
        server = new ServerSocket(port); // create a server socket and bind it to the given port number

        while (true) {
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

  private static class ClientHandler extends Thread {
    private Socket clientSocket;

    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }
    
    public void run () {
        try {
          InputStream istream = clientSocket.getInputStream ();
          OutputStream ostream = clientSocket.getOutputStream ();
          ObjectInputStream objectIStream = new ObjectInputStream(istream);
          ObjectOutputStream objectOStream = new ObjectOutputStream(ostream);

          Request request = (Request) objectIStream.readObject();
          Response response;
          Account account;

          switch (request.getOperation()) {
            case "CreateAccount":
                int UID = accountUIDCounter.getAndIncrement();
                account = new Account(UID);
                accounts.put(UID, account);
                response = new Response("OK", UID);
                break;

            case "Deposit":
                account = accounts.get(request.getSourceAcountUID());

                if (account != null) {
                    account.deposit(request.getAmount());
                    response = new Response("OK", account.getBalance());
                }

                else {
                    response = new Response("FAILED", 0);
                }

                break;

            case "GetBalance":
                account = accounts.get(request.getSourceAcountUID());

                if (account != null) {
                    response = new Response("OK", account.getBalance());
                }

                else {
                    response = new Response("FAILED", 0);
                }

                break;

            case "Transfer":
                account = accounts.get(request.getSourceAcountUID());
                Account targetAccount = accounts.get(request.getTargetAcountUID());

                if (account != null && targetAccount != null && account.transfer(request.getAmount())) {
                    targetAccount.deposit(request.getAmount());
                    response = new Response("OK", account.getBalance());
                }

                else {
                    response = new Response("FAILED", 0);
                }

                break;

            default:
                response = new Response("ERROR", 0);
          }

          objectOStream.writeObject(response);
          objectOStream.flush();
          ServerLogger.log(request.getOperation(), request.getSourceAcountUID(), request.getTargetAcountUID(), response.getBalance(), response.getStatus());

          
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace ();
        }
    }
}
}
