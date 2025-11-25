package test.hibernate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRXmlDataSource;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
import javax.persistence.*;
import javax.persistence.criteria.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Класс Specialization_interface представляет собой интерфейс для управления специальностями.
 * Он предоставляет функции для добавления, редактирования, удаления и фильтрации записей специальностей,
 * а также для загрузки и сохранения данных в XML и генерации отчетов.
 */
public class Specialization_interface {
    protected JFrame specializationList; // Окно для отображения списка специальностей
    protected DefaultTableModel model; // Модель таблицы для отображения специальностей
    private JToolBar toolBar; // Панель инструментов
    private JButton save; // Кнопка для сохранения данных
    private JButton edit; // Кнопка для редактирования записи
    private JButton delete; // Кнопка для удаления записи
    private JButton adding; // Кнопка для добавления новой записи
    private JButton back; // Кнопка для возврата в главное меню
    private JButton open; // Кнопка для открытия полного списка
    private JButton cancel; // Кнопка для сброса выбора
    private JButton report; // Кнопка для создания отчета
    private JScrollPane scroll; // Прокручиваемая панель для таблицы
    private JButton loadButton; // Кнопка для загрузки данных из XML
    protected JTable specializations; // Таблица для отображения специальностей
    protected JComboBox field; // Выпадающий список для фильтрации
    protected JTextField fieldName; // Поле для ввода текста фильтра
    private JButton filter; // Кнопка для фильтрации записей

    public boolean errorMessageShown = false; // Флаг для отображения сообщения об ошибке
    public String errorMessage = ""; // Сообщение об ошибке
    public boolean isTesting = false; // Флаг для тестирования

    private static final Logger logger = LogManager.getLogger(Specialization_interface.class); // Логгер для записи сообщений

    /**
     * Метод для удаления выбранной записи из базы данных.
     */
    protected void deleteRecord() {
        int selectedRow = specializations.getSelectedRow(); // Получаем выбранную строку из таблицы
        logger.warn("Попытка удалить запись: строка " + selectedRow);

        if (selectedRow != -1) { // Проверяем, выбрана ли строка
            EntityManager em = null;
            EntityManagerFactory emf = Persistence.createEntityManagerFactory("test_persistence");
            try {
                int id = Integer.parseInt(specializations.getValueAt(selectedRow, 0).toString()); // Получаем ID записи
                em = emf.createEntityManager();
                em.getTransaction().begin(); // Начинаем транзакцию

                Specialization specializationToRemove = em.find(Specialization.class, id); // Находим специальность по ID
                if (specializationToRemove != null) {
                    // Диалоговое окно для подтверждения удаления
                    int confirmation = JOptionPane.showConfirmDialog(specializationList,
                            "Вы уверены, что хотите удалить эту запись?",
                            "Подтверждение удаления",
                            JOptionPane.YES_NO_OPTION);

                    if (confirmation == JOptionPane.YES_OPTION) {
                        em.remove(specializationToRemove); // Удаляем специальность
                        em.getTransaction().commit(); // Подтверждаем транзакцию
                        model.removeRow(selectedRow); // Удаляем строку из модели таблицы
                        logger.debug("Запись удалена: ID = " + id);
                        errorMessageShown = false; // Сбрасываем флаг ошибки
                        errorMessage = ""; // Сбрасываем сообщение об ошибке
                    } else {
                        // Если пользователь отменил удаление
                        logger.info("Удаление отменено пользователем: ID = " + id);
                    }
                } else {
                    logger.warn("Запись не найдена в базе данных: ID = " + id);
                    errorMessageShown = true;
                    errorMessage = "Запись не найдена в базе данных.";
                    JOptionPane.showMessageDialog(specializationList, errorMessage);
                }
            } catch (NumberFormatException e) {
                logger.error("Ошибка преобразования ID: " + e.getMessage(), e);
                errorMessageShown = true;
                errorMessage = "Ошибка преобразования ID.";
                JOptionPane.showMessageDialog(specializationList, errorMessage);
            } catch (Exception e) {
                logger.error("Ошибка удаления записи из базы данных: " + e.getMessage(), e);
                if (em != null && em.getTransaction().isActive()) {
                    em.getTransaction().rollback(); // Откатываем транзакцию в случае ошибки
                }
                errorMessageShown = true;
                errorMessage = "Невозможно удалить запись из базы данных, так как она имеет связи с другими объектами." +
                        "Перед удалением объекта убедитесь, что он не используется в других таблицах.";
                JOptionPane.showMessageDialog(specializationList, errorMessage);
            } finally {
                if (em != null && em.isOpen()) {
                    em.close(); // Закрываем EntityManager
                }
            }
        } else {
            errorMessageShown = true;
            errorMessage = "Выберите строку для удаления.";
            logger.warn(errorMessage);
            JOptionPane.showMessageDialog(specializationList, errorMessage);
        }
    }

