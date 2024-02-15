// andr0821 and rasmu984

import java.io.*;

public class Request implements Serializable {
    private String operation; // name of the requested operation
    private int sourceAcountUID; // account UID for source
    private int targetAccountUID; // account UID for target
    private int amount; 

    public Request(String operation, int sourceAcountUID, int targetAccountUID, int amount) {
        this.operation = operation;
        this.sourceAcountUID = sourceAcountUID;
        this.targetAccountUID = targetAccountUID;
        this.amount = amount;
    }

    public String getOperation() {
        return operation;
    }

    public int getSourceAcountUID() {
        return sourceAcountUID;
    }

    public int getTargetAcountUID() {
        return targetAccountUID;
    }

    public int getAmount() {
        return amount;
    }
}
