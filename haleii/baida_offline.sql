--odps sql 
--********************************************************************--
--author:haleii
--create time:2015-12-06 14:02:07
--********************************************************************--
--------------------------------------------------------------------------------
                      --找出与test id在同一类下的百搭商品--
--------------------------------------------------------------------------------
drop table if exists history_match_cat_haleii_offline;
create table if not exists history_match_cat_haleii_offline as
select b.cat_id as cat1,a.item_id
from (select distinct item_id from test_in_history_haleii_offline) a 
join tianchi_fm.dim_items b on a.item_id = b.item_id;

drop table if exists matchpairs_cat_haleii_offline;
create table if not exists matchpairs_cat_haleii_offline as
select a.cat_id as cat1,b.* 
from tianchi_fm.dim_items a
join matchpairs_Train b
on a.item_id = b.item1;

--获取百搭列表item1:item2:count
drop table if exists history_match_pair_haleii_offline;
create table if not exists history_match_pair_haleii_offline as
select cat1,item2,-count(catitem) as count from
(select a.cat1,b.item2,b.item1 as catitem
from history_match_cat_haleii_offline a
join matchpairs_cat_haleii_offline b
on a.cat1=b.cat1
) c
group by cat1,item2;


--对百搭表排序,item1,item2,count
drop table if exists matchpair_top200_haleii_offline;
create table matchpair_top200_haleii_offline as 
select item1,item2,count,row_num from
(select item1,item2,count,row_number() over (partition by item1 order by count) as row_num 
from (
select t2.item_id as item1,t1.item2,t1.count from history_match_pair_haleii_offline t1
join history_match_cat_haleii_offline t2
on t1.cat1 = t2.cat1
)a 
) t where row_num<=200;



--将百搭表与历史pair拼接
drop table if exists history_top200_haleii_offline;
create table history_top200_haleii_offline as 
select item1 as item_id, split_part(wm_concat(',',item2),',',1,200) as item_list
from history_match_top200_haleii_offline 
group by item1;
