package com.lunark.lunark.auth.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.annotation.Nullable;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Lob;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcType;
import org.hibernate.type.descriptor.jdbc.VarbinaryJdbcType;

@Embeddable
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProfileImage {
    @Lob
    @JdbcType(VarbinaryJdbcType.class)
    @JsonIgnore
    byte[] imageData;
    String mimeType;
}
