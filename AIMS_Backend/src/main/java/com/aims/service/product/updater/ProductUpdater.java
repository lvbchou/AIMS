package com.aims.service.product.updater;

import com.aims.dto.product.ProductInfoDTO;
import com.aims.entity.product.Product;

public abstract class ProductUpdater<P extends Product, T extends ProductInfoDTO> {

    private final ProductCommonUpdater productCommonUpdater;

    protected ProductUpdater (ProductCommonUpdater productCommonUpdater) {
        this.productCommonUpdater = productCommonUpdater;
    }

    public void update(P product, T dto) {
        productCommonUpdater.update(product, dto);
        updateTypeFields(product, dto);
    }

    protected abstract void updateTypeFields(P product, T dto);
    public abstract String getSupportedType();
}
