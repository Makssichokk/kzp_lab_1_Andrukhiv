
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Консольна програма для роботи з каталогом книжок.
 * Зчитує дані, перевіряє записи, обчислює показники
 * та формує підсумковий звіт.
 */
public class Main {

    private static final String DEFAULT_INPUT = "data/input.csv";
    private static final String DEFAULT_OUTPUT = "data/report.txt";

    /**
     * Головний метод програми.
     *
     * @param args аргументи командного рядка
     */
    public static void main(String[] args) {

        Path input = Paths.get(DEFAULT_INPUT);
        Path output = Paths.get(DEFAULT_OUTPUT);

        try {
            for (int i = 0; i < args.length; i++) {

                switch (args[i]) {

                    case "--help":
                        printHelp();
                        return;

                    case "--input":
                        if (i + 1 >= args.length) {
                            throw new IllegalArgumentException(
                                    "Не вказано шлях після --input");
                        }
                        input = Paths.get(args[++i]);
                        break;

                    case "--output":
                        if (i + 1 >= args.length) {
                            throw new IllegalArgumentException(
                                    "Не вказано шлях після --output");
                        }
                        output = Paths.get(args[++i]);
                        break;

                    default:
                        throw new IllegalArgumentException(
                                "Невідомий аргумент: " + args[i]);
                }
            }

            runTests();
            System.out.println();

            runProgram(input, output);

        } catch (IllegalArgumentException e) {
            System.out.println("Помилка: " + e.getMessage());
            printHelp();
        }
    }

    /**
     * Виводить довідку про команди програми.
     */
    private static void printHelp() {

        System.out.println("КАТАЛОГ КНИЖОК");
        System.out.println();
        System.out.println("Використання: java Main [параметри]");
        System.out.println("--help              Показати довідку");
        System.out.println("--input <шлях>      Вказати вхідний файл");
        System.out.println("--output <шлях>     Вказати вихідний файл");
    }

    /**
     * Зчитує вхідний файл або використовує тестові дані,
     * якщо файл відсутній.
     *
     * @param input шлях до вхідного файлу
     * @param output шлях до вихідного файлу
     */
    private static void runProgram(Path input, Path output) {

        List<String[]> books = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        if (Files.exists(input)) {

            try (BufferedReader reader = Files.newBufferedReader(
                    input, StandardCharsets.UTF_8)) {

                String line;
                int lineNumber = 0;

                while ((line = reader.readLine()) != null) {
                    lineNumber++;
                    processLine(line, lineNumber, books, errors);
                }

            } catch (IOException e) {
                System.out.println("Помилка читання файлу: "
                        + e.getMessage());
                return;
            }

        } else {

            System.out.println(
                    "Файл не знайдено. Використовуються демонстраційні дані.");

            String[] testData = {
                    "Кобзар;Тарас Шевченко;320;250.50",
                    "Лісова пісня;Леся Українка;180;180.00",
                    "Тигролови;Іван Багряний;280;220.75",
                    "Місто;Вал'ян Підмогильний;360;300.00",
                    "Захар Беркут;Іван Франко;240;195.50"
            };

            for (int i = 0; i < testData.length; i++) {
                processLine(testData[i], i + 1, books, errors);
            }
        }

        String report = createReport(books, errors);

        System.out.println();
        System.out.print(report);

        try {
            Path parent = output.getParent();

            if (parent != null) {
                Files.createDirectories(parent);
            }

            Files.writeString(
                    output,
                    report,
                    StandardCharsets.UTF_8
            );

            System.out.println();
            System.out.println("Звіт збережено у файл: " + output);

        } catch (IOException e) {
            System.out.println(
                    "Не вдалося зберегти звіт: " + e.getMessage());
        }
    }

    /**
     * Перевіряє один рядок вхідних даних.
     *
     * @param line текст рядка
     * @param lineNumber номер рядка
     * @param books список коректних книжок
     * @param errors список помилок
     */
    private static void processLine(
            String line,
            int lineNumber,
            List<String[]> books,
            List<String> errors) {

        if (line.trim().isEmpty()) {
            errors.add("Рядок " + lineNumber
                    + ": порожній рядок.");
            return;
        }

        String[] fields = line.split(";", -1);

        if (fields.length != 4) {
            errors.add("Рядок " + lineNumber
                    + ": неправильна кількість полів.");
            return;
        }

        String title = fields[0].trim();
        String author = fields[1].trim();

        if (title.isEmpty()) {
            errors.add("Рядок " + lineNumber
                    + ": назва книжки відсутня.");
            return;
        }

        if (author.isEmpty()) {
            errors.add("Рядок " + lineNumber
                    + ": автор книжки відсутній.");
            return;
        }

        try {
            int pages = Integer.parseInt(fields[2].trim());

            double price = Double.parseDouble(
                    fields[3].trim().replace(',', '.'));

            if (pages <= 0) {
                errors.add("Рядок " + lineNumber
                        + ": кількість сторінок має бути більшою за 0.");
                return;
            }

            if (!Double.isFinite(price) || price < 0) {
                errors.add("Рядок " + lineNumber
                        + ": ціна має бути невід'ємним числом.");
                return;
            }

            books.add(new String[]{
                    title,
                    author,
                    String.valueOf(pages),
                    String.valueOf(price)
            });

        } catch (NumberFormatException e) {
            errors.add("Рядок " + lineNumber
                    + ": неправильний формат числових даних.");
        }
    }

