/**
 * {DESCRIPTION}
 *
 * @author Huw Jones
 * @since 22/02/2016
 */

public class MinTree {

    Tree tree = new Tree( 24,
            new Tree( 45,
                    null ,
                    new Tree(8, null , null) ) ,
            new Tree ( 17,
                    new Tree (74 , null , null ) ,
                    null ) );

    public static void main(String[] args){
        MinTree mt = new MinTree();
        System.out.println("Minimum is: " + mt.findMin());
    }

    public int findMin(){
        return findMinAux(this.tree);
    }

    private int findMinAux(Tree tree){
        // Get min value from tree
        int minValue = tree.getVal();

        // Get min value from right tree
        Tree rightTree = tree.right();
        if(rightTree != null){
            int rightMinValue = findMinAux(rightTree);

            // If less than minValue, update minValue to this value
            minValue = (rightMinValue < minValue) ? rightMinValue : minValue;
        }

        // Do the same for the left tree
        Tree leftTree = tree.left();
        if(leftTree != null){
            int leftMinValue = findMinAux(leftTree);
            minValue = (leftMinValue < minValue) ? leftMinValue : minValue;
        }

        // Return the minValue
        return minValue;
    }

}