    /**
     * Метод для редактирования выбранной записи в базе данных.
     */
    protected void editRecord() {
        int selectedRow = specializations.getSelectedRow(); // Получаем выбранную строку из таблицы
        logger.warn("Попытка редактировать запись: строка " + selectedRow);

        if (selectedRow != -1) { // Проверяем, выбрана ли строка
            int id = Integer.parseInt(specializations.getValueAt(selectedRow, 0).toString()); // Получаем ID записи
            String currentSpecialization = (String) model.getValueAt(selectedRow, 1); // Получаем текущее название специальности
            String newSpecialization; // Переменная для нового названия специальности

            EntityManagerFactory emf = Persistence.createEntityManagerFactory("test_persistence");
            EntityManager em = null;

            try {
                em = emf.createEntityManager(); // Создаем EntityManager

                while (true) { // Цикл для повторного открытия окна редактирования
                    JTextField textField = new JTextField(currentSpecialization); // Поле для ввода новой специальности
                    JLabel idLabel = new JLabel(String.valueOf(id)); // Используем JLabel для отображения ID
                    Object[] message = {
                            "ID:", idLabel, // Показываем ID как метку
                            "Специальность:", textField // Поле для ввода специальности
                    };

                    int option = JOptionPane.showConfirmDialog(specializationList, message, "Редактировать запись", JOptionPane.OK_CANCEL_OPTION);
                    if (option == JOptionPane.OK_OPTION) {
                        newSpecialization = textField.getText().trim(); // Получаем введенное значение

                        // Валидация: строка не может быть пустой и не может содержать цифры
                        if (newSpecialization.isEmpty()) {
                            JOptionPane.showMessageDialog(specializationList, "Пожалуйста, заполните специальность.");
                            errorMessageShown = true;
                            errorMessage = "Специальность не может быть пустой.";
                            logger.warn(errorMessage);
                            continue; // Возврат в начало цикла для повторного ввода
                        }

                        if (!newSpecialization.matches("[a-zA-Zа-яА-ЯёЁ\\s]+")) { // Проверка на наличие только букв
                            JOptionPane.showMessageDialog(specializationList, "Специальность может содержать только буквы.");
                            errorMessageShown = true;
                            errorMessage = "Специальность может содержать только буквы.";
                            logger.warn(errorMessage);
                            continue; // Возврат в начало цикла для повторного ввода
                        }

                        // Если валидация прошла успешно, выходим из цикла
                        break;
                    } else {
                        return; // Выход, если отменено
                    }
                }

                em.getTransaction().begin(); // Начинаем транзакцию
                Specialization specializationToUpdate = em.find(Specialization.class, id); // Находим специальность по ID
                if (specializationToUpdate != null) {
                    specializationToUpdate.setSpecialization_name(newSpecialization); // Обновляем название специальности
                    em.getTransaction().commit(); // Подтверждаем транзакцию
                    model.setValueAt(newSpecialization, selectedRow, 1); // Обновляем модель таблицы
                    logger.info("Запись обновлена: ID = " + id + ", новая специальность: " + newSpecialization);
                    errorMessageShown = false; // Сбрасываем флаг ошибки
                    errorMessage = ""; // Сбрасываем сообщение об ошибке
                } else {
                    logger.warn("Запись не найдена в базе данных: ID = " + id);
                    errorMessageShown = true;
                    errorMessage = "Запись не найдена в базе данных.";
                    JOptionPane.showMessageDialog(specializationList, errorMessage);
                }
            } catch (Exception e) {
                logger.error("Ошибка обновления записи в базе данных: " + e.getMessage(), e);
                if (em != null && em.getTransaction().isActive()) {
                    em.getTransaction().rollback(); // Откатываем транзакцию в случае ошибки
                }
                errorMessageShown = true;
                errorMessage = "Ошибка обновления записи в базе данных.";
                JOptionPane.showMessageDialog(specializationList, errorMessage);
            } finally {
                if (em != null && em.isOpen()) {
                    em.close(); // Закрываем EntityManager
                }
            }
        } else {
            logger.warn("Попытка редактирования без выбора строки.");
            JOptionPane.showMessageDialog(specializationList, "Выберите строку для редактирования.");
            errorMessageShown = true;
            errorMessage = "Выберите строку для редактирования.";
        }
    }

