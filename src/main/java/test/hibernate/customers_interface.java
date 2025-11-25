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
 * Класс для отображения интерфейса управления клиентами.
 * Позволяет добавлять, редактировать, удалять и фильтровать записи клиентов.
 */
public class customers_interface {
    private JFrame customerList; // Главное окно для списка клиентов
    private DefaultTableModel model; // Модель таблицы для отображения клиентов
    private JToolBar toolBar; // Панель инструментов
    private JButton save; // Кнопка сохранения
    private JButton edit; // Кнопка редактирования
    private JButton delete; // Кнопка удаления
    private JButton adding; // Кнопка добавления
    private JButton back; // Кнопка возврата в основное меню
    private JButton open; // Кнопка открытия полного списка
    private JButton cancel; // Кнопка сброса выбора
    private JButton viewCars; // Кнопка для просмотра машин клиента
    private JScrollPane scroll; // Полоса прокрутки для таблицы
    private JTable customers; // Таблица с клиентами
    private JComboBox field; // Комбобокс для выбора поля для фильтрации
    private JTextField fieldName; // Поле для ввода текста фильтрации
    private JButton filter; // Кнопка фильтрации

    private static final Logger logger = LogManager.getLogger(customers_interface.class); // Логгер для отслеживания событий

    /**
     * Метод для удаления записи о клиенте.
     */
    private void deleteRecord() {
        int selectedRow = customers.getSelectedRow(); // Получаем индекс выбранной строки
        logger.warn("Попытка удалить запись: строка " + selectedRow);

        if (selectedRow != -1) {
            // Получаем идентификатор клиента из выбранной строки
            int customerId = (int) model.getValueAt(selectedRow, 0); // Предполагаем, что ID клиента находится в первом столбце

            // Всплывающее окно подтверждения удаления
            int confirm = JOptionPane.showConfirmDialog(customers,
                    "Вы действительно хотите удалить запись с ID: " + customerId + "?",
                    "Подтверждение удаления",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) {
                EntityManagerFactory emf = Persistence.createEntityManagerFactory("test_persistence"); // Замените на ваше имя единицы постоянства
                EntityManager em = emf.createEntityManager();

                try {
                    em.getTransaction().begin();

                    // Находим клиента по ID
                    Customer customerToDelete = em.find(Customer.class, customerId);
                    if (customerToDelete != null) {
                        em.remove(customerToDelete); // Удаляем клиента из базы данных
                        em.getTransaction().commit(); // Подтверждаем транзакцию
                        model.removeRow(selectedRow); // Удаляем выбранную строку из модели таблицы
                        logger.info("Запись удалена: строка " + selectedRow + ", ID: " + customerId);
                    } else {
                        logger.warn("Не удалось найти клиента с ID: " + customerId);
                        JOptionPane.showMessageDialog(customers, "Запись не найдена в базе данных.");
                    }
                } catch (Exception e) {
                    em.getTransaction().rollback(); // Откатываем транзакцию в случае ошибки
                    logger.error("Ошибка при удалении записи: {}", e.getMessage(), e);
                    JOptionPane.showMessageDialog(customers, "Невозможно удалить запись, так как существуют связанные записи. Пожалуйста, сначала убедитесь, что объект не используется в других таблицах.");
                } finally {
                    if (em != null && em.isOpen()) em.close();
                    if (emf != null && emf.isOpen()) emf.close();
                }
            } else {
                logger.info("Удаление записи отменено пользователем.");
            }
        } else {
            logger.warn("Выбор строки для удаления не был сделан.");
            JOptionPane.showMessageDialog(customers, "Выберите строку для удаления."); // Сообщение, если строка не выбрана
        }
    }

