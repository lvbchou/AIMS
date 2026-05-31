package com.aims.service.creator;

import com.aims.dto.product.BookInfoDTO;
import com.aims.dto.product.ProductInfoDTO;
import com.aims.entity.product.Book;
import com.aims.entity.product.Product;
import org.springframework.stereotype.Component;

@Component
public class BookCreator extends ProductCreator {

    @Override
    protected Product buildProduct(ProductInfoDTO dto) {
        BookInfoDTO book = (BookInfoDTO) dto;
        return new Book(
                book.getTitle(), book.getCategory(), book.getBarcode(), book.getImage(),
                book.getOriginalValue(), book.getSellingPrice(), book.getWeight(),
                book.getDescription(), book.getDimensions(), 0,
                book.getPublisher(), book.getPublicationDate(), book.getLanguage(),
                book.getAuthor(), book.getCoverType(), book.getPages(), book.getGenre()
        );
    }
}