package LW_4;

import mpi.*;

import java.util.concurrent.TimeUnit;
import com.google.common.base.Stopwatch;


// коллективный обмен c помощью функций Scatter(v) / Gather(v)
public class LW_4_3 {
    public static void main(String[] args) throws MPIException {
        MPI.Init(args);
        int rank = MPI.COMM_WORLD.Rank();
        int size = MPI.COMM_WORLD.Size();

        // определение матрицы и вектора
        int N = 1000;
        int[] vector = new int[N];
        int[][] matrix = new int[N][N];
        // заполнение матрицы и вектора случайными значениями
        int i, j;
        for (i = 0; i < N; i++) {
            vector[i] = (int) (Math.random() * 100);
            for (j = 0; j < N; j++) {
                matrix[i][j] = (int) (Math.random() * 100);
            }
        }
        // переопределение матрицы в массив
        int[] array = new int[N];
        for (i = 0; i < N; i++) {
            for (j = 0; j < N; j++) {
                array[i] = matrix[j][i];
            }
        }

        Stopwatch stopwatch = Stopwatch.createStarted();
        // блокировка выполнения программы до тех пор, пока все процессы не достигнут этой точки
        MPI.COMM_WORLD.Barrier();
        if (rank == 0) {
            stopwatch.reset().start();
        }

        int[] localVector = new int[N / size];
        // рассылка вектора на все процессы
        MPI.COMM_WORLD.Scatter(vector, 0, N / size, MPI.INT, localVector, 0, N / size, MPI.INT, 0);

        int[] localMatrix = new int[N / size];
        // распределение матрицы на все процессы
        MPI.COMM_WORLD.Scatter(array, 0,  N / size, MPI.INT, localMatrix, 0,  N / size, MPI.INT, 0);

        // вычисление локального произведения матрицы и вектора
        for (i = 0; i < N / size; i++) {
            localVector[i] = 0;
            for (j = 0; j < N; j++) {
                localVector[i] += localMatrix[i] * vector[j];
            }
        }

        // сбор результатов на корневом процессе
        int[] result = new int[N];
        MPI.COMM_WORLD.Gather(localVector, 0, N / size, MPI.INT, result, 0, N / size, MPI.INT, 0);

        if (rank == 0) {
            stopwatch.stop();
            long elapsedTime = stopwatch.elapsed(TimeUnit.MILLISECONDS);
            double globalResult = 0;
            for (double results : result) {
                globalResult += results;
            }
            System.out.println("Скалярное произведение: " + globalResult);
            System.out.println("Затраченное время: " + elapsedTime + " milliseconds");
        }
        // освобождение ресурсов
        MPI.Finalize();
    }

}
