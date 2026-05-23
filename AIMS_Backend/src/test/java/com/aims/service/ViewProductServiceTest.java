package com.aims.service;

import com.aims.entity.*;
import com.aims.exception.ProductNotFoundException;
import com.aims.repository.ProductRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 Unit tests for UC: View Product Details
 Unit under test: ProductService.viewProduct(String barcode)

 Equivalence Partitions:
  - EP1: barcode exists → returns full product object
  - EP2: barcode does NOT exist → throws ProductNotFoundException
  - EP3: barcode is null → throws ProductNotFoundException
  - EP4: barcode is empty / whitespace → throws ProductNotFoundException
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ViewProductServiceTest – UC: View Product Details")
class ViewProductServiceTest {

    @Mock  private ProductRepository productRepository;
    @InjectMocks private ProductService productService;

    private Book      sampleBook;
    private DVD       sampleDvd;
    private CD        sampleCd;
    private Newspaper sampleNewspaper;

    @BeforeEach
    void setUp() {
        sampleBook = new Book(
                "Clean Code", "Book", "978-0132350884", "img.jpg",
                300_000L, 350_000L, 0.5, "Agile handbook", "24x16 cm", 50,
                "Prentice Hall", LocalDate.of(2008, 8, 1), "English",
                "Robert C. Martin", "Paperback", 431, "Technology");
        sampleBook.setProductId(1);

        sampleDvd = new DVD(
                "Inception", "DVD", "DVD-001", "inception.jpg",
                200_000L, 250_000L, 0.1, "Sci-fi thriller", "19x13 cm", 20,
                "Sci-Fi", LocalDate.of(2010, 7, 16),
                "Blu-ray", "Christopher Nolan", 148, "Warner Bros", "English", "Vietnamese");
        sampleDvd.setProductId(2);

        sampleCd = new CD(
                "Abbey Road", "CD", "CD-001", "abbey.jpg",
                150_000L, 180_000L, 0.08, "Classic album", "12x12 cm", 30,
                "Rock", LocalDate.of(1969, 9, 26),
                List.of("The Beatles"), "Apple Records");
        sampleCd.setProductId(3);
        Track track1 = new Track("Come Together", "4:19", sampleCd);
        Track track2 = new Track("Something", "3:03", sampleCd);
        sampleCd.getTracks().addAll(List.of(track1, track2));

        sampleNewspaper = new Newspaper(
                "Tuoi Tre Daily", "Newspaper", "NEWS-001", "tuoitre.jpg",
                10_000L, 12_000L, 0.2, "Daily newspaper", "30x42 cm", 100,
                "Tuoi Tre", LocalDate.of(2024, 1, 1), "Vietnamese",
                "Nguyen Van A", "1234", "Daily", "ISSN-001",
                List.of("Politics", "Sports", "Economy"));
        sampleNewspaper.setProductId(4);
    }

    // ================================================================
    // EP1 – barcode exists: full product object returned
    // ================================================================

    @Test
    @DisplayName("UT001 View Valid Book Product")
    void UT001_viewValidBookProduct() {
        // EP1: barcode exists, product type = Book
        // Input : barcode = '978-0132350884'
        // Expect: Returns full Book object with all fields matching fixture
        when(productRepository.findByBarcode("978-0132350884")).thenReturn(Optional.of(sampleBook));

        Product result = productService.viewProduct("978-0132350884");
        Book book = (Book) result;

        assertThat(book.getProductId()).isEqualTo(1);
        assertThat(book.getTitle()).isEqualTo("Clean Code");
        assertThat(book.getCategory()).isEqualTo("Book");
        assertThat(book.getBarcode()).isEqualTo("978-0132350884");
        assertThat(book.getImage()).isEqualTo("img.jpg");
        assertThat(book.getOriginalValue()).isEqualTo(300_000L);
        assertThat(book.getSellingPrice()).isEqualTo(350_000L);
        assertThat(book.getWeight()).isEqualTo(0.5);
        assertThat(book.getDescription()).isEqualTo("Agile handbook");
        assertThat(book.getDimensions()).isEqualTo("24x16 cm");
        assertThat(book.getQuantityInStock()).isEqualTo(50);
        assertThat(book.getStatus()).isEqualTo("active");
        assertThat(book.getPublisher()).isEqualTo("Prentice Hall");
        assertThat(book.getPublicationDate()).isEqualTo(LocalDate.of(2008, 8, 1));
        assertThat(book.getLanguage()).isEqualTo("English");
        assertThat(book.getAuthor()).isEqualTo("Robert C. Martin");
        assertThat(book.getCoverType()).isEqualTo("Paperback");
        assertThat(book.getPages()).isEqualTo(431);
        assertThat(book.getGenre()).isEqualTo("Technology");
    }

