import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;

public class Server {
    private ServerSocket serverSocket = null;
    private Socket socket = null;
    private Socket[] slavesSocket = null;
    private ObjectOutputStream clientOutputStream = null;
    private ObjectInputStream clientInputStream = null;
    private ObjectOutputStream[] slavesOutputStream = null;
    private ObjectInputStream[] slavesInputStream = null;
    private Random random = new Random();
    private int matrixSize = 3;
    private int[][][] matrices = null;
    private int[][] resultMatrix = null;
    private String[] slavesIpAddresses = {"127.0.0.1"};
    private int slavesPort = 9000;
    private int countOfSlaves;
    private String username = "Hita";
    private String password = "97723814";

    public Server(int serverPort) {
        try {
            countOfSlaves = slavesIpAddresses.length;
            int errorCount = 0;

            // Test slaves that are ready or not
            Socket[] slavesSocketTest = new Socket[countOfSlaves];
            for (int slaveIndex = 0; slaveIndex < countOfSlaves; slaveIndex++) {
                try {
                    slavesSocketTest[slaveIndex] = new Socket(slavesIpAddresses[slaveIndex], slavesPort);
                    System.out.println("Slave #" + (slaveIndex + 1) + " is ready.");
                } catch (Exception e) {
                    System.out.println("Slave #" + (slaveIndex + 1) + " did not connect.");
                    slavesSocketTest[slaveIndex] = null;
                    errorCount++;
                }
            }

            // Connect to slaves and wait for client to connect
            countOfSlaves -= errorCount;
            matrices = new int[countOfSlaves][][];
            slavesSocket = new Socket[countOfSlaves];
            slavesOutputStream = new ObjectOutputStream[countOfSlaves];
            slavesInputStream = new ObjectInputStream[countOfSlaves];
            for (int slaveIndex = 0, counter = 0; slaveIndex < countOfSlaves; slaveIndex++) {
                while (slavesSocketTest[counter] == null) counter++;
                slavesSocket[slaveIndex] = slavesSocketTest[counter++];
                slavesInputStream[slaveIndex] = new ObjectInputStream(slavesSocket[slaveIndex].getInputStream());
                slavesOutputStream[slaveIndex] = new ObjectOutputStream(slavesSocket[slaveIndex].getOutputStream());
            }
            System.out.println("Master Server is listening on port " + serverPort);
            serverSocket = new ServerSocket(serverPort);
            socket = serverSocket.accept();
            System.out.println("Client connected.");
            clientOutputStream = new ObjectOutputStream(socket.getOutputStream());
            clientInputStream = new ObjectInputStream(socket.getInputStream());

            // Authorize the user of client
            User user = new User();
            user.setMessage("Authorizing...");
            System.out.println("Authorizing user.");
            clientOutputStream.writeObject(user);
            user = (User) clientInputStream.readObject();
            if (user.getUsername().equalsIgnoreCase(username)) {
                if (user.getPassword().equalsIgnoreCase(password)) {
                    user.setMessage("Authorized.");
                    clientOutputStream.writeObject(user);
                    System.out.println("User Authorized.");
                    user = new User();
                    user.setMessage(String.valueOf(countOfSlaves));
                    clientOutputStream.writeObject(user);

                    // Start computing for the authorized user
                    for (int matrixIndex = 0; matrixIndex < countOfSlaves * 2; matrixIndex++) {
                        int[][] tempMatrix = new int[matrixSize][matrixSize];
                        for (int row = 0; row < matrixSize; row++) {
                            for (int column = 0; column < matrixSize; column++) {
                                tempMatrix[row][column] = random.nextInt(10);
                            }
                        }
                        MyMatrix myMatrix = new MyMatrix();
                        myMatrix.setMatrixValues(tempMatrix);
                        clientOutputStream.writeObject(myMatrix);
                        slavesOutputStream[matrixIndex / 2].writeObject(myMatrix);
                    }
                    for (int slaveIndex = 0; slaveIndex < countOfSlaves; slaveIndex++) {
                        MyMatrix myMatrix = (MyMatrix) slavesInputStream[slaveIndex].readObject();
                        matrices[slaveIndex] = myMatrix.getMatrixValues();
                    }
                    resultMatrix = matrices[random.nextInt(countOfSlaves)];
                    MyMatrix myMatrix = new MyMatrix();
                    myMatrix.setMatrixValues(resultMatrix);
                    clientOutputStream.writeObject(myMatrix);
                } else {
                    user.setMessage("Password is not correct.");
                    clientOutputStream.writeObject(user);
                }
            } else {
                user.setMessage("Username is not correct.");
                clientOutputStream.writeObject(user);
            }

            // Close all sockets
            for (int slaveIndex = 0; slaveIndex < countOfSlaves; slaveIndex++) {
                slavesInputStream[slaveIndex].close();
                slavesOutputStream[slaveIndex].close();
                slavesSocket[slaveIndex].close();
            }
            clientOutputStream.close();
            clientInputStream.close();
            socket.close();
            serverSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Server server = new Server(5000);
    }
}