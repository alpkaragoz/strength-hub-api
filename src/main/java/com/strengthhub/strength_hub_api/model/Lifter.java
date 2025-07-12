package com.strengthhub.strength_hub_api.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "lifter")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Lifter {

    @Id
    @Column(name = "lifter_id", columnDefinition = "UUID")
    private UUID lifterId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "lifter_id")
    @ToString.Exclude // Prevent circular references
    private User app_user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coach_id", nullable = true)
    @ToString.Exclude // Prevent circular references and lazy loading issues
    private Coach coach;

    public boolean hasCoach() {
        return coach != null;
    }

    public String getCoachName() {
        if (coach != null && coach.getApp_user() != null) {
            User coachUser = coach.getApp_user();
            return coachUser.getFirstName() + " " + coachUser.getLastName();
        }
        return "No Coach";
    }
}
