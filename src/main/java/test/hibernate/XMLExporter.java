package test.hibernate;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import java.io.File;
import java.util.List;

/**
 * Класс для экспорта данных в формат XML.
 */
public class XMLExporter {

    /**
     * Экранирует специальные символы в строке для корректного формирования XML.
     *
     * @param value строка, которую необходимо экранировать
     * @return экранированная строка
     */
    private String escapeXml(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }

    /**
     * Очищает имя элемента, заменяя недопустимые символы на подчеркивания.
     * Если имя пустое, возвращает "default".
     *
     * @param name имя элемента
     * @return очищенное имя элемента
     */
    private String cleanElementName(String name) {
        if (name == null || name.isEmpty()) {
            return "default"; // Возвращаем значение по умолчанию для пустых имен
        }
        StringBuilder cleanedName = new StringBuilder();
        for (char c : name.toCharArray()) {
            if (Character.isLetter(c) || Character.isDigit(c) || c == '_' || c == '-' || c == '.') {
                cleanedName.append(c);
            } else {
                cleanedName.append('_'); // Заменяем недопустимые символы на подчеркивание
            }
        }
        // Если первое символ не буква, добавляем "a" в начало
        if (!Character.isLetter(cleanedName.charAt(0))) {
            cleanedName.insert(0, 'a');
        }
        return cleanedName.toString();
    }

    /**
     * Экспортирует данные в XML файл.
     *
     * @param data список массивов объектов, представляющих строки данных
     * @param filePath путь к файлу, в который будет сохранен XML
     * @param rootElement имя корневого элемента XML
     * @param columnNames имена колонок, которые будут использоваться в качестве имен элементов
     * @param nestedElementName имя вложенного элемента, в который будут помещены строки данных
     */
    public void exportToXML(List<Object[]> data, String filePath, String rootElement, String[] columnNames, String nestedElementName) {
        try {
            // Создание фабрики и билдера документа
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document;
            File xmlFile = new File(filePath);

            // Удаляем файл, если он уже существует
            if (xmlFile.exists()) {
                xmlFile.delete();
            }

            // Создание нового документа
            document = builder.newDocument();
            Element root = document.createElement(rootElement); // Создание корневого элемента
            document.appendChild(root); // Добавление корневого элемента в документ

            Element nestedElement = document.createElement(nestedElementName); // Создание вложенного элемента
            root.appendChild(nestedElement); // Добавление вложенного элемента в корень

            // Обработка данных и создание элементов для каждой строки
            for (Object[] row : data) {
                Element rowElement = document.createElement("row"); // Создание элемента строки
                for (int i = 0; i < columnNames.length; i++) {
                    String cleanedColumnName = cleanElementName(columnNames[i]); // Очистка имени колонки
                    Element columnElement = document.createElement(cleanedColumnName); // Создание элемента колонки
                    // Добавление текста в элемент колонки, экранируя специальные символы
                    columnElement.appendChild(document.createTextNode(escapeXml(row[i] != null ? row[i].toString() : "")));
                    rowElement.appendChild(columnElement); // Добавление элемента колонки в строку
                }
                nestedElement.appendChild(rowElement); // Добавление строки в вложенный элемент
            }

            // Настройка трансформатора для записи XML в файл
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes"); // Включение отступов для удобства чтения
            DOMSource domSource = new DOMSource(document); // Создание источника DOM
            StreamResult streamResult = new StreamResult(xmlFile); // Создание результата записи в файл
            transformer.transform(domSource, streamResult); // Преобразование и запись в файл

            System.out.println("Данные успешно экспортированы в " + filePath);
        } catch (TransformerConfigurationException e) {
            System.err.println("Ошибка конфигурации трансформатора: " + e.getMessage());
        } catch (TransformerException e) {
            System.err.println("Ошибка трансформации: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Произошла неожиданная ошибка: " + e.getMessage());
        }
    }
}