package com.example.mvmax.speakrecognition.speakrecognition.util;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;

public final class ConcurrencyUtils {

    private static final ExecutorService THREAD_POOL = Executors.newCachedThreadPool(new CustomThreadFactory(new CustomExceptionHandler()));

    private static final int NTHREADS = prevPow2(getNumberOfProcessors());

    private ConcurrencyUtils() {

    }

    private static class CustomExceptionHandler implements Thread.UncaughtExceptionHandler {

        public void uncaughtException(final Thread t, final Throwable e) {
            e.printStackTrace();
        }

    }

    private static class CustomThreadFactory implements ThreadFactory {

        private static final ThreadFactory defaultFactory = Executors.defaultThreadFactory();

        private final Thread.UncaughtExceptionHandler handler;

        CustomThreadFactory(final Thread.UncaughtExceptionHandler handler) {
            this.handler = handler;
        }

        public Thread newThread(final Runnable r) {
            final Thread t = defaultFactory.newThread(r);
            t.setUncaughtExceptionHandler(handler);
            return t;
        }
    }

    private static int getNumberOfProcessors() {
        return Runtime.getRuntime().availableProcessors();
    }

    public static int getNumberOfThreads() {
        return NTHREADS;
    }

    public static int getThreadsBeginN_1D_FFT_2Threads() {
        return 8192;
    }

    public static int getThreadsBeginN_1D_FFT_4Threads() {
        return 65536;
    }

    public static int nextPow2(int x) {
        if (x < 1) {
            throw new IllegalArgumentException("x must be greater or equal 1");
        }
        if ((x & (x - 1)) == 0) {
            return x; // x is already a power-of-two number 
        }
        x |= (x >>> 1);
        x |= (x >>> 2);
        x |= (x >>> 4);
        x |= (x >>> 8);
        x |= (x >>> 16);
        x |= (x >>> 32);
        return x + 1;
    }

    private static int prevPow2(final int x) {
        if (x < 1) {
            throw new IllegalArgumentException("x must be greater or equal 1");
        }
        return (int) Math.pow(2, Math.floor(Math.log(x) / Math.log(2)));
    }

    public static boolean isPowerOf2(final int x) {
        if (x <= 0) {
            return false;
        } else {
            return (x & (x - 1)) == 0;
        }
    }

    public static Future<?> submit(final Runnable task) {
        return THREAD_POOL.submit(task);
    }

    public static void waitForCompletion(final Future<?>[] futures) {
        final int size = futures.length;
        try {
            for (int j = 0; j < size; j++) {
                futures[j].get();
            }
        } catch (final ExecutionException ex) {
            ex.printStackTrace();
        } catch (final InterruptedException e) {
            e.printStackTrace();
        }
    }
}