package dev.andreasgeorgatos.pointofservicebackend.dto;

import dev.andreasgeorgatos.pointofservicebackend.dto.product.ProductCreateRequest;
import dev.andreasgeorgatos.pointofservicebackend.enums.Category;
import dev.andreasgeorgatos.pointofservicebackend.models.items.Ingredient;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@DisplayName("ProductCreateRequest validation")
class ProductCreateRequestTest {

    private static final String NAME = "Espresso";
    private static final String DESCRIPTION = "A double shot of arabica.";
    private static final String IMAGE = "https://cdn.example.com/espresso.png";
    private static final Category CATEGORY = Category.values()[0];
    private static final BigDecimal PRICE = new BigDecimal("2.50");

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

    private Set<Ingredient> ingredients(int count) {
        return Stream.generate(() -> mock(Ingredient.class)).limit(count).collect(Collectors.toSet());
    }

    private ProductCreateRequest valid() {
        return new ProductCreateRequest(NAME, DESCRIPTION, IMAGE, ingredients(3), CATEGORY, PRICE);
    }

    private Set<ConstraintViolation<ProductCreateRequest>> validate(ProductCreateRequest request) {
        return validator.validate(request);
    }

    private void assertViolatesOnly(ProductCreateRequest request, String property) {
        assertThat(validate(request)).extracting(violation -> violation.getPropertyPath().toString()).containsOnly(property);
    }

    @Test
    @DisplayName("accepts a fully populated request")
    void validRequest_hasNoViolations() {
        assertThat(validate(valid())).isEmpty();
    }

    @Test
    @DisplayName("rejects a blank name")
    void blankName_isRejected() {
        assertViolatesOnly(new ProductCreateRequest("   ", DESCRIPTION, IMAGE, ingredients(1), CATEGORY, PRICE), "name");
    }

    @Test
    @DisplayName("rejects a null name")
    void nullName_isRejected() {
        assertViolatesOnly(new ProductCreateRequest(null, DESCRIPTION, IMAGE, ingredients(1), CATEGORY, PRICE), "name");
    }

    @Test
    @DisplayName("rejects a name shorter than two characters")
    void nameTooShort_isRejected() {
        assertViolatesOnly(new ProductCreateRequest("E", DESCRIPTION, IMAGE, ingredients(1), CATEGORY, PRICE), "name");
    }

    @Test
    @DisplayName("rejects a name longer than 120 characters")
    void nameTooLong_isRejected() {
        assertViolatesOnly(new ProductCreateRequest("E".repeat(121), DESCRIPTION, IMAGE, ingredients(1), CATEGORY, PRICE), "name");
    }

    @Test
    @DisplayName("accepts a name at the 120 character boundary")
    void nameAtMaxLength_isAccepted() {
        assertThat(validate(new ProductCreateRequest("E".repeat(120), DESCRIPTION, IMAGE, ingredients(1), CATEGORY, PRICE))).isEmpty();
    }

    @Test
    @DisplayName("rejects a blank description")
    void blankDescription_isRejected() {
        assertViolatesOnly(new ProductCreateRequest(NAME, "   ", IMAGE, ingredients(1), CATEGORY, PRICE), "description");
    }

    @Test
    @DisplayName("rejects a description longer than 1000 characters")
    void descriptionTooLong_isRejected() {
        assertViolatesOnly(new ProductCreateRequest(NAME, "d".repeat(1001), IMAGE, ingredients(1), CATEGORY, PRICE), "description");
    }

    @Test
    @DisplayName("rejects a blank image")
    void blankImage_isRejected() {
        assertViolatesOnly(new ProductCreateRequest(NAME, DESCRIPTION, "   ", ingredients(1), CATEGORY, PRICE), "image");
    }

    @Test
    @DisplayName("rejects an image reference longer than 500 characters")
    void imageTooLong_isRejected() {
        assertViolatesOnly(new ProductCreateRequest(NAME, DESCRIPTION, "i".repeat(501), ingredients(1), CATEGORY, PRICE), "image");
    }

    @Test
    @DisplayName("rejects a product with no ingredients")
    void emptyIngredients_isRejected() {
        assertViolatesOnly(new ProductCreateRequest(NAME, DESCRIPTION, IMAGE, Set.of(), CATEGORY, PRICE), "ingredients");
    }

    @Test
    @DisplayName("rejects a null ingredient set")
    void nullIngredients_isRejected() {
        assertViolatesOnly(new ProductCreateRequest(NAME, DESCRIPTION, IMAGE, null, CATEGORY, PRICE), "ingredients");
    }

    @Test
    @DisplayName("accepts exactly 50 ingredients")
    void fiftyIngredients_isAccepted() {
        assertThat(validate(new ProductCreateRequest(NAME, DESCRIPTION, IMAGE, ingredients(50), CATEGORY, PRICE))).isEmpty();
    }

    @Test
    @DisplayName("rejects more than 50 ingredients with the custom message")
    void tooManyIngredients_isRejectedWithCustomMessage() {
        Set<ConstraintViolation<ProductCreateRequest>> violations = validate(new ProductCreateRequest(NAME, DESCRIPTION, IMAGE, ingredients(51), CATEGORY, PRICE));

        assertThat(violations).singleElement().satisfies(violation -> {
            assertThat(violation.getPropertyPath()).hasToString("ingredients");
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
        assertViolatesOnly(new ProductCreateRequest(NAME, DESCRIPTION, IMAGE, ingredients(1), CATEGORY, null), "price");
    }

    @Test
    @DisplayName("rejects a free product")
    void zeroPrice_isRejected() {
        assertViolatesOnly(new ProductCreateRequest(NAME, DESCRIPTION, IMAGE, ingredients(1), CATEGORY, new BigDecimal("0.00")), "price");
    }

    @Test
    @DisplayName("rejects a negative price")
    void negativePrice_isRejected() {
        assertViolatesOnly(new ProductCreateRequest(NAME, DESCRIPTION, IMAGE, ingredients(1), CATEGORY, new BigDecimal("-0.01")), "price");
    }

    @Test
    @DisplayName("accepts the smallest price above zero")
    void smallestPositivePrice_isAccepted() {
        assertThat(validate(new ProductCreateRequest(NAME, DESCRIPTION, IMAGE, ingredients(1), CATEGORY, new BigDecimal("0.01")))).isEmpty();
    }

    @Test
    @DisplayName("rejects a price with more than two decimal places")
    void priceWithTooManyFractionDigits_isRejected() {
        assertViolatesOnly(new ProductCreateRequest(NAME, DESCRIPTION, IMAGE, ingredients(1), CATEGORY, new BigDecimal("2.555")), "price");
    }

    @Test
    @DisplayName("rejects a price with more than eight integer digits")
    void priceWithTooManyIntegerDigits_isRejected() {
        assertViolatesOnly(new ProductCreateRequest(NAME, DESCRIPTION, IMAGE, ingredients(1), CATEGORY, new BigDecimal("123456789.00")), "price");
    }

    @Test
    @DisplayName("reports every invalid field at once")
    void multipleInvalidFields_areAllReported() {
        Set<ConstraintViolation<ProductCreateRequest>> violations = validate(new ProductCreateRequest("", "", "", Set.of(), null, null));

        assertThat(violations).extracting(violation -> violation.getPropertyPath().toString()).contains("name", "description", "image", "ingredients", "category", "price");
    }
}