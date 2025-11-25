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
 * Класс car_interface представляет собой интерфейс для управления автомобилями.
 * Он предоставляет функции для добавления, редактирования, удаления и фильтрации записей,
 * а также для сохранения данных в XML.
 */

public class car_interface {
    private JFrame carList; // Главное окно для отображения списка автомобилей
    private DefaultTableModel model; // Модель таблицы для отображения данных
    private JToolBar toolBar; // Панель инструментов
    private JButton save; // Кнопка для сохранения данных
    private JButton edit; // Кнопка для редактирования записи
    private JButton delete; // Кнопка для удаления записи
    private JButton adding; // Кнопка для добавления новой записи
    private JButton back; // Кнопка для возврата в главное меню
    private JButton open; // Кнопка для открытия полного списка
    private JButton cancel; // Кнопка для сброса выбора
    private JScrollPane scroll; // Прокручиваемая панель для таблицы
    private JTable cars; // Таблица для отображения автомобилей
    private JComboBox field; // Выпадающий список для выбора поля фильтрации
    private JTextField fieldName; // Поле для ввода текста поиска
    private JButton filter; // Кнопка для фильтрации записей

    private static final Logger logger = LogManager.getLogger(car_interface.class); // Логгер для отслеживания событий

    /**
     * Проверяет корректность введенных данных для автомобиля.
     *
     * @param name Название автомобиля
     * @param bodyType Тип кузова
     * @param yearStr Год выпуска
     * @param powerStr Мощность двигателя
     * @param maxSpeedStr Максимальная скорость
     * @param mileageStr Пробег
     * @param stcNumber Номер СТС
     * @param ptsNumber Номер ПТС
     * @throws Exception Если данные некорректны
     */
    private void validateFields(String name, String bodyType, String yearStr, String powerStr, String maxSpeedStr, String mileageStr, String stcNumber, String ptsNumber) throws Exception {
        if (name.isEmpty() || bodyType.isEmpty() || yearStr.isEmpty() || powerStr.isEmpty() ||
                maxSpeedStr.isEmpty() || mileageStr.isEmpty()) {
            throw new Exception("Пожалуйста, заполните все поля.");
        }
        if (!bodyType.matches("[a-zA-Zа-яА-Я]+")) {
            throw new Exception("Поле 'Тип кузова' может содержать только буквы.");
        }
        if (!yearStr.matches("\\d+") || Integer.parseInt(yearStr) <= 0) {
            throw new Exception("Поле 'Год выпуска' должно быть положительным числом.");
        }
        if (!powerStr.matches("\\d+") || Integer.parseInt(powerStr) <= 0) {
            throw new Exception("Поле 'Мощность' должно быть положительным числом.");
        }
        if (!maxSpeedStr.matches("\\d+") || Integer.parseInt(maxSpeedStr) <= 0) {
            throw new Exception("Поле 'Максимальная скорость' должно быть положительным числом.");
        }
        if (!mileageStr.matches("\\d+") || Integer.parseInt(mileageStr) <= 0) {
            throw new Exception("Поле 'Пробег' должно быть положительным числом.");
        }

        // Проверка номера СТС (10 символов, формат ччББчччччч)
        if (!stcNumber.matches("\\d{2}[A-Z]{2}\\d{6}")) {
            throw new Exception("Поле 'Номер СТС' должно быть содержать 10 символов и иметь формат ччББчччччч.");
        }

        // Проверка номера ПТС (10 символов, формат ччББчччччч)
        if (!ptsNumber.matches("\\d{2}[A-Z]{2}\\d{6}")) {
            throw new Exception("Поле 'Номер ПТС' должно быть содержать 10 символов и иметь формат ччББчччччч.");
        }
    }

