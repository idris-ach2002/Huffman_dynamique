package Experimentation;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * RandomFileGenerator
 *
 * Cette classe permet de générer des fichiers textuels aléatoires en contrôlant
 * la distribution de probabilité des caractères. Trois types de distributions
 * sont supportées :
 *
 * 1) Distribution uniforme
 *    Tous les caractères de l'alphabet ont la même probabilité d'apparition.
 *
 * 2) Distribution personnalisée
 *    L'utilisateur fournit explicitement un tableau de probabilités (p1, p2, ..., pn)
 *    qui doit vérifier :  sum(pi) = 1  et  pi >= 0.
 *
 * 3) Distribution suivant la loi de Zipf
 *    Les caractères sont classés par rang i = 1..n et la probabilité
 *    est proportionnelle à 1 / i^s, où s > 1.
 *
 * *** Amélioration importante ***
 * Pour permettre la génération de fichiers très volumineux (plusieurs dizaines
 * ou centaines de mégaoctets), cette version n'utilise plus de StringBuilder
 * géant. Elle écrit au fur et à mesure dans un fichier, grâce à un
 * BufferedWriter qui limite la consommation mémoire.
 *
 * Cela permet de générer des fichiers de 100 Mo, 500 Mo ou plus, sans jamais
 * stocker le contenu complet en mémoire.
 */
public class RandomFileGenerator {

    /** Alphabet utilisé pour générer les caractères */

    private static final String ALPHABET = buildAsciiAlphabet();

    private static String buildAsciiAlphabet() {
        int size = 62;
        char[] cars = new char[size];
        int index = 0;

        // chiffres 0–9
        for (char c = '0'; c <= '9'; c++) {
            cars[index++] = c;
        }

        // A–Z
        for (char c = 'A'; c <= 'Z'; c++) {
            cars[index++] = c;
        }

        // a–z
        for (char c = 'a'; c <= 'z'; c++) {
            cars[index++] = c;
        }
        var random = ThreadLocalRandom.current();
        for (int i = size - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);  // 0..i
            char tmp = cars[i];
            cars[i] = cars[j];
            cars[j] = tmp;
        }

        StringBuilder sb = new StringBuilder(size);
        for (int i = 0; i < size; i++) {
            sb.append(cars[i]);
        }

        return sb.toString();
    }

    private static final int LINE_LENGTH = 80;


    /** Générateur pseudo-aléatoire */
    private static Random random = new Random();

    // -------------------------------------------------------------
    // 1) Distribution uniforme — version bufferisée
    // -------------------------------------------------------------

    /**
     * Génère un fichier selon une distribution uniforme.
     *
     * Mathématiquement :
     *      P(c) = 1 / |A|
     * pour tout caractère c appartenant à l'alphabet A.
     *
     * @param size taille du fichier (en caractères)
     * @param path destination du fichier généré
     */
    public static void generateUniformToFile(int size, String path) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(path), 65_536)) {

            int col = 0; // compteur de position dans la ligne

            for (int i = 0; i < size; i++) {
                char c = ALPHABET.charAt(random.nextInt(ALPHABET.length()));
                bw.write(c);
                col++;

                if (col >= LINE_LENGTH) {
                    bw.newLine();   // écrit "\n" de manière portable
                    col = 0;
                }
            }
        }
    }

    // -------------------------------------------------------------
    // 2) Distribution personnalisée — version bufferisée
    // -------------------------------------------------------------

    /**
     * Génère un fichier selon une distribution de probabilité personnalisée.
     *
     * Mathématiquement :
     *  - On choisit un caractère ci avec probabilité pi.
     *  - La somme des pi doit faire 1.
     *
     * Technique de tirage :
     *  - Construire la distribution cumulative :
     *        Ck = p1 + p2 + … + pk
     *  - Tirer r uniformément dans [0,1]
     *  - Choisir le plus petit k tel que Ck ≥ r
     *
     * @param size taille du fichier
     * @param probabilities tableau des probabilités (même taille que ALPHABET)
     * @param path fichier de sortie
     */
    public static void generateFromDistributionToFile(int size, double[] probabilities, String path)
            throws IOException {

        double[] cumulative = new double[probabilities.length];
        cumulative[0] = probabilities[0];

        for (int i = 1; i < probabilities.length; i++) {
            cumulative[i] = cumulative[i - 1] + probabilities[i];
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(path), 65_536)) {

            int col = 0;

            for (int i = 0; i < size; i++) {
                double r = random.nextDouble();
                int index = 0;

                while (index < cumulative.length && r > cumulative[index]) {
                    index++;
                }

                bw.write(ALPHABET.charAt(index));
                col++;

                if (col >= LINE_LENGTH) {
                    bw.newLine();
                    col = 0;
                }
            }
        }
    }

    // -------------------------------------------------------------
    // 3) Distribution Zipf — version bufferisée
    // -------------------------------------------------------------

    /**
     * Génère un fichier dont la distribution suit la loi de Zipf.
     *
     * Mathématiquement :
     *      P(i) = (1 / i^s) / (sum_{k=1..n} 1/k^s)
     *
     * @param size taille du fichier
     * @param s paramètre de Zipf (généralement entre 1 et 2)
     * @param path fichier de sortie
     */
    public static void generateZipfToFile(int size, double s, String path) throws IOException {

        int n = ALPHABET.length();
        double[] probs = new double[n];

        // Normalisation : calcul du dénominateur Σ(1/k^s)
        double sum = 0;
        for (int i = 1; i <= n; i++) {
            sum += 1.0 / Math.pow(i, s);
        }

        // Probabilités normalisées
        for (int i = 1; i <= n; i++) {
            probs[i - 1] = (1.0 / Math.pow(i, s)) / sum;
        }

        // Réutilisation de la méthode précédente (distribution personnalisée)
        generateFromDistributionToFile(size, probs, path);
    }




    // -------------------------------------------------------------
    // 3) Distribution Croissante pour voir le pire cas théorique
    // -------------------------------------------------------------

    /**
     * Génère un fichier dont la distribution Croissante.
     *
     *
     * @param size taille du fichier
     * @param path fichier de sortie
     */
    public static void generateSortedToFile(int size, String path) throws IOException {

        int n = ALPHABET.length();
        double[] probs = new double[n];

        // Somme des poids croissants : 1 + 2 + ... + n
        double sumWeights = n * (n + 1) / 2.0;

        // Probabilités croissantes normalisées
        for (int i = 0; i < n; i++) {
            probs[i] = (i + 1) / sumWeights;
        }

        // Réutilisation de la méthode précédente (distribution personnalisée)
        generateFromDistributionToFile(size, probs, path);
    }
}
