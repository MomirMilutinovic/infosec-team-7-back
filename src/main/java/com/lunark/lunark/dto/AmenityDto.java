package com.lunark.lunark.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.awt.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AmenityDto {
    private Long id;
    private String name;
    private Image icon;
}
