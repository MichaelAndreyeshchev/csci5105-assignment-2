import java.io.*;

public class Response implements Serializable {
    private String status;
    private int balance;

    public Response(String status, int balance) {
        this.status = status;
        this.balance = balance;
    }

    public String getStatus() {
        return status;
    }

    public int getBalance() {
        return balance;
    }
}

