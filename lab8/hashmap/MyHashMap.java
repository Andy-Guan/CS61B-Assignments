package hashmap;

import java.util.*;

/**
 *  @author Andy
 */
public class MyHashMap<K, V> implements Map61B<K, V> {

    protected class Node {
        K key;
        V value;

        Node(K k, V v) {
            key = k;
            value = v;
        }
    }

    /* Instance Variables */
    private Collection<Node>[] buckets; // 哈希表的每一个格子
    private int initialSize;
    private double maxLoad;
    private int size;
    private HashSet<K> keys; // 储存每一个key


    /** Constructors */
    public MyHashMap() {
        this(16, 0.75);
    }

    public MyHashMap(int initialSize) {
        this(initialSize, 0.75);
    }

    /**
     * MyHashMap constructor that creates a backing array of initialSize.
     * The load factor (# items / # buckets) should always be <= loadFactor
     *
     * @param initialSize initial size of backing array
     * @param maxLoad maximum load factor
     */
    public MyHashMap(int initialSize, double maxLoad) {
        this.initialSize = initialSize;
        this.maxLoad = maxLoad;
        this.size = 0;
        this.buckets = createTable(initialSize);
        for (int i = 0; i < initialSize; i++) {
            this.buckets[i] = createBucket();
        }
        this.keys = new HashSet<>();
    }

    /**
     * Returns a new node to be placed in a hash table bucket
     */
    private Node createNode(K key, V value) {
        return new Node(key, value);
    }

    /**
     * Returns a data structure to be a hash table bucket
     *
     * The only requirements of a hash table bucket are that we can:
     *  1. Insert items (`add` method)
     *  2. Remove items (`remove` method)
     *  3. Iterate through items (`iterator` method)
     *
     * Each of these methods is supported by java.util.Collection,
     * Most data structures in Java inherit from Collection, so we
     * can use almost any data structure as our buckets.
     *
     * BE SURE TO CALL THIS FACTORY METHOD INSTEAD OF CREATING YOUR
     * OWN BUCKET DATA STRUCTURES WITH THE NEW OPERATOR!
     */
    protected Collection<Node> createBucket() {
        return new LinkedList<>();
    }

    /**
     * Returns a table to back our hash table. As per the comment
     * above, this table can be an array of Collection objects
     *
     * BE SURE TO CALL THIS FACTORY METHOD WHEN CREATING A TABLE SO
     * THAT ALL BUCKET TYPES ARE OF JAVA.UTIL.COLLECTION
     *
     * @param tableSize the size of the table to create
     */
    private Collection<Node>[] createTable(int tableSize) {
        return (Collection<Node>[]) new Collection[tableSize];
    }

    private int getIndex(K key) {
        int hashCode = key.hashCode();
        return Math.floorMod(hashCode, buckets.length);
    }

    @Override
    public void clear(){
        this.size = 0;
        this.buckets = createTable(initialSize);
        for (int i = 0; i < initialSize; i++) {
            this.buckets[i] = createBucket();
        }
        this.keys = new HashSet<>();
    }

    @Override
    public boolean containsKey(K key){
        return get(key) != null;
    }

    @Override
    public V get(K key){
        if (key == null) return null;
        int index = getIndex(key);
        Collection<Node> bucket = buckets[index];

        for (Node node : bucket) {
            if (node.key.equals(key)) {
                return node.value;
            }
        }
        return null;
    }

    @Override
    public int size(){
        return this.size;
    }

    @Override
    public void put(K key, V value){
        if (key == null) return;

        int index = getIndex(key);
        Collection<Node> bucket = buckets[index];

        for (Node node : bucket) {
            if (node.key.equals(key)) {
                node.value = value;
                return;
            }
        }

        Node newNode = createNode(key, value);
        bucket.add(newNode);
        size++;
        keys.add(key);

        if (((double) size / buckets.length) > maxLoad) {
            resize(buckets.length * 2);
        }
    }

    private void resize(int newCapacity) {
        Collection<Node>[] newBuckets = createTable(newCapacity);
        for (int i = 0; i < newCapacity; i++) {
            newBuckets[i] = createBucket();
        }

        for (int i = 0; i < buckets.length; i++) {
            for (Node node : buckets[i]) {
                int newIndex = Math.floorMod(node.key.hashCode(), newCapacity);
                newBuckets[newIndex].add(node);
            }
        }
        this.buckets = newBuckets;
    }

    /** Returns a Set view of the keys contained in this map. */
    public Set<K> keySet(){
        return this.keys;
    }

    public Iterator<K> iterator() {
        return this.keys.iterator();
    }

    @Override
    public V remove(K key){
        if (key == null) {
            return null;
        }

        int index = getIndex(key);
        Collection<Node> bucket = buckets[index];

        Node nodeToRemove = null;
        for (Node node : bucket) {
            if (node.key.equals(key)) {
                nodeToRemove = node;
                break;
            }
        }

        if (nodeToRemove != null) {
            bucket.remove(nodeToRemove);
            size--;
            if (((double) size / buckets.length) > maxLoad) {
                resize(buckets.length / 2);
            }
            keys.remove(key);
            return nodeToRemove.value;
        }

        return null;
    }

    @Override
    public V remove(K key, V value){
        if (key == null) {
            return null;
        }

        int index = getIndex(key);
        Collection<Node> bucket = buckets[index];

        Node nodeToRemove = null;
        for (Node node : bucket) {
            if (node.key.equals(key) && node.value.equals(value)) {
                nodeToRemove = node;
                break;
            }
        }

        if (nodeToRemove != null) {
            bucket.remove(nodeToRemove);
            size--;
            if (((double) size / buckets.length) > maxLoad) {
                resize(buckets.length / 2);
            }
            keys.remove(key);
            return nodeToRemove.value;
        }

        return null;
    }
}
