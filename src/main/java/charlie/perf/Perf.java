package charlie.perf;

import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by charlie.chang on 6/27/16.
 */
public class Perf {


    public static void main(String[] argus) throws Exception {

        final int MAX_THREAD = 16;
        final int STEP = 1;
        final int COUNTER_PER_THREAD = 50000;

        //Wrap up
        System.out.println("cores:" + Runtime.getRuntime().availableProcessors());
        test(MAX_THREAD,COUNTER_PER_THREAD,new ConcurrentLinkedQueue());
        test(MAX_THREAD,COUNTER_PER_THREAD,new LinkedBlockingQueue());

        for(int s = STEP;s <= MAX_THREAD; s+= STEP) {
            System.out.println(s + "\t" + test(s,COUNTER_PER_THREAD,new ConcurrentLinkedQueue()) + "\t" + test(4,COUNTER_PER_THREAD,new LinkedBlockingQueue()));
        }

    }

    static long test(int nThread, final int countPerThread, final Queue queue)
                    throws InterruptedException {
        final Thread[] workerThreads = new Thread[nThread];
        final long[] times = new long[nThread];

        for(int t = 0;t < workerThreads.length;t++) {
            final int tt = t;
            final boolean isProducer = t < (workerThreads.length >> 1);
            workerThreads[t] = new Thread(new Runnable(){
                public void run() {
                    times[tt] = System.currentTimeMillis();

                    for(int c = 0;c < countPerThread;c++) {
                        if(isProducer) {
                            queue.offer(new Object());
                        } else {
                            if(queue instanceof LinkedBlockingQueue) {
                                try {
                                    ((BlockingQueue) queue).poll(0, TimeUnit.MILLISECONDS);
                                } catch (InterruptedException e) {}
                            } else {
                                queue.poll();
                            }
                        }
                    }

                    synchronized (times) {
                        times[tt] = System.currentTimeMillis() - times[tt];
                    } //force memory barrier here
                }
            });
            workerThreads[t].start();
        }

        long timeInMs = 0;
        for(int t = 0;t < workerThreads.length;t++) {
            workerThreads[t].join();
            timeInMs += times[t];
        }

        return timeInMs;
    }

}
