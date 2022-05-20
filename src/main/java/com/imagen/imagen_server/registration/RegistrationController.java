package com.imagen.imagen_server.registration;

import com.imagen.imagen_server.user.ClientUserService;
import lombok.AllArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("api/register")
@AllArgsConstructor
public class RegistrationController {

    private final ClientUserService userService;

    private static String formatMessage(String message) {
        return String.format(
                "'message' : '%s'",
                message
        );
    }

    @PostMapping
    public ResponseEntity<?> registerUser(@RequestBody RequestTemplate request) {
        Pair<HttpStatus, String> response = userService.registerClientUser(request);
        return ResponseEntity.status(response.getFirst()).contentType(MediaType.APPLICATION_JSON)
                .body(formatMessage(response.getSecond()));
    }

    @DeleteMapping
    public ResponseEntity<?> removeUser(@RequestParam String username) {
        if (username == null) {
            return ResponseEntity.badRequest().body(formatMessage("Username must be provided"));
        }
        Pair<HttpStatus, String> response = userService.removeClientUser(username);
        return ResponseEntity.status(response.getFirst()).contentType(MediaType.APPLICATION_JSON)
                .body(formatMessage(response.getSecond()));
    }
}
