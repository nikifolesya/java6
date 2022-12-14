import java.awt.*;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.awt.geom.Rectangle2D;
import java.awt.event.*;
import java.io.*;

//Класс для отображения фрактала
public class FractalExplorer {
    private int displaySize;
    private int rowsRemaining;
    //Константы, хардкоженные строки
    private static final String TITLE = "Fractal Explorer";
    private static final String RESET = "Reset Display";
    private static final String SAVE = "Save Image";
    private static final String CHOOSE = "Fractal:";
    private static final String COMBOBOX_CHANGE = "comboBoxChanged";
    private static final String SAVE_ERROR = "Ошибка при сохранении изображения";
    private JImageDisplay display;
    private FractalGenerator fractal;
    private Rectangle2D.Double range;
    private JComboBox<FractalGenerator> comboBox;
    private JButton resetButton;
    private JButton saveButton;

    //Имплементируем интерфейс ActionListener для обработки событий
    class ActionsHandler implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
            if(command.equals(RESET)){
                fractal.getInitialRange(range);
                drawFractal();
            } else if (command.equals(COMBOBOX_CHANGE)) {
                //new!!!
                JComboBox<FractalGenerator> source = (JComboBox) e.getSource();
                fractal = (FractalGenerator) source.getSelectedItem();
                fractal.getInitialRange(range);
                display.clearImage();
                drawFractal();
            } else if (command.equals(SAVE)) {
                JFileChooser fileChooser = new JFileChooser();
                FileNameExtensionFilter filter = new FileNameExtensionFilter("PNG Images", "png");
                fileChooser.setFileFilter(filter);
                fileChooser.setAcceptAllFileFilterUsed(false);
                if(fileChooser.showSaveDialog(display) == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();
                    String path = file.toString();
                    if(path.length() == 0) return;
                    if(!path.contains(".png")){
                        file = new File(path + ".png");
                    }
                    try {
                        javax.imageio.ImageIO.write(display.getImage(), "png", file);
                    } catch (Exception exception) {
                        JOptionPane.showMessageDialog(display, exception.getMessage(), SAVE_ERROR, JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        }
    }
    //Наследуем MouseAdapter для обработки событий мыши
    class MouseHandler extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            if(rowsRemaining != 0) return;
            display.clearImage();
            int x = e.getX();
            //по координате дисплея получаем координату фрактала
            double xCoord = FractalGenerator.getCoord(range.x, range.x + range.width, displaySize, x);
            int y = e.getY();
            double yCoord = FractalGenerator.getCoord(range.y, range.y + range.height, displaySize, y);
            fractal.recenterAndZoomRange(range, xCoord, yCoord, 0.5);
            drawFractal();
        }
    }

    // NEW
    /* вычисление значений цвета для одной строки фрактала, поэтому ему потребуются два поля: 
    целочисленная y-координата вычисляемой строки, и массив чисел 
    типа int для хранения вычисленных значений RGB для каждого пикселя в этой строке. 
    Конструктор должен будет получать y-координату в качестве параметра и сохранять это. 
    (На данном этапе не надо выделять память под целочисленный 
    массив, так как он не потребуется, пока строка не будет вычислена) */
    class FractalWorker extends javax.swing.SwingWorker<Object, Object> { 
        private int y;//вычисляемая строка
        private int[] rgb; //хранение вычисленных значений RGB
        public FractalWorker(int y){
            this.y = y;
        }
        
        /*вызывается в фоновом потоке и отвечает за выполнение длительной задачи. 
        «draw fractal» помещаем в этот метод. 
        Вместо того, чтобы рисовать изображение в окне, цикл должен будет сохранить каждое значение RGB в соответствующем элементе целочисленного массива. 
        Вы не сможете изменять отображение из этого потока, потому что вы нарушите ограничения ограничения потоков Swing. */
        @Override
        protected Object doInBackground(){ // добавляем пиксели в массив
            rgb = new int[displaySize];
            int color;
            for(int x = 0; x < displaySize; x++){
                double xCoord = FractalGenerator.getCoord(range.x, range.x + range.width, displaySize, x);
                double yCoord = FractalGenerator.getCoord(range.y, range.y + range.height, displaySize, y);
                int iteration = fractal.numIterations(xCoord, yCoord);
                color = 0;
                if(iteration > 0){
                    float hue = 0.7f + (float) iteration / 200f;
                    color = Color.HSBtoRGB(hue, 1f, 1f);
                }
                rgb[x] = color;
            }
            return null;
        }

