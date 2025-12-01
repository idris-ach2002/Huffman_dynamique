package utils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Locale;


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
                // pour afficher même les 0 de fort qui sont inutiles pour former le nombre. '00001010'
                String bin = String.format("%8s", Integer.toBinaryString(octet)).replace(' ', '0');
                System.out.println(bin);
            }

        } catch (IOException e) {
            System.out.println("Error reading file. " + e.getMessage());
        }
    }

    /**
     * Ecrit la suite de bit qui est dans @src dans un fichier binaire @dst
     * On utilise BufferedReader pour de meilleur performances (grace aux buffers)
     * Source : https://www.w3schools.com/java/java_bufferedreader.asp
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
            // Flush du buffer avant fermeture
            output.flush();

        } catch (IOException e) {
            System.out.println("Error handling file. " + e.getMessage());
        }
    }

    /**

     */

    /**
     * Une fonction pour écrire les informations sur les fichiers d'entrées et de soties dans
     * compression.txt et dans decompression.txt
     * @param in fichier soit à compressé soit à décompressé
     * @param out fichier compressé ou bien décompressé
     * @param time temps de compression/ decompression
     * @param dst compression.txt ou bien decompression.txt
     */
    public static void writeInfos(String in, String out, long time, String dst){
        try {
            long inputSize = new File(in).length();
            long outputSize = new File(out).length();

            double taux = (inputSize == 0) ? 0.0 : (double) outputSize / (double) inputSize;
            taux = Math.round(taux * 1e5) / 1e5;

            String line = in + ";" +
                    out + ";" +
                    inputSize + ";" +
                    outputSize + ";" +
                    taux + ";" +
                    time;

            Files.write(
                    Paths.get(dst),
                    (line + System.lineSeparator()).getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            );

        } catch (Exception e) {
            System.err.println("Erreur lors de l'écriture dans " + dst);
            e.printStackTrace();
        }
    }


}
