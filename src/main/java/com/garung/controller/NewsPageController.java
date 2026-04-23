package com.garung.controller;

import com.garung.news.NewsComment;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/news")
public class NewsPageController {

    private final JdbcTemplate jdbcTemplate;

    @Value("${file.upload-root}")
    private String uploadRoot;

    public NewsPageController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/new")
    public String newsForm(Model model) {
        model.addAttribute("content", "news/news-form");
        return "layout/layout-news";
    }

    @PostMapping
    public String save(
            @RequestParam String title,
            @RequestParam String content,
            @RequestParam String category,
            @RequestParam String source,
            @RequestParam(required = false) MultipartFile[] images
    ) throws Exception {
        // 1️⃣ 뉴스 저장 (PostgreSQL 문법으로 수정: 시퀀스 대신 SERIAL 활용)
        // 만약 DB에서 SERIAL을 썼다면 id는 빼고 insert 해야해!
        jdbcTemplate.update("""
            INSERT INTO news (title, content, category, source, created_at, view_count)
            VALUES (?, ?, ?, ?, NOW(), 0)
        """, title, content, category, source);

        // 2️⃣ 방금 넣은 ID 가져오기 (PostgreSQL 방식)
        Long newsId = jdbcTemplate.queryForObject("SELECT lastval()", Long.class);

        Path newsDir = Paths.get(uploadRoot, "news");
        if (!Files.exists(newsDir)) Files.createDirectories(newsDir);

        if (images != null) {
            for (MultipartFile file : images) {
                if (!file.isEmpty()) {
                    String originalName = file.getOriginalFilename();
                    String ext = originalName.contains(".") ? originalName.substring(originalName.lastIndexOf(".")) : "";
                    String savedName = System.currentTimeMillis() + ext;
                    Path savePath = newsDir.resolve(savedName);

                    Files.copy(file.getInputStream(), savePath);

                    // 이미지 저장 (SERIAL id 자동생성 가정)
                    jdbcTemplate.update("""
                        INSERT INTO news_image (news_id, file_name, original_name)
                        VALUES (?, ?, ?)
                    """, newsId, savedName, originalName);
                }
            }
        }
        return "redirect:/news/view/" + newsId;
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id) throws Exception {
        // 이미지 조회 시 대문자 키 문제 해결을 위해 별칭 사용
        List<Map<String, Object>> images = jdbcTemplate.queryForList(
                "SELECT file_name as \"file_name\" FROM news_image WHERE news_id = ?", id);

        Path newsDir = Paths.get(uploadRoot, "news");
        for (Map<String, Object> img : images) {
            // PostgreSQL 결과를 꺼낼 땐 소문자 키가 안전해!
            String fileName = (String) img.get("file_name");
            if (fileName != null) Files.deleteIfExists(newsDir.resolve(fileName));
        }

        jdbcTemplate.update("DELETE FROM news_image WHERE news_id = ?", id);
        jdbcTemplate.update("DELETE FROM news_comment WHERE news_id = ?", id);
        jdbcTemplate.update("DELETE FROM news WHERE id = ?", id);
        return "redirect:/news/list";
    }

    @PostMapping("/update/{id}")
    public String updateNews(
            @PathVariable Long id,
            @RequestParam String title,
            @RequestParam String content,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String source,
            @RequestParam(required = false) List<Long> deleteImageIds,
            @RequestParam(required = false) MultipartFile[] images
    ) throws Exception {
        jdbcTemplate.update("""
            UPDATE news SET title = ?, content = ?, category = ?, source = ?
            WHERE id = ?""", title, content, category, source, id);

        if (deleteImageIds != null && !deleteImageIds.isEmpty()) {
            for (Long imgId : deleteImageIds) {
                jdbcTemplate.update("DELETE FROM news_image WHERE id = ?", imgId);
            }
        }

        if (images != null) {
            Path saveDir = Paths.get(uploadRoot, "news");
            if (!Files.exists(saveDir)) Files.createDirectories(saveDir);

            for (MultipartFile file : images) {
                if (!file.isEmpty()) {
                    String originalName = file.getOriginalFilename();
                    String ext = originalName.contains(".") ? originalName.substring(originalName.lastIndexOf(".")) : "";
                    String savedName = System.currentTimeMillis() + "_" + (int)(Math.random() * 1000) + ext;
                    Files.copy(file.getInputStream(), saveDir.resolve(savedName));

                    jdbcTemplate.update("""
                        INSERT INTO news_image (news_id, file_name, original_name)
                        VALUES (?, ?, ?)
                    """, id, savedName, originalName);
                }
            }
        }
        return "redirect:/news/view/" + id;
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        Map<String, Object> news = jdbcTemplate.queryForMap("SELECT * FROM news WHERE id = ?", id);
        List<Map<String, Object>> images = jdbcTemplate.queryForList("SELECT * FROM news_image WHERE news_id = ?", id);

        model.addAttribute("news", news);
        model.addAttribute("images", images);
        model.addAttribute("uploadUrl", "/" + uploadRoot + "/news/");
        model.addAttribute("content", "news/news-edit");
        return "layout/layout-news";
    }

    @GetMapping("/list")
    public String list(Model model) {
        // 🔥 가령아! 여기서 에러 났던 대문자 쌍따옴표를 싹 지웠어. 이제 소문자로 잘 찾아갈거야.
        String sql = """
                SELECT n.*,
                (
                    SELECT ni.file_name 
                    FROM news_image ni
                    WHERE ni.news_id = n.id
                    ORDER BY ni.id ASC
                    LIMIT 1
                ) AS thumbnail
                FROM news n
                ORDER BY n.created_at DESC
            """;
        List<Map<String, Object>> newsList = jdbcTemplate.queryForList(sql);
        model.addAttribute("newsList", newsList);
        model.addAttribute("uploadUrl", "/" + uploadRoot + "/news/");
        model.addAttribute("content", "news/news-list");
        return "layout/layout-news";
    }

    @GetMapping("/view/{id}")
    public String view(@PathVariable Long id, Model model) {
        jdbcTemplate.update("UPDATE news SET view_count = view_count + 1 WHERE id = ?", id);
        Map<String, Object> news = jdbcTemplate.queryForMap("SELECT * FROM news WHERE id = ?", id);
        List<Map<String, Object>> images = jdbcTemplate.queryForList("SELECT * FROM news_image WHERE news_id = ?", id);

        // 🔥 NVL 대신 PostgreSQL의 COALESCE 사용
        List<NewsComment> comments = jdbcTemplate.query(
                """
                  SELECT id, news_id as "newsId", parent_id as "parentId", 
                         writer, content, created_at as "createdAt"
                  FROM news_comment
                  WHERE news_id = ?
                  ORDER BY COALESCE(parent_id, id) ASC, created_at ASC
                """,
                new BeanPropertyRowMapper<>(NewsComment.class),
                id
        );

        Integer commentCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM news_comment WHERE news_id = ?", Integer.class, id);

        model.addAttribute("news", news);
        model.addAttribute("images", images);
        model.addAttribute("uploadUrl", "/" + uploadRoot + "/news/");
        model.addAttribute("comments", comments);
        model.addAttribute("commentCount", commentCount);
        // 오라클 스타일 대문자 키 호환을 위해 TITLE 확인
        model.addAttribute("title", news.get("title") != null ? news.get("title") : news.get("TITLE"));
        model.addAttribute("content", "news/news-view");

        return "layout/layout-news";
    }
}