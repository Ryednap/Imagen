package com.imagen.imagen_server.host;

import com.imagen.imagen_server.user.ClientUser;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;

@AllArgsConstructor
@Service
@Slf4j
public class ImageService {
    private final ImageRepository imageRepository;
    private final Path imageDir = Paths.get("image");



    @PostConstruct
    private void init () throws IOException {
        if (!Files.exists(imageDir)) Files.createDirectories(imageDir);
    }

    public Pair<HttpStatus, Object> getImageFile(String referenceUrl) {
        Optional<Image> image = imageRepository.findImageByReferenceUrl(referenceUrl);
        if (image.isEmpty()) {
            return Pair.of(HttpStatus.NOT_FOUND, "Image at the specified URL doesn't exist");
        }
        String imageName = image.get().getImageName();
        File imageFile = this.imageDir.resolve(imageName).toFile();

        try{
           byte [] bytes = Files.readAllBytes(imageFile.toPath());
           return Pair.of(HttpStatus.ACCEPTED, bytes);

        } catch (FileNotFoundException fileNotFoundException) {
            return Pair.of(HttpStatus.NOT_FOUND, "Image File not located on server.");
        } catch (IOException e) {
            return Pair.of(HttpStatus.INTERNAL_SERVER_ERROR, "Error loading image File");
        }
    }

    // TODO: Async and MultiPart doesn't work well need to have some concrete solution for dispatching this heavy process to other thread.
    @Transactional
    public Pair<HttpStatus, String> storeImageFile(MultipartFile file, String requestUrl, ClientUser user) {


        String fileName = Objects.requireNonNull(file.getOriginalFilename());
        if (Files.exists(this.imageDir.resolve(fileName))) {
            fileName = FilenameUtils.removeExtension(fileName) + new Pbkdf2PasswordEncoder(LocalDateTime.now().toString())
                    .encode(LocalDateTime.now() + fileName)
                    + "." + FilenameUtils.getExtension(fileName);
        }

        String referenceUrl =  new Pbkdf2PasswordEncoder(fileName)
                .encode(fileName + UUID.randomUUID());

        try {
            InputStream stream = file.getInputStream();
            log.info("Saving image: " + this.imageDir.resolve(fileName));
            Files.copy(file.getInputStream(), this.imageDir.resolve(fileName));
            log.info("Saved File");
            Image image = new Image(
                    fileName,
                    referenceUrl,
                    user
            );

            // Transaction
            imageRepository.save(image);

        } catch (IOException ioException) {
            log.error("IO Exception during image saving: " + ioException.getMessage() + "\n" +
                    Arrays.toString(ioException.getStackTrace()));
            return Pair.of(HttpStatus.INTERNAL_SERVER_ERROR, "Error storing file");
        } catch (DataIntegrityViolationException integrityViolationException) {
            log.error("Clashing reference URL generated : " + referenceUrl);
            return Pair.of(HttpStatus.INTERNAL_SERVER_ERROR, "Clashing reference URL this shouldn't occur");
        }

        return Pair.of(HttpStatus.CREATED, requestUrl + referenceUrl);
    }

    public Collection<String> listImageURLService(ClientUser user, String url) {
        return imageRepository.findAllByUser(user).stream()
                .map(image -> url + image.getReferenceUrl()).toList();

    }

    public boolean isImagePresent(String referenceUrl) {
        return imageRepository.findImageByReferenceUrl(referenceUrl).isPresent();
    }
    @Transactional
    @Async
    public void deleteImageFile(String referenceUrl) {
        if (!isImagePresent(referenceUrl)) return;
        Image image = imageRepository.findImageByReferenceUrl(referenceUrl).get();
        try {
            Files.delete(this.imageDir.resolve(image.getImageName()));
            imageRepository.delete(image);
        } catch (IOException e) {
            log.error("Exception while deleting image with referenceURL + " + referenceUrl + "\n" + Arrays.toString(e.getStackTrace()));
        }

    }
}
