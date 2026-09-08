package dev.andreasgeorgatos.pointofservicebackend.services.product;

import dev.andreasgeorgatos.pointofservicebackend.dto.product.ProductCreateRequest;
import dev.andreasgeorgatos.pointofservicebackend.dto.product.ProductResponseDTO;
import dev.andreasgeorgatos.pointofservicebackend.models.items.Product;

import java.util.List;

public interface ProductService {

    List<ProductResponseDTO> getAllProducts();

    Product getProductById(Long id);

    Product createProduct(ProductCreateRequest request);

    Product updateProduct(Long id, ProductCreateRequest request);

    void deleteProduct(Long id);

}