    /**
     * Удаляет выбранную запись из списка автомобилей.
     */
    private void deleteRecord() {
        int selectedRow = cars.getSelectedRow(); // Получаем индекс выбранной строки
        logger.warn("Попытка удалить запись: строка " + selectedRow);
        if (selectedRow != -1) {
            int confirm = JOptionPane.showConfirmDialog(carList, "Вы уверены, что хотите удалить эту запись?", "Подтверждение удаления", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                EntityManagerFactory emf = Persistence.createEntityManagerFactory("test_persistence");
                Integer carIdObj = (Integer) model.getValueAt(selectedRow, 0); // Получаем ID автомобиля
                int carId = carIdObj != null ? carIdObj.intValue() : -1;

                if (carId != -1) {
                    EntityManager em = emf.createEntityManager();
                    EntityTransaction transaction = null;
                    try {
                        transaction = em.getTransaction();
                        transaction.begin();

                        Car carToDelete = em.find(Car.class, carId); // Находим автомобиль по ID
                        if (carToDelete != null) {
                            em.remove(carToDelete); // Удаляем автомобиль
                            transaction.commit();

                            model.removeRow(selectedRow); // Удаляем строку из таблицы
                            model.fireTableDataChanged(); // Обновляем модель таблицы

                            logger.info("Запись успешно удалена из базы данных.");
                            JOptionPane.showMessageDialog(carList, "Запись успешно удалена!");
                        } else {
                            logger.warn("Запись не найдена в базе данных.");
                            JOptionPane.showMessageDialog(carList, "Запись не найдена в базе данных.");
                            if (transaction != null && transaction.isActive()) {
                                transaction.rollback();
                            }
                        }
                    } catch (Exception e) {
                        if (transaction != null && transaction.isActive()) {
                            transaction.rollback();
                        }
                        logger.error("Database error during delete: " + e.getMessage(), e);
                        JOptionPane.showMessageDialog(carList, "Database error during delete.");
                    } finally {
                        if (em != null && em.isOpen()) {
                            em.close(); // Закрываем EntityManager
                        }
                    }
                } else {
                    JOptionPane.showMessageDialog(cars, "Ошибка получения ID записи.");
                    logger.error("Ошибка получения ID записи.");
                }
            }
        } else {
            JOptionPane.showMessageDialog(cars, "Выберите строку для удаления.");
            logger.warn("Не выбрана строка для удаления.");
        }
    }

