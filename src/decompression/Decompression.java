package decompression;

import structure.HuffmanTree;
import utils.BitBufferedInput;
import utils.UTF8DecodeException;
import utils.UTF8Decoder;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class Decompression {


    /**
     * Décompresse un fichier binaire produit par la compression Huffman adaptatif et écrit le texte reconstruit en UTF-8.
     * La décompression peut produit un surplus de caractères à cause du padding rajouté à la compression
     *
     * @param huffCompFile chemin du fichier compressé (binaire)
     * @param dst chemin du fichier de sortie (texte UTF-8)
     */

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

        }catch (IOException e) {
            System.out.println(e.getMessage());
        } catch (UTF8DecodeException e) {
            throw new RuntimeException(e);
        }
    }
    /**
     * Lit des bits depuis {@code in} en partant de {@code root} et suit l’arbre jusqu’à atteindre une feuille.
     *
     * <p>Chaque bit lu correspond à un déplacement : 0 → gauche, 1 → droite. Lorsque la feuille est atteinte,
     * la méthode renvoie le symbole associé.</p>
     *
     * @param in flux d’entrée binaire permettant une lecture bit à bit
     * @param root racine de l’arbre de Huffman adaptatif à l’instant courant du décodage
     * @return le symbole décodé (contenu dans la feuille), ou {@code null} si fin de fichier
     * @throws IOException si une erreur d’entrée/sortie survient pendant la lecture
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

}
