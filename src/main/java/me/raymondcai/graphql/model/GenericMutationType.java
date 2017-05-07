package me.raymondcai.graphql.model;

public class GenericMutationType<T> {
    private T input;
    private MutationStatus status;

    public T getInput() {
        return input;
    }

    public GenericMutationType setInput(T input) {
        this.input = input;
        return this;
    }

    public MutationStatus getStatus() {
        return status;
    }

    public GenericMutationType setStatus(MutationStatus status) {
        this.status = status;
        return this;
    }
}
