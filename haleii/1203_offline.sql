--odps sql 
--********************************************************************--
--author:haleii
--create time:2015-12-02 10:26:07
--********************************************************************--

--top150+matchpair50=0.075389

--------------------------------------------------------------------------------
                         --构造测试集，训练集，答案集--
--valAnswer_xzk:答案集item_id,item_list,已有
--valItem_xzk:测试集，item_id，已有
--matchpairs2_Train训练集：
--------------------------------------------------------------------------------
--drop table if exists matchpairs_Train;
create table if not exists matchpairs_Train as 
select item1,item2,count from (
select /* + mapjoin(a) */ b.*,(case when a.item>0 then 1 else 0 end) as flag  
from  valItem_xzk a
right outer join matchpairs2 b
on a.item = b.item1 or a.item = b.item2
) c
where flag <> 1;

--item对应的第一个分词
create table if not exists item_first_term as
select item_id,split_part(terms,",",1) as 1st
from tianchi_fm.dim_items;
--------------------------------------------------------------------------------
                         --test in history32827--
--------------------------------------------------------------------------------
drop table if exists test_in_history_haleii_offline;
create table if not exists test_in_history_haleii_offline as
select a.*
from tianchi_fm.user_bought_history a
join valItem_xzk b
on a.item_id = b.item;

--------------------------------------------------------------------------------
                     --history_match_time--
--------------------------------------------------------------------------------
drop table if exists history_match_based_time_haleii_offline;
create table if not exists history_match_based_time_haleii_offline as
select a.user_id,a.item_id,b.item_id as match_item_id,abs(datediff(to_date(b.create_at,'yyyymmdd'),to_date(a.create_at,'yyyymmdd'),'dd')) as delta_day 
from test_in_history_haleii_offline a 
join tianchi_fm.user_bought_history b
on a.user_id = b.user_id
where datediff(to_date(b.create_at,'yyyymmdd'),to_date(a.create_at,'yyyymmdd'),'dd') < 14
and datediff(to_date(b.create_at,'yyyymmdd'),to_date(a.create_at,'yyyymmdd'),'dd') >-6;


-----1119---haleii---
--------------------------------------------------------------------------------
------购买A商品,A-B搭配,相当于也购买了B商品,B的排序值略低
--------------------------------------------------------------------------------

--drop table if exists history_matchpair_score_haleii_offline;
create table if not exists history_matchpair_score_haleii_offline as
select a.user_id,a.item_id,b.item2 as match_item_id, a.delta_day + (100-b.count)*0.01 as delta_day
from history_match_based_time_haleii_offline a 
join matchpairs_Train b 
on a.match_item_id = b.item1;

--------------------------------------------------------------------------------
-------history_match_score:user_id,item_id,match_item_id,delta_day-----------

--drop table if exists history_match_score_haleii_offline;
create table if not exists history_match_score_haleii_offline as 
select * from (
select user_id,item_id,match_item_id,cast(delta_day as double) as delta_day 
from history_match_based_time_haleii_offline
union all 
select * from history_matchpair_score_haleii_offline
) a;
-------1119-end-----


---1118--haleii-------
--------------------------------------------------------------------------------
------去除同类的商品搭配
--------------------------------------------------------------------------------

---------history_match_based_time_cat:user_id,item_id,match_item_id,delta_day---
drop table if exists history_match_based_time_cat_haleii_offline;
create table if not exists history_match_based_time_cat_haleii_offline as
select  a.*,b.cat_id as match_cat 
from (
select  c.*,d.cat_id as cat 
from history_match_based_time_haleii_offline c 
join tianchi_fm.dim_items d on c.item_id=d.item_id
)a 
join tianchi_fm.dim_items b on a.match_item_id=b.item_id;

drop table if exists history_match_based_time_filter_by_matchcat_haleii_offline;
create table if not exists history_match_based_time_filter_by_matchcat_haleii_offline as 
select * from history_match_based_time_cat_haleii_offline where cat<>match_cat;

