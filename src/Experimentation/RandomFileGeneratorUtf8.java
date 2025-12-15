package Experimentation;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Random;



/**
 * RandomFileGenerator
 * 
 *
 * Générateur de fichiers textuels UTF-8 de grande taille.
 *
 * IMPORTANT :
 *  - size = taille du fichier en OCTETS (UTF-8)
 *  - alphabet UTF-8 jusqu'à 38 000 caractères distincts
 *  - chaque caractère est comptabilisé selon sa taille réelle (1 à 4 octets)
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
public class RandomFileGeneratorUtf8 {

    // -------------------------------------------------------------
    // Configuration générale
    // -------------------------------------------------------------

    private static final int MAX_ALPHABET_SIZE = 512; //38_000 
    /*
    Huffman adaptatif casse ici (fondamental)

    Huffman adaptatif fait :

    1 symbole distinct → 2 nœuds

    38 000 symboles → ≈ 76 000 nœuds

    128 000 symboles → ≈ 256 000 nœuds
    */
    private static final int LINE_LENGTH = 80;
    private static final int BUFFER_SIZE = 65_536;

    private static final Random random = new Random();

    // -------------------------------------------------------------
    // Alphabet UTF-8
    // -------------------------------------------------------------

    private static final int[] ALPHABET = buildUtf8Alphabet();

    private static int[] buildUtf8Alphabet() {
        int[] alphabet = new int[MAX_ALPHABET_SIZE];
        int count = 0;

        for (int cp = 0x20; cp <= 0x10FFFF && count < MAX_ALPHABET_SIZE; cp++) {

            // 1) code point valide Unicode
            if (!Character.isValidCodePoint(cp)) continue;

            // 2) exclusion stricte des surrogates UTF-16
            if (cp >= 0xD800 && cp <= 0xDFFF) continue;

            // 3) optionnel : exclure contrôles (sauf \n si voulu)
            if (Character.getType(cp) == Character.CONTROL) continue;

            alphabet[count++] = cp;
        }

        return alphabet;
    }



    // -------------------------------------------------------------
    // Outils UTF-8
    // -------------------------------------------------------------

    private static int utf8Length(int codePoint) {
        if (codePoint <= 0x7F) return 1;
        if (codePoint <= 0x7FF) return 2;
        if (codePoint <= 0xFFFF) return 3;
        return 4;
    }

    private static BufferedWriter utf8Writer(String path) throws IOException {
        return new BufferedWriter(
                new OutputStreamWriter(
                        new FileOutputStream(path),
                        StandardCharsets.UTF_8
                ),
                BUFFER_SIZE
        );
    }

    private static boolean writeCodePoint(
            BufferedWriter bw,
            int codePoint,
            long maxSize,
            long[] bytesWritten,
            int[] col
    ) throws IOException {

        int charBytes = utf8Length(codePoint);

        if (bytesWritten[0] + charBytes > maxSize) {
            return false;
        }

        bw.write(Character.toChars(codePoint));
        bytesWritten[0] += charBytes;
        col[0]++;

        if (col[0] >= LINE_LENGTH) {
            if (bytesWritten[0] + 1 > maxSize) {
                return false;
            }
            bw.newLine(); // '\n' = 1 octet en UTF-8
            bytesWritten[0] += 1;
            col[0] = 0;
        }
        return true;
    }

    // -------------------------------------------------------------
    // 1) Distribution uniforme UTF-8
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

    public static void generateUniformToFile(long size, String path)
            throws IOException {

        try (BufferedWriter bw = utf8Writer(path)) {

            long[] bytesWritten = {0};
            int[] col = {0};

            while (bytesWritten[0] < size) {
                int cp = ALPHABET[random.nextInt(ALPHABET.length)];
                if (!writeCodePoint(bw, cp, size, bytesWritten, col)) {
                    return;
                }
            }
        }
    }

    // -------------------------------------------------------------
    // 2) Distribution personnalisée UTF-8
    // -------------------------------------------------------------

    public static void generateFromDistributionToFile(
            long size,
            double[] probabilities,
            String path
    ) throws IOException {

        if (probabilities.length != ALPHABET.length) {
            throw new IllegalArgumentException(
                    "La taille du tableau de probabilités doit être égale à celle de l'alphabet"
            );
        }

        double[] cumulative = new double[probabilities.length];
        cumulative[0] = probabilities[0];
        for (int i = 1; i < probabilities.length; i++) {
            cumulative[i] = cumulative[i - 1] + probabilities[i];
        }

        try (BufferedWriter bw = utf8Writer(path)) {

            long[] bytesWritten = {0};
            int[] col = {0};

            while (bytesWritten[0] < size) {
                double r = random.nextDouble();
                int i = 0;
                while (r > cumulative[i]) i++;

                if (!writeCodePoint(
                        bw, ALPHABET[i], size, bytesWritten, col
                )) {
                    return;
                }
            }
        }
    }

    // -------------------------------------------------------------
    // 3) Distribution Zipf UTF-8
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
    public static void generateZipfToFile(
            long size,
            double s,
            String path
    ) throws IOException {

        int n = ALPHABET.length;
        double[] probs = new double[n];

        double sum = 0.0;
        for (int i = 1; i <= n; i++) {
            sum += 1.0 / Math.pow(i, s);
        }

        for (int i = 1; i <= n; i++) {
            probs[i - 1] = (1.0 / Math.pow(i, s)) / sum;
        }

        generateFromDistributionToFile(size, probs, path);
    }

    // -------------------------------------------------------------
    // 4) Distribution croissante UTF-8
    // -------------------------------------------------------------

    public static void generateSortedToFile(long size, String path)
            throws IOException {

        int n = ALPHABET.length;
        double[] probs = new double[n];

        double sumWeights = n * (n + 1) / 2.0;
        for (int i = 0; i < n; i++) {
            probs[i] = (i + 1) / sumWeights;
        }

        generateFromDistributionToFile(size, probs, path);
    }


/**
 * Génère un fichier correspondant au pire cas théorique (Suite Fibonacci)
 * du Huffman dynamique 
 *
 * Séquence générée :
a
b
cc
ddd
eeeee
hhhhhhhhh
 *   ...
 *
 * Cette construction force un arbre extrêmement déséquilibré
 * (forme de peigne).
 *
 * @param size taille du fichier (en caractères)
 * @param path fichier de sortie
 */

    public static void generateWorstCaseHuffmanDynamicToFile(
            long size,
            String path
    ) throws IOException {

        try (BufferedWriter bw = utf8Writer(path)) {

            long[] bytesWritten = {0};
            int[] col = {0};

            long fPrev = 1;
            long fCurr = 1;

            for (int i = 0; i < ALPHABET.length && bytesWritten[0] < size; i++) {

                long repeat;
                if (i <= 1) {
                    repeat = 1;
                } else {
                    repeat = fPrev + fCurr;
                    fPrev = fCurr;
                    fCurr = repeat;
                }

                for (long r = 0; r < repeat; r++) {
                    if (!writeCodePoint(
                            bw,
                            ALPHABET[i],
                            size,
                            bytesWritten,
                            col
                    )) {
                        return;
                    }
                }
            }
        }
    }
}

