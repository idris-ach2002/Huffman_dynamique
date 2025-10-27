package utils;

import java.io.IOException;
import java.io.InputStream;

/**
 * Version optimisée pour lecture bit à bit et bloc de bits (1 à 32 bits)
 * depuis un flux tamponné (par blocs de 64 Ko).
 */
public class BitInputStreamOptimized implements AutoCloseable {
    private final InputStream in;
    private final byte[] buffer = new byte[65_536];
    private int bufferPos = 0, bufferSize = 0;

    // Octet courant et nombre de bits restants à lire
    private int currentByte;
    private int numBitsRemaining = 0;

    public BitInputStreamOptimized(InputStream in) {
        this.in = in;
    }

    /** Recharge le tampon du flux si nécessaire. */
    private boolean refill() throws IOException {
        bufferSize = in.read(buffer);
        bufferPos = 0;
        return bufferSize != -1;
    }

    /**
     * Lit un seul bit (0 ou 1) du flux.
     * @return le bit lu ou -1 si fin du fichier.
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


    @Override
    public void close() throws IOException {
        in.close();
    }
}
