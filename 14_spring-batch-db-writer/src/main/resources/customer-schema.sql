CREATE TABLE public.customer (
	id int NOT NULL,
	firstName varchar(255) default NULL,
    lastName varchar(255) default NULL,
    birthdate varchar(255),
	CONSTRAINT customer_pk PRIMARY KEY (id)
);