    @Test
    @DisplayName("UT002 View Valid DVD Product")
    void UT002_viewValidDvdProduct() {
        // EP1: barcode exists, product type = DVD
        // Input : barcode = 'DVD-001'
        // Expect: Returns full DVD object with all fields matching fixture
        when(productRepository.findByBarcode("DVD-001")).thenReturn(Optional.of(sampleDvd));

        Product result = productService.viewProduct("DVD-001");
        DVD dvd = (DVD) result;

        assertThat(dvd.getProductId()).isEqualTo(2);
        assertThat(dvd.getTitle()).isEqualTo("Inception");
        assertThat(dvd.getCategory()).isEqualTo("DVD");
        assertThat(dvd.getBarcode()).isEqualTo("DVD-001");
        assertThat(dvd.getImage()).isEqualTo("inception.jpg");
        assertThat(dvd.getOriginalValue()).isEqualTo(200_000L);
        assertThat(dvd.getSellingPrice()).isEqualTo(250_000L);
        assertThat(dvd.getWeight()).isEqualTo(0.1);
        assertThat(dvd.getDescription()).isEqualTo("Sci-fi thriller");
        assertThat(dvd.getDimensions()).isEqualTo("19x13 cm");
        assertThat(dvd.getQuantityInStock()).isEqualTo(20);
        assertThat(dvd.getStatus()).isEqualTo("active");
        assertThat(dvd.getGenre()).isEqualTo("Sci-Fi");
        assertThat(dvd.getReleaseDate()).isEqualTo(LocalDate.of(2010, 7, 16));
        assertThat(dvd.getDiscType()).isEqualTo("Blu-ray");
        assertThat(dvd.getDirector()).isEqualTo("Christopher Nolan");
        assertThat(dvd.getRuntime()).isEqualTo(148);
        assertThat(dvd.getStudio()).isEqualTo("Warner Bros");
        assertThat(dvd.getLanguage()).isEqualTo("English");
        assertThat(dvd.getSubtitles()).isEqualTo("Vietnamese");
    }

    @Test
    @DisplayName("UT003 View Valid CD Product")
    void UT003_viewValidCdProduct() {
        // EP1: barcode exists, product type = CD
        // Input : barcode = 'CD-001'
        // Expect: Returns full CD object including tracks
        when(productRepository.findByBarcode("CD-001")).thenReturn(Optional.of(sampleCd));

        Product result = productService.viewProduct("CD-001");
        CD cd = (CD) result;

        assertThat(cd.getProductId()).isEqualTo(3);
        assertThat(cd.getTitle()).isEqualTo("Abbey Road");
        assertThat(cd.getCategory()).isEqualTo("CD");
        assertThat(cd.getBarcode()).isEqualTo("CD-001");
        assertThat(cd.getImage()).isEqualTo("abbey.jpg");
        assertThat(cd.getOriginalValue()).isEqualTo(150_000L);
        assertThat(cd.getSellingPrice()).isEqualTo(180_000L);
        assertThat(cd.getWeight()).isEqualTo(0.08);
        assertThat(cd.getDescription()).isEqualTo("Classic album");
        assertThat(cd.getDimensions()).isEqualTo("12x12 cm");
        assertThat(cd.getQuantityInStock()).isEqualTo(30);
        assertThat(cd.getStatus()).isEqualTo("active");
        assertThat(cd.getGenre()).isEqualTo("Rock");
        assertThat(cd.getReleaseDate()).isEqualTo(LocalDate.of(1969, 9, 26));
        assertThat(cd.getArtists()).containsExactly("The Beatles");
        assertThat(cd.getRecordLabel()).isEqualTo("Apple Records");
        assertThat(cd.getTracks()).hasSize(2);
        assertThat(cd.getTracks().get(0).getTrackTitle()).isEqualTo("Come Together");
        assertThat(cd.getTracks().get(0).getTrackLength()).isEqualTo("4:19");
        assertThat(cd.getTracks().get(1).getTrackTitle()).isEqualTo("Something");
        assertThat(cd.getTracks().get(1).getTrackLength()).isEqualTo("3:03");
    }

