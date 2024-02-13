import java.io.*;

public class ClientLogger {
    private static final String FILE_NAME = "clientLogfile.log";

    public static synchronized void log(String operation, int sourceAcountUID, int targetAccountUID, int amount, String status) {
        try (FileWriter fileWriter = new FileWriter(FILE_NAME, true);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            PrintWriter printWriter = new PrintWriter(bufferedWriter)) {

            printWriter.println("Operation Name: " + operation + " | Source Account UID: " + sourceAcountUID + " | Target Account UID: " + targetAccountUID + " | Amount: " + amount + " | Status: " + status);
        
        }

        catch (IOException e) {
            System.err.println("ServerLogger failed to log!");
        }
    }
}


