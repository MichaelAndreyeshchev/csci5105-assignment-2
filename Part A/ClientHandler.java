import java.io.*;
import java.net.Socket;

class ClientHandler extends Thread {
    private Socket clientSocket;
    private BankServer server;

    public ClientHandler(Socket clientSocket, BankServer server) {
        this.clientSocket = clientSocket;
        this.server = server;
    }

    @Override
    public void run() {
        try (
            ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream())
        ) {
            Request request = (Request) in.readObject();
            Response response = server.handleRequest(request);
            out.writeObject(response);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
