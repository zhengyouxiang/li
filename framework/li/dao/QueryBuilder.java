package li.dao;

import java.util.Map;
import java.util.Map.Entry;

import javax.sql.DataSource;

import li.model.Bean;
import li.model.Field;
import li.util.Reflect;
import li.util.Verify;

/**
 * 工具类,用以组装SQL
 * 
 * @author li (limw@w.cn)
 * @version 0.1.8 (2012-05-08)
 */
public class QueryBuilder {
	/**
	 * 根据传入的ID,构建一个用于删除单条记录的SQL
	 */
	public String delete(DataSource dataSource, Class<?> type, Integer id) {
		Bean iocBean = Bean.getMeta(dataSource, type);
		return String.format("DELETE FROM %s WHERE %s=%s", iocBean.table, iocBean.getId().column, id);
	}

	/**
	 * 根据传入的SQL,构建一个用于删除的SQL
	 * 
	 * @param sql 传入的sql语句,可以包含'?'占位符和具名占位符
	 * @param args 替换sql中占位符的值,或者对应具名占位符的Map
	 * @see li.dao.QueryBuilder#setArgs(String, Object[])
	 */
	public String delete(DataSource dataSource, Class<?> type, String sql, Object[] args) {
		if (!Verify.startWith(sql, "DELETE")) {
			sql = String.format("DELETE FROM %s %s", Bean.getMeta(dataSource, type).table, sql);
		}
		return setArgs(sql, args);// 处理args
	}

	/**
	 * 根据传入的对象构建一个用于插入记录的SQL
	 */
	public String update(DataSource dataSource, Object object, Class<?> type) {
		Bean iocBean = Bean.getMeta(dataSource, type);
		String sets = "";
		for (Field field : iocBean.fields) {
			Object fieldValue = Reflect.get(object, field.name);
			if (!field.isId && null != fieldValue) {
				sets += String.format("%s='%s',", field.column, fieldValue);
			}
		}
		Object id = Reflect.get(object, iocBean.getId().name);
		sets = sets.substring(0, sets.length() - 1);
		return String.format("UPDATE %s SET %s WHERE %s=%s", iocBean.table, sets, iocBean.getId().column, id);
	}

	/**
	 * 根据传入的SQL,构建一个用于更新的SQL
	 * 
	 * @param sql 传入的sql语句,可以包含'?'占位符和具名占位符
	 * @param args 替换sql中占位符的值,或者对应具名占位符的Map
	 * @see li.dao.QueryBuilder#setArgs(String, Object[])
	 */
	public String update(DataSource dataSource, Class<?> type, String sql, Object[] args) {
		if (Verify.startWith(sql, "SET")) {
			return String.format("UPDATE %s %s", Bean.getMeta(dataSource, type).table, sql);
		}
		return setArgs(sql, args);// 处理args
	}

	/**
	 * 根据传入的对象构建一个插入的SQL
	 */
	public String save(DataSource dataSource, Object object, Class<?> type) {
		Bean iocBean = Bean.getMeta(dataSource, type);
		String columns = "", values = "";
		for (Field field : iocBean.fields) {
			if (!field.isId) {
				Object fieldValue = Reflect.get(object, field.name);
				columns += field.column + ",";
				values += (null == fieldValue ? "NULL" : "'" + fieldValue + "'") + ",";
			}
		}
		columns = columns.substring(0, columns.length() - 1);
		values = values.substring(0, values.length() - 1);
		return String.format("INSERT INTO %s (%s) VALUES (%s)", iocBean.table, columns, values);
	}

	/**
	 * 构造一默认的COUNT(*)查询的SQL,查询表中的总记录数
	 */
	public String count(DataSource dataSource, Class<?> type) {
		return String.format("SELECT COUNT(*) FROM %s", Bean.getMeta(dataSource, type).table);
	}

	/**
	 * 根据传入的SQL,构造一个用于COUNT(*)查询的SQL
	 * 
	 * @param sql 传入的sql语句,可以包含'?'占位符和具名占位符
	 * @param args 替换sql中占位符的值,或者对应具名占位符的Map
	 * @see li.dao.QueryBuilder#setArgs(String, Object[])
	 */
	public String count(DataSource dataSource, Class<?> type, String sql, Object[] args) {
		if (!Verify.startWith(sql, "SELECT")) {
			sql = String.format("SELECT COUNT(*) FROM %s %s", Bean.getMeta(dataSource, type).table, sql);
		} else if (!Verify.contain(sql, "COUNT(*)")) {
			sql = String.format("SELECT COUNT(*) FROM %s", sql.substring(sql.toUpperCase().indexOf("FROM") + 4, sql.length()).trim());
		}
		if (Verify.contain(sql, "LIMIT")) {
			sql = sql.substring(0, sql.toUpperCase().indexOf("LIMIT"));// 去掉limit部分
		}
		return setArgs(sql, args);// 处理args
	}

	/**
	 * 使用传入的ID,构造一个用于查询一条记录的SQL
	 */
	public String find(DataSource dataSource, Class<?> type, Integer id) {
		Bean iocBean = Bean.getMeta(dataSource, type);
		return String.format("SELECT * FROM %s WHERE %s=%s LIMIT 1", iocBean.table, iocBean.getId().column, id);
	}

