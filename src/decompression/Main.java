package decompression;

import utils.Fichier;

public class Main {
    public static void main(String[] args) {
//        if (args.length != 2) {
//            System.err.println("Usage: java compression.Main <input.txt> <output.huff>");
//            System.exit(1);
//        }
        //String input = args[0];
        String input = "data/final_fail1.bin";
        //String output = args[1];
        String output ="data/test_decomp.txt";

        long start = System.currentTimeMillis();
        //Decompression.decompress("src/resources/1.huff", "src/resources/1_decompressed.txt");
        Decompression.decompresser(input, output);
        //Decompression.decompress("src/utils/test/un.bin", "src/utils/test/un_decompressed.txt");
        long end = System.currentTimeMillis();

        Fichier.writeInfos(input, output, end-start, "decompression.txt");


    }
}
