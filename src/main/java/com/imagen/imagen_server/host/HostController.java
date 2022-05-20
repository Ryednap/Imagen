package com.imagen.imagen_server.host;

import com.imagen.imagen_server.user.ClientUser;
import lombok.AllArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("api/v1/host")
@AllArgsConstructor
@EnableAsync
public class HostController {

    private final ImageService imageService;



    private String getAbsoluteServerURL(HttpServletRequest request) {
        return request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getServletPath();
    }

    @GetMapping("{referenceUrl}")
    @Async
    public CompletableFuture<ResponseEntity<?>> downloadImageFile(@PathVariable String referenceUrl) {
        Pair<HttpStatus, Object> response = imageService.getImageFile(referenceUrl);
        return CompletableFuture.completedFuture(ResponseEntity.status(response.getFirst()).contentType(MediaType.IMAGE_PNG)
                .body(response.getSecond()));
    }

    @PostMapping
    @Async
    public CompletableFuture<ResponseEntity<?>> uploadImageFile(@RequestParam("image")  MultipartFile imageFile,
                                                                HttpServletRequest request, @AuthenticationPrincipal ClientUser user) {


        Pair<HttpStatus, String> response = imageService.storeImageFile(imageFile, getAbsoluteServerURL(request), user);

       return CompletableFuture.completedFuture(ResponseEntity.status(response.getFirst()).contentType(MediaType.APPLICATION_JSON)
               .body(String.format("{ 'url' : '%s'}", response.getSecond())));
    }

    @GetMapping
    public ResponseEntity<?> listImageFiles(@AuthenticationPrincipal ClientUser user, HttpServletRequest request) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON)
                .body(imageService.listImageURLService(user, getAbsoluteServerURL(request)));
    }

    @DeleteMapping("{referenceUrl}")
    public ResponseEntity<?> removeImageFile(@PathVariable String referenceUrl) {
        imageService.deleteImageFile(referenceUrl);
        if (imageService.isImagePresent(referenceUrl)) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
