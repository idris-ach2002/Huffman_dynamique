package utils;

import java.io.IOException;

/**
 * Utilitaire de décodage UTF-8 à partir d’un flux binaire lu bit à bit.
 *
 * <p>Cette classe lit un premier octet pour déterminer la longueur de la séquence UTF-8 (1 à 4 octets),
 * lit ensuite les octets de continuation, reconstruit le code point Unicode, puis retourne le caractère
 * correspondant sous forme de {@link String} (en tenant compte du fait que Java utilise l’UTF-16).</p>
 */
public class UTF8Decoder {

    /**
     * Décode et retourne le prochain caractère encodé en UTF-8 depuis le flux binaire.
     *
     * @param in flux binaire permettant la lecture (par blocs de 8 bits)
     * @return le caractère décodé sous forme de {@link String}, ou {@code null} si fin de fichier
     * @throws IOException si une erreur d'entrée/sortie survient pendant la lecture
     * @throws UTF8DecodeException si la séquence UTF-8 lue est invalide (octet de continuation incorrect, préfixe invalide, etc.)
     */
    public static String decode(BitBufferedInput in) throws IOException, UTF8DecodeException {
        // determine la longueur de l'encodage
        int firstByte;
        firstByte = in.readBits(8);
        if (firstByte == -1) return null;

        int n_bytes;
        // RQ: firstBit(firstByte) représente l'entré de l'automate
        switch (firstBit(firstByte)){
            case "ASCII":
                return String.valueOf((char) firstByte);
            case "TWO_BYTES" :
                n_bytes = 2;
                break;
            case "THREE_BYTES":
                n_bytes = 3;
                break;
            case "FOUR_BYTES":
                n_bytes = 4;
                break;
            default: throw new UTF8DecodeException("Function Decode Fault Signal");
        }
        // le code point resultant des concaténations des octets de l'UTF8
        int codePoint = getKiemesBits(8-(n_bytes+1), firstByte); // on recup les premiers bits en ignorant les "110", "1110" etc
        for (int i = 0; i<n_bytes-1; i++){
            int octet = in.readBits(8);
            if (isValidContinuation(octet)){
                // On concatene avec les 6 bits restant après le "10"
                codePoint = concatBits(codePoint,getKiemesBits(6, octet));
            }else{
                throw new UTF8DecodeException("Not valid continuation byte");
            }
        }
        // très important, à cause du fait que java  utilise de l'UTF-16
        return new String(Character.toChars(codePoint));

    }

    private static String firstBit(int byte_) throws UTF8DecodeException{
        // Quand on dit premier bit c'est à partir du poids fort
        if (getIemeBit(7, byte_) == 0) return "ASCII";
        return secondBit(byte_);
    }

    private static String secondBit(int byte_) throws UTF8DecodeException{
        if (getIemeBit(6, byte_) == 1) return thirdBit(byte_);
        throw new UTF8DecodeException("Second Bit Function");
    }

    private static String thirdBit(int byte_) throws UTF8DecodeException{
        if (getIemeBit(5, byte_) == 1) return fourthBit(byte_);
        // Dans le cas: 110
        return "TWO_BYTES";
    }

    private static String fourthBit(int byte_) throws UTF8DecodeException{
        if (getIemeBit(4, byte_) == 1) return fifthBit(byte_);
        // Dans le cas: 1110
        return "THREE_BYTES";
    }

    private static String fifthBit(int byte_) throws UTF8DecodeException{
        if (getIemeBit(3, byte_) == 1) throw new UTF8DecodeException("fifth Bit Function");
        // Dans le cas: 11110
        return "FOUR_BYTES";
    }

    /**
     * Extrait les {@code k} bits de poids faible de {@code b}.
     *
     * @param k nombre de bits à extraire (k &gt; 0)
     * @param b octet source
     * @return valeur entière correspondant aux {@code k} bits de poids faible
     */
    private static int getKiemesBits(int k, int b){
        int mask = 1;
        k = k-1;
        for (int i = 0; i < k; i++) mask = (mask << 1) + 1;
        return b & mask;
    }

    /**
     * Retourne le i-ème bit de {@code b}.
     *
     * @param i index du bit (0 = bit de poids faible, 7 = bit de poids fort)
     * @param b octet source
     * @return 0 ou 1
     */
    private static int getIemeBit(int i, int b){
        return b >>> i & 1;
    }

    /**
     * Concatène deux segments de bits dans le cas du décodage UTF-8.
     *
     * <p>Dans ce projet, les octets de continuation apportent toujours 6 bits utiles (après avoir retiré le préfixe {@code 10}).</p>
     *
     * @param n1 bits déjà construits (placés dans les poids forts)
     * @param n2 bits suivants (placés dans les poids faibles), supposés sur 6 bits
     * @return résultat de la concaténation
     */
    private static int concatBits(int n1, int n2){
        return (n1 << 6) + n2;
    }

    /**
     * test si l'octet est bien un octet de continuation (qui commence donc avec 10xxxxxx)
     * @param b octet à tester
     * @return
     */
    private static boolean isValidContinuation(int b){
        return getKiemesBits (2,b >>> 6) == 2; // 2 = 10
    }

}
