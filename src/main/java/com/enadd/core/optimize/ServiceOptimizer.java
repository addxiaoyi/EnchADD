package com.enadd.core.optimize;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.function.Consumer;


public final class ServiceOptimizer {
    private static final Logger LOGGER = Logger.getLogger(ServiceOptimizer.class.getName());

    private static final AtomicBoolean ENABLED = new AtomicBoolean(false);
    private static final AtomicInteger ACTIVE_SERVICES = new AtomicInteger(0);
    private static final AtomicInteger TOTAL_SERVICES = new AtomicInteger(0);

    private static ConcurrentHashMap<String, ServiceInstance> services;
    private static ConcurrentHashMap<String, Set<String>> serviceDependencies;
    private static ConcurrentHashMap<String, Set<String>> dependentServices;
    private static ScheduledExecutorService monitorScheduler;

    private static volatile long healthCheckIntervalMs = 10000L;
    private static volatile double maxCpuPercentPerService = 10.0;
    private static volatile long maxMemoryMBPerService = 256L;
    private static volatile int maxRequestLatencyMs = 1000;

    private static final int CPU_SAMPLES = 5;
    private static final long CPU_SAMPLE_INTERVAL_MS = 1000L;

    private ServiceOptimizer() {}

    public static synchronized void initialize() {
        if (ENABLED.get()) {
            return;
        }

        services = new ConcurrentHashMap<>();
        serviceDependencies = new ConcurrentHashMap<>();
        dependentServices = new ConcurrentHashMap<>();

        monitorScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "ServiceOptimizer-Monitor");
            t.setDaemon(true);
            return t;
        });

        monitorScheduler.scheduleAtFixedRate(ServiceOptimizer::performHealthChecks,
                                            healthCheckIntervalMs,
                                            healthCheckIntervalMs,
                                            TimeUnit.MILLISECONDS);

        monitorScheduler.scheduleAtFixedRate(ServiceOptimizer::analyzeRedundancy,
                                            60000L,
                                            60000L,
                                            TimeUnit.MILLISECONDS);

        ENABLED.set(true);
        LOGGER.info("ServiceOptimizer initialized");
    }

    public static String registerService(String name, Runnable initializer,
                                         Runnable healthCheck, Consumer<Boolean> stateChange) {
        if (!ENABLED.get()) {
            return null;
        }

        if (services.containsKey(name)) {
            LOGGER.log(Level.WARNING, "Service already registered: {0}", name);
            return name;
        }

        ServiceInstance instance = new ServiceInstance(name, initializer, healthCheck, stateChange);
        services.put(name, instance);
        serviceDependencies.put(name, ConcurrentHashMap.newKeySet());
        dependentServices.put(name, ConcurrentHashMap.newKeySet());

        TOTAL_SERVICES.incrementAndGet();

        return name;
    }

    public static void registerDependency(String service, String dependency) {
        if (!ENABLED.get() || service == null || dependency == null) return;

        serviceDependencies.computeIfAbsent(service, k -> ConcurrentHashMap.newKeySet()).add(dependency);
        dependentServices.computeIfAbsent(dependency, k -> ConcurrentHashMap.newKeySet()).add(service);
    }

    public static void startService(String name) {
        ServiceInstance service = services.get(name);
        if (service == null) return;

        service.start();
        ACTIVE_SERVICES.incrementAndGet();
    }

    public static void stopService(String name) {
        ServiceInstance service = services.get(name);
        if (service == null) return;

        Set<String> dependents = dependentServices.get(name);
        if (dependents != null && !dependents.isEmpty()) {
            LOGGER.log(Level.WARNING, "Cannot stop service {0} - still depended on by: {1}", new Object[]{name, dependents});
            return;
        }

        service.stop();
        ACTIVE_SERVICES.decrementAndGet();
    }

    public static void restartService(String name) {
        ServiceInstance service = services.get(name);
        if (service == null) return;

        Set<String> dependents = dependentServices.get(name);
        if (dependents != null && !dependents.isEmpty()) {
            dependents.forEach(ServiceOptimizer::stopService);
        }

        service.stop();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        service.start();

        if (dependents != null) {
            dependents.forEach(ServiceOptimizer::startService);
        }
    }

    public static void submitTask(String serviceName, Runnable task) {
        ServiceInstance service = services.get(serviceName);
        if (service != null) {
            service.submitTask(task);
        } else {
            task.run();
        }
    }

    private static void performHealthChecks() {
        if (!ENABLED.get()) return;

        services.forEach((name, service) -> {
            if (!service.isRunning()) return;

            double cpuUsage = measureCpuUsage(service);
            long memoryUsage = service.getMemoryUsage();
            long latency = measureLatency(service);

            service.updateMetrics(cpuUsage, memoryUsage, latency);

            if (cpuUsage > maxCpuPercentPerService * 2) {
                LOGGER.log(Level.WARNING, "High CPU usage detected for service {0}: {1}%", new Object[]{name, cpuUsage});
            }

            if (memoryUsage > maxMemoryMBPerService * 2 * 1024 * 1024) {
                LOGGER.log(Level.WARNING, "High memory usage detected for service {0}: {1}MB",
                              new Object[]{name, (memoryUsage / 1024 / 1024)});
            }

            if (latency > maxRequestLatencyMs * 3) {
                LOGGER.log(Level.WARNING, "High latency detected for service {0}: {1}ms", new Object[]{name, latency});
            }
        });
    }

    private static double measureCpuUsage(ServiceInstance service) {
        double[] samples = new double[CPU_SAMPLES];

        for (int i = 0; i < CPU_SAMPLES; i++) {
            long before = System.nanoTime();
            service.runHealthCheck();
            long after = System.nanoTime();
            samples[i] = Math.min(100.0, (double)(after - before) / CPU_SAMPLE_INTERVAL_MS * 100);

            try {
                Thread.sleep(CPU_SAMPLE_INTERVAL_MS);
            } catch (InterruptedException e) {
                break;
            }
        }

        return Arrays.stream(samples).average().orElse(0);
    }

    private static long measureLatency(ServiceInstance service) {
        long start = System.currentTimeMillis();
        service.runHealthCheck();
        return System.currentTimeMillis() - start;
    }

    private static void analyzeRedundancy() {
        if (!ENABLED.get()) return;

        List<RedundancyReport> reports = new ArrayList<>();

        for (Map.Entry<String, ServiceInstance> entry : services.entrySet()) {
            String name = entry.getKey();

            List<String> similarServices = findSimilarServices(name);

            if (!similarServices.isEmpty()) {
                reports.add(new RedundancyReport(name, similarServices,
                    calculateSimilarityScore(name, similarServices)));
            }
        }

        if (!reports.isEmpty()) {
            LOGGER.info("Redundancy analysis found " + reports.size() + " potential issues");
            reports.forEach(report ->
                LOGGER.info("  - " + report.getServiceName() + " similar to: " + report.getSimilarServices()));
        }
    }

    private static List<String> findSimilarServices(String serviceName) {
        List<String> similar = new ArrayList<>();
        ServiceInstance target = services.get(serviceName);

        if (target == null) return similar;

        for (Map.Entry<String, ServiceInstance> entry : services.entrySet()) {
            if (entry.getKey().equals(serviceName)) continue;

            ServiceInstance candidate = entry.getValue();
            if (areServicesSimilar(target, candidate)) {
                similar.add(entry.getKey());
            }
        }

        return similar;
    }

    private static boolean areServicesSimilar(ServiceInstance a, ServiceInstance b) {
        String[] keywordsA = {"cache", "storage", "data", "db", "database"};
        String[] keywordsB = {"cache", "storage", "data", "db", "database"};

        String nameA = a.getName().toLowerCase();
        String nameB = b.getName().toLowerCase();

        for (String keyword : keywordsA) {
            if (nameA.contains(keyword)) {
                for (String keywordB : keywordsB) {
                    if (nameB.contains(keywordB) && keyword.equals(keywordB)) {
                        return true;
                    }
                }
            }
        }

        Set<String> depsA = serviceDependencies.get(a.getName());
        Set<String> depsB = serviceDependencies.get(b.getName());

        if (depsA != null && depsB != null) {
            Set<String> intersection = new HashSet<>(depsA);
            intersection.retainAll(depsB);
            return !intersection.isEmpty();
        }

        return false;
    }

    private static double calculateSimilarityScore(String service, List<String> similarServices) {
        if (similarServices.isEmpty()) return 0;

        double totalScore = 0;
        for (String similar : similarServices) {
            totalScore += calculatePairwiseSimilarity(service, similar);
        }
        return totalScore / similarServices.size();
    }

    private static double calculatePairwiseSimilarity(String a, String b) {
        Set<String> depsA = serviceDependencies.get(a);
        Set<String> depsB = serviceDependencies.get(b);

        double dependencyScore = 0;
        if (depsA != null && depsB != null) {
            Set<String> intersection = new HashSet<>(depsA);
            intersection.retainAll(depsB);
            Set<String> union = new HashSet<>(depsA);
            union.addAll(depsB);
            dependencyScore = union.isEmpty() ? 0 : (double) intersection.size() / union.size();
        }

        return dependencyScore;
    }

    public static List<ServiceInstance.ServiceMetrics> getAllMetrics() {
        List<ServiceInstance.ServiceMetrics> allMetrics = new ArrayList<>();
        services.forEach((name, service) ->
            allMetrics.add(service.getMetrics()));
        return allMetrics;
    }

    public static List<String> getServiceNames() {
        return new ArrayList<>(services.keySet());
    }

    public static ServiceStatus getServiceStatus(String name) {
        ServiceInstance service = services.get(name);
        if (service == null) return null;

        Set<String> deps = serviceDependencies.get(name);
        Set<String> dependents = dependentServices.get(name);

        return new ServiceStatus(
            name,
            service.isRunning(),
            service.getMetrics(),
            deps != null ? new ArrayList<>(deps) : Collections.emptyList(),
            dependents != null ? new ArrayList<>(dependents) : Collections.emptyList()
        );
    }

    public static void setHealthCheckInterval(long intervalMs) {
        healthCheckIntervalMs = Math.max(1000L, Math.min(60000L, intervalMs));
    }

    public static void setMaxCpuPercentPerService(double percent) {
        maxCpuPercentPerService = Math.max(1.0, Math.min(50.0, percent));
    }

    public static void setMaxMemoryMBPerService(long mb) {
        maxMemoryMBPerService = Math.max(64L, Math.min(1024L, mb));
    }

    public static void setMaxRequestLatencyMs(int ms) {
        maxRequestLatencyMs = Math.max(100, Math.min(5000, ms));
    }

    public static ServiceOptimizerReport getReport() {
        List<ServiceStatus> statuses = new ArrayList<>();
        List<RedundancyReport> redundancies = new ArrayList<>();

        for (String name : services.keySet()) {
            statuses.add(getServiceStatus(name));
        }

        for (Map.Entry<String, ServiceInstance> entry : services.entrySet()) {
            List<String> similar = findSimilarServices(entry.getKey());
            if (!similar.isEmpty()) {
                redundancies.add(new RedundancyReport(entry.getKey(), similar,
                    calculateSimilarityScore(entry.getKey(), similar)));
            }
        }

        int runningCount = 0;
        double totalCpu = 0;
        long totalMemory = 0;
        long totalLatency = 0;

        for (ServiceStatus status : statuses) {
            if (status.isRunning()) {
                runningCount++;
                ServiceInstance.ServiceMetrics metrics = status.getMetrics();
                if (metrics != null) {
                    totalCpu += metrics.getCpuUsage();
                    totalMemory += metrics.getMemoryUsage();
                    totalLatency += metrics.getAverageLatency();
                }
            }
        }

        double avgCpu = runningCount > 0 ? totalCpu / runningCount : 0;
        long avgMemory = runningCount > 0 ? totalMemory / runningCount : 0;
        long avgLatency = runningCount > 0 ? totalLatency / runningCount : 0;

        return new ServiceOptimizerReport(
            ENABLED.get(),
            statuses,
            redundancies,
            TOTAL_SERVICES.get(),
            ACTIVE_SERVICES.get(),
            runningCount,
            avgCpu,
            avgMemory,
            avgLatency
        );
    }

    public static boolean isEnabled() {
        return ENABLED.get();
    }

    public static synchronized void shutdown() {
        if (!ENABLED.get()) return;

        ENABLED.set(false);

        if (monitorScheduler != null && !monitorScheduler.isShutdown()) {
            monitorScheduler.shutdown();
            try {
                if (!monitorScheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    monitorScheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                monitorScheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        services.forEach((name, service) -> service.stop());
        services.clear();
        serviceDependencies.clear();
        dependentServices.clear();

        LOGGER.info("ServiceOptimizer shutdown complete");
    }

    public static final class RedundancyReport {
        private final String serviceName;
        private final List<String> similarServices;
        private final double similarityScore;

        public RedundancyReport(String serviceName, List<String> similarServices,
                               double similarityScore) {
            this.serviceName = serviceName;
            this.similarServices = similarServices;
            this.similarityScore = similarityScore;
        }

        public String getServiceName() { return serviceName; }
        public List<String> getSimilarServices() { return similarServices; }
        public double getSimilarityScore() { return similarityScore; }
    }

    public static final class ServiceStatus {
        private final String name;
        private final boolean running;
        private final ServiceInstance.ServiceMetrics metrics;
        private final List<String> dependencies;
        private final List<String> dependents;

        public ServiceStatus(String name, boolean running, ServiceInstance.ServiceMetrics metrics,
                           List<String> dependencies, List<String> dependents) {
            this.name = name;
            this.running = running;
            this.metrics = metrics;
            this.dependencies = dependencies;
            this.dependents = dependents;
        }

        public String getName() { return name; }
        public boolean isRunning() { return running; }
        public ServiceInstance.ServiceMetrics getMetrics() { return metrics; }
        public List<String> getDependencies() { return dependencies; }
        public List<String> getDependents() { return dependents; }
    }

    public static final class ServiceOptimizerReport {
        private final boolean enabled;
        private final List<ServiceStatus> services;
        private final List<RedundancyReport> redundancies;
        private final int totalServices;
        private final int activeServices;
        private final int healthyServices;
        private final double avgCpuUsage;
        private final long avgMemoryUsage;
        private final long avgLatency;

        public ServiceOptimizerReport(boolean enabled, List<ServiceStatus> services,
                                     List<RedundancyReport> redundancies,
                                     int totalServices, int activeServices,
                                     int healthyServices, double avgCpuUsage,
                                     long avgMemoryUsage, long avgLatency) {
            this.enabled = enabled;
            this.services = services;
            this.redundancies = redundancies;
            this.totalServices = totalServices;
            this.activeServices = activeServices;
            this.healthyServices = healthyServices;
            this.avgCpuUsage = avgCpuUsage;
            this.avgMemoryUsage = avgMemoryUsage;
            this.avgLatency = avgLatency;
        }

        public boolean isEnabled() { return enabled; }
        public List<ServiceStatus> getServices() { return services; }
        public List<RedundancyReport> getRedundancies() { return redundancies; }
        public int getTotalServices() { return totalServices; }
        public int getActiveServices() { return activeServices; }
        public int getHealthyServices() { return healthyServices; }
        public double getAvgCpuUsage() { return avgCpuUsage; }
        public long getAvgMemoryUsage() { return avgMemoryUsage; }
        public long getAvgLatency() { return avgLatency; }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("=== Service Optimizer Report ===\n");
            sb.append("Enabled: ").append(enabled).append("\n");
            sb.append("Total Services: ").append(totalServices).append("\n");
            sb.append("Active Services: ").append(activeServices).append("\n");
            sb.append("Healthy Services: ").append(healthyServices).append("\n");
            sb.append("Avg CPU Usage: ").append(String.format("%.2f%%", avgCpuUsage)).append("\n");
            sb.append("Avg Memory Usage: ").append(avgMemoryUsage / 1024 / 1024).append("MB\n");
            sb.append("Avg Latency: ").append(avgLatency).append("ms\n");
            sb.append("Redundancy Issues: ").append(redundancies.size()).append("\n");
            redundancies.forEach(r ->
                sb.append("  - ").append(r.getServiceName()).append(" similar to ")
                  .append(r.getSimilarServices()).append("\n"));
            return sb.toString();
        }
    }

    private static final class ServiceInstance {
        private final String name;
        private final Runnable initializer;
        private final Runnable healthCheck;
        private final Consumer<Boolean> stateChange;
        private final AtomicBoolean running = new AtomicBoolean(false);
        private final ExecutorService executor;

        private volatile double cpuUsage;
        private volatile long memoryUsage;
        private volatile long averageLatency;

        public ServiceInstance(String name, Runnable initializer, Runnable healthCheck,
                             Consumer<Boolean> stateChange) {
            this.name = name;
            this.initializer = initializer;
            this.healthCheck = healthCheck;
            this.stateChange = stateChange;
            this.executor = Executors.newSingleThreadExecutor(r -> {
                Thread t = new Thread(r, "Service-" + name);
                t.setDaemon(true);
                return t;
            });
        }

        public String getName() { return name; }
        public boolean isRunning() { return running.get(); }

        public void start() {
            if (running.compareAndSet(false, true)) {
                initializer.run();
                stateChange.accept(true);
                LOGGER.info("Service started: " + name);
            }
        }

        public void stop() {
            if (running.compareAndSet(true, false)) {
                executor.shutdown();
                try {
                    if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                        executor.shutdownNow();
                    }
                } catch (InterruptedException e) {
                    executor.shutdownNow();
                    Thread.currentThread().interrupt();
                }
                stateChange.accept(false);
                LOGGER.info("Service stopped: " + name);
            }
        }

        public void submitTask(Runnable task) {
            if (running.get()) {
                executor.submit(task);
            } else {
                task.run();
            }
        }

        public void runHealthCheck() {
            if (running.get() && healthCheck != null) {
                healthCheck.run();
            }
        }

        public void updateMetrics(double cpu, long memory, long latency) {
            this.cpuUsage = cpu;
            this.memoryUsage = memory;
            this.averageLatency = latency;
        }

        public long getMemoryUsage() {
            return memoryUsage;
        }

        public ServiceMetrics getMetrics() {
            return new ServiceMetrics(cpuUsage, memoryUsage, averageLatency);
        }

        public static final class ServiceMetrics {
            private final double cpuUsage;
            private final long memoryUsage;
            private final long averageLatency;

            public ServiceMetrics(double cpuUsage, long memoryUsage, long averageLatency) {
                this.cpuUsage = cpuUsage;
                this.memoryUsage = memoryUsage;
                this.averageLatency = averageLatency;
            }

            public double getCpuUsage() { return cpuUsage; }
            public long getMemoryUsage() { return memoryUsage; }
            public long getAverageLatency() { return averageLatency; }
        }
    }
}
