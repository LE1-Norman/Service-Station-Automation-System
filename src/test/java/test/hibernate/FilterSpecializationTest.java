package test.hibernate;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class FilterSpecializationTest {
    private Specialization_interface specializationInterface;

    @BeforeEach
    public void setUp() {
        specializationInterface = new Specialization_interface();
        specializationInterface.isTesting = true;

        // Инициализация таблицы с данными для тестов
        specializationInterface.model = new DefaultTableModel(new Object[][]{
                {"1", "Электрик"},
                {"2", "Механик"},
                {"3", "Сварщик"},
                {"4", "Кладовщик"}
        }, new String[]{"ID", "Специальность"});

        specializationInterface.specializations = new JTable(specializationInterface.model);
        specializationInterface.specializationList = new JFrame(); // Создаем JFrame для тестов
        specializationInterface.specializationList.setVisible(false);

        // Инициализация JComboBox и JTextField
        specializationInterface.field = new JComboBox<>(new String[]{"ID", "Специальность"});
        specializationInterface.fieldName = new JTextField();

        // Добавляем флаг для проверки сообщений об ошибках
        specializationInterface.errorMessageShown = false; // Предполагаем, что в вашем классе есть этот флаг
    }

    @AfterEach
    public void tearDown() {
        specializationInterface.specializationList.dispose(); // Закрываем созданное окно
    }

    @Test
    public void testFilterRecords_ByID() {
        System.out.println("Тест: testFilterRecords_ByID");
        // Устанавливаем выбранное поле и текст для поиска
        specializationInterface.field.setSelectedItem("ID");
        specializationInterface.fieldName.setText("1"); // Текст для поиска

        // Выводим информацию о параметре фильтрации
        System.out.println("Фильтрация по параметру: " + specializationInterface.field.getSelectedItem() +
                ", Значение: " + specializationInterface.fieldName.getText());

        // Вызываем метод filterRecords
        specializationInterface.filterRecords();

        // Выводим результаты теста
        System.out.println("errorMessageShown: " + specializationInterface.errorMessageShown);
        System.out.println("errorMessage: " + specializationInterface.errorMessage); // Выводим значение для отладки
        System.out.println("Количество записей в модели: " + specializationInterface.model.getRowCount());

        // Проверяем, что в модели осталась только одна запись
        assertEquals(1, specializationInterface.model.getRowCount());
        assertEquals("1", specializationInterface.model.getValueAt(0, 0));
        assertEquals("Электрик", specializationInterface.model.getValueAt(0, 1));

        // Выводим информацию о найденной записи
        System.out.println("Найдена запись: ID = " + specializationInterface.model.getValueAt(0, 0) +
                ", Специальность = " + specializationInterface.model.getValueAt(0, 1));
        System.out.println("");
    }

    @Test
    public void testFilterRecords_BySpecialization() {
        System.out.println("Тест: testFilterRecords_BySpecialization");
        // Устанавливаем выбранное поле и текст для поиска
        specializationInterface.field.setSelectedItem("Специальность");
        specializationInterface.fieldName.setText("Механик"); // Текст для поиска

        // Выводим информацию о параметре фильтрации
        System.out.println("Фильтрация по параметру: " + specializationInterface.field.getSelectedItem() +
                ", Значение: " + specializationInterface.fieldName.getText());

        // Вызываем метод filterRecords
        specializationInterface.filterRecords();

        // Выводим результаты теста
        System.out.println("errorMessageShown: " + specializationInterface.errorMessageShown);
        System.out.println("errorMessage: " + specializationInterface.errorMessage); // Выводим значение для отладки
        System.out.println("Количество записей в модели: " + specializationInterface.model.getRowCount());

        // Проверяем, что в модели осталась только одна запись
        assertEquals(1, specializationInterface.model.getRowCount());
        assertEquals("2", specializationInterface.model.getValueAt(0, 0));
        assertEquals("Механик", specializationInterface.model.getValueAt(0, 1));

        // Выводим информацию о найденной записи
        System.out.println("Найдена запись: ID = " + specializationInterface.model.getValueAt(0, 0) +
                ", Специальность = " + specializationInterface.model.getValueAt(0, 1));
        System.out.println("");
    }

    @Test
    public void testFilterRecords_NoResults() {
        System.out.println("Тест: testFilterRecords_NoResults");
        // Устанавливаем выбранное поле и текст для поиска
        specializationInterface.field.setSelectedItem("ID");
        specializationInterface.fieldName.setText("999"); // Текст для поиска, которого нет в данных

        // Выводим информацию о параметре фильтрации
        System.out.println("Фильтрация по параметру: " + specializationInterface.field.getSelectedItem() +
                ", Значение: " + specializationInterface.fieldName.getText());

        // Вызываем метод filterRecords
        specializationInterface.filterRecords();

        // Выводим результаты теста
        System.out.println("errorMessageShown: " + specializationInterface.errorMessageShown);
        System.out.println("errorMessage: " + specializationInterface.errorMessage); // Выводим значение для отладки
        System.out.println("Количество записей в модели: " + specializationInterface.model.getRowCount());

        // Проверяем, что модель пуста
        assertEquals(0, specializationInterface.model.getRowCount());
        assertTrue(specializationInterface.errorMessageShown); // Предполагаем, что сообщение об ошибке было показано
        System.out.println("");
    }

    @Test
    public void testFilterRecords_EmptySearchText() {
        // Устанавливаем выбранное поле и текст для поиска
        specializationInterface.field.setSelectedItem("Специальность");
        specializationInterface.fieldName.setText(""); // Пустой текст для поиска

        // Вызываем метод filterRecords
        specializationInterface.filterRecords();

        // Выводим результаты теста
        System.out.println("Тест: testFilterRecords_EmptySearchText");
        System.out.println("errorMessageShown: " + specializationInterface.errorMessageShown);
        System.out.println("errorMessage: " + specializationInterface.errorMessage); // Выводим значение для отладки
        System.out.println("Количество записей в модели: " + specializationInterface.model.getRowCount());

        // Проверяем, что сообщение об ошибке было показано
        assertEquals(4, specializationInterface.model.getRowCount()); // Предположим, что изначально в модели 4 записи
        assertTrue(specializationInterface.errorMessageShown); // Проверяем, что сообщение об ошибке было показано
        assertEquals("Введите текст для поиска.", specializationInterface.errorMessage); // Проверяем, что сообщение об ошибке корректное
        System.out.println("");
    }
}
