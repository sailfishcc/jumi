// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.runs;

import fi.jumi.core.api.RunId;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class RunIdSequenceTest {

    private final RunIdSequence sequence = new RunIdSequence();

    @Test
    public void starts_from_the_first_RunId() {
        assertThat(sequence.nextRunId(), is(new RunId(RunId.FIRST_ID)));
    }

    @Test
    public void each_subsequent_RunId_is_incremented_by_one() {
        RunId id0 = sequence.nextRunId();
        RunId id1 = sequence.nextRunId();
        RunId id2 = sequence.nextRunId();

        assertThat(id1.toInt(), is(id0.toInt() + 1));
        assertThat(id2.toInt(), is(id1.toInt() + 1));
    }

    @Test
    public void the_sequence_is_thread_safe() throws Exception {
        final int ITERATIONS = 50;
        List<RunId> expectedRunIds = generateRunIdsSequentially(ITERATIONS);
        List<RunId> actualRunIds = generateRunIdsInParallel(ITERATIONS);

        assertThat("generating RunIds in parallel should have produced the same values as sequentially",
                actualRunIds, is(expectedRunIds));
    }

    private static List<RunId> generateRunIdsSequentially(int count) {
        RunIdSequence sequence = new RunIdSequence();

        List<RunId> results = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            results.add(sequence.nextRunId());
        }
        return results;
    }

    private static List<RunId> generateRunIdsInParallel(int count) throws Exception {
        RunIdSequence sequence = new RunIdSequence();
        ExecutorService executor = Executors.newFixedThreadPool(10);

        List<Future<RunId>> futures = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            futures.add(executor.submit((Callable<RunId>) sequence::nextRunId));
        }

        List<RunId> results = new ArrayList<>();
        for (Future<RunId> future : futures) {
            results.add(future.get(1000, TimeUnit.MILLISECONDS));
        }
        Collections.sort(results, (id1, id2) -> id1.toInt() - id2.toInt());

        executor.shutdown();
        return results;
    }
}
