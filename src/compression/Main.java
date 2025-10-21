package compression;

public class Main {

    public static void main(String[] args) {
        HuffmaneTree ht = new HuffmaneTree();

        ht.add("a");
        ht.add("a");
        ht.add("a");
        ht.add("a");

        ht.add("b");
        ht.add("b");
        ht.add("c");
        ht.add("d");
        System.out.println("Liste des caracteres présents: \n");
        System.out.println(ht.getCars());

        System.out.println("GDBH: \n");
        ht.GDBH();
        ht.permute(ht.getRoot().getLeft().getLeft(), ht.getRoot().getRight() );
        // Après le permute le a doit etre à gauche noeud qui contient b. La profondeur de l'arbre devient 3
        System.out.println("GDBH: \n");
        ht.GDBH();




    }

}