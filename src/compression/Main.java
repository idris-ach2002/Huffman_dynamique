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

        System.out.println(ht.getCars());
        System.out.println(ht.getCarSpecial());
        ht.GDBH();




    }

}