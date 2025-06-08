package com.boycottpro.companies;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.boycottpro.models.Companies;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanResponse;

import java.util.*;
import java.util.stream.Collectors;

public class GetAllCompaniesHandler implements RequestHandler<Map<String, Object>, APIGatewayProxyResponseEvent> {

    private static final String TABLE_NAME = "companies";
    private final DynamoDbClient dynamoDb;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public GetAllCompaniesHandler() {
        this.dynamoDb = DynamoDbClient.create();
    }

    public GetAllCompaniesHandler(DynamoDbClient dynamoDb) {
        this.dynamoDb = dynamoDb;
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(Map<String, Object> input, Context context) {
        try {
            // Scan companies table
            ScanRequest scanRequest = ScanRequest.builder()
                    .tableName(TABLE_NAME)
                    .build();
            ScanResponse scanResponse = dynamoDb.scan(scanRequest);

            // Convert AttributeValue map to Map<String, Object>
            List<Companies> companies = scanResponse.items().stream()
                    .map(this::mapToCompany)
                    .collect(Collectors.toList());

            // Serialize to JSON
            String responseBody = objectMapper.writeValueAsString(companies);

            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(200)
                    .withBody(responseBody);

        } catch (JsonProcessingException e) {
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(500)
                    .withBody("{\"error\": \"Failed to serialize items to JSON\"}");
        } catch (Exception e) {
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(500)
                    .withBody("{\"error\": \"Unexpected server error: " + e.getMessage() + "\"}");
        }
    }

    private Companies mapToCompany(Map<String, AttributeValue> item) {
        Companies company = new Companies();
        company.setCompany_id(item.get("company_id").s());
        company.setCompany_name(item.get("company_name").s());
        company.setDescription(item.getOrDefault("description", AttributeValue.fromS("")).s());
        company.setIndustry(item.getOrDefault("industry", AttributeValue.fromS("")).s());
        company.setCity(item.getOrDefault("city", AttributeValue.fromS("")).s());
        company.setState(item.getOrDefault("state", AttributeValue.fromS("")).s());
        company.setZip(item.getOrDefault("zip", AttributeValue.fromS("")).s());
        company.setEmployees(Integer.parseInt(item.getOrDefault("employees", AttributeValue.fromN("0")).n()));
        company.setRevenue(Long.parseLong(item.getOrDefault("revenue", AttributeValue.fromN("0")).n()));
        company.setValuation(Long.parseLong(item.getOrDefault("valuation", AttributeValue.fromN("0")).n()));
        company.setProfits(Long.parseLong(item.getOrDefault("profits", AttributeValue.fromN("0")).n()));
        company.setStock_symbol(item.getOrDefault("stock_symbol", AttributeValue.fromS("")).s());
        company.setCeo(item.getOrDefault("ceo", AttributeValue.fromS("")).s());
        company.setBoycott_count(Integer.parseInt(item.getOrDefault("boycott_count", AttributeValue.fromN("0")).n()));
        return company;
    }
}
