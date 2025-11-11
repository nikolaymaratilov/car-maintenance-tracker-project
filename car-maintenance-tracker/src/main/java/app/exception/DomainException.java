package app.exception;

public class DomainException extends RuntimeException{

    public DomainException(String message){
        super(message);
    }

    public static DomainException userNotFound(String username){

        return new DomainException(String.format("User with username '%s' does not exist.", username));
    }

    public static DomainException passwordsDontMatch(){

        return new DomainException("Passwords do not match");
    }

    public static DomainException takenUsername(){

        return new DomainException("Username is already taken");
    }
    public static DomainException takenEmail(){

        return new DomainException("Email is already in use");
    }

    public static DomainException invalidPassword(){

        return new DomainException("Incorrect current password.");
    }
}
