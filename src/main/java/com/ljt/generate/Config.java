package com.ljt.generate;

public class Config {
	/**
	 * 数据库驱动
	 */
	public final static String driverName = "com.mysql.jdbc.Driver";
	public final static String type_char = "char";
	public final static String type_date = "date";
	public final static String type_timestamp = "timestamp";
	public final static String type_int = "int";
	public final static String type_bigint = "bigint";
	public final static String type_tinyint = "tinyint";
	public final static String type_text = "text";
	public final static String type_bit = "bit";
	public final static String type_decimal = "decimal";
	public final static String type_blob = "blob";
	/**
	 * 实体生成路径
	 */
	public static String file_path = "";
	/**
	 * 实体包名
	 */
	public static String bean_package = "";
	/**
	 * dao包名
	 */
	public static String mapper_package = "";
	/**
	 * service 包名
	 */
	public static String service_package = "";
	/**
	 * service 实现包名
	 */
	public static String service_impl_package = service_package+".impl";

	/**
	 * 数据库用户名
	 */
	public static String username = "";
	/**
	 * 数据库密码
	 */
	public static String password = "";
	/**
	 * 数据库地址
	 */
	public static String address="";
	/**
	 * 数据库端口
	 */
	public static String port="";
	/**
	 * 数据库名
	 */
	public static String database="";
}
