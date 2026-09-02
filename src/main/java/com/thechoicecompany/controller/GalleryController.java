package com.thechoicecompany.controller;

import com.thechoicecompany.dto.response.ApiResponse;
import com.thechoicecompany.entity.GalleryItem;
import com.thechoicecompany.service.GalleryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/gallery")
@RequiredArgsConstructor
@Tag(name = "Gallery", description = "Project gallery — public read access")
public class GalleryController {

    private final GalleryService galleryService;

    @GetMapping
    @Operation(summary = "Get gallery items — optionally filter by category")
    public ResponseEntity<ApiResponse<List<GalleryItem>>> list(
            @RequestParam(required = false) String category) {
        return ResponseEntity.ok(ApiResponse.success(galleryService.getItems(category)));
    }
}
