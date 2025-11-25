package test.hibernate;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class EditSpecializationTest {
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
    public void testEditRecord_WithSelectedRow() {
        // Устанавливаем начальные значения в модели
        specializationInterface.model.setValueAt("1", 0, 0); // Устанавливаем ID
        specializationInterface.model.setValueAt("Электрик", 0, 1); // Устанавливаем старую специальность

        // Устанавливаем выбранную строку (например, первую строку)
        specializationInterface.specializations.setRowSelectionInterval(0, 0);

        // Имитация выбора специальности
        String newSpecialization = "Механик";
        JComboBox<String> specializationComboBox = new JComboBox<>(new String[]{"Электрик", "Мойщик", "Кладовщик", newSpecialization});
        specializationComboBox.setSelectedItem(newSpecialization); // Устанавливаем новую специальность

        System.out.println("Тест: testEditRecord_WithSelectedRow");

        // Выводим исходное состояние строки перед редактированием
        System.out.println("Исходная строка: ID = " + specializationInterface.model.getValueAt(0, 0) +
                ", Специальность = " + specializationInterface.model.getValueAt(0, 1));

        // Вызываем метод editRecord
        specializationInterface.editRecord();

        // Проверяем, что значение в модели обновлено
        assertEquals("1", specializationInterface.model.getValueAt(0, 0)); // ID должен остаться тем же
        assertEquals(newSpecialization, specializationInterface.model.getValueAt(0, 1)); // Проверяем, что специальность обновилась

        // Выводим редактированное состояние строки
        System.out.println("Редактированная строка: ID = " + specializationInterface.model.getValueAt(0, 0) +
                ", Специальность = " + specializationInterface.model.getValueAt(0, 1));

        // Выводим результаты теста
        System.out.println("errorMessageShown: " + specializationInterface.errorMessageShown);
        System.out.println("errorMessage: " + specializationInterface.errorMessage); // Выводим значение для отладки
    }

    @Test
    public void testEditRecord_WithoutSelectedRow() {
        // Убедитесь, что строка не выбрана
        specializationInterface.specializations.clearSelection(); // Очистите выбор строк

        // Сохраняем текущее состояние для проверки
        int initialRowCount = specializationInterface.model.getRowCount();

        // Вызываем метод editRecord
        specializationInterface.editRecord();

        // Проверяем, что количество строк осталось прежним
        int newRowCount = specializationInterface.model.getRowCount();
        assertEquals(initialRowCount, newRowCount);

        // Проверяем, что сообщение об ошибке было показано
        System.out.println("Тест: testEditRecord_WithoutSelectedRow");
        System.out.println("errorMessageShown: " + specializationInterface.errorMessageShown);
        System.out.println("errorMessage: " + specializationInterface.errorMessage); // Выводим значение для отладки
        System.out.println("");

        assertTrue(specializationInterface.errorMessageShown);
        assertEquals("Выберите строку для редактирования.", specializationInterface.errorMessage);
    }
}
