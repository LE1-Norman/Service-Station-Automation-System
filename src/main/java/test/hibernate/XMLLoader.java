package test.hibernate;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class XMLLoader {

    public List<Object[]> importFromXML(String filePath, String nestedElementName) {
        List<Object[]> data = new ArrayList<>();
        try {
            File xmlFile = new File(filePath);
            if (!xmlFile.exists()) {
                throw new IOException("Файл не найден: " + filePath);
            }

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(xmlFile);
            document.getDocumentElement().normalize();

            NodeList nodeList = document.getElementsByTagName(nestedElementName);
            if (nodeList.getLength() == 0) {
                throw new IOException("Не найден элемент: " + nestedElementName);
            }

            NodeList rows = nodeList.item(0).getChildNodes();
            for (int i = 0; i < rows.getLength(); i++) {
                if (rows.item(i) instanceof Element) {
                    Element rowElement = (Element) rows.item(i);
                    List<Object> rowData = new ArrayList<>();
                    NodeList columnNodes = rowElement.getChildNodes();
                    for (int j = 0; j < columnNodes.getLength(); j++) {
                        if (columnNodes.item(j) instanceof Element) {
                            Element columnElement = (Element) columnNodes.item(j);
                            rowData.add(columnElement.getTextContent());
                        }
                    }
                    data.add(rowData.toArray());
                }
            }

            System.out.println("Данные успешно загружены из " + filePath);
        } catch (Exception e) {
            System.err.println("Ошибка при чтении файла: " + e.getMessage());
        }
        return data;
    }
}
