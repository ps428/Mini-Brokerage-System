
class MyException extends Exception{
    String str1;
    /* Constructor of custom exception class
     * here I am copying the message that we are passing while
     * throwing the exception to a string and then displaying
     * that string along with the message.
     */
    MyException(String str2) {
        str1=str2;
    }
    public String toString(){
        return ("MyException Occurred: "+str1) ;
    }
}

public class ExceptionTesting {
    public static void main(String[] args) {
        try {
            System.out.println("Starting of try block");
            // I'm throwing the custom exception using throw
            throw new MyException("This is My error Message");
        } catch (MyException exp) {
            System.out.println("Catch Block");
            System.out.println(exp);
        }
        System.out.println("fdafsdfasd");
    }
}