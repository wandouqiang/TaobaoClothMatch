package taobao.TaoBao;

import com.aliyun.odps.data.Record;
import com.aliyun.odps.mapred.Mapper;

import java.io.IOException;
import java.math.BigInteger;

/**
 * Mapper模板。请用真实逻辑替换模板内容
 */
public class MyMapper implements Mapper {
    private Record item_id;
    private Record value;
   

    public void setup(TaskContext context) throws IOException {
    	item_id = context.createMapOutputKeyRecord();
    	value = context.createMapOutputValueRecord();
    }

    public void map(long recordNum, Record record, TaskContext context) throws IOException {
    	item_id.set("item", record.getString(0));
    	value.set("word", record.getString(1));
    	value.set("tfidf", record.getDouble(8));
        context.write(item_id, value);
    }

    public void cleanup(TaskContext context) throws IOException {

    }
}
/*
<!-- classes -->
<jobLauncher>taobao.TaoBao.JobLauncher</jobLauncher>
<mapper>taobao.TaoBao.MyMapper</mapper>
<reducer>taobao.TaoBao.MyReducer</reducer>
 
<!--task-->
<mapOutputKey>item:string</mapOutputKey>
<mapOutputValue>word:string,tfidf:double</mapOutputValue>
<!--
<partitionColumns>col1,col2</partitionColumns>
<outputKeySortColumns>col1,col2</outputKeySortColumns>
<outputKeySortOrders>ASC,DESC</outputKeySortOrders>
<outputGroupingColumns>col1,col2</outputGroupingColumns>
<numReduceTask>8</numReduceTask>
<memoryForMapTask>2048</memoryForMapTask>
<memoryForReduceTask>2048</memoryForReduceTask>
-->

<!-- tables -->
<inputTables>
	<table>
		<name>item_word_tfidf_haleii</name>
	</table>
</inputTables>
<outputTable>
	<name>item_tfidf_mod_haleii</name>
</outputTable>
*/
