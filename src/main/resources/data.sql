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
