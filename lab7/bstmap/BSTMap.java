package bstmap;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

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


    @Override
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

    private int size(BSTNode Node){
        if (Node == null){
            return 0;
        }
        return Node.size;
    }



    @Override
    public void put(K key, V value){
        /*
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


    /* Returns a Set view of the keys contained in this map.
    For an extra challenge implement keySet() and iterator without using a second instance variable to store the set of keys.
     */
    @Override
    public Set<K> keySet(){
        Set<K> keySet = new TreeSet<>();
        traverseCollect(root, keySet);
        return keySet;
    }

    private void traverseCollect(BSTNode curr, Set<K> container) {
        if (curr == null) return;
        traverseCollect(curr.left, container);
        container.add(curr.key);
        traverseCollect(curr.right, container);
    }

    /* For remove, you should return null if the argument key does not exist in the BSTMap. Otherwise, delete the key-value pair (key, value) and return value. */
    @Override
    public V remove(K key){
        V oldVal = get(key);
        if (oldVal == null) {
            return null;
        }
        root = removeHelper(root, key);
        return oldVal;
    }

    private BSTNode removeHelper(BSTNode curr, K targetKey) {
        if (curr == null) return null;
        int cmp = targetKey.compareTo(curr.key);
        if (cmp < 0) {
            curr.left = removeHelper(curr.left, targetKey);
        }
        else if (cmp > 0) {
            curr.right = removeHelper(curr.right, targetKey);
        }
        else {
            if (curr.left == null && curr.right == null) {
                return null;
            }
            else if (curr.left == null) {
                return curr.right;
            }
            else if (curr.right == null) {
                return curr.left;
            }
            BSTNode successor = findMin(curr.right);
            curr.key = successor.key;
            curr.val = successor.val;
            curr.right = removeHelper(curr.right, successor.key);
        }
        curr.size = 1 + size(curr.left) + size(curr.right);
        return curr;
    }


    private BSTNode findMin(BSTNode node) {
        while (node.left != null) {
            node = node.left;
        }
        return node;
    }

    private BSTNode findMax(BSTNode node) {
        while (node.right != null) {
            node = node.right;
        }
        return node;
    }

    @Override
    public V remove(K key, V value){
        V existVal = get(key);
        if (existVal == null || !existVal.equals(value)) {
            return null;
        }
        root = removeHelper(root, key);
        return existVal;
    }

    /*When implementing the iterator method, you should return an iterator over the keys. */
    @Override
    public Iterator<K> iterator() {
        return new BSTMapIter(root);
    }

    private class BSTMapIter implements Iterator<K> {
        private ArrayList<K> keyList;
        private int ptr;

        public BSTMapIter(BSTNode rootNode) {
            keyList = new ArrayList<>();
            ptr = 0;
            collectInOrder(rootNode);
        }

        private void collectInOrder(BSTNode curr) {
            if (curr == null) return;
            collectInOrder(curr.left);
            keyList.add(curr.key);
            collectInOrder(curr.right);
        }

        @Override
        public boolean hasNext() {
            return ptr < keyList.size();
        }

        @Override
        public K next() {
            if (!hasNext()) {
                throw new NullPointerException();
            }
            return keyList.get(ptr++);
        }


    }

    public void printInOrder() {
        printHelper(root);
    }

    private void printHelper(BSTNode curr) {
        if (curr == null) return;
        printHelper(curr.left);
        System.out.print(curr.key + ":" + curr.val + "\n");
        printHelper(curr.right);
    }

}
