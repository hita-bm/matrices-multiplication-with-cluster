import java.io.Serializable;

public class MyMatrix implements Serializable {
    private int[][] matrixValues;
    private int matrixSize;

    public void setMatrixValues(int[][] matrixValues) {
        this.matrixValues = matrixValues;
        this.matrixSize = matrixValues.length;
    }

    public int[][] getMatrixValues() {
        return matrixValues;
    }

    public int getMatrixSize() {
        return matrixSize;
    }
}