package org.dsh.metrics.tests;

import static org.testng.AssertJUnit.assertEquals;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.dsh.metrics.Counter;
import org.dsh.metrics.EventListener;
import org.dsh.metrics.Gauge;
import org.dsh.metrics.MetricRegistry;
import org.dsh.metrics.Timer;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;


// @todo update to properly assert..
abstract public class BaseListenerTest {
    abstract public EventListener getListener();

    protected MetricRegistry reg;

    @BeforeMethod
    public void beforeEachTestMethod() {
        if (reg !=null) {
            reg.removeAllEventListeners();
        }
        reg = buildRegistry();
    }

    private MetricRegistry buildRegistry() {
        return new MetricRegistry.Builder("dsh-metrics","test")
                .addTag("dc", "dataCenter1")
                .addTag("host", "host-1.xyz.org")
                .build();
    }

    @Test
    public void counterTest() {
        try {
            reg.counter("counter1");
            reg.addEventListener(getListener());
            for (int i = 0; i < 10_000;i++) {
                reg.counter("counter1").increment();
            }
            Thread.sleep(5000);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void counterTestTags1() {
        try {
        	String[] tgs = {"customer","xyz","queue","super"};
            reg.counter("counter1",tgs);
            reg.addEventListener(getListener());
            for (int i = 0; i < 10_000;i++) {
                reg.counter("counter1").increment();
            }
            Thread.sleep(5000);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }


    @Test
    public void counterNoTagsWithNoLookupTest() {
        // counter is constructed, then each use of counter, Builder is used to retrieve it.
        try {
            Counter c = reg.counter("counter1");
            reg.addEventListener(getListener());
            for (int i = 0; i < 10_000; i++) {
                Thread.sleep(10);
                c.increment();
            }
            Thread.sleep(1000);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
    @Test
    public void counterNoTagsWithLookupTest() {
        // counter is constructed, then each use of counter, Builder is used to retrieve it.
        try {
            reg.counter("counter1");
            reg.addEventListener(getListener());
            for (int i = 0; i < 10_000; i++) {
                reg.counter("counter1").increment();;
            }
            Thread.sleep(1000);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void counterWithTagsReuseTest() {
        // counter is constructed, then each use of counter, Builder is used to retrieve it.
        try {
            Counter c = reg.counterWithTags("counter1")
               .addTag("customer","customer-x")
               .addTag("queue","superQueue").build();
            reg.addEventListener(getListener());
            for (int i = 0; i < 10_000; i++) {
                c.increment();
            }
            Thread.sleep(1000);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }


    @Test
    public void counterWithTagsRebuildTest() {
        // counter is constructed, then each use of counter, Builder is used to retrieve it.
        try {
            reg.counterWithTags("counter1")
               .addTag("customer","customer-x")
               .addTag("queue","someQueue").build();
            reg.addEventListener(getListener());
            for (int i = 0; i < 10_000; i++) {
                reg.counterWithTags("counter1")
                    .addTag("customer","customer_x")
                    .addTag("queue","someQueue").build().increment();
            }
            Thread.sleep(1000);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }


    @Test
    public void timerWithTagsTest() {
        try {
            reg.addEventListener(getListener());
            for (int i = 0; i < 100 ; i++) {
                Timer t = reg.timerWithTags("testTimer")
                			 .addTag("cust", "customer-x")
                			 .build();
                if (i % 5 == 0) {
                    Thread.sleep(100);
                }
                else {
                    Thread.sleep(10);
                }
                t.stop();
            }

            Thread.sleep(10000);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void timerTest() {
        try {
            reg.addEventListener(getListener());
            Timer t = reg.timer("testTimer");
            Thread.sleep(1000);
            t.stop();
            Thread.sleep(10000);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void eventTest() {
        try {
            reg.addEventListener(getListener());
            reg.event("event");
            Thread.sleep(1000);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void eventTestWithTags() {
        try {
            reg.addEventListener(getListener());
            reg.eventWithTags("event")
                .addTag("customer","customer-x")
                .addTag("queue", "foobar-queue")
                .build();

            Thread.sleep(1000);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void gaugeTest() {
        try {
            reg.addEventListener(getListener());
            Random r = new Random();
            reg.scheduleGauge("test-gauge", 1, () -> {return r.nextInt(100);});
            Thread.sleep(5000);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void gaugeWithTagsTest() {
        try {
            reg.addEventListener(getListener());
            Map<String,String> tags = new HashMap<>();
            tags.put("customer", "customer-X");
            tags.put("queue", "fastQueue");
            reg.scheduleGauge("test-gauge", 1, new Gauge<Double>() {

                Random r = new Random();
                @Override
                public Double getValue() {
                    return r.nextDouble();
                }

                @Override
                public Map<String,String> getTags() {
                    return tags;
                }
            });
            Thread.sleep(5000);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void gaugeRecreatedWithTagsTest() {
        try {
            reg.addEventListener(getListener());
            Map<String,String> tags = new HashMap<>();
            tags.put("core", "0");
            reg.scheduleGauge("cpu-gauge", 1, new Gauge<Integer>() {

                Random r = new Random();
                @Override
                public Integer getValue() {
                    return r.nextInt();
                }

                @Override
                public Map<String,String> getTags() {
                    return tags;
                }
            });
            Thread.sleep(5000);
            // recreate same gauge - we should not see 'double gauge' running..since is the same ident
            reg.scheduleGauge("cpu-gauge", 1, new Gauge<Integer>() {

                Random r = new Random();
                @Override
                public Integer getValue() {
                    return r.nextInt();
                }

                @Override
                public Map<String,String> getTags() {
                    return tags;
                }
            });

            Map<String,String> tagscore2 = new HashMap<>();
            tagscore2.put("core", "1");
            // we should see the test-gauge-2 at same interval
            reg.scheduleGauge("cpu-gauge", 1, new Gauge<Integer>() {

                Random r = new Random();
                @Override
                public Integer getValue() {
                    return r.nextInt();
                }

                @Override
                public Map<String,String> getTags() {
                    return tagscore2;
                }
            });

            Thread.sleep(5000);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void pause(EventListener listener, int expectedBufferSize, long timeout) {
        try {
            long startTime = System.currentTimeMillis();
            while (listener.eventsBuffered() != expectedBufferSize && (System.currentTimeMillis() - startTime < timeout)) {
                Thread.sleep(100);
            }
            if (listener.eventsBuffered() == expectedBufferSize)
                return;
        }
        catch(Exception e) {

        }
        assertEquals("buffered events mismatch!",expectedBufferSize,listener.eventsBuffered());
    }

    private Map<String,String> getTags(String dataCenter, String host) {
        Map<String,String> tags = new HashMap<>();
        tags.put("dc", dataCenter);
        tags.put("host", host);
        return tags;
    }
}
