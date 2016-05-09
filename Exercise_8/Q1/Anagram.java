import java.util.*;

public class Anagram {

    private char[] input;
    private StringBuffer sb = new StringBuffer();
    private List output;

    public Anagram(String input){
        this.input = input.toCharArray();
    }

    public List generate(){
        output = new ArrayList();
        doAnagram(input.length);
        return output;
    }

    private void doAnagram(int rsize){
        if (rsize==1){ return; }
        for (int i=0; i < rsize ; i++){
            doAnagram(rsize-1);
            if (rsize==2){
                for (int j=0; j < input.length; j++){
                    sb.append(input[j]);
                }
                output.add(sb.toString());
                sb.delete(0,input.length+1);
            }
            rotate(rsize);
        }
    }

    private void rotate(int rsize){
        int i;
        int pos = input.length - rsize;
        char tmp = input[pos];
        for (i=pos + 1; i < input.length ; i++){
            input[i-1] = input[i];
        }
        input[i-1] = tmp;
    }
}