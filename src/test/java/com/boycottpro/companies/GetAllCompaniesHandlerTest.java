package com.boycottpro.companies;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.boycottpro.models.Companies;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanResponse;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class GetAllCompaniesHandlerTest {

    private static final String TABLE_NAME = "companies";

    @Mock
    private DynamoDbClient dynamoDbMock;


    private GetAllCompaniesHandler companiesHandler;

    @Test
    public void handleRequest_shouldReturnCompanies() throws JsonProcessingException {

        companiesHandler = new GetAllCompaniesHandler(dynamoDbMock);
        // Arrange: Create mocked DynamoDB items
        List<Map<String, AttributeValue>> mockItems = new ArrayList<>();

        Map<String, AttributeValue> item1 = new HashMap<>();
        item1.put("company_id", AttributeValue.builder().s("uuid-1").build());
        item1.put("company_name", AttributeValue.builder().s("Acme Inc").build());
        item1.put("revenue", AttributeValue.builder().n("1000000").build());

        Map<String, AttributeValue> item2 = new HashMap<>();
        item2.put("company_id", AttributeValue.builder().s("uuid-2").build());
        item2.put("company_name", AttributeValue.builder().s("Globex Corp").build());
        item2.put("revenue", AttributeValue.builder().n("2000000").build());

        mockItems.add(item1);
        mockItems.add(item2);

        // Mock ScanResponse
        ScanResponse mockScanResponse = ScanResponse.builder()
                .items(mockItems)
                .build();

        when(dynamoDbMock.scan(any(ScanRequest.class))).thenReturn(mockScanResponse);

        // Act
        APIGatewayProxyResponseEvent response = companiesHandler.handleRequest(new HashMap<>(), null);

        // Deserialize JSON body
        String body = response.getBody();
        ObjectMapper objectMapper = new ObjectMapper();
        List<Companies> result = objectMapper.readValue(body, new TypeReference<List<Companies>>() {});


        // Assert
        assertEquals(2, result.size());

        Companies first = result.get(0);
        assertEquals("uuid-1", first.getCompany_id());
        assertEquals("Acme Inc", first.getCompany_name());
        assertEquals(1000000, first.getRevenue()); // Still string, unless you explicitly cast to number

        Companies second = result.get(1);
        assertEquals("uuid-2", second.getCompany_id());
        assertEquals("Globex Corp", second.getCompany_name());
        assertEquals(2000000, second.getRevenue());
    }

}
