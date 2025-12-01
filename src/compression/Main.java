package compression;

public class Main {

    public static void main(String[] args) {
        //Compression comp = new Compression("src/resources/Blaise_Pascal.txt", "src/resources/Blaise_Pascal.huff");
        //Compression comp = new Compression("src/resources/Carambar.txt", "src/resources/CarambarCompresse.bin");
        long deb = System.currentTimeMillis();
        Compression.compresser("src/resources/1.txt", "src/resources/1.huff");
        long fin = System.currentTimeMillis();

        System.out.println("Compression à pris => " + ((double)(fin - deb) / 1000.0) + "S");

    }

}