    /**
     * Метод для открытия полного списка специальностей из базы данных.
     */
    private void openFullList() {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("test_persistence");
        EntityManager em = emf.createEntityManager();
        try {
            // Создаем JPA запрос для получения всех специальностей
            TypedQuery<Specialization> query = em.createQuery("SELECT s FROM Specialization s", Specialization.class);
            List<Specialization> specializations = query.getResultList(); // Получаем список специальностей

            // Обновляем модель таблицы данными из базы данных
            model.setRowCount(0); // Очищаем существующие строки
            model.setColumnIdentifiers(new String[]{"ID", "Специальность"}); // Устанавливаем заголовки столбцов

            for (Specialization specialization : specializations) {
                model.addRow(new Object[]{specialization.getSid(), specialization.getSpecialization_name()}); // Добавляем данные в модель
            }

            logger.info("Полный список из базы данных выведен на экран.");
        } catch (Exception e) {
            logger.error("Ошибка при получении полного списка из базы данных: " + e.getMessage(), e);
            JOptionPane.showMessageDialog(null, "Ошибка при получении полного списка из базы данных."); // Используем null как родителя для диалога
        } finally {
            if (em != null && em.isOpen()) {
                em.close(); // Закрываем EntityManager
            }
        }
    }

    /**
     * Метод для добавления новой записи в базу данных.
     */
    protected void addRecord() {
        JTextField specializationField = new JTextField(); // Поле для ввода специальности
        JLabel idLabel = new JLabel("ID будет назначен после добавления"); // Устанавливаем текст метки
        Object[] message = {
                "ID:", idLabel, // Показываем ID как метку
                "Специальность:", specializationField // Поле для ввода специальности
        };

        EntityManagerFactory emf = null; // Объект для создания EntityManager
        EntityManager em = null;

        try {
            // Создаем EntityManagerFactory и EntityManager
            emf = Persistence.createEntityManagerFactory("test_persistence");
            em = emf.createEntityManager();

            while (true) {
                int option = JOptionPane.showConfirmDialog(specializationList, message, "Добавить запись", JOptionPane.OK_CANCEL_OPTION);

                if (option == JOptionPane.CANCEL_OPTION) {
                    errorMessageShown = true;
                    errorMessage = "Отмена действия.";
                    logger.info("Добавление записи отменено пользователем.");
                    return; // Выход, если отменено
                }

                String specialization = specializationField.getText().trim(); // Получаем введенное значение

                // Валидация: строка не может быть пустой и не может содержать цифры
                if (specialization.isEmpty()) {
                    JOptionPane.showMessageDialog(specializationList, "Пожалуйста, заполните специальность.");
                    errorMessageShown = true;
                    errorMessage = "Специальность не может быть пустой.";
                    logger.warn(errorMessage);
                    continue; // Возврат в начало цикла для повторного ввода
                }

                if (!specialization.matches("[a-zA-Zа-яА-ЯёЁ\\s]+")) { // Проверка на наличие только букв
                    JOptionPane.showMessageDialog(specializationList, "Специальность может содержать только буквы.");
                    errorMessageShown = true;
                    errorMessage = "Специальность может содержать только буквы.";
                    logger.warn(errorMessage);
                    continue; // Возврат в начало цикла для повторного ввода
                }

                try {
                    em.getTransaction().begin(); // Начинаем транзакцию

                    Specialization spec = new Specialization(); // Создаем новый объект специальности
                    spec.setSpecialization_name(specialization); // Устанавливаем название специальности

                    em.persist(spec); // Сохраняем объект в базе данных
                    em.getTransaction().commit(); // Подтверждаем транзакцию

                    // Получаем ID добавленной записи
                    int id = spec.getSid(); // Предполагается, что у вас есть метод getId() в классе Specialization

                    idLabel.setText(String.valueOf(id)); // Устанавливаем ID в метку
                    model.addRow(new Object[]{id, specialization}); // Добавляем в модель таблицы после успешного добавления в БД
                    logger.info("Добавлена новая запись: ID = " + id + ", специальность = " + specialization);
                    break; // Выход из цикла, если добавление прошло успешно

                } catch (Exception e) {
                    JOptionPane.showMessageDialog(specializationList, "Ошибка добавления записи в базу данных: " + e.getMessage());
                    logger.error("Ошибка добавления записи в базу данных: " + e.getMessage(), e);
                    errorMessageShown = true;
                    errorMessage = "Ошибка добавления записи в базу данных.";
                    if (em.getTransaction().isActive()) {
                        em.getTransaction().rollback(); // Откатываем транзакцию в случае ошибки
                    }
                }
            }
        } catch (Exception ex) { // Обработка неожиданных исключений
            logger.error("Unexpected error in addRecord:", ex);
            JOptionPane.showMessageDialog(specializationList, "Критическая ошибка: " + ex.getMessage());
        } finally {
            if (em != null && em.isOpen()) {
                em.close(); // Закрываем EntityManager
            }
            if (emf != null) {
                emf.close(); // Закрываем EntityManagerFactory
            }
        }
    }

