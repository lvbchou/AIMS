package com.aims.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

import java.io.Serializable;

@Data
public class CartItemRequest implements Serializable {
    private Integer productId;

    @JsonAlias("qOrder")
    private Integer quantity;
}
