package com.prodeca.services;

import com.prodeca.data.Product;
import com.prodeca.data.ProductRepository;
import com.prodeca.data.Supplier;
import com.prodeca.data.SupplierRepository;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProductDataTransferService {
    
    private final ProductRepository productRepository;
    private final SupplierRepository supplierRepository;

    public ProductDataTransferService(ProductRepository productRepository, SupplierRepository supplierRepository) {
        this.productRepository = productRepository;
        this.supplierRepository = supplierRepository;
    }

    // Palautetaan kaikki tuotteet CSV-tavutaulukossa
    @Transactional(readOnly = true)
    public byte[] exportToCSV() throws IOException {
        List<Product> products = this.productRepository.findAll();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (CSVWriter writer = new CSVWriter(new OutputStreamWriter(baos, "UTF-8"))) {
            // Otsikkorivi
            writer.writeNext(new String[]{"ID","Nimi","SKU","Hinta","Varastossa","Kategoria","Paino","Aktiivinen","Toimittaja"});

            // Tuoterivit
            for (Product product : products) {
                writer.writeNext(new String[]{
                    String.valueOf(product.getId()),
                    product.getName(),
                    product.getSku(),
                    product.getUnitPrice() != null ? product.getUnitPrice().toString() : "",
                    product.getStockQuantity() != null ? product.getStockQuantity().toString() : "",
                    product.getCategory(),
                    product.getWeight() != null ? product.getWeight().toString() : "",
                    product.isActive() ? "Kyllä" : "Ei",
                    product.getSupplier() != null ? product.getSupplier().getName() : ""
                });
            }
        }
        return baos.toByteArray();
    }

    // Palautetaan kaikki tuoteet XLSX-tavutaulukkona
    @Transactional(readOnly = true)
    public byte[] exportToExcel() throws IOException {
        List<Product> products = this.productRepository.findAll();
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Tuotteet");

            // Otsikkorivi
            String[] headers = {"ID","Nimi","SKU","Hinta","Varastossa","Kategoria","Paino","Aktiivinen","Toimittaja"};
            Row headerRow = sheet.createRow(0);
            CellStyle headerStyle = wb.createCellStyle();
            Font headerFont = wb.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Tuoterivit
            int rowNum = 1;
            for (Product product : products) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(product.getId());
                row.createCell(1).setCellValue(product.getName());
                row.createCell(2).setCellValue(product.getSku());
                row.createCell(3).setCellValue(product.getUnitPrice() != null ? product.getUnitPrice().doubleValue() : 0);
                row.createCell(4).setCellValue(product.getStockQuantity() != null ? product.getStockQuantity() : 0);
                row.createCell(5).setCellValue(product.getCategory());
                row.createCell(6).setCellValue(product.getWeight() != null ? product.getWeight().doubleValue() : 0);
                row.createCell(7).setCellValue(product.isActive() ? "Kyllä" : "Ei");
                row.createCell(8).setCellValue(product.getSupplier() != null ? product.getSupplier().getName() : "");
            }
            for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            wb.write(baos);
            return baos.toByteArray();
        }
    }

    // Tuodaan tuotteet CSV-tiedostosta (tavutaulukko). Ensimmäinen rivi ohitetaan (oletetaan, että se on otsikkorivi)
    @Transactional
    public List<String> importFromCSV(byte[] csvBytes) throws IOException, CsvException {
        List<String> results = new ArrayList<>();
        try (CSVReader reader = new CSVReader(new InputStreamReader(new ByteArrayInputStream(csvBytes), "UTF-8"))) {
            List<String[]> rows = reader.readAll();
            if (rows.size() < 2) {
                results.add("Tiedosto on tyhjä tai sisältää vain otsikkorivin");
                return results;
            }
            // Ohitetaan ensimmäinen rivi (otsikkorivi)
            for (int i = 1; i < rows.size(); i++) {
                String[] row = rows.get(i);

                // Vaaditut sarakkeet: Nimi, SKU, Hinta, Varastossa, Kategoria (indeksit 1-5)
                if (row.length < 6) {
                    results.add("Rivi " + i + " ohitettu: liian vähän sarakkeita (" + row.length + ")");
                    continue;
                }

                try {
                    Product product = new Product();
                    product.setName(row[1].trim());
                    product.setSku(row[2].trim().toUpperCase());
                    product.setUnitPrice(new BigDecimal(row[3].trim().replace(",", ".")));
                    product.setStockQuantity(Integer.parseInt(row[4].trim()));
                    product.setCategory(row[5].trim());

                    if (row.length > 6 && !row[6].isBlank()) product.setWeight(new BigDecimal(row[6].trim().replace(",", ".")));

                    if (row.length > 7 && !row[7].isBlank()) {
                        product.setActive(row[7].trim().equalsIgnoreCase("Kyllä") || row[7].trim().equalsIgnoreCase("true"));
                    } else {
                        product.setActive(true);
                    }

                    if (row.length > 8 && !row[8].isBlank()) {
                        String supplierName = row[8].trim();
                        Supplier supplier = this.supplierRepository.findByNameIgnoreCase(supplierName).orElse(null);

                        if (supplier == null) {
                            results.add("Rivi " + i + " ohitettu: toimittajaa '" + supplierName + "' ei löydy tietokannasta");
                            continue;
                        }
                        product.setSupplier(supplier);
                    } else {
                        results.add("Rivi " + i + " ohitettu: toimittaja puuttuu");
                        continue;
                    }

                    if (product.getName().isBlank() || product.getSku().isBlank()) {
                        results.add("Rivi " + i + " ohitettu: nimi tai tuotekoodi puuttuu");
                        continue;
                    }

                    this.productRepository.save(product);
                    results.add("Rivi " + i + " tuotu: " + product.getName());
                } catch (NumberFormatException e) {
                    results.add("Virhe tapahtui käsitellessä riviä " + i + ": " + e.getMessage());
                }
            }
        }
        return results;
    }
}