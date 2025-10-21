package ABR;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.NavigableSet;
import java.util.SortedSet;
import java.util.TreeSet;

public class HuffmanTreeOptimized {
    private Node root;
    private final Leaf carSpecial = new Leaf(); // C'est le '#'
    
    /**
     * Une collection de donnée sans doublons ordonnée par rapport à la clé 
     * représentée en interne par un TreeSet (ARN) dans la clé est comparable
     * ou on définie son ordre (Ce qu'on a fait pour Node), la valeur c'est la clé elle même
     * identité (Un couple) (key=Valeur, value=Valeur)
     */
    private final TreeSet<Node> gdbh = new TreeSet<>();
    
    /**
     * contient tous les cars qui sont dans l'arbre (recherche en O(1) Table de Hash)
     * Le plus intéressant on a une référence à la feuille de chaque caractère
     * */
    
    private final HashMap<String, Leaf> cars = new HashMap<>();

    public HuffmanTreeOptimized() {
        this.root = this.carSpecial;
        this.root.code = "";
        this.root.profondeur = 0;
    }

    /**
     * Cette méthode permet d'ajouter ou mettre à jour une occurence 
     * d'un caractère dans l'AHD toute on conservant sa validité
     * 
     * @param c caractère qu'on vient de lire
     */
    public void modification(String c) {
    	// 1 er Cas si l'arbre est vide
    	if(root == carSpecial) {
    		/* L'arbre devient un AHD avec un noeud interne (pds 1)
				avec enfant gauche #
				et enfant droit [(C) (pds 1)]
			*/
    		root = new Node(); // nouvel arbre (SAG => '#' et SAD => Leaf(c))
    		Leaf newCar = new Leaf(c);
    		root.setLeft(carSpecial);
    		root.setRight(newCar);
    		root.setOccurence(1);
    		
    		root.majProfondeur(); // Màj Profondeur des fils
    		root.majCode(); // Màj code des fils
    		
    		// Ajout du Noeud interne et La feuille qui représente le caractère dans GDBH
    		gdbh.add(root);
    		gdbh.add(newCar);
    		
    		// Ajout du caractère à la liste des caractères rencontrés
    		cars.put(c, newCar);
    	} else { // L'arbre n'est pas vide
    		
    		 // Parent de ('#')  ou  c'est la feuille(c)
    		 Node Q = null;
    		 // Flag pour savoir si C est présent ou pas
    		 Leaf feuille_c = cars.get(c);
    		 
  	        //Optimisation anticipée on génère dès le début gdbh_Q 
    		 //pour le passer à finbloc et traitement
    		 // sinon on le calcule une fois dans ABR_NYT_CHAR(Q) && Q.getParent() == finBloc(Q)
    		 // et une deuxième fois dans traitement
    		 // Ainsi traitement et finbloc deviennent paramétrées avec gdbh_q
    		 SortedSet<Node> gdbh_q = null;

    		// deux cas se maniffestent
    		// 1 er cas (Caractère n'est pas présent dans l'arbre)
 	        if (feuille_c == null){
 	        	// on cherche le parent de la feuille Spéciale NYT
 	        	Q = carSpecial.getParent();
 	        	
 	        	/*
	 	        	 Remplacer noeud(#) par noeud interne (pds 1)
	 	        	 avec enfant gauche #
					 et enfant droit s (pds 1)
 	        	*/
 	        	Node interne = new Node();
 	        	Leaf newCar = new Leaf(c);
 	        	
 	        	interne.setLeft(carSpecial);
 	        	interne.setRight(newCar);
 	        	interne.setOccurence(1);
 	        	
 	        	Q.setLeft(interne);
 	        	// Le dernier noeud qu'était à jour de (Profondeur + Code) c'est parent
 	    		Q.majProfondeur(); // Màj Profondeur des fils
 	    		Q.majCode(); // Màj code des fils
 	    		
 	    		// Ajout du Noeud interne et La feuille qui représente le caractère dans GDBH
 	    		gdbh.add(interne);
 	    		gdbh.add(newCar);
 	    		
 	    		// Ajout du caractère à la liste des caractères rencontrés
 	    		cars.put(c, newCar);

 	    		gdbh_q = gdbh.tailSet(Q);
 	        	
	        } else {
	        	Q = feuille_c;
 	    		gdbh_q = gdbh.tailSet(Q);
 	    		
	        	if(ABR_NYT_CHAR(Q) && Q.getParent() == finBloc(Q, gdbh_q)) {
	        		System.out.println("Rentée dans le cas rare");
	        		System.out.println("[Cas2] Q => " + Q + " , et Parent(Q) => " + Q.getParent());
	        		Q.setOccurence(Q.getOccurence() + 1);
	        		Q = Q.getParent();
	        		//System.out.println("Après les échanges : ");
	        		//System.out.println("Q => " + feuille_c + " , et Parent(Q) => " + Q);
	        		// On recalcule le nouveau chemin direct car maintenent Q est devenu l'encêtre
	 	    		gdbh_q = gdbh.tailSet(Q);
	        		//System.out.println("Nouveau chemin GDBH pour Q : " + Q);
	        		//System.out.println(gdbh_q);
	        	}
	        }
 	        
 	        // S'assurer de la validité de AHD Après (Insertion | MàJ) d'un caractère
 	        traitement(Q, gdbh_q);
    	}
    }
    
