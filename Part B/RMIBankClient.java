//package Andrev-Assignment-2.Part B;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.rmi.RemoteException;
import java.rmi.Naming;
import java.util.Date;
import java.net.*;
import java.io.*;
import java.util.*;

public class RMIBankClient {
    public static void main (String args[]) throws Exception {
        try {
            if (args.length != 4) {
                System.err.println("Usage: java BankClient <serverHostname> <serverPortnumber> <threadCount> <iterationCount>");
                System.exit(1);
            }
        
            String serverHost = args[0];
            int serverPort = Integer.parseInt( args[1] );
            int threads = Integer.parseInt( args[2] );
            int iterations = Integer.parseInt( args[3] );

            Registry registry = LocateRegistry.getRegistry(serverHost);
            RMIBankServer bankServerStub = (RMIBankServer) registry.lookup("RMIBankServer");

            List<Integer> UIDs = new ArrayList<>();

            for (int i = 0; i < 100; i++) {
                int UID = bankServerStub.createAccount();
                UIDs.add(UID);
                ClientLogger.log("CreateAccount", UID, 0, 0, "OK");
            }

            for (Integer UID : UIDs) {
                bankServerStub.deposit(UID, 100);
                ClientLogger.log("Deposit", UID, 0, 100, "OK");
            }

            int totalAccountsBalance = 0;
            for (Integer UID : UIDs) {
                totalAccountsBalance += bankServerStub.getBalance(UID);
                ClientLogger.log("GetBalance", UID, 0, 0, "OK");
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
                        
                        try {
                            bankServerStub.transfer(sourceAcountUID, targetAccountUID, 10);
                            ClientLogger.log("Transfer", sourceAcountUID, targetAccountUID, 10, "OK");
                        }
        
                        catch (RemoteException e) {
                            ClientLogger.log("Transfer", sourceAcountUID, targetAccountUID, 10, "FAILED");
                            System.out.println("Failed transfer");
                        }
                    }
                });
            }

            executor.shutdown();

            try {
                executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            }

            catch (InterruptedException e){
                Thread.currentThread().interrupt();
                System.out.println("Thread was interrupted!");
            }
            
            totalAccountsBalance = 0;
            for (int UID : UIDs) {
                totalAccountsBalance += bankServerStub.getBalance(UID);
                ClientLogger.log("GetBalance", UID, 0, 0, "OK");
            }
            System.out.println("After Threads, The Total Account Balance is: " + totalAccountsBalance);
            bankServerStub.shutdown();   
        }

    catch (Exception e) {
        Thread.currentThread().interrupt();
        System.out.println("Client Exception!");
        e.printStackTrace();
    } 
}
}
