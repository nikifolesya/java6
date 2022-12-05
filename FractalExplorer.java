import java.awt.*;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.*;
import java.awt.geom.Rectangle2D;
import java.awt.event.*;

//Класс для отображения фрактала
public class FractalExplorer {
    private int displaySize;
    
    //Константы, строки-константы
    private static final String TITLE = "Fractal Explorer";
    private static final String RESET = "Reset Display";
    private static final String SAVE = "Save Display";
    private JImageDisplay display;
    private FractalGenerator fractal;
    private Rectangle2D.Double range;
    private JComboBox<FractalGenerator> comboBox;

    //Имплементируем интерфейс ActionListener для кнопки сброса
    class Handler implements ActionListener { // используется нериализованный метод (класс интерфейс), который реализуется, implements означает, что используются элементы интерфейса в классе, а extends означает, что создается подкласс от класса
        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
            if (RESET.equals(command)){
                fractal.getInitialRange(range);
                drawFractal();
            }
            else if ("comboBoxChanged".equals(command)){
                fractal = (FractalGenerator) comboBox.getSelectedItem();
                fractal.getInitialRange(range);
                drawFractal();
            }
            else if (SAVE.equals(command)){
                JFileChooser chooser = new JFileChooser();
                FileNameExtensionFilter filter = new FileNameExtensionFilter("PNG Images", "png");
                chooser.setFileFilter(filter);
                //запрещает ввыбирать файлы кроме png
                chooser.setAcceptAllFileFilterUsed(false); 
                if (chooser.showSaveDialog(display)==JFileChooser.APPROVE_OPTION){
                    File file = chooser.getSelectedFile(); //получили настройки для файла(имя, путь и тд)
                    String path = file.toString(); //путь
                    if (path.length()==0){
                        return;
                    }
                    if (!path.contains(".png")){
                        file = new File(path+".png");
                    }
                    try {
                        javax.imageio.ImageIO.write(display.getImage(), "png", file); //записал изображение в файл
                    }
                    //обработка ошибок
                    catch (Exception exception){
                        JOptionPane.showMessageDialog(display, exception.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }

            }
        }
    }
    //Наследуем MouseAdapter для обработки событий мыши
    class MouseHandler extends MouseAdapter {
        @Override //указывает на то, что мы переопределяем метод
        public void mouseClicked(MouseEvent e) { // считывает координаты мышки
            display.clearImage();
            int x = e.getX();
            // по координате дисплея получаем координату фрактала
            double xCoord = FractalGenerator.getCoord(range.x, range.x + range.width, displaySize, x);
            int y = e.getY();
            double yCoord = FractalGenerator.getCoord(range.y, range.y + range.height, displaySize, y);
            fractal.recenterAndZoomRange(range, xCoord, yCoord, 0.5);
            drawFractal();
        }
    }

    //Точка входа в программу
    public static void main(String[] args){
        FractalExplorer fractalExplorer = new FractalExplorer(800);
        fractalExplorer.createAndShowGUI();
    }

    //Конструктор класса
    public FractalExplorer(int displaySize){
        this.displaySize = displaySize;
        fractal = new Mandelbrot(); // создаётся объект типа ПОКА ЧТО МАНДЕЛЬБРУТ
        range = new Rectangle2D.Double(); //создаётся новый класс для хранения координат фрактала
        fractal.getInitialRange(range); //вызывается метод для определённого фрактала и в объект ренджь помещаются определённые координаты
    }

    //Метод для инициализации графического интерфейса Swing
    public void createAndShowGUI(){
        JFrame frame = new JFrame(TITLE); // устанавливается рамка с названием
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        display = new JImageDisplay(displaySize, displaySize);
        frame.add(display, BorderLayout.CENTER);

        Handler handler = new Handler();

        //объявление панелей
        JPanel top = new JPanel();
        JPanel bottom = new JPanel();

        JLabel label = new JLabel("Fractal: ");
        top.add(label,BorderLayout.WEST);


        //выбор фрактала
        comboBox = new JComboBox<FractalGenerator>(); //создали селектор
        comboBox.addItem(new Mandelbrot());
        comboBox.addItem(new Tricorn());
        comboBox.addItem(new BurningShip());
        comboBox.addActionListener(handler);
        top.add(comboBox,BorderLayout.EAST);

        JButton saveButton = new JButton(SAVE);
        saveButton.addActionListener(handler); //оброботчик для save
        bottom.add(saveButton,BorderLayout.WEST);

        JButton resetButton = new JButton(RESET); // кнопка
        resetButton.addActionListener(handler);
        bottom.add(resetButton, BorderLayout.EAST);

        MouseHandler click = new MouseHandler();
        display.addMouseListener(click);

        frame.add(bottom,BorderLayout.SOUTH);//рамка
        frame.add(top,BorderLayout.NORTH);

        frame.pack();
        frame.setVisible(true); //отрисовывает элементы интерфейса (делает видимым)
        frame.setResizable(false); //запрет на изменение размера экрана
        drawFractal();
    }

    //Метод для отрисовки фрактала
    private void drawFractal(){
        for(int i = 0; i < displaySize; i++){
            for(int j = 0; j < displaySize; j++){
                double xCoord = FractalGenerator.getCoord(range.x, range.x + range.width, displaySize, i);
                double yCoord = FractalGenerator.getCoord(range.y, range.y + range.height, displaySize, j);
                int iteration = fractal.numIterations(xCoord, yCoord);
                if (iteration == -1) { // точка не выходит за границы
                    display.drawPixel(i, j, 0);
                }
                else { //выбираем цвет на основе кол-ва итераций
                    float hue = 0.7f + (float) iteration / 200f;
                    int rgbColor = Color.HSBtoRGB(hue, 1f, 1f);
                    display.drawPixel(i, j, rgbColor);
                }
            }
            display.repaint(); // обновление изображения на экране
        }
    }
}