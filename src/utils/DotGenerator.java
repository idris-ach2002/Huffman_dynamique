package utils;

import structure.HuffmanTree;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Stack;
/**
 * Classe utilitaire pour la génération de graphiques représentant l'arbre de Huffman.
 * <p>
 * Cette classe génère un fichier au format DOT (compatible avec le logiciel Graphviz)
 * permettant de visualiser la structure de l'arbre, les occurrences et les caractères
 * des feuilles.
 * </p>
 */
public class DotGenerator {

    public static final String nodeAttSuffix = " shape=\"square\" style=\"filled\" width=0.2 height=0.2]";

    /**
     * Génère un fichier .dot représentant l'Arbre de Huffman enraciné au nœud spécifié.
     *
     * @param root La racine de l'arbre de Huffman (ou du sous-arbre) à visualiser.
     * @param dst  Le chemin ou le nom du fichier de destination (ex: "arbre.dot").
     * @throws RuntimeException Si une erreur d'entrée/sortie (IOException) survient lors de l'écriture du fichier.
     */
    public static void gen(HuffmanTree.Node root, String dst){

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(dst))){
            bw.write("digraph G {\n");
            Stack<GenContext> stack = new Stack<>();
            stack.push(new GenContext(root, 0));
            // id courant unique
            int id = 0;

            while (!stack.isEmpty()){
                id ++;
                GenContext curr = stack.pop();

                if(!(curr.n instanceof HuffmanTree.Leaf)){
                    bw.write(id + constructLabel(curr.n.getOccurence(), null) + nodeAttSuffix + ";\n");
                    stack.push(new GenContext(curr.n.getRight(), id));
                    stack.push(new GenContext(curr.n.getLeft(), id));
                }else{
                    bw.write(id + constructLabel(curr.n.getOccurence(),((HuffmanTree.Leaf) curr.n).getCaractere()) + nodeAttSuffix + ";\n");
                }
                // si == 0 le curr est la racine
                if (curr.idParent != 0){
                    bw.write(curr.idParent + " -> " + id + ";\n");
                }
            }

            bw.write("}\n");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
    private static String constructLabel(int occ, String car){
        if (car == null)
            return "[label=\" occ = " + occ + " \" ";
        else
            return "[label=\" occ = " + occ + "\\n" +
                     "\\" + car + " \" ";
    }
    /**
     * Représente le contexte local à un noeud, pendant la récursion
     */
    private static class GenContext {
        HuffmanTree.Node n;
        int idParent;

        GenContext(HuffmanTree.Node n, int idParent){
            this.n = n;
            this.idParent = idParent;
        }
    }

}
