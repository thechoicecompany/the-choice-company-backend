//package com.thechoicecompany.service;
//
//import com.thechoicecompany.entity.BlogPost;
//import com.thechoicecompany.exception.ResourceNotFoundException;
//import com.thechoicecompany.repository.BlogRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Sort;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.List;
//
//@Service
//@RequiredArgsConstructor
//public class BlogService {
//
//    private final BlogRepository blogRepository;
//
//    @Transactional(readOnly = true)
//    public Page<BlogPost> listPosts(String category, int page, int size) {
//        PageRequest pageable = PageRequest.of(page, size, Sort.by("publishedAt").descending());
//        return category != null && !category.isBlank()
//            ? blogRepository.findByCategoryAndIsPublishedTrue(category, pageable)
//            : blogRepository.findByIsPublishedTrue(pageable);
//    }
//
//    @Transactional(readOnly = true)
//    public BlogPost getPostBySlug(String slug) {
//        return blogRepository.findBySlugAndIsPublishedTrue(slug)
//            .orElseThrow(() -> new ResourceNotFoundException("Blog post", "slug", slug));
//    }
//
//    @Transactional(readOnly = true)
//    public List<String> getAllSlugs() { return blogRepository.findAllPublishedSlugs(); }
//
//    @Transactional(readOnly = true)
//    public List<String> getCategories() { return blogRepository.findDistinctCategories(); }
//}
package com.thechoicecompany.service;

import com.thechoicecompany.dto.request.BlogRequest;
import com.thechoicecompany.entity.BlogPost;
import com.thechoicecompany.exception.ResourceNotFoundException;
import com.thechoicecompany.repository.BlogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BlogService {

    private final BlogRepository blogRepository;
    private final UploadService uploadService; // ← matches your existing service

    // ── Public ───────────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public Page<BlogPost> listPosts(String category, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("publishedAt").descending());
        return category != null && !category.isBlank()
            ? blogRepository.findByCategoryAndIsPublishedTrue(category, pageable)
            : blogRepository.findByIsPublishedTrue(pageable);
    }

    @Transactional(readOnly = true)
    public BlogPost getPostBySlug(String slug) {
        return blogRepository.findBySlugAndIsPublishedTrue(slug)
            .orElseThrow(() -> new ResourceNotFoundException("Blog post", "slug", slug));
    }

    @Transactional(readOnly = true)
    public List<String> getAllSlugs() { return blogRepository.findAllPublishedSlugs(); }

    @Transactional(readOnly = true)
    public List<String> getCategories() { return blogRepository.findDistinctCategories(); }

    // ── Admin ─────────────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public Page<BlogPost> listAllPostsAdmin(String status, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        if ("published".equalsIgnoreCase(status)) return blogRepository.findByIsPublishedTrue(pageable);
        if ("draft".equalsIgnoreCase(status))     return blogRepository.findByIsPublishedFalse(pageable);
        return blogRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public BlogPost getPostById(Long id) {
        return blogRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Blog post", "id", id));
    }

    @Transactional
    public BlogPost createPost(BlogRequest request) {
        String slug = resolveSlug(request.getSlug(), request.getTitle(), null);
        BlogPost post = BlogPost.builder()
            .title(request.getTitle())
            .slug(slug)
            .excerpt(request.getExcerpt())
            .content(request.getContent())
            .featuredImage(request.getFeaturedImage())
            .featuredImagePublicId(request.getFeaturedImagePublicId())
            .category(request.getCategory())
            .tags(request.getTags() != null ? request.getTags() : new ArrayList<>())
            .author(request.getAuthor())
            .readTime(request.getReadTime() != null ? request.getReadTime() : estimateReadTime(request.getContent()))
            .metaTitle(request.getMetaTitle())
            .metaDescription(request.getMetaDescription())
            .isPublished(false)
            .build();
        return blogRepository.save(post);
    }

    @Transactional
    public BlogPost updatePost(Long id, BlogRequest request) {
        BlogPost post = getPostById(id);
        post.setTitle(request.getTitle());
        post.setSlug(resolveSlug(request.getSlug(), request.getTitle(), id));
        post.setExcerpt(request.getExcerpt());
        post.setContent(request.getContent());
        if (request.getFeaturedImage() != null) {
            post.setFeaturedImage(request.getFeaturedImage());
            post.setFeaturedImagePublicId(request.getFeaturedImagePublicId());
        }
        post.setCategory(request.getCategory());
        if (request.getTags() != null) post.setTags(request.getTags());
        post.setAuthor(request.getAuthor());
        if (request.getReadTime() != null) post.setReadTime(request.getReadTime());
        post.setMetaTitle(request.getMetaTitle());
        post.setMetaDescription(request.getMetaDescription());
        return post;
    }

    @Transactional
    public void deletePost(Long id) {
        BlogPost post = getPostById(id);
        try {
            if (post.getFeaturedImagePublicId() != null) {
                uploadService.deleteImage(post.getFeaturedImagePublicId()); // ← matches UploadService
            }
        } catch (Exception ignored) {}
        blogRepository.delete(post);
    }

    @Transactional
    public BlogPost publishPost(Long id) {
        BlogPost post = getPostById(id);
        post.setIsPublished(true);
        if (post.getPublishedAt() == null) post.setPublishedAt(LocalDateTime.now());
        return post;
    }

    @Transactional
    public BlogPost unpublishPost(Long id) {
        BlogPost post = getPostById(id);
        post.setIsPublished(false);
        return post;
    }

    // ── Cloudinary uploads ───────────────────────────────────────────────
    public Map<String, String> uploadFeaturedImage(MultipartFile file) {
        List<Map<String, String>> results = uploadService.uploadImages(List.of(file), "tcc/blogs/featured");
        return results.get(0);
    }

    public Map<String, String> uploadContentImage(MultipartFile file) {
        List<Map<String, String>> results = uploadService.uploadImages(List.of(file), "tcc/blogs/content");
        return results.get(0);
    }

    // ── Helpers ──────────────────────────────────────────────────────────
    private String resolveSlug(String requestedSlug, String title, Long excludeId) {
        String base = slugify(requestedSlug != null && !requestedSlug.isBlank() ? requestedSlug : title);
        String candidate = base;
        int suffix = 1;
        while (excludeId == null ? blogRepository.existsBySlug(candidate)
                                 : blogRepository.existsBySlugAndIdNot(candidate, excludeId)) {
            candidate = base + "-" + (++suffix);
        }
        return candidate;
    }

    private String slugify(String input) {
        return input.toLowerCase().trim()
            .replaceAll("[^a-z0-9\\s-]", "")
            .replaceAll("\\s+", "-")
            .replaceAll("-+", "-");
    }

    private Integer estimateReadTime(String html) {
        String text = html.replaceAll("<[^>]*>", " ").trim();
        int words = text.isEmpty() ? 0 : text.split("\\s+").length;
        return Math.max(1, (int) Math.ceil(words / 200.0));
    }

    // Same logic as ProductImageService.extractPublicId()
    private String extractPublicId(String url) {
        if (url == null) return "";
        String path = url.replaceAll(".*/upload/(v\\d+/)?", "");
        int dotIdx  = path.lastIndexOf('.');
        return dotIdx > 0 ? path.substring(0, dotIdx) : path;
    }
}