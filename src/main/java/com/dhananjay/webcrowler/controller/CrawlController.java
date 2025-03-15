package com.dhananjay.webcrowler.controller;

import com.dhananjay.webcrowler.model.CrawlResult;
import com.dhananjay.webcrowler.service.CrawlService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/crawl")
public class CrawlController {


    private final CrawlService crawlService;
    public CrawlController(CrawlService crawlService) {
        this.crawlService = crawlService;
    }

    @PostMapping("/start")
    public CompletableFuture<ResponseEntity<CrawlResult>> startCrawl(@RequestParam String url) {
        return crawlService.startCrawling(url)
                .thenApply(ResponseEntity::ok);
    }

    @GetMapping("/results")
    public ResponseEntity<List<CrawlResult>> getAllCrawls() {
        return ResponseEntity.ok(crawlService.getAllCrawls());
    }

    @GetMapping("/status/{id}")
    public ResponseEntity<CrawlResult> getCrawlStatus(@PathVariable Long id) {
        return crawlService.getAllCrawls().stream()
                .filter(crawl -> crawl.getId().equals(id))
                .findFirst()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}

