package be.orbinson.aem.mocks;

import org.apache.sling.distribution.queue.DistributionQueueEntry;
import org.apache.sling.distribution.queue.DistributionQueueItem;
import org.apache.sling.distribution.queue.DistributionQueueState;
import org.apache.sling.distribution.queue.DistributionQueueStatus;
import org.apache.sling.distribution.queue.DistributionQueueType;
import org.apache.sling.distribution.queue.spi.DistributionQueue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class MockDistributionQueue implements DistributionQueue {
    @Override
    public @NotNull String getName() {
        return "main";
    }

    @Override
    public @Nullable DistributionQueueEntry add(@NotNull DistributionQueueItem distributionQueueItem) {
        throw new UnsupportedOperationException();
    }

    @Override
    public @Nullable DistributionQueueEntry getHead() {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull Iterable<DistributionQueueEntry> getEntries(int i, int i1) {
        throw new UnsupportedOperationException();
    }

    @Override
    public @Nullable DistributionQueueEntry getEntry(@NotNull String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public @Nullable DistributionQueueEntry remove(@NotNull String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull Iterable<DistributionQueueEntry> remove(@NotNull Set<String> set) {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull Iterable<DistributionQueueEntry> clear(int i) {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull DistributionQueueStatus getStatus() {
        return new DistributionQueueStatus(1, DistributionQueueState.RUNNING);
    }

    @Override
    public @NotNull DistributionQueueType getType() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasCapability(@NotNull String s) {
        throw new UnsupportedOperationException();
    }
}