    public boolean ABR_NYT_CHAR(Node Q) {
    	Node p = Q.getParent();
    	if(p == null) return false;
    	return p.getLeft() == carSpecial && p.getRight() == Q;
    }
    
    /**
     * soit Q le premier sommet de Γφ qui ne vérifie pas P, et soit b tel que
		pds(xq) = pds(xq+1) = . . . = pds(xb) et pds(xb) < pds(xb+1)
		(b est en fin de bloc de Q )
		b => c'est le premier Noeud qui vérifie le parcours GDBH en ordre ( < )
     * @param Q Le neoud qui viole parcours GDBH
     *  @param gdbh_q [Xq -> Xracine] tri des Noeuds par Ordre Specialisé de Node
     * @return
     */
    public Node finBloc(Node Q, SortedSet<Node> gdbh_q) {
    	Node res = null;
    	Iterator<Node> it = gdbh_q.iterator();
    	// Se positionner à la bonne place (à cause du cas rare Q != m) Page 59 cours
    	while(it.hasNext() && res != Q) {
    		res = it.next();
    	}
    	
    	while(it.hasNext()) {
    		Node courant = it.next();
    		if(res.getOccurence() < courant.getOccurence()) 
    			return res;
    		else
    			res = courant;
    	}
    	
    	return res;
    }
    
    /**
     * Cette méthode permet de mettre à jour les occurences (Poids) des noeuds
     * de Q jusqu'à la racine tout on veillant sur la correction et la validité
     * de l'AHD obtenu
     * @param Q
     */
    public void traitement(Node Q, SortedSet<Node> gdbh_q) {
    	if(Q == null) return ;
    	// On regarde le chemin direct de Q jusqu ’à la racine
    	// ΓQ = [Q, Qi1, ..., Qik] x_i0 , ..., x_ik = les num des noeuds de Gamma_Q

    	//Si le chemin est incémentable alors On Ajoute 1 à chaque poids sur le chemin de Gamma_Q
    	List<Node> chemin = directPath(Q);
    	
    	Node m = estIncrementable(chemin, gdbh_q);
    	if(m == null) {
    		for(Node noeud : chemin) {
    			noeud.setOccurence(noeud.getOccurence() + 1);
    		}
    	} else { // le chemin n'est pas incémentable
    		Node b = finBloc(m, gdbh_q);
    		
    		// Ajoute 1 à chaque poids du chemin de Q a Q_m
    		incrementerChemin(Q, m, chemin);

    		// Échanger dans H les sous − arbres enracin és en Q_m et Q_b
    		permute(m, b);
    		
    		//On Propage la correction vers le parent pour mettre à jour les occurences (poids)
    		// jusqu'à la racine
    		traitement(m.getParent(), gdbh.tailSet(m.getParent()));
    	}
    	
    }
    
