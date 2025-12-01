package decompression;

public class Main {
    public static void main(String[] args) {

        long deb = System.currentTimeMillis();
        //Decompression.decompress("src/resources/1.huff", "src/resources/1_decompressed.txt");
        Decompression.decompress("src/resources/Blaise_Pascal.txt.huff", "src/resources/Blaise_Pascal.txt_decompressed.txt");
        //Decompression.decompress("src/utils/test/un.bin", "src/utils/test/un_decompressed.txt");
        long fin = System.currentTimeMillis();

        System.out.println("Décompression à pris => " + ((double)(fin - deb) / 1000.0) + "S");

    }
}