    /**
     * Метод для редактирования записи о клиенте.
     */
    private void editRecord() {
        int selectedRow = customers.getSelectedRow(); // Получаем индекс выбранной строки
        logger.warn("Попытка редактировать запись: строка " + selectedRow);
        if (selectedRow != -1) {
            // Получаем текущее значение всех полей
            String id = model.getValueAt(selectedRow, 0).toString();
            String fullName = (String) model.getValueAt(selectedRow, 1);
            String age = model.getValueAt(selectedRow, 2).toString();
            String gender = (String) model.getValueAt(selectedRow, 3);
            String phoneNumber = model.getValueAt(selectedRow, 4).toString();
            String passportData = model.getValueAt(selectedRow, 5).toString();
            String visitCount = model.getValueAt(selectedRow, 6).toString();
            String discount = model.getValueAt(selectedRow, 7).toString();
            String carReturnDate = (String) model.getValueAt(selectedRow, 8);

            // Создаем текстовые поля для редактирования
            JTextField idField = new JTextField(id);
            idField.setEditable(false); // Делаем поле ID только для чтения
            JTextField fullNameField = new JTextField(fullName);
            JTextField ageField = new JTextField(age);
            JTextField genderField = new JTextField(gender);
            JTextField phoneNumberField = new JTextField(phoneNumber);
            JTextField passportDataField = new JTextField(passportData);
            JTextField visitCountField = new JTextField(visitCount);
            JTextField discountField = new JTextField(discount);
            JTextField carReturnDateField = new JTextField(carReturnDate);

            Object[] message = {
                    "ID:", idField,
                    "Полное имя:", fullNameField,
                    "Возраст:", ageField,
                    "Пол:", genderField,
                    "Номер телефона:", phoneNumberField,
                    "Паспортные данные:", passportDataField,
                    "Количество посещений:", visitCountField,
                    "Общая скидка (%):", discountField,
                    "Дата сдачи автомобиля:", carReturnDateField };

            int option = JOptionPane.showConfirmDialog(customerList, message, "Редактировать запись", JOptionPane.OK_CANCEL_OPTION);
            if (option == JOptionPane.OK_OPTION) {
                // Проверяем все поля перед обновлением
                if (!validateFields(fullNameField, ageField, genderField, phoneNumberField,
                        passportDataField, visitCountField, discountField, carReturnDateField)) {
                    return; // Выход из метода, если есть ошибка
                }

                // Обновляем модель таблицы
                model.setValueAt(fullNameField.getText(), selectedRow, 1);
                model.setValueAt(ageField.getText(), selectedRow, 2);
                model.setValueAt(genderField.getText(), selectedRow, 3);
                model.setValueAt(phoneNumberField.getText(), selectedRow, 4);
                model.setValueAt(passportDataField.getText(), selectedRow, 5);
                model.setValueAt(visitCountField.getText(), selectedRow, 6);
                model.setValueAt(discountField.getText(), selectedRow, 7);
                model.setValueAt(carReturnDateField.getText(), selectedRow, 8);
                logger.info("Запись обновлена в интерфейсе: строка " + selectedRow);

                // Обновляем запись в базе данных
                EntityManagerFactory emf = Persistence.createEntityManagerFactory("test_persistence");
                EntityManager em = emf.createEntityManager();
                try {
                    em.getTransaction().begin();
                    Customer customerToUpdate = em.find(Customer.class, Integer.parseInt(id));
                    if (customerToUpdate != null) {
                        // Не обновляем ID, только остальные поля
                        customerToUpdate.setFullName(fullNameField.getText());
                        customerToUpdate.setAge(Integer.parseInt(ageField.getText()));
                        customerToUpdate.setGender(genderField.getText());
                        customerToUpdate.setNumber(Long.parseLong(phoneNumberField.getText()));
                        customerToUpdate.setPassport_data(Long.parseLong(passportDataField.getText()));
                        customerToUpdate.setVisitingCount(Integer.parseInt(visitCountField.getText()));
                        customerToUpdate.setDiscount(Integer.parseInt(discountField.getText()));
                        customerToUpdate.setDeliveryDate(carReturnDateField.getText());
                        em.getTransaction().commit();
                        logger.info("Запись обновлена в базе данных: ID " + id);
                    }
                } catch (Exception e) {
                    logger.error("Ошибка при обновлении записи в базе данных: " + e.getMessage());
                    JOptionPane.showMessageDialog(customerList, "Ошибка при обновлении записи в базе данных: " + e.getMessage());
                    if (em.getTransaction().isActive()) {
                        em.getTransaction().rollback();
                    }
                } finally {
                    if (em != null && em.isOpen()) {
                        em.close();
                    }
                    if (emf != null && emf.isOpen()) {
                        emf.close();
                    }
                }
            } else {
                logger.warn("Редактирование записи отменено пользователем.");
            }
        } else {
            logger.warn("Выбор строки для редактирования не был сделан.");
            JOptionPane.showMessageDialog(customerList, "Выберите строку для редактирования."); // Сообщение, если строка не выбрана
        }
    }

