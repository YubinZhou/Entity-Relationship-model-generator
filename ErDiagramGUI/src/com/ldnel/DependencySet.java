package com.ldnel;

/**
 * Created by ldnel_000 on 2015-11-04.
 */
import javafx.scene.control.TextArea;

import java.util.ArrayList;

public class DependencySet {
	/*
	 * This class represents a set of functional dependencies
	 * The set prevents duplicates via checks in the add() method.
	 * Elements within the set should NOT be modified while in the set
	 * as this will not guarantee removal of duplicates.
	 * Instead elements should be removed, modified and then re-inserted
	 * to guarantee that duplicates are suppressed
	 */

    private ArrayList<FunctionalDependency> elements;

    public DependencySet() { elements = new ArrayList<FunctionalDependency>(); }

    public void add(FunctionalDependency anFD) {
        //add anFD without duplications
        if(anFD == null) return;

        for(FunctionalDependency fd : elements) if(fd.equals(anFD)) return; //don't add duplicates

        elements.add(anFD);
    }
    public void addAll(DependencySet anFDSet) {
        //add anFD without duplications
        if(anFDSet == null) return;

        for(FunctionalDependency fd : anFDSet.elements)
            this.add(fd);
    }


    public void remove(FunctionalDependency anFD) {
        //remove all items equal to anFD

        DependencySet itemsToRemove = new DependencySet();

        for(FunctionalDependency fd : elements) if(fd.equals(anFD)) itemsToRemove.add(fd);

        elements.removeAll(itemsToRemove.elements);
    }
    public DependencySet copy(){
        //return a shallow copy of this dependency set
        DependencySet theCopy = new DependencySet();
        theCopy.addAll(this);
        return theCopy;
    }
    public ArrayList<FunctionalDependency> getElements() {return elements; }
    public boolean isEmpty() {return elements.isEmpty(); }
    public void clear() {elements.clear(); }

    public AttributeSet getAllAttributes(){
        //Answer an AttributeSet of all the attributes
        //that occur in this set of functional dependencies

        AttributeSet theAttributes = new AttributeSet();
        for(FunctionalDependency fd : elements){
            theAttributes.addAll(fd.getLHS());
            theAttributes.addAll(fd.getRHS());
        }
        return theAttributes;
    }

    public boolean equals(DependencySet F){
    	/* Answer whether this dependency set equals the set F
    	 * Two dependency sets are equal if each can generate all the
    	 * dependencies of the other under closure of attribute sets.
    	*/
        for(FunctionalDependency fd : this.elements){
            AttributeSet closure = fd.getLHS().closure(F);
            if(!closure.containsAll(fd.getRHS())) return false;

        }
        for(FunctionalDependency fd : F.elements){
            AttributeSet closure = fd.getLHS().closure(this);
            if(!closure.containsAll(fd.getRHS())) return false;

        }

        return true;

    }

    private int removeRedundantLeftHandAttributes(DependencySet F){
        //find the first dependency in F that has a redundant left hand attribute and
        //replace it in F with one that has the redundant attribute removed
        //return the number of replacements done
        //return 0 if no change (improvement) has been made


        for(FunctionalDependency fd : F.copy().getElements()){
            if(fd.getLHS().size() > 1){
                //compound left hand side.

                AttributeSet leftAttributesToCheck = fd.getLHS().copy();

                for(Attribute a: leftAttributesToCheck.getElements() ){
                    AttributeSet newLHS = fd.getLHS().copy();
                    newLHS.remove(a);
                    FunctionalDependency newFD = new FunctionalDependency(newLHS,fd.getRHS());
                    DependencySet Fcopy = F.copy();
                    Fcopy.remove(fd);
                    Fcopy.add(newFD);

                    if(Fcopy.equals(F)) {
                        F.remove(fd);
                        F.add(newFD);
                        System.out.println("REPLACE: " + fd + " with " + newFD);
                        return 1; //made one modification
                    }

                    //System.out.println("TRY REPLACE: " + fd + " with " +  newFD + " in: " + Fcopy + " ::" + Fcopy.equals(F));
                    //System.out.println("CLOSURE: " + fd + " in " + F + " {" + fd.getLHS().closure(F)+ "}");
                    //System.out.println("CLOSURE: " + newFD + " in " + Fcopy + " {" + newFD.getLHS().closure(Fcopy)+ "}");
                    //System.out.println(" ");

                }

                //System.out.println("F restored: " +  F);


            }

        }

        return 0; //no modifications found
    }

