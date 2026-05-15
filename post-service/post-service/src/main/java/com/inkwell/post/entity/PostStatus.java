package com.inkwell.post.entity;

/**
 * Enum representing the different statuses a post can have
 */
public enum PostStatus {
    DRAFT,      // Post is in draft mode, not published
    PUBLISHED,  // Post is published and visible to readers
    UNPUBLISHED,// Post was published but is now unpublished
    ARCHIVED    // Post is archived (legacy/old)
}

