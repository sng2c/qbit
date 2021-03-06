/*
 * Copyright (c) 2015. Rick Hightower, Geoff Chandler
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  		http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * QBit - The Microservice lib for Java : JSON, WebSocket, REST. Be The Web!
 */

package io.advantageous.qbit.events.impl;

import io.advantageous.boon.core.Sys;
import io.advantageous.qbit.QBit;
import io.advantageous.qbit.annotation.Listen;
import io.advantageous.qbit.annotation.OnEvent;
import io.advantageous.qbit.client.ClientProxy;
import io.advantageous.qbit.events.EventConsumer;
import io.advantageous.qbit.events.EventManager;
import io.advantageous.qbit.events.EventSubscriber;
import io.advantageous.qbit.message.Event;
import io.advantageous.qbit.service.ServiceProxyUtils;
import io.advantageous.qbit.service.ServiceQueue;
import io.advantageous.qbit.test.TimedTesting;
import org.junit.Before;
import org.junit.Test;

import static io.advantageous.boon.core.Exceptions.die;
import static io.advantageous.boon.core.IO.puts;
import static io.advantageous.qbit.events.EventUtils.callbackEventListener;
import static io.advantageous.qbit.service.ServiceBuilder.serviceBuilder;
import static io.advantageous.qbit.service.ServiceContext.serviceContext;

@SuppressWarnings("ALL")
public class BoonEventManagerTest extends TimedTesting {

    EventManager eventManager;

    ClientProxy clientProxy;

    volatile int subscribeMessageCount = 0;

    volatile int consumerMessageCount = 0;

    boolean ok;

    @Before
    public void setup() {

        super.setupLatch();
        eventManager = QBit.factory().systemEventManager();
        clientProxy = (ClientProxy) eventManager;
        subscribeMessageCount = 0;
        consumerMessageCount = 0;

    }


    @Test
    public void test() throws Exception {


        String rick = "rick";

        MyEventListener myEventListener = new MyEventListener();

        eventManager.listen(myEventListener);
        clientProxy.clientProxyFlush();

        eventManager.register(rick, new EventConsumer<Object>() {
            @Override
            public void listen(Event<Object> event) {
                //puts(event);
                consumerMessageCount++;
            }
        });


        eventManager.register(rick, new EventSubscriber<Object>() {
            @Override
            public void listen(Event<Object> event) {
                //puts(event);
                subscribeMessageCount++;
            }
        });

        eventManager.register(rick, callbackEventListener(event -> {
            if (subscribeMessageCount < 1000) puts(event);
            subscribeMessageCount++;
        }));


        final MyServiceConsumer myServiceConsumer = new MyServiceConsumer();

        final MyService myService = new MyService();

        ServiceQueue consumerServiceQueue = serviceBuilder()
                .setServiceObject(myServiceConsumer)
                .setInvokeDynamic(false).build().startServiceQueue();

        clientProxy.clientProxyFlush();

        Sys.sleep(100);


        eventManager.send(rick, "Hello Rick");
        clientProxy.clientProxyFlush();

        Sys.sleep(1000);

        ok = subscribeMessageCount == 2 || die(subscribeMessageCount);

        ok = consumerMessageCount == 1 || die();


        ok = myEventListener.callCount == 1 || die();

        ok = myServiceConsumer.callCount() == 1 || die();


        Sys.sleep(100);
        ServiceQueue senderServiceQueue = serviceBuilder().setServiceObject(myService)
                .setInvokeDynamic(false).build().startServiceQueue();

        final MyServiceClient clientProxy = senderServiceQueue.createProxy(MyServiceClient.class);

        clientProxy.sendHi("Hello");
        ServiceProxyUtils.flushServiceProxy(clientProxy);

        Sys.sleep(100);

        ok = subscribeMessageCount == 4 || die(subscribeMessageCount);

        ok = consumerMessageCount == 2 || die();


        ok = myEventListener.callCount == 2 || die();

        ok = myServiceConsumer.callCount() == 2 || die();

    }


    //@Test This takes a long time to run. I only need it for perf tuning.
    public void testPerfMultiple() throws Exception {

        for (int index = 0; index < 5; index++) {
            testPerf();
            Sys.sleep(5_000);
        }
    }

    @Test
    public void testPerf() throws Exception {


        eventManager = QBit.factory().systemEventManager();
        consumerMessageCount = 0;
        Sys.sleep(100);
        subscribeMessageCount = 0;
        Sys.sleep(100);


        String rick = "rick";

        eventManager.register(rick, event -> consumerMessageCount++);


        eventManager.register(rick, callbackEventListener(event -> {
            subscribeMessageCount++;
        }));

        clientProxy.clientProxyFlush();
        Sys.sleep(100);


        long start = System.currentTimeMillis();

        for (int index = 0; index < 100_000; index++) {
            eventManager.send(rick, "PERF");

        }


        clientProxy.clientProxyFlush();
        Sys.sleep(100);


        super.waitForTrigger(60, o -> consumerMessageCount >= 90_000);


        long stop = System.currentTimeMillis();
        Sys.sleep(100);


        long duration = (stop - start);

        if (duration > 10_000) {
            die("duration", duration);
        }


        if (consumerMessageCount < 90_000) {
            die("consumerMessageCount", consumerMessageCount);
        }

        puts("Duration to sendText messages", duration, "ms. \nconsume message count", consumerMessageCount, "\ntotal message count", consumerMessageCount + subscribeMessageCount);


    }


    public interface MyServiceClient {

        void sendHi(String hi);
    }

    public static class MyEventListener {

        volatile int callCount = 0;

        @Listen("rick")
        void listen(String message) {
            callCount++;
        }
    }

    public static class MyService {

        private void queueInit() {
            puts("QUEUE START MyService");
        }

        public void sendHi(String hi) {
            serviceContext().send("rick", "hello rick " + hi);
        }
    }

    public static class MyServiceConsumer {

        int callCount = 0;

        public MyServiceConsumer() {
            puts("MyService created");
        }


        @OnEvent("rick")
        private void listen(String message) {
            //puts(message);
            callCount++;
        }


        private int callCount() {
            return callCount;
        }


    }


}