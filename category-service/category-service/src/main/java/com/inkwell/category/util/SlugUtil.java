package com.inkwell.category.util;

import lombok.extern.slf4j.Slf4j;

/**
 * Utility class for generating URL-safe slugs from category and tag names.
 * Examples: "Programming Languages" -> "programming-languages", "C++" -> "c"
 */
@Slf4j
public class SlugUtil {

    /**
     * Generate a URL-safe slug from a given name.
     * Process:
     * 1. Convert to lowercase
     * 2. Replace spaces and underscores with hyphens
     * 3. Remove non-alphanumeric characters (except hyphens)
     * 4. Collapse consecutive hyphens
     * 5. Remove leading/trailing hyphens
     *
     * @param name The input name
     * @return A URL-safe slug
     */
    public static String generateSlug(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be null or empty");
        }

        // Convert to lowercase
        String slug = name.toLowerCase();

        // Replace spaces and underscores with hyphens
        slug = slug.replaceAll("[\\s_]+", "-");

        // Remove non-alphanumeric characters except hyphens
        slug = slug.replaceAll("[^a-z0-9-]", "");

        // Collapse consecutive hyphens
        slug = slug.replaceAll("-+", "-");

        // Remove leading and trailing hyphens
        slug = slug.replaceAll("^-+|-+$", "");

        if (slug.isEmpty()) {
            throw new IllegalArgumentException("Name generates an empty slug after processing");
        }

        log.debug("Generated slug: '{}' from name: '{}'", slug, name);
        return slug;
    }

    /**
     * Check if a slug is valid (alphanumeric and hyphens only, no leading/trailing hyphens).
     *
     * @param slug The slug to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidSlug(String slug) {
        if (slug == null || slug.isEmpty()) {
            return false;
        }
        return slug.matches("^[a-z0-9]+(?:-[a-z0-9]+)*$");
    }
}

