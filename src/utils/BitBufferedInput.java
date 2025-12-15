package utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Classe utilitaire permettant la lecture bit par bit (ou par groupes de bits)
 * à partir d'un flux d'entrée.
 * <p>
 * Cette classe utilise un tampon (buffer) interne de 8192 octets pour limiter
 * le nombre d'appels système lors de la lecture du flux, améliorant ainsi les performances.
 * Elle implémente {@link AutoCloseable} pour permettre une gestion automatique des ressources.
 * </p>
 */
public class BitBufferedInput implements AutoCloseable {
    private final InputStream in;
    private final byte[] buffer = new byte[8192];
    private int bufferPos = 0, bufferSize = 0;
    private int currentByte;
    //Le nombre de bits restants à lire dans l'octet courant (currentByte)
    private int numBitsRemaining = 0;

    public BitBufferedInput(FileInputStream in) {
        this.in = in;
    }

    /**
     * Recharge le tampon interne à partir du flux sous-jacent si nécessaire.
     *
     * @return {@code true} si des données ont été lues avec succès,
     * {@code false} si la fin du flux est atteinte.
     * @throws IOException En cas d'erreur de lecture depuis le flux.
     */
    private boolean refill() throws IOException {
        bufferSize = in.read(buffer);
        bufferPos = 0;
        return bufferSize != -1;
    }
    /**
     * Lit un seul bit du flux.
     *
     * @return Le bit lu (0 ou 1), ou -1 si la fin du flux est atteinte.
     * @throws IOException En cas d'erreur d'entrée/sortie.
     */
    public int readBit() throws IOException {
        if (numBitsRemaining == 0) {
            if (bufferPos >= bufferSize && !refill()) return -1;
            currentByte = buffer[bufferPos++] & 0xFF;
            numBitsRemaining = 8;
        }
        numBitsRemaining--;
        return (currentByte >>> numBitsRemaining) & 1;
    }

    /**
     * Lit {@code n} bits consécutifs et les assemble pour former un entier.
     * <p>
     * La lecture se fait du bit de poids fort vers le bit de poids faible (Big Endian).
     * Cette méthode permet de reconstituer des valeurs traversant les frontières des octets.
     * </p>
     *
     * @param n Le nombre de bits à lire. Doit être {@code > 0}.
     * Pour un résultat cohérent dans un int.
     * @return La valeur entière composée des bits lus, ou -1 si la fin de fichier
     * est rencontrée avant d'avoir pu lire tous les bits demandés.
     * @throws IOException En cas d'erreur d'entrée/sortie.
     */
    public int readBits(int n) throws IOException {
        int res = readBit(), b;
        if (res == -1) return -1;

        for(int i = 0; i<n-1; i++){
            res = res << 1;
            b = readBit();
            if(b==-1) return -1;
            res += b;
        }
        return res;

    }


    @Override
    public void close() throws IOException {
        in.close();
    }
}
