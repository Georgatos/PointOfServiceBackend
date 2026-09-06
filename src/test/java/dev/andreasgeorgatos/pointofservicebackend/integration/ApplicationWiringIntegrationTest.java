package dev.andreasgeorgatos.pointofservicebackend.integration;

import dev.andreasgeorgatos.pointofservicebackend.models.users.Permission;
import dev.andreasgeorgatos.pointofservicebackend.models.users.Role;
import dev.andreasgeorgatos.pointofservicebackend.repository.IngredientRepository;
import dev.andreasgeorgatos.pointofservicebackend.repository.RoleRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Application wiring and seed data")
class ApplicationWiringIntegrationTest extends AbstractIntegrationTest {

    private static final Set<String> ALL_PERMISSIONS = Set.of(
            "user:viewAll",
            "user:readAny",
            "user:createUser",
            "user:updateAnyUserEmail",
            "user:deleteAnyUser",
            "product:viewAll",
            "product:viewProduct",
            "product:createProduct",
            "product:update",
            "product:deleteProduct");

    @Autowired
    private DataSource dataSource;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private IngredientRepository ingredientRepository;

    private Set<String> permissionNamesOf(String roleName) {
        Role role = roleRepository.findByName(roleName).orElseThrow();

        return role.getPermissions().stream().map(Permission::getName).collect(Collectors.toSet());
    }

    @Test
    @DisplayName("runs against a real MySQL server, not an in-memory database")
    void datasourceIsMySql() throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            assertThat(connection.getMetaData().getDatabaseProductName()).isEqualTo("MySQL");
            assertThat(connection.getCatalog()).isEqualTo("pos_test");
        }
    }

    @Test
    @DisplayName("seeds the full permission vocabulary")
    void permissionVocabularyIsSeeded() {
        assertThat(permissionNamesOf("ROLE_ADMIN")).containsExactlyInAnyOrderElementsOf(ALL_PERMISSIONS);
    }

    @Test
    @DisplayName("grants staff the four product permissions")
    void staffRoleGrantsProductManagement() {
        assertThat(permissionNamesOf("ROLE_STAFF"))
                .containsExactlyInAnyOrder("product:viewAll", "product:viewProduct", "product:createProduct", "product:update");
    }

    @Test
    @DisplayName("grants customers read access to products and nothing else")
    void customerRoleGrantsReadOnlyProductAccess() {
        assertThat(permissionNamesOf("ROLE_CUSTOMER"))
                .containsExactlyInAnyOrder("product:viewAll", "product:viewProduct");
    }

    @Test
    @DisplayName("seeds the fourteen ingredients products can reference")
    void ingredientsAreSeeded() {
        assertThat(ingredientRepository.count()).isEqualTo(14);
    }

    @Test
    @DisplayName("resolves a role's permissions outside any transaction")
    void rolePermissionsAreReachableOutsideATransaction() {
        assertThat(permissionNamesOf("ROLE_CUSTOMER")).isNotEmpty();
    }
}
