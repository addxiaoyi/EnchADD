package com.enadd.core.optimize;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.ThreadMXBean;
import java.net.NetworkInterface;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


public final class ServerAnalyzer {

    private static final Runtime RUNTIME = Runtime.getRuntime();
    private static final MemoryMXBean MEMORY_MX_BEAN = ManagementFactory.getMemoryMXBean();
    private static final ThreadMXBean THREAD_MX_BEAN = ManagementFactory.getThreadMXBean();

    private static double baselineCpuUsage = 0;
    private static double baselineMemoryUsage = 0;

    private static final ConcurrentHashMap<String, MetricSnapshot> metricHistory = new ConcurrentHashMap<>();
    private static long analysisStartTime = 0;

    // Fields for singleton and analysis state
    private static volatile ServerAnalyzer instance;
    private volatile boolean isAnalyzing = false;
    private volatile Thread analysisThread = null;

    public enum ServerType {
        DEDICATED("Dedicated Server"),
        BUKKIT("Bukkit/CraftBukkit"),
        PAPER("Paper/PaperMC"),
        SPIGOT("Spigot"),
        FORGE("Minecraft Forge"),
        FABRIC("Fabric"),
        HYBRID("Hybrid Server"),
        UNKNOWN("Unknown");

        private final String displayName;

