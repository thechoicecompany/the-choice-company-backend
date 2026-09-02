package com.thechoicecompany.controller; 

import com.thechoicecompany.dto.request.BlogRequest;
import com.thechoicecompany.dto.response.ApiResponse;
import com.thechoicecompany.entity.BlogPost;
import com.thechoicecompany.service.BlogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/blog")
@RequiredArgsConstructor
@Tag(name = "Admin Blog", description = "Blog management — SUPER_ADMIN, CONTENT_MANAGER")
@PreAuthorize("hasAnyRole('SUPER_ADMIN','CONTENT_MANAGER')")
public class AdminBlogController {

    private final BlogService blogService;

    @GetMapping
    @Operation(summary = "List all blog posts (any status)")
    public ResponseEntity<ApiResponse<Page<BlogPost>>> list(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(blogService.listAllPostsAdmin(status, page, size)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BlogPost>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(blogService.getPostById(id)));
    }

    @PostMapping
    @Operation(summary = "Create blog post (draft)")
    public ResponseEntity<ApiResponse<BlogPost>> create(@Valid @RequestBody BlogRequest request) {
        return ResponseEntity.ok(ApiResponse.success(blogService.createPost(request), "Blog post created"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BlogPost>> update(@PathVariable Long id, @Valid @RequestBody BlogRequest request) {
        return ResponseEntity.ok(ApiResponse.success(blogService.updatePost(id, request), "Blog post updated"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        blogService.deletePost(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Blog post deleted"));
    }

    @PatchMapping("/{id}/publish")
    public ResponseEntity<ApiResponse<BlogPost>> publish(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(blogService.publishPost(id), "Blog post published"));
    }

    @PatchMapping("/{id}/unpublish")
    public ResponseEntity<ApiResponse<BlogPost>> unpublish(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(blogService.unpublishPost(id), "Moved to draft"));
    }

    @PostMapping(value = "/upload/featured", consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadFeatured(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(ApiResponse.success(blogService.uploadFeaturedImage(file)));
    }

    @PostMapping(value = "/upload/content", consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadContent(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(ApiResponse.success(blogService.uploadContentImage(file)));
    }
}