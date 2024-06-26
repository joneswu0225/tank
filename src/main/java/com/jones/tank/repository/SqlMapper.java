package com.jones.tank.repository;

import org.apache.ibatis.builder.StaticSqlSource;
import org.apache.ibatis.exceptions.TooManyResultsException;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.scripting.LanguageDriver;
import org.apache.ibatis.session.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author chaild
 * @Date 2020-7-7 14:43:35
 * 自定义SQL查询类
 */
@Component
public class SqlMapper{

    /**
     * 使用方式
     *
     * @Autowired private SqlMapper sqlMapper;
     */
    private SqlSession sqlSession;
    private SqlMapper.MSUtils msUtils;

    @Autowired
    SqlSessionFactory sqlSessionFactory;

    public SqlMapper() {
    }
    /**这个注解具体意思可以自己去了解一下**/
    @PostConstruct
    private void init() {

        SqlSessionManager sqlSessionManager = SqlSessionManager.newInstance(sqlSessionFactory);
//                this.sqlSession = sqlSessionFactory.openSession(true);
//        this.sqlSession = sqlSessionManager.openSession(true);
        this.sqlSession = sqlSessionManager;
        Configuration conf = this.sqlSession.getConfiguration();
        conf.setCacheEnabled(false);
        this.msUtils = new SqlMapper.MSUtils(conf);
    }

    private <T> T getOne(List<T> list) {
        if (list.size() == 1) {
            return list.get(0);
        } else if (list.size() > 1) {
            throw new TooManyResultsException("Expected one result (or null) to be returned by selectOne(), but found: " + list.size());
        } else {
            return null;
        }
    }

    /**
     * 查询单条数据返回Map<String, Object>
     *
     * @param sql sql语句
     * @return Map<String, Object>
     */
    public Map<String, Object> sqlSelectOne(String sql) {
        List<Map<String, Object>> list = this.sqlSelectList(sql);
        return (Map) this.getOne(list);
    }

    public Integer sqlSelectCount(String sql){
        return this.sqlSelectList(sql).size();
    }

    /**
     * 查询单条数据返回Map<String, Object>
     *
     * @param sql   sql语句
     * @param value 参数
     * @return Map<String, Object>
     */
    public Map<String, Object> sqlSelectOne(String sql, Object value) {
        List<Map<String, Object>> list = this.sqlSelectList(sql, value);
        return (Map) this.getOne(list);
    }

    /**
     * 查询单条数据返回实体类型
     *
     * @param sql        sql语句
     * @param resultType 具体类型
     * @return 定义的实体类型
     */
    public <T> T sqlSelectOne(String sql, Class<T> resultType) {
        List<T> list = this.sqlSelectList(sql, resultType);
        return this.getOne(list);
    }

    /**
     * 查询单条数据返回实体类型
     *
     * @param sql        sql语句
     * @param value      参数
     * @param resultType 具体类型
     * @return 定义的实体类型
     */
    public <T> T sqlSelectOne(String sql, Object value, Class<T> resultType) {
        List<T> list = this.sqlSelectList(sql, value, resultType);
        return this.getOne(list);
    }

    /**
     * 查询数据返回
     *
     * @param sql sql语句
     * @return List<Map < String, Object>>
     */
    public List<Map<String, Object>> sqlSelectList(String sql) {
        String msId = this.msUtils.select(sql);
        return this.sqlSession.selectList(msId);
    }

    /**
     * 查询数据返回
     *
     * @param sql   sql语句
     * @param value 参数
     * @return List<Map < String, Object>>
     */
    public List<Map<String, Object>> sqlSelectList(String sql, Object value) {
        Class<?> parameterType = value != null ? value.getClass() : null;
        String msId = this.msUtils.selectDynamic(sql, parameterType);
        return this.sqlSession.selectList(msId, value);
    }

