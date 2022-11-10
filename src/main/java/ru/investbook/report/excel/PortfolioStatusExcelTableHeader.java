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

package ru.investbook.report.excel;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PortfolioStatusExcelTableHeader implements ExcelTableHeader {
    SECURITY("Бумага"),
    TYPE("Тип"),
    FIRST_TRANSACTION_DATE("Дата первой сделки"),
    LAST_TRANSACTION_DATE("Дата последней сделки"),
    BUY_COUNT("Куплено"),
    CELL_COUNT("Продано"),
    COUNT("Изменение позиции"),
    AVERAGE_PRICE("Усредненная цена"),
    AVERAGE_ACCRUED_INTEREST("Усредненный НКД"),
    COMMISSION("Комиссия"),
    LAST_EVENT_DATE("Дата последней выплаты"),
    COUPON("Выплаченные купоны"),
    AMORTIZATION("Амортизация облигации"),
    DIVIDEND("Дивиденды"),
    TAX("Налог (удержанный)"),
    LAST_PRICE("Текущая цена"),
    LAST_ACCRUED_INTEREST("Текущий НКД"),
    GROSS_PROFIT("Курсовой доход"),
    PROFIT("Прибыль"),
    INTERNAL_RATE_OF_RETURN("Доходность, %"),
    PROFIT_PROPORTION("Доля прибыли, %"),
    INVESTMENT_PROPORTION("Доля вложений, %"),
    PROPORTION("Доля в портфеле, %");

    private final String description;
}
