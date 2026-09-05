INSERT INTO permission (name) SELECT 'user:viewAll' WHERE NOT EXISTS (SELECT 1 FROM permission WHERE name = 'user:viewAll');
INSERT INTO permission (name) SELECT 'user:readAny' WHERE NOT EXISTS (SELECT 1 FROM permission WHERE name = 'user:readAny');
INSERT INTO permission (name) SELECT 'user:createUser' WHERE NOT EXISTS (SELECT 1 FROM permission WHERE name = 'user:createUser');
INSERT INTO permission (name) SELECT 'user:updateAnyUserEmail' WHERE NOT EXISTS (SELECT 1 FROM permission WHERE name = 'user:updateAnyUserEmail');
INSERT INTO permission (name) SELECT 'user:deleteAnyUser' WHERE NOT EXISTS (SELECT 1 FROM permission WHERE name = 'user:deleteAnyUser');
INSERT INTO permission (name) SELECT 'product:viewAll' WHERE NOT EXISTS (SELECT 1 FROM permission WHERE name = 'product:viewAll');
INSERT INTO permission (name) SELECT 'product:viewProduct' WHERE NOT EXISTS (SELECT 1 FROM permission WHERE name = 'product:viewProduct');
INSERT INTO permission (name) SELECT 'product:createProduct' WHERE NOT EXISTS (SELECT 1 FROM permission WHERE name = 'product:createProduct');
INSERT INTO permission (name) SELECT 'product:update' WHERE NOT EXISTS (SELECT 1 FROM permission WHERE name = 'product:update');
INSERT INTO permission (name) SELECT 'product:deleteProduct' WHERE NOT EXISTS (SELECT 1 FROM permission WHERE name = 'product:deleteProduct');

INSERT INTO role (name) SELECT 'ROLE_ADMIN' WHERE NOT EXISTS (SELECT 1 FROM role WHERE name = 'ROLE_ADMIN');
INSERT INTO role (name) SELECT 'ROLE_STAFF' WHERE NOT EXISTS (SELECT 1 FROM role WHERE name = 'ROLE_STAFF');
INSERT INTO role (name) SELECT 'ROLE_CUSTOMER' WHERE NOT EXISTS (SELECT 1 FROM role WHERE name = 'ROLE_CUSTOMER');

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM role r, permission p
WHERE r.name = 'ROLE_ADMIN'
  AND NOT EXISTS (SELECT 1 FROM role_permissions rp WHERE rp.role_id = r.id AND rp.permission_id = p.id);

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM role r, permission p
WHERE r.name = 'ROLE_STAFF'
  AND p.name IN ('product:viewAll', 'product:viewProduct', 'product:createProduct', 'product:update')
  AND NOT EXISTS (SELECT 1 FROM role_permissions rp WHERE rp.role_id = r.id AND rp.permission_id = p.id);

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM role r, permission p
WHERE r.name = 'ROLE_CUSTOMER'
  AND p.name IN ('product:viewAll', 'product:viewProduct')
  AND NOT EXISTS (SELECT 1 FROM role_permissions rp WHERE rp.role_id = r.id AND rp.permission_id = p.id);

INSERT INTO ingredient (name, description, cost_per_unit) SELECT 'Tomato', 'Fresh vine tomato', 0.40 WHERE NOT EXISTS (SELECT 1 FROM ingredient WHERE name = 'Tomato');
INSERT INTO ingredient (name, description, cost_per_unit) SELECT 'Basil', 'Fresh basil leaves', 0.25 WHERE NOT EXISTS (SELECT 1 FROM ingredient WHERE name = 'Basil');
INSERT INTO ingredient (name, description, cost_per_unit) SELECT 'Olive Oil', 'Extra virgin olive oil', 0.90 WHERE NOT EXISTS (SELECT 1 FROM ingredient WHERE name = 'Olive Oil');
INSERT INTO ingredient (name, description, cost_per_unit) SELECT 'Chicken Breast', 'Boneless skinless chicken breast', 3.20 WHERE NOT EXISTS (SELECT 1 FROM ingredient WHERE name = 'Chicken Breast');
INSERT INTO ingredient (name, description, cost_per_unit) SELECT 'Mozzarella Cheese', 'Fresh mozzarella', 1.85 WHERE NOT EXISTS (SELECT 1 FROM ingredient WHERE name = 'Mozzarella Cheese');
INSERT INTO ingredient (name, description, cost_per_unit) SELECT 'Butter', 'Unsalted dairy butter', 1.20 WHERE NOT EXISTS (SELECT 1 FROM ingredient WHERE name = 'Butter');
INSERT INTO ingredient (name, description, cost_per_unit) SELECT 'Wheat Flour', 'Plain wheat flour', 0.30 WHERE NOT EXISTS (SELECT 1 FROM ingredient WHERE name = 'Wheat Flour');
INSERT INTO ingredient (name, description, cost_per_unit) SELECT 'Egg', 'Free range hen egg', 0.35 WHERE NOT EXISTS (SELECT 1 FROM ingredient WHERE name = 'Egg');
INSERT INTO ingredient (name, description, cost_per_unit) SELECT 'Salmon Fillet', 'Atlantic salmon fillet', 5.60 WHERE NOT EXISTS (SELECT 1 FROM ingredient WHERE name = 'Salmon Fillet');
INSERT INTO ingredient (name, description, cost_per_unit) SELECT 'Prawns', 'Peeled king prawns', 4.50 WHERE NOT EXISTS (SELECT 1 FROM ingredient WHERE name = 'Prawns');
INSERT INTO ingredient (name, description, cost_per_unit) SELECT 'Peanut Butter', 'Smooth roasted peanut butter', 1.10 WHERE NOT EXISTS (SELECT 1 FROM ingredient WHERE name = 'Peanut Butter');
INSERT INTO ingredient (name, description, cost_per_unit) SELECT 'Walnuts', 'Shelled walnut halves', 2.40 WHERE NOT EXISTS (SELECT 1 FROM ingredient WHERE name = 'Walnuts');
INSERT INTO ingredient (name, description, cost_per_unit) SELECT 'Soy Sauce', 'Naturally brewed soy sauce', 0.45 WHERE NOT EXISTS (SELECT 1 FROM ingredient WHERE name = 'Soy Sauce');
INSERT INTO ingredient (name, description, cost_per_unit) SELECT 'Sesame Seeds', 'Toasted sesame seeds', 0.60 WHERE NOT EXISTS (SELECT 1 FROM ingredient WHERE name = 'Sesame Seeds');

