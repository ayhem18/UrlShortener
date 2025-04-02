package org.apiUtils.commonClasses;

public class CommonExceptions {
    public static class InsufficientRoleAuthority extends RuntimeException {
        public InsufficientRoleAuthority(String message) {
            super(message);
        }
    }

    public static class UserNotFoundException extends RuntimeException {
        public UserNotFoundException(String message) {
            super(message);
        }
    }
}
