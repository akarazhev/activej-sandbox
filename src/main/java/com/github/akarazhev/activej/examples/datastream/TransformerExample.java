package com.github.akarazhev.activej.examples.datastream;

import io.activej.datastream.consumer.AbstractStreamConsumer;
import io.activej.datastream.consumer.StreamConsumer;
import io.activej.datastream.processor.transformer.StreamTransformer;
import io.activej.datastream.supplier.AbstractStreamSupplier;
import io.activej.datastream.supplier.StreamSupplier;
import io.activej.datastream.supplier.StreamSuppliers;
import io.activej.eventloop.Eventloop;
import io.activej.reactor.ImplicitlyReactive;

import static io.activej.common.exception.FatalErrorHandlers.rethrow;

/**
 * Example of creating custom StreamTransformer, which takes strings from input stream
 * and transforms strings to their length if particular length is less than MAX_LENGTH
 */
public final class TransformerExample extends ImplicitlyReactive implements StreamTransformer<String, Integer> {
    private static final int MAX_LENGTH = 10;

    //[START REGION_1]
    private final AbstractStreamConsumer<String> input = new AbstractStreamConsumer<>() {
        @Override
        protected void onEndOfStream() {
            output.sendEndOfStream();
        }
    };

    private final AbstractStreamSupplier<Integer> output = new AbstractStreamSupplier<>() {
        @Override
        protected void onResumed() {
            input.resume(item -> {
                int len = item.length();
                if (len < MAX_LENGTH) {
                    output.send(len);
                }
            });
        }

        @Override
        protected void onSuspended() {
            input.suspend();
        }
    };
    //[END REGION_1]

    {
        input.getAcknowledgement()
                .whenException(output::closeEx);
        output.getAcknowledgement()
                .whenResult(input::acknowledge)
                .whenException(input::closeEx);
    }

    @Override
    public StreamConsumer<String> getInput() {
        return input;
    }

    @Override
    public StreamSupplier<Integer> getOutput() {
        return output;
    }

    //[START REGION_2]
    public static void main(String[] args) {
        Eventloop eventloop = Eventloop.builder()
                .withCurrentThread()
                .withFatalErrorHandler(rethrow())
                .build();

        StreamSupplier<String> source = StreamSuppliers.ofValues("testdata", "testdata1", "testdata1000");
        TransformerExample transformer = new TransformerExample();

        source.transformWith(transformer)
                .toList()
                .whenResult(v -> System.out.println(v));

        eventloop.run();
    }
    //[END REGION_2]
}