    /**
     * 查询数据返回
     *
     * @param sql   sql语句
     * @param value 参数
     * @return List<Map < String, Object>>
     */
    private static final String PAGE_SIZE = "page_size";
    private static final Integer PAGE_SIZE_DEFAULT = 20;
    private static final String PAGE_NUMBER = "page_number";
    private static final Integer PAGE_NUMBER_DEFAULT = 1;
    public List<Map<String, Object>> sqlSelectList(String sql, Map<String, String> params) {
        Class<?> parameterType = params != null ? params.getClass() : null;
        String msId = this.msUtils.selectDynamic(sql, parameterType);
        if(params.containsKey(PAGE_SIZE) || params.containsKey(PAGE_NUMBER)){
            Integer pageSize = Integer.valueOf(params.getOrDefault(PAGE_SIZE, "0"));
            pageSize = pageSize == null ? PAGE_SIZE_DEFAULT : pageSize;
            Integer pageNumber = Integer.valueOf(params.getOrDefault(PAGE_NUMBER, "0"));
            pageNumber = pageNumber <= 0 ? PAGE_NUMBER_DEFAULT : pageNumber;
            RowBounds rowBounds = new RowBounds(pageNumber*pageSize-pageSize, pageSize);
            return this.sqlSession.selectList(msId, params, rowBounds);
        }
        try {
            return this.sqlSession.selectList(msId, params);
        } catch (Exception e) {
            this.sqlSession.clearCache();
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    /**
     * 查询数据返回
     *
     * @param sql        sql语句
     * @param resultType 具体类型
     * @return List<T>
     */
    public <T> List<T> sqlSelectList(String sql, Class<T> resultType) {
        String msId;
        if (resultType == null) {
            msId = this.msUtils.select(sql);
        } else {
            msId = this.msUtils.select(sql, resultType);
        }

        return this.sqlSession.selectList(msId);
    }

    /**
     * 查询数据返回
     *
     * @param sql        sql语句
     * @param value      参数
     * @param resultType 具体类型
     * @return List<T>
     */
    public <T> List<T> sqlSelectList(String sql, Object value, Class<T> resultType) {
        Class<?> parameterType = value != null ? value.getClass() : null;
        String msId;
        if (resultType == null) {
            msId = this.msUtils.selectDynamic(sql, parameterType);
        } else {
            msId = this.msUtils.selectDynamic(sql, parameterType, resultType);
        }

        return this.sqlSession.selectList(msId, value);
    }

    /**
     * 插入数据
     *
     * @param sql sql语句
     * @return int
     */
    public int sqlInsert(String sql) {
        String msId = this.msUtils.insert(sql);
        return this.sqlSession.insert(msId);
    }

    /**
     * 插入数据
     *
     * @param sql   sql语句
     * @param value 参数
     * @return int
     */
    public Long sqlInsert(String sql, Object value) {
        Class<?> parameterType = value != null ? value.getClass() : null;
        String msId = this.msUtils.insertDynamic(sql, parameterType);
        return Long.valueOf(this.sqlSession.selectOne(msId, value).toString());
    }

    /**
     * 更新数据
     *
     * @param sql sql语句
     * @return int
     */
    public int sqlUpdate(String sql) {
        String msId = this.msUtils.update(sql);
        return this.sqlSession.update(msId);
    }

    /**
     * 更新数据
     *
     * @param sql   sql语句
     * @param value 参数
     * @return int
     */
    public int sqlUpdate(String sql, Object value) {
        Class<?> parameterType = value != null ? value.getClass() : null;
        String msId = this.msUtils.updateDynamic(sql, parameterType);
        try {
            return this.sqlSession.update(msId, value);
        } catch (Exception e) {
            this.sqlSession.clearCache();
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * 删除数据
     *
     * @param sql sql语句
     * @return int
     */
    public int sqlDelete(String sql) {
        String msId = this.msUtils.delete(sql);
        return this.sqlSession.delete(msId);
    }

    /**
     * 查询数据返回List<T>
     *
     * @param sql   sql语句
     * @param value 参数
     * @return int
     */
    public int sqlDelete(String sql, Object value) {
        Class<?> parameterType = value != null ? value.getClass() : null;
        String msId = this.msUtils.deleteDynamic(sql, parameterType);
        return this.sqlSession.delete(msId, value);
    }

    /**
     * 进行预编译
     */
    private class MSUtils {
        private Configuration configuration;
        private LanguageDriver languageDriver;

        private MSUtils(Configuration configuration) {
            this.configuration = configuration;
            this.languageDriver = configuration.getDefaultScriptingLanguageInstance();
        }

        private String newMsId(String sql, SqlCommandType sqlCommandType) {
            StringBuilder msIdBuilder = new StringBuilder(sqlCommandType.toString());
            msIdBuilder.append(".").append(sql.hashCode());
            return msIdBuilder.toString();
        }

        private boolean hasMappedStatement(String msId) {
            return this.configuration.hasStatement(msId, false);
        }

        private void newSelectMappedStatement(String msId, SqlSource sqlSource, final Class<?> resultType) {
            MappedStatement ms = (new MappedStatement.Builder(this.configuration, msId, sqlSource, SqlCommandType.SELECT)).flushCacheRequired(true).resultMaps(new ArrayList<ResultMap>() {
                {
                    this.add((new org.apache.ibatis.mapping.ResultMap.Builder(com.jones.tank.repository.SqlMapper.MSUtils.this.configuration, "defaultResultMap", resultType, new ArrayList(0))).build());
                }
            }).build();

            this.configuration.addMappedStatement(ms);
        }

        private void newUpdateMappedStatement(String msId, SqlSource sqlSource, SqlCommandType sqlCommandType) {
            MappedStatement ms = (new MappedStatement.Builder(this.configuration, msId, sqlSource, sqlCommandType)).flushCacheRequired(true).resultMaps(new ArrayList<ResultMap>() {
                {
                    this.add((new org.apache.ibatis.mapping.ResultMap.Builder(com.jones.tank.repository.SqlMapper.MSUtils.this.configuration, "defaultResultMap", Integer.TYPE, new ArrayList(0))).build());
                }
            }).build();
            this.configuration.addMappedStatement(ms);
        }

        private String select(String sql) {
            String msId = this.newMsId(sql, SqlCommandType.SELECT);
            if (this.hasMappedStatement(msId)) {
                return msId;
            } else {
                StaticSqlSource sqlSource = new StaticSqlSource(this.configuration, sql);
                this.newSelectMappedStatement(msId, sqlSource, Map.class);
                return msId;
            }
        }

        private String selectDynamic(String sql, Class<?> parameterType) {
            String msId = this.newMsId(sql + parameterType, SqlCommandType.SELECT);
            if (this.hasMappedStatement(msId)) {
                return msId;
            } else {
                SqlSource sqlSource = this.languageDriver.createSqlSource(this.configuration, sql, parameterType);
                this.newSelectMappedStatement(msId, sqlSource, Map.class);
                return msId;
            }
        }

        private String select(String sql, Class<?> resultType) {
            String msId = this.newMsId(resultType + sql, SqlCommandType.SELECT);
            if (this.hasMappedStatement(msId)) {
                return msId;
            } else {
                StaticSqlSource sqlSource = new StaticSqlSource(this.configuration, sql);
                this.newSelectMappedStatement(msId, sqlSource, resultType);
                return msId;
            }
        }

        private String selectDynamic(String sql, Class<?> parameterType, Class<?> resultType) {
            String msId = this.newMsId(resultType + sql + parameterType, SqlCommandType.SELECT);
            if (this.hasMappedStatement(msId)) {
                return msId;
            } else {
                SqlSource sqlSource = this.languageDriver.createSqlSource(this.configuration, sql, parameterType);
                this.newSelectMappedStatement(msId, sqlSource, resultType);
                return msId;
            }
        }

        private String insert(String sql) {
            String msId = this.newMsId(sql, SqlCommandType.INSERT);
            if (this.hasMappedStatement(msId)) {
                return msId;
            } else {
                StaticSqlSource sqlSource = new StaticSqlSource(this.configuration, sql);
                this.newUpdateMappedStatement(msId, sqlSource, SqlCommandType.INSERT);
                return msId;
            }
        }

        private String insertDynamic(String sql, Class<?> parameterType) {
            String msId = this.newMsId(sql + parameterType, SqlCommandType.INSERT);
            if (this.hasMappedStatement(msId)) {
                return msId;
            } else {
                SqlSource sqlSource = this.languageDriver.createSqlSource(this.configuration, sql, parameterType);
                this.newUpdateMappedStatement(msId, sqlSource, SqlCommandType.INSERT);
                return msId;
            }
        }

        private String update(String sql) {
            String msId = this.newMsId(sql, SqlCommandType.UPDATE);
            if (this.hasMappedStatement(msId)) {
                return msId;
            } else {
                StaticSqlSource sqlSource = new StaticSqlSource(this.configuration, sql);
                this.newUpdateMappedStatement(msId, sqlSource, SqlCommandType.UPDATE);
                return msId;
            }
        }

        private String updateDynamic(String sql, Class<?> parameterType) {
            String msId = this.newMsId(sql + parameterType, SqlCommandType.UPDATE);
            if (this.hasMappedStatement(msId)) {
                return msId;
            } else {
                SqlSource sqlSource = this.languageDriver.createSqlSource(this.configuration, sql, parameterType);
                this.newUpdateMappedStatement(msId, sqlSource, SqlCommandType.UPDATE);
                return msId;
            }
        }

        private String delete(String sql) {
            String msId = this.newMsId(sql, SqlCommandType.DELETE);
            if (this.hasMappedStatement(msId)) {
                return msId;
            } else {
                StaticSqlSource sqlSource = new StaticSqlSource(this.configuration, sql);
                this.newUpdateMappedStatement(msId, sqlSource, SqlCommandType.DELETE);
                return msId;
            }
        }

        private String deleteDynamic(String sql, Class<?> parameterType) {
            String msId = this.newMsId(sql + parameterType, SqlCommandType.DELETE);
            if (this.hasMappedStatement(msId)) {
                return msId;
            } else {
                SqlSource sqlSource = this.languageDriver.createSqlSource(this.configuration, sql, parameterType);
                this.newUpdateMappedStatement(msId, sqlSource, SqlCommandType.DELETE);
                return msId;
            }
        }
    }
}
