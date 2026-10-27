
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class TagExtractor extends JFrame {

    private JButton chooseTextButton;
    private JButton chooseStopWordsButton;
    private JButton extractButton;
    private JButton saveButton;

    private JLabel fileLabel;
    private JLabel stopWordsLabel;
    private JTextArea outputArea;

    private File textFile;
    private File stopWordsFile;

    private final Map<String, Integer> wordFrequencies = new TreeMap<>();
    private final Set<String> stopWords = new TreeSet<>();

    public TagExtractor() {
        setTitle("Tag / Keyword Extractor");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        chooseTextButton = new JButton("Choose Text File");
        chooseStopWordsButton = new JButton("Choose Stop Words");
        extractButton = new JButton("Extract Tags");
        saveButton = new JButton("Save Results");

        fileLabel = new JLabel("Text file: None selected");
        stopWordsLabel = new JLabel("Stop-words file: None selected");

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(chooseTextButton);
        buttonPanel.add(chooseStopWordsButton);
        buttonPanel.add(extractButton);
        buttonPanel.add(saveButton);

        JPanel informationPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        informationPanel.add(fileLabel);
        informationPanel.add(stopWordsLabel);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(buttonPanel, BorderLayout.NORTH);
        topPanel.add(informationPanel, BorderLayout.SOUTH);

        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));

        JScrollPane scrollPane = new JScrollPane(outputArea);

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        chooseTextButton.addActionListener(e -> chooseTextFile());
        chooseStopWordsButton.addActionListener(e -> chooseStopWordsFile());
        extractButton.addActionListener(e -> extractTags());
        saveButton.addActionListener(e -> saveResults());
    }

    private void chooseTextFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Choose a text document");

        int result = chooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            textFile = chooser.getSelectedFile();
            fileLabel.setText("Text file: " + textFile.getName());
            outputArea.setText("");
        }
    }

    private void chooseStopWordsFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Choose the stop-words file");

        int result = chooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            stopWordsFile = chooser.getSelectedFile();
            stopWordsLabel.setText(
                    "Stop-words file: " + stopWordsFile.getName());
            loadStopWords();
        }
    }

    private void loadStopWords() {
        stopWords.clear();

        try (Scanner scanner = new Scanner(
                stopWordsFile, StandardCharsets.UTF_8)) {

            while (scanner.hasNextLine()) {
                String word = scanner.nextLine()
                        .trim()
                        .toLowerCase();

                word = word.replaceAll("[^a-z]", "");

                if (!word.isEmpty()) {
                    stopWords.add(word);
                }
            }

            JOptionPane.showMessageDialog(
                    this, stopWords.size() + " stop words loaded.");

        } catch (IOException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Could not read stop-words file:\n" + ex.getMessage(),
                    "File Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void extractTags() {
        if (textFile == null) {
            JOptionPane.showMessageDialog(this,
                    "Please choose a text file first.");
            return;
        }

        if (stopWordsFile == null) {
            JOptionPane.showMessageDialog(this,
                    "Please choose a stop-words file first.");
            return;
        }

        wordFrequencies.clear();

        try (Scanner scanner = new Scanner(
                textFile, StandardCharsets.UTF_8)) {

            while (scanner.hasNext()) {
                String word = scanner.next()
                        .toLowerCase()
                        .replaceAll("[^a-z]", "");

                if (!word.isEmpty() && !stopWords.contains(word)) {
                    wordFrequencies.put(
                            word,
                            wordFrequencies.getOrDefault(word, 0) + 1);
                }
            }

            displayResults();

        } catch (IOException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Could not read text file:\n" + ex.getMessage(),
                    "File Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void displayResults() {
        StringBuilder output = new StringBuilder();

        output.append("Source file: ")
                .append(textFile.getName())
                .append("\n\n");

        output.append(String.format("%-30s %s%n", "TAG / KEYWORD", "FREQUENCY"));
        output.append("------------------------------------------\n");

        for (Map.Entry<String, Integer> entry
                : wordFrequencies.entrySet()) {

            output.append(String.format(
                    "%-30s %d%n",
                    entry.getKey(),
                    entry.getValue()));
        }

        outputArea.setText(output.toString());
        outputArea.setCaretPosition(0);
    }

    private void saveResults() {
        if (wordFrequencies.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Extract tags before saving the results.");
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save extracted tags");
        chooser.setSelectedFile(new File("tag-output.txt"));

        int result = chooser.showSaveDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            File outputFile = chooser.getSelectedFile();

            try (PrintWriter writer = new PrintWriter(
                    outputFile, StandardCharsets.UTF_8)) {

                writer.println("Source file: " + textFile.getName());
                writer.println();
                writer.printf("%-30s %s%n", "TAG / KEYWORD", "FREQUENCY");
                writer.println("------------------------------------------");

                for (Map.Entry<String, Integer> entry
                        : wordFrequencies.entrySet()) {

                    writer.printf("%-30s %d%n",
                            entry.getKey(),
                            entry.getValue());
                }

                JOptionPane.showMessageDialog(this,
                        "Results saved to:\n" + outputFile.getAbsolutePath());

            } catch (IOException ex) {
                JOptionPane.showMessageDialog(
                        this,
                        "Could not save results:\n" + ex.getMessage(),
                        "File Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            TagExtractor app = new TagExtractor();
            app.setVisible(true);
        });
    }
}
