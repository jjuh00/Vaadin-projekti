package com.prodeca.views.productinventory;

import com.prodeca.services.ProductDataTransferService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api/products/export")
public class ProductExportController {
    
    private final ProductDataTransferService service;

    public ProductExportController(ProductDataTransferService service) {
        this.service = service;
    }

    // GET /api/products/export/csv
    // Ladataan kaikki tuotteet CSV-tiedostoksi
    @GetMapping("/csv")
    @Secured({"ROLE_USER", "ROLE_SUPER", "ROLE_ADMIN"})
    public ResponseEntity<byte[]> exportCsv() throws IOException {
        byte[] csvData = this.service.exportToCSV();
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"tuotteet.csv\"")
            .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
            .body(csvData);
    }

    // GET /api/products/export/xlsx
    // Ladataan kaikki tuotteet Excel-tiedostoksi
    @GetMapping("/xlsx")
    @Secured({"ROLE_USER", "ROLE_SUPER", "ROLE_ADMIN"})
    public ResponseEntity<byte[]> exportExcel() throws IOException {
        byte[] excelData = this.service.exportToExcel();
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"tuotteet.xlsx\"")
            .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
            .body(excelData);
    }
}