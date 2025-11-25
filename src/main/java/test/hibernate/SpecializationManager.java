package test.hibernate;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRXmlDataSource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.awt.*;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class SpecializationManager {
    private JFrame specializationList;
    private DefaultTableModel model;
    private JTable specializations;
    private CountDownLatch loadLatch = new CountDownLatch(1);
    private CountDownLatch editLatch = new CountDownLatch(1);

    public SpecializationManager() {
        show0();
    }

    private void loadDataFromXML() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("Поток " + Thread.currentThread().getName() + " выполняет загрузку данных из XML");
                try {
                    XMLLoader xmlLoader = new XMLLoader();
                    // Загрузка данных из XML
                    List<Object[]> data = xmlLoader.importFromXML("C:\\Users\\bobbo\\OneDrive\\Рабочий стол\\ООП\\HibernateTest\\service_station.xml", "specializations");

                    // Обновление модели таблицы
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            String[] columns = {"ID", "Специальность"};
                            model.setDataVector(data.toArray(new Object[0][]), columns);
                        }
                    });
                } catch (Exception e) {
                    System.err.println("Ошибка при загрузке данных: " + e.getMessage());
                } finally {
                    loadLatch.countDown(); // Уведомляем о завершении загрузки
                    System.out.println("Поток " + Thread.currentThread().getName() + " завершил загрузку данных из XML.");
                }
            }
        }).start();
    }

    private void generateXMLFile(String filePath) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.newDocument();

            // Создание корневого элемента
            Element rootElement = document.createElement("specializations");
            document.appendChild(rootElement);

            // Добавление данных из таблицы
            for (int i = 0; i < model.getRowCount(); i++) {
                Element specializationElement = document.createElement("specialization");

                // Создание элемента ID
                Element idElement = document.createElement("ID");
                idElement.appendChild(document.createTextNode((String) model.getValueAt(i, 0)));
                specializationElement.appendChild(idElement);

                // Создание элемента Специальность
                Element nameElement = document.createElement("Специальность");
                nameElement.appendChild(document.createTextNode((String) model.getValueAt(i, 1)));
                specializationElement.appendChild(nameElement);

                rootElement.appendChild(specializationElement);
            }

            // Запись документа в файл
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(javax.xml.transform.OutputKeys.INDENT, "yes");
            DOMSource source = new DOMSource(document);
            StreamResult result = new StreamResult(new File(filePath));
            transformer.transform(source, result);

            System.out.println("XML файл успешно сгенерирован: " + filePath);
        } catch (Exception e) {
            System.err.println("Ошибка при генерации XML файла: " + e.getMessage());
        }
    }

    private void editDataAndGenerateXML() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String currentThreadName = Thread.currentThread().getName(); // Сохраняем имя текущего потока
                System.out.println("Поток " + currentThreadName + " выполняет редактирование данных и генерацию XML");
                try {
                    loadLatch.await(); // Ожидаем завершения загрузки данных

                    // Пример редактирования данных
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            // Предположим, что мы редактируем первую строку
                            int rowToEdit = 0; // Индекс строки, которую мы будем редактировать
                            String newSpecialization = JOptionPane.showInputDialog(specializationList,
                                    "Введите новую специальность для ID " + model.getValueAt(rowToEdit, 0) + ":",
                                    model.getValueAt(rowToEdit, 1)); // Предлагаем пользователю ввести новую специальность

                            if (newSpecialization != null && !newSpecialization.trim().isEmpty()) {
                                model.setValueAt(newSpecialization, rowToEdit, 1); // Обновляем модель таблицы
                                System.out.println("Данные успешно отредактированы: ID = " + model.getValueAt(rowToEdit, 0) +
                                        ", Специальность = " + newSpecialization);
                            }

                            // После редактирования данных вызываем метод для генерации XML
                            generateXMLFile("updated_specializations.xml"); // Путь к новому XML-файлу
                            editLatch.countDown(); // Уведомляем о завершении редактирования
                            System.out.println("Поток " + currentThreadName + " завершил редактирование данных и генерацию XML.");
                        }
                    });

                } catch (InterruptedException e) {
                    System.err.println("Ошибка при редактировании данных: " + e.getMessage());
                }
            }
        }).start();
    }

    private void generateHtmlReport() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("Поток " + Thread.currentThread().getName() + " выполняет генерацию отчета в HTML");
                try {
                    editLatch.await(); // Ожидаем завершения редактирования данных

                    // Параметры для генерации отчета
                    String datasource = "C:\\Users\\bobbo\\OneDrive\\Рабочий стол\\ООП\\HibernateTest\\updated_specializations.xml";
                    String xpath = "/specializations/specialization"; // Правильный XPath
                    String template = "C:\\Users\\bobbo\\OneDrive\\Рабочий стол\\ООП\\HibernateTest\\Test.jrxml";
                    String resultPath = "C:\\Users\\bobbo\\OneDrive\\Рабочий стол\\ООП\\HibernateTest\\specializations_report1.html";

                    // Генерация отчета
                    JRDataSource ds = new JRXmlDataSource(datasource, xpath);
                    System.out.println("Данные загружены из XML: " + datasource);

                    JasperReport jasperReport = JasperCompileManager.compileReport(template);
                    System.out.println("Отчет скомпилирован: " + jasperReport);

                    Map<String, Object> parameters = new HashMap<>();
                    JasperPrint print = JasperFillManager.fillReport(jasperReport, parameters, ds);
                    System.out.println("Отчет заполнен: " + print);

                    // Экспортируем отчет в HTML
                    JasperExportManager.exportReportToHtmlFile(print, resultPath);

                    System.out.println("Отчет успешно сгенерирован в HTML: " + resultPath);
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            JOptionPane.showMessageDialog(specializationList, "Отчет успешно сгенерирован в HTML: " + resultPath, "Успех", JOptionPane.INFORMATION_MESSAGE);
                        }
                    });

                } catch (InterruptedException e) {
                    System.err.println("Ошибка при ожидании завершения редактирования: " + e.getMessage());
                } catch (JRException e) {
                    System.err.println("Ошибка при генерации отчета: " + e.getMessage());
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            JOptionPane.showMessageDialog(specializationList, "Ошибка при генерации отчета: " + e.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
                        }
                    });
                } finally {
                    System.out.println("Поток " + Thread.currentThread().getName() + " завершил генерацию отчета в HTML.");
                }
            }
        }).start();
    }

    public void show0() {
        specializationList = new JFrame("Список специальностей");
        specializationList.setSize(500, 300);
        specializationList.setLocation(100, 100);
        specializationList.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        model = new DefaultTableModel(new String[]{"ID", "Специальность"}, 0);
        specializations = new JTable(model);
        JScrollPane scroll = new JScrollPane(specializations);
        specializationList.add(scroll, BorderLayout.CENTER);

        JButton loadButton = new JButton("Загрузить данные");
        loadButton.addActionListener(e -> {
            System.out.println("Пользователь нажал кнопку 'Загрузить данные'.");
            loadDataFromXML();
            // Ожидание завершения загрузки данных перед началом редактирования и генерации XML
            new Thread(new Runnable() {
                @Override
                public void run() {
                    System.out.println("Поток " + Thread.currentThread().getName() + " начинает выполнение комплексной обработки данных.");
                    try {
                        loadLatch.await(); // Ожидаем завершения загрузки данных
                        editDataAndGenerateXML(); // Запускаем редактирование и генерацию XML
                        editLatch.await(); // Ожидаем завершения редактирования
                        generateHtmlReport(); // Генерация отчета
                        System.out.println("Поток " + Thread.currentThread().getName() + " завершил выполнение комплексной обработки данных.");
                    } catch (InterruptedException ex) {
                        System.err.println("Ошибка при ожидании завершения загрузки или редактирования: " + ex.getMessage());
                    }
                }
            }).start();
        });

        specializationList.add(loadButton, BorderLayout.SOUTH);
        specializationList.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new SpecializationManager();
            }
        });
    }
}