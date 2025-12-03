-- clean domain tables
DELETE
  FROM items
 WHERE TRUE;
-- reset sequences
ALTER SEQUENCE items_id_seq RESTART WITH 1;
