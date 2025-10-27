package com.fintech.dataproviders;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.testng.annotations.DataProvider;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class JsonDataProvider {

    @DataProvider(name = "transferDataJson")
    public static Iterator<Object[]> getTransferData() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        List<Map<String, Object>> data = mapper.readValue(
            new File("src/test/resources/testdata/transferData.json"),
            new TypeReference<List<Map<String, Object>>>() {}
        );

        return data.stream()
            .map(entry -> new Object[] {
                entry.get("scenarioLabel"),
                entry.get("amount"),
                entry.get("expectSuccess")
            })
            .iterator();
    }
}
