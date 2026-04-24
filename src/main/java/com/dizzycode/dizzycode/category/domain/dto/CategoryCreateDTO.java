package com.dizzycode.dizzycode.category.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryCreateDTO {

    @NotBlank
    private String categoryName;
}
