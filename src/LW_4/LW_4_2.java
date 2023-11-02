package LW_4;

import com.google.common.base.Stopwatch;
import mpi.*;

import java.util.Random;
import java.util.concurrent.TimeUnit;

// двухточечный обмен, синхронный, не блокирующий
public class LW_4_2 {
    public static void main(String[] args) throws MPIException {
        MPI.Init(args);
        int rank = MPI.COMM_WORLD.Rank();
        int size = MPI.COMM_WORLD.Size();

        if (rank == 0) {
            // определение матрицы и вектора
            int N = 1000;
            double[][] matrix = new double[N][N];
            double[] vector = new double[N];
            Random rand = new Random();
            // заполнение матрицы и вектора случайными значениями
            int i, j;
            for (i = 0; i < N; i++) {
                for (j = 0; j < N; j++) {
                    matrix[i][j] = rand.nextDouble(100);
                }
                vector[i] = rand.nextDouble(100);
            }

            // отправка данных в slaves
            for (i = 1; i < size; i++) {
                MPI.COMM_WORLD.Irsend(new double[]{N}, 0, 1, MPI.DOUBLE, i, 0);
                MPI.COMM_WORLD.Irsend(matrix[i - 1], 0, N, MPI.DOUBLE, i, 1);
                MPI.COMM_WORLD.Irsend(vector, 0, N, MPI.DOUBLE, i, 2);
            }

            // вычисление локального скалярного произведения
            double localResult = 0;
            Stopwatch stopwatch = Stopwatch.createStarted();
            for (i = 0; i < N / size; i++) {
                for (j = 0; j < N; j++) {
                    localResult += matrix[i][j] * vector[j];
                }
            }
            stopwatch.stop();
            long elapsedTime = stopwatch.elapsed(TimeUnit.MILLISECONDS);

            // получение результата из slaves
            double[] results = new double[size];
            results[0] = localResult;
            for (i = 1; i < size; i++) {
                MPI.COMM_WORLD.Recv(new double[]{localResult}, 0, 1, MPI.DOUBLE, i, 3);
                results[i] = localResult;
            }

            // итоговый результат глобального скалярного произведения
            double globalResult = 0;
            for (double result : results) {
                globalResult += result;
            }
            System.out.println("Скалярное произведение: " + globalResult);
            System.out.println("Затраченное время: " + elapsedTime + " milliseconds");
        } else {
            // получение данных из master
            double[] data = new double[1001];
            MPI.COMM_WORLD.Recv(data, 0, data.length, MPI.DOUBLE, 0, MPI.ANY_TAG);

            // вычисление локального скалярного произведения
            int N = (int) data[0];
            double[] matrixRow = new double[N];
            double[] vector = new double[N];
            MPI.COMM_WORLD.Recv(matrixRow, 0, N, MPI.DOUBLE, 0, MPI.ANY_TAG);
            MPI.COMM_WORLD.Recv(vector, 0, N, MPI.DOUBLE, 0, MPI.ANY_TAG);
            double localResult = 0;
            for (int i = 0; i < N; i++) {
                localResult += matrixRow[i] * vector[i];
            }

            // отправка в master
            MPI.COMM_WORLD.Irsend(new double[]{localResult}, 0, 1, MPI.DOUBLE, 0, 3);
        }
        // освобождение ресурсов
        MPI.Finalize();
    }
}
