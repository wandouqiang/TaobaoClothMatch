package taobao.udf_test;

import com.aliyun.odps.udf.UDTF;
import com.aliyun.odps.udf.UDTFCollector;
import com.aliyun.odps.udf.annotation.Resolve;
import com.aliyun.odps.udf.UDFException;

/**
 * BASE UDTF
 */
@Resolve({"string->bigint,bigint"})
// TODO define input and output types, e.g., "string,string->string,bigint".
public class get_matchpairs extends UDTF {

    /**
     * UDTF Process接口
     *
     * 每条记录都会调用此接口。
     */
    public void process(Object[] args) throws UDFException {
        // TODO: 实现对每条记录的处理逻辑
    	String matchsets[] = ((String) args[0]).split(";");
    	int len=matchsets.length;
    	for (int i=0;i<len;i++)
    	{
    		String items1[]=matchsets[i].split(",");
    		for (int j=i+1;j<len;j++)
    		{
    			String items2[]=matchsets[j].split(",");
    			for (String item1:items1)
    			{
    				for (String item2:items2)
    				{
    					forward(Long.parseLong(item1), Long.parseLong(item2));
    				}
    			}
    			
    		}
    	}
    	
    }

    /**
     * UDTF Close接口
     *
     * 任务最后调用此接口，规格化所有数据并输出。forward方法用于输出结果
     */
    public void close() throws UDFException {
        // TODO: 实现终结逻辑
    }

}