    @Test
    @DisplayName("UT004 View Valid Newspaper Product")
    void UT004_viewValidNewspaperProduct() {
        // EP1: barcode exists, product type = Newspaper
        // Input : barcode = 'NEWS-001'
        // Expect: Returns full Newspaper object with all fields matching fixture
        when(productRepository.findByBarcode("NEWS-001")).thenReturn(Optional.of(sampleNewspaper));

        Product result = productService.viewProduct("NEWS-001");
        Newspaper newspaper = (Newspaper) result;

        assertThat(newspaper.getProductId()).isEqualTo(4);
        assertThat(newspaper.getTitle()).isEqualTo("Tuoi Tre Daily");
        assertThat(newspaper.getCategory()).isEqualTo("Newspaper");
        assertThat(newspaper.getBarcode()).isEqualTo("NEWS-001");
        assertThat(newspaper.getImage()).isEqualTo("tuoitre.jpg");
        assertThat(newspaper.getOriginalValue()).isEqualTo(10_000L);
        assertThat(newspaper.getSellingPrice()).isEqualTo(12_000L);
        assertThat(newspaper.getWeight()).isEqualTo(0.2);
        assertThat(newspaper.getDescription()).isEqualTo("Daily newspaper");
        assertThat(newspaper.getDimensions()).isEqualTo("30x42 cm");
        assertThat(newspaper.getQuantityInStock()).isEqualTo(100);
        assertThat(newspaper.getStatus()).isEqualTo("active");
        assertThat(newspaper.getPublisher()).isEqualTo("Tuoi Tre");
        assertThat(newspaper.getPublicationDate()).isEqualTo(LocalDate.of(2024, 1, 1));
        assertThat(newspaper.getLanguage()).isEqualTo("Vietnamese");
        assertThat(newspaper.getEditorInChief()).isEqualTo("Nguyen Van A");
        assertThat(newspaper.getIssueNumber()).isEqualTo("1234");
        assertThat(newspaper.getPublicationFrequency()).isEqualTo("Daily");
        assertThat(newspaper.getISSN()).isEqualTo("ISSN-001");
        assertThat(newspaper.getSections()).containsExactly("Politics", "Sports", "Economy");
    }

    // ================================================================
    // EP2 – barcode does NOT exist → ProductNotFoundException
    // ================================================================

    @Test
    @DisplayName("UT005 View Product With Nonexistent Barcode")
    void UT005_viewProductWithNonexistentBarcode() {
        // EP2: barcode not in repository
        // Input : barcode = 'UNKNOWN-999'
        // Expect: Throws ProductNotFoundException; message contains 'UNKNOWN-999'
        when(productRepository.findByBarcode("UNKNOWN-999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.viewProduct("UNKNOWN-999"))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("UNKNOWN-999");
    }

    // ================================================================
    // EP3 – barcode is null
    // ================================================================

    @Test
    @DisplayName("UT006 View Product With Null Barcode")
    void UT006_viewProductWithNullBarcode() {
        // EP3: null barcode – passed directly to repository
        // Input : barcode = null
        // Expect: Throws ProductNotFoundException (NOT NullPointerException)
        when(productRepository.findByBarcode(null)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.viewProduct(null))
                .isInstanceOf(ProductNotFoundException.class);
    }

    // ================================================================
    // EP4 / BVA – barcode is empty or whitespace
    // ================================================================

    @Test
    @DisplayName("UT007 View Product With Empty Barcode")
    void UT007_viewProductWithEmptyBarcode() {
        // BVA1: barcode length = 0 (empty string)
        // Input : barcode = ''
        // Expect: Throws ProductNotFoundException
        when(productRepository.findByBarcode("")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.viewProduct(""))
                .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    @DisplayName("UT008 View Product With Whitespace Barcode")
    void UT008_viewProductWithWhitespaceBarcode() {
        // BVA2: barcode is whitespace only – no trim in code, passed as-is
        // Input : barcode = '   ' (3 spaces)
        // Expect: Throws ProductNotFoundException
        when(productRepository.findByBarcode("   ")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.viewProduct("   "))
                .isInstanceOf(ProductNotFoundException.class);
    }
}