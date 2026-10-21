
package com.eventpro.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "administrators")
@Getter
@NoArgsConstructor
@SuperBuilder
public class Administrator extends User {

    public void manageUsers() {

    }
}
