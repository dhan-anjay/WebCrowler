package com.dhananjay.webcrowler.service;

import com.dhananjay.webcrowler.model.CrawlResult;
import com.dhananjay.webcrowler.repository.CrawlResultRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Getter
@Service
public class CrawlService {
    @Autowired
    private final CrawlResultRepository crawlResultRepository;
    @Autowired
    private final ThreadPoolTaskExecutor taskExecutor;

    public CrawlService(CrawlResultRepository crawlResultRepository, ThreadPoolTaskExecutor taskExecutor) {
        this.crawlResultRepository = crawlResultRepository;
        this.taskExecutor = taskExecutor;
    }

    @Transactional
    public CompletableFuture<CrawlResult> startCrawling(String seedUrl) {
        CrawlResult crawlResult = new CrawlResult();
        crawlResult.setSeedUrl(seedUrl);
        crawlResult.setStatus(CrawlResult.Status.IN_PROGRESS);
        crawlResult = crawlResultRepository.save(crawlResult);

        Long crawlId = crawlResult.getId();

        CrawlResult finalCrawlResult = crawlResult;
        CompletableFuture.runAsync(() -> {
            try {
                Set<String> crawledUrls = Collections.newSetFromMap(new ConcurrentHashMap<>());
                fetchUrls(seedUrl, crawledUrls);
                finalCrawlResult.setCrawledUrls(List.copyOf(crawledUrls));
                finalCrawlResult.setStatus(CrawlResult.Status.COMPLETED);
            } catch (Exception e) {
                finalCrawlResult.setStatus(CrawlResult.Status.FAILED);
            } finally {
                crawlResultRepository.save(finalCrawlResult);
            }
        }, taskExecutor);

        return CompletableFuture.completedFuture(crawlResult);
    }

    private void fetchUrls(String url, Set<String> crawledUrls) {
        CompletableFuture.runAsync(() -> {
            try {
                Document doc = Jsoup.connect(url).get();
                Elements links = doc.select("a[href]");
                for (Element link : links) {
                    String absUrl = link.absUrl("href");
                    if (!crawledUrls.contains(absUrl)) {
                        crawledUrls.add(absUrl);
                        fetchUrls(absUrl, crawledUrls);  // Recursive fetching
                    }
                }
            } catch (IOException ignored) {}
        }, taskExecutor);
    }

    public List<CrawlResult> getAllCrawls() {
        return crawlResultRepository.findAll();
    }
    @Scheduled(cron = "0 0 0 * * ?") // Runs daily at midnight
    @Transactional
    public void deleteOldCrawls() {
        Instant fourDaysAgo = Instant.now().minus(4, ChronoUnit.DAYS);
        crawlResultRepository.deleteByCreatedAtBefore(fourDaysAgo);
    }
}
