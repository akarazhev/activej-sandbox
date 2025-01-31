package com.github.akarazhev.activej.examples.http;

import io.activej.dns.DnsClient;
import io.activej.dns.IDnsClient;
import io.activej.eventloop.Eventloop;
import io.activej.http.*;
import io.activej.http.IWebSocket.Message;
import io.activej.inject.annotation.Inject;
import io.activej.inject.annotation.Provides;
import io.activej.inject.module.Module;
import io.activej.launcher.Launcher;
import io.activej.reactor.nio.NioReactor;
import io.activej.service.ServiceGraphModule;

import java.util.Scanner;
import java.util.concurrent.CompletableFuture;

/**
 * A WebSocket client for the {@link WebSocketEchoServerExample}
 * <p>
 * You may write messages to the terminal and receive echoed responses back from the server
 * <p>
 * <b>{@link WebSocketEchoServerExample} should be running prior to launching this example</b>
 */
public final class WebSocketEchoClientExample extends Launcher {

    @Inject
    NioReactor reactor;

    @Inject
    IWebSocketClient client;

    @Provides
    NioReactor reactor() {
        return Eventloop.create();
    }

    @Provides
    IDnsClient dnsClient(NioReactor reactor) {
        return DnsClient.create(reactor, HttpUtils.inetAddress("8.8.8.8"));
    }

    @Provides
    IWebSocketClient client(NioReactor reactor, IDnsClient dnsClient) {
        return HttpClient.create(reactor, dnsClient);
    }

    @Override
    protected Module getModule() {
        return ServiceGraphModule.create();
    }

    //[START MAIN]
    @Override
    protected void run() throws Exception {
        String url = args.length != 0 ? args[0] : "ws://127.0.0.1:8080/";
        System.out.println("\nConnecting to WebSocket at: " + url);

        IWebSocket webSocket = reactor.submit(() -> client.webSocketRequest(HttpRequest.get(url).build())).get();

        Scanner scanIn = new Scanner(System.in);
        while (true) {
            System.out.print("> ");
            String line = scanIn.nextLine();
            if (line.isEmpty()) {
                reactor.submit(webSocket::close);
                break;
            }
            CompletableFuture<?> future = reactor.submit(() ->
                    webSocket.writeMessage(Message.text(line))
                            .then(webSocket::readMessage)
                            .whenResult(message -> System.out.println("Response: " + message.getText())));
            future.get();
        }
    }
    //[END MAIN]

    public static void main(String[] args) throws Exception {
        WebSocketEchoClientExample launcher = new WebSocketEchoClientExample();
        launcher.launch(args);
    }
}
