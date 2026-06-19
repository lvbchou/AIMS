package com.aims.service.product.creator;

import com.aims.dto.product.BookInfoDTO;
import com.aims.entity.product.Book;
import com.aims.entity.product.Product;
import org.springframework.stereotype.Component;

@Component
public class BookCreator extends ProductCreator<BookInfoDTO> {

    @Override
    public String getSupportedType() {
        return "BOOK";
    }

    @Override
    protected Product buildProduct(BookInfoDTO book) {
        return new Book(
                book.getTitle(), book.getCategory(), book.getBarcode(), book.getImage(),
                book.getOriginalValue(), book.getSellingPrice(), book.getWeight(),
                book.getDescription(), book.getDimensions(), 0,
                book.getPublisher(), book.getPublicationDate(), book.getLanguage(),
                book.getAuthor(), book.getCoverType(), book.getPages(), book.getGenre()
        );
    }
}