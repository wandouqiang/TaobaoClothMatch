--------------------------------------------------------------------------------
                         --test in history32827--
--------------------------------------------------------------------------------
drop table if exists test_in_history_haleii;
create table if not exists test_in_history_haleii as
select a.*
from tianchi_fm.user_bought_history a
join tianchi_fm.test_items b
on a.item_id = b.item_id;

--------------------------------------------------------------------------------
                     --history_match_based_time_xzk--
--------------------------------------------------------------------------------
drop table if exists history_match_based_time_haleii;
create table if not exists history_match_based_time_haleii as
select a.user_id,a.item_id,b.item_id as match_item_id,abs(datediff(to_date(b.create_at,'yyyymmdd'),to_date(a.create_at,'yyyymmdd'),'dd')) as delta_day 
from test_in_history_haleii a 
join tianchi_fm.user_bought_history b
on a.user_id = b.user_id
where datediff(to_date(b.create_at,'yyyymmdd'),to_date(a.create_at,'yyyymmdd'),'dd') < 14
and datediff(to_date(b.create_at,'yyyymmdd'),to_date(a.create_at,'yyyymmdd'),'dd') >-6;


-----1119---haleii---
--------------------------------------------------------------------------------
------购买A商品,A-B搭配,相当于也购买了B商品,B的排序值略低
--------------------------------------------------------------------------------

drop table if exists history_matchpair_score_haleii;
create table if not exists history_matchpair_score_haleii as
select a.user_id,a.item_id,b.item2 as match_item_id, a.delta_day + (100-b.count)*0.01 as delta_day
from history_match_based_time_haleii a 
join matchpairs2 b 
on a.match_item_id = b.item1;

--------------------------------------------------------------------------------
-------history_match_score:user_id,item_id,match_item_id,delta_day-----------

drop table if exists history_match_score_haleii;
create table if not exists history_match_score_haleii as 
select * from (
select user_id,item_id,match_item_id,cast(delta_day as double) as delta_day from history_match_based_time_haleii
union all 
select * from history_matchpair_score_haleii
) a;
-------1119-end-----------------------------------------------------------------


---1118--haleii-------
--------------------------------------------------------------------------------
------去除同类的商品搭配
--------------------------------------------------------------------------------

---------history_match_based_time_cat:user_id,item_id,match_item_id,delta_day---
drop table if exists history_match_based_time_cat_haleii;
create table if not exists history_match_based_time_cat_haleii as
select  a.*,b.cat_id as match_cat 
from (
select  c.*,d.cat_id as cat 
from history_match_score_haleii c 
join tianchi_fm.dim_items d on c.item_id=d.item_id
)a 
join tianchi_fm.dim_items b on a.match_item_id=b.item_id;

drop table if exists history_match_based_time_filter_by_matchcat_haleii;
create table if not exists history_match_based_time_filter_by_matchcat_haleii as 
select * from history_match_based_time_cat_haleii where cat<>match_cat;

--1118-end----

--------------------------------------------------------------------------------
                     --history_match_based_num_xzk--
