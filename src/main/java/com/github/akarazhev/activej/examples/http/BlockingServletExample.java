package com.github.akarazhev.activej.examples.http;

import io.activej.http.AsyncServlet;
import io.activej.http.HttpResponse;
import io.activej.http.RoutingServlet;
import io.activej.inject.annotation.Provides;
import io.activej.launcher.Launcher;
import io.activej.launchers.http.HttpServerLauncher;
import io.activej.reactor.Reactor;

import java.util.concurrent.Executor;

import static java.util.concurrent.Executors.newCachedThreadPool;

public final class BlockingServletExample extends HttpServerLauncher {
    @Provides
    Executor executor() {
        return newCachedThreadPool();
    }

    //[START EXAMPLE]
    @Provides
    AsyncServlet servlet(Reactor reactor, Executor executor) {
        return RoutingServlet.builder(reactor)
                .with("/", request -> HttpResponse.ok200()
                        .withHtml("<a href='hardWork'>Do hard work</a>")
                        .toPromise())
                .with("/hardWork", AsyncServlet.ofBlocking(executor, request -> {
                    Thread.sleep(2000); //Hard work
                    return HttpResponse.ok200()
                            .withHtml("Hard work is done")
                            .build();
                }))
                .build();
    }
    //[END EXAMPLE]

    public static void main(String[] args) throws Exception {
        Launcher launcher = new BlockingServletExample();
        launcher.launch(args);
    }
}
