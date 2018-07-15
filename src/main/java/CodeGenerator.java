import com.fmi110.common.utils.MybatisGeneratorUtil;
import com.fmi110.common.utils.PropertiesFileUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * 代码生成类
 * Created by ZhangShuzheng on 2017/1/10.
 */
public class CodeGenerator {

    // 根据命名规范，只修改此常量值即可
//    private static String              MODULE                = "zheng-pay";
//    private static String              DATABASE              = "zheng";
//    private static String              TABLE_PREFIX          = "pay_";
//    private static String              PACKAGE_NAME          = "com.zheng.pay";

    private static String MODULE       = PropertiesFileUtil.getInstance("codeGenerator")
                                                           .get("generator.jdbc.moduleName");
    private static String DATABASE     = PropertiesFileUtil.getInstance("codeGenerator")
                                                           .get("generator.jdbc.database");
    private static String TABLE_PREFIX = PropertiesFileUtil.getInstance("codeGenerator")
                                                           .get("generator.jdbc.tablePrefix");
    private static String PACKAGE_NAME = PropertiesFileUtil.getInstance("codeGenerator")
                                                           .get("generator.jdbc.packageName");

    private static String              JDBC_DRIVER           = PropertiesFileUtil.getInstance("codeGenerator")
                                                                                 .get("generator.jdbc.driver");
    private static String              JDBC_URL              = PropertiesFileUtil.getInstance("codeGenerator")
                                                                                 .get("generator.jdbc.url");
    private static String              JDBC_USERNAME         = PropertiesFileUtil.getInstance("codeGenerator")
                                                                                 .get("generator.jdbc.username");
    private static String              JDBC_PASSWORD         = PropertiesFileUtil.getInstance("codeGenerator")
                                                                                 .get("generator.jdbc.password");
    // 需要insert后返回主键的表配置，key:表名,value:主键名
    private static Map<String, String> LAST_INSERT_ID_TABLES = new HashMap<>();

    static {

    }

    /**
     * 自动代码生成
     *
     * @param args
     */
    public static void main(String[] args) throws Exception {
        MybatisGeneratorUtil.generator(JDBC_DRIVER, JDBC_URL, JDBC_USERNAME, JDBC_PASSWORD, MODULE, DATABASE, TABLE_PREFIX, PACKAGE_NAME, LAST_INSERT_ID_TABLES);
    }

}
