package LW_5;

import mpi.*;

// вычисление диаметра произвольного неориентированного графа
public class LW_5 {
    public static void main(String[] args) throws MPIException {
        MPI.Init(args);
        int rank = MPI.COMM_WORLD.Rank();
        int size = MPI.COMM_WORLD.Size();
        // выбираем корневой процесс
        int root = 0;

        // размер матрицы смежности (количество вершин графа)
        int n = 5;
        // матрица смежности графа
        int[][] matrix = new int[n][n];
        // матрица кратчайших расстояний между вершинами
        int[][] dist = new int[n][n];
        // диаметр графа - максимальная длина кратчайшего пути между двумя вершинами
        int diameter = 0;

        if (rank == root)
        {
            // заполнение матрицы смежности случайными значениями от 0 до 9
            // матрица симметрична, так как граф неориентированный
            int i,j;
            for (i = 0; i < n; i++)
            {
                for (j = i; j < n; j++)
                {
                    if (i == j)
                    {
                        // диагональные элементы равны 0
                        matrix[i][j] = 0;
                    }
                    else
                    {
                        matrix[i][j] = (int) (Math.random() * 10);
                        // симметричный элемент
                        matrix[j][i] = matrix[i][j];
                    }
                }
            }
            System.out.println("Матрица смежности графа:");
            printMatrix(matrix);
        }

        // преобразование двумерной матрицы смежности графа в одномерный массив (для операции коллективной рассылки)
        int[] array_matrix = null;
        // определение размера нового массива
        int m = matrix.length * matrix[0].length;
        array_matrix = new int[m];
        int i,j,l = 0;
        for (i = 0; i < matrix.length; i++)
        {
            for (j = 0; j < matrix[i].length; j++)
            {
                array_matrix[l] = matrix[i][j];
                l++;
            }
        }

        // рассылаем матрицу смежности всем процессам
        MPI.COMM_WORLD.Bcast(array_matrix, 0, m, MPI.INT, root);

        // копируем матрицу смежности в матрицу расстояний
        for (i = 0; i < n; i++)
        {
            System.arraycopy(matrix[i], 0, dist[i], 0, n);
        }

        // применение алгоритма Флойда-Уоршелла для нахождения кратчайших путей
        // каждый процесс отвечает за свою часть матрицы
        // используем барьерную синхронизацию для согласования шагов алгоритма
        int k;
        for (k = 0; k < n; k++)
        {
            for (i = rank; i < n; i += size)
            {
                for (j = 0; j < n; j++)
                {
                    // если есть путь через вершину k
                    if (dist[i][k] > 0 && dist[k][j] > 0)
                    {
                        // вычисляем длину этого пути
                        int newDist = dist[i][k] + dist[k][j];
                        // если он короче текущего пути или текущего пути нет
                        if (dist[i][j] == 0 || newDist < dist[i][j])
                        {
                            // обновляем значение в матрице расстояний
                            dist[i][j] = newDist;
                        }
                    }
                }
            }

            // преобразование двумерной матрицы кратчайших расстояний в одномерный массив (для операции коллективной рассылки)
            int[] array_dist = null;
            // определение размера нового массива
            int d = dist.length * dist[0].length;
            array_dist = new int[d];
            int t = 0;
            for (i = 0; i < dist.length; i++) {
                for (j = 0; j < dist[i].length; j++) {
                    array_dist[t] = dist[i][j];
                    t++;
                }
            }

            // синхронизируемся с другими процессами
            MPI.COMM_WORLD.Barrier();
            //
            MPI.COMM_WORLD.Allgather(array_dist, 0, size,MPI.INT, array_dist, 0,size, MPI.INT);
        }

        if (rank == root)
        {
            System.out.println("Матрица кратчайших расстояний между вершинами:");
            printMatrix(dist);

            // поиск максимального значения в матрице расстояний - диаметра
            for (i = 0; i < n; i++) {
                for (j = i + 1; j < n; j++)
                {
                    if (dist[i][j] > diameter)
                    {
                        diameter = dist[i][j];
                    }
                }
            }
            System.out.println("Диаметр графа: " + diameter);
        }

        MPI.Finalize();
    }
    // вспомогательный метод для вывода матрицы на экран
    public static void printMatrix(int[][] matrix) {
        for (int[] row : matrix) {
            for (int element : row) {
                System.out.print(element + " ");
            }
            System.out.println();
        }
    }
}