    /**
     * Метод для валидации полей ввода.
     * Проверяет, что поля заполнены корректно.
     */
    private boolean validateFields(JTextField fullName, JTextField age, JTextField gender,
                                   JTextField phoneNumber, JTextField passportData,
                                   JTextField visitCount,
                                   JTextField discount, JTextField carReturnDate) {
        // Проверка на пустые поля
        if (fullName.getText().trim().isEmpty() || age.getText().trim().isEmpty() ||
                gender.getText().trim().isEmpty() || phoneNumber.getText().trim().isEmpty() ||
                passportData.getText().trim().isEmpty() || visitCount.getText().trim().isEmpty() ||
                discount.getText().trim().isEmpty() ||
                carReturnDate.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(customerList, "Все поля должны быть заполнены.");
            return false;
        }

        // Проверка имени (только буквы)
        if (!fullName.getText().matches("[a-zA-Zа-яА-ЯёЁ\\s]{6,}")) {
            JOptionPane.showMessageDialog(customerList, "Имя должно содержать только буквы и быть не менее 6 символов.");
            return false;
        }

        // Проверка возраста (только положительные числа)
        if (!age.getText().matches("\\d+") || Integer.parseInt(age.getText()) <= 0) {
            JOptionPane.showMessageDialog(customerList, "Возраст должен быть положительным числом.");
            return false;
        }

        // Проверка пола (должен быть "Мужчина" или "Женщина")
        String genderValue = gender.getText().trim().toLowerCase();
        if (!genderValue.equals("мужской") && !genderValue.equals("женский")) {
            JOptionPane.showMessageDialog(customerList, "Пол должен быть 'Мужской' или 'Женский'.");
            return false;
        }

        // Проверка номера телефона (11 цифр)
        if (!phoneNumber.getText().matches("\\d{11}")) {
            JOptionPane.showMessageDialog(customerList, "Номер телефона должен содержать 11 цифр.");
            return false;
        }

        // Проверка паспорта (10 цифр)
        if (!passportData.getText().matches("\\d{10}")) {
            JOptionPane.showMessageDialog(customerList, "Паспортные данные должны содержать 10 цифр.");
            return false;
        }

        // Проверка количества посещений (только положительные числа)
        if (!visitCount.getText().matches("\\d+") || Integer.parseInt(visitCount.getText()) < 0) {
            JOptionPane.showMessageDialog(customerList, "Количество посещений должно быть положительным числом.");
            return false;
        }

        // Проверка общей скидки (только положительные числа или 0)
        if (!discount.getText().matches("\\d+") || Integer.parseInt(discount.getText()) < 0) {
            JOptionPane.showMessageDialog(customerList, "Общая скидка должна быть положительным числом или 0.");
            return false;
        }

        // Проверка даты (формат dd.MM.yyyy)
        if (!carReturnDate.getText().matches("\\d{2}\\.\\d{2}\\.\\d{4}")) {
            JOptionPane.showMessageDialog(customerList, "Дата должна быть в формате: dd.MM.yyyy.");
            return false;
        }

        return true; // Все проверки пройдены
    }

    /**
     * Метод для открытия полного списка клиентов из базы данных.
     */
    private void openFullList() {
        // Обновление модели таблицы полным списком из БД
        String[] columns = {"ID", "Полное имя", "Возраст", "Пол", "Номер телефона", "Паспортные данные", "Количество посещений", "Общая скидка (%)", "Дата сдачи автомобиля"};

        // Создаем EntityManagerFactory и EntityManager
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("test_persistence");
        EntityManager em = emf.createEntityManager();

        List<Customer> customers = null;
        String[][] data = null;

        try {
            // Извлекаем список клиентов из базы данных
            TypedQuery<Customer> query = em.createQuery("SELECT c FROM Customer c", Customer.class);
            customers = query.getResultList();

            // Преобразуем список клиентов в двумерный массив строк
            if (customers != null && !customers.isEmpty()) {
                data = new String[customers.size()][9]; // 9 столбцов
                for (int i = 0; i < customers.size(); i++) {
                    Customer customer = customers.get(i);
                    data[i][0] = String.valueOf(customer.getCustomer_id());
                    data[i][1] = customer.getFullName();
                    data[i][2] = String.valueOf(customer.getAge());
                    data[i][3] = customer.getGender();
                    data[i][4] = String.valueOf(customer.getNumber());
                    data[i][5] = String.valueOf(customer.getPassport_data());
                    data[i][6] = String.valueOf(customer.getVisitingCount());
                    data[i][7] = String.valueOf(customer.getDiscount());
                    data[i][8] = customer.getDeliveryDate();
                }
            }
        } finally {
            em.close();
            emf.close();
        }

        // Если данных нет, возвращаем пустой массив
        if (data == null) {
            data = new String[0][0];
        }

        model.setDataVector(data, columns); // Обновляем модель таблицы
        logger.info("Полный список клиентов открыт из базы данных.");
    }

