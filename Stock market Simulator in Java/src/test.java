import java.io.FileNotFoundException;
import java.util.HashMap;

public class test {
    public static void main(String[] args) throws FileNotFoundException {
        readtx("/home/pranav/Desktop/Github/Java/GLA 2/src/INFY_15days_data.csv");

    }

    public static void readtx(String location) throws FileNotFoundException {

        HashMap<String, Integer> news = new HashMap<>();
        news.put("a",1);
        news.put("b",2);
        news.put("c",3);
        news.put("d",4);
        news.put("e",5);

        System.out.println(news.get("a"));
//0,



    }
}
