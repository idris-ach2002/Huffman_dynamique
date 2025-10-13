package ABR;

public class BinaryTree {
	private Node root;
	
	public BinaryTree() {
		root = null;
	}
	
	private Node add(Node r, int _d) {
		if(r == null) {
			return new Node(_d);
		}else if(r.getData() > _d) {
			r.setLeft(add(r.getLeft(), _d));	
		}else {
			r.setRight(add(r.getRight(), _d));	
		}
		return r;
	}
	
	public void add(int _d) {
		root = add(root, _d);
	}
	
	public String affichage(Node r) {
		String res = "";
		if(r != null) {
			res += affichage(r.getLeft());
			res += r.getData() + " -> ";
			res += affichage(r.getRight());
		}
		return res;
	}
	
	public String toString() {
		return affichage(root);
	}
	
	
	 private class Node {
		 private Node left;
		 private Node right;
		 private Node parent;
		 private int data;
		 
		 public Node(int _data) {
			 this.data = _data;
			 left = right = parent = null;
		 }
		 
		 public Node getLeft() {
			 return left;
		 }
		 public void setLeft(Node left) {
			 this.left = left;
		 }
		 public Node getRight() {
			 return right;
		 }
		 public void setRight(Node right) {
			 this.right = right;
		 }
		 public Node getParent() {
			 return parent;
		 }
		 public void setParent(Node parent) {
			 this.parent = parent;
		 }
		 public int getData() {
			 return data;
		 }
		 public void setData(int data) {
			 this.data = data;
		 }
	 }
	 
	 public static void main(String[] args) {
		 BinaryTree tree = new BinaryTree();
		 /*for(int i = 0; i < 10; i++) {
			 tree.add((int)(Math.random() * 20));
		 }*/
		 tree.add(2);
		 tree.add(3);
		 tree.add(1);
		 System.out.println(tree);
	 }
}
