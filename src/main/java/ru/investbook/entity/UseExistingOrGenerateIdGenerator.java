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

package ru.investbook.entity;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentityGenerator;

import java.io.Serializable;

/**
 * {@code @GeneratedValue} doesn't work out of box for H2.
 * {@code @GeneratedValue(strategy = GenerationType.IDENTITY)} ignores entity id field set manually.
 *
 * @see <a href="https://stackoverflow.com/questions/2108178/id-generatedvalue-but-set-own-id-value">Stack Overflow</a>
 */
public class UseExistingOrGenerateIdGenerator extends IdentityGenerator {
    static final String NAME = "UseExistingOrGenerateIdGenerator";
    static final String STRATEGY = "ru.investbook.entity.UseExistingOrGenerateIdGenerator";

    @Override
    public Serializable generate(SharedSessionContractImplementor session, Object object) throws HibernateException {
        Serializable id = session.getEntityPersister(null, object)
                .getClassMetadata()
                .getIdentifier(object, session);
        return (id == null) ? super.generate(session, object) : id;
    }
}
