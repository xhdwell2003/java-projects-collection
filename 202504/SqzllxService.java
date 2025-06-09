public void saveSqzllx(String systemId, String indexId, String mnIndexId, String user) {
    List<PreMonCommand> dataList = preMonCommandDao.queryPreMonCommandListByIndexId(systemId, indexId);
    List<PreMonCommand> mnDataList = preMonCommandDao.queryPreMonCommandListByIndexId(systemId, mnIndexId);
    
    // 创建一个新的列表来存储需要添加的记录
    List<PreMonCommand> toInsertList = new ArrayList<>();
    
    for (PreMonCommand preMonCommand : dataList) {
        // 如果没有对应的类型的事前指令则添加
        if (mnDataList.stream().noneMatch(mn -> mn.getPreCommandType().equals(preMonCommand.getPreCommandType()))) {
            PreMonCommand newCommand = new PreMonCommand();
            // 复制原始对象的属性到新对象，避免修改原始对象
            BeanUtils.copyProperties(preMonCommand, newCommand);
            newCommand.setIndexCode(mnIndexId);
            newCommand.setOptUser(user);
            toInsertList.add(newCommand);
        }
    }
    
    // 只插入需要添加的记录
    if (!toInsertList.isEmpty()) {
        preMonCommandDao.batchInsert(toInsertList);
    }
}