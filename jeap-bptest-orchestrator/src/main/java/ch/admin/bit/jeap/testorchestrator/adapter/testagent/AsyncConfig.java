package ch.admin.bit.jeap.testorchestrator.adapter.testagent;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.Executor;

@AutoConfiguration
@ComponentScan
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

    private final OrchestratorAsyncExceptionHandler orchestratorAsyncExceptionHandler;

    public AsyncConfig(OrchestratorAsyncExceptionHandler orchestratorAsyncExceptionHandler) {
        this.orchestratorAsyncExceptionHandler = orchestratorAsyncExceptionHandler;
    }

    @Override
    public Executor getAsyncExecutor() {
        return new SimpleAsyncTaskExecutor();
    }


    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return this.orchestratorAsyncExceptionHandler;
    }

}
