package com.eventpro.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "organizers")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class Organizer extends User {

    @Column(length = 150)
    private String organisationName;

    public void createEvent() {

    }

    public void updateEvent() {
    }
}
