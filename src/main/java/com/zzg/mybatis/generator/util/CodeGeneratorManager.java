package com.zzg.mybatis.generator.util;

import com.google.common.base.CaseFormat;
import com.zzg.mybatis.generator.model.CodeGeneratorConfig;
import com.zzg.mybatis.generator.model.GeneratorConfig;
import com.zzg.mybatis.generator.plugins.ControllerGenerator;
import com.zzg.mybatis.generator.plugins.ServiceGenerator;
import freemarker.template.TemplateExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import freemarker.template.Configuration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

/**
 * 代码生成器基础项 (常量信息 & 通用方法)
 * Created by zhh on 2017/09/20.
 */
public class CodeGeneratorManager extends CodeGeneratorConfig {

    protected static final Logger logger = LoggerFactory.getLogger(CodeGeneratorManager.class);

    private static Configuration configuration = null;

    /**
     * 获取 Freemarker 模板环境配置
     *
     * @return
     */
    public Configuration getFreemarkerConfiguration() {
        if (configuration == null) {
            configuration = initFreemarkerConfiguration();
        }
        return configuration;
    }

    /**
     * 生成简单名称代码
     * eg:
     * genCode("gen_test_demo");  gen_test_demo ==> Demo
     *
     * @param tableNames 表名, 可以多表
     */
    public void genCodeWithSimpleName(String... tableNames) {
        genCodeByTableName(true, tableNames);
    }

    /**
     * 生成具体名称代码
     * eg:
     * genCode("gen_test_demo");  gen_test_demo ==> GenTestDemo
     *
     * @param tableNames 表名, 可以多表
     */
    public void genCodeWithDetailName(String... tableNames) {
        genCodeByTableName(false, tableNames);
    }

    /**
     * 生成自定义名称代码
     * eg:
     * genCode("gen_test_demo", "IDemo");  gen_test_demo ==> IDemo
     *
     * @param tableName 表名, 只能单表
     */
    public void genCodeWithCustomName(String tableName, String customModelName) {
        genCodeByTableName(tableName, customModelName, false);
    }

