// andr0821 and rasmu984

import java.io.*;

public class Account {
    private int UID; // unique Id for accounts
    private int balance;

    public Account(int UID) {
        this.UID = UID;
        this.balance = 0;
    }

    public int getUID() {
        return UID;
    }

    public synchronized int getBalance() { // used to get account balance
        return balance;
    }

    public synchronized void deposit(int amount) { // used to deposit a specified amount to the account
        this.balance += amount;
    }

    public synchronized boolean transfer(int amount) { // this is a withdraw NOT transfer, the rest of the method is handled by the caller
        if ((this.balance - amount) >= 0) {
            this.balance -= amount;
    
            return true;
        }

        else {
            return false;
        }
    }
}

