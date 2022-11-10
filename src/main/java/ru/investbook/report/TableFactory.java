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

package ru.investbook.report;

import org.spacious_team.broker.pojo.Portfolio;

import java.util.Collection;

public interface TableFactory {

    default Table create() {
        throw new UnsupportedOperationException("Not implemented");
    }

    Table create(Portfolio portfolio);

    default Table create(Collection<String> portfolioIds) {
        throw new UnsupportedOperationException("Not implemented");
    }

    default Table create(Collection<String> portfolioIds, String forCurrency) {
        throw new UnsupportedOperationException("Not implemented");
    }

    default Table create(Portfolio portfolio, String forCurrency) {
        throw new UnsupportedOperationException("Not implemented");
    }

}
