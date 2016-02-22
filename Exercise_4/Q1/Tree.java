class Tree {

    private int val;
    private Tree left, right;

    public Tree(int val, Tree left, Tree right){
        this.val = val;
        this.left = left;
        this.right = right;
    }

    public int getVal(){
        return val;
    }

    public Tree left(){
        return left;
    }

    public Tree right(){
        return right;
    }
}