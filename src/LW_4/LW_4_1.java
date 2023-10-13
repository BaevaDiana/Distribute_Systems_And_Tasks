package LW_4;

import mpi.*;
public class LW_4_1 {
    public static void main(String[] args) throws MPIException {
        MPI.Init(args);
        int rank = MPI.COMM_WORLD.Rank();
        int size = MPI.COMM_WORLD.Size();

        if (rank == 0) {
            // Define matrix and vector
            int N = 1000;
            double[][] matrix = new double[N][N];
            double[] vector = new double[N];
            for (int i = 0; i < N; i++) {
                for (int j = 0; j < N; j++) {
                    matrix[i][j] = Math.random();
                }
                vector[i] = Math.random();
            }

            // Send data to slaves
            for (int i = 1; i < size; i++) {
                MPI.COMM_WORLD.Send(new double[]{N}, 0, 1, MPI.DOUBLE, i, 0);
                MPI.COMM_WORLD.Send(matrix[i - 1], 0, N, MPI.DOUBLE, i, 1);
                MPI.COMM_WORLD.Send(vector, 0, N, MPI.DOUBLE, i, 2);
            }

            // Calculate local scalar product
            double localResult = 0;
            long startTime = System.currentTimeMillis();
            for (int i = 0; i < N / size; i++) {
                for (int j = 0; j < N; j++) {
                    localResult += matrix[i][j] * vector[j];
                }
            }
            long endTime = System.currentTimeMillis();

            // Receive results from slaves
            double[] results = new double[size];
            results[0] = localResult;
            for (int i = 1; i < size; i++) {
                MPI.COMM_WORLD.Recv(new double[]{localResult}, 0, 1, MPI.DOUBLE, i, 3);
                results[i] = localResult;
            }

            // Reduce results
            double globalResult = 0;
            for (double result : results) {
                globalResult += result;
            }

            System.out.println("Global scalar product: " + globalResult);

            long elapsedTime = endTime - startTime;
            System.out.println("Elapsed time: " + elapsedTime + " milliseconds");
        } else {
            // Receive data from master
            double[] data = new double[1001];
            MPI.COMM_WORLD.Recv(data, 0, 1, MPI.DOUBLE, 0, 0);
            int N = (int) data[0];
            double[][] matrix = new double[N / size][N];
            double[] vector = new double[N];
            MPI.COMM_WORLD.Recv(matrix[0], 0, N / size * N, MPI.DOUBLE, 0, 1);
            MPI.COMM_WORLD.Recv(vector, 0, N, MPI.DOUBLE, 0, 2);

            // Calculate local scalar product
            double localResult = 0;
            for (int i = 0; i < N / size; i++) {
                for (int j = 0; j < N; j++) {
                    localResult += matrix[i][j] * vector[j];
                }
            }

            // Send result to master
            MPI.COMM_WORLD.Send(new double[]{localResult}, 0, 1, MPI.DOUBLE, 0, 3);
        }
        MPI.Finalize();
    }
}