    /**
     * Метод для добавления новой записи о клиенте.
     */
    private void addRecord() {
        // Создаем текстовые поля для ввода данных о новом клиенте
        JTextField fullNameField = new JTextField();
        JTextField ageField = new JTextField();
        JTextField genderField = new JTextField();
        JTextField phoneNumberField = new JTextField();
        JTextField passportDataField = new JTextField();
        JTextField visitCountField = new JTextField();
        JTextField discountField = new JTextField();
        JTextField carReturnDateField = new JTextField();

        Object[] message = {
                "Полное имя:", fullNameField,
                "Возраст:", ageField,
                "Пол:", genderField,
                "Номер телефона:", phoneNumberField,
                "Паспортные данные:", passportDataField,
                "Количество посещений:", visitCountField,
                "Общая скидка (%):", discountField,
                "Дата сдачи автомобиля:", carReturnDateField
        };

        while (true) {
            int option = JOptionPane.showConfirmDialog(customerList, message, "Добавить запись", JOptionPane.OK_CANCEL_OPTION);
            if (option == JOptionPane.CANCEL_OPTION) {
                logger.info("Добавление записи отменено пользователем.");
                return;
            }

            String fullName = fullNameField.getText();
            String ageStr = ageField.getText();
            String gender = genderField.getText();
            String phoneNumberStr = phoneNumberField.getText();
            String passportDataStr = passportDataField.getText();
            String visitCountStr = visitCountField.getText();
            String discountStr = discountField.getText();
            String carReturnDate = carReturnDateField.getText();

            // Проверка на пустые поля
            if (!validateFields(fullName, ageStr, gender, phoneNumberStr, passportDataStr, visitCountStr,
                    discountStr, carReturnDate)) {
                logger.warn("Попытка добавить запись с некорректными данными.");
                continue;
            }

            EntityManagerFactory emf = Persistence.createEntityManagerFactory("test_persistence");
            EntityManager em = emf.createEntityManager();
            try {
                em.getTransaction().begin();

                Customer newCustomer = new Customer();
                try {
                    newCustomer.setFullName(fullName);
                    newCustomer.setAge(Integer.parseInt(ageStr));
                    newCustomer.setGender(gender);
                    newCustomer.setNumber(Long.parseLong(phoneNumberStr));
                    newCustomer.setPassport_data(Long.parseLong(passportDataStr));
                    newCustomer.setVisitingCount(Integer.parseInt(visitCountStr));
                    newCustomer.setDiscount(Integer.parseInt(discountStr));
                    newCustomer.setDeliveryDate(carReturnDate);

                    em.persist(newCustomer);
                    em.getTransaction().commit();

                    int generatedId = newCustomer.getCustomer_id(); // Получаем сгенерированный ID

                    // Добавляем новую строку в модель таблицы
                    model.addRow(new Object[]{
                            generatedId,
                            newCustomer.getFullName(),
                            newCustomer.getAge(),
                            newCustomer.getGender(),
                            newCustomer.getNumber(),
                            newCustomer.getPassport_data(),
                            newCustomer.getVisitingCount(),
                            newCustomer.getDiscount(),
                            newCustomer.getDeliveryDate()
                    });
                    logger.info("Добавлена новая запись в базу данных и интерфейс (ID: {})", generatedId);
                    break; // Выходим из цикла, если запись успешно добавлена
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(customerList, "Неверный формат числовых данных.");
                    logger.warn("Неверный формат числовых данных при добавлении записи: {}", e.getMessage());
                    em.getTransaction().rollback();
                    continue;
                }

            } catch (RollbackException e) {
                JOptionPane.showMessageDialog(customerList, "Ошибка добавления записи в базу данных: " + e.getMessage());
                logger.error("Ошибка добавления записи в базу данных: {}", e.getMessage(), e);
                em.getTransaction().rollback();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(customerList, "Ошибка добавления записи: " + e.getMessage());
                logger.error("Ошибка добавления записи: {}", e.getMessage(), e);
            } finally {
                if (em != null && em.isOpen()) em.close();
                if (emf != null && emf.isOpen()) emf.close();
            }
        }
    }

