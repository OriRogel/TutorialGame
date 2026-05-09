package com.example.tutorialgame.managers.objectpool;

import java.util.function.Supplier;

public class ObjectPool<T> {
    private final T[] pool;
    private final Supplier<T> factory;
    private int top = 0;

    @SuppressWarnings("unchecked")
    public ObjectPool(int size, Supplier<T> factory) {
        this.pool = (T[]) new Object[size];
        this.factory = factory;
    }

    public synchronized T acquire() {
        if (top > 0) {
            T item = pool[--top];
            pool[top] = null; // Clear reference
            return item;
        }
        return factory.get(); // Create new if pool is empty
    }

    public synchronized void release(T item) {
        if (item == null) return;
        if (top < pool.length) {
            pool[top++] = item;
        }
    }

    public synchronized void clear() {
        for (int i = 0; i < top; i++) {
            pool[i] = null;
        }
        top = 0;
    }
}