package demo.disample.beans;

public class Utils {

    public static String capitalizeWord(String word){
        if (word == null)
            return null;

        if (word.isEmpty())
            return word;

        return word.substring(0, 1).toUpperCase() + word.substring(1);
    }

    public static String insertString(String source, String expr, int pos){
        if (source == null){
            throw new NullPointerException("Source string must not be null");
        }
        if (expr == null){
            throw new NullPointerException("Expression string must not be null");
        }

        if (pos < 0){
            throw new IllegalArgumentException("Position must be non-negative");
        } else if (pos >= source.length()){
            throw new IndexOutOfBoundsException("Position must be less than string length");
        }

        return source.substring(0, pos) + expr + source.substring(pos);
    }
}
