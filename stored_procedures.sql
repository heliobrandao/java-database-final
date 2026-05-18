USE inventory;

DROP PROCEDURE IF EXISTS GetMonthlySalesForEachStore;
DROP PROCEDURE IF EXISTS GetAggregateSalesForCompany;
DROP PROCEDURE IF EXISTS GetTopSellingProductsByCategory;
DROP PROCEDURE IF EXISTS GetTopSellingProductByStore;

DELIMITER $$

CREATE PROCEDURE GetMonthlySalesForEachStore(IN year_param INT, IN month_param INT)
BEGIN
    SELECT
        od.store_id,
        SUM(DISTINCT od.total_price) AS total_sales,
        MONTH(od.`date`) AS sale_month,
        YEAR(od.`date`) AS sale_year
    FROM order_details od
    WHERE YEAR(od.`date`) = year_param
      AND MONTH(od.`date`) = month_param
    GROUP BY od.store_id, MONTH(od.`date`), YEAR(od.`date`)
    ORDER BY total_sales DESC;
END$$

CREATE PROCEDURE GetAggregateSalesForCompany(IN year_param INT, IN month_param INT)
BEGIN
    SELECT
        SUM(DISTINCT od.total_price) AS total_sales,
        MONTH(od.`date`) AS sale_month,
        YEAR(od.`date`) AS sale_year
    FROM order_details od
    WHERE YEAR(od.`date`) = year_param
      AND MONTH(od.`date`) = month_param
    GROUP BY MONTH(od.`date`), YEAR(od.`date`);
END$$

CREATE PROCEDURE GetTopSellingProductsByCategory(IN target_month INT, IN target_year INT)
BEGIN
    WITH sales_by_product AS (
        SELECT
            p.category,
            p.name,
            SUM(oi.quantity) AS total_quantity_sold,
            SUM(oi.quantity * oi.price) AS total_sales
        FROM order_item oi
        JOIN order_details od ON od.id = oi.order_id
        JOIN product p ON p.id = oi.product_id
        WHERE MONTH(od.`date`) = target_month
          AND YEAR(od.`date`) = target_year
        GROUP BY p.category, p.name
    ),
    max_by_category AS (
        SELECT
            category,
            MAX(total_quantity_sold) AS max_qty
        FROM sales_by_product
        GROUP BY category
    )
    SELECT
        sbp.category,
        sbp.name,
        sbp.total_quantity_sold,
        sbp.total_sales
    FROM sales_by_product sbp
    JOIN max_by_category mbc
      ON mbc.category = sbp.category
     AND mbc.max_qty = sbp.total_quantity_sold
    ORDER BY sbp.category, sbp.name;
END$$

CREATE PROCEDURE GetTopSellingProductByStore(IN target_month INT, IN target_year INT)
BEGIN
    WITH sales_by_product_store AS (
        SELECT
            oi.product_id,
            p.name AS product_name,
            od.store_id,
            SUM(oi.quantity) AS total_quantity_sold,
            SUM(oi.quantity * oi.price) AS total_sales
        FROM order_item oi
        JOIN order_details od ON od.id = oi.order_id
        JOIN product p ON p.id = oi.product_id
        WHERE MONTH(od.`date`) = target_month
          AND YEAR(od.`date`) = target_year
        GROUP BY oi.product_id, p.name, od.store_id
    ),
    max_by_store AS (
        SELECT
            store_id,
            MAX(total_quantity_sold) AS max_qty
        FROM sales_by_product_store
        GROUP BY store_id
    )
    SELECT
        sbps.product_name,
        sbps.store_id,
        sbps.total_quantity_sold,
        sbps.total_sales
    FROM sales_by_product_store sbps
    JOIN max_by_store mbs
      ON mbs.store_id = sbps.store_id
     AND mbs.max_qty = sbps.total_quantity_sold
    ORDER BY sbps.store_id, sbps.product_name;
END$$

DELIMITER ;
