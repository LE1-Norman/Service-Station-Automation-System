package test.hibernate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.persistence.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Класс для интерфейса управления неисправностями.
 */
public class defect_interface {
    private JFrame defectList; // Основное окно интерфейса
    private DefaultTableModel model; // Модель таблицы для отображения данных
    private JToolBar toolBar; // Панель инструментов
    private JButton save; // Кнопка сохранения
    private JButton edit; // Кнопка редактирования
    private JButton delete; // Кнопка удаления
    private JButton adding; // Кнопка добавления новой записи
    private JButton back; // Кнопка возврата в основное меню
    private JButton open; // Кнопка открытия полного списка
    private JButton cancel; // Кнопка сброса выбора
    private JScrollPane scroll; // Полоса прокрутки для таблицы
    private JTable defects; // Таблица для отображения неисправностей
    private JComboBox field; // Выпадающий список для выбора поля фильтрации
    private JTextField fieldName; // Поле ввода для текста фильтрации
    private JButton filter; // Кнопка фильтрации

    private static final Logger logger = LogManager.getLogger(defect_interface.class); // Логгер для записи событий

    /**
     * Метод для удаления записи из таблицы и базы данных.
     */
    private void deleteRecord() {
        int selectedRow = defects.getSelectedRow(); // Получаем выбранную строку
        logger.warn("Попытка удалить запись: строка {}", selectedRow);

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(defects, "Выберите строку для удаления.");
            logger.warn("Строка для удаления не выбрана.");
            return; // Выход, если строка не выбрана
        }

        // Подтверждение удаления
        int confirm = JOptionPane.showConfirmDialog(defects, "Вы уверены, что хотите удалить эту запись?", "Подтверждение удаления", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            logger.info("Удаление записи отменено пользователем.");
            return; // Выход, если пользователь отменяет
        }

        // Создаем EntityManager для работы с базой данных
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("test_persistence"); // Замените на имя вашего блока постоянства
        EntityManager em = emf.createEntityManager();

