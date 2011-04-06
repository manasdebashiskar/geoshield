CREATE TABLE offerings
(
   id_off serial, 
   id_sur_fk integer NOT NULL, 
   name_off character varying(50) NOT NULL, 
    PRIMARY KEY (id_off), 
    FOREIGN KEY (id_sur_fk) REFERENCES services_urls (id_sur) ON UPDATE NO ACTION ON DELETE CASCADE, 
    UNIQUE (id_sur_fk, name_off)
);

CREATE TABLE offerings_permissions
(
   id_opr serial, 
   id_off_fk integer NOT NULL, 
   id_grp_fk integer NOT NULL, 
    PRIMARY KEY (id_opr), 
    FOREIGN KEY (id_off_fk) REFERENCES offerings (id_off) ON UPDATE NO ACTION ON DELETE CASCADE, 
    FOREIGN KEY (id_grp_fk) REFERENCES groups (id_grp) ON UPDATE NO ACTION ON DELETE CASCADE, 
    UNIQUE (id_off_fk, id_grp_fk)
);

INSERT INTO services VALUES (3,'SOS');
INSERT INTO requests VALUES (9, 3, 'getcapabilities');
INSERT INTO requests VALUES (10, 3, 'getobservation');
INSERT INTO requests VALUES (11, 3, 'describesensor');

