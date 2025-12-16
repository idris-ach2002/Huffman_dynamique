package Experimentation;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import compression.Compression;
import decompression.Decompression;


/**
 * ExperimentLauncher
 * ------------------
 *
 * Cette classe exécute automatiquement une campagne d’expérimentations
 * sur l’algorithme de compression/décompression dynamique de Huffman
 * implémenté dans la classe.
 *
 * Elle permet :
 *   - de générer des fichiers aléatoires de tailles variées (uniforme / Zipf), Plus des fichiers de code structurés
 *   - de mesurer le temps de compression et de décompression,
 *   - de vérifier la validité de la décompression (égalité bit-à-bit),
 *   - d’écrire les résultats dans deux fichiers CSV :
 *         - results_raw.csv : résultats pour chaque fichier
 *         - results_avg.csv : moyennes des temps par taille
 *
 * Le but est de produire les données nécessaires à l’étude expérimentale
 * (Question 11 du projet) : tracer les courbes
 *      temps_moyen_compression(taille) et temps_moyen_decompression(taille).
 *
 * Tout est fait en Java pur : aucune commande externe n’est invoquée.
 * Cela garantit que les mesures reflètent précisément le comportement
 * réel de l'algorithme plutôt qu’un overhead lié à ProcessBuilder ou au système.
 */
public class ExperimentLauncher {

    /** Tailles des fichiers à générer et tester */
    private static final int[] SIZES = new int[]{
            1_000_000, 2_000_000, 3_000_000, 4_000_000, 5_000_000,6_000_000, 7_000_000 , 8_000_000, 9_000_000, 10_000_000};
            //10M, 20M, 30M, 40M --> 100M 

    /** Nombre de fichiers générés par taille */
    private static final int FILES_PER_SIZE = 5;

    /** Répertoire contenant les données générées */
    private static String DATA_DIR = "data";

    /** Répertoire où les résultats seront enregistrés */
    private static String OUT_DIR = "out";


