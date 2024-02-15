// andr0821 and rasmu984

import java.net.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class BankClient {
  private String host;
  private int port;

  public BankClient(String host, int port) {
    this.host = host;
    this.port = port;
  }

  public Response sendRequest(Request request) {
    try (Socket socket = new Socket(host, port);
        ObjectOutputStream objectOStream = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream objectIStream = new ObjectInputStream(socket.getInputStream())) {

        objectOStream.writeObject(request);
        Response response = (Response) objectIStream.readObject();
        return response;
    }
    catch (IOException | ClassNotFoundException e) {
      e.printStackTrace();
      Response response = new Response("FAILED", 0);
      return response;
    }
  }

  public static void main (String args[]) throws InterruptedException {
    if (args.length != 4) {
        System.err.println("Usage: java BankClient <serverHostname> <serverPortnumber> <threadCount> <iterationCount>");
        System.exit(1);
    }

    String serverHost = args[0];
    int serverPort = Integer.parseInt( args[1] );
    int threads = Integer.parseInt( args[2] );
    int iterations = Integer.parseInt( args[3] );

    BankClient bankClient = new BankClient(serverHost, serverPort);

    List<Integer> UIDs = new ArrayList<>();

    for (int i = 0; i < 100; i++) { // creates 100 accounts by sending CreateAccount requests to the server, logging each operation, and storing the received account UIDs
        Request request = new Request("CreateAccount", 0, -1, 0);
        Response response = bankClient.sendRequest(request);
        UIDs.add(response.getBalance());
        ClientLogger.log(request.getOperation(), i+1, request.getTargetAcountUID(), request.getAmount(), response.getStatus());
    }

    for (Integer UID : UIDs) { // deposits 100 into each created account and logs the operation
        Request request = new Request("Deposit", UID, -1, 100);
        Response response = bankClient.sendRequest(request);
        ClientLogger.log(request.getOperation(), request.getSourceAcountUID(), request.getTargetAcountUID(), request.getAmount(), response.getStatus());
    }

    int totalAccountsBalance = 0;
    for (Integer UID : UIDs) { // fetches and sums up the balances of all created accounts, logging each operation
        Request request = new Request("GetBalance", UID, -1, 0);
        Response response = bankClient.sendRequest(request);
        totalAccountsBalance += response.getBalance();
        ClientLogger.log(request.getOperation(), request.getSourceAcountUID(), request.getTargetAcountUID(), response.getBalance(), response.getStatus());
    }

    System.out.println("Step #3: The Total Account Balance (Before Threading) is: " + totalAccountsBalance);

    Random random = new Random();
    ExecutorService executor = Executors.newFixedThreadPool(threads); // fixed thread pool size
    int UIDsSize = UIDs.size();

    for (int i = 0; i < threads; i++) {
        executor.submit(() -> {
            for (int k = 0; k < iterations; k++) { // each thread performs multiple iterations of transferring a small amount between randomly selected accounts, logging each operation
                int sourceAcountUID = UIDs.get(random.nextInt(UIDsSize));
                int targetAccountUID = UIDs.get(random.nextInt(UIDsSize));
                
                Request request = new Request("Transfer", sourceAcountUID, targetAccountUID, 10);
                Response response = bankClient.sendRequest(request);
                ClientLogger.log(request.getOperation(), request.getSourceAcountUID(), request.getTargetAcountUID(), request.getAmount(), response.getStatus());

                if ("FAILED".equals(response.getStatus())) {
                    System.out.println("Failed transfer from " + sourceAcountUID + " to " + targetAccountUID);
                }
            }
        });
    }

    executor.shutdown(); // shuts down the executor service

    try {
        executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS); // waits for all tasks to complete
    }

    catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        System.out.println("Thread was interrupted!");
    }

    totalAccountsBalance = 0; // after all threads have completed their tasks, it calculates and prints the total balance across all accounts again
    for (Integer UID : UIDs) {
        Request request = new Request("GetBalance", UID, -1, 0);
        Response response = bankClient.sendRequest(request);
        totalAccountsBalance += response.getBalance();
        ClientLogger.log(request.getOperation(), request.getSourceAcountUID(), request.getTargetAcountUID(), response.getBalance(), response.getStatus());
    }
    System.out.println("Step #7: The Total Account Balance (After Threading) is: " + totalAccountsBalance);
}
}
