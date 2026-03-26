package com.bsuir.taskmanager.service.racecondition;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.bsuir.taskmanager.model.dto.response.RaceConditionCounterResultResponse;
import com.bsuir.taskmanager.model.dto.response.RaceConditionDemoResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class RaceConditionDemoServiceTest {
    private static final int EXPECTED_VALUE = 10_000;

    private final RaceConditionDemoService raceConditionDemoService = new RaceConditionDemoService();

    @Test
    void runAllDemosShouldReturnCorrectSafeCounters() throws InterruptedException {
        RaceConditionDemoResponse response = raceConditionDemoService.runAllDemos();

        assertEquals(50, response.getThreadCount());
        assertEquals(200, response.getIncrementsPerThread());
        assertCounterIsCorrect(response.getSynchronizedCounter(), "Synchronized counter");
        assertCounterIsCorrect(response.getAtomicCounter(), "Atomic counter");
        assertUnsafeCounterMatchesResponseContract(response.getUnsafeCounter());
    }

    @Test
    void runAllDemosShouldKeepSafeCountersCorrectAcrossConcurrentInvocations()
            throws InterruptedException, ExecutionException {
        int concurrentCalls = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(concurrentCalls);
        List<Future<RaceConditionDemoResponse>> futures = new ArrayList<>();

        try {
            for (int i = 0; i < concurrentCalls; i++) {
                futures.add(executorService.submit(raceConditionDemoService::runAllDemos));
            }
        } finally {
            executorService.shutdown();
        }

        assertTrue(executorService.awaitTermination(1, TimeUnit.MINUTES));

        for (Future<RaceConditionDemoResponse> future : futures) {
            RaceConditionDemoResponse response = future.get();
            assertCounterIsCorrect(response.getSynchronizedCounter(), "Synchronized counter");
            assertCounterIsCorrect(response.getAtomicCounter(), "Atomic counter");
            assertUnsafeCounterMatchesResponseContract(response.getUnsafeCounter());
        }
    }

    private void assertCounterIsCorrect(
            RaceConditionCounterResultResponse counter,
            String counterName
    ) {
        assertEquals(counterName, counter.getCounterName());
        assertEquals(EXPECTED_VALUE, counter.getExpectedValue());
        assertEquals(EXPECTED_VALUE, counter.getActualValue());
        assertEquals(0, counter.getLostUpdates());
        assertTrue(counter.isCorrect());
    }

    private void assertUnsafeCounterMatchesResponseContract(
            RaceConditionCounterResultResponse counter
    ) {
        assertEquals("Unsafe counter", counter.getCounterName());
        assertEquals(EXPECTED_VALUE, counter.getExpectedValue());
        assertTrue(counter.getActualValue() <= EXPECTED_VALUE);
        assertEquals(
                EXPECTED_VALUE - counter.getActualValue(),
                counter.getLostUpdates()
        );
    }
}
