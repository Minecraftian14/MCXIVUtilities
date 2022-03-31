package in.mcxiv.math.gen;

import java.io.*;

public class MainGenerator {

    private static final File input = new File("src\\mathGen\\java\\in\\mcxiv\\math\\gen\\MathBox.java").getAbsoluteFile();
    private static final File ouput = new File("src\\math\\java\\in\\mcxiv\\math\\MathBox.java").getAbsoluteFile();

    public static void main(String[] args) throws IOException {
        TypeReplacer replacer = new TypeReplacer();
        try (
                FileReader __reader__ = new FileReader(input);
                BufferedReader reader = new BufferedReader(__reader__);
                FileWriter __writer__ = new FileWriter(ouput);
                BufferedWriter writer = new BufferedWriter(__writer__);
        ) {
            String line;
            while ((line = reader.readLine()) != null) {
                replacer.evaluateLine(line);
                String newLine = replacer.getCode();
                if (newLine != null)
                    writer.write(newLine + System.lineSeparator());
            }
        }
    }
}