    /**
     * Редактирует выбранную запись автомобиля.
     */
    private void editRecord() {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("test_persistence");
        int selectedRow = cars.getSelectedRow(); // Получаем индекс выбранной строки
        if (selectedRow != -1) {
            Integer carIdObj = (Integer) model.getValueAt(selectedRow, 0); // Получаем ID автомобиля
            int carId = carIdObj != null ? carIdObj.intValue() : -1;

            // Получаем данные из выбранной строки
            Object nameObj = model.getValueAt(selectedRow, 1);
            Object bodyTypeObj = model.getValueAt(selectedRow, 2);
            Integer yearInt = (Integer.parseInt(model.getValueAt(selectedRow, 3).toString()));
            Integer powerInt = (Integer.parseInt(model.getValueAt(selectedRow, 4).toString()));
            Integer maxSpeedInt = (Integer.parseInt(model.getValueAt(selectedRow, 5).toString()));
            Integer mileageInt = (Integer.parseInt(model.getValueAt(selectedRow, 6).toString()));
            String owner = model.getValueAt(selectedRow, 7).toString();
            String stcNumberObj = (String) model.getValueAt(selectedRow, 8); // Номер СТС
            String ptsNumberObj = (String) model.getValueAt(selectedRow, 9); // Номер ПТС

            // Создаем текстовые поля для редактирования
            JTextField nameField = new JTextField(nameObj != null ? nameObj.toString() : "");
            JTextField bodyTypeField = new JTextField(bodyTypeObj != null ? bodyTypeObj.toString() : "");
            JTextField yearField = new JTextField(yearInt != null ? String.valueOf(yearInt) : "");
            JTextField powerField = new JTextField(powerInt != null ? String.valueOf(powerInt) : "");
            JTextField maxSpeedField = new JTextField(maxSpeedInt != null ? String.valueOf(maxSpeedInt) : "");
            JTextField mileageField = new JTextField(mileageInt != null ? String.valueOf(mileageInt) : "");
            JTextField stcField = new JTextField(stcNumberObj); // Поле для номера СТС
            JTextField ptsField = new JTextField(ptsNumberObj); // Поле для номера ПТС

            // Извлечение списка клиентов
            EntityManager em = emf.createEntityManager();
            List<Customer> customers = em.createQuery("SELECT c FROM Customer c", Customer.class).getResultList();

            // Создание JComboBox для выбора клиента
            JComboBox<Customer> customerComboBox = new JComboBox<>();
            for (Customer customer : customers) {
                customerComboBox.addItem(customer);
            }

            // Установка выбранного клиента (если есть)
            if (owner != null) {
                for (Customer customer : customers) {
                    if (customer.getFullName().equals(owner)) {
                        customerComboBox.setSelectedItem(customer);
                        break;
                    }
                }
            }

            JLabel idLabel = new JLabel("ID: " + carId); // Метка для отображения ID автомобиля
            Object[] message = {
                    idLabel,
                    "Название:", nameField,
                    "Тип кузова:", bodyTypeField,
                    "Год выпуска:", yearField,
                    "Мощность двигателя (л.с.):", powerField,
                    "Максимальная скорость (км/ч):", maxSpeedField,
                    "Пробег (км):", mileageField,
                    "Клиент:", customerComboBox, // Добавление выпадающего списка
                    "Номер СТС:", stcField,
                    "Номер ПТС:", ptsField
            };

            int option = JOptionPane.showConfirmDialog(carList, message, "Редактировать запись", JOptionPane.OK_CANCEL_OPTION);
            if (option == JOptionPane.OK_OPTION) {
                // Валидация введенных данных
                try {
                    validateFields(nameField.getText(), bodyTypeField.getText(), yearField.getText(), powerField.getText(), maxSpeedField.getText(), mileageField.getText(), stcField.getText(), ptsField.getText());
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(carList, e.getMessage());
                    return; // Прерываем выполнение метода
                }

                // Проверка текстовых полей на наличие цифр
                if (!bodyTypeField.getText().matches("[a-zA-Zа-яА-Я ]+")) {
                    JOptionPane.showMessageDialog(carList, "Поле 'Тип кузова' может содержать только буквы.");
                    return;
                }

                // Проверка числовых полей на положительные числа
                try {
                    int year = Integer.parseInt(yearField.getText());
                    int power = Integer.parseInt(powerField.getText());
                    int maxSpeed = Integer.parseInt(maxSpeedField.getText());
                    int mileage = Integer.parseInt(mileageField.getText());

                    if (year <= 0 || power <= 0 || maxSpeed <= 0 || mileage < 0) {
                        JOptionPane.showMessageDialog(carList, "Числовые поля должны содержать только положительные числа.");
                        return;
                    }

                    EntityTransaction transaction = null;
                    try {
                        transaction = em.getTransaction();
                        transaction.begin();

                        Car carToUpdate = em.find(Car.class, carId); // Находим автомобиль по ID
                        if (carToUpdate != null) {
                            // Обновляем данные автомобиля
                            carToUpdate.setCarName(nameField.getText());
                            carToUpdate.setBodyType(bodyTypeField.getText());
                            carToUpdate.setReleaseYear(year);
                            carToUpdate.setEnginePower(power);
                            carToUpdate.setMaxSpeed(maxSpeed);
                            carToUpdate.setMileage(mileage);

                            // Получение выбранного клиента из JComboBox
                            Customer selectedCustomer = (Customer) customerComboBox.getSelectedItem();
                            if (selectedCustomer != null) {
                                carToUpdate.setCar_cust_id(selectedCustomer.getCustomer_id()); // Установка ID клиента
                            }

                            // Установка новых значений для номера СТС и ПТС
                            carToUpdate.setSts(stcField.getText());
                            carToUpdate.setPts(ptsField.getText());

                            em.merge(carToUpdate); // Обновляем данные в базе данных
                            transaction.commit();

                            // Обновление JTable
                            model.setValueAt(nameField.getText(), selectedRow, 1);
                            model.setValueAt(bodyTypeField.getText(), selectedRow, 2);
                            model.setValueAt(yearField.getText(), selectedRow, 3);
                            model.setValueAt(powerField.getText(), selectedRow, 4);
                            model.setValueAt(maxSpeedField.getText(), selectedRow, 5);
                            model.setValueAt(mileageField.getText(), selectedRow, 6);
                            model.setValueAt(selectedCustomer.getFullName(), selectedRow, 7); // Обновление владельца
                            model.setValueAt(stcField.getText(), selectedRow, 8); // Обновление номера СТС
                            model.setValueAt(ptsField.getText(), selectedRow, 9); // Обновление номера ПТС
                            model.fireTableDataChanged(); // Обновляем модель таблицы

                            logger.info("Запись успешно отредактирована в базе данных.");
                            JOptionPane.showMessageDialog(carList, "Запись успешно отредактирована!");
                        } else {
                            logger.warn("Запись не найдена в базе данных.");
                            JOptionPane.showMessageDialog(carList, "Запись не найдена в базе данных.");
                            if (transaction != null && transaction.isActive()) {
                                transaction.rollback();
                            }
                        }
                    } catch (Exception e) {
                        if (transaction != null && transaction.isActive()) {
                            transaction.rollback();
                        }
                        logger.error("Ошибка базы данных при обновлении: " + e.getMessage(), e);
                        JOptionPane.showMessageDialog(carList, "Ошибка базы данных при обновлении.");
                    }
                } catch (NumberFormatException nfe) {
                    JOptionPane.showMessageDialog(carList, "Пожалуйста, введите корректные числовые значения.");
                    return;
                } finally {
                    if (em != null && em.isOpen()) {
                        em.close(); // Закрываем EntityManager
                    }
                }
            }
        } else {
            JOptionPane.showMessageDialog(carList, "Выберите строку для редактирования.");
        }
    }

