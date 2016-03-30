package com.ldnel;

import java.io.*;
import java.util.ArrayList;

/**
 * Created by ldnel_000 on 2015-11-04.
 */
public class Normalizer {

    /*
    Utility Functions
     */

    public static FunctionalDependency parseFDString(String inputLine) {
        if (inputLine == null || inputLine.length() == 0) return null;

        //strip off comments
        int commentIndex = inputLine.indexOf("//");
        if (commentIndex > -1) inputLine = inputLine.substring(0, commentIndex).trim();

        if (inputLine.equals("")) return null;

        //Expecting inputLine like name,address -> property1,property2

        int arrowIndex = inputLine.indexOf("->");
        if(arrowIndex == -1) return null; //not valid functional dependency

        String LHS = inputLine.substring(0, arrowIndex).trim();
        String RHS = inputLine.substring(arrowIndex + 2, inputLine.length()).trim();

        //System.out.println(LHS + " -> " + RHS);

        String[] LHSAttributes = LHS.split(",");
        String[] RHSAttributes = RHS.split(",");
        AttributeSet LeftAttributes = new AttributeSet();
        AttributeSet RightAttributes = new AttributeSet();


        for (String s : LHSAttributes) LeftAttributes.add(new Attribute(s.trim()));
        for (String s : RHSAttributes) RightAttributes.add(new Attribute(s.trim()));

        if (!LeftAttributes.isEmpty() && !RightAttributes.isEmpty()) {
            return new FunctionalDependency(LeftAttributes, RightAttributes);
        }
        else
            return null;
    }

    public static DependencySet parseInputFile(File inputFile){

		/*
		 * Parse the input data file and produce the set of functional dependencies it represents
		 *
		 * Input file is expected to be a text file with one dependency per line.
		 * Attributes are separated by commas
		 * Comments are any content at appears after "//" on a line
		 * Comments will be stripped away in the parse
		 *
		 * Example input file format:
		 *
		 *       //From previous midterm
         *       U,V->W,X,Y,Z  //U,V is superkey
         *       X->W
         *       V->X
		 */

        System.out.println("Parse File Data:");

        if(inputFile == null) return null;

        DependencySet aDependencySet = new DependencySet();

        BufferedReader inputFileReader;

        String inputLine; //current input line
        try{
            inputFileReader= new BufferedReader(new FileReader(inputFile));

            while((inputLine = inputFileReader.readLine()) != null){

                System.out.println(inputLine);
                FunctionalDependency fd = parseFDString(inputLine);
                if(fd != null) aDependencySet.add(fd);

            } //end while


        }catch (EOFException e) {
            System.out.println("File Read Error: EOF encountered, file may be corrupted.");
        } catch (IOException e) {
            System.out.println("File Read Error: Cannot read from file.");
        }


        System.out.println("END Data Parse");

        return aDependencySet;

    }

    public static Relation findRedunantTable(ArrayList<Relation> database){

        //Find and return any relation within database whose attributes are all contained within another
        //table
        for(Relation r : database){
            for(Relation r2 : database){
                if(r != r2 && r2.containsAll(r)) return r;

            }
        }
        return null;
    }
}
