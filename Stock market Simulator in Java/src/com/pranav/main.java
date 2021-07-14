
package com.pranav;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class main {
    static HashMap<String, StockTrading> hashMapStocks = new HashMap<>();//Hash Map of all stocks
    static int noOfStocks = 0;
    static StockTrading [] stockTradingArray = new StockTrading[30];
    //static ArrayList<StockTrading> stc = new ArrayList<StockTrading>();

    static HashMap<String, User> hashMapUsers = new HashMap<>();
    static int noOfUsers = 0;
    static User [] userArray = new User[30];

    static HashMap<Integer, Transaction> orderBook = new HashMap<>();
    static int noOfTransactions = 0;
    static Transaction [] transactionArray = new Transaction[30];

    static HashMap<Integer, Transaction> shortSellingOrderBook = new HashMap<>();
    static int noOFShortSellers = 0;
    static Transaction [] shortSellers = new Transaction[30];

    static ArrayList<CSVReader> csvReaderArray= new ArrayList<CSVReader>();
    static int noOfCSVReads = 0;


    public static void main(String[] args) throws FileNotFoundException, LCViolation, LowFunds, UCViolation {
        System.out.println("-------------------------------------------\n-------STOCK MARKET MANAGEMENT SYSTEM------\n-------------------------------------------\n");

       readTxt("/home/pranav/Desktop/Github/Java/GLA 2/src/sample_input.txt");
       readCSV("/home/pranav/Downloads/WIPRO_15days_data.csv");

        // showShortSellers();

      // showOrderBook("Show OrderBook\n");

        //uncomment this to see all the stocks of same sector
       //listAllStocksSectorWise("IT");

        //Uncomment this to see all the stocks
       // printAllStocks();

        //uncomment this to print all users
       // printAllUsers();
    }

    public static void readTxt(String location) throws FileNotFoundException, LowFunds, LCViolation, UCViolation {
        //TAKING INPUT txt file
        try {
            File txt = new File(location);
            Scanner scan = new Scanner(txt);
            //System.out.println("Reading the file now.");
            while (scan.hasNextLine()) {
                String line = scan.nextLine();
                //System.out.println(line);

                checkForStockAddition(line);//to check for stock addition
                checkForUserAddition(line);//to check id new user is there to be added
                stockDeletion(line);
                userDeletion(line);
                stockSectorWise(line);
                placeOrders(line);//need to be completed
                showOrderBook(line);

//TO-DO add execute commands: DONE
                if(line.matches("Execut.*")){
                    executeTransactions();
                    System.out.println("\n*****Now since the transactions are over and day is closed, the short sellers who ere unable to sell the stocks borrowed will have to pay the penalty\n*****If there are any short seller, they will be listed below.");

                    for (Iterator<Integer> keys = orderBook.keySet().iterator(); keys.hasNext();) {///loop over order book
                        Integer key = keys.next();

                        //showOrderBook("Show Orderbook");
                        if(orderBook.get(key).openOrNot==true)
                        if (!(hashMapUsers.get(orderBook.get(key).name.name).stocksOwned.isEmpty())) {
                            for (String stockName : hashMapUsers.get(orderBook.get(key).name.name).stocksOwned.keySet()) {//loop over user's owned stocks
                                if (hashMapUsers.get(orderBook.get(key).name.name).stocksOwned.get(stockName) < 0) {
                                    //user still has negative quantity of a stock so he will have to pay the penalty
                                    System.out.println("User: " + hashMapUsers.get(orderBook.get(key).name.name).name + " was unable to short sell, they will have to pay the penalty of RS 200,000 to the bank.");
                                   // hashMapUsers.get(orderBook.get(key).name.name).funds = hashMapUsers.get(orderBook.get(key).name.name).funds - 200000;
                                    //hashMapUsers.get(orderBook.get(key).name.name).stocksOwned.remove(stockName);
                                    //System.out.println("New status of user " + hashMapUsers.get(orderBook.get(key).name.name).name + " is:");
                                    //hashMapUsers.get(orderBook.get(key).name.name).print();
                                }
                            }
                        }
                    }

                    System.out.println("\n---------------End of defaulters----------\n");
                }

                if(line.matches("Show Scrips.*")){
                    System.out.print("\n----------\nAll stocks are:");
                    printAllStocks();
                }
                if(line.matches("Show Users.*")){
                    System.out.print("\n----------\nAll user are:");
                    printAllUsers();
                }
                if(line.matches("EXIT.*")){
                  //  return;
                    System.out.println("Completed reading the .txt file");
                }
            }
        }

        catch (FileNotFoundException notFound){
            System.out.println("File not found.");
            throw new FileNotFoundException("Please Enter a valid file address");
        }
        catch ( LowFunds user){
            throw new LowFunds("Error low funds");
        }
        catch (LCViolation user){
            throw new LCViolation("ERROR: LC Violation");
        }
        catch (UCViolation ucViolation) {
            throw new UCViolation("ERROR: UC Violation");
        }
    }

    public static void stockSectorWise(String line){
        if(line.matches("Show sector:.*")){
            String [] sectorIs = line.split(":");
            listAllStocksSectorWise(sectorIs[1].strip());
        }
    }

    public static void stockDeletion(String line){
        if(line.matches("Delete scrip:.*")){
            String [] toBeDeleted = line.split(":");
            deleteStockInMain(toBeDeleted[1].strip());
        }
    }
    public static void userDeletion(String line){
        if(line.matches("Delete User:.*")){
            String [] toBeDeleted = line.split(":");
            deleteUserInMain(toBeDeleted[1].strip());
        }
    }
    public static void checkForStockAddition(String a){//to check if the text inputted wants to add a stock
        if(!a.matches("^Add scrip:.*"))//to check for starting of the inputted text
            return;
       // System.out.println("Adding Stock.");

        int [] semi = new int[6];//an array to store indices of all semicolons
        int [] coma = new int [6];//an array to store indices of all comas
        double latestTradingPrice;//
        double openPrice;//
        double closePrice;//
        double highPrice;//

//Making some string buffers to store the characters that would be iterated over
        StringBuffer tickerBuff = new StringBuffer();
        StringBuffer sectorBuff = new StringBuffer();
        StringBuffer latestTradingPriceBuff = new StringBuffer();
        StringBuffer openPriceBuff = new StringBuffer();
        StringBuffer closePriceBuff = new StringBuffer();
        StringBuffer highPriceBuff = new StringBuffer();

        int i=0,j=0;
        int len = 0;

        while (len<a.length()) {//getting coma and semicolon location
            if(a.charAt(len)==',') {
                coma[j] = len;
                j++;
            }
            if(a.charAt(len)==':') {
                semi[i] = len;
                i++;
            }
            len++;
        }

        for( i=0; i<6;i++) {
            if(i==0){//adding ticker values to string buffer of ticker
                for ( j= semi[i]+2;j<coma[i];j++) {
                    tickerBuff.append((a.charAt(j)));
                }
            }
            if(i==1){//adding sector values to string buffer of sector
                for ( j= semi[i]+2;j<coma[i];j++) {
                    sectorBuff.append((a.charAt(j)));
                }
            }
            if(i==2)//adding openPrice values to string buffer of openPrice
                for ( j= semi[i]+1;j<coma[i];j++) {
                    openPriceBuff.append(a.charAt(j));
                }
            if(i==3)//adding highPrice values to string buffer of highPrice
                for ( j= semi[i]+1;j<coma[i];j++) {
                    highPriceBuff.append(a.charAt(j));
                }
            if(i==4)//adding latestTradingPrice values to string buffer of latestTradingPrice
                for ( j= semi[i]+1;j<coma[i];j++) {
                    latestTradingPriceBuff.append(a.charAt(j));
                }
            if(i==5)//for last entry from the string as there is no comma at end so going till end
                for ( j= semi[i]+1;j<a.length();j++) {
                    closePriceBuff.append(a.charAt(j));
                }
        }

        //Getting the values from string buffers
        openPrice = Double.parseDouble(openPriceBuff.toString());
        highPrice = Double.parseDouble(highPriceBuff.toString());
        latestTradingPrice = Double.parseDouble(latestTradingPriceBuff.toString());
        closePrice = Double.parseDouble(closePriceBuff.toString());

        addToStocks(tickerBuff.toString(), sectorBuff.toString(), openPrice, highPrice, latestTradingPrice, closePrice);

    }

    public  static void addToStocks(String ticker, String sector, double openingPrice, double highPrice, double latestTradingPrice, double closingPrice){
        if (hashMapStocks.containsKey(ticker))
        {
            System.out.println("Key: " + ticker +" already exists.\n");
        }

        /*//Arraylist
        StockTrading tmp = new StockTrading();
        tmp.add(ticker, sector, openingPrice, highPrice, latestTradingPrice, closingPrice );
        stc.add(tmp);
        hashMapStocks.put(ticker, stc.get(noOfStocks));
        noOfStocks++;
        System.out.println(ticker);
        System.out.println(hashMapStocks.get(ticker).getTicker());*/

        //Array
        stockTradingArray[noOfStocks] = new StockTrading();
        stockTradingArray[noOfStocks].add(ticker, sector, openingPrice, highPrice, latestTradingPrice, closingPrice );
        hashMapStocks.put(ticker, stockTradingArray[noOfStocks]);
        //System.out.println(ticker);
        //System.out.println(hashMapStocks.get(ticker).getTicker());
        //System.out.println(noOfStocks);
        noOfStocks++;

        //simple variable
        /*StockTrading test = new StockTrading();
        test.add(ticker, sector, openingPrice, highPrice, latestTradingPrice, closingPrice );
        hashMapStocks.put(ticker, test);
        System.out.println(ticker);
        System.out.println(hashMapStocks.get(ticker).getTicker());*/
    }

    public static void listAllStocksSectorWise(String sector){//a function to show all the stocks of a specific class

        System.out.println("\nScrips listed in sector: "+sector);
        for (Map.Entry mapElement : hashMapStocks.entrySet()) {
            String key = (String)mapElement.getKey();

            if(hashMapStocks.get(key).getSector().equals(sector))
            {
               hashMapStocks.get(key).print();
            }
        }
    }
    public static void printAllStocks(){
        for (String key : hashMapStocks.keySet()) {
            System.out.println("\n"+key);
            //System.out.println(hashMapStocks.get(key).getTicker());
            hashMapStocks.get(key).print();
        }
    }

    public static void deleteStockInMain(String name){
        if (!(hashMapStocks.containsKey(name))) {
            System.out.print("Stock " + name + " does not exists.\n");
            return;
        }
            hashMapStocks.get(name).delete();
            hashMapStocks.remove(name);
    }


    public static void checkForUserAddition(String a){//have used regex here
        if(!a.matches("^Add user:.*"))//to check for starting of the inputted text
            return;
        // System.out.println("Adding User.");

        String name;
        double funds;
//Add user: Jaydeep, funds:25000 holding: None
        Pattern p = Pattern.compile("[^:,{} ]*");//regex separating everything inputted by ':',',','{','}' and' '
        Matcher m = p.matcher(a);

        int i=0;
        String [] elements = new String[100];
        while(m.find()) {
            elements[i] =(m.group());
            //System.out.println(elements[i] +"---"+i);
            i++;
        }
        int size = i;//number of elements in the array elements - 1

        name = elements[5].strip();
        funds = Double.parseDouble(elements[10]);

        HashMap<String, Integer> stocksOwned = new HashMap<>();

        if(!(size<16)){
            int checks = (size-16)/5;
           // System.out.println("Cehcks"+checks);

            for (i=0;i<checks;i++){
                int indexOfStock = 16+5*(i);//since after 16 elements, every 5th element is a stock ticker
                String stockName = elements[indexOfStock];
                String quantityString = elements[indexOfStock+2];
                int quantity = Integer.parseInt(quantityString);
                //System.out.println(stockName+"--"+quantityString);
                stocksOwned.put(stockName,quantity);
            }
        }
        addToUser(name, funds, stocksOwned);
    }

    public static void addToUser(String name, double funds, HashMap<String, Integer> stocksOwned){
        if(hashMapUsers.containsKey(name)){
            System.out.println("User: "+name+ " already exists.\n");
        }
//TO-DO: will be back later: DONE
        userArray[noOfUsers] = new User();
        userArray[noOfUsers].add(name, funds, stocksOwned );
        hashMapUsers.put(name, userArray[noOfUsers]);
        //System.out.println(ticker);
        //System.out.println(hashMapStocks.get(ticker).getTicker());
        //System.out.println(noOfStocks);
        noOfUsers++;
    }

    public static void printAllUsers(){
        for (String key : hashMapUsers.keySet()) {
            hashMapUsers.get(key).print();
        }
    }

    public static void deleteUserInMain(String name){
        if(!(hashMapUsers.containsKey(name))){
            System.out.print("User "+name+" does not exists.\n");
                return;
            }
            hashMapUsers.get(name).deleteUser();
            hashMapUsers.remove(name);
    }

    public static void placeOrders(String line) throws LowFunds, LCViolation, UCViolation {
        if(!(line.matches("Place order.*"))){
            return;
        }

        System.out.println("\nPlacing Orders:");
        Pattern p = Pattern.compile("[^:,]*");
        Matcher m = p.matcher(line);

        int i=0;
        String [] elements = new String[100];
        while(m.find()) {
            elements[i] =(m.group());
           // System.out.println(elements[i] +"---"+i);
            i++;
        }
        String name = elements[4].strip();
        String typeOfOrder = elements[8].trim();
        String stockName = elements[12].strip();
        int quantity = Integer.parseInt(elements[16]);
        double rate = Double.parseDouble(elements[20]);
        double cost = rate*quantity;

        if(typeOfOrder.equals("sell")) {
            System.out.print("------------"+name+"---------------\n");
            //TO-DO Exception handling: DONE
            if (hashMapStocks.get(stockName).lowerCircuit > rate) {
                System.out.println("ERROR: LC Violation");
                // throw new LCViolation("----------------Error: Price of stock is less than Lower Circuit");
            } else if (hashMapStocks.get(stockName).upperCircuit < rate) {
                System.out.println("ERROR: UC Violation");
                // throw new UCViolation("----------------Error: Price of stock is more than Upper Circuit");
            }
            else if (!(hashMapUsers.get(name).stocksOwned.containsKey(stockName))) {
                {
                    System.out.println("Short selling Case... Made a new record of user " + hashMapUsers.get(name).name+" in shortSellers Array ");
                    shortSellers[noOFShortSellers] = new Transaction();
                    shortSellers[noOFShortSellers].initiateSelling(hashMapUsers.get(name), typeOfOrder, stockName, quantity, rate, noOFShortSellers);
                    shortSellingOrderBook.put(noOFShortSellers, shortSellers[noOFShortSellers]);
                    noOFShortSellers++;

                    //adding the short sell specifically to the order book
                    transactionArray[noOfTransactions] = new Transaction();
                    transactionArray[noOfTransactions].initiateSelling(hashMapUsers.get(name), typeOfOrder, stockName, quantity, rate, noOfTransactions);
                    //adding to order book
                    orderBook.put(noOfTransactions, transactionArray[noOfTransactions]);
                    noOfTransactions++;

                    //updating user's info
                    hashMapUsers.get(name).funds = hashMapUsers.get(name).funds + quantity * rate;
                    //adding a special case where the stocks owned by the user would be negative. This is to deal with the fact that the user has borrowed some shares to perform short selling
                   // hashMapUsers.get(name).stocksOwned.put(stockName, -quantity);
                    //if by end of the day, the stocks in negative part is not sold, then there will be huge penalty on the user.
                }
            } else {
                transactionArray[noOfTransactions] = new Transaction();
                transactionArray[noOfTransactions].initiateSelling(hashMapUsers.get(name), typeOfOrder, stockName, quantity, rate, noOfTransactions);
                orderBook.put(noOfTransactions, transactionArray[noOfTransactions]);
                //  System.out.println("------------------------------"+orderBook.get(noOfTransactions).stock);
                noOfTransactions++;
            }
        }
        else if(typeOfOrder.equals("buy")) {
            System.out.print("--------"+name+"---------------\n");

            // System.out.print("--------"+typeOfOrder+"---------------\n");

            if (hashMapStocks.get(stockName).lowerCircuit > rate) {
               System.out.println("ERROR: LC Violation");
                //throw new LCViolation("-----ERROR: LC VIOLATION");
            }
            else if (hashMapStocks.get(stockName).upperCircuit < rate) {
                System.out.println("ERROR: UC Violation");
               // throw new UCViolation("-----ERROR: UC VIOLATION");
            }
            else if(hashMapUsers.get(name).funds<cost){
                System.out.println("ERROR: Not enough funds");
                //throw new LowFunds("------Error: Low Funds ");
            }
            else {
                transactionArray[noOfTransactions] = new Transaction();
                transactionArray[noOfTransactions].initiateBuying(hashMapUsers.get(name), typeOfOrder, stockName, quantity, rate, noOfTransactions);
                orderBook.put(noOfTransactions, transactionArray[noOfTransactions]);
                //System.out.println("-------"+orderBook.get(noOfTransactions).name+"-------\n");
                noOfTransactions++;
            }
        }

        else {
            System.out.println("Wrong transaction type.");
        }
    }

    public static void showOrderBook(String line){
        if(!(line.matches("Show Orderbook.*"))){
            return;
        }
        //printAllUsers();

        System.out.print("\nOrderBook is:\n");
        //System.out.print(noOfTransactions);
        //System.out.print(orderBook.get(1).stock);

        System.out.print("Buy Orders:\n");
        for (Map.Entry mapElement : orderBook.entrySet()) {
            int key = (int)mapElement.getKey();
            //System.out.print("\n"+key);
            //System.out.print("\n"+orderBook.get(key).name+" has ");
            if((orderBook.get(key).requestType.equals("buy"))&&(orderBook.get(key).openOrNot==true)){
                System.out.println(orderBook.get(key).quantity+" buy orders of "+orderBook.get(key).stock+" at "+orderBook.get(key).rate+", Status: open");
            }
        }

        System.out.print("Sell Orders:\n");
        for (Map.Entry mapElement : orderBook.entrySet()) {
            int key = (int)mapElement.getKey();
            if((orderBook.get(key).requestType.equals("sell"))&&(orderBook.get(key).openOrNot==true)){
                System.out.println(orderBook.get(key).quantity+" sell orders of "+orderBook.get(key).stock+" at "+orderBook.get(key).rate+", Status: open");
            }
        }
    }
    public static void showShortSellers(){
        System.out.print("\nShort Sellers list is:\n");
        for (Map.Entry mapElement : shortSellingOrderBook.entrySet()) {
            int key = (int)mapElement.getKey();
            //System.out.print("\n"+key);
            //System.out.print("\n"+orderBook.get(key).name+" has ");
            if((shortSellingOrderBook.get(key).openOrNot=true)){
                System.out.println(shortSellingOrderBook.get(key).quantity+" buy orders of "+shortSellingOrderBook.get(key).stock+" at "+shortSellingOrderBook.get(key).rate);
            }
        }
    }
//added true or false in transaction

    public static void executeTransactions(){
        /*for (Map.Entry mapElement : orderBook.entrySet()) {
            int key = (int)mapElement.getKey();*/
        for (Iterator<Integer> keys = orderBook.keySet().iterator(); keys.hasNext();) {
            Integer key = keys.next();
            {
               for(int i=key; i<noOfTransactions; i++){

                   if(key!=i){

                       if(!(orderBook.get(key).requestType.equals(orderBook.get(i).requestType))){

                           if(orderBook.get(key).stock.equals(orderBook.get(i).stock)){

                               if(orderBook.get(key).requestType.equals("buy")){

                                   /*String buyer = orderBook.get(key).name;
                                   String seller = orderBook.get(i).name;*/
                                   if(orderBook.get(key).rate>=orderBook.get(i).rate){
                                       //System.out.println("Execution successful");
                                       System.out.println("\n----------Executing orders: ");

                                       //case 1 more to buy
                                       if(orderBook.get(key).quantity>orderBook.get(i).quantity){
                                           System.out.println("Executing order type 1 more to buy for i is buyer ");
                                            System.out.println("Buyer: "+orderBook.get(key).name.name+", Seller: "+orderBook.get(i).name.name+", Stock Name: "+orderBook.get(key).stock+", Quantity: "+orderBook.get(i).quantity+", Rate: "+orderBook.get(i).rate);
                                           //updating new ltp of stock
                                           hashMapStocks.get(orderBook.get(key).stock).latestTradingPrice = orderBook.get(i).rate;

                                           //User profile updated here
                                           double cost = orderBook.get(i).quantity*orderBook.get(i).rate;
                                           String stockName = orderBook.get(i).stock;

                                           if(!hashMapUsers.get(orderBook.get(key).name.name).stocksOwned.containsKey(stockName)) {
                                               hashMapUsers.get(orderBook.get(key).name.name).stocksOwned.put(stockName,0);
                                           }
                                           if(!hashMapUsers.get(orderBook.get(i).name.name).stocksOwned.containsKey(stockName)) {
                                               hashMapUsers.get(orderBook.get(i).name.name).stocksOwned.put(stockName,0);
                                           }

                                           int newQuantityForBuyer = hashMapUsers.get(orderBook.get(key).name.name).stocksOwned.get(stockName) + orderBook.get(i).quantity;
                                           int newQuantityForSeller = hashMapUsers.get(orderBook.get(i).name.name).stocksOwned.get(stockName) - orderBook.get(i).quantity;//simply 0
                                           double newFundsBuyer = hashMapUsers.get(orderBook.get(key).name.name).funds - cost;
                                           double newFundsSeller = hashMapUsers.get(orderBook.get(i).name.name).funds + cost;
                                           int remainingStocks = orderBook.get(key).quantity - orderBook.get(i).quantity;

                                           hashMapUsers.get(orderBook.get(key).name.name).stocksOwned.replace(stockName,newQuantityForBuyer);
                                           hashMapUsers.get(orderBook.get(i).name.name).stocksOwned.replace(stockName,newQuantityForSeller);
                                           hashMapUsers.get(orderBook.get(key).name.name).funds= newFundsBuyer;
                                           hashMapUsers.get(orderBook.get(i).name.name).funds= newFundsSeller;

                                           //reassigning the remaining stocks to buyer
                                           orderBook.get(key).quantity = remainingStocks;


                                         /*  //order book updated above
                                           orderBook.get(key).quantity = orderBook.get(key).quantity - orderBook.get(i).quantity;
                                           orderBook.get(i).quantity = 0;*/

                                           //removed from order book
                                           orderBook.get(i).openOrNot=false;

                                          // orderBook.get(key).openOrNot=false;
                                           //orderBook.remove(i);

                                           //hashMapUsers->to user1 via order book.getkey.name->owned stocks hashmap->
                                           //orderBook<int, transaction>
                                           //hashmapUsers<name, user>
                                           //hashMapStock<stock name, stock>
                                           //transaction (user , and other things)
                                       }
                                       //case 2 more to sell
                                      else if(orderBook.get(key).quantity<orderBook.get(i).quantity) {
                                           System.out.println("Executing order type 2 more to sell i is buyer");
                                           System.out.println("Buyer: "+orderBook.get(key).name.name+", Seller: "+orderBook.get(i).name.name+", Stock Name: "+orderBook.get(key).stock+", Quantity: "+orderBook.get(key).quantity+", Rate: "+orderBook.get(i).rate);

                                           //updating new ltp of stock
                                           hashMapStocks.get(orderBook.get(key).stock).latestTradingPrice = orderBook.get(i).rate;

                                           //User profile updated here
                                           double cost = orderBook.get(key).quantity*orderBook.get(i).rate;
                                           String stockName = orderBook.get(i).stock;

                                           if(!hashMapUsers.get(orderBook.get(key).name.name).stocksOwned.containsKey(stockName)) {
                                               hashMapUsers.get(orderBook.get(key).name.name).stocksOwned.put(stockName,0);
                                           }
                                           if(!hashMapUsers.get(orderBook.get(i).name.name).stocksOwned.containsKey(stockName)) {
                                               hashMapUsers.get(orderBook.get(i).name.name).stocksOwned.put(stockName,0);
                                           }
                                            //Change here..quantity change is picked from orderbook not user hashmap
                                           int newQuantityForBuyer = hashMapUsers.get(orderBook.get(key).name.name).stocksOwned.get(stockName) + orderBook.get(key).quantity;//0 here
                                           int newQuantityForSeller = hashMapUsers.get(orderBook.get(i).name.name).stocksOwned.get(stockName) - orderBook.get(key).quantity;//
                                           //System.out.println("****************"+hashMapUsers.get(orderBook.get(key).name.name).name+" ---- "+hashMapUsers.get(orderBook.get(key).name.name).stocksOwned.get(stockName)+"----"+newQuantityForBuyer);
                                           double newFundsBuyer = hashMapUsers.get(orderBook.get(key).name.name).funds - cost;
                                           double newFundsSeller = hashMapUsers.get(orderBook.get(i).name.name).funds + cost;
                                           int remainingStocks = orderBook.get(i).quantity - orderBook.get(key).quantity;

                                           hashMapUsers.get(orderBook.get(key).name.name).stocksOwned.replace(stockName,newQuantityForBuyer);
                                           hashMapUsers.get(orderBook.get(i).name.name).stocksOwned.replace(stockName,newQuantityForSeller);
                                           hashMapUsers.get(orderBook.get(key).name.name).funds= newFundsBuyer;
                                           hashMapUsers.get(orderBook.get(i).name.name).funds= newFundsSeller;

                                      /*     //removing previous transactions and initiating a new one for the remaining stocks
                                           Transaction newTransaction = new Transaction();
                                           newTransaction.initiateBuying(orderBook.get(key).name, orderBook.get(key).requestType, orderBook.get(key).stock, newQuantityForBuyer, orderBook.get(key).rate, orderBook.get(key).transactionID);
                                           orderBook.replace(key,newTransaction);*/

                                           //order book updated above
                                           //orderBook.get(key).quantity = 0; no need
                                           orderBook.get(i).quantity = remainingStocks;

                                           //removed from order book
                                           orderBook.get(key).openOrNot=false;
                                           //orderBook.get(i).openOrNot=false;

                                           //orderBook.remove(key);
                                       }

                                       //case 3 sell = buy

                                       else {
                                           System.out.println("Executing order type 3 for equilibrium case i is buyer");
                                           System.out.println("Buyer: "+orderBook.get(key).name.name+", Seller: "+orderBook.get(i).name.name+", Stock Name: "+orderBook.get(key).stock+", Quantity: "+orderBook.get(i).quantity+", Rate: "+orderBook.get(i).rate);

                                           //updating new ltp of stock
                                           hashMapStocks.get(orderBook.get(key).stock).latestTradingPrice = orderBook.get(i).rate;

                                           //User profile updated here
                                           double cost = orderBook.get(key).quantity*orderBook.get(i).rate;
                                           String stockName = orderBook.get(i).stock;

                                           if(!hashMapUsers.get(orderBook.get(key).name.name).stocksOwned.containsKey(stockName)) {
                                               hashMapUsers.get(orderBook.get(key).name.name).stocksOwned.put(stockName,0);
                                           }
                                           if(!hashMapUsers.get(orderBook.get(i).name.name).stocksOwned.containsKey(stockName)) {
                                               hashMapUsers.get(orderBook.get(i).name.name).stocksOwned.put(stockName,0);
                                           }

                                           int newQuantityForBuyer = hashMapUsers.get(orderBook.get(key).name.name).stocksOwned.get(stockName) + orderBook.get(key).quantity;//0 here
                                           int newQuantityForSeller = hashMapUsers.get(orderBook.get(i).name.name).stocksOwned.get(stockName) - orderBook.get(key).quantity;//simply 0
                                           double newFundsBuyer = hashMapUsers.get(orderBook.get(key).name.name).funds - cost;
                                           double newFundsSeller = hashMapUsers.get(orderBook.get(i).name.name).funds + cost;

                                           hashMapUsers.get(orderBook.get(key).name.name).stocksOwned.replace(stockName,newQuantityForBuyer);
                                           hashMapUsers.get(orderBook.get(i).name.name).stocksOwned.replace(stockName,newQuantityForSeller);
                                           hashMapUsers.get(orderBook.get(key).name.name).funds= newFundsBuyer;
                                           hashMapUsers.get(orderBook.get(i).name.name).funds= newFundsSeller;

                                           //removing from order book
                                           orderBook.get(key).openOrNot=false;
                                         //  orderBook.remove(key);
                                           orderBook.get(i).openOrNot=false;
                                           //orderBook.remove(i);

                                       }
                                   }
                               }


                               else if(orderBook.get(key).requestType.equals("sell")){

                                   /*String seller = orderBook.get(key).name;
                                   String buyer = orderBook.get(i).name;*/
                                   if(orderBook.get(key).rate<=orderBook.get(i).rate){
                                       //System.out.println("Execution successful");
                                       System.out.println("\n----------Executing orders: ");

                                       //case 1 more to buy
                                       if(orderBook.get(key).quantity<orderBook.get(i).quantity){
                                           System.out.println("Executing order type 1 more to buy for ");
                                           System.out.println("Buyer: "+orderBook.get(i).name.name+", Seller: "+orderBook.get(key).name.name+", Stock Name: "+orderBook.get(key).stock+", Quantity: "+orderBook.get(key).quantity+", Rate: "+orderBook.get(key).rate);

                                           //updating new ltp of stock
                                           hashMapStocks.get(orderBook.get(key).stock).latestTradingPrice = orderBook.get(key).rate;

                                           //User profile updated here
                                           double cost = orderBook.get(key).quantity*orderBook.get(key).rate;
                                           String stockName = orderBook.get(i).stock;

                                           if(!hashMapUsers.get(orderBook.get(key).name.name).stocksOwned.containsKey(stockName)) {
                                               hashMapUsers.get(orderBook.get(key).name.name).stocksOwned.put(stockName,0);
                                           }
                                           if(!hashMapUsers.get(orderBook.get(i).name.name).stocksOwned.containsKey(stockName)) {
                                               hashMapUsers.get(orderBook.get(i).name.name).stocksOwned.put(stockName,0);
                                           }
                                           //////////here

                                           int newQuantityForBuyer = hashMapUsers.get(orderBook.get(i).name.name).stocksOwned.get(stockName) + orderBook.get(key).quantity;
                                           int newQuantityForSeller = hashMapUsers.get(orderBook.get(key).name.name).stocksOwned.get(stockName) - orderBook.get(key).quantity;//simply 0
                                           double newFundsBuyer = hashMapUsers.get(orderBook.get(i).name.name).funds - cost;
                                           double newFundsSeller = hashMapUsers.get(orderBook.get(key).name.name).funds + cost;
                                           int remainingStocks = orderBook.get(i).quantity - orderBook.get(key).quantity;

                                           hashMapUsers.get(orderBook.get(i).name.name).stocksOwned.replace(stockName,newQuantityForBuyer);
                                           hashMapUsers.get(orderBook.get(key).name.name).stocksOwned.replace(stockName,newQuantityForSeller);
                                           hashMapUsers.get(orderBook.get(i).name.name).funds= newFundsBuyer;
                                           hashMapUsers.get(orderBook.get(key).name.name).funds= newFundsSeller;

                                           //reassigning the remaining stocks to buyer
                                          orderBook.get(i).quantity = remainingStocks;

                                         /*  //order book updated above
                                           orderBook.get(key).quantity = orderBook.get(key).quantity - orderBook.get(i).quantity;
                                           orderBook.get(i).quantity = 0;*/

                                           //removed from order book
                                           orderBook.get(key).openOrNot=false;

                                          // orderBook.remove(key);

                                           //hashMapUsers->to user1 via order book.getkey.name->owned stocks hashmap->
                                           //orderBook<int, transaction>
                                           //hashmapUsers<name, user>
                                           //hashMapStock<stock name, stock>
                                           //transaction (user , and other things)
                                       }
                                       //case 2 more to sell
                                       else if(orderBook.get(key).quantity>orderBook.get(i).quantity) {
                                           System.out.println("Executing order type 2 more to sell ");
                                           System.out.println("Buyer: "+orderBook.get(i).name.name+", Seller: "+orderBook.get(key).name.name+", Stock Name: "+orderBook.get(key).stock+", Quantity: "+orderBook.get(i).quantity+", Rate: "+orderBook.get(key).rate);

                                           //updating new ltp of stock
                                           hashMapStocks.get(orderBook.get(key).stock).latestTradingPrice = orderBook.get(key).rate;

                                           //User profile updated here
                                           double cost = orderBook.get(i).quantity*orderBook.get(key).rate;
                                           String stockName = orderBook.get(i).stock;

                                           if(!hashMapUsers.get(orderBook.get(key).name.name).stocksOwned.containsKey(stockName)) {
                                               hashMapUsers.get(orderBook.get(key).name.name).stocksOwned.put(stockName,0);
                                           }
                                           if(!hashMapUsers.get(orderBook.get(i).name.name).stocksOwned.containsKey(stockName)) {
                                               hashMapUsers.get(orderBook.get(i).name.name).stocksOwned.put(stockName,0);
                                           }

                                           int newQuantityForBuyer = hashMapUsers.get(orderBook.get(i).name.name).stocksOwned.get(stockName) + orderBook.get(i).quantity;//0 here
                                           int newQuantityForSeller = hashMapUsers.get(orderBook.get(key).name.name).stocksOwned.get(stockName) - orderBook.get(i).quantity;//simply 0
                                           double newFundsBuyer = hashMapUsers.get(orderBook.get(i).name.name).funds - cost;
                                           double newFundsSeller = hashMapUsers.get(orderBook.get(key).name.name).funds + cost;
                                           int remainingStocks = orderBook.get(key).quantity - orderBook.get(i).quantity;

                                           hashMapUsers.get(orderBook.get(i).name.name).stocksOwned.replace(stockName,newQuantityForBuyer);
                                           hashMapUsers.get(orderBook.get(key).name.name).stocksOwned.replace(stockName,newQuantityForSeller);
                                           hashMapUsers.get(orderBook.get(i).name.name).funds= newFundsBuyer;
                                           hashMapUsers.get(orderBook.get(key).name.name).funds= newFundsSeller;

                                      /*     //removing previous transactions and initiating a new one for the remaining stocks
                                           Transaction newTransaction = new Transaction();
                                           newTransaction.initiateBuying(orderBook.get(key).name, orderBook.get(key).requestType, orderBook.get(key).stock, newQuantityForBuyer, orderBook.get(key).rate, orderBook.get(key).transactionID);
                                           orderBook.replace(key,newTransaction);*/

                                           //order book updated above
                                         orderBook.get(key).quantity = remainingStocks;

                                           //removed from order book
                                           orderBook.get(i).openOrNot=false;
                                          // orderBook.remove(i);
                                       }

                                       //case 3 sell = buy

                                       else {
                                           System.out.println("Executing order type 3 equilibrium case");
                                           System.out.println("Buyer: "+orderBook.get(i).name.name+", Seller: "+orderBook.get(key).name.name+", Stock Name: "+orderBook.get(key).stock+", Quantity: "+orderBook.get(key).quantity+", Rate: "+orderBook.get(key).rate);

                                           //updating new ltp of stock
                                           hashMapStocks.get(orderBook.get(key).stock).latestTradingPrice = orderBook.get(key).rate;
                                           //User profile updated here
                                           double cost = orderBook.get(key).quantity*orderBook.get(key).rate;
                                           String stockName = orderBook.get(i).stock;

                                           if(!hashMapUsers.get(orderBook.get(key).name.name).stocksOwned.containsKey(stockName)) {
                                               hashMapUsers.get(orderBook.get(key).name.name).stocksOwned.put(stockName,0);
                                           }
                                           if(!hashMapUsers.get(orderBook.get(i).name.name).stocksOwned.containsKey(stockName)) {
                                               hashMapUsers.get(orderBook.get(i).name.name).stocksOwned.put(stockName,0);
                                           }

                                           int newQuantityForBuyer = hashMapUsers.get(orderBook.get(i).name.name).stocksOwned.get(stockName) + orderBook.get(key).quantity;//0 here
                                           int newQuantityForSeller = hashMapUsers.get(orderBook.get(key).name.name).stocksOwned.get(stockName) - orderBook.get(key).quantity;//simply 0
                                           double newFundsBuyer = hashMapUsers.get(orderBook.get(i).name.name).funds - cost;
                                           double newFundsSeller = hashMapUsers.get(orderBook.get(key).name.name).funds + cost;

                                           hashMapUsers.get(orderBook.get(i).name.name).stocksOwned.replace(stockName,newQuantityForBuyer);
                                           hashMapUsers.get(orderBook.get(key).name.name).stocksOwned.replace(stockName,newQuantityForSeller);
                                           hashMapUsers.get(orderBook.get(i).name.name).funds= newFundsBuyer;
                                           hashMapUsers.get(orderBook.get(key).name.name).funds= newFundsSeller;

                                           //removing from order book
                                           orderBook.get(key).openOrNot=false;
                                          // orderBook.remove(key);
                                           orderBook.get(i).openOrNot=false;
                                         //  orderBook.remove(i);

                                       }
                                   }
                               }
                           }
                           //TO-DO check share type  and then reqs: Done
                       }
                   }
               }
            }
        }
    }

    public static void readCSV(String location) throws FileNotFoundException {
        try {
            File txt = new File(location);
            Scanner scan = new Scanner(txt);
            boolean firstLineOrNot = true;

            //System.out.println("Reading the file now.");
            while (scan.hasNextLine()) {

                String line = scan.nextLine();
                //System.out.println(line);
                if(firstLineOrNot==false) {
                    String[] elements = new String[100];
                    elements = line.split(",");
                    int i = 0;
                    for (i = 0; i < elements.length; i++) {
                        elements[i] = elements[i].trim();
                        //System.out.println(elements[i]+"-----"+i);
                    }


                        CSVReader instance = new CSVReader();
                        String stockName = elements[0];
                        String date = elements[1];

                        // System.out.println("--------"+elements[2]);
                        double previousClosePrice = Double.parseDouble(elements[2]);
                        double openPrice = Double.parseDouble(elements[3]);
                        double highPrice = Double.parseDouble(elements[4]);
                        double lowPrice = Double.parseDouble(elements[5]);
                        double latestTradingPrice = Double.parseDouble(elements[6]);
                        double closePrice = Double.parseDouble(elements[7]);

                        instance.addFromCSV(stockName, date, previousClosePrice, openPrice, highPrice, lowPrice, latestTradingPrice, closePrice);
                        csvReaderArray.add(instance);

                    int size = i;
                   // System.out.println("dsdsd");

                }
                firstLineOrNot = false;

            }

            System.out.println("\n-----------------------Start of CSV file----------\n");
            profitAndLoss();
            maxDrawDown();
            averagePrice();
            maxReturnPotential();

        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
            System.out.println("Error CSV file not found");
        }
    }

    public static void profitAndLoss() {
        double openingPriceOfFirstDay= csvReaderArray.get(0).openPrice;
        double closingPriceOfLastDay=csvReaderArray.get(csvReaderArray.size()-1).closePrice;
        double profitOrLoss = closingPriceOfLastDay -openingPriceOfFirstDay;

        if(profitOrLoss>0)
            System.out.printf("There was a profit of %.2f per stock in the given period.\n",profitOrLoss);
        else if (profitOrLoss<0)
            System.out.printf("There was a loss of %.2f per stock in the given period.\n",profitOrLoss);
        else
            System.out.println("This was a period of no profit an no loss.");
    }

    public static void maxDrawDown() {
       // double maximumLoss=0;
        int maxIndexi = 0;
        int minIndexi = 0;
        int maxIndexj = 0;
//100,91,102,112,87,111,75
        for(int i=0;i<csvReaderArray.size();i++){
            //maxIndexj=0;
            double max=0;
             for (int j=i+1;j<csvReaderArray.size();j++){
                 if (csvReaderArray.get(i).closePrice - csvReaderArray.get(j).closePrice > max){
                     maxIndexj = j;
                     max = csvReaderArray.get(i).closePrice - csvReaderArray.get(j).closePrice;
                 }
             }
             if(max > csvReaderArray.get(maxIndexi).closePrice - csvReaderArray.get(minIndexi).closePrice){
                 maxIndexi = i;
                 minIndexi = maxIndexj;
             }
        }
        double maximumLoss = csvReaderArray.get(maxIndexi).closePrice - csvReaderArray.get(minIndexi).closePrice;

        System.out.printf("Max DrawDown is %.2f\n", maximumLoss);
    }

    public static void averagePrice() {

        double[] dailyAverageArray = new double[csvReaderArray.size()];
        double sumOfDailyAverages = 0;

        for(int i=0;i<csvReaderArray.size();i++) {
            dailyAverageArray[i] = csvReaderArray.get(i).closePrice;
        }
        for (double v : dailyAverageArray) {
            sumOfDailyAverages = sumOfDailyAverages + v;
        }
        double avg = sumOfDailyAverages/dailyAverageArray.length;

        System.out.printf("Average of the given time period is %.2f \n", avg );
    }

    public static void maxReturnPotential() {
        double maxReturnPotential = 0;
        double[] dailyProfitsArray = new double[csvReaderArray.size()];

        for(int i=0;i<csvReaderArray.size();i++) {
            dailyProfitsArray[i] = Math.abs(csvReaderArray.get(i).openPrice-csvReaderArray.get(i).closePrice);//getting the best scenario i.e. buy early on bullish and sell early on bearish day.
            maxReturnPotential += dailyProfitsArray[i];
        }

        System.out.printf("Max Return Potential is %.2f \n", maxReturnPotential);

        double MaxPer =  100*maxReturnPotential/csvReaderArray.get(0).openPrice;
        System.out.printf("Max Percentage Return Potential is %.2f %s\n", MaxPer, "%");

    }

}

