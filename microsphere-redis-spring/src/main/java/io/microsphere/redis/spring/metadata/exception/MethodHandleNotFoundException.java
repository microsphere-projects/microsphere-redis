package io.microsphere.redis.spring.metadata.exception;

public class MethodHandleNotFoundException extends RuntimeException {

    private final String methodSignature;
    public MethodHandleNotFoundException(String message, String methodSignature) {
        super(message);
        this.methodSignature = methodSignature;
    }

    public String getMethodSignature() {
        return methodSignature;
    }
}
