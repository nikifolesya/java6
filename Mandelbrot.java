import java.awt.geom.Rectangle2D;

//Класс фрактала множества мандельброта, наследуемый от генератора фракталов
public class Mandelbrot extends FractalGenerator{
    //Константа с максимальным количеством итераций
    public static final int MAX_ITERATIONS = 2000;

    //Переопределение метода для получения исходного диапазона на определённое комп.число
    @Override
    public void getInitialRange(Rectangle2D.Double range) { 
        //начальные координаты фрактала
        range.x = -2;
        range.y = -1.5;
        range.width = 3;
        range.height = 3;
    }

    //Переопределение метода для получения кол-ва итераций для текущей координаты
    @Override
    public int numIterations(double x, double y) {
        int iteration = 0;
        double zRe = 0;
        double zIm = 0;
        double zRe2 = 0;
        double zIm2 = 0;
        while(iteration < MAX_ITERATIONS && (zRe2 + zIm2) < 4)
        {
            zIm = (2 * zRe * zIm) + y; //считается мнимая часть числа
            zRe = (zRe2 - zIm2) + x; //считается действ. часть числа

            zRe2 = zRe*zRe;
            zIm2 = zIm*zIm;
            iteration++;
        }
        if (iteration == MAX_ITERATIONS) {
            return -1;
        }
        return iteration;
    }
    public String toString() {
        return "Mandelbrot";
    }

}