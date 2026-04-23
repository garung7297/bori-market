package com.garung.news;

import java.time.LocalDateTime;

public class NewsComment {
    private Long id;
    private Long newsId;
    private Long parentId;
    private String writer;
    private String content;
    private LocalDateTime createdAt;

    // 기본 생성자 (필수!)
    public NewsComment() {}

    // 가령이가 만든 생성자
    public NewsComment(Long id, Long newsId, Long parentId, String writer, String content, LocalDateTime createdAt) {
        this.id = id;
        this.newsId = newsId;
        this.parentId = parentId;
        this.writer = writer;
        this.content = content;
        this.createdAt = createdAt;
    }

    // Getter & Setter (주석 처리했던 parentId 세터도 살려주자!)
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getNewsId() { return newsId; }
    public void setNewsId(Long newsId) { this.newsId = newsId; }

    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }

    public String getWriter() { return writer; }
    public void setWriter(String writer) { this.writer = writer; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
