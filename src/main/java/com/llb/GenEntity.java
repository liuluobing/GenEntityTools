package com.llb;

import org.apache.commons.lang.time.DateFormatUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;


/**
 * @author liuluobing
 * @description 实体类生成器
 */
public class GenEntity {

    public static void main(String[] args) {
        new GenEntity();
    }

    /**
     * 相对路径 Copy relative path
     */
    private String package_url = "com.llb.entity";
    /**
     * 类输出路径
     */
    private String outputPath = "E:\\idea-workspace\\GenEntityTools\\src\\main\\java\\com\\llb\\entity";
    /**
     * 表名
     */
    private String tableName = "web_business_house_property";
    /**
     * 数据库连接
     */
    private static final String DRIVER = "com.mysql.cj.jdbc.Driver";
    private static final String URL = "jdbc:mysql://192.168.10.200:3316/platform_foreign_website?useUnicode=true&characterEncoding=UTF8&useSSL=false";
    private static final String NAME = "dbadmin_leader";
    private static final String PASSWORD = "dtl123456@";

    /**
     * 常量
     */
    private static final String BIT = "bit";
    private static final String TINYINT = "tinyint";
    private static final String SMALLINT = "smallint";
    private static final String INT = "int";
    private static final String BIGINT = "bigint";
    private static final String FLOAT = "float";
    private static final String DECIMAL = "decimal";
    private static final String NUMERIC = "numeric";
    private static final String VARCHAR = "varchar";
    private static final String CHAR = "char";
    private static final String DATETIME = "datetime";
    private static final String TIMESTAMP = "timestamp";
    private static final String IMAGE = "image";
    private static final Integer TEN = 10;
    /**
     * 列名数组
     */
    private String[] columnNames;
    /**
     * 列名类型数组
     */
    private String[] colTypes;
    /**
     * 列名描述数组
     */
    private String[] colDecs;
    /**
     * 列名大小数组
     */
    private int[] colSizes;
    /**
     * 是否需要导入包java.util.*
     */
    private boolean f_util = false;
    /**
     * 是否需要导入包java.sql.*
     */
    private boolean f_sql = false;

