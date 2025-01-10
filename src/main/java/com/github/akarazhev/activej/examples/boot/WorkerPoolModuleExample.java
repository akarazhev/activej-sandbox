package com.github.akarazhev.activej.examples.boot;

import io.activej.inject.Injector;
import io.activej.inject.annotation.Provides;
import io.activej.inject.module.AbstractModule;
import io.activej.worker.WorkerPool;
import io.activej.worker.WorkerPoolModule;
import io.activej.worker.WorkerPools;
import io.activej.worker.annotation.Worker;
import io.activej.worker.annotation.WorkerId;

//[START EXAMPLE]
public final class WorkerPoolModuleExample extends AbstractModule {
    @Provides
    WorkerPool workerPool(WorkerPools workerPools) {
        return workerPools.createPool(4);
    }

    @Provides
    @Worker
    String string(@WorkerId int workerId) {
        return "Hello from worker #" + workerId;
    }

    public static void main(String[] args) {
        Injector injector = Injector.of(WorkerPoolModule.create(), new WorkerPoolModuleExample());
        WorkerPool workerPool = injector.getInstance(WorkerPool.class);
        WorkerPool.Instances<String> strings = workerPool.getInstances(String.class);
        strings.forEach(System.out::println);
    }
}
//[END EXAMPLE]
