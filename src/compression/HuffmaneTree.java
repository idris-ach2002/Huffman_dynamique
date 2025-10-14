package compression;

import java.util.*;

public class HuffmaneTree {
    private Node root;
    private Leaf carSpecial; // C'est le '#'
    private TreeSet<Node> gdbh = new TreeSet<>();
    // contient tous les cars qui sont dans l'arbre (recherche en log)
    private TreeMap<String, Leaf> cars = new TreeMap<>();

    public HuffmaneTree() {
        this.carSpecial = new Leaf();
        this.root = this.carSpecial;
        this.root.code = "";
        this.root.profondeur = 0;
    }

    /**
     * Ce add ne respecte les algos donné en cours pour respecter le AHA
     * on l'utilise pour tester le parcours gdbh
     */
    public void add(String car){
        Leaf n = cars.get(car);
        if (n != null){
            n.incrOcc();
            return;
        }
        Leaf newLeaf = new Leaf(car);
        cars.put(car,newLeaf);
        Node newNode = new Node(); // le noeud qui va contenir à gauche le "# " et à droite le nouveau car

        //Comparaison d'adresses!, la comapraison est vrai dans le cas de l'arbre vide: #
        if(this.carSpecial == this.root){
            newNode.code = "";
            this.root = newNode;
        }else{
            Node parentSpecial = this.carSpecial.getParent();
            parentSpecial.setLeft(newNode);
        }
        // Important de laisser le set après le if comme ça le newNode.code est initialisé
        // et donc il pourra déterminer à son tour le code de ses enfants
        newNode.setLeft(this.carSpecial);
        newNode.setRight(newLeaf);

        newNode.majOcc();

        // Ajout dans la liste Trié qui représente le parcours GDBH
        gdbh.add(newNode);
        gdbh.add(newLeaf);
    }

    public void GDBH(){
        for (Node n: this.gdbh){
            System.out.println(n);
        }
    }

    public TreeMap<String, Leaf> getCars() {
        return cars;
    }

    public Leaf getCarSpecial() {
        return carSpecial;
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
            occurence = 0;
            left = right = parent = null;
        }

        /**
         * La comparaision qui va nous permettre de mentenir une liste trié qui représente le parcour GDBH
         * @param o the object to be compared.
         * @return
         */
        @Override
        public int compareTo(Node o) {
            if (this.profondeur > o.profondeur){
                return -1;
            }

            if (this.profondeur < o.profondeur){
                return 1;
            }

            return this.code.compareTo(o.code);
        }

        /**
         * Attention! ne met pas à jour les occurence.
         * Utilisez majOcc() après
         * @param right
         */
        public void setRight(Node right) {
            right.parent = this;
            this.right = right;
            // Traitement code de huffmane
            // !!!! O(n)
            this.right.code = this.code.concat("1");
            // Profondeur
            this.right.profondeur = this.profondeur+1;
        }

        /**
         * Attention! ne met pas à jour les occurence.
         * Utilisez majOcc() après
         * @param left
         */
        public void setLeft(Node left) {
            left.parent = this;
            this.left = left;
            // Traitement code de huffmane
            // !!!! O(n)
            this.left.code = this.code.concat("0");
            // Profondeur
            this.left.profondeur = this.profondeur+1;

        }

        /**
         * Met à jours les occurence à partir de this à la racine
         * On profite de l'invariant 2 qui nous permet de ne pas tester si left ou roght sont null
         */
        public void majOcc(){
            Node curr = this;
            while (curr != null){
                curr.occurence = curr.left.occurence + curr.right.occurence;
                curr = curr.parent;
            }
        }

        /**
         * Recursive, propage l'incrément jusqu'à la racine, encore une fois c'est pas le comprtement attendu
         * de l'algorithme de Huffmane c'est pour tester le parcours GDBH
         */
        public void incrOcc(){
            this.occurence++;
            if (this.parent != null){
                this.parent.incrOcc();
            }
        }

        protected String toStr() {
            return "occurence: " + this.occurence
                    + " profondeur: " + this.profondeur
                    + " code: " + this.code;
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


}

	
