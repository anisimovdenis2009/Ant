package com.app.ant.app.AddressBook.gui.components;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SortedValueHashMap<K, V> implements Map<K, V> {
    private Map<K, V> map_ = new HashMap<K, V>();
    private List<V> valueList_ = new ArrayList<V>();
    private boolean needsSort_ = false;
    private Comparator<V> comparator_;

    public SortedValueHashMap() {
    }

    public SortedValueHashMap(List<V> valueList) {
        valueList_ = valueList;
    }

    public List<V> sortedValues() {
        if (needsSort_) {
            needsSort_ = false;
            Collections.sort(valueList_, comparator_);
        }
        return valueList_;
    }

    // mutators
    public void clear() {
        map_.clear();
        valueList_.clear();
        needsSort_ = false;
    }

    public V put(K key, V value) {
        valueList_.add(value);
        needsSort_ = true;
        return map_.put(key, value);
    }

    public void putAll(Map<? extends K, ? extends V> m) {
        map_.putAll(m);
        valueList_.addAll(m.values());
        needsSort_ = true;
    }

    public V remove(Object key) {
        V value = map_.remove(key);
        valueList_.remove(value);
        return value;
    }

    // accessors
    public boolean containsKey(Object key) {
        return map_.containsKey(key);
    }

    public boolean containsValue(Object value) {
        return map_.containsValue(value);
    }

    public Set<java.util.Map.Entry<K, V>> entrySet() {
        return map_.entrySet();
    }

    public boolean equals(Object o) {
        return map_.equals(o);
    }

    public V get(Object key) {
        return map_.get(key);
    }

    public int hashCode() {
        return map_.hashCode();
    }

    public boolean isEmpty() {
        return map_.isEmpty();
    }

    public Set<K> keySet() {
        return map_.keySet();
    }

    public int size() {
        return map_.size();
    }

    public Collection<V> values() {
        return map_.values();
    }
}