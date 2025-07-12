package com.strengthhub.strength_hub_api.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;
import java.util.UUID;
import jakarta.validation.constraints.*;
import java.util.ArrayList;

@Entity
@Table(name = "coach")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Coach {

    @Id
    @Column(name = "coach_id", columnDefinition = "UUID")
    private UUID coachId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "coach_id")
    @ToString.Exclude // Prevent circular references
    private User app_user;

    @Size(max = 2000, message = "Bio must not exceed 2000 characters")
    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio;

    @Size(max = 1000, message = "Certifications must not exceed 1000 characters")
    @Column(name = "certifications", columnDefinition = "TEXT")
    private String certifications;

    @OneToMany(mappedBy = "coach",
            cascade = {CascadeType.PERSIST, CascadeType.MERGE},
            fetch = FetchType.LAZY)
    @Builder.Default
    @ToString.Exclude // Prevent circular references and lazy loading issues
    private List<Lifter> lifters = new ArrayList<>();

    public int getLifterCount() {
        return lifters != null ? lifters.size() : 0;
    }

    public void addLifter(Lifter lifter) {
        if (lifters == null) {
            lifters = new ArrayList<>();
        }
        lifters.add(lifter);
        lifter.setCoach(this);
    }

    public void removeLifter(Lifter lifter) {
        if (lifters != null) {
            lifters.remove(lifter);
            lifter.setCoach(null);
        }
    }
}