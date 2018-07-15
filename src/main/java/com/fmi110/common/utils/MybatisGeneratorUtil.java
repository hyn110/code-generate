package com.fmi110.common.utils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.velocity.VelocityContext;
import org.mybatis.generator.api.MyBatisGenerator;
import org.mybatis.generator.config.Configuration;
import org.mybatis.generator.config.xml.ConfigurationParser;
import org.mybatis.generator.internal.DefaultShellCallback;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.fmi110.common.utils.StringUtil.lineToHump;


/**
 * 代码生成类
 * Created by ZhangShuzheng on 2017/1/10.
 */
public class MybatisGeneratorUtil {

    // generatorConfig模板路径
    private static String generatorConfig_vm = "/template/generatorConfig.vm";
    // Service模板路径
    private static String service_vm         = "/template/Service.vm";
    // ServiceMock模板路径
    private static String serviceMock_vm     = "/template/ServiceMock.vm";
    // ServiceImpl模板路径
    private static String serviceImpl_vm     = "/template/ServiceImpl.vm";

    private static String controller_vm     = "/template/Controller.vm";

    /**
     * 根据模板生成generatorConfig.xml文件
     *
     * @param jdbcDriver   驱动路径
     * @param jdbcUrl      链接
     * @param jdbcUsername 帐号
     * @param jdbcPassword 密码
     * @param module       项目模块
     * @param database     数据库
     * @param tablePrefix  表前缀
     * @param packageName  包名
     */
    public static void generator(
            String jdbcDriver,
            String jdbcUrl,
            String jdbcUsername,
            String jdbcPassword,
            String module,
            String database,
            String tablePrefix,
            String packageName,
            Map<String, String> lastInsertIdTables) throws Exception {

        String os            = System.getProperty("os.name");
        String targetProject = module + "/" + module + "";
        String basePath = MybatisGeneratorUtil.class.getResource("/")
                                                    .getPath()
                                                    .replace("/target/classes/", "")
                                                    .replace(targetProject, "");
        if (os.toLowerCase()
              .startsWith("win") || os.toLowerCase()
                                      .startsWith("Win")) {
            generatorConfig_vm = MybatisGeneratorUtil.class.getResource(generatorConfig_vm)
                                                           .getPath()
                                                           .replaceFirst("/", "");
            service_vm = MybatisGeneratorUtil.class.getResource(service_vm)
                                                   .getPath()
                                                   .replaceFirst("/", "");
            serviceMock_vm = MybatisGeneratorUtil.class.getResource(serviceMock_vm)
                                                       .getPath()
                                                       .replaceFirst("/", "");
            serviceImpl_vm = MybatisGeneratorUtil.class.getResource(serviceImpl_vm)
                                                       .getPath()
                                                       .replaceFirst("/", "");
            basePath = basePath.replaceFirst("/", "");
        } else {
            generatorConfig_vm = MybatisGeneratorUtil.class.getResource(generatorConfig_vm)
                                                           .getPath();
            service_vm = MybatisGeneratorUtil.class.getResource(service_vm)
                                                   .getPath();
            serviceMock_vm = MybatisGeneratorUtil.class.getResource(serviceMock_vm)
                                                       .getPath();
            serviceImpl_vm = MybatisGeneratorUtil.class.getResource(serviceImpl_vm)
                                                       .getPath();
        }

        String generatorConfigXml = MybatisGeneratorUtil.class.getResource("/")
                                                              .getPath()
                                                              .replace("/target/classes/", "") +
                                    "/src/main/resources/generatorConfig.xml";
        targetProject = basePath + "/" + targetProject;

        if (!FileUtils.getFile(targetProject)
                      .exists()) {
            FileUtils.getFile(targetProject)
                     .mkdirs();
        }

        String sql = "SELECT table_name FROM INFORMATION_SCHEMA.TABLES WHERE table_schema = '" + database +
                     "' AND table_name LIKE '" + tablePrefix + "_%';";

        System.out.println("========== 开始生成generatorConfig.xml文件 ==========");
        List<Map<String, Object>> tables = new ArrayList<>();
        try {
            VelocityContext     context = new VelocityContext();
            Map<String, Object> table;

            // 查询定制前缀项目的所有表
            JdbcUtil  jdbcUtil = new JdbcUtil(jdbcDriver, jdbcUrl, jdbcUsername, jdbcPassword);
            List<Map> result   = jdbcUtil.selectByParams(sql, null);
            for (Map map : result) {
                System.out.println(map.get("TABLE_NAME"));
                table = new HashMap<>(2);
                table.put("table_name", map.get("TABLE_NAME"));
                table.put("model_name", lineToHump(ObjectUtils.toString(map.get("TABLE_NAME"))));
                tables.add(table);
            }
            jdbcUtil.release();

            String targetProjectSqlMap = basePath + "/" + module + "/" + module + "";

            if (!FileUtils.getFile(targetProjectSqlMap)
                          .exists()) {
                FileUtils.getFile(targetProjectSqlMap)
                         .mkdirs();
            }

            context.put("tables", tables);
            context.put("generator_javaModelGenerator_targetPackage", packageName + ".dao.model");
            context.put("generator_sqlMapGenerator_targetPackage", packageName + ".dao.mapper");
            context.put("generator_javaClientGenerator_targetPackage", packageName + ".dao.mapper");
            context.put("targetProject", targetProject);
            context.put("targetProject_sqlMap", targetProjectSqlMap);
            context.put("generator_jdbc_password", jdbcPassword);
            context.put("last_insert_id_tables", lastInsertIdTables);
            VelocityUtil.generate(generatorConfig_vm, generatorConfigXml, context);
            // 删除旧代码
            File dirModel =
                    new File(targetProject + "/src/main/java/" + packageName.replaceAll("\\.", "/") + "/dao/model");
            File dirMapper =
                    new File(targetProject + "/src/main/java/" + packageName.replaceAll("\\.", "/") + "/dao/mapper");

            deleteDir(dirModel);
            deleteDir(dirMapper);

            FileUtils.getFile(dirModel).mkdirs();
            FileUtils.getFile(dirMapper).mkdirs();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("========== 结束生成generatorConfig.xml文件 ==========");

        System.out.println("========== 开始运行MybatisGenerator ==========");
        List<String>         warnings         = new ArrayList<>();
        File                 configFile       = new File(generatorConfigXml);
        ConfigurationParser  cp               = new ConfigurationParser(warnings);
        Configuration        config           = cp.parseConfiguration(configFile);
        DefaultShellCallback callback         = new DefaultShellCallback(true);
        MyBatisGenerator     myBatisGenerator = new MyBatisGenerator(config, callback, warnings);
        myBatisGenerator.generate(null);
        for (String warning : warnings) {
            System.out.println(warning);
        }
        System.out.println("========== 结束运行MybatisGenerator ==========");

        System.out.println("========== 开始生成Service ==========");
        String ctime = new SimpleDateFormat("yyyy/M/d").format(new Date());

        String controllerPath =
                basePath + "/" + module + "/" + module + "" + "/src/main/java/" +
                packageName.replaceAll("\\.", "/") +
                "/controller";

        String servicePath =
                basePath + "/" + module + "/" + module + "" + "/src/main/java/" +
                packageName.replaceAll("\\.", "/") +
                "/service";
        String serviceImplPath = basePath + "/" + module + "/" + module + "" + "/src/main/java/" +
                                 packageName.replaceAll("\\.", "/") + "/service/impl";
        for (int i = 0; i < tables.size(); i++) {
            String model = lineToHump(ObjectUtils.toString(tables.get(i)
                                                                 .get("table_name")));
            String controller     = controllerPath + "/" + model + "Controller.java";
            String service     = servicePath + "/" + model + "Service.java";
            String serviceMock = servicePath + "/" + model + "ServiceMock.java";
            String serviceImpl = serviceImplPath + "/" + model + "ServiceImpl.java";

            // 生成controller
            File controllerFile = new File(controller);
            if (!controllerFile.exists()) {
                VelocityContext context = new VelocityContext();
                context.put("package_name", packageName);
                context.put("model", model);
                context.put("ctime", ctime);
                VelocityUtil.generate(controller_vm, controller, context);
                System.out.println(controller);
            }

            // 生成service
            File serviceFile = new File(service);
            if (!serviceFile.exists()) {
                VelocityContext context = new VelocityContext();
                context.put("package_name", packageName);
                context.put("model", model);
                context.put("ctime", ctime);
                VelocityUtil.generate(service_vm, service, context);
                System.out.println(service);
            }
            // 生成serviceMock
            File serviceMockFile = new File(serviceMock);
            if (!serviceMockFile.exists()) {
                VelocityContext context = new VelocityContext();
                context.put("package_name", packageName);
                context.put("model", model);
                context.put("ctime", ctime);
                VelocityUtil.generate(serviceMock_vm, serviceMock, context);
                System.out.println(serviceMock);
            }
            // 生成serviceImpl
            File serviceImplFile = new File(serviceImpl);
            if (!serviceImplFile.exists()) {
                VelocityContext context = new VelocityContext();
                context.put("package_name", packageName);
                context.put("model", model);
                context.put("mapper", StringUtil.toLowerCaseFirstOne(model));
                context.put("ctime", ctime);
                VelocityUtil.generate(serviceImpl_vm, serviceImpl, context);
                System.out.println(serviceImpl);
            }
        }
        System.out.println("========== 结束生成Service ==========");
    }

    // 递归删除非空文件夹
    public static void deleteDir(File dir) {
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            for (int i = 0; i < files.length; i++) {
                deleteDir(files[i]);
            }
        }
        dir.delete();
    }

}
