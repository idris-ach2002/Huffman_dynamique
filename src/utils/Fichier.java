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
    public static void ecriture(String src, String dst) {
        try (
            // Lecture optimisée : lecture UTF-8 par blocs (8K)
            BufferedReader br = new BufferedReader(new FileReader(src), 8192);

            // Écriture optimisée : sortie bufferisée (8K)
            BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(dst), 8192)
        ) {

            int codeChar;
            int cpt = 0; // compte le nombre de bits lus
            StringBuilder octetString = new StringBuilder(8); // plus efficace que concat()
            while ((codeChar = br.read()) != -1) {
                char c = (char) codeChar;
                cpt++;
                octetString.append(c); // même logique que concat()

                if (cpt == 8) {
                    cpt = 0;
                    int octet = Integer.parseInt(octetString.toString(), 2); // conversion base 2 -> décimal
                    output.write(octet);
                    octetString.setLength(0); // vide le StringBuilder
                }
            }

           /* D'après les information du prof on gère plus ce cas 
            int padding = 0;

            // Gestion du bourrage (même logique)
            if (cpt != 0) {
                padding = 8 - cpt;
                for (int i = 0; i < padding; i++) {
                    octetString.append("0");
                }
                int octet = Integer.parseInt(octetString.toString(), 2);
                output.write(octet);
            }

            // Écrire le padding à la fin (identique à ta version)
            output.write(padding);
            */

            // Flush du buffer avant fermeture
            output.flush();

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
