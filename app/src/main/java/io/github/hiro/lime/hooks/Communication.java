package io.github.hiro.lime.hooks;

public class Communication {
    public Type type;
    public String name;
    public Object value;

    public Communication(Type type, String name, Object value) {
        this.type = type;
        this.name = name;
        this.value = value;
    }

    @Override
    public String toString() {
        return type.toString() + ": " + name + ", " + value.toString();
    }

    public enum Type {
        REQUEST,
        RESPONSE
    }
}
