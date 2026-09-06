package dev.andreasgeorgatos.pointofservicebackend.dto;

import dev.andreasgeorgatos.pointofservicebackend.dto.product.ProductCreateRequest;
import dev.andreasgeorgatos.pointofservicebackend.enums.Category;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ProductCreateRequest validation")
class ProductCreateRequestTest {

    private static final String NAME = "Espresso";
    private static final String DESCRIPTION = "A double shot of arabica.";
    private static final String IMAGE = "https://cdn.example.com/espresso.png";
    private static final Category CATEGORY = Category.BEVERAGES;
    private static final BigDecimal PRICE = new BigDecimal("2.50");

    private static final int NAME_MAX = 120;
    private static final int DESCRIPTION_MAX = 1000;
    private static final int IMAGE_MAX = 500;
    private static final int INGREDIENTS_MAX = 50;

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void startValidator() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void closeValidator() {
        factory.close();
    }

    private Set<Long> ingredients(int count) {
        return LongStream.rangeClosed(1, count).boxed().collect(Collectors.toSet());
    }

    private ProductCreateRequest valid() {
        return new ProductCreateRequest(NAME, DESCRIPTION, IMAGE, ingredients(3), CATEGORY, PRICE);
    }

    private ProductCreateRequest withName(String name) {
        return new ProductCreateRequest(name, DESCRIPTION, IMAGE, ingredients(1), CATEGORY, PRICE);
    }

    private ProductCreateRequest withDescription(String description) {
        return new ProductCreateRequest(NAME, description, IMAGE, ingredients(1), CATEGORY, PRICE);
    }

    private ProductCreateRequest withImage(String image) {
        return new ProductCreateRequest(NAME, DESCRIPTION, image, ingredients(1), CATEGORY, PRICE);
    }

    private ProductCreateRequest withIngredients(Set<Long> ingredientIds) {
        return new ProductCreateRequest(NAME, DESCRIPTION, IMAGE, ingredientIds, CATEGORY, PRICE);
    }

    private ProductCreateRequest withPrice(BigDecimal price) {
        return new ProductCreateRequest(NAME, DESCRIPTION, IMAGE, ingredients(1), CATEGORY, price);
    }

    private Set<ConstraintViolation<ProductCreateRequest>> validate(ProductCreateRequest request) {
        return validator.validate(request);
    }

    private void assertViolatesOnly(ProductCreateRequest request, String property) {
        assertThat(validate(request)).isNotEmpty().extracting(violation -> violation.getPropertyPath().toString()).containsOnly(property);
    }

    private void assertAccepted(ProductCreateRequest request) {
        assertThat(validate(request)).isEmpty();
    }

    @Test
    @DisplayName("accepts a fully populated request")
    void validRequest_hasNoViolations() {
        assertAccepted(valid());
    }

