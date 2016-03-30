package com.ldnel;

/**
 * Created by ldnel_000 on 2015-11-04.
 */
public class Attribute implements Comparable{
    //This class represents a functional dependency attribute
    //Attribute equality is based on equality of the name string

    private String attributeName;

    public Attribute(String anAttributeName){ attributeName = anAttributeName; }
    public boolean equals(Attribute a){
        if(a == null) return false;
        return a.attributeName.equals(attributeName);
    }

    public String toString(){ return attributeName;}

    public int compareTo(Object arg) {
        if(!(arg instanceof Attribute)) return -1;
        Attribute a = (Attribute) arg;
        return attributeName.compareTo(a.attributeName);
    }

}