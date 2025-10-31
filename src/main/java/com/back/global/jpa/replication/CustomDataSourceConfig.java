package com.back.global.jpa.replication;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.*;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * Database Replication 을 위한 Source DataSource 설정
 * Source DB 1개(쓰기)
 * Replica DB 2개(읽기, 랜덤분산)
 */

@Profile("prod")
@Configuration
public class CustomDataSourceConfig {

    /**
     * Source DataSource Bean 생성
     *  custom.datasource.source 설정을 자동 바인딩
     *  return HikariCP 커넥션 풀을 사용하는 Source DataSource
     */
    @Bean
    @ConfigurationProperties(prefix = "custom.datasource.source")
    public DataSource sourceDataSource() {
        return DataSourceBuilder.create()
                .type(HikariDataSource.class)
                .build();
    }


    /**
     * replica1 DataSource Bean 생성
     *  custom.datasource.source 설정을 자동 바인딩
     *  return HikariCP 커넥션 풀을 사용하는 replica1 DataSource
     */
    @Bean
    @ConfigurationProperties(prefix = "custom.datasource.replica1")
    public DataSource replica1DataSource() {
        return DataSourceBuilder.create()
                .type(HikariDataSource.class)
                .build();
    }

    /**
     * replica1 DataSource Bean 생성
     *  custom.datasource.source 설정을 자동 바인딩
     *  return HikariCP 커넥션 풀을 사용하는 replica2 DataSource
     */
    @Bean
    @ConfigurationProperties(prefix = "custom.datasource.replica2")
    public DataSource replica2DataSource() {
        return DataSourceBuilder.create()
                .type(HikariDataSource.class)
                .build();
    }

    /**
     * DataSourceRouter 라우팅 설정
     */

    @Bean
    @DependsOn({"sourceDataSource", "replica1DataSource", "replica2DataSource"})
    public DataSource routeDataSource() {
        // 1. DataSourceRouter 인스턴스 생성
        DataSourceRouter dataSourceRouter = new DataSourceRouter();

        // 2. 각 DataSource Bean 가지고 오기
        DataSource sourceDataSource = sourceDataSource();
        DataSource replica1DataSource = replica1DataSource();
        DataSource replica2DataSource = replica2DataSource();

        // 3. DataSource 맵핑 설정
        // Key: DataSourceRouter 에서 반환하는 키 값 (0, 1, 2)
        Map<Object, Object> dataSourceMap = new HashMap<>();
        dataSourceMap.put(0, sourceDataSource);      // 쓰기 작업용 Source DB
        dataSourceMap.put(1, replica1DataSource);    // 읽기 작업용 Replica DB 1
        dataSourceMap.put(2, replica2DataSource);    // 읽기 작업용 Replica DB 2

        // 4. DataSourceRouter 에 DataSource 맵 등록
        dataSourceRouter.setTargetDataSources(dataSourceMap);

        // 5. 기본 DataSource 설정 (라우팅 실패 시 fallback)
        // lookup key를 결정할 수 없거나 맵에 없는 key인 경우 Source DB 사용
        dataSourceRouter.setDefaultTargetDataSource(sourceDataSource);

        return dataSourceRouter;
    }

    /**
     * 최종 DataSource Bean 생성 (애플리케이션에서 실제 주입받는 Bean)
     * LazyConnectionDataSourceProxy  실제 DB 연결 지연
     * LazyConnectionDataSourceProxy 사용 이유
     * 문제: 트렌잭션 시작 전에는 readOnly 여부를 알 수 없음
     * 해결: 실제 DB 연결 트렌잭션 시작 후로 지연
     * 효과: 트렌잭션 정보 확인 후 적절한 DataSource 선택 가능
     *
     * 동작순서
     * 1. 트랜잭션 시작 ( @Transactional 어노테이션 적용 시점 )
     * 2. LazyProxy 가 실제 DB 연결 요청 대기
     * 3. readOnly 여부 확인
     * 4. DataSourceRouter 가 적절한 DataSource 선택
     * 5. 실제 DB 연결 수행
     * 6. 쿼리 실행
     *
     */
    @Bean
    @Primary
    @DependsOn("routeDataSource")
    public DataSource dataSource() {
        return new LazyConnectionDataSourceProxy(routeDataSource());
    }
}
