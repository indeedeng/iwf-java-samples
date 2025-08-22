package io.iworkflow.patterns.workflow.storage;

public record AddStorageItemRequest(String key, String value) {
    public AddStorageItemRequest {
        if (key == null) {
            throw new IllegalArgumentException("key is null");
        }
        if (value == null) {
            throw new IllegalArgumentException("value is null");
        }
    }
}
