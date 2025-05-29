package com.flash.user.exception;

public class UserException extends RuntimeException {

    public static class UserNotFoundException extends UserException {
        public UserNotFoundException() {
            super("사용자를 찾을 수 없습니다.");
        }
    }

    public static class UserAlreadyExistsException extends UserException {
        public UserAlreadyExistsException() {
            super("이미 존재하는 이메일입니다.");
        }
    }

    public static class InvalidPasswordException extends UserException {
        public InvalidPasswordException() {
            super("비밀번호가 일치하지 않습니다.");
        }
    }

    public static class UnauthorizedException extends UserException {
        public UnauthorizedException() {
            super("Unauthorized access");
        }
    }

    protected UserException() {
        super();
    }

    protected UserException(String message) {
        super(message);
    }

    protected UserException(String message, Throwable cause) {
        super(message, cause);
    }
}