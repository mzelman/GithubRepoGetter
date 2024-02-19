package mz.githubrepogetter.exception;

public class UsernameNotFoundException extends RuntimeException {
    public UsernameNotFoundException(String username) {
        super("User with username " + username + " does not exist.");
    }
}