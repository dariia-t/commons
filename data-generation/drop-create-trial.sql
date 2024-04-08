DROP TRIGGER update_rent;
DROP TRIGGER set_initial_rent;
DROP TABLE Prop_Amenity_In_Lease;
DROP TABLE CryptoPayment;
DROP TABLE DebitPayment;
DROP TABLE CreditPayment;
DROP TABLE Payment;
DROP TABLE Lease;
DROP TABLE Visits;
DROP TABLE Roommate;
DROP TABLE Pet;
DROP TABLE Tenant;
DROP TABLE Visitor;
DROP TABLE Apart_Amenity;
DROP TABLE Prop_Amenity;
DROP TABLE Amenity;
DROP TABLE Apartment;
DROP TABLE Property;

CREATE TABLE Property (
  prop_id NUMBER(3) GENERATED ALWAYS AS IDENTITY,
  street VARCHAR(50) NOT NULL,
  city VARCHAR(30) NOT NULL,
  zip NUMBER(5) NOT NULL,
  year_built INTEGER NOT NULL CHECK (year_built >= 1000 AND year_built <= 9999),
  PRIMARY KEY (prop_id)
);

CREATE TABLE Apartment (
  apart_id NUMBER(5) GENERATED ALWAYS AS IDENTITY,
  ap_number INTEGER NOT NULL,
  bathrooms NUMBER(2) NOT NULL,
  bedrooms INTEGER,
  apartment_size NUMBER(7,2) NOT NULL,
  monthly_rent NUMBER(7,2) NOT NULL,
  security_deposit NUMBER(7,2) NOT NULL,
  property_id NUMBER(3) NOT NULL,
  PRIMARY KEY (apart_id),
  FOREIGN KEY (property_id) REFERENCES Property(prop_id)
);

CREATE TABLE Amenity (
  amen_id NUMBER(3) GENERATED ALWAYS AS IDENTITY,
  name VARCHAR(50) NOT NULL,
  PRIMARY KEY (amen_id)
);

CREATE TABLE Prop_Amenity (
  prop_id NUMBER(3) NOT NULL,
  amen_id NUMBER(3) NOT NULL,
  monthly_rate NUMBER(7,2),
  PRIMARY KEY (prop_id, amen_id), 
  FOREIGN KEY (amen_id) REFERENCES Amenity(amen_id),
  FOREIGN KEY (prop_id) REFERENCES Property(prop_id)
);

CREATE TABLE Apart_Amenity (
  apart_id NUMBER(5) NOT NULL,
  amen_id NUMBER(3) NOT NULL,
  PRIMARY KEY (apart_id, amen_id), 
  FOREIGN KEY (amen_id) REFERENCES Amenity(amen_id),
  FOREIGN KEY (apart_id) REFERENCES Apartment(apart_id)
);

CREATE TABLE Visitor (
  visitor_id NUMBER(5) GENERATED ALWAYS AS IDENTITY,
  first_name VARCHAR(40) NOT NULL,
  last_name VARCHAR(40) NOT NULL,
  phone_number CHAR(12) CHECK (REGEXP_LIKE(phone_number, '^\d{3}-\d{3}-\d{4}$')),
  email VARCHAR(100) CHECK (REGEXP_LIKE(email, '^.+@.+\..+$')),
  PRIMARY KEY (visitor_id)
);

CREATE TABLE Tenant (
  tenant_id NUMBER(5) NOT NULL,
  ssn CHAR(11) NOT NULL CHECK (REGEXP_LIKE(ssn, '^\d{3}-\d{2}-\d{4}$')),
  dob DATE NOT NULL,
  PRIMARY KEY (tenant_id),
  FOREIGN KEY (tenant_id) REFERENCES Visitor(visitor_id)
);

CREATE TABLE Roommate (
  roommate_id NUMBER(5) GENERATED ALWAYS AS IDENTITY,
  tenant_id NUMBER(5) NOT NULL,
  first_name VARCHAR(40) NOT NULL,
  last_name VARCHAR(40) NOT NULL,
  dob DATE NOT NULL,
  PRIMARY KEY (roommate_id),
  FOREIGN KEY (tenant_id) REFERENCES Tenant(tenant_id)
);

CREATE TABLE Pet (
  pet_id NUMBER(5) GENERATED ALWAYS AS IDENTITY,
  tenant_id NUMBER(5) NOT NULL,
  pet_name VARCHAR(50) NOT NULL,
  pet_type VARCHAR(50) NOT NULL,
  PRIMARY KEY (pet_id),
  FOREIGN KEY (tenant_id) REFERENCES Tenant(tenant_id)
);