//------------Stocks starts
abstract class Stock {
    public String ticker ;
    public String exchange ;
    public int stockId;//would be randomly generated id for accessing the stock later
    public String sector;
    public boolean exists;//to check for deletion of the stock
    double latestTradingPrice;//
    double openPrice;//
    double closePrice;//
    double highPrice;//
    double lowPrice=99999999;
    double upperCircuit;
    double lowerCircuit;
    public abstract void add(String ticker, String sector, double openingPrice, double highPrice, double latestTradingPrice, double closingPrice);
    public abstract void print();
    public abstract void delete();
    }


class StockTrading extends Stock{

    public void add( String ticker,  String sector, double openingPrice, double highPrice,double latestTradingPrice, double closingPrice){

        this.exists = true;
        if(Math.random()*10%2==0){
            this.exchange = "BSE";
        }
        else if(Math.random()*10%3==0)
            this.exchange = "Registered in both BSE and NSE";
        else {
            this.exchange = "NSE";
        }

        this.stockId = (int) (Math.random()*100000);
        this.ticker = ticker;
        this.sector = sector;
        this.openPrice = openingPrice;
        this.highPrice = highPrice;
        this.latestTradingPrice = latestTradingPrice;
        this.closePrice = closingPrice;
        this.lowerCircuit = 0.9*closingPrice;
        this.upperCircuit = 1.1*closingPrice;

        if(latestTradingPrice< this.lowPrice){
            this.lowPrice = latestTradingPrice;
        }

        System.out.printf("Added scrip: %s with a new instantiation of %s\n", ticker, "StockTrading class.");
    }

