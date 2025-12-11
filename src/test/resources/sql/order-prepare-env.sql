-- create cart
INSERT INTO carts (id)
VALUES (555_555);
-- insert items in the cart
INSERT INTO cart_item (cart_id, item_id, items_count, total_cost)
VALUES (555_555, 1, 1, (SELECT price * 1 FROM items WHERE id = 1)),
       (555_555, 2, 2, (SELECT price * 2 FROM items WHERE id = 2)),
       (555_555, 3, 3, (SELECT price * 3 FROM items WHERE id = 3)),
       (555_555, 4, 4, (SELECT price * 4 FROM items WHERE id = 4)),
       (555_555, 5, 5, (SELECT price * 5 FROM items WHERE id = 5));
