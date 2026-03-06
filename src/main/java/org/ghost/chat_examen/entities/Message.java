package org.ghost.chat_examen.entities;

import lombok.*;
import org.ghost.chat_examen.enums.MessageStatut;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
@Getter
@Setter
@NoArgsConstructor
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    @Column(length = 1000, nullable = false)
    private String contenu;

    private LocalDateTime dateEnvoi;

    @Enumerated(EnumType.STRING)
    private MessageStatut statut;

    @PrePersist
    protected void onSend() {
        this.dateEnvoi = LocalDateTime.now();
        this.statut = MessageStatut.ENVOYE;
    }
}
