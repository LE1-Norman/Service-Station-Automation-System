package test.hibernate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.RollbackException;
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
 * Класс для реализации интерфейса с работниками.
 */
public class workers_interface {
    private JFrame workerList; // Главное окно интерфейса
    private DefaultTableModel model; // Модель таблицы работников
    private JToolBar toolBar; // Панель инструментов
    private JButton save; // Кнопка сохранения
    private JButton edit; // Кнопка редактирования
    private JButton delete; // Кнопка удаления
    private JButton adding; // Кнопка добавления
    private JButton back; // Кнопка "Назад"
    private JButton open; // Кнопка открытия полного списка
    private JButton cancel; // Кнопка сброса выбора
    private JScrollPane scroll; // Панель прокрутки для таблицы
    private JTable workers; // Таблица работников
    private JComboBox field; // Выпадающий список для выбора поля фильтрации
    private JTextField fieldName; // Поле ввода для текста фильтрации
    private JButton filter; // Кнопка фильтрации

    private static final Logger logger = LogManager.getLogger(workers_interface.class); // Логгер для отслеживания событий

    /**
     * Метод для удаления записи о работнике.
     */
    private void deleteRecord() {
        int selectedRow = workers.getSelectedRow(); // Получаем индекс выбранной строки
        logger.warn("Попытка удалить запись: строка " + selectedRow);

        if (selectedRow != -1) {
            // Получаем ID работника из модели
            String workerId = model.getValueAt(selectedRow, 0).toString(); // Предполагается, что ID находится в первой колонке

            // Запрашиваем подтверждение удаления
            int confirm = JOptionPane.showConfirmDialog(workers,
                    "Вы уверены, что хотите удалить запись работника с ID " + workerId + "?",
                    "Подтверждение удаления",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);

            // Если пользователь нажал "Нет", выходим из метода
            if (confirm != JOptionPane.YES_OPTION) {
                logger.info("Удаление записи отменено пользователем.");
                return;
            }

            // Создаем EntityManager для работы с базой данных
            EntityManagerFactory emf = Persistence.createEntityManagerFactory("test_persistence"); // Замените на ваше имя единицы постоянства
            EntityManager em = emf.createEntityManager();

            try {
                em.getTransaction().begin(); // Начинаем транзакцию

                // Находим работника по ID
                Worker workerToDelete = em.find(Worker.class, Integer.parseInt(workerId));
                if (workerToDelete != null) {
                    em.remove(workerToDelete); // Удаляем работника из базы данных
                    logger.info("Запись удалена из базы данных: ID " + workerId);
                } else {
                    logger.warn("Работник с ID " + workerId + " не найден в базе данных.");
                    JOptionPane.showMessageDialog(workers, "Работник не найден в базе данных.");
                    return;
                }

                em.getTransaction().commit(); // Завершаем транзакцию
                model.removeRow(selectedRow); // Удаляем выбранную строку из модели таблицы
                logger.info("Запись удалена из интерфейса: строка " + selectedRow);
            } catch (Exception e) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback(); // Откатываем транзакцию в случае ошибки
                }
                logger.error("Ошибка при удалении записи: " + e.getMessage(), e);
                JOptionPane.showMessageDialog(workers, "Ошибка при удалении записи: " + e.getMessage());
            } finally {
                if (em != null && em.isOpen()) em.close(); // Закрываем EntityManager
                if (emf != null && emf.isOpen()) emf.close(); // Закрываем EntityManagerFactory
            }
        } else {
            logger.warn("Выбор строки для удаления не был сделан.");
            JOptionPane.showMessageDialog(workers, "Выберите строку для удаления."); // Сообщение, если строка не выбрана
        }
    }

    /**
     * Метод для редактирования записи о работнике.
     */
    private void editRecord() {
        int selectedRow = workers.getSelectedRow(); // Получаем индекс выбранной строки
        logger.warn("Попытка редактировать запись: строка " + selectedRow);

        if (selectedRow != -1) {
            // Получаем текущее значение всех полей
            String id = model.getValueAt(selectedRow, 0).toString();
            String fullName = (String) model.getValueAt(selectedRow, 1);
            String age = model.getValueAt(selectedRow, 2).toString();
            String gender = (String) model.getValueAt(selectedRow, 3);
            String phoneNumber = model.getValueAt(selectedRow, 4).toString();
            String passportData = model.getValueAt(selectedRow, 5).toString();
            String workExperience = model.getValueAt(selectedRow, 6).toString();
            String specializationName = model.getValueAt(selectedRow, 7).toString();
            String salary = model.getValueAt(selectedRow, 8).toString();
            String shiftCount = model.getValueAt(selectedRow, 9).toString();
            String daysOff = model.getValueAt(selectedRow, 10).toString();

            // Открываем диалог для редактирования
            JLabel idLabel = new JLabel(id); // Используем JLabel для отображения ID
            JTextField fullNameField = new JTextField(fullName);
            JTextField ageField = new JTextField(age);
            JTextField genderField = new JTextField(gender);
            JTextField phoneNumberField = new JTextField(phoneNumber);
            JTextField passportDataField = new JTextField(passportData);
            JTextField workExperienceField = new JTextField(workExperience);
            JTextField salaryField = new JTextField(salary);
            JTextField shiftCountField = new JTextField(shiftCount);
            JTextField daysOffField = new JTextField(daysOff);

            // Извлечение списка специализаций
            EntityManagerFactory emf = Persistence.createEntityManagerFactory("test_persistence");
            EntityManager em = emf.createEntityManager();
            List<Specialization> specializations = em.createQuery("SELECT s FROM Specialization s", Specialization.class).getResultList();

            // Создание JComboBox для выбора специализации
            JComboBox<Specialization> specializationComboBox = new JComboBox<>();
            for (Specialization specialization : specializations) {
                specializationComboBox.addItem(specialization);
            }

            // Установка выбранной специализации (если есть)
            if (specializationName != null) {
                for (Specialization specialization : specializations) {
                    if (specialization.getSpecialization_name().equals(specializationName)) {
                        specializationComboBox.setSelectedItem(specialization);
                        break;
                    }
                }
            }

            Object[] message = {
                    "ID:", idLabel, // Показываем ID как метку
                    "Полное имя:", fullNameField,
                    "Возраст:", ageField,
                    "Пол:", genderField,
                    "Номер телефона:", phoneNumberField,
                    "Паспортные данные:", passportDataField,
                    "Стаж работы:", workExperienceField,
                    "Специализация:", specializationComboBox, // Добавление выпадающего списка для специализации
                    "Зарплата (Руб):", salaryField,
                    "Количество смен:", shiftCountField,
                    "Выходные дни:", daysOffField
            };

            int option = JOptionPane.showConfirmDialog(workerList, message, "Редактировать запись", JOptionPane.OK_CANCEL_OPTION);
            if (option == JOptionPane.OK_OPTION) {
                // Проверка данных
                if (!isValidInput(fullNameField.getText(), ageField.getText(), genderField.getText(),
                        phoneNumberField.getText(), passportDataField.getText(),
                        workExperienceField.getText(), salaryField.getText(),
                        shiftCountField.getText(), daysOffField.getText())) {
                    return; // Если данные не валидны, выходим из метода
                }

                // Создаем EntityManager для работы с базой данных
                try {
                    em.getTransaction().begin(); // Начинаем транзакцию

                    // Находим работника по ID
                    Worker workerToUpdate = em.find(Worker.class, Integer.parseInt(id));
                    Specialization selectedSpecialization;
                    if (workerToUpdate != null) {
                        // Обновляем данные работника
                        workerToUpdate.setFullName(fullNameField.getText());
                        workerToUpdate.setAge(Integer.parseInt(ageField.getText()));
                        workerToUpdate.setGender(genderField.getText());
                        workerToUpdate.setNumber(Long.parseLong(phoneNumberField.getText()));
                        workerToUpdate.setPassport_data(Long.parseLong(passportDataField.getText()));
                        workerToUpdate.setWorkPeriod(Integer.parseInt(workExperienceField.getText()));

                        // Получаем выбранную специализацию из JComboBox
                        selectedSpecialization = (Specialization) specializationComboBox.getSelectedItem();
                        if (selectedSpecialization != null) {
                            workerToUpdate.setSpecialization(selectedSpecialization.getSid());
                        }

                        workerToUpdate.setSalary(Integer.parseInt(salaryField.getText()));
                        workerToUpdate.setShifts(Integer.parseInt(shiftCountField.getText()));
                        workerToUpdate.setRestDay(daysOffField.getText());

                        em.getTransaction().commit(); // Завершаем транзакцию
                        logger.info("Запись обновлена в базе данных: ID " + id);
                    } else {
                        logger.warn("Работник с ID " + id + " не найден в базе данных.");
                        JOptionPane.showMessageDialog(workerList, "Работник не найден в базе данных.");
                        return;
                    }

                    // Обновляем модель таблицы
                    model.setValueAt(fullNameField.getText(), selectedRow, 1);
                    model.setValueAt(ageField.getText(), selectedRow, 2);
                    model.setValueAt(genderField.getText(), selectedRow, 3);
                    model.setValueAt(phoneNumberField.getText(), selectedRow, 4);
                    model.setValueAt(passportDataField.getText(), selectedRow, 5);
                    model.setValueAt(workExperienceField.getText(), selectedRow, 6);
                    model.setValueAt(selectedSpecialization.getSpecialization_name(), selectedRow, 7); // Обновление ID специализации
                    model.setValueAt(salaryField.getText(), selectedRow, 8);
                    model.setValueAt(shiftCountField.getText(), selectedRow, 9);
                    model.setValueAt(daysOffField.getText(), selectedRow, 10);
                    logger.info("Запись обновлена в интерфейсе: строка " + selectedRow);
                } catch (Exception e) {
                    if (em.getTransaction().isActive()) {
                        em.getTransaction().rollback(); // Откатываем транзакцию в случае ошибки
                    }
                    logger.error("Ошибка при редактировании записи: " + e.getMessage(), e);
                    JOptionPane.showMessageDialog(workerList, "Ошибка при редактировании записи: " + e.getMessage());
                } finally {
                    if (em != null && em.isOpen()) em.close(); // Закрываем EntityManager
                    if (emf != null && emf.isOpen()) emf.close(); // Закрываем EntityManagerFactory
                }
            } else {
                logger.warn("Редактирование отменено пользователем.");
            }
        } else {
            logger.warn("Выбор строки для редактирования не был сделан.");
            JOptionPane.showMessageDialog(workerList, "Выберите строку для редактирования."); // Сообщение, если строка не выбрана
        }
    }

    /**
     * Метод для проверки валидности введённых данных.
     * @param fullName Полное имя работника
     * @param age Возраст работника
     * @param gender Пол работника
     * @param phoneNumber Номер телефона работника
     * @param passportData Паспортные данные работника
     * @param workExperience Стаж работы работника
     * @param salary Зарплата работника
     * @param shiftCount Количество смен работника
     * @param daysOff Выходные дни работника
     * @return true, если данные валидны; false в противном случае
     */
    private boolean isValidInput(String fullName, String age, String gender, String phoneNumber,
                                 String passportData, String workExperience,
                                 String salary, String shiftCount, String daysOff) {
        // Проверка на пустоту
        if (fullName.isEmpty() || age.isEmpty() || gender.isEmpty() || phoneNumber.isEmpty() ||
                passportData.isEmpty() || workExperience.isEmpty() ||
                salary.isEmpty() || shiftCount.isEmpty() || daysOff.isEmpty()) {
            JOptionPane.showMessageDialog(workerList, "Все поля должны быть заполнены.");
            return false;
        }

        // Проверка имени
        if (!fullName.matches("[a-zA-Zа-яА-ЯёЁ\\s]{6,}")) {
            JOptionPane.showMessageDialog(workerList, "Имя должно содержать только буквы и быть не менее 6 символов.");
            return false;
        }

        // Проверка возраста
        if (!age.matches("\\d+") || Integer.parseInt(age) <= 0) {
            JOptionPane.showMessageDialog(workerList, "Возраст должен содержать только положительные числа.");
            return false;
        }

        // Проверка пола
        if (!gender.equalsIgnoreCase("мужской") && !gender.equalsIgnoreCase("женский")) {
            JOptionPane.showMessageDialog(workerList, "Пол может быть только 'мужской' или 'женский'.");
            return false;
        }

        // Проверка номера телефона
        if (!phoneNumber.matches("\\d{11}")) {
            JOptionPane.showMessageDialog(workerList, "Номер телефона должен содержать только 11 цифр.");
            return false;
        }

        // Проверка паспортных данных
        if (!passportData.matches("\\d{10}")) {
            JOptionPane.showMessageDialog(workerList, "Паспортные данные должны содержать только 10 цифр.");
            return false;
        }

        // Проверка стажа
        if (!workExperience.matches("\\d+") || Integer.parseInt(workExperience) >= Integer.parseInt(age)) {
            JOptionPane.showMessageDialog(workerList, "Стаж работы должен содержать только положительные числа и быть меньше возраста.");
            return false;
        }

        // Проверка зарплаты
        if (!salary.matches("\\d+") || Integer.parseInt(salary) <= 0) {
            JOptionPane.showMessageDialog(workerList, "Зарплата должна содержать только положительные числа.");
            return false;
        }

        // Проверка количества смен
        if (!shiftCount.matches("\\d+")) {
            JOptionPane.showMessageDialog(workerList, "Количество смен должно содержать только числа.");
            return false;
        }

        // Проверка выходных дней
        if (!daysOff.matches("\\d+") || Integer.parseInt(daysOff) < 0) {
            JOptionPane.showMessageDialog(workerList, "Выходные дни должны содержать только положительные числа.");
            return false;
        }

        // Проверка на превышение лимита смен и выходных
        if (Integer.parseInt(shiftCount) + Integer.parseInt(daysOff) > 31) {
            JOptionPane.showMessageDialog(workerList, "Сумма количества смен и выходных не должна превышать 31.");
            return false;
        }

        return true; // Все проверки пройдены успешно
    }

    /**
     * Метод для открытия полного списка работников.
     */
    private void openFullList() {
        // Создаем EntityManager для работы с базой данных
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("test_persistence"); // Замените на ваше имя единицы постоянства
        EntityManager em = emf.createEntityManager();

        try {
            // Запрос к базе данных для получения всех работников
            List<Worker> workersList = em.createQuery("SELECT w FROM Worker w", Worker.class).getResultList();

            // Запрос для получения всех специальностей
            List<Specialization> specializationsList = em.createQuery("SELECT s FROM Specialization s", Specialization.class).getResultList();

            // Создаем Map для быстрого поиска названия специальности по её ID
            Map<Integer, String> specializationIdToNameMap = new HashMap<>();
            for (Specialization specialization : specializationsList) {
                specializationIdToNameMap.put(specialization.getSid(), specialization.getSpecialization_name()); // Предполагается, что метод getSpecialization_name() существует
            }

            // Определяем колонки
            String[] columns = {"ID", "Полное имя", "Возраст", "Пол", "Номер телефона", "Паспортные данные", "Стаж работы", "Специальность", "Зарплата (Руб)", "Количество смен", "Выходные дни"};

            // Создаем массив данных для таблицы
            String[][] data = new String[workersList.size()][columns.length];

            for (int i = 0; i < workersList.size(); i++) {
                Worker worker = workersList.get(i);
                data[i][0] = String.valueOf(worker.getWorker_id()); // Предполагается, что у вас есть метод getId()
                data[i][1] = worker.getFullName();
                data[i][2] = String.valueOf(worker.getAge());
                data[i][3] = worker.getGender();
                data[i][4] = String.valueOf(worker.getNumber());
                data[i][5] = String.valueOf(worker.getPassport_data());
                data[i][6] = String.valueOf(worker.getWorkPeriod());
                // Получаем название специальности по ID
                String specializationName = specializationIdToNameMap.get(worker.getSpecialization());
                if (specializationName == null) {
                    specializationName = "Неизвестно"; // Значение по умолчанию, если специальность не найдена
                }
                data[i][7] = specializationName; // Название специальности
                data[i][8] = String.valueOf(worker.getSalary());
                data[i][9] = String.valueOf(worker.getShifts());
                data[i][10] = worker.getRestDay();
            }

            // Обновляем модель таблицы
            model.setDataVector(data, columns);
            logger.info("Полный список рабочих открыт.");
        } catch (Exception e) {
            logger.error("Ошибка при открытии полного списка рабочих: " + e.getMessage(), e);
            JOptionPane.showMessageDialog(workerList, "Ошибка при открытии полного списка рабочих: " + e.getMessage());
        } finally {
            if (em != null && em.isOpen()) em.close(); // Закрываем EntityManager
            if (emf != null && emf.isOpen()) emf.close(); // Закрываем EntityManagerFactory
        }
    }

    /**
     * Метод для добавления новой записи о работнике.
     * Запрашивает у пользователя информацию о работнике через диалоговое окно.
     * Проверяет корректность введенных данных и сохраняет запись в базе данных.
     */
    private void addRecord() {
        // Создание текстовых полей для ввода данных работника
        JTextField fullNameField = new JTextField();
        JTextField ageField = new JTextField();
        JTextField genderField = new JTextField();
        JTextField phoneNumberField = new JTextField();
        JTextField passportDataField = new JTextField();
        JTextField workExperienceField = new JTextField();
        JTextField salaryField = new JTextField();
        JTextField shiftCountField = new JTextField();
        JTextField daysOffField = new JTextField();

        // Извлечение списка специализаций из базы данных
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("test_persistence");
        EntityManager em = emf.createEntityManager();
        List<Specialization> specializations = em.createQuery("SELECT s FROM Specialization s", Specialization.class).getResultList();

        // Создание JComboBox для выбора специализации
        JComboBox<Specialization> specializationComboBox = new JComboBox<>();
        for (Specialization specialization : specializations) {
            specializationComboBox.addItem(specialization);
        }

        // Подготовка сообщения для диалогового окна
        Object[] message = {
                "Полное имя:", fullNameField,
                "Возраст:", ageField,
                "Пол:", genderField,
                "Номер телефона:", phoneNumberField,
                "Паспортные данные:", passportDataField,
                "Стаж работы:", workExperienceField,
                "Специализация:", specializationComboBox,
                "Зарплата (Руб):", salaryField,
                "Количество смен:", shiftCountField,
                "Выходные дни:", daysOffField
        };

        while (true) {
            // Отображение диалогового окна и ожидание ответа пользователя
            int option = JOptionPane.showConfirmDialog(workerList, message, "Добавить запись", JOptionPane.OK_CANCEL_OPTION);
            if (option == JOptionPane.CANCEL_OPTION) {
                logger.info("Добавление записи отменено пользователем.");
                return; // Выход из метода, если пользователь отменил
            }

            // Получаем значения из полей
            String fullName = fullNameField.getText();
            String ageStr = ageField.getText();
            String gender = genderField.getText();
            String phoneNumberStr = phoneNumberField.getText();
            String passportDataStr = passportDataField.getText();
            String workExperienceStr = workExperienceField.getText();
            Specialization selectedSpecialization = (Specialization) specializationComboBox.getSelectedItem(); // Получаем выбранную специализацию
            String salaryStr = salaryField.getText();
            String shiftCountStr = shiftCountField.getText();
            String daysOffStr = daysOffField.getText();

            // Проверка на заполненность всех полей
            if (fullName.isEmpty() || ageStr.isEmpty() || gender.isEmpty() || phoneNumberStr.isEmpty() ||
                    passportDataStr.isEmpty() || workExperienceStr.isEmpty() || selectedSpecialization == null ||
                    salaryStr.isEmpty() || shiftCountStr.isEmpty() || daysOffStr.isEmpty()) {
                JOptionPane.showMessageDialog(workerList, "Пожалуйста, заполните все поля.");
                logger.warn("Попытка добавить запись с пустыми полями.");
                continue; // Возврат к началу цикла для повторного ввода
            }

            // Проверка имени на корректность
            if (!fullName.matches("[a-zA-Zа-яА-ЯёЁ\\s]{6,}")) {
                JOptionPane.showMessageDialog(workerList, "Имя должно содержать только буквы и быть не менее 6 символов.");
                logger.warn("Некорректное имя: {}", fullName);
                continue; // Возврат к началу цикла
            }

            // Проверка возраста
            int age;
            try {
                age = Integer.parseInt(ageStr);
                if (age <= 0) {
                    throw new NumberFormatException(); // Исключение для отрицательного возраста
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(workerList, "Возраст должен быть положительным числом.");
                logger.warn("Некорректный возраст: {}", ageStr);
                continue; // Возврат к началу цикла
            }

            // Проверка пола
            if (!gender.equalsIgnoreCase("мужской") && !gender.equalsIgnoreCase("женский")) {
                JOptionPane.showMessageDialog(workerList, "Пол может быть только 'мужской' или 'женский'.");
                logger.warn("Некорректный пол: {}", gender);
                continue; // Возврат к началу цикла
            }

            // Проверка номера телефона
            if (!phoneNumberStr.matches("\\d{11}")) {
                JOptionPane.showMessageDialog(workerList, "Номер телефона должен содержать только 11 цифр.");
                logger.warn("Некорректный номер телефона: {}", phoneNumberStr);
                continue; // Возврат к началу цикла
            }

            // Проверка паспортных данных
            if (!passportDataStr.matches("\\d{10}")) {
                JOptionPane.showMessageDialog(workerList, "Паспортные данные должны содержать 10 цифр.");
                logger.warn("Некорректные паспортные данные: {}", passportDataStr);
                continue; // Возврат к началу цикла
            }

            // Проверка стажа работы
            int workExperience;
            try {
                workExperience = Integer.parseInt(workExperienceStr);
                if (workExperience < 0 || workExperience >= age) {
                    throw new NumberFormatException(); // Исключение для некорректного стажа
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(workerList, "Стаж работы должен быть положительным и меньше возраста.");
                logger.warn("Некорректный стаж работы: {}", workExperienceStr);
                continue; // Возврат к началу цикла
            }

            // Получаем ID специальности
            int specialtyId = selectedSpecialization.getSid(); // Получаем ID выбранной специализации

            // Проверка зарплаты
            int salary;
            try {
                salary = Integer.parseInt(salaryStr);
                if (salary <= 0) {
                    throw new NumberFormatException(); // Исключение для некорректной зарплаты
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(workerList, "Зарплата должна быть положительным числом.");
                logger.warn("Некорректная зарплата: {}", salaryStr);
                continue; // Возврат к началу цикла
            }

            // Проверка количества смен
            int shiftCount;
            try {
                shiftCount = Integer.parseInt(shiftCountStr);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(workerList, "Количество смен должно быть числом.");
                logger.warn("Некорректное количество смен: {}", shiftCountStr);
                continue; // Возврат к началу цикла
            }

            // Проверка выходных дней
            int daysOff;
            try {
                daysOff = Integer.parseInt(daysOffStr);
                if (daysOff < 0) {
                    throw new NumberFormatException(); // Исключение для отрицательных выходных
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(workerList, "Выходные дни должны быть положительным числом.");
                logger.warn("Некорректное количество выходных дней: {}", daysOffStr);
                continue; // Возврат к началу цикла
            }

            // Проверка суммы смен и выходных
            if (shiftCount + daysOff > 31) {
                JOptionPane.showMessageDialog(workerList, "Сумма количества смен и выходных не должна превышать 31.");
                logger.warn("Сумма смен и выходных превышает 31: {} + {}", shiftCount, daysOff);
                continue; // Возврат к началу цикла
            }

            // Создаем EntityManager для работы с базой данных
            try {
                em.getTransaction().begin(); // Начинаем транзакцию

                // Создание новой сущности работника
                Worker newWorker = new Worker();
                try {
                    // Установка значений для нового работника
                    newWorker.setFullName(fullName);
                    newWorker.setAge(age);
                    newWorker.setGender(gender);
                    newWorker.setNumber(Long.parseLong(phoneNumberStr));
                    newWorker.setPassport_data(Long.parseLong(passportDataStr));
                    newWorker.setWorkPeriod(workExperience);
                    newWorker.setSpecialization(specialtyId); // Сохраняем ID специальности
                    newWorker.setSalary(salary);
                    newWorker.setShifts(shiftCount);
                    newWorker.setRestDay(daysOffStr);

                    em.persist(newWorker); // Сохраняем работника в базе данных
                    em.getTransaction().commit(); // Подтверждаем транзакцию

                    // Получаем сгенерированный ID
                    int generatedId = newWorker.getWorker_id(); // Предполагается, что у вас есть метод getId()

                    // Получаем название специальности
                    String specializationName = selectedSpecialization.getSpecialization_name(); // Предполагается, что у вас есть метод getName()

                    // Добавляем новую строку в модель таблицы
                    model.addRow(new Object[]{
                            generatedId,
                            newWorker.getFullName(),
                            newWorker.getAge(),
                            newWorker.getGender(),
                            newWorker.getNumber(),
                            newWorker.getPassport_data(),
                            newWorker.getWorkPeriod(),
                            specializationName, // Используем название специальности
                            newWorker.getSalary(),
                            newWorker.getShifts(),
                            newWorker.getRestDay()
                    });
                    logger.info("Добавлена новая запись в базу данных и интерфейс (ID: {})", generatedId);
                    break; // Выходим из цикла, если запись успешно добавлена
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(workerList, "Неверный формат числовых данных.");
                    logger.warn("Неверный формат числовых данных при добавлении записи: {}", e.getMessage());
                    em.getTransaction().rollback(); // Откатываем транзакцию при ошибке
                    continue; // Возврат к началу цикла
                }

            } catch (RollbackException e) {
                JOptionPane.showMessageDialog(workerList, "Ошибка добавления записи в базу данных: " + e.getMessage());
                logger.error("Ошибка добавления записи в базу данных: {}", e.getMessage(), e);
                em.getTransaction().rollback(); // Откатываем транзакцию
            } catch (Exception e) {
                JOptionPane.showMessageDialog(workerList, "Ошибка добавления записи: " + e.getMessage());
                logger.error("Ошибка добавления записи: {}", e.getMessage(), e);
            } finally {
                // Закрываем EntityManager и EntityManagerFactory
                if (em != null && em.isOpen()) em.close();
                if (emf != null && emf.isOpen()) emf.close();
            }
        }
    }

    /**
     * Метод для фильтрации записей в таблице по выбранному полю.
     * Запрашивает текст для поиска и обновляет модель таблицы в соответствии с результатами поиска.
     */
    private void filterRecords() {
        String selectedField = (String) field.getSelectedItem(); // Получаем выбранный столбец
        String searchText = fieldName.getText().toLowerCase(); // Получаем текст для поиска и преобразуем его в нижний регистр

        // Проверка на пустое поле или текст "Введите необходимый элемент"
        if (searchText.isEmpty() || searchText.equals("введите необходимый элемент")) {
            JOptionPane.showMessageDialog(workerList, "Пожалуйста, введите текст для фильтрации.", "Ошибка фильтрации", JOptionPane.ERROR_MESSAGE);
            logger.warn("Попытка фильтрации с пустым полем или текстом 'Введите необходимый элемент'.");
            return; // Выходим из метода, не обновляя модель таблицы
        }

        logger.info("Фильтрация записей по полю: " + selectedField + ", текст: " + searchText);

        List<Object[]> filteredData = new ArrayList<>(); // Создаем новый список для хранения отфильтрованных данных
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
                case "Стаж работы":
                    valueToCheck = model.getValueAt(row, 6).toString();
                    break;
                case "Специальность":
                    valueToCheck = model.getValueAt(row, 7).toString();
                    break;
                case "Зарплата (Руб)":
                    valueToCheck = model.getValueAt(row, 8).toString();
                    break;
                case "Количество смен":
                    valueToCheck = model.getValueAt(row, 9).toString();
                    break;
                case "Выходные дни":
                    valueToCheck = (String) model.getValueAt(row, 10);
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
                        model.getValueAt(row, 8),
                        model.getValueAt(row, 9),
                        model.getValueAt(row, 10)
                });
            }
        }

        // Обновляем модель таблицы с отфильтрованными данными
        model.setRowCount(0); // Очищаем текущую модель
        if (filteredData.isEmpty()) {
            logger.warn("Нет записей, соответствующих вашему запросу.");
            // Если отфильтрованные данные пусты, выводим сообщение
            JOptionPane.showMessageDialog(workerList, "Нет записей, соответствующих вашему запросу.", "Результаты поиска", JOptionPane.INFORMATION_MESSAGE);
        } else {
            for (Object[] rowData : filteredData) {
                model.addRow(rowData); // Добавляем отфильтрованные строки
            }
            logger.info("Записи отфильтрованы, найдено: " + filteredData.size() + " записей.");
        }
    }

    /**
     * Метод для сохранения данных работников в XML файл.
     * Извлекает данные из модели таблицы и экспортирует их в указанный файл.
     */
    private void saveToXML() {
        // Получаем данные из модели таблицы
        List<Object[]> data = new ArrayList<>();
        for (int row = 0; row < model.getRowCount(); row++) {
            Object[] rowData = new Object[model.getColumnCount()];
            for (int col = 0; col < model.getColumnCount(); col++) {
                rowData[col] = model.getValueAt(row, col);
            }
            data.add(rowData); // Добавляем строку в список данных
        }

        // Определите путь к файлу и названия столбцов
        String filePath = "workers.xml"; // Путь к файлу
        String[] columnNames = {"ID", "Полное имя", "Возраст", "Пол", "Номер телефона", "Паспортные данные", "Стаж работы", "Специальность", "Зарплата (Руб)", "Количество смен", "Выходные дни"}; // Названия столбцов
        XMLExporter exporter = new XMLExporter();
        exporter.exportToXML(data, filePath, "service_station", columnNames, "workers"); // Экспорт данных в XML
        logger.info("Данные сохранены в XML файл: " + filePath);
    }


    /**
     * Метод для отображения окна со списком рабочих.
     * Создает графический интерфейс, который позволяет пользователю
     * просматривать, добавлять, редактировать и удалять записи о рабочих.
     */
    public void show5() {
        // Создание нового окна для отображения списка рабочих
        workerList = new JFrame("Список рабочих");
        workerList.setSize(500, 300); // Установка размера окна
        workerList.setLocation(100, 100); // Установка позиции окна на экране
        workerList.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Завершение работы приложения при закрытии окна

        // Кнопка для сохранения списка рабочих
        save = new JButton(new ImageIcon("Files/save.png"));
        save.setToolTipText("Сохранить список"); // Подсказка для кнопки
        save.addActionListener(e -> {
            logger.info("Пользователь нажал кнопку 'Сохранить'.");
            saveToXML(); // Вызов метода для сохранения данных в XML
        });

        // Кнопка для возврата в основное меню
        back = new JButton(new ImageIcon("Files/back.png"));
        back.setToolTipText("Вернуться в основное меню");
        back.addActionListener(e -> {
            logger.info("Пользователь нажал кнопку 'Назад'.");
            workerList.dispose(); // Закрыть текущее окно
        });

        // Кнопка для добавления новой записи
        adding = new JButton(new ImageIcon("Files/add.png"));
        adding.setToolTipText("Добавить новую запись");
        adding.addActionListener(e -> {
            logger.info("Пользователь нажал кнопку 'Добавить запись'.");
            addRecord(); // Вызов метода для добавления новой записи
        });

        // Кнопка для удаления записи
        delete = new JButton(new ImageIcon("Files/delete.png"));
        delete.setToolTipText("Удалить запись");
        delete.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                logger.info("Пользователь нажал кнопку 'Удалить запись'.");
                deleteRecord(); // Вызов метода для удаления записи
            }
        });

        // Кнопка для редактирования записи
        edit = new JButton(new ImageIcon("Files/edit.png"));
        edit.setToolTipText("Изменить запись");
        edit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                logger.info("Пользователь нажал кнопку 'Изменить запись'.");
                editRecord(); // Вызов метода редактирования
            }
        });

        // Кнопка для открытия полного списка рабочих
        open = new JButton(new ImageIcon("Files/open.png"));
        open.setToolTipText("Открыть полный список");
        open.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                logger.info("Пользователь нажал кнопку 'Открыть полный список'.");
                openFullList(); // Вызов метода для открытия полного списка
            }
        });

        // Кнопка для сброса выбора
        cancel = new JButton(new ImageIcon("Files/cancel.png"));
        cancel.setToolTipText("Сбросить выбор");
        cancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                logger.info("Пользователь нажал кнопку 'Сбросить выбор'.");
                workers.clearSelection(); // Сбросить выделение в таблице
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
        workerList.setLayout(new BorderLayout());
        workerList.add(toolBar, BorderLayout.NORTH);

        // Определение заголовков столбцов для таблицы
        String[] columns = {"ID", "Полное имя", "Возраст", "Пол", "Номер телефона", "Паспортные данные", "Стаж работы", "Специальность", "Зарплата (Руб)", "Количество смен", "Выходные дни"};
        model = new DefaultTableModel(new String[0][0], columns); // Инициализация пустой модели таблицы
        workers = new JTable(model); // Создание таблицы с заданной моделью
        scroll = new JScrollPane(workers); // Добавление прокрутки к таблице

        // Добавление таблицы в окно
        workerList.add(scroll, BorderLayout.CENTER);

        // Создание EntityManager для работы с базой данных
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("test_persistence"); // Замените на ваше имя единицы постоянства
        EntityManager em = emf.createEntityManager();

        try {
            // Запрос для получения всех работников
            List<Worker> workersList = em.createQuery("SELECT w FROM Worker w", Worker.class).getResultList();

            // Запрос для получения всех специальностей
            List<Specialization> specializationsList = em.createQuery("SELECT s FROM Specialization s", Specialization.class).getResultList();

            // Создаем Map для быстрого поиска названия специальности по её ID
            Map<Integer, String> specializationIdToNameMap = new HashMap<>();
            for (Specialization specialization : specializationsList) {
                specializationIdToNameMap.put(specialization.getSid(), specialization.getSpecialization_name()); // Предполагается, что метод getName() существует
            }

            // Подготовка данных для таблицы
            String[][] data = new String[workersList.size()][columns.length];

            for (int i = 0; i < workersList.size(); i++) {
                Worker worker = workersList.get(i);
                data[i][0] = String.valueOf(worker.getWorker_id()); // ID
                data[i][1] = worker.getFullName(); // Полное имя
                data[i][2] = String.valueOf(worker.getAge()); // Возраст
                data[i][3] = worker.getGender(); // Пол
                data[i][4] = String.valueOf(worker.getNumber()); // Номер телефона
                data[i][5] = String.valueOf(worker.getPassport_data()); // Паспортные данные
                data[i][6] = String.valueOf(worker.getWorkPeriod()); // Стаж работы
                // Получаем название специальности по ID
                String specializationName = specializationIdToNameMap.get(worker.getSpecialization());
                if (specializationName == null) {
                    specializationName = "Неизвестно"; // Значение по умолчанию, если специальность не найдена
                }
                data[i][7] = specializationName; // Название специальности
                data[i][8] = String.valueOf(worker.getSalary()); // Зарплата
                data[i][9] = String.valueOf(worker.getShifts()); // Количество смен
                data[i][10] = worker.getRestDay(); // Выходные дни
            }

            // Обновление модели таблицы с загруженными данными
            model.setDataVector(data, columns);
        } catch (Exception e) {
            // Обработка исключений при загрузке данных из базы данных
            JOptionPane.showMessageDialog(workerList, "Ошибка загрузки данных из базы данных: " + e.getMessage());
            logger.error("Ошибка загрузки данных из базы данных: {}", e.getMessage(), e);
        } finally {
            // Закрытие EntityManager и EntityManagerFactory
            if (em != null && em.isOpen()) em.close();
            if (emf != null && emf.isOpen()) emf.close();
        }

        // Создание элементов для фильтрации данных
        field = new JComboBox(new String[]{"ID", "Полное имя", "Возраст", "Пол", "Номер телефона", "Паспортные данные", "Стаж работы", "Специальность", "Зарплата (Руб)", "Количество смен", "Выходные дни"});
        fieldName = new JTextField("Введите необходимый элемент"); // Поле для ввода значения для фильтрации
        filter = new JButton("Поиск"); // Кнопка для фильтрации
        filter.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                logger.info("Пользователь нажал кнопку 'Поиск'.");
                filterRecords(); // Вызов метода фильтрации при нажатии кнопки поиска
            }
        });

        // Панель для размещения элементов фильтрации
        JPanel filterPanel = new JPanel();
        filterPanel.add(field); // Добавление выпадающего списка
        filterPanel.add(fieldName); // Добавление текстового поля
        filterPanel.add(filter); // Добавление кнопки поиска

        // Добавление панели фильтрации в окно
        workerList.add(filterPanel, BorderLayout.SOUTH);

        // Отображение окна
        workerList.setVisible(true);
    }
}