        ServerType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum ServerRole {
        STANDALONE("Standalone"),
        PROXY("Proxy (BungeeCord/Velocity)"),
        NODE("Node/Worker"),
        MASTER("Master/Controller"),
        DISTRIBUTED("Distributed"),
        UNKNOWN("Unknown");

        private final String displayName;

        ServerRole(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public static class ServerProfile {
        public final ServerType type;
        public final ServerRole role;
        public final HardwareInfo hardware;
        public final NetworkInfo network;
        public final int playerCapacity;
        public final boolean isVirtualized;

        public ServerProfile(ServerType type, ServerRole role, HardwareInfo hardware,
                          NetworkInfo network, int playerCapacity, boolean isVirtualized) {
            this.type = type;
            this.role = role;
            this.hardware = hardware;
            this.network = network;
            this.playerCapacity = playerCapacity;
            this.isVirtualized = isVirtualized;
        }
    }

    public static class HardwareInfo {
        public final int availableProcessors;
        public final long maxMemory;
        public final long heapMemory;
        public final long nonHeapMemory;
        public final String processorName;
        public final double cpuCores;
        public final boolean supportsThreadMXBean;
        public final double cpuUsagePercent;
        public final long totalMemoryMB;

        public HardwareInfo(int availableProcessors, long maxMemory, long heapMemory,
                          long nonHeapMemory, String processorName, double cpuCores,
                          boolean supportsThreadMXBean, double cpuUsagePercent, long totalMemoryMB) {
            this.availableProcessors = availableProcessors;
            this.maxMemory = maxMemory;
            this.heapMemory = heapMemory;
            this.nonHeapMemory = nonHeapMemory;
            this.processorName = processorName;
            this.cpuCores = cpuCores;
            this.supportsThreadMXBean = supportsThreadMXBean;
            this.cpuUsagePercent = cpuUsagePercent;
            this.totalMemoryMB = totalMemoryMB;
        }
    }

    public static class NetworkInfo {
        public final List<String> addresses;
        public final boolean isLoopbackOnly;
        public final String macAddress;
        public final int networkInterfaces;
        public final long estimatedBandwidth;

        public NetworkInfo(List<String> addresses, boolean isLoopbackOnly,
                          String macAddress, int networkInterfaces, long estimatedBandwidth) {
            this.addresses = addresses;
            this.isLoopbackOnly = isLoopbackOnly;
            this.macAddress = macAddress;
            this.networkInterfaces = networkInterfaces;
            this.estimatedBandwidth = estimatedBandwidth;
        }
    }

    public static class MetricSnapshot {
        public final long timestamp;
        public final double cpuUsage;
        public final long memoryUsed;
        public final long memoryFree;
        public final int activeThreads;
        public final double loadAverage;
        public final long heapUsed;
        public final long nonHeapUsed;
        public final long gcCount;
        public final double gcTime;

        public MetricSnapshot(long timestamp, double cpuUsage, long memoryUsed,
                            long memoryFree, int activeThreads, double loadAverage,
                            long heapUsed, long nonHeapUsed, long gcCount, double gcTime) {
            this.timestamp = timestamp;
            this.cpuUsage = cpuUsage;
            this.memoryUsed = memoryUsed;
            this.memoryFree = memoryFree;
            this.activeThreads = activeThreads;
            this.loadAverage = loadAverage;
            this.heapUsed = heapUsed;
            this.nonHeapUsed = nonHeapUsed;
            this.gcCount = gcCount;
            this.gcTime = gcTime;
        }
    }

    private ServerAnalyzer() {}

    public static synchronized ServerProfile analyzeServer() {
        if (analysisStartTime == 0) {
            analysisStartTime = System.currentTimeMillis();
        }

        ServerType type = detectServerType();
        ServerRole role = detectServerRole(type);
        HardwareInfo hardware = analyzeHardware();
        NetworkInfo network = analyzeNetwork();
        int capacity = estimatePlayerCapacity(hardware);
        boolean virtualized = detectVirtualization();

        baselineCpuUsage = calculateCurrentCpuUsage();
        baselineMemoryUsage = calculateCurrentMemoryUsage();

        return new ServerProfile(type, role, hardware, network, capacity, virtualized);
    }

    private static ServerType detectServerType() {
        String serverBrand = System.getProperty("server.name", "");
        String mcVersion = System.getProperty("server.version", "");

        if (serverBrand.toLowerCase().contains("paper") ||
            mcVersion.contains("Paper") ||
            existsClass("io.papermc.paper")) {
            return ServerType.PAPER;
        }

        if (serverBrand.toLowerCase().contains("spigot") ||
            existsClass("org.spigotmc")) {
            return ServerType.SPIGOT;
        }

        if (serverBrand.toLowerCase().contains("bukkit") ||
            existsClass("org.bukkit.Bukkit")) {
            return ServerType.BUKKIT;
        }

        if (serverBrand.toLowerCase().contains("forge") ||
            existsClass("net.minecraftforge")) {
            return ServerType.FORGE;
        }

        if (serverBrand.toLowerCase().contains("fabric") ||
            existsClass("net.fabricmc")) {
            return ServerType.FABRIC;
        }

        return ServerType.DEDICATED;
    }

    private static ServerRole detectServerRole(ServerType type) {
        if (type == ServerType.PAPER || type == ServerType.SPIGOT ||
            type == ServerType.BUKKIT || type == ServerType.DEDICATED) {
            if (isLikelyProxy()) {
                return ServerRole.PROXY;
            }
            return ServerRole.STANDALONE;
        }

        if (type == ServerType.FORGE || type == ServerType.FABRIC) {
            return ServerRole.STANDALONE;
        }

        return ServerRole.UNKNOWN;
    }

    private static boolean isLikelyProxy() {
        int port = -1;
        try {
            port = Integer.parseInt(System.getProperty("server.port", "25565"));
        } catch (NumberFormatException e) {
        }

        if (port == 25577 || port == 25565) {
            return false;
        }

        String serverName = System.getProperty("server.name", "").toLowerCase();
        return serverName.contains("proxy") || serverName.contains("bungee") ||
               serverName.contains("velocity");
    }

    private static HardwareInfo analyzeHardware() {
        int processors = RUNTIME.availableProcessors();
        long maxMemory = RUNTIME.maxMemory();

        MemoryUsage heapUsage = MEMORY_MX_BEAN.getHeapMemoryUsage();
        long heapMemory = heapUsage.getUsed();
        long nonHeapMemory = MEMORY_MX_BEAN.getNonHeapMemoryUsage().getUsed();

        String processorName = ManagementFactory.getRuntimeMXBean().getVmName();
        double cpuCores = processors;
        boolean supportsThreadMXBean = THREAD_MX_BEAN.isThreadCpuTimeSupported();
        double cpuUsagePercent = calculateCurrentCpuUsage();
        long totalMemoryMB = RUNTIME.maxMemory() / (1024 * 1024);

        return new HardwareInfo(processors, maxMemory, heapMemory,
                               nonHeapMemory, processorName, cpuCores,
                               supportsThreadMXBean, cpuUsagePercent, totalMemoryMB);
    }

    private static NetworkInfo analyzeNetwork() {
        List<String> addresses = new ArrayList<>();
        boolean isLoopbackOnly = true;
        String primaryMac = null;
        int interfaceCount = 0;

        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            interfaceCount = 0;

            while (interfaces.hasMoreElements()) {
                NetworkInterface ni = interfaces.nextElement();
                if (ni.isUp() && !ni.isLoopback()) {
                    isLoopbackOnly = false;
                    interfaceCount++;

                    var ips = ni.getInetAddresses();
                    while (ips.hasMoreElements()) {
                        addresses.add(ips.nextElement().getHostAddress());
                    }

                    if (primaryMac == null) {
                        byte[] mac = ni.getHardwareAddress();
                        if (mac != null) {
                            StringBuilder sb = new StringBuilder();
                            for (int i = 0; i < mac.length; i++) {
                                sb.append(String.format("%02X%s", mac[i],
                                    (i < mac.length - 1) ? "-" : ""));
                            }
                            primaryMac = sb.toString();
                        }
                    }
                }
            }
        } catch (Exception e) {
        }

        long estimatedBandwidth = isLoopbackOnly ? 1000000000L : 100000000L;

        return new NetworkInfo(addresses, isLoopbackOnly, primaryMac,
                             interfaceCount, estimatedBandwidth);
    }

    private static int estimatePlayerCapacity(HardwareInfo hardware) {
        int processors = hardware.availableProcessors;
        long maxMemory = hardware.maxMemory;
        double cpuFactor = processors * 20;
        double memoryFactor = maxMemory / (1024 * 1024 * 512);

        int capacity = (int) Math.min(cpuFactor, memoryFactor);

        if (processors >= 8 && maxMemory >= 1024 * 1024 * 1024) {
            capacity = Math.max(capacity, 100);
        } else if (processors >= 4 && maxMemory >= 512 * 1024 * 1024) {
            capacity = Math.max(capacity, 50);
        } else {
            capacity = Math.max(capacity, 20);
        }

        return Math.min(capacity, 500);
    }

    private static boolean detectVirtualization() {
        String vmVendor = System.getProperty("java.vm.vendor", "");
        String vmName = System.getProperty("java.vm.name", "");
        String osName = System.getProperty("os.name", "");

        vmVendor = vmVendor.toLowerCase();
        vmName = vmName.toLowerCase();
        osName = osName.toLowerCase();

        if (vmVendor.contains("amazon") || vmVendor.contains("alibaba") ||
            vmVendor.contains("huawei") || vmVendor.contains("microsoft") ||
            vmVendor.contains("google") || vmVendor.contains("digitalocean") ||
            vmVendor.contains("oracle")) {
            return true;
        }

        if (vmName.contains("kvm") || vmName.contains("xen") ||
            vmName.contains("vmware") || vmName.contains("virtualbox") ||
            vmName.contains("hyper-v") || vmName.contains("hvf")) {
            return true;
        }

        if (osName.contains("ec2") || osName.contains("container")) {
            return true;
        }

        return false;
    }

    public static MetricSnapshot captureMetrics() {
        MemoryUsage heapUsage = MEMORY_MX_BEAN.getHeapMemoryUsage();

        long gcCount = 0;
        double gcTime = 0;
        try {
            var gcBeans = ManagementFactory.getGarbageCollectorMXBeans();
            for (var gcBean : gcBeans) {
                gcCount += gcBean.getCollectionCount();
                gcTime += gcBean.getCollectionTime();
            }
        } catch (Exception e) {
        }

        int threads = THREAD_MX_BEAN.getThreadCount();

        double cpuUsage = calculateCurrentCpuUsage();
        long usedMemory = RUNTIME.totalMemory() - RUNTIME.freeMemory();
        long freeMemory = RUNTIME.maxMemory() - usedMemory;

        double loadAverage = ManagementFactory.getOperatingSystemMXBean().getSystemLoadAverage();

        return new MetricSnapshot(
            System.currentTimeMillis(),
            cpuUsage,
            usedMemory,
            freeMemory,
            threads,
            loadAverage,
            heapUsage.getUsed(),
            MEMORY_MX_BEAN.getNonHeapMemoryUsage().getUsed(),
            gcCount,
            gcTime
        );
    }

    public static void recordMetric(String key, MetricSnapshot snapshot) {
        metricHistory.put(key, snapshot);
    }

    public static boolean isEnabled() {
        return true;
    }

    private static double calculateCurrentCpuUsage() {
        try {
            var osBean = ManagementFactory.getOperatingSystemMXBean();
            if (osBean instanceof com.sun.management.OperatingSystemMXBean) {
                com.sun.management.OperatingSystemMXBean sunOsBean =
                    (com.sun.management.OperatingSystemMXBean) osBean;
                return sunOsBean.getCpuLoad() * 100;
            }
        } catch (Exception e) {
        }

        return -1;
    }

    private static double calculateCurrentMemoryUsage() {
        long total = RUNTIME.maxMemory();
        long used = RUNTIME.totalMemory() - RUNTIME.freeMemory();
        return (used * 100.0) / total;
    }

    private static boolean existsClass(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static String generateAnalysisReport() {
        ServerProfile profile = analyzeServer();
        MetricSnapshot current = captureMetrics();

        StringBuilder sb = new StringBuilder();
        sb.append("=== Server Analysis Report ===\n\n");

        sb.append("Server Information:\n");
        sb.append("  Type: ").append(profile.type.getDisplayName()).append("\n");
        sb.append("  Role: ").append(profile.role.getDisplayName()).append("\n");
        sb.append("  Virtualized: ").append(profile.isVirtualized ? "Yes" : "No").append("\n");
        sb.append("  Player Capacity: ").append(profile.playerCapacity).append("\n\n");

        sb.append("Hardware Information:\n");
        sb.append("  Processors: ").append(profile.hardware.availableProcessors).append("\n");
        sb.append("  Max Memory: ").append(formatSize(profile.hardware.maxMemory)).append("\n");
        sb.append("  Processor: ").append(profile.hardware.processorName).append("\n\n");

        sb.append("Current Metrics:\n");
        sb.append("  CPU Usage: ").append(String.format("%.2f%%", current.cpuUsage)).append("\n");
        sb.append("  Memory Used: ").append(formatSize(current.memoryUsed)).append("\n");
        sb.append("  Memory Free: ").append(formatSize(current.memoryFree)).append("\n");
        sb.append("  Heap Used: ").append(formatSize(current.heapUsed)).append("\n");
        sb.append("  Non-Heap Used: ").append(formatSize(current.nonHeapUsed)).append("\n");
        sb.append("  Active Threads: ").append(current.activeThreads).append("\n");
        sb.append("  GC Count: ").append(current.gcCount).append("\n");
        sb.append("  GC Time: ").append(String.format("%.2fms", current.gcTime)).append("\n\n");

        sb.append("Baseline:\n");
        sb.append("  CPU Baseline: ").append(String.format("%.2f%%", baselineCpuUsage)).append("\n");
        sb.append("  Memory Baseline: ").append(String.format("%.2f%%", baselineMemoryUsage)).append("\n");

        return sb.toString();
    }

    private static String formatSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.2f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.2f MB", bytes / (1024.0 * 1024));
        return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
    }

    public void startAnalysis() {
        if (!isAnalyzing) {
            isAnalyzing = true;
            analysisThread = new Thread(() -> {
                while (isAnalyzing) {
                    try {
                        MetricSnapshot snapshot = captureMetrics();
                        recordMetric("current_" + System.currentTimeMillis(), snapshot);
                        Thread.sleep(5000); // Capture metrics every 5 seconds
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    } catch (Exception e) {
                        // Log error but continue analysis
                    }
                }
            }, "ServerAnalyzer-Thread");
            analysisThread.setDaemon(true);
            analysisThread.start();
        }
    }

    public static ServerAnalyzer getInstance() {
        ServerAnalyzer instance = ServerAnalyzer.instance;
        if (instance == null) {
            synchronized (ServerAnalyzer.class) {
                instance = ServerAnalyzer.instance;
                if (instance == null) {
                    ServerAnalyzer.instance = instance = new ServerAnalyzer();
                }
            }
        }
        return instance;
    }

    public void stopAnalysis() {
        isAnalyzing = false;
        if (analysisThread != null) {
            analysisThread.interrupt();
            analysisThread = null;
        }
    }
}
