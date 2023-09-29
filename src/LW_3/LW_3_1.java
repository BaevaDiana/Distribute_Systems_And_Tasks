package LW_3;
import mpi.*;
import java.util.Arrays;

class LW_3_1 {
    public static void main(String[] args) throws MPIException {
        MPI.Init(args);
        int rank = MPI.COMM_WORLD.Rank();
        int size = MPI.COMM_WORLD.Size();

        // проверка количества рангов
        if (size != 3) {
            if (rank == 0) {
                System.out.println("Необходимо запустить программу с 3 процессами");
            }
            MPI.Finalize();
            return;
        }

        int data[] = new int[1];
        int buf[] = {1, 3, 5};
        int count, TAG = 0;
        Status st;
        Request request;

        data[0] = 2016;


        if (rank == 0) { // отправка процессу 2
            System.out.println("Rank 0 отправляет данные: " + data[0]);
            MPI.COMM_WORLD.Send(data, 0, 1, MPI.INT, 2, TAG);
        } else if (rank == 1) { // отправка процессу 2
            System.out.println("Rank 1 отправляет буфер: " + Arrays.toString(buf));
            MPI.COMM_WORLD.Send(buf, 0, buf.length, MPI.INT, 2, TAG);
        } else {
            //проверка входящих сообщений
            int source = MPI.ANY_SOURCE;
            int tag = MPI.ANY_TAG;
            int[] back_buf;
            int[] back_buf2 = new int[buf.length];

            // проверка первого сообщения (от процесса 0)
            while (true) {
                st = MPI.COMM_WORLD.Iprobe(source, tag); // проверка наличия сообщения
                if (st != null && st.tag == TAG) {
                    back_buf = new int[st.Get_count(MPI.INT)]; // массив полученных данных
                    break;
                }
            }
            MPI.COMM_WORLD.Recv(back_buf, 0, back_buf.length, MPI.INT, 0, TAG); // приём сообщений
            System.out.print("Rank = 0, Принятые данные: "); // вывод данных, которые были приняты
            for (int i = 0; i < back_buf.length; i++) {
                System.out.print(back_buf[i] + " ");
            }
            System.out.println();

            // проверка второго сообщения (от процесса 1)
            while (true) {
                st = MPI.COMM_WORLD.Iprobe(source, tag);
                if (st != null && st.tag == TAG) {
                    back_buf2 = new int[st.Get_count(MPI.INT)];
                    break;
                }
            }
            MPI.COMM_WORLD.Recv(back_buf2, 0, back_buf2.length, MPI.INT, 1, TAG);
            System.out.print("Rank = 1, Принятые данные: ");
            for (int i = 0; i < back_buf2.length; i++) {
                System.out.print(back_buf2[i] + " ");
            }
            System.out.println();
        }

        MPI.Finalize();
    }
}