    public void print(){
        if(exists=true){
            System.out.println("Ticker: "+this.ticker+", Sector: "+this.sector+", Open Price: "+openPrice+", High Price: "+highPrice+", Low Price: "+lowPrice+", Close Value: "+closePrice+" Registered at: "+this.exchange);
        }
    }

    public double getOpenPrice(){
        return openPrice;
    }
    public double getHighPrice(){
        return  highPrice;
    }
    public double getLowPrice(){
        return  lowPrice;
    }
    public double getClosePrice(){
        return closePrice;
    }
    public String getSector(){
    return this.sector;
    }
    public String getTicker(){
        return this.ticker;
    }

    public void delete(){
        this.exists = false;
        this.stockId = -1;
        this.sector = null;
        this.openPrice = -1;
        this.highPrice = -1;
        this.latestTradingPrice = -1;
        this.closePrice = -1;
        this.lowerCircuit = -1;
        this.upperCircuit = -1;
        System.out.println("Deleted scrip: "+ticker);
        this.ticker = null;
    }
}

//------------------User starts
abstract class UserInfo{
    public String name ;
    public int customerID ;
    public boolean exists;
    HashMap<String, Integer> stocksOwned = new HashMap<>();//storing all the shares owned by the user
    //key stock name or ticker///// value number of stocks
    public double funds;
    public abstract void add(String name, double funds, HashMap<String, Integer> stocksOwned);
    public abstract void print();
    public abstract void updateQuantity(String stockName, int newQuantity);
    public abstract void deleteUser();
}
class User extends UserInfo {

