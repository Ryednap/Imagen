package com.imagen.imagen_server.host;

import com.imagen.imagen_server.user.ClientUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {
    Optional<Image> findImageByReferenceUrl(String referenceUrl);

    Collection<Image> findAllByUser(ClientUser user);

}
