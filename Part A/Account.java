import java.io.*;

public class Account {
    private int UID;
    private int balance;

    public Account(int UID) {
        this.UID = UID;
        this.balance = 0;
    }

    public int getUID() {
        return UID;
    }

    public synchronized int getBalance() {
        return balance;
    }

    public synchronized void deposit(int amount) {
        this.balance += amount;
    }

    public synchronized boolean transfer(int amount) { // this is withdraw NOT transfer
        if ((this.balance - amount) >= 0) {
            this.balance -= amount;
    
            return true;
        }

        else {
            return false;
        }
    }
}

