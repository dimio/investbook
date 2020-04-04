/*
 * Portfolio
 * Copyright (C) 2020  Vitalii Ananev <an-vitek@ya.ru>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ru.portfolio.portfolio.parser.psb;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.util.CellAddress;
import ru.portfolio.portfolio.parser.BrokerReport;
import ru.portfolio.portfolio.parser.ExcelTable;
import ru.portfolio.portfolio.parser.ExcelTableHelper;
import ru.portfolio.portfolio.parser.ReportTable;
import ru.portfolio.portfolio.pojo.PortfolioPropertyType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class PortfolioPropertyTable implements ReportTable<PortfolioPropertyTable.PortfolioPropertyRow> {
    private static final String ASSETS = "\"СУММА АКТИВОВ\" на конец дня";
    @Getter
    private final BrokerReport report;
    @Getter
    private final List<PortfolioPropertyTable.PortfolioPropertyRow> data = new ArrayList<>();


    protected PortfolioPropertyTable(PsbBrokerReport report) {
        this.report = report;
        this.data.addAll(getRow(report));
    }

    protected Collection<PortfolioPropertyRow> getRow(PsbBrokerReport report) {
        CellAddress address = ExcelTableHelper.find(report.getSheet(), ASSETS);
        if (address == ExcelTableHelper.NOT_FOUND) {
            return Collections.emptyList();
        }
        CellAddress assestsAddr = ExcelTableHelper.findByPredicate(
                report.getSheet(),
                address.getRow(),
                cell -> cell.getCellType() == CellType.NUMERIC);
        if (assestsAddr == ExcelTableHelper.NOT_FOUND) {
            return Collections.emptyList();
        }

        return Collections.singletonList(
                PortfolioPropertyRow.builder()
                .property(PortfolioPropertyType.TOTAL_ASSETS)
                .value(ExcelTable.getCurrencyCellValue(ExcelTableHelper.getCell(report.getSheet(), assestsAddr)))
                .build());
    }

    @Getter
    @Builder
    @EqualsAndHashCode
    public static class PortfolioPropertyRow {
        private final PortfolioPropertyType property;
        private final Object value;
    }
}
