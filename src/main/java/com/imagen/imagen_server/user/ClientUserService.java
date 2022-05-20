package com.imagen.imagen_server.user;

import com.imagen.imagen_server.host.Image;
import com.imagen.imagen_server.registration.RequestTemplate;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;

@AllArgsConstructor
@Service
@Slf4j
public class ClientUserService implements UserDetailsService {

    private final PasswordEncoder passwordEncoder;
    private final ClientUserRepository userRepository;

    private ClientUser getClientUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findClientUserByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("No such user of username: " + username + " exists"));
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return getClientUserByUsername(username);
    }

    @Transactional
    public Pair<HttpStatus, String> registerClientUser(RequestTemplate request) {
        try {
            ClientUser user = new ClientUser(request.username(), request.email(), request.password());
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            userRepository.save(user);
            return Pair.of(HttpStatus.CREATED, "user has been registered");

        } catch (DataIntegrityViolationException integrityViolationException) {
            log.error("Integrity Violation : " + integrityViolationException.getMessage());
            return Pair.of(HttpStatus.BAD_REQUEST, "Another user with similar username or email exists");
        }
    }

    @Transactional
    public Pair<HttpStatus, String> removeClientUser(String username) {
        try {

            ClientUser user = getClientUserByUsername(username);
            userRepository.delete(user);
            return Pair.of(HttpStatus.ACCEPTED, "User Removed");

        } catch (UsernameNotFoundException notFoundException) {
          log.error("username not found while removing user: " + notFoundException.getMessage());
          return Pair.of(HttpStatus.BAD_REQUEST, "No such user of username: " + username + " exists");

        } catch (Exception e) {
            log.error("Some exception occurred while removing client : " + e.getMessage());
            return Pair.of(HttpStatus.INTERNAL_SERVER_ERROR, "Some error occurred");
        }
    }
}
