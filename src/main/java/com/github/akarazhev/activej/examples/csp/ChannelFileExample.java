package com.github.akarazhev.activej.examples.csp;

import io.activej.bytebuf.ByteBufStrings;
import io.activej.csp.consumer.ChannelConsumers;
import io.activej.csp.file.ChannelFileReader;
import io.activej.csp.file.ChannelFileWriter;
import io.activej.csp.supplier.ChannelSuppliers;
import io.activej.eventloop.Eventloop;
import io.activej.promise.Promise;
import io.activej.promise.Promises;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardOpenOption.WRITE;
import static java.util.concurrent.Executors.newSingleThreadExecutor;

public final class ChannelFileExample {
    private static final ExecutorService executor = newSingleThreadExecutor();
    private static final Eventloop eventloop = Eventloop.builder()
            .withCurrentThread()
            .build();
    private static final Path PATH;

    static {
        try {
            PATH = Files.createTempFile("NewFile", ".txt");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    //[START REGION_1]
    private static Promise<Void> writeToFile() {
        return ChannelSuppliers.ofValues(
                        ByteBufStrings.wrapAscii("Hello, this is example file\n"),
                        ByteBufStrings.wrapAscii("This is the second line of file\n"))
                .streamTo(ChannelFileWriter.open(executor, PATH, WRITE));
    }

    private static Promise<Void> readFile() {
        return ChannelFileReader.open(executor, PATH)
                .then(cfr -> cfr.streamTo(ChannelConsumers.ofConsumer(buf -> System.out.print(buf.asString(UTF_8)))));

    }
    //[END REGION_1]

    public static void main(String[] args) {
        Promises.sequence(
                ChannelFileExample::writeToFile,
                ChannelFileExample::readFile);

        eventloop.run();
        executor.shutdown();
    }
}
