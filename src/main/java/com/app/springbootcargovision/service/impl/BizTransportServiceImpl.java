package com.app.springbootcargovision.service.impl;

import com.app.springbootcargovision.mapper.BaseGoodsMapper;
import com.app.springbootcargovision.mapper.BizTransportMapper;
import com.app.springbootcargovision.model.BaseGoods;
import com.app.springbootcargovision.model.BizTransport;
import com.app.springbootcargovision.service.BizTransportService;
import com.app.springbootcargovision.utils.SecurityUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 货物运输记录服务实现类
 * 实现运输记录的增删改查逻辑，包含事务管理
 */
@Service
public class BizTransportServiceImpl implements BizTransportService {

    private final BizTransportMapper bizTransportMapper;
    private final BaseGoodsMapper baseGoodsMapper;

    public BizTransportServiceImpl(BizTransportMapper bizTransportMapper, BaseGoodsMapper baseGoodsMapper) {
        this.bizTransportMapper = bizTransportMapper;
        this.baseGoodsMapper = baseGoodsMapper;
    }

    /**
     * 分页查询运输记录实现
     * 使用 PageHelper 插件进行分页
     */
    @Override
    public PageInfo<BizTransport> getTransportList(Integer page, Integer size, String transportNo, String goodsName,
            Integer status, String customerName) {
        PageHelper.startPage(page, size);
        BizTransport query = new BizTransport();
        query.setTransportNo(transportNo);
        query.setGoodsName(goodsName);
        query.setStatus(status);
        query.setCustomerName(customerName);
        return new PageInfo<>(bizTransportMapper.selectList(query));
    }

    @Override
    public BizTransport getTransportById(Long id) {
        return bizTransportMapper.selectById(id);
    }

    /**
     * 创建运输记录实现
     * 预留业务逻辑检查点
     */
    @Override
    @Transactional
    public void createTransport(BizTransport transport) {
        // 1. 检查库存并扣减
        if (transport.getGoodsId() != null) {
            BaseGoods goods = baseGoodsMapper.selectById(transport.getGoodsId());
            if (goods == null) {
                throw new RuntimeException("货物不存在");
            }

            int requestQty = transport.getQuantity() != null ? transport.getQuantity() : 1;
            // 确保 quantity 至少为 1
            if (transport.getQuantity() == null) {
                transport.setQuantity(1);
            }

            // 检查库存
            int currentStock = goods.getStock() != null ? goods.getStock() : 0;
            if (currentStock < requestQty) {
                throw new RuntimeException("库存不足，无法创建运单 (当前库存: " + currentStock + ", 请求数量: " + requestQty + ")");
            }

            // 扣减库存
            goods.setStock(currentStock - requestQty);
            baseGoodsMapper.update(goods);
        }

        // 自动生成运单号 (如果未提供)
        if (transport.getTransportNo() == null || transport.getTransportNo().isEmpty()) {
            transport.setTransportNo("T" + System.currentTimeMillis());
        }

        // 默认状态为 0 (待运输)
        if (transport.getStatus() == null) {
            transport.setStatus(0);
        }

        // 设置创建人
        Long currentUserId = SecurityUtils.getUserId();
        if (currentUserId != null) {
            transport.setCreatedBy(currentUserId);
        }

        bizTransportMapper.insert(transport);
    }

    @Override
    @Transactional
    public void updateTransport(BizTransport transport) {

        bizTransportMapper.update(transport);
    }

    @Override
    @Transactional
    public void deleteTransport(Long id) {
        bizTransportMapper.deleteById(id);
    }

    @Override
    @Transactional
    public void startTransport(Long id) {

        BizTransport transport = bizTransportMapper.selectById(id);
        if (transport == null) {
            throw new RuntimeException("运单不存在");
        }
        if (transport.getStatus() != 0) {
            throw new RuntimeException("当前状态不可开始运输，仅支持从[待运输]状态开始");
        }

        transport.setStatus(1); // 运输中
        transport.setStartTime(LocalDateTime.now());
        bizTransportMapper.update(transport);
    }

    @Override
    @Transactional
    public void completeTransport(Long id) {

        BizTransport transport = bizTransportMapper.selectById(id);
        if (transport == null) {
            throw new RuntimeException("运单不存在");
        }
        if (transport.getStatus() != 1) {
            throw new RuntimeException("当前状态不可完成运输，仅支持从[运输中]状态完成");
        }

        transport.setStatus(2); // 已送达
        transport.setEndTime(LocalDateTime.now());
        bizTransportMapper.update(transport);
    }

    @Override
    @Transactional
    public void cancelTransport(Long id) {

        // 检查运单状态是否为待运输
        BizTransport transport = bizTransportMapper.selectById(id);
        if (transport == null) {
            throw new RuntimeException("运单不存在");
        }
        // 检查运单状态是否为待运输
        if (transport.getStatus() != 0) {
            throw new RuntimeException("当前状态不可取消，仅支持取消[待运输]状态的运单");
        }
        // 设置运单状态为已取消
        transport.setStatus(3); // 已取消
        bizTransportMapper.update(transport);

        // 回滚库存
        if (transport.getGoodsId() != null) {
            BaseGoods goods = baseGoodsMapper.selectById(transport.getGoodsId());
            if (goods != null) {
                int quantityToRestore = transport.getQuantity() != null ? transport.getQuantity() : 0;
                if (quantityToRestore > 0) {
                    int currentStock = goods.getStock() != null ? goods.getStock() : 0;
                    goods.setStock(currentStock + quantityToRestore);
                    baseGoodsMapper.update(goods);
                }
            }
        }
    }
}
