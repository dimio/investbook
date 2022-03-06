/*
 * InvestBook
 * Copyright (C) 2022  Vitalii Ananev <spacious-team@ya.ru>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ru.investbook.web;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.investbook.report.ViewFilter;
import ru.investbook.report.excel.ExcelView;
import ru.investbook.repository.PortfolioRepository;
import ru.investbook.web.model.ViewFilterModel;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutionException;

import static java.time.ZoneId.systemDefault;
import static ru.investbook.web.ControllerHelper.getActivePortfolios;
import static ru.investbook.web.ControllerHelper.getPortfolios;
import static ru.investbook.web.HttpAttachResponseHelper.sendErrorPage;
import static ru.investbook.web.HttpAttachResponseHelper.sendSuccessHeader;

@Controller
@RequestMapping("/portfolio")
@RequiredArgsConstructor
@Slf4j
public class InvestbookReportController {

    private static final String REPORT_NAME = "investbook";
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private final PortfolioRepository portfolioRepository;
    private final ExcelView excelView;
    private volatile int expectedFileSize = 0xFFFF;

    @GetMapping("/select-period")
    public String getPage(Model model, @ModelAttribute("viewFilter") ViewFilterModel viewFilter) {
        viewFilter.setPortfolios(getActivePortfolios(portfolioRepository));
        model.addAttribute("allPortfolios", getPortfolios(portfolioRepository));
        return "select-period";
    }

    @GetMapping
    public void buildInvestbookReportByGet(HttpServletResponse response) throws IOException {
        buildInvestbookReport(new ViewFilterModel(), response);
    }

    @PostMapping
    public void buildInvestbookReport(@ModelAttribute("viewFilter") ViewFilterModel viewFilter,
                                      HttpServletResponse response) throws IOException {
        try {
            long t0 = System.nanoTime();
            String fileName = sendExcelFile(viewFilter, response);
            log.info("Отчет '{}' сформирован за {}", fileName, Duration.ofNanos(System.nanoTime() - t0));
        } catch (Exception e) {
            log.error("Ошибка сборки отчета", e);
            sendErrorPage(response, e);
        }
        response.flushBuffer();
    }

    private String sendExcelFile(ViewFilterModel viewFilterModel, HttpServletResponse response)
            throws IOException, InterruptedException, ExecutionException {
        ViewFilter viewFilter = ViewFilter.of(viewFilterModel, () -> getPortfolios(portfolioRepository));
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(expectedFileSize);
        try (XSSFWorkbook book = new XSSFWorkbook()) {
            excelView.writeTo(book, viewFilter);
            book.write(outputStream);
            expectedFileSize = outputStream.size();
        }
        String fileName = getReportName(viewFilter);
        sendSuccessHeader(response, fileName, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        outputStream.writeTo(response.getOutputStream());
        return fileName;
    }

    private String getReportName(ViewFilter filter) {
        LocalDate fromDate = LocalDate.ofInstant(filter.getFromDate(), systemDefault());
        LocalDate toDate = LocalDate.ofInstant(filter.getToDate(), systemDefault());
        LocalDate maxToDate = LocalDate.now().plusDays(2);
        if (toDate.isAfter(maxToDate)) {
            toDate = maxToDate;
        }
        return REPORT_NAME + " с " + dateFormatter.format(fromDate) + " по " + dateFormatter.format(toDate) + ".xlsx";
    }
}
