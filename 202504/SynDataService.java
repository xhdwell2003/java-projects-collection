@Value("${syn.fof.thread.count:10}")
private int fofSynThreadCount;

@Value("${syn.fof.concurrent.enabled:true}")
private boolean fofSynConcurrentEnabled;

private void fofManagerSyn(final SynDataEntity data, final MonPreSynDetailEntity monPreSynDetail, final List<String> fofCpcode)
            throws BusException {
        List<CpUpdatetimeEntity> fofgzDatetimesHs = new ArrayList<>();
        Lists.partition(fofCpcode, batchNum).forEach(incpcodes ->
                fofgzDatetimesHs.addAll(hsdbSqSynDataDao.getEvlgzUpdatetimes4Fof(incpcodes, data.getGzdate())));
        List<CpUpdatetimeEntity> synFofPrdcodes = new ArrayList<>();

        //强制同步逻辑放入synDataByInterfaceAndBackDatabase中
        synFofPrdcodes = fofgzDatetimesHs;

        if (fofSynConcurrentEnabled) {
            // 多线程并发处理
            processConcurrently(data, monPreSynDetail, synFofPrdcodes);
        } else {
            // 串行处理
            processSerially(data, monPreSynDetail, synFofPrdcodes);
        }
    }

    /**
     * 串行处理同步数据
     */
    private void processSerially(final SynDataEntity data, final MonPreSynDetailEntity monPreSynDetail, 
                                List<CpUpdatetimeEntity> synFofPrdcodes) throws BusException {
        for (CpUpdatetimeEntity synFofPrdcode : synFofPrdcodes) {
            SqSyncDataReq sqSyncDataReq = buildSqSyncDataReq(data, monPreSynDetail, synFofPrdcode);
            synDataByInterfaceAndBackDatabase(data, sqSyncDataReq);
        }
    }

    /**
     * 并发处理同步数据
     */
    private void processConcurrently(final SynDataEntity data, final MonPreSynDetailEntity monPreSynDetail, 
                                   List<CpUpdatetimeEntity> synFofPrdcodes) throws BusException {
        // 从线程池队列获取一个线程池
        ExecutorService executorService = ThreadPoolQueueManager.getInstance().getThreadPool();
        
        // 如果没有可用的线程池，则执行串行处理
        if (executorService == null) {
            log.warn("没有可用的线程池，将使用串行方式处理同步数据");
            processSerially(data, monPreSynDetail, synFofPrdcodes);
            return;
        }
        
        List<Future<?>> futures = new ArrayList<>();
        
        try {
            for (CpUpdatetimeEntity synFofPrdcode : synFofPrdcodes) {
                Future<?> future = executorService.submit(() -> {
                    try {
                        SqSyncDataReq sqSyncDataReq = buildSqSyncDataReq(data, monPreSynDetail, synFofPrdcode);
                        synDataByInterfaceAndBackDatabase(data, sqSyncDataReq);
                    } catch (BusException e) {
                        log.error("并发处理同步数据异常, cpcode: {}, gzdate: {}", synFofPrdcode.getCpcode(), data.getGzdate(), e);
                        throw new RuntimeException(e);
                    }
                    return null;
                });
                futures.add(future);
            }

            // 等待所有线程完成
            for (Future<?> future : futures) {
                try {
                    future.get();
                } catch (InterruptedException | ExecutionException e) {
                    log.error("等待线程完成时发生异常", e);
                    throw new BusException("并发处理同步数据异常: " + e.getMessage());
                }
            }
        } finally {
            // 将线程池归还到队列中，而不是关闭它
            ThreadPoolQueueManager.getInstance().returnThreadPool(executorService);
        }
    }

    /**
     * 构建同步请求对象
     */
    private SqSyncDataReq buildSqSyncDataReq(final SynDataEntity data, final MonPreSynDetailEntity monPreSynDetail,
                                            CpUpdatetimeEntity synFofPrdcode) {
        return SqSyncDataReq.builder()
                .table(data.getTable())
                .conditions(monPreSynDetail.getConditions())
                .synCol(monPreSynDetail.getSynCol())
                .cpcode(synFofPrdcode.getCpcode())
                .gzdate(data.getGzdate())
                .chdate(data.getChdate())
                .zldate(synFofPrdcode.getGzdate())
                .build();
    }