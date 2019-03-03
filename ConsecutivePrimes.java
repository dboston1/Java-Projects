import java.util.*;

// Drew Boston, 3/3/19

/* This program finds the longest sequence of primes that sums to a prime number. For example, when run with the maximum integer = 100, the answer should be 41, as 41 is prime, and:
41 = 2 + 3 + 5 + 7 + 11 + 13, all of which are consecutive prime numbers. Please note that this finds
the longest sequence, not the one with the highest value. 

This problem appears at this link: https://projecteuler.net/problem=50. */


public class ConsecutivePrimes{
    
    private HashSet<Integer> primes;    //list of all primes up to maxNum
    private List<Integer> primesList;   //used for quick indexing to check if sums are prime
    private int maxNum;                 //maximum value (given by user input)
    
    
    //constructor method
    public ConsecutivePrimes(int n){
        this.primes = new HashSet<Integer>();
        this.primesList = new ArrayList<Integer>();
        this.maxNum = n;
        
        //add 2 to primes list (saves complexity for sieve of eros. later)
        this.primes.add(2);
        this.primesList.add(2);
    }
    
    
    //essentially the driver method
    public void start(){
        populateLists();
        findConSum();
    }
    
    
    //prints the answer, number of terms, and the terms themselves to the console
    public void printResults(int maxPrime, int maxCount, int startIndex, int endIndex){
        System.out.printf("Answer is: %d\n", maxPrime);
        System.out.printf("Which equals the sum of the following [%d] consecutive primes: \n", maxCount);
        for(int i = startIndex; i <= endIndex; ++i){
            if(i != endIndex)
                System.out.printf(" %d, ", this.primesList.get(i));
            else
                System.out.printf(" %d\n", this.primesList.get(i));
        }
    }
    
    
    //finds largest prime number that is also a sum of consecutive primes
    //NOTE: possible optimizations - once we find the max length of consecutive primes seen so far,
    //only consider lengths of a greater size - this would cut down on a lot of duplicate calculation...
    
    public void findConSum(){
        int maxPrime = 1;
        int maxCount = 0;
        int startIndex = 0;
        int endIndex = 0;
        
        for(int i = 0; i < primesList.size()-1; ++i){
            int currentSum = this.primesList.get(i);
            int count = 1;
            for(int j = i+1; j < primesList.size(); ++j){
                count++;
                int nextVal = this.primesList.get(j);
                currentSum += nextVal;
                //to avoid int overflow, also cuts down complexity slightly
                if(currentSum > this.maxNum)
                    break;
                if(this.primes.contains(Integer.valueOf(currentSum)) && count > maxCount){
                    maxPrime = currentSum;
                    maxCount = count;
                    startIndex = i;
                    endIndex = j;
                }
            }
        }
        printResults(maxPrime, maxCount, startIndex, endIndex);
    }
    
    
    //non-threaded implementation of the sieve of eratosthenes (slow for large numbers)
    public void findPrimes(List<Integer> allNums){
        
        for(int i = 3; i <= maxNum; i+=2){
            if (allNums.contains(Integer.valueOf(i))){
                for(int j = 3 * i; j <= maxNum; j += (2*i)){
                    allNums.remove(Integer.valueOf(j));
                }
            }
        }
    }
    
    
    //setup for sieve of eratosthenes, also populates primes list and hashmap
    private void populateLists(){
        List<Integer> allNums = new ArrayList<Integer>();
        for(int i = 3; i <= maxNum; i+=2){
            allNums.add(i);
        }
        this.findPrimes(allNums);
        //populate primes / primesList with all primes
        for(int i = 0; i < allNums.size(); ++i){
            this.primes.add(allNums.get(i));
            this.primesList.add(allNums.get(i));
        }
    }
        
    
    //testing method, gets maximum integer from console / user input
    public static void main(String[] args){
        Scanner sc = new Scanner(System.in);
        System.out.printf("Enter the maximum Integer: ");
        int n = sc.nextInt();
        ConsecutivePrimes test = new ConsecutivePrimes(n);
        test.start();
    }
}
