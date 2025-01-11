package com.github.akarazhev.activej.examples.csp;

import io.activej.csp.consumer.ChannelConsumers;
import io.activej.csp.queue.ChannelBuffer;
import io.activej.csp.queue.ChannelQueue;
import io.activej.csp.queue.ChannelZeroBuffer;
import io.activej.csp.supplier.ChannelSupplier;
import io.activej.eventloop.Eventloop;
import io.activej.promise.Promises;

public final class ChannelBufferExample {
    //[START REGION_1]
    static final class ChannelBufferStream {
        public static void main(String[] args) {
            Eventloop eventloop = Eventloop.builder()
                    .withCurrentThread()
                    .build();

            ChannelBuffer<Integer> plate = new ChannelBuffer<>(5, 10);
            ChannelSupplier<Integer> granny = plate.getSupplier();
            Promises.loop(0,
                    apple -> apple < 25,
                    apple -> plate.put(apple).map($ -> {
                        System.out.println("Granny gives apple   #" + apple);
                        return apple + 1;
                    }));
            granny.streamTo(ChannelConsumers.ofConsumer(apple -> System.out.println("Grandson takes apple #" + apple)));
            eventloop.run();
        }
    }
    //[END REGION_1]

    //[START REGION_2]
    static final class ChannelBufferZeroExample {
        public static void main(String[] args) {
            Eventloop eventloop = Eventloop.builder()
                    .withCurrentThread()
                    .build();

            ChannelQueue<Integer> buffer = new ChannelZeroBuffer<>();
            ChannelSupplier<Integer> granny = buffer.getSupplier();

            Promises.loop(0,
                    apple -> apple < 10,
                    apple -> buffer.put(apple).map($ -> {
                        System.out.println("Granny gives apple   #" + apple);
                        return apple + 1;
                    }));

            granny.streamTo(ChannelConsumers.<Integer>ofConsumer(apple ->
                    System.out.println("Grandson takes apple #" + apple)).async());

            eventloop.run();
        }
    }
    //[END REGION_2]
}
