package com.github.akarazhev.activej.examples.http;

import io.activej.http.HttpResponse;
import io.activej.http.RoutingServlet;
import io.activej.inject.Key;
import io.activej.inject.annotation.Provides;
import io.activej.inject.binding.Multibinder;
import io.activej.inject.binding.Multibinders;
import io.activej.inject.module.AbstractModule;
import io.activej.inject.module.Module;
import io.activej.inject.module.ModuleBuilder;
import io.activej.inject.module.Modules;
import io.activej.launchers.http.HttpServerLauncher;
import io.activej.reactor.Reactor;

import static io.activej.http.HttpMethod.GET;
import static io.activej.http.HttpMethod.POST;

/**
 * An example of combining {@link RoutingServlet}s provided in multiple DI modules.
 * <p>
 * Servlets are provided via the same {@link Key} ({@link RoutingServlet}).
 * So, to resolve DI conflicts we may use {@link Multibinder} which combines all the
 * conflicting {@link RoutingServlet}s into a single {@link RoutingServlet} which contains all the routes
 * mapped by other servlets.
 * If there are conflicting routes mapped in different modules, a runtime exception would be thrown
 * <p>
 * You may test routes either by accessing mapped routes via browser or by issuing {@code curl} commands:
 * <ul>
 *     <li>{@code curl http://localhost:8080}</li>
 *     <li>{@code curl http://localhost:8080/a}</li>
 *     <li>{@code curl http://localhost:8080/a/b}</li>
 *     <li>{@code curl http://localhost:8080/a/c}</li>
 *     <li>{@code curl http://localhost:8080/b}</li>
 *     <li>{@code curl http://localhost:8080/b/a}</li>
 *     <li>{@code curl http://localhost:8080/b/c}</li>
 *     <li>{@code curl http://localhost:8080/d}</li>
 *     <li>{@code curl -X POST http://localhost:8080/d}</li>
 * </ul>
 */

public final class RoutingServletMultibinderExample extends HttpServerLauncher {

    //[START MULTIBINDER]
    public static final Multibinder<RoutingServlet> SERVLET_MULTIBINDER = Multibinders.ofBinaryOperator(RoutingServlet::merge);
    //[END MULTIBINDER]

    //[START MAIN_MODULE]
    @Override
    protected Module getBusinessLogicModule() {
        return Modules.combine(
                new ModuleA(),
                new ModuleB(),
                new ModuleC(),
                ModuleBuilder.create()
                        .multibind(Key.of(RoutingServlet.class), SERVLET_MULTIBINDER)
                        .build()
        );
    }
    //[END MAIN_MODULE]

    public static void main(String[] args) throws Exception {
        new RoutingServletMultibinderExample().launch(args);
    }

    //[START MODULE_A]
    private static final class ModuleA extends AbstractModule {
        @Provides
        RoutingServlet servlet(Reactor reactor) {
            return RoutingServlet.builder(reactor)
                    .with(GET, "/a", request -> HttpResponse.ok200()
                            .withPlainText("Hello from '/a' path\n")
                            .toPromise())
                    .with(GET, "/b", request -> HttpResponse.ok200()
                            .withPlainText("Hello from '/b' path\n")
                            .toPromise())
                    .with(GET, "/", request -> HttpResponse.ok200()
                            .withPlainText("Hello from '/' path\n")
                            .toPromise())
                    .build();
        }
    }
    //[END MODULE_A]

    //[START MODULE_B]
    private static final class ModuleB extends AbstractModule {
        @Provides
        RoutingServlet servlet(Reactor reactor) {
            return RoutingServlet.builder(reactor)
                    .with(GET, "/a/b", request -> HttpResponse.ok200()
                            .withPlainText("Hello from '/a/b' path\n")
                            .toPromise())
                    .with(GET, "/b/a", request -> HttpResponse.ok200()
                            .withPlainText("Hello from '/b/a' path\n")
                            .toPromise())
                    .with(GET, "/d", request -> HttpResponse.ok200()
                            .withPlainText("Hello from '/d' path\n")
                            .toPromise())
                    .build();
        }
    }
    //[END MODULE_B]

    //[START MODULE_C]
    private static final class ModuleC extends AbstractModule {
        @Provides
        RoutingServlet servlet(Reactor reactor) {
            return RoutingServlet.builder(reactor)
                    .with(GET, "/a/c", request -> HttpResponse.ok200()
                            .withPlainText("Hello from '/a/c' path\n")
                            .toPromise())
                    .with(GET, "/b/c", request -> HttpResponse.ok200()
                            .withPlainText("Hello from '/b/c' path\n")
                            .toPromise())
                    .with(POST, "/d", request -> HttpResponse.ok200()
                            .withPlainText("Hello from POST '/d' path\n")
                            .toPromise())
                    .build();
        }
    }
    //[END MODULE_C]
}
