package utils;

import java.io.IOException;

/**
 * Responsable du décodage des caractères UTF8 à partir d'un binaire.
 *
 * L'encodage UTF8 encode les "code points" de l'unicode slon la manière suivante:
 * - pour les points de codes entre U+0000 et U+007F (qui sont les caracteres ascii) l'encodage est
 * le même que le code point et donc il commence par "0" parceque l'ASCII c'est sur 7 bits
 * - Pour les points de codes entre U+0080 et U+007FF, le caractère est encodé par deux octets,
 * avec le premier octet qui commence avec "110" et le second qui commence avec "10"
 * - Pour les points de codes entre U+0800 et U+FFFF, le caractère est encodé par trois octets,
 *  avec le premier octet qui commence avec "1110" et le restes des octets qui commencent avec "10"
 * - Pour les points de codes entre U+10000 et U+10FFFF, le caractère est encodé par 4 octets,
 *  avec le premier octet qui commence avec "11110" et le restes des octets qui commencent avec "10"
 *
 *  En java il faut juste faire attention  a la conversion du code point (généré par le décodage) en un char.
 *  Parceque Java utilise de l'UTF-16 qui est fixe donc sur deux octets
 */
public class UTF8Decoder {

    /**
     * Décode et retourne le prochain caractere encodé en UTF8 dans le flux.
     *
     * @param in
     * @return retourne null quand c'est une fin de fichier
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
                throw new UTF8DecodeException("Function concatBits In The body of Decode Function Fault Signal");
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
     * retourne les k premier bits de l'octes (premiers en partant du poids faible)
     * @param k k premiers bits, k > 0
     * @param b un octet
     * @return
     */
    private static int getKiemesBits(int k, int b){
        int mask = 1;
        k = k-1;
        for (int i = 0; i < k; i++) mask = (mask << 1) + 1;
        return b & mask;
    }

    /**
     *
     * @param i entre 0 et 7, 0 poids faible
     * @param b byte
     * @return
     */
    private static int getIemeBit(int i, int b){
        return b >>> i & 1;
    }

    /**
     * Cette méthode est spécifique pour la concaténation de bits significatifs dans l'encodage utf8,
     * donc on sait que pour n2 on a toujours exactement 6 bits (après avoir retiré le "10")
     * @param n1 sera dans les poids fort dans le resultat.
     * @param n2 sera dans les poids faibles dans le resultat. @pre: nombre de bits = 6.
     * @return
     */
    private static int concatBits(int n1, int n2){
        return (n1 << 6) + n2;
    }

    /**
     * test si l'octet est bien un octet de continuation (qui commence donc avec 10xxxxxx)
     * @param b
     * @return
     */
    private static boolean isValidContinuation(int b){
        return getKiemesBits (2,b >>> 6) == 2; // 2 = 10
    }

}
