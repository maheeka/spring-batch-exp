CREATE TABLE public.customer (
	id int NOT NULL,
	firstName varchar(255) default NULL,
    lastName varchar(255) default NULL,
    age int,
	CONSTRAINT customer_pk PRIMARY KEY (id)
);