    /**
     * Метод для валидации полей ввода при добавлении нового клиента.
     * Проверяет, что поля заполнены корректно.
     */
    private boolean validateFields(String fullName, String ageStr, String gender, String phoneNumberStr,
                                   String passportDataStr, String visitCountStr,
                                   String discountStr, String carReturnDate) {
        // Проверка на пустоту
        if (fullName.isEmpty() || ageStr.isEmpty() || gender.isEmpty() || phoneNumberStr.isEmpty() ||
                passportDataStr.isEmpty() || visitCountStr.isEmpty()  ||
                discountStr.isEmpty() || carReturnDate.isEmpty()) {
            JOptionPane.showMessageDialog(customerList, "Пожалуйста, заполните все поля.");
            return false;
        }

        // Проверка имени
        if (!fullName.matches("[a-zA-Zа-яА-ЯёЁ\\s]{6,}")) {
            JOptionPane.showMessageDialog(customerList, "Имя должно содержать только буквы и быть не менее 6 символов.");
            return false;
        }

        // Проверка возраста (только положительные числа)
        if (!ageStr.matches("\\d+") || Integer.parseInt(ageStr) <= 0) {
            JOptionPane.showMessageDialog(customerList, "Возраст должен быть положительным числом.");
            return false;
        }

        // Проверка пола (должен быть "Мужчина" или "Женщина")
        if (!gender.equalsIgnoreCase("Мужской") && !gender.equalsIgnoreCase("Женский")) {
            JOptionPane.showMessageDialog(customerList, "Пол должен быть 'Мужской' или 'Женский'.");
            return false;
        }

        // Проверка номера телефона (11 цифр)
        if (!phoneNumberStr.matches("\\d{11}")) {
            JOptionPane.showMessageDialog(customerList, "Номер телефона должен содержать 11 цифр.");
            return false;
        }

        // Проверка паспорта (10 цифр)
        if (!passportDataStr.matches("\\d{10}")) {
            JOptionPane.showMessageDialog(customerList, "Паспортные данные должны содержать 10 цифр.");
            return false;
        }

        // Проверка количества посещений (только положительные числа)
        if (!visitCountStr.matches("\\d+") || Integer.parseInt(visitCountStr) < 0) {
            JOptionPane.showMessageDialog(customerList, "Количество посещений должно быть положительным числом.");
            return false;
        }

        // Проверка общей скидки (только положительные числа или 0)
        if (!discountStr.matches("\\d+") || Integer.parseInt(discountStr) < 0) {
            JOptionPane.showMessageDialog(customerList, "Общая скидка должна быть положительным числом или 0.");
            return false;
        }

        // Проверка даты (формат dd.MM.yyyy)
        if (!carReturnDate.matches("\\d{2}\\.\\d{2}\\.\\d{4}")) {
            JOptionPane.showMessageDialog(customerList, "Дата должна быть в формате: dd.MM.yyyy.");
            return false;
        }

        return true; // Все проверки пройдены
    }

    /**
     * Исключение, выбрасываемое при попытке выполнить поиск с пустым полем.
     */
    private static class emptyException extends Exception {
        public emptyException() {
            super("Поле для поиска не должно быть пустым.");
        }
    }

    /**
     * Исключение, выбрасываемое при попытке выполнить поиск без введенного элемента.
     */
    private static class defException extends Exception{
        public defException() {
            super("Пожалуйста, введите элемент, который вы хотите найти.");
        }
    }

    /**
     * Проверяет ввод пользователя для поиска.
     *
     * @param input текст для поиска
     * @throws customers_interface.emptyException если поле пустое
     * @throws customers_interface.defException если введен текст "введите необходимый элемент"
     */
    private void checkInput(String input) throws customers_interface.emptyException, customers_interface.defException {
        // Проверяем, является ли текст для поиска пустым
        if (input.isEmpty()) {
            throw new emptyException();
        }
        // Проверяем, равно ли значение "введите необходимый элемент"
        if (input.trim().equalsIgnoreCase("введите необходимый элемент")) {
            throw new defException();
        }
    }

