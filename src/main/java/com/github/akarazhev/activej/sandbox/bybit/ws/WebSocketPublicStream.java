package com.github.akarazhev.activej.sandbox.bybit.ws;

import io.activej.dns.DnsClient;
import io.activej.dns.IDnsClient;
import io.activej.eventloop.Eventloop;
import io.activej.http.HttpClient;
import io.activej.http.HttpRequest;
import io.activej.http.HttpUtils;
import io.activej.http.IWebSocket;
import io.activej.http.IWebSocket.Message;
import io.activej.http.IWebSocketClient;
import io.activej.inject.annotation.Inject;
import io.activej.inject.annotation.Provides;
import io.activej.inject.module.Module;
import io.activej.launcher.Launcher;
import io.activej.reactor.nio.NioReactor;
import io.activej.service.ServiceGraphModule;

import javax.net.ssl.SSLContext;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public final class WebSocketPublicStream extends Launcher {
    private static final String WSS_TESTNET_SPOT_URL = "wss://stream-testnet.bybit.com/v5/public/spot";
    private static final String WSS_SUBSCRIBE = "{\"op\":\"subscribe\",\"args\":[\"orderbook.1.BTCUSDT\"]}";
    private static final Executor SINGLE_THREAD_EXECUTOR = Executors.newSingleThreadExecutor();

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
        try {
            return HttpClient.builder(reactor, dnsClient)
                    .withSslEnabled(SSLContext.getDefault(), SINGLE_THREAD_EXECUTOR)
                    .build();
        } catch (NoSuchAlgorithmException e) {
            System.err.println("Error: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    protected Module getModule() {
        return ServiceGraphModule.create();
    }

    @Override
    protected void run() throws ExecutionException, InterruptedException {
        System.out.println("\nConnecting to Bybit WebSocket: " + WSS_TESTNET_SPOT_URL);
        CompletableFuture<?> future = reactor.submit(() ->
                webSocketClient.webSocketRequest(HttpRequest.get(WSS_TESTNET_SPOT_URL).build())
                        .then(webSocket -> {
                            System.out.println("Connected to Bybit WebSocket");
                            return webSocket.writeMessage(Message.text(WSS_SUBSCRIBE))
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
        WebSocketPublicStream service = new WebSocketPublicStream();
        service.launch(args);
    }
}
