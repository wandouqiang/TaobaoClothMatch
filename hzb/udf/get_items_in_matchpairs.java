package taobao.udf_test;

import com.aliyun.odps.udf.UDTF;
import com.aliyun.odps.udf.UDTFCollector;
import com.aliyun.odps.udf.annotation.Resolve;
import com.aliyun.odps.udf.UDFException;

/**
 * BASE UDTF
 */
@Resolve({"bigint,bigint->bigint"})
// TODO define input and output types, e.g., "string,string->string,bigint".
public class get_items_in_matchpairs extends UDTF {

    /**
     * UDTF Process接口
     *
     * 每条记录都会调用此接口。
     */
    public void process(Object[] args) throws UDFException {
        // TODO: 实现对每条记录的处理逻辑
    	Long item1 = (Long) args[0];
    	Long item2 = (Long) args[1];
        forward(item1);
        forward(item2);
    	
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