    /**
     * Фильтрует записи на основе текста поиска и выбранного поля.
     */
    private void filterRecords() {
        String selectedField = (String) field.getSelectedItem(); // Получаем выбранный столбец
        String searchText = fieldName.getText().toLowerCase().trim(); // Получаем текст для поиска и убираем лишние пробелы

        logger.info("Фильтрация записей по полю: " + selectedField + ", текст: " + searchText);

        // Проверяем, не пустое ли поле для поиска
        if (searchText.isEmpty() || searchText.equals("введите необходимый элемент")) {
            JOptionPane.showMessageDialog(customerList, "Пожалуйста, введите корректный текст для поиска.", "Ошибка поиска", JOptionPane.ERROR_MESSAGE);
            return; // Выходим из метода, не обновляя модель таблицы
        }

        List<Object[]> filteredData = new ArrayList<>(); // Создаем новый список для хранения отфильтрованных данных

        try {
            checkInput(searchText); // Проверяем ввод пользователя
        } catch (customers_interface.emptyException |
                 customers_interface.defException ex) {
            logger.error("Ошибка при поиске: " + ex.getMessage());
            JOptionPane.showMessageDialog(customerList, "Ошибка при поиске: " + ex.getMessage());
            return; // Выходим из метода в случае ошибки
        }

        // Проходим по всем записям в модели и проверяем, содержат ли они искомый текст
        for (int row = 0; row < model.getRowCount(); row++) {
            String valueToCheck = "";
            // Получаем значение из выбранного столбца
            switch (selectedField) {
                case "ID":
                    valueToCheck = model.getValueAt(row, 0).toString();
                    break;
                case "Полное имя":
                    valueToCheck = (String) model.getValueAt(row, 1);
                    break;
                case "Возраст":
                    valueToCheck = model.getValueAt(row, 2).toString();
                    break;
                case "Пол":
                    valueToCheck = (String) model.getValueAt(row, 3);
                    break;
                case "Номер телефона":
                    valueToCheck = model.getValueAt(row, 4).toString();
                    break;
                case "Паспортные данные":
                    valueToCheck = model.getValueAt(row, 5).toString();
                    break;
                case "Количество посещений":
                    valueToCheck = model.getValueAt(row, 6).toString();
                    break;
                case "Общая скидка (%)":
                    valueToCheck = model.getValueAt(row, 7).toString();
                    break;
                case "Дата сдачи автомобиля":
                    valueToCheck = (String) model.getValueAt(row, 8);
                    break;
            }

            // Проверяем, содержит ли значение искомый текст
            if (valueToCheck.toLowerCase().contains(searchText)) {
                filteredData.add(new Object[]{
                        model.getValueAt(row, 0),
                        model.getValueAt(row, 1),
                        model.getValueAt(row, 2),
                        model.getValueAt(row, 3),
                        model.getValueAt(row, 4),
                        model.getValueAt(row, 5),
                        model.getValueAt(row, 6),
                        model.getValueAt(row, 7),
                        model.getValueAt(row, 8)
                });
            }
        }

        // Очищаем текущую модель таблицы
        model.setRowCount(0);
        if (filteredData.isEmpty()) {
            logger.warn("Нет записей, соответствующих вашему запросу.");
            // Если отфильтрованные данные пусты, выводим сообщение
            JOptionPane.showMessageDialog(customerList, "Нет записей, соответствующих вашему запросу.", "Результаты поиска", JOptionPane.INFORMATION_MESSAGE);
        } else {
            for (Object[] rowData : filteredData) {
                model.addRow(rowData); // Добавляем отфильтрованные строки
            }
            logger.info("Записи отфильтрованы, найдено: " + filteredData.size() + " записей.");
        }
    }

    /**
     * Сохраняет данные из модели таблицы в XML файл.
     */
    private void saveToXML() {
        // Получаем данные из модели таблицы
        List<Object[]> data = new ArrayList<>();
        for (int row = 0; row < model.getRowCount(); row++) {
            Object[] rowData = new Object[model.getColumnCount()];
            for (int col = 0; col < model.getColumnCount(); col++) {
                rowData[col] = model.getValueAt(row, col);
            }
            data.add(rowData);
        }

        // Определите путь к файлу и названия столбцов
        String filePath = "customers.xml"; // Путь к файлу
        String[] columnNames = {"ID", "Полное имя", "Возраст", "Пол", "Номер телефона", "Паспортные данные", "Количество посещений", "Общая скидка (%)", "Дата сдачи автомобиля"}; // Названия столбцов
        XMLExporter exporter = new XMLExporter();
        exporter.exportToXML(data, filePath, "service_station", columnNames, "customers");
        logger.info("Данные сохранены в XML файл: " + filePath);
    }

