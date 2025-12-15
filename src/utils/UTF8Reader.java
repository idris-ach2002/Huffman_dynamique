package utils;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * Utilitaires liés à l’UTF-8 : lecture de points de code Unicode depuis un {@link Reader}
 * et conversion d’un point de code en représentation binaire UTF-8.
 *
 * <p>⚠️ Note : un {@link Reader} Java fournit des unités UTF-16 (char). Cette classe gère donc
 * les paires de substitution (surrogate pairs) pour reconstruire les points de code au-delà du BMP.</p>
 */
public class UTF8Reader {

    /**
     * Lit le prochain point de code Unicode complet depuis un {@link Reader} (UTF-16 côté Java).
     *
     * <p>La méthode lit un premier {@code char}. Si ce {@code char} est un surrogate haut, elle lit un second
     * {@code char} et vérifie qu’il s’agit bien d’un surrogate bas, puis recompose le point de code.</p>
     *
     * @param reader lecteur de caractères (généralement un {@code InputStreamReader} en UTF-8)
     * @return le point de code Unicode, ou {@code -1} si fin de flux
     * @throws IOException si une erreur d’entrée/sortie survient ou si une paire de surrogates est invalide/incomplète
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
     * Convertit un point de code Unicode en une chaîne de bits correspondant à son encodage UTF-8.
     *
     * <p>Exemple : le caractère 'A' (U+0041) donne {@code 01000001}.</p>
     *
     * @param codePoint point de code Unicode à convertir
     * @return chaîne binaire composée de '0' et '1' représentant les octets UTF-8 du point de code
     * @throws IllegalArgumentException si {@code codePoint} est invalide
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