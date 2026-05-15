package com.inkwell.category.exception;

/**
 * Exception thrown when a category or tag is not found.
 */
public class TaxonomyNotFoundException extends RuntimeException {

    public TaxonomyNotFoundException(String message) {
        super(message);
    }

    public TaxonomyNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public static TaxonomyNotFoundException categoryNotFound(Integer categoryId) {
        return new TaxonomyNotFoundException("Category not found with ID: " + categoryId);
    }

    public static TaxonomyNotFoundException categoryNotFound(String slug) {
        return new TaxonomyNotFoundException("Category not found with slug: " + slug);
    }

    public static TaxonomyNotFoundException tagNotFound(Integer tagId) {
        return new TaxonomyNotFoundException("Tag not found with ID: " + tagId);
    }

    public static TaxonomyNotFoundException tagNotFound(String slug) {
        return new TaxonomyNotFoundException("Tag not found with slug: " + slug);
    }
}