    /**
     * Отображает список автомобилей, принадлежащих выбранному клиенту.
     */
    private void viewCustomerCars() {
        int selectedRow = customers.getSelectedRow(); // Получаем индекс выбранной строки
        if (selectedRow != -1) {
            int customerId = (Integer.parseInt(model.getValueAt(selectedRow, 0).toString())); // Получаем ID клиента из выбранной строки
            logger.info("Открытие списка машин для клиента с ID: " + customerId);

            // Создаем новое окно для отображения машин
            JFrame carsFrame = new JFrame("Машины клиента с ID: " + customerId);
            carsFrame.setSize(600, 400);
            carsFrame.setLocation(150, 150);
            carsFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

            // Создаем таблицу для отображения машин
            String[] columns = {"ID", "Название", "Тип кузова", "Год выпуска", "Мощность двигателя", "Макс. скорость", "Пробег", "Номер СТС", "Номер ПТС", "Клиент"};
            DefaultTableModel carsModel = new DefaultTableModel(columns, 0);
            JTable carsTable = new JTable(carsModel);
            JScrollPane scrollPane = new JScrollPane(carsTable);
            carsFrame.add(scrollPane, BorderLayout.CENTER);

            // Извлекаем машины клиента из базы данных
            EntityManagerFactory emf = Persistence.createEntityManagerFactory("test_persistence");
            EntityManager em = emf.createEntityManager();
            try {
                // Запрос для получения машин клиента
                TypedQuery<Car> query = em.createQuery("SELECT c FROM Car c WHERE c.car_cust_id = :customerId", Car.class);
                query.setParameter("customerId", customerId);
                List<Car> cars = query.getResultList();

                // Запрос для получения всех клиентов
                TypedQuery<Customer> clientQuery = em.createQuery("SELECT c FROM Customer c", Customer.class);
                List<Customer> clientsFromDb = clientQuery.getResultList();

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

                    // Заполняем модель таблицы данными машин
                    carsModel.addRow(new Object[]{
                            car.getCar_id(),
                            car.getCarName(),
                            car.getBodyType(),
                            car.getReleaseYear(),
                            car.getEnginePower(),
                            car.getMaxSpeed(),
                            car.getMileage(),
                            car.getSts(),
                            car.getPts(),
                            clientName
                    });
                }

                logger.info("Список машин для клиента с ID: " + customerId + " успешно загружен.");

            } catch (Exception e) {
                logger.error("Ошибка при загрузке машин клиента: " + e.getMessage());
                JOptionPane.showMessageDialog(carsFrame, "Ошибка при загрузке машин клиента: " + e.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
            } finally {
                if (em != null && em.isOpen()) {
                    em.close();
                }
                if (emf != null && emf.isOpen()) {
                    emf.close();
                }
            }

            carsFrame.setVisible(true); // Показываем новое окно
        } else {
            // Если строка не выбрана, показываем предупреждение
            JOptionPane.showMessageDialog(customerList, "Выберите клиента для просмотра машин.", "Ошибка", JOptionPane.WARNING_MESSAGE);
        }
    }

