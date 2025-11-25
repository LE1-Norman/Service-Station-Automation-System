package test.hibernate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.persistence.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

/**
 * Класс, представляющий интерфейс для работы с оказанными услугами.
 */
public class rendered_services_interface {
    private JFrame renderedServiceList; // Главное окно интерфейса
    private DefaultTableModel model; // Модель таблицы для отображения данных
    private JToolBar toolBar; // Панель инструментов
    private JButton save; // Кнопка сохранения в XML
    private JButton edit; // Кнопка редактирования записи
    private JButton delete; // Кнопка удаления записи
    private JButton adding; // Кнопка добавления новой записи
    private JButton back; // Кнопка возврата в основное меню
    private JButton open; // Кнопка открытия полного списка
    private JButton cancel; // Кнопка сброса выбора
    private JScrollPane scroll; // Прокручиваемая панель для таблицы
    private JTable services; // Таблица для отображения оказанных услуг
    private JComboBox field; // Выпадающий список для выбора поля фильтрации
    private JTextField fieldName; // Поле ввода для текста фильтрации
    private JButton filter; // Кнопка фильтрации записей

    private static final Logger logger = LogManager.getLogger(rendered_services_interface.class); // Логгер для отслеживания событий

    /**
     * Удаляет выбранную запись из базы данных и таблицы.
     */
    private void deleteRecord() {
        int selectedRow = services.getSelectedRow(); // Получаем выбранную строку
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(services, "Выберите строку для удаления."); // Сообщение об ошибке
            logger.warn("Строка для удаления не выбрана."); // Логирование предупреждения
            return;
        }

        int serviceId = (int) model.getValueAt(selectedRow, 0); // Получаем ID услуги из первого столбца
        logger.info("Попытка удаления услуги с ID: {}", serviceId); // Логирование информации о попытке удаления

        // Диалоговое окно для подтверждения удаления
        int confirm = JOptionPane.showConfirmDialog(services, "Вы уверены, что хотите удалить запись с ID " + serviceId + "?", "Подтверждение удаления", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            logger.info("Удаление записи отменено пользователем."); // Логирование информации об отмене
            return; // Если пользователь выбрал "Нет", выходим из метода
        }

        EntityManagerFactory emf = Persistence.createEntityManagerFactory("test_persistence"); // Создаем фабрику менеджеров сущностей
        EntityManager em = emf.createEntityManager(); // Создаем менеджер сущностей

