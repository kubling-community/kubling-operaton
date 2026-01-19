package com.kubling.samples.operaton.config;

import lombok.extern.slf4j.Slf4j;
import org.operaton.bpm.engine.impl.util.ReflectUtil;
import org.operaton.bpm.engine.spring.SpringProcessEngineConfiguration;

import java.io.InputStream;

@Slf4j
public class KublingProcessEngineConfiguration extends SpringProcessEngineConfiguration {

    @Override
    public void initDatabaseType() {
        databaseType = "h2";
    }

    @Override
    public String getDatabaseType() {
        return "h2";
    }

    @Override
    protected InputStream getMyBatisXmlConfigurationSteam() {
        return ReflectUtil.getResourceAsStream("com/kubling/operaton/bpm/engine/impl/mapping/mappings.xml");
    }

}
