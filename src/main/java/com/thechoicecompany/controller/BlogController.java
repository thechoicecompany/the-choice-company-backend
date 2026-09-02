package com.thechoicecompany.controller;

import com.thechoicecompany.dto.response.ApiResponse;
import com.thechoicecompany.entity.BlogPost;
import com.thechoicecompany.service.BlogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/blog")
@RequiredArgsConstructor
@Tag(name = "Blog", description = "Blog posts — public read access")
public class BlogController {

    private final BlogService blogService;

    @GetMapping
    @Operation(summary = "List published blog posts")
    public ResponseEntity<ApiResponse<Page<BlogPost>>> list(
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        return ResponseEntity.ok(ApiResponse.success(blogService.listPosts(category, page, size)));
    }

    @GetMapping("/slugs")
    @Operation(summary = "Get all published blog slugs")
    public ResponseEntity<ApiResponse<List<String>>> slugs() {
        return ResponseEntity.ok(ApiResponse.success(blogService.getAllSlugs()));
    }

    @GetMapping("/categories")
    @Operation(summary = "Get all blog categories")
    public ResponseEntity<ApiResponse<List<String>>> categories() {
        return ResponseEntity.ok(ApiResponse.success(blogService.getCategories()));
    }

    @GetMapping("/{slug}")
    @Operation(summary = "Get blog post by slug")
    public ResponseEntity<ApiResponse<BlogPost>> getBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(ApiResponse.success(blogService.getPostBySlug(slug)));
    }
}
