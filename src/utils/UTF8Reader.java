package utils;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class UTF8Reader {

    /**
     * Lit un fichier UTF-8 caractère par caractère (code point complet)
     * et affiche son encodage UTF-8 binaire.
     */
    public static void readUTF8File(String filePath) throws IOException {
        try (Reader reader = new InputStreamReader(
                new FileInputStream(filePath), StandardCharsets.UTF_8)) {

            int codePoint;
            while ((codePoint = readCodePoint(reader)) != -1) {
                String symbol = new String(Character.toChars(codePoint));
                String utf8Bits = toUTF8Bits(codePoint);
                System.out.printf("%s -> U+%04X -> %s%n", symbol, codePoint, utf8Bits);
            }
        }
    }

    /**
     * Lit le prochain code point Unicode complet depuis un Reader UTF-8.
     * Retourne -1 si la fin du flux est atteinte.
     */
    public static int readCodePoint(Reader reader) throws IOException {
        int first = reader.read(); // lit un premier char UTF-16
        if (first == -1) {
            return -1; // fin du fichier
        }

        char ch = (char) first;

        // Gestion d’une paire de substitution (surrogate pair)
        if (Character.isHighSurrogate(ch)) {
            int second = reader.read();
            if (second == -1) {
                throw new IOException("Fin de fichier inattendue après un surrogate haut");
            }
            char low = (char) second;
            if (!Character.isLowSurrogate(low)) {
                throw new IOException("Caractère invalide : surrogate haut suivi d’autre chose");
            }
            return Character.toCodePoint(ch, low);
        } else {
            return ch; // simple BMP
        }
    }

    /**
     * Convertir un code point Unicode en chaîne de bits représentant son encodage UTF-8.
     */
    public static String toUTF8Bits(int codePoint) {
        String s = new String(Character.toChars(codePoint));
        byte[] bytes = s.getBytes(StandardCharsets.UTF_8);

        StringBuilder sb = new StringBuilder(bytes.length * 8);
        for (byte b : bytes) {
            // & 0xFF pour obtenir la valeur non signée
            sb.append(String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0'));
        }
        return sb.toString();
    }
    
    /**
     * Version optimisée : retourne directement les bits du codePoint UTF-8
     * sous forme d'entiers (0/1), sans créer de chaîne binaire.
     */
    public static int[] toUTF8BitsFast(int codePoint) {
        // Convertit le code point en bytes UTF-8
        byte[] utf8Bytes = new String(Character.toChars(codePoint)).getBytes(StandardCharsets.UTF_8);
        int totalBits = utf8Bytes.length * 8;
        int[] bits = new int[totalBits];
        int index = 0;

        // Pour chaque byte UTF-8, on extrait ses bits (du MSB vers le LSB)
        for (byte b : utf8Bytes) {
            for (int i = 7; i >= 0; i--) {
                bits[index++] = (b >> i) & 1; // extrait le bit i
            }
        }
        return bits;
    }

    
    /**
     * Pour reconstruire un caractère (String) à partir d’une
     *  suite binaire UTF-8 (ex: "1100001110100111" → "ç")
     * */
    public static String fromUTF8Bits(String bits) {
        // Vérification basique
        if (bits == null || bits.isEmpty()) return "";

        // Découper la chaîne en groupes de 8 bits
        int nbOctets = bits.length() / 8;
        if (bits.length() % 8 != 0) {
            throw new IllegalArgumentException("La chaîne binaire n'est pas un multiple de 8 bits : " + bits.length());
        }
        //'ç'
        //0xC3 0xA7 deux suite en héxa
        //11000011 10100111

        byte[] bytes = new byte[nbOctets];
        for (int i = 0; i < nbOctets; i++) {
            String octetStr = bits.substring(i * 8, i * 8 + 8);
            int val = Integer.parseInt(octetStr, 2);
            bytes[i] = (byte) val; // Hex
        }

        // Décodage UTF-8 inverse
        return new String(bytes, StandardCharsets.UTF_8);
    }
    
    /**
     * Détermine la longueur (en octets) d’un caractère UTF-8
     * à partir de son premier octet.
     *
     * 
     * En UTF-8, le premier octet d’un caractère indique
     * combien d’octets composent la séquence d’encodage.
     * On identifie ce nombre en regardant les bits de poids fort
     * (les bits les plus à gauche) du premier octet, selon le schéma :
     *
     * 
     * 1 octet : 0xxxxxxx
     * 2 octets : 110xxxxx
     * 3 octets : 1110xxxx
     * 4 octets : 11110xxx
     * Suite d’un caractère (non valide en premier octet) : 10xxxxxx
     * 
     *
     
     * Cette méthode applique des masques binaires pour isoler les bits de tête :
     *
     *   0b10000000 → garde le bit de poids fort (test du préfixe "0")
     *   0b11100000 → garde les 3 bits de tête (préfixe "110")
     *   0b11110000 → garde les 4 bits de tête (préfixe "1110")
     *   0b11111000 → garde les 5 bits de tête (préfixe "11110")
     * </ul>
     * Ces masques permettent de tester la forme binaire du premier octet :
     *

     * (b & 0b11100000) == 0b11000000  // "110xxxxx" → 2 octets


     * Exemples :

     * 0x41 ('A')      → 0b01000001 → 1 octet
     * 0xC3 ('ç' 1er)  → 0b11000011 → 2 octets
     * 0xE2 ('€' 1er)  → 0b11100010 → 3 octets
     * 0xF0 ('😄' 1er) → 0b11110000 → 4 octets
     * 0xA7 (suite)    → 0b10100111 → -1 (octet de continuation)

     */
    public static int utf8LengthFromFirstByte(byte firstByte) {
        // En Java, les bytes sont signés (-128..127).
        // On force ici l'interprétation non signée (0..255).
        int b = firstByte & 0xFF;

        // Cas 1 : 0xxxxxxx → caractère ASCII (1 octet)
        // Masque : 10000000 → garde le bit le plus à gauche
        if ((b & 0b10000000) == 0b00000000) {
            return 1;
        }

        // Cas 2 : 110xxxxx → caractère UTF-8 sur 2 octets
        // Masque : 11100000 → garde les 3 bits de tête
        // Résultat attendu : 11000000
        else if ((b & 0b11100000) == 0b11000000) {
            return 2;
        }

        // Cas 3 : 1110xxxx → caractère UTF-8 sur 3 octets
        // Masque : 11110000 → garde les 4 bits de tête
        // Résultat attendu : 11100000
        else if ((b & 0b11110000) == 0b11100000) {
            return 3;
        }

        // Cas 4 : 11110xxx → caractère UTF-8 sur 4 octets
        // Masque : 11111000 → garde les 5 bits de tête
        // Résultat attendu : 11110000
        else if ((b & 0b11111000) == 0b11110000) {
            return 4;
        }

        // Cas 5 : 10xxxxxx → octet de continuation (non valide en premier)
        // ou bien motif totalement invalide.
        return -1;
    }



    public static void main(String[] args) throws IOException {
        readUTF8File("src/utils/test/exemple.txt");
        int cp = 'ç'; // code point 0x00E7
        String bits = toUTF8Bits(cp);
        System.out.println("Bits : " + bits);

        String decoded = fromUTF8Bits(bits);
        System.out.println("Décodé : " + decoded);
    }
}
