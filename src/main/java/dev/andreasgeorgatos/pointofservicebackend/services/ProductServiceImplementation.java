package dev.andreasgeorgatos.pointofservicebackend.services;

import dev.andreasgeorgatos.pointofservicebackend.dto.product.ProductCreateRequest;
import dev.andreasgeorgatos.pointofservicebackend.dto.product.ProductResponseDTO;
import dev.andreasgeorgatos.pointofservicebackend.models.items.Product;
import dev.andreasgeorgatos.pointofservicebackend.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductServiceImplementation implements ProductService {

    private final ProductRepository productRepository;

    public ProductServiceImplementation(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<ProductResponseDTO> getAllProducts() {
        return productRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Product getProductById(Long id) {
        Product product = productRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Product not found: " + id));

        return product;
    }

    @Override
    @Transactional
    public Product createProduct(ProductCreateRequest request) {
        if (productRepository.existsByName(request.name())) {
            throw new IllegalArgumentException("Product already exists: " + request.name());
        }

        Product product = new Product();

        product.setName(request.name());
        product.setDescription(request.description());
        product.setImage(request.image());
        product.setIngredients(request.ingredients());
        product.setCategory(request.category());
        product.setPrice(request.price());

        return productRepository.save(product);

    }

    @Override
    @Transactional
    public Product updateProduct(Long id, ProductCreateRequest request) {
        if (productRepository.findById(id).isEmpty()) {
            throw new IllegalArgumentException("Product doesn't exists: " + request.name());
        }

        Product product = productRepository.getReferenceById(id);

        product.setName(request.name());
        product.setCategory(request.category());
        product.setDescription(request.description());
        product.setIngredients(request.ingredients());
        product.setPrice(request.price());
        product.setImage(request.image());


        return product;
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        if (productRepository.findById(id).isEmpty()) {
            throw new IllegalArgumentException("Product doesn't exists: " + id);
        }

        productRepository.delete(productRepository.getReferenceById(id));
    }

    private ProductResponseDTO toResponse(Product product) {
        ProductResponseDTO productResponseDTO = new ProductResponseDTO(product.getId(), product.getName(), product.getPrice());

        return productResponseDTO;
    }
}
