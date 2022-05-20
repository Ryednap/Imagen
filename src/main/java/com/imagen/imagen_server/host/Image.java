package com.imagen.imagen_server.host;


import com.imagen.imagen_server.user.ClientUser;
import lombok.*;

import javax.persistence.*;

@NoArgsConstructor
@Getter
@Setter
@Entity
@EqualsAndHashCode
@ToString
public class Image {

    @Id
    @SequenceGenerator(name="image_id_generator", allocationSize = 1)
    @GeneratedValue(generator="image_id_generator", strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String imageName;

    @Column(nullable = true, unique = true, columnDefinition = "TEXT")
    private String referenceUrl;

    @ManyToOne
    @JoinColumn(name = "client_user_id")
    private ClientUser user;

    public Image(String imageName, String referenceUrl, ClientUser user) {
        this.imageName = imageName;
        this.referenceUrl = referenceUrl;
        this.user = user;
    }
}
