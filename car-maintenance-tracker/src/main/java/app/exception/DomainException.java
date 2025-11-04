package app.exception;

public class DomainException extends RuntimeException{

    public DomainException(String message){
        super(message);
    }

    public static DomainException userNotFound(String username){

        return new DomainException(String.format("User with username '%s' does not exist.", username));
    }

    public static DomainException invalidData() {
        return new DomainException("Username or password are incorrect");
    }
}