    /**
     * Cette Méthode incrémente les poids des noeuds dans
     * le chemin [ Xq -> Xm] On commence d'abord par construire cette plage
     * */
    public void incrementerChemin(Node Q, Node m, List<Node> path) {
    	if(Q == m) { m.setOccurence(m.getOccurence() + 1); }
    	else {
    		for(Node n : path) {
    			if(n != m) 
    				n.setOccurence(n.getOccurence() + 1);
    			else {
    				m.setOccurence(m.getOccurence() + 1); break;
    			}
    				
    		}
    	}
    }
    
    /**
     * Cette Méthode fait deux tâche à la fois
     * d'une part elle permet de savoir si un chemin est incrémentable (null est renvoyé)
     * 
     * mais aussi dans le cas réciproque renvoie le premier noeud 
     * qui viole la propièté de chemin incrémentable
     * */
    public Node estIncrementable(List<Node> path, SortedSet<Node> gdbh_q) {
    	// un chemin réduit à La racine est incémentable
    	if(path.size() == 1 && path.get(0) == root) return null;
    	
    	for(Node sommet : path) {
    		if(sommet != root) {
    			// à revoir (ou optimiser on garde un poniteur vers le sommet dans gdbh
    			// Sinon le problème 56 dans le cours insertion de r ( chemin incrémentable)
    			// mais on cherche le succ de 2 dans tout le gdbh y'a une occ de 2 donc n'est pas inc
	    		Node succ = successeurNoeudGDBH(sommet, gdbh_q);
	    		if(sommet.getOccurence() >= succ.getOccurence())
	    			return sommet;
    		}
    	}
    	return null;
    }
    
    /**
     * Cette méthode permet de visiter Q jusqu ’à la racine
       ΓQ = [Q, Qi1, ..., Qik] x_i0 , ..., x_ik = les num des noeuds de Gamma_Q
    */
    public List<Node> directPath(Node Q) {
        List<Node> gamma = new ArrayList<>();

        if (Q == null) return gamma;

        gamma.add(Q);

        Node cur = Q.getParent();
        while (cur != null) {
            gamma.add(cur);
            cur = cur.getParent();
        }

        return gamma;
    }

    /** Avant O(m)
     * Maintenant => Complexité O(log(m)) avec m taille de gdbh_q (notre liste de noeuds de parcours)
     * Cette Méthode renseigne le suivant d'un noeud Q dans le parcours Gdbh si Q = X1 
     * alors son successeur sera X2
     * 
     * On utilise NavigableSet<E> qui est une interface introduite dans Java 6 
     * qui étend SortedSet<E> et fournit des méthodes supplémentaires pour 
     * naviguer dans un ensemble trié (set ordonné).
     * */
    public Node successeurNoeudGDBH(Node Q, SortedSet<Node> gdbh_q) {
        if (Q == null || Q == root) return Q;

        return ((NavigableSet<Node>) gdbh_q) 
                .tailSet(Q, false) // false => exclure Q lui-même
                .stream()
                .findFirst()
                .orElse(Q);
    }

    