    @Test
    @DisplayName("accepts a request sitting on every upper boundary at once")
    void requestAtEveryUpperBoundary_isAccepted() {
        assertAccepted(new ProductCreateRequest(
                "E".repeat(NAME_MAX),
                "d".repeat(DESCRIPTION_MAX),
                "i".repeat(IMAGE_MAX),
                ingredients(INGREDIENTS_MAX),
                CATEGORY,
                new BigDecimal("99999999.99")));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "   ", "\t", "\n"})
    @DisplayName("rejects a blank name")
    void blankName_isRejected(String name) {
        assertViolatesOnly(withName(name), "name");
    }

    @Test
    @DisplayName("rejects a null name")
    void nullName_isRejected() {
        assertViolatesOnly(withName(null), "name");
    }

    @Test
    @DisplayName("rejects a name shorter than two characters")
    void nameTooShort_isRejected() {
        assertViolatesOnly(withName("E"), "name");
    }

    @Test
    @DisplayName("accepts a name at the two character lower boundary")
    void nameAtMinLength_isAccepted() {
        assertAccepted(withName("Es"));
    }

    @Test
    @DisplayName("rejects a name longer than 120 characters")
    void nameTooLong_isRejected() {
        assertViolatesOnly(withName("E".repeat(NAME_MAX + 1)), "name");
    }

    @Test
    @DisplayName("accepts a name at the 120 character boundary")
    void nameAtMaxLength_isAccepted() {
        assertAccepted(withName("E".repeat(NAME_MAX)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "   ", "\t"})
    @DisplayName("rejects a blank description")
    void blankDescription_isRejected(String description) {
        assertViolatesOnly(withDescription(description), "description");
    }

    @Test
    @DisplayName("rejects a null description")
    void nullDescription_isRejected() {
        assertViolatesOnly(withDescription(null), "description");
    }

    @Test
    @DisplayName("rejects a description shorter than two characters")
    void descriptionTooShort_isRejected() {
        assertViolatesOnly(withDescription("d"), "description");
    }

    @Test
    @DisplayName("accepts a description at the two character lower boundary")
    void descriptionAtMinLength_isAccepted() {
        assertAccepted(withDescription("de"));
    }

    @Test
    @DisplayName("rejects a description longer than 1000 characters")
    void descriptionTooLong_isRejected() {
        assertViolatesOnly(withDescription("d".repeat(DESCRIPTION_MAX + 1)), "description");
    }

    @Test
    @DisplayName("accepts a description at the 1000 character boundary")
    void descriptionAtMaxLength_isAccepted() {
        assertAccepted(withDescription("d".repeat(DESCRIPTION_MAX)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "   ", "\t"})
    @DisplayName("rejects a blank image")
    void blankImage_isRejected(String image) {
        assertViolatesOnly(withImage(image), "image");
    }

    @Test
    @DisplayName("rejects a null image")
    void nullImage_isRejected() {
        assertViolatesOnly(withImage(null), "image");
    }

    @Test
    @DisplayName("rejects an image reference shorter than two characters")
    void imageTooShort_isRejected() {
        assertViolatesOnly(withImage("i"), "image");
    }

    @Test
    @DisplayName("rejects an image reference longer than 500 characters")
    void imageTooLong_isRejected() {
        assertViolatesOnly(withImage("i".repeat(IMAGE_MAX + 1)), "image");
    }

    @Test
    @DisplayName("accepts an image reference at the 500 character boundary")
    void imageAtMaxLength_isAccepted() {
        assertAccepted(withImage("i".repeat(IMAGE_MAX)));
    }

    @Test
    @DisplayName("rejects a product with no ingredients")
    void emptyIngredients_isRejected() {
        assertViolatesOnly(withIngredients(Set.of()), "ingredientIds");
    }

    @Test
    @DisplayName("rejects a null ingredient set")
    void nullIngredients_isRejected() {
        assertViolatesOnly(withIngredients(null), "ingredientIds");
    }

    @Test
    @DisplayName("accepts a product with a single ingredient")
    void singleIngredient_isAccepted() {
        assertAccepted(withIngredients(ingredients(1)));
    }

    @Test
    @DisplayName("accepts exactly 50 ingredients")
    void fiftyIngredients_isAccepted() {
        assertAccepted(withIngredients(ingredients(INGREDIENTS_MAX)));
    }

    @Test
    @DisplayName("rejects more than 50 ingredients with the custom message")
    void tooManyIngredients_isRejectedWithCustomMessage() {
        Set<ConstraintViolation<ProductCreateRequest>> violations = validate(withIngredients(ingredients(INGREDIENTS_MAX + 1)));

        assertThat(violations).singleElement().satisfies(violation -> {
            assertThat(violation.getPropertyPath()).hasToString("ingredientIds");
            assertThat(violation.getMessage()).isEqualTo("A product cannot have more than 50 ingredients.");
        });
    }

    @Test
    @DisplayName("rejects a null category")
    void nullCategory_isRejected() {
        assertViolatesOnly(new ProductCreateRequest(NAME, DESCRIPTION, IMAGE, ingredients(1), null, PRICE), "category");
    }

    @Test
    @DisplayName("rejects a null price")
    void nullPrice_isRejected() {
        assertViolatesOnly(withPrice(null), "price");
    }

    @ParameterizedTest
    @ValueSource(strings = {"0.00", "0", "-0.01", "-1.00"})
    @DisplayName("rejects a price that is not strictly positive")
    void nonPositivePrice_isRejected(String price) {
        assertViolatesOnly(withPrice(new BigDecimal(price)), "price");
    }

    @Test
    @DisplayName("accepts the smallest price above zero")
    void smallestPositivePrice_isAccepted() {
        assertAccepted(withPrice(new BigDecimal("0.01")));
    }

    @ParameterizedTest
    @ValueSource(strings = {"2.555", "2.5551", "0.001"})
    @DisplayName("rejects a price with more than two decimal places")
    void priceWithTooManyFractionDigits_isRejected(String price) {
        assertViolatesOnly(withPrice(new BigDecimal(price)), "price");
    }

    @Test
    @DisplayName("rejects a price with more than eight integer digits")
    void priceWithTooManyIntegerDigits_isRejected() {
        assertViolatesOnly(withPrice(new BigDecimal("123456789.00")), "price");
    }

    @Test
    @DisplayName("accepts a price at the eight integer digit boundary")
    void priceAtMaxIntegerDigits_isAccepted() {
        assertAccepted(withPrice(new BigDecimal("99999999.99")));
    }

    @Test
    @DisplayName("reports every invalid field at once")
    void multipleInvalidFields_areAllReported() {
        Set<ConstraintViolation<ProductCreateRequest>> violations = validate(new ProductCreateRequest("", "", "", Set.of(), null, null));

        assertThat(violations)
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("name", "description", "image", "ingredientIds", "category", "price");
    }
}
