package deque;

public class LinkedListDeque<T> implements Deque<T>{
    private class IntNode {
        public IntNode prev;
        public T item;
        public IntNode next;

        public IntNode(IntNode p,T i, IntNode n) {
            prev = p;
            item = i;
            next = n;
        }
    }

    /* The first item (if it exists) is at sentinel.next. */
    private IntNode sentinel;
    private int size;

    /** Creates an empty LinkedListDeque */
    public LinkedListDeque() {
        sentinel = new IntNode(null, null,null);
        sentinel.prev = sentinel;
        sentinel.next = sentinel;
        size = 0;
    }

    public LinkedListDeque(T item) {
        sentinel = new IntNode(null, null, null);
        sentinel.prev = sentinel;
        sentinel.next = sentinel;
        sentinel.next = new IntNode(sentinel, item, sentinel);
        sentinel.prev = sentinel.next;
        size = 1;
    }

    @Override
    /** Adds x to the front of the list. */
    public void addFirst(T item){
        IntNode first = new IntNode(sentinel, item, sentinel.next);
        sentinel.next.prev = first;
        sentinel.next = first;
        size += 1;
    }

    @Override
    public void addLast(T item){
        IntNode last = new IntNode(sentinel.prev, item, sentinel);
        sentinel.prev.next = last;
        sentinel.prev = last;
        size +=1;
    }

    @Override
    public boolean isEmpty() {
        if (size == 0) {
            return true;
        }else{
            return false;
        }
    }
    @Override
    public int size(){
        return size;
    }

    @Override
    /** 1 2 3 4
     *
     */
    public void printDeque(){
        IntNode curr = sentinel.next;
        while (curr != sentinel) {
            System.out.print(curr.item + " ");
            curr = curr.next;
        }
        System.out.println();
    }

    @Override
    public T removeFirst(){
        if(sentinel.next == sentinel){
            return null;
        }else{
            IntNode k = sentinel.next;
            sentinel.next = sentinel.next.next;
            sentinel.next.prev = sentinel;
            size -= 1;
            return k.item;
        }
    }
    @Override
    public T removeLast(){
        if(sentinel.next == sentinel){
            return null;
        }else{
            IntNode k = sentinel.prev;
            sentinel.prev = sentinel.prev.prev;
            sentinel.prev.next = sentinel;
            size -= 1;
            return k.item;
    }}


    @Override
    public T get(int index){
        IntNode p = sentinel.next;
        for (int i = 0; i < index; i++) {
            p = p.next;
        }
        return p.item;
    }


    /** It's a helper function to hide IntNode */
    private T getRecursiveHelper(IntNode curr, int idx) {
        if (idx == 0) return curr.item;
        return getRecursiveHelper(curr.next, idx - 1);
    }
    public T getRecursive(int index) {
        return getRecursiveHelper(sentinel.next, index);
    }


}


