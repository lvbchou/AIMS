package com.aims.mapper.product;

import com.aims.dto.product.BookInfoDTO;
import com.aims.entity.product.Book;
import org.springframework.stereotype.Component;

@Component
public class BookMapper extends ProductMapper<BookInfoDTO, Book> {

    public BookMapper(ProductCommonMapper commonMapper){
        super(commonMapper);
    }

    @Override
    public Class<Book> supportedType() {
        return Book.class;
    }

    @Override
    protected void mapTypeFields(BookInfoDTO dto, Book product) {
        dto.setProductType("BOOK");
        dto.setAuthor(product.getAuthor());
        dto.setPublisher(product.getPublisher());
        dto.setPublicationDate(product.getPublicationDate());
        dto.setPages(product.getPages());
        dto.setCoverType(product.getCoverType());
        dto.setLanguage(product.getLanguage());
        dto.setGenre(product.getGenre());
    }

    @Override
    protected BookInfoDTO createDTO() {
        return new BookInfoDTO();
    }
}
