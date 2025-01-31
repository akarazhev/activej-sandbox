package com.github.akarazhev.activej.examples.http;

import io.activej.http.AsyncServlet;
import io.activej.http.HttpResponse;
import io.activej.inject.annotation.Provides;
import io.activej.launcher.Launcher;
import io.activej.launchers.http.HttpServerLauncher;

//[START EXAMPLE]
public final class HttpHelloWorldExample extends HttpServerLauncher {
    @Provides
    AsyncServlet servlet() {
        return request -> HttpResponse.ok200()
                .withPlainText("Hello World")
                .toPromise();
    }

    public static void main(String[] args) throws Exception {
        Launcher launcher = new HttpHelloWorldExample();
        launcher.launch(args);
    }
}
//[END EXAMPLE]
