import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.rmi.Naming;
import java.rmi.registry.*;
import java.util.Date;
import java.net.*;

public class RMIBankServerImp implements RMIBankServer {
    private static int port = 1099; // RMI port
    private static final ConcurrentHashMap<Integer, Account> accounts = new ConcurrentHashMap<>(); // ConcurrentHashMap that maps unique account IDs to Account objects, representing the bank accounts managed by the server
    private static final AtomicInteger accountUIDCounter = new AtomicInteger(1); // generate unique IDs for new accounts, starting from 1

    public RMIBankServerImp() throws RemoteException {
        super();
    }

    public RMIBankServerImp(int rmiport) throws RemoteException {
        super();
        port = rmiport;
    }

    public void shutdown() throws RemoteException { // unbinds the server from the RMI registry and unexports the RMI object, effectively shutting down the server
        try {
            System.out.println("Server is terminating.");
            Registry localRegistry = LocateRegistry.getRegistry(InetAddress.getLocalHost().getCanonicalHostName(), port);
            localRegistry.unbind ("RMIBankServer");
            UnicastRemoteObject.unexportObject(this, true);
            System.out.println("RMI Server Port Shutdown Completed!");
            //System.exit(0);
        }

        catch (Exception e) {
            System.out.println("exception in shutdown");
        }
    }

    public int createAccount() throws RemoteException { // generates a unique ID for a new account, creates an account, logs the operation, and stores it in the accounts map
        int sourceAcountUID = accountUIDCounter.getAndIncrement();
        Account account = new Account(sourceAcountUID);
        accounts.put(sourceAcountUID, account);
        ServerLogger.log("CreateAccount", sourceAcountUID, -1, 0, "OK");

        return sourceAcountUID;
    }

    public String deposit(int sourceAcountUID, int amount) throws RemoteException { // adds a specified amount to an existing account and logs the operation
        Account account = accounts.get(sourceAcountUID);

        if (account != null) {
            account.deposit(amount);
            ServerLogger.log("Deposit", sourceAcountUID, -1, amount, "OK");
            return "OK";
        }

        else {
            ServerLogger.log("Deposit", sourceAcountUID, -1, amount, "FAILED");
            return "FAILED";
        }
    }

    public int getBalance(int sourceAcountUID) throws RemoteException { // retrieves the balance of a specified account and logs the operation
        Account account = accounts.get(sourceAcountUID);

        if (account != null) {
            int balance = account.getBalance();
            ServerLogger.log("GetBalance", sourceAcountUID, -1, balance, "OK");
            return balance;
        }

        else {
            ServerLogger.log("GetBalance", sourceAcountUID, -1, -1, "FAILED");
            return -1;
        }
    }

    public String transfer(int sourceAcountUID, int targetAccountUID, int amount) throws RemoteException { // transfers funds from one account to another if sufficient funds are available and logs the operation.
        Account account = accounts.get(sourceAcountUID);
        Account targetAccount = accounts.get(targetAccountUID);

        if (account != null && targetAccount != null && account.transfer(amount)) {
            targetAccount.deposit(amount);
            ServerLogger.log("Transfer", sourceAcountUID, targetAccountUID, amount, "OK");
            return "OK";
        }

        else {
            ServerLogger.log("Transfer", sourceAcountUID, targetAccountUID, amount, "FAILED");
            return "FAILED";
        }
    }

    public static void main (String args[]) throws Exception {
        if ( args.length == 0 ) {
            Registry localRegistry = LocateRegistry.createRegistry(1099);
            RMIBankServerImp bankServer = new RMIBankServerImp();
            System.setProperty("java.rmi.server.hostname", InetAddress.getLocalHost().getCanonicalHostName());
            RMIBankServer bankServerStub = (RMIBankServer) UnicastRemoteObject.exportObject(bankServer, 0) ;
            Naming.bind("//" + InetAddress.getLocalHost().getCanonicalHostName() + ":" + 1099 + "/RMIBankServer", bankServerStub);
        }
        else {
           // rmiregistry is on port specified in args[0]. Bind to that registry.
           //Registry localRegistry = LocateRegistry.getRegistry(Integer.parseInt( args[0] ));
           Registry localRegistry = LocateRegistry.createRegistry(Integer.parseInt( args[0] ));
           RMIBankServerImp bankServer = new RMIBankServerImp(Integer.parseInt( args[0] ));
           System.setProperty("java.rmi.server.hostname", InetAddress.getLocalHost().getCanonicalHostName());
           RMIBankServer bankServerStub = (RMIBankServer) UnicastRemoteObject.exportObject(bankServer, 0) ;
           //localRegistry.bind("//" + InetAddress.getLocalHost().getHostAddress() + ":" + args[0] + "/RMIBankServer", bankServerStub);
           Naming.bind("//" + InetAddress.getLocalHost().getCanonicalHostName() + ":" + args[0] + "/RMIBankServer", bankServerStub);
        }
        System.out.println("Started RMIBankServer on host: " + InetAddress.getLocalHost().getCanonicalHostName());
      }
}
