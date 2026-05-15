package com.inkwell.payment;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class PaymentHistoryStore {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
    private final Path historyFile;

    public PaymentHistoryStore(@Value("${payment.history-file:}") String configuredHistoryFile) {
        if (configuredHistoryFile != null && !configuredHistoryFile.isBlank()) {
            this.historyFile = Paths.get(configuredHistoryFile);
        } else {
            this.historyFile = Paths.get(System.getProperty("user.home"), ".inkwell", "payments.json");
        }
    }

    public synchronized void save(PaymentRecord record) {
        List<PaymentRecord> records = readAll();
        records.add(record);
        writeAll(records);
    }

    public synchronized List<PaymentRecord> getBySupporterUserId(Integer userId) {
        return readAll().stream()
                .filter(record -> userId != null && userId.equals(record.getSupporterUserId()))
                .sorted(Comparator.comparing(PaymentRecord::getPaidAt, Comparator.nullsLast(String::compareTo)).reversed())
                .collect(Collectors.toList());
    }

    private List<PaymentRecord> readAll() {
        try {
            if (!Files.exists(historyFile)) {
                return new ArrayList<>();
            }
            return objectMapper.readValue(historyFile.toFile(), new TypeReference<List<PaymentRecord>>() {});
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    private void writeAll(List<PaymentRecord> records) {
        try {
            Files.createDirectories(historyFile.getParent());
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(historyFile.toFile(), records);
        } catch (IOException e) {
            throw new RuntimeException("Failed to persist payment history", e);
        }
    }
}
