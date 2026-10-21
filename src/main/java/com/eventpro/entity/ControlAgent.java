package com.eventpro.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "control_agents")
@Getter
@NoArgsConstructor
@SuperBuilder
public class ControlAgent extends User {

    public boolean verifyTicket(String code) {

        return true;
    }
}
