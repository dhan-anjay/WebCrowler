package com.dhananjay.webcrowler.repository;

import com.dhananjay.webcrowler.model.CrawlResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;

public interface CrawlResultRepository extends JpaRepository<CrawlResult, Long> {
    void deleteByCreatedAtBefore(Instant fourDaysAgo);
}
