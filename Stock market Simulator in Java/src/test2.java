import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class test2 {
    public static void main(String[] args) {
        //Working fine for Adding stock
        /*String a ="Add scrip: TC S, sector: IT, O:2250, H:2250, L:2250, C:2250\n";

        Pattern p = Pattern.compile("[^:,]*");
        Matcher m = p.matcher(a);
        int i =0;
        String [] ele = new String[24];
        while(m.find()) {
            ele [i]= m.group();
            //System.out.println(m.group() + "  ---"+i);
            i++;

        }
//2, 6, 10 14  18  22
String tick = ele[2];
        String sec = ele[6];*/
        //Adding user
        String b = "Add user: Mimi, funds:1000 holding: {INFY:10, TCS:5, SBI:20}\n";
        String c = "Add user: Jaydeep, funds:15000 holding: None";
        String d = "Add user: Kapil, funds:25000 holding: {SBI:100, M&M:20}";
        String e = "Add user: Nusrat, funds:2000 holding: {INFY:20, M&M:25, SBI:25}";
        String a = "Place order, user: Kapil, type: buy, scrip: M&M, qty:10, rate: 580\n";
        Pattern p = Pattern.compile("[^:,{} ]*");
        Matcher m = p.matcher(b);

        int i=0;
        String [] elements = new String[100];
        while(m.find()) {
            elements[i] =(m.group());
            System.out.println(elements[i] +"---"+i);
            i++;
        }
        int size =i;
        //System.out.println(size);
        if(!(size<16)){
            int checks = (size-16)/5;
            // System.out.println("Cehcks"+checks);

            for (i=0;i<checks;i++){
                int indexOfStock = 16+5*(i);//since after 16 elements, every 5th element is a stock ticker
                String stockName = elements[indexOfStock];
                String quantityString = elements[indexOfStock+2];
                int quantity = Integer.parseInt(quantityString);
               // System.out.println("share "+stockName);
                //System.out.println(stockName+"--"+quantityString);
                //stocksOwned.put(stockName,quantity);
            }
        }
      /*  String name = elements[4];
        String typeOfOrder = elements[8];
        String stockName = elements[12];
        int quantity = Integer.parseInt(elements[16]);
        double rate = Double.parseDouble(elements[20]);*/

       // System.out.print( name+" "+quantity+" "+ typeOfOrder+" "+rate+" "+stockName);
//placing order
        //4,8,12,16,20

//adding user
//3*31//4*36//2-26//1*21
        //b.lenght==31
       // System.out.println(elements[5]+" "+ elements[10]+" "+elements[15]);
        //System.out.println(elements[5]+" "+ elements[10]+" "+elements[16]+"-"+elements[18]);


//if 15=None..don't proceed else go with 16 and so on
//5, 10,   //16,18,21,23,26,28
        //16,21,26 and 18,23128
     /*   String [] c =new String[b.length*2];
        int j=0;
        for(int i=0;i<b.length;i++){
            c[j] =(Arrays.toString(b[i].split(":")));
            j++;
        }

        for(int i=0;i<c.length;i++) {
            System.out.println(c[i]);
        }*/
        }
}