--1118-end----



--------------------------------------------------------------------------------
                     --history_match_based_num_xzk--
--去除item_id和match_item_id相同的项                     
--------------------------------------------------------------------------------
-------history_match_based_num_xzk:user_id,item_id,match_id,bought_num
drop table if exists history_match_based_num_haleii_offline;
create table if not exists history_match_based_num_haleii_offline as
select a.user_id,a.item_id,a.match_item_id,count(*) as bought_num 
from (
select * from history_match_based_time_filter_by_matchcat_haleii_offline
where item_id <> match_item_id
) a
group by a.user_id,a.item_id,a.match_item_id;


--------------------------------------------------------------------------------
---同一个用户购买同一件商品多次则视为一次
--------------------------------------------------------------------------------
--------------------------------------------------------------------------------
                     --history_match_boughttimes_xzk--
-- item_id and bought_num by same user
--------------------------------------------------------------------------------
drop table if exists history_match_based_boughttimes_haleii_offline;
create table if not exists history_match_based_boughttimes_haleii_offline as
select user_id,item_id, count(create_at) as bought_num
from test_in_history_haleii_offline
group by item_id,user_id;

drop table if exists useritem_bought_offline;
create table if not exists useritem_bought_offline as
select concat(user_id,item_id) as useritem,bought_num
from history_match_based_boughttimes_haleii_offline;


drop table if exists useritem_match_bought_offline;
create table if not exists useritem_match_bought_offline as
select concat(user_id,item_id) as useritem,item_id,match_item_id,bought_num
from history_match_based_num_haleii_offline;
 
drop table if exists history_match_based_sales_temp_haleii_offline;
create table if not exists  history_match_based_sales_temp_haleii_offline as
select a.item_id,a.match_item_id,(a.bought_num - b.bought_num +1) as sales
from useritem_match_bought_offline a
join useritem_bought_offline b
on a.useritem = b.useritem;

--------------------------------------------------------------------------------
--同一用户购买商品对和相应得分,这里的得分是:同一用户-7~14天内,购买同一对商品次数
--(包括假设购买A商品同时购买与之搭配的B)
--------------------------------------------------------------------------------
---history_match_based_sales_xzk:item_id,match_item_id,score
drop table if exists history_match_based_sales_haleii_offline;
create table  if not exists history_match_based_sales_haleii_offline as
select item_id,match_item_id,sum(sales) as score
from history_match_based_sales_temp_haleii_offline
group by item_id,match_item_id;

--------------------------------------------------------------------------------
                    --history_match_based_all_xzk--
--------------------------------------------------------------------------------
--history_match_based_all_xzk:item_id,match_item_id,score=(a.delta_day-b.score*0.5)
drop table if exists  history_match_based_all_haleii_offline;
create table if not exists history_match_based_all_haleii_offline as 
select item_id, match_item_id,min(score) as score 
from (
select a.item_id,a.match_item_id,(a.delta_day-b.score*0.5) as score 
from history_match_based_time_haleii_offline a
join 
history_match_based_sales_haleii_offline b
on a.item_id=b.item_id and a.match_item_id=b.match_item_id
) c 
group by item_id,match_item_id;
--------------------------------------------------------------------------------
                 --sorted_history_match_based_all_xzk--
--相同的item_id根据score排序
--------------------------------------------------------------------------------
--sorted_history_match_based_all_xzk:item1,item2,s
drop table if exists sorted_history_match_based_all_haleii_offline;
create table sorted_history_match_based_all_haleii_offline as 
select item_id,match_item_id,score from history_match_based_all_haleii_offline 
distribute by item_id sort by item_id,score;


-------------1120-haleii---------
--百搭
--drop table if exists matchpairs_cat_haleii_offline;
create table if not exists matchpairs_cat_haleii_offline as
select a.cat_id as cat1,b.* 
from tianchi_fm.dim_items a
join matchpairs_Train b
on a.item_id = b.item1;

