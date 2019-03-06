package org.lab1505.ue.entity;

import java.util.HashMap;
import java.util.Set;

/**
 * A Map<String,String> wrapper used as typical string-composed edges.
 */
public class StringMapEdge {
    private HashMap<String, String> map;

    public StringMapEdge() {
        this.map = new HashMap<>();
    }

    /**
     * Put a key-value pair in the map.
     * 
     * @param name  key of the entry
     * @param value value of the entry
     */
    public void put(String name, String value) {
        map.put(name, value);
    }

    /**
     * Return the keys of the map.
     * 
     * @return the keys of the map
     */
    public Set<String> keys() {
        return map.keySet();
    }

    /**
     * Get the value from the map by a specific key
     * 
     * @param key key of the entry
     * @return value value of the entry
     * 
     */
    public String get(String key) {
        return map.get(key);
    }
}