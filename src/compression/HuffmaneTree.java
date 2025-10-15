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
            newNode.setLeft(this.carSpecial);
            newNode.setRight(newLeaf);
            this.root = newNode;
            newNode.majOcc();
            newNode.majProfondeur();
            newNode.majCode();
        }else{
            Node parentSpecial = this.carSpecial.getParent();
            newNode.setLeft(this.carSpecial);
            newNode.setRight(newLeaf);
            newNode.majOcc(); // Important !! comprend comment

            parentSpecial.setLeft(newNode);
            parentSpecial.majCode();
            parentSpecial.majOcc();
            parentSpecial.majProfondeur();

        }

        // Ajout dans la liste Trié qui représente le parcours GDBH
        gdbh.add(newNode);
        gdbh.add(newLeaf);
    }

    /**
     * Complexité du tailset à analyser
     * @param n
     */
    public void GDBH(Node n){
        for(Node n2 : this.gdbh.tailSet(n)){
            System.out.println(n2);
        }
    }

    /**
     * @pre n1 et n2 existent dans l'arbre
     * @param n1
     * @param n2
     */
    public void permute(Node n1, Node n2){
        Node tmp = n2;
        Node n2Parent = n2.parent;
        Node n1Parent = n1.parent;
        // si le dernier bit du code est 1 alors le noeud et le fils droit de son parent sinon c'est le gauche
        if(n2.code.charAt(n2.code.length()-1) == '1'){
            n2Parent.right = n1;
        }else{
            n2Parent.left = n1;
        }

        if(n1.code.charAt(n1.code.length()-1) == '1'){
            n1Parent.right = tmp;
        }else{
            n1Parent.left = tmp;
        }

        n1.parent = n2Parent;
        n2.parent = n1Parent;

        // Il n'ya pas de redondances dans les maj parceque n1Parent et n2Parent ne sont ancetres l'un de l'autre
        n1Parent.majOcc();
        n2Parent.majOcc();
        n1Parent.majProfondeur();
        n2Parent.majProfondeur();
        n1Parent.majCode();
        n2Parent.majCode();
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

    public TreeMap<String, Leaf> getCars() {
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
            occurence = 0;
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
            if (this.profondeur > o.profondeur){
                return -1;
            }

            if (this.profondeur < o.profondeur){
                return 1;
            }

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
            // Traitement code de huffmane
            // !!!! O(n)
//            this.right.code = this.code.concat("1");
//            // Profondeur
//            this.right.profondeur = this.profondeur+1;
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
         * Récursive! à changer en itérative par la suite pour éviter le stackOverflow
         * met à jour la profondeur de chaque sous noeud en fonction de celle de this
         */
        public void majProfondeur(){
            if (!(this instanceof Leaf)){
                this.left.profondeur = this.profondeur + 1;
                this.right.profondeur = this.profondeur + 1;
                this.left.majProfondeur();
                this.right.majProfondeur();
            }

        }
        /**
         * Récursive! à changer en itérative par la suite pour éviter le stackOverflow
         * met à jour le code de chaque sous noeud en fonction de celle de this
         */
        public void majCode(){
            if (!(this instanceof Leaf)){
                // !!!! O(n)
                this.left.code = this.code.concat("0");
                // !!!! O(n)
                this.right.code = this.code.concat("1");
                this.left.majCode();
                this.right.majCode();
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

	
