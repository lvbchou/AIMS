package com.aims.dto;

import lombok.Data;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class Cart implements Serializable {
    private List<CartItem> items = new ArrayList<>();
}
