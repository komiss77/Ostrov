package ru.komiss77.objects;

import java.util.Collection;
import java.util.Comparator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

//сортировка ключа по возрастанию!!
public class ValueSortedMap<K extends Comparable<K>, V extends Comparable<V>> extends TreeMap<K, V> {

    private static final long serialVersionUID = -5867567955420954905L;
    private final boolean backOrder;

  public ValueSortedMap() {
    backOrder = false;
  }
  public ValueSortedMap(boolean backOrder) {
    this.backOrder = backOrder;
  }

  @Override
    public Set<Entry<K, V>> entrySet() {
        Set<Entry<K, V>> originalEntries = super.entrySet();
        Set<Entry<K, V>> sortedEntry = new TreeSet<>(new Comparator<Entry<K, V>>() {
            @Override
            public int compare(Entry<K, V> entryA, Entry<K, V> entryB) {
              if (backOrder) {
                int compareTo = entryB.getValue().compareTo(entryA.getValue());
                if (compareTo == 0) {
                  compareTo = entryB.getKey().compareTo(entryA.getKey());
                }
                return compareTo;
              } else {
                int compareTo = entryA.getValue().compareTo(entryB.getValue());
                if (compareTo == 0) {
                  compareTo = entryA.getKey().compareTo(entryB.getKey());
                }
                return compareTo;
              }

            }
        });
        sortedEntry.addAll(originalEntries);
        return sortedEntry;
    }

    @Override
    public Collection<V> values() {
        Set<V> sortedValues = new TreeSet<>(new Comparator<V>() {
            @Override
            public int compare(V vA, V vB) {
                return vA.compareTo(vB);
            }
        });
        sortedValues.addAll(super.values());
        return sortedValues;
    }

}