    /**
     * Метод для фильтрации записей по выбранному полю и введенному тексту.
     */
    protected void filterRecords() {
        String selectedField = (String) field.getSelectedItem(); // Получаем выбранное поле для фильтрации
        String searchText = fieldName.getText().toLowerCase(); // Получаем текст для поиска

        logger.info("Фильтрация записей по полю: " + selectedField + ", текст: " + searchText);

        // Проверка на пустое поле или на текст "Введите необходимый элемент"
        if (searchText.isEmpty() || "введите необходимый элемент".equals(searchText)) {
            errorMessageShown = true;
            errorMessage = "Введите текст для поиска.";
            logger.warn(errorMessage);
            if (!isTesting) {
                JOptionPane.showMessageDialog(specializationList, "Введите текст для поиска.", "Ошибка поиска", JOptionPane.INFORMATION_MESSAGE);
            }
            return; // Выход из метода, если текст пуст
        }

        // Проверка на допустимые символы (только буквы)
        if ("Специальность".equals(selectedField) && !searchText.matches("[a-zа-яёA-ZА-ЯЁ]+")) {
            errorMessageShown = true;
            errorMessage = "Введите только буквы для фильтрации по специальности.";
            logger.warn(errorMessage);
            JOptionPane.showMessageDialog(specializationList, errorMessage, "Ошибка ввода", JOptionPane.ERROR_MESSAGE);
            return; // Выход из метода, если введены недопустимые символы
        }

        EntityManagerFactory emf = Persistence.createEntityManagerFactory("test_persistence");
        EntityManager em = emf.createEntityManager();
        try {
            CriteriaBuilder cb = em.getCriteriaBuilder(); // Создаем CriteriaBuilder для построения запросов
            CriteriaQuery<Specialization> cq = cb.createQuery(Specialization.class); // Создаем запрос
            Root<Specialization> root = cq.from(Specialization.class); // Определяем корень запроса
            Predicate predicate = null; // Условие для фильтрации

            if ("ID".equals(selectedField)) {
                try {
                    int idToSearch = Integer.parseInt(searchText); // Преобразуем текст в ID
                    predicate = cb.equal(root.get("sid"), idToSearch); // Условие для фильтрации по ID
                } catch (NumberFormatException e) {
                    logger.warn("Неверный формат ID для поиска: " + searchText);
                    JOptionPane.showMessageDialog(specializationList, "Неверный формат ID для поиска.");
                    return; // Выход из метода, если формат неверный
                }
            } else if ("Специальность".equals(selectedField)) {
                predicate = cb.like(cb.lower(root.get("specialization_name")), "%" + searchText + "%"); // Условие для фильтрации по названию
            }

            if (predicate != null) {
                cq.select(root).where(predicate); // Применяем условие к запросу
                List<Specialization> filteredSpecializations = em.createQuery(cq).getResultList(); // Выполняем запрос и получаем результаты

                model.setRowCount(0); // Очистка таблицы
                if (filteredSpecializations.isEmpty()) {
                    JOptionPane.showMessageDialog(specializationList, "Нет записей, соответствующих вашему запросу.", "Результаты поиска", JOptionPane.INFORMATION_MESSAGE);
                    logger.info("Нет записей, соответствующих вашему запросу.");
                } else {
                    for (Specialization spec : filteredSpecializations) {
                        model.addRow(new Object[]{spec.getSid(), spec.getSpecialization_name()}); // Добавляем отфильтрованные записи в модель
                    }
                    logger.info("Записи отфильтрованы, найдено: " + filteredSpecializations.size() + " записей.");
                }
            }
        } catch (Exception e) {
            logger.error("Ошибка при выполнении запроса к базе данных: " + e.getMessage(), e);
            JOptionPane.showMessageDialog(specializationList, "Ошибка при выполнении запроса к базе данных.");
        } finally {
            if (em != null && em.isOpen()) {
                em.close(); // Закрываем EntityManager
            }
        }
    }

