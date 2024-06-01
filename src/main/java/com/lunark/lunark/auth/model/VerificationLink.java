package com.lunark.lunark.auth.model;

import jakarta.persistence.*;
import jakarta.persistence.CascadeType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.*;

import java.time.Duration;
import java.util.Date;

@Entity
@SQLDelete(sql
        = "UPDATE verification_link "
        + "SET deleted = true "
        + "WHERE id = ?")
@Where(clause = "deleted = false")
@NoArgsConstructor
@Data
public class VerificationLink {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotNull
    private boolean used;
    @NotNull
    private Date created;
    @NotNull
    @OneToOne(cascade = CascadeType.ALL)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Account account;

    @Column(name = "deleted", columnDefinition = "boolean default false")
    private boolean deleted = false;

    public VerificationLink(Account account, Date created) {
        this.account = account;
        this.created = created;
        this.used = false;
    }
    
    public boolean isValid() {
        return !used && Duration.between(created.toInstant(), new Date().toInstant()).toDays() < 1;
    }

    public boolean tryUse() {
        if (isValid()) {
            this.used = true;
            return true;
        }
        return false;
    }
}