INSERT INTO ingredient_allergens (ingredient_id, allergen)
SELECT i.id, 'MILK' FROM ingredient i
WHERE i.name IN ('Mozzarella Cheese', 'Butter')
  AND NOT EXISTS (SELECT 1 FROM ingredient_allergens ia WHERE ia.ingredient_id = i.id AND ia.allergen = 'MILK');

INSERT INTO ingredient_allergens (ingredient_id, allergen)
SELECT i.id, 'WHEAT' FROM ingredient i
WHERE i.name IN ('Wheat Flour', 'Soy Sauce')
  AND NOT EXISTS (SELECT 1 FROM ingredient_allergens ia WHERE ia.ingredient_id = i.id AND ia.allergen = 'WHEAT');

INSERT INTO ingredient_allergens (ingredient_id, allergen)
SELECT i.id, 'EGGS' FROM ingredient i
WHERE i.name = 'Egg'
  AND NOT EXISTS (SELECT 1 FROM ingredient_allergens ia WHERE ia.ingredient_id = i.id AND ia.allergen = 'EGGS');

INSERT INTO ingredient_allergens (ingredient_id, allergen)
SELECT i.id, 'FISH' FROM ingredient i
WHERE i.name = 'Salmon Fillet'
  AND NOT EXISTS (SELECT 1 FROM ingredient_allergens ia WHERE ia.ingredient_id = i.id AND ia.allergen = 'FISH');

INSERT INTO ingredient_allergens (ingredient_id, allergen)
SELECT i.id, 'CRUSTACEAN_SHELLFISH' FROM ingredient i
WHERE i.name = 'Prawns'
  AND NOT EXISTS (SELECT 1 FROM ingredient_allergens ia WHERE ia.ingredient_id = i.id AND ia.allergen = 'CRUSTACEAN_SHELLFISH');

INSERT INTO ingredient_allergens (ingredient_id, allergen)
SELECT i.id, 'PEANUTS' FROM ingredient i
WHERE i.name = 'Peanut Butter'
  AND NOT EXISTS (SELECT 1 FROM ingredient_allergens ia WHERE ia.ingredient_id = i.id AND ia.allergen = 'PEANUTS');

INSERT INTO ingredient_allergens (ingredient_id, allergen)
SELECT i.id, 'TREE_NUTS' FROM ingredient i
WHERE i.name = 'Walnuts'
  AND NOT EXISTS (SELECT 1 FROM ingredient_allergens ia WHERE ia.ingredient_id = i.id AND ia.allergen = 'TREE_NUTS');

INSERT INTO ingredient_allergens (ingredient_id, allergen)
SELECT i.id, 'SOYBEANS' FROM ingredient i
WHERE i.name = 'Soy Sauce'
  AND NOT EXISTS (SELECT 1 FROM ingredient_allergens ia WHERE ia.ingredient_id = i.id AND ia.allergen = 'SOYBEANS');

INSERT INTO ingredient_allergens (ingredient_id, allergen)
SELECT i.id, 'SESAME' FROM ingredient i
WHERE i.name = 'Sesame Seeds'
  AND NOT EXISTS (SELECT 1 FROM ingredient_allergens ia WHERE ia.ingredient_id = i.id AND ia.allergen = 'SESAME');
