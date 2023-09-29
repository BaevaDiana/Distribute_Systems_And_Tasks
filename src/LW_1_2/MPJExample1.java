package LW_1_2;//import mpi.*;

//public class LW_1_2.MPJExample1 {
//    public static void main(String[] args) throws Exception {
//        MPI.Init(args);
//        int rank = MPI.COMM_WORLD.Rank();// можно узнать ранг процесса
//        int size = MPI.COMM_WORLD.Size();// количество всех процессов
//        System.out.println("Hello from <"+rank+">");
//
//        MPI.Finalize();
//    }
//}

// двухточечный обмен
// при запуске четного числа процессов, те из них, которые имеют четный ранг, отправляют сообщение следующим по величине ранга процессам

/*import mpi.*;

public class LW_1_2.MPJExample1
{
    public static void main(String[] args)
    {
        int myrank, size, message;
        int TAG = 0;
        MPI.Init(args);
        myrank = MPI.COMM_WORLD.Rank();
        size = MPI.COMM_WORLD.Size();
        message = myrank;
        if ((myrank % 2) == 0)
        {
            if ((myrank + 1) != size)
                MPI.COMM_WORLD.Send(new int[]{message}, 0, 1, MPI.INT, myrank + 1, TAG);
        }
        else
        {
            if (myrank != 0)
                MPI.COMM_WORLD.Recv(new int[]{message}, 0, 1, MPI.INT, myrank - 1, TAG);
            System.out.println("received :" + message);
        }
        MPI.Finalize();
    }
}*/

import mpi.MPI;
import mpi.MPIException;

public class MPJExample1
{
    public static void main(String[] args)
    {
        MPI.Init(args);

        int rank = MPI.COMM_WORLD.Rank();
        int size = MPI.COMM_WORLD.Size();
        int TAG = 0;
        int s = 0; // переменная для суммирования данных
        int buf = rank;// запись ранга текущего процесса

        int nextRank = (rank + 1) % size; // это ранг следующего процесса по кольцу (если он последний,то станет первым)
        int prevRank = (rank - 1 + size) % size; // это ранг предыдущего процесса по кольцу (если он  первый, то станет последним)
        if (rank == 0) {
            // массив для получения данных от соседнего процесса
            // блокирующий режим
            int[] output = new int[]{0};

            // отправка и прием данных(одновременная)
            MPI.COMM_WORLD.Sendrecv(new int[]{buf}, 0, 1, MPI.INT, nextRank, TAG,
                    output, 0, 1, MPI.INT, prevRank, TAG);

            System.out.println("Sum of total: " + output[0]);
        } else {
            //неблокирующий режим
            // массив для получения данных от предыдущего процеса
            int[] output = new int[]{0};

            // прием данных
            MPI.COMM_WORLD.Recv(output, 0, 1, MPI.INT, prevRank, TAG);

            // суммирование полученных данных с данными текущего процессора
            s += output[0] + rank;

            // отправка данных следующему процессору
            MPI.COMM_WORLD.Send(new int[]{s}, 0, 1, MPI.INT, nextRank, TAG);
        }

        MPI.Finalize();
    }

}

