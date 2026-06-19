package com.aims.service.product.updater;

import com.aims.dto.product.BookInfoDTO;
import com.aims.entity.product.Book;
import org.springframework.stereotype.Component;

@Component
public class BookUpdater extends ProductUpdater<Book, BookInfoDTO> {

    public BookUpdater(ProductCommonUpdater productCommonUpdater){
        super(productCommonUpdater);
    }

    @Override
    public String getSupportedType(){
        return "BOOK";
    }

    @Override
    protected void updateTypeFields(Book book, BookInfoDTO dto){
        book.setAuthor(dto.getAuthor());
        book.setCoverType(dto.getCoverType());
        book.setPages(dto.getPages());
        book.setGenre(dto.getGenre());
        book.setPublisher(dto.getPublisher());
        book.setPublicationDate(dto.getPublicationDate());
        book.setLanguage(dto.getLanguage());
    }
}
