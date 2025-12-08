package decompression;

import structure.HuffmanTree;
import utils.BitBufferedInput;
import utils.UTF8DecodeException;
import utils.UTF8Decoder;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * Fournit une procédure de décompression d'un fichier compressé.
 * <br>
 * NB: La décompression fournit n'est pas égale exactement au texte d'origine, elle contient des caractères
 * en plus par rapport à l'original, à cause des bits de padding ajouté par le système de fichier qui taravail
 * avec des blocs d'octets, alors que pour avoir la décompression parfaite il faut connaitre le nombre de bits exacte
 */
public class Decompression {

    public static void decompresser(String huffCompFile, String dst){
        HuffmanTree aha = new HuffmanTree();
        String carSpecial = aha.getCarSpecial().getCaractere();

        try (BitBufferedInput in = new BitBufferedInput(new FileInputStream(huffCompFile));
             BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(dst), 65_536);
             OutputStreamWriter writer = new OutputStreamWriter(bos, StandardCharsets.UTF_8)){

            String c = UTF8Decoder.decode(in);
            if (c==null) return;

            writer.write(c);
            aha.modification(c);
            c = readFromRoot(in, aha.getRoot());

            while(c != null){
                // Le cas ou l'on croise un car qui n'est pas dans l'arbre
                if (c.equals(carSpecial)) {
                    c = UTF8Decoder.decode(in);
                    if (c == null) break;
                }
                writer.write(c);
                aha.modification(c);
                c = readFromRoot(in, aha.getRoot());
            }
            writer.flush();
            //aha.afficherArbre();

        }catch (IOException e) {
            System.out.println(e.getMessage());
        } catch (UTF8DecodeException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     *
     * Retourne le caractere présent à la feuille, suite au suivi du chemin lu à partir du fichier compressé
     *
     * @param in
     * @param root racine du AHA à l'étape courante de décompression
     * @return Retourne null si fin de fichier
     * @throws IOException
     */
    private static String readFromRoot(BitBufferedInput in, HuffmanTree.Node root) throws IOException {
        HuffmanTree.Node n = root;
        while (!(n instanceof HuffmanTree.Leaf)){
            int b = in.readBit();
            if (b == -1) return null;

            if (b == 0) n = n.getLeft();
            else n = n.getRight();
        }
        return ((HuffmanTree.Leaf) n).getCaractere();
    }



	public static void main(String[] args) {
        String input = "/home/idris-achabou/git/Huffman_dynamique/data/final_fail.huff";
        String output = "/home/idris-achabou/git/Huffman_dynamique/data/final_fail_decompressed.txt";

        long start = System.currentTimeMillis();
        Decompression.decompresser(input, output);   // ta méthode existante
        long end = System.currentTimeMillis();
		System.out.println("Compression : " + (end-start) + " ms");
	}


}
