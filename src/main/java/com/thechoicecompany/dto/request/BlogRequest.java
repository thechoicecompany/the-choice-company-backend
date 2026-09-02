package com.thechoicecompany.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
public class BlogRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 300)
    private String title;

    @Size(max = 200)
    private String slug; // optional — auto-generated from title when blank

    @NotBlank(message = "Excerpt is required")
    @Size(max = 500)
    private String excerpt;

    @NotBlank(message = "Content is required")
    private String content;

    private String featuredImage;
    private String featuredImagePublicId;

    @NotBlank(message = "Category is required")
    @Size(max = 100)
    private String category;

    private List<String> tags;

    @NotBlank(message = "Author is required")
    @Size(max = 100)
    private String author;

    private Integer readTime; // auto-estimated if omitted

    @Size(max = 200)
    private String metaTitle;

    @Size(max = 500)
    private String metaDescription;
}