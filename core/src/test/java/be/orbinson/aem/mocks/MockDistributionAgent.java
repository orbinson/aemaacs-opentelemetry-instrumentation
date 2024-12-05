package be.orbinson.aem.mocks;

import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.distribution.DistributionRequest;
import org.apache.sling.distribution.DistributionResponse;
import org.apache.sling.distribution.agent.DistributionAgentState;
import org.apache.sling.distribution.agent.spi.DistributionAgent;
import org.apache.sling.distribution.common.DistributionException;
import org.apache.sling.distribution.log.spi.DistributionLog;
import org.apache.sling.distribution.queue.spi.DistributionQueue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osgi.service.component.annotations.Component;

@Component(service = DistributionAgent.class)
public class MockDistributionAgent implements DistributionAgent {
    @Override
    public @NotNull Iterable<String> getQueueNames() {
        throw new UnsupportedOperationException();
    }

    @Override
    public @Nullable DistributionQueue getQueue(@NotNull String s) {
        return new MockDistributionQueue();
    }

    @Override
    public @NotNull DistributionLog getLog() {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull DistributionAgentState getState() {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull DistributionResponse execute(@NotNull ResourceResolver resourceResolver, @NotNull DistributionRequest distributionRequest) throws DistributionException {
        throw new UnsupportedOperationException();
    }
}