    /**
     * Метод для сохранения данных специальностей в XML файл.
     */
    private void saveToXML() {
        // Получаем данные из модели таблицы
        List<Object[]> data = new ArrayList<>();
        for (int row = 0; row < model.getRowCount(); row++) {
            Object[] rowData = new Object[model.getColumnCount()]; // Массив для хранения данных строки
            for (int col = 0; col < model.getColumnCount(); col++) {
                rowData[col] = model.getValueAt(row, col); // Получаем данные из каждой строки
            }
            data.add(rowData); // Добавляем строку в список данных
        }

        // Определите путь к файлу и названия столбцов
        String filePath = "C:\\Users\\bobbo\\OneDrive\\Рабочий стол\\ООП\\HibernateTest\\specializations.xml"; // Путь к XML-файлу
        String[] columnNames = {"ID", "Специальность"}; // Названия столбцов
        XMLExporter exporter = new XMLExporter(); // Создаем экземпляр XMLExporter
        exporter.exportToXML(data, filePath, "service_station", columnNames, "specializations"); // Экспортируем данные в XML
    }

    /**
     * Метод генерирует отчет в формате PDF на основе данных специальностей.
     */
    private void generateReport() {
        // Определите путь к новому файлу XML для отчета
        String reportXmlPath = "C:\\Users\\bobbo\\OneDrive\\Рабочий стол\\ООП\\HibernateTest\\report_specializations.xml";

        // Удаление существующего XML-файла, если он существует
        File reportFile = new File(reportXmlPath);
        try {
            if (reportFile.exists()) {
                // Удаляем файл, если он существует
                reportFile.delete();
            }
            // Создаем новый файл
            reportFile.createNewFile();
        } catch (IOException e) {
            logger.error("Ошибка при удалении или создании файла отчета: " + e.getMessage(), e);
            JOptionPane.showMessageDialog(specializationList, "Ошибка при удалении или создании файла отчета: " + e.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
            return; // Прерываем выполнение метода, если произошла ошибка
        }

        // Сбор данных для отчета
        List<Object[]> reportData = new ArrayList<>();
        for (int row = 0; row < model.getRowCount(); row++) {
            Object[] rowData = new Object[model.getColumnCount()];
            for (int col = 0; col < model.getColumnCount(); col++) {
                rowData[col] = model.getValueAt(row, col);
            }
            reportData.add(rowData);
        }

        // Проверка на наличие данных для отчета
        if (reportData.isEmpty()) {
            logger.warn("Нет данных для записи в отчет.");
            JOptionPane.showMessageDialog(specializationList, "Нет данных для записи в отчет.", "Предупреждение", JOptionPane.WARNING_MESSAGE);
            return; // Прерываем выполнение метода, если нет данных
        }

        String[] columnNames = {"ID", "Специальность"}; // Названия столбцов
        XMLExporter exporter = new XMLExporter();

        // Попробуйте записать данные в XML
        try {
            exporter.exportToXML(reportData, reportXmlPath, "service_station", columnNames, "specializations");
        } catch (Exception e) {
            logger.error("Ошибка при записи данных в XML: " + e.getMessage(), e);
            JOptionPane.showMessageDialog(specializationList, "Ошибка при записи данных в XML: " + e.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
            return; // Прерываем выполнение метода, если произошла ошибка
        }

        // Теперь создаем отчет на основе нового XML-файла
        String datasource = reportXmlPath; // Используем новый файл
        String xpath = "service_station/specializations/row"; // Убедитесь, что XPath правильный
        String template = "C:\\Users\\bobbo\\OneDrive\\Рабочий стол\\ООП\\HibernateTest\\Test.jrxml";
        String resultPath = "C:\\Users\\bobbo\\OneDrive\\Рабочий стол\\ООП\\HibernateTest\\specializations_report.pdf";

        try {
            // Создание источника данных для отчета на основе XML
            JRDataSource ds = new JRXmlDataSource(datasource, xpath);
            // Компиляция шаблона отчета
            JasperReport jasperReport = JasperCompileManager.compileReport(template);

            Map<String, Object> parameters = new HashMap<>();
            // parameters.put("paramName", paramValue); // Добавьте параметры, если нужно

            // Заполнение отчета данными
            JasperPrint print = JasperFillManager.fillReport(jasperReport, parameters, ds);
            JRExporter exporterPdf = new JRPdfExporter();
            exporterPdf.setParameter(JRExporterParameter.OUTPUT_FILE_NAME, resultPath); // Указываем путь для сохранения PDF
            exporterPdf.setParameter(JRExporterParameter.JASPER_PRINT, print);
            exporterPdf.exportReport(); // Экспорт отчета в PDF

            logger.info("Отчет успешно сгенерирован: " + resultPath);
            JOptionPane.showMessageDialog(specializationList, "Отчет успешно сгенерирован: " + resultPath, "Успех", JOptionPane.INFORMATION_MESSAGE);

        } catch (JRException e) {
            logger.warn("Ошибка при генерации отчета: " + e.getMessage());
            JOptionPane.showMessageDialog(specializationList, "Ошибка при генерации отчета: " + e.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Метод загружает данные специальностей из XML-файла и сохраняет их в базе данных.
     */
    protected void loadFromXML() {
        File xmlFile = new File("C:\\Users\\bobbo\\OneDrive\\Рабочий стол\\ООП\\HibernateTest\\specializations.xml");
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("test_persistence");
        EntityManager em = emf.createEntityManager();

        try {
            // Создаем парсер для XML
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(xmlFile);
            doc.getDocumentElement().normalize();

            // Получаем все строки специальностей
            NodeList nList = doc.getElementsByTagName("row");

            em.getTransaction().begin(); // Начинаем транзакцию

            for (int temp = 0; temp < nList.getLength(); temp++) {
                Node nNode = nList.item(temp);
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;

                    // Получаем данные из XML
                    String specializationName = eElement.getElementsByTagName("Специальность").item(0).getTextContent();

                    // Создаем новый объект Specialization
                    Specialization specialization = new Specialization();
                    specialization.setSpecialization_name(specializationName);

                    // Сохраняем объект в базе данных
                    em.persist(specialization);
                }
            }

            em.getTransaction().commit(); // Подтверждаем транзакцию
            logger.info("Данные успешно загружены из XML и сохранены в базе данных.");

            // Обновляем таблицу в интерфейсе
            openFullList(); // Вызываем метод для обновления таблицы

        } catch (Exception e) {
            logger.error("Ошибка при загрузке данных из XML: " + e.getMessage(), e);
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback(); // Откатываем транзакцию в случае ошибки
            }
            JOptionPane.showMessageDialog(specializationList, "Ошибка при загрузке данных из XML: " + e.getMessage());
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
            if (emf != null) {
                emf.close();
            }
        }
    }

    /**
     * Метод создает и отображает окно для работы со списком специальностей.
     */
    public void show0() {
        // Создание основного окна для списка специальностей
        specializationList = new JFrame("Список специальностей");
        specializationList.setSize(500, 300);
        specializationList.setLocation(100, 100);
        specializationList.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Кнопка для сохранения списка специальностей
        save = new JButton(new ImageIcon("Files/save.png"));
        save.setToolTipText("Сохранить список специальностей");
        save.addActionListener(e -> {
            logger.info("Пользователь нажал кнопку 'Сохранить'.");
            saveToXML(); // Вызываем метод сохранения в XML
        });

        // Кнопка для возврата в основное меню
        back = new JButton(new ImageIcon("Files/back.png"));
        back.setToolTipText("Вернуться в основное меню");
        back.addActionListener(e -> {
            logger.info("Пользователь нажал кнопку 'Назад'.");
            specializationList.dispose(); // Закрываем текущее окно
            logger.info("Открыто главное меню.");
        });

        // Кнопка для добавления новой записи
        adding = new JButton(new ImageIcon("Files/add.png"));
        adding.setToolTipText("Добавить новую запись");
        adding.addActionListener(e -> {
            logger.info("Пользователь нажал кнопку 'Добавить запись'.");
            addRecord(); // Вызываем метод добавления записи
        });

        // Кнопка для удаления записи
        delete = new JButton(new ImageIcon("Files/delete.png"));
        delete.setToolTipText("Удалить запись");
        delete.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                logger.info("Пользователь нажал кнопку 'Удалить запись'.");
                deleteRecord(); // Вызываем метод удаления записи
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
                openFullList(); // Вызываем метод для открытия полного списка
            }
        });

        // Кнопка для сброса выбора
        cancel = new JButton(new ImageIcon("Files/cancel.png"));
        cancel.setToolTipText("Сбросить выбор");
        cancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                logger.info("Пользователь нажал кнопку 'Сбросить выбор'.");
                specializations.clearSelection(); // Сбрасываем выделение в таблице
            }
        });

        // Кнопка для создания PDF-отчета
        report = new JButton(new ImageIcon("Files/pdf-save.png"));
        report.setToolTipText("Создать PDF-отчёт");
        report.addActionListener(e -> {
            logger.info("Пользователь нажал кнопку 'Создать PDF-отчет'.");
            generateReport(); // Вызываем метод генерации отчета
        });

        // Кнопка для загрузки данных из XML
        loadButton = new JButton(new ImageIcon("Files/import_from_XML.png"));
        loadButton.setToolTipText("Загрузить данные из XML");
        loadButton.addActionListener(e -> {
            logger.info("Пользователь нажал кнопку 'Загрузить данные из XML'.");
            loadFromXML(); // Вызов метода загрузки из XML
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
        toolBar.add(report);
        toolBar.add(loadButton); // Добавляем кнопку на панель инструментов

        specializationList.setLayout(new BorderLayout());
        specializationList.add(toolBar, BorderLayout.NORTH); // Добавляем панель инструментов в верхнюю часть окна

        EntityManagerFactory emf = null;
        EntityManager em = null;
        List<Specialization> specializationsFromDb = new ArrayList<>();

        try {
            // Подключение к базе данных и получение данных специальностей
            emf = Persistence.createEntityManagerFactory("test_persistence");
            em = emf.createEntityManager();
            TypedQuery<Specialization> query = em.createQuery("SELECT s FROM Specialization s", Specialization.class);
            specializationsFromDb = query.getResultList();
        } catch (Exception e) {
            logger.error("Error fetching specializations from database: ", e);
            JOptionPane.showMessageDialog(specializationList, "Ошибка загрузки данных из базы данных.");
        } finally {
            if (em != null && em.isOpen()) {
                em.close(); // Закрываем EntityManager
            }
            if (emf != null) {
                emf.close(); // Закрываем EntityManagerFactory
            }
        }

        // Преобразование данных из базы данных в двумерный массив для модели таблицы
        Object[][] data = new Object[specializationsFromDb.size()][2];
        for (int i = 0; i < specializationsFromDb.size(); i++) {
            Specialization spec = specializationsFromDb.get(i);
            data[i][0] = spec.getSid(); // ID специальности
            data[i][1] = spec.getSpecialization_name(); // Название специальности
        }

        String[] columns = {"ID", "Специальность"}; // Названия столбцов
        model = new DefaultTableModel(data, columns); // Создаем модель таблицы
        specializations = new JTable(model); // Создаем таблицу на основе модели
        specializations.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); // Устанавливаем режим выбора
        scroll = new JScrollPane(specializations); // Добавляем таблицу в прокручиваемую панель

        specializationList.add(scroll, BorderLayout.CENTER); // Добавляем прокручиваемую панель в центр окна

        // Создание панели фильтрации
        field = new JComboBox(new String[]{"ID", "Специальность"}); // Выпадающий список для выбора поля фильтрации
        fieldName = new JTextField("Введите необходимый элемент"); // Поле для ввода ключевого слова фильтрации
        filter = new JButton("Поиск"); // Кнопка для выполнения фильтрации
        filter.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                logger.info("Пользователь нажал кнопку 'Поиск'.");
                filterRecords(); // Вызов метода фильтрации при нажатии кнопки поиска
            }
        });

        // Создание панели для фильтрации и добавление компонентов
        JPanel filterPanel = new JPanel();
        filterPanel.add(field);
        filterPanel.add(fieldName);
        filterPanel.add(filter);

        specializationList.add(filterPanel, BorderLayout.SOUTH); // Добавляем панель фильтрации в нижнюю часть окна

        specializationList.setVisible(true); // Отображаем окно
    }
}
