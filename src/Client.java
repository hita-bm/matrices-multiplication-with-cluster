import java.net.Socket;
import java.io.*;
import java.util.Scanner;

public class Client {
    private Socket socket = null;
    private ObjectOutputStream objectOutputStream = null;
    private ObjectInputStream objectInputStream = null;
    private Scanner scanner = new Scanner(System.in);
    private int countOfMatrices;

    public Client(String address, int port) {
        try {
            // Connect to master server
            socket = new Socket(address, port);
            System.out.println("Connected to server");
            objectInputStream = new ObjectInputStream(socket.getInputStream());
            objectOutputStream = new ObjectOutputStream(socket.getOutputStream());


            // Authorize the user
            User user = (User) objectInputStream.readObject();
            System.out.println(user.getMessage());
            System.out.println("Enter your username:");
            String username = scanner.nextLine();
            user.setUsername(username);
            System.out.println("Enter your password:");
            String password = scanner.nextLine();
            user.setPassword(password);
            objectOutputStream.writeObject(user);
            user = (User) objectInputStream.readObject();
            if (user.getMessage().equalsIgnoreCase("Authorized.")) {
                System.out.println("Authorized.");
                user = (User) objectInputStream.readObject();
                countOfMatrices = Integer.parseInt(user.getMessage());
                for (int matrixIndex = 0; matrixIndex < (countOfMatrices * 2) + 1; matrixIndex++) {
                    MyMatrix myMatrix = (MyMatrix) objectInputStream.readObject();
                    if (matrixIndex != countOfMatrices * 2)
                        System.out.println("Matrix #" + (matrixIndex + 1));
                    else
                        System.out.println("Final Matrix");
                    for (int row = 0; row < myMatrix.getMatrixSize(); row++) {
                        for (int column = 0; column < myMatrix.getMatrixSize(); column++)
                            System.out.print(myMatrix.getMatrixValues()[row][column] + " ");
                        System.out.println();
                    }
                    System.out.println();
                }
            } else {
                System.out.println(user.getMessage());
            }

            // Close socket
            objectOutputStream.close();
            objectInputStream.close();
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Client client = new Client("127.0.0.1", 5000);
    }
}