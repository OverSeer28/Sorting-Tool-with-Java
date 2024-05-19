package sorting;

import java.util.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.io.*;


public class Main {
    public static void main(String[] args) {
        String sortingType = "natural"; // Default sorting type
        String dataType = "word"; // Default data type
        String inputFile = null;
        String outputFile = null;
        boolean validSortingType = false;
        boolean validDataType = false;

        // Parse command-line arguments
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-sortingType":
                    if (i + 1 < args.length && !args[i + 1].startsWith("-")) {
                        sortingType = args[i + 1];
                        validSortingType = true;
                        i++;
                    } else {
                        System.out.println("No sorting type defined!");
                        return;
                    }
                    break;
                case "-dataType":
                    if (i + 1 < args.length && !args[i + 1].startsWith("-")) {
                        dataType = args[i + 1];
                        validDataType = true;
                        i++;
                    } else {
                        System.out.println("No data type defined!");
                        return;
                    }
                    break;
                case "-inputFile":
                    if (i + 1 < args.length && !args[i + 1].startsWith("-")) {
                        inputFile = args[i + 1];
                        i++;
                    }
                    break;
                case "-outputFile":
                    if (i + 1 < args.length && !args[i + 1].startsWith("-")) {
                        outputFile = args[i + 1];
                        i++;
                    }
                    break;
                default:
                    if (args[i].startsWith("-")) {
                        System.out.printf("\"%s\" is not a valid parameter. It will be skipped.%n", args[i]);
                    }
                    break;
            }
        }

        if (!validSortingType && Arrays.asList(args).contains("-sortingType")) {
            System.out.println("No sorting type defined!");
            return;
        }

        if (!validDataType && Arrays.asList(args).contains("-dataType")) {
            System.out.println("No data type defined!");
            return;
        }

        List<String> input = new ArrayList<>();

        // Read input from file or standard input
        if (inputFile != null) {
            try (Scanner fileScanner = new Scanner(new File(inputFile))) {
                while (fileScanner.hasNextLine()) {
                    input.add(fileScanner.nextLine());
                }
            } catch (FileNotFoundException e) {
                System.out.println("File not found: " + inputFile);
                return;
            }
        } else {
            Scanner scanner = new Scanner(System.in);
            while (scanner.hasNextLine()) {
                input.add(scanner.nextLine());
            }
        }

        // Prepare to write output
        PrintStream output = System.out;
        if (outputFile != null) {
            try {
                output = new PrintStream(new File(outputFile));
            } catch (FileNotFoundException e) {
                System.out.println("Cannot write to file: " + outputFile);
                return;
            }
        }

        switch (dataType) {
            case "long":
                List<Long> numbers = new ArrayList<>();
                for (String line : input) {
                    for (String part : line.trim().split("\\s+")) {
                        try {
                            numbers.add(Long.parseLong(part));
                        } catch (NumberFormatException e) {
                            System.out.printf("\"%s\" is not a long. It will be skipped.%n", part);
                        }
                    }
                }
                if ("byCount".equals(sortingType)) {
                    sortByCount(numbers, output);
                } else {
                    sortNaturally(numbers, "numbers", output);
                }
                break;
            case "line":
                if ("byCount".equals(sortingType)) {
                    sortByCount(input, output);
                } else {
                    sortNaturally(input, "lines", output);
                }
                break;
            case "word":
                List<String> words = input.stream()
                        .flatMap(line -> Arrays.stream(line.trim().split("\\s+")))
                        .collect(Collectors.toList());
                if ("byCount".equals(sortingType)) {
                    sortByCount(words, output);
                } else {
                    sortNaturally(words, "words", output);
                }
                break;
            default:
                System.out.println("Unknown data type: " + dataType);
                break;
        }

        if (outputFile != null) {
            output.close();
        }
    }

    private static <T extends Comparable<T>> void sortNaturally(List<T> data, String elementType, PrintStream output) {
        Collections.sort(data);
        output.printf("Total %s: %d.%n", elementType, data.size());
        if ("lines".equals(elementType)) {
            output.println("Sorted data:");
            data.forEach(output::println);
        } else {
            output.print("Sorted data: ");
            output.println(data.stream().map(Object::toString).collect(Collectors.joining(" ")));
        }
    }

    private static <T extends Comparable<T>> void sortByCount(List<T> data, PrintStream output) {
        Map<T, Long> frequencyMap = data.stream().collect(Collectors.groupingBy(e -> e, Collectors.counting()));
        List<Map.Entry<T, Long>> sortedByCount = frequencyMap.entrySet().stream()
                .sorted(Comparator.<Map.Entry<T, Long>, Long>comparing(Map.Entry::getValue)
                        .thenComparing(Map.Entry::getKey))
                .collect(Collectors.toList());

        long totalCount = data.size();
        String elementType = getElementType(data);
        output.printf("Total %s: %d.%n", elementType, totalCount);
        for (Map.Entry<T, Long> entry : sortedByCount) {
            long count = entry.getValue();
            double percentage = (count * 100.0) / totalCount;
            output.printf("%s: %d time(s), %.0f%%%n", entry.getKey().toString(), count, percentage);
        }
    }

    private static <T> String getElementType(List<T> data) {
        if (data.isEmpty()) return "elements";
        Object firstElement = data.get(0);
        if (firstElement instanceof Long) {
            return "numbers";
        } else if (firstElement instanceof String) {
            String sample = (String) firstElement;
            if (sample.contains("\n") || sample.contains("\r")) {
                return "lines";
            } else {
                return "words";
            }
        }
        return "elements";
    }

}
