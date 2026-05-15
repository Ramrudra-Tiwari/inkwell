package com.inkwell.post.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Utility class for slug generation
 * Converts titles to URL-safe hyphenated slugs
 * Example: "My First Post" -> "my-first-post"
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SlugUtil {

    /**
     * Generate a URL-safe slug from a title
     *
     * Conversion rules:
     * - Convert to lowercase
     * - Replace spaces with hyphens
     * - Remove special characters, keeping only alphanumeric and hyphens
     * - Remove leading/trailing hyphens
     * - Replace multiple consecutive hyphens with single hyphen
     *
     * @param title the title to convert
     * @return the generated slug
     */
    public static String generateSlug(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Title cannot be null or empty");
        }

        // Convert to lowercase
        String slug = title.toLowerCase();

        // Replace spaces with hyphens
        slug = slug.replaceAll("\\s+", "-");

        // Remove all special characters except hyphens
        // Keep only alphanumeric characters and hyphens
        slug = slug.replaceAll("[^a-z0-9-]", "");

        // Replace multiple consecutive hyphens with a single hyphen
        slug = slug.replaceAll("-+", "-");

        // Remove leading and trailing hyphens
        slug = slug.replaceAll("^-+|-+$", "");

        log.debug("Generated slug '{}' from title '{}'", slug, title);
        return slug;
    }

    public static String toSlug(String title) {
        return generateSlug(title);
    }

    /**
     * Validate if a string is a valid slug format
     *
     * @param slug the slug to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidSlug(String slug) {
        if (slug == null || slug.isEmpty()) {
            return false;
        }
        return slug.matches("^[a-z0-9]+(?:-[a-z0-9]+)*$");
    }
}