        /*вызывается, когда фоновая задача завершена, и этот метод 
        вызывается из потока обработки событий Swing. 
        Поэтому в этом методе вы можете перебирать массив строк данных, 
        рисуя пиксели, которые были вычислены в doInBackground(). */
        @Override
        protected void done() { //отрисовывает фрактал
            for(int x = 0; x < displaySize; x++){
                display.drawPixel(x, y, rgb[x]);
            }
            display.repaint(0, 0, y, displaySize, 1);// область для перерисовки
            rowsRemaining--;
            if(rowsRemaining == 0) enableUI(true);
        }
    }
    //Точка входа в программу
    public static void main(String[] args){
        FractalExplorer fractalExplorer = new FractalExplorer(600);
        fractalExplorer.createAndShowGUI();
    }

    //Конструктор класса
    public FractalExplorer(int displaySize){
        this.displaySize = displaySize;
        fractal = new Mandelbrot();
        range = new Rectangle2D.Double(); //хранение координат фрактала
        fractal.getInitialRange(range); //вызов метода для фрактала
    }

    //Метод для инициализации графического интерфейса Swing
    public void createAndShowGUI(){
        ActionsHandler actionsHandler = new ActionsHandler();
        //рамка с названием
        JFrame frame = new JFrame(TITLE);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Display
        display = new JImageDisplay(displaySize, displaySize);
        frame.add(display, BorderLayout.CENTER);

        //объявление панелей
        JPanel topPanel = new JPanel();
        JPanel bottomPanel = new JPanel();

        //выборка фрактала
        JLabel label = new JLabel(CHOOSE);
        topPanel.add(label);

        //ComboBox - выбор фрактала
        comboBox = new JComboBox<>(); //селектор
        comboBox.addItem(new Mandelbrot());
        comboBox.addItem(new Tricorn());
        comboBox.addItem(new BurningShip());
        comboBox.addActionListener(actionsHandler);
        topPanel.add(comboBox, BorderLayout.NORTH);


        //Save Button
        saveButton = new JButton(SAVE);
        saveButton.addActionListener(actionsHandler); //обработчик save
        bottomPanel.add(saveButton, BorderLayout.WEST);

        //Reset Button
        resetButton = new JButton(RESET); //кнопка сброса
        resetButton.addActionListener(actionsHandler);
        bottomPanel.add(resetButton, BorderLayout.EAST);



        frame.add(bottomPanel, BorderLayout.SOUTH); //рамка
        frame.add(topPanel, BorderLayout.NORTH);

        //Mouse Handler
        MouseHandler click = new MouseHandler();
        display.addMouseListener(click);

        //Misc
        frame.pack();
        frame.setVisible(true); //отрисовка элементов
        frame.setResizable(false); //запрет на изменение размера экрана
        drawFractal();
    }

    private void enableUI(boolean val){ 
        //функция, которая включает и выключает кнопки
        comboBox.setEnabled(val);
        resetButton.setEnabled(val);
        saveButton.setEnabled(val);
    }

    //Метод для отрисовки фрактала
    private void drawFractal(){
        enableUI(false); //вызывает метод, чтобы отключить все элементы во время перерисовки
        rowsRemaining = displaySize; //устанавливает значение «rows remaining» равным общему количеству строк, которые нужно нарисовать
        for(int y = 0; y < displaySize; y++){
            FractalWorker worker = new FractalWorker(y);
            worker.execute(); // запускает многопоточность
        }
    }
}