    /**
     * @pre n1 et n2 existent dans l'arbre
     * !!! modofie les éléments de Set l'ABR devient incoherent
     * ! Peut etre utilier les seter right et left c'est mieux
     * @param m
     * @param b
     */
    public void permute(Node m, Node b){
        if (root == carSpecial) return;



        Node parentB = b.getParent();
        Node parentM = m.getParent();

        if (parentM == null || parentB == null) return;
        
        // 1. Retirer m et b de gdbh avant toute modification
        removeFromGDBH(m);
        removeFromGDBH(b);

        if (parentM == parentB) {
            // Cas où les deux ont le même parent
            if (parentB.getRight() == b) {
                parentB.setRight(m);
                parentB.setLeft(b);
            } else {
                parentB.setRight(b);
                parentB.setLeft(m);
            }
            parentB.majProfondeur();
            parentB.majCode();
        } else {
            if (parentB.getRight() == b) {
                parentB.setRight(m);
            } else {
                parentB.setLeft(m);
            }

            if (parentM.getRight() == m) {
                parentM.setRight(b);
            } else {
                parentM.setLeft(b);
            }

            m.setParent(parentB);
            b.setParent(parentM);

            parentM.majProfondeur();
            parentB.majProfondeur();
            parentM.majCode();
            parentB.majCode();
        }

        // 2. Réinsérer m et b après modification
        addToGDBH(m);
        addToGDBH(b);
    }

    
    /**
     * Recursive sur les fils (pour enlever un noeud avec tous ses successeurs)
     * @param n
     */
    public void removeFromGDBH(Node n){
        if (n != null) {
	        gdbh.remove(n);
	        removeFromGDBH(n.getLeft());
	        removeFromGDBH(n.getRight());
        }    
    }



    /**
     * Recursive sur les fils (pour ajouter un noeud avec tous ses successeurs)
     * @param n
     */
    public void addToGDBH(Node n){
	   	 if (n != null) {
	         gdbh.add(n);
	         addToGDBH(n.getLeft());
	         addToGDBH(n.getRight());
		 }
    }
   
    
    
    private int hauteurAHA(Node r) {
    	if(r == null || (r.getLeft() == null && r.getRight() == null)) {
    		return 0;
    	}else {
    		return 1 + Math.max(
	    				hauteurAHA(r.getLeft()),
	    				hauteurAHA(r.getRight())
    				);
    	}
    }
    
    public int hauteurAHA() {
    	return hauteurAHA(root);
    }
    
    
   /**
    * Cette méthode est implémantée pour des raison expérimentales
    * c'est exactement celle d'avant ou on passe notre Collection ordonnée de feuille
    * selon GDBH
    * @param Q
    * @param gdbh
    * @return
    */
    private Node finBloc2(Node Q, TreeSet<Node> gdbh) {
    	Node res = Q;
    	// tri des Noeuds par Ordre Specialisé
    	SortedSet<Node> tri = gdbh.tailSet(Q); //[Xq -> Xracine]
    	Iterator<Node> it = tri.iterator();
    	
    	while(it.hasNext()) {
    		Node courant = it.next();
    		if(res.getOccurence() < courant.getOccurence()) 
    			return res;
    		else
    			res = courant;
    	}
    	
    	return res;
    }
    
    /**
     * Test de la validité de GDBH (Selon notre ordre de comparaison de Node)
     *  et fin bloc et successeurNoeudGDBH
     *  
     *             profondeur = 0
              [n1] (5)
              "" | occ=3

         /                   \
profondeur = 1       profondeur = 1
 [n3] (2)               [n2] (7)
  "0" | occ=1           "1" | occ=1

                            \
                         profondeur = 2
                             [n4] (6)
                             "10" | occ=1

Résultat attendu 

[
  n4 (6) : code="10", profondeur=2, occ=1,
  n3 (2) : code="0",  profondeur=1, occ=1,
  n2 (7) : code="1",  profondeur=1, occ=1,
  n1 (5) : code="",   profondeur=0, occ=3
]

et fin bloc de 6 c'est 7 avec le code 1
     * */
    public void test() {
    	Node n1 = new Node(); //5
    	n1.profondeur = 0;
    	n1.code = "";
    	n1.occurence = 3;

    	Node n2 = new Node(); // 7
    	n2.profondeur = 1;
    	n2.code = "1";
    	n2.occurence = 1;

    	Node n3 = new Node(); // 2
    	n3.profondeur = 1;
    	n3.code = "0";
    	n3.occurence = 1;


    	Node n4 = new Node(); // 6
    	n4.profondeur = 2;
    	n4.code = "10";
    	n4.occurence = 1;

    	TreeSet<Node> g = new TreeSet<>();
    	g.add(n1);
    	g.add(n2);
    	g.add(n3);
    	g.add(n4);
    	

    	SortedSet<Node> result = g.tailSet(n4);
    	System.out.println("tailset de 6 => 10");
    	System.out.println(result);

    	System.out.println("finbloc de 6 => 10");
    	System.out.println(finBloc2(n4, g));
    	System.out.println("successeur de 2 dans GDBH");

    	System.out.println(successeurNoeudGDBH(n3, g.tailSet(n4)));
    }
    
    
    
    
    public void afficherArbre() {
        afficherArbreRecur(root, "", true);
    }