    public void add(String name, double funds, HashMap<String, Integer> stocksOwned){
        this.exists =true;
        this.name = name;
        this.funds = funds;
        this.customerID = (int)(Math.random()*100000);
        this.stocksOwned =stocksOwned;
        System.out.printf("Added user: %s with a new instantiation of %s\n", name, "User class.");
    }

    public void print(){
        if(this.exists=true){
            System.out.printf("\nName: %s, UserID: %d, Funds: %.2f\n", name, customerID, funds);
            System.out.println("Stocks Owned: ");
            for (Map.Entry mapElement : stocksOwned.entrySet()) {
                String key = (String)mapElement.getKey();
                System.out.print(key + " --- " + stocksOwned.get(key)+"\n");
            }
            System.out.print("\n");
        }
    }
    public void updateQuantity(String stockName, int newQuantity){
        this.stocksOwned.put(stockName,newQuantity);
    }

    public  void deleteUser(){
        this.exists = false;
        this.funds = -1;
        this.customerID = -1;
        this.stocksOwned =null;
        System.out.println("Deleted User: "+name);
        this.name = null;
    }
}

//-------------------Transaction starts
class Transaction{
    public User name;
    public int transactionID;
    public String requestType;//buy/sell
    public int quantity;
    public double rate;
    public String stock;
    public boolean openOrNot;

