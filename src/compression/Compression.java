package compression;

import java.io.*;
import java.nio.charset.StandardCharsets;

import structure.HuffmanTree;
import utils.UTF8Reader;

public class Compression {

	public static void compresser(String src, String dst) {
		HuffmanTree AHA = new HuffmanTree();

		try (
				// Ouvrir un flux binaire vers le ficher à lire et puis décoder
				//les suites binaires en UTF8 (Char <=> Unicode qui est un UTF-16 en java)
				BufferedInputStream in = new BufferedInputStream(new FileInputStream(src), 65_536);
				InputStreamReader reader = new InputStreamReader(in, StandardCharsets.UTF_8);

				// Écriture optimisée : sortie bufferisée (32K)
				BufferedOutputStream writer = new BufferedOutputStream(new FileOutputStream(dst), 65_536);
		) {

			int codePoint;
			int bitBuffer = 0;  // tampon de bits
			int bitCount = 0;   // nombre de bits dans le tampon

			while ((codePoint = UTF8Reader.readCodePoint(reader)) != -1) {
				// Transformer le code point (Unicode) à un caractère à partir de son char UTF-16 java
				String symbol = new String(Character.toChars(codePoint));

				// On a une table de hashage dans HuffmanTree pour savoir si un car est présent
				// Accès en O(1) en moyenne (Pas coûteux)
				HuffmanTree.Leaf feuille = AHA.getCars().get(symbol);
				String codeBinaire;

				if (feuille == null) {
					// Transformer le code point (Unicode) d'un caractère à une suite binaire (lisible Base 2)
					// caractère n'existe pas (Code de la fuille SP | Code UTF8 du car (en binaire)
					codeBinaire = AHA.getCarSpecial().getCode() + UTF8Reader.toUTF8Bits(codePoint);
				} else {
					// On commence par écrire son code dans l'arbre avant de l'ajouter (pour le décompresseur)
					codeBinaire = feuille.getCode();
				}

				for (int i = 0; i < codeBinaire.length(); i++) {
					bitBuffer = (bitBuffer << 1) | (codeBinaire.charAt(i) - '0');
					bitCount++;

					if (bitCount == 8) {
						writer.write(bitBuffer);
						bitBuffer = 0;
						bitCount = 0;
					}
				}

				AHA.modification(symbol);
			}
			writer.flush();

		} catch (IOException ie) {
			ie.printStackTrace();
		}

	}

}