	/**
	 * 使用传入的page,构造一个用于分页查询的SQL
	 * 
	 * @param page page如果为空,则使用new Page()
	 */
	public String list(DataSource dataSource, Class<?> type, Page page) {
		return list(dataSource, type, page, String.format("SELECT * FROM %s", Bean.getMeta(dataSource, type).table), null);
	}

	/**
	 * 根据传入的SQL和page,构造一个用于分页查询的SQL
	 * 
	 * @param page 分页对象
	 * @param sql 传入的sql语句,可以包含'?'占位符和具名占位符
	 * @param args 替换sql中占位符的值,或者对应具名占位符的Map
	 * @see li.dao.QueryBuilder#setPage(String, Page)
	 * @see li.dao.QueryBuilder#setArgs(String, Object[])
	 */
	public String list(DataSource dataSource, Class<?> type, Page page, String sql, Object[] args) {
		if (!Verify.startWith(sql, "SELECT")) {// 添加SELECT * FROM table 部分
			sql = String.format("SELECT * FROM %s %s", Bean.getMeta(dataSource, type).table, sql);
		}
		return setPage(setArgs(setAlias(dataSource, sql), args), page);// 先处理别名,再处理args,最后处理page
	}

	/**
	 * 如果args不为空的话,SQL会用args逐次替换SQL中的占位符
	 * 
	 * @param sql 传入的sql语句,可以包含'?'占位符或者具名参数占位符
	 * @param args 替换sql中 '?' 或具名参数占位符的值
	 * @see li.dao.QueryBuilder#setArgs(String, Map)
	 */
	public String setArgs(String sql, Object[] args) {
		if (null != sql && sql.length() > 0 && null != args && args.length > 0) {// 非空判断
			for (int i = 0; i < args.length; i++) {
				if (args[i] instanceof Map<?, ?>) {
					sql = setArgs(sql, (Map<?, ?>) args[i]);// 替换具名参数
				} else {
					sql = sql.replaceFirst("[?]", String.format("'%s'", args[i]));// 为参数加上引号后替换问号
				}
			}
		}
		return sql;
	}

	/**
	 * 用Map中的值替换SQL中的具名参数
	 * 
	 * @param sql 传入的sql语句,可以包含':name'占位符
	 * @param argMap 替换sql中 ':name' 的键值Map
	 */
	public String setArgs(String sql, Map<?, ?> argMap) {
		if (null != sql && sql.length() > 0 && null != argMap && argMap.size() > 0) {// 非空判断
			for (Entry<?, ?> arg : argMap.entrySet()) {
				sql = sql.replaceFirst(arg.getKey() + "", String.format("'%s'", arg.getValue()));// 为参数加上引号后替换问号
			}
		}
		return sql;
	}

	/**
	 * 为SQL添加分页语句
	 */
	public String setPage(String sql, Page page) {
		if (!Verify.contain(sql, "LIMIT") && null != page) {// 分页
			return sql + String.format(" LIMIT %s,%s", page.getFrom(), page.getPageSize());
		}
		return sql;
	}

	/**
	 * 处理批量别名,将类似 SELECT t_account.#,t_member.# AS member_# FROM t_account,t_member的SQL转换为标准SQL
	 */
	public String setAlias(DataSource dataSource, String sql) {
		int index = sql.indexOf(".#");
		if (index > 5) {// 如果有替换字符,则开始处理,否则直接返回
			int end = index + 2;// end为第一个#的位置

			int s1 = sql.substring(0, end).lastIndexOf(" ") + 1;// #之前最后一个空格的位置
			int s2 = sql.substring(0, end).lastIndexOf(",") + 1;// #之前最后一个,的位置
			int start = s1 > s2 ? s1 : s2;// table.#部分部分开始位置

			int asIndex = sql.toUpperCase().indexOf(" AS ", end);// end之后第一个AS的位置

			String replacement = "";// 申明替换部分字符串
			int toreplaceEnd = (asIndex > 0 && asIndex - end < 3) ? sql.indexOf("#", asIndex) + 1 : end;// 如果有AS,截取到AS后的第一个#,如果没有AS,截取到end
			String toreplace = sql.substring(start, toreplaceEnd);// 求出被替换部分字符串
			String table = sql.substring(start, end - 2);// 求得表名

			for (Field field : Field.list(dataSource, table)) {// 构造替换字符串
				if (asIndex > 0 && asIndex - end < 3) {// 如果有AS
					String fix = toreplace.substring(asIndex - start, toreplace.length() - 1);// 取得AS+别名前缀,如ASmember_
					replacement = replacement + (table + "." + field.column) + (fix + field.column) + ",";// 构造一列加AS
				} else {
					replacement = replacement + table + "." + field.column + ",";// 构造一列不加AS
				}
			}

			// 替换,去掉最后一个逗号
			String result = sql.replaceFirst(toreplace, replacement.substring(0, replacement.length() - 1));

			return setAlias(dataSource, result);// 处理下一个
		}
		return sql;// 如果SQL中不包含#,则直接返回
	}
}