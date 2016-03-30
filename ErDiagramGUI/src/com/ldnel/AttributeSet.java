package com.ldnel;

/**
 * Created by ldnel_000 on 2015-11-04.
 */
import java.util.ArrayList;

public class AttributeSet {
	/*
	 * This class represents a set of relation or functional dependency attributes
	 * or table columns.
	 * The set prevents duplicates via checks in the add() method.
	 * Elements within the set should NOT be modified directly as this will not
	 * guarantee removal of duplicates. Instead elements should be removed,
	 * modified and then re-inserted using add() to guarantee that duplicates are suppressed
	 */

    private ArrayList<Attribute> elements;

    public AttributeSet() {
        elements = new ArrayList<Attribute>();
    }
    public AttributeSet(Attribute a) {
        elements = new ArrayList<Attribute>();
        this.add(a);
    }

    public AttributeSet(AttributeSet anAttributeSet) {
        elements = new ArrayList<Attribute>();
        this.addAll(anAttributeSet);
    }

    public ArrayList<Attribute> getElements() {return elements;}

    public void add(Attribute anAttribute) {
        //Add anAttribute without duplication
        if(anAttribute == null) return;

        for(Attribute a : elements) if(a.equals(anAttribute)) return; //don't add duplicates
        elements.add(anAttribute);
    }
    public void addAll(AttributeSet anAttributeSet) {
        for(Attribute a : anAttributeSet.elements)
            this.add(a);
    }
    public void remove(Attribute anAttribute) {
        //remove all equal to anAttribute
        AttributeSet itemsToRemove = new AttributeSet();
        for(Attribute a : elements) if(a.equals(anAttribute)) itemsToRemove.add(a);
        elements.removeAll(itemsToRemove.elements);

    }
    public AttributeSet copy(){
        AttributeSet theCopy = new AttributeSet();
        theCopy.addAll(this);
        return theCopy;
    }
    public boolean isEmpty() {return elements.isEmpty(); }
    public int size() {return elements.size();} //answer number of attributes in this set
    public void clear(){ elements.clear(); }
    public boolean contains(Attribute anAttribute){
        //answer whether this contains an Attribute equal to anAttribute
        for(Attribute a : elements)
            if(a.equals(anAttribute)) return true;

        return false;
    }
    public boolean containsAll(AttributeSet anAttributeSet){
        //answer whether this contains all attributes in anAttributeSet
        if(anAttributeSet.isEmpty()) return true;

        for(Attribute a : anAttributeSet.elements)
            if(!this.contains(a)) return false;

        return true;
    }

    public boolean equals(AttributeSet anAttributeSet){
        //two attribute sets are equal if the are mutually subsets of each others
        if(!anAttributeSet.containsAll(this)) return false;
        if(!this.containsAll(anAttributeSet)) return false;
        return true;

    }

    public AttributeSet closure(DependencySet F){

		/*Compute and return the closure of an attribute set with respect to a set of functional
		 * dependencies F
		 * The closure are all those attributes that are implied by this wrt. F using
		 * Armstrong's axioms
		 */

        AttributeSet previous = new AttributeSet();

        AttributeSet current = new AttributeSet(this);	//reflexive rule
        while(!current.equals(previous)){
            previous = current;
            current = new AttributeSet(current);
            for(FunctionalDependency fd : F.getElements()){
                if(current.containsAll(fd.getLHS())) current.addAll(fd.getRHS());
            }
        }

        return current;

    }

    public SetOfAttributeSets allCandidateKeys(DependencySet theFDs){
		/*
		 * Return a Set of all the minimal candidate keys of a relation consisting of theAttributes
		 * with respect to the functional dependencies: theFDs
		 *
		 *
		 * Approach:
		 * Start with the complete set of attributes as a super key and recursively
		 * decompose it leaving only minimal keys.
		 */

        int MAX_NUMBER_OF_ATTRIBUTES_FOR_ALL_KEYS_FIND = 10;

        SetOfAttributeSets initialSuperKeys = new SetOfAttributeSets();
        AttributeSet key = this.copy();
        initialSuperKeys.add(key);

        //System.out.println("R: " + theAttributes);
        //System.out.println("FDs: " + theFDs);
        //System.out.println("INITIAL KEY: " + key);

        if(this.size() <= MAX_NUMBER_OF_ATTRIBUTES_FOR_ALL_KEYS_FIND)
            return candidateKeys( initialSuperKeys, theFDs);
        else{
            System.out.println("WARNING: Too many attributes to find all possible keys, RETURNING NULL");

            return null;
        }

    }

