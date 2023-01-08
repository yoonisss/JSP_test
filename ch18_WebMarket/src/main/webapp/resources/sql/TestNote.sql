use WebMarketDB;
select * from product;
select * from board;
show tables;

CREATE TABLE IF NOT EXISTS member2(
   id VARCHAR(20) NOT NULL,
   passwd  VARCHAR(20),
   name VARCHAR(30),    
   PRIMARY KEY (id)
);
INSERT INTO member2 VALUES('1', '1234', '이상용1');
INSERT INTO member2 VALUES('2', '1235', '이상용2');


select * from member;
select * from product;
select * from board;
select  count(*) from board;
select * from board ORDER BY num DESC;
select * from board where num = 5;

drop table board_images;
drop table board;
commit;
CREATE TABLE board (
       num int not null auto_increment,
       id varchar(10) not null,
       name varchar(10) not null,
       subject varchar(100) not null,
       content text not null,
       regist_day varchar(30),
       hit int,
       ip varchar(20),
       PRIMARY KEY (num)
)default CHARSET=utf8;

select * from board;
select * from board_images;
desc board;


CREATE TABLE board_images (
       Fnum int not null auto_increment,
       fileName varchar(50) not null,
       regist_day varchar(30),
	   num int,
       PRIMARY KEY (Fnum)
)default CHARSET=utf8;
commit;
ALTER TABLE board_images add CONSTRAINT file_fk FOREIGN KEY(num) 
REFERENCES board (num) ON DELETE CASCADE;

desc board_images;
select * from board_images;
select * from board;
delete from board where num=2;
select  count(*) from board;



