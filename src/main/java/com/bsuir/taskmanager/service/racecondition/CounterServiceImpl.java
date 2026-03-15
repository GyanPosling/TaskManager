package com.bsuir.taskmanager.service.racecondition;

import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.stereotype.Service;

@Service
public class CounterServiceImpl {
    private int unsafeValue;
    private int synchronizedValue;
    private final AtomicInteger atomicValue;

    public CounterServiceImpl() {
        this.atomicValue = new AtomicInteger();
    }

    public void incrementUnsafe() {
        int currentValue = unsafeValue;
        Thread.yield();
        unsafeValue = currentValue + 1;
    }

    public synchronized void incrementSynchronized() {
        synchronizedValue++;
    }

    public void incrementAtomic() {
        atomicValue.incrementAndGet();
    }

    public synchronized void reset() {
        unsafeValue = 0;
        synchronizedValue = 0;
        atomicValue.set(0);
    }

    public int getUnsafeValue() {
        return unsafeValue;
    }

    public synchronized int getSynchronizedValue() {
        return synchronizedValue;
    }

    public int getAtomicValue() {
        return atomicValue.get();
    }
}
