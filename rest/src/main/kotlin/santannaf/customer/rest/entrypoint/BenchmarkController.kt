package santannaf.customer.rest.entrypoint

import java.time.Duration
import java.time.Instant
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.RestClient

@RestController
@RequestMapping("/benchmark")
class BenchmarkController(
    private val restClient: RestClient = RestClient.builder().baseUrl("http://localhost:8080").build()
) {
    @GetMapping
    fun runBenchmark(): String {
        val requestCount = 250_000

        val result = StringBuilder()
        result.append("\n=== VIRTUAL THREADS PERFORMANCE BENCHMARK ===\n")
        result.append("JDK Version: ").append(System.getProperty("java.version")).append("\n")
        result.append("===========================================\n\n")

        result.append("--- STARTING BENCHMARK: ")
            .append(requestCount)
            .append(" processing requests ---\n")

        return try {
            var executor = Executors.newVirtualThreadPerTaskExecutor()
            val start = Instant.now()

            val futures: Array<CompletableFuture<*>> = Array(requestCount) { i ->
                CompletableFuture.runAsync({
                    try {
                        restClient.get()
                            .uri("/pessoas/920c25cb-2431-4f59-85a8-42ab8f6255d2")
                            .retrieve()
                            .toEntity(String::class.java)
                    } catch (_: Exception) {
                        // Ignora exceções
                    }
                }, executor)
            }

            CompletableFuture.allOf(*futures)

            // Record end time and calculate duration
            val end = Instant.now()
            val duration = Duration.between(start, end)

            // Calculate statistics
            val totalTimeSeconds = duration.toMillis() / 1000.0
            val requestsPerSecond = requestCount / totalTimeSeconds
            val avgTimePerRequest = (duration.toMillis() / requestCount.toDouble())

            // Format results for display
            result.append("\n╔════════════ BENCHMARK RESULTS ════════════╗\n")
            result.append("║ JDK Version:           ")
                .append(String.format("%-18s", System.getProperty("java.version"))).append(" ║\n")
            result.append("║ Total Requests:        ").append(String.format("%-18s", requestCount)).append(" ║\n")
            result.append("║ Total Time:            ")
                .append(String.format("%-18s", String.format("%.3f seconds", totalTimeSeconds))).append(" ║\n")
            result.append("║ Avg Time Per Request:  ")
                .append(String.format("%-18s", String.format("%.3f ms", avgTimePerRequest))).append(" ║\n")
            result.append("║ Requests Per Second:   ")
                .append(String.format("%-18s", String.format("%.1f", requestsPerSecond))).append(" ║\n")
            result.append("╚═══════════════════════════════════════════╝\n")

            result.toString()
        } catch (error: Exception) {
            result.append("Benchmark failed: ").append(error.message)
            error.printStackTrace()
            result.toString()
        }
    }
}
