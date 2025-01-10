package com.github.akarazhev.activej.sandbox;

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

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public final class ByBitWebSocketService extends Launcher {
    private static final String BYBIT_WSS_URL = "wss://stream.bybit.com/v5/public/spot";

    @Inject
    IWebSocketClient webSocketClient;

    @Inject
    NioReactor reactor;

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

    @Override
    protected void run() throws ExecutionException, InterruptedException {
        System.out.println("\nConnecting to Bybit WebSocket: " + BYBIT_WSS_URL);
        CompletableFuture<?> future = reactor.submit(() ->
                webSocketClient.webSocketRequest(HttpRequest.get(BYBIT_WSS_URL).build())
                        .then(webSocket -> {
                            System.out.println("Connected to Bybit WebSocket");
                            // Subscribe to a specific topic (e.g., BTC-USDT orderbook)
                            String subscribeMessage = "{\"op\":\"subscribe\",\"args\":[\"orderbook.1.BTCUSDT\"]}";
                            return webSocket.writeMessage(Message.text(subscribeMessage))
                                    .then(() -> webSocket.readMessage()
                                            .whenResult(message -> {
                                                System.out.println("Received: " + message.getText());
                                                // Continue reading messages
                                                readMessages(webSocket);
                                            })
                                            .whenException(e -> {
                                                System.err.println("Error: " + e.getMessage());
                                                webSocket.close();
                                            }));
                        })
        );
        future.get();
    }

    private void readMessages(IWebSocket webSocket) {
        webSocket.readMessage()
                .whenResult(message -> {
                    System.out.println("Received: " + message.getText());
                    // Continue reading messages
                    readMessages(webSocket);
                })
                .whenException(e -> {
                    System.err.println("Error: " + e.getMessage());
                    webSocket.close();
                });
    }

    public static void main(String[] args) throws Exception {
        ByBitWebSocketService service = new ByBitWebSocketService();
        service.launch(args);
    }
}