    AttributeSet findCandidateKey(DependencySet theFDs){
        AttributeSet candidateKey = this.copy();
        for(Attribute a : this.getElements()){
            AttributeSet tryCandidate = candidateKey.copy();
            tryCandidate.remove(a);
            if(tryCandidate.closure(theFDs).containsAll(this)) candidateKey = tryCandidate;
        }

        return candidateKey;
    }

    private  SetOfAttributeSets candidateKeys( SetOfAttributeSets superkeys, DependencySet theFDs){

		/*
		 * Answer a Set of all the minimal candidate keys the a relation consisting of attributes: theAttrubutes
		 * with respect to a set of functional dependencies: theFDs
		 * given an initial set of super keys: superkeys.
		 *
		 * WARNING: this is a computationally expensive recursion (exponential time)
		 * It is intended for only small attributes sets.
		 * For large sets use the candidateKey() method that only finds one
		 * candidate key

		 * theAttributes: the attributes defining the relation
		 * theFDs: the functional dependencies that apply to the relation
		 * superkeys: a Set of superkeys of the relation wrt the functional dependencies.
		 *
		 * Approach:
		 * recursively try to minimize all the superkeys until only minimal keys are left and
		 * return that set
		 *
		 */

        //System.out.println("----------------------------------------------------");
        //System.out.println("SUPERKEYS: " + superkeys);

        //basis cases
        if(superkeys.isEmpty()) return superkeys; //return an empty set


        //recursive cases
        AttributeSet aSuperkey = superkeys.get(0);
        //System.out.println("A SUPERKEY: " + aSuperkey);

        superkeys.remove(aSuperkey);


        if(aSuperkey.size()== 1){
            //can't make it any smaller
            SetOfAttributeSets candidateKeys = new SetOfAttributeSets();
            candidateKeys.add(aSuperkey);
            candidateKeys.addAll(candidateKeys(superkeys, theFDs));
            return candidateKeys;

        }

        //try removing an attribute to minimize the superkey
        AttributeSet attributesToRemove = aSuperkey.copy();
        SetOfAttributeSets newCandidates = new SetOfAttributeSets();

        for(Attribute a : attributesToRemove.getElements()){
            AttributeSet newPossibleKey = aSuperkey.copy();
            newPossibleKey.remove(a);
            if(newPossibleKey.closure(theFDs).containsAll(this))
                newCandidates.add(newPossibleKey);

        }

        //System.out.println("New Candidates: " + newCandidates);

        if(newCandidates.isEmpty()){
            SetOfAttributeSets candidateKeys = new SetOfAttributeSets();
            candidateKeys.add(aSuperkey);
            candidateKeys.addAll(candidateKeys(superkeys, theFDs));
            return candidateKeys;

        }
        else {
            SetOfAttributeSets candidateKeys = new SetOfAttributeSets();
            for(AttributeSet candidate : newCandidates.getElements()){
                SetOfAttributeSets candidateKeyAsSet = new SetOfAttributeSets();
                candidateKeyAsSet.add(candidate);
                candidateKeys.addAll(candidateKeys(candidateKeyAsSet, theFDs));

            }
            candidateKeys.addAll(candidateKeys(superkeys, theFDs));
            return candidateKeys;


        }

    }

    public String toString(){
        String returnString = "";
        for(Attribute a : elements){
            returnString = returnString + a.toString() + ",";

        }
        if(returnString.endsWith(",")) //strip off last ","
            returnString = returnString.substring(0, returnString.length()-1);

        return returnString;

    }

    public void printToSystemOut(){
        //System.out.println("Attribute Set:");
        //System.out.println("--------------");
        for(int i=0; i<elements.size(); i++){
            System.out.println(elements.get(i));
        }

    }

}