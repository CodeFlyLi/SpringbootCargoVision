package com.app.springbootcargovision.service.impl;

import com.app.springbootcargovision.mapper.BaseGoodsMapper;
import com.app.springbootcargovision.model.BaseGoods;
import com.app.springbootcargovision.mapper.BaseGoodsTypeMapper;
import com.app.springbootcargovision.model.BaseGoodsType;
import com.app.springbootcargovision.service.BaseGoodsService;
import com.app.springbootcargovision.utils.SecurityUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 基础货物信息服务实现类
 * 实现货物信息的增删改查逻辑，包含事务管理
 */
@Service
public class BaseGoodsServiceImpl implements BaseGoodsService {

    private final BaseGoodsMapper baseGoodsMapper;
    private final BaseGoodsTypeMapper baseGoodsTypeMapper;

    public BaseGoodsServiceImpl(BaseGoodsMapper baseGoodsMapper, BaseGoodsTypeMapper baseGoodsTypeMapper) {
        this.baseGoodsMapper = baseGoodsMapper;
        this.baseGoodsTypeMapper = baseGoodsTypeMapper;
    }

    /**
     * 获取所有货物类型实现
     */
    @Override
    public List<BaseGoodsType> getGoodsTypes() {
        return baseGoodsTypeMapper.selectAll();
    }

    /**
     * 分页查询货物列表实现
     * 使用 PageHelper 插件进行分页
     * 加入数据权限控制：非管理员只能查看自己创建的数据
     */
    @Override
    public PageInfo<BaseGoods> getGoodsList(Integer page, Integer size, String goodsNo, String name, Long typeId) {
        // 设置分页参数
        PageHelper.startPage(page, size);

        // 构建查询条件
        BaseGoods query = new BaseGoods();
        query.setGoodsNo(goodsNo);
        query.setName(name);
        query.setTypeId(typeId);

        // 执行查询
        List<BaseGoods> list = baseGoodsMapper.selectList(query);

        // 返回分页结果
        return new PageInfo<>(list);
    }

    @Override
    public BaseGoods getGoodsById(Long id) {
        return baseGoodsMapper.selectById(id);
    }

    /**
     * 创建货物实现
     * 包含业务校验：检查货物编号是否已存在
     * 自动填充创建人信息
     */
    @Override
    @Transactional
    public void createGoods(BaseGoods goods) {
        // 校验货物编号唯一性
        if (baseGoodsMapper.selectByGoodsNo(goods.getGoodsNo()) != null) {
            throw new RuntimeException("货物编号已存在");
        }

        // 设置创建人
        Long currentUserId = SecurityUtils.getUserId();
        if (currentUserId != null) {
            goods.setCreatedBy(currentUserId);
        }

        baseGoodsMapper.insert(goods);
    }

    @Override
    @Transactional
    public void updateGoods(BaseGoods goods) {

        baseGoodsMapper.update(goods);
    }

    @Override
    @Transactional
    public void deleteGoods(Long id) {

        baseGoodsMapper.deleteById(id);
    }
}
