package com.dhananjay.webcrowler.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.List;

@Entity
@Getter
@Setter
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table
public class CrawlResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String seedUrl;

    @ElementCollection
    @CollectionTable(name = "crawled_urls", joinColumns = @JoinColumn(name = "crawl_result_id"))
    @Column(name = "url")
    private List<String> crawledUrls;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(updatable = false, nullable = false)
    private Instant createdAt = Instant.now();

    public enum Status {
        IN_PROGRESS, COMPLETED, FAILED
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSeedUrl() {
        return seedUrl;
    }

    public void setSeedUrl(String seedUrl) {
        this.seedUrl = seedUrl;
    }

    public List<String> getCrawledUrls() {
        return crawledUrls;
    }

    public void setCrawledUrls(List<String> crawledUrls) {
        this.crawledUrls = crawledUrls;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}

