-- Altera uf de CHAR(2) para VARCHAR(2) para compatibilidade com JPA/Hibernate schema validation
ALTER TABLE core.tenants ALTER COLUMN uf TYPE VARCHAR(2) USING uf::VARCHAR(2);