    /**
     * Открывает полный список автомобилей из базы данных и отображает их в таблице.
     */
    private void openFullList() {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("test_persistence");
        EntityManager em = emf.createEntityManager();
        try {
            String[] columns = {"ID", "Название", "Тип кузова", "Год выпуска", "Мощность двигателя (л.с.)", "Максимальная скорость (км/ч)", "Пробег (км)", "Владелец", "Номер СТС", "Номер ПТС"};
            model.setColumnIdentifiers(columns); // Установка названий столбцов
            model.setRowCount(0); // Очистка существующих строк

            TypedQuery<Car> query = em.createQuery("SELECT c FROM Car c", Car.class);
            List<Car> cars = query.getResultList(); // Получаем список автомобилей

            TypedQuery<Customer> clientQuery = em.createQuery("SELECT c FROM Customer c", Customer.class);
            List<Customer> clientsFromDb = clientQuery.getResultList(); // Получаем список клиентов

            // Создаем Map для быстрого поиска имени клиента по его ID
            Map<Integer, String> clientIdToNameMap = new HashMap<>();
            for (Customer client : clientsFromDb) {
                clientIdToNameMap.put(client.getCustomer_id(), client.getFullName());
            }

            // Добавляем автомобили в модель
            for (Car car : cars) {
                String clientName = clientIdToNameMap.get(car.getCar_cust_id()); // Получаем имя клиента по ID
                if (clientName == null) {
                    clientName = "Неизвестно"; // Значение по умолчанию, если клиент не найден
                }

                model.addRow(new Object[]{
                        car.getCar_id(),
                        car.getCarName(),
                        car.getBodyType(),
                        car.getReleaseYear(),
                        car.getEnginePower(),
                        car.getMaxSpeed(),
                        car.getMileage(),
                        clientName,
                        car.getSts(), // Добавление номера СТС
                        car.getPts()  // Добавление номера ПТС
                });
            }
            logger.info("Открыт полный список.");
        } catch (Exception e) {
            logger.error("Error fetching data from database: " + e.getMessage(), e);
            JOptionPane.showMessageDialog(carList, "Error fetching data from database.");
        } finally {
            if (em != null && em.isOpen()) {
                em.close(); // Закрываем EntityManager
            }
        }
    }

