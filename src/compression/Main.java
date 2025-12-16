package compression;

import utils.Fichier;

public class Main {
    public static void main(String[] args) throws Exception {

        if (args.length != 2) {
            System.err.println("Usage: java compression.Main <input.txt> <output.huff>");
             System.exit(1);
        }
        String input = args[0];
        String output = args[1];

        long start = System.currentTimeMillis();
        Compression.compresser(input, output);
        long end = System.currentTimeMillis();

        Fichier.writeInfos(input, output, end-start, "compression.txt");
    }
}
