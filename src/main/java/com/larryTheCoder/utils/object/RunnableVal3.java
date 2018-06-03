package com.larryTheCoder.utils.object;

public abstract class RunnableVal3<T, U, V> implements Runnable {
    private T value1;
    private U value2;
    private V value3;

    public RunnableVal3() {
    }

    public RunnableVal3(T value1, U value2, V value3) {
        this.value1 = value1;
        this.value2 = value2;
        this.value3 = value3;
    }

    @Override
    public void run() {
        run(value1, value2, value3);
    }

    public abstract void run(T value1, U value2, V value3);
}