    /**
     * 下划线转成驼峰, 首字符为小写
     * eg: gen_test_demo ==> genTestDemo
     *
     * @param tableName 表名, eg: gen_test_demo
     * @return
     */
    protected String tableNameConvertLowerCamel(String tableName) {
        return CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, tableName.toLowerCase());
    }

    /**
     * 下划线转成驼峰, 首字符为大写
     * eg: gen_test_demo ==> GenTestDemo
     *
     * @param tableName 表名, eg: gen_test_demo
     * @return
     */
    protected String tableNameConvertUpperCamel(String tableName) {
        return CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, tableName.toLowerCase());
    }

    /**
     * 表名转成映射路径
     * eg: gen_test_demo ==> /gen/test/demo
     *
     * @param tableName 表名
     * @return
     */
    protected String tableNameConvertMappingPath(String tableName) {
        tableName = tableName.toLowerCase();
        return File.separator + (tableName.contains("_") ? tableName.replaceAll("_", File.separator) : tableName);
    }

    /**
     * ModelName转成映射路径
     * eg: Demo ==> /demo
     *
     * @param modelName
     * @return
     */
    protected String modelNameConvertMappingPath(String modelName) {
        String tableName = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, modelName);
        return tableNameConvertMappingPath(tableName);
    }

    /**
     * 获取表的区分字段
     *
     * @param tableName 表名, eg: gen_test_demo
     * @return 区分字段 eg: test
     */
    protected String getSign(String tableName) {
        return getTableNameSplit(tableName)[1];
    }

    /**
     * 获取默认 modelName
     *
     * @param tableName 表名
     * @return
     */
    protected String getDefModelName(String tableName) {
        String[] strs = getTableNameSplit(tableName);
        StringBuilder sb = new StringBuilder();
        for (int i = 2; i < strs.length; i++) {
            sb.append(StringUtils.toUpperCaseFirstOne(strs[i].toLowerCase()));
        }
        return sb.toString();
    }

    /**
     * 获取表名切割后的数组
     *
     * @param tableName 表名
     * @return
     */
    private String[] getTableNameSplit(String tableName) {
        String[] strs = tableName.split("_");
        if (!tableName.contains("_") || strs.length < 1) {
            throw new RuntimeException("表名格式不正确, 请按规定格式! 例如: gen_test_demo");
        }
        return strs;
    }

    /**
     * 通过数据库表名, 生成代码
     * 如表名为 gen_test_demo
     * 将生成  Demo & DemoMapper & DemoService & DemoServiceImpl & DemoController
     *
     * @param flag       标志
     * @param tableNames 表名数组
     */
    private void genCodeByTableName(boolean flag, String... tableNames) {
        for (String tableName : tableNames) {
            genCodeByTableName(tableName, null, flag);
        }
    }

    /**
     * 通过数据库表名, 和自定义 modelName 生成代码
     * 如表名为 gen_test_demo, 自定义 modelName 为 IDemo
     * 将生成  IDemo & IDemoMapper & IDemoService & IDemoServiceImpl & IDemoController
     *
     * @param tableName 表名
     * @param modelName 实体类名
     * @param flag      标志
     */
    private void genCodeByTableName(String tableName, String modelName, boolean flag) {
//        String sign = getSign(tableName);
        String sign = "test";
        if (flag) {
            modelName = getDefModelName(tableName);
        }
        new ServiceGenerator().genCode(tableName, modelName, sign);
        new ControllerGenerator().genCode(tableName, modelName, sign);
    }

    /**
     * Freemarker 模板环境配置
     *
     * @return
     * @throws IOException
     */
    private Configuration initFreemarkerConfiguration() {
        Configuration cfg = null;
        try {
            cfg = new Configuration(Configuration.VERSION_2_3_23);
            cfg.setDirectoryForTemplateLoading(new File(TEMPLATE_FILE_PATH));
            cfg.setDefaultEncoding("UTF-8");
            cfg.setTemplateExceptionHandler(TemplateExceptionHandler.IGNORE_HANDLER);
        } catch (IOException e) {
            throw new RuntimeException("Freemarker 模板环境初始化异常!", e);
        }
        return cfg;
    }


    /**
     * 包转成路径
     * eg: com.bigsea.sns ==> com/bigsea/sns
     *
     * @param packageName
     * @return
     */
    private static String packageConvertPath(String packageName) {
        return String.format("/%s/", packageName.contains(".") ? packageName.replaceAll("\\.", "/") : packageName);
    }

    /**
     * 初始化配置信息
     */
    public static void init(GeneratorConfig generatorConfig) {
        Properties prop = loadProperties();


        JAVA_PATH = generatorConfig.getProjectFolder();
        TEMPLATE_FILE_PATH = PROJECT_PATH + prop.getProperty("template.file.path");

//        BASE_PACKAGE = prop.getProperty("base.package");
        SERVICE_PACKAGE = generatorConfig.getServicePackage();
        SERVICE_IMPL_PACKAGE = generatorConfig.getServicePackage() + ".impl";
        CONTROLLER_PACKAGE = generatorConfig.getControllerPackage();

        SERVICE_INTERFACE_REFERENCE = prop.getProperty("service.interface.reference");

        PACKAGE_PATH_SERVICE = generatorConfig.getServiceProject() + packageConvertPath(SERVICE_PACKAGE);
        PACKAGE_PATH_SERVICE_IMPL =  generatorConfig.getServiceProject() + packageConvertPath(SERVICE_IMPL_PACKAGE);
        PACKAGE_PATH_CONTROLLER =  generatorConfig.getControllerProject() + packageConvertPath(CONTROLLER_PACKAGE);

        SERVICE_BASE_PACKAGE = getBasePackcge(generatorConfig.getServicePackage());
        CONTROLLER_BASE_PACKAGE = getBasePackcge(generatorConfig.getControllerPackage());

        AUTHOR = prop.getProperty("author");
        String dateFormat = "".equals(prop.getProperty("date-format")) ? "yyyy/MM/dd" : prop.getProperty("date-format");
        DATE = new SimpleDateFormat(dateFormat).format(new Date());
    }

    /**
     * 加载配置文件
     *
     * @return
     */
    private static Properties loadProperties() {
        Properties prop = null;
        try {
            prop = new Properties();
            InputStream in = CodeGeneratorManager.class.getClassLoader().getResourceAsStream("generatorConfig.properties");
            prop.load(in);
        } catch (Exception e) {
            throw new RuntimeException("加载配置文件异常!", e);
        }
        return prop;
    }

    private static String getBasePackcge(String basePackage){
        return org.apache.commons.lang3.StringUtils.substring(basePackage,0,basePackage.lastIndexOf("."));
    }

    public static void main(String[] args) {
        System.out.println(getBasePackcge("com.xcar.ad.push.service"));
    }

}
