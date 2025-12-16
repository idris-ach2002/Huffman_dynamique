package compression;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

import structure.HuffmanTree;
import utils.DotGenerator;
import utils.UTF8Reader;

public class Compression {
	/**
	 * Compresse le fichier {@code src} et écrit le résultat binaire dans {@code dst}.
	 *
	 * <p>Si le nombre de bits à écrire n'est pas multiple de 8 on rajoute du padding à la fin
	 * ,donc il n'ya pas moyen de savoir combien de bits il y'en a à lire à la décompression
	 * </p>
	 *
	 * @param src chemin du fichier source (texte UTF-8)
	 * @param dst chemin du fichier de sortie (binaire compressé)
	 */
	public static void compresser(String src, String dst) {
		HuffmanTree AHA = new HuffmanTree();

		try (
				BufferedInputStream in = new BufferedInputStream(new FileInputStream(src), 65_536);
				InputStreamReader reader = new InputStreamReader(in, StandardCharsets.UTF_8);
				BufferedOutputStream writer = new BufferedOutputStream(new FileOutputStream(dst), 65_536)  ;
		)
		{

			int codePoint;
			int bitBuffer = 0;  // tampon de bits
			int bitCount = 0;   // nombre de bits dans le tampon

			while((codePoint = UTF8Reader.readCodePoint(reader)) != -1) {
				// Transformer le code point (Unicode) à un caractère à partir de son char UTF-16 java
				String symbol = new String(Character.toChars(codePoint));
				HuffmanTree.Leaf feuille = AHA.getCars().get(symbol);
				String codeBinaire;

				if(feuille == null) {
					// symbol pas présent dans l'arbre
					codeBinaire = HuffmanTree.getCode(AHA.getCarSpecial()) + UTF8Reader.toUTF8Bits(codePoint);
				}else {
					codeBinaire = HuffmanTree.getCode(feuille);
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
			if (bitCount > 0) {
				// on complète avec des 0 à droite
				bitBuffer <<= (8 - bitCount);
				writer.write(bitBuffer & 0xFF);
			}
			writer.flush();

			//Path p = Paths.get(src).getFileName();
			//DotGenerator.gen(AHA.getRoot(), "src/resources/" + p.toString().replace("txt", "dot"));

		} catch(IOException ie) {
			ie.printStackTrace();
		}		
	}
}
