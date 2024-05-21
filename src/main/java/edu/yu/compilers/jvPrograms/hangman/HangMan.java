package edu.yu.compilers.jvPrograms.hangman;

public class Hangman
{
    private static java.util.Scanner _sysin = new java.util.Scanner(System.in);

    public static int convertLetterToIndex(String letter){
        {
            if(letter.equals("a")){
                {

                    return 0;
                }
            }
            if(letter.equals("b")){
                {

                    return 1;
                }
            }
            if(letter.equals("c")){
                {

                    return 2;
                }
            }
            if(letter.equals("d")){
                {

                    return 3;
                }
            }
            if(letter.equals("e")){
                {

                    return 4;
                }
            }
            if(letter.equals("f")){
                {

                    return 5;
                }
            }
            if(letter.equals("g")){
                {

                    return 6;
                }
            }
            if(letter.equals("h")){
                {

                    return 7;
                }
            }
            if(letter.equals("i")){
                {

                    return 8;
                }
            }
            if(letter.equals("j")){
                {

                    return 9;
                }
            }
            if(letter.equals("k")){
                {

                    return 10;
                }
            }
            if(letter.equals("l")){
                {

                    return 11;
                }
            }
            if(letter.equals("m")){
                {

                    return 12;
                }
            }
            if(letter.equals("n")){
                {

                    return 13;
                }
            }
            if(letter.equals("o")){
                {

                    return 14;
                }
            }
            if(letter.equals("p")){
                {

                    return 15;
                }
            }
            if(letter.equals("q")){
                {

                    return 16;
                }
            }
            if(letter.equals("r")){
                {

                    return 17;
                }
            }
            if(letter.equals("s")){
                {

                    return 18;
                }
            }
            if(letter.equals("t")){
                {

                    return 19;
                }
            }
            if(letter.equals("u")){
                {

                    return 20;
                }
            }
            if(letter.equals("v")){
                {

                    return 21;
                }
            }
            if(letter.equals("w")){
                {

                    return 22;
                }
            }
            if(letter.equals("x")){
                {

                    return 23;
                }
            }
            if(letter.equals("y")){
                {

                    return 24;
                }
            }
            if(letter.equals("z")){
                {

                    return 25;
                }
            }

            return -1;
        }
    }
    public static String displayWord(String word, boolean[] guessedLetters){
        {
            String display = "";
            for(var i = 0;i<word.length(); i =i+1)
            {
                String letter = String.valueOf(word.charAt(i));
                int index = convertLetterToIndex(letter);
                if(index==-1){
                    {
                        System.out.printf(String.valueOf("This is not a good letter"));
                    }
                }
                if(guessedLetters[index]==true){
                    {
                        display = display+letter;
                        display = display+" ";
                    }
                }
                else{
                    {
                        display = display+"_ ";
                    }
                }
            }


            return display;
        }
    }

    public static void main(String[] args)

    {
        String playerWord = args[0];
        String maxGuesses = args[1];
        int MAX_TRIES = Integer.parseInt(maxGuesses);
        var guessedLetters = new boolean[26];
        int tries = 0;
        int numOfGuessesUserMade = 0;
        System.out.printf(String.valueOf("Welcome. Try and guess my secret word: :"));
        String displayToUser = displayWord(playerWord,guessedLetters);
        System.out.printf(String.valueOf(displayToUser));
        System.out.println();
        System.out.printf(String.valueOf("You are allowed "));
        System.out.printf(String.valueOf(maxGuesses));
        System.out.printf(String.valueOf(" strikes"));
        System.out.println();
        System.out.println();
        boolean exitLoop = false;
        do
        {
            {
                boolean alreadyGuessedThisLetter = false;
                System.out.printf(String.valueOf("Select a letter: "));
                String guess = String.valueOf(_sysin.next().charAt(0));
                numOfGuessesUserMade = numOfGuessesUserMade+1;
                int index = convertLetterToIndex(guess);
                if(guessedLetters[index]==true){
                    {
                        alreadyGuessedThisLetter = true;
                    }
                }
                guessedLetters[index] = true;
                int howMany = 0;
                for(var i = 0;i<playerWord.length(); i =i+1)
                {
                    String letterInWord = String.valueOf(playerWord.charAt(i));
                    if(letterInWord.equals(guess)){
                        {
                            howMany = howMany+1;
                        }
                    }
                }

                if(alreadyGuessedThisLetter==true){
                    {
                        System.out.printf(String.valueOf("'"));
                        System.out.printf(String.valueOf(guess));
                        System.out.printf(String.valueOf("' was already guessed: "));
                        displayToUser = displayWord(playerWord,guessedLetters);
                        System.out.printf(String.valueOf(displayToUser));
                        System.out.println();
                    }
                }
                else{
                    {
                        if(howMany>0){
                            {
                                System.out.printf(String.valueOf("The letter '"));
                                System.out.printf(String.valueOf(guess));
                                System.out.printf(String.valueOf("' appears "));
                                System.out.printf(String.valueOf(howMany));
                                System.out.printf(String.valueOf(" time(s):"));
                                displayToUser = displayWord(playerWord,guessedLetters);
                                System.out.printf(String.valueOf(displayToUser));
                                System.out.println();
                            }
                        }
                        else{
                            {
                                tries = tries+1;
                                System.out.printf(String.valueOf("The letter '"));
                                System.out.printf(String.valueOf(guess));
                                System.out.printf(String.valueOf("' does not appear. You have "));
                                System.out.printf(String.valueOf(tries));
                                System.out.printf(String.valueOf(" strike(s): "));
                                displayToUser = displayWord(playerWord,guessedLetters);
                                System.out.printf(String.valueOf(displayToUser));
                                System.out.println();
                            }
                        }
                    }
                }
                boolean allLettersGuessed = true;
                for(var i = 0;i<playerWord.length(); i =i+1)
                {
                    String upToLetter = String.valueOf(playerWord.charAt(i));
                    int covertIndex = convertLetterToIndex(upToLetter);
                    if(!guessedLetters[covertIndex]){
                        {
                            allLettersGuessed = false;
                        }
                    }
                }

                if(allLettersGuessed){
                    {
                        exitLoop = true;
                    }
                }
            }
        }
        while ((tries<MAX_TRIES&&exitLoop==false));
        if(tries==MAX_TRIES){
            {
                System.out.printf(String.valueOf("You have struck out. Goodbye!"));
            }
        }
        else{
            {
                System.out.printf(String.valueOf("You won with "));
                System.out.printf(String.valueOf(numOfGuessesUserMade));
                System.out.printf(String.valueOf(" guesses. Goodbye!"));
                System.out.println();
            }
        }
    }
}