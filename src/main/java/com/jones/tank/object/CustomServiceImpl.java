package com.jones.tank.object;

import com.jones.tank.entity.query.Query;
import com.jones.tank.repository.CustomBaseMapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
public class CustomServiceImpl<M extends CustomBaseMapper<T>, T>  implements ICustomService<T> {
    @Autowired
    protected M baseMapper;

    public CustomServiceImpl() {
    }

    public M getBaseMapper() {
        return this.baseMapper;
    }

    public Page<T> findPage(Query query) {
        Long count = this.baseMapper.findCount(query);
        List data = this.baseMapper.findList(query);
        return new Page<T>(query, count, data);
    }

    public BaseResponse findList(Query query) {
        List data = this.baseMapper.findList(query);
        return BaseResponse.builder().data(data).build();
    }

    /**
     * 新增
     * @param param
     * @return
     */
    public BaseResponse add(BaseObject param){
        this.baseMapper.insert(param);
        Map<String, Long> map = new HashMap<>();
        map.put("id", param.getId());
        return BaseResponse.builder().data(map).build();
    }

    /**
     * 更新
     * @param param
     * @return
     */
    public BaseResponse update(BaseObject param){
        this.baseMapper.update(param);
        return BaseResponse.builder().build();
    }

    /**
     * 获取详情
     * @param id
     * @return
     */
    public BaseResponse findById(Long id){
        Object item = this.baseMapper.findById(id);
        return BaseResponse.builder().data(item).build();
    }

    @Override
    public BaseResponse findOne(Query query) {
        List<T> data = this.baseMapper.findAll(query);
        T result = null;
        if(data.size() > 0){
            result = data.get(0);
        }
        return BaseResponse.builder().data(result).build();
    }

    /**
     * 获取列表
     * @param query
     * @return
     */
    public BaseResponse findByPage(Query query){
        Page list = findPage(query);
        return BaseResponse.builder().data(list).build();
    }

    /**
     * 获取全部内容
     * @param query
     * @return
     */
    public BaseResponse findAll(Query query){
        List list = this.baseMapper.findAll(query);
        return BaseResponse.builder().data(list).build();
    }

    /**
     * 删除
     * @param id
     * @return
     */
    public BaseResponse delete(Long id){
        this.baseMapper.delete(id);
        return BaseResponse.builder().build();
    }

}
