
package com.eventpro.entity;

import com.eventpro.entity.enums.TicketStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

@Entity
@Table(name = "tickets")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reservation_line_id", nullable = false)
    private ReservationLine reservationLine;

    @Column(nullable = false)
    private Instant issueDate;

    private Instant scanDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TicketStatus status;

    public boolean validate() {
        return status == TicketStatus.VALID;
    }

    public void scan() {
        if (status == TicketStatus.VALID) {
            this.status = TicketStatus.USED;
            this.scanDate = Instant.now();
        }
    }
}
