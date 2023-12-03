CREATE CATALOG iceberg_catalog WITH (
  'type'='iceberg',
  'catalog-type'='hadoop',
  'warehouse'='file:/tmp/iceberg'
); 

USE CATALOG iceberg_catalog;

CREATE TEMPORARY TABLE customers_gen (
    id INT PRIMARY KEY NOT ENFORCED,
    name STRING,
    country STRING,
    postal_code STRING
) WITH (
 'connector' = 'faker',
 'number-of-rows' = '100',   
 'fields.id.expression' = '#{number.numberBetween ''1'',''10''}',
 'fields.name.expression' = '#{harry_potter.character}',
 'fields.country.expression' = '#{Address.country}',
 'fields.postal_code.expression' = '#{number.numberBetween ''100001'',''699999''}'
);

CREATE TABLE customers (
   id INT PRIMARY KEY NOT ENFORCED,
   name STRING,
   country STRING,
   postal_code STRING
 );

insert into customers select * from customers_gen;

CREATE TEMPORARY TABLE Orders (
    order_id INT,
    total INT,
    customer_id INT,
    proc_time AS PROCTIME()
) WITH (
 'connector' = 'datagen',
 'fields.order_id.kind' = 'random',
 'fields.order_id.max' = '100',
 'fields.order_id.min' = '1',
 'fields.total.kind' = 'random',
 'fields.total.max' = '1000',
 'fields.total.min' = '10',
 'fields.customer_id.kind' = 'random',
 'fields.customer_id.max' = '10',
 'fields.customer_id.min' = '1',
 'rows-per-second' = '1'
);

CREATE TABLE sales_country (
    country STRING,
    total INT,
    PRIMARY KEY (country) NOT ENFORCED
) WITH (
    'format-version'='2', 
    'write.upsert.enabled'='true'
);

SET 'execution.checkpointing.interval' = '10 s';

select * from sales_country /*+ OPTIONS('table.exec.resource.default-parallelism'='10','pipeline.max-parallelism' = '10') */;


INSERT INTO sales_country
SELECT c.country, SUM(o.total)
FROM Orders AS o
JOIN customers c
ON o.customer_id = c.id
GROUP BY c.country;