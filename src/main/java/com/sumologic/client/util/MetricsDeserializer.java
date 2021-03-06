package com.sumologic.client.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.sumologic.client.metrics.model.CreateMetricsJobResponse;
import com.sumologic.client.metrics.model.Metric;
import org.joda.time.DateTime;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

public class MetricsDeserializer extends StdDeserializer<CreateMetricsJobResponse> {

  public MetricsDeserializer() {
    super(CreateMetricsJobResponse.class);
  }

  @Override
  public CreateMetricsJobResponse deserialize(JsonParser parser, DeserializationContext deserializer) throws IOException {
    CreateMetricsJobResponse metricsResponse = new CreateMetricsJobResponse();
    ObjectCodec codec = parser.getCodec();
    JsonNode root = codec.readTree(parser);

    JsonNode response = root.get("response");
    JsonNode error = root.get("error");
    JsonNode errorMessage = root.get("errorMessage");
    JsonNode results = response.findValue("results");
    JsonNode sessionId = root.path("queryInfo").path("sessionIdStr");
    JsonNode startTime = root.path("queryInfo").path("startTime");
    JsonNode endTime = root.path("queryInfo").path("endTime");

    metricsResponse.setResponse(response.toString());
    metricsResponse.setSessionId(sessionId.toString());
    metricsResponse.setStartTime(startTime.asLong());
    metricsResponse.setEndTime(endTime.asLong());
    metricsResponse.setError(error.toString());
    metricsResponse.setErrorMessage(errorMessage.toString());

    if ((results != null) && (results.size() > 0)) {
      for (JsonNode v : results) {
        Metric m = new Metric();
        m.setDimensions(parseDimensions(v));
        m.setTimestamps(parseTimestamps(v));
        m.setValues(parseValues(v.path("datapoints")));
        metricsResponse.addMetric(m);
      }
      metricsResponse.resetEmpty();
    }

    return metricsResponse;
  }

  private DateTime[] parseTimestamps(JsonNode metric) {
    JsonNode timestamp = metric.findValue("timestamp");
    DateTime[] timestamps = new DateTime[timestamp.size()];
    int idx = 0;

    for (JsonNode v : timestamp) {
      timestamps[idx] = new DateTime(v.asLong());
      idx++;
    }
    return timestamps;
  }

  private ArrayList<Double> parseValues(JsonNode metric) {
    JsonNode value = metric.findValue("value");
    ArrayList<Double> values = new ArrayList<>(value.size());

    for (JsonNode v : value) {
      values.add(v.asDouble());
    }
    return values;
  }

  private HashMap<String, String> parseDimensions(JsonNode metric) {
    JsonNode dim = metric.findValue("dimensions");
    HashMap<String, String> dimensions = new HashMap<>();

    for (JsonNode v : dim) {
      dimensions.put(v.findValue("key").toString(), v.findValue("value").toString());
    }
    return dimensions;
  }
}