    public DependencySet minCover() {

    	System.out.println("finding min cover:");

        //return a minCover of this dependency set
        DependencySet minCoverSoFar = null;

    	/*convert dependencies with compound right had sides to
    	 *ones with single attribute right hand side and remove
    	 *any obvious duplicates.
    	 *E.g. A,B->A,C,D => A,B->C, A,B->D (A,B->A eliminated because it is trivial)
    	 *
    	 */

        DependencySet singleRightHandSides = new DependencySet();
        for(FunctionalDependency fd : elements){
            if(fd.isTrivial()) {/*don' add it*/}
            else if(fd.getRHS().size() == 1) singleRightHandSides.add(new FunctionalDependency(fd.getLHS(), fd.getRHS()));
            else{
                //create a separate FD for each right hand side attribute
                for(Attribute a : fd.getRHS().getElements()){
                    FunctionalDependency newFD = new FunctionalDependency(fd.getLHS(), new AttributeSet(a));
                    if(!newFD.isTrivial()) singleRightHandSides.add(newFD);
                }
            }

        }

        minCoverSoFar = singleRightHandSides;


        System.out.println("-----------------------------------");
        System.out.println("SINGLE RIGHT HAND SIDES:");

        //minCoverSoFar.printToTextArea(FXApplicationMain.consoleTextArea);


    	/*
    	 * Remove any redundant attributes from the left hand side of dependencies
    	 * Replace dependencies with redundant left hand sides with ones with the
    	 * redundancy removed
    	 */
        System.out.println("-----------------------------------");
        System.out.println("REMOVAL OF REDUNDANT LHS ATTRIBUTES");

        while(removeRedundantLeftHandAttributes(minCoverSoFar) > 0) { }

        //minCoverSoFar.printToTextArea(FXApplicationMain.consoleTextArea);

        System.out.println("-----------------------------------");
        System.out.println("REMOVAL OF REDUNDANT DEPENDENCIES");

    	/*
    	 * Remove any unnecessary dependencies
    	 * A dependency X->Y is unnecessary in F if
    	 * X+ with respect to F-(X->Y) yields Y
    	 */

        for(FunctionalDependency fd : minCoverSoFar.copy().getElements()){
            minCoverSoFar.remove(fd);
            //System.out.println("CHECKING: " + fd + "  CLOSURE: " + fd.getLHS().closure(minCoverSoFar) + " WRT: " + minCoverSoFar);
            if(!fd.getLHS().closure(minCoverSoFar).containsAll(fd.getRHS())) {minCoverSoFar.add(fd);}
            else System.out.println("REMOVING: " + fd.toString());
        }

        //minCoverSoFar.printToTextArea(FXApplicationMain.consoleTextArea);


        DependencySet minCover = minCoverSoFar;

        System.out.println("\nEND finding min cover");

        return minCover;

    }

    /*
    public void printToSystemOut(){
        //System.out.println("Dependency Set:");
        //System.out.println("--------------");
        for(int i=0; i<elements.size(); i++){
            System.out.println(elements.get(i));
        }
    }
    */

    public void printToTextArea(TextArea area){
        //System.out.println("Dependency Set:");
        //System.out.println("--------------");
        for(int i=0; i<elements.size(); i++){
            area.appendText(elements.get(i).toString() + "\n");
        }
    }
    public String toString(){
        String returnString = "";
        for(int i=0; i<elements.size(); i++){
            returnString = returnString + elements.get(i) + " ";
        }
        return returnString;

    }


}