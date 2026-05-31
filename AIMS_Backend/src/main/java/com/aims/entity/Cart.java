package com.aims.entity;

import lombok.Getter;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;

/**
 * Cart - representing a shopping cart containing products.
 */
@Getter
@Setter
public class Cart {
    private List<Object> items = new ArrayList<>();

    public boolean isEmpty() {
        return items.isEmpty();
    }
}