--drop table if exists test_history_cat_haleii_offline;
create table if not exists test_history_cat_haleii_offline as
select distinct a.item_id,b.cat_id from test_in_history_haleii_offline a
join tianchi_fm.dim_items b
on a.item_id = b.item_id;

---testid,matchid,cat_count sorted--------
--drop table if exists test_cat_match_haleii_offline;
create table if not exists test_cat_match_haleii_offline as
select * from (
select testid,item2 as match_item_id,-(count(item1)+count) as cat_count 
from  (
select a.item_id as testid,a.cat_id as cat,b.item1,b.item2,b.count
from test_history_cat_haleii_offline a 
join matchpairs_cat_haleii_offline b
on a.cat_id = b.cat1
) c
group by testid,cat,item2,count
) d
distribute by testid sort by testid,cat_count;
-----------1120-end--生成百搭表-------------------
--------------------------------------------------------------------------------
                         --history_match_top200_xzk--
--test_history_list_top150;历史搭配150
--matchpair_top50；百搭50
---------------

-----------------------------------------------------------------
---------item_id,item_list top  ------
--drop table if exists matchpair_top50_offline;
create table if not exists matchpair_top50_offline as 
select testid as item_id, split_part(wm_concat(',',match_item_id),',',1,50) as item_list
from test_cat_match_haleii_offline 
group by testid;


--排序后的top200,此命令和上一条功能一样
--test_history_top200:titem,mitem,match_value,row_num
drop  table if exists test_history_top200_offline;
create table if not exists test_history_top200_offline as 
select item_id as titem,match_item_id as mitem,score as match_value,row_num 
from (
select item_id,match_item_id,score, row_number() over (partition by item_id order by score)  as row_num
from history_match_based_all_haleii_offline
) t 
where row_num<=200;

--------------1202-------------
--将第一个分词相同的提到第一位

--drop table if exists itempairs_1st_haleii_offline;
create table if not exists itempairs_1st_haleii_offline as 
select c.*,d.1st as match_1st from
(select a.*, b.1st as item_1st from history_match_based_all_haleii_offline a
join item_first_term b
on a.item_id = b.item_id
) c
join item_first_term d
on c.match_item_id = d.item_id;


--drop  table if exists test_history_1st_top200_offline;
create table if not exists test_history_1st_top200_offline as 
select item_id as titem,match_item_id as mitem,score as match_value,row_num 
from (
select item_id,match_item_id,(case when item_1st = match_1st then score-10000 else score end) as score, row_number() over (partition by item_id order by score)  as row_num
from itempairs_1st_haleii_offline
) t 
where row_num<=200;


--取前150，test_history_sim_sort_top200是增加了相似度排名的item_list
drop table if exists test_history_list_top200_offline;
create table if not exists test_history_list_top200_offline as
select titem as item_id, split_part(wm_concat(',',mitem),',',1,200) as item_list
from test_history_top200_offline 
group by titem;


drop table if exists history_match_top200_haleii_offline;
create table if not exists history_match_top200_haleii_offline as 
select a.item_id,concat(a.item_list,',',b.item_list) as item_list
from test_history_list_top150_offline a
join matchpair_top50_offline b
on a.item_id = b.item_id;

--------------------------------------------------------------------------------

drop table if exists fm_submissions_history_offline;
create table fm_submissions_history_offline as select * from history_match_top200_haleii_offline;

select * from fm_submissions_history_offline;
select count(*) from fm_submissions_history_offline;

--------------------------------------------------------------------------------
                           --compute score--
--------------------------------------------------------------------------------
select sum(score)/7643.0 from
(
select a.item_id,cast(mapscore(a.item_list,b.item_list) as double) as score 
from test_history_list_top200_offline a join valAnswer_xzk b on a.item_id = b.item_id
)c;