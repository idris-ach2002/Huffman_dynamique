package utils;

import structure.HuffmanTree;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Stack;

public class DotGenerator {

    public static final String nodeAttSuffix = " shape=\"square\" style=\"filled\" width=0.2 height=0.2]";
    /**
     * génère un fichier dst.dot représentant l'AHA enraciné en root
     * @param root
     * @param dst
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
                    bw.write(id + constructLabel(curr.n.getCode(), curr.n.getOccurence(), curr.n.getProfondeur(), null) + nodeAttSuffix + ";\n");
                    stack.push(new GenContext(curr.n.getRight(), id));
                    stack.push(new GenContext(curr.n.getLeft(), id));
                }else{
                    bw.write(id + constructLabel(curr.n.getCode(), curr.n.getOccurence(), curr.n.getProfondeur(),((HuffmanTree.Leaf) curr.n).getCaractere()) + nodeAttSuffix + ";\n");
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
    private static String constructLabel(String code, int occ, int prof, String car){
        if (car == null)
            return "[label=\" occ = " + occ + "\\n" +
                    "code = " + code + "\\n" +
                    "profondeur = " + prof + " \" ";
        else
            return "[label=\" occ = " + occ + "\\n" +
                "code = " + code + "\\n" +
                "profondeur = " + prof + "\\n \\" +
                    car + " \" ";
    }
    /**
     * Représente le contexte local à un noeud, pendant la récursion
     */
    static class GenContext {
        HuffmanTree.Node n;
        int idParent;

        GenContext(HuffmanTree.Node n, int idParent){
            this.n = n;
            this.idParent = idParent;
        }
    }

}
