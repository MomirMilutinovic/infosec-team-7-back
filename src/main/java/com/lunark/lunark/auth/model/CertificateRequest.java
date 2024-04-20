package com.lunark.lunark.auth.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.sql.Date;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CertificateRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    @NotNull
    private Account account;

    @Column
    private boolean isUsed;

    @Column
    @NotNull
    private Date timestamp;

    public CertificateRequest(Account account, Date timestamp) {
        this.account = account;
        this.isUsed = false;
        this.timestamp = timestamp;
    }
}
