package com.aims.service;

import com.aims.dto.ProductInfoDTO;
import com.aims.dto.ProductSummaryDTO;
import com.aims.entity.*;
import com.aims.exception.*;
import com.aims.mapper.ProductMapper;
import com.aims.entity.*;
import com.aims.exception.*;
import com.aims.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * ProductService - implements business logic for CUD Product use case.
 * Matches the ProductService class in the service package diagram.
 */
@Service
@Transactional
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // ----------------------------------------------------------------
    // CREATE
    // ----------------------------------------------------------------

    public Product saveProduct(ProductInfoDTO productInfo) {
    public void saveProduct(ProductInfoDTO productInfo) {
        validateProductInfo(productInfo);

        if (productRepository.existsByBarcode(productInfo.getBarcode())) {
            throw new ProductAlreadyExistsException(productInfo.getBarcode());
        }

        Product product = buildProductFromDTO(productInfo);
        return productRepository.save(product);
        productRepository.save(product);
    }

    public void updateProduct(Integer productId, ProductInfoDTO dto) {
        validateProductInfo(dto);
        Product existing = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        existing.setTitle(dto.getTitle());
        existing.setCategory(dto.getCategory());
        existing.setBarcode(dto.getBarcode());
        existing.setImage(dto.getImage());
        existing.setStatus(dto.getStatus());
        existing.setOriginalValue(dto.getOriginalValue());
        existing.setSellingPrice(dto.getSellingPrice());
        existing.setWeight(dto.getWeight());
        existing.setDimensions(dto.getDimensions());
        existing.setDescription(dto.getDescription());

        if (existing instanceof Book book) {
            book.setAuthor(dto.getAuthor());
            book.setPublisher(dto.getPublisher());
            book.setPublicationDate(dto.getPublicationDate());
            book.setPages(dto.getPages());
            book.setCoverType(dto.getCoverType());
            book.setLanguage(dto.getLanguage());
            book.setGenre(dto.getGenre());

        } else if (existing instanceof Newspaper newspaper) {
            newspaper.setPublisher(dto.getPublisher());
            newspaper.setPublicationDate(dto.getPublicationDate());
            newspaper.setIssueNumber(dto.getIssueNumber());
            newspaper.setPublicationFrequency(dto.getPublicationFrequency());
            newspaper.setISSN(dto.getISSN());
            newspaper.setLanguage(dto.getLanguage());
            newspaper.setEditorInChief(dto.getEditorInChief());
            newspaper.setSections(dto.getSections());

        } else if (existing instanceof DVD dvd) {
            dvd.setDiscType(dto.getDiscType());
            dvd.setDirector(dto.getDirector());
            dvd.setRuntime(dto.getRuntime());
            dvd.setStudio(dto.getStudio());
            dvd.setLanguage(dto.getLanguage());
            dvd.setSubtitles(dto.getSubtitles());
            dvd.setReleaseDate(dto.getReleaseDate());
            dvd.setGenre(dto.getGenre());

        } else if (existing instanceof CD cd) {
            cd.setRecordLabel(dto.getRecordLabel());
            cd.setGenre(dto.getGenre());
            cd.setReleaseDate(dto.getReleaseDate());

            if (dto.getArtists() != null) {
                cd.getArtists().clear();
                cd.getArtists().addAll(dto.getArtists());
            }

            if (dto.getTracks() != null) {
                cd.getTracks().clear();
                dto.getTracks().forEach(t ->
                        cd.getTracks().add(
                                new Track(t.getTrackTitle(), t.getTrackLength(), cd))
                );
            }
        }

        productRepository.save(existing);
    }

    // ----------------------------------------------------------------
    // READ
    // ----------------------------------------------------------------
    @Transactional(readOnly = true)
    public List<ProductSummaryDTO> getAllProducts() {
        return productRepository.findAllActive()
                .stream()
                .map(p -> new ProductSummaryDTO(
                        p.getProductId(),
                        p.getTitle(),
                        p.getClass().getSimpleName().toUpperCase(),  // "CD", "DVD"...
                        p.getSellingPrice(),
                        p.getImage()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductInfoDTO viewProduct(Integer productId) {
        Product product = productRepository.findActiveById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        return ProductMapper.toDTO(product);
    }

    @Transactional(readOnly = true)
    public List<ProductInfoDTO> searchProduct(String keyword, String category) {
        return productRepository.searchByKeywordAndCategory(keyword, category)
                .stream()
                .map(ProductMapper::toDTO)
                .toList();
    }

    public List<ProductInfoDTO> filterProduct(List<ProductInfoDTO> products, String priceRange) {

    @Transactional(readOnly = true)
    public Product viewProduct(String barcode) {
        return productRepository.findByBarcode(barcode)
                .orElseThrow(() -> new ProductNotFoundException(barcode));
    }

    /**
     * Search products by keyword.
     */
    @Transactional(readOnly = true)
    public List<Product> searchProduct(String keyword) {
        return productRepository.searchByKeywordOrCategory(keyword);
    }

    @Transactional(readOnly = true)
    public List<Product> filterProduct(List<Product> products, String priceRange) {
        String[] parts = priceRange.split("-");
        if (parts.length != 2) {
            throw new InvalidProductInfoException("Invalid price range format. Expected: min-max");
        }
        long min = Long.parseLong(parts[0].trim());
        long max = Long.parseLong(parts[1].trim());

        return products.stream()
                .filter(p -> p.getSellingPrice() >= min && p.getSellingPrice() <= max)
                .toList();
        return productRepository.findByPriceRange(min, max);
    }

    @Transactional(readOnly = true)
    public boolean validateQuantityOfSelectedProducts() {
        return productRepository.findAll()
                .stream()
                .allMatch(p -> p.getQuantityInStock() >= 0);
    }

    @Transactional(readOnly = true)
    public boolean checkStockAvailable(Product product) {
        return product.getQuantityInStock() > 0;
    }

    // ----------------------------------------------------------------
    // UPDATE
    // ----------------------------------------------------------------

    public void deleteProduct(Integer productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        if (product.getQuantityInStock() > 0) {
            // Còn hàng → chỉ deactivated
            product.setStatus("deactivated");
        } else {
            // Hết hàng → deleted
            product.setStatus("deleted");
        }

        productRepository.save(product);
    // DELETE
    // ----------------------------------------------------------------

    public void deleteProduct(Product product) {
        productRepository.findById(product.getProductId())
                .orElseThrow(() -> new ProductNotFoundException(product.getProductId()));
        productRepository.delete(product);
    }

    // ----------------------------------------------------------------
    // VALIDATION
    // ----------------------------------------------------------------

    public boolean validateProductInfo(ProductInfoDTO productInfo) {
        if (productInfo.getTitle() == null || productInfo.getTitle().isBlank()) {
            throw new InvalidProductInfoException("Title must not be empty");
        }
        if (productInfo.getBarcode() == null || productInfo.getBarcode().isBlank()) {
            throw new InvalidProductInfoException("Barcode must not be empty");
        }
        if (productInfo.getCategory() == null || productInfo.getCategory().isBlank()) {
            throw new InvalidProductInfoException("Category must not be empty");
        }
        if (productInfo.getImage() == null || productInfo.getImage().isBlank()) {
            throw new InvalidProductInfoException("Image must not be empty");
        }
        if (productInfo.getDescription() == null || productInfo.getDescription().isBlank()) {
            throw new InvalidProductInfoException("Description must not be empty");
        }
        if (productInfo.getDimensions() == null || productInfo.getDimensions().isBlank()) {
            throw new InvalidProductInfoException("Dimensions must not be empty");
        }
        if (productInfo.getOriginalValue() == null || productInfo.getOriginalValue() <= 0) {
            throw new InvalidProductInfoException("Original value must be positive");
        }
        if (productInfo.getSellingPrice() == null || productInfo.getSellingPrice() <= 0) {
            throw new InvalidProductInfoException("Selling price must be positive");
        }
        if (productInfo.getSellingPrice() < productInfo.getOriginalValue() * 0.3) {
            throw new InvalidProductInfoException(
                    "Selling price must not smaller than 30% of original value");
        }
        if (productInfo.getOriginalValue() <= 0) {
            throw new InvalidProductInfoException("Original value must be positive");
        }
        if (productInfo.getSellingPrice() <= 0) {
            throw new InvalidProductInfoException("Selling price must be positive");
        }
        if (productInfo.getSellingPrice() > productInfo.getOriginalValue() * 1.5) {
            throw new InvalidProductInfoException(
                    "Selling price must not exceed 150% of original value");
        }
        if (productInfo.getWeight() == null || productInfo.getWeight() <= 0) {
            throw new InvalidProductInfoException("Weight must be positive");
        if (productInfo.getQuantityInStock() < 0) {
            throw new InvalidProductInfoException("Quantity must not be negative");
        }
        if (productInfo.getProductType() == null) {
            throw new InvalidProductInfoException("Product type is required");
        }
        switch (productInfo.getProductType().toUpperCase()) {
            case "BOOK" -> {
                if (productInfo.getAuthor() == null || productInfo.getAuthor().isBlank()) {
                    throw new InvalidProductInfoException("Author is required for Book");
                }

                if (productInfo.getCoverType() == null || productInfo.getCoverType().isBlank()) {
                    throw new InvalidProductInfoException("Cover type is required for Book");
                }
                if (!productInfo.getCoverType().toUpperCase().equals("HARDCOVER") &&
                        !productInfo.getCoverType().toUpperCase().equals("PAPERBACK")) {
                    throw new InvalidProductInfoException("Cover type must be Paperback or Hardcover");
                }
                if (productInfo.getPublisher() == null || productInfo.getPublisher().isBlank()) {
                    throw new InvalidProductInfoException("Publisher is required for Book");
                }
                if (productInfo.getPublicationDate() == null) {
                    throw new InvalidProductInfoException("Publication date is required for Book");
                }
                if (productInfo.getPages() != null && productInfo.getPages() <= 0) {
                    throw new InvalidProductInfoException("Pages value must be positive");
                }
            }
            case "NEWSPAPER" -> {
                if (productInfo.getEditorInChief() == null || productInfo.getEditorInChief().isBlank()) {
                    throw new InvalidProductInfoException("Editor in chief is required for Newspaper");
                }
                if (productInfo.getPublisher() == null || productInfo.getPublisher().isBlank()) {
                    throw new InvalidProductInfoException("Publisher is required for Newspaper");
                }
                if (productInfo.getPublicationDate() == null) {
                    throw new InvalidProductInfoException("Publication date is required for Newspaper");
                }
            }
            case "CD" -> {
                if (productInfo.getArtists() == null || productInfo.getArtists().isEmpty())
                    throw new InvalidProductInfoException("Artists is required for CD");

                if (productInfo.getRecordLabel() == null || productInfo.getRecordLabel().isBlank())
                    throw new InvalidProductInfoException("Record label is required for CD");

                if (productInfo.getGenre() == null || productInfo.getGenre().isBlank())
                    throw new InvalidProductInfoException("Genre is required for CD");

                if (productInfo.getTracks() == null || productInfo.getTracks().isEmpty())
                    throw new InvalidProductInfoException(
                            "CD must have at least one track");

                // validate each track
                for (ProductInfoDTO.TrackDTO track : productInfo.getTracks()) {
                    if (track.getTrackTitle() == null || track.getTrackTitle().isBlank())
                        throw new InvalidProductInfoException("Track title is required");

                    if (track.getTrackLength() == null || track.getTrackLength().isBlank())
                        throw new InvalidProductInfoException("Track length is required");
                }
            }
            case "DVD" -> {
                if (productInfo.getDiscType() == null || productInfo.getDiscType().isBlank())
                    throw new InvalidProductInfoException("Disc type is required for DVD");

                if (!productInfo.getDiscType().toUpperCase().equals("BLU-RAY") &&
                !productInfo.getDiscType().toUpperCase().equals("HD-DVD"))
                    throw new InvalidProductInfoException(
                            "Disc type must be Blu-ray or HD-DVD");

                if (productInfo.getDirector() == null || productInfo.getDirector().isBlank())
                    throw new InvalidProductInfoException("Director is required for DVD");

                if (productInfo.getRuntime() == null)
                    throw new InvalidProductInfoException("Runtime is required for DVD");

                if (productInfo.getRuntime() <= 0)
                    throw new InvalidProductInfoException("Runtime must be positive");

                if (productInfo.getStudio() == null || productInfo.getStudio().isBlank())
                    throw new InvalidProductInfoException("Studio is required for DVD");

                if (productInfo.getLanguage() == null || productInfo.getLanguage().isBlank())
                    throw new InvalidProductInfoException("Language is required for DVD");

                if (productInfo.getSubtitles() == null || productInfo.getSubtitles().isBlank())
                    throw new InvalidProductInfoException("Subtitles is required for DVD");
            }
            default -> {
                throw new InvalidProductInfoException(
                        "Product type must be one of: BOOK, CD, NEWSPAPER, DVD");
            }
        }
        return true;
    }

    // ----------------------------------------------------------------
    // FACTORY METHOD - builds correct subtype from DTO
    // ----------------------------------------------------------------

    private Product buildProductFromDTO(ProductInfoDTO dto) {
        dto.setQuantityInStock(0);
    // FACTORY METHOD
    // ----------------------------------------------------------------

    private Product buildProductFromDTO(ProductInfoDTO dto) {
        return switch (dto.getProductType().toUpperCase()) {
            case "BOOK" -> {
                Book book = new Book(
                        dto.getTitle(), dto.getCategory(), dto.getBarcode(), dto.getImage(),
                        dto.getOriginalValue(), dto.getSellingPrice(), dto.getWeight(),
                        dto.getDescription(), dto.getDimensions(), dto.getQuantityInStock(),
                        dto.getPublisher(), dto.getPublicationDate(), dto.getLanguage(),
                        dto.getAuthor(), dto.getCoverType(), dto.getPages(), dto.getGenre());
                yield book;
            }
            case "NEWSPAPER" -> {
                Newspaper newspaper = new Newspaper(
                        dto.getTitle(), dto.getCategory(), dto.getBarcode(), dto.getImage(),
                        dto.getOriginalValue(), dto.getSellingPrice(), dto.getWeight(),
                        dto.getDescription(), dto.getDimensions(), dto.getQuantityInStock(),
                        dto.getPublisher(), dto.getPublicationDate(), dto.getLanguage(),
                        dto.getEditorInChief(), dto.getIssueNumber(),
                        dto.getPublicationFrequency(), dto.getISSN(), dto.getSections());
                yield newspaper;
            }
            case "DVD" -> {
                DVD dvd = new DVD(
                        dto.getTitle(), dto.getCategory(), dto.getBarcode(), dto.getImage(),
                        dto.getOriginalValue(), dto.getSellingPrice(), dto.getWeight(),
                        dto.getDescription(), dto.getDimensions(), dto.getQuantityInStock(),
                        dto.getGenre(), dto.getReleaseDate(),
                        dto.getDiscType(), dto.getDirector(), dto.getRuntime(),
                        dto.getStudio(), dto.getLanguage(), dto.getSubtitles());
                yield dvd;
            }
            case "CD" -> {
                CD cd = new CD(
                        dto.getTitle(), dto.getCategory(), dto.getBarcode(), dto.getImage(),
                        dto.getOriginalValue(), dto.getSellingPrice(), dto.getWeight(),
                        dto.getDescription(), dto.getDimensions(), dto.getQuantityInStock(),
                        dto.getGenre(), dto.getReleaseDate(),
                        dto.getArtists(), dto.getRecordLabel());
                if (dto.getTracks() != null) {
                    List<Track> tracks = dto.getTracks().stream()
                            .map(t -> new Track(t.getTrackTitle(),
                                                t.getTrackLength(), cd))
                            .map(t -> new Track(t.getTrackTitle(), t.getTrackLength(), cd))
                            .collect(Collectors.toCollection(ArrayList::new));
                    cd.setTracks(tracks);
                }
                yield cd;
            }
            default -> throw new InvalidProductInfoException(
                    "Unknown product type: " + dto.getProductType());
        };
    }
}
}
