/**
 * Finds the minimum integer in array of ints.
 * Uses recursion
 *
 * @author Huw Jones
 * @since 22/02/2016
 */

public class MinInt {

    int[] arr = {24,52,74,9,34,23,64,34};

    public static void main(String[] args){
        MinInt m = new MinInt();
        System.out.println("Minimum is :" + m.findMin());
    }

    public int findMin(){
        return findMinAux(arr.length - 1);
    }

    private int findMinAux(int index){
        if(index == 0) {
            return arr[index];
        }
        int minRes = findMinAux(index - 1);
        return minRes < arr[index] ? minRes: arr[index];
    }
}
