package com.back.global.jpa.replication;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Random;

public class DataSourceRouter extends AbstractRoutingDataSource {
    /*
    * 0 - Source DB - 쓰기 작업
    * 1 - Replica DB 1 - 읽기 작업
    * 2 - Replica DB 2 - 읽기 작업
    *  */

    private  final Random random = new Random();

    @Override
    protected Object determineCurrentLookupKey() {
        // 현재 트렌젝션이 읽기 전용인지 확인
        // @Transactional(readOnly = true) 설정된 경우 true 반환
        if (TransactionSynchronizationManager.isCurrentTransactionReadOnly()) {
            //  Replica DB 1 , 2 중 하나를 무작위로 선택
            // random.nextInt(2) : 0 또는 1 반환
            // + 1 을 통해 1 또는 2 반환
            return random.nextInt(2) + 1;
        }

        // 쓰기 작업인 경우 Source DB 사용
        // @Transactional(readOnly = false) 또는 @Transactional 설정된 경우
        return 0;
    }
}
