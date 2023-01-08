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