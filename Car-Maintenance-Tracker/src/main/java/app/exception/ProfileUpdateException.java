package app.exception;

import app.user.model.User;
import app.web.dto.EditProfileRequest;

public class ProfileUpdateException extends RuntimeException {

    private final User user;
    private final EditProfileRequest editProfileRequest;

    public ProfileUpdateException(String message, User user, EditProfileRequest request) {
        super(message);
        this.user = user;
        this.editProfileRequest = request;
    }

    public User getUser() {
        return user;
    }

    public EditProfileRequest getEditProfileRequest() {
        return editProfileRequest;
    }
}
