package com.ldnel;

/**
 * Created by ldnel_000 on 2015-11-04.
 */
public class FunctionalDependency {

    //This class represents a functional dependency lhs -> rhs

    private AttributeSet lhs; //left hand side
    private AttributeSet rhs; //right hand side

    public FunctionalDependency(AttributeSet aLHS, AttributeSet aRHS){

        if(aLHS == null || aRHS == null)
            System.out.println("ERROR: NULL ATTRITBUTE SET");
        if(aLHS.isEmpty() || aRHS.isEmpty())
            System.out.println("ERROR: EMPTY ATTRITBUTE SET");

        lhs = new AttributeSet();
        rhs = new AttributeSet();
        lhs.addAll(aLHS);
        rhs.addAll(aRHS);

    }


    public AttributeSet getLHS(){return lhs;}
    public AttributeSet getRHS(){return rhs;}

    public boolean equals(FunctionalDependency anFD){
		/*]
		 * Perform an equality check based on whether both the left hand side and right hand side
		 * sets are equal. That is this.lhs equals anFD.lhs and this.rhs equals anFD.rhs.
		 * This is not an equality check based on whether one FD can be inferred
		 * from another based in Armstrong's Axioms. Rather it is a simple equality check
		 * based on whether the left hand sides and right hand sides of this and anFD are
		 * equal as sets.
		 */

        if(!lhs.equals(anFD.lhs)) return false;
        if((!rhs.equals(anFD.rhs))) return false;
        return true;
    }

    public boolean isTrivial(){
        //Answer whether this functional dependency is trivial.
        //A dependency X->Y is trivial is Y is a subset of X

        if(rhs.isEmpty()) return true;
        return lhs.containsAll(rhs); }

    public String toString(){return lhs.toString() + " -> " + rhs.toString();}

}