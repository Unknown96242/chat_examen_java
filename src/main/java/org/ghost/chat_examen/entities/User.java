package org.ghost.chat_examen.entities;

import lombok.*;
import org.ghost.chat_examen.enums.Role;
import org.ghost.chat_examen.enums.Status;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Enumerated(EnumType.STRING)
    private Status status;

    private LocalDateTime dateCreation;


    @PrePersist
    protected void onCreate() {
        this.dateCreation = LocalDateTime.now();
        this.status = Status.OFFLINE;
    }
}