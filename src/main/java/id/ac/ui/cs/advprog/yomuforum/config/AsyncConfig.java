package id.ac.ui.cs.advprog.yomuforum.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Konfigurasi Asynchronous Processing & Multithreading.
 *
 * Mengaktifkan @Async dan @Scheduled di seluruh aplikasi.
 * Thread pool dikonfigurasi dengan ukuran kecil agar aman
 * di environment dengan memori terbatas (Koyeb eNano 256MB).
 */
@Configuration
@EnableAsync
@EnableScheduling
public class AsyncConfig {

    /**
     * Custom thread pool executor untuk operasi @Async.
     * Core pool: 2 threads (selalu siap)
     * Max pool: 4 threads (naik saat load tinggi)
     * Queue capacity: 50 (antrian tugas sebelum thread baru dibuat)
     */
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("yomu-async-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        return executor;
    }
}