    /**
     * Отображает окно со списком клиентов и их данными.
     */
    public void show4() {
        customerList = new JFrame("Список клиентов");
        customerList.setSize(500, 300);
        customerList.setLocation(100, 100);
        customerList.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Создаем кнопки для различных действий
        save = new JButton(new ImageIcon("Files/save.png"));
        save.setToolTipText("Сохранить список");
        save.addActionListener(e -> {
            logger.info("Пользователь нажал кнопку 'Сохранить'.");
            saveToXML(); // Сохраняем данные в XML
        });

        back = new JButton(new ImageIcon("Files/back.png"));
        back.setToolTipText("Вернуться в основное меню");
        back.addActionListener(e -> {
            logger.info("Пользователь нажал кнопку 'Назад'.");
            customerList.dispose(); // Закрываем текущее окно
        });

        adding = new JButton(new ImageIcon("Files/add.png"));
        adding.setToolTipText("Добавить новую запись");
        adding.addActionListener(e -> {
            logger.info("Пользователь нажал кнопку 'Добавить запись'.");
            addRecord(); // Вызываем метод для добавления записи
        });

        delete = new JButton(new ImageIcon("Files/delete.png"));
        delete.setToolTipText("Удалить запись");
        delete.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                logger.info("Пользователь нажал кнопку 'Удалить запись'.");
                deleteRecord(); // Вызываем метод для удаления записи
            }
        });

        edit = new JButton(new ImageIcon("Files/edit.png"));
        edit.setToolTipText("Изменить запись");
        edit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                logger.info("Пользователь нажал кнопку 'Изменить запись'.");
                editRecord(); // Вызываем метод редактирования
            }
        });

        open = new JButton(new ImageIcon("Files/open.png"));
        open.setToolTipText("Открыть полный список");
        open.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                logger.info("Пользователь нажал кнопку 'Открыть полный список'.");
                openFullList(); // Вызываем метод для открытия полного списка
            }
        });

        cancel= new JButton(new ImageIcon("Files/cancel.png"));
        cancel.setToolTipText("Сбросить выбор");
        cancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                logger.info("Пользователь нажал кнопку 'Сбросить выбор'.");
                customers.clearSelection(); // Сбрасываем выбор в таблице
            }
        });

        viewCars = new JButton(new ImageIcon("Files/car.png"));
        viewCars.setToolTipText("Просмотреть машины клиента");
        viewCars.addActionListener(e -> {
            logger.info("Пользователь нажал кнопку 'Просмотреть машины клиента'.");
            viewCustomerCars(); // Вызов метода для просмотра машин клиента
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
        toolBar.add(viewCars);

        customerList.setLayout(new BorderLayout());

        // Добавляем панель инструментов (toolBar) в верхнюю часть
        customerList.add(toolBar, BorderLayout.NORTH);

        // Инициализируем EntityManager для работы с базой данных
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("test_persistence"); // Замените на ваше имя единицы постоянства
        EntityManager em = emf.createEntityManager();

        try {
            // Запрос для получения всех клиентов
            TypedQuery<Customer> query = em.createQuery("SELECT c FROM Customer c", Customer.class);
            List<Customer> customerListData = query.getResultList();

            // Определяем названия столбцов и создаем массив данных
            String[] columns = {"ID", "Полное имя", "Возраст", "Пол", "Номер телефона", "Паспортные данные", "Количество посещений", "Общая скидка (%)", "Дата сдачи автомобиля"};
            Object[][] data = new Object[customerListData.size()][columns.length];

            // Заполняем массив данными клиентов
            for (int i = 0; i < customerListData.size(); i++) {
                Customer customer = customerListData.get(i);
                data[i][0] = customer.getCustomer_id();
                data[i][1] = customer.getFullName();
                data[i][2] = customer.getAge();
                data[i][3] = customer.getGender();
                data[i][4] = customer.getNumber();
                data[i][5] = customer.getPassport_data();
                data[i][6] = customer.getVisitingCount();
                data[i][7] = customer.getDiscount();
                data[i][8] = customer.getDeliveryDate();
            }

            // Создаем модель таблицы и добавляем данные
            model = new DefaultTableModel(data, columns);
            customers = new JTable(model);
            scroll = new JScrollPane(customers);
            customerList.add(scroll, BorderLayout.CENTER);

        } catch (Exception e) {
            // Логируем ошибку и показываем сообщение пользователю
            e.printStackTrace(); // или используйте logger, если у вас есть
            JOptionPane.showMessageDialog(customerList, "Ошибка при загрузке данных из базы данных: " + e.getMessage(), "Ошибка базы данных", JOptionPane.ERROR_MESSAGE);
        } finally {
            // Закрываем EntityManager и EntityManagerFactory
            if (em != null && em.isOpen()) {
                em.close();
            }
            if (emf != null && emf.isOpen()) {
                emf.close();
            }
        }

        // Создаем панель для фильтрации записей
        field = new JComboBox(new String[]{"ID", "Полное имя", "Возраст","Пол","Номер телефона","Паспортные данные","Количество посещений","Общая скидка (%)","Дата сдачи автомобиля"});
        fieldName = new JTextField("Введите необходимый элемент");
        filter = new JButton("Поиск");
        filter.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                logger.info("Пользователь нажал кнопку 'Поиск'.");
                filterRecords(); // Вызов метода фильтрации при нажатии кнопки поиска
            }
        });

        JPanel filterPanel = new JPanel();
        filterPanel.add(field);
        filterPanel.add(fieldName);
        filterPanel.add(filter);

        customerList.add(filterPanel, BorderLayout.SOUTH); // Добавляем панель фильтрации в нижнюю часть окна

        customerList.setVisible(true); // Показываем окно со списком клиентов
    }
}
