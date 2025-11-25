package test.hibernate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Главный интерфейс приложения, который отображает кнопки для доступа к различным разделам.
 */
public class MainInterface {
    private JFrame mainFrame; // Главное окно приложения
    private JButton specializationButton; // Кнопка для отображения списка специальностей
    private JButton carButton; // Кнопка для отображения списка автомобилей
    private JButton defectButton; // Кнопка для отображения списка неисправностей
    private JButton renderedServiceButton; // Кнопка для отображения списка оказанных услуг
    private JButton customerButton; // Кнопка для отображения списка клиентов
    private JButton workerButton; // Кнопка для отображения списка рабочих

    private static final Logger logger = LogManager.getLogger(MainInterface.class); // Логгер для записи информации

    /**
     * Метод для отображения главного интерфейса.
     * Создает окно, добавляет кнопки и обрабатывает их действия.
     */
    public void show() {
        logger.info("Запуск главного интерфейса");
        mainFrame = new JFrame("Главный интерфейс"); // Создание главного окна с заголовком
        mainFrame.setSize(500, 300); // Установка размера окна
        mainFrame.setLocation(100, 100); // Установка положения окна на экране
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Завершение работы приложения при закрытии окна

        // Инициализация кнопки "Список специальностей"
        specializationButton = new JButton("Список специальностей");
        specializationButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                logger.info("Кнопка 'Список специальностей' нажата");
                new Specialization_interface().show0(); // Переход к интерфейсу специальностей
            }
        });

        // Инициализация кнопки "Список автомобилей"
        carButton = new JButton("Список автомобилей");
        carButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                logger.info("Кнопка 'Список автомобилей' нажата");
                new car_interface().show1(); // Переход к интерфейсу автомобилей
            }
        });

        // Инициализация кнопки "Список неисправностей"
        defectButton = new JButton("Список неисправностей");
        defectButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                logger.info("Кнопка 'Список неисправностей' нажата");
                new defect_interface().show2(); // Переход к интерфейсу неисправностей
            }
        });

        // Инициализация кнопки "Список оказанных услуг"
        renderedServiceButton = new JButton("Список оказанных услуг");
        renderedServiceButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                logger.info("Кнопка 'Список оказанных услуг' нажата");
                new rendered_services_interface().show3(); // Переход к интерфейсу оказанных услуг
            }
        });

        // Инициализация кнопки "Список клиентов"
        customerButton = new JButton("Список клиентов");
        customerButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                logger.info("Кнопка 'Список клиентов' нажата");
                new customers_interface().show4(); // Переход к интерфейсу клиентов
            }
        });

        // Инициализация кнопки "Список рабочих"
        workerButton = new JButton("Список рабочих");
        workerButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                logger.info("Кнопка 'Список рабочих' нажата");
                new workers_interface().show5(); // Переход к интерфейсу рабочих
            }
        });

        // Создание панели для размещения кнопок
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(6, 1)); // Установка сеточного расположения для кнопок
        panel.add(specializationButton); // Добавление кнопки "Список специальностей"
        panel.add(carButton); // Добавление кнопки "Список автомобилей"
        panel.add(defectButton); // Добавление кнопки "Список неисправностей"
        panel.add(renderedServiceButton); // Добавление кнопки "Список оказанных услуг"
        panel.add(customerButton); // Добавление кнопки "Список клиентов"
        panel.add(workerButton); // Добавление кнопки "Список рабочих"

        mainFrame.add(panel, BorderLayout.CENTER); // Добавление панели в главное окно
        mainFrame.setVisible(true); // Отображение главного окна
        logger.info("Главный интерфейс отображен");
    }

    /**
     * Точка входа в приложение.
     * @param args аргументы командной строки
     */
    public static void main(String[] args) {
        new MainInterface().show(); // Создание и отображение главного интерфейса
    }
}