    private void afficherArbreRecur(Node node, String prefix, boolean isTail) {
        if (node == null) return;

        System.out.println(prefix + (isTail ? "└── " : "├── ") + formatNode(node));

        if (node.getLeft() != null || node.getRight() != null) {
            if (node.getRight() != null)
                afficherArbreRecur(node.getRight(), prefix + (isTail ? "    " : "│   "), false);
            if (node.getLeft() != null)
                afficherArbreRecur(node.getLeft(), prefix + (isTail ? "    " : "│   "), true);
        }
    }

    private String formatNode(Node node) {
        if (node instanceof Leaf) {
            Leaf leaf = (Leaf) node;
            return "Leaf[" + leaf.getCaractere() + "] (occ=" + node.getOccurence()
                    + ", prof=" + node.getProfondeur()
                    + ", code=" + node.code + ")";
        } else {
            return "Node (occ=" + node.getOccurence()
                    + ", prof=" + node.getProfondeur()
                    + ", code=" + node.code + ")";
        }
    }

    
    

    /**
     * Complexité du tailset à analyser
     * @param n
     */
    public void GDBHfrom(Node n){
        for(Node n2 : this.gdbh.tailSet(n)){
            System.out.println(n2);
        }
    }

    public void GDBH(){
        for(Node n : this.gdbh){
            System.out.println(n);
        }
    }





    /**
     * Renvoi le noeud corresepend au chemin tracin par le mot binaire c
     * @pre c est correct
     * @param code
     * @return
     */
    public Node getNodebyCode(String code){
        int len = code.length();
        Node res = this.root;

        for (int i =0; i<len; i++){
            if(code.charAt(i) == '0'){
                res = res.left;
            }else{
                // égale 1 forcément (@pre)
                res = res.right;
            }
        }

        return res;

    }

    public HashMap<String, Leaf> getCars() {
        return cars;
    }

    public Leaf getCarSpecial() {
        return carSpecial;
    }

    public Node getRoot() {
        return root;
    }

    /**
     * Invariants:
     * -1 La somme des occurences de left et right est égale à celle de this
     * -2 Par construction, Tout noeud interne possède forcément exactement 2 (fils),
     * c'est à dire on peut pas avoir this avec avec left ou right qui est null
     */
    public class Node implements Comparable<Node> {
        private Node left;
        private Node right;
        protected Node parent;
        protected int occurence;
        protected int profondeur;
        protected String code; // la suite de 0 ou de 1 menant à ce noeud

		public Node() {
            occurence = profondeur = 0;
            this.code = "";
            left = right = parent = null;
        }

        /**
         * La comparaision qui va nous permettre de mentenir une liste trié qui représente le parcour GDBH
         * @param o the object to be compared.
         * @return
         */
        @Override
        public int compareTo(Node o) {
        	/*
        	 * 
        	 Si la profondeur du noeud qu'on veut insérer est plus grande que celle du noeud
        	 courant donc il se code avec plus de car (il apprait moins) 
        	 on le considère plus petit en terme d'occurence plus grand en codage
        	 */
        	if(this.profondeur != o.profondeur)
        		return Integer.compare(o.profondeur, profondeur);

            return this.code.compareTo(o.code);
        }

        /**
         * Attention! ne met pas à jour les occurence, profondeur et codes.
         * Utilisez mej...() après
         * parceque l'invariant 2 n'est pas forcément préserver après cette apelle
         * @param right
         */
        public void setRight(Node right) {
            right.parent = this;
            this.right = right;
        }