--去除item_id和match_item_id相同的项                     
--------------------------------------------------------------------------------
-------history_match_based_num_xzk:user_id,item_id,match_id,bought_num
drop table if exists history_match_based_num_haleii;
create table if not exists history_match_based_num_haleii as
select a.user_id,a.item_id,a.match_item_id,count(*) as bought_num 
from (
select * from history_match_based_time_filter_by_matchcat_haleii
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
drop table if exists history_match_based_boughttimes_haleii;
create table if not exists history_match_based_boughttimes_haleii as
select user_id,item_id, count(create_at) as bought_num
from test_in_history_haleii
group by item_id,user_id;

drop table if exists useritem_bought;
create table if not exists useritem_bought as
select concat(user_id,item_id) as useritem,bought_num
from history_match_based_boughttimes_haleii;


drop table if exists useritem_match_bought;
create table if not exists useritem_match_bought as
select concat(user_id,item_id) as useritem,item_id,match_item_id,bought_num
from history_match_based_num_haleii;
 
drop table if exists history_match_based_sales_temp_haleii;
create table if not exists  history_match_based_sales_temp_haleii as
select a.item_id,a.match_item_id,(a.bought_num - b.bought_num +1) as sales
from useritem_match_bought a
join useritem_bought b
on a.useritem = b.useritem;

--------------------------------------------------------------------------------
--同一用户购买商品对和相应得分,这里的得分是:同一用户-7~14天内,购买同一对商品次数
--(包括假设购买A商品同时购买与之搭配的B)
--------------------------------------------------------------------------------
---history_match_based_sales_xzk:item_id,match_item_id,score
drop table if exists history_match_based_sales_haleii;
create table  if not exists history_match_based_sales_haleii as
select item_id,match_item_id,sum(sales) as score
from history_match_based_sales_temp_haleii
group by item_id,match_item_id;

--------------------------------------------------------------------------------
                    --history_match_based_all_xzk--
--------------------------------------------------------------------------------
--history_match_based_all_xzk:item_id,match_item_id,score=(a.delta_day-b.score*0.5)
drop table if exists  history_match_based_all_haleii;
create table if not exists history_match_based_all_haleii as 
select item_id, match_item_id,min(score) as score 
from (
select a.item_id,a.match_item_id,(a.delta_day-b.score*0.5) as score 
from history_match_based_time_haleii a
join 
history_match_based_sales_haleii b
on a.item_id=b.item_id and a.match_item_id=b.match_item_id
) c 
group by item_id,match_item_id;
--------------------------------------------------------------------------------
                 --sorted_history_match_based_all_xzk--
--相同的item_id根据score排序
--------------------------------------------------------------------------------
--sorted_history_match_based_all_xzk:item1,item2,s
drop table if exists sorted_history_match_based_all_haleii;
create table sorted_history_match_based_all_haleii as 
select item_id,match_item_id,score from history_match_based_all_haleii 
distribute by item_id sort by item_id,score;


-------------1120-haleii---------
--百搭
drop table if exists matchpairs_cat_haleii;
create table if not exists matchpairs_cat_haleii as
select a.cat_id as cat1,b.* 
from tianchi_fm.dim_items a
join matchpairs2 b
on a.item_id = b.item1;

drop table if exists test_history_cat_haleii;
create table if not exists test_history_cat_haleii as
select distinct a.item_id,b.cat_id from test_in_history_haleii a
join tianchi_fm.dim_items b
on a.item_id = b.item_id;

---testid,matchid,cat_count sorted--------
drop table if exists test_cat_match_haleii;
create table if not exists test_cat_match_haleii as
select * from (
select testid,item2 as match_item_id,-(count(item1)+count) as cat_count 
from  (
select a.item_id as testid,a.cat_id as cat,b.item1,b.item2,b.count
from test_history_cat_haleii a 
join matchpairs_cat_haleii b
on a.cat_id = b.cat1
) c
group by testid,cat,item2,count
) d
distribute by testid sort by testid,cat_count;
-----------1120-end---------------------


----------------1124-------------
--白搭Top50
---------item_id,item_list top  ------
drop table if exists matchpair_top50;
create table if not exists matchpair_top50 as 
select testid as item_id, split_part(wm_concat(',',match_item_id),',',1,50) as item_list
from test_cat_match_haleii 
group by testid;

--------------------------------------------------------------------------------
                         --history_match_top200_xzk--
--历史数据item,item_list,top200
--------------------------------------------------------------------------------
drop table if exists history_match_top150;
create table history_match_top150 as 
select item_id, split_part(wm_concat(',',match_item_id),',',1,150) as item_list
from sorted_history_match_based_all_haleii
group by item_id;

--------------------------------------------------------------------------------
                         --history_match_top200_xzk--
--test_history_list_top150;历史搭配150
--matchpair_top50；百搭50
--------------------------------------------------------------------------------
drop table if exists history_match_top200_haleii;
create table if not exists history_match_top200_haleii as 
select a.item_id,concat(a.item_list,',',b.item_list) as item_list
from history_match_top150 a
join matchpair_top50 b
on a.item_id = b.item_id;

 
drop table if exists fm_submissions_history;
create table fm_submissions_history as 
select * from history_match_top200_haleii;
alter table fm_submissions_history set lifecycle 30;
select * from fm_submissions_history;
select count(*) from fm_submissions_history;
 
 