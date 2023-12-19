import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class Main {
    private static final String HOST = "old-orel-city.narod.ru";
    private static final int THREADS = 120;
    private static final int TIMEOUT = 100;
    private static final int MIN_PORT_NUMBER = 0;
    private static final int MAX_PORT_NUMBER = 65535;

    public static void main(String[] args) {
        scan();
    }

    private static void scan() {
        printTitle();
        ExecutorService executor = Executors.newFixedThreadPool(THREADS);
        List<Integer> listPorts = new ArrayList<>();

        for (int i = MIN_PORT_NUMBER; i <= MAX_PORT_NUMBER; i++) {
            final int port = i;
            executor.execute(() -> {
                progress(port);
                var inetSocketAddress = new InetSocketAddress(HOST, port);
                try (var socket = new Socket()) {
                    socket.connect(inetSocketAddress, TIMEOUT);
                    listPorts.add(port);
                } catch (IOException ignored) {
                }
            });
        }
        executor.shutdown();
        try {
            if (executor.awaitTermination(10, TimeUnit.MINUTES)) {
                System.out.println("Program completed");
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        printBottom(listPorts);
    }

    private static void printBottom(List<Integer> listPorts) {
        System.out.print("\nList of open ports: ");
        System.out.println(listPorts.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(", ")));
    }

    private static void printTitle() {
        System.out.println();
        System.out.println("<<==========================>>");
        System.out.println("| Port scanner               |");
        System.out.println("<============================>");
        System.out.printf("Host: %s \n", HOST);
    }

    private static void progress(int currentPos) {
        var pos = ((currentPos - MIN_PORT_NUMBER) * 100 / (MAX_PORT_NUMBER - MIN_PORT_NUMBER));
        System.out.printf("Scanning ports (%d-%d): %d %s  \r", MIN_PORT_NUMBER, MAX_PORT_NUMBER, pos, "%");
    }
}