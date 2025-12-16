package structure;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class HuffmanTree {
    private Node root;
    private final Leaf carSpecial = new Leaf(); // C'est le NYT
    private HashMap<String, Leaf> cars = new HashMap<>();
    private ArrayList<Node> gdbh = new ArrayList<>();

    /**
     * Numérote l'arbre enraciné en {@code root} selon le parcours GDBH et initialise la qui stock
     * le parcours.
     * @param root racine de l'arbre à numéroté
     */
    public void numAHAsetGDBH(Node root) {
        gdbh.clear();
        int nbNodes = 0;
        LinkedList<Node> queue = new LinkedList<>();
        Stack<Node> stack = new Stack<>();
        queue.add(root);

        while (!queue.isEmpty()) {
            Node n = queue.removeFirst();
            nbNodes ++;

            stack.push(n);
            //Invariant P0, si on aumoins un fils on en a forcément 2
            if (n.right != null) {
                queue.add(n.right);
                queue.add(n.left);
            }
        }
        // pour éviter les redimensionement en cours de construction
        gdbh.ensureCapacity(nbNodes);
        int rang = 0;
        while (!stack.isEmpty()) {
            Node n = stack.pop();
            n.rang = rang++;
            gdbh.add(n);
        }
    }


    public HuffmanTree() {
        this.root = this.carSpecial;
        this.root.rang = 0;
        this.gdbh.add(this.root);

    }

    public void modification(String c) {
        // 1 er Cas si l'arbre est vide
        if (root == carSpecial) {
            root = new Node(); //nombreNodes++;
            Leaf newCar = new Leaf(c); //nombreNodes++;
            root.setLeft(carSpecial);
            root.setRight(newCar);
            root.occurence = 1;

            this.numAHAsetGDBH(root);
            //carSpecial.rang = 0; newCar.rang = 1; root.rang = 2;


            cars.put(c, newCar);
        } else {
            Leaf feuille_c = cars.get(c);
            Node Q;

            //(Caractère n'est pas présent dans l'arbre)
            if (feuille_c == null) {
                Q = carSpecial.parent;
                Node interne = new Node();
                Leaf newCar = new Leaf(c);

                interne.setLeft(carSpecial);
                interne.setRight(newCar);
                interne.occurence = 1;

                Q.setLeft(interne);
                numAHAsetGDBH(root);

                cars.put(c, newCar);

            } else {
                Q = feuille_c;

                if (ABR_NYT_CHAR(Q) && Q.parent == finBloc(Q)) {
                    Q.occurence = Q.occurence + 1;
                    Q = Q.parent;
                }
            }
            traitement(Q);
        }
    }

    /**
     * Indique si {@code Q} est la feuille symbole directement associée au NYT au sein du même parent.
     *
     * @param Q nœud à tester
     * @return {@code true} si {@code Q} est le frère droit de NYT sous le même parent, sinon {@code false}
     */
    public boolean ABR_NYT_CHAR(Node Q) {
        Node p = Q.parent;
        if (p == null)
            return false;
        return p.left == carSpecial && p.right == Q;
    }
    /**
     * Met à jour l’arbre depuis un nœud {@code Q} jusqu’à la racine.
     * En préservant à travers les permutation si necessaire les invariants du AHA
     *
     * @param Q nœud de départ (feuille du symbole courant ou nœud interne selon le cas)
     */
    public void traitement(Node Q) {

        while (Q != null) {
            Node m = nodeTobeIncr(Q);
            if (m == null) {
                // Tout le chemin [Q -> racine] a été incrémenté
                return;
            }
            Node b = finBloc(m);
            // important: incrémenter après finbloc pour pas fausser le calcul
            m.occurence = m.occurence + 1;
            permute(m, b);
            Q = m.parent;
        }
    }

    /**
     * Rend le premier noeud qui ne verifie pas l'incrementabilité (m)
     * et incremente sur son chemin tout les descendants strict de m
     * @param Q
     * @return le premier noeud qui ne vérifie pas la condition d'incrémentabilité
     */
    private Node nodeTobeIncr(Node Q) {
        Node cur = Q;
        while (cur != null) {
            if (cur != root) {
                Node succ = this.gdbh.get(cur.rang + 1);
                if (cur.occurence >= succ.occurence) {
                    return cur;
                }
            }
            cur.occurence = cur.occurence + 1;
            cur = cur.parent;
        }
        return null;
    }


    /**
     * Retourne la fin du bloc de poids du nœud {@code Q} dans l’ordre {@link #gdbh}.
     *
     * @param Q nœud dont on veut déterminer la fin de bloc
     * @return le dernier nœud du bloc de poids {@code Q.occurence}, ou {@code null} si non trouvé
     */
    public Node finBloc(Node Q) {
        int w = Q.occurence;
        for (int i = Q.rang; i < gdbh.size() - 1; i++) {
            Node curr = gdbh.get(i);
            Node succ = gdbh.get(i + 1);
            // on reste dans le bloc tant que le poids == w
            if (curr.occurence == w && succ.occurence != w) {
                return curr; // fin du bloc de poids w
            }
        }
        return null;
    }

    public void permute(Node m, Node b) {
        if (b == null || m == null || m == root || b == root) {
            return;
        }

        Node parentB = b.parent;
        Node parentM = m.parent;

        if (parentB == parentM) {
            if (parentB.left == m && parentB.right == b) {
                parentB.setLeft(b);
                parentB.setRight(m);
            } else if (parentB.left == b && parentB.right == m) {
                parentB.setLeft(m);
                parentB.setRight(b);
            }
        } else {
            if (parentB.right == b) {
                parentB.setRight(m);
            } else {
                parentB.setLeft(m);
            }

            if (parentM.right == m) {
                parentM.setRight(b);
            } else {
                parentM.setLeft(b);
            }
        }

        numAHAsetGDBH(root);
    }



    /**
     * Construit le code binaire d’un nœud en remontant jusqu’à la racine.
     *
     * <p> gauche = {@code 0}, droite = {@code 1}.</p>
     *
     * @param n nœud dont on veut le code
     * @return chaîne de bits représentant le chemin racine → nœud
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

        public int getOccurence() {
            return occurence;
        }

        public Node getRight() {
            return right;
        }

        public Node getLeft() {
            return left;
        }
    }

    public class Leaf extends Node {

        private final String caractere;

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