        try {
            int idToDelete = (int) model.getValueAt(selectedRow, 0); // Получаем ID из первого столбца

            em.getTransaction().begin(); // Начинаем транзакцию

            // Находим сущность для удаления по ID
            Defect defectToDelete = em.find(Defect.class, idToDelete);

            if (defectToDelete != null) {
                em.remove(defectToDelete); // Удаляем из базы данных
                em.getTransaction().commit(); // Подтверждаем транзакцию
                model.removeRow(selectedRow); // Удаляем из интерфейса
                logger.info("Запись {} удалена из базы данных и интерфейса", idToDelete);
                JOptionPane.showMessageDialog(defects, "Запись успешно удалена.");
            } else {
                JOptionPane.showMessageDialog(defects, "Запись не найдена в базе данных.");
                logger.warn("Запись с ID {} не найдена в базе данных.", idToDelete);
            }

        } catch (RollbackException e) {
            JOptionPane.showMessageDialog(defects, "Ошибка удаления записи из базы данных: " + e.getMessage());
            logger.error("Ошибка удаления записи из базы данных: {}", e.getMessage(), e);
            em.getTransaction().rollback(); // Откат транзакции в случае ошибки
        } catch (Exception e) {
            JOptionPane.showMessageDialog(defects, "Ошибка удаления записи: " + e.getMessage());
            logger.error("Ошибка удаления записи: {}", e.getMessage(), e);
        } finally {
            if (em != null && em.isOpen()) {
                em.close(); // Закрываем EntityManager
            }
            if (emf != null && emf.isOpen()) {
                emf.close(); // Закрываем EntityManagerFactory
            }
        }
    }

    /**
     * Метод для редактирования записи в таблице и базе данных.
     */
    private void editRecord() {
        int selectedRow = defects.getSelectedRow(); // Получаем выбранную строку
        logger.warn("Попытка редактировать запись: строка {}", selectedRow);

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(defectList, "Выберите строку для редактирования.");
            logger.warn("Строка для редактирования не выбрана.");
            return; // Выход, если строка не выбрана
        }

        int idToUpdate = (int) model.getValueAt(selectedRow, 0); // Получаем ID (не должен изменяться)
        String name = (String) model.getValueAt(selectedRow, 1);
        String carName = model.getValueAt(selectedRow, 2).toString();

        JTextField nameField = new JTextField(name); // Поле для ввода нового названия

        // Извлечение списка автомобилей
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("test_persistence");
        EntityManager em = emf.createEntityManager();
        List<Car> cars = em.createQuery("SELECT c FROM Car c", Car.class).getResultList(); // Получаем список автомобилей

        // Создание JComboBox для выбора автомобиля
        JComboBox<Car> carComboBox = new JComboBox<>();
        for (Car car : cars) {
            carComboBox.addItem(car);
        }

        // Установка выбранного автомобиля (если есть)
        if (carName != null) {
            for (Car car : cars) {
                if (car.getCarName().equals(carName)) {
                    carComboBox.setSelectedItem(car);
                    break;
                }
            }
        }

        Object[] message = {
                "Название:", nameField,
                "Автомобиль:", carComboBox // Добавление выпадающего списка
        };

        int option = JOptionPane.showConfirmDialog(defectList, message, "Редактировать запись", JOptionPane.OK_CANCEL_OPTION);

        if (option == JOptionPane.OK_OPTION) {
            try {
                String newName = nameField.getText(); // Получаем новое название
                Car selectedCar = (Car) carComboBox.getSelectedItem(); // Получаем выбранный автомобиль
                Integer newCarId = selectedCar != null ? selectedCar.getCar_id() : null; // Получаем ID автомобиля

                // Валидация введенных данных
                if (newName.isEmpty() || newCarId == null) {
                    JOptionPane.showMessageDialog(defectList, "Пожалуйста, заполните все поля.");
                    return; // Прерываем выполнение метода
                }

                // Проверка, что поле "Название" содержит только буквы
                if (!newName.matches("[a-zA-Zа-яА-Я ]+")) {
                    JOptionPane.showMessageDialog(defectList, "Поле 'Название' может содержать только буквы.");
                    return;
                }

                em.getTransaction().begin(); // Начинаем транзакцию

                Defect defectToUpdate = em.find(Defect.class, idToUpdate); // Находим запись для обновления

                if (defectToUpdate != null) {
                    defectToUpdate.setDefectName(newName); // Устанавливаем новое название
                    defectToUpdate.setCar_id(newCarId); // Установка нового carId
                    em.getTransaction().commit(); // Подтверждаем транзакцию

                    String newCarName = selectedCar.getCarName(); // Получаем название автомобиля
                    model.setValueAt(newName, selectedRow, 1); // Обновляем модель с новым названием
                    model.setValueAt(newCarName, selectedRow, 2); // Обновляем модель с новым carName
                    logger.info("Запись {} обновлена в базе данных и интерфейса", idToUpdate);
                } else {
                    JOptionPane.showMessageDialog(defectList, "Запись не найдена в базе данных.");
                    logger.warn("Запись с ID {} не найдена в базе данных.", idToUpdate);
                }
            } catch (RollbackException e) {
                JOptionPane.showMessageDialog(defectList, "Ошибка обновления записи в базе данных: " + e.getMessage());
                logger.error("Ошибка обновления записи в базе данных: {}", e.getMessage(), e);
                em.getTransaction().rollback(); // Откат транзакции в случае ошибки
            } catch (Exception e) {
                JOptionPane.showMessageDialog(defectList, "Ошибка обновления записи: " + e.getMessage());
                logger.error("Ошибка обновления записи: {}", e.getMessage(), e);
            } finally {
                if (em != null && em.isOpen()) {
                    em.close(); // Закрываем EntityManager
                }
                if (emf != null && emf.isOpen()) {
                    emf.close(); // Закрываем EntityManagerFactory
                }
            }
        }
    }

    /**
     * Метод для открытия полного списка неисправностей из базы данных.
     */
    private void openFullList() {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("test_persistence"); // Создаем EntityManagerFactory
        EntityManager em = emf.createEntityManager(); // Создаем EntityManager

        try {
            TypedQuery<Defect> query = em.createQuery("SELECT d FROM Defect d", Defect.class); // Запрос для получения всех неисправностей
            List<Defect> allDefects = query.getResultList(); // Получаем список всех неисправностей

            // Получаем список автомобилей
            TypedQuery<Car> carQuery = em.createQuery("SELECT c FROM Car c", Car.class);
            List<Car> carsFromDb = carQuery.getResultList();

            // Создаем Map для быстрого поиска названия автомобиля по его ID
            Map<Integer, String> carIdToNameMap = new HashMap<>();
            for (Car car : carsFromDb) {
                carIdToNameMap.put(car.getCar_id(), car.getCarName()); // Предполагается, что у автомобиля есть метод getCarName()
            }

            String[] columns = {"ID", "Название", "Автомобиль"}; // Названия столбцов
            Object[][] data = new Object[allDefects.size()][columns.length]; // Данные для таблицы

            for (int i = 0; i < allDefects.size(); i++) {
                Defect defect = allDefects.get(i);
                data[i][0] = defect.getDefid(); // ID неисправности
                data[i][1] = defect.getDefectName(); // Название неисправности
                // Получаем название автомобиля по ID
                String carName = carIdToNameMap.get(defect.getCar_id());
                if (carName == null) {
                    carName = "Неизвестно"; // Значение по умолчанию, если автомобиль не найден
                }
                data[i][2] = carName; // Добавляем название автомобиля
            }

            model.setDataVector(data, columns); // Обновляем модель таблицы
            logger.info("Полный список неисправностей открыт.");
        } catch (Exception e) {
            logger.error("Ошибка загрузки неисправностей из базы данных: {}", e.getMessage(), e);
        } finally {
            if (em != null && em.isOpen()) {
                em.close(); // Закрываем EntityManager
            }
        }
    }

    /**
     * Метод для добавления новой записи о неисправности.
     */
    private void addRecord() {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("test_persistence");
        EntityManager em = emf.createEntityManager();
        JTextField nameField = new JTextField(); // Поле для ввода названия

        // Извлечение списка автомобилей
        List<Car> cars = em.createQuery("SELECT c FROM Car c", Car.class).getResultList();

        // Создание JComboBox для выбора автомобиля
        JComboBox<Car> carComboBox = new JComboBox<>();
        for (Car car : cars) {
            carComboBox.addItem(car);
        }

        Object[] message = {
                "Название:", nameField,
                "Автомобиль:", carComboBox // Добавление выпадающего списка
        };

        while (true) {
            int option = JOptionPane.showConfirmDialog(defectList, message, "Добавить запись", JOptionPane.OK_CANCEL_OPTION);
            if (option == JOptionPane.CANCEL_OPTION) {
                logger.info("Добавление записи отменено пользователем.");
                return; // Выход, если пользователь отменяет
            }

            String name = nameField.getText(); // Получаем название
            Car selectedCar = (Car) carComboBox.getSelectedItem(); // Получаем выбранный автомобиль
            Integer carId = selectedCar != null ? selectedCar.getCar_id() : null; // Получаем ID автомобиля

            // Валидация введенных данных
            if (name.isEmpty() || carId == null) {
                JOptionPane.showMessageDialog(defectList, "Пожалуйста, заполните все поля.");
                logger.warn("Попытка добавить запись с пустыми полями.");
                continue; // Продолжаем цикл для повторного ввода
            }

            // Проверка, что поле "Название" содержит только буквы
            if (!name.matches("[a-zA-Zа-яА-Я ]+")) {
                JOptionPane.showMessageDialog(defectList, "Поле 'Название' может содержать только буквы.");
                logger.warn("Поле 'Название' содержит недопустимые символы.");
                continue; // Продолжаем цикл для повторного ввода
            }

            try {
                em.getTransaction().begin(); // Начинаем транзакцию

                Defect defect = new Defect(); // Создаем новую запись о неисправности
                defect.setDefectName(name); // Устанавливаем название
                defect.setCar_id(carId); // Установка carId из выпадающего списка

                em.persist(defect); // Сохраняем запись в базе данных
                em.getTransaction().commit(); // Подтверждаем транзакцию

                // Получаем сгенерированный ID из базы данных
                int generatedId = defect.getDefid(); // Предполагается, что getDefid() возвращает автоматически сгенерированный ID

                String carName = selectedCar.getCarName(); // Получаем название автомобиля
                // Обновляем таблицу UI со сгенерированным ID
                model.addRow(new Object[]{generatedId, name, carName}); // Добавляем новую строку в таблицу
                logger.info("Добавлена новая запись в базу данных и интерфейс (ID: {})", generatedId);
                break; // Выходим из цикла после успешного добавления

            } catch (RollbackException e) {
                JOptionPane.showMessageDialog(defectList, "Ошибка добавления записи в базу данных: " + e.getMessage());
                logger.error("Ошибка добавления записи в базу данных: {}", e.getMessage(), e);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(defectList, "Ошибка добавления записи: " + e.getMessage());
                logger.error("Ошибка добавления записи: {}", e.getMessage(), e);
            }
        }

        // Закрываем EntityManager и EntityManagerFactory
        if (em != null && em.isOpen()) {
            em.close(); // Закрываем EntityManager
        }
        if (emf != null && emf.isOpen()) {
            emf.close(); // Закрываем EntityManagerFactory
        }
    }

    /**
     * Метод для фильтрации записей по выбранному полю и тексту.
     */
    private void filterRecords() {
        String selectedField = (String) field.getSelectedItem(); // Получаем выбранный столбец
        String searchText = fieldName.getText().toLowerCase(); // Получаем текст для поиска и преобразуем его в нижний регистр

        // Проверка на пустое поле или текст "Введите необходимый элемент"
        if (searchText.isEmpty() || searchText.equals("введите необходимый элемент")) {
            JOptionPane.showMessageDialog(defectList, "Пожалуйста, введите корректный текст для поиска.", "Ошибка поиска", JOptionPane.ERROR_MESSAGE);
            logger.warn("Попытка фильтрации с пустым полем или текстом 'Введите необходимый элемент'.");
            return; // Выходим из метода, не обновляя модель таблицы
        }

        logger.info("Фильтрация записей по полю: " + selectedField + ", текст: " + searchText);

        List<Object[]> filteredData = new ArrayList<>(); // Список для хранения отфильтрованных данных

        for (int row = 0; row < model.getRowCount(); row++) {
            String valueToCheck = "";
            // Получаем значение из выбранного столбца
            switch (selectedField) {
                case "ID":
                    valueToCheck = model.getValueAt(row, 0).toString(); // Получаем значение ID
                    break;
                case "Название":
                    valueToCheck = (String) model.getValueAt(row, 1); // Получаем значение названия
                    break;
                case "Автомобиль":
                    valueToCheck = model.getValueAt(row, 2).toString(); // Получаем значение названия автомобиля
                    break;
            }

            // Проверяем, содержит ли значение искомый текст
            if (valueToCheck.toLowerCase().contains(searchText)) {
                filteredData.add(new Object[]{
                        model.getValueAt(row, 0), // ID
                        model.getValueAt(row, 1), // Название
                        model.getValueAt(row, 2)  // Автомобиль
                });
            }
        }

        // Обновляем модель таблицы с отфильтрованными данными
        model.setRowCount(0); // Очищаем текущую модель
        if (filteredData.isEmpty()) { // Если отфильтрованные данные пусты, выводим сообщение
            logger.info("Нет записей, соответствующих вашему запросу.");
            JOptionPane.showMessageDialog(defectList, "Нет записей, соответствующих вашему запросу.", "Результаты поиска", JOptionPane.INFORMATION_MESSAGE);
        } else {
            for (Object[] rowData : filteredData) {
                model.addRow(rowData); // Добавляем отфильтрованные строки
            }
            logger.info("Записи отфильтрованы, найдено: " + filteredData.size() + " записей.");
        }
    }

    /**
     * Метод для сохранения данных в XML файл.
     */
    private void saveToXML() {
        // Получаем данные из модели таблицы
        List<Object[]> data = new ArrayList<>();
        for (int row = 0; row < model.getRowCount(); row++) {
            Object[] rowData = new Object[model.getColumnCount()]; // Создаем массив для строки
            for (int col = 0; col < model.getColumnCount(); col++) {
                rowData[col] = model.getValueAt(row, col); // Заполняем массив данными из модели
            }
            data.add(rowData); // Добавляем строку в список данных
        }

        // Определите путь к файлу и названия столбцов
        String filePath = "defects.xml"; // Путь к файлу
        String[] columnNames = {"ID", "Название", "Автомобиль"}; // Названия столбцов
        XMLExporter exporter = new XMLExporter(); // Создаем экземпляр класса для экспорта
        exporter.exportToXML(data, filePath, "service_station", columnNames, "defects"); // Экспортируем данные в XML
        logger.info("Данные сохранены в XML файл: " + filePath);
    }

    /**
     * Метод для отображения окна списка неисправностей.
     * Создает графический интерфейс
     * и добавляет функциональность для работы с неисправностями.
     */
    public void show2() {
        // Создание основного окна для отображения списка неисправностей
        defectList = new JFrame("Список неисправностей");
        defectList.setSize(500, 300);
        defectList.setLocation(100, 100);
        defectList.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Кнопка для сохранения списка неисправностей
        save = new JButton(new ImageIcon("Files/save.png"));
        save.setToolTipText("Сохранить список");
        save.addActionListener(e -> {
            logger.info("Пользователь нажал кнопку 'Сохранить'.");
            saveToXML(); // Метод для сохранения данных в XML
        });

        // Кнопка для возврата в основное меню
        back = new JButton(new ImageIcon("Files/back.png"));
        back.setToolTipText("Вернуться в основное меню");
        back.addActionListener(e -> {
            logger.info("Пользователь нажал кнопку 'Назад'.");
            defectList.dispose(); // Закрываем текущее окно
        });

        // Кнопка для добавления новой записи
        adding = new JButton(new ImageIcon("Files/add.png"));
        adding.setToolTipText("Добавить новую запись");
        adding.addActionListener(e -> {
            logger.info("Пользователь нажал кнопку 'Добавить запись'.");
            addRecord(); // Метод для добавления новой записи
        });

        // Кнопка для удаления записи
        delete = new JButton(new ImageIcon("Files/delete.png"));
        delete.setToolTipText("Удалить запись");
        delete.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                logger.info("Пользователь нажал кнопку 'Удалить запись'.");
                deleteRecord(); // Метод для удаления выбранной записи
            }
        });

        // Кнопка для редактирования записи
        edit = new JButton(new ImageIcon("Files/edit.png"));
        edit.setToolTipText("Изменить запись");
        edit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                logger.info("Пользователь нажал кнопку 'Изменить запись'.");
                editRecord(); // Вызываем метод редактирования
            }
        });

        // Кнопка для открытия полного списка
        open = new JButton(new ImageIcon("Files/open.png"));
        open.setToolTipText("Открыть полный список");
        open.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                logger.info("Пользователь нажал кнопку 'Открыть полный список'.");
                openFullList(); // Метод для открытия полного списка
            }
        });

        // Кнопка для сброса выбора
        cancel = new JButton(new ImageIcon("Files/cancel.png"));
        cancel.setToolTipText("Сбросить выбор");
        cancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                logger.info("Пользователь нажал кнопку 'Сбросить выбор'.");
                defects.clearSelection(); // Сбрасываем выбор в таблице
            }
        });

        // Создание панели инструментов и добавление кнопок
        toolBar = new JToolBar("Панель инструментов");
        toolBar.add(save);
        toolBar.add(back);
        toolBar.add(adding);
        toolBar.add(delete);
        toolBar.add(edit);
        toolBar.add(open);
        toolBar.add(cancel);

        // Установка компоновки окна и добавление панели инструментов
        defectList.setLayout(new BorderLayout());
        defectList.add(toolBar, BorderLayout.NORTH);

        // Создание EntityManager для работы с базой данных
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("test_persistence"); // Замените на ваше имя единицы хранения
        EntityManager em = emf.createEntityManager();

        try {
            // Запрос для получения списка неисправностей из базы данных
            TypedQuery<Defect> query = em.createQuery("SELECT d FROM Defect d", Defect.class);
            List<Defect> defectListFromDB = query.getResultList();

            // Запрос для получения списка автомобилей
            TypedQuery<Car> carQuery = em.createQuery("SELECT c FROM Car c", Car.class);
            List<Car> carsFromDb = carQuery.getResultList();

            // Создаем Map для быстрого поиска названия автомобиля по его ID
            Map<Integer, String> carIdToNameMap = new HashMap<>();
            for (Car car : carsFromDb) {
                carIdToNameMap.put(car.getCar_id(), car.getCarName()); // Предполагается, что у автомобиля есть метод getCarName()
            }

            // Определение заголовков таблицы
            String[] columns = {"ID", "Название", "Автомобиль"};
            Object[][] data = new Object[defectListFromDB.size()][columns.length];

            // Заполнение данных для таблицы
            for (int i = 0; i < defectListFromDB.size(); i++) {
                Defect defect = defectListFromDB.get(i);
                data[i][0] = defect.getDefid(); // ID неисправности
                data[i][1] = defect.getDefectName(); // Название неисправности
                // Получаем название автомобиля по ID
                String carName = carIdToNameMap.get(defect.getCar_id());
                if (carName == null) {
                    carName = "Неизвестно"; // Значение по умолчанию, если автомобиль не найден
                }
                data[i][2] = carName; // Добавляем название автомобиля
            }

            // Создание модели таблицы и добавление ее в JScrollPane
            model = new DefaultTableModel(data, columns);
            defects = new JTable(model);
            scroll = new JScrollPane(defects);
            defectList.add(scroll, BorderLayout.CENTER);

        } catch (Exception e) {
            logger.error("Error loading defects from database: {}", e.getMessage(), e);
            JOptionPane.showMessageDialog(defectList, "Error loading defects from the database.", "Database Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            // Закрытие EntityManager и EntityManagerFactory
            if (em != null && em.isOpen()) {
                em.close();
            }
            if (emf != null && emf.isOpen()) {
                emf.close();
            }
        }

        // Создание панели фильтрации
        field = new JComboBox(new String[]{"ID", "Название", "Автомобиль"});
        fieldName = new JTextField("Введите необходимый элемент");
        filter = new JButton("Поиск");
        filter.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                logger.info("Пользователь нажал кнопку 'Поиск'.");
                filterRecords(); // Вызов метода фильтрации при нажатии кнопки поиска
            }
        });

        // Добавление элементов фильтрации в панель
        JPanel filterPanel = new JPanel();
        filterPanel.add(field);
        filterPanel.add(fieldName);
        filterPanel.add(filter);

        // Добавление панели фильтрации в окно
        defectList.add(filterPanel, BorderLayout.SOUTH);

        // Отображение окна
        defectList.setVisible(true);
    }
}
