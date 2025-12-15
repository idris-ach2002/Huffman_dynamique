package compression;

import utils.Fichier;

public class Main {
    public static void main(String[] args) throws Exception {

//        if (args.length != 2) {
//            System.err.println("Usage: java compression.Main <input.txt> <output.huff>");
//             System.exit(1);
//        }
//        String input = args[0];
//        String output = args[1];

        String input = "data/data_code/structured_1000000_5.py";
        String output= "data/data_code/structured_1000000_5.huff";

        long start = System.currentTimeMillis();
        Compression.compresser(input, output);
        long end = System.currentTimeMillis();

        Fichier.writeInfos(input, output, end-start, "compression.txt");
    }
}
