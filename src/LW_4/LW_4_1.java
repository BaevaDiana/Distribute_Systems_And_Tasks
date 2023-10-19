package LW_4;

import com.google.common.base.Stopwatch;
import mpi.*;

import java.util.concurrent.TimeUnit;

public class LW_4_1 {
    public static void main(String[] args) throws MPIException {
        // блокирующий процесс
        MPI.Init(args);
        int rank = MPI.COMM_WORLD.Rank();
        int size = MPI.COMM_WORLD.Size();

        if (rank == 0) {
            // определение матрицы и вектора
            int N = 1000;
            double[][] matrix = new double[N][N];
            double[] vector = new double[N];
            for (int i = 0; i < N; i++) {
                for (int j = 0; j < N; j++) {
                    matrix[i][j] = Math.random();
                }
                vector[i] = Math.random();
            }

            // отправка данных slaves
            for (int i = 1; i < size; i++) {
                MPI.COMM_WORLD.Send(new double[]{N}, 0, 1, MPI.DOUBLE, i, 0);
                MPI.COMM_WORLD.Send(matrix[i - 1], 0, N, MPI.DOUBLE, i, 1);
                MPI.COMM_WORLD.Send(vector, 0, N, MPI.DOUBLE, i, 2);
            }

            // вычисление скалярного локального произведения
            double localResult = 0;
            Stopwatch stopwatch = Stopwatch.createStarted();
            for (int i = 0; i < N / size; i++) {
                for (int j = 0; j < N; j++) {
                    localResult += matrix[i][j] * vector[j];
                }
            }
            stopwatch.stop();
            long elapsedTime = stopwatch.elapsed(TimeUnit.MILLISECONDS);

            // отправка данных slaves
            double[] results = new double[size];
            results[0] = localResult;
            for (int i = 1; i < size; i++) {
                MPI.COMM_WORLD.Recv(new double[]{localResult}, 0, 1, MPI.DOUBLE, i, 3);
                results[i] = localResult;
            }

            // итоговый результат скалярного произведения матрицы и вектора
            double globalResult = 0;
            for (double result : results) {
                globalResult += result;
            }

            System.out.println("Глобальное скалярное произведение: " + globalResult);
            System.out.println("Затраченное время: " + elapsedTime + " milliseconds");
        } else {
            // получение данных от master
            double[] data = new double[1001];
            MPI.COMM_WORLD.Recv(data, 0, data.length, MPI.DOUBLE, 0, MPI.ANY_TAG);

            // вычисление скалярного локального произведения
            int N = (int) data[0];
            double[] matrixRow = new double[N];
            double[] vector = new double[N];
            MPI.COMM_WORLD.Recv(matrixRow, 0, N, MPI.DOUBLE, 0, MPI.ANY_TAG);
            MPI.COMM_WORLD.Recv(vector, 0, N, MPI.DOUBLE, 0, MPI.ANY_TAG);
            double localResult = 0;
            for (int i = 0; i < N; i++) {
                localResult += matrixRow[i] * vector[i];
            }

            // отправка результатов master
            MPI.COMM_WORLD.Send(new double[]{localResult}, 0, 1, MPI.DOUBLE, 0, 3);
        }

        // освобождение ресурсов
        MPI.Finalize();
    }
}
