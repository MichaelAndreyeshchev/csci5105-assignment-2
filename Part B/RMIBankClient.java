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
                throw new RuntimeException("Usage: java BankClient <serverHostname> <serverPortnumber> <threadCount> <iterationCount>");
                //System.err.println("Usage: java BankClient <serverHostname> <serverPortnumber> <threadCount> <iterationCount>");
                //System.exit(1);
            }
        
            String serverHost = args[0];
            int serverPort = Integer.parseInt( args[1] );
            int threads = Integer.parseInt( args[2] );
            int iterations = Integer.parseInt( args[3] );
            RMIBankServer bankServerStub = (RMIBankServer) Naming.lookup("//" + serverHost + ":" + serverPort + "/RMIBankServer");
            //Registry registry = LocateRegistry.getRegistry(serverHost);
            //RMIBankServer bankServerStub = (RMIBankServer) registry.lookup("RMIBankServer");

            List<Integer> UIDs = new ArrayList<>();

            for (int i = 0; i < 100; i++) {
                int UID = bankServerStub.createAccount();
                UIDs.add(UID);
                ClientLogger.log("CreateAccount", UID, -1, 0, "OK");
            }

            for (Integer UID : UIDs) {
                String status = bankServerStub.deposit(UID, 100);

                if ("OK".equals(status)) {
                    ClientLogger.log("Deposit", UID, -1, 100, "OK");
                }

                else {
                    ClientLogger.log("Deposit", UID, -1, 100, "FAILED");
                }
            }

            int totalAccountsBalance = 0;
            for (Integer UID : UIDs) {
                int balance = bankServerStub.getBalance(UID);
                totalAccountsBalance += balance;
                if (balance != -1) {
                    ClientLogger.log("GetBalance", UID, -1, balance, "OK");
                }

                else {
                    ClientLogger.log("GetBalance", UID, -1, -1, "FAILED");
                }
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
                            String status = bankServerStub.transfer(sourceAcountUID, targetAccountUID, 10);
                            
                            if ("OK".equals(status)) {
                                ClientLogger.log("Transfer", sourceAcountUID, targetAccountUID, 10, "OK");
                            }

                            else {
                                ClientLogger.log("Transfer", sourceAcountUID, targetAccountUID, 10, "FAILED");
                                System.out.println("Failed transfer from " + sourceAcountUID + " to " + targetAccountUID);
                            }
                        }
        
                        catch (RemoteException e) {
                            System.out.print("Remote Exception error encountered!");
                            //ClientLogger.log("Transfer", sourceAcountUID, targetAccountUID, 10, "FAILED");
                            //System.out.println("Failed transfer");
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
                int balance = bankServerStub.getBalance(UID);
                totalAccountsBalance += balance;
                if (balance != -1) {
                    ClientLogger.log("GetBalance", UID, -1, balance, "OK");
                }

                else {
                    ClientLogger.log("GetBalance", UID, -1, balance, "FAILED");
                }
            }
            System.out.println("After Threads, The Total Account Balance is: " + totalAccountsBalance);
            bankServerStub.shutdown();   
        }

    catch (Exception e) {
        Thread.currentThread().interrupt();
        System.out.println("Client Exception!");
        e.printStackTrace();
        throw e;
    } 
}
}
