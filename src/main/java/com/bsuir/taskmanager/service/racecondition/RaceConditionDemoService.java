package com.bsuir.taskmanager.service.racecondition;

import com.bsuir.taskmanager.model.dto.response.RaceConditionCounterResultResponse;
import com.bsuir.taskmanager.model.dto.response.RaceConditionDemoResponse;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RaceConditionDemoService {
    private static final int THREAD_COUNT = 50;
    private static final int INCREMENTS_PER_THREAD = 200;
    private static final int EXPECTED_VALUE = THREAD_COUNT * INCREMENTS_PER_THREAD;

    private final CounterServiceImpl counterService;

    public RaceConditionCounterResultResponse demonstrateRaceCondition()
            throws InterruptedException {
        log.info("RACE CONDITION");
        counterService.reset();
        executeParallelIncrements(counterService::incrementUnsafe);
        return logResults("Unsafe counter", counterService.getUnsafeValue());
    }

    public RaceConditionCounterResultResponse demonstrateSynchronizedSolution()
            throws InterruptedException {
        log.info("SYNCHRONIZED SOLUTION");
        counterService.reset();
        executeParallelIncrements(counterService::incrementSynchronized);
        return logResults("Synchronized counter", counterService.getSynchronizedValue());
    }

    public RaceConditionCounterResultResponse demonstrateAtomicSolution()
            throws InterruptedException {
        log.info("ATOMIC SOLUTION");
        counterService.reset();
        executeParallelIncrements(counterService::incrementAtomic);
        return logResults("Atomic counter", counterService.getAtomicValue());
    }

    public RaceConditionDemoResponse runAllDemos() throws InterruptedException {
        log.info("Starting race condition demo with {} threads", THREAD_COUNT);
        log.info("Each thread performs {} increments", INCREMENTS_PER_THREAD);
        log.info("Expected final value: {}", EXPECTED_VALUE);

        RaceConditionCounterResultResponse unsafeResult = demonstrateRaceCondition();
        RaceConditionCounterResultResponse synchronizedResult = demonstrateSynchronizedSolution();
        RaceConditionCounterResultResponse atomicResult = demonstrateAtomicSolution();
        return new RaceConditionDemoResponse(
                THREAD_COUNT,
                INCREMENTS_PER_THREAD,
                unsafeResult,
                synchronizedResult,
                atomicResult
        );
    }

    private void executeParallelIncrements(Runnable incrementAction) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        try {
            for (int i = 0; i < THREAD_COUNT; i++) {
                executor.submit(() -> {
                    for (int j = 0; j < INCREMENTS_PER_THREAD; j++) {
                        incrementAction.run();
                    }
                });
            }
        } finally {
            executor.shutdown();
            executor.awaitTermination(1, TimeUnit.MINUTES);
        }
    }

    private RaceConditionCounterResultResponse logResults(String counterName, int actualValue) {
        int lostUpdates = EXPECTED_VALUE - actualValue;
        boolean correct = actualValue == EXPECTED_VALUE;

        log.info("{} result", counterName);
        log.info("Expected value: {}", EXPECTED_VALUE);
        log.info("Actual value: {}", actualValue);

        if ("Unsafe counter".equals(counterName)) {
            log.info("Lost updates: {}", lostUpdates);
            log.info("Race condition present: {}", !correct);
        } else {
            log.info("Result is correct: {}", correct);
        }

        return new RaceConditionCounterResultResponse(
                counterName,
                EXPECTED_VALUE,
                actualValue,
                lostUpdates,
                correct
        );
    }
}