    /**
     * 构造函数
     */
    public GenEntity() {
        //创建连接
        Connection con = null;
        //查要生成实体类的表
        try {
            try {
                Class.forName(DRIVER);
            } catch (ClassNotFoundException e1) {
                e1.printStackTrace();
            }
            con = DriverManager.getConnection(URL, NAME, PASSWORD);
            DatabaseMetaData dbmd = con.getMetaData();
            //直接取表字段
            ResultSet rs = dbmd.getColumns(null, "%", tableName, "%");
            System.out.println("表名：" + tableName + "\t\n表字段信息：");
            rs.last();
            int size = rs.getRow();
            rs.beforeFirst();
            columnNames = new String[size];
            colTypes = new String[size];
            colSizes = new int[size];
            colDecs = new String[size];
            int i = 0;
            while (rs.next()) {
                System.out.println(rs.getString("COLUMN_NAME") + "----" + rs.getString("REMARKS") + "----" + rs.getString("TYPE_NAME"));
                columnNames[i] = initcapCol(rs.getString("COLUMN_NAME"));
                colTypes[i] = rs.getString("TYPE_NAME");
                colDecs[i] = rs.getString("REMARKS");
                colSizes[i] = rs.getInt("COLUMN_SIZE");
                if (colTypes[i].equalsIgnoreCase("datetime")) {
                    f_util = true;
                }
                if (colTypes[i].equalsIgnoreCase("image") || colTypes[i].equalsIgnoreCase("text")) {
                    f_sql = true;
                }
                i++;
            }
            String content = parse(columnNames, colTypes, colSizes);

            try {
                File directory = new File("");

                outputPath += "\\" + initcap(tableName) + ".java";
                FileWriter fw = new FileWriter(outputPath);
                System.out.println("输出路径：" + outputPath);
                PrintWriter pw = new PrintWriter(fw);
                pw.println(content);
                pw.flush();
                pw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }


        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 生成实体类主体代码
     */
    private String parse(String[] colNames, String[] colTypes, int[] colSizes) {
        StringBuffer sb = new StringBuffer();
        //判断是否导入工具包
        sb.append("package " + package_url + ";\r\n");
        if (f_util) {
            sb.append("import java.util.Date;\r\n");
        }
        if (f_sql) {
            sb.append("import java.sql.*;\r\n");
        }
        sb.append("\r\n");
        //注释部分
        sb.append("   /**\r\n");
        sb.append("    * @author liuluobing\r\n");
        sb.append("    * @description " + tableName + " 实体类\r\n");
        sb.append("    * @date " + DateFormatUtils.format(new java.util.Date(), "yyyy-MM-dd") + " \r\n");
        sb.append("    */ \r\n");
        //实体部分
        sb.append("\r\npublic class " + initcap(tableName) + "{\r\n");
        //属性
        processAllAttrs(sb);
       /* //get set方法
        processAllMethod(sb);*/
        sb.append("}\r\n");
        return sb.toString();
    }

    /**
     * 生成所有属性
     */
    private void processAllAttrs(StringBuffer sb) {

        for (int i = 0; i < columnNames.length; i++) {
            sb.append("\t /**" + colDecs[i] + "**/\r\n");
            sb.append("\tprivate " + sqlType2JavaType(colTypes[i], colSizes[i]) + " " + columnNames[i] + ";\r\n");
        }

    }

    /**
     * 生成所有方法
     */
    private void processAllMethod(StringBuffer sb) {

        for (int i = 0; i < columnNames.length; i++) {
            sb.append("\r\n\tpublic void set" + initcap(columnNames[i]) + "(" + sqlType2JavaType(colTypes[i], colSizes[i]) + " " +
                    columnNames[i] + "){\r\n");
            sb.append("\t\tthis." + columnNames[i] + "=" + columnNames[i] + ";\r\n");
            sb.append("\t}\r\n");
            sb.append("\r\n\tpublic " + sqlType2JavaType(colTypes[i], colSizes[i]) + " get" + initcap(columnNames[i]) + "(){\r\n");
            sb.append("\t\treturn " + columnNames[i] + ";\r\n");
            sb.append("\t}\r\n");
        }

    }

    /**
     * 将输入字符串的首字母及下划线后的字母改成大写
     */
    private String initcap(String str) {

        String[] arr = str.split("_");
        String tempStr = "";
        if (arr.length > 0) {
            for (String st : arr) {
                char[] c = st.toCharArray();
                if (c[0] >= 'a' && c[0] <= 'z') {
                    c[0] = (char) (c[0] - 32);
                }
                tempStr += new String(c);
            }
        }

        return tempStr;
    }

    /**
     * 将输入字符串的下划线后的字母改成大写
     */
    private String initcapCol(String str) {
        String[] arr = str.split("_");
        String tempStr = "";
        if (arr.length > 1) {
            int i = 0;
            for (String st : arr) {
                if (i > 0) {
                    char[] c = st.toCharArray();
                    if (c[0] >= 'a' && c[0] <= 'z') {
                        c[0] = (char) (c[0] - 32);
                    }
                    tempStr += new String(c);
                } else {
                    tempStr += st;
                }
                i++;
            }
        } else {
            tempStr = str;
        }
        return tempStr;
    }

    /**
     * 获得列的数据类型
     */
    private String sqlType2JavaType(String sqlType, int typeSize) {

        if (BIT.equalsIgnoreCase(sqlType)) {
            return "Boolean";
        } else if (TINYINT.equalsIgnoreCase(sqlType)) {
            return "Byte";
        } else if (SMALLINT.equalsIgnoreCase(sqlType)) {
            return "Short";
        } else if (INT.equalsIgnoreCase(sqlType)) {
            if (typeSize >= TEN) {
                return "Integer";
            } else {
                return "Integer";
            }
        } else if (BIGINT.equalsIgnoreCase(sqlType)) {
            return "Long";
        } else if (FLOAT.equalsIgnoreCase(sqlType)) {
            return "Float";
        } else if (DECIMAL.equalsIgnoreCase(sqlType) || NUMERIC.equalsIgnoreCase(sqlType)
                || "real".equalsIgnoreCase(sqlType) || "money".equalsIgnoreCase(sqlType)
                || "smallmoney".equalsIgnoreCase(sqlType) || "double".equalsIgnoreCase(sqlType)) {
            return "Double";
        } else if (VARCHAR.equalsIgnoreCase(sqlType) || CHAR.equalsIgnoreCase(sqlType)
                || "nvarchar".equalsIgnoreCase(sqlType) || "nchar".equalsIgnoreCase(sqlType)
                || "text".equalsIgnoreCase(sqlType)) {
            return "String";
        } else if (DATETIME.equalsIgnoreCase(sqlType) || TIMESTAMP.equalsIgnoreCase(sqlType)) {
            return "Date";
        } else if (IMAGE.equalsIgnoreCase(sqlType)) {
            return "Blod";
        } else if (BIGINT.equalsIgnoreCase(sqlType)) {
            return "BigInteger";
        }

        return null;
    }


}