    /**
     * Формує звіт та обчислює показники каталогу.
     *
     * @param books список коректних книжок
     * @param errors список повідомлень про помилки
     * @return готовий текст звіту
     */
    private static String createReport(
            List<String[]> books,
            List<String> errors) {

        StringBuilder report = new StringBuilder();

        long totalPages = 0;
        double totalPrice = 0;
        String[] mostExpensive = null;

        report.append("========== КАТАЛОГ КНИЖОК ==========\n");

        report.append("Кількість коректних книжок: ")
                .append(books.size()).append("\n\n");

        for (String[] book : books) {

            int pages = Integer.parseInt(book[2]);
            double price = Double.parseDouble(book[3]);

            totalPages += pages;
            totalPrice += price;

            if (mostExpensive == null
                    || price > Double.parseDouble(mostExpensive[3])) {
                mostExpensive = book;
            }

            report.append(String.format(
                    Locale.ROOT,
                    "%s | %s | %d стор. | %.2f грн%n",
                    book[0], book[1], pages, price
            ));
        }

        double averagePages = books.isEmpty()
                ? 0.0
                : (double) totalPages / books.size();

        report.append("\n========== РЕЗУЛЬТАТИ ==========\n");

        report.append(String.format(
                Locale.ROOT,
                "1. Середня кількість сторінок: %.2f%n",
                averagePages
        ));

        if (mostExpensive != null) {

            report.append("2. Найдорожча книжка: ")
                    .append(mostExpensive[0]).append("\n");

            report.append("   Автор: ")
                    .append(mostExpensive[1]).append("\n");

            report.append(String.format(
                    Locale.ROOT,
                    "   Ціна: %.2f грн%n",
                    Double.parseDouble(mostExpensive[3])
            ));

        } else {
            report.append("2. Найдорожча книжка: немає даних\n");
        }

        report.append(String.format(
                Locale.ROOT,
                "3. Сумарна вартість каталогу: %.2f грн%n",
                totalPrice
        ));

        report.append("\n========== ПЕРЕВІРКА ДАНИХ ==========\n");

        if (errors.isEmpty()) {
            report.append("Помилок не виявлено.\n");
        } else {
            for (String error : errors) {
                report.append(error).append("\n");
            }
        }

        return report.toString();
    }

    /**
     * Виконує три автоматичні тести.
     */
    private static void runTests() {

        int passed = 0;
        int total = 3;

        System.out.println("========== АВТОМАТИЧНІ ТЕСТИ ==========");

        List<String[]> books = new ArrayList<>();

        books.add(new String[]{
                "Кобзар", "Шевченко", "320", "250.50"
        });

        books.add(new String[]{
                "Лісова пісня", "Українка", "180", "180.00"
        });

        String report = createReport(books, new ArrayList<>());

        // Тест 1: середня кількість сторінок
        if (report.contains("250.00")) {
            System.out.println(
                    "Тест 1: PASS - середня кількість сторінок.");
            passed++;
        } else {
            System.out.println(
                    "Тест 1: FAIL - неправильне середнє значення.");
        }

        // Тест 2: сумарна вартість
        if (report.contains("430.50")) {
            System.out.println(
                    "Тест 2: PASS - сумарна вартість.");
            passed++;
        } else {
            System.out.println(
                    "Тест 2: FAIL - неправильна сумарна вартість.");
        }

        // Тест 3: перевірка некоректного запису
        List<String[]> testBooks = new ArrayList<>();
        List<String> testErrors = new ArrayList<>();

        processLine(
                "Неправильний запис",
                1,
                testBooks,
                testErrors
        );

        if (testBooks.isEmpty() && !testErrors.isEmpty()) {
            System.out.println(
                    "Тест 3: PASS - перевірка некоректного запису.");
            passed++;
        } else {
            System.out.println(
                    "Тест 3: FAIL - помилка валідації.");
        }

        System.out.println("---------------------------------------");
        System.out.println("Успішно пройдено: " + passed + "/" + total);
        System.out.println("========== ТЕСТУВАННЯ ЗАВЕРШЕНО ==========");
    }
}
