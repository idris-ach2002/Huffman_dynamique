package compression;

import utils.Fichier;

public class Main {
    public static void main(String[] args) throws Exception {

//        if (args.length != 2) {
//            System.err.println("Usage: java compression.Main <input.txt> <output.huff>");
//            System.exit(1);
//        }
//        String input = args[0];
//        String output = args[1];

        String input = "./data/rand_25000000_1.txt";
        String output= "./data/rand_1000_4.bin";

        long start = System.currentTimeMillis();
        Compression.compresser(input, output);   // ta méthode existante
        long end = System.currentTimeMillis();

        Fichier.writeInfos(input, output, end-start, "compression.txt");
    }
}
