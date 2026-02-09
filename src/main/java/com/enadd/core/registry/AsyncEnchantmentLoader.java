package com.enadd.core.registry;

import com.enadd.EnchAdd;
import org.bukkit.Bukkit;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;


public class AsyncEnchantmentLoader {

    private static AsyncEnchantmentLoader instance;
    private final ExecutorService executorService;
    private final AtomicInteger loadedCount;
    private volatile boolean isLoading;

    private AsyncEnchantmentLoader() {
        this.executorService = Executors.newFixedThreadPool(
            Math.max(2, Runtime.getRuntime().availableProcessors() / 2)
        );
        this.loadedCount = new AtomicInteger(0);
        this.isLoading = false;
    }

    public static AsyncEnchantmentLoader getInstance() {
        if (instance == null) {
            instance = new AsyncEnchantmentLoader();
        }
        return instance;
    }

    public CompletableFuture<Void> loadAllEnchantmentsAsync() {
        if (isLoading) {
            return CompletableFuture.completedFuture(null);
        }

        isLoading = true;
        loadedCount.set(0);

        return CompletableFuture.runAsync(() -> {
            long startTime = System.currentTimeMillis();

            try {
                EnchantmentRegistry.registerAll();

                long duration = System.currentTimeMillis() - startTime;
                Bukkit.getScheduler().runTask(
                    EnchAdd.getInstance(),
                    () -> {
                        int count = EnchantmentRegistry.getCount();
                        EnchAdd.getInstance().getLogger()
                            .info("异步加载完成: " + count + " 个附魔，耗时: " + duration + "ms");
                        isLoading = false;
                    }
                );
            } catch (Exception e) {
                Bukkit.getScheduler().runTask(
                    EnchAdd.getInstance(),
                    () -> {
                        EnchAdd.getInstance().getLogger()
                            .severe("异步加载失败: " + e.getMessage());
                        e.printStackTrace();
                        isLoading = false;
                    }
                );
            }
        }, executorService);
    }

    public CompletableFuture<Boolean> isLoadedAsync() {
        return CompletableFuture.supplyAsync(() -> !isLoading, executorService);
    }

    public void shutdown() {
        executorService.shutdown();
    }

    public boolean isLoading() {
        return isLoading;
    }

    public int getLoadedCount() {
        return loadedCount.get();
    }
}
