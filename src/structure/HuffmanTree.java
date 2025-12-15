package structure;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class HuffmanTree {
    private Node root;
    private final Leaf carSpecial = new Leaf(); // C'est le NYT
    private final HashMap<String, Leaf> cars = new HashMap<>();
    private ArrayList<Node> gdbh = new ArrayList<>();

    public void numAHAsetGDBH(Node root){
        // pour faire de l'effet de bord dans parcoursGDBH de la maniere la plus simple
        AtomicInteger rang  = new AtomicInteger(0);
        int h = hauteur(); // profondeur max de l’arbre
        gdbh.clear();
        for (int niveau = h; niveau >= 0; niveau--) {
            parcoursNiveauGDDBH(root, niveau, rang);
        }

        
    }

    private void parcoursNiveauGDDBH(Node n, int niveau, AtomicInteger i) {
        if (n == null) return;
        if (niveau == 0) {
            // invariant: là ou est inséré n c'est l'indice i passé en param
            n.rang = i.getAndIncrement();
            gdbh.add(n);
        } else {
            parcoursNiveauGDDBH(n.getLeft(), niveau - 1, i);
            parcoursNiveauGDDBH(n.getRight(), niveau - 1, i);
        }
    }

    public HuffmanTree() {
        this.root = this.carSpecial;
        this.root.setRang(0);
        this.gdbh.add(this.root);

    }

    public void modification(String c) {
        // 1 er Cas si l'arbre est vide
        if (root == carSpecial) {
            root = new Node(); //nombreNodes++;
            Leaf newCar = new Leaf(c); //nombreNodes++;
            root.setLeft(carSpecial);
            root.setRight(newCar);
            root.setOccurence(1);

            // gdbh à jour et Aha aussi
            this.numAHAsetGDBH(root);

            cars.put(c, newCar);
        } else {
            Leaf feuille_c = cars.get(c);
            Node Q;

            //(Caractère n'est pas présent dans l'arbre)
            if (feuille_c == null) {
                Q = carSpecial.getParent();
                Node interne = new Node();
                Leaf newCar = new Leaf(c);

                interne.setLeft(carSpecial);
                interne.setRight(newCar);
                interne.setOccurence(1);

                Q.setLeft(interne);

                // gdbh à jour et Aha aussi
                numAHAsetGDBH(root);

                cars.put(c, newCar);

            } else {
                Q = feuille_c;

                if (ABR_NYT_CHAR(Q) && Q.getParent() == finBloc(Q)) {
                    Q.setOccurence(Q.getOccurence() + 1);
                    Q = Q.getParent();
                }
            }
            traitement(Q);
        }
    }

    public boolean ABR_NYT_CHAR(Node Q) {
        Node p = Q.getParent();
        if (p == null)
            return false;
        return p.getLeft() == carSpecial && p.getRight() == Q;
    }

    /**
     * Cette méthode permet de mettre à jour les occurences (Poids) des noeuds de Q
     * jusqu'à la racine tout on veillant sur la correction et la validité de l'AHD
     * obtenu
     *
     * @param Q
     */
    public void traitement(Node Q) {

        List<Node> cheminRacine = directPath(Q);
        Node m = estIncrementable(cheminRacine);

        if (m == null) {
            for (Node noeud : cheminRacine) {
                noeud.setOccurence(noeud.getOccurence() + 1);
            }
        } else {
            // le cheminRacine n'est pas incémentable
            Node b = finBloc(m);
            incrementerChemin(Q, m);
            permute(m, b);
            traitement(m.getParent());
        }

    }

    /**
     * Cette méthode permet de visiter Q jusqu ’à la racine ΓQ = [Q, Qi1, ..., Qik]
     * x_i0 , ..., x_ik = les num des noeuds de Gamma_Q
     */
    public List<Node> directPath(Node Q) {
        List<Node> gamma = new ArrayList<>();

        Node cur = Q;
        while (cur != null) {
            gamma.add(cur);
            cur = cur.getParent();
        }

        return gamma;
    }

    /**
     * Cette Méthode fait deux tâche à la fois d'une part elle permet de savoir si
     * un chemin est incrémentable (null est renvoyé)
     *
     * mais aussi dans le cas réciproque renvoie le premier noeud qui viole la
     * propièté de chemin incrémentable
     */
   public Node estIncrementable(List<Node> path) {

        for (Node sommet : path) {
            if (sommet != root) {
                Node succ = this.gdbh.get(sommet.getRang() + 1);
                if (sommet.getOccurence() >= succ.getOccurence())
                    return sommet;
            }
        }
        return null;
    }


    /**
     * Cette Méthode incrémente les poids des noeuds dans le chemin [ Xq -> Xm] On
     * commence d'abord par construire cette plage
     */
    public void incrementerChemin(Node Q, Node m) {
        Node curr = Q;
        while (curr != m){
            curr.setOccurence(curr.getOccurence() + 1);
            curr = curr.getParent();
        }
        //
        curr.setOccurence(curr.getOccurence() + 1);

    }

    /**
     * soit Q le premier sommet de Γφ qui ne vérifie pas P, et soit b tel que
     * pds(xq) = pds(xq+1) = . . . = pds(xb) et pds(xb) < pds(xb+1) (b est en fin de
     * bloc de Q ) b => c'est le premier Noeud qui vérifie le parcours GDBH en ordre
     * ( < )
     *
     * @param Q      Le neoud qui viole parcours GDBH
     * @return
     */
    public Node finBloc(Node Q) {
        int w = Q.getOccurence();
        for (int i = Q.getRang(); i < gdbh.size() - 1; i++) {
            Node curr = gdbh.get(i);
            Node succ = gdbh.get(i + 1);
            // on reste dans le bloc tant que le poids == w
            if (curr.getOccurence() == w && succ.getOccurence() != w) {
                return curr; // fin du bloc de poids w
            }
        }
        return null;
    }

    /**
     * @pre n1 et n2 existent dans l'arbre !!! modofie les éléments de Set l'ABR
     *      devient incoherent ! Peut etre utilier les seter right et left c'est
     *      mieux
     * @param m
     * @param b
     */
 

    public void permute(Node m, Node b) {
        if (b == null || m == null || m == root || b == root) {
            return;
        }

        Node parentB = b.getParent();
        Node parentM = m.getParent();

        if (parentB == parentM) {
            if (parentB.getLeft() == m && parentB.getRight() == b) {
                parentB.setLeft(b);
                parentB.setRight(m);
            } else if (parentB.getLeft() == b && parentB.getRight() == m) {
                parentB.setLeft(m);
                parentB.setRight(b);
            }
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
        }

        numAHAsetGDBH(root);
    }


    /**
     * Génère le code binaire pour un noeud donné en remontant l'arbre
     */
    public static String getCode(Node n) {
        StringBuilder sb = new StringBuilder();
        Node curr = n;
        while (curr.parent != null) {
            if (curr.parent.left == curr) {
                sb.append("0");
            } else {
                sb.append("1");
            }
            curr = curr.parent;
        }
        return sb.reverse().toString();
    }

    /**
     * Invariant: le NYT est toujours dans la profondeur maximal
     */
    public int hauteur(){
        int h = 0;
        Node curr = carSpecial;
        while(curr != null && curr != root){
            curr = curr.parent;
            h++;
        }
        return h;
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

    public class Node {
        private Node left;
        private Node right;
        protected Node parent;
        protected int occurence;
        // Pour l'ordre dans le gdbh
        protected int rang;

        public Node() {
            occurence = 0;
            left = right = parent = null;
        }

        public void setRight(Node right) {
            right.parent = this;
            this.right = right;
        }

        public void setLeft(Node left) {
            left.parent = this;
            this.left = left;
        }

        protected String toStr() {
            return "occurence: " + this.occurence  + " code: " + "\n";
        }

        @Override
        public String toString() {
            return "Node : " + this.toStr();
        }

        public Node getLeft() {
            return left;
        }

        public Node getRight() {
            return right;
        }

        public Node getParent() {
            return parent;
        }

        public int getOccurence() {
            return occurence;
        }

        public void setOccurence(int occurence) {
            this.occurence = occurence;
        }

        public void setRang(int rang) {
            this.rang = rang;
        }

        public int getRang() {
            return rang;
        }
    }

    public class Leaf extends Node {

        private String caractere;

        public Leaf(String c) {
            this.caractere = c;
            this.occurence = 1;
        }

        public Leaf() {
            this.caractere = "NYT";
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