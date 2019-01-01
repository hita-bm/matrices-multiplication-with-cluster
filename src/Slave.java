import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Slave {
    private ServerSocket serverSocket = null;
    private Socket socket = null;
    private ObjectOutputStream objectOutputStream = null;
    private ObjectInputStream objectInputStream = null;
    private int matrixSize;
    private int[][] matrix1 = null;
    private int[][] matrix2 = null;
    private int[][] resultMatrix = null;

    public Slave(int serverPort) {
        try {
            // Start socket and wait master server to connect
            serverSocket = new ServerSocket(serverPort);
            System.out.println("Slave Server is ready on serverPort " + serverPort);
            socket = serverSocket.accept();
            System.out.println("Master Server connected.");
            objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectInputStream = new ObjectInputStream(socket.getInputStream());

            // Receive two matrices from master
            MyMatrix myMatrix = (MyMatrix) objectInputStream.readObject();
            matrixSize = myMatrix.getMatrixSize();
            matrix1 = myMatrix.getMatrixValues();
            myMatrix = (MyMatrix) objectInputStream.readObject();
            matrix2 = myMatrix.getMatrixValues();

            // Multiplication of two matrices
            resultMatrix = new int[matrixSize][matrixSize];
            for (int row = 0; row < matrixSize; row++)
                for (int column = 0; column < matrixSize; column++)
                    for (int sharedIndex = 0; sharedIndex < matrixSize; sharedIndex++)
                        resultMatrix[row][column] += matrix1[row][sharedIndex] * matrix2[sharedIndex][column];

            // Send result to master
            myMatrix.setMatrixValues(resultMatrix);
            objectOutputStream.writeObject(myMatrix);

            // Close socket
            objectOutputStream.close();
            objectInputStream.close();
            socket.close();
            serverSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Slave slave = new Slave(9000);
    }
}