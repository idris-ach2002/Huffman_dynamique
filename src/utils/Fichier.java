package utils;

import java.io.*;


public class Fichier {

    /**
     * FileInputStream est d'après recherche la meilleur option pour lire les fichiers binaires,
     * elle permet de lire octet par octet exactement comme demandé
     * Source : https://www.w3schools.com/java/java_fileinputstream.asp
     * @param chemin chemin du fichier à ouvrir
     */
    public static void lecture (String chemin){
        // try-with-resources: FileInputStream sera fermé automatiquement
        try (FileInputStream input = new FileInputStream(chemin)) {
            int octet;
            while ((octet = input.read()) != -1) {
                String bin = Integer.toBinaryString(octet);
                System.out.println(bin);
            }

        } catch (IOException e) {
            System.out.println("Error reading file. " + e.getMessage());
        }
    }

    /**
     * On utilise BufferedReader pour de meilleur performances (grace aux buffers)
     * Source : https://www.w3schools.com/java/java_bufferedreader.asp
     * Ce n'est pas la meuilleur manière de lire un octet et de l'écrire directement niveau nombre d'netré sortie faites
     * ! A optimiser après révision de manipulation de fichier
     * @param src Fichier textuelle représentant une chaine de 0 et de 1
     * @param dst Fichier binaire représentant la quite binaire du fichier src
     */
    public static void ecriture (String src, String dst){
        try (BufferedReader br = new BufferedReader(new FileReader(src));
             FileOutputStream output = new FileOutputStream(dst)) {

            int codeChar;
            int cpt = 0; // compte le nombre de char lu
            String octetString = "";
            while ((codeChar = br.read()) != -1) {
                char c = (char) codeChar;
                cpt++;
                octetString = octetString.concat("" + c);

                if (cpt == 8){
                    cpt = 0;
                    // retourne la valeur que représnete la chaine de bit
                    int octet = Integer.parseInt(octetString, 2); // 2 == base
                    output.write(octet);
                    octetString = "";
                }
            }
            // pas multiple de 8
            if (cpt != 0){
                for (int i=0; i<8-cpt; i++){
                    octetString = octetString.concat("0");
                }
                int octet = Integer.parseInt(octetString, 2); // 2 == base
                output.write(octet);
            }

        } catch (IOException e) {
            System.out.println("Error handling file. " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        //
        Fichier.ecriture("./src/utils/test/binary_code.txt", "./src/utils/test/output.bin");
        // On lit le fichier généré
        Fichier.lecture("./src/utils/test/output.bin");
    }
}
