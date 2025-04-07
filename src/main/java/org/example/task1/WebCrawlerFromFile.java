package org.example.task1;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class WebCrawlerFromFile {

    public static void main(String[] args) {
        String inputFile = "task1/urls.txt";       // Файл со списком URL (по одному на строку)
        String outputDir = "task1";        // Папка для сохранения страниц
        String indexFile = outputDir + "/index.txt";  // Файл с индексацией

        // Создаем папку, если её нет
        new File(outputDir).mkdirs();

        try {
            // Читаем все URL из файла
            InputStream input = WebCrawlerFromFile.class.getResourceAsStream("/" + inputFile);
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            List<String> urls = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                urls.add(line);
            }
            // Открываем index.txt для записи
            try (PrintWriter indexWriter = new PrintWriter(new FileWriter(indexFile))) {
                for (int i = 0; i < urls.size(); i++) {
                    String url = urls.get(i).trim(); // Убираем лишние пробелы
                    if (url.isEmpty()) continue;      // Пропускаем пустые строки

                    String fileName = outputDir + "/page_" + (i + 1) + ".txt";

                    try {
                        String pageContent = downloadPage(url);
                        saveToFile(fileName, pageContent);
                        indexWriter.println((i + 1) + " " + url); // Запись в индекс
                        System.out.println("Успешно: " + url);
                    } catch (IOException e) {
                        System.err.println("Ошибка при загрузке " + url + ": " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
//            System.err.println("Ошибка при чтении файла " + inputFile + ": " + e.getMessage());
        }
    }

    // Скачивание HTML-страницы по URL
    private static String downloadPage(String url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();

        // Устанавливаем заголовки (примеры)
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/18.3 Safari/605.1.15");
        connection.setRequestProperty("Accept", "*/*");

        // Настраиваем таймауты (чтобы запрос не зависал)
        connection.setConnectTimeout(5000); // 5 секунд
        connection.setReadTimeout(10000);

        // Проверяем код ответа (200 = OK)
        int statusCode = connection.getResponseCode();
        if (statusCode != HttpURLConnection.HTTP_OK) {
            throw new IOException("HTTP Error: " + statusCode);
        }

        // Читаем содержимое
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            return content.toString();
        } finally {
            connection.disconnect(); // Закрываем соединение
        }
    }

    // Сохранение содержимого в файл
    private static void saveToFile(String fileName, String content) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(fileName))) {
            writer.print(content);
        }
    }
}