    public static void experimentCodeFiles() throws Exception {
        DATA_DIR = "data" + "/" + "data" + "_code";
        OUT_DIR = "out" + "/" + "out" + "_code";

        Files.createDirectories(Paths.get(DATA_DIR));
        Files.createDirectories(Paths.get(OUT_DIR));

        Path rawCsv = Paths.get(OUT_DIR, "results_raw.csv");

        try (BufferedWriter rawWriter = Files.newBufferedWriter(rawCsv)) {

            rawWriter.write("input,compressed,input_bytes,compressed_bytes,compress_ms,decompress_ms\n");

            for (int size : SIZES) {

                System.out.println("\n=== Taille " + size + " bytes ===");

                List<Long> compressTimes = new ArrayList<>();
                List<Long> decompressTimes = new ArrayList<>();

                for (int j = 0; j < FILES_PER_SIZE; j++) {

                    // Nom de base
                    String name = String.format("structured_%d_%d", size, j+1);
                    String input = Paths.get(DATA_DIR, name).toString();
                    System.out.println("Processing Current File : " + name);


                    // -----------------------------------------------------------------
                    //   NOUVELLE GÉNÉRATION : JSON / PYTHON / C en alternance
                    // -----------------------------------------------------------------
                    String format = null;
                    switch (j % 3) {
                        case 0:
                            format = ".json";
                            input += format;
                            CodeGenerator.generateJsonToFile(size / 60, input);
                            break;

                        case 1:
                            format = ".py";
                            input += format;
                            CodeGenerator.generatePythonToFile(size / 50, input);
                            break;

                        case 2:
                            format = ".c";
                            input += format;
                            CodeGenerator.generateCToFile(size / 45, input);
                            break;
                    }

                    // Fichiers de sortie
                    String compressed = Paths.get(DATA_DIR, name+".huff").toString();

                    // --------------------------------------------------
                    //   MESURE TEMPS COMPRESSION
                    // --------------------------------------------------

                    long t0 = System.nanoTime();
                    Compression.compresser(input, compressed);
                    long t1 = System.nanoTime();

                    long compressMs = (t1 - t0) / 1_000_000L;

                    // --------------------------------------------------
                    //   MESURE TEMPS DÉCOMPRESSION
                    // --------------------------------------------------
                    
                    String file_decomp = Paths.get(DATA_DIR, name+"_decompressed"+format).toString();
                    long t2 = System.nanoTime();
                    Decompression.decompresser(compressed, file_decomp);
                    long t3 = System.nanoTime();

                    long decompressMs = (t3 - t2) / 1_000_000L;


                    try {
                        testFiles(input, file_decomp);
                    }catch(Exception ie) {
                        ie.printStackTrace();
                    }
                    System.out.println("Décompression checked " + input + " = " + input + "_decompressed.txt");


                    // -----------------------------------------------------------------
                    //   STATS
                    // -----------------------------------------------------------------
                    long inBytes = Files.size(Paths.get(input));
                    long compBytes = Files.size(Paths.get(compressed));

                    rawWriter.write(String.format(
                            "%s,%s,%d,%d,%d,%d\n",
                            input, compressed, inBytes, compBytes,
                            compressMs, decompressMs
                    ));
                    rawWriter.flush();

                    compressTimes.add(compressMs);
                    decompressTimes.add(decompressMs);

                    System.out.println(" - " + input +
                            " | C=" + compressMs + "ms | D=" + decompressMs + "ms");
                }

                // ---------------------------------------------------------------------
                //   MOYENNES PAR TAILLE
                // ---------------------------------------------------------------------
                long avgComp = (long) compressTimes.stream().mapToLong(Long::longValue).average().orElse(0);
                long avgDec = (long) decompressTimes.stream().mapToLong(Long::longValue).average().orElse(0);

                Path avgCsv = Paths.get(OUT_DIR, "results_avg.csv");
                boolean exists = Files.exists(avgCsv);

                try (BufferedWriter w = Files.newBufferedWriter(
                        avgCsv, StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {

                    if (!exists)
                        w.write("size_bytes,avg_compress_ms,avg_decompress_ms,count\n");

                    w.write(String.format("%d,%d,%d,%d\n",
                            size, avgComp, avgDec, FILES_PER_SIZE));
                }
            }
        }

        System.out.println("\n=== FIN DES EXPÉRIMENTATIONS (STRUCTURÉES) ===");
    
    }


    public static void experimentTextFiles() throws Exception {
        DATA_DIR = "data" + "/" + "data" + "_txt";
        OUT_DIR = "out" + "/" + "out" + "_txt";

        Files.createDirectories(Paths.get(DATA_DIR));
        Files.createDirectories(Paths.get(OUT_DIR));

        Path rawCsv = Paths.get(OUT_DIR, "results_raw.csv");

        try (BufferedWriter rawWriter = Files.newBufferedWriter(rawCsv)) {

            // En-tête du CSV des résultats bruts
            rawWriter.write("input,compressed,input_bytes,compressed_bytes,compress_ms,decompress_ms\n");

            // ----------------------------------------------------------
            //   BOUCLE PRINCIPALE : ON TESTE CHAQUE TAILLE DE FICHIER
            // ----------------------------------------------------------
            for (int size : SIZES) {

                System.out.println("\n=== Taille " + size + " bytes ===");

                List<Long> compressTimes = new ArrayList<>();
                List<Long> decompressTimes = new ArrayList<>();

                // ------------------------------------------------------
                //   ON GÉNÈRE M FICHIERS POUR CETTE TAILLE
                // ------------------------------------------------------
                for (int j = 0; j < FILES_PER_SIZE; j++) {

                    // Nom du fichier à générer
                    String name = String.format("rand_%d_%d.txt", size, j+1);
                    String input = Paths.get(DATA_DIR, name).toString();
                    System.out.println("Deb Génération fichier " + name);


                    // Distribution alternée : Zipf / Uniforme / Ordonnée
                   // switch (j % 3) {
                        //case 0:
                                //System.out.println("Processing Current File : " + name + " Distribution Zipf");
                               // RandomFileGeneratorUtf8.generateZipfToFile(size, 1.5, input);
                                //break;
                            //case 1:
                                System.out.println("Processing Current File : " + name + " Distribution Uniforme");
                                RandomFileGeneratorUtf8.generateUniformToFile(size, input);
                                //break;
                           // case 2:
                               // System.out.println("Processing Current File : " + name + " Distribution Ordonnée croissante Fibonacci");
                               // RandomFileGeneratorUtf8.generateWorstCaseHuffmanDynamicToFile(size, input);
                               //break;
                       // default:
                            //break;
                    //}

                    System.out.println("Fin Génération fichier " + name);


                    // Fichiers de sortie
                    String compressed = Paths.get(DATA_DIR, name.replace(".txt", ".huff")).toString();

                    // --------------------------------------------------
                    //   MESURE TEMPS COMPRESSION
                    // --------------------------------------------------
                    System.out.println("Deb Compression");
                    long t0 = System.nanoTime();
                    Compression.compresser(input, compressed);
                    long t1 = System.nanoTime();
                    System.out.println("Fin Compression");


                    long compressMs = (t1 - t0) / 1_000_000L;

                    // --------------------------------------------------
                    //   MESURE TEMPS DÉCOMPRESSION
                    // --------------------------------------------------
                    System.out.println("Deb Decompression");

                    String file_decomp = Paths.get(DATA_DIR, name.replace(".txt", "_decompressed.txt")).toString();
                    long t2 = System.nanoTime();
                    Decompression.decompresser(compressed, file_decomp);
                    long t3 = System.nanoTime();
                    System.out.println("Fin Decompression");
                    long decompressMs = (t3 - t2) / 1_000_000L;

                    try {
                        testFiles(input, file_decomp);
                    }catch(Exception ie) {
                        ie.printStackTrace();
                    }
                    System.out.println("Décompression checked " + input + " = " + input + "_decompressed.txt");

                    // --------------------------------------------------
                    //   STATISTIQUES FICHIER
                    // --------------------------------------------------
                    long inBytes = Files.size(Paths.get(input));
                    long compBytes = Files.size(Paths.get(compressed));

                    // Enregistrement des résultats bruts
                    rawWriter.write(String.format(
                            "%s,%s,%d,%d,%d,%d\n",
                            input, compressed, inBytes, compBytes,
                            compressMs, decompressMs
                    ));
                    rawWriter.flush();

                    compressTimes.add(compressMs);
                    decompressTimes.add(decompressMs);

                    System.out.println(" - " + name +
                            " | C=" + compressMs + "ms | D=" + decompressMs + "ms");
                }

                // ------------------------------------------------------
                //   MOYENNES (pour la courbe de la Question 11)
                // ------------------------------------------------------
                long avgComp = (long) compressTimes.stream().mapToLong(Long::longValue).average().orElse(0);
                long avgDec = (long) decompressTimes.stream().mapToLong(Long::longValue).average().orElse(0);

                Path avgCsv = Paths.get(OUT_DIR, "results_avg.csv");
                boolean exists = Files.exists(avgCsv);

                try (BufferedWriter w = Files.newBufferedWriter(
                        avgCsv, StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {

                    if (!exists) {
                        w.write("size_bytes,avg_compress_ms,avg_decompress_ms,count\n");
                    }

                    w.write(String.format("%d,%d,%d,%d\n",
                            size, avgComp, avgDec, FILES_PER_SIZE));
                }
            }
        }

        System.out.println("\n=== FIN DES EXPÉRIMENTATIONS ===");
    
    }


    public static void testFiles(String file1, String file2) throws Exception{
        try(BufferedReader reader1 = new BufferedReader(new FileReader(file1), 65_536);
            BufferedReader reader2 = new BufferedReader(new FileReader(file2), 65_536);) {
                String ligne_file1 = null, ligne_file2 = null;
                while(true) {
                    ligne_file1 = reader1.readLine();
                    ligne_file2 = reader2.readLine();
                    if(ligne_file1 == null || ligne_file2 == null) break;
                    if(!ligne_file1.equals(ligne_file2)) throw new Exception("File " + file1 + " doesn't match File " + file2 + " Problem spotted [ "+
                        ligne_file1 + " != " + ligne_file2 + " ]");
                }
        } catch(IOException ie) {
            ie.printStackTrace();
        }
    }


    /**
     * Point d’entrée du programme.
     *
     * Étapes principales :
     *  1. Création des dossiers data/ et out/
     *  2. Pour chaque taille T :
     *        – générer M fichiers aléatoires de taille T
     *        – compresser chacun avec Archivage.compresser()
     *        – décompresser avec Archivage.decompresser()
     *        – mesurer les temps
     *        – vérifier la correction
     *        – stocker les résultats détaillés dans results_raw.csv
     *        – calculer les moyennes et les écrire dans results_avg.csv
     *
     * @param args ignoré ici (utilisé dans la version ProcessBuilder)
     */
    public static void main(String[] args) throws Exception {
        Files.createDirectories(Paths.get(DATA_DIR));
        experimentTextFiles();
        //experimentCodeFiles();
    }
}
