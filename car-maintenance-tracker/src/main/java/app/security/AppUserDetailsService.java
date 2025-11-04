package app.security;

import app.user.model.User;
import app.user.model.UserRole;
import app.user.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class AppUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public AppUserDetailsService(UserRepository userRepository)  {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        Set<UserRole> roles = user.getRoles();
        UserRole role = roles.isEmpty() ? UserRole.USER : roles.iterator().next();

        return new UserData(
                user.getId(),
                role,
                user.getUsername(),
                user.getPassword(),
                user.isEnabled()
        );
    }
}
