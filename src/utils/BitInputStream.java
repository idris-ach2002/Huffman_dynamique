package utils;

import java.io.IOException;
import java.io.InputStream;

public class BitInputStream implements AutoCloseable {
    /** Flux sous-jacent, idéalement déjà bufferisé (BufferedInputStream). */
    private final InputStream in;

    /** Dernier octet lu (0..255). -1 uniquement renvoyé par in.read(), jamais stocké. */
    private int currentByte;

    /** Nombre de bits restant à extraire dans currentByte (entre 0 et 8). */
    private int numBitsRemaining;

    /**
     * @param in flux binaire sous-jacent (pensez à le bufferiser : new BufferedInputStream(...))
     */
    public BitInputStream(InputStream in) {
        this.in = in;
        this.currentByte = 0;
        this.numBitsRemaining = 0;
    }

    /**
     * Lit le prochain bit du flux.
     * @return 0 ou 1 si un bit a été lu ; -1 si fin de flux (aucun bit restant).
     * @throws IOException si une erreur E/S survient sur le flux sous-jacent.
     */
    public int readBit() throws IOException {
        // 1) Recharge un octet si nécessaire
        if (numBitsRemaining == 0) {
            currentByte = in.read();     // 0..255, ou -1 fin de flux
            if (currentByte == -1) {
                return -1;               // fin du fichier : plus de bits
            }
            numBitsRemaining = 8;        // 8 bits à extraire dans ce nouvel octet
        }

        // 2) Avance d’un bit (MSB→LSB) et calcule ce bit
        numBitsRemaining--;               // passe de 8→7→6→...→0
        // Décalage logique >>> : on amène le prochain bit à droite, puis &1 pour l’isoler.
        // Exemple : currentByte=0b01010100, numBitsRemaining=7 → (01010100 >>> 7) = 00000000 → &1 = 0
        //           currentByte=0b01010100, numBitsRemaining=6 → (01010100 >>> 6) = 00000001 → &1 = 1
        return (currentByte >>> numBitsRemaining) & 1;
    }

    /**
     * Ferme le flux sous-jacent.
     */
    @Override
    public void close() throws IOException {
        in.close();
    }
}
