package com.flash.common.exception;

public class ValidationException extends RuntimeException {
    
    public ValidationException(String message) {
        super(message);
    }

    public static class InvalidEmailException extends ValidationException {
        public InvalidEmailException() {
            super("이메일 형식이 올바르지 않습니다");
        }
    }

    public static class InvalidPasswordException extends ValidationException {
        public InvalidPasswordException() {
            super("비밀번호는 8자 이상이어야 합니다");
        }
    }

    public static class EmptyNameException extends ValidationException {
        public EmptyNameException() {
            super("이름은 비어있을 수 없습니다");
        }
    }
} 