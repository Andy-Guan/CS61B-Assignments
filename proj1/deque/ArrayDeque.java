package deque;

import java.util.Iterator;

public class ArrayDeque<T> implements Deque<T>, Iterable<T> {

    private T[] items;
    private int size;
    private int nextFirst;
    private int nextLast;

    /**
     * Creates an empty list.
     */
    public ArrayDeque() {
        items = (T[]) new Object[8];
        size = 0;
        nextFirst = 4;
        nextLast = 5;
    }

    /**
     * 改变数组格局
     */
    private void resize(int capacity) {
        int i = 0;
        T[] newitems = (T[]) new Object[capacity];
        for (; i < size; i++) {
            newitems[i] = items[plusOne(nextFirst)];
            nextFirst = plusOne(nextFirst);
        }
        nextFirst = capacity - 1;
        nextLast = size;
        items = newitems;
    }

    /**
     * 计算某个索引向左移一位后的新索引（处理绕回）
     */
    private int minusOne(int index) {
        if (index < 1) {
            return items.length - 1;
        }
        return index - 1;
    }

    /**
     * 计算某个索引向右移一位后的新索引（处理绕回）
     */
    private int plusOne(int index) {
        if (index + 1 == items.length) {
            return 0;
        }
        return index + 1;
    }

    @Override
    public void addFirst(T item) {
        if (size == items.length) {
            resize(items.length * 2);
        }
        items[nextFirst] = item;
        nextFirst = minusOne(nextFirst);
        size++;
    }

    @Override
    public void addLast(T item) {
        if (size == items.length) {
            resize(items.length * 2);
        }
        items[nextLast] = item;
        nextLast = plusOne(nextLast);
        size++;

    }


    @Override
    public int size() {
        return size;
    }

    @Override
    public void printDeque() {
        int count = plusOne(nextFirst);
        while (count != nextFirst) {
            System.out.print(items[count] + " ");
            count = plusOne(count);
        }
        System.out.println();
    }

    @Override
    public T removeFirst() {
        if (size == 0) {
            return null;
        }
        nextFirst = plusOne(nextFirst);
        T returnItem = items[nextFirst];
        items[nextFirst] = null;
        size--;
        if (items.length >= 16 && size < items.length / 4) {
            resize(items.length / 2);
        }
        return returnItem;
    }

    @Override
    public T removeLast() {
        if (size == 0) {
            return null;
        }
        nextLast = minusOne(nextLast);
        T returnItem = items[nextLast];
        items[nextLast] = null;
        size--;
        if (items.length >= 16 && size < items.length / 4) {
            resize(items.length / 2);
        }
        return returnItem;
    }

    @Override
    public T get(int index) {
        if (index >= size || index < 0) {
            return null;
        }
        int start = plusOne(nextFirst);
        int actualIndex = (start + index) % items.length;
        return items[actualIndex];
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof Deque)) {
            return false;
        }

        Deque<T> other = (Deque<T>) o;

        if (this.size() != other.size()) {
            return false;
        }

        for (int i = 0; i < this.size(); i++) {
            if (!this.get(i).equals(other.get(i))) {
                return false;
            }
        }

        return true;
    }

    @Override
    public Iterator<T> iterator() {
        return new ArrayDequeIterator();
    }

    private class ArrayDequeIterator implements Iterator<T> {
        private int pos;

        public ArrayDequeIterator() {
            pos = 0;
        }

        @Override
        public boolean hasNext() {
            return pos < size;
        }

        @Override
        public T next() {
            T returnItem = get(pos);
            pos += 1;
            return returnItem;
        }
    }
}
