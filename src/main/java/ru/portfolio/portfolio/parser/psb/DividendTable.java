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
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import ru.portfolio.portfolio.parser.*;
import ru.portfolio.portfolio.pojo.CashFlowType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;

import static ru.portfolio.portfolio.parser.psb.DividendTable.DividendTableHeader.*;

@Slf4j
public class DividendTable extends AbstractReportTable<DividendTable.DividendTableRow> {
    private static final String TABLE_NAME = "Выплата дивидендов";

    public DividendTable(PsbBrokerReport report) {
        super(report, TABLE_NAME, "", DividendTableHeader.class);
    }

    @Override
    protected Collection<DividendTableRow> getRow(ExcelTable table, Row row) {
        DividendTableRow.DividendTableRowBuilder builder = DividendTableRow.builder()
                .timestamp(convertToInstant(table.getStringCellValue(row, DATE)))
                .event(CashFlowType.DIVIDEND)
                .isin(table.getStringCellValue(row, ISIN))
                .count(table.getIntCellValue(row, COUNT))
                .value(table.getCurrencyCellValue(row, VALUE))
                .currency(table.getStringCellValue(row, CURRENCY));
        Collection<DividendTableRow> data = new ArrayList<>();
        data.add(builder.build());
        BigDecimal tax = table.getCurrencyCellValue(row, TAX).negate();
        if (!tax.equals(BigDecimal.ZERO)) {
            data.add(builder
                    .event(CashFlowType.TAX)
                    .value(tax)
                    .build());
        }
        return data;
    }

    enum DividendTableHeader implements TableColumnDescription {
        DATE("дата"),
        ISIN("isin"),
        COUNT("кол-во"),
        VALUE("сумма", "дивидендов"),
        CURRENCY("валюта", "выплаты"),
        TAX("сумма", "налога");

        @Getter
        private final TableColumn column;
        DividendTableHeader(String... words) {
            this.column = TableColumnImpl.of(words);
        }
    }

    @Getter
    @Builder(toBuilder = true)
    @EqualsAndHashCode
    public static class DividendTableRow {
        private String isin;
        private Instant timestamp;
        private CashFlowType event;
        private int count;
        private BigDecimal value; // НКД, амортизация или налог
        private String currency; // валюта
    }
}
