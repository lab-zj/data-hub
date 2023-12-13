package net.zjvis.flint.data.hub.service.graph;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;
import net.zjvis.flint.data.hub.lib.schedule.graph.SerializableSupplier;
import net.zjvis.flint.data.hub.lib.schedule.graph.Vertex;
import net.zjvis.flint.data.hub.service.graph.planner.Planner;
import net.zjvis.flint.data.hub.service.graph.planner.conf.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@EqualsAndHashCode(callSuper = true)
@ToString
public class DefaultVertex extends Vertex<Vertex.SupplierResult> {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultVertex.class);
    @EqualsAndHashCode
    @ToString
    public static class DefaultSupplier implements SerializableSupplier<Vertex.SupplierResult> {
        private final Planner<Void> planner;
        private final Configuration configuration;

        @Builder
        @Jacksonized
        public DefaultSupplier(Planner<Void> planner, Configuration configuration) {
            this.planner = planner;
            this.configuration = configuration;
        }

        @Override
        public SupplierResult get() {
            try {
                planner.executor(configuration)
                        .execute();
                return SupplierResult.builder()
                        .success(true)
                        .build();
            } catch (Exception e) {
                LOGGER.info("[DefaultVertex-get] failed, exception={}", e.toString());
                return SupplierResult.builder()
                        .success(false)
                        .message(String.format("execute failed: %s", e.getMessage()))
                        .exception(e)
                        .build();
            }
        }
    }


    @Builder
    @Jacksonized
    public DefaultVertex(String name, DefaultSupplier supplier) {
        super(name, supplier);
    }
}
