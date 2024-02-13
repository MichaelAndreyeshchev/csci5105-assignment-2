import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RMIBankServer extends Remote {
    int createAccount() throws RemoteException;
    String deposit(int sourceAcountUID, int amount) throws RemoteException;
    int getBalance(int sourceAcountUID) throws RemoteException;
    String transfer(int sourceAcountUID, int targetAccountUID, int amount) throws RemoteException;
    void shutdown() throws RemoteException;
}
