package io.advantageous.qbit.http.jetty.test;

import io.advantageous.qbit.annotation.RequestMapping;
import io.advantageous.qbit.annotation.RequestMethod;
import io.advantageous.qbit.client.Client;
import io.advantageous.qbit.client.ClientBuilder;
import io.advantageous.qbit.http.*;
import io.advantageous.qbit.server.ServiceServer;
import io.advantageous.qbit.server.ServiceServerBuilder;
import io.advantageous.qbit.service.Callback;
import io.advantageous.qbit.service.ServiceProxyUtils;
import org.boon.core.Sys;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.boon.Boon.puts;
import static org.boon.Exceptions.die;

/**
 * Created by rhightower on 2/14/15.
 */
public class FullJettyIntegration {


    Client client;
    ServiceServer server;
    HttpClient httpClient;
    ClientServiceInterface clientProxy;
    volatile int callCount;
    AtomicReference<String> pongValue;
    boolean ok;
    static volatile int port = 7777;
    private volatile int returnCount;

    static interface ClientServiceInterface {
        String ping(Callback<String> callback, String ping);
    }

    class MockService {

        @RequestMapping(method = RequestMethod.POST)
        public String ping(String ping) {
            callCount++;
            return ping + " pong";
        }
    }


    @Test
    public void testWebSocket() throws Exception {

        clientProxy.ping(new Callback<String>() {
            @Override
            public void accept(String s) {
                puts(s);
                pongValue.set(s);
            }
        }, "hi");

        ServiceProxyUtils.flushServiceProxy(clientProxy);

        while (pongValue.get() == null) {
            Sys.sleep(100);
        }

        final String pongValue = this.pongValue.get();
        ok = pongValue.equals("hi pong") || die();

    }



    @Test
    public void testWebSocketFlushHappy() throws Exception {



        final Callback<String> callback = new Callback<String>() {
            @Override
            public void accept(String s) {
                returnCount++;

                if (returnCount % 2 == 0) {
                    puts("return count", returnCount);
                }

                puts("                     PONG");
                pongValue.set(s);
            }
        };

        for (int index=0; index< 11; index++) {

            clientProxy.ping(callback, "hi");

        }

        ServiceProxyUtils.flushServiceProxy(clientProxy);
        Sys.sleep(1000);

        client.flush();
        Sys.sleep(5000);


        puts("HERE                        ", callCount, returnCount);

        ok = returnCount >= callCount -1 || die(returnCount, callCount); //TODO off by one error?



    }



    @Test
    public void testWebSocketSend10() throws Exception {



        final Callback<String> callback = new Callback<String>() {
            @Override
            public void accept(String s) {
                returnCount++;

                if (returnCount % 2 == 0) {
                    puts("return count", returnCount);
                }

                puts("                     PONG");
                pongValue.set(s);
            }
        };

        for (int index=0; index< 10; index++) {

            clientProxy.ping(callback, "hi");

        }

        ServiceProxyUtils.flushServiceProxy(clientProxy);
        Sys.sleep(100);



        client.flush();
        Sys.sleep(500);


        puts("HERE                        ", callCount, returnCount);

        ok = returnCount == callCount || die(returnCount, callCount);



    }

    @Test
    public void testRestCallSimple() throws Exception {

        final HttpRequest request = new HttpRequestBuilder()
                .setUri("/services/mockservice/ping")
                .setJsonBodyForPost("\"hello\"")
                .setTextResponse(new HttpTextResponse() {
                    @Override
                    public void response(int code, String mimeType, String body) {
                        if (code==200) {
                            pongValue.set(body);
                        } else {
                            pongValue.set("ERROR " + body + " code " + code);
                            throw new RuntimeException("ERROR " + code + " " + body);

                        }
                    }
                })
                .build();

        httpClient.sendHttpRequest(request);

        httpClient.flush();


        while (pongValue.get() == null) {
            Sys.sleep(100);
        }


        final String pongValue = this.pongValue.get();
        ok = pongValue.equals("\"hello pong\"") || die("message##", pongValue, "##message");

    }

    @Before
    public synchronized void setup() throws Exception {

        port+=10;
        pongValue = new AtomicReference<>();

        httpClient = new HttpClientBuilder().setPort(port).build();

        puts("PORT", port);

        client = new ClientBuilder().setPort(port).build();
        server = new ServiceServerBuilder().setPort(port).build();

        server.initServices(new MockService());

        server.start();

        Sys.sleep(200);

        clientProxy = client.createProxy(ClientServiceInterface.class, "mockService");
        Sys.sleep(100);
        httpClient.start();
        Sys.sleep(100);
        client.start();

        callCount = 0;
        pongValue.set(null);

        Sys.sleep(200);



    }

    @After
    public void teardown() throws Exception {

        port++;


        Sys.sleep(200);
        server.stop();
        Sys.sleep(200);
        client.stop();
        httpClient.stop();
        Sys.sleep(200);
        server = null;
        client = null;
        System.gc();
        Sys.sleep(1000);

    }
}

