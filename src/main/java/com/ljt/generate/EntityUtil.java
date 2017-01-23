package com.ljt.generate;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 自动生成MyBatis的实体类、实体映射XML文件、Mapper、service和service实现
 *
 * @author LJT
 * @version v1.0
 */
public class EntityUtil {

	/**
	 ********************************** 使用前必读*******************
	 **
	 ** 如需生成个别表需配置 private Map<String,String> tables=new HashMap
	 * <String,String>(); key为表名 value为实体名
	 **
	 ***********************************************************
	 */
	/**
	 * 生成全局变量 无需设置
	 */
	private String tableName = null;
	/**
	 * 生成全局实体名变量 无需设置
	 */
	private String beanName = null;
	/**
	 * 生成全局mapper名变量 无需设置
	 */
	private String mapperName = null;
	/**
	 * 生成全局mapper名变量 无需设置
	 */
	private String serviceName = null;
	/**
	 * 生成全局主键类型 无需设置
	 */
	private String idType = null;
	/**
	 * 数据库连接对象
	 */
	private static Connection conn = null;

	public static void main(String[] args) {
		try {
			new EntityUtil().generate(new HashSet<String>());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static void init() throws Exception{
		if(conn!=null){
			conn.close();
		}
		Class.forName(Config.driverName);
		String url = "jdbc:mysql://"+Config.address+":"+Config.port+"/"+Config.database+"?useUnicode=true&amp;characterEncoding=utf8";
		conn = DriverManager.getConnection(url, Config.username, Config.password);
	}
	/**
	 * 获取所有的数据库
	 *
	 * @return
	 * @throws SQLException
	 */
	public static String getDatabases() {
		try {
			PreparedStatement prepareStatement = conn.prepareStatement("show databases");
			ResultSet executeQuery = prepareStatement.executeQuery();
			StringBuffer sb = new StringBuffer();
			while (executeQuery.next()) {
				sb.append(executeQuery.getString(1)).append(",");
			}
			return sb.substring(0, sb.length() - 1);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return "";
	}

	/**
	 * 获取所有的表
	 *
	 * @return
	 * @throws SQLException
	 */
	public static List<String> getTables() throws SQLException {
		PreparedStatement pstate = conn.prepareStatement("show tables");
		ResultSet results = pstate.executeQuery();
		List<String> set= new ArrayList<String>();
		while (results.next()) {
			String tableName = results.getString(1);
			set.add(tableName);
		}
		return set;
	}

	/**
	 * 将dao和实体转换为驼峰命名
	 * 
	 * @param table
	 */
	private void processTable(String table) {
		StringBuffer sb = new StringBuffer(table.length());
		String tableNew = table.toLowerCase();
		String[] tables = tableNew.split("_");
		String temp = null;
		for (int i = 0; i < tables.length; i++) {
			temp = tables[i].trim();
			sb.append(temp.substring(0, 1).toUpperCase()).append(temp.substring(1));
		}
		beanName = sb.toString();
		mapperName = beanName + "Mapper";
		serviceName = beanName + "Service";
	}

	/**
	 * 驼峰命名转换
	 * 
	 * @param field
	 * @return
	 */
	private String toCamelCase(String field) {
		StringBuffer sb = new StringBuffer(field.length());
		String[] fields = field.split("_");
		String temp = null;
		sb.append(fields[0]);
		for (int i = 1; i < fields.length; i++) {
			temp = fields[i].trim();
			sb.append(temp.substring(0, 1).toUpperCase()).append(temp.substring(1));
		}
		return sb.toString();
	}

	/**
	 * 数据库字段类型转java类型
	 * 
	 * @param type
	 * @return
	 */
	private String processType(String type) {
		if (type.indexOf(Config.type_char) > -1) {
			return "String";
		} else if (type.indexOf(Config.type_bigint) > -1) {
			return "Long";
		} else if (type.indexOf(Config.type_int) > -1) {
			return "Integer";
		} else if (type.indexOf(Config.type_tinyint) > -1) {
			return "int";
		} else if (type.indexOf(Config.type_date) > -1) {
			return "java.util.Date";
		} else if (type.indexOf(Config.type_text) > -1) {
			return "String";
		} else if (type.indexOf(Config.type_timestamp) > -1) {
			return "java.util.Date";
		} else if (type.indexOf(Config.type_bit) > -1) {
			return "Boolean";
		} else if (type.indexOf(Config.type_decimal) > -1) {
			return "java.math.BigDecimal";
		} else if (type.indexOf(Config.type_blob) > -1) {
			return "byte[]";
		}
		return "String";
	}

	private String processTypeUpperCase(String type) {
		int indexOf = type.indexOf("(") != -1 ? type.indexOf("(") : type.length();
		return type.substring(0, indexOf).toUpperCase();
	}

	/**
	 * 转换数据库类型为大写
	 *
	 * @param beanName
	 * @return
	 */
	private String processResultMapId(String beanName) {
		return beanName.substring(0, 1).toLowerCase() + beanName.substring(1);
	}

	/**
	 * 方法上面的注释
	 *
	 * @param bw
	 * @param text
	 * @return
	 * @throws IOException
	 */
	private void buildMethodComment(BufferedWriter bw, String text) throws IOException {
		bw.newLine();
		bw.write("\t/**");
		bw.newLine();
		bw.write("\t * ");
		bw.newLine();
		bw.write("\t * " + text);
		bw.newLine();
		bw.write("\t * ");
		bw.newLine();
		bw.write("\t */");
	}

	private void buildCommont1(BufferedWriter bw, String text) throws IOException {
		bw.newLine();
		bw.write("\t/**");
		bw.newLine();
		bw.write("\t * " + text);
		bw.newLine();
		bw.write("\t */");
		bw.newLine();
	}

	private void buildServiceImpl() throws IOException {
		String pathname = Config.file_path + "/" + Config.service_impl_package.replaceAll("\\.", "/");
		File folder = new File(pathname);
		if (!folder.exists()) {
			folder.mkdirs();
		}
		File mapperFile = new File(pathname, serviceName + "Impl" + ".java");
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(mapperFile), "utf-8"));
		bw.write("package " + Config.service_impl_package + ";");
		bw.newLine();
		bw.newLine();
		bw.write("import " + Config.bean_package + "." + beanName + ";");
		bw.newLine();
		bw.write("import " + Config.mapper_package + "." + mapperName + ";");
		bw.newLine();
		bw.write("import java.util.List;");
		bw.newLine();
		bw.write("import org.springframework.beans.factory.annotation.Autowired;");
		bw.newLine();
		bw.write("import "+Config.service_package+"."+serviceName+";");
		bw.newLine();
		bw.write("import org.springframework.stereotype.Service;");
		buildMethodComment(bw, serviceName + "Impl" + "实现");
		bw.newLine();
		bw.write("@Service");
		bw.newLine();
		bw.write("public class " + serviceName + "Impl implements "+serviceName + "{");
		bw.newLine();
		bw.newLine();
		String str = mapperName.substring(0, 1).toLowerCase() + mapperName.substring(1);
		bw.write("\t@Autowired");
		bw.newLine();
		bw.write("\tprivate " + mapperName + " " + str + ";");
		bw.newLine();
		// ----------定义Mapper中的方法Begin----------
		buildMethodComment(bw, "查询（根据主键ID查询）");
		bw.newLine();
		bw.write("\t@Override");
		bw.newLine();
		bw.write("\tpublic " + beanName + "  selectByPrimaryKey (" + idType + " id){");
		bw.newLine();
		bw.write("\t\treturn " + str + ".selectByPrimaryKey(id);");
		bw.newLine();
		bw.write("\t}");
		bw.newLine();
		buildMethodComment(bw, "删除（根据主键ID删除）");
		bw.newLine();
		bw.write("\t@Override");
		bw.newLine();
		bw.write("\tpublic " + "int deleteByPrimaryKey (" + idType + " id){");
		bw.newLine();
		bw.write("\t\treturn " + str + ".deleteByPrimaryKey(id);");
		bw.newLine();
		bw.write("\t}");
		bw.newLine();
		buildMethodComment(bw, "添加");
		bw.newLine();
		bw.write("\t@Override");
		bw.newLine();
		bw.write("\tpublic " + "int insert(" + beanName + " record){");
		bw.newLine();
		bw.write("\t\treturn " + str + ".insert(record);");
		bw.newLine();
		bw.write("\t}");
		bw.newLine();
		buildMethodComment(bw, "添加 （匹配有值的字段）");
		bw.newLine();
		bw.write("\t@Override");
		bw.newLine();
		bw.write("\tpublic " + "int insertSelective(" + beanName + " record){");
		bw.newLine();
		bw.write("\t\treturn " + str + ".insertSelective(record);");
		bw.newLine();
		bw.write("\t}");
		buildMethodComment(bw, "修改 （匹配有值的字段）");
		bw.newLine();
		bw.write("\t@Override");
		bw.newLine();
		bw.write("\tpublic " + "int updateByPrimaryKeySelective(" + beanName + " record){");
		bw.newLine();
		bw.write("\t\treturn " + str + ".updateByPrimaryKeySelective(record);");
		bw.newLine();
		bw.write("\t}");
		buildMethodComment(bw, "修改（根据主键ID修改）");
		bw.newLine();
		bw.write("\t@Override");
		bw.newLine();
		bw.write("\tpublic " + "int updateByPrimaryKey (" + beanName + " record){");
		bw.newLine();
		bw.write("\t\treturn " + str + ".updateByPrimaryKey(record);");
		bw.newLine();
		bw.write("\t}");
		buildMethodComment(bw, "查询（匹配有值的字段）");
		bw.newLine();
		bw.write("\t@Override");
		bw.newLine();
		bw.write("\tpublic " + "List<" + beanName + "> selectByObject (" + beanName + " record){");
		bw.newLine();
		bw.write("\t\treturn " + str + ".selectByObject(record);");
		bw.newLine();
		bw.write("\t}");
		// ----------定义Mapper中的方法End----------
		bw.newLine();
		bw.write("}");
		bw.flush();
		bw.close();
	}

	private void buildServiceOrMappers(String flieName, String packages) throws IOException {
		String pathname = Config.file_path + "/" + packages.replaceAll("\\.", "/");
		File folder = new File(pathname);
		if (!folder.exists()) {
			folder.mkdirs();
		}
		File mapperFile = new File(pathname, flieName + ".java");
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(mapperFile), "utf-8"));
		bw.write("package " + packages + ";");
		bw.newLine();
		bw.newLine();
		bw.write("import " + Config.bean_package + "." + beanName + ";");
		bw.newLine();
		bw.write("import java.util.List;");
		buildMethodComment(bw, flieName + "接口");
		bw.newLine();
		bw.write("public interface " + flieName + "{");
		bw.newLine();
		bw.newLine();
		// ----------定义Mapper中的方法Begin----------
		buildMethodComment(bw, "查询（根据主键ID查询）");
		bw.newLine();
		bw.write("\tpublic " + beanName + "  selectByPrimaryKey (" + idType + " id);");
		bw.newLine();
		buildMethodComment(bw, "查询（匹配有值的字段）");
		bw.newLine();
		bw.write("\tpublic " + "List<" + beanName + "> selectByObject (" + beanName + " record);");
		bw.newLine();
		buildMethodComment(bw, "删除（根据主键ID删除）");
		bw.newLine();
		bw.write("\tpublic " + "int deleteByPrimaryKey (" + idType + " id);");
		bw.newLine();
		buildMethodComment(bw, "添加");
		bw.newLine();
		bw.write("\tpublic " + "int insert(" + beanName + " record);");
		bw.newLine();
		buildMethodComment(bw, "添加 （匹配有值的字段）");
		bw.newLine();
		bw.write("\tpublic " + "int insertSelective(" + beanName + " record);");
		bw.newLine();
		buildMethodComment(bw, "修改 （匹配有值的字段）");
		bw.newLine();
		bw.write("\tpublic " + "int updateByPrimaryKeySelective(" + beanName + " record);");
		bw.newLine();
		buildMethodComment(bw, "修改（根据主键ID修改）");
		bw.newLine();
		bw.write("\tpublic " + "int updateByPrimaryKey (" + beanName + " record);");
		bw.newLine();
		// ----------定义Mapper中的方法End----------
		bw.newLine();
		bw.write("}");
		bw.flush();
		bw.close();
	}

	/**
	 * 生成实体类
	 *
	 * @param columns
	 * @param types
	 * @param comments
	 * @throws IOException
	 */
	private void buildEntityBean(List<String> columns, List<String> types, List<String> comments, String tableComment)
			throws Exception {
		String pathname = Config.file_path + "/" + Config.bean_package.replaceAll("\\.", "/");
		File folder = new File(pathname);
		if (!folder.exists()) {
			folder.mkdirs();
		}
		File beanFile = new File(pathname, beanName + ".java");
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(beanFile)));
		bw.write("package " + Config.bean_package + ";");
		bw.newLine();
		bw.write("import java.io.Serializable;");
		bw.newLine();
		buildMethodComment(bw, tableComment);
		bw.newLine();
		bw.write("public class " + beanName + " implements Serializable {");
		bw.newLine();
		bw.newLine();
		int size = columns.size();
		for (int i = 0; i < size; i++) {
			buildCommont1(bw, comments.get(i));
			bw.write("\tprivate " + processType(types.get(i)) + " " + toCamelCase(columns.get(i)) + ";");
			bw.newLine();
		}
		bw.newLine();
		// 生成get 和 set方法
		String tempField = null;
		String _tempField = null;
		String tempType = null;
		for (int i = 0; i < size; i++) {
			tempType = processType(types.get(i));
			_tempField = toCamelCase(columns.get(i));
			tempField = _tempField.substring(0, 1).toUpperCase() + _tempField.substring(1);
			buildCommont1(bw, "set" + comments.get(i));
			bw.write("\tpublic void set" + tempField + "(" + tempType + " " + _tempField + "){");
			bw.newLine();
			bw.write("\t\tthis." + _tempField + " = " + _tempField + ";");
			bw.newLine();
			bw.write("\t}");
			bw.newLine();
			buildCommont1(bw, "get" + comments.get(i));
			bw.write("\tpublic " + tempType + " get" + tempField + "(){");
			bw.newLine();
			bw.write("\t\treturn this." + _tempField + ";");
			bw.newLine();
			bw.write("\t}");
			bw.newLine();
		}
		bw.newLine();
		bw.write("}");
		bw.newLine();
		bw.flush();
		bw.close();
	}

	/**
	 * 构建实体类映射XML文件
	 *
	 * @param columns
	 * @param types
	 * @param comments
	 * @throws IOException
	 */
	private void buildMapperXml(List<String> columns, List<String> types, List<String> comments) throws IOException {
		File folder = new File(Config.file_path);
		if (!folder.exists()) {
			folder.mkdirs();
		}
		File mapperXmlFile = new File(Config.file_path, mapperName + ".xml");
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(mapperXmlFile)));
		bw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		bw.newLine();
		bw.write("<!DOCTYPE mapper PUBLIC \"-//mybatis.org//DTD Mapper 3.0//EN\" ");
		bw.newLine();
		bw.write("    \"http://mybatis.org/dtd/mybatis-3-mapper.dtd\">");
		bw.newLine();
		bw.write("<mapper namespace=\"" + Config.mapper_package + "." + mapperName + "\">");
		bw.newLine();
		bw.newLine();
		bw.write("\t<!--实体映射-->");
		bw.newLine();
		bw.write("\t<resultMap id=\"" + this.processResultMapId(beanName) + "\" type=\"" + Config.bean_package + "." + beanName
				+ "\">");
		bw.newLine();
		bw.write("\t\t<!--" + comments.get(0) + "-->");
		bw.newLine();
		bw.write("\t\t<id property=\"" + this.toCamelCase(columns.get(0)) + "\" column=\"" + columns.get(0)
				+ "\" jdbcType=\"" + processTypeUpperCase(types.get(0)) + "\" />");
		bw.newLine();
		int size = columns.size();
		for (int i = 1; i < size; i++) {
			bw.write("\t\t<!--" + comments.get(i) + "-->");
			bw.newLine();
			bw.write("\t\t<result property=\"" + this.toCamelCase(columns.get(i)) + "\" column=\"" + columns.get(i)
					+ "\" jdbcType=\"" + processTypeUpperCase(types.get(i)) + "\" />");
			bw.newLine();
		}
		bw.write("\t</resultMap>");
		bw.newLine();
		bw.newLine();
		bw.newLine();
		// 下面开始写SqlMapper中的方法
		buildSQL(bw, columns, types);
		bw.write("</mapper>");
		bw.flush();
		bw.close();
	}

	private void buildSQL(BufferedWriter bw, List<String> columns, List<String> types) throws IOException {
		int size = columns.size();
		// 通用结果列
		bw.write("\t<!-- 通用查询结果列-->");
		bw.newLine();
		bw.write("\t<sql id=\"Base_Column_List\">");
		bw.newLine();
		bw.write("\t\t");
		// bw.write("\t\t id,");
		for (int i = 0; i < size; i++) {
			bw.write(columns.get(i));
			if (i != size - 1) {
				bw.write(",");
			}
		}
		bw.newLine();
		bw.write("\t</sql>");
		bw.newLine();
		bw.newLine();
		// 查询（根据主键ID查询）
		bw.write("\t<!-- 查询（根据主键ID查询） -->");
		bw.newLine();
		bw.write("\t<select id=\"selectByPrimaryKey\" resultMap=\"" + processResultMapId(beanName)
				+ "\" parameterType=\"java.lang." + processType(types.get(0)) + "\">");
		bw.newLine();
		bw.write("\t\t SELECT");
		bw.newLine();
		bw.write("\t\t <include refid=\"Base_Column_List\" />");
		bw.newLine();
		bw.write("\t\t FROM " + tableName);
		bw.newLine();
		bw.write("\t\t WHERE " + columns.get(0) + " = #{" + toCamelCase(columns.get(0)) + ",jdbcType="
				+ processTypeUpperCase(types.get(0)) + "}");
		bw.newLine();
		bw.write("\t</select>");
		bw.newLine();
		bw.newLine();
		// 查询完
		// 查询对象方法
		bw.write("\t<!-- 查询（匹配有值的字段）-->");
		bw.newLine();
		bw.write("\t<select id=\"selectByObject\" resultMap=\"" + processResultMapId(beanName) + "\" parameterType=\""
				+ Config.bean_package + "." + beanName + "\">");
		bw.newLine();
		bw.write("\t\t SELECT");
		bw.newLine();
		bw.write("\t\t <include refid=\"Base_Column_List\" />");
		bw.newLine();
		bw.write("\t\t FROM " + tableName);
		bw.newLine();
		bw.write(" \t\t<where>");
		bw.newLine();
		String tempField = null;
		for (int i = 1; i < size; i++) {
			tempField = toCamelCase(columns.get(i));
			bw.write("\t\t\t<if test=\"" + tempField + " != null AND " + tempField + "!=''\">");
			bw.newLine();
			bw.write("\t\t\t\t AND  " + columns.get(i) + " = #{" + tempField + ",jdbcType="
					+ processTypeUpperCase(types.get(i)) + "}");
			bw.newLine();
			bw.write("\t\t\t</if>");
			bw.newLine();
		}
		bw.write(" \t\t</where>");
		bw.newLine();
		bw.write("\t</select>");
		bw.newLine();
		bw.newLine();
		// 查询方法完毕
		// 删除（根据主键ID删除）
		bw.write("\t<!--删除：根据主键ID删除-->");
		bw.newLine();
		bw.write("\t<delete id=\"deleteByPrimaryKey\" parameterType=\"java.lang." + processType(types.get(0)) + "\">");
		bw.newLine();
		bw.write("\t\t DELETE FROM " + tableName);
		bw.newLine();
		bw.write("\t\t WHERE " + columns.get(0) + " = #{" + toCamelCase(columns.get(0)) + ",jdbcType="
				+ processTypeUpperCase(types.get(0)) + "}");
		bw.newLine();
		bw.write("\t</delete>");
		bw.newLine();
		bw.newLine();
		// 删除完
		// 添加insert方法
		bw.write("\t<!-- 添加 -->");
		bw.newLine();
		bw.write("\t<insert id=\"insert\" parameterType=\"" + Config.bean_package + "." + beanName + "\">");
		bw.newLine();
		bw.write("\t\t INSERT INTO " + tableName);
		bw.newLine();
		bw.write(" \t\t(");
		for (int i = 0; i < size; i++) {
			bw.write(columns.get(i));
			if (i != size - 1) {
				bw.write(",");
			}
			if (i % 3 == 0 && i != 0 && i != size - 1) {
				bw.newLine();
				bw.write(" \t\t   ");
			}
		}
		bw.write(") ");
		bw.newLine();
		bw.write("\t\t VALUES  ");
		for (int i = 0; i < size; i++) {
			bw.write("#{" + toCamelCase(columns.get(i)) + ",jdbcType=" + processTypeUpperCase(types.get(i)) + "}");
			if (i != size - 1) {
				bw.write(",");
			}
			if (i % 3 == 0 && i != 0 && i != size - 1) {
				bw.newLine();
				bw.write(" \t\t   ");
			}
		}
		bw.write(") ");
		bw.newLine();
		bw.write("\t</insert>");
		bw.newLine();
		bw.newLine();
		// 添加insert完
		// --------------- insert方法（匹配有值的字段）
		bw.write("\t<!-- 添加 （匹配有值的字段）-->");
		bw.newLine();
		bw.write("\t<insert id=\"insertSelective\" parameterType=\"" + Config.bean_package + "." + beanName + "\">");
		bw.newLine();
		bw.write("\t\t INSERT INTO " + tableName);
		bw.newLine();
		bw.write("\t\t <trim prefix=\"(\" suffix=\")\" suffixOverrides=\",\" >");
		bw.newLine();
		for (int i = 0; i < size; i++) {
			tempField = toCamelCase(columns.get(i));
			bw.write("\t\t\t<if test=\"" + tempField + " != null AND " + tempField + "!=''\">");
			bw.newLine();
			bw.write("\t\t\t\t " + columns.get(i) + ",");
			bw.newLine();
			bw.write("\t\t\t</if>");
			bw.newLine();
		}
		bw.write("\t\t </trim>");
		bw.newLine();

		bw.write("\t\t <trim prefix=\"values (\" suffix=\")\" suffixOverrides=\",\" >");
		bw.newLine();
		for (int i = 0; i < size; i++) {
			tempField = toCamelCase(columns.get(i));
			bw.write("\t\t\t<if test=\"" + tempField + "!=null AND " + tempField + "!= ''\">");
			bw.newLine();
			bw.write("\t\t\t\t #{" + tempField + ",jdbcType=" + processTypeUpperCase(types.get(i)) + "},");
			bw.newLine();
			bw.write("\t\t\t</if>");
			bw.newLine();
		}
		bw.write("\t\t </trim>");
		bw.newLine();
		bw.write("\t</insert>");
		bw.newLine();
		bw.newLine();
		// --------------- 完毕
		// 修改update方法
		bw.write("\t<!-- 修 改（匹配有值的字段）-->");
		bw.newLine();
		bw.write("\t<update id=\"updateByPrimaryKeySelective\" parameterType=\"" + Config.bean_package + "." + beanName
				+ "\">");
		bw.newLine();
		bw.write("\t\t UPDATE " + tableName);
		bw.newLine();
		bw.write(" \t\t <set> ");
		bw.newLine();
		for (int i = 1; i < size; i++) {
			tempField = toCamelCase(columns.get(i));
			bw.write("\t\t\t<if test=\"" + tempField + " != null AND " + tempField + "!=''\">");
			bw.newLine();
			bw.write("\t\t\t\t " + columns.get(i) + " = #{" + tempField + ",jdbcType="
					+ processTypeUpperCase(types.get(i)) + "},");
			bw.newLine();
			bw.write("\t\t\t</if>");
			bw.newLine();
		}
		bw.newLine();
		bw.write(" \t\t </set>");
		bw.newLine();
		bw.write("\t\t WHERE " + columns.get(0) + " = #{" + toCamelCase(columns.get(0)) + ",jdbcType="
				+ processTypeUpperCase(types.get(0)) + "}");
		bw.newLine();
		bw.write("\t</update>");
		bw.newLine();
		bw.newLine();
		// update方法完毕
		// 修改update方法
		bw.write("\t<!-- 修改-->");
		bw.newLine();
		bw.write("\t<update id=\"updateByPrimaryKey\" parameterType=\"" + Config.bean_package + "." + beanName + "\">");
		bw.newLine();
		bw.write("\t\t UPDATE " + tableName);
		bw.newLine();
		bw.write("\t\t SET ");
		bw.newLine();
		tempField = null;
		for (int i = 1; i < size; i++) {
			tempField = toCamelCase(columns.get(i));
			bw.write("\t\t\t " + columns.get(i) + " = #{" + tempField + ",jdbcType="
					+ processTypeUpperCase(types.get(i)) + "}");
			if (i != size - 1) {
				bw.write(",");
			}
			bw.newLine();
		}
		bw.write("\t\t WHERE " + columns.get(0) + " = #{" + toCamelCase(columns.get(0)) + ",jdbcType="
				+ processTypeUpperCase(types.get(0)) + "}");
		bw.newLine();
		bw.write("\t</update>");
		bw.newLine();
		// 完毕
	}

	/**
	 * 获取所有的数据库表注释
	 *
	 * @return
	 * @throws SQLException
	 */
	private Map<String, String> getTableComment() throws SQLException {
		Map<String, String> maps = new HashMap<String, String>();
		PreparedStatement pstate = conn.prepareStatement("show table status");
		ResultSet results = pstate.executeQuery();
		while (results.next()) {
			String tableName = results.getString("NAME");
			String comment = results.getString("COMMENT");
			maps.put(tableName, comment);
		}
		return maps;
	}

	public void generate(Set<String> tables) throws Exception {
		String prefix = "show full fields from ";
		List<String> columns = null;
		List<String> types = null;
		List<String> comments = null;
		PreparedStatement pstate = null;
		Map<String, String> tableComments = getTableComment();
		for (String table : tables) {
			columns = new ArrayList<String>();
			types = new ArrayList<String>();
			comments = new ArrayList<String>();
			pstate = conn.prepareStatement(prefix + table);
			ResultSet results = pstate.executeQuery();
			while (results.next()) {
				columns.add(results.getString("FIELD"));
				types.add(results.getString("TYPE"));
				comments.add(results.getString("COMMENT"));
			}
			tableName = table;
			processTable(table);
			String tableComment = tableComments.get(table);
			buildEntityBean(columns, types, comments, tableComment);
			idType = processType(types.get(0));
			buildServiceOrMappers(mapperName, Config.mapper_package);
			buildServiceOrMappers(serviceName, Config.service_package);
			buildServiceImpl();
			buildMapperXml(columns, types, comments);
		}
		System.out.println("OK");
	}
}
