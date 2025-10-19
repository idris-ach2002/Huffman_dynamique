package huffman;

import java.util.*;

public class HuffmanTree {
    private Node root;
    private Node NYT;
    private Map<Character, Node> symbolTable;
    private int maxOrder;

    public HuffmanTree() {
        this.NYT = new Node('#', 0, 512);
        this.root = NYT;
        this.symbolTable = new HashMap<>();
        this.maxOrder = 512;
    }

    /** Vérifie si le symbole est déjà connu */
    public boolean contains(char c) {
        // TODO
        return false;
    }

    /** Renvoie le code binaire d’un symbole ou du NYT */
    public String getCode(char c) {
        // TODO
        return "";
    }

    /** Renvoie le chemin binaire d’un nœud vers la racine */
    private String getPathToRoot(Node n) {
        // TODO
        return "";
    }

    /** Met à jour l’arbre après lecture d’un symbole */
    public void update(char c) {
        // TODO
    }

    /** Trouve le dernier nœud d’un bloc de poids égal */
    private Node findBlockEnd(Node n) {
        // TODO : implémenter l’équivalent de finBloc(H, m)
        return n;
    }

    /** Échange deux nœuds (ordre, poids, etc.) */
    private void swap(Node a, Node b) {
        // TODO
    }

    public Node getRoot() { return root; }

    public Node getNYT() { return NYT; }
}
