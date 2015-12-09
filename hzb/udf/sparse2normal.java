package taobao.udf_test;

import java.util.ArrayList;
import java.util.List;

import com.aliyun.odps.udf.UDF;

/***
 * BASE UDF
 */
public final class sparse2normal extends UDF {
    /**
     * UDF Evaluate接口
     * 
     * UDF在记录层面上是一对一，字段上是一对一或多对一。 Evaluate方法在每条记录上被调用一次，输入为一个或多个字段，输出为一个字段
     */
    public String evaluate(String a) {
        // TODO: 请按需要修改参数和返回值，并在这里实现你自己的逻辑

        if (a == null ) {
            return "0";
        } 
        String c[]=new String[4096];
        for (String string : a.split(",")) {
  		  String wt[]=string.split(":");
  		  c[Integer.parseInt(wt[0])]=wt[1];  		 
  		}
        String d="";
        for(String t:c)
        { 
       	 if (t==null)
       		 d+="0";
         else 
             d+=String.format("%.2f", Double.parseDouble(t));
         d+=",";
        }
        return d.substring(0,d.length()-1);
    }
    
}
