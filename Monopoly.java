import java.util.*;


/* Problem Description:

Uses a monopoly board with spaces numbered from "00" to "39". Chance cards, Community Chest cards, rolling three doubles in a row, and the "Go to Jail" space (GTJ) all affect the probabilities of ending on various squares. 

The heart of this problem concerns the likelihood of visiting a particular square. That is, the probability of finishing at that square after a roll. For this reason it should be clear that, with the exception of G2J for which the probability of finishing on it is zero, the CH squares will have the lowest probabilities, as 5/8 request a movement to another square, and it is the final square that the player finishes at on each roll that we are interested in. We shall make no distinction between "Just Visiting" and being sent to JAIL, and we shall also ignore the rule about requiring a double to "get out of jail", assuming that they pay to get out on their next turn.

By starting at GO and numbering the squares sequentially from 00 to 39 we can concatenate these two-digit numbers to produce strings that correspond with sets of squares. If, instead of using two 6-sided dice, two 4-sided dice are used, find the six-digit modal string.

*/





/*
-------------------------
TODO:
-------------------------
* errorCheck() method: check if all class var. strings have been initialized to at least empty strings 
* implement method that calculates probability of starting from a certain spot...
    each space may need its own table of possible values it could end on? (hmm)
     -- ideal solution is essentially a markov chain, I believe (check into this)--
    
    I think this needs to be completely redone, as currently it doesn't account for 
    where each turn is most likely to start from (i.e. jail is the most common space; as such,
    spaces near it have a much higher probability of being reached thoughout the game). 
    
    Easier solution may be just to simulate a game with a significant enough number of rolls;
    eventually, probabilities would converge to actual values... 
*/


class Monopoly {
    
