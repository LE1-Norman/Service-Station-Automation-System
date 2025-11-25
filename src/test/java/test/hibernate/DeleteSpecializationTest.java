package test.hibernate;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class DeleteSpecializationTest {
    private Specialization_interface specializationInterface;

    @BeforeEach
    public void setUp() {
        specializationInterface = new Specialization_interface();
        specializationInterface.isTesting = true;
        // Инициализация таблицы с данными для тестов
        specializationInterface.model = new DefaultTableModel(new Object[][]{
                {"1", "Электрик"},
                {"2", "Механик"}
        }, new String[]{"ID", "Специальность"});
        specializationInterface.specializations = new JTable(specializationInterface.model);
        specializationInterface.specializationList = new JFrame(); // Создаем JFrame для тестов
        specializationInterface.specializationList.setVisible(false);

        // Добавляем флаг для проверки сообщений об ошибках
        specializationInterface.errorMessageShown = false; // Предполагаем, что в вашем классе есть этот флаг
    }

    @AfterEach
    public void tearDown() {
        specializationInterface.specializationList.dispose(); // Закрываем созданное окно
    }

    @Test
    public void testDeleteRecord_WithSelectedRow() {
        // Устанавливаем выбранную строку (например, первую строку)
        specializationInterface.specializations.setRowSelectionInterval(0, 0);

        // Проверяем количество строк до удаления
        int initialRowCount = specializationInterface.model.getRowCount();

        // Вызываем метод deleteRecord
        specializationInterface.deleteRecord();

        // Проверяем количество строк после удаления
        int newRowCount = specializationInterface.model.getRowCount();

        // Выводим результаты теста
        System.out.println("Тест: testDeleteRecord_WithSelectedRow");
        System.out.println("Количество строк до удаления: " + initialRowCount);
        System.out.println("Количество строк после удаления: " + newRowCount);
        System.out.println("errorMessageShown: " + specializationInterface.errorMessageShown);
        System.out.println("errorMessage: " + specializationInterface.errorMessage); // Выводим значение для отладки

        // Убедимся, что строка была удалена
        assertEquals(initialRowCount - 1, newRowCount);
        assertFalse(specializationInterface.errorMessageShown); // Проверяем, что сообщение об ошибке не было показано
    }

    @Test
    public void testDeleteRecord_WithoutSelectedRow() {
        // Убедитесь, что строка не выбрана
        specializationInterface.specializations.clearSelection(); // Очистите выбор строк

        // Сохраняем текущее состояние для проверки
        int initialRowCount = specializationInterface.model.getRowCount();

        // Вызываем метод deleteRecord
        specializationInterface.deleteRecord();

        // Проверяем, что количество строк осталось прежним
        int newRowCount = specializationInterface.model.getRowCount();

        // Выводим результаты теста
        System.out.println("Тест: testDeleteRecord_WithoutSelectedRow");
        System.out.println("Количество строк до удаления: " + initialRowCount);
        System.out.println("Количество строк после удаления: " + newRowCount);
        System.out.println("errorMessageShown: " + specializationInterface.errorMessageShown);
        System.out.println("errorMessage: " + specializationInterface.errorMessage); // Выводим значение для отладки
        System.out.println("");

        assertEquals(initialRowCount, newRowCount);
        // Проверяем, что сообщение об ошибке было показано
        assertTrue(specializationInterface.errorMessageShown);
        assertEquals("Выберите строку для удаления.", specializationInterface.errorMessage);
    }
}
