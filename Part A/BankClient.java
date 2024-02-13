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
    //System.out.println ("Connecting to " + host + ":" + port + "..");

    BankClient bankClient = new BankClient(serverHost, serverPort);
    //System.out.println ("Connected.");

    List<Integer> UIDs = new ArrayList<>();

    for (int i = 0; i < 100; i++) {
        Request request = new Request("CreateAccount", 0, 0, 0);
        Response response = bankClient.sendRequest(request);
        UIDs.add(response.getBalance());
        ClientLogger.log(request.getOperation(), request.getSourceAcountUID(), request.getTargetAcountUID(), request.getAmount(), response.getStatus());
    }

    for (Integer UID : UIDs) {
        Request request = new Request("Deposit", UID, 0, 100);
        Response response = bankClient.sendRequest(request);
        ClientLogger.log(request.getOperation(), request.getSourceAcountUID(), request.getTargetAcountUID(), request.getAmount(), response.getStatus());
    }

    int totalAccountsBalance = 0;
    for (Integer UID : UIDs) {
        Request request = new Request("GetBalance", UID, 0, 0);
        Response response = bankClient.sendRequest(request);
        totalAccountsBalance += response.getBalance();
        ClientLogger.log(request.getOperation(), request.getSourceAcountUID(), request.getTargetAcountUID(), request.getAmount(), response.getStatus());
    }

    System.out.println("The Total Account Balance is: " + totalAccountsBalance);

    Random random = new Random();
    ExecutorService executor = Executors.newFixedThreadPool(threads);
    int UIDsSize = UIDs.size();

    for (int i = 0; i < threads; i++) {
        executor.submit(() -> {
            for (int k = 0; k < iterations; k++) {
                int sourceAcountUID = UIDs.get(random.nextInt(UIDsSize));
                int targetAccountUID = UIDs.get(random.nextInt(UIDsSize));
                
                Request request = new Request("Transfer", sourceAcountUID, targetAccountUID, 10);
                Response response = bankClient.sendRequest(request);
                ClientLogger.log(request.getOperation(), request.getSourceAcountUID(), request.getTargetAcountUID(), request.getAmount(), response.getStatus());

                if ("FAILED".equals(response.getStatus())) {
                    System.out.println("Failed transfer");
                }
            }
        });
    }

    executor.shutdown(); // DOUBLE CHECK: STEP # 5

    try {
        executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS); // DOUBLE CHECK: STEP #6
    }

    catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        System.out.println("Thread was interrupted!");
    }

    totalAccountsBalance = 0;
    for (Integer UID : UIDs) {
        Request request = new Request("GetBalance", UID, 0, 0);
        Response response = bankClient.sendRequest(request);
        totalAccountsBalance += response.getBalance();
    }
    System.out.println("After Threads, The Total Account Balance is: " + totalAccountsBalance);
}
}
