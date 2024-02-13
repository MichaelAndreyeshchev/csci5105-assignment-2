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
    private static int port = 1099;
    private static final ConcurrentHashMap<Integer, Account> accounts = new ConcurrentHashMap<>();
    private static final AtomicInteger accountUIDCounter = new AtomicInteger(1);

    public RMIBankServerImp() throws RemoteException {
        super();
    }

    public RMIBankServerImp(int port) throws RemoteException {
        super();
        port = port;
    }

    public void shutdown() throws RemoteException {
        try {
            System.out.println("Server is terminating.");
            Registry localRegistry = LocateRegistry.getRegistry(port);
            localRegistry.unbind ("RMIBankServer");
            UnicastRemoteObject.unexportObject(this, true);
            //System.exit(0);
        }

        catch (Exception e) {
            System.out.println("exception in shutdown");
        }
    }

    public int createAccount() throws RemoteException { // consider catching NullPointerException
        int sourceAcountUID = accountUIDCounter.getAndIncrement();
        Account account = new Account(sourceAcountUID);
        accounts.put(sourceAcountUID, account);
        ServerLogger.log("CreateAccount", sourceAcountUID, 0, 0, "OK");

        return sourceAcountUID;
    }

    public String deposit(int sourceAcountUID, int amount) throws RemoteException {
        Account account = accounts.get(sourceAcountUID);

        if (account != null) {
            account.deposit(amount);
            ServerLogger.log("Deposit", sourceAcountUID, 0, amount, "OK");
            return "OK";
        }

        else {
            ServerLogger.log("Deposit", sourceAcountUID, 0, amount, "FAILED");
            return "FAILED";
        }
    }

    public int getBalance(int sourceAcountUID) throws RemoteException {
        Account account = accounts.get(sourceAcountUID);

        if (account != null) {
            ServerLogger.log("GetBalance", sourceAcountUID, 0, 0, "OK");
            return account.getBalance();
        }

        else {
            ServerLogger.log("Deposit", sourceAcountUID, 0, 0, "FAILED");
            return -1;
        }
    }

    public String transfer(int sourceAcountUID, int targetAccountUID, int amount) throws RemoteException {
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
            RMIBankServerImp bankServer = new RMIBankServerImp();
            System.setProperty("java.rmi.server.hostname", InetAddress.getLocalHost().getCanonicalHostName());
            RMIBankServer bankServerStub = (RMIBankServer) UnicastRemoteObject.exportObject(bankServer, 0) ;
            Naming.bind("RMIBankServer", bankServerStub);
        }
        else {
           // rmiregistry is on port specified in args[0]. Bind to that registry.
           Registry localRegistry = LocateRegistry.getRegistry(Integer.parseInt( args[0] ));
           RMIBankServerImp bankServer = new RMIBankServerImp(Integer.parseInt( args[0] ));
           System.setProperty("java.rmi.server.hostname", InetAddress.getLocalHost().getCanonicalHostName());
           RMIBankServer bankServerStub = (RMIBankServer) UnicastRemoteObject.exportObject(bankServer, 0) ;
           localRegistry.bind("RMIBankServer", bankServerStub);
        }
    
        System.out.println("Started RMIBankServer on host: " + InetAddress.getLocalHost().getCanonicalHostName());
      }
}
