package bstmap;

import java.util.Iterator;
import java.util.Set;

public class BSTMap<K extends Comparable<K>, V> implements Map61B<K, V> {
    private BSTNode root;             // root of BST

    private class BSTNode {
        private K key;
        private V val;
        private BSTNode left, right;
        private int size;

        public BSTNode(K key, V val, int size) {
            this.key = key;
            this.val = val;
            this.size = size;
        }
    }


    public BSTMap(){
    }


    /** Removes all of the mappings from this map. */
    public void clear(){
        root = null;
    }

    @Override
    public boolean containsKey(K key){
        if (getKey(root, key) == null){
            return false;
        }
        return true;
    }

    private BSTNode getKey(BSTNode node, K key) {
        if (key == null) return null;
        if (node == null) return null;
        int cmp = key.compareTo(node.key);
        if      (cmp < 0) return getKey(node.left, key);
        else if (cmp > 0) return getKey(node.right, key);
        else              return node;
    }

    @Override
    public V get(K key){
        return get(root, key);
    }

    private V get(BSTNode node, K key) {
        if (key == null) return null;
        if (node == null) return null;
        int cmp = key.compareTo(node.key);
        if      (cmp < 0) return get(node.left, key);
        else if (cmp > 0) return get(node.right, key);
        else              return node.val;
    }

    @Override
    public int size(){
        if (root == null){
            return 0;
        }
        return root.size;
    }

    public int size(BSTNode Node){
        if (Node == null){
            return 0;
        }
        return Node.size;
    }



    @Override
    public void put(K key, V value){

        /**

        if (key == null) {
            throw new IllegalArgumentException("calls put() with a null key");
        }
        if (value == null) {
            throw new IllegalArgumentException("calls put() with a null value");
        }
         */
        root = put(root, key, value);
    }

    private BSTNode put(BSTNode node, K key, V val) {
        if (node == null) return new BSTNode(key, val, 1);
        int cmp = key.compareTo(node.key);
        if      (cmp < 0) node.left  = put(node.left,  key, val);
        else if (cmp > 0) node.right = put(node.right, key, val);
        else              node.val   = val;
        node.size = 1 + size(node.left) + size(node.right);
        return node;
    }

    @Override
    public Set<K> keySet(){
        throw new UnsupportedOperationException();
    }

    @Override
    public V remove(K key){
        throw new UnsupportedOperationException();
    }

    @Override
    public V remove(K key, V value){
        throw new UnsupportedOperationException();
    }


    @Override
    public Iterator<K> iterator() {
        return new BSTMapIter();
    }

    private class BSTMapIter implements Iterator<K> {


        public BSTMapIter() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean hasNext() {
            throw new UnsupportedOperationException();
        }

        @Override
        public K next() {
            throw new UnsupportedOperationException();
        }


    }

    public void printInOrder(){
        throw new UnsupportedOperationException();
    }

}