    /**
     * Добавляет новую запись автомобиля в базу данных.
     */
    private void addRecord() {
        // Создаем текстовые поля для ввода данных
        JTextField nameField = new JTextField();
        JTextField bodyTypeField = new JTextField();
        JTextField yearField = new JTextField();
        JTextField powerField = new JTextField();
        JTextField maxSpeedField = new JTextField();
        JTextField mileageField = new JTextField();
        JTextField stcField = new JTextField(); // Поле для номера СТС
        JTextField ptsField = new JTextField(); // Поле для номера ПТС

        // Создание JComboBox для выбора клиента
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("test_persistence");
        EntityManager em = emf.createEntityManager();
        List<Customer> customers = em.createQuery("SELECT c FROM Customer c", Customer.class).getResultList();
        JComboBox<Customer> customerComboBox = new JComboBox<>();

        for (Customer customer : customers) {
            customerComboBox.addItem(customer); // Добавляем клиентов в выпадающий список
        }

        Object[] message = {
                "Название:", nameField,
                "Тип кузова:", bodyTypeField,
                "Год выпуска:", yearField,
                "Мощность двигателя (л.с.):", powerField,
                "Максимальная скорость (км/ч):", maxSpeedField,
                "Пробег (км):", mileageField,
                "Владелец:", customerComboBox, // Добавление выпадающего списка для выбора владельца
                "Номер СТС:", stcField,
                "Номер ПТС:", ptsField
        };

        while (true) {
            int option = JOptionPane.showConfirmDialog(carList, message, "Добавить запись", JOptionPane.OK_CANCEL_OPTION);
            if (option == JOptionPane.CANCEL_OPTION) {
                logger.info("Добавление отменено.");
                return; // Прерываем выполнение метода, если добавление отменено
            }

            // Валидация введенных данных
            try {
                validateFields(nameField.getText(), bodyTypeField.getText(), yearField.getText(), powerField.getText(), maxSpeedField.getText(), mileageField.getText(), stcField.getText(), ptsField.getText());
            } catch (Exception e) {
                JOptionPane.showMessageDialog(carList, e.getMessage());
                logger.warn(e.getMessage());
                continue; // Возвращаемся к началу цикла
            }

            EntityTransaction transaction = null;
            try {
                transaction = em.getTransaction();
                transaction.begin();

                Car newCar = new Car(); // Создаем новый объект Car
                newCar.setCarName(nameField.getText());
                newCar.setBodyType(bodyTypeField.getText());
                newCar.setReleaseYear(Integer.parseInt(yearField.getText()));
                newCar.setEnginePower(Integer.parseInt(powerField.getText()));
                newCar.setMaxSpeed(Integer.parseInt(maxSpeedField.getText()));
                newCar.setMileage(Integer.parseInt(mileageField.getText()));

                // Получение выбранного клиента из JComboBox
                Customer selectedCustomer = (Customer) customerComboBox.getSelectedItem();
                if (selectedCustomer != null) {
                    newCar.setCar_cust_id(selectedCustomer.getCustomer_id()); // Установка ID клиента
                }

                // Установка значений для номера СТС и ПТС
                newCar.setSts(stcField.getText());
                newCar.setPts(ptsField.getText());

                em.persist(newCar); // Сохраняем новый автомобиль в базе данных
                transaction.commit();

                int generatedId = newCar.getCar_id(); // Получаем ID нового автомобиля

                // Добавляем новую строку в модель таблицы
                model.addRow(new Object[]{
                        generatedId,
                        nameField.getText(),
                        bodyTypeField.getText(),
                        yearField.getText(),
                        powerField.getText(),
                        maxSpeedField.getText(),
                        mileageField.getText(),
                        selectedCustomer != null ? selectedCustomer.getFullName() : "",
                        stcField.getText(),
                        ptsField.getText()
                });
                logger.info("Добавлена новая запись в базу данных. ID: " + generatedId);
                JOptionPane.showMessageDialog(carList, "Запись добавлена успешно!");
                break; // Выход из цикла, если запись добавлена успешно
            } catch (NumberFormatException nfe) {
                if (transaction != null && transaction.isActive()) {
                    transaction.rollback();
                }
                JOptionPane.showMessageDialog(carList, "Неверный формат числовых данных.");
                logger.warn("Ошибка преобразования числовых данных: " + nfe.getMessage());
            } catch (Exception e) {
                if (transaction != null && transaction.isActive()) {
                    transaction.rollback();
                }
                JOptionPane.showMessageDialog(carList, "Ошибка добавления записи в базу данных.");
                logger.warn("Ошибка добавления записи в базу данных: " + e.getMessage());
            } finally {
                if (em != null && em.isOpen()) {
                    em.close(); // Закрываем EntityManager
                }
            }
        }
    }


