package LW_3;

import mpi.*;
import java.util.Arrays;
import java.util.Random;

class LW_3_2 {
    public static void main(String[] args) throws MPIException {
        MPI.Init(args);
        int rank = MPI.COMM_WORLD.Rank();
        int size = MPI.COMM_WORLD.Size();

        // генерация ранга от 3 до size - 1
        Random rand = new Random();
        int[] randNums = new int[size - 3];
        if (rank >= 3) {
            randNums[rank - 3] = rand.nextInt(100);
            // ассинхронная отправка случайных чисел в процесс 1 и 2
            MPI.COMM_WORLD.Isend(randNums, rank - 3, 1, MPI.INT, 1, 0);
            MPI.COMM_WORLD.Isend(randNums, rank - 3, 1, MPI.INT, 2, 0);
        }

        // ассинхронное получение случайных чисел
        if (rank == 1 || rank == 2) {
            Request[] requests = new Request[size - 3];
            int[] numbers = new int[size - 3];
            for (int i = 3; i < size; i++) {
                requests[i - 3] = MPI.COMM_WORLD.Irecv(numbers, i - 3, 1, MPI.INT, i, 0);
            }
            Request.Waitall(requests);


            // сортировка и ассинхронная отправка отсортированных чисел
            Arrays.sort(numbers);
            if (rank == 1) {
                // ранг 1 - первая половина
                randNums = Arrays.copyOfRange(numbers, 0, numbers.length / 2);
            } else {
                // ранг 2 - вторая половина
                randNums = Arrays.copyOfRange(numbers, numbers.length / 2, numbers.length);
            }
            MPI.COMM_WORLD.Isend(randNums, 0, randNums.length, MPI.INT, 0, 0);
        }

        // объединение отсортированных списков
        if (rank == 0) {
            int[] list1 = null, list2 = null;
            Status status;
            while (true) {
                status = MPI.COMM_WORLD.Iprobe(1, 0);  // проверка наличия сообщения в ранге 1
                if (status != null) {
                    list1 = new int[status.Get_count(MPI.INT)];
                    MPI.COMM_WORLD.Recv(list1,0, list1.length ,MPI.INT, 1, 0); // принимаем список сообщений
                    break;
                }
            }
            while (true) {
                status = MPI.COMM_WORLD.Iprobe(2, 0);  // проверка наличия сообщения в ранге 2
                if (status != null) {
                    list2 = new int[status.Get_count(MPI.INT)];
                    MPI.COMM_WORLD.Recv(list2,0, list2.length, MPI.INT, 2, 0); // принимаем список сообщений
                    break;
                }
            }

            // объединяем списки
            int[] mergedList = new int[list1.length + list2.length];
            int i = 0, j = 0, k = 0;
            while (i < list1.length && j < list2.length) {
                if (list1[i] <= list2[j]) {
                    mergedList[k++] = list1[i++];
                } else {
                    mergedList[k++] = list2[j++];
                }
            }
            while (i < list1.length) {
                mergedList[k++] = list1[i++];
            }
            while (j < list2.length) {
                mergedList[k++] = list2[j++];
            }

            // вывод результирующего списка
            for (int num : mergedList) {
                System.out.println(num);
            }
        }

        MPI.Finalize();
    }
}