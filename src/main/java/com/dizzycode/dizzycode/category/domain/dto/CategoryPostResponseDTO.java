package com.dizzycode.dizzycode.category.domain.dto;

import com.dizzycode.dizzycode.category.domain.Category;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryPostResponseDTO {

    private Long categoryId;
    private Long roomId;
    private String categoryName;

    public static CategoryPostResponseDTO from(Category category) {
        CategoryPostResponseDTO dto = new CategoryPostResponseDTO();
        dto.categoryId = category.getCategoryId();
        dto.roomId = category.getRoom().getRoomId();
        dto.categoryName = category.getCategoryName();
        return dto;
    }
}