    /**
     * Метод для фильтрации записей в таблице на основе выбранного поля и введенного текста.
     */
    private void filterRecords() {
        String selectedField = (String) field.getSelectedItem(); // Получаем выбранный столбец
        String searchText = fieldName.getText().trim(); // Получаем текст для поиска и убираем лишние пробелы

        // Проверяем, пустое ли поле для поиска
        if (searchText.isEmpty() || searchText.equals("Введите необходимый элемент")) {
            JOptionPane.showMessageDialog(carList, "Пожалуйста, введите текст для поиска.");
            logger.warn("Попытка фильтрации с пустым полем.");
            return; // Завершаем метод, если поле пустое
        }

        logger.info("Фильтрация записей по полю: " + selectedField + ", текст: " + searchText);

        List<Object[]> filteredData = new ArrayList<>(); // Список для хранения отфильтрованных данных

        // Проходим по всем строкам модели таблицы
        for (int row = 0; row < model.getRowCount(); row++) {
            String valueToCheck = ""; // Переменная для хранения значения для проверки

            // Получаем значение из выбранного столбца в зависимости от его названия
            switch (selectedField) {
                case "ID":
                    valueToCheck = String.valueOf(Integer.parseInt(model.getValueAt(row, 0).toString()));
                    break;
                case "Название":
                    valueToCheck = (String) model.getValueAt(row, 1);
                    break;
                case "Тип кузова":
                    valueToCheck = (String) model.getValueAt(row, 2);
                    break;
                case "Год выпуска":
                    valueToCheck = String.valueOf(Integer.parseInt(model.getValueAt(row, 3).toString()));
                    break;
                case "Мощность двигателя (л.с.)":
                    valueToCheck = String.valueOf(Integer.parseInt(model.getValueAt(row, 4).toString()));
                    break;
                case "Максимальная скорость (км/ч)":
                    valueToCheck = String.valueOf(Integer.parseInt(model.getValueAt(row, 5).toString()));
                    break;
                case "Пробег (км)":
                    valueToCheck = String.valueOf(Integer.parseInt(model.getValueAt(row, 6).toString()));
                    break;
                case "Владелец":
                    valueToCheck = model.getValueAt(row, 7).toString();
                    break;
                case "Номер СТС": // Фильтрация по номеру СТС
                    valueToCheck = (String) model.getValueAt(row, 8);
                    break;
                case "Номер ПТС": // Фильтрация по номеру ПТС
                    valueToCheck = (String) model.getValueAt(row, 9);
                    break;
            }

            // Проверяем, содержит ли значение искомый текст
            if (valueToCheck.toLowerCase().contains(searchText.toLowerCase())) {
                filteredData.add(new Object[]{
                        model.getValueAt(row, 0),
                        model.getValueAt(row, 1),
                        model.getValueAt(row, 2),
                        model.getValueAt(row, 3),
                        model.getValueAt(row, 4),
                        model.getValueAt(row, 5),
                        model.getValueAt(row, 6),
                        model.getValueAt(row, 7),
                        model.getValueAt(row, 8), // Добавление номера СТС
                        model.getValueAt(row, 9)  // Добавление номера ПТС
                });
            }
        }

        // Обновляем модель таблицы с отфильтрованными данными
        model.setRowCount(0); // Очищаем текущие данные в модели
        if (filteredData.isEmpty()) {
            // Если отфильтрованные данные пусты, выводим сообщение
            JOptionPane.showMessageDialog(carList, "Нет записей, соответствующих вашему запросу.", "Результаты поиска", JOptionPane.INFORMATION_MESSAGE);
            logger.info("Нет записей, соответствующих запросу");
        } else {
            // Добавляем отфильтрованные строки в модель
            for (Object[] rowData : filteredData) {
                model.addRow(rowData);
                logger.debug("Фильтрация завершена, найдено записей: " + filteredData.size());
            }
        }
    }

