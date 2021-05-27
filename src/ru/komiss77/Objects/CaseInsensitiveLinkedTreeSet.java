package ru.komiss77.Objects;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.TreeSet;
/*
 * LinkedTreeSetCaseInsensitive combines the features of a LinkedList and a
 * TreeSet with case insensitive strings. The linked list will be in the order
 * that strings were added provided they didn't already exist in the TreeSet.
 * 
 * A number of methods throw UnsupportedOperationExceptions because they were
 * incompatible with the notions of lists and sets combined, or I got lazy.
 */
public class CaseInsensitiveLinkedTreeSet
implements List<String> {
    
    LinkedList<String>        linkedList = new LinkedList<>();
    TreeSet<String>           treeSet = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
 
    @Override
    public boolean add( String s )
    {
        if( treeSet.add( s ) )
        {
            linkedList.add( s );
            return true;
        }
        return false;
    }
 
    @Override
    public void add( int arg0, String arg1 )
    {
        throw new UnsupportedOperationException();
    }
 
    @Override
    public boolean addAll( Collection<? extends String> collection )
    {
        boolean             changed = false;
        for( String s : collection )
        {
            if( treeSet.add( s ) )
            {
                linkedList.add( s );
                changed = true;
            }
        }
        return changed;
    }
 
    @Override
    public boolean addAll( int arg0, Collection<? extends String> arg1 )
    {
        throw new UnsupportedOperationException();
    }
 
    @Override
    public void clear()
    {
        linkedList.clear();
        treeSet.clear();
    }
 
    @Override
    public boolean contains( Object arg0 )
    {
        return treeSet.contains( arg0 );
    }
 
    @Override
    public boolean containsAll( Collection<?> collection )
    {
        for( Object o : collection )
            if( ! treeSet.contains( o ) )
                return false;
        return true;
    }
 
    @Override
    public String get( int index )
    {
        return linkedList.get( index );
    }
 
    @Override
    public int indexOf( Object o )
    {
        throw new UnsupportedOperationException();
    }
 
    @Override
    public boolean isEmpty()
    {
        return linkedList.isEmpty();
    }
 
    @Override
    public Iterator<String> iterator()
    {
        return linkedList.iterator();
    }
 
    @Override
    public int lastIndexOf( Object o )
    {
        throw new UnsupportedOperationException();
    }
 
    @Override
    public ListIterator<String> listIterator()
    {
        throw new UnsupportedOperationException();
    }
 
    @Override
    public ListIterator<String> listIterator( int arg0 )
    {
        throw new UnsupportedOperationException();
    }
 
    @Override
    public boolean remove( Object arg0 )
    {
        throw new UnsupportedOperationException();
    }
 
    @Override
    public String remove( int arg0 )
    {
        throw new UnsupportedOperationException();
    }
 
    @Override
    public boolean removeAll( Collection<?> arg0 )
    {
        throw new UnsupportedOperationException();
    }
 
    @Override
    public boolean retainAll( Collection<?> arg0 )
    {
        throw new UnsupportedOperationException();
    }
 
    @Override
    public String set( int arg0, String arg1 )
    {
        throw new UnsupportedOperationException();
    }
 
    @Override
    public int size()
    {
        return linkedList.size();
    }
 
    @Override
    public List<String> subList( int arg0, int arg1 )
    {
        throw new UnsupportedOperationException();
    }
 
    @Override
    public Object[] toArray()
    {
        int             index = 0;
        Object[]        ary = new Object[ size() ];
        for( String s : linkedList )
            ary[ index++ ] = s;
        return ary;
    }
 
    @Override
    public <T> T[] toArray( T[] arg0 )
    {
        throw new UnsupportedOperationException();
    }
     
    //----------------------------------------------------------------------
    // very basic test
   /* public static void main( String[] args )
    {
        String startingString = "Hello hello Ab aB";
        List<String> caseSensitiveList = Arrays.asList(startingString.split("\\s"));
        CaseInsensitiveLinkedTreeSet caseInsensitiveSet = new CaseInsensitiveLinkedTreeSet();
        caseInsensitiveSet.addAll(caseSensitiveList);
  
        for (String s: caseInsensitiveSet) {
            System.out.println(s);
        }
    }*/
}