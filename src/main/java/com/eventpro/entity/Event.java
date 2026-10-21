package com.eventpro.entity;

import com.eventpro.entity.enums.EventStatus;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "events")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organizer_id", nullable = false)
    private Organizer organizer;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 255)
    private String location;

    @Column(nullable = false)
    private Instant startDate;

    @Column(nullable = false)
    private Instant endDate;

    @Column(nullable = false)
    private Instant salesOpeningDate;

    @Column(nullable = false)
    private Instant salesClosingDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EventStatus status;

    @Column(nullable = false)
    private int capacity;

    @ManyToMany
    @JoinTable(
            name = "event_categories",
            joinColumns = @JoinColumn(name = "event_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    @Builder.Default
    private List<TicketCategory> categories = new ArrayList<>();

    public void publish() {
        this.status = EventStatus.PUBLISHED;
    }

    public void cancel() {
        this.status = EventStatus.CANCELED;
    }

    public boolean checkAvailability(int requestedQuantity) {
        return requestedQuantity > 0 && requestedQuantity <= capacity;
    }
}
