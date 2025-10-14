package ABR;

import java.util.*;

public class BinaryTree {
    private Node root;
    private ArrayList<Node> parcours; // pour les indices dans le tableau comme le tas structure générale [Node i (SAG -> 2i+1, SAD- > 2i+2)]
    private Map<String, Node> mapCaractere; // Pour accès rapide aux nœuds

    public BinaryTree() {
        root = null;
        parcours = new ArrayList<>();
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
		    //root.setIndex(0); //pas nécessaire fais au niveau du constructeur
		  } else { // on connait le parent de z d'ou l'insertion facile par un test
		    if (z.getOccurence() < y.getOccurence()) { // z sera à gauche de y
		      z.setIndex(2*y.getIndex() + 1);
		      y.setLeft(z); 
		    }else { // z sera à droite de y
			  z.setIndex(2*y.getIndex() + 2);
		      y.setRight(z);
		    }
		    z.setNiveau(y.getNiveau() + 1);
		  }
		  
		  
		  ensureSize(z.getIndex());
		  parcours.set(z.getIndex(), z);

        // Ajoute le nœud dans la map
        mapCaractere.put(z.getCaractere(), z);

        z.setLeft(null);
        z.setRight(null);
    }
    
    // pour l'ajout dynamique sinon on risque de déborder
    private void ensureSize(int index) {
        while (parcours.size() <= index) {
            parcours.add(null); // Remplissage avec des "trous"
        }
    }
    


    public void add(int _o, String _c) {
        add(new Node(_o, _c));
    }
    
    public List<Node> GDBH(Node x) {
    	//Stockage du résultat 
    	List<Node> res = new ArrayList<>();
    	//on commence immédaitement par ajouter x lui même
    	res.add(x);
    	
    	
    	// ******************cas particulier ****************** 
    	//on commence par X et n'on pas toujours par le noeud le plus à gauche de l'arbre
    	
    	
    	// On commence par obtenir le niveau et l'indice de x
    	int level = x.getNiveau();
    	int indice = x.getIndex();
    	
    	// on a ajouté x d'ou on commence par indice + 1
    	for(int i = indice + 1; i <= Math.pow(2, level + 1) - 2; i++) {
    		if(i >= parcours.size())
    			break;
    		if(parcours.get(i) != null)
    			res.add(parcours.get(i));
    	}
    	
    	//passage au noeud le plus à gauche => un niveau de plus en haut par rapport à x
    	level--;
    	
    	while(level > 0) {
        	indice = (int)Math.pow(2, level) - 1;
        	
        	// on a ajouté x d'ou on commence par indice + 1
        	for(int i = indice; i <= Math.pow(2, level + 1) - 2; i++) {
        		if(i >= parcours.size())
        			break;
        		if(parcours.get(i) != null)
        			res.add(parcours.get(i));
        	}
        	// on a fini sur le niveau level on passe au niveau supérieur
        	level--;
    	}
    	
    	//ajoute la racine
    	res.add(parcours.get(0));
    	
    	return res;
    }

    public String affichage(Node r) {
        String res = "";
        if (r != null) {
            res += affichage(r.getLeft());
            res += "[ {" + r.getCaractere() + "," + r.getOccurence() + "} @ index " + r.getIndex() + " Niveau " + r.getNiveau() + " ] -> ";
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
        private int niveau;

		public Node(int _o, String _c) {
            occurence = _o;
            caractere = _c;
            index = 0;
            niveau = 0;
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
        public int getNiveau() {return niveau;}
		public void setNiveau(int niveau) {this.niveau = niveau;}
    }

    // Méthode de test
    public static void main(String[] args) {
        BinaryTree tree = new BinaryTree();

        tree.add(18, "A");  // racine
        tree.add(5, "B");
        tree.add(34, "C");
        tree.add(27, "D");
        tree.add(36, "E");
        tree.add(3, "Z");
        tree.add(6, "R");

        System.out.println("=== Affichage de l'arbre (In-Order) ===");
        System.out.println(tree);    

        // On récupère le noeud "Z" et fait un test de GDBH
        BinaryTree.Node nodeZ = tree.getNodeByCaractere("D");

        if (nodeZ != null) {
            List<BinaryTree.Node> gdbhResult = tree.GDBH(nodeZ);
            System.out.println("\n=== Résultat du parcours GDBH à partir de Z ===");
            for (BinaryTree.Node n : gdbhResult) {
                System.out.println("{" + n.getCaractere() + ", " + n.getOccurence() +
                        "} @ index " + n.getIndex() + ", niveau " + n.getNiveau());
            }
        } else {
            System.out.println("Nœud Z non trouvé !");
        }
    }

}

	