        try {
            em.getTransaction().begin(); // Начинаем транзакцию

            Rendered_Service serviceToDelete = em.find(Rendered_Service.class, serviceId); // Находим услугу по ID
            if (serviceToDelete != null) {
                em.remove(serviceToDelete); // Удаляем услугу
                em.getTransaction().commit(); // Завершаем транзакцию
                model.removeRow(selectedRow); // Удаляем строку из модели таблицы
                logger.info("Запись {} удалена из базы данных и интерфейса", serviceId); // Логирование информации об успешном удалении
            } else {
                JOptionPane.showMessageDialog(services, "Запись не найдена в базе данных."); // Сообщение об ошибке
                logger.warn("Запись с ID {} не найдена в базе данных", serviceId); // Логирование предупреждения
            }
        } catch (RollbackException e) {
            JOptionPane.showMessageDialog(services, "Ошибка удаления записи из базы данных: " + e.getMessage()); // Сообщение об ошибке
            logger.error("Ошибка удаления записи из базы данных: {}", e.getMessage(), e); // Логирование ошибки
            em.getTransaction().rollback(); // Откат транзакции
        } catch (Exception e) {
            JOptionPane.showMessageDialog(services, "Ошибка удаления записи: " + e.getMessage()); // Сообщение об ошибке
            logger.error("Ошибка удаления записи: {}", e.getMessage(), e); // Логирование ошибки
        } finally {
            if (em != null && em.isOpen()) {
                em.close(); // Закрываем менеджер сущностей
            }
            if (emf != null && emf.isOpen()) {
                emf.close(); // Закрываем фабрику менеджеров сущностей
            }
        }
    }

    /**
     * Редактирует выбранную запись в базе данных и обновляет таблицу.
     */
    private void editRecord() {
        int selectedRow = services.getSelectedRow(); // Получаем выбранную строку
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(renderedServiceList, "Выберите строку для редактирования."); // Сообщение об ошибке
            logger.warn("Строка для редактирования не выбрана."); // Логирование предупреждения
            return;
        }

        int serviceId = (int) model.getValueAt(selectedRow, 0); // Получаем ID услуги
        logger.info("Редактирование услуги с ID: {}", serviceId); // Логирование информации о редактировании

        // Получаем текущие значения из модели таблицы
        String name = model.getValueAt(selectedRow, 1).toString();
        String workerName = model.getValueAt(selectedRow, 2).toString();
        String priceStr = model.getValueAt(selectedRow, 3).toString();
        String endDateStr = model.getValueAt(selectedRow, 4).toString();
        String customerName = model.getValueAt(selectedRow, 5).toString();

        // Создаем поля ввода с текущими значениями
        JTextField nameField = new JTextField(name);
        JTextField priceField = new JTextField(priceStr);
        JTextField endDateField = new JTextField(endDateStr);

        // Извлечение списка работников
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("test_persistence");
        EntityManager em = emf.createEntityManager();
        List<Worker> workers = em.createQuery("SELECT w FROM Worker w", Worker.class).getResultList();

        // Создание JComboBox для выбора рабочего
        JComboBox<Worker> workerComboBox = new JComboBox<>();
        for (Worker worker : workers) {
            workerComboBox.addItem(worker);
        }

        // Установка выбранного рабочего (если есть)
        if (workerName != null) {
            String workerIn = workerName;
            for (Worker worker : workers) {
                if (worker.getFullName().equals(workerIn)) {
                    workerComboBox.setSelectedItem(worker); // Устанавливаем выбранного рабочего
                    break;
                }
            }
        }

        // Извлечение списка клиентов
        List<Customer> customers = em.createQuery("SELECT c FROM Customer c", Customer.class).getResultList();

        // Создание JComboBox для выбора клиента
        JComboBox<Customer> customerComboBox = new JComboBox<>();
        for (Customer customer : customers) {
            customerComboBox.addItem(customer);
        }

        // Установка выбранного клиента (если есть)
        if (customerName != null) {
            String clientName = customerName;
            for (Customer customer : customers) {
                if (customer.getFullName().equals(clientName)) {
                    customerComboBox.setSelectedItem(customer); // Устанавливаем выбранного клиента
                    break;
                }
            }
        }

        // Создаем массив для отображения полей ввода в диалоговом окне
        Object[] message = {
                "Название:", nameField,
                "Рабочий:", workerComboBox, // Добавление выпадающего списка для рабочего
                "Цена услуги (Руб):", priceField,
                "Дата окончания ремонта (dd.MM.yyyy):", endDateField,
                "Клиент:", customerComboBox // Добавление выпадающего списка для клиента
        };

        // Диалоговое окно для редактирования записи
        int option = JOptionPane.showConfirmDialog(renderedServiceList, message, "Редактировать запись", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            // Проверка на пустые поля и корректность данных
            if (nameField.getText().trim().isEmpty() ||
                    priceField.getText().trim().isEmpty() ||
                    endDateField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(renderedServiceList, "Все поля должны быть заполнены."); // Сообщение об ошибке
                logger.warn("Одно или несколько полей пустые."); // Логирование предупреждения
                return;
            }

            // Проверка, что название состоит только из букв
            if (!nameField.getText().matches("[a-zA-Zа-яА-ЯёЁ\\s]+")) {
                JOptionPane.showMessageDialog(renderedServiceList, "Название должно содержать только буквы."); // Сообщение об ошибке
                logger.warn("Название содержит недопустимые символы."); // Логирование предупреждения
                return;
            }

            // Проверка на положительные числа
            try {
                int price = Integer.parseInt(priceField.getText());

                if (price <= 0) {
                    JOptionPane.showMessageDialog(renderedServiceList, "Цена должна быть положительным числом."); // Сообщение об ошибке
                    logger.warn("Неправильное значение для цены."); // Логирование предупреждения
                    return;
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(renderedServiceList, "Неверный формат числовых данных."); // Сообщение об ошибке
                logger.warn("Неверный формат числовых данных при редактировании записи: {}", e.getMessage()); // Логирование предупреждения
                return;
            }

            String dateInput = endDateField.getText();

            // Регулярное выражение для проверки формата даты dd.MM.yyyy
            String datePattern = "^\\d{2}\\.\\d{2}\\.\\d{4}$";
            if (!dateInput.matches(datePattern)) {
                JOptionPane.showMessageDialog(renderedServiceList, "Неверный формат даты. Используйте формат dd.MM.yyyy."); // Сообщение об ошибке
                logger.warn("Неверный формат даты: {}", dateInput); // Логирование предупреждения
                return;
            }

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
            dateFormat.setLenient(false); // Устанавливаем строгий режим
            try {
                dateFormat.parse(dateInput); // Проверяем корректность даты
            } catch (ParseException e) {
                JOptionPane.showMessageDialog(renderedServiceList, "Неверный формат даты. Используйте формат dd.MM.yyyy."); // Сообщение об ошибке
                logger.warn("Неверный формат даты при редактировании записи: {}", e.getMessage()); // Логирование предупреждения
                return;
            }

            try {
                em.getTransaction().begin(); // Начинаем транзакцию

                Rendered_Service serviceToUpdate = em.find(Rendered_Service.class, serviceId); // Находим услугу по ID
                if (serviceToUpdate != null) {
                    serviceToUpdate.setServiceName(nameField.getText()); // Обновляем название услуги
                    Worker selectedWorker = (Worker) workerComboBox.getSelectedItem(); // Получаем выбранного рабочего
                    if (selectedWorker != null) {
                        serviceToUpdate.setService_worker(selectedWorker.getWorker_id()); // Обновляем ID рабочего
                    }
                    serviceToUpdate.setPrice(Integer.parseInt(priceField.getText())); // Обновляем цену услуги
                    serviceToUpdate.setReturnDate(endDateField.getText()); // Сохраняем дату в нужном формате
                    Customer selectedCustomer = (Customer) customerComboBox.getSelectedItem(); // Получаем выбранного клиента
                    if (selectedCustomer != null) {
                        serviceToUpdate.setCust_id(selectedCustomer.getCustomer_id()); // Обновляем ID клиента
                    }

                    em.getTransaction().commit(); // Завершаем транзакцию
                    // Обновляем таблицу
                    model.setValueAt(nameField.getText(), selectedRow, 1); // Обновляем название в таблице
                    model.setValueAt(selectedWorker.getFullName(), selectedRow, 2); // Обновляем рабочего в таблице
                    model.setValueAt(priceField.getText(), selectedRow, 3); // Обновляем цену в таблице
                    model.setValueAt(endDateField.getText(), selectedRow, 4); // Обновляем дату в таблице
                    model.setValueAt(selectedCustomer.getFullName(), selectedRow, 5); // Обновляем клиента в таблице

                    logger.info("Запись {} обновлена в базе данных и интерфейсе", serviceId); // Логирование информации об успешном обновлении
                } else {
                    JOptionPane.showMessageDialog(renderedServiceList, "Запись не найдена в базе данных."); // Сообщение об ошибке
                    logger.warn("Запись с ID {} не найдена в базе данных", serviceId); // Логирование предупреждения
                }
            } catch (RollbackException e) {
                JOptionPane.showMessageDialog(renderedServiceList, "Ошибка обновления записи в базе данных: " + e.getMessage()); // Сообщение об ошибке
                logger.error("Ошибка обновления записи в базе данных: {}", e.getMessage(), e); // Логирование ошибки
                em.getTransaction().rollback(); // Откат транзакции
            } catch (Exception e) {
                JOptionPane.showMessageDialog(renderedServiceList, "Ошибка обновления записи: " + e.getMessage()); // Сообщение об ошибке
                logger.error("Ошибка обновления записи: {}", e.getMessage(), e); // Логирование ошибки
            } finally {
                if (em != null && em.isOpen()) {
                    em.close(); // Закрываем менеджер сущностей
                }
                if (emf != null && emf.isOpen()) {
                    emf.close(); // Закрываем фабрику менеджеров сущностей
                }
            }
        }
    }

    /**
     * Открывает полный список оказанных услуг из базы данных и отображает их в таблице.
     */
    private void openFullList() {
        EntityManagerFactory emf = null;
        EntityManager em = null;

        try {
            emf = Persistence.createEntityManagerFactory("test_persistence"); // Создаем фабрику менеджеров сущностей
            em = emf.createEntityManager(); // Создаем менеджер сущностей

            // Создаем запрос к базе данных для извлечения всех оказанных услуг
            TypedQuery<Rendered_Service> query = em.createQuery("SELECT s FROM Rendered_Service s", Rendered_Service.class);
            List<Rendered_Service> allServices = query.getResultList(); // Получаем все услуги

            // Проверяем, есть ли услуги
            if (allServices.isEmpty()) {
                JOptionPane.showMessageDialog(renderedServiceList, "Нет оказанных услуг для отображения."); // Сообщение об отсутствии услуг
                logger.info("Нет оказанных услуг для отображения."); // Логирование информации
                return; // Завершаем метод, если список пуст
            }

            // Получаем список клиентов
            TypedQuery<Customer> clientQuery = em.createQuery("SELECT c FROM Customer c", Customer.class);
            List<Customer> clientsFromDb = clientQuery.getResultList(); // Получаем всех клиентов

            // Создаем Map для быстрого поиска имени клиента по его ID
            Map<Integer, String> clientIdToNameMap = new HashMap<>();
            for (Customer client : clientsFromDb) {
                clientIdToNameMap.put(client.getCustomer_id(), client.getFullName()); // Предполагается, что метод getFullName() существует
            }

            // Получаем список рабочих
            TypedQuery<Worker> workerQuery = em.createQuery("SELECT w FROM Worker w", Worker.class);
            List<Worker> workersFromDb = workerQuery.getResultList(); // Получаем всех рабочих

            // Создаем Map для быстрого поиска имени рабочего по его ID
            Map<Integer, String> workerIdToNameMap = new HashMap<>();
            for (Worker worker : workersFromDb) {
                workerIdToNameMap.put(worker.getWorker_id(), worker.getFullName()); // Предполагается, что метод getFullName() существует
            }

            // Определяем столбцы для данных
            String[] columns = {"ID", "Название", "Рабочий", "Цена услуги (Руб)", "Дата окончания ремонта", "Клиент"};
            Object[][] data = new Object[allServices.size()][columns.length]; // Создаем массив для данных

            // Заполняем данные из списка услуг
            for (int i = 0; i < allServices.size(); i++) {
                Rendered_Service service = allServices.get(i);
                data[i][0] = service.getService_id(); // ID услуги
                data[i][1] = service.getServiceName(); // Название услуги
                // Получаем имя рабочего по ID
                String workerName = workerIdToNameMap.get(service.getService_worker());
                if (workerName == null) {
                    workerName = "Неизвестно"; // Значение по умолчанию, если рабочий не найден
                }
                data[i][2] = workerName; // Имя рабочего
                data[i][3] = service.getPrice(); // Получаем цену услуги
                data[i][4] = service.getReturnDate() != null ? service.getReturnDate().toString() : "Не указана"; // Проверка на null
                // Получаем имя клиента по ID
                String clientName = clientIdToNameMap.get(service.getCust_id());
                if (clientName == null) {
                    clientName = "Неизвестно"; // Значение по умолчанию, если клиент не найден
                }
                data[i][5] = clientName; // Имя клиента
            }

            // Обновляем модель таблицы
            model.setDataVector(data, columns); // Устанавливаем данные в модель
            logger.info("Полный список оказанных услуг открыт."); // Логирование информации об успешном открытии
        } catch (Exception e) {
            logger.error("Ошибка при загрузке услуг из базы данных: {}", e.getMessage(), e); // Логирование ошибки
            JOptionPane.showMessageDialog(renderedServiceList, "Ошибка при загрузке данных из базы данных: " + e.getMessage()); // Сообщение об ошибке
        } finally {
            // Чистим ресурсы
            if (em != null && em.isOpen()) {
                em.close(); // Закрываем менеджер сущностей
            }
            if (emf != null && emf.isOpen()) {
                emf.close(); // Закрываем фабрику менеджеров сущностей
            }
        }
    }

    /**
     * Метод для добавления новой записи в базу данных.
     */
    private void addRecord() {
        // Поля для ввода данных
        JTextField nameField = new JTextField();
        JTextField priceField = new JTextField();
        JTextField endDateField = new JTextField();

        // Извлечение списка работников
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("test_persistence");
        EntityManager em = emf.createEntityManager();
        List<Worker> workers = em.createQuery("SELECT w FROM Worker w", Worker.class).getResultList();

        // Создание JComboBox для выбора рабочего
        JComboBox<Worker> workerComboBox = new JComboBox<>();
        for (Worker worker : workers) {
            workerComboBox.addItem(worker);
        }

        // Извлечение списка клиентов
        List<Customer> customers = em.createQuery("SELECT c FROM Customer c", Customer.class).getResultList();

        // Создание JComboBox для выбора клиента
        JComboBox<Customer> customerComboBox = new JComboBox<>();
        for (Customer customer : customers) {
            customerComboBox.addItem(customer);
        }

        // Создание массива для отображения диалогового окна
        Object[] message = {
                "Название:", nameField,
                "Рабочий:", workerComboBox,
                "Цена услуги (Руб):", priceField,
                "Дата окончания ремонта (dd.MM.yyyy):", endDateField,
                "Клиент:", customerComboBox
        };

        // Цикл для повторного отображения диалогового окна, пока не будет введена корректная информация
        while (true) {
            int option = JOptionPane.showConfirmDialog(renderedServiceList, message, "Добавить запись", JOptionPane.OK_CANCEL_OPTION);
            if (option == JOptionPane.CANCEL_OPTION) {
                logger.info("Добавление записи отменено пользователем.");
                return; // Выход из метода, если пользователь отменил
            }

            String name = nameField.getText().trim();
            String priceStr = priceField.getText().trim();
            String endDateStr = endDateField.getText().trim();

            // Проверка на пустые поля
            if (name.isEmpty() || priceStr.isEmpty() || endDateStr.isEmpty()) {
                JOptionPane.showMessageDialog(renderedServiceList, "Пожалуйста, заполните все поля.");
                logger.warn("Попытка добавить запись с пустыми полями.");
                continue; // Продолжение цикла
            }

            // Проверка, что название состоит только из букв
            if (!name.matches("[a-zA-Zа-яА-ЯёЁ\\s]+")) {
                JOptionPane.showMessageDialog(renderedServiceList, "Название должно содержать только буквы.");
                logger.warn("Название содержит недопустимые символы.");
                continue; // Продолжение цикла
            }

            // Проверка, что цена содержит только положительное число
            try {
                int price = Integer.parseInt(priceStr);
                if (price <= 0) {
                    JOptionPane.showMessageDialog(renderedServiceList, "Цена должна быть положительным числом.");
                    logger.warn("Неправильное значение для цены.");
                    continue; // Продолжение цикла
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(renderedServiceList, "Неверный формат числовых данных.");
                logger.warn("Неверный формат числовых данных при добавлении записи: {}", e.getMessage());
                continue; // Продолжение цикла
            }

            // Проверка формата даты
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
            dateFormat.setLenient(false); // Устанавливаем строгий режим
            try {
                Date endDate = dateFormat.parse(endDateStr); // Парсим дату для проверки
            } catch (ParseException e) {
                JOptionPane.showMessageDialog(renderedServiceList, "Неверный формат даты. Используйте формат dd.MM.yyyy.");
                logger.warn("Неверный формат даты при добавлении записи: {}", e.getMessage());
                continue; // Продолжение цикла
            }

            // Попытка добавления записи в базу данных
            try {
                em.getTransaction().begin();

                Rendered_Service renderedService = new Rendered_Service();
                renderedService.setServiceName(name);
                Worker selectedWorker = (Worker) workerComboBox.getSelectedItem();
                if (selectedWorker != null) {
                    renderedService.setService_worker(selectedWorker.getWorker_id());
                }
                renderedService.setCust_id(((Customer) customerComboBox.getSelectedItem()).getCustomer_id());
                renderedService.setPrice(Integer.parseInt(priceStr)); // Устанавливаем цену
                renderedService.setReturnDate(dateFormat.format(dateFormat.parse(endDateStr))); // Сохраняем дату в формате строки

                em.persist(renderedService); // Сохраняем объект в базе данных
                em.getTransaction().commit(); // Подтверждаем транзакцию

                int generatedId = renderedService.getService_id(); // Получаем ID добавленной записи
                model.addRow(new Object[]{generatedId, name, selectedWorker.getFullName(), priceStr, endDateStr, ((Customer) customerComboBox.getSelectedItem()).getFullName()});
                logger.info("Добавлена новая запись в базу данных и интерфейс (ID: {})", generatedId);
                break; // Выход из цикла после успешного добавления
            } catch (RollbackException e) {
                JOptionPane.showMessageDialog(renderedServiceList, "Ошибка добавления записи в базу данных: " + e.getMessage());
                logger.error("Ошибка добавления записи в базу данных: {}", e.getMessage(), e);
                em.getTransaction().rollback(); // Откат транзакции в случае ошибки
            } catch (Exception e) {
                JOptionPane.showMessageDialog(renderedServiceList, "Ошибка добавления записи: " + e.getMessage());
                logger.error("Ошибка добавления записи: {}", e.getMessage(), e);
            } finally {
                // Закрытие ресурсов
                if (em != null && em.isOpen()) em.close();
                if (emf != null && emf.isOpen()) emf.close();
            }
        }
    }

    /**
     * Метод для фильтрации записей в таблице.
     */
    private void filterRecords() {
        String selectedField = (String) field.getSelectedItem(); // Получаем выбранный столбец
        String searchText = fieldName.getText().toLowerCase().trim(); // Получаем текст для поиска и удаляем лишние пробелы

        // Проверка на пустое поле и текст "Введите необходимый элемент"
        if (searchText.isEmpty() || searchText.equals("введите необходимый элемент")) {
            JOptionPane.showMessageDialog(renderedServiceList, "Пожалуйста, введите корректный текст для фильтрации.", "Ошибка фильтрации", JOptionPane.ERROR_MESSAGE);
            logger.warn("Попытка фильтрации с пустым полем или текстом 'Введите необходимый элемент'.");
            return; // Выходим из метода, не обновляя модель таблицы
        }

        logger.info("Фильтрация записей по полю: " + selectedField + ", текст: " + searchText);

        List<Object[]> filteredData = new ArrayList<>(); // Список для хранения отфильтрованных данных

        // Проходим по всем строкам модели и проверяем на соответствие
        for (int row = 0; row < model.getRowCount(); row++) {
            String valueToCheck = "";
            // Получаем значение из выбранного столбца
            switch (selectedField) {
                case "ID":
                    valueToCheck = model.getValueAt(row, 0).toString();
                    break;
                case "Название":
                    valueToCheck = (String) model.getValueAt(row, 1);
                    break;
                case "Рабочий":
                    valueToCheck = model.getValueAt(row, 2).toString();
                    break;
                case "Цена услуги (Руб)":
                    valueToCheck = model.getValueAt(row, 3).toString();
                    break;
                case "Дата окончания ремонта":
                    valueToCheck = (String) model.getValueAt(row, 4);
                    break;
                case "Клиент":
                    valueToCheck = model.getValueAt(row, 5).toString();
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
                        model.getValueAt(row, 5)
                });
            }
        }

        // Обновляем модель таблицы с отфильтрованными данными
        model.setRowCount(0); // Очищаем текущую модель

        if (filteredData.isEmpty()) {
            logger.warn("Нет записей, соответствующих вашему запросу.");
            // Если отфильтрованные данные пусты, выводим сообщение
            JOptionPane.showMessageDialog(renderedServiceList, "Нет записей, соответствующих вашему запросу.", "Результаты поиска", JOptionPane.INFORMATION_MESSAGE);
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
            Object[] rowData = new Object[model.getColumnCount()];
            for (int col = 0; col < model.getColumnCount(); col++) {
                rowData[col] = model.getValueAt(row, col);
            }
            data.add(rowData);
        }

        // Определите путь к файлу и названия столбцов
        String filePath = "rendered_services.xml"; // Путь к файлу
        String[] columnNames = {"ID", "Название", "Рабочий", "Цена услуги (Руб)", "Дата окончания ремонта", "Клиент"}; // Названия столбцов
        XMLExporter exporter = new XMLExporter();
        exporter.exportToXML(data, filePath, "service_station", columnNames, "rendered_services");
        logger.info("Данные сохранены в XML файл: " + filePath);
    }

    /**
     * Метод для отображения окна со списком оказанных услуг.
     */
    public void show3() {
        renderedServiceList = new JFrame("Список оказанных услуг");
        renderedServiceList.setSize(500, 300);
        renderedServiceList.setLocation(100, 100);
        renderedServiceList.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Кнопка для сохранения списка
        save = new JButton(new ImageIcon("Files/save.png"));
        save.setToolTipText("Сохранить список");
        save.addActionListener(e -> {
            logger.info("Пользователь нажал кнопку 'Сохранить'.");
            saveToXML(); // Вызов метода сохранения
        });

        // Кнопка для возврата в основное меню
        back = new JButton(new ImageIcon("Files/back.png"));
        back.setToolTipText("Вернуться в основное меню");
        back.addActionListener(e -> {
            logger.info("Пользователь нажал кнопку 'Назад'.");
            renderedServiceList.dispose(); // Закрытие окна
        });

        // Кнопка для добавления новой записи
        adding = new JButton(new ImageIcon("Files/add.png"));
        adding.setToolTipText("Добавить новую запись");
        adding.addActionListener(e -> {
            logger.info("Пользователь нажал кнопку 'Добавить запись'.");
            addRecord(); // Вызов метода добавления записи
        });

        // Кнопка для удаления записи
        delete = new JButton(new ImageIcon("Files/delete.png"));
        delete.setToolTipText("Удалить запись");
        delete.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                logger.info("Пользователь нажал кнопку 'Удалить запись'.");
                deleteRecord(); // Вызов метода удаления записи
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

        // Кнопка для открытия полного списка
        open = new JButton(new ImageIcon("Files/open.png"));
        open.setToolTipText("Открыть полный список");
        open.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                logger.info("Пользователь нажал кнопку 'Открыть полный список'.");
                openFullList(); // Вызов метода для открытия полного списка
            }
        });

        // Кнопка для сброса выбора
        cancel= new JButton(new ImageIcon("Files/cancel.png"));
        cancel.setToolTipText("Сбросить выбор");
        cancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                logger.info("Пользователь нажал кнопку 'Сбросить выбор'.");
                services.clearSelection(); // Сброс выделения в таблице
            }
        });

        // Создание панели инструментов
        toolBar = new JToolBar("Панель инструментов");
        toolBar.add(save);
        toolBar.add(back);
        toolBar.add(adding);
        toolBar.add(delete);
        toolBar.add(edit);
        toolBar.add(open);
        toolBar.add(cancel);

        // Установка компоновки окна
        renderedServiceList.setLayout(new BorderLayout());
        renderedServiceList.add(toolBar, BorderLayout.NORTH);

        EntityManagerFactory emf = Persistence.createEntityManagerFactory("test_persistence"); // Создание фабрики EntityManager
        EntityManager em = emf.createEntityManager();

        try {
            // Запрос для получения всех оказанных услуг
            TypedQuery<Rendered_Service> query = em.createQuery("SELECT rs FROM Rendered_Service rs", Rendered_Service.class);
            List<Rendered_Service> renderedServices = query.getResultList();

            // Получаем список клиентов
            TypedQuery<Customer> clientQuery = em.createQuery("SELECT c FROM Customer c", Customer.class);
            List<Customer> clientsFromDb = clientQuery.getResultList();

            // Создаем Map для быстрого поиска имени клиента по его ID
            Map<Integer, String> clientIdToNameMap = new HashMap<>();
            for (Customer client : clientsFromDb) {
                clientIdToNameMap.put(client.getCustomer_id(), client.getFullName()); // Предполагается, что метод getFullName() существует
            }

            // Получаем список рабочих
            TypedQuery<Worker> workerQuery = em.createQuery("SELECT w FROM Worker w", Worker.class);
            List<Worker> workersFromDb = workerQuery.getResultList();

            // Создаем Map для быстрого поиска имени рабочего по его ID
            Map<Integer, String> workerIdToNameMap = new HashMap<>();
            for (Worker worker : workersFromDb) {
                workerIdToNameMap.put(worker.getWorker_id(), worker.getFullName()); // Предполагается, что метод getFullName() существует
            }

            // Определение названий столбцов и создание массива для хранения данных
            String[] columns = {"ID", "Название", "Рабочий", "Цена услуги (Руб)", "Дата окончания ремонта", "Клиент"};
            Object[][] data = new Object[renderedServices.size()][columns.length];

            // Заполнение массива данными из базы
            for (int i = 0; i < renderedServices.size(); i++) {
                Rendered_Service service = renderedServices.get(i);
                data[i][0] = service.getService_id();
                data[i][1] = service.getServiceName();
                // Получаем имя рабочего по ID
                String workerName = workerIdToNameMap.get(service.getService_worker());
                if (workerName == null) {
                    workerName = "Неизвестно"; // Значение по умолчанию, если рабочий не найден
                }
                data[i][2] = workerName; // Добавляем имя рабочего
                data[i][3] = service.getPrice();
                data[i][4] = service.getReturnDate();
                // Получаем имя клиента по ID
                String clientName = clientIdToNameMap.get(service.getCust_id());
                if (clientName == null) {
                    clientName = "Неизвестно"; // Значение по умолчанию, если клиент не найден
                }
                data[i][5] = clientName; // Добавляем имя клиента
            }

            // Создание модели таблицы и добавление ее в JScrollPane
            model = new DefaultTableModel(data, columns);
            services = new JTable(model);
            scroll = new JScrollPane(services);
            renderedServiceList.add(scroll, BorderLayout.CENTER);

        } catch (Exception e) {
            logger.error("Error loading rendered services from database: {}", e.getMessage(), e);
            JOptionPane.showMessageDialog(renderedServiceList, "Error loading data from the database.", "Database Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            // Закрытие ресурсов
            if (em != null && em.isOpen()) {
                em.close();
            }
            if (emf != null && emf.isOpen()) {
                emf.close();
            }
        }

        // Создание интерфейса для фильтрации записей
        field = new JComboBox(new String[]{"ID", "Название", "Рабочий","Цена услуги (Руб)","Дата окончания ремонта","Клиент"});
        fieldName = new JTextField("Введите необходимый элемент");
        filter = new JButton("Поиск");
        filter.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                logger.info("Пользователь нажал кнопку 'Поиск'.");
                filterRecords(); // Вызов метода фильтрации при нажатии кнопки поиска
            }
        });

        // Создание панели для фильтрации
        JPanel filterPanel = new JPanel();
        filterPanel.add(field);
        filterPanel.add(fieldName);
        filterPanel.add(filter);

        // Добавление панели фильтрации в окно
        renderedServiceList.add(filterPanel, BorderLayout.SOUTH);

        renderedServiceList.setVisible(true); // Отображение окна
    }
}