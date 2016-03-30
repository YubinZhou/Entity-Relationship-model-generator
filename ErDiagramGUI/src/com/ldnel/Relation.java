package com.ldnel;

/**
 * Created by ldnel_000 on 2015-11-04.
 */
public class Relation {

	/*
	 * This class represents a database relation, or relational table
	 */

    private AttributeSet attributes = null; //attributes or columns of the table
    private AttributeSet primaryKey = null; //designated primary key

    public Relation(AttributeSet theAttributes, AttributeSet key){

        if(theAttributes == null)
            System.out.println("ERROR: Attribute set cannot be null");
        if(theAttributes.isEmpty())
            System.out.println("ERROR: EMPTY ATTRITBUTE SET");

        if(key != null && !theAttributes.containsAll(key))
            System.out.println("ERROR: PRIMARY KEY MUST BE A SUBSET OF THE ATTRIBUTES");

        attributes = new AttributeSet();
        attributes.addAll(theAttributes);
        if(key != null){
            primaryKey = new AttributeSet();
            primaryKey.addAll(key);
        }
    }
    public Relation(FunctionalDependency FD){

			/* create a relation out of the functional dependency FD
			 * The left hand side becomes the primary key
			 */

        if(FD == null)
            System.out.println("ERROR: Cannot create table out of null dependency");
        if(FD.getLHS().isEmpty() || FD.getRHS().isEmpty())
            System.out.println("ERROR: Cannot create table out of empty dependency");


        attributes = new AttributeSet();
        attributes.addAll(FD.getLHS());
        attributes.addAll(FD.getRHS());

        primaryKey = new AttributeSet();
        primaryKey.addAll(FD.getLHS());

    }

    public AttributeSet getAttributes() {return attributes;}
    public AttributeSet getPrimaryKey() {return primaryKey; }

    public boolean containsAll(Relation r){
        return attributes.containsAll(r.attributes);
    }

    public String toString(){

        String returnString = "[";
        for(Attribute a : attributes.getElements()){
            if(primaryKey.contains(a)) returnString = returnString + a + ",";
        }
        returnString = returnString.substring(0, returnString.length()-1);  //strip off last ","
        returnString = returnString + " | ";

        for(Attribute a : attributes.getElements()){
            if(!primaryKey.contains(a)) returnString = returnString + a + ",";
        }
        returnString = returnString.substring(0, returnString.length()-1);  //strip off last ","
        returnString = returnString + "]";

        return returnString;
    }


}