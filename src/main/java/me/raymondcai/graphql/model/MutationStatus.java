package me.raymondcai.graphql.model;

public class MutationStatus {
    private String error;
    private String message;

    public String getError() {
        return error;
    }

    public MutationStatus setError(String error) {
        this.error = error;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public MutationStatus setMessage(String message) {
        this.message = message;
        return this;
    }
}
