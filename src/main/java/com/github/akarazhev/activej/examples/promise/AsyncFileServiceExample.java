package com.github.akarazhev.activej.examples.promise;

import io.activej.async.file.ExecutorFileService;
import io.activej.async.file.IFileService;
import io.activej.bytebuf.ByteBuf;
import io.activej.common.exception.UncheckedException;
import io.activej.eventloop.Eventloop;
import io.activej.promise.Promise;
import io.activej.promise.Promises;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.delete;
import static java.nio.file.StandardOpenOption.*;
import static java.util.concurrent.Executors.newCachedThreadPool;

@SuppressWarnings("Convert2MethodRef")
public final class AsyncFileServiceExample {
    private static final Eventloop EVENTLOOP = Eventloop.builder()
            .withCurrentThread()
            .build();
    private static final ExecutorService EXECUTOR_SERVICE = newCachedThreadPool();
    private static final IFileService FILE_SERVICE = new ExecutorFileService(EVENTLOOP, EXECUTOR_SERVICE);
    private static final Path PATH;

    static {
        try {
            PATH = Files.createTempFile("NewFile", ".txt");
        } catch (IOException e) {
            throw UncheckedException.of(e);
        }
    }

    //[START REGION_1]
    private static Promise<Void> writeToFile() {
        try {
            FileChannel channel = FileChannel.open(PATH, Set.of(WRITE, APPEND));

            byte[] message1 = "Hello\n".getBytes();
            byte[] message2 = "This is test file\n".getBytes();
            byte[] message3 = "This is the 3rd line in file".getBytes();

            return FILE_SERVICE.write(channel, 0, message1, 0, message1.length)
                    .then(() -> FILE_SERVICE.write(channel, 0, message2, 0, message2.length))
                    .then(() -> FILE_SERVICE.write(channel, 0, message3, 0, message3.length))
                    .toVoid();
        } catch (IOException e) {
            return Promise.ofException(e);
        }
    }

    private static Promise<ByteBuf> readFromFile() {
        byte[] array = new byte[1024];
        FileChannel channel;
        try {
            channel = FileChannel.open(PATH, Set.of(READ));
        } catch (IOException e) {
            return Promise.ofException(e);
        }

        return FILE_SERVICE.read(channel, 0, array, 0, array.length)
                .map(bytesRead -> {
                    ByteBuf buf = ByteBuf.wrap(array, 0, bytesRead);
                    System.out.println(buf.getString(UTF_8));
                    return buf;
                });
    }
    //[END REGION_1]

    public static void main(String[] args) {
        Promises.sequence(
                        () -> writeToFile(),
                        () -> readFromFile().toVoid())
                .whenComplete(($, e) -> {
                    if (e != null) {
                        System.out.println("Something went wrong : " + e);
                    }
                    try {
                        delete(PATH);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    EXECUTOR_SERVICE.shutdown();
                });

        EVENTLOOP.run();
    }
}