    List<String> chanceSquares;     //chance squares (ch)
    List<String> chestSquares;      //community chest squares (cc)
    List<String> rrSquares;         //railroads 
    List<String> utSquares;         //utilities
    Map<String, Double> odds;       //probability of landing on a given space
    double odds3Doubles;            //odds of rolling three doubles in a row = straight to jail
    int diceSides;                  //provided by user input
    int numOfSquares;               //most boards have 40 spaces, can be adjusted in initBoard()
    int numOfCards;                 //number of chance and comm. chest cards (identical, typ. = 16)
    String g2J;                     //go to jail space
    String jail;
    String go;                  
    
    
    //constructor just initializes class variables 
    public Monopoly(int diceSides){
        this.diceSides = diceSides;
        this.odds = new HashMap<String, Double>();
        this.chanceSquares = new ArrayList<String>();
        this.chestSquares = new ArrayList<String>();
        this.rrSquares = new ArrayList<String>();
        this.utSquares = new ArrayList<String>();
    }
    
    
    //initializes data structures and other class variables
    //this is the only function with hardcoded values. Changes here will not affect program!
    public void initBoard(){
        //add community chest (cc), chance (ch), and other important squares by number.
        //if adjusting locations, please keep strings at size 2: i.e. "2" should be "02", etc.
        this.chestSquares.add("02");
        this.chestSquares.add("17");
        this.chestSquares.add("33");
        this.chanceSquares.add("07");
        this.chanceSquares.add("22");
        this.chanceSquares.add("36");
        this.rrSquares.add("05");
        this.rrSquares.add("15");
        this.rrSquares.add("25");
        this.rrSquares.add("35");
        this.utSquares.add("12");
        this.utSquares.add("28");
        this.g2J = "30";
        this.jail = "10";
        this.go = "00";
        this.numOfSquares = 40;     //do not increase past 99
        this.numOfCards = 16;
        setOdds();
    }
    
    
    //set odds of rolling three doubles in a row (changes per number of sides on dice)
    public void setOdds(){
        //odds of rolling three doubles in a row is: (1/ sides of dice) ^ 3
        this.odds3Doubles = Math.pow((1.0 / (diceSides)), 3);
        //set initial odds for each space 
        for(int i = 0; i < this.numOfSquares; ++i){
            String squareId = normalizeKey(Integer.toString(i));
            this.odds.put(squareId, (1.0 / this.numOfSquares));
        }
    }
    
    
    //CC has 1/16 chance of "Go to Jail" card, and 1/16 chance of "Advance to Go" card 
    //NOTE: this must be done AFTER handleCH() call, as CH actually changes odds of landing on
    //cc square (CH3 -> CC3 is possible if "Go Back 3 Spaces" card is drawn).
    public void handleCC(){
        double ccOdds = 0.0;    // odds of landing on one of the community chest squares
        for(String cc : this.chestSquares){
            ccOdds += this.odds.get(cc);
        }
        double adjustment = ccOdds / (double)this.numOfCards;  //odds of drawing any card
        double jailOdds = this.odds.get(this.jail) + adjustment;
        double goOdds = this.odds.get(this.go) + adjustment;
        
        //add all adjusted values back to this.odds:
        this.odds.put(this.jail, jailOdds);
        this.odds.put(this.go, goOdds);
        
        //adjust odds of staying on community chest square
        for(String cc : this.chestSquares){
            ccOdds = this.odds.get(cc);
            //as 2 cards move player to another square:
            ccOdds *= (this.numOfCards -2)/(double)this.numOfCards; 
            this.odds.put(cc, ccOdds);
        }
    }

    
    //helper method for handleDependentCH (which is a helper method for handleCH())
    //finds next railroad after a given chance square (parameter ch)
    public String findNextRailroad(String ch){
        for(int i = Integer.parseInt(ch)+1; i < this.numOfSquares; ++i){
            String check = normalizeKey(Integer.toString(i));
            if(this.rrSquares.contains(check)){
                return check;
            }
        }
        //otherwise, we may have "lapped" the board and should check from the beginning:
        for(int i = 0; i < this.numOfSquares; ++i){
            String check = normalizeKey(Integer.toString(i));
            if(this.rrSquares.contains(check)){
                return check;
            }
        }
        return "ERROR"; //should never hit this point if it passed original errorCheck() method
    }
    
    
    //helper method for handleDependentCH (which is a helper method for handleCH())
    //finds next utility square after a given chance square (parameter ch)
    public String findNextUtility(String ch){
        for(int i = Integer.parseInt(ch)+1; i < this.numOfSquares; ++i){
            String check = normalizeKey(Integer.toString(i));
            if(this.utSquares.contains(check)){
                return check;
            }
        }
        //otherwise, we may have "lapped" the board and should check from the beginning:
        for(int i = 0; i < this.numOfSquares; ++i){
            String check = normalizeKey(Integer.toString(i));
            if(this.utSquares.contains(check)){
                return check;
            }
        }
        return "ERROR"; //should never hit this point if it passed original errorCheck() method
    }
            
            
    //helper method for handleDependentCH (which is a helper method for handleCH())
    //finds square 3 spaces back from given chance square (parameter ch)
    public String goBack3(String ch){
        int chInt = Integer.parseInt(ch);
        chInt -= 3;
        //check for negative values (which are invalid) - note, mod in java allows negative numbers
        if(chInt < 0){
            chInt += this.numOfSquares;
        }
        return normalizeKey(Integer.toString(chInt));
    }
    
    
    //helper method for handleCH(). Adjusts odds for relevant spaces that are dependent on which
    //chance square they are called from; i.e. "Go Back 3 Spaces" or "Go to Next Railroad".
    public void handleDependentCH(){
        //handle by each chance square:
        for(String ch : this.chanceSquares){
            double chOdds = this.odds.get(ch);
            //first, railroads:
            String nextR = findNextRailroad(ch);
            double adjustment = (chOdds/(double)this.numOfCards)*2; //as there are 2 railroad cards
            double rrOdds = this.odds.get(nextR) + adjustment;
            this.odds.put(nextR, rrOdds);
            //next, utility:
            String nextUt = findNextUtility(ch);
            adjustment = chOdds / (double)this.numOfCards;
            double utOdds = this.odds.get(nextUt) + adjustment;
            this.odds.put(nextUt, utOdds);
            //lastly, "Go Back 3 Squares":
            String back3 = goBack3(ch);
            double back3Odds = this.odds.get(back3) + adjustment;
            this.odds.put(back3, back3Odds);
            //note: chance odds are adjusted back in calling method, handleCH().
        }
    }

    
    //adjusts odds of relevant squares based on chance cards
    public void handleCH(){
        double chOdds = 0.0;    //odds of landing on one of the chance squares
        for(String ch : this.chanceSquares){
            chOdds += this.odds.get(ch);
        }
        double adjustment = chOdds / (double)this.numOfCards;      //odds of drawing any card = 1/16
        //adjust current odds for "move to X" cards:
        double jailOdds = this.odds.get(this.jail) + adjustment;
        double goOdds = this.odds.get(this.go) + adjustment;
        double c1Odds = this.odds.get("11") + adjustment;    //square C1 = "11"
        double e3Odds = this.odds.get("24") + adjustment;    //square E3 = "24"
        double h2Odds = this.odds.get("39") + adjustment;    //square H2 = "39"
        double r1Odds = this.odds.get("05") + adjustment;    //square R1 = "05"
        this.odds.put(this.jail, jailOdds);
        this.odds.put(this.go, goOdds);
        this.odds.put("11", c1Odds);
        this.odds.put("24", e3Odds);
        this.odds.put("39", h2Odds);
        this.odds.put("05", r1Odds);
        
        //handle spaces dependent on particular chance square (i.e. "Go to Next Railroad")
        handleDependentCH();
        
        //adjust odds of staying on ch squares:
        for(String ch: this.chanceSquares){
            chOdds = this.odds.get(ch);
            //as 10 cards move player to another (non chance) square:
            chOdds *= (this.numOfCards - 10)/(double)this.numOfCards;  
            this.odds.put(ch, chOdds);
        }
    }
    
    
    //adjusts odds of going to jail, given 3 doubles in a row = straight to jail
    public void handle3Doubles(){
        double temp = this.odds.get(this.jail);
        temp += this.odds3Doubles;
        this.odds.put(this.jail, temp);
        for(int i = 0; i < this.numOfSquares; ++i){
            String square = normalizeKey(Integer.toString(i));
            //doubles can be rolled from all spaces except "Go to Jail", as no turn can start there
            //per prob. definition, rolling doubles from jail sends you back to jail (paid to get out)
            if(square.equals(this.g2J)){
                continue;
            }
            double currentOdds = this.odds.get(square);
            double adjustedOdds;
            if(this.g2J.equals("")){
                adjustedOdds = currentOdds - (this.odds3Doubles / (this.numOfSquares));
            }
            else{
                adjustedOdds = currentOdds - (this.odds3Doubles / (this.numOfSquares-1));
            }
            this.odds.put(square, adjustedOdds);
        }
    }
            
    
    //accounts for "Go To Jail" space. Should be adjusted very last 
    public void handleG2J(){
        if(this.g2J.equals("")){
            return;
        }
        double g2JOdds = this.odds.get(this.g2J);
        double jailOdds = this.odds.get(this.jail);
        this.odds.put(this.jail, (g2JOdds + jailOdds));
        this.odds.put(this.g2J, 0.0);
    }
    
    
    //basic helper method - changes '0' to '00' and '1' to '01' but '11' etc would remain unchanged.
    //ensures consistency in labels (all labels must be exactly 2 letters)
    public String normalizeKey(String key){
        if(key.length() == 1){
            key = "0" + key;
        }
        return key;
    }
        
     
    //helper method for printOdds().
    //prints the spaces with the three highest probabilities.
    public void printMax3(){
        double max1 = 0.0;
        double max2 = 0.0;
        double max3 = 0.0;
        String max1S = "";
        String max2S = ""; 
        String max3S = "";
        for(int i = 0; i < this.numOfSquares; ++i){
            String key = normalizeKey(Integer.toString(i));
            double val = this.odds.get(key);
            if(val > max1){
                max3 = max2;
                max3S = max2S;
                max2 = max1;
                max2S = max1S;
                max1 = val;
                max1S = key;
            }
            else if(val > max2){
                max3 = max2;
                max3S = max2S;
                max2 = val;
                max2S = key;
            }
            else if(val > max3){
                max3 = val;
                max3S = key;
            }
        } 
        System.out.println("\n\nThe top 3 spaces by probability are:");
        System.out.printf("[%s]: %.4f%%\t[%s]: %.4f%%\t[%s]: %.4f%%\n", max1S, max1*100, max2S, max2*100, max3S, max3*100);
    }
                
    
    //prints this.odds HashMap, i.e. probabilities for each of the 40 spaces 
    public void printOdds(){
        for(int i = 0; i < this.numOfSquares; ++i){
            if(i % 5 == 0){
                System.out.println();
            }
            String key = normalizeKey(Integer.toString(i));
            System.out.printf("[%s]: %.4f%%\t", key, this.odds.get(key) * 100);
        }
        printMax3();
    }
        
        
    //performs basic error checking. See the console prints below for more information.
    public void errorCheck(){
        if(!this.chanceSquares.isEmpty()){
            if(this.rrSquares.isEmpty() | this.utSquares.isEmpty() | this.go.equals("") | this.jail.equals("")){
                System.out.println("Chance Squares require at least one Railroad and Utility squares exist, as well as one Go and Jail square. Please either add these, or remove all Chance squares.");
                System.exit(-1);
            }
        }
        if(!this.chestSquares.isEmpty()){
            if(this.go.equals("") | this.jail.equals("")){
                System.out.println("Community Chest Squares depend on one Go and one Jail square existing. Please either add these, or remove all Community Chest squares.");
                System.exit(-1);
            }
        }
    }
            
    
    //ensures all probabilities at the end add to 100%. Does not guarantee correctness, however.
    //uses an epsilon of .0001 (1/100 of one percent - BigDecimal class would have better precision)
    public void checkAnswer(){
        double sum = 0.0;
        for(int i = 0; i < this.numOfSquares; ++i){
            String key = normalizeKey(Integer.toString(i)); 
            sum += this.odds.get(key);
        }
        if(Math.abs(sum - 1.0) <= .0001){
            System.out.printf("\nProbabilites correctly sum to 1.0 (100%%).\n\n");
        }
        else{
            System.out.printf("\nERROR: probabilties sum to: %.8f%%\n", sum);
        }
    }
            
        
    //This is the driver | entry method, ensures correct sequence of odd-adjustment
    public void driver(){
        initBoard();
        errorCheck();       //ensures at least 1 railroad, utility, etc square exist (req. for chance)
        handle3Doubles();   //called first, as rolling 3 doubles automatically = jail
        handleCH();         //must be called before handleCC() for correct results
        handleCC();         //can be called anytime after handle3Doubles() and handleCH()
        handleG2J();        //handle "Go to Jail" space (if applicable). Should be done last.
        printOdds();
        checkAnswer();      //does not verify correctness, but ensures odds approx. add to 100%
    }
    
    
    //testing method
    public static void main(String[] args){
        Scanner sc = new Scanner(System.in);
        System.out.printf("Enter the number of sides per dice: ");
        int sides = sc.nextInt();
        Monopoly test = new Monopoly(sides);
        test.driver();
    }
}
    
    