    /**
     * Метод для сохранения данных таблицы в XML файл.
     */
    private void saveToXML() {
        // Получаем данные из модели таблицы
        List<Object[]> data = new ArrayList<>();
        for (int row = 0; row < model.getRowCount(); row++) {
            Object[] rowData = new Object[model.getColumnCount()];
            for (int col = 0; col < model.getColumnCount(); col++) {
                rowData[col] = model.getValueAt(row, col);
            }
            data.add(rowData); // Добавляем данные строки в список
        }

        // Определите путь к файлу и названия столбцов
        String filePath = "cars.xml"; // Путь к файлу
        String[] columnNames = {
                "ID",
                "Название",
                "Тип кузова",
                "Год выпуска",
                "Мощность двигателя (л.с.)",
                "Максимальная скорость (км/ч)",
                "Пробег (км)",
                "Владелец",
                "Номер СТС",  // Добавлено поле "номер СТС"
                "Номер ПТС"   // Добавлено поле "номер ПТС"
        };

        // XMLExporter поддерживает передачу данных с новыми полями
        XMLExporter exporter = new XMLExporter();
        exporter.exportToXML(data, filePath, "service_station", columnNames, "cars");
        logger.debug("Данные сохранены в XML файл: " + filePath);
    }

    /**
     * Метод для отображения окна со списком автомобилей.
     */
    public void show1() {
        carList = new JFrame("Список автомобилей"); // Создаем новое окно
        carList.setSize(500, 300); // Устанавливаем размер окна
        carList.setLocation(100, 100); // Устанавливаем положение окна
        carList.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Действие при закрытии окна

        // Создаем кнопку для сохранения списка
        save = new JButton(new ImageIcon("Files/save.png"));
        save.setToolTipText("Сохранить список");
        save.addActionListener(e -> {
            logger.info("Пользователь нажал кнопку 'Сохранить'.");
            saveToXML(); // Вызываем метод сохранения данных в XML
        });

        // Создаем кнопку для возврата в основное меню
        back = new JButton(new ImageIcon("Files/back.png"));
        back.setToolTipText("Вернуться в основное меню");
        back.addActionListener(e -> {
            logger.info("Пользователь нажал кнопку 'Назад'.");
            carList.dispose(); // Закрываем текущее окно
            logger.info("Открыто главное меню.");
        });

        // Создаем кнопку для добавления новой записи
        adding = new JButton(new ImageIcon("Files/add.png"));
        adding.setToolTipText("Добавить новую запись");
        adding.addActionListener(e -> {
            logger.info("Пользователь нажал кнопку 'Добавить запись'.");
            addRecord(); // Вызываем метод добавления записи
        });

        // Создаем кнопку для удаления записи
        delete = new JButton(new ImageIcon("Files/delete.png"));
        delete.setToolTipText("Удалить запись");
        delete.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                logger.info("Пользователь нажал кнопку 'Удалить запись'.");
                deleteRecord(); // Вызываем метод удаления записи
            }
        });

        // Создаем кнопку для редактирования записи
        edit = new JButton(new ImageIcon("Files/edit.png"));
        edit.setToolTipText("Изменить запись");
        edit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                logger.info("Пользователь нажал кнопку 'Изменить запись'.");
                editRecord(); // Вызываем метод редактирования
            }
        });

        // Создаем кнопку для открытия полного списка
        open = new JButton(new ImageIcon("Files/open.png"));
        open.setToolTipText("Открыть полный список");
        open.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                logger.info("Пользователь нажал кнопку 'Открыть полный список'.");
                openFullList(); // Вызываем метод для открытия полного списка
            }
        });

        // Создаем кнопку для сброса выбора
        cancel = new JButton(new ImageIcon("Files/cancel.png"));
        cancel.setToolTipText("Сбросить выбор");
        cancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                logger.info("Пользователь нажал кнопку 'Сбросить выбор'.");
                cars.clearSelection(); // Сбрасываем выбор в таблице
            }
        });

        // Создаем панель инструментов и добавляем кнопки
        toolBar = new JToolBar("Панель инструментов");
        toolBar.add(save);
        toolBar.add(back);
        toolBar.add(adding);
        toolBar.add(delete);
        toolBar.add(edit);
        toolBar.add(open);
        toolBar.add(cancel);

        carList.setLayout(new BorderLayout()); // Устанавливаем компоновку для окна
        carList.add(toolBar, BorderLayout.NORTH); // Добавляем панель инструментов в верхнюю часть окна

        // Обновленный массив столбцов
        String[] columns = {
                "ID",
                "Название",
                "Тип кузова",
                "Год выпуска",
                "Мощность двигателя (л.с.)",
                "Максимальная скорость (км/ч)",
                "Пробег (км)",
                "Владелец",
                "Номер СТС",  // Новое поле
                "Номер ПТС"   // Новое поле
        };

        model = new DefaultTableModel(columns, 0); // Начинаем с 0 строк

        // Создаем EntityManager для работы с базой данных
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("test_persistence");
        EntityManager em = emf.createEntityManager(); // Используем существующий emf
        try {
            // Загружаем автомобили из базы данных
            TypedQuery<Car> query = em.createQuery("SELECT c FROM Car c", Car.class);
            List<Car> carsFromDb = query.getResultList();

            // Загружаем клиентов из базы данных
            TypedQuery<Customer> clientQuery = em.createQuery("SELECT c FROM Customer c", Customer.class);
            List<Customer> clientsFromDb = clientQuery.getResultList();

            // Создаем Map для быстрого поиска имени клиента по его ID
            Map<Integer, String> clientIdToNameMap = new HashMap<>();
            for (Customer client : clientsFromDb) {
                clientIdToNameMap.put(client.getCustomer_id(), client.getFullName());
            }

            // Добавляем автомобили в модель
            for (Car car : carsFromDb) {
                String clientName = clientIdToNameMap.get(car.getCar_cust_id()); // Получаем имя клиента по ID
                if (clientName == null) {
                    clientName = "Неизвестно"; // Значение по умолчанию, если клиент не найден
                }

                model.addRow(new Object[]{
                        car.getCar_id(),
                        car.getCarName(),
                        car.getBodyType(),
                        car.getReleaseYear(),
                        car.getEnginePower(),
                        car.getMaxSpeed(),
                        car.getMileage(),
                        clientName,
                        car.getSts(),
                        car.getPts()
                });
            }
        } catch (Exception e) {
            logger.warn("Ошибка при получении автомобилей из базы данных: " + e.getMessage());
            JOptionPane.showMessageDialog(carList, "Ошибка при получении автомобилей из базы данных.");
        } finally {
            // Закрываем EntityManager
            if (em != null && em.isOpen()) {
                em.close();
            }
        }

        // Создаем таблицу и добавляем её в окно
        cars = new JTable(model);
        scroll = new JScrollPane(cars);
        carList.add(scroll, BorderLayout.CENTER); // Добавляем таблицу в центр окна

        // Обновленный JComboBox для фильтрации
        field = new JComboBox(new String[]{
                "ID",
                "Название",
                "Тип кузова",
                "Год выпуска",
                "Мощность двигателя (л.с.)",
                "Максимальная скорость (км/ч)",
                "Пробег (км)",
                "Владелец",
                "Номер СТС",  // Новое поле
                "Номер ПТС"   // Новое поле
        });

        // Поле для ввода текста для поиска
        fieldName = new JTextField("Введите необходимый элемент");
        filter = new JButton("Поиск"); // Кнопка для поиска
        filter.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                logger.info("Пользователь нажал кнопку 'Поиск'.");
                filterRecords(); // Вызов метода фильтрации при нажатии кнопки поиска
            }
        });

        // Панель для фильтрации
        JPanel filterPanel = new JPanel();
        filterPanel.add(field);
        filterPanel.add(fieldName);
        filterPanel.add(filter);

        carList.add(filterPanel, BorderLayout.SOUTH); // Добавляем панель фильтрации в нижнюю часть окна

        carList.setVisible(true); // Делаем окно видимым
    }
}
