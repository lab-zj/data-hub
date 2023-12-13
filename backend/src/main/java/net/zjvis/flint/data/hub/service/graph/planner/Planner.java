package net.zjvis.flint.data.hub.service.graph.planner;

import net.zjvis.flint.data.hub.service.graph.executor.TaskExecutor;
import net.zjvis.flint.data.hub.service.graph.planner.conf.Configuration;

public interface Planner<ResultType> {
    TaskExecutor<ResultType> executor(Configuration configuration) throws Exception;
}