CREATE TABLE Visits (
  visitor_id NUMBER(5) NOT NULL,
  apart_id NUMBER(5) NOT NULL,
  visit_date DATE NOT NULL,
  PRIMARY KEY (visitor_id, apart_id),
  FOREIGN KEY (visitor_id) REFERENCES Visitor(visitor_id),
  FOREIGN KEY (apart_id) REFERENCES Apartment(apart_id)
);

CREATE TABLE Lease (
  lease_id NUMBER(5) GENERATED ALWAYS AS IDENTITY,
  start_date DATE NOT NULL,
  end_date DATE NOT NULL,
  rent_amount NUMBER(7,2), 
  move_out_date DATE, 
  refund_amount NUMBER(7,2), 
  apart_id NUMBER(5) NOT NULL,
  tenant_id NUMBER(5) NOT NULL,
  PRIMARY KEY (lease_id),
  FOREIGN KEY (apart_id) REFERENCES Apartment(apart_id),
  FOREIGN KEY (tenant_id) REFERENCES Tenant(tenant_id),
  CONSTRAINT check_end_date_after_start CHECK (end_date > start_date)
);

CREATE TABLE Payment (
  payment_id NUMBER(5) GENERATED ALWAYS AS IDENTITY,
  lease_id NUMBER(5) NOT NULL,
  payment_date DATE NOT NULL,
  amount NUMBER(7,2) NOT NULL,
  PRIMARY KEY (payment_id),
  FOREIGN KEY (lease_id) REFERENCES Lease(lease_id)
);

CREATE TABLE CreditPayment (
  payment_id NUMBER(5) NOT NULL,
  card_number CHAR(16) NOT NULL,
  card_expiry_date DATE NOT NULL,
  cardholder_name VARCHAR(50) NOT NULL,
  PRIMARY KEY (payment_id),
  FOREIGN KEY (payment_id) REFERENCES Payment(payment_id)
);

CREATE TABLE DebitPayment (
  payment_id NUMBER(5) NOT NULL,
  card_number CHAR(16) NOT NULL,
  debit_expiry_date DATE NOT NULL,
  cardholder_name VARCHAR(50) NOT NULL,
  PRIMARY KEY (payment_id),
  FOREIGN KEY (payment_id) REFERENCES Payment(payment_id)
);

CREATE TABLE CryptoPayment (
  payment_id NUMBER(5) NOT NULL,
  wallet_address VARCHAR(50) NOT NULL,
  PRIMARY KEY (payment_id),
  FOREIGN KEY (payment_id) REFERENCES Payment(payment_id)
);

CREATE TABLE Prop_Amenity_In_Lease (
  lease_id NUMBER(5) NOT NULL,
  amen_id NUMBER(3) NOT NULL,
  PRIMARY KEY (lease_id, amen_id), 
  FOREIGN KEY (amen_id) REFERENCES Amenity(amen_id),
  FOREIGN KEY (lease_id) REFERENCES Lease(lease_id)
);

CREATE TRIGGER set_initial_rent
BEFORE INSERT ON Lease
FOR EACH ROW
DECLARE
    monthly_rent NUMBER(7,2);
BEGIN
    SELECT monthly_rent INTO monthly_rent
    FROM Apartment
    WHERE apart_id = :NEW.apart_id;

    :NEW.rent_amount := monthly_rent;
END set_initial_rent;
/

CREATE OR REPLACE TRIGGER update_rent
AFTER INSERT
ON Prop_Amenity_In_Lease
FOR EACH ROW
DECLARE
    amenity_price NUMBER;
BEGIN

    SELECT COALESCE(monthly_rate, 0) INTO amenity_price
    FROM prop_amenity
    WHERE amen_id = :NEW.amen_id
    ORDER BY prop_id DESC
    FETCH FIRST 1 ROW ONLY;

    UPDATE lease
    SET rent_amount = rent_amount + amenity_price
    WHERE lease_id = :NEW.lease_id;
END;
/

CREATE OR REPLACE TRIGGER update_rent_after_delete
AFTER DELETE
ON Prop_Amenity_In_Lease
FOR EACH ROW
DECLARE
    amenity_price NUMBER;
BEGIN
    SELECT COALESCE(monthly_rate, 0) INTO amenity_price
    FROM prop_amenity
    WHERE amen_id = :OLD.amen_id
    ORDER BY prop_id DESC
    FETCH FIRST 1 ROW ONLY;

    UPDATE lease
    SET rent_amount = rent_amount - amenity_price
    WHERE lease_id = :OLD.lease_id;
END;


