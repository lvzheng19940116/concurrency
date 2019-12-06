package concurrency.concurrency;

import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.*;

@SpringBootTest
@Slf4j
@SuppressWarnings("all")
class ConcurrencyApplicationTests {

    private static int threadTotal = 200;
    private static int clicentTotal = 5000;
    private static int count = 0;
    //原子类  cas  线程安全 全局准确
    public static AtomicInteger atomicInteger = new AtomicInteger(0);
    // 全局准确
    public static AtomicLong atomicLong = new AtomicLong(0);
    //效率高 并发计数可能计数失败
    public static LongAdder longAdder = new LongAdder();


    private static Map<Integer, Integer> map = Maps.newHashMap();

    @Test
    void contextLoads() {
        ExecutorService exec = Executors.newCachedThreadPool();
        /**
         *  信号量  模拟200个进程数值回小于5000
         *  如果使用1哥进程 没有问题 等于5000
         *  并发问题
         */
        Semaphore semaphore = new Semaphore(threadTotal);
        final CountDownLatch countDownLatch = new CountDownLatch(clicentTotal);
        for (int i = 0; i < clicentTotal; i++) {
            exec.execute(() -> {

                        try {
                            semaphore.acquire();
                            add();
                            semaphore.release();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        //计数减一
                        countDownLatch.countDown();
                        //取数
                        long cpn = countDownLatch.getCount();
                        log.info(cpn + "");
                    }
            );
        }
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        exec.shutdown();
        log.info("count{}", count);
    }

    private static void add() {
        count++;
    }


    private static int threadNum = 200;
    private static int clicentNum = 5000;

    @Test
    void concu() {
        ExecutorService exec = Executors.newCachedThreadPool();
        Semaphore semaphore = new Semaphore(threadNum);

        for (int i = 0; i < clicentNum; i++) {
            exec.execute(() -> {

                        try {
                            semaphore.acquire();
                            func(threadNum++);
                            semaphore.release();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    }
            );
        }
        exec.shutdown();
        log.info("mapSize{}", map.size());

    }

    private static void func(int threadNum) {
        map.put(threadNum, threadNum);
    }


    @Test
    void time() {
        String num = "000122";
        System.out.println(num.substring(0, 4));
    }


    private static AtomicReference<Integer> atomicReference = new AtomicReference<>(0);

    @Test
    void atomicReference() {
        atomicReference.compareAndSet(0, 2); // 2
        atomicReference.compareAndSet(0, 1); // no
        atomicReference.compareAndSet(1, 3); // no
        atomicReference.compareAndSet(2, 4); // 4
        atomicReference.compareAndSet(3, 5); // no
        log.info("count:{}", atomicReference.get());
    }


    private static AtomicBoolean isHappened = new AtomicBoolean(false);

    // 请求总数
    public static int clientTotal = 5000;

    // 同时并发执行的线程数
    public static int threadTotala = 200;

    @Test
        // 原子操作 只执行一次
    void atomicBoolean() throws Exception {
        ExecutorService executorService = Executors.newCachedThreadPool();
        final Semaphore semaphore = new Semaphore(threadTotala);
        final CountDownLatch countDownLatch = new CountDownLatch(clientTotal);
        for (int i = 0; i < clientTotal; i++) {
            executorService.execute(() -> {
                try {
                    semaphore.acquire();
                    test();
                    semaphore.release();
                } catch (Exception e) {
                    log.error("exception", e);
                }
                countDownLatch.countDown();
            });
        }
        countDownLatch.await();
        executorService.shutdown();
        log.info("isHappened:{}", isHappened.get());
    }

    private static void test() {
        if (isHappened.compareAndSet(false, true)) {
            log.info("execute");
        }
    }

}
