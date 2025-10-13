package ABR;

import java.util.*;

public class BinaryTree {
    private Node root;
    private ArrayList<Node> gdbh; // pour les indices dans le tableau comme le tas structure générale [Node i (SAG -> 2i+1, SAD- > 2i+2)]
    private Map<String, Node> mapCaractere; // Pour accès rapide aux nœuds

    public BinaryTree() {
        root = null;
        gdbh = new ArrayList<>();
        mapCaractere = new HashMap<>();
    }

    private void add(Node z) {
		//On essaie de trouver le parent de z
		  Node y = null;
		  Node x = root;
		  while (x != null) {
		    y = x;
		    x = z.getOccurence() < x.getOccurence() ? x.getLeft() : x.getRight();
		  }
		  z.setParent(y);
		  
		  if (y == null) { // arbre vide
		    root = z;
		    root.setIndex(0);
		  } else { // on connait le parent de z d'ou l'insertion facile par un test
		    if (z.getOccurence() < y.getOccurence()) { // z sera à gauche de y
		      z.setIndex(2*y.getIndex() + 1);
		      y.setLeft(z); 
		    }else { // z sera à droite de y
			  z.setIndex(2*y.getIndex() + 2);
		      y.setRight(z);
		    }
		    gdbh.set(z.getIndex(), z);
		  }

        // Ajoute le nœud dans la map
        mapCaractere.put(z.getCaractere(), z);

        z.setLeft(null);
        z.setRight(null);
    }
    


    public void add(int _o, String _c) {
        add(new Node(_o, _c));
    }

    public String affichage(Node r) {
        String res = "";
        if (r != null) {
            res += affichage(r.getLeft());
            res += "{" + r.getCaractere() + "," + r.getOccurence() + "} @ index " + r.getIndex() + " -> ";
            res += affichage(r.getRight());
        }
        return res;
    }

    public String toString() {
        return affichage(root);
    }

    public Node getNodeByCaractere(String c) {
        return mapCaractere.get(c);
    }


    // Classe interne Node
    public class Node {
        private Node left;
        private Node right;
        private Node parent;
        private int occurence;
        private String caractere;
        private int index;

        public Node(int _o, String _c) {
            occurence = _o;
            caractere = _c;
            index = 0;
            left = right = parent = null;
        }

        public Node getLeft() { return left; }
        public void setLeft(Node left) { this.left = left; }
        public Node getRight() { return right; }
        public void setRight(Node right) { this.right = right; }
        public Node getParent() { return parent; }
        public void setParent(Node parent) { this.parent = parent; }
        public void setIndex(int i) { index = i; }
        public int getIndex() { return index; }
        public int getOccurence() { return occurence; }
        public void setOccurence(int occurence) { this.occurence = occurence; }
        public String getCaractere() { return caractere; }
        public void setCaractere(String caractere) { this.caractere = caractere; }
    }

    // Méthode de test
    public static void main(String[] args) {
        BinaryTree tree = new BinaryTree();

        // Construction manuelle de l'exemple :
        tree.add(18, "A"); // racine
        tree.add(5, "B");  // gauche de A
        tree.add(34, "C"); // droite de A
        tree.add(27, "D"); // gauche de C
        tree.add(36, "E"); // droite de C

        System.out.println("Arbre:");
        System.out.println(tree);

     
    }
}

	
