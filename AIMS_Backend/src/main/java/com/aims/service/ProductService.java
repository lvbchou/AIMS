package com.aims.service;
import com.aims.dto.ProductInfoDTO;
import com.aims.entity.*;
import com.aims.exception.*;
import com.aims.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*; import java.util.stream.Collectors;
@Service @Transactional
public class ProductService {
    private final ProductRepository productRepository;
    public ProductService(ProductRepository productRepository){ this.productRepository=productRepository; }
    public void saveProduct(ProductInfoDTO productInfo){
        validateProductInfo(productInfo);
        if(productRepository.existsByBarcode(productInfo.getBarcode()))
            throw new ProductAlreadyExistsException(productInfo.getBarcode());
        productRepository.save(buildProductFromDTO(productInfo));
    }
    public void updateProduct(Integer productId,ProductInfoDTO dto){
        validateProductInfo(dto);
        Product existing=productRepository.findById(productId)
                .orElseThrow(()->new ProductNotFoundException(productId));
        existing.setTitle(dto.getTitle()); existing.setCategory(dto.getCategory());
        existing.setBarcode(dto.getBarcode()); existing.setImage(dto.getImage());
        existing.setStatus(dto.getStatus()); existing.setOriginalValue(dto.getOriginalValue());
        existing.setSellingPrice(dto.getSellingPrice()); existing.setWeight(dto.getWeight());
        existing.setDimensions(dto.getDimensions()); existing.setDescription(dto.getDescription());
        if(existing instanceof Book book){
            book.setAuthor(dto.getAuthor()); book.setPublisher(dto.getPublisher());
            book.setPublicationDate(dto.getPublicationDate()); book.setPages(dto.getPages());
            book.setCoverType(dto.getCoverType()); book.setLanguage(dto.getLanguage());
            book.setGenre(dto.getGenre());
        } else if(existing instanceof Newspaper newspaper){
            newspaper.setPublisher(dto.getPublisher()); newspaper.setPublicationDate(dto.getPublicationDate());
            newspaper.setIssueNumber(dto.getIssueNumber()); newspaper.setPublicationFrequency(dto.getPublicationFrequency());
            newspaper.setISSN(dto.getISSN()); newspaper.setLanguage(dto.getLanguage());
            newspaper.setEditorInChief(dto.getEditorInChief()); newspaper.setSections(dto.getSections());
        } else if(existing instanceof DVD dvd){
            dvd.setDiscType(dto.getDiscType()); dvd.setDirector(dto.getDirector());
            dvd.setRuntime(dto.getRuntime()); dvd.setStudio(dto.getStudio());
            dvd.setLanguage(dto.getLanguage()); dvd.setSubtitles(dto.getSubtitles());
            dvd.setReleaseDate(dto.getReleaseDate()); dvd.setGenre(dto.getGenre());
        } else if(existing instanceof CD cd){
            cd.setRecordLabel(dto.getRecordLabel()); cd.setGenre(dto.getGenre());
            cd.setReleaseDate(dto.getReleaseDate());
            if(dto.getArtists()!=null){cd.getArtists().clear();cd.getArtists().addAll(dto.getArtists());}
            if(dto.getTracks()!=null){
                cd.getTracks().clear();
                dto.getTracks().forEach(t->cd.getTracks().add(new Track(t.getTrackTitle(),t.getTrackLength(),cd)));
            }
        }
        productRepository.save(existing);
    }
    @Transactional(readOnly=true)
    public Product viewProduct(String barcode){
        return productRepository.findByBarcode(barcode)
                .orElseThrow(()->new ProductNotFoundException(barcode));
    }
    @Transactional(readOnly=true)
    public List<Product> searchProduct(String keyword,String category){
        return productRepository.searchByKeywordAndCategory(keyword,category);
    }
    @Transactional(readOnly=true)
    public List<Product> filterProduct(List<Product> products,String priceRange){
        String[] parts=priceRange.split("-");
        if(parts.length!=2) throw new InvalidProductInfoException("Invalid price range format. Expected: min-max");
        long min=Long.parseLong(parts[0].trim()); long max=Long.parseLong(parts[1].trim());
        return productRepository.findByPriceRange(min,max);
    }
    @Transactional(readOnly=true)
    public boolean validateQuantityOfSelectedProducts(){
        return productRepository.findAll().stream().allMatch(p->p.getQuantityInStock()>=0);
    }
    @Transactional(readOnly=true)
    public boolean checkStockAvailable(Product product){ return product.getQuantityInStock()>0; }
    public void deleteProduct(Product product){
        productRepository.findById(product.getProductId())
                .orElseThrow(()->new ProductNotFoundException(product.getProductId()));
        productRepository.delete(product);
    }
    public boolean validateProductInfo(ProductInfoDTO p){
        if(p.getTitle()==null||p.getTitle().isBlank()) throw new InvalidProductInfoException("Title must not be empty");
        if(p.getBarcode()==null||p.getBarcode().isBlank()) throw new InvalidProductInfoException("Barcode must not be empty");
        if(p.getOriginalValue()<=0) throw new InvalidProductInfoException("Original value must be positive");
        if(p.getSellingPrice()<=0) throw new InvalidProductInfoException("Selling price must be positive");
        if(p.getSellingPrice()>p.getOriginalValue()*1.5) throw new InvalidProductInfoException("Selling price must not exceed 150% of original value");
        if(p.getQuantityInStock()<0) throw new InvalidProductInfoException("Quantity must not be negative");
        if(p.getProductType()==null) throw new InvalidProductInfoException("Product type is required");
        return true;
    }
    private Product buildProductFromDTO(ProductInfoDTO dto){
        return switch(dto.getProductType().toUpperCase()){
            case "BOOK" -> new Book(dto.getTitle(),dto.getCategory(),dto.getBarcode(),dto.getImage(),
                    dto.getOriginalValue(),dto.getSellingPrice(),dto.getWeight(),dto.getDescription(),
                    dto.getDimensions(),dto.getQuantityInStock(),dto.getPublisher(),dto.getPublicationDate(),
                    dto.getLanguage(),dto.getAuthor(),dto.getCoverType(),dto.getPages(),dto.getGenre());
            case "NEWSPAPER" -> new Newspaper(dto.getTitle(),dto.getCategory(),dto.getBarcode(),dto.getImage(),
                    dto.getOriginalValue(),dto.getSellingPrice(),dto.getWeight(),dto.getDescription(),
                    dto.getDimensions(),dto.getQuantityInStock(),dto.getPublisher(),dto.getPublicationDate(),
                    dto.getLanguage(),dto.getEditorInChief(),dto.getIssueNumber(),
                    dto.getPublicationFrequency(),dto.getISSN(),dto.getSections());
            case "DVD" -> new DVD(dto.getTitle(),dto.getCategory(),dto.getBarcode(),dto.getImage(),
                    dto.getOriginalValue(),dto.getSellingPrice(),dto.getWeight(),dto.getDescription(),
                    dto.getDimensions(),dto.getQuantityInStock(),dto.getGenre(),dto.getReleaseDate(),
                    dto.getDiscType(),dto.getDirector(),dto.getRuntime(),
                    dto.getStudio(),dto.getLanguage(),dto.getSubtitles());
            case "CD" -> {
                CD cd=new CD(dto.getTitle(),dto.getCategory(),dto.getBarcode(),dto.getImage(),
                        dto.getOriginalValue(),dto.getSellingPrice(),dto.getWeight(),dto.getDescription(),
                        dto.getDimensions(),dto.getQuantityInStock(),dto.getGenre(),dto.getReleaseDate(),
                        dto.getArtists(),dto.getRecordLabel());
                if(dto.getTracks()!=null){
                    List<Track> tracks=dto.getTracks().stream()
                            .map(t->new Track(t.getTrackTitle(),t.getTrackLength(),cd))
                            .collect(Collectors.toCollection(ArrayList::new));
                    cd.setTracks(tracks);
                }
                yield cd;
            }
            default -> throw new InvalidProductInfoException("Unknown product type: "+dto.getProductType());
        };
    }
}