        /**
         * Attention! ne met pas à jour les occurence, profondeur et codes.
         * Utilisez mej...() après
         * @param left
         */
        public void setLeft(Node left) {
            left.parent = this;
            this.left = left;
        }


        /**
         * Met à jour la profondeur de chaque nœud de l’arbre binaire à partir du nœud courant.
         * 
         * La profondeur d’un nœud est définie comme sa distance par rapport à la racine (nœud de départ).
         * Utilise un parcours en profondeur (DFS) itératif à l’aide d’une pile explicite,
         * ce qui évite les risques de StackOverflowError qu'on aurait avec une récursion profonde.
         * 
         * Les feuilles (instances de {@code Leaf}) ne sont pas explorées, car elles n’ont pas d’enfants.
         */
        public void majProfondeur() {
            Deque<Node> stack = new ArrayDeque<>();

            stack.push(this); // Démarrage depuis le nœud courant (racine)

            while (!stack.isEmpty()) {
                Node current = stack.pop();
                if (!(current instanceof Leaf)) {
                    if (current.getLeft() != null) {
                        current.left.profondeur = current.profondeur + 1;
                        stack.push(current.left);
                    }
                    if (current.getRight() != null) {
                        current.right.profondeur = current.profondeur + 1;
                        stack.push(current.right);
                    }
                }
            }
        }



        /**
         * Attribue un code binaire à chaque nœud de l’arbre binaire, en partant du nœud courant.
         * 
         * Le code est construit en ajoutant "0" pour un enfant gauche, et "1" pour un enfant droit.
         * Cette méthode est typiquement utilisée pour générer des codes de Huffman.
         * 
         * Utilise un parcours en profondeur (DFS) itératif avec une pile explicite,
         * ce qui permet de gérer de grands arbres sans risque de StackOverflowError.
         * 
         * Les feuilles ne sont pas explorées (elles conservent leur code assigné depuis leur parent).
         */
        public void majCode() {
            Deque<Node> stack = new ArrayDeque<>();

            stack.push(this); // Démarrage depuis le nœud courant (racine)

            while (!stack.isEmpty()) {
                Node current = stack.pop();
                if (!(current instanceof Leaf)) {
                    if (current.getLeft() != null) {
                        current.left.code = current.code + "0";
                        stack.push(current.left);
                    }
                    if (current.getRight() != null) {
                        current.right.code = current.code + "1";
                        stack.push(current.right);
                    }
                }
            }
        }



        protected String toStr() {
            return "occurence: " + this.occurence
                    + " profondeur: " + this.profondeur
                    + " code: " + this.code + "\n";
        }

        @Override
        public String toString() {
            return "Node : " + this.toStr();
        }

        public Node getLeft() { return left; }
        public Node getRight() { return right; }
        public Node getParent() { return parent; }
        public void setParent(Node parent) { this.parent = parent; }
        public int getOccurence() { return occurence; }
        public void setOccurence(int occurence) { this.occurence = occurence; }
        public int getProfondeur() {return profondeur;}
    }

    public class Leaf extends Node{

        private String caractere;

        public Leaf(String c){
            this.caractere = c;
            this.occurence = 1;
        }

        public Leaf(){
            this.caractere = "#";
            this.occurence = 0;
        }


        @Override
        public String toString() {
            return "Leaf: caractere: " + this.caractere + " " + super.toStr();
        }

        public String getCaractere() {
            return caractere;
        }
    }
    


    public static void construireAHA(String file) {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            HuffmanTreeOptimized AHA = new HuffmanTreeOptimized();
            String line;
            while ((line = br.readLine()) != null) {
                for(int i = 0; i < line.length(); i++) {
             	   AHA.modification(line.charAt(i) + "");
                }             
            }
            
     	   AHA.afficherArbre();
    	   System.out.println("Hauteur => " + AHA.hauteurAHA());
        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }

    
    public static void main(String[] args) {
    	construireAHA(Paths.get("src/resources/livre.txt").toAbsolutePath().toString());
    	System.out.println("Path du Fichier => "+ Paths.get("src/resources/livre.txt").toAbsolutePath().toString());
    }


}