    public void initiateSelling (User name, String orderType, String stock, int quantity, double rate, int transactionID){

//To-do compare with user's hashmaps*********************************** :Done
        this.requestType = orderType;
        this.quantity = quantity;
        this.stock = stock;
        this.rate = rate;
        this.name = name;
        this.transactionID = transactionID;
        this.openOrNot = true;
        System.out.printf("Order placed for user: %s, type: %s, scrip: %s, qty:%d, rate: %f\n", name.name, orderType, stock,quantity, rate);
    }

    public void initiateBuying(User name, String orderType,  String stock, int quantity, double rate, int transactionID){//12 100 B

        double costToBuy = rate*quantity;

        //test
        this.requestType = orderType;
        this.quantity = quantity;
        this.rate = rate;
        this.stock = stock;
        this.name = name;
        this.transactionID = transactionID;
        this.openOrNot = true;
        System.out.printf("Order placed for user: %s, type: %s, scrip: %s, qty:%d, rate: %f\n", name.name, orderType, stock,quantity, rate);
    }
}

class CSVReader {
    public String stockName ;
    double latestTradingPrice;//
    double openPrice;//
    double closePrice;//
    double highPrice;//
    double lowPrice;
    public String date;
    double prevClosePrice;

    public void addFromCSV(String stockName, String date, Double prevClosePrice, Double openPrice, Double highPrice, Double lowPrice, Double latestTradingPrice, Double closePrice) {
        this.stockName = stockName;
        this.date = date;
        this.prevClosePrice = prevClosePrice;
        this.openPrice = openPrice;
        this.highPrice = highPrice;
        this.lowPrice = lowPrice;
        this.latestTradingPrice = latestTradingPrice;
        this.closePrice = closePrice;
    }
    public void printDataFromCSV() {
        System.out.println("Stock Name: " + stockName + " Date: " + date + " Previous Closing Price " + prevClosePrice + " Opening Price: " + openPrice + " High Price: " + highPrice + " Low Price: " + lowPrice + " Latest Trading Price (Last Price): " + latestTradingPrice + " Closing Price: " + closePrice);
    }

}


//TO-DO Exceptions addition: DONE

class LowFunds extends Exception{
   String str1 = new String();

    LowFunds(String str2) {
        str1=str2;
    }
    public String toString(){
        return (str1) ;
    }
}
class LCViolation extends Exception{
    String str1 = new String();

    LCViolation(String str2) {
        str1=str2;
    }
    public String toString(){
        return (str1) ;
    }
}
class UCViolation extends Exception{
    String str1 = new String();

    UCViolation(String str2) {
        str1=str2;
    }
    public String toString(){
        return (str1) ;
    }
}
