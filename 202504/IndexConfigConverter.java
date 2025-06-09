public class IndexConfigConverter {

    public IndexConfigRsp buildndexConfig(IndexConfigPo indexConfigPo) {
        if (indexConfigPo == null) {
            return null;
        }

        IndexConfigRsp indexConfigRsp = new IndexConfigRsp();
        
        // 转换IndexInfo
        if (indexConfigPo.getIndexInfo() != null) {
            indexConfigRsp.setIndexInfoRsp(convertIndexInfo(indexConfigPo.getIndexInfo()));
        }
        
        // 转换IndexInstance
        if (indexConfigPo.getIndexInstance() != null) {
            indexConfigRsp.setIndexInstanceRsp(convertIndexInstance(indexConfigPo.getIndexInstance()));
        }
        
        // 转换Thresholds
        if (indexConfigPo.getThresholds() != null) {
            List<ThresholdRsp> thresholdRsps = indexConfigPo.getThresholds().stream()
                    .map(this::convertThreshold)
                    .collect(Collectors.toList());
            indexConfigRsp.setThresholdRsps(thresholdRsps);
        }
        
        // 转换LimitCondtitons
        if (indexConfigPo.getLimitCondtitons() != null) {
            List<LimitCondtitonRsp> limitCondtitonRsps = indexConfigPo.getLimitCondtitons().stream()
                    .map(this::convertLimitCondtiton)
                    .collect(Collectors.toList());
            indexConfigRsp.setLimitCondtitonRsps(limitCondtitonRsps);
        }
        
        // 转换IndexExprassions
        if (indexConfigPo.getIndexExprassions() != null) {
            List<IndexExprassionRsp> indexExprassionRsps = indexConfigPo.getIndexExprassions().stream()
                    .map(this::convertIndexExprassion)
                    .collect(Collectors.toList());
            indexConfigRsp.setIndexExprassionRsps(indexExprassionRsps);
        }
        
        // 转换DataItemMap
        if (indexConfigPo.getDataItemMap() != null) {
            Map<String, List<DataItemRsp>> dataItemRspMap = new HashMap<>();
            indexConfigPo.getDataItemMap().forEach((key, dataItems) -> {
                List<DataItemRsp> dataItemRsps = dataItems.stream()
                        .map(this::convertDataItem)
                        .collect(Collectors.toList());
                dataItemRspMap.put(key, dataItemRsps);
            });
            indexConfigRsp.setDataItemMap(dataItemRspMap);
        }
        
        return indexConfigRsp;
    }
    
    private IndexInfoRsp convertIndexInfo(IndexInfoPo indexInfoPo) {
        if (indexInfoPo == null) {
            return null;
        }
        
        IndexInfoRsp indexInfoRsp = new IndexInfoRsp();
        indexInfoRsp.setKey(indexInfoPo.getKey());
        indexInfoRsp.setIndexId(indexInfoPo.getIndexId());
        indexInfoRsp.setName(indexInfoPo.getName());
        indexInfoRsp.setDescription(indexInfoPo.getDescription());
        indexInfoRsp.setMonObjectType(indexInfoPo.getMonObjectType());
        indexInfoRsp.setMajorClass(indexInfoPo.getMajorClass());
        indexInfoRsp.setBehavior(indexInfoPo.getBehavior());
        indexInfoRsp.setNewBehavior(indexInfoPo.getNewBehavior());
        indexInfoRsp.setPrdType(indexInfoPo.getPrdType());
        indexInfoRsp.setMoment(indexInfoPo.getMoment());
        indexInfoRsp.setOpenCloseConf(indexInfoPo.getOpenCloseConf());
        indexInfoRsp.setOpenCloseConfDetail(indexInfoPo.getOpenCloseConfDetail());
        indexInfoRsp.setLimitedIllegalDays(indexInfoPo.getLimitedIllegalDays());
        indexInfoRsp.setIndexType(indexInfoPo.getIndexType());
        indexInfoRsp.setNumeratorID(indexInfoPo.getNumeratorID());
        indexInfoRsp.setDenominatorID(indexInfoPo.getDenominatorID());
        indexInfoRsp.setMonType(indexInfoPo.getMonType());
        indexInfoRsp.setNumeratorGranularity(indexInfoPo.getNumeratorGranularity());
        indexInfoRsp.setDenominatorGranularity(indexInfoPo.getDenominatorGranularity());
        
        return indexInfoRsp;
    }
    
    private IndexInstanceRsp convertIndexInstance(MonIndexInstance indexInstance) {
        if (indexInstance == null) {
            return null;
        }
        
        IndexInstanceRsp indexInstanceRsp = new IndexInstanceRsp();
        indexInstanceRsp.setInstanceId(indexInstance.getInstanceId());
        indexInstanceRsp.setIndexId(indexInstance.getIndexId());
        indexInstanceRsp.setSystemId(indexInstance.getSystemId());
        indexInstanceRsp.setMonObject(indexInstance.getMonObject());
        indexInstanceRsp.setMonObjectType(indexInstance.getMonObjectType());
        indexInstanceRsp.setMonLevel(indexInstance.getMonLevel());
        indexInstanceRsp.setStartDate(indexInstance.getStartDate());
        indexInstanceRsp.setEndDate(indexInstance.getEndDate());
        indexInstanceRsp.setOptUser(indexInstance.getOptUser());
        indexInstanceRsp.setChkUser(indexInstance.getChkUser());
        indexInstanceRsp.setState(indexInstance.getState());
        indexInstanceRsp.setCreateUser(indexInstance.getCreateUser());
        indexInstanceRsp.setUpdateUser(indexInstance.getUpdateUser());
        indexInstanceRsp.setCreateTime(indexInstance.getCreateTime());
        indexInstanceRsp.setUpdateTime(indexInstance.getUpdateTime());
        indexInstanceRsp.setUseState(indexInstance.getUseState());
        indexInstanceRsp.setSuccessNum(indexInstance.getSuccessNum());
        indexInstanceRsp.setPreSuccessNum(indexInstance.getPreSuccessNum());
        indexInstanceRsp.setPreMon(indexInstance.getPreMon());
        indexInstanceRsp.setIsPenerated(indexInstance.getIsPenerated());
        indexInstanceRsp.setPrdUnion(indexInstance.getPrdUnion());
        indexInstanceRsp.setGroupId(indexInstance.getGroupId());
        indexInstanceRsp.setUnionType(indexInstance.getUnionType());
        indexInstanceRsp.setUnionInstanceId(indexInstance.getUnionInstanceId());
        indexInstanceRsp.setPreChkUser(indexInstance.getPreChkUser());
        
        return indexInstanceRsp;
    }
    
    private ThresholdRsp convertThreshold(MonThreshold threshold) {
        if (threshold == null) {
            return null;
        }
        
        ThresholdRsp thresholdRsp = new ThresholdRsp();
        thresholdRsp.setThresholdId(threshold.getThresholdId());
        thresholdRsp.setSystemId(threshold.getSystemId());
        thresholdRsp.setInstanceId(threshold.getInstanceId());
        thresholdRsp.setConditionType(threshold.getConditionType());
        thresholdRsp.setOutputColumn(threshold.getOutputColumn());
        thresholdRsp.setUnit(threshold.getUnit());
        thresholdRsp.setParameter(threshold.getParameter());
        thresholdRsp.setCreateUser(threshold.getCreateUser());
        thresholdRsp.setUpdateUser(threshold.getUpdateUser());
        thresholdRsp.setCreateTime(threshold.getCreateTime());
        thresholdRsp.setUpdateTime(threshold.getUpdateTime());
        
        return thresholdRsp;
    }
    
    private LimitCondtitonRsp convertLimitCondtiton(MonLimitCondtiton limitCondtiton) {
        if (limitCondtiton == null) {
            return null;
        }
        
        LimitCondtitonRsp limitCondtitonRsp = new LimitCondtitonRsp();
        limitCondtitonRsp.setConditionId(limitCondtiton.getConditionId());
        limitCondtitonRsp.setInstanceId(limitCondtiton.getInstanceId());
        limitCondtitonRsp.setSystemId(limitCondtiton.getSystemId());
        limitCondtitonRsp.setConditionExp(limitCondtiton.getConditionExp());
        limitCondtitonRsp.setCreateUser(limitCondtiton.getCreateUser());
        limitCondtitonRsp.setUpdateUser(limitCondtiton.getUpdateUser());
        limitCondtitonRsp.setCreateTime(limitCondtiton.getCreateTime());
        limitCondtitonRsp.setUpdateTime(limitCondtiton.getUpdateTime());
        
        return limitCondtitonRsp;
    }
    
    private IndexExprassionRsp convertIndexExprassion(MonIndexExprassion indexExprassion) {
        if (indexExprassion == null) {
            return null;
        }
        
        IndexExprassionRsp indexExprassionRsp = new IndexExprassionRsp();
        indexExprassionRsp.setIndexId(indexExprassion.getIndexId());
        indexExprassionRsp.setMetricGroupId(indexExprassion.getMetricGroupId());
        indexExprassionRsp.setSystemId(indexExprassion.getSystemId());
        indexExprassionRsp.setExpType(indexExprassion.getExpType());
        indexExprassionRsp.setExp(indexExprassion.getExp());
        indexExprassionRsp.setExpDescription(indexExprassion.getExpDescription());
        indexExprassionRsp.setOptUser(indexExprassion.getOptUser());
        indexExprassionRsp.setChkUser(indexExprassion.getChkUser());
        indexExprassionRsp.setState(indexExprassion.getState());
        indexExprassionRsp.setCreateUser(indexExprassion.getCreateUser());
        indexExprassionRsp.setUpdateUser(indexExprassion.getUpdateUser());
        indexExprassionRsp.setCreateTime(indexExprassion.getCreateTime());
        indexExprassionRsp.setUpdateTime(indexExprassion.getUpdateTime());
        indexExprassionRsp.setTableInfo(indexExprassion.getTableInfo());
        indexExprassionRsp.setExprItemList(indexExprassion.getExprItemList());
        
        return indexExprassionRsp;
    }
    
    private DataItemRsp convertDataItem(DataItem dataItem) {
        if (dataItem == null) {
            return null;
        }
        
        DataItemRsp dataItemRsp = new DataItemRsp();
        dataItemRsp.setDataItemId(dataItem.getDataItemId());
        dataItemRsp.setDataItemName(dataItem.getDataItemName());
        dataItemRsp.setSystemId(dataItem.getSystemId());
        dataItemRsp.setOriginId(dataItem.getOriginId());
        dataItemRsp.setOriginType(dataItem.getOriginType());
        dataItemRsp.setDataItemField(dataItem.getDataItemField());
        dataItemRsp.setMonObjectMark(dataItem.getMonObjectMark());
        dataItemRsp.setSubDimensionMark(dataItem.getSubDimensionMark());
        dataItemRsp.setDataSourceType(dataItem.getDataSourceType());
        dataItemRsp.setDataSource(dataItem.getDataSource());
        dataItemRsp.setDataSourceDescription(dataItem.getDataSourceDescription());
        dataItemRsp.setOutColumMark(dataItem.getOutColumMark());
        dataItemRsp.setDataType(dataItem.getDataType());
        dataItemRsp.setState(dataItem.getState());
        dataItemRsp.setCreateUser(dataItem.getCreateUser());
        dataItemRsp.setUpdateUser(dataItem.getUpdateUser());
        dataItemRsp.setOptUser(dataItem.getOptUser());
        dataItemRsp.setChkUser(dataItem.getChkUser());
        dataItemRsp.setCreateTime(dataItem.getCreateTime());
        dataItemRsp.setUpdateTime(dataItem.getUpdateTime());
        
        return dataItemRsp;
    }
}