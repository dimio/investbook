/*
 * InvestBook
 * Copyright (C) 2022  Spacious Team <spacious-team@ya.ru>
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

package ru.investbook.parser.psb;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.spacious_team.broker.pojo.CashFlowType;
import org.spacious_team.broker.pojo.Security;
import org.spacious_team.broker.pojo.SecurityEventCashFlow;
import org.spacious_team.table_wrapper.api.TableColumn;
import org.spacious_team.table_wrapper.api.TableColumnDescription;
import org.spacious_team.table_wrapper.api.TableColumnImpl;
import org.spacious_team.table_wrapper.api.TableRow;
import ru.investbook.parser.SingleAbstractReportTable;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;

import static ru.investbook.parser.psb.CouponAmortizationRedemptionTable.CouponAndAmortizationTableHeader.*;

@Slf4j
public class CouponAmortizationRedemptionTable extends SingleAbstractReportTable<SecurityEventCashFlow> {

    private static final String TABLE_NAME = "Погашение купонов и ЦБ";
    private static final String TABLE_END_TEXT = "*Налог удерживается с рублевого брокерского счета";
    private static final BigDecimal minValue = BigDecimal.valueOf(0.01);

    public CouponAmortizationRedemptionTable(PsbBrokerReport report) {
        super(report, TABLE_NAME, TABLE_END_TEXT, CouponAndAmortizationTableHeader.class);
    }

    @Override
    protected Collection<SecurityEventCashFlow> parseRowToCollection(TableRow row) {
        CashFlowType event;
        String action = row.getStringCellValue(TYPE);
        if (action.equalsIgnoreCase("Погашение купона")) {
            event = CashFlowType.COUPON;
        } else if (action.equalsIgnoreCase("Амортизация")) {
            event = CashFlowType.AMORTIZATION;
        } else if (action.equalsIgnoreCase("Погашение бумаг")) {
            event = CashFlowType.REDEMPTION;
        } else {
            throw new RuntimeException("Обработчик события " + action + " не реализован");
        }

        BigDecimal value = ((event == CashFlowType.COUPON) ?
                row.getBigDecimalCellValue(COUPON) :
                row.getBigDecimalCellValue(VALUE));
        BigDecimal tax = row.getBigDecimalCellValue(TAX).negate();

        String isin = row.getStringCellValue(ISIN);
        int securityId = getReport().getSecurityRegistrar().declareBondByIsin(isin, () -> Security.builder()
                .isin(isin)
                .name(row.getStringCellValue(BOND_NAME)));

        SecurityEventCashFlow.SecurityEventCashFlowBuilder builder = SecurityEventCashFlow.builder()
                .security(securityId)
                .portfolio(getReport().getPortfolio())
                .count(row.getIntCellValue(COUNT))
                .eventType(event)
                .timestamp(convertToInstant(row.getStringCellValue(DATE)))
                .value(value)
                .currency(row.getStringCellValue(CURRENCY));
        Collection<SecurityEventCashFlow> data = new ArrayList<>();
        data.add(builder.build());
        if (tax.abs().compareTo(minValue) >= 0) {
            data.add(builder.eventType(CashFlowType.TAX).value(tax).build());
        }
        return data;
    }

    enum CouponAndAmortizationTableHeader implements TableColumnDescription {
        DATE("дата"),
        TYPE("вид операции"),
        BOND_NAME("наименование"),
        ISIN("isin"),
        COUNT("кол-во"),
        COUPON("нкд"),
        VALUE("сумма амортизации"),
        TAX("удержанного налога"),
        CURRENCY("валюта выплаты");

        @Getter
        private final TableColumn column;

        CouponAndAmortizationTableHeader(String... words) {
            this.column = TableColumnImpl.of(words);
        }
    }
}
