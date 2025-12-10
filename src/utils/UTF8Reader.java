package utils;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class UTF8